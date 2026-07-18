// Chunk capture policy, classification, culling, and fingerprints.

#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <bit>

namespace mcrtx::chunk {

using namespace mcrtx::detail;
using namespace mcrtx::geometry;

std::uint32_t countUniqueBlockIds(const ChunkBuildState& chunkBuild) {
  std::uint32_t unique = 0;
  for (const std::uint32_t count : chunkBuild.blockIdCounts) {
    if (count != 0) {
      ++unique;
    }
  }
  return unique;
}

ChunkKey makeChunkKey(const ChunkBuildState& chunkBuild) {
  return ChunkKey {
      .originX = chunkBuild.origin[0],
      .originY = chunkBuild.origin[1],
      .originZ = chunkBuild.origin[2],
      .renderPass = chunkBuild.renderPass,
  };
}

bool isWaterBlock(int blockId) {
  return blockId == kWaterStillBlockId || blockId == kWaterFlowingBlockId;
}

bool isLavaBlock(int blockId) {
  return blockId == kLavaStillBlockId || blockId == kLavaFlowingBlockId;
}

bool isLiquidBlock(int blockId) {
  return isWaterBlock(blockId) || isLavaBlock(blockId);
}

bool isCrossedQuadRenderType(int renderType) {
  return renderType == kCrossedQuadBlockRenderType;
}

bool isFireRenderType(int renderType) {
  return renderType == kFireBlockRenderType;
}

bool isTorchRenderType(int renderType) {
  return renderType == kTorchBlockRenderType;
}

bool isRedstoneDustRenderType(int renderType) {
  return renderType == kRedstoneDustBlockRenderType;
}

bool isCropRenderType(int renderType) {
  return renderType == kCropBlockRenderType;
}

bool isDoorRenderType(int renderType) {
  return renderType == kDoorBlockRenderType;
}

bool isLadderRenderType(int renderType) {
  return renderType == kLadderBlockRenderType;
}

bool isRailRenderType(int renderType) {
  return renderType == kRailBlockRenderType;
}

bool isStairRenderType(int renderType) {
  return renderType == kStairBlockRenderType;
}

bool isFenceRenderType(int renderType) {
  return renderType == kFenceBlockRenderType;
}

bool isLeverOrButtonRenderType(int renderType) {
  return renderType == kLeverOrButtonBlockRenderType;
}

bool isCactusRenderType(int renderType) {
  return renderType == kCactusBlockRenderType;
}

bool isBedRenderType(int renderType) {
  return renderType == kBedBlockRenderType;
}

bool isRepeaterRenderType(int renderType) {
  return renderType == kRepeaterBlockRenderType;
}

bool isPistonBaseRenderType(int renderType) {
  return renderType == kPistonBaseBlockRenderType;
}

bool isPistonHeadRenderType(int renderType) {
  return renderType == kPistonHeadBlockRenderType;
}

bool isRedstoneDustBlockId(int blockId) {
  return blockId == kRedstoneDustBlockId;
}

bool isCropBlockId(int blockId) {
  return blockId == kCropsBlockId;
}

bool isCactusBlockId(int blockId) {
  return blockId == kCactusBlockId;
}

bool isBedBlockId(int blockId) {
  return blockId == kBedBlockId;
}

bool isSingleSlabBlockId(int blockId) {
  return blockId == kSingleSlabBlockId;
}

bool isStairBlockId(int blockId) {
  return blockId == kWoodStairsBlockId || blockId == kStoneStairsBlockId;
}

bool isDoorBlockId(int blockId) {
  return blockId == kWoodDoorBlockId || blockId == kIronDoorBlockId;
}

bool isRailBlockId(int blockId) {
  return blockId == kGoldenRailBlockId || blockId == kDetectorRailBlockId || blockId == kRailBlockId;
}

bool isLeverBlockId(int blockId) {
  return blockId == kLeverBlockId;
}

bool isButtonBlockId(int blockId) {
  return blockId == kStoneButtonBlockId;
}

bool isRepeaterBlockId(int blockId) {
  return blockId == kRepeaterIdleBlockId || blockId == kRepeaterPoweredBlockId;
}

bool isPistonBaseBlockId(int blockId) {
  return blockId == kStickyPistonBlockId || blockId == kPistonBaseBlockId;
}

bool isPistonHeadBlockId(int blockId) {
  return blockId == kPistonHeadBlockId;
}

bool isRedstoneConnectionCell(const ChunkBlockCell& cell, int direction) {
  if (cell.blockId == kRedstoneDustBlockId) {
    return true;
  }

  if (cell.blockId == kRepeaterIdleBlockId || cell.blockId == kRepeaterPoweredBlockId) {
    if (direction < 0) {
      return false;
    }
    static constexpr int kRepeaterDirections[4] = {2, 3, 0, 1};
    return direction == kRepeaterDirections[cell.blockMetadata & 3];
  }

  switch (cell.blockId) {
    case kLeverBlockId:
    case kStonePressurePlateBlockId:
    case kWoodPressurePlateBlockId:
    case kRedstoneTorchOffBlockId:
    case kRedstoneTorchOnBlockId:
    case kStoneButtonBlockId:
    case kDetectorRailBlockId:
      return true;
    default:
      return false;
  }
}

bool isSupportedPass0RenderType(int renderType) {
  switch (renderType) {
    case kCubeBlockRenderType:
    case kCrossedQuadBlockRenderType:
    case kFireBlockRenderType:
    case kTorchBlockRenderType:
    case kRedstoneDustBlockRenderType:
    case kCropBlockRenderType:
    case kDoorBlockRenderType:
    case kLadderBlockRenderType:
    case kRailBlockRenderType:
    case kStairBlockRenderType:
    case kFenceBlockRenderType:
    case kLeverOrButtonBlockRenderType:
    case kCactusBlockRenderType:
    case kBedBlockRenderType:
    case kRepeaterBlockRenderType:
    case kPistonBaseBlockRenderType:
    case kPistonHeadBlockRenderType:
      return true;
    default:
      return false;
  }
}

bool shouldCaptureBlock(int blockId, int renderType) {
  return blockId > 0 && isSupportedPass0RenderType(renderType);
}

bool shouldCaptureBlock(int blockId, int renderType, int renderPass) {
  if (blockId <= 0) {
    return false;
  }
  if (renderPass == 0) {
    return isSupportedPass0RenderType(renderType)
        || (renderType == kLiquidBlockRenderType && isLavaBlock(blockId));
  }
  if (renderPass == 1) {
    return (renderType == kLiquidBlockRenderType && isWaterBlock(blockId))
        || (renderType == kCubeBlockRenderType && (blockId == kIceBlockId || blockId == kNetherPortalBlockId));
  }
  return false;
}

bool usesCutoutMaterialForBlock(int blockId, int renderType) {
  if (isCrossedQuadRenderType(renderType)
      || isFireRenderType(renderType)
      || isTorchRenderType(renderType)
      || isRedstoneDustRenderType(renderType)
      || isCropRenderType(renderType)
      || isCactusRenderType(renderType)
      || isDoorRenderType(renderType)
      || isBedRenderType(renderType)
      || isLadderRenderType(renderType)
      || isRailRenderType(renderType)
      || isRepeaterRenderType(renderType)) {
    return true;
  }

  switch (blockId) {
    case 18:
    case 20:
    case 52:
    case kNetherPortalBlockId:
    case kTrapdoorBlockId:
      return true;
    default:
      return false;
  }
}

std::uint8_t materialClassForBlock(int blockId, int blockMetadata, int renderType) {
  if (isWaterBlock(blockId)) {
    return kWaterTerrainMaterialClass;
  }
  if (isLavaBlock(blockId)) {
    return kLavaTerrainMaterialClass;
  }
  if (blockId == kIceBlockId) {
    return kIceTerrainMaterialClass;
  }
  if (blockId == kNetherPortalBlockId) {
    return kPortalTerrainMaterialClass;
  }
  if (blockId == kRedstoneDustBlockId && renderType == kRedstoneDustBlockRenderType && blockMetadata > 0) {
    return kPoweredRedstoneTerrainMaterialClass;
  }
  return usesCutoutMaterialForBlock(blockId, renderType) ? kCutoutTerrainMaterialClass : kOpaqueTerrainMaterialClass;
}

std::uint64_t makeChunkMeshHash(const ChunkKey& key, std::uint64_t sequence) {
  std::uint64_t hash = 0x4D43525458484B30ull;
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.originX));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.originY));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.originZ));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.renderPass));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(sequence));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(sequence >> 32));
  return hash;
}

