// Chunk mesh residency, coverage, eviction, and neighbor refresh.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;

namespace {

// Per-present ceiling on deferred neighbor mesh rebuilds. The section-scan
// budget on the Java side bounds primary recapture work, but each rebuilt
// section schedules up to six neighbor refreshes; draining the entire pending
// set in one present can still stall the present thread. Cap the rebuilds per
// flush and carry leftovers to the next frame. The budget escalates once the
// pending set grows past a backlog threshold so reveals still settle promptly.
constexpr std::size_t kSteadyNeighborRefreshBudget = 8;
constexpr std::size_t kBacklogNeighborRefreshBudget = 24;
constexpr std::size_t kNeighborRefreshBacklogThreshold = 64;

}  // namespace

void RemixRenderer::destroyChunkMeshHandle(ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyChunkMeshHandle");
  destroyMeshHandle(meshData.meshHandle);
  meshData.meshHash = 0;
}
void RemixRenderer::unloadChunkSection(int originX, int originY, int originZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::unloadChunkSection");
  std::scoped_lock lock(mutex_);

  for (int renderPass = 0; renderPass <= 1; ++renderPass) {
    const ChunkKey key {originX, originY, originZ, renderPass};
    auto it = chunkMeshes_.find(key);
    if (it != chunkMeshes_.end()) {
      destroyChunkMesh(it->second);
      chunkMeshes_.erase(it);
    }
    pendingNeighborRefresh_.erase(key);
    recentlyRebuiltChunks_.erase(key);
  }
}

void RemixRenderer::setChunkSectionHidden(int originX, int originY, int originZ, bool hidden) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setChunkSectionHidden");
  std::scoped_lock lock(mutex_);

  for (int renderPass = 0; renderPass <= 1; ++renderPass) {
    const ChunkKey key {originX, originY, originZ, renderPass};
    auto it = chunkMeshes_.find(key);
    if (it != chunkMeshes_.end()) {
      it->second.hidden = hidden;
    }
  }
}

bool RemixRenderer::isChunkBuried(const ChunkKey& chunkKey) const {
  // A chunk is buried if all 6 face-adjacent opaque-pass neighbors exist and
  // each neighbor's opposite face is fully covered by solid opaque blocks.
  // Conservative: any absent neighbor or uncovered face returns false.
  for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
    ChunkKey neighborKey {
      chunkKey.originX + kNeighborOffsets[faceIndex][0] * kChunkDimension,
      chunkKey.originY + kNeighborOffsets[faceIndex][1] * kChunkDimension,
      chunkKey.originZ + kNeighborOffsets[faceIndex][2] * kChunkDimension,
      0  // opaque pass only
    };
    const auto neighborIt = chunkMeshes_.find(neighborKey);
    if (neighborIt == chunkMeshes_.end()) {
      return false;
    }
    if (neighborIt->second.meshHandle == nullptr || neighborIt->second.hidden) {
      return false;
    }
    // Opposite face index: faceIndex ^ 1 (swaps within each axis pair: 0↔1, 2↔3, 4↔5)
    if (!neighborIt->second.faceCovered[faceIndex ^ 1]) {
      return false;
    }
  }
  return true;
}

void RemixRenderer::computeFaceCoverage(ChunkMeshData& meshData) {
  // For each of the 6 face directions, scan the 16×16 border plane and set
  // faceCovered[face] = true only if every cell is a solid opaque full-cube.
  // Face index mapping matches kNeighborOffsets:
  //   0 = -Z (localZ == 0),  1 = +Z (localZ == 15)
  //   2 = -X (localX == 0),  3 = +X (localX == 15)
  //   4 = -Y (localY == 0),  5 = +Y (localY == 15)

  const auto isSolidOccluder = [&](int index) -> bool {
    if (meshData.occupancy[index] == 0) return false;
    const ChunkBlockCell& cell = meshData.cells[index];
    if (cell.renderType != kCubeBlockRenderType) return false;
    if (cell.materialClass != kOpaqueTerrainMaterialClass) return false;
    if (usesPartialCubeBounds(cell)) return false;
    return true;
  };

  // Face 0: -Z, fixed localZ = 0
  {
    bool covered = true;
    for (int localY = 0; localY < kChunkDimension && covered; ++localY) {
      for (int localX = 0; localX < kChunkDimension && covered; ++localX) {
        if (!isSolidOccluder(blockIndex(localX, localY, 0))) covered = false;
      }
    }
    meshData.faceCovered[0] = covered;
  }
  // Face 1: +Z, fixed localZ = 15
  {
    bool covered = true;
    for (int localY = 0; localY < kChunkDimension && covered; ++localY) {
      for (int localX = 0; localX < kChunkDimension && covered; ++localX) {
        if (!isSolidOccluder(blockIndex(localX, localY, kChunkDimension - 1))) covered = false;
      }
    }
    meshData.faceCovered[1] = covered;
  }
  // Face 2: -X, fixed localX = 0
  {
    bool covered = true;
    for (int localY = 0; localY < kChunkDimension && covered; ++localY) {
      for (int localZ = 0; localZ < kChunkDimension && covered; ++localZ) {
        if (!isSolidOccluder(blockIndex(0, localY, localZ))) covered = false;
      }
    }
    meshData.faceCovered[2] = covered;
  }
  // Face 3: +X, fixed localX = 15
  {
    bool covered = true;
    for (int localY = 0; localY < kChunkDimension && covered; ++localY) {
      for (int localZ = 0; localZ < kChunkDimension && covered; ++localZ) {
        if (!isSolidOccluder(blockIndex(kChunkDimension - 1, localY, localZ))) covered = false;
      }
    }
    meshData.faceCovered[3] = covered;
  }
  // Face 4: -Y, fixed localY = 0
  {
    bool covered = true;
    for (int localZ = 0; localZ < kChunkDimension && covered; ++localZ) {
      for (int localX = 0; localX < kChunkDimension && covered; ++localX) {
        if (!isSolidOccluder(blockIndex(localX, 0, localZ))) covered = false;
      }
    }
    meshData.faceCovered[4] = covered;
  }
  // Face 5: +Y, fixed localY = 15
  {
    bool covered = true;
    for (int localZ = 0; localZ < kChunkDimension && covered; ++localZ) {
      for (int localX = 0; localX < kChunkDimension && covered; ++localX) {
        if (!isSolidOccluder(blockIndex(localX, kChunkDimension - 1, localZ))) covered = false;
      }
    }
    meshData.faceCovered[5] = covered;
  }
}

