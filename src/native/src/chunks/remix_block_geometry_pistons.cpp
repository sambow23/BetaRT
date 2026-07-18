// Piston-specific transformed block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_pistons.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <array>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;

namespace {

void appendRotatedBoundsFaceGeometry(
    int faceIndex,
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::int16_t terrainTileIndex,
    int rotation,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
  const bool flipU = usesFlippedTerrainTile(terrainTileIndex);
  const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
  const float usableTileSize = kAtlasTileSizePixels - kAtlasUvInsetPixels;
  const float scaleU = usableTileSize / kAtlasSizePixels;
  const float scaleV = usableTileSize / kAtlasSizePixels;
  const float blockOriginX = std::floor(minX);
  const float blockOriginY = std::floor(minY);
  const float blockOriginZ = std::floor(minZ);

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    const float px = kFaceVertexOffsets[faceIndex][vertexIndex][0] == 0.0f ? minX : maxX;
    const float py = kFaceVertexOffsets[faceIndex][vertexIndex][1] == 0.0f ? minY : maxY;
    const float pz = kFaceVertexOffsets[faceIndex][vertexIndex][2] == 0.0f ? minZ : maxZ;
    const float relX = std::clamp(px - blockOriginX, 0.0f, 1.0f);
    const float relY = std::clamp(py - blockOriginY, 0.0f, 1.0f);
    const float relZ = std::clamp(pz - blockOriginZ, 0.0f, 1.0f);

    float normalizedU = 0.0f;
    float normalizedV = 0.0f;
    switch (faceIndex) {
      case 0:
        normalizedU = 1.0f - relX;
        normalizedV = 1.0f - relY;
        break;
      case 1:
        normalizedU = relX;
        normalizedV = 1.0f - relY;
        break;
      case 2:
        normalizedU = relZ;
        normalizedV = 1.0f - relY;
        break;
      case 3:
        normalizedU = 1.0f - relZ;
        normalizedV = 1.0f - relY;
        break;
      case 4:
        normalizedU = 1.0f - relX;
        normalizedV = 1.0f - relZ;
        break;
      case 5:
      default:
        normalizedU = relX;
        normalizedV = relZ;
        break;
    }

    if (rotation == 1) {
      const float rotatedU = normalizedV;
      const float rotatedV = 1.0f - normalizedU;
      normalizedU = rotatedU;
      normalizedV = rotatedV;
    } else if (rotation == 2) {
      const float rotatedU = 1.0f - normalizedV;
      const float rotatedV = normalizedU;
      normalizedU = rotatedU;
      normalizedV = rotatedV;
    } else if (rotation == 3) {
      normalizedU = 1.0f - normalizedU;
      normalizedV = 1.0f - normalizedV;
    }

    vertex.position[0] = px;
    vertex.position[1] = py;
    vertex.position[2] = pz;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    vertex.texcoord[0] = maybeFlipTileU(tileMinU + normalizedU * scaleU, tileMinU, scaleU, flipU);
    vertex.texcoord[1] = tileMinV + normalizedV * scaleV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

std::array<int, 6> pistonFaceRotationsForFacing(int facing) {
  std::array<int, 6> rotations = {0, 0, 0, 0, 0, 0};
  switch (facing) {
    case 0:
      rotations[0] = 3;
      rotations[1] = 3;
      rotations[2] = 3;
      rotations[3] = 3;
      break;
    case 2:
      rotations[2] = 2;
      rotations[3] = 1;
      break;
    case 3:
      rotations[3] = 2;
      rotations[2] = 1;
      rotations[4] = 3;
      rotations[5] = 3;
      break;
    case 4:
      rotations[0] = 1;
      rotations[1] = 2;
      rotations[4] = 1;
      rotations[5] = 2;
      break;
    case 5:
      rotations[0] = 2;
      rotations[1] = 1;
      rotations[4] = 2;
      rotations[5] = 1;
      break;
    case 1:
    default:
      break;
  }
  return rotations;
}

void appendPistonBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    const std::array<std::int16_t, 6>& terrainTiles,
    const std::array<int, 6>& faceRotations,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
    const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
    appendRotatedBoundsFaceGeometry(
        faceIndex,
        minX,
        minY,
        minZ,
        maxX,
        maxY,
        maxZ,
        terrainTiles[minecraftSide],
        faceRotations[faceIndex],
        vertexColor,
        vertices,
        indices);
  }
}

