// Captured-block occupancy ingestion and partial chunk merging.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <array>
#include <cstddef>
#include <cstdint>
#include <vector>

namespace mcrtx {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
bool RemixRenderer::rebuildChunkMesh(
    const ChunkBuildState& chunkBuild,
    const std::vector<CapturedBlockInstance>& blocks,
    ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildChunkMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildChunkMesh");
  const ChunkKey chunkKey = makeChunkKey(chunkBuild);
  const bool partialChunkBuild = chunkBuild.dirtyMin[0] != chunkBuild.origin[0]
      || chunkBuild.dirtyMin[1] != chunkBuild.origin[1]
      || chunkBuild.dirtyMin[2] != chunkBuild.origin[2]
      || chunkBuild.dirtyMax[0] != chunkBuild.origin[0] + chunkBuild.size[0] - 1
      || chunkBuild.dirtyMax[1] != chunkBuild.origin[1] + chunkBuild.size[1] - 1
      || chunkBuild.dirtyMax[2] != chunkBuild.origin[2] + chunkBuild.size[2] - 1;
  MCRTX_TRACY_VALUE(blocks.size());

  if (blocks.empty() && !partialChunkBuild) {
    destroyChunkMesh(meshData);
    meshData.geometryFingerprint = 0;
    meshData.meshFingerprint = 0;
    meshData.blockCount = 0;
    meshData.occupancy.fill(0);
    meshData.cells.fill(ChunkBlockCell {});
    meshData.hasOccupancy = false;
    return true;
  }

  std::array<std::uint8_t, kBlocksPerChunk> occupancy {};
  std::array<ChunkBlockCell, kBlocksPerChunk> cells {};
  if (partialChunkBuild && meshData.hasOccupancy) {
    MCRTX_TRACY_SCOPE("rebuildChunkMesh.mergeDirtyRegion");
    occupancy = meshData.occupancy;
    cells = meshData.cells;

    const int dirtyMinLocalX = std::max(0, std::min(kChunkDimension - 1, chunkBuild.dirtyMin[0] - chunkKey.originX));
    const int dirtyMinLocalY = std::max(0, std::min(kChunkDimension - 1, chunkBuild.dirtyMin[1] - chunkKey.originY));
    const int dirtyMinLocalZ = std::max(0, std::min(kChunkDimension - 1, chunkBuild.dirtyMin[2] - chunkKey.originZ));
    const int dirtyMaxLocalX = std::max(0, std::min(kChunkDimension - 1, chunkBuild.dirtyMax[0] - chunkKey.originX));
    const int dirtyMaxLocalY = std::max(0, std::min(kChunkDimension - 1, chunkBuild.dirtyMax[1] - chunkKey.originY));
    const int dirtyMaxLocalZ = std::max(0, std::min(kChunkDimension - 1, chunkBuild.dirtyMax[2] - chunkKey.originZ));

    for (int localY = dirtyMinLocalY; localY <= dirtyMaxLocalY; ++localY) {
      for (int localZ = dirtyMinLocalZ; localZ <= dirtyMaxLocalZ; ++localZ) {
        for (int localX = dirtyMinLocalX; localX <= dirtyMaxLocalX; ++localX) {
          const int occupancyIndex = blockIndex(localX, localY, localZ);
          occupancy[occupancyIndex] = 0;
          cells[occupancyIndex] = ChunkBlockCell {};
        }
      }
    }
  }

  {
    MCRTX_TRACY_SCOPE("rebuildChunkMesh.copyCapturedBlocks");
    for (const CapturedBlockInstance& block : blocks) {
      const int localX = block.position[0] - chunkKey.originX;
      const int localY = block.position[1] - chunkKey.originY;
      const int localZ = block.position[2] - chunkKey.originZ;
      if (localX < 0 || localX >= kChunkDimension
          || localY < 0 || localY >= kChunkDimension
          || localZ < 0 || localZ >= kChunkDimension) {
        continue;
      }
      const int occupancyIndex = blockIndex(localX, localY, localZ);
      occupancy[occupancyIndex] = 1;
      cells[occupancyIndex].terrainTiles = block.terrainTiles;
      cells[occupancyIndex].materialClass = block.materialClass < kTerrainMaterialClassCount
          ? block.materialClass
          : kOpaqueTerrainMaterialClass;
      cells[occupancyIndex].blockId = static_cast<std::uint8_t>(block.blockId & 0xFF);
      cells[occupancyIndex].blockMetadata = static_cast<std::uint8_t>(block.blockMetadata & 0xFF);
      cells[occupancyIndex].renderType = static_cast<std::uint8_t>(block.renderType & 0xFF);
      cells[occupancyIndex].bounds = block.bounds;
      cells[occupancyIndex].liquidVisibilityMask = block.liquidVisibilityMask;
      cells[occupancyIndex].liquidHeights = block.liquidHeights;
      cells[occupancyIndex].liquidFlowAngle = block.liquidFlowAngle;
      cells[occupancyIndex].blockColor = block.blockColor;
    }
  }

  std::vector<std::uint16_t> fireCellIndices;
  fireCellIndices.reserve(16);
  std::size_t occupiedBlocks = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildChunkMesh.scanOccupancy");
    for (int occupancyIndex = 0; occupancyIndex < kBlocksPerChunk; ++occupancyIndex) {
      if (occupancy[occupancyIndex] == 0) {
        continue;
      }
      ++occupiedBlocks;
      if (isFireRenderType(cells[occupancyIndex].renderType)) {
        fireCellIndices.push_back(static_cast<std::uint16_t>(occupancyIndex));
      }
    }
  }
  MCRTX_TRACY_VALUE(occupiedBlocks);

  if (occupiedBlocks == 0) {
    destroyChunkMesh(meshData);
    meshData.geometryFingerprint = 0;
    meshData.meshFingerprint = 0;
    meshData.blockCount = 0;
    meshData.occupancy.fill(0);
    meshData.cells.fill(ChunkBlockCell {});
    meshData.fireCellIndices.clear();
    meshData.hasOccupancy = false;
    return true;
  }

  const std::uint64_t geometryFingerprint = computeChunkFingerprint(occupancy, cells);
  if (meshData.blockCount != occupiedBlocks
      || meshData.geometryFingerprint != geometryFingerprint
      || !meshData.hasOccupancy) {
    meshData.geometryFingerprint = geometryFingerprint;
    meshData.blockCount = occupiedBlocks;
    meshData.occupancy = occupancy;
    meshData.cells = cells;
  }
  meshData.fireCellIndices = std::move(fireCellIndices);
  meshData.hasOccupancy = true;

  {
    MCRTX_TRACY_SCOPE("rebuildChunkMesh.rebuildChunkMeshFromData");
    return rebuildChunkMeshFromData(chunkKey, meshData, false);
  }
}

}  // namespace mcrtx
