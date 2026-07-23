#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>

namespace mcrtx {

using namespace mcrtx::detail;

void RemixRenderer::setDisplacementFactor(float factor) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDisplacementFactor");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(factor)) {
    factor = 1.0f;
  }

  if (factor < 0.0f) {
    factor = 0.0f;
  } else if (factor > 4.0f) {
    factor = 4.0f;
  }

  if (std::abs(displacementFactor_ - factor) < 0.001f) {
    return;
  }

  displacementFactor_ = factor;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceMeasurementDistance(float distance) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceMeasurementDistance");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(distance)) {
    distance = 1.0f;
  }

  if (distance < 0.0f) {
    distance = 0.0f;
  } else if (distance > 10.0f) {
    distance = 10.0f;
  }

  if (std::abs(subsurfaceMeasurementDistance_ - distance) < 0.001f) {
    return;
  }

  subsurfaceMeasurementDistance_ = distance;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceRadiusScale(float scale) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceRadiusScale");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(scale)) {
    scale = 1.0f;
  }

  if (scale < 0.0f) {
    scale = 0.0f;
  } else if (scale > 10.0f) {
    scale = 10.0f;
  }

  if (std::abs(subsurfaceRadiusScale_ - scale) < 0.001f) {
    return;
  }

  subsurfaceRadiusScale_ = scale;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceMaxSampleRadius(float radius) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceMaxSampleRadius");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(radius)) {
    radius = 16.0f;
  }

  if (radius < 0.0f) {
    radius = 0.0f;
  } else if (radius > 64.0f) {
    radius = 64.0f;
  }

  if (std::abs(subsurfaceMaxSampleRadius_ - radius) < 0.001f) {
    return;
  }

  subsurfaceMaxSampleRadius_ = radius;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceVolumetricAnisotropy(float anisotropy) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceVolumetricAnisotropy");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(anisotropy)) {
    anisotropy = 0.0f;
  }

  if (anisotropy < -1.0f) {
    anisotropy = -1.0f;
  } else if (anisotropy > 1.0f) {
    anisotropy = 1.0f;
  }

  if (std::abs(subsurfaceVolumetricAnisotropy_ - anisotropy) < 0.001f) {
    return;
  }

  subsurfaceVolumetricAnisotropy_ = anisotropy;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceDiffusionProfileEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceDiffusionProfileEnabled");
  std::scoped_lock lock(mutex_);

  if (subsurfaceDiffusionProfileEnabled_ == enabled) {
    return;
  }

  subsurfaceDiffusionProfileEnabled_ = enabled;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setWaterTransmissionSettings(
    float red,
    float green,
    float blue,
    float measurementDistance,
    float refractiveIndex,
    bool diffuseLayerEnabled,
    float diffuseLayerScale,
    bool thinWalledEnabled,
    float thickness) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setWaterTransmissionSettings");
  std::scoped_lock lock(mutex_);

  const auto normalize = [](float value, float fallback, float minimum, float maximum) {
    return std::clamp(std::isfinite(value) ? value : fallback, minimum, maximum);
  };

  red = normalize(red, kWaterTransmittanceColor.x, kWaterMinTransmittanceColor, kWaterMaxTransmittanceColor);
  green = normalize(green, kWaterTransmittanceColor.y, kWaterMinTransmittanceColor, kWaterMaxTransmittanceColor);
  blue = normalize(blue, kWaterTransmittanceColor.z, kWaterMinTransmittanceColor, kWaterMaxTransmittanceColor);
  measurementDistance = normalize(
      measurementDistance,
      kWaterTransmittanceDistance,
      kWaterMinTransmittanceDistance,
      kWaterMaxTransmittanceDistance);
  refractiveIndex = normalize(
      refractiveIndex,
      kWaterRefractiveIndex,
      kWaterMinRefractiveIndex,
      kWaterMaxRefractiveIndex);
  diffuseLayerScale = normalize(
      diffuseLayerScale,
      kWaterDiffuseLayerScale,
      kWaterMinDiffuseLayerScale,
      kWaterMaxDiffuseLayerScale);
  thickness = normalize(
      thickness,
      kWaterDefaultThinWallThickness,
      kWaterThinWallThickness,
      kWaterMaxThinWallThickness);

  const bool unchanged =
      std::abs(waterTransmittanceColor_.x - red) < 0.0001f
      && std::abs(waterTransmittanceColor_.y - green) < 0.0001f
      && std::abs(waterTransmittanceColor_.z - blue) < 0.0001f
      && std::abs(waterTransmittanceDistance_ - measurementDistance) < 0.0001f
      && std::abs(waterRefractiveIndex_ - refractiveIndex) < 0.0001f
      && waterDiffuseLayerEnabled_ == diffuseLayerEnabled
      && std::abs(waterDiffuseLayerScale_ - diffuseLayerScale) < 0.0001f
      && waterThinWalledEnabled_ == thinWalledEnabled
      && std::abs(waterMaterialThickness_ - thickness) < 0.0001f;
  if (unchanged) {
    return;
  }

  waterTransmittanceColor_ = {red, green, blue};
  waterTransmittanceDistance_ = measurementDistance;
  waterRefractiveIndex_ = refractiveIndex;
  waterDiffuseLayerEnabled_ = diffuseLayerEnabled;
  waterDiffuseLayerScale_ = diffuseLayerScale;
  waterThinWalledEnabled_ = thinWalledEnabled;
  waterMaterialThickness_ = thickness;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

}  // namespace mcrtx
