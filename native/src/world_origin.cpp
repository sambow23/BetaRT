#include "mcrtx/world_origin.hpp"

#include <cmath>
#include <string>

namespace mcrtx {

namespace {

std::string formatWorldOriginCoordinate(const WorldRenderOrigin& origin, int coordinate) {
  return std::to_string(origin.enabled ? coordinate : 0);
}

}  // namespace

int snapWorldCoordinateToChunkOrigin(double coordinate) noexcept {
  return static_cast<int>(std::floor(coordinate / static_cast<double>(kWorldOriginGridSize)))
      * kWorldOriginGridSize;
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

std::uint64_t persistentLightHashForRenderOrigin(std::uint64_t hash, const WorldRenderOrigin& origin) noexcept {
  (void)origin;
  return hash;
}

std::array<WorldRenderOriginGameValue, 3> makeWorldRenderOriginGameValues(const WorldRenderOrigin& origin) {
  return {{
      {kWorldOriginGameValueX, formatWorldOriginCoordinate(origin, origin.x)},
      {kWorldOriginGameValueY, formatWorldOriginCoordinate(origin, origin.y)},
      {kWorldOriginGameValueZ, formatWorldOriginCoordinate(origin, origin.z)},
  }};
}

}  // namespace mcrtx
