// Chunk capture lifecycle and build admission.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <chrono>
#include <iostream>
#include <sstream>

namespace mcrtx {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;

namespace {

std::uint64_t toNanoseconds(std::chrono::steady_clock::duration duration) {
  return static_cast<std::uint64_t>(std::chrono::duration_cast<std::chrono::nanoseconds>(duration).count());
}

}  // namespace

bool RemixRenderer::beginChunkBuild(
  int originX,
  int originY,
  int originZ,
  int sizeX,
  int sizeY,
  int sizeZ,
  int dirtyMinX,
  int dirtyMinY,
  int dirtyMinZ,
  int dirtyMaxX,
  int dirtyMaxY,
  int dirtyMaxZ,
  int renderPass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginChunkBuild");
  MCRTX_TRACY_SCOPE("RemixRenderer::beginChunkBuild");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return false;
  }

  if (renderPass != 0 && renderPass != 1) {
    return false;
  }

  chunkBuildActive_ = true;
  activeChunkBuild_ = {};
  activeChunkBuild_.origin[0] = originX;
  activeChunkBuild_.origin[1] = originY;
  activeChunkBuild_.origin[2] = originZ;
  activeChunkBuild_.size[0] = sizeX;
  activeChunkBuild_.size[1] = sizeY;
  activeChunkBuild_.size[2] = sizeZ;
  activeChunkBuild_.dirtyMin[0] = dirtyMinX;
  activeChunkBuild_.dirtyMin[1] = dirtyMinY;
  activeChunkBuild_.dirtyMin[2] = dirtyMinZ;
  activeChunkBuild_.dirtyMax[0] = dirtyMaxX;
  activeChunkBuild_.dirtyMax[1] = dirtyMaxY;
  activeChunkBuild_.dirtyMax[2] = dirtyMaxZ;
  activeChunkBuild_.renderPass = renderPass;
  activeChunkBlocks_.clear();
  return true;
}

void RemixRenderer::captureBlock(
  int blockX,
  int blockY,
  int blockZ,
  int blockId,
  int blockMetadata,
  int renderType,
  int texture0,
  int texture1,
  int texture2,
  int texture3,
  int texture4,
  int texture5,
  float boundsMinX,
  float boundsMinY,
  float boundsMinZ,
  float boundsMaxX,
  float boundsMaxY,
  float boundsMaxZ,
  int blockColorRgb,
  int liquidVisibilityMask,
  float liquidHeight0,
  float liquidHeight1,
  float liquidHeight2,
  float liquidHeight3,
  float liquidFlowAngle) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureBlock");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !chunkBuildActive_) {
    return;
  }

  ++perfCaptureBlockCallsThisFrame_;
  ++activeChunkBuild_.blockCount;
  ++capturedBlocks_;
  if (blockId >= 0 && blockId < static_cast<int>(activeChunkBuild_.blockIdCounts.size())) {
    ++activeChunkBuild_.blockIdCounts[static_cast<std::size_t>(blockId)];
  }

  if (!shouldCaptureBlock(blockId, renderType, activeChunkBuild_.renderPass) || activeChunkBlocks_.size() >= kMaxOpaqueBlocksPerChunk) {
    return;
  }

  CapturedBlockInstance block {};
  block.position[0] = blockX;
  block.position[1] = blockY;
  block.position[2] = blockZ;
  block.blockId = blockId;
  block.blockMetadata = blockMetadata;
  block.terrainTiles = {
      static_cast<std::int16_t>(texture0),
      static_cast<std::int16_t>(texture1),
      static_cast<std::int16_t>(texture2),
      static_cast<std::int16_t>(texture3),
      static_cast<std::int16_t>(texture4),
      static_cast<std::int16_t>(texture5),
  };
  block.renderType = renderType;
  block.materialClass = materialClassForBlock(blockId, blockMetadata, renderType);
    block.bounds = {boundsMinX, boundsMinY, boundsMinZ, boundsMaxX, boundsMaxY, boundsMaxZ};
  block.liquidVisibilityMask = static_cast<std::uint8_t>(liquidVisibilityMask & 0x3F);
  block.liquidHeights = {liquidHeight0, liquidHeight1, liquidHeight2, liquidHeight3};
  block.liquidFlowAngle = liquidFlowAngle;
  block.blockColor = static_cast<std::uint32_t>(blockColorRgb) & 0x00FFFFFFu;
  activeChunkBlocks_.push_back(block);
}

