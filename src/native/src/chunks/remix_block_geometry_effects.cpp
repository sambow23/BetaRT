// Animated fire and portal block geometry emitters.

#include "mcrtx/chunks/remix_block_geometry_effects.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx::block_geometry {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void appendAnimatedFireSheet(
    float x0,
    float y0,
    float z0,
    float x1,
    float y1,
    float z1,
    float x2,
    float y2,
    float z2,
    float x3,
    float y3,
    float z3,
    std::uint32_t frameIndex,
    bool alternateRow,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t clampedFrame = frameIndex % kFireAnimationFrameCount;
  const float tileMinU = (static_cast<float>(clampedFrame) * kAtlasTileSizePixels) / kFireAtlasWidthPixels;
  const float tileMinV = alternateRow ? (kAtlasTileSizePixels / kFireAtlasHeightPixels) : 0.0f;
  const float tileMaxU = ((static_cast<float>(clampedFrame) * kAtlasTileSizePixels) + 15.99f) / kFireAtlasWidthPixels;
  const float tileMaxV = tileMinV + ((kAtlasTileSizePixels - kAtlasUvInsetPixels) / kFireAtlasHeightPixels);

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
  appendCrossedQuadSheet(
      x3,
      y3,
      z3,
      tileMinU,
      tileMinV,
      x2,
      y2,
      z2,
      tileMinU,
      tileMaxV,
      x1,
      y1,
      z1,
      tileMaxU,
      tileMaxV,
      x0,
      y0,
      z0,
      tileMaxU,
      tileMinV,
      kDefaultVertexColor,
      vertices,
      indices);
}


void appendFireGeometry(
    int worldX,
    int worldY,
    int worldZ,
    bool hasBase,
    bool westNeighbor,
    bool eastNeighbor,
    bool northNeighbor,
    bool southNeighbor,
    bool upNeighbor,
    float localX,
    float localY,
    float localZ,
    std::uint32_t frameIndex,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  if (hasBase) {
    const float height = 1.4f;
    appendAnimatedFireSheet(
        localX + 0.2f,
        localY + height,
        localZ + 1.0f,
        localX + 0.7f,
        localY,
        localZ + 1.0f,
        localX + 0.7f,
        localY,
        localZ + 0.0f,
        localX + 0.2f,
        localY + height,
        localZ + 0.0f,
        frameIndex,
        false,
        vertices,
        indices);
      appendAnimatedFireSheet(
        localX + 0.8f,
        localY + height,
        localZ + 0.0f,
        localX + 0.3f,
        localY,
        localZ + 0.0f,
        localX + 0.3f,
        localY,
        localZ + 1.0f,
        localX + 0.8f,
        localY + height,
        localZ + 1.0f,
        frameIndex,
        false,
        vertices,
        indices);
      appendAnimatedFireSheet(
        localX + 1.0f,
        localY + height,
        localZ + 0.8f,
        localX + 1.0f,
        localY,
        localZ + 0.3f,
        localX + 0.0f,
        localY,
        localZ + 0.3f,
        localX + 0.0f,
        localY + height,
        localZ + 0.8f,
        frameIndex,
        true,
        vertices,
        indices);
      appendAnimatedFireSheet(
        localX + 0.0f,
        localY + height,
        localZ + 0.2f,
        localX + 0.0f,
        localY,
        localZ + 0.7f,
        localX + 1.0f,
        localY,
        localZ + 0.7f,
        localX + 1.0f,

        localY + height,
        localZ + 0.2f,
        frameIndex,
        false,
        vertices,
        indices);
    return;
  }

  const float sideInset = 0.2f;
  const float topY = 1.4625f;
  const float bottomY = 0.0625f;

  if (westNeighbor) {
    appendAnimatedFireSheet(
        localX + sideInset,
        localY + topY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + sideInset,
        localY + topY,
        localZ + 0.0f,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (eastNeighbor) {
      appendAnimatedFireSheet(
        localX + 0.8f,
        localY + topY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.8f,
        localY + topY,
        localZ + 1.0f,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (northNeighbor) {
      appendAnimatedFireSheet(
        localX + 0.0f,
        localY + topY,
        localZ + sideInset,
        localX + 0.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + topY,
        localZ + sideInset,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (southNeighbor) {
      appendAnimatedFireSheet(
        localX + 1.0f,
        localY + topY,
        localZ + 0.8f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + bottomY,

        localZ + 1.0f,
        localX + 0.0f,
        localY + topY,
        localZ + 0.8f,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (upNeighbor) {
    const bool rotateTop = ((worldX + worldY + worldZ + 1) & 1) == 0;
    if (rotateTop) {
      appendAnimatedFireSheet(
          localX + 0.0f,
          localY + 0.8f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 0.8f,
          localZ + 1.0f,
            frameIndex,
            false,
          vertices,
          indices);
          appendAnimatedFireSheet(
          localX + 1.0f,
          localY + 0.8f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 0.8f,
          localZ + 0.0f,
            frameIndex,
            true,
          vertices,
          indices);
    } else {
          appendAnimatedFireSheet(
          localX + 0.0f,
          localY + 0.8f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 0.8f,
          localZ + 1.0f,
            frameIndex,
            false,
          vertices,
          indices);
          appendAnimatedFireSheet(
          localX + 1.0f,
          localY + 0.8f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 0.8f,
          localZ + 0.0f,
          frameIndex,
          true,
          vertices,
          indices);
    }
  }
}


void appendPortalGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float minX = localX + cell.bounds[0];
  const float minY = localY + cell.bounds[1];
  const float minZ = localZ + cell.bounds[2];
  const float maxX = localX + cell.bounds[3];
  const float maxY = localY + cell.bounds[4];
  const float maxZ = localZ + cell.bounds[5];
  const float xThickness = cell.bounds[3] - cell.bounds[0];
  const float zThickness = cell.bounds[5] - cell.bounds[2];

  if (xThickness <= zThickness) {
    const float portalX = (minX + maxX) * 0.5f;
    appendCrossedQuadSheet(
        portalX,
        maxY,
        maxZ,
        0.0f,
        0.0f,
        portalX,
        minY,
        maxZ,
        0.0f,
        1.0f,
        portalX,
        minY,
        minZ,
        1.0f,
        1.0f,
        portalX,
        maxY,
        minZ,
        1.0f,
        0.0f,
        kDefaultVertexColor,
        vertices,
        indices);
    return;
  }

  const float portalZ = (minZ + maxZ) * 0.5f;
  appendCrossedQuadSheet(
      maxX,
      maxY,
      portalZ,
      0.0f,
      0.0f,
      maxX,
      minY,
      portalZ,
      0.0f,
      1.0f,
      minX,
      minY,
      portalZ,
      1.0f,
      1.0f,
      minX,
      maxY,
      portalZ,
      1.0f,
      0.0f,
      kDefaultVertexColor,
      vertices,
      indices);
}

}  // namespace mcrtx::block_geometry
