#include "mcrtx/world_origin.hpp"

#include <cmath>

namespace mcrtx {

int snapWorldCoordinateToChunkOrigin(double coordinate) noexcept {
  return static_cast<int>(std::floor(coordinate / static_cast<double>(kWorldOriginChunkSize)))
      * kWorldOriginChunkSize;
}

WorldRenderOrigin makeWorldRenderOrigin(bool enabled, double cameraX, double cameraY, double cameraZ) noexcept {
  if (!enabled) {
    return {};
  }

  return {
      true,
      snapWorldCoordinateToChunkOrigin(cameraX),
      snapWorldCoordinateToChunkOrigin(cameraY),
      snapWorldCoordinateToChunkOrigin(cameraZ)};
}

float rebaseWorldCoordinate(float coordinate, int origin) noexcept {
  return coordinate - static_cast<float>(origin);
}

double rebaseWorldCoordinate(double coordinate, int origin) noexcept {
  return coordinate - static_cast<double>(origin);
}

float rebaseWorldCoordinate(int coordinate, int origin) noexcept {
  return static_cast<float>(coordinate - origin);
}

WorldRenderPosition rebaseWorldPosition(
    float x,
    float y,
    float z,
    const WorldRenderOrigin& origin) noexcept {
  if (!origin.enabled) {
    return {x, y, z};
  }

  return {
      rebaseWorldCoordinate(x, origin.x),
      rebaseWorldCoordinate(y, origin.y),
      rebaseWorldCoordinate(z, origin.z)};
}

WorldRenderPosition rebaseWorldPosition(
    double x,
    double y,
    double z,
    const WorldRenderOrigin& origin) noexcept {
  if (!origin.enabled) {
    return {
        static_cast<float>(x),
        static_cast<float>(y),
        static_cast<float>(z),
    };
  }

  return {
      static_cast<float>(rebaseWorldCoordinate(x, origin.x)),
      static_cast<float>(rebaseWorldCoordinate(y, origin.y)),
      static_cast<float>(rebaseWorldCoordinate(z, origin.z)),
  };
}

bool sameWorldRenderOrigin(const WorldRenderOrigin& left, const WorldRenderOrigin& right) noexcept {
  return left.enabled == right.enabled
      && left.x == right.x
      && left.y == right.y
      && left.z == right.z;
}

std::uint64_t mixWorldRenderOriginHash(std::uint64_t hash, const WorldRenderOrigin& origin) noexcept {
  if (!origin.enabled) {
    return hash;
  }

  const auto mix = [](std::uint64_t value, std::uint64_t component) noexcept {
    value ^= component + 0x9E3779B97F4A7C15ull + (value << 6) + (value >> 2);
    return value;
  };

  hash = mix(hash, static_cast<std::uint32_t>(origin.x));
  hash = mix(hash, static_cast<std::uint32_t>(origin.y));
  hash = mix(hash, static_cast<std::uint32_t>(origin.z));
  return hash;
}

}  // namespace mcrtx