void appendPistonRodGeometry(
    int facing,
    float localX,
    float localY,
    float localZ,
    std::int16_t terrainTile,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(terrainTile);
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = tileMinU + (kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileBandMaxV = tileMinV + (4.0f - kAtlasUvInsetPixels) / kAtlasSizePixels;

  float rodMinX = localX + 0.375f;
  float rodMaxX = localX + 0.625f;
  float rodMinY = localY + 0.25f;
  float rodMaxY = localY + 1.25f;
  float rodMinZ = localZ + 0.375f;
  float rodMaxZ = localZ + 0.625f;
  enum class RodAxis {
    Vertical,
    NorthSouth,
    EastWest,
  };
  RodAxis axis = RodAxis::Vertical;

  switch (facing) {
    case 1:
      rodMinY = localY - 0.25f;
      rodMaxY = localY + 0.75f;
      axis = RodAxis::Vertical;
      break;
    case 2:
      rodMinX = localX + 0.375f;
      rodMaxX = localX + 0.625f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ + 0.25f;
      rodMaxZ = localZ + 1.25f;
      axis = RodAxis::NorthSouth;
      break;
    case 3:
      rodMinX = localX + 0.375f;
      rodMaxX = localX + 0.625f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ - 0.25f;
      rodMaxZ = localZ + 0.75f;
      axis = RodAxis::NorthSouth;
      break;
    case 4:
      rodMinX = localX + 0.25f;
      rodMaxX = localX + 1.25f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ + 0.375f;
      rodMaxZ = localZ + 0.625f;
      axis = RodAxis::EastWest;
      break;
    case 5:
      rodMinX = localX - 0.25f;
      rodMaxX = localX + 0.75f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ + 0.375f;
      rodMaxZ = localZ + 0.625f;
      axis = RodAxis::EastWest;
      break;
    case 0:
    default:
      axis = RodAxis::Vertical;
      break;
  }

  const auto appendRodQuad = [&](float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3) {
    appendDoubleSidedTexturedQuad(
        x0,
        y0,
        z0,
        tileMinU,
        tileMinV,
        x1,
        y1,
        z1,
        tileMinU,
        tileBandMaxV,
        x2,
        y2,
        z2,
        tileMaxU,
        tileBandMaxV,
        x3,
        y3,
        z3,
        tileMaxU,
        tileMinV,
        vertices,
        indices);
  };

  if (axis == RodAxis::Vertical) {
    appendRodQuad(rodMinX, rodMaxY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ, rodMinX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMaxX, rodMaxY, rodMinZ);
    appendRodQuad(rodMinX, rodMaxY, rodMaxZ, rodMinX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMinZ, rodMaxX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMaxY, rodMinZ);
  } else if (axis == RodAxis::NorthSouth) {
    appendRodQuad(rodMinX, rodMaxY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ, rodMinX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMaxX, rodMaxY, rodMinZ);
    appendRodQuad(rodMinX, rodMaxY, rodMaxZ, rodMinX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ);
  } else {
    appendRodQuad(rodMinX, rodMaxY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMaxX, rodMinY, rodMinZ, rodMaxX, rodMaxY, rodMinZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMinX, rodMinY, rodMaxZ, rodMinX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMinX, rodMaxY, rodMaxZ, rodMinX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ);
  }
}

}  // namespace

void appendPistonBaseGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendPistonBoxGeometry(
      localX + cell.bounds[0],
      localY + cell.bounds[1],
      localZ + cell.bounds[2],
      localX + cell.bounds[3],
      localY + cell.bounds[4],
      localZ + cell.bounds[5],
      cell.terrainTiles,
      pistonFaceRotationsForFacing(cell.blockMetadata & 7),
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendPistonHeadGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int facing = cell.blockMetadata & 7;
  appendPistonBoxGeometry(
      localX + cell.bounds[0],
      localY + cell.bounds[1],
      localZ + cell.bounds[2],
      localX + cell.bounds[3],
      localY + cell.bounds[4],
      localZ + cell.bounds[5],
      cell.terrainTiles,
      pistonFaceRotationsForFacing(facing),
      kDefaultVertexColor,
      vertices,
      indices);

  static constexpr int kOppositeFace[6] = {1, 0, 3, 2, 5, 4};
  std::int16_t sideTile = cell.terrainTiles[2];
  for (int face = 0; face < 6; ++face) {
    if (face != facing && face != kOppositeFace[facing]) {
      sideTile = cell.terrainTiles[face];
      break;
    }
  }
  appendPistonRodGeometry(
      facing,
      localX,
      localY,
      localZ,
      sideTile,
      vertices,
      indices);
}

}  // namespace mcrtx::block_geometry
