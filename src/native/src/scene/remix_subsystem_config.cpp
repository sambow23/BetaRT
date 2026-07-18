#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/scene/remix_cloud_mode.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>
#include <iomanip>
#include <sstream>
#include <string>
#include <string_view>
#include <utility>

namespace mcrtx {

using namespace mcrtx::detail;
namespace {

constexpr double kPi = 3.14159265358979323846;

std::string formatConfigFloat(float value, int precision) {
  std::ostringstream stream;
  stream << std::fixed << std::setprecision(precision) << value;
  return stream.str();
}

const char* dlssQualityConfigValue(int preset) {
  switch (preset) {
    case 3:
      return "3";
    case 2:
      return "2";
    case 1:
      return "1";
    case 0:
      return "0";
    case 5:
      return "5";
    case 4:
    default:
      return "4";
  }
}

const char* xessPresetConfigValue(int preset) {
  switch (preset) {
    case 0:
      return "0";
    case 1:
      return "1";
    case 3:
      return "3";
    case 4:
      return "4";
    case 5:
      return "5";
    case 6:
      return "6";
    case 2:
    default:
      return "2";
  }
}

const char* taauPresetConfigValue(int preset) {
  switch (preset) {
    case 0:
      return "0";
    case 1:
      return "1";
    case 3:
      return "3";
    case 4:
      return "4";
    case 2:
    default:
      return "2";
  }
}

}  // namespace

bool RemixRenderer::setConfigVariableLocked(
    std::string_view key,
    const std::string& value,
    bool logChange,
    bool force) {
  if (remix_.SetConfigVariable == nullptr) {
    if (!warnedMissingSetConfigVariable_) {
      warnedMissingSetConfigVariable_ = true;
      log("SetConfigVariable not available; runtime Remix config updates are disabled");
    }
    return false;
  }

  std::string keyString(key);
  if (!force) {
    const auto existing = appliedRemixConfigValues_.find(keyString);
    if (existing != appliedRemixConfigValues_.end() && existing->second == value) {
      return true;
    }
  }

  const remixapi_ErrorCode result = remix_.SetConfigVariable(keyString.c_str(), value.c_str());
  if (logChange || result != REMIXAPI_ERROR_CODE_SUCCESS) {
    log(std::string("SetConfigVariable ") + keyString + "=" + value + " -> " + errorCodeToString(result));
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    return false;
  }

  appliedRemixConfigValues_[std::move(keyString)] = value;
  return true;
}

bool RemixRenderer::setConfigFloatLocked(
    std::string_view key,
    float value,
    int precision,
    bool logChange,
    bool force) {
  return setConfigVariableLocked(key, formatConfigFloat(value, precision), logChange, force);
}

bool RemixRenderer::setGameValueLocked(
    std::string_view key,
    const std::string& value,
    bool logChange) {
  if (remix_.SetGameValue == nullptr) {
    if (!warnedMissingSetGameValue_) {
      warnedMissingSetGameValue_ = true;
      log("SetGameValue not available; runtime Remix game-state updates are disabled");
    }
    return false;
  }

  std::string keyString(key);
  const auto existing = appliedGameStateValues_.find(keyString);
  if (existing != appliedGameStateValues_.end() && existing->second == value) {
    return true;
  }

  const remixapi_ErrorCode result = remix_.SetGameValue(keyString.c_str(), value.c_str());
  if (logChange || result != REMIXAPI_ERROR_CODE_SUCCESS) {
    log(std::string("SetGameValue ") + keyString + "=" + value + " -> " + errorCodeToString(result));
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    return false;
  }

  appliedGameStateValues_[std::move(keyString)] = value;
  return true;
}

bool RemixRenderer::setGameValueFloatLocked(
    std::string_view key,
    float value,
    int precision,
    bool logChange) {
  return setGameValueLocked(key, formatConfigFloat(value, precision), logChange);
}

void RemixRenderer::publishWorldRenderOriginLocked(const WorldRenderOrigin& origin) {
  const auto originValues = makeWorldRenderOriginGameValues(origin);
  for (const WorldRenderOriginGameValue& originValue : originValues) {
    setGameValueLocked(originValue.key, originValue.value, false);
  }
}

void RemixRenderer::applyRemixConfigPreStartupLocked() {
  setConfigVariableLocked("rtx.sceneScale", "0.01", true);
  applyRtQualityConfigLocked();
}

void RemixRenderer::applyRtQualityConfigLocked() {
  switch (rtQuality_) {
    case kRtQualityPotato:
      setConfigVariableLocked("rtx.volumetrics.initialRISSampleCount", "1", true, true);
      setConfigVariableLocked("rtx.di.initialSampleCount", "1", true, true);
      setConfigVariableLocked("rtx.di.enableBestLightSampling", "False", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserConfidence", "False", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserGradient", "False", true, true);
      setConfigVariableLocked("rtx.pathMinBounces", "0", true, true);
      setConfigVariableLocked("rtx.pathMaxBounces", "1", true, true);
      setConfigVariableLocked("rtx.risLightSampleCount", "1", true, true);
      setConfigVariableLocked("rtx.denoiseDirectAndIndirectLightingSeparately", "False", true, true);
      setConfigVariableLocked("rtx.integrateIndirectMode", "0", true, true);
      setConfigVariableLocked("rtx.postfx.enable", "False", true, true);
      setConfigVariableLocked("rtx.useRTXDI", "False", true, true);
      setConfigVariableLocked("rtx.neeCache.enable", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedResolveInIndirectRays", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedEmissiveParticlesInIndirectRays", "False", true, true);
      break;
    case kRtQualityLow:
      setConfigVariableLocked("rtx.volumetrics.initialRISSampleCount", "8", true, true);
      setConfigVariableLocked("rtx.di.initialSampleCount", "4", true, true);
      setConfigVariableLocked("rtx.di.enableBestLightSampling", "False", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserConfidence", "False", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserGradient", "False", true, true);
      setConfigVariableLocked("rtx.pathMinBounces", "0", true, true);
      setConfigVariableLocked("rtx.pathMaxBounces", "1", true, true);
      setConfigVariableLocked("rtx.risLightSampleCount", "4", true, true);
      setConfigVariableLocked("rtx.denoiseDirectAndIndirectLightingSeparately", "False", true, true);
      setConfigVariableLocked("rtx.integrateIndirectMode", "0", true, true);
      setConfigVariableLocked("rtx.postfx.enable", "True", true, true);
      setConfigVariableLocked("rtx.useRTXDI", "False", true, true);
      setConfigVariableLocked("rtx.neeCache.enable", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedResolveInIndirectRays", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedEmissiveParticlesInIndirectRays", "False", true, true);
      break;
    case kRtQualityMedium:
      setConfigVariableLocked("rtx.volumetrics.initialRISSampleCount", "16", true, true);
      setConfigVariableLocked("rtx.di.initialSampleCount", "8", true, true);
      setConfigVariableLocked("rtx.di.enableBestLightSampling", "True", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserConfidence", "False", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserGradient", "False", true, true);
      setConfigVariableLocked("rtx.pathMinBounces", "1", true, true);
      setConfigVariableLocked("rtx.pathMaxBounces", "2", true, true);
      setConfigVariableLocked("rtx.risLightSampleCount", "8", true, true);
      setConfigVariableLocked("rtx.denoiseDirectAndIndirectLightingSeparately", "False", true, true);
      setConfigVariableLocked("rtx.integrateIndirectMode", "1", true, true);
      setConfigVariableLocked("rtx.postfx.enable", "True", true, true);
      setConfigVariableLocked("rtx.useRTXDI", "True", true, true);
      setConfigVariableLocked("rtx.neeCache.enable", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedResolveInIndirectRays", "True", true, true);
      setConfigVariableLocked("rtx.enableUnorderedEmissiveParticlesInIndirectRays", "False", true, true);
      break;
    case kRtQualityUltra:
      setConfigVariableLocked("rtx.volumetrics.initialRISSampleCount", "64", true, true);
      setConfigVariableLocked("rtx.di.initialSampleCount", "32", true, true);
      setConfigVariableLocked("rtx.di.enableBestLightSampling", "True", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserConfidence", "True", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserGradient", "True", true, true);
      setConfigVariableLocked("rtx.pathMinBounces", "1", true, true);
      setConfigVariableLocked("rtx.pathMaxBounces", "8", true, true);
      setConfigVariableLocked("rtx.risLightSampleCount", "32", true, true);
      setConfigVariableLocked("rtx.denoiseDirectAndIndirectLightingSeparately", "True", true, true);
      setConfigVariableLocked("rtx.integrateIndirectMode", "2", true, true);
      setConfigVariableLocked("rtx.postfx.enable", "True", true, true);
      setConfigVariableLocked("rtx.useRTXDI", "True", true, true);
      setConfigVariableLocked("rtx.neeCache.enable", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedResolveInIndirectRays", "True", true, true);
      setConfigVariableLocked("rtx.enableUnorderedEmissiveParticlesInIndirectRays", "True", true, true);
      break;
    case kRtQualityHigh:
    default:
      setConfigVariableLocked("rtx.volumetrics.initialRISSampleCount", "32", true, true);
      setConfigVariableLocked("rtx.di.initialSampleCount", "16", true, true);
      setConfigVariableLocked("rtx.di.enableBestLightSampling", "True", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserConfidence", "True", true, true);
      setConfigVariableLocked("rtx.di.enableDenoiserGradient", "True", true, true);
      setConfigVariableLocked("rtx.pathMinBounces", "1", true, true);
      setConfigVariableLocked("rtx.pathMaxBounces", "4", true, true);
      setConfigVariableLocked("rtx.risLightSampleCount", "16", true, true);
      setConfigVariableLocked("rtx.denoiseDirectAndIndirectLightingSeparately", "True", true, true);
      setConfigVariableLocked("rtx.integrateIndirectMode", "2", true, true);
      setConfigVariableLocked("rtx.postfx.enable", "True", true, true);
      setConfigVariableLocked("rtx.useRTXDI", "True", true, true);
      setConfigVariableLocked("rtx.neeCache.enable", "False", true, true);
      setConfigVariableLocked("rtx.enableUnorderedResolveInIndirectRays", "True", true, true);
      setConfigVariableLocked("rtx.enableUnorderedEmissiveParticlesInIndirectRays", "False", true, true);
      break;
  }
}

void RemixRenderer::applyUpscalerConfigLocked() {
  switch (upscalerType_) {
    case 0:
      setConfigVariableLocked("rtx.upscalerType", "0", true, true);
      setConfigVariableLocked("rtx.enableRayReconstruction", "False", true, true);
      setConfigVariableLocked("rtx.sparseRendering.enableSparseRendering", "False", true, true);
      setConfigVariableLocked("rtx.reflexMode", "0", true, true);
      break;
    case 4:
      setConfigVariableLocked("rtx.upscalerType", "4", true, true);
      setConfigVariableLocked("rtx.xess.preset", xessPresetConfigValue(xessPreset_), true, true);
      setConfigVariableLocked("rtx.enableRayReconstruction", "False", true, true);
      setConfigVariableLocked("rtx.sparseRendering.enableSparseRendering", "False", true, true);
      setConfigVariableLocked("rtx.reflexMode", "0", true, true);
      break;
    case 3:
      setConfigVariableLocked("rtx.upscalerType", "3", true, true);
      setConfigVariableLocked("rtx.taauPreset", taauPresetConfigValue(taauPreset_), true, true);
      setConfigVariableLocked("rtx.enableRayReconstruction", "False", true, true);
      setConfigVariableLocked("rtx.sparseRendering.enableSparseRendering", "False", true, true);
      setConfigVariableLocked("rtx.reflexMode", "0", true, true);
      break;
    case 1:
    default:
      setConfigVariableLocked("rtx.upscalerType", "1", true, true);
      setConfigVariableLocked("rtx.qualityDLSS", dlssQualityConfigValue(dlssPreset_), true, true);
      setConfigVariableLocked(
          "rtx.enableRayReconstruction",
          rayReconstructionEnabled_ ? "True" : "False",
          true,
          true);
      setConfigVariableLocked(
          "rtx.sparseRendering.enableSparseRendering",
          sparseRenderingEnabled_ ? "True" : "False",
          true,
          true);
      setConfigVariableLocked("rtx.reflexMode", "1", true, true);
      break;
  }
}

void RemixRenderer::applyRemixAtmosphereCloudConfigLocked() {
  for (const RemixConfigValue& configValue :
       remixAtmosphereCloudConfigValues(remixAtmosphereCloudsEnabled_)) {
    setConfigVariableLocked(configValue.key, std::string(configValue.value), true, true);
  }
}

void RemixRenderer::applyRemixConfigPostStartupLocked() {
  applyRemixAtmosphereCloudConfigLocked();
  applyRtQualityConfigLocked();
  applyUpscalerConfigLocked();
  setConfigVariableLocked(
      "rtx.playerModel.enablePrimaryShadows",
      playerShadowsEnabled_ ? "True" : "False",
      true,
      true);
}

void RemixRenderer::publishCelestialTexturePathsLocked() {
  if (sunTexturePath_.empty()) {
    sunTexturePath_ = resolveSunTexturePath();
    if (sunTexturePath_.empty()) {
      log("Sun texture asset not found; Numos sun disk texture will be skipped");
    }
  }

  if (moonTexturePath_.empty()) {
    moonTexturePath_ = resolveMoonTexturePath();
    if (moonTexturePath_.empty()) {
      log("Moon texture asset not found; Numos moon disk will use the procedural fallback");
    }
  }

  const auto values = makeCelestialTextureGameValues({
      sunTexturePath_,
      moonTexturePath_,
  });
  for (const auto& value : values) {
    if (!value.second.empty()) {
      setGameValueLocked(value.first, value.second, false);
    }
  }
}

void RemixRenderer::updateAtmosphereConfigLocked(float celestialAngle, bool forceDarkAtmosphere) {
  publishCelestialTexturePathsLocked();

  // Skyless dimensions (Nether): the runtime suppresses physical sky/star
  // sampling and forces full fog opacity on miss pixels while this flag is set.
  setGameValueLocked("__atmosphere.skyless", forceDarkAtmosphere ? "1" : "0", false);

  if (forceDarkAtmosphere) {
    setConfigFloatLocked("rtx.atmosphere.sunElevation", -30.0f, 2, false);
    setGameValueLocked("__atmosphere.moon0.enabled", "0", false);
    return;
  }

  const float wrappedAngle = celestialAngle - std::floor(celestialAngle);
  const double rotationRadians = static_cast<double>(wrappedAngle) * 2.0 * kPi;
  const double elevationRadians = std::asin(std::clamp(std::cos(rotationRadians), -1.0, 1.0));
  const float elevationDegrees = static_cast<float>(elevationRadians * 180.0 / kPi);
  const float sunRotationDegrees = std::sin(rotationRadians) < 0.0 ? 180.0f : 0.0f;
  setConfigFloatLocked("rtx.atmosphere.sunElevation", elevationDegrees, 2, false);
  setConfigFloatLocked("rtx.atmosphere.sunRotation", sunRotationDegrees, 2, false);

  // Minecraft's moon sits directly opposite the sun on the celestial wheel:
  // mirrored elevation and a 180-degree-flipped rotation. Beta 1.7.3 has a
  // single full-moon texture, so the phase stays fixed at full (0.5).
  setGameValueLocked("__atmosphere.moon0.enabled", "1", false);
  setGameValueFloatLocked("__atmosphere.moon0.elevation", -elevationDegrees, 2, false);
  setGameValueFloatLocked(
      "__atmosphere.moon0.rotation",
      sunRotationDegrees < 90.0f ? 180.0f : 0.0f,
      2,
      false);
  setGameValueFloatLocked("__atmosphere.moon0.phase", 0.5f, 2, false);
}

} // namespace mcrtx
