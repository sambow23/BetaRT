// Fog, camera, geometry, and light submission to Remix.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <functional>
#include <mutex>
#include <sstream>
#include <unordered_map>
#include <utility>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

namespace {

constexpr std::uint32_t kRtTextureArgNone = 0;
constexpr std::uint32_t kRtTextureArgTexture = 1;
constexpr std::uint32_t kRtTextureArgVertexColor0 = 2;
constexpr std::uint32_t kRtTextureOpSelectArg1 = 1;
constexpr std::uint32_t kRtTextureOpModulate = 3;

}  // namespace

void RemixRenderer::updateFogState(
    std::uint32_t fogMode,
    float colorR,
    float colorG,
    float colorB,
    float fogScale,
    float fogEnd,
    float fogDensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateFogState");
  std::scoped_lock lock(mutex_);
  if (!initialized_) {
    return;
  }

  if (remix_.SetFogState == nullptr) {
    if (!warnedMissingSetFogState_) {
      warnedMissingSetFogState_ = true;
      log("SetFogState not available; runtime depth-fog updates are disabled");
    }
    return;
  }

  remixapi_FogInfo fogInfo {};
  fogInfo.sType = REMIXAPI_STRUCT_TYPE_FOG_INFO;
  fogInfo.mode = fogMode;
  fogInfo.color = {colorR, colorG, colorB};
  fogInfo.scale = fogScale;
  fogInfo.end = fogEnd;
  fogInfo.density = fogDensity;

  remixapi_ErrorCode result;
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetFogState");
    result = remix_.SetFogState(&fogInfo);
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetFogState failed: " + errorCodeToString(result));
  }
}

