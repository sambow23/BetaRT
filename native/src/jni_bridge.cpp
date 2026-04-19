#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/perf_log.hpp"

#include <jni.h>
#include <string>

namespace {

using mcrtx::CameraState;
using mcrtx::RemixRenderer;

bool fromJniBoolean(bool value) {
  return value;
}

}  // namespace

extern "C" {

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nInitialize(
    JNIEnv*, jclass, jlong hwnd, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nInitialize");
  auto& renderer = RemixRenderer::instance();
  const bool ok = renderer.initialize(
      reinterpret_cast<HWND>(static_cast<intptr_t>(hwnd)),
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height));
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nShutdown(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nShutdown");
  RemixRenderer::instance().shutdown();
  ::mcrtx::perf::shutdown();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nResize(
    JNIEnv*, jclass, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nResize");
  RemixRenderer::instance().resize(static_cast<std::uint32_t>(width), static_cast<std::uint32_t>(height));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateCamera(
    JNIEnv*, jclass,
    jfloat px, jfloat py, jfloat pz,
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateCloudLayer(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateAtmosphereState(
    JNIEnv*, jclass, jfloat celestialAngle, jboolean forceDarkAtmosphere) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateAtmosphereState");
  RemixRenderer::instance().updateAtmosphereState(celestialAngle, forceDarkAtmosphere == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateFogState(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nClearCloudLayer(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearCloudLayer");
  RemixRenderer::instance().clearCloudLayer();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginDynamicEntityFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDynamicEntityFrame");
  RemixRenderer::instance().beginDynamicEntityFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginDynamicEntity(
    JNIEnv*, jclass, jint entityId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDynamicEntity");
  RemixRenderer::instance().beginDynamicEntity(entityId);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetDynamicEntityTexture(
    JNIEnv* env, jclass, jstring texturePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityTexture");
  if (texturePath == nullptr) {
    return;
  }

  const char* utfChars = env->GetStringUTFChars(texturePath, nullptr);
  if (utfChars == nullptr) {
    return;
  }

  RemixRenderer::instance().setDynamicEntityTexture(utfChars);
  env->ReleaseStringUTFChars(texturePath, utfChars);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetDynamicEntityBoneTransform(
    JNIEnv*, jclass,
    jint boneIndex,
    jfloat m00, jfloat m01, jfloat m02, jfloat m03,
    jfloat m10, jfloat m11, jfloat m12, jfloat m13,
    jfloat m20, jfloat m21, jfloat m22, jfloat m23) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityBoneTransform");
  if (boneIndex < 0) {
    return;
  }

  remixapi_Transform transform {};
  transform.matrix[0][0] = m00;
  transform.matrix[0][1] = m01;
  transform.matrix[0][2] = m02;
  transform.matrix[0][3] = m03;
  transform.matrix[1][0] = m10;
  transform.matrix[1][1] = m11;
  transform.matrix[1][2] = m12;
  transform.matrix[1][3] = m13;
  transform.matrix[2][0] = m20;
  transform.matrix[2][1] = m21;
  transform.matrix[2][2] = m22;
  transform.matrix[2][3] = m23;
  RemixRenderer::instance().setDynamicEntityBoneTransform(static_cast<std::uint32_t>(boneIndex), transform);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureDynamicEntityQuad(
    JNIEnv*, jclass,
    jfloat x0, jfloat y0, jfloat z0, jfloat u0, jfloat v0,
    jfloat x1, jfloat y1, jfloat z1, jfloat u1, jfloat v1,
    jfloat x2, jfloat y2, jfloat z2, jfloat u2, jfloat v2,
    jfloat x3, jfloat y3, jfloat z3, jfloat u3, jfloat v3,
    jint colorRgba,
    jint boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureDynamicEntityQuad");
  if (boneIndex < 0) {
    return;
  }

  RemixRenderer::instance().captureDynamicEntityQuad(
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3,
      static_cast<std::uint32_t>(colorRgba),
      static_cast<std::uint32_t>(boneIndex));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nEndDynamicEntity(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEndDynamicEntity");
  RemixRenderer::instance().endDynamicEntity();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginDestroyOverlayFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDestroyOverlayFrame");
  RemixRenderer::instance().beginDestroyOverlayFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureDestroyOverlay(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginParticleFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginParticleFrame");
  RemixRenderer::instance().beginParticleFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureParticleQuad(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nClearWorldScene(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearWorldScene");
  RemixRenderer::instance().clearWorldScene();
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginChunkBuild(
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
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureBlock(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nEndChunkBuild(
    JNIEnv*, jclass, jboolean emittedGeometry, jboolean deferNeighborRefresh, jboolean allowNeighborRefresh) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEndChunkBuild");
  RemixRenderer::instance().endChunkBuild(
      emittedGeometry == JNI_TRUE,
      deferNeighborRefresh == JNI_TRUE,
      allowNeighborRefresh == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nFlushChunkNeighborRefreshes(
    JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFlushChunkNeighborRefreshes");
  RemixRenderer::instance().flushChunkNeighborRefreshes();
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nDrawScreenOverlay(
    JNIEnv* env,
    jclass,
    jobject pixelBuffer,
    jint width,
    jint height,
    jint format,
    jfloat opacity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nDrawScreenOverlay");
  if (pixelBuffer == nullptr) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  void* pixelData = env->GetDirectBufferAddress(pixelBuffer);
  if (pixelData == nullptr) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  const bool ok = RemixRenderer::instance().drawScreenOverlay(
      pixelData,
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height),
      static_cast<remixapi_Format>(format),
      opacity);
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nClearScreenOverlay(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearScreenOverlay");
  return static_cast<jboolean>(fromJniBoolean(RemixRenderer::instance().clearScreenOverlay()));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetUiState(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetUiState");
  return static_cast<jint>(RemixRenderer::instance().getUiState());
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetUiState(
    JNIEnv*, jclass, jint state) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetUiState");
  return static_cast<jboolean>(fromJniBoolean(
      RemixRenderer::instance().setUiState(static_cast<remixapi_UIState>(state))));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nPresent(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nPresent");
  return static_cast<jboolean>(fromJniBoolean(RemixRenderer::instance().present()));
}

JNIEXPORT jstring JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetLastError(JNIEnv* env, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetLastError");
  const auto message = RemixRenderer::instance().lastError();
  return env->NewStringUTF(message.c_str());
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRecordJavaSample(
    JNIEnv* env, jclass, jint side, jstring site, jlong nanoseconds) {
  if (site == nullptr) {
    return;
  }
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) {
    return;
  }
  const ::mcrtx::perf::Side sideEnum = (side == 0)
      ? ::mcrtx::perf::Side::Hook
      : ::mcrtx::perf::Side::Call;
  ::mcrtx::perf::recordDuration(sideEnum, utfChars, static_cast<std::uint64_t>(nanoseconds));
  env->ReleaseStringUTFChars(site, utfChars);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nFlushJavaFrame(JNIEnv*, jclass) {
  // Currently a no-op — Java samples are recorded synchronously by
  // nRecordJavaSample. Kept as a JNI entry point so the Java helper can call
  // it on frame boundaries without needing ABI changes if batching is added
  // later.
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRegisterPerfSite(
    JNIEnv* env, jclass, jint side, jstring site) {
  if (site == nullptr) return -1;
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) return -1;
  ::mcrtx::perf::Side sideEnum;
  switch (side) {
    case 0: sideEnum = ::mcrtx::perf::Side::Hook; break;
    case 1: sideEnum = ::mcrtx::perf::Side::Call; break;
    case 2: sideEnum = ::mcrtx::perf::Side::Jni; break;
    case 3: sideEnum = ::mcrtx::perf::Side::Native; break;
    case 4: sideEnum = ::mcrtx::perf::Side::Remix; break;
    default: sideEnum = ::mcrtx::perf::Side::Hook; break;
  }
  const int id = ::mcrtx::perf::registerSite(sideEnum, utfChars);
  env->ReleaseStringUTFChars(site, utfChars);
  return static_cast<jint>(id);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRecordJavaSampleBatch(
    JNIEnv* env, jclass, jintArray ids, jlongArray nanos, jint count) {
  if (ids == nullptr || nanos == nullptr || count <= 0) return;
  void* idsPtr = env->GetPrimitiveArrayCritical(ids, nullptr);
  if (idsPtr == nullptr) return;
  void* nanosPtr = env->GetPrimitiveArrayCritical(nanos, nullptr);
  if (nanosPtr == nullptr) {
    env->ReleasePrimitiveArrayCritical(ids, idsPtr, JNI_ABORT);
    return;
  }
  static_assert(sizeof(jlong) == sizeof(std::uint64_t),
                "jlong must be 64-bit for the perf batch fast path");
  ::mcrtx::perf::recordDurationsBatch(
      reinterpret_cast<const int*>(idsPtr),
      reinterpret_cast<const std::uint64_t*>(nanosPtr),
      static_cast<std::size_t>(count));
  env->ReleasePrimitiveArrayCritical(nanos, nanosPtr, JNI_ABORT);
  env->ReleasePrimitiveArrayCritical(ids, idsPtr, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRecordJavaCount(
    JNIEnv* env, jclass, jint side, jstring site, jlong count) {
  if (site == nullptr) {
    return;
  }
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) {
    return;
  }
  const ::mcrtx::perf::Side sideEnum = (side == 0)
      ? ::mcrtx::perf::Side::Hook
      : ::mcrtx::perf::Side::Call;
  ::mcrtx::perf::recordCount(sideEnum, utfChars, static_cast<std::uint64_t>(count));
  env->ReleaseStringUTFChars(site, utfChars);
}

}  // extern "C"
