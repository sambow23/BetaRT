#pragma once

#include <cstdint>

namespace mcrtx {

inline constexpr int kWorldOriginChunkSize = 16;

struct WorldRenderOrigin {
  bool enabled {false};
  int x {0};
  int y {0};
  int z {0};
};

struct WorldRenderPosition {
  float x {0.0f};
  float y {0.0f};
  float z {0.0f};
};

int snapWorldCoordinateToChunkOrigin(double coordinate) noexcept;
WorldRenderOrigin makeWorldRenderOrigin(bool enabled, double cameraX, double cameraY, double cameraZ) noexcept;
float rebaseWorldCoordinate(float coordinate, int origin) noexcept;
double rebaseWorldCoordinate(double coordinate, int origin) noexcept;
float rebaseWorldCoordinate(int coordinate, int origin) noexcept;
WorldRenderPosition rebaseWorldPosition(
    float x,
    float y,
    float z,
    const WorldRenderOrigin& origin) noexcept;
WorldRenderPosition rebaseWorldPosition(
    double x,
    double y,
    double z,
    const WorldRenderOrigin& origin) noexcept;
bool sameWorldRenderOrigin(const WorldRenderOrigin& left, const WorldRenderOrigin& right) noexcept;
std::uint64_t mixWorldRenderOriginHash(std::uint64_t hash, const WorldRenderOrigin& origin) noexcept;

}  // namespace mcrtx