bool RemixRenderer::drawCapturedGeometry(FrameRenderSnapshot& snapshot) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::drawCapturedGeometry");
  MCRTX_TRACY_SCOPE("RemixRenderer::drawCapturedGeometry");
  MCRTX_TRACY_VALUE(snapshot.chunkMeshes.size() + snapshot.dynamicEntities.size() + snapshot.torchLights.size());
  if (!snapshot.hasScene() && primingMeshHandle_ != nullptr) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.priming");
    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgNone;
    blendInfo.textureAlphaOperation = kRtTextureOpSelectArg1;
    blendInfo.isVertexColorBakedLighting = FALSE;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &blendInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = primingMeshHandle_;
    instanceInfo.transform = makeTranslationTransform(0.0f, -10000.0f, 0.0f);
    instanceInfo.doubleSided = FALSE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.priming");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance(priming) failed: " + errorCodeToString(result));
      return false;
    }
  }

  {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.chunkInstances");
    MCRTX_TRACY_VALUE(snapshot.chunkMeshes.size());
    for (const ChunkRenderInstance& chunkInstance : snapshot.chunkMeshes) {
      if (chunkInstance.meshHandle == nullptr) {
        continue;
      }

      remixapi_InstanceInfoBlendEXT blendInfo {};
      blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
      blendInfo.textureColorArg1Source = kRtTextureArgTexture;
      blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
      blendInfo.textureColorOperation = kRtTextureOpModulate;
      blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
      blendInfo.textureAlphaArg2Source = kRtTextureArgNone;
      blendInfo.textureAlphaOperation = kRtTextureOpSelectArg1;
      blendInfo.isVertexColorBakedLighting = FALSE;
      if (chunkInstance.chunkKey.renderPass == 1) {
        blendInfo.alphaBlendEnabled = TRUE;
        blendInfo.srcColorBlendFactor = 6;
        blendInfo.dstColorBlendFactor = 7;
        blendInfo.colorBlendOp = 0;
        blendInfo.srcAlphaBlendFactor = 1;
        blendInfo.dstAlphaBlendFactor = 0;
        blendInfo.alphaBlendOp = 0;
      }

      remixapi_InstanceInfo instanceInfo {};
      instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
      instanceInfo.pNext = &blendInfo;
      instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
      instanceInfo.mesh = chunkInstance.meshHandle;
      instanceInfo.transform = makeTranslationTransform(
          rebaseWorldCoordinate(chunkInstance.chunkKey.originX, snapshot.renderOrigin.x),
          rebaseWorldCoordinate(chunkInstance.chunkKey.originY, snapshot.renderOrigin.y),
          rebaseWorldCoordinate(chunkInstance.chunkKey.originZ, snapshot.renderOrigin.z));
      instanceInfo.doubleSided = chunkInstance.chunkKey.renderPass == 1 ? TRUE : FALSE;

      const remixapi_ErrorCode result = [&]() {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.chunk");
        return remix_.DrawInstance(&instanceInfo);
      }();
      if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
        setError("DrawInstance failed: " + errorCodeToString(result));
        return false;
      }
    }
  }

  {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.dynamicEntities");
    MCRTX_TRACY_VALUE(snapshot.dynamicEntities.size());
    std::size_t dynamicEntityBoneCount = 0;
    for (const DynamicEntityRenderInstance& frameInstance : snapshot.dynamicEntities) {
      dynamicEntityBoneCount += frameInstance.boneTransforms.size();
    }
    MCRTX_TRACY_VALUE(dynamicEntityBoneCount);

    const auto dynamicEntityCategoryFlags = [this](int entityId) {
      return entityId == kFirstPersonPlayerShadowEntityId
        ? (playerShadowsEnabled_
          ? (REMIXAPI_INSTANCE_CATEGORY_BIT_THIRD_PERSON_PLAYER_MODEL
            | REMIXAPI_INSTANCE_CATEGORY_BIT_FIRST_PERSON_PLAYER_SHADOW)
          : REMIXAPI_INSTANCE_CATEGORY_BIT_THIRD_PERSON_PLAYER_MODEL)
        : (entityId == kFirstPersonDynamicEntityId
            ? REMIXAPI_INSTANCE_CATEGORY_BIT_VIEW_MODEL
            : REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN);
    };

    struct RigidDynamicBatchKey {
      remixapi_MeshHandle meshHandle {nullptr};
      remixapi_InstanceCategoryFlags categoryFlags {0};
      remixapi_Bool doubleSided {TRUE};

      bool operator==(const RigidDynamicBatchKey& other) const noexcept {
        return meshHandle == other.meshHandle
            && categoryFlags == other.categoryFlags
            && doubleSided == other.doubleSided;
      }
    };

    struct RigidDynamicBatchKeyHash {
      std::size_t operator()(const RigidDynamicBatchKey& key) const noexcept {
        std::size_t hash = std::hash<std::uintptr_t> {}(reinterpret_cast<std::uintptr_t>(key.meshHandle));
        hash ^= std::hash<std::uint32_t> {}(key.categoryFlags) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
        hash ^= std::hash<int> {}(static_cast<int>(key.doubleSided)) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
        return hash;
      }
    };

    struct RigidDynamicBatch {
      RigidDynamicBatchKey key {};
      const DynamicEntityRenderInstance* representative {nullptr};
      std::vector<remixapi_Transform> instanceTransforms {};
    };

    std::vector<const DynamicEntityRenderInstance*> fallbackDynamicEntities;
    fallbackDynamicEntities.reserve(snapshot.dynamicEntities.size());
    std::vector<RigidDynamicBatch> rigidDynamicBatches;
    rigidDynamicBatches.reserve(snapshot.dynamicEntities.size());
    std::unordered_map<RigidDynamicBatchKey, std::size_t, RigidDynamicBatchKeyHash> rigidDynamicBatchIndex;
    rigidDynamicBatchIndex.reserve(snapshot.dynamicEntities.size());
    std::size_t rigidDynamicCandidates = 0;
    std::size_t singletonRigidFallbacks = 0;
    std::size_t skinnedDynamicFallbacks = 0;

    {
      MCRTX_TRACY_SCOPE("drawCapturedGeometry.dynamicEntities.groupCandidates");
      for (const DynamicEntityRenderInstance& frameInstance : snapshot.dynamicEntities) {
        if (frameInstance.meshHandle == nullptr || frameInstance.boneTransforms.empty()) {
          continue;
        }

        if (frameInstance.boneTransforms.size() != 1) {
          ++skinnedDynamicFallbacks;
          fallbackDynamicEntities.push_back(&frameInstance);
          continue;
        }

        ++rigidDynamicCandidates;

        const RigidDynamicBatchKey key {
          .meshHandle = frameInstance.meshHandle,
          .categoryFlags = static_cast<remixapi_InstanceCategoryFlags>(dynamicEntityCategoryFlags(frameInstance.entityId)),
          .doubleSided = TRUE,
        };
        const auto [it, inserted] = rigidDynamicBatchIndex.try_emplace(key, rigidDynamicBatches.size());
        if (inserted) {
          RigidDynamicBatch batch;
          batch.key = key;
          batch.representative = &frameInstance;
          batch.instanceTransforms.reserve(8);
          rigidDynamicBatches.push_back(std::move(batch));
        }

        rigidDynamicBatches[it->second].instanceTransforms.push_back(frameInstance.boneTransforms[0]);
      }
    }

    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgNone;
    blendInfo.textureAlphaOperation = kRtTextureOpSelectArg1;
    blendInfo.isVertexColorBakedLighting = FALSE;

    remixapi_InstanceInfoBoneTransformsEXT boneTransformsInfo {};
    boneTransformsInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BONE_TRANSFORMS_EXT;
    boneTransformsInfo.pNext = &blendInfo;

    remixapi_InstanceInfoGpuInstancingEXT gpuInstancingInfo {};
    gpuInstancingInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_GPU_INSTANCING_EXT;
    gpuInstancingInfo.pNext = &boneTransformsInfo;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &boneTransformsInfo;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_Transform rigidBoneTransform = makeTranslationTransform(0.0f, 0.0f, 0.0f);

    const auto submitDynamicEntity = [&](const DynamicEntityRenderInstance& frameInstance) {
      boneTransformsInfo.boneTransforms_values = frameInstance.boneTransforms.data();
      boneTransformsInfo.boneTransforms_count = static_cast<std::uint32_t>(frameInstance.boneTransforms.size());
      instanceInfo.pNext = &boneTransformsInfo;
      instanceInfo.categoryFlags = dynamicEntityCategoryFlags(frameInstance.entityId);
      instanceInfo.mesh = frameInstance.meshHandle;

      return [&]() {
        MCRTX_TRACY_SCOPE("drawCapturedGeometry.dynamicEntities.drawInstance");
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.entity");
        return remix_.DrawInstance(&instanceInfo);
      }();
    };

    std::size_t instancedDynamicDrawCalls = 0;
    std::size_t instancedDynamicTransforms = 0;
    std::size_t fallbackDynamicDrawCalls = 0;

    {
      MCRTX_TRACY_SCOPE("drawCapturedGeometry.dynamicEntities.instancedRigid");
      for (const RigidDynamicBatch& batch : rigidDynamicBatches) {
        if (batch.instanceTransforms.size() < 2 || batch.representative == nullptr) {
          if (batch.representative != nullptr) {
            ++singletonRigidFallbacks;
            fallbackDynamicEntities.push_back(batch.representative);
          }
          continue;
        }

        boneTransformsInfo.boneTransforms_values = &rigidBoneTransform;
        boneTransformsInfo.boneTransforms_count = 1;
        gpuInstancingInfo.instanceTransforms_values = batch.instanceTransforms.data();
        gpuInstancingInfo.instanceTransforms_count = static_cast<std::uint32_t>(batch.instanceTransforms.size());
        instanceInfo.pNext = &gpuInstancingInfo;
        instanceInfo.categoryFlags = batch.key.categoryFlags;
        instanceInfo.mesh = batch.key.meshHandle;
        instanceInfo.doubleSided = batch.key.doubleSided;

        const remixapi_ErrorCode result = [&]() {
          MCRTX_TRACY_SCOPE("drawCapturedGeometry.dynamicEntities.instancedDraw");
          MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.entityInstanced");
          return remix_.DrawInstance(&instanceInfo);
        }();
        if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
          setError("DrawInstance failed: " + errorCodeToString(result));
          return false;
        }

        ++instancedDynamicDrawCalls;
        instancedDynamicTransforms += batch.instanceTransforms.size();
      }
    }

    {
      MCRTX_TRACY_SCOPE("drawCapturedGeometry.dynamicEntities.fallback");
      for (const DynamicEntityRenderInstance* frameInstance : fallbackDynamicEntities) {
        if (frameInstance == nullptr || frameInstance->meshHandle == nullptr || frameInstance->boneTransforms.empty()) {
          continue;
        }

        const remixapi_ErrorCode result = submitDynamicEntity(*frameInstance);
        if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
          setError("DrawInstance failed: " + errorCodeToString(result));
          return false;
        }
        ++fallbackDynamicDrawCalls;
      }
    }

    snapshot.submittedDynamicEntityDrawCalls = instancedDynamicDrawCalls + fallbackDynamicDrawCalls;
    snapshot.submittedDynamicEntityFallbackDrawCalls = fallbackDynamicDrawCalls;
    snapshot.submittedDynamicEntityInstancedDrawCalls = instancedDynamicDrawCalls;
    snapshot.submittedDynamicEntityInstancedTransforms = instancedDynamicTransforms;
    snapshot.submittedDynamicEntityRigidCandidates = rigidDynamicCandidates;
    snapshot.submittedDynamicEntitySingletonRigidFallbacks = singletonRigidFallbacks;
    snapshot.submittedDynamicEntitySkinnedFallbacks = skinnedDynamicFallbacks;

    if (instancedDynamicTransforms != 0) {
      MCRTX_TRACY_VALUE(instancedDynamicTransforms);
    } else if (instancedDynamicDrawCalls != 0 || fallbackDynamicDrawCalls != 0) {
      MCRTX_TRACY_VALUE(instancedDynamicDrawCalls + fallbackDynamicDrawCalls);
    }
  }

  if (snapshot.cloudMeshHandle != nullptr) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.cloud");
    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureAlphaOperation = kRtTextureOpModulate;
    blendInfo.isVertexColorBakedLighting = FALSE;
    blendInfo.alphaBlendEnabled = TRUE;
    blendInfo.srcColorBlendFactor = 6;
    blendInfo.dstColorBlendFactor = 7;
    blendInfo.colorBlendOp = 0;
    blendInfo.srcAlphaBlendFactor = 1;
    blendInfo.dstAlphaBlendFactor = 0;
    blendInfo.alphaBlendOp = 0;

    remixapi_InstanceInfoObjectPickingEXT objectPickingInfo {};
    objectPickingInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_OBJECT_PICKING_EXT;
    objectPickingInfo.pNext = &blendInfo;
    objectPickingInfo.objectPickingValue = 0x434C4F55u;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &objectPickingInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = snapshot.cloudMeshHandle;
    instanceInfo.transform = makeTranslationTransform(
        snapshot.cloudTransformX,
        snapshot.cloudTransformY,
        snapshot.cloudTransformZ);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.cloud");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }
  }

  if (snapshot.fireMeshHandle != nullptr) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.fire");
    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureAlphaOperation = kRtTextureOpModulate;
    blendInfo.isVertexColorBakedLighting = FALSE;
    blendInfo.alphaBlendEnabled = TRUE;
    blendInfo.srcColorBlendFactor = 6;
    blendInfo.dstColorBlendFactor = 7;
    blendInfo.colorBlendOp = 0;
    blendInfo.srcAlphaBlendFactor = 1;
    blendInfo.dstAlphaBlendFactor = 0;
    blendInfo.alphaBlendOp = 0;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &blendInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = snapshot.fireMeshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.fire");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }
  }

  if (snapshot.destroyOverlayMeshHandle != nullptr) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.destroyOverlay");
    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.alphaTestEnabled = TRUE;
    blendInfo.alphaTestReferenceValue = 1;
    blendInfo.alphaTestCompareOp = 4;
    blendInfo.alphaBlendEnabled = TRUE;
    blendInfo.srcColorBlendFactor = 6;
    blendInfo.dstColorBlendFactor = 7;
    blendInfo.colorBlendOp = 0;
    blendInfo.srcAlphaBlendFactor = 1;
    blendInfo.dstAlphaBlendFactor = 0;
    blendInfo.alphaBlendOp = 0;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgNone;
    blendInfo.textureAlphaOperation = kRtTextureOpSelectArg1;
    blendInfo.isVertexColorBakedLighting = FALSE;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &blendInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = snapshot.destroyOverlayMeshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.overlay");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }
  }

  if (snapshot.blockOutlineMeshHandle != nullptr) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.blockOutline");
    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.alphaTestEnabled = TRUE;
    blendInfo.alphaTestReferenceValue = 1;
    blendInfo.alphaTestCompareOp = 4;
    blendInfo.alphaBlendEnabled = TRUE;
    blendInfo.srcColorBlendFactor = 6;
    blendInfo.dstColorBlendFactor = 7;
    blendInfo.colorBlendOp = 0;
    blendInfo.srcAlphaBlendFactor = 1;
    blendInfo.dstAlphaBlendFactor = 0;
    blendInfo.alphaBlendOp = 0;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgNone;
    blendInfo.textureAlphaOperation = kRtTextureOpSelectArg1;
    blendInfo.isVertexColorBakedLighting = FALSE;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &blendInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = snapshot.blockOutlineMeshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.blockOutline");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }
  }

  if (snapshot.particleMeshHandle != nullptr) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.particles");
    remixapi_InstanceInfoBlendEXT blendInfo {};
    blendInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
    blendInfo.textureColorArg1Source = kRtTextureArgTexture;
    blendInfo.textureColorArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureColorOperation = kRtTextureOpModulate;
    blendInfo.textureAlphaArg1Source = kRtTextureArgTexture;
    blendInfo.textureAlphaArg2Source = kRtTextureArgVertexColor0;
    blendInfo.textureAlphaOperation = kRtTextureOpModulate;
    blendInfo.isVertexColorBakedLighting = FALSE;
    blendInfo.alphaBlendEnabled = TRUE;
    blendInfo.srcColorBlendFactor = 6;
    blendInfo.dstColorBlendFactor = 7;
    blendInfo.colorBlendOp = 0;
    blendInfo.srcAlphaBlendFactor = 1;
    blendInfo.dstAlphaBlendFactor = 0;
    blendInfo.alphaBlendOp = 0;

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &blendInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = snapshot.particleMeshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.particle");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }
  }

  if (!snapshot.torchLights.empty()) {
    MCRTX_TRACY_SCOPE("drawCapturedGeometry.torchLights");
    MCRTX_TRACY_VALUE(snapshot.torchLights.size());
    if (!loggedLightSubmissionPath_) {
      loggedLightSubmissionPath_ = true;
      std::ostringstream pathStream;
      pathStream << "Light submission path: ";
      if (remix_.AutoInstancePersistentLights != nullptr) {
        pathStream << "AutoInstancePersistentLights (persistent RTXDI reservoirs)";
      } else if (remix_.DrawLightInstance != nullptr) {
        pathStream << "DrawLightInstance per-frame fallback (no persistent reservoirs -- expect boiling)";
      } else {
        pathStream << "NONE -- lights will not be rendered";
      }
      pathStream << "; CreateLight="
             << (remix_.CreateLight != nullptr ? "yes" : "no")
                 << "; torch lights registered=" << snapshot.submittedTorchLights;
      log(pathStream.str());
    }
    if (remix_.AutoInstancePersistentLights != nullptr) {
      const remixapi_ErrorCode result = [&]() {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "AutoInstancePersistentLights");
        return remix_.AutoInstancePersistentLights();
      }();
      if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
        setError("AutoInstancePersistentLights failed: " + errorCodeToString(result));
        return false;
      }
    } else if (remix_.DrawLightInstance != nullptr) {
      for (remixapi_LightHandle lightHandle : snapshot.torchLights) {
        if (lightHandle == nullptr) {
          continue;
        }

        const remixapi_ErrorCode result = [&]() {
          MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawLightInstance");
          return remix_.DrawLightInstance(lightHandle);
        }();
        if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
          setError("DrawLightInstance failed: " + errorCodeToString(result));
          return false;
        }
      }
    }
  }
  return true;
}

