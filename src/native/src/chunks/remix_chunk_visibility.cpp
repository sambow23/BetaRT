#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_build.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <cmath>
#include <limits>
#include <map>

namespace mcrtx {

std::array<std::int64_t, 5> RemixRenderer::undergroundStatistics() const {
  std::scoped_lock lock(mutex_);
  return {static_cast<std::int64_t>(undergroundVisibleGroups_), static_cast<std::int64_t>(undergroundHiddenGroups_),
    static_cast<std::int64_t>(undergroundHiddenTriangles_), static_cast<std::int64_t>(undergroundHiddenLights_),
    static_cast<std::int64_t>(undergroundPendingGroups_)};
}

void RemixRenderer::setUndergroundCullingEnabled(bool enabled) {
  std::scoped_lock lock(mutex_);
  if (undergroundFrame_.enabled != enabled) {
    undergroundFrame_.enabled = enabled;
    ++undergroundFrame_.revision;
  }
}

void RemixRenderer::resetUndergroundCulling() {
  std::scoped_lock lock(mutex_);
  undergroundFrame_.enabled = false;
  undergroundFrame_.sections = std::make_shared<UndergroundFrame::Sections>();
  ++undergroundFrame_.topologyRevision;
  ++undergroundFrame_.revision;
}

void RemixRenderer::updateUndergroundTopology(const ChunkKey& key, std::shared_ptr<const UndergroundSection> section) {
  std::scoped_lock lock(mutex_);
  if (!section) {
    if (undergroundFrame_.sections->count(key) != 0) {
      undergroundFrame_.editSections().erase(key);
      ++undergroundFrame_.topologyRevision;
      ++undergroundFrame_.revision;
    }
    return;
  }
  const auto it = undergroundFrame_.sections->find(key);
  if (it == undergroundFrame_.sections->end() || it->second->revision != section->revision) {
    undergroundFrame_.editSections()[key] = std::move(section);
    ++undergroundFrame_.topologyRevision;
    ++undergroundFrame_.revision;
  }
}

void RemixRenderer::updateUndergroundVisibility(
    const ChunkKey& key, std::uint64_t revision, const std::array<std::uint64_t, 64>& hidden) {
  std::scoped_lock lock(mutex_);
  const auto it = undergroundFrame_.sections->find(key);
  if (it == undergroundFrame_.sections->end() || it->second->revision != revision || it->second->hiddenPockets == hidden) {
    return;
  }
  auto section = std::make_shared<UndergroundSection>(*it->second);
  section->hiddenPockets = hidden;
  undergroundFrame_.editSections()[key] = std::move(section);
  ++undergroundFrame_.revision;
}

void RemixRenderer::destroyUndergroundMeshes(ChunkMeshData& meshData) {
  for (auto& group : meshData.visibilityGroups) {
    destroyMeshHandle(group.handle);
  }
  meshData.visibilityGroups.clear();
  meshData.visibilityGeometryFingerprint = 0;
}

bool RemixRenderer::rebuildUndergroundMeshes(const ChunkKey& key, ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildUndergroundMeshes");
  if (renderSubmissionInFlight_) {
    return false;
  }
  ChunkGeometryBuild build;
  emitChunkGeometry(key, meshData, build);
  using Surfaces = std::map<std::uintptr_t, geometry::SurfaceBuildBuffers>;
  std::map<std::vector<UndergroundPocket>, Surfaces> groups;
  for (const auto& source : build.surfacesToBuild) {
    for (std::size_t index = 0; index + 2 < source.indices.size(); index += 3) {
      std::array<double, 3> min {INFINITY, INFINITY, INFINITY}, max {-INFINITY, -INFINITY, -INFINITY};
      const int origin[] = {key.originX, key.originY, key.originZ};
      for (int corner = 0; corner < 3; ++corner) {
        const auto& p = source.vertices[source.indices[index + corner]].position;
        const double value[] = {p[0], p[1], p[2]};
        for (int axis = 0; axis < 3; ++axis) {
          min[axis] = std::min(min[axis], origin[axis] + value[axis] - 0.01);
          max[axis] = std::max(max[axis], origin[axis] + value[axis] + 0.01);
        }
      }
      auto owners = activeUndergroundFrame_.findPockets(min, max);
      // Overflow shares an always-retained group rather than dropping geometry.
      if (groups.size() >= 31 && groups.find(owners) == groups.end()) {
        owners.clear();
      }
      auto& surface = groups[owners][reinterpret_cast<std::uintptr_t>(source.materialHandle)];
      surface.materialHandle = source.materialHandle;
      for (int corner = 0; corner < 3; ++corner) {
        surface.indices.push_back(static_cast<std::uint32_t>(surface.vertices.size()));
        surface.vertices.push_back(source.vertices[source.indices[index + corner]]);
      }
    }
  }
  std::vector<UndergroundMeshGroup> nextGroups;
  for (auto& [owners, materials] : groups) {
    std::vector<geometry::SurfaceBuildBuffers> buffers;
    for (auto& [material, surface] : materials) {
      (void)material;
      buffers.push_back(std::move(surface));
    }
    UndergroundMeshGroup next;
    next.pockets = owners;
    next.hash = chunk::makeChunkMeshHash(key, chunk::computeChunkMeshFingerprint(buffers) ^ 0x554E44455247524Full);
    const auto previous = std::find_if(meshData.visibilityGroups.begin(), meshData.visibilityGroups.end(),
      [&](const auto& group) { return group.hash == next.hash; });
    std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
    for (const auto& buffer : buffers) {
      remixapi_MeshInfoSurfaceTriangles surface {};
      surface.vertices_values = buffer.vertices.data();
      surface.vertices_count = buffer.vertices.size();
      surface.indices_values = buffer.indices.data();
      surface.indices_count = buffer.indices.size();
      surface.material = buffer.materialHandle;
      surfaces.push_back(surface);
      next.triangleCount += buffer.indices.size() / 3;
    }
    if (previous != meshData.visibilityGroups.end()) {
      next.handle = previous->handle;
    } else {
      remixapi_MeshInfo info {};
      info.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
      info.hash = next.hash;
      info.surfaces_values = surfaces.data();
      info.surfaces_count = static_cast<std::uint32_t>(surfaces.size());
      if (remix_.CreateMesh(&info, &next.handle) != REMIXAPI_ERROR_CODE_SUCCESS) {
        for (auto& created : nextGroups) {
          if (std::none_of(meshData.visibilityGroups.begin(), meshData.visibilityGroups.end(),
              [&](const auto& old) { return old.handle == created.handle; })) {
            destroyMeshHandle(created.handle);
          }
        }
        return false;
      }
    }
    nextGroups.push_back(std::move(next));
  }
  for (auto& old : meshData.visibilityGroups) {
    if (std::none_of(nextGroups.begin(), nextGroups.end(), [&](const auto& next) { return old.handle == next.handle; })) {
      destroyMeshHandle(old.handle);
    }
  }
  meshData.visibilityGroups = std::move(nextGroups);
  meshData.visibilityGeometryFingerprint = meshData.meshFingerprint;
  meshData.visibilityTopologyFingerprint = activeUndergroundFrame_.topologyFingerprint(key);
  meshData.visibilityCheckedTopology = activeUndergroundFrame_.topologyRevision;
  meshData.visibilityCurrent = true;
  meshData.visibilityCheckedFrame = ~std::uint64_t {0};
  return true;
}

} // namespace mcrtx
