#include "mcrtx/direct/runtime.hpp"
#include <jni.h>
#include <limits>
#include "mcrtx/lifecycle/perf_log.hpp"

namespace {
using mcrtx::direct::Runtime;
using mcrtx::direct::Bytes;
Bytes buffer(JNIEnv *env, jobject value, jint size) {
  if (size < 0 || size > 256 * 1024 * 1024) throw std::invalid_argument("Invalid submission length");
  if (!size) return {};
  if (!value || env->GetDirectBufferCapacity(value) < size) throw std::invalid_argument("Invalid direct buffer");
  auto *data = static_cast<const std::byte *>(env->GetDirectBufferAddress(value));
  if (!data) throw std::invalid_argument("Missing direct buffer data");
  return Bytes(data, data + size);
}
std::string text(JNIEnv *env, jbyteArray value) {
  if (!value) return {};
  const auto size = env->GetArrayLength(value);
  if (size > 1024 * 1024) throw std::invalid_argument("Path too long");
  std::string result(size, '\0');
  env->GetByteArrayRegion(value, 0, size, reinterpret_cast<jbyte *>(result.data()));
  if (env->ExceptionCheck() || result.find('\0') != std::string::npos)
    throw std::invalid_argument("Invalid UTF-8 path");
  return result;
}
}

extern "C" {
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *, void *) { mcrtx::perf::shutdown(); }
JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_DirectBridge_nStart(
    JNIEnv *env, jclass, jbyteArray library, jint width, jint height) {
  try {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    const auto path = std::filesystem::u8path(text(env, library));
#pragma clang diagnostic pop
    return Runtime::instance().start(path, width, height);
  } catch (...) { return JNI_FALSE; }
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_DirectBridge_nStop(JNIEnv *, jclass) {
  Runtime::instance().stop();
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_DirectBridge_nSubmit(
    JNIEnv *env, jclass, jobject resources, jint resourceBytes, jobject frame, jint frameBytes) {
  MCRTX_PERF_CPU_SCOPE(mcrtx::perf::Side::Jni, "direct.submit");
  try {
    auto resourceData = buffer(env, resources, resourceBytes);
    auto frameData = buffer(env, frame, frameBytes);
    return Runtime::instance().submit(std::move(resourceData), std::move(frameData));
  } catch (...) { return JNI_FALSE; }
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_DirectBridge_nControl(
    JNIEnv *env, jclass, jint operation, jint value, jfloat x, jfloat y, jbyteArray path) {
  try { return Runtime::instance().control(operation, value, x, y, text(env, path)); }
  catch (...) { return JNI_FALSE; }
}

JNIEXPORT jintArray JNICALL Java_mcrtx_bridge_DirectBridge_nPoll(JNIEnv *env, jclass) {
  try {
    const auto values = Runtime::instance().poll(256);
    auto result = env->NewIntArray(static_cast<jsize>(values.size()));
    if (result) env->SetIntArrayRegion(result, 0, static_cast<jsize>(values.size()),
      reinterpret_cast<const jint *>(values.data()));
    return result;
  } catch (...) { return nullptr; }
}

JNIEXPORT jlongArray JNICALL Java_mcrtx_bridge_DirectBridge_nCounts(JNIEnv *env, jclass) {
  const auto values = Runtime::instance().counts();
  jlongArray result = env->NewLongArray(values.size());
  if (result) env->SetLongArrayRegion(result, 0, values.size(), reinterpret_cast<const jlong *>(values.data()));
  return result;
}

JNIEXPORT jstring JNICALL Java_mcrtx_bridge_DirectBridge_nError(JNIEnv *env, jclass) {
  try { return env->NewStringUTF(Runtime::instance().error().c_str()); }
  catch (...) { return nullptr; }
}
}
