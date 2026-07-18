// Water and lava block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_fluids.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void appendWaterQuad(
    float x0,
    float y0,
    float z0,
    float u0,
    float v0,
    float x1,
    float y1,
    float z1,
    float u1,
    float v1,
    float x2,
    float y2,
    float z2,
    float u2,
    float v2,
    float x3,
    float y3,
    float z3,
    float u3,
    float v3,
    float normalX,
    float normalY,
    float normalZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const std::array<std::array<float, 5>, 4> vertexData = {{
      {{x0, y0, z0, u0, v0}},
      {{x1, y1, z1, u1, v1}},
      {{x2, y2, z2, u2, v2}},
      {{x3, y3, z3, u3, v3}},
  }};

  for (const auto& data : vertexData) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = data[0];
    vertex.position[1] = data[1];
    vertex.position[2] = data[2];
    vertex.normal[0] = normalX;
    vertex.normal[1] = normalY;
    vertex.normal[2] = normalZ;
    vertex.texcoord[0] = data[3];
    vertex.texcoord[1] = data[4];
    vertex.color = kDefaultVertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}


void appendWaterGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const bool isWaterMaterial = cell.materialClass == kWaterTerrainMaterialClass;
  const bool isLavaMaterial = cell.materialClass == kLavaTerrainMaterialClass;
  const int stillTerrainTileIndex = isLavaMaterial ? kLavaStillTerrainTile : kWaterStillTerrainTile;
  const int flowingTerrainTileIndex = isLavaMaterial ? kLavaFlowingTerrainTile : kWaterFlowingTerrainTile;
  const auto canonicalizeLiquidTerrainTile =
      [isWaterMaterial, isLavaMaterial, stillTerrainTileIndex, flowingTerrainTileIndex](
          std::int16_t terrainTileIndex,
          bool preferFlowing) {
        const int normalizedTerrainTileIndex = normalizeTerrainTileIndex(terrainTileIndex);
        if (!isWaterMaterial && !isLavaMaterial) {
          return normalizedTerrainTileIndex;
        }
        if (normalizedTerrainTileIndex != stillTerrainTileIndex
            && normalizedTerrainTileIndex != flowingTerrainTileIndex) {
          return normalizedTerrainTileIndex;
        }
        return preferFlowing ? flowingTerrainTileIndex : stillTerrainTileIndex;
      };

  const auto remapLiquidUv = [&cell](int terrainTileIndex, float atlasU, float atlasV) {
    const float tileOriginU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
    const float tileOriginV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
    const float tileSpan = kAtlasTileSizePixels / kAtlasSizePixels;
    const float localTileU = (atlasU - tileOriginU) / tileSpan;
    const float localTileV = (atlasV - tileOriginV) / tileSpan;
    const bool useFlowTile = (cell.materialClass == kWaterTerrainMaterialClass && terrainTileIndex == kWaterFlowingTerrainTile)
        || (cell.materialClass == kLavaTerrainMaterialClass && terrainTileIndex == kLavaFlowingTerrainTile);
    return std::array<float, 2> {
        localTileU * 0.5f + (useFlowTile ? 0.5f : 0.0f),
        localTileV,
    };
  };

  const auto appendLiquidQuad = [&cell, &remapLiquidUv, &vertices, &indices](
                                   int terrainTileIndex,
                                   float x0,
                                   float y0,
                                   float z0,
                                   float u0,
                                   float v0,
                                   float x1,
                                   float y1,
                                   float z1,
                                   float u1,
                                   float v1,
                                   float x2,
                                   float y2,
                                   float z2,
                                   float u2,
                                   float v2,
                                   float x3,
                                   float y3,
                                   float z3,
                                   float u3,
                                   float v3,
                                   float normalX,
                                   float normalY,
                                   float normalZ) {
    const auto uv0 = remapLiquidUv(terrainTileIndex, u0, v0);
    const auto uv1 = remapLiquidUv(terrainTileIndex, u1, v1);
    const auto uv2 = remapLiquidUv(terrainTileIndex, u2, v2);
    const auto uv3 = remapLiquidUv(terrainTileIndex, u3, v3);
    appendWaterQuad(
        x0,
        y0,
        z0,
        uv0[0],
        uv0[1],
        x1,
        y1,
        z1,
        uv1[0],
        uv1[1],

        x2,
        y2,
        z2,
        uv2[0],
        uv2[1],
        x3,
        y3,
        z3,
        uv3[0],
        uv3[1],
        normalX,
        normalY,
        normalZ,
        vertices,
        indices);
      if (cell.materialClass == kLavaTerrainMaterialClass) {
        appendWaterQuad(
          x3,
          y3,
          z3,
          uv3[0],
          uv3[1],
          x2,
          y2,
          z2,
          uv2[0],
          uv2[1],
          x1,
          y1,
          z1,
          uv1[0],
          uv1[1],
          x0,
          y0,
          z0,
          uv0[0],
          uv0[1],
          -normalX,
          -normalY,
          -normalZ,
          vertices,
          indices);
      }
  };

  const float heightNorthWest = cell.liquidHeights[0];
  const float heightNorthEast = cell.liquidHeights[1];
  const float heightSouthEast = cell.liquidHeights[2];
  const float heightSouthWest = cell.liquidHeights[3];

  if ((cell.liquidVisibilityMask & (1 << 1)) != 0) {
    int terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[1], false);
    double uvCenterU = static_cast<double>((terrainTileIndex & 0x0F) * 16 + 8) / kAtlasSizePixels;
    double uvCenterV = static_cast<double>((terrainTileIndex & 0xF0) + 8) / kAtlasSizePixels;
    float flowAngle = cell.liquidFlowAngle;
    if (flowAngle > -999.0f) {
      terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[2], true);
      uvCenterU = static_cast<double>((terrainTileIndex & 0x0F) * 16 + 8) / kAtlasSizePixels;
      uvCenterV = static_cast<double>((terrainTileIndex & 0xF0) + 8) / kAtlasSizePixels;
    } else {
      flowAngle = 0.0f;
    }

    const float sinAngle = std::sin(flowAngle) * 8.0f / kAtlasSizePixels;
    const float cosAngle = std::cos(flowAngle) * 8.0f / kAtlasSizePixels;
    appendLiquidQuad(
      terrainTileIndex,
        localX + 0.0f,
        localY + heightNorthWest,
        localZ + 0.0f,
        static_cast<float>(uvCenterU - cosAngle - sinAngle),
        static_cast<float>(uvCenterV - cosAngle + sinAngle),
        localX + 0.0f,
        localY + heightSouthWest,
        localZ + 1.0f,
        static_cast<float>(uvCenterU - cosAngle + sinAngle),
        static_cast<float>(uvCenterV + cosAngle + sinAngle),
        localX + 1.0f,
        localY + heightSouthEast,
        localZ + 1.0f,

        static_cast<float>(uvCenterU + cosAngle + sinAngle),
        static_cast<float>(uvCenterV + cosAngle - sinAngle),
        localX + 1.0f,
        localY + heightNorthEast,
        localZ + 0.0f,
        static_cast<float>(uvCenterU + cosAngle - sinAngle),
        static_cast<float>(uvCenterV - cosAngle - sinAngle),
        0.0f,
        1.0f,
        0.0f);
  }

  if ((cell.liquidVisibilityMask & (1 << 0)) != 0) {
      const int terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[0], false);
      const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
      const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
      const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      appendLiquidQuad(
        terrainTileIndex,
        localX,
        localY,
        localZ,
        tileMinU,
        tileMinV,
        localX,
        localY,
        localZ + 1.0f,
        tileMinU,
        tileMaxV,
        localX + 1.0f,
        localY,
        localZ + 1.0f,
        tileMaxU,
        tileMaxV,
        localX + 1.0f,
        localY,
        localZ,
        tileMaxU,
        tileMinV,
        0.0f,
        -1.0f,
        0.0f);
  }

  for (int sideIndex = 0; sideIndex < 4; ++sideIndex) {
    const int minecraftSide = sideIndex + 2;
    if ((cell.liquidVisibilityMask & (1 << minecraftSide)) == 0) {
      continue;
    }

    const int terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[minecraftSide], true);
    const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
    const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
    const float tileBaseV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
    const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

    float edgeHeightA = 0.0f;
    float edgeHeightB = 0.0f;
    float x0 = 0.0f;
    float x1 = 0.0f;
    float z0 = 0.0f;
    float z1 = 0.0f;
    float normalX = 0.0f;
    float normalZ = 0.0f;

    if (sideIndex == 0) {
      edgeHeightA = heightNorthWest;
      edgeHeightB = heightNorthEast;
      x0 = localX + 0.0f;
      x1 = localX + 1.0f;
      z0 = localZ + 0.0f;
      z1 = localZ + 0.0f;
      normalZ = -1.0f;
    } else if (sideIndex == 1) {
      edgeHeightA = heightSouthEast;
      edgeHeightB = heightSouthWest;
      x0 = localX + 1.0f;
      x1 = localX + 0.0f;
      z0 = localZ + 1.0f;

      z1 = localZ + 1.0f;
      normalZ = 1.0f;
    } else if (sideIndex == 2) {
      edgeHeightA = heightSouthWest;
      edgeHeightB = heightNorthWest;
      x0 = localX + 0.0f;
      x1 = localX + 0.0f;
      z0 = localZ + 1.0f;
      z1 = localZ + 0.0f;
      normalX = -1.0f;
    } else {
      edgeHeightA = heightNorthEast;
      edgeHeightB = heightSouthEast;
      x0 = localX + 1.0f;
      x1 = localX + 1.0f;
      z0 = localZ + 0.0f;
      z1 = localZ + 1.0f;
      normalX = 1.0f;
    }

    const float tileMinVA = tileBaseV + (1.0f - edgeHeightA) * kAtlasTileSizePixels / kAtlasSizePixels;
    const float tileMinVB = tileBaseV + (1.0f - edgeHeightB) * kAtlasTileSizePixels / kAtlasSizePixels;
  appendLiquidQuad(
    terrainTileIndex,
        x0,
        localY + edgeHeightA,
        z0,
        tileMinU,
        tileMinVA,
        x1,
        localY + edgeHeightB,
        z1,
        tileMaxU,
        tileMinVB,
        x1,
        localY + 0.0f,
        z1,
        tileMaxU,
        tileMaxV,
        x0,
        localY + 0.0f,
        z0,
        tileMinU,
        tileMaxV,
        normalX,
        0.0f,
        normalZ);
  }
}

}  // namespace mcrtx::block_geometry
