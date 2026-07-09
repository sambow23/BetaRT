#include "mcrtx/perf_log.hpp"
#include "mcrtx/remix_renderer.hpp"

#include <cmath>

namespace mcrtx {

void RemixRenderer::setPlayerShadowsEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setPlayerShadowsEnabled");
  std::scoped_lock lock(mutex_);

  playerShadowsEnabled_ = enabled;
  if (!initialized_) {
    return;
  }

  setConfigVariableLocked(
      "rtx.playerModel.enablePrimaryShadows",
      enabled ? "True" : "False",
      true,
      true);
}

void RemixRenderer::setHeldTorchLightsEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setHeldTorchLightsEnabled");
  std::scoped_lock lock(mutex_);

  heldTorchLightsEnabled_ = enabled;
  if (!initialized_ || enabled) {
    return;
  }

  clearHeldTorchLightsLocked();
}

void RemixRenderer::setBlockOutlineEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setBlockOutlineEnabled");
  std::scoped_lock lock(mutex_);
  blockOutlineEnabled_ = enabled;
}

void RemixRenderer::setBlockOutlineStyle(int style) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setBlockOutlineStyle");
  std::scoped_lock lock(mutex_);
  if (style < 0 || style > 5) {
    style = 1;
  }
  blockOutlineStyle_ = style;
}

void RemixRenderer::setBlockOutlineEmissiveIntensity(float intensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setBlockOutlineEmissiveIntensity");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(intensity)) {
    intensity = 4.5f;
  }

  if (intensity < 0.0f) {
    intensity = 0.0f;
  } else if (intensity > 10.0f) {
    intensity = 10.0f;
  }

  if (std::abs(blockOutlineEmissiveIntensity_ - intensity) < 0.001f) {
    return;
  }

  blockOutlineEmissiveIntensity_ = intensity;
  if (!initialized_) {
    return;
  }

  destroyBlockOutlineMesh();
  destroyBlockOutlineMaterials();
  createBlockOutlineMaterials();
  if (!blockOutlineInstances_.empty()) {
    rebuildBlockOutlineMesh(currentRenderOriginLocked());
  }
}

void RemixRenderer::setViewModelFovDegrees(float fovYDegrees) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setViewModelFovDegrees");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(fovYDegrees)) {
    fovYDegrees = 70.0f;
  }

  if (fovYDegrees < 1.0f) {
    fovYDegrees = 1.0f;
  } else if (fovYDegrees > 179.0f) {
    fovYDegrees = 179.0f;
  }

  viewModelFovDegrees_ = fovYDegrees;
}

}  // namespace mcrtx
