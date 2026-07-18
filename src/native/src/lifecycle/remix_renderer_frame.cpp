// Per-frame camera state, render-origin selection, and immutable snapshot preparation.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <cstddef>
#include <cstdint>
#include <mutex>
#include <utility>

namespace mcrtx {

using namespace mcrtx::detail;

void RemixRenderer::resetPerFramePerfCounters() noexcept {
  perfCaptureBlockCallsThisFrame_ = 0;
  perfChunkBuildsThisFrame_ = 0;
  perfChunkMeshRebuildsThisFrame_ = 0;
  perfChunkBuildWorkNanosThisFrame_ = 0;
  perfChunkMeshRebuildNanosThisFrame_ = 0;
  perfNeighborRefreshNanosThisFrame_ = 0;
  perfCachedChunkMeshesThisFrame_ = 0;
  perfSubmittedChunkMeshesThisFrame_ = 0;
  perfSubmittedChunkBlocksThisFrame_ = 0;
}

void RemixRenderer::resize(std::uint32_t width, std::uint32_t height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::resize");
  std::scoped_lock lock(mutex_);
  width_ = width == 0 ? 1 : width;
  height_ = height == 0 ? 1 : height;
  camera_.aspect = static_cast<float>(width_) / static_cast<float>(height_);
  updateOutputWindowSize();
}

void RemixRenderer::updateCamera(const CameraState& camera) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateCamera");
  std::scoped_lock lock(mutex_);
  camera_ = camera;
}

WorldRenderOrigin RemixRenderer::currentRenderOriginLocked() const noexcept {
  return makeWorldRenderOrigin(
      worldOriginRebaseEnabled_,
      camera_.position[0],
      camera_.position[1],
      camera_.position[2]);
}