std::uint64_t computeChunkMeshFingerprint(const std::vector<SurfaceBuildBuffers>& surfaces) {
  std::uint64_t fingerprint = 0x4D435254584D4553ull;
  fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(surfaces.size()));

  for (const SurfaceBuildBuffers& surface : surfaces) {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(surface.materialHandle);
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(materialKey));
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(materialKey >> 32));
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(surface.vertices.size()));
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(surface.indices.size()));

    for (const remixapi_HardcodedVertex& vertex : surface.vertices) {
      for (float position : vertex.position) {
        fingerprint = mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(position));
      }
      for (float normal : vertex.normal) {
        fingerprint = mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(normal));
      }
      for (float texcoord : vertex.texcoord) {
        fingerprint = mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(texcoord));
      }
      fingerprint = mixHashComponent(fingerprint, vertex.color);
    }

    for (std::uint32_t index : surface.indices) {
      fingerprint = mixHashComponent(fingerprint, index);
    }
  }

  return fingerprint;
}

std::uint64_t computeChunkFingerprint(
    const std::array<std::uint8_t, kBlocksPerChunk>& occupancy,
    const std::array<ChunkBlockCell, kBlocksPerChunk>& cells) {
  std::uint64_t fingerprint = 1469598103934665603ull;
  for (std::size_t index = 0; index < occupancy.size(); ++index) {
    const std::uint8_t occupied = occupancy[index];
    fingerprint ^= static_cast<std::uint64_t>(occupied);
    fingerprint *= 1099511628211ull;
    if (occupied == 0) {
      continue;
    }

    fingerprint ^= static_cast<std::uint64_t>(cells[index].materialClass);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].blockId);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].blockMetadata);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].renderType);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].liquidVisibilityMask);
    fingerprint *= 1099511628211ull;
    for (const float liquidHeight : cells[index].liquidHeights) {
      fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(liquidHeight));
      fingerprint *= 1099511628211ull;
    }
    fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(cells[index].liquidFlowAngle));
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].blockColor);
    fingerprint *= 1099511628211ull;
    for (const std::int16_t tileIndex : cells[index].terrainTiles) {
      fingerprint ^= static_cast<std::uint64_t>(static_cast<std::uint16_t>(tileIndex));
      fingerprint *= 1099511628211ull;
    }
    for (const float boundValue : cells[index].bounds) {
      fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(boundValue));
      fingerprint *= 1099511628211ull;
    }
  }
  return fingerprint;
}

