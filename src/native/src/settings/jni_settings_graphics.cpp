#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

using mcrtx::RemixRenderer;

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetRtQuality(
    JNIEnv*, jclass, jint rtQuality) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetRtQuality");
  RemixRenderer::instance().setRtQuality(static_cast<int>(rtQuality));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetUpscalerConfig(
    JNIEnv*,
    jclass,
    jint upscalerType,
    jint dlssPreset,
    jint xessPreset,
    jint taauPreset,
    jboolean rayReconstructionEnabled,
    jboolean sparseRenderingEnabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetUpscalerConfig");
  RemixRenderer::instance().setUpscalerConfig(
      static_cast<int>(upscalerType),
      static_cast<int>(dlssPreset),
      static_cast<int>(xessPreset),
      static_cast<int>(taauPreset),
      rayReconstructionEnabled == JNI_TRUE,
      sparseRenderingEnabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetFrameGenerationConfig(
    JNIEnv*, jclass, jboolean enabled, jint multiplier) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetFrameGenerationConfig");
  RemixRenderer::instance().setFrameGenerationConfig(
      enabled == JNI_TRUE, static_cast<int>(multiplier));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nGetCompiledFeatureMask(
    JNIEnv*, jclass) {
  return static_cast<jint>(RemixRenderer::instance().compiledFeatureMask());
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nGetAvailableFeatureMask(
    JNIEnv*, jclass) {
  return static_cast<jint>(RemixRenderer::instance().availableFeatureMask());
}

JNIEXPORT jint JNICALL
Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nGetDlssFrameGenerationMaxInterpolatedFrames(
    JNIEnv*, jclass) {
  return static_cast<jint>(
      RemixRenderer::instance().dlssFrameGenerationMaxInterpolatedFrames());
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetRemixAtmosphereCloudsEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetRemixAtmosphereCloudsEnabled");
  RemixRenderer::instance().setRemixAtmosphereCloudsEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetAerialPerspectiveEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetAerialPerspectiveEnabled");
  RemixRenderer::instance().setAerialPerspectiveEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetAerialPerspectiveStrength(
    JNIEnv*, jclass, jint strength) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetAerialPerspectiveStrength");
  RemixRenderer::instance().setAerialPerspectiveStrength(static_cast<int>(strength));
}

JNIEXPORT void JNICALL
Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetAerialPerspectiveSceneShadowEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetAerialPerspectiveSceneShadowEnabled");
  RemixRenderer::instance().setAerialPerspectiveSceneShadowEnabled(enabled == JNI_TRUE);
}

}  // extern "C"
