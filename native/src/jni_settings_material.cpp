#include "mcrtx/perf_log.hpp"
#include "mcrtx/remix_renderer.hpp"

#include <jni.h>

using mcrtx::RemixRenderer;

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetDisplacementFactor(
    JNIEnv*, jclass, jfloat factor) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDisplacementFactor");
  RemixRenderer::instance().setDisplacementFactor(factor);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetSubsurfaceMeasurementDistance(
    JNIEnv*, jclass, jfloat distance) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetSubsurfaceMeasurementDistance");
  RemixRenderer::instance().setSubsurfaceMeasurementDistance(distance);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetSubsurfaceRadiusScale(
    JNIEnv*, jclass, jfloat scale) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetSubsurfaceRadiusScale");
  RemixRenderer::instance().setSubsurfaceRadiusScale(scale);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetSubsurfaceMaxSampleRadius(
    JNIEnv*, jclass, jfloat radius) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetSubsurfaceMaxSampleRadius");
  RemixRenderer::instance().setSubsurfaceMaxSampleRadius(radius);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetSubsurfaceVolumetricAnisotropy(
    JNIEnv*, jclass, jfloat anisotropy) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetSubsurfaceVolumetricAnisotropy");
  RemixRenderer::instance().setSubsurfaceVolumetricAnisotropy(anisotropy);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetSubsurfaceDiffusionProfileEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetSubsurfaceDiffusionProfileEnabled");
  RemixRenderer::instance().setSubsurfaceDiffusionProfileEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxMaterialSettingsNative_nSetWaterTransmissionSettings(
    JNIEnv*,
    jclass,
    jfloat red,
    jfloat green,
    jfloat blue,
    jfloat measurementDistance,
    jfloat refractiveIndex,
    jboolean diffuseLayerEnabled,
    jboolean thinWalledEnabled,
    jfloat thickness) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetWaterTransmissionSettings");
  RemixRenderer::instance().setWaterTransmissionSettings(
      red,
      green,
      blue,
      measurementDistance,
      refractiveIndex,
      diffuseLayerEnabled == JNI_TRUE,
      thinWalledEnabled == JNI_TRUE,
      thickness);
}

}  // extern "C"
