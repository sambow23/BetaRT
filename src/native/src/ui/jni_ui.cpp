#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

#include <cstdint>

namespace {

using mcrtx::RemixRenderer;

}  // namespace

extern "C" {

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixUiBridge_nDrawScreenOverlay(
    JNIEnv* env,
    jclass,
    jobject pixelBuffer,
    jint width,
    jint height,
    jint format,
    jfloat opacity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nDrawScreenOverlay");
  if (pixelBuffer == nullptr) {
    return JNI_FALSE;
  }

  void* pixelData = env->GetDirectBufferAddress(pixelBuffer);
  if (pixelData == nullptr) {
    return JNI_FALSE;
  }

  const bool ok = RemixRenderer::instance().drawScreenOverlay(
      pixelData,
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height),
      static_cast<remixapi_Format>(format),
      opacity);
  return mcrtx::jni::toJniBoolean(ok);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixUiBridge_nClearScreenOverlay(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearScreenOverlay");
  return mcrtx::jni::toJniBoolean(RemixRenderer::instance().clearScreenOverlay());
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixUiBridge_nRegisterUiTexture(
    JNIEnv* env,
    jclass,
    jobject pixelBuffer,
    jlong id,
    jint width,
    jint height,
    jint format) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nRegisterUiTexture");
  if (pixelBuffer == nullptr || width <= 0 || height <= 0) {
    return JNI_FALSE;
  }

  void* pixelData = env->GetDirectBufferAddress(pixelBuffer);
  if (pixelData == nullptr) {
    return JNI_FALSE;
  }

  const std::uint64_t dataSize =
      static_cast<std::uint64_t>(width) * static_cast<std::uint64_t>(height) * 4ull;
  const bool ok = RemixRenderer::instance().registerUiTexture(
      static_cast<std::uint64_t>(id),
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height),
      static_cast<remixapi_Format>(format),
      pixelData,
      dataSize);
  return mcrtx::jni::toJniBoolean(ok);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixUiBridge_nFreeUiTexture(
    JNIEnv*, jclass, jlong id) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFreeUiTexture");
  return mcrtx::jni::toJniBoolean(
      RemixRenderer::instance().freeUiTexture(static_cast<std::uint64_t>(id)));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixUiBridge_nSubmitUiDrawList(
    JNIEnv* env,
    jclass,
    jfloatArray vertexXYZUV,
    jintArray vertexColor,
    jint vertexCount,
    jlongArray cmdTextureIds,
    jintArray cmdQuadCounts,
    jintArray cmdFlags,
    jint cmdCount,
    jint displayWidth,
    jint displayHeight) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSubmitUiDrawList");

  // Empty lists clear the submitted UI and do not need backing arrays.
  if (vertexCount <= 0 || cmdCount <= 0) {
    return mcrtx::jni::toJniBoolean(
        RemixRenderer::instance().submitUiDrawListFromArrays(
            nullptr, nullptr, 0, nullptr, nullptr, nullptr, 0,
            static_cast<std::uint32_t>(displayWidth),
            static_cast<std::uint32_t>(displayHeight)));
  }

  if (vertexXYZUV == nullptr || vertexColor == nullptr
      || cmdTextureIds == nullptr || cmdQuadCounts == nullptr || cmdFlags == nullptr) {
    return JNI_FALSE;
  }
  if (env->GetArrayLength(vertexXYZUV) < static_cast<jsize>(vertexCount) * 5
      || env->GetArrayLength(vertexColor) < static_cast<jsize>(vertexCount)
      || env->GetArrayLength(cmdTextureIds) < static_cast<jsize>(cmdCount)
      || env->GetArrayLength(cmdQuadCounts) < static_cast<jsize>(cmdCount)
      || env->GetArrayLength(cmdFlags) < static_cast<jsize>(cmdCount)) {
    return JNI_FALSE;
  }

  auto* xyzuv = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(vertexXYZUV, nullptr));
  auto* colors = static_cast<jint*>(env->GetPrimitiveArrayCritical(vertexColor, nullptr));
  auto* texIds = static_cast<jlong*>(env->GetPrimitiveArrayCritical(cmdTextureIds, nullptr));
  auto* quadCounts = static_cast<jint*>(env->GetPrimitiveArrayCritical(cmdQuadCounts, nullptr));
  auto* flags = static_cast<jint*>(env->GetPrimitiveArrayCritical(cmdFlags, nullptr));

  bool ok = false;
  if (xyzuv != nullptr && colors != nullptr && texIds != nullptr
      && quadCounts != nullptr && flags != nullptr) {
    ok = RemixRenderer::instance().submitUiDrawListFromArrays(
        reinterpret_cast<const float*>(xyzuv),
        reinterpret_cast<const std::uint32_t*>(colors),
        static_cast<std::uint32_t>(vertexCount),
        reinterpret_cast<const std::uint64_t*>(texIds),
        reinterpret_cast<const std::int32_t*>(quadCounts),
        reinterpret_cast<const std::uint32_t*>(flags),
        static_cast<std::uint32_t>(cmdCount),
        static_cast<std::uint32_t>(displayWidth),
        static_cast<std::uint32_t>(displayHeight));
  }

  if (flags != nullptr) {
    env->ReleasePrimitiveArrayCritical(cmdFlags, flags, JNI_ABORT);
  }
  if (quadCounts != nullptr) {
    env->ReleasePrimitiveArrayCritical(cmdQuadCounts, quadCounts, JNI_ABORT);
  }
  if (texIds != nullptr) {
    env->ReleasePrimitiveArrayCritical(cmdTextureIds, texIds, JNI_ABORT);
  }
  if (colors != nullptr) {
    env->ReleasePrimitiveArrayCritical(vertexColor, colors, JNI_ABORT);
  }
  if (xyzuv != nullptr) {
    env->ReleasePrimitiveArrayCritical(vertexXYZUV, xyzuv, JNI_ABORT);
  }

  return mcrtx::jni::toJniBoolean(ok);
}

}  // extern "C"
