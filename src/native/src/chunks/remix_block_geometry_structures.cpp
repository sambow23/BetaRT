// Structural and attached block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_structures.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void appendSlabGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    appendBoxGeometry(
        localX,
        localY,
        localZ,
        localX + 1.0f,
        localY + 0.5f,
        localZ + 1.0f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }


void appendStairGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    const int metadata = cell.blockMetadata & 3;

    if (metadata == 0) {
      appendBoxGeometry(
          localX,
          localY,
          localZ,
          localX + 0.5f,
          localY + 0.5f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      appendBoxGeometry(
          localX + 0.5f,
          localY,
          localZ,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      return;
    }

    if (metadata == 1) {
      appendBoxGeometry(
          localX,
          localY,
          localZ,
          localX + 0.5f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      appendBoxGeometry(
          localX + 0.5f,
          localY,
          localZ,
          localX + 1.0f,
          localY + 0.5f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      return;
    }


    if (metadata == 2) {
      appendBoxGeometry(
          localX,
          localY,
          localZ,
          localX + 1.0f,
          localY + 0.5f,
          localZ + 0.5f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      appendBoxGeometry(
          localX,
          localY,
          localZ + 0.5f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      return;
    }

    appendBoxGeometry(
        localX,
        localY,
        localZ,
        localX + 1.0f,
        localY + 1.0f,
        localZ + 0.5f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
    appendBoxGeometry(
        localX,
        localY,
        localZ + 0.5f,
        localX + 1.0f,
        localY + 0.5f,
        localZ + 1.0f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }


void appendDoorGeometry(
      const ChunkBlockCell& cell,
      int resolvedMetadata,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    const int doorShape = (resolvedMetadata & 4) == 0
        ? ((resolvedMetadata - 1) & 3)
        : (resolvedMetadata & 3);
    constexpr float kDoorThickness = 0.1875f;

    float minX = localX;
    float maxX = localX + 1.0f;
    float minZ = localZ;
    float maxZ = localZ + 1.0f;

    if (doorShape == 0) {
      maxZ = localZ + kDoorThickness;
    } else if (doorShape == 1) {
      minX = localX + 1.0f - kDoorThickness;
    } else if (doorShape == 2) {
      minZ = localZ + 1.0f - kDoorThickness;
    } else {
      maxX = localX + kDoorThickness;
    }

    appendBoxGeometry(
        minX,
        localY,
        minZ,
        maxX,
        localY + 1.0f,
        maxZ,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }


void appendBedGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    static constexpr int kFootConnectedSide[4] = {3, 4, 2, 5};
    static constexpr int kHeadConnectedSide[4] = {2, 5, 3, 4};
    static constexpr int kFlippedSideByFacing[4] = {5, 3, 4, 2};
    static constexpr float kTopUvOrder[4][4][2] = {
        {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
        {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
        {{0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}},
        {{1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}},
    };

    const int facing = cell.blockMetadata & 3;
    const bool isHead = (cell.blockMetadata & 8) != 0;
    const int hiddenMinecraftSide = isHead ? kHeadConnectedSide[facing] : kFootConnectedSide[facing];
    const int flippedMinecraftSide = kFlippedSideByFacing[facing];

    const float minX = localX + cell.bounds[0];
    const float minY = localY + cell.bounds[1];
    const float minZ = localZ + cell.bounds[2];
    const float maxX = localX + cell.bounds[3];
    const float maxY = localY + cell.bounds[4];
    const float maxZ = localZ + cell.bounds[5];

    const auto appendBedQuad = [&vertices, &indices](
                                   float x0, float y0, float z0, float u0, float v0,
                                   float x1, float y1, float z1, float u1, float v1,
                                   float x2, float y2, float z2, float u2, float v2,
                                   float x3, float y3, float z3, float u3, float v3) {
                      const auto normal = computeQuadNormal(x3, y3, z3, x2, y2, z2, x1, y1, z1);
      appendCloudQuad(
                        x3, y3, z3, u3, v3,
                        x2, y2, z2, u2, v2,
                        x1, y1, z1, u1, v1,
                        x0, y0, z0, u0, v0,
          normal[0], normal[1], normal[2],
          kDefaultVertexColor,
          vertices,
          indices);
    };

    const auto appendMappedBedFace = [&](std::int16_t terrainTileIndex, const float uvOrder[4][2]) {
      const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
      const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
      const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
      const float tileMaxU = (static_cast<float>((terrainTile & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels)
          / kAtlasSizePixels;
      const float tileMaxV = (static_cast<float>(terrainTile & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels)
          / kAtlasSizePixels;

      const auto mapU = [tileMinU, tileMaxU](float normalized) {
        return tileMinU + (tileMaxU - tileMinU) * normalized;
      };
      const auto mapV = [tileMinV, tileMaxV](float normalized) {

        return tileMinV + (tileMaxV - tileMinV) * normalized;
      };

      appendBedQuad(
          maxX, maxY, maxZ, mapU(uvOrder[0][0]), mapV(uvOrder[0][1]),
          maxX, maxY, minZ, mapU(uvOrder[1][0]), mapV(uvOrder[1][1]),
          minX, maxY, minZ, mapU(uvOrder[2][0]), mapV(uvOrder[2][1]),
          minX, maxY, maxZ, mapU(uvOrder[3][0]), mapV(uvOrder[3][1]));
    };

    appendBedQuad(
        minX, minY, maxZ, static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) / kAtlasSizePixels,
        (static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels,
        minX, minY, minZ, static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) / kAtlasSizePixels,
        static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) / kAtlasSizePixels,
        maxX, minY, minZ, (static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels,
        static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) / kAtlasSizePixels,
        maxX, minY, maxZ, (static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels,
        (static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels);

    appendMappedBedFace(cell.terrainTiles[1], kTopUvOrder[facing]);

    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
      if (minecraftSide == 0 || minecraftSide == 1 || minecraftSide == hiddenMinecraftSide) {
        continue;
      }

      std::int16_t terrainTileIndex = cell.terrainTiles[minecraftSide];
      if (minecraftSide == flippedMinecraftSide && terrainTileIndex > 0) {
        terrainTileIndex = static_cast<std::int16_t>(-terrainTileIndex);
      }

      appendBoundsFaceGeometry(
          faceIndex,
          minX,
          minY,
          minZ,
          maxX,
          maxY,
          maxZ,
          terrainTileIndex,
          kDefaultVertexColor,
          vertices,
          indices);
    }
  }


void appendFenceGeometry(
    bool connectWest,
    bool connectEast,
    bool connectNorth,
    bool connectSouth,
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendBoxGeometry(
      localX + 0.375f,
      localY + 0.0f,
      localZ + 0.375f,
      localX + 0.625f,
      localY + 1.0f,
      localZ + 0.625f,
      cell.terrainTiles,
      kDefaultVertexColor,
      vertices,
      indices);

  bool connectX = connectWest || connectEast;
  bool connectZ = connectNorth || connectSouth;
  if (!connectX && !connectZ) {
    connectX = true;
  }

  const float xMin = connectWest ? 0.0f : 0.4375f;
  const float xMax = connectEast ? 1.0f : 0.5625f;
  const float zMin = connectNorth ? 0.0f : 0.4375f;
  const float zMax = connectSouth ? 1.0f : 0.5625f;

  if (connectX) {
    appendBoxGeometry(
        localX + xMin,
        localY + 0.75f,
        localZ + 0.4375f,
        localX + xMax,
        localY + 0.9375f,
        localZ + 0.5625f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
    appendBoxGeometry(
        localX + xMin,
        localY + 0.375f,
        localZ + 0.4375f,
        localX + xMax,
        localY + 0.5625f,
        localZ + 0.5625f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }

  if (connectZ) {
    appendBoxGeometry(
        localX + 0.4375f,
        localY + 0.75f,
        localZ + zMin,
        localX + 0.5625f,
        localY + 0.9375f,
        localZ + zMax,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
    appendBoxGeometry(
        localX + 0.4375f,
        localY + 0.375f,
        localZ + zMin,
        localX + 0.5625f,
        localY + 0.5625f,
        localZ + zMax,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }
}


void appendLadderGeometry(
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
  const int metadata = cell.blockMetadata & 7;
  const float epsilon = 0.05f;

  if (metadata == 5) {
    appendCrossedQuadSheet(
        localX + epsilon,
        localY + 1.0f,
        localZ + 1.0f,
        tileMinU,
        tileMinV,
        localX + epsilon,
        localY + 0.0f,
        localZ + 1.0f,
        tileMinU,
        tileMaxV,
        localX + epsilon,
        localY + 0.0f,
        localZ + 0.0f,
        tileMaxU,
        tileMaxV,
        localX + epsilon,
        localY + 1.0f,
        localZ + 0.0f,
        tileMaxU,
        tileMinV,
        kDefaultVertexColor,
        vertices,
        indices);
  } else if (metadata == 4) {
    appendCrossedQuadSheet(
        localX + 1.0f - epsilon,
        localY + 0.0f,
        localZ + 1.0f,
        tileMaxU,
        tileMaxV,
        localX + 1.0f - epsilon,
        localY + 1.0f,
        localZ + 1.0f,
        tileMaxU,
        tileMinV,
        localX + 1.0f - epsilon,
        localY + 1.0f,
        localZ + 0.0f,
        tileMinU,
        tileMinV,
        localX + 1.0f - epsilon,
        localY + 0.0f,
        localZ + 0.0f,
        tileMinU,
        tileMaxV,
        kDefaultVertexColor,
        vertices,
        indices);
  } else if (metadata == 3) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        localY + 0.0f,
        localZ + epsilon,
        tileMaxU,
        tileMaxV,
        localX + 1.0f,
        localY + 1.0f,
        localZ + epsilon,
        tileMaxU,
        tileMinV,
        localX + 0.0f,
        localY + 1.0f,
        localZ + epsilon,

        tileMinU,
        tileMinV,
        localX + 0.0f,
        localY + 0.0f,
        localZ + epsilon,
        tileMinU,
        tileMaxV,
        kDefaultVertexColor,
        vertices,
        indices);
  } else if (metadata == 2) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        localY + 1.0f,
        localZ + 1.0f - epsilon,
        tileMinU,
        tileMinV,
        localX + 1.0f,
        localY + 0.0f,
        localZ + 1.0f - epsilon,
        tileMinU,
        tileMaxV,
        localX + 0.0f,
        localY + 0.0f,
        localZ + 1.0f - epsilon,
        tileMaxU,
        tileMaxV,
        localX + 0.0f,
        localY + 1.0f,
        localZ + 1.0f - epsilon,
        tileMaxU,
        tileMinV,
        kDefaultVertexColor,
        vertices,
        indices);
  }

}

}  // namespace mcrtx::block_geometry