int normalizeTerrainTileIndex(std::int16_t terrainTileIndex) {
  return std::abs(static_cast<int>(terrainTileIndex));
}

bool usesFlippedTerrainTile(std::int16_t terrainTileIndex) {
  return terrainTileIndex < 0;
}

float maybeFlipTileU(float u, float tileMinU, float scaleU, bool flipU) {
  if (!flipU) {
    return u;
  }

  return tileMinU + scaleU - (u - tileMinU);
}

bool usesPartialCubeBounds(const ChunkBlockCell& cell) {
  return cell.bounds[0] > 0.0f
      || cell.bounds[1] > 0.0f
      || cell.bounds[2] > 0.0f
      || cell.bounds[3] < 1.0f
      || cell.bounds[4] < 1.0f
      || cell.bounds[5] < 1.0f;
}

bool isSolidSupportBlock(const ChunkBlockCell& cell) {
  return cell.renderType == kCubeBlockRenderType
      && !usesPartialCubeBounds(cell)
      && cell.blockId != kIceBlockId
      && !usesCutoutMaterialForBlock(cell.blockId, cell.renderType);
}

int blockIndex(int x, int y, int z) {
  return x + kChunkDimension * (z + kChunkDimension * y);
}

std::uint32_t faceTintColorForBlock(std::uint8_t blockId, int minecraftSide, std::uint32_t blockColor) {
  if (blockId == kGrassBlockId && minecraftSide == 1) {
    return blockColor;
  }

  if (blockId == kLeavesBlockId) {
    return blockColor;
  }

  return 0x00FFFFFFu;
}

bool usesFancyLeavesTexture(const ChunkBlockCell& cell) {
  if (cell.blockId != kLeavesBlockId) {
    return false;
  }

  const int terrainTile = normalizeTerrainTileIndex(cell.terrainTiles[0]);
  return terrainTile == kLeavesFancyTextureOak || terrainTile == kLeavesFancyTextureBirchSpruce;
}

bool shouldCullFaceAgainstNeighbor(const ChunkBlockCell& cell, const ChunkBlockCell& neighborCell) {
  if (cell.renderType != kCubeBlockRenderType || neighborCell.renderType != kCubeBlockRenderType) {
    return false;
  }

  if (usesPartialCubeBounds(cell) || usesPartialCubeBounds(neighborCell)) {
    return false;
  }

  if (cell.blockId == kLeavesBlockId
      && neighborCell.blockId == kLeavesBlockId
      && usesFancyLeavesTexture(cell)) {
    return false;
  }

  if (usesCutoutMaterialForBlock(neighborCell.blockId, neighborCell.renderType) || neighborCell.blockId == kIceBlockId) {
    if (cell.blockId != neighborCell.blockId) {
      return false;
    }
  } else if (!isSolidSupportBlock(neighborCell)) {
    return false;
  }

  return true;
}

}  // namespace mcrtx::chunk
