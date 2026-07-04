#pragma once

#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/remix_renderer_dynamic.hpp"
#include "mcrtx/remix_renderer_overlay.hpp"
#include "mcrtx/world_origin.hpp"

namespace mcrtx {

struct CameraState {
  double position[3] {0.0, 0.0, 0.0};
  float forward[3] {0.0f, 0.0f, 1.0f};
  float up[3] {0.0f, 1.0f, 0.0f};
  float right[3] {1.0f, 0.0f, 0.0f};
  float fovYDegrees {70.0f};
  float aspect {16.0f / 9.0f};
  float nearPlane {0.05f};
  float farPlane {1024.0f};
};

struct DurationPerfCounter {
  std::uint64_t sampleCount {0};
  std::uint64_t totalNanoseconds {0};
  std::uint64_t maxNanoseconds {0};

  void add(std::uint64_t nanoseconds) noexcept {
    ++sampleCount;
    totalNanoseconds += nanoseconds;
    if (nanoseconds > maxNanoseconds) {
      maxNanoseconds = nanoseconds;
    }
  }

  void reset() noexcept {
    sampleCount = 0;
    totalNanoseconds = 0;
    maxNanoseconds = 0;
  }
};

struct CountPerfCounter {
  std::uint64_t sampleCount {0};
  std::uint64_t totalCount {0};
  std::uint64_t maxCount {0};

  void add(std::uint64_t count) noexcept {
    ++sampleCount;
    totalCount += count;
    if (count > maxCount) {
      maxCount = count;
    }
  }

  void reset() noexcept {
    sampleCount = 0;
    totalCount = 0;
    maxCount = 0;
  }
};

struct NativePerfWindow {
  std::uint64_t frames {0};
  DurationPerfCounter presentLockWait {};
  DurationPerfCounter presentLockHold {};
  DurationPerfCounter outputWindowWork {};
  DurationPerfCounter cameraSubmit {};
  DurationPerfCounter geometrySubmit {};
  DurationPerfCounter remixPresent {};
  DurationPerfCounter uiStateSync {};
  DurationPerfCounter frameChunkBuildWork {};
  DurationPerfCounter frameChunkMeshRebuildWork {};
  DurationPerfCounter frameNeighborRefreshWork {};
  CountPerfCounter frameCaptureBlockCalls {};
  CountPerfCounter frameCachedChunkMeshes {};
  CountPerfCounter frameChunkBuilds {};
  CountPerfCounter frameChunkMeshRebuilds {};
  CountPerfCounter frameSubmittedChunkMeshes {};
  CountPerfCounter frameSubmittedChunkBlocks {};

  void reset() noexcept {
    frames = 0;
    presentLockWait.reset();
    presentLockHold.reset();
    outputWindowWork.reset();
    cameraSubmit.reset();
    geometrySubmit.reset();
    remixPresent.reset();
    uiStateSync.reset();
    frameChunkBuildWork.reset();
    frameChunkMeshRebuildWork.reset();
    frameNeighborRefreshWork.reset();
    frameCaptureBlockCalls.reset();
    frameCachedChunkMeshes.reset();
    frameChunkBuilds.reset();
    frameChunkMeshRebuilds.reset();
    frameSubmittedChunkMeshes.reset();
    frameSubmittedChunkBlocks.reset();
  }
};

struct FrameRenderSnapshot {
  CameraState camera {};
  WorldRenderOrigin renderOrigin {};
  std::vector<ChunkRenderInstance> chunkMeshes {};
  std::vector<DynamicEntityRenderInstance> dynamicEntities {};
  std::vector<remixapi_LightHandle> torchLights {};
  remixapi_MeshHandle cloudMeshHandle {nullptr};
  remixapi_MeshHandle fireMeshHandle {nullptr};
  remixapi_MeshHandle destroyOverlayMeshHandle {nullptr};
  remixapi_MeshHandle blockOutlineMeshHandle {nullptr};
  remixapi_MeshHandle particleMeshHandle {nullptr};
  float cloudTransformX {0.0f};
  float cloudTransformY {0.0f};
  float cloudTransformZ {0.0f};
  std::size_t cachedChunkMeshes {0};
  std::size_t submittedChunkBlocks {0};
  std::size_t submittedCloudQuads {0};
  std::size_t submittedFireQuads {0};
  std::size_t submittedDynamicEntityQuads {0};
  std::size_t submittedDynamicEntityDrawCalls {0};
  std::size_t submittedDynamicEntityFallbackDrawCalls {0};
  std::size_t submittedDynamicEntityInstancedDrawCalls {0};
  std::size_t submittedDynamicEntityInstancedTransforms {0};
  std::size_t submittedDynamicEntityRigidCandidates {0};
  std::size_t submittedDynamicEntitySingletonRigidFallbacks {0};
  std::size_t submittedDynamicEntitySkinnedFallbacks {0};
  std::size_t submittedDestroyOverlays {0};
  std::size_t submittedBlockOutlines {0};
  std::size_t submittedParticleQuads {0};
  std::size_t submittedTorchLights {0};

  bool hasScene() const noexcept {
    return !chunkMeshes.empty()
        || !dynamicEntities.empty()
        || cloudMeshHandle != nullptr
        || fireMeshHandle != nullptr
        || destroyOverlayMeshHandle != nullptr
          || blockOutlineMeshHandle != nullptr
        || particleMeshHandle != nullptr
        || !torchLights.empty();
  }
};

} // namespace mcrtx