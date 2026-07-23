// Redstone, repeater, and rail block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_redstone.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void appendRepeaterGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendBoxGeometry(
      localX + cell.bounds[0],
      localY + cell.bounds[1],
      localZ + cell.bounds[2],
      localX + cell.bounds[3],
      localY + cell.bounds[4],
      localZ + cell.bounds[5],
      cell.terrainTiles,
      kDefaultVertexColor,
      vertices,
      indices);

  // Each post is emitted as a narrow cross clipped to the visible torch body
  // texels (tile columns 7-9, rows 6-16). The full-block torch helper would
  // put both posts' side quads in the same planes, and coplanar alpha-cutout
  // quads flicker under path tracing depending on camera angle.
  const auto appendTorchPost = [&](float offsetX, float offsetZ) {
    const int tileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
    const float tileU = static_cast<float>((tileIndex & 0x0F) * 16) / kAtlasSizePixels;
    const float tileV = static_cast<float>(tileIndex & 0xF0) / kAtlasSizePixels;
    const float bodyMinU = tileU + 7.0f / 256.0f;
    const float bodyMaxU = tileU + 9.0f / 256.0f;
    const float bodyMinV = tileV + 6.0f / 256.0f;
    const float bodyMaxV = tileV + 15.99f / 256.0f;
    const float capMinU = tileU + 7.0f / 256.0f;
    const float capMaxU = tileU + 9.0f / 256.0f;
    const float capMinV = tileV + 6.0f / 256.0f;
    const float capMaxV = tileV + 8.0f / 256.0f;

    const float anchorY = localY - 0.1875f;
    const float centerX = localX + offsetX + 0.5f;
    const float centerZ = localZ + offsetZ + 0.5f;
    const float halfWidth = 0.0625f;
    const float bodyBottomY = anchorY;
    const float bodyTopY = anchorY + 0.625f;
    const float capY = bodyTopY;
    const float minX = centerX - halfWidth;
    const float maxX = centerX + halfWidth;
    const float minZ = centerZ - halfWidth;
    const float maxZ = centerZ + halfWidth;

    appendTexturedQuad(
        minX, capY, minZ, capMinU, capMinV,
        minX, capY, maxZ, capMinU, capMaxV,
        maxX, capY, maxZ, capMaxU, capMaxV,
        maxX, capY, minZ, capMaxU, capMinV,
        vertices,
        indices);

    appendTexturedQuad(
        minX, bodyTopY, minZ, bodyMinU, bodyMinV,
        minX, bodyBottomY, minZ, bodyMinU, bodyMaxV,
        minX, bodyBottomY, maxZ, bodyMaxU, bodyMaxV,
        minX, bodyTopY, maxZ, bodyMaxU, bodyMinV,
        vertices,
        indices);

    appendTexturedQuad(
        maxX, bodyTopY, maxZ, bodyMinU, bodyMinV,
        maxX, bodyBottomY, maxZ, bodyMinU, bodyMaxV,
        maxX, bodyBottomY, minZ, bodyMaxU, bodyMaxV,
        maxX, bodyTopY, minZ, bodyMaxU, bodyMinV,
        vertices,
        indices);

    appendTexturedQuad(
        minX, bodyTopY, maxZ, bodyMinU, bodyMinV,
        minX, bodyBottomY, maxZ, bodyMinU, bodyMaxV,
        maxX, bodyBottomY, maxZ, bodyMaxU, bodyMaxV,
        maxX, bodyTopY, maxZ, bodyMaxU, bodyMinV,
        vertices,
        indices);


    appendTexturedQuad(
        maxX, bodyTopY, minZ, bodyMinU, bodyMinV,
        maxX, bodyBottomY, minZ, bodyMinU, bodyMaxV,
        minX, bodyBottomY, minZ, bodyMaxU, bodyMaxV,
        minX, bodyTopY, minZ, bodyMaxU, bodyMinV,
        vertices,
        indices);
  };

  static constexpr float kRepeaterTorchOffsets[4] = {-0.0625f, 0.0625f, 0.1875f, 0.3125f};
  const int facing = cell.blockMetadata & 3;
  const int delay = (cell.blockMetadata >> 2) & 3;

  float firstOffsetX = 0.0f;
  float firstOffsetZ = 0.0f;
  float secondOffsetX = 0.0f;
  float secondOffsetZ = 0.0f;
  switch (facing) {
    case 0:
      secondOffsetZ = -0.3125f;
      firstOffsetZ = kRepeaterTorchOffsets[delay];
      break;
    case 2:
      secondOffsetZ = 0.3125f;
      firstOffsetZ = -kRepeaterTorchOffsets[delay];
      break;
    case 3:
      secondOffsetX = -0.3125f;
      firstOffsetX = kRepeaterTorchOffsets[delay];
      break;
    case 1:
    default:
      secondOffsetX = 0.3125f;
      firstOffsetX = -kRepeaterTorchOffsets[delay];
      break;
  }

  appendTorchPost(firstOffsetX, firstOffsetZ);
  appendTorchPost(secondOffsetX, secondOffsetZ);

  const int topTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[1]);
  const float tileMinU = static_cast<float>((topTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(topTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((topTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(topTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  const float minX = localX;
  const float maxX = localX + 1.0f;
  const float minZ = localZ;
  const float maxZ = localZ + 1.0f;
  const float topY = localY + 0.125f + 0.0005f;

  float x0 = minX;
  float z0 = minZ;
  float x1 = minX;
  float z1 = maxZ;
  float x2 = maxX;
  float z2 = maxZ;
  float x3 = maxX;
  float z3 = minZ;
  switch (facing & 3) {
    case 1:
      x0 = maxX;
      z0 = minZ;
      x1 = minX;
      z1 = minZ;
      x2 = minX;
      z2 = maxZ;
      x3 = maxX;
      z3 = maxZ;
      break;
    case 2:
      x0 = maxX;
      z0 = maxZ;
      x1 = maxX;
      z1 = minZ;
      x2 = minX;
      z2 = minZ;
      x3 = minX;
      z3 = maxZ;

      break;
    case 3:
      x0 = minX;
      z0 = maxZ;
      x1 = maxX;
      z1 = maxZ;
      x2 = maxX;
      z2 = minZ;
      x3 = minX;
      z3 = minZ;
      break;
    default:
      break;
  }

  appendCloudQuad(
      x0,
      topY,
      z0,
      tileMinU,
      tileMinV,
      x1,
      topY,
      z1,
      tileMinU,
      tileMaxV,
      x2,
      topY,
      z2,
      tileMaxU,
      tileMaxV,
      x3,
      topY,
      z3,
      tileMaxU,
      tileMinV,
      0.0f,
      1.0f,
      0.0f,
      kDefaultVertexColor,
      vertices,
      indices);
  appendCloudQuad(
      x3,
      topY,
      z3,
      tileMaxU,
      tileMinV,
      x2,
      topY,
      z2,
      tileMaxU,
      tileMaxV,
      x1,
      topY,
      z1,
      tileMinU,
      tileMaxV,
      x0,
      topY,
      z0,
      tileMinU,
      tileMinV,
      0.0f,
      -1.0f,
      0.0f,
      kDefaultVertexColor,
      vertices,
      indices);
}


void appendRailGeometry(
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
  int metadata = cell.blockMetadata;
  if (cell.blockId == kGoldenRailBlockId || cell.blockId == kDetectorRailBlockId) {
    metadata &= 7;
  }

  float x0 = localX + 1.0f;
  float x1 = localX + 1.0f;
  float x2 = localX + 0.0f;
  float x3 = localX + 0.0f;
  float z0 = localZ + 0.0f;
  float z1 = localZ + 1.0f;
  float z2 = localZ + 1.0f;
  float z3 = localZ + 0.0f;
  float y0 = localY + 0.0625f;
  float y1 = localY + 0.0625f;
  float y2 = localY + 0.0625f;
  float y3 = localY + 0.0625f;

  if (metadata == 1 || metadata == 2 || metadata == 3 || metadata == 7) {
    x0 = localX + 1.0f;
    x1 = localX + 0.0f;
    x2 = localX + 0.0f;
    x3 = localX + 1.0f;
    z0 = localZ + 1.0f;
    z1 = localZ + 1.0f;
    z2 = localZ + 0.0f;
    z3 = localZ + 0.0f;
  } else if (metadata == 8) {
    x0 = localX + 0.0f;
    x1 = localX + 0.0f;
    x2 = localX + 1.0f;
    x3 = localX + 1.0f;
    z0 = localZ + 1.0f;
    z1 = localZ + 0.0f;
    z2 = localZ + 0.0f;
    z3 = localZ + 1.0f;
  } else if (metadata == 9) {
    x0 = localX + 0.0f;
    x1 = localX + 1.0f;
    x2 = localX + 1.0f;
    x3 = localX + 0.0f;
    z0 = localZ + 0.0f;
    z1 = localZ + 0.0f;
    z2 = localZ + 1.0f;
    z3 = localZ + 1.0f;
  }

  if (metadata == 2 || metadata == 4) {
    y0 += 1.0f;
    y3 += 1.0f;
  } else if (metadata == 3 || metadata == 5) {
    y1 += 1.0f;
    y2 += 1.0f;
  }

  appendCrossedQuadSheet(
      x0,
      y0,
      z0,
      tileMaxU,
      tileMinV,

      x1,
      y1,
      z1,
      tileMaxU,
      tileMaxV,
      x2,
      y2,
      z2,
      tileMinU,
      tileMaxV,
      x3,
      y3,
      z3,
      tileMinU,
      tileMinV,
      kDefaultVertexColor,
      vertices,
      indices);
}


void appendRedstoneDustGeometry(
    const ChunkBlockCell& cell,
    bool connectWest,
    bool connectEast,
    bool connectNorth,
    bool connectSouth,
    bool climbWest,
    bool climbEast,
    bool climbNorth,
    bool climbSouth,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int crossTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
  const int lineTileIndex = crossTileIndex + 1;
  const int powerLevel = cell.blockMetadata & 0x0F;
  const float power = static_cast<float>(powerLevel) / 15.0f;
  float red = power * 0.6f + 0.4f;
  if (powerLevel == 0) {
    red = 0.3f;
  }
  float green = std::max(power * power * 0.7f - 0.5f, 0.0f);
  float blue = std::max(power * power * 0.6f - 0.7f, 0.0f);
  const std::uint32_t vertexColor = packVertexColorRgba(red, green, blue, 1.0f);

  const auto computeTileUv = [](int terrainTileIndex, float& tileMinU, float& tileMaxU, float& tileMinV, float& tileMaxV) {
    tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
    tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
    tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
    tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  };

  const bool lineX = (connectWest || connectEast) && !connectNorth && !connectSouth;
  const bool lineZ = (connectNorth || connectSouth) && !connectWest && !connectEast;
  const int topTileIndex = (lineX || lineZ) ? lineTileIndex : crossTileIndex;

  float topMinU = 0.0f;
  float topMaxU = 0.0f;
  float topMinV = 0.0f;
  float topMaxV = 0.0f;
  computeTileUv(topTileIndex, topMinU, topMaxU, topMinV, topMaxV);

  float xMin = localX + 0.0f;
  float xMax = localX + 1.0f;
  float zMin = localZ + 0.0f;
  float zMax = localZ + 1.0f;
  constexpr float kConnectionInset = 0.3125f;
  constexpr float kConnectionUvInset = 5.0f / 256.0f;

  if (!(lineX || lineZ) && (connectWest || connectEast || connectNorth || connectSouth)) {
    if (!connectWest) {
      xMin += kConnectionInset;
      topMinU += kConnectionUvInset;
    }
    if (!connectEast) {
      xMax -= kConnectionInset;
      topMaxU -= kConnectionUvInset;
    }
    if (!connectNorth) {
      zMin += kConnectionInset;
      topMinV += kConnectionUvInset;
    }
    if (!connectSouth) {
      zMax -= kConnectionInset;
      topMaxV -= kConnectionUvInset;
    }
  }

  const float topY = localY + 0.015625f;
  if (lineZ) {
    appendCrossedQuadSheet(
        xMax,
        topY,
        zMax,
        topMaxU,
        topMaxV,
        xMax,
        topY,

        zMin,
        topMinU,
        topMaxV,
        xMin,
        topY,
        zMin,
        topMinU,
        topMinV,
        xMin,
        topY,
        zMax,
        topMaxU,
        topMinV,
        vertexColor,
        vertices,
        indices);
  } else {
    appendCrossedQuadSheet(
        xMax,
        topY,
        zMax,
        topMaxU,
        topMaxV,
        xMax,
        topY,
        zMin,
        topMaxU,
        topMinV,
        xMin,
        topY,
        zMin,
        topMinU,
        topMinV,
        xMin,
        topY,
        zMax,
        topMinU,
        topMaxV,
        vertexColor,
        vertices,
        indices);
  }

  float sideMinU = 0.0f;
  float sideMaxU = 0.0f;
  float sideMinV = 0.0f;
  float sideMaxV = 0.0f;
  computeTileUv(topTileIndex, sideMinU, sideMaxU, sideMinV, sideMaxV);

  const float wallLowY = localY + 0.0f;
  const float wallHighY = localY + 1.021875f;
  const float wallInset = 0.015625f;

  if (climbWest) {
    appendCrossedQuadSheet(
        localX + wallInset,
        wallHighY,
        localZ + 1.0f,
        sideMaxU,
        sideMinV,
        localX + wallInset,
        wallLowY,
        localZ + 1.0f,
        sideMinU,
        sideMinV,
        localX + wallInset,
        wallLowY,
        localZ + 0.0f,
        sideMinU,
        sideMaxV,
        localX + wallInset,
        wallHighY,
        localZ + 0.0f,
        sideMaxU,
        sideMaxV,
        vertexColor,
        vertices,
        indices);
  }


  if (climbEast) {
    appendCrossedQuadSheet(
        localX + 1.0f - wallInset,
        wallLowY,
        localZ + 1.0f,
        sideMinU,
        sideMaxV,
        localX + 1.0f - wallInset,
        wallHighY,
        localZ + 1.0f,
        sideMaxU,
        sideMaxV,
        localX + 1.0f - wallInset,
        wallHighY,
        localZ + 0.0f,
        sideMaxU,
        sideMinV,
        localX + 1.0f - wallInset,
        wallLowY,
        localZ + 0.0f,
        sideMinU,
        sideMinV,
        vertexColor,
        vertices,
        indices);
  }

  if (climbNorth) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        wallLowY,
        localZ + wallInset,
        sideMinU,
        sideMaxV,
        localX + 1.0f,
        wallHighY,
        localZ + wallInset,
        sideMaxU,
        sideMaxV,
        localX + 0.0f,
        wallHighY,
        localZ + wallInset,
        sideMaxU,
        sideMinV,
        localX + 0.0f,
        wallLowY,
        localZ + wallInset,
        sideMinU,
        sideMinV,
        vertexColor,
        vertices,
        indices);
  }

  if (climbSouth) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        wallHighY,
        localZ + 1.0f - wallInset,
        sideMaxU,
        sideMinV,
        localX + 1.0f,
        wallLowY,
        localZ + 1.0f - wallInset,
        sideMinU,
        sideMinV,
        localX + 0.0f,
        wallLowY,
        localZ + 1.0f - wallInset,
        sideMinU,
        sideMaxV,
        localX + 0.0f,
        wallHighY,
        localZ + 1.0f - wallInset,
        sideMaxU,
        sideMaxV,
        vertexColor,
        vertices,
        indices);
  }
}

}  // namespace mcrtx::block_geometry
