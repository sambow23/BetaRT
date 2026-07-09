#include "mcrtx/perf_log.hpp"
#include "mcrtx/remix_renderer.hpp"

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

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGraphicsSettingsNative_nSetRemixAtmosphereCloudsEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetRemixAtmosphereCloudsEnabled");
  RemixRenderer::instance().setRemixAtmosphereCloudsEnabled(enabled == JNI_TRUE);
}

}  // extern "C"
