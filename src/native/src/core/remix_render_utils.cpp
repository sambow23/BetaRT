// Generic Remix formatting, transform, hash, color, and normal utilities.

#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>
#include <sstream>

namespace mcrtx::detail {

std::string errorCodeToString(remixapi_ErrorCode code) {
  std::ostringstream stream;
  stream << "remixapi error " << static_cast<int>(code);
  return stream.str();
}

remixapi_Transform makeTranslationTransform(float x, float y, float z) {
  remixapi_Transform transform {};
  transform.matrix[0][0] = 1.0f;
  transform.matrix[1][1] = 1.0f;
  transform.matrix[2][2] = 1.0f;
  transform.matrix[0][3] = x;
  transform.matrix[1][3] = y;
  transform.matrix[2][3] = z;
  return transform;
}

std::uint64_t mixHashComponent(std::uint64_t hash, std::uint32_t value) {
  hash ^= static_cast<std::uint64_t>(value) + 0x9e3779b97f4a7c15ull + (hash << 6) + (hash >> 2);
  return hash;
}

std::uint8_t clampColorChannel(float value) {
  const float clampedValue = std::clamp(value, 0.0f, 1.0f);
  return static_cast<std::uint8_t>(std::lround(clampedValue * 255.0f));
}

std::uint32_t packVertexColorRgba(float red, float green, float blue, float alpha) {
  return (static_cast<std::uint32_t>(clampColorChannel(alpha)) << 24)
      | (static_cast<std::uint32_t>(clampColorChannel(red)) << 16)
      | (static_cast<std::uint32_t>(clampColorChannel(green)) << 8)
      | static_cast<std::uint32_t>(clampColorChannel(blue));
}

std::uint32_t packVertexColor(std::uint32_t rgbColor) {
  return 0xFF000000u | (rgbColor & 0x00FFFFFFu);
}

std::array<float, 3> computeQuadNormal(
    float x0,
    float y0,
    float z0,
    float x1,
    float y1,
    float z1,
    float x2,
    float y2,
    float z2) {
  const float edgeAx = x1 - x0;
  const float edgeAy = y1 - y0;
  const float edgeAz = z1 - z0;
  const float edgeBx = x2 - x0;
  const float edgeBy = y2 - y0;
  const float edgeBz = z2 - z0;

  float normalX = edgeAy * edgeBz - edgeAz * edgeBy;
  float normalY = edgeAz * edgeBx - edgeAx * edgeBz;
  float normalZ = edgeAx * edgeBy - edgeAy * edgeBx;
  const float normalLength = std::sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);

  if (normalLength <= 0.00001f) {
    return {0.0f, 1.0f, 0.0f};
  }

  return {normalX / normalLength, normalY / normalLength, normalZ / normalLength};
}

}  // namespace mcrtx::detail
