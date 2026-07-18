#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

#include <cstdint>

namespace {

using mcrtx::RemixRenderer;

}  // namespace

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nBeginDynamicEntityFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDynamicEntityFrame");
  RemixRenderer::instance().beginDynamicEntityFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nBeginDynamicEntity(
    JNIEnv*, jclass, jint entityId, jint hurtStage, jint creeperFuseStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDynamicEntity");
  const std::uint32_t clampedHurtStage = hurtStage < 0 ? 0u : static_cast<std::uint32_t>(hurtStage);
  const std::uint32_t clampedCreeperFuseStage = creeperFuseStage < 0 ? 0u : static_cast<std::uint32_t>(creeperFuseStage);
  RemixRenderer::instance().beginDynamicEntity(
      static_cast<int>(entityId),
      clampedHurtStage,
      clampedCreeperFuseStage);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nSetDynamicEntityTexture(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nSetFirstPersonHeldItem(
    JNIEnv*, jclass, jint itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetFirstPersonHeldItem");
  RemixRenderer::instance().setFirstPersonHeldItem(itemId);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nSetEntityHeldTorch(
    JNIEnv*, jclass, jint entityId, jdouble worldX, jdouble worldY, jdouble worldZ, jint itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetEntityHeldTorch");
  RemixRenderer::instance().setEntityHeldTorch(entityId, worldX, worldY, worldZ, itemId);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nSetDynamicEntityBoneTransform(
    JNIEnv*, jclass,
    jint boneIndex,
    jfloat m00, jfloat m01, jfloat m02, jdouble m03,
    jfloat m10, jfloat m11, jfloat m12, jdouble m13,
    jfloat m20, jfloat m21, jfloat m22, jdouble m23) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityBoneTransform");
  if (boneIndex < 0) {
    return;
  }

  remixapi_Transform transform {};
  transform.matrix[0][0] = m00;
  transform.matrix[0][1] = m01;
  transform.matrix[0][2] = m02;
  transform.matrix[0][3] = 0.0f;
  transform.matrix[1][0] = m10;
  transform.matrix[1][1] = m11;
  transform.matrix[1][2] = m12;
  transform.matrix[1][3] = 0.0f;
  transform.matrix[2][0] = m20;
  transform.matrix[2][1] = m21;
  transform.matrix[2][2] = m22;
  transform.matrix[2][3] = 0.0f;
  RemixRenderer::instance().setDynamicEntityBoneTransform(
      static_cast<std::uint32_t>(boneIndex),
      transform,
      m03,
      m13,
      m23);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nCaptureDynamicEntityQuad(
    JNIEnv*, jclass,
    jfloat x0, jfloat y0, jfloat z0, jfloat u0, jfloat v0,
    jfloat x1, jfloat y1, jfloat z1, jfloat u1, jfloat v1,
    jfloat x2, jfloat y2, jfloat z2, jfloat u2, jfloat v2,
    jfloat x3, jfloat y3, jfloat z3, jfloat u3, jfloat v3,
    jint colorRgba,
    jboolean blendEnabled,
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
      blendEnabled == JNI_TRUE,
      static_cast<std::uint32_t>(boneIndex));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nCaptureDynamicEntityQuadBatch(
    JNIEnv* env, jclass,
    jfloatArray vertices,
    jint quadCount,
    jint colorRgba,
    jboolean blendEnabled,
    jint boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureDynamicEntityQuadBatch");
  if (boneIndex < 0 || quadCount <= 0 || vertices == nullptr) {
    return;
  }

  const jsize available = env->GetArrayLength(vertices);
  const jlong required = static_cast<jlong>(quadCount) * 20;
  if (required > available) {
    return;
  }

  auto* data = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(vertices, nullptr));
  if (data == nullptr) {
    return;
  }

  RemixRenderer::instance().captureDynamicEntityQuadBatch(
      reinterpret_cast<const float*>(data),
      static_cast<std::uint32_t>(quadCount),
      static_cast<std::uint32_t>(colorRgba),
      blendEnabled == JNI_TRUE,
      static_cast<std::uint32_t>(boneIndex));

  env->ReleasePrimitiveArrayCritical(vertices, data, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixDynamicEntityBridge_nEndDynamicEntity(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEndDynamicEntity");
  RemixRenderer::instance().endDynamicEntity();
}

}  // extern "C"
