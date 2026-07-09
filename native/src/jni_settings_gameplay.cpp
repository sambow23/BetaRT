#include "mcrtx/perf_log.hpp"
#include "mcrtx/remix_renderer.hpp"

#include <jni.h>

using mcrtx::RemixRenderer;

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGameplaySettingsNative_nSetPlayerShadowsEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetPlayerShadowsEnabled");
  RemixRenderer::instance().setPlayerShadowsEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGameplaySettingsNative_nSetHeldTorchLightsEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetHeldTorchLightsEnabled");
  RemixRenderer::instance().setHeldTorchLightsEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGameplaySettingsNative_nSetBlockOutlineEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetBlockOutlineEnabled");
  RemixRenderer::instance().setBlockOutlineEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGameplaySettingsNative_nSetBlockOutlineStyle(
    JNIEnv*, jclass, jint style) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetBlockOutlineStyle");
  RemixRenderer::instance().setBlockOutlineStyle(static_cast<int>(style));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGameplaySettingsNative_nSetBlockOutlineEmissiveIntensity(
    JNIEnv*, jclass, jfloat intensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetBlockOutlineEmissiveIntensity");
  RemixRenderer::instance().setBlockOutlineEmissiveIntensity(intensity);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxGameplaySettingsNative_nSetViewModelFovDegrees(
    JNIEnv*, jclass, jfloat fovYDegrees) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetViewModelFovDegrees");
  RemixRenderer::instance().setViewModelFovDegrees(fovYDegrees);
}

}  // extern "C"