bool RemixRenderer::submitCamera(const CameraState& camera) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::submitCamera");
  MCRTX_TRACY_SCOPE("RemixRenderer::submitCamera");
  const float nearPlane = camera.nearPlane > 0.001f ? camera.nearPlane : 0.05f;
  const float farPlane = camera.farPlane > nearPlane ? camera.farPlane : (nearPlane + 1024.0f);
  const float aspect = camera.aspect > 0.001f ? camera.aspect : 1.0f;
  const float worldFovYDegrees = (camera.fovYDegrees >= 1.0f && camera.fovYDegrees <= 179.0f) ? camera.fovYDegrees : 70.0f;
  const float viewModelFovYDegrees = (viewModelFovDegrees_ >= 1.0f && viewModelFovDegrees_ <= 179.0f) ? viewModelFovDegrees_ : worldFovYDegrees;
  const float viewModelNearPlane = std::min(nearPlane, 0.001f);
  const float viewModelFarPlane = farPlane > viewModelNearPlane ? farPlane : (viewModelNearPlane + 1024.0f);

  remixapi_CameraInfoParameterizedEXT params {};
  params.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO_PARAMETERIZED_EXT;
  params.position = {
      static_cast<float>(camera.position[0]),
      static_cast<float>(camera.position[1]),
      static_cast<float>(camera.position[2])};
  params.forward = {camera.forward[0], camera.forward[1], camera.forward[2]};
  params.up = {camera.up[0], camera.up[1], camera.up[2]};
  params.right = {camera.right[0], camera.right[1], camera.right[2]};
  params.fovYInDegrees = worldFovYDegrees;
  params.aspect = aspect;
  params.nearPlane = nearPlane;
  params.farPlane = farPlane;

  remixapi_CameraInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO;
  info.pNext = &params;
  info.type = REMIXAPI_CAMERA_TYPE_WORLD;

  remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetupCamera.world");
    MCRTX_TRACY_SCOPE("submitCamera.world");
    return remix_.SetupCamera(&info);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetupCamera(world) failed: " + errorCodeToString(result));
    return false;
  }

  params.fovYInDegrees = viewModelFovYDegrees;
  params.nearPlane = viewModelNearPlane;
  params.farPlane = viewModelFarPlane;
  info.type = REMIXAPI_CAMERA_TYPE_VIEW_MODEL;
  result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetupCamera.viewmodel");
    MCRTX_TRACY_SCOPE("submitCamera.viewmodel");
    return remix_.SetupCamera(&info);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetupCamera(viewmodel) failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

}  // namespace mcrtx
