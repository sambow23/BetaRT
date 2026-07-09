#include "mcrtx/perf_log.hpp"
#include "mcrtx/remix_renderer.hpp"

namespace mcrtx {

void RemixRenderer::setDynamicEntityRenderingEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityRenderingEnabled");
  std::scoped_lock lock(mutex_);

  dynamicEntityRenderingEnabled_ = enabled;
  if (enabled) {
    return;
  }

  activeDynamicEntity_.entityId = -1;
  activeDynamicEntity_.hurtStage = 0;
  activeDynamicEntity_.creeperFuseStage = 0;
  activeDynamicEntity_.maxBoneCount = 0;
  activeDynamicEntity_.quadFingerprint = 0;
  activeDynamicEntity_.currentTextureIndex = 0xFFFFFFFFu;
  activeDynamicEntity_.currentTextureFingerprint = 0;
  activeDynamicEntity_.texturePaths.clear();
  activeDynamicEntity_.quadCount = 0;
  activeDynamicEntity_.boneTransforms.clear();
  activeDynamicEntity_.active = false;
  clearDynamicEntityFrameInstances();
}

}  // namespace mcrtx