bool RemixRenderer::prepareFrameSnapshotLocked(FrameRenderSnapshot& snapshot, bool& logNoCapturedScene) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::prepareFrameSnapshotLocked");
  MCRTX_TRACY_SCOPE("RemixRenderer::prepareFrameSnapshotLocked");
  snapshot = {};
  snapshot.camera = camera_;
  snapshot.renderOrigin = currentRenderOriginLocked();
  snapshot.camera.position[0] = rebaseWorldCoordinate(snapshot.camera.position[0], snapshot.renderOrigin.x);
  snapshot.camera.position[1] = rebaseWorldCoordinate(snapshot.camera.position[1], snapshot.renderOrigin.y);
  snapshot.camera.position[2] = rebaseWorldCoordinate(snapshot.camera.position[2], snapshot.renderOrigin.z);

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.rebuildTransientMeshes");
    if (!rebuildFireMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildDestroyOverlayMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildBlockOutlineMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildParticleMesh(snapshot.renderOrigin)) {
      return false;
    }
  }

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.collectChunkMeshes");
    MCRTX_TRACY_VALUE(chunkMeshes_.size());
    snapshot.chunkMeshes.reserve(chunkMeshes_.size());
    for (const auto& [chunkKey, meshData] : chunkMeshes_) {
      if (meshData.meshHandle == nullptr) {
        continue;
      }

      if (meshData.hidden) {
        continue;
      }

      if (isChunkBuried(chunkKey)) {
        continue;
      }

      ChunkRenderInstance renderInstance;
      renderInstance.chunkKey = chunkKey;
      renderInstance.meshHandle = meshData.meshHandle;
      renderInstance.blockCount = meshData.blockCount;
      snapshot.chunkMeshes.push_back(renderInstance);
      snapshot.cachedChunkMeshes += 1;
      snapshot.submittedChunkBlocks += meshData.blockCount;
    }
  }

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.collectDynamicEntities");
    MCRTX_TRACY_VALUE(dynamicEntityFrameInstanceCount_);
    snapshot.dynamicEntities.reserve(dynamicEntityFrameInstanceCount_);
    for (std::size_t index = 0; index < dynamicEntityFrameInstanceCount_; ++index) {
      const DynamicEntityFrameInstance& frameInstance = dynamicEntityFrameInstances_[index];
      if (frameInstance.meshHandle == nullptr || frameInstance.boneTransforms.empty()) {
        continue;
      }

      snapshot.submittedDynamicEntityQuads += frameInstance.quadCount;
      DynamicEntityRenderInstance snapshotFrameInstance;
      snapshotFrameInstance.entityId = frameInstance.entityId;
      snapshotFrameInstance.meshHandle = frameInstance.meshHandle;
      snapshotFrameInstance.quadCount = frameInstance.quadCount;
      snapshotFrameInstance.boneTransforms.reserve(frameInstance.boneTransforms.size());
      for (const DynamicEntityBoneTransform& sourceTransform : frameInstance.boneTransforms) {
        remixapi_Transform transform = sourceTransform.transform;
        const WorldRenderPosition rebasedPosition = rebaseWorldPosition(
            sourceTransform.worldX,
            sourceTransform.worldY,
            sourceTransform.worldZ,
            snapshot.renderOrigin);
        transform.matrix[0][3] = rebasedPosition.x;
        transform.matrix[1][3] = rebasedPosition.y;
        transform.matrix[2][3] = rebasedPosition.z;
        snapshotFrameInstance.boneTransforms.push_back(transform);
      }
      snapshot.dynamicEntities.push_back(std::move(snapshotFrameInstance));
    }
  }

  snapshot.cloudMeshHandle = cloudMeshHandle_;
  snapshot.fireMeshHandle = fireMeshHandle_;
  snapshot.destroyOverlayMeshHandle = destroyOverlayMeshHandle_;
  snapshot.blockOutlineMeshHandle = blockOutlineMeshHandle_;
  snapshot.particleMeshHandle = particleMeshHandle_;
  snapshot.cloudTransformX = cloudTransformX_;
  snapshot.cloudTransformY = cloudTransformY_;
  snapshot.cloudTransformZ = cloudTransformZ_;
  snapshot.submittedCloudQuads = cloudMeshHandle_ != nullptr ? cloudQuadCount_ : 0;
  snapshot.submittedFireQuads = fireMeshHandle_ != nullptr ? fireQuadCount_ : 0;
  snapshot.submittedDestroyOverlays = destroyOverlayMeshHandle_ != nullptr ? destroyOverlayCount_ : 0;
  snapshot.submittedBlockOutlines = blockOutlineMeshHandle_ != nullptr ? blockOutlineCount_ : 0;
  snapshot.submittedParticleQuads = particleMeshHandle_ != nullptr ? particleQuadCount_ : 0;

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.reconcileTorchLights");
    MCRTX_TRACY_VALUE(torchLights_.size() + entityHeldTorchLights_.size());
    if (heldTorchLightsEnabled_) {
      if (!reconcileHeldItemTorchLight(snapshot.renderOrigin)) {
        return false;
      }

      for (auto lightIt = entityHeldTorchLights_.begin(); lightIt != entityHeldTorchLights_.end(); ) {
        if (entityHeldTorchLightsSeenThisFrame_.find(lightIt->first) != entityHeldTorchLightsSeenThisFrame_.end()) {
          ++lightIt;
          continue;
        }

        if (lightIt->second.handle != nullptr) {
          destroyLightHandle(lightIt->second.handle);
        }
        lightIt = entityHeldTorchLights_.erase(lightIt);
      }

    } else {
      clearHeldTorchLightsLocked();
    }

    if (!refreshTorchLightDefinitions(snapshot.renderOrigin)) {
      return false;
    }
  }

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.collectTorchLights");
    snapshot.torchLights.reserve(
        torchLights_.size() + entityHeldTorchLights_.size() + (heldItemTorchLightHandle_ != nullptr ? 1 : 0));
    for (const auto& [position, lightState] : torchLights_) {
      (void)position;
      if (lightState.handle != nullptr) {
        snapshot.torchLights.push_back(lightState.handle);
      }
    }
    for (const auto& [entityId, lightState] : entityHeldTorchLights_) {
      (void)entityId;
      if (lightState.handle != nullptr) {
        snapshot.torchLights.push_back(lightState.handle);
      }
    }
    if (heldItemTorchLightHandle_ != nullptr) {
      snapshot.torchLights.push_back(heldItemTorchLightHandle_);
    }
    snapshot.submittedTorchLights = snapshot.torchLights.size();
    MCRTX_TRACY_VALUE(snapshot.submittedTorchLights);
  }

  if (!snapshot.hasScene()) {
    logNoCapturedScene = presentedFrames_ < 4;
    if (primingMeshHandle_ != nullptr) {
      renderSubmissionInFlight_ = true;
    }
    return true;
  }

  renderSubmissionInFlight_ = true;
  return true;
}

}  // namespace mcrtx
