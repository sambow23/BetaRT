#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <jni.h>

#include <cstdint>

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxPerfNative_nRecordJavaSample(
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxPerfNative_nFlushJavaFrame(JNIEnv*, jclass) {
  // Java samples are currently recorded synchronously. Keep this frame-boundary
  // entrypoint so batching can change later without another ABI update.
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_McrtxPerfNative_nRegisterPerfSite(
    JNIEnv* env, jclass, jint side, jstring site) {
  if (site == nullptr) {
    return -1;
  }
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) {
    return -1;
  }
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxPerfNative_nRecordJavaSampleBatch(
    JNIEnv* env, jclass, jintArray ids, jlongArray nanos, jint count) {
  if (ids == nullptr || nanos == nullptr || count <= 0) {
    return;
  }
  void* idsPtr = env->GetPrimitiveArrayCritical(ids, nullptr);
  if (idsPtr == nullptr) {
    return;
  }
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

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxPerfNative_nRecordJavaCount(
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
