// Lever and torch block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_fixtures.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void appendLeverGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    const std::array<std::int16_t, 6> leverTiles = {
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
    };

    const int metadata = cell.blockMetadata & 7;
    if (metadata < 1 || metadata > 6) {
      return;
    }

    float baseMinX = localX;
    float baseMinY = localY;
    float baseMinZ = localZ;
    float baseMaxX = localX + 1.0f;
    float baseMaxY = localY + 1.0f;
    float baseMaxZ = localZ + 1.0f;

    constexpr float kLeverPlateLongHalf = 0.25f;
    constexpr float kLeverPlateShortHalf = 0.1875f;
    constexpr float kLeverPlateThickness = 0.1875f;

    if (metadata == 5) {
      baseMinX = localX + 0.5f - kLeverPlateShortHalf;
      baseMaxX = localX + 0.5f + kLeverPlateShortHalf;
      baseMinY = localY;
      baseMaxY = localY + kLeverPlateThickness;
      baseMinZ = localZ + 0.5f - kLeverPlateLongHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateLongHalf;
    } else if (metadata == 6) {
      baseMinX = localX + 0.5f - kLeverPlateLongHalf;
      baseMaxX = localX + 0.5f + kLeverPlateLongHalf;
      baseMinY = localY;
      baseMaxY = localY + kLeverPlateThickness;
      baseMinZ = localZ + 0.5f - kLeverPlateShortHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateShortHalf;
    } else if (metadata == 4) {
      baseMinX = localX + 0.5f - kLeverPlateShortHalf;
      baseMaxX = localX + 0.5f + kLeverPlateShortHalf;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ + 1.0f - kLeverPlateThickness;
      baseMaxZ = localZ + 1.0f;
    } else if (metadata == 3) {
      baseMinX = localX + 0.5f - kLeverPlateShortHalf;
      baseMaxX = localX + 0.5f + kLeverPlateShortHalf;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ;
      baseMaxZ = localZ + kLeverPlateThickness;
    } else if (metadata == 2) {
      baseMinX = localX + 1.0f - kLeverPlateThickness;
      baseMaxX = localX + 1.0f;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ + 0.5f - kLeverPlateShortHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateShortHalf;
    } else if (metadata == 1) {
      baseMinX = localX;
      baseMaxX = localX + kLeverPlateThickness;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ + 0.5f - kLeverPlateShortHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateShortHalf;
    }

    appendBoxGeometry(
        baseMinX,
        baseMinY,
        baseMinZ,

        baseMaxX,
        baseMaxY,
        baseMaxZ,
        leverTiles,
        kDefaultVertexColor,
        vertices,
        indices);

    const bool powered = (cell.blockMetadata & 8) != 0;
    const int handleTerrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);

    struct LeverVertex {
      float x;
      float y;
      float z;
    };

    auto rotateX = [](LeverVertex& vertex, float angle) {
      const float sinAngle = std::sin(angle);
      const float cosAngle = std::cos(angle);
      const float rotatedY = vertex.y * cosAngle + vertex.z * sinAngle;
      const float rotatedZ = vertex.z * cosAngle - vertex.y * sinAngle;
      vertex.y = rotatedY;
      vertex.z = rotatedZ;
    };

    auto rotateY = [](LeverVertex& vertex, float angle) {
      const float sinAngle = std::sin(angle);
      const float cosAngle = std::cos(angle);
      const float rotatedX = vertex.x * cosAngle + vertex.z * sinAngle;
      const float rotatedZ = -vertex.x * sinAngle + vertex.z * cosAngle;
      vertex.x = rotatedX;
      vertex.z = rotatedZ;
    };

    std::array<LeverVertex, 8> leverVertices = {{
        {-0.0625f, 0.0f, -0.0625f},
        {0.0625f, 0.0f, -0.0625f},
        {0.0625f, 0.0f, 0.0625f},
        {-0.0625f, 0.0f, 0.0625f},
        {-0.0625f, 0.625f, -0.0625f},
        {0.0625f, 0.625f, -0.0625f},
        {0.0625f, 0.625f, 0.0625f},
        {-0.0625f, 0.625f, 0.0625f},
    }};

    for (LeverVertex& vertex : leverVertices) {
      vertex.z += powered ? -0.0625f : 0.0625f;
      rotateX(vertex, powered ? 0.69813174f : -0.69813174f);

      if (metadata == 6) {
        rotateY(vertex, 1.5707964f);
      }

      if (metadata < 5) {
        vertex.y -= 0.375f;
        rotateX(vertex, 1.5707964f);

        if (metadata == 3) {
          rotateY(vertex, 3.1415927f);
        } else if (metadata == 2) {
          rotateY(vertex, 1.5707964f);
        } else if (metadata == 1) {
          rotateY(vertex, -1.5707964f);
        }

        vertex.x += localX + 0.5f;
        vertex.y += localY + 0.5f;
        vertex.z += localZ + 0.5f;
      } else {
        vertex.x += localX + 0.5f;
        vertex.y += localY + 0.125f;
        vertex.z += localZ + 0.5f;
      }
    }

    const std::array<std::array<int, 4>, 6> faceVertexIndices = {{
        {{0, 1, 2, 3}},
        {{7, 6, 5, 4}},
        {{1, 0, 4, 5}},

        {{2, 1, 5, 6}},
        {{3, 2, 6, 7}},
        {{0, 3, 7, 4}},
    }};

    auto appendLeverQuad = [&vertices, &indices](
                               const LeverVertex& v0,
                               const LeverVertex& v1,
                               const LeverVertex& v2,
                               const LeverVertex& v3,
                               float uMin,
                               float uMax,
                               float vMin,
                               float vMax) {
      const auto normal = computeQuadNormal(v3.x, v3.y, v3.z, v2.x, v2.y, v2.z, v1.x, v1.y, v1.z);
      appendCloudQuad(
          v3.x, v3.y, v3.z, uMin, vMin,
          v2.x, v2.y, v2.z, uMax, vMin,
          v1.x, v1.y, v1.z, uMax, vMax,
          v0.x, v0.y, v0.z, uMin, vMax,
          normal[0], normal[1], normal[2],
          kDefaultVertexColor,
          vertices,
          indices);
    };

    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      float uMin;
      float uMax;
      float vMin;
      float vMax;

      if (faceIndex <= 1) {
        uMin = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 7.0f) / kAtlasSizePixels;
        uMax = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 8.99f) / kAtlasSizePixels;
        vMin = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 6.0f) / kAtlasSizePixels;
        vMax = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 7.99f) / kAtlasSizePixels;
      } else {
        uMin = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 7.0f) / kAtlasSizePixels;
        uMax = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 8.99f) / kAtlasSizePixels;
        vMin = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 6.0f) / kAtlasSizePixels;
        vMax = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 15.99f) / kAtlasSizePixels;
      }

      const auto& face = faceVertexIndices[faceIndex];
      appendLeverQuad(
          leverVertices[face[0]],
          leverVertices[face[1]],
          leverVertices[face[2]],
          leverVertices[face[3]],
          uMin,
          uMax,
          vMin,
          vMax);
    }
  }


