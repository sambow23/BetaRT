#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

#include <cstdint>

namespace {

using mcrtx::CameraState;
using mcrtx::RemixRenderer;

}  // namespace

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nUpdateCamera(
    JNIEnv*, jclass,
    jdouble px, jdouble py, jdouble pz,
    jfloat fx, jfloat fy, jfloat fz,
    jfloat ux, jfloat uy, jfloat uz,
    jfloat rx, jfloat ry, jfloat rz,
    jfloat fovYDegrees,
    jfloat aspect,
    jfloat nearPlane,
    jfloat farPlane) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateCamera");
  CameraState camera;
  camera.position[0] = px;
  camera.position[1] = py;
  camera.position[2] = pz;
  camera.forward[0] = fx;
  camera.forward[1] = fy;
  camera.forward[2] = fz;
  camera.up[0] = ux;
  camera.up[1] = uy;
  camera.up[2] = uz;
  camera.right[0] = rx;
  camera.right[1] = ry;
  camera.right[2] = rz;
  camera.fovYDegrees = fovYDegrees;
  camera.aspect = aspect;
  camera.nearPlane = nearPlane;
  camera.farPlane = farPlane;
  RemixRenderer::instance().updateCamera(camera);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nUpdateCloudLayer(
    JNIEnv*, jclass,
    jboolean fancy,
    jfloat cameraX,
    jfloat cameraY,
    jfloat cameraZ,
    jfloat cloudHeight,
    jfloat cloudScroll,
    jfloat celestialAngle,
    jfloat colorR,
    jfloat colorG,
    jfloat colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateCloudLayer");
  RemixRenderer::instance().updateCloudLayer(
      fancy == JNI_TRUE,
      cameraX,
      cameraY,
      cameraZ,
      cloudHeight,
      cloudScroll,
      celestialAngle,
      colorR,
      colorG,
      colorB);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nUpdateAtmosphereState(
    JNIEnv*, jclass, jfloat celestialAngle, jboolean forceDarkAtmosphere) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateAtmosphereState");
  RemixRenderer::instance().updateAtmosphereState(celestialAngle, forceDarkAtmosphere == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nUpdateFogState(
    JNIEnv*, jclass,
    jint fogMode,
    jfloat colorR,
    jfloat colorG,
    jfloat colorB,
    jfloat fogScale,
    jfloat fogEnd,
    jfloat fogDensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateFogState");
  RemixRenderer::instance().updateFogState(
      static_cast<std::uint32_t>(fogMode),
      colorR,
      colorG,
      colorB,
      fogScale,
      fogEnd,
      fogDensity);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nClearCloudLayer(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearCloudLayer");
  RemixRenderer::instance().clearCloudLayer();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nClearWorldScene(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearWorldScene");
  RemixRenderer::instance().clearWorldScene();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixSceneBridge_nSetScreenTint(
    JNIEnv*, jclass, jfloat r, jfloat g, jfloat b, jfloat a) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetScreenTint");
  RemixRenderer::instance().setScreenTint(r, g, b, a);
}

}  // extern "C"
