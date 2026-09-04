#include <algorithm>

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

namespace mcrtx {

void RemixRenderer::setRtQuality(int rtQuality) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setRtQuality");
  std::scoped_lock lock(mutex_);

  if ((rtQuality < kRtQualityLow || rtQuality > kRtQualityUltra) && rtQuality != kRtQualityPotato) {
    rtQuality = kRtQualityHigh;
  }

  rtQuality_ = rtQuality;
  if (!initialized_) {
    return;
  }

  applyRtQualityConfigLocked();
}

void RemixRenderer::setUpscalerConfig(
    int upscalerType,
    int dlssPreset,
    int xessPreset,
    int taauPreset,
    bool rayReconstructionEnabled,
    bool sparseRenderingEnabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setUpscalerConfig");
  std::scoped_lock lock(mutex_);

  if (upscalerType != 0 && upscalerType != 1 && upscalerType != 3 && upscalerType != 4) {
    upscalerType = 1;
  }
  if (dlssPreset < 0 || dlssPreset > 5) {
    dlssPreset = 4;
  }
  if (xessPreset < 0 || xessPreset > 6) {
    xessPreset = 2;
  }
  if (taauPreset < 0 || taauPreset > 4) {
    taauPreset = 2;
  }

  upscalerType_ = upscalerType;
  dlssPreset_ = dlssPreset;
  xessPreset_ = xessPreset;
  taauPreset_ = taauPreset;
  rayReconstructionEnabled_ = rayReconstructionEnabled;
  sparseRenderingEnabled_ = sparseRenderingEnabled;
  if (!initialized_) {
    return;
  }

  applyUpscalerConfigLocked();
}

void RemixRenderer::setFrameGenerationConfig(bool enabled, int multiplier) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setFrameGenerationConfig");
  std::scoped_lock lock(mutex_);

  dlssFrameGenerationEnabled_ = enabled;
  dlssFrameGenerationMultiplier_ = std::clamp(multiplier, 2, 6);
  if (initialized_) {
    applyUpscalerConfigLocked();
  }
}

std::uint32_t RemixRenderer::compiledFeatureMask() const {
  std::scoped_lock lock(mutex_);
  return featureAvailability_.compiledFeatures;
}

std::uint32_t RemixRenderer::availableFeatureMask() const {
  std::scoped_lock lock(mutex_);
  return featureAvailability_.availableFeatures;
}

std::uint32_t RemixRenderer::dlssFrameGenerationMaxInterpolatedFrames() const {
  std::scoped_lock lock(mutex_);
  return featureAvailability_.dlssFrameGenerationMaxInterpolatedFrames;
}

void RemixRenderer::setRemixAtmosphereCloudsEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setRemixAtmosphereCloudsEnabled");
  std::scoped_lock lock(mutex_);
  remixAtmosphereCloudsEnabled_ = enabled;
  if (!initialized_) {
    return;
  }
  applyRemixAtmosphereCloudConfigLocked();
  if (enabled) {
    destroyCloudMesh();
  }
}

void RemixRenderer::setAerialPerspectiveEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setAerialPerspectiveEnabled");
  std::scoped_lock lock(mutex_);
  aerialPerspectiveEnabled_ = enabled;
  if (!initialized_) {
    return;
  }
  applyAerialPerspectiveConfigLocked();
}

void RemixRenderer::setAerialPerspectiveStrength(int strength) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setAerialPerspectiveStrength");
  std::scoped_lock lock(mutex_);
  aerialPerspectiveStrength_ = strength;
  if (!initialized_) {
    return;
  }
  applyAerialPerspectiveConfigLocked();
}

void RemixRenderer::setAerialPerspectiveSceneShadowEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(
      ::mcrtx::perf::Side::Native, "RemixRenderer::setAerialPerspectiveSceneShadowEnabled");
  std::scoped_lock lock(mutex_);
  aerialPerspectiveSceneShadowEnabled_ = enabled;
  if (!initialized_) {
    return;
  }
  applyAerialPerspectiveConfigLocked();
}

}  // namespace mcrtx
