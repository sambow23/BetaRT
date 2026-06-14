#include "mcrtx/world_origin.hpp"

#include <cmath>
#include <cstdint>
#include <cstdlib>
#include <iostream>

namespace {

void require(bool condition, const char* message) {
  if (!condition) {
    std::cerr << message << '\n';
    std::exit(1);
  }
}

void requireEqual(int actual, int expected, const char* message) {
  if (actual != expected) {
    std::cerr << message << ": expected " << expected << ", got " << actual << '\n';
    std::exit(1);
  }
}

void requireNear(float actual, float expected, const char* message) {
  if (std::fabs(actual - expected) > 0.0001f) {
    std::cerr << message << ": expected " << expected << ", got " << actual << '\n';
    std::exit(1);
  }
}

void requireNearDouble(double actual, double expected, const char* message) {
  if (std::fabs(actual - expected) > 0.000001) {
    std::cerr << message << ": expected " << expected << ", got " << actual << '\n';
    std::exit(1);
  }
}

}  // namespace

int main() {
  const mcrtx::WorldRenderOrigin disabled =
      mcrtx::makeWorldRenderOrigin(false, 46029.25f, 71.75f, 49762.5f);
  require(!disabled.enabled, "disabled origin should report disabled");
  requireEqual(disabled.x, 0, "disabled origin x");
  requireEqual(disabled.y, 0, "disabled origin y");
  requireEqual(disabled.z, 0, "disabled origin z");
  requireNear(mcrtx::rebaseWorldCoordinate(46029.25f, disabled.x), 46029.25f, "disabled camera x");

  const mcrtx::WorldRenderOrigin enabled =
      mcrtx::makeWorldRenderOrigin(true, 46029.25f, 71.75f, 49762.5f);
  require(enabled.enabled, "enabled origin should report enabled");
  requireEqual(enabled.x, 46016, "enabled origin x");
  requireEqual(enabled.y, 64, "enabled origin y");
  requireEqual(enabled.z, 49760, "enabled origin z");
  requireNear(mcrtx::rebaseWorldCoordinate(46029.25f, enabled.x), 13.25f, "relative camera x");
  requireNear(mcrtx::rebaseWorldCoordinate(71.75f, enabled.y), 7.75f, "relative camera y");
  requireNear(mcrtx::rebaseWorldCoordinate(49762.5f, enabled.z), 2.5f, "relative camera z");
  requireNear(mcrtx::rebaseWorldCoordinate(46016, enabled.x), 0.0f, "relative chunk x");
  requireNear(mcrtx::rebaseWorldCoordinate(64, enabled.y), 0.0f, "relative chunk y");
  requireNear(mcrtx::rebaseWorldCoordinate(49760, enabled.z), 0.0f, "relative chunk z");
  const mcrtx::WorldRenderPosition dynamicPosition =
      mcrtx::rebaseWorldPosition(46029.5f, 72.0f, 49762.75f, enabled);
  requireNear(dynamicPosition.x, 13.5f, "relative dynamic position x");
  requireNear(dynamicPosition.y, 8.0f, "relative dynamic position y");
  requireNear(dynamicPosition.z, 2.75f, "relative dynamic position z");

  const double preciseCameraX = 46029.001953125;
  const mcrtx::WorldRenderOrigin preciseOrigin =
      mcrtx::makeWorldRenderOrigin(true, preciseCameraX, 71.75, 49762.5);
  requireNearDouble(
      mcrtx::rebaseWorldCoordinate(preciseCameraX, preciseOrigin.x),
      13.001953125,
      "relative camera x preserves sub-float movement");
  const mcrtx::WorldRenderPosition preciseDynamicPosition =
      mcrtx::rebaseWorldPosition(46029.001953125, 71.75, 49762.998046875, preciseOrigin);
  requireNear(preciseDynamicPosition.x, 13.001953125f, "relative dynamic double x preserves sub-float movement");
  requireNear(preciseDynamicPosition.y, 7.75f, "relative dynamic double y");
  requireNear(preciseDynamicPosition.z, 2.998046875f, "relative dynamic double z preserves sub-float movement");

  const std::uint64_t lightHash = 0x123456789ABCDEF0ull;
  const std::uint64_t firstOriginHash = mcrtx::mixWorldRenderOriginHash(lightHash, enabled);
  const mcrtx::WorldRenderOrigin nextOrigin =
      mcrtx::makeWorldRenderOrigin(true, 46032.0, 71.75, 49762.5);
  require(
      firstOriginHash != mcrtx::mixWorldRenderOriginHash(lightHash, nextOrigin),
      "origin-aware light hash changes across origin chunks");
  require(
      lightHash == mcrtx::mixWorldRenderOriginHash(lightHash, disabled),
      "disabled origin keeps light hash unchanged");
  require(mcrtx::sameWorldRenderOrigin(enabled, enabled), "same origin compares equal");
  require(!mcrtx::sameWorldRenderOrigin(enabled, nextOrigin), "different origin compares unequal");

  const mcrtx::WorldRenderOrigin negative =
      mcrtx::makeWorldRenderOrigin(true, -1.0f, -0.1f, -17.0f);
  requireEqual(negative.x, -16, "negative origin x floors to previous chunk");
  requireEqual(negative.y, -16, "negative origin y floors to previous chunk");
  requireEqual(negative.z, -32, "negative origin z floors to previous chunk");

  return 0;
}
