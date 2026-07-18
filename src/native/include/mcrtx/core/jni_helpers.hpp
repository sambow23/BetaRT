#pragma once

#include <jni.h>

namespace mcrtx::jni {

inline jboolean toJniBoolean(bool value) {
  return value ? JNI_TRUE : JNI_FALSE;
}

}  // namespace mcrtx::jni
