#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

namespace mcrtx::geometry {

struct SurfaceBuildBuffers {
  remixapi_MaterialHandle materialHandle {nullptr};
  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  std::vector<float> blendWeights;
  std::vector<std::uint32_t> blendIndices;
};

void appendFaceGeometry(
    int faceIndex,
    float localX,
    float localY,
    float localZ,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    float normalOffset,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

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
    std::vector<std::uint32_t>& indices);

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
    std::vector<std::uint32_t>& indices);

void appendCloudQuad(
    float x0, float y0, float z0, float u0, float v0,
    float x1, float y1, float z1, float u1, float v1,
    float x2, float y2, float z2, float u2, float v2,
    float x3, float y3, float z3, float u3, float v3,
    float normalX, float normalY, float normalZ,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendCrossedQuadSheet(
    float x0, float y0, float z0, float u0, float v0,
    float x1, float y1, float z1, float u1, float v1,
    float x2, float y2, float z2, float u2, float v2,
    float x3, float y3, float z3, float u3, float v3,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendDoubleSidedTexturedQuad(
    float x0, float y0, float z0, float u0, float v0,
    float x1, float y1, float z1, float u1, float v1,
    float x2, float y2, float z2, float u2, float v2,
    float x3, float y3, float z3, float u3, float v3,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

}  // namespace mcrtx::geometry
