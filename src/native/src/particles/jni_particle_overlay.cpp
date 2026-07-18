#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

#include <cstdint>

namespace {

using mcrtx::RemixRenderer;

}  // namespace

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixParticleOverlayBridge_nBeginDestroyOverlayFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDestroyOverlayFrame");
  RemixRenderer::instance().beginDestroyOverlayFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixParticleOverlayBridge_nBeginBlockOutlineFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginBlockOutlineFrame");
  RemixRenderer::instance().beginBlockOutlineFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixParticleOverlayBridge_nCaptureDestroyOverlay(
    JNIEnv*, jclass,
    jint blockX,
    jint blockY,
    jint blockZ,
    jint blockId,
    jint blockMetadata,
    jint renderType,
    jint destroyStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureDestroyOverlay");
  RemixRenderer::instance().captureDestroyOverlay(
      blockX,
      blockY,
      blockZ,
      blockId,
      blockMetadata,
      renderType,
      destroyStage);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixParticleOverlayBridge_nCaptureBlockOutline(
    JNIEnv*, jclass,
    jint blockX,
    jint blockY,
    jint blockZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureBlockOutline");
  RemixRenderer::instance().captureBlockOutline(blockX, blockY, blockZ);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixParticleOverlayBridge_nBeginParticleFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginParticleFrame");
  RemixRenderer::instance().beginParticleFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixParticleOverlayBridge_nCaptureParticleQuad(
    JNIEnv*, jclass,
    jfloat x0, jfloat y0, jfloat z0, jfloat u0, jfloat v0,
    jfloat x1, jfloat y1, jfloat z1, jfloat u1, jfloat v1,
    jfloat x2, jfloat y2, jfloat z2, jfloat u2, jfloat v2,
    jfloat x3, jfloat y3, jfloat z3, jfloat u3, jfloat v3,
    jint colorRgba,
    jint textureKind) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureParticleQuad");
  if (textureKind < 0) {
    return;
  }

  RemixRenderer::instance().captureParticleQuad(
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3,
      static_cast<std::uint32_t>(colorRgba),
      static_cast<std::uint32_t>(textureKind));
}

}  // extern "C"
