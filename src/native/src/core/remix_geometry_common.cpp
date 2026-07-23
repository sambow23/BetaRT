// Reusable mesh geometry primitives.

#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;

void appendCloudQuad(
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
    std::uint32_t vertexColor,
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
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void appendCrossedQuadSheet(
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
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const auto normal = computeQuadNormal(x0, y0, z0, x1, y1, z1, x2, y2, z2);
  // Remix uses the reverse winding while the caller order defines the outward normal.
  appendCloudQuad(
      x0,
      y0,
      z0,
      u0,
      v0,
      x1,
      y1,
      z1,
      u1,
      v1,
      x2,
      y2,
      z2,
      u2,
      v2,
      x3,
      y3,
      z3,
      u3,
      v3,
      normal[0],
      normal[1],
      normal[2],
      vertexColor,
      vertices,
      indices);
  appendCloudQuad(
      x3,
      y3,
      z3,
      u3,
      v3,
      x2,
      y2,
      z2,
      u2,
      v2,
      x1,
      y1,
      z1,
      u1,
      v1,
      x0,
      y0,
      z0,
      u0,
      v0,
      -normal[0],
      -normal[1],
      -normal[2],
      vertexColor,
      vertices,
      indices);
}

void appendBoundsFaceGeometry(
    int faceIndex,
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::int16_t terrainTileIndex,
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
    vertex.position[0] = px;
    vertex.position[1] = py;
    vertex.position[2] = pz;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];

    float u = tileMinU;
    float v = tileMinV;
    if (faceIndex == 0) {
      u = tileMinU + (1.0f - relX) * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 1) {
      u = tileMinU + relX * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 2) {
      u = tileMinU + relZ * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 3) {
      u = tileMinU + (1.0f - relZ) * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 4 || faceIndex == 5) {
      u = tileMinU + relX * scaleU;
      v = tileMinV + relZ * scaleV;
    }

    vertex.texcoord[0] = maybeFlipTileU(u, tileMinU, scaleU, flipU);
    vertex.texcoord[1] = v;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void appendTexturedQuad(
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
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const auto normal = computeQuadNormal(x0, y0, z0, x1, y1, z1, x2, y2, z2);
  appendCloudQuad(
      x3,
      y3,
      z3,
      u3,
      v3,
      x2,
      y2,
      z2,
      u2,
      v2,
      x1,
      y1,
      z1,
      u1,
      v1,
      x0,
      y0,
      z0,
      u0,
      v0,
      normal[0],
      normal[1],
      normal[2],
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
  const std::array<std::int16_t, 6>& terrainTiles,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
    const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
    appendBoundsFaceGeometry(
        faceIndex,
        minX,
        minY,
        minZ,
        maxX,
        maxY,
        maxZ,
        terrainTiles[minecraftSide],
        vertexColor,
        vertices,
        indices);
  }
}

void appendFaceGeometry(
    int faceIndex,
    float localX,
    float localY,
    float localZ,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    float normalOffset,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
  const bool flipU = usesFlippedTerrainTile(terrainTileIndex);
  const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTile & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTile & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = localX + kFaceVertexOffsets[faceIndex][vertexIndex][0] + kFaceNormals[faceIndex][0] * normalOffset;
    vertex.position[1] = localY + kFaceVertexOffsets[faceIndex][vertexIndex][1] + kFaceNormals[faceIndex][1] * normalOffset;
    vertex.position[2] = localZ + kFaceVertexOffsets[faceIndex][vertexIndex][2] + kFaceNormals[faceIndex][2] * normalOffset;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    const float u = kFaceTexcoords[faceIndex][vertexIndex][0] == 0.0f ? tileMinU : tileMaxU;
    vertex.texcoord[0] = flipU ? (u == tileMinU ? tileMaxU : tileMinU) : u;
    vertex.texcoord[1] = kFaceTexcoords[faceIndex][vertexIndex][1] == 0.0f ? tileMinV : tileMaxV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

}  // namespace mcrtx::geometry
