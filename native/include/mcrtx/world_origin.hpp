#pragma once

#include <array>
#include <cstdint>
#include <string>
#include <string_view>

namespace mcrtx {

inline constexpr int kWorldOriginGridSize = 1024;
inline constexpr std::string_view kWorldOriginGameValueX = "__mcrtx.world_origin.x";
inline constexpr std::string_view kWorldOriginGameValueY = "__mcrtx.world_origin.y";
inline constexpr std::string_view kWorldOriginGameValueZ = "__mcrtx.world_origin.z";

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

struct WorldRenderOriginGameValue {
  std::string_view key {};
  std::string value {};
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
std::uint64_t persistentLightHashForRenderOrigin(std::uint64_t hash, const WorldRenderOrigin& origin) noexcept;
std::array<WorldRenderOriginGameValue, 3> makeWorldRenderOriginGameValues(const WorldRenderOrigin& origin);

}  // namespace mcrtx
