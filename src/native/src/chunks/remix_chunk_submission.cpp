#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <chrono>
#include <sstream>
#include <unordered_set>

namespace mcrtx {
namespace {
using Positions = std::unordered_set<WorldBlockPosition, WorldBlockPositionHash>;

template<typename Map>
Map captureEntries(const Map& source, const Positions& positions) {
  Map result;
  for (const auto& position : positions) {
    const auto it = source.find(position);
    if (it != source.end()) {
      result.insert(*it);
    }
  }
  return result;
}

template<typename Map>
void restoreEntries(Map& target, Map& backup, const Positions& positions) {
  for (const auto& position : positions) {
    target.erase(position);
  }
  target.merge(backup);
}

void addHandles(std::unordered_set<remixapi_LightHandle>& handles, const TorchLightState& state) {
  handles.insert(state.handle);
}
void addHandles(std::unordered_set<remixapi_LightHandle>& handles, const PortalLightState& state) {
  handles.insert(state.handleFront);
  handles.insert(state.handleBack);
}
void addHandles(std::unordered_set<remixapi_LightHandle>& handles, const GlowstoneLightState& state) {
  handles.insert(state.handles.begin(), state.handles.end());
}
}

bool RemixRenderer::createTerrainMesh(const ChunkKey& key, const ChunkGeometryBuild& build,
                                     const ChunkMeshData& previous, ChunkMeshData& next) {
  next.meshFingerprint = build.fingerprint;
  for (const auto& surface : build.surfacesToBuild) {
    next.triangleCount += surface.indices.size() / 3;
  }
  if (previous.meshFingerprint == build.fingerprint) {
    next.meshHandle = previous.meshHandle;
    next.meshHash = previous.meshHash;
    return true;
  }
  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  for (const auto& source : build.surfacesToBuild) {
    if (source.indices.empty()) {
      continue;
    }
    remixapi_MeshInfoSurfaceTriangles surface {};
    surface.vertices_values = source.vertices.data();
    surface.vertices_count = source.vertices.size();
    surface.indices_values = source.indices.data();
    surface.indices_count = source.indices.size();
    surface.material = terrainMaterialHandles_[source.materialClass];
    if (surface.material == nullptr) {
      return false;
    }
    surfaces.push_back(surface);
  }
  if (surfaces.empty()) {
    return true;
  }
  remixapi_MeshInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  info.hash = chunk::makeChunkMeshHash(key, build.fingerprint);
  info.surfaces_values = surfaces.data();
  info.surfaces_count = static_cast<std::uint32_t>(surfaces.size());
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.terrain");
  if (remix_.CreateMesh(&info, &next.meshHandle) != REMIXAPI_ERROR_CODE_SUCCESS || next.meshHandle == nullptr) {
    return false;
  }
  // Remix handles are hashes; retrying can reuse a failed transaction's handle.
  deferredMeshDestroys_.erase(std::remove(deferredMeshDestroys_.begin(), deferredMeshDestroys_.end(), next.meshHandle),
                             deferredMeshDestroys_.end());
  ++terrainMeshCreates_;
  next.meshHash = info.hash;
  return true;
}

bool RemixRenderer::publishTerrainSection(const TerrainResult& result) {
  std::array<ChunkMeshData, 2> next;
  std::array<ChunkMeshData*, 2> previous;
  auto key = result.inputs.key;
  for (int pass = 0; pass < 2; ++pass) {
    key.renderPass = pass;
    previous[pass] = &chunkMeshes_[key];
    next[pass].section = result.inputs.sections[13];
    next[pass].lifetime = result.inputs.lifetime;
    next[pass].blockCount = result.blockCounts[pass];
    next[pass].torchLights = previous[pass]->torchLights;
    next[pass].portalLights = previous[pass]->portalLights;
    next[pass].glowstoneLights = previous[pass]->glowstoneLights;
  }
  const auto discardMeshes = [&] {
    for (int pass = 0; pass < 2; ++pass) {
      if (next[pass].meshHandle != previous[pass]->meshHandle) {
        destroyMeshHandle(next[pass].meshHandle);
      }
    }
  };
  for (int pass = 0; pass < 2; ++pass) {
    key.renderPass = pass;
    if (!createTerrainMesh(key, result.passes[pass], *previous[pass], next[pass])) {
      discardMeshes();
      return false;
    }
  }

  Positions positions;
  const auto addPositions = [&](const auto& placements) {
    for (const auto& placement : placements) {
      positions.insert(placement.blockPosition);
    }
  };
  for (int pass = 0; pass < 2; ++pass) {
    addPositions(previous[pass]->torchLights);
    addPositions(previous[pass]->portalLights);
    addPositions(previous[pass]->glowstoneLights);
    addPositions(result.passes[pass].desiredTorchLights);
    addPositions(result.passes[pass].desiredPortalLights);
    addPositions(result.passes[pass].desiredGlowstoneLights);
  }
  auto torchBackup = captureEntries(torchLights_, positions);
  auto portalBackup = captureEntries(portalLights_, positions);
  auto glowstoneBackup = captureEntries(glowstoneLights_, positions);
  auto torchPlacementBackup = captureEntries(torchLightPlacements_, positions);
  auto portalPlacementBackup = captureEntries(portalLightPlacements_, positions);
  auto glowstonePlacementBackup = captureEntries(glowstoneLightPlacements_, positions);
  std::unordered_set<remixapi_LightHandle> oldHandles;
  for (const auto& [position, state] : torchBackup) { addHandles(oldHandles, state); }
  for (const auto& [position, state] : portalBackup) { addHandles(oldHandles, state); }
  for (const auto& [position, state] : glowstoneBackup) { addHandles(oldHandles, state); }
  std::vector<remixapi_LightHandle> retired;
  terrainRetiredLights_ = &retired;
  bool ready = true;
  for (int pass = 0; pass < 2 && ready; ++pass) {
    ready = reconcileChunkTorchLights(next[pass], result.passes[pass].desiredTorchLights)
         && reconcileChunkPortalLights(next[pass], result.passes[pass].desiredPortalLights)
         && reconcileChunkGlowstoneLights(next[pass], result.passes[pass].desiredGlowstoneLights);
  }
  terrainRetiredLights_ = nullptr;
  bool committed = false;
  if (ready) {
    // The pipeline lock spans validation and both swaps, never Remix allocation.
    committed = terrain_.finish(result, [&] {
      for (int pass = 0; pass < 2; ++pass) {
        std::swap(*previous[pass], next[pass]);
      }
    });
  }
  if (committed) {
    discardMeshes();
    for (auto handle : retired) {
      destroyLightHandle(handle);
    }
    terrainSubmissionsDirty_ = true;
    ++terrainPublications_;
  } else {
    std::unordered_set<remixapi_LightHandle> discard(retired.begin(), retired.end());
    const auto collect = [&](const auto& map) {
      for (const auto& position : positions) {
        const auto it = map.find(position);
        if (it != map.end()) { addHandles(discard, it->second); }
      }
    };
    collect(torchLights_);
    collect(portalLights_);
    collect(glowstoneLights_);
    restoreEntries(torchLights_, torchBackup, positions);
    restoreEntries(portalLights_, portalBackup, positions);
    restoreEntries(glowstoneLights_, glowstoneBackup, positions);
    restoreEntries(torchLightPlacements_, torchPlacementBackup, positions);
    restoreEntries(portalLightPlacements_, portalPlacementBackup, positions);
    restoreEntries(glowstoneLightPlacements_, glowstonePlacementBackup, positions);
    for (auto handle : discard) {
      if (!oldHandles.count(handle)) {
        destroyLightHandle(handle);
      }
    }
    discardMeshes();
  }
  // Obsolete work was consumed by finish; only allocation failure needs a retry.
  return ready;
}

void RemixRenderer::publishTerrain() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "terrain.publish");
  using Clock = std::chrono::steady_clock;
  for (auto key : terrain_.takeResidencyChanges()) {
    const auto lifetime = terrain_.lifetime(key);
    for (int pass = 0; pass < 2; ++pass) {
      key.renderPass = pass;
      const auto it = chunkMeshes_.find(key);
      if (it != chunkMeshes_.end() && it->second.lifetime != lifetime) {
        destroyChunkMesh(it->second);
        chunkMeshes_.erase(it);
      }
    }
  }
  terrain_.pump({camera_.position[0], camera_.position[1], camera_.position[2]});
  const auto start = Clock::now();
  std::size_t sections = 0, bytes = 0;
  while (sections < 4 && bytes < 8 * 1024 * 1024 && Clock::now() - start < std::chrono::milliseconds(1)) {
    auto result = terrain_.takeCompleted();
    if (!result) {
      break;
    }
    bytes += result->bytes;
    ++sections;
    if (!publishTerrainSection(*result)) {
      terrain_.retry(std::move(result));
      break;
    }
  }
  const auto duration = Clock::now() - start;
  terrainPublicationNanos_ += std::chrono::duration_cast<std::chrono::nanoseconds>(duration).count();
  if (duration > std::chrono::milliseconds(1) || bytes > 8 * 1024 * 1024) {
    ++terrainPublicationOverruns_;
  }
  if (presentedFrames_ % 120 == 0) {
    const auto stats = terrain_.statistics();
    std::ostringstream stream;
    stream << "Terrain: resident=" << stats.resident << " published=" << stats.published << " pending=" << stats.pending
           << " dispatched=" << stats.dispatched << " completed=" << stats.completed
           << " completedBytes=" << stats.completedBytes << " canonicalBytes=" << stats.canonicalBytes
           << " oldestNs=" << stats.oldestNanos << " captures=" << stats.captures << " unchanged=" << stats.unchanged
           << " builds=" << stats.builds << " stale=" << stats.stale << " failures=" << stats.failures
           << " workerNs=" << stats.workerNanos << " lockNs=" << stats.lockNanos
           << " publications=" << terrainPublications_ << " publishNs=" << terrainPublicationNanos_
           << " overruns=" << terrainPublicationOverruns_ << " meshCreates=" << terrainMeshCreates_
           << " triangles=" << terrainTriangles_;
    log(stream.str());
  }
}
} // namespace mcrtx
