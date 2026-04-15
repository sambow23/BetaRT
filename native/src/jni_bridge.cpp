#include "mcrtx/remix_renderer.hpp"

#include <jni.h>

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
  auto& renderer = RemixRenderer::instance();
  const bool ok = renderer.initialize(
      reinterpret_cast<HWND>(static_cast<intptr_t>(hwnd)),
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height));
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nShutdown(JNIEnv*, jclass) {
  RemixRenderer::instance().shutdown();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nResize(
    JNIEnv*, jclass, jint width, jint height) {
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

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginChunkBuild(
    JNIEnv*, jclass,
    jint originX, jint originY, jint originZ,
    jint sizeX, jint sizeY, jint sizeZ,
    jint renderPass) {
  const bool ok = RemixRenderer::instance().beginChunkBuild(
      originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureBlock(
    JNIEnv*, jclass,
    jint blockX, jint blockY, jint blockZ,
    jint blockId, jint blockMetadata, jint renderType,
    jint texture0, jint texture1, jint texture2,
    jint texture3, jint texture4, jint texture5) {
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
      texture5);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nEndChunkBuild(
    JNIEnv*, jclass, jboolean emittedGeometry) {
  RemixRenderer::instance().endChunkBuild(emittedGeometry == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nPresent(JNIEnv*, jclass) {
  return static_cast<jboolean>(fromJniBoolean(RemixRenderer::instance().present()));
}

JNIEXPORT jstring JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetLastError(JNIEnv* env, jclass) {
  const auto message = RemixRenderer::instance().lastError();
  return env->NewStringUTF(message.c_str());
}

}  // extern "C"