void appendTorchGeometry(
  float anchorX,
  float anchorY,
  float anchorZ,
  float leanX,
  float leanZ,
  std::int16_t terrainTile,
  std::vector<remixapi_HardcodedVertex>& vertices,
  std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(terrainTile);
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + 15.99f) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + 15.99f) / kAtlasSizePixels;
  const float capMinU = tileMinU + 7.0f / 256.0f;
  const float capMaxU = tileMinU + 9.0f / 256.0f;
  const float capMinV = tileMinV + 6.0f / 256.0f;
  const float capMaxV = tileMinV + 8.0f / 256.0f;
  const float centerX = anchorX + 0.5f;
  const float centerZ = anchorZ + 0.5f;
  const float minX = centerX - 0.5f;
  const float maxX = centerX + 0.5f;
  const float minZ = centerZ - 0.5f;
  const float maxZ = centerZ + 0.5f;
  const float halfWidth = 0.0625f;
  const float capY = anchorY + 0.625f;
  const float topCenterX = centerX + leanX * 0.375f;
  const float topCenterZ = centerZ + leanZ * 0.375f;
  const float bodyTopY = anchorY + 1.0f;
  const float bodyBottomY = anchorY;

  appendTexturedQuad(
      topCenterX - halfWidth,
      capY,
      topCenterZ - halfWidth,
      capMinU,
      capMinV,
      topCenterX - halfWidth,
      capY,
      topCenterZ + halfWidth,
      capMinU,
      capMaxV,
      topCenterX + halfWidth,
      capY,
      topCenterZ + halfWidth,
      capMaxU,
      capMaxV,
      topCenterX + halfWidth,
      capY,
      topCenterZ - halfWidth,
      capMaxU,
      capMinV,
      vertices,
      indices);

  appendTexturedQuad(
      centerX - halfWidth,
      bodyTopY,
      minZ,
      tileMinU,
      tileMinV,
      centerX - halfWidth + leanX,
      bodyBottomY,
      minZ + leanZ,
      tileMinU,
      tileMaxV,
      centerX - halfWidth + leanX,
      bodyBottomY,
      maxZ + leanZ,
      tileMaxU,
      tileMaxV,
      centerX - halfWidth,
      bodyTopY,
      maxZ,
      tileMaxU,
      tileMinV,
      vertices,
      indices);

  appendTexturedQuad(

      centerX + halfWidth,
      bodyTopY,
      maxZ,
      tileMinU,
      tileMinV,
      centerX + halfWidth + leanX,
      bodyBottomY,
      maxZ + leanZ,
      tileMinU,
      tileMaxV,
      centerX + halfWidth + leanX,
      bodyBottomY,
      minZ + leanZ,
      tileMaxU,
      tileMaxV,
      centerX + halfWidth,
      bodyTopY,
      minZ,
      tileMaxU,
      tileMinV,
      vertices,
      indices);

  appendTexturedQuad(
      minX,
      bodyTopY,
      centerZ + halfWidth,
      tileMinU,
      tileMinV,
      minX + leanX,
      bodyBottomY,
      centerZ + halfWidth + leanZ,
      tileMinU,
      tileMaxV,
      maxX + leanX,
      bodyBottomY,
      centerZ + halfWidth + leanZ,
      tileMaxU,
      tileMaxV,
      maxX,
      bodyTopY,
      centerZ + halfWidth,
      tileMaxU,
      tileMinV,
      vertices,
      indices);

  appendTexturedQuad(
      maxX,
      bodyTopY,
      centerZ - halfWidth,
      tileMinU,
      tileMinV,
      maxX + leanX,
      bodyBottomY,
      centerZ - halfWidth + leanZ,
      tileMinU,
      tileMaxV,
      minX + leanX,
      bodyBottomY,
      centerZ - halfWidth + leanZ,
      tileMaxU,
      tileMaxV,
      minX,
      bodyTopY,
      centerZ - halfWidth,
      tileMaxU,
      tileMinV,
      vertices,
      indices);
}


void appendTorchGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  float anchorX = localX;
  float anchorY = localY;
  float anchorZ = localZ;
  float leanX = 0.0f;
  float leanZ = 0.0f;
  const int metadata = cell.blockMetadata & 7;
  if (metadata == 1) {
    anchorX -= 0.1f;
    anchorY += 0.2f;
    leanX = -0.4f;
  } else if (metadata == 2) {
    anchorX += 0.1f;
    anchorY += 0.2f;
    leanX = 0.4f;
  } else if (metadata == 3) {
    anchorY += 0.2f;
    anchorZ -= 0.1f;
    leanZ = -0.4f;
  } else if (metadata == 4) {
    anchorY += 0.2f;
    anchorZ += 0.1f;
    leanZ = 0.4f;
  }
    appendTorchGeometry(
      anchorX,
      anchorY,
      anchorZ,
      leanX,
      leanZ,
      cell.terrainTiles[0],
      vertices,
      indices);
}

}  // namespace mcrtx::block_geometry