void RemixRenderer::endChunkBuild(bool emittedGeometry, bool deferNeighborRefresh, bool allowNeighborRefresh) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endChunkBuild");
  MCRTX_TRACY_SCOPE("RemixRenderer::endChunkBuild");
  std::scoped_lock lock(mutex_);
  const auto buildStart = std::chrono::steady_clock::now();

  if (!chunkBuildActive_) {
    return;
  }

  const ChunkKey chunkKey = makeChunkKey(activeChunkBuild_);
  const bool partialChunkBuild = activeChunkBuild_.dirtyMin[0] != activeChunkBuild_.origin[0]
      || activeChunkBuild_.dirtyMin[1] != activeChunkBuild_.origin[1]
      || activeChunkBuild_.dirtyMin[2] != activeChunkBuild_.origin[2]
      || activeChunkBuild_.dirtyMax[0] != activeChunkBuild_.origin[0] + activeChunkBuild_.size[0] - 1
      || activeChunkBuild_.dirtyMax[1] != activeChunkBuild_.origin[1] + activeChunkBuild_.size[1] - 1
      || activeChunkBuild_.dirtyMax[2] != activeChunkBuild_.origin[2] + activeChunkBuild_.size[2] - 1;
  MCRTX_TRACY_VALUE(activeChunkBuild_.blockCount);

  // When deferring, enqueue neighbor keys into a set so multiple sibling
  // rebuilds in the same flush collapse into one refresh per unique neighbor.
  // Also mark the rebuilt chunk as "recently rebuilt" so a subsequent sibling
  // doesn't schedule a redundant refresh of an already-fresh mesh.
  const auto scheduleNeighbors = [this, &chunkKey]() {
    recentlyRebuiltChunks_.insert(chunkKey);
    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      ChunkKey neighborKey = chunkKey;
      neighborKey.originX += kNeighborOffsets[faceIndex][0] * kChunkDimension;
      neighborKey.originY += kNeighborOffsets[faceIndex][1] * kChunkDimension;
      neighborKey.originZ += kNeighborOffsets[faceIndex][2] * kChunkDimension;
      pendingNeighborRefresh_.insert(neighborKey);
    }
  };

  std::uint64_t rebuildNanos = 0;
  std::uint64_t neighborRefreshNanos = 0;
  bool rebuiltChunkMesh = false;
  if ((emittedGeometry && !activeChunkBlocks_.empty()) || partialChunkBuild) {
    auto [chunkIt, inserted] = chunkMeshes_.try_emplace(chunkKey);
    (void)inserted;
    ChunkMeshData& meshData = chunkIt->second;
    const auto rebuildStart = std::chrono::steady_clock::now();
    {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endChunkBuild.rebuildChunkMesh");
      MCRTX_TRACY_SCOPE("endChunkBuild.rebuildChunkMesh");
      MCRTX_TRACY_VALUE(activeChunkBlocks_.size());
      if (!rebuildChunkMesh(activeChunkBuild_, activeChunkBlocks_, meshData)) {
        activeChunkBlocks_.clear();
        chunkBuildActive_ = false;
        activeChunkBuild_ = {};
        return;
      }
    }
    if (!meshData.hasOccupancy && meshData.meshHandle == nullptr && meshData.blockCount == 0) {
      chunkMeshes_.erase(chunkIt);
    }
    rebuildNanos = toNanoseconds(std::chrono::steady_clock::now() - rebuildStart);
    rebuiltChunkMesh = true;
    const auto neighborRefreshStart = std::chrono::steady_clock::now();
    if (allowNeighborRefresh) {
      if (deferNeighborRefresh) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endChunkBuild.deferNeighborRefresh");
        MCRTX_TRACY_SCOPE("endChunkBuild.deferNeighborRefresh");
        scheduleNeighbors();
      } else {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endChunkBuild.refreshNeighborRefresh");
        MCRTX_TRACY_SCOPE("endChunkBuild.refreshNeighborRefresh");
        refreshNeighborChunkMeshes(chunkKey);
      }
    }
    neighborRefreshNanos = toNanoseconds(std::chrono::steady_clock::now() - neighborRefreshStart);
  } else {
    auto chunkIt = chunkMeshes_.find(chunkKey);
    if (chunkIt != chunkMeshes_.end()) {
      destroyChunkMesh(chunkIt->second);
      chunkMeshes_.erase(chunkIt);
      const auto neighborRefreshStart = std::chrono::steady_clock::now();
      if (allowNeighborRefresh) {
        if (deferNeighborRefresh) {
          MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endChunkBuild.deferNeighborRefresh");
          MCRTX_TRACY_SCOPE("endChunkBuild.deferNeighborRefresh");
          scheduleNeighbors();
        } else {
          MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endChunkBuild.refreshNeighborRefresh");
          MCRTX_TRACY_SCOPE("endChunkBuild.refreshNeighborRefresh");
          refreshNeighborChunkMeshes(chunkKey);
        }
      }
      neighborRefreshNanos = toNanoseconds(std::chrono::steady_clock::now() - neighborRefreshStart);
    }
  }

  perfChunkBuildsThisFrame_ += 1;
  perfChunkBuildWorkNanosThisFrame_ += toNanoseconds(std::chrono::steady_clock::now() - buildStart);
  perfChunkMeshRebuildNanosThisFrame_ += rebuildNanos;
  perfNeighborRefreshNanosThisFrame_ += neighborRefreshNanos;
  if (rebuiltChunkMesh) {
    perfChunkMeshRebuildsThisFrame_ += 1;
  }

  ++capturedChunkBuilds_;
  const bool shouldLog = capturedChunkBuilds_ <= 8 || capturedChunkBuilds_ % 128 == 0;
  if (shouldLog && isVerboseLoggingEnabled()) {
    std::ostringstream stream;
    stream << "chunk build origin=("
           << activeChunkBuild_.origin[0] << ", "
           << activeChunkBuild_.origin[1] << ", "
           << activeChunkBuild_.origin[2] << ") size=("
           << activeChunkBuild_.size[0] << ", "
           << activeChunkBuild_.size[1] << ", "
           << activeChunkBuild_.size[2] << ") pass="
           << activeChunkBuild_.renderPass
           << " blocks=" << activeChunkBuild_.blockCount
           << " capturedOpaqueBlocks=" << activeChunkBlocks_.size()
           << " uniqueBlockIds=" << countUniqueBlockIds(activeChunkBuild_)
           << " emittedGeometry=" << (emittedGeometry ? "true" : "false")
           << " storedChunks=" << chunkMeshes_.size()
           << " totalCapturedBlocks=" << capturedBlocks_;
    log(stream.str());
  }

  chunkBuildActive_ = false;
  activeChunkBuild_ = {};
  activeChunkBlocks_.clear();
}
}  // namespace mcrtx
