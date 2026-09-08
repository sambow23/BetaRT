#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/core/remix_renderer.hpp"

#include <jni.h>

using mcrtx::RemixRenderer;

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxLodSettingsNative_nSetLodDebugViewEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetLodDebugViewEnabled");
  RemixRenderer::instance().setLodDebugViewEnabled(enabled == JNI_TRUE);
}

}  // extern "C"