void RemixRenderer::destroyChunkMesh(ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyChunkMesh");
  destroyChunkMeshHandle(meshData);
  destroyChunkTorchLights(meshData);
  meshData.meshFingerprint = 0;
  meshData.fireCellIndices.clear();
}

void RemixRenderer::evictDistantChunks(int cameraChunkX, int cameraChunkZ, int evictRadiusChunks) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::evictDistantChunks");
  // Caller holds mutex_.
  if (evictRadiusChunks <= 0) {
    return;
  }

  for (auto it = chunkMeshes_.begin(); it != chunkMeshes_.end(); ) {
    const ChunkKey& key = it->first;
    const int chunkX = key.originX / kChunkDimension;
    const int chunkZ = key.originZ / kChunkDimension;
    const int chebyshev = std::max(
        std::abs(chunkX - cameraChunkX),
        std::abs(chunkZ - cameraChunkZ));
    if (chebyshev > evictRadiusChunks) {
      destroyChunkMesh(it->second);
      pendingNeighborRefresh_.erase(key);
      recentlyRebuiltChunks_.erase(key);
      it = chunkMeshes_.erase(it);
    } else {
      ++it;
    }
  }
}

void RemixRenderer::flushChunkNeighborRefreshes() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::flushChunkNeighborRefreshes");
  MCRTX_TRACY_SCOPE("RemixRenderer::flushChunkNeighborRefreshes");
  std::scoped_lock lock(mutex_);
  if (pendingNeighborRefresh_.empty()) {
    recentlyRebuiltChunks_.clear();
    return;
  }
  MCRTX_TRACY_VALUE(pendingNeighborRefresh_.size());

  const std::size_t rebuildBudget = pendingNeighborRefresh_.size() >= kNeighborRefreshBacklogThreshold
      ? kBacklogNeighborRefreshBudget
      : kSteadyNeighborRefreshBudget;

  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::flushChunkNeighborRefreshes.iterateNeighbors");
    std::size_t rebuilds = 0;
    for (auto it = pendingNeighborRefresh_.begin(); it != pendingNeighborRefresh_.end();) {
      const ChunkKey neighborKey = *it;

      // Entries that need no rebuild cost nothing; drop them regardless of the
      // rebuild budget so the pending set drains and recentlyRebuiltChunks_ can
      // be cleared once real work is done.
      if (recentlyRebuiltChunks_.find(neighborKey) != recentlyRebuiltChunks_.end()) {
        it = pendingNeighborRefresh_.erase(it);
        continue;
      }
      const auto neighborIt = chunkMeshes_.find(neighborKey);
      if (neighborIt == chunkMeshes_.end()
          || !neighborIt->second.hasOccupancy
          || neighborIt->second.blockCount == 0) {
        it = pendingNeighborRefresh_.erase(it);
        continue;
      }

      // A genuine rebuild. Stop once the per-present budget is spent and leave
      // the remaining neighbors queued for the next frame.
      if (rebuilds >= rebuildBudget) {
        break;
      }
      if (!rebuildChunkMeshFromData(neighborKey, neighborIt->second, true)) {
        break;
      }
      ++rebuilds;
      it = pendingNeighborRefresh_.erase(it);
    }
  }

  // Preserve the dedup set across frames while neighbors remain queued so a
  // carried-over refresh doesn't redundantly rebuild an already-fresh mesh.
  if (pendingNeighborRefresh_.empty()) {
    recentlyRebuiltChunks_.clear();
  }
}

void RemixRenderer::refreshNeighborChunkMeshes(const ChunkKey& chunkKey) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::refreshNeighborChunkMeshes");
  MCRTX_TRACY_SCOPE("RemixRenderer::refreshNeighborChunkMeshes");
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::refreshNeighborChunkMeshes.iterateNeighbors");
    MCRTX_TRACY_SCOPE("refreshNeighborChunkMeshes.iterateNeighbors");
    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      ChunkKey neighborKey = chunkKey;
      neighborKey.originX += kNeighborOffsets[faceIndex][0] * kChunkDimension;
      neighborKey.originY += kNeighborOffsets[faceIndex][1] * kChunkDimension;
      neighborKey.originZ += kNeighborOffsets[faceIndex][2] * kChunkDimension;

      const auto neighborIt = chunkMeshes_.find(neighborKey);
      if (neighborIt == chunkMeshes_.end()) {
        continue;
      }

      if (!neighborIt->second.hasOccupancy || neighborIt->second.blockCount == 0) {
        continue;
      }

      if (!rebuildChunkMeshFromData(neighborKey, neighborIt->second, true)) {
        return;
      }
    }
  }
}

}  // namespace mcrtx
