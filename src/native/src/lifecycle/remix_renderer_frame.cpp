// Per-frame camera state, render-origin selection, and immutable snapshot preparation.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <cstddef>
#include <algorithm>
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

  // The aerial perspective volume sizes its depth axis and its scene-shadow
  // bound from the render distance, which reaches us only as the camera far
  // plane. Minecraft projects to farPlaneDistance * 2, so half of it is the
  // distance actually drawn. Re-push only when the video setting has moved -
  // this runs every frame.
  const float viewDistanceBlocks = camera_.farPlane * 0.5f;
  if (initialized_ && viewDistanceBlocks > 0.0f
      && viewDistanceBlocks != aerialPerspectiveViewDistanceBlocks_) {
    aerialPerspectiveViewDistanceBlocks_ = viewDistanceBlocks;
    applyAerialPerspectiveConfigLocked();
  }
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
  activeUndergroundFrame_ = standaloneOutputWindow_ ? publishedUndergroundFrame_ : undergroundFrame_;
  undergroundHiddenGroups_ = undergroundHiddenTriangles_ = undergroundHiddenEntities_ = 0;
  undergroundHiddenParticles_ = undergroundHiddenLights_ = 0;
  undergroundVisibleGroups_ = undergroundPendingGroups_ = 0;
  snapshot.camera = standaloneOutputWindow_ && publishedCameraValid_
      ? publishedCamera_
      : camera_;
  snapshot.renderOrigin = makeWorldRenderOrigin(
      worldOriginRebaseEnabled_,
      snapshot.camera.position[0],
      snapshot.camera.position[1],
      snapshot.camera.position[2]);
  snapshot.camera.position[0] = rebaseWorldCoordinate(snapshot.camera.position[0], snapshot.renderOrigin.x);
  snapshot.camera.position[1] = rebaseWorldCoordinate(snapshot.camera.position[1], snapshot.renderOrigin.y);
  snapshot.camera.position[2] = rebaseWorldCoordinate(snapshot.camera.position[2], snapshot.renderOrigin.z);

  const std::vector<ParticleQuad>& frameParticleQuads = standaloneOutputWindow_
      ? publishedParticleQuads_
      : particleQuads_;
  const std::vector<WorldRenderPosition>& frameFlameParticleLightPositions = standaloneOutputWindow_
      ? publishedFlameParticleLightPositions_
      : flameParticleLightPositions_;
  const std::unordered_map<int, EntityHeldTorchLightInput>& frameEntityHeldTorchLightInputs = standaloneOutputWindow_
      ? publishedEntityHeldTorchLightInputs_
      : entityHeldTorchLightInputs_;
  const int frameHeldItemId = standaloneOutputWindow_
      ? publishedHeldItemId_
      : heldItemId_;

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.rebuildTransientMeshes");
    // A stable cloud handle can only be replaced after the preceding submission.
    const CloudLayerState& clouds = standaloneOutputWindow_ ? publishedCloudLayer_ : cloudLayer_;
    if (clouds.enabled && !remixAtmosphereCloudsEnabled_) {
      if (!rebuildCloudMesh(clouds.fancy, clouds.cameraX, clouds.cameraY, clouds.cameraZ,
                           clouds.height, clouds.scroll, clouds.colorR, clouds.colorG, clouds.colorB,
                           snapshot.renderOrigin)) {
        return false;
      }
    } else {
      destroyCloudMesh();
    }

    if (!rebuildFireMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildDestroyOverlayMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildBlockOutlineMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildLightLevelOverlayMesh(snapshot.renderOrigin)) {
      return false;
    }

    if (!rebuildParticleMesh(snapshot.renderOrigin, frameParticleQuads)) {
      return false;
    }
  }

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.collectChunkMeshes");
    MCRTX_TRACY_VALUE(chunkMeshes_.size());
    snapshot.chunkMeshes.reserve(chunkMeshes_.size());
    std::size_t groupBuildBudget = 2;
    for (auto& [chunkKey, meshData] : chunkMeshes_) {
      if (meshData.meshHandle == nullptr) {
        continue;
      }

      if (meshData.hidden) {
        continue;
      }

      if (isChunkBuried(chunkKey)) {
        continue;
      }

      if (activeUndergroundFrame_.enabled) {
        const auto topology = activeUndergroundFrame_.sections->find({chunkKey.originX, chunkKey.originY, chunkKey.originZ, 0});
        const bool captured = topology != activeUndergroundFrame_.sections->end()
          && topology->second->revision == meshData.undergroundCaptureRevision;
        if (meshData.visibilityCheckedTopology != activeUndergroundFrame_.topologyRevision) {
          meshData.visibilityCheckedTopology = activeUndergroundFrame_.topologyRevision;
          meshData.visibilityCurrent = meshData.visibilityTopologyFingerprint == activeUndergroundFrame_.topologyFingerprint(chunkKey)
            && std::all_of(meshData.visibilityGroups.begin(), meshData.visibilityGroups.end(),
              [&](const auto& group) { return activeUndergroundFrame_.matches(group.pockets); });
        }
        const bool current = meshData.visibilityCurrent && meshData.visibilityGeometryFingerprint == meshData.meshFingerprint
          && !meshData.visibilityGroups.empty();
        if (!current && captured && groupBuildBudget != 0) {
          --groupBuildBudget;
          rebuildUndergroundMeshes(chunkKey, meshData);
        } else if (!current) {
          ++undergroundPendingGroups_;
        }
        if (meshData.visibilityCheckedFrame != activeUndergroundFrame_.revision) {
          meshData.visibilityCheckedFrame = activeUndergroundFrame_.revision;
          for (auto& group : meshData.visibilityGroups) {
            group.hidden = activeUndergroundFrame_.isHidden(group.pockets);
          }
        }
        const bool anyHidden = std::any_of(meshData.visibilityGroups.begin(), meshData.visibilityGroups.end(),
          [](const auto& group) { return group.hidden; });
        if (captured && meshData.visibilityGeometryFingerprint == meshData.meshFingerprint && anyHidden) {
          bool submitted = false;
          for (const auto& group : meshData.visibilityGroups) {
            if (group.hidden) {
              ++undergroundHiddenGroups_;
              undergroundHiddenTriangles_ += group.triangleCount;
              continue;
            }
            snapshot.chunkMeshes.push_back({chunkKey, group.handle, submitted ? 0 : meshData.blockCount});
            submitted = true;
          }
          if (submitted) {
            ++snapshot.cachedChunkMeshes;
            snapshot.submittedChunkBlocks += meshData.blockCount;
          }
          continue;
        }
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
    const std::vector<DynamicEntityFrameInstance>& frameInstances = standaloneOutputWindow_
        ? publishedDynamicEntityFrameInstances_
        : dynamicEntityFrameInstances_;
    const std::size_t frameInstanceCount = standaloneOutputWindow_
        ? publishedDynamicEntityFrameInstanceCount_
        : dynamicEntityFrameInstanceCount_;
    MCRTX_TRACY_VALUE(frameInstanceCount);
    snapshot.dynamicEntities.reserve(frameInstanceCount);
    for (std::size_t index = 0; index < frameInstanceCount; ++index) {
      const DynamicEntityFrameInstance& frameInstance = frameInstances[index];
      if (frameInstance.meshHandle == nullptr || frameInstance.boneTransforms.empty()) {
        continue;
      }
      if (frameInstance.entityId != kFirstPersonDynamicEntityId && frameInstance.entityId != kFirstPersonPlayerShadowEntityId
          && activeUndergroundFrame_.isHidden(frameInstance.boundsMin, frameInstance.boundsMax)) {
        ++undergroundHiddenEntities_;
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
  undergroundVisibleGroups_ = snapshot.chunkMeshes.size();
  snapshot.fireMeshHandle = fireMeshHandle_;
  snapshot.destroyOverlayMeshHandle = destroyOverlayMeshHandle_;
  snapshot.blockOutlineMeshHandle = blockOutlineMeshHandle_;
  snapshot.lightLevelOverlayMeshHandle = lightLevelOverlayMeshHandle_;
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
    MCRTX_TRACY_VALUE(torchLights_.size() + entityHeldTorchLights_.size() + activeFlameParticleLights_.size());
    if (heldTorchLightsEnabled_) {
      if (!reconcileHeldItemTorchLight(frameHeldItemId, snapshot.camera, snapshot.renderOrigin)) {
        return false;
      }
      if (!updateEntityLightsLocked(frameEntityHeldTorchLightInputs, snapshot.renderOrigin)) {
        return false;
      }
    } else {
      clearHeldTorchLightsLocked();
    }

    reconcileParticleLights(snapshot.renderOrigin, frameFlameParticleLightPositions);

    if (!refreshTorchLightDefinitions(snapshot.renderOrigin)) {
      return false;
    }
    if (!refreshGlowstoneLightDefinitions(snapshot.renderOrigin)) {
      return false;
    }
  }

  {
    MCRTX_TRACY_SCOPE("prepareFrameSnapshot.collectTorchLights");
    snapshot.torchLights.reserve(
        torchLights_.size() + entityHeldTorchLights_.size() + activeFlameParticleLights_.size()
        + (heldItemTorchLightHandle_ != nullptr ? 1 : 0));
    for (const auto& [position, lightState] : torchLights_) {
      if (activeUndergroundFrame_.isHidden(
          {position.x - 0.01, position.y - 0.01, position.z - 0.01},
          {position.x + 1.01, position.y + 1.01, position.z + 1.01})) {
        ++undergroundHiddenLights_;
        continue;
      }
      if (lightState.handle != nullptr) {
        snapshot.torchLights.push_back(lightState.handle);
      }
    }
    for (const auto& [entityId, lightState] : entityHeldTorchLights_) {
      if (activeUndergroundFrame_.isHidden(
          {lightState.worldX - 1, lightState.worldY - 1, lightState.worldZ - 1},
          {lightState.worldX + 1, lightState.worldY + 1, lightState.worldZ + 1})) {
        ++undergroundHiddenLights_;
        continue;
      }
      if (lightState.handle != nullptr) {
        snapshot.torchLights.push_back(lightState.handle);
      }
    }
    for (const auto& lightState : activeFlameParticleLights_) {
      const double x = lightState.submittedPosition.x + lightState.renderOrigin.x;
      const double y = lightState.submittedPosition.y + lightState.renderOrigin.y;
      const double z = lightState.submittedPosition.z + lightState.renderOrigin.z;
      if (activeUndergroundFrame_.isHidden({x - 1, y - 1, z - 1}, {x + 1, y + 1, z + 1})) {
        ++undergroundHiddenLights_;
        continue;
      }
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
