#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <vector>

#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx::chunk {

inline constexpr std::size_t kMaxOpaqueBlocksPerChunk = 4096;
inline constexpr int kChunkDimension = 16;
inline constexpr int kBlocksPerChunk = kChunkDimension * kChunkDimension * kChunkDimension;

std::uint32_t countUniqueBlockIds(const ChunkBuildState& chunkBuild);
ChunkKey makeChunkKey(const ChunkBuildState& chunkBuild);
bool isWaterBlock(int blockId);
bool isLavaBlock(int blockId);
bool isLiquidBlock(int blockId);
bool isCrossedQuadRenderType(int renderType);
bool isFireRenderType(int renderType);
bool isTorchRenderType(int renderType);
bool isRedstoneDustRenderType(int renderType);
bool isCropRenderType(int renderType);
bool isDoorRenderType(int renderType);
bool isLadderRenderType(int renderType);
bool isRailRenderType(int renderType);
bool isStairRenderType(int renderType);
bool isFenceRenderType(int renderType);
bool isLeverOrButtonRenderType(int renderType);
bool isCactusRenderType(int renderType);
bool isBedRenderType(int renderType);
bool isRepeaterRenderType(int renderType);
bool isPistonBaseRenderType(int renderType);
bool isPistonHeadRenderType(int renderType);
bool isRedstoneDustBlockId(int blockId);
bool isCropBlockId(int blockId);
bool isCactusBlockId(int blockId);
bool isBedBlockId(int blockId);
bool isSingleSlabBlockId(int blockId);
bool isStairBlockId(int blockId);
bool isDoorBlockId(int blockId);
bool isRailBlockId(int blockId);
bool isLeverBlockId(int blockId);
bool isButtonBlockId(int blockId);
bool isRepeaterBlockId(int blockId);
bool isPistonBaseBlockId(int blockId);
bool isPistonHeadBlockId(int blockId);
bool isRedstoneConnectionCell(const ChunkBlockCell& cell, int direction);
bool isSupportedPass0RenderType(int renderType);
bool shouldCaptureBlock(int blockId, int renderType);
bool shouldCaptureBlock(int blockId, int renderType, int renderPass);
bool usesCutoutMaterialForBlock(int blockId, int renderType);
std::uint8_t materialClassForBlock(int blockId, int blockMetadata, int renderType);
std::uint64_t makeChunkMeshHash(const ChunkKey& key, std::uint64_t sequence);
std::uint64_t computeChunkMeshFingerprint(const std::vector<geometry::SurfaceBuildBuffers>& surfaces);
std::uint64_t computeChunkFingerprint(
    const std::array<std::uint8_t, kBlocksPerChunk>& occupancy,
    const std::array<ChunkBlockCell, kBlocksPerChunk>& cells);
int normalizeTerrainTileIndex(std::int16_t terrainTileIndex);
bool usesFlippedTerrainTile(std::int16_t terrainTileIndex);
float maybeFlipTileU(float u, float tileMinU, float scaleU, bool flipU);
bool usesPartialCubeBounds(const ChunkBlockCell& cell);
bool isSolidSupportBlock(const ChunkBlockCell& cell);
int blockIndex(int x, int y, int z);
std::uint32_t faceTintColorForBlock(std::uint8_t blockId, int minecraftSide, std::uint32_t blockColor);
bool usesFancyLeavesTexture(const ChunkBlockCell& cell);
bool shouldCullFaceAgainstNeighbor(const ChunkBlockCell& cell, const ChunkBlockCell& neighborCell);

}  // namespace mcrtx::chunk
