#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

namespace {

using mcrtx::RemixRenderer;

}  // namespace

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nUnloadChunkSection(
    JNIEnv*, jclass, jint originX, jint originY, jint originZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUnloadChunkSection");
  RemixRenderer::instance().unloadChunkSection(
      static_cast<int>(originX),
      static_cast<int>(originY),
      static_cast<int>(originZ));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nSetChunkSectionHidden(
    JNIEnv*, jclass, jint originX, jint originY, jint originZ, jboolean hidden) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetChunkSectionHidden");
  RemixRenderer::instance().setChunkSectionHidden(
      static_cast<int>(originX),
      static_cast<int>(originY),
      static_cast<int>(originZ),
      hidden != JNI_FALSE);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixChunkBridge_nBeginChunkBuild(
    JNIEnv*, jclass,
    jint originX, jint originY, jint originZ,
    jint sizeX, jint sizeY, jint sizeZ,
    jint dirtyMinX, jint dirtyMinY, jint dirtyMinZ,
    jint dirtyMaxX, jint dirtyMaxY, jint dirtyMaxZ,
    jint renderPass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginChunkBuild");
  const bool ok = RemixRenderer::instance().beginChunkBuild(
      originX,
      originY,
      originZ,
      sizeX,
      sizeY,
      sizeZ,
      dirtyMinX,
      dirtyMinY,
      dirtyMinZ,
      dirtyMaxX,
      dirtyMaxY,
      dirtyMaxZ,
      renderPass);
  return mcrtx::jni::toJniBoolean(ok);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nCaptureBlock(
    JNIEnv*, jclass,
    jint blockX, jint blockY, jint blockZ,
    jint blockId, jint blockMetadata, jint renderType,
    jint texture0, jint texture1, jint texture2,
    jint texture3, jint texture4, jint texture5,
    jfloat boundsMinX,
    jfloat boundsMinY,
    jfloat boundsMinZ,
    jfloat boundsMaxX,
    jfloat boundsMaxY,
    jfloat boundsMaxZ,
    jint blockColorRgb,
    jint liquidVisibilityMask,
    jfloat liquidHeight0,
    jfloat liquidHeight1,
    jfloat liquidHeight2,
    jfloat liquidHeight3,
    jfloat liquidFlowAngle) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureBlock");
  RemixRenderer::instance().captureBlock(
      blockX,
      blockY,
      blockZ,
      blockId,
      blockMetadata,
      renderType,
      texture0,
      texture1,
      texture2,
      texture3,
      texture4,
      texture5,
      boundsMinX,
      boundsMinY,
      boundsMinZ,
      boundsMaxX,
      boundsMaxY,
      boundsMaxZ,
      blockColorRgb,
      liquidVisibilityMask,
      liquidHeight0,
      liquidHeight1,
      liquidHeight2,
      liquidHeight3,
      liquidFlowAngle);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nEndChunkBuild(
    JNIEnv*, jclass, jboolean emittedGeometry, jboolean deferNeighborRefresh, jboolean allowNeighborRefresh) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEndChunkBuild");
  RemixRenderer::instance().endChunkBuild(
      emittedGeometry == JNI_TRUE,
      deferNeighborRefresh == JNI_TRUE,
      allowNeighborRefresh == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixChunkBridge_nFlushChunkNeighborRefreshes(
    JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFlushChunkNeighborRefreshes");
  RemixRenderer::instance().flushChunkNeighborRefreshes();
}

}  // extern "C"
