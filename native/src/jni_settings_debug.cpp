#include "mcrtx/perf_log.hpp"
#include "mcrtx/remix_renderer.hpp"

#include <jni.h>

using mcrtx::RemixRenderer;

extern "C" {

JNIEXPORT void JNICALL Java_mcrtx_bridge_McrtxDebugSettingsNative_nSetDynamicEntityRenderingEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityRenderingEnabled");
  RemixRenderer::instance().setDynamicEntityRenderingEnabled(enabled == JNI_TRUE);
}

}  // extern "C"
