// Natural vegetation block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_natural.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void appendCrossedQuadGeometry(
    const ChunkBlockCell& cell,
  int worldX,
  int worldY,
  int worldZ,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  float offsetX = 0.0f;
  float offsetY = 0.0f;
  float offsetZ = 0.0f;
  if (cell.blockId == kTallGrassBlockId) {
    std::uint64_t seed = static_cast<std::uint64_t>(worldX * 3129871)
      ^ static_cast<std::uint64_t>(worldZ) * 116129781ull
      ^ static_cast<std::uint64_t>(worldY);
    seed = seed * seed * 42317861ull + seed * 11ull;
    offsetX = ((static_cast<float>((seed >> 16) & 0x0F) / 15.0f) - 0.5f) * 0.5f;
    offsetY = ((static_cast<float>((seed >> 20) & 0x0F) / 15.0f) - 1.0f) * 0.2f;
    offsetZ = ((static_cast<float>((seed >> 24) & 0x0F) / 15.0f) - 0.5f) * 0.5f;
  }

  const float centerX = localX + 0.5f + offsetX;
  const float baseY = localY + offsetY;
  const float centerZ = localZ + 0.5f + offsetZ;
  const float minX = centerX - 0.45f;
  const float maxX = centerX + 0.45f;
  const float minZ = centerZ - 0.45f;
  const float maxZ = centerZ + 0.45f;
  const std::uint32_t vertexColor = packVertexColor(cell.blockColor);

  appendCrossedQuadSheet(
      minX,
      baseY + 1.0f,
      minZ,
      tileMinU,
      tileMinV,
      minX,
      baseY + 0.0f,
      minZ,
      tileMinU,
      tileMaxV,
      maxX,
      baseY + 0.0f,
      maxZ,
      tileMaxU,
      tileMaxV,
      maxX,
      baseY + 1.0f,
      maxZ,
      tileMaxU,
      tileMinV,
      vertexColor,
      vertices,
      indices);
  appendCrossedQuadSheet(
      minX,
      baseY + 1.0f,
      maxZ,
      tileMinU,
      tileMinV,
      minX,
      baseY + 0.0f,
      maxZ,
      tileMinU,
      tileMaxV,
      maxX,
      baseY + 0.0f,
      minZ,
      tileMaxU,
      tileMaxV,
      maxX,
      baseY + 1.0f,
      minZ,
      tileMaxU,
      tileMinV,
      vertexColor,
      vertices,
      indices);
}


void appendCropGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
      const int terrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
      const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
      const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
      const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      const std::uint32_t vertexColor = packVertexColor(cell.blockColor);

      const float baseY = localY - 0.0625f;
      const float centerX = localX + 0.5f;
      const float centerZ = localZ + 0.5f;

      appendCrossedQuadSheet(
        centerX - 0.25f,
        baseY + 1.0f,
        centerZ - 0.5f,
        tileMinU,
        tileMinV,
        centerX - 0.25f,
        baseY,
        centerZ - 0.5f,
        tileMinU,
        tileMaxV,
        centerX - 0.25f,
        baseY,
        centerZ + 0.5f,
        tileMaxU,
        tileMaxV,
        centerX - 0.25f,
        baseY + 1.0f,
        centerZ + 0.5f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
      appendCrossedQuadSheet(
        centerX + 0.25f,
        baseY + 1.0f,
        centerZ + 0.5f,
        tileMinU,
        tileMinV,
        centerX + 0.25f,
        baseY,
        centerZ + 0.5f,
        tileMinU,
        tileMaxV,
        centerX + 0.25f,
        baseY,
        centerZ - 0.5f,
        tileMaxU,
        tileMaxV,
        centerX + 0.25f,
        baseY + 1.0f,
        centerZ - 0.5f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
      appendCrossedQuadSheet(
        centerX - 0.5f,
        baseY + 1.0f,
        centerZ - 0.25f,
        tileMinU,
        tileMinV,
        centerX - 0.5f,
        baseY,
        centerZ - 0.25f,
        tileMinU,
        tileMaxV,
        centerX + 0.5f,
        baseY,
        centerZ - 0.25f,
        tileMaxU,
        tileMaxV,
        centerX + 0.5f,
        baseY + 1.0f,
        centerZ - 0.25f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
      appendCrossedQuadSheet(
        centerX + 0.5f,
        baseY + 1.0f,
        centerZ + 0.25f,
        tileMinU,
        tileMinV,
        centerX + 0.5f,
        baseY,
        centerZ + 0.25f,
        tileMinU,
        tileMaxV,
        centerX - 0.5f,
        baseY,
        centerZ + 0.25f,
        tileMaxU,
        tileMaxV,
        centerX - 0.5f,
        baseY + 1.0f,
        centerZ + 0.25f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
    }


void appendCactusGeometry(
      bool renderBottom,
      bool renderTop,
      bool renderNorth,
      bool renderSouth,
      bool renderWest,
      bool renderEast,
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    constexpr float kCactusInset = 0.0625f;

    if (renderBottom) {
      appendBoundsFaceGeometry(
          4,
          localX,
          localY,
          localZ,
          localX + 1.0f,
          localY,
          localZ + 1.0f,
          cell.terrainTiles[0],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderTop) {
      appendBoundsFaceGeometry(
          5,
          localX,
          localY + 1.0f,
          localZ,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles[1],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderNorth) {
      appendBoundsFaceGeometry(
          0,
          localX,
          localY,
          localZ + kCactusInset,
          localX + 1.0f,
          localY + 1.0f,
          localZ + kCactusInset,
          cell.terrainTiles[2],
          kDefaultVertexColor,
          vertices,

          indices);
    }

    if (renderSouth) {
      appendBoundsFaceGeometry(
          1,
          localX,
          localY,
          localZ + 1.0f - kCactusInset,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f - kCactusInset,
          cell.terrainTiles[3],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderWest) {
      appendBoundsFaceGeometry(
          2,
          localX + kCactusInset,
          localY,
          localZ,
          localX + kCactusInset,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles[4],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderEast) {
      appendBoundsFaceGeometry(
          3,
          localX + 1.0f - kCactusInset,
          localY,
          localZ,
          localX + 1.0f - kCactusInset,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles[5],
          kDefaultVertexColor,
          vertices,
          indices);
    }
  }

}  // namespace mcrtx::block_geometry
