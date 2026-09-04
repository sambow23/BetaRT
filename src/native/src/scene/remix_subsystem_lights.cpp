// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/scene/remix_light_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"

#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::light;

namespace {
enum class TorchLightHashLogMode {
  Disabled,
  Changes,
  Verbose,
};

TorchLightHashLogMode torchLightHashLogMode() {
  static const TorchLightHashLogMode mode = []() {
    const std::string value = readEnvironmentVariable("MCRTX_DEBUG_LIGHT_HASHES");
    if (value.empty()) {
      return TorchLightHashLogMode::Disabled;
    }

    if (equalsIgnoreCase(value, "verbose") || equalsIgnoreCase(value, "all")) {
      return TorchLightHashLogMode::Verbose;
    }

    return isTruthyEnvValue(value.c_str())
        ? TorchLightHashLogMode::Changes
        : TorchLightHashLogMode::Disabled;
  }();
  return mode;
}

bool sameWorldRenderPosition(const WorldRenderPosition& left, const WorldRenderPosition& right) noexcept {
  return left.x == right.x
      && left.y == right.y
      && left.z == right.z;
}

remixapi_LightInfoLocalOriginEXT makeLightLocalOriginInfo(
    const WorldRenderOrigin& renderOrigin,
    void* next) noexcept {
  remixapi_LightInfoLocalOriginEXT originInfo {};
  originInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_LOCAL_ORIGIN_EXT;
  originInfo.pNext = next;
  originInfo.origin = {
      static_cast<float>(renderOrigin.x),
      static_cast<float>(renderOrigin.y),
      static_cast<float>(renderOrigin.z),
  };
  return originInfo;
}

void appendHex64(std::ostringstream& stream, std::uint64_t value) {
  stream << "0x"
         << std::hex << std::uppercase << std::setfill('0') << std::setw(16)
         << value
         << std::dec << std::nouppercase << std::setfill(' ');
}

void appendHandle(std::ostringstream& stream, remixapi_LightHandle handle) {
  stream << "0x"
         << std::hex << std::uppercase << reinterpret_cast<std::uintptr_t>(handle)
         << std::dec << std::nouppercase;
}

void appendOrigin(std::ostringstream& stream, const WorldRenderOrigin& origin) {
  stream << "(enabled=" << (origin.enabled ? "true" : "false")
         << ", x=" << origin.x
         << ", y=" << origin.y
         << ", z=" << origin.z
         << ")";
}

void appendRenderPosition(std::ostringstream& stream, const WorldRenderPosition& position) {
  stream << std::setprecision(9)
         << "(" << position.x
         << ", " << position.y
         << ", " << position.z
         << ")";
}

bool shouldLogTorchLightHashUpdate(
    TorchLightHashLogMode mode,
    const TorchLightState& previousState,
    const WorldRenderOrigin& renderOrigin,
    const WorldRenderPosition& submittedPosition,
    std::uint64_t apiHash) noexcept {
  if (mode == TorchLightHashLogMode::Verbose) {
    return true;
  }

  if (mode == TorchLightHashLogMode::Disabled) {
    return false;
  }

  return previousState.apiHash != apiHash
      || !sameWorldRenderOrigin(previousState.renderOrigin, renderOrigin)
      || !sameWorldRenderPosition(previousState.submittedPosition, submittedPosition);
}

std::string describeTorchLightHashSubmission(
    std::string_view action,
    const TorchLightPlacement& placement,
    const WorldRenderOrigin& renderOrigin,
    const WorldRenderPosition& submittedPosition,
    std::uint64_t apiHash,
    remixapi_LightHandle handle,
    const TorchLightState* previousState) {
  std::ostringstream stream;
  stream << "MCRTX_DEBUG_LIGHT_HASHES torchLight." << action
         << " block=(" << placement.blockPosition.x
         << ", " << placement.blockPosition.y
         << ", " << placement.blockPosition.z
         << ") worldLight=" << std::setprecision(9)
         << "(" << placement.lightX
         << ", " << placement.lightY
         << ", " << placement.lightZ
         << ") submittedLight=";
  appendRenderPosition(stream, submittedPosition);
  stream << " origin=";
  appendOrigin(stream, renderOrigin);
  stream << " apiHash=";
  appendHex64(stream, apiHash);
  stream << " handle=";
  appendHandle(stream, handle);

  if (previousState != nullptr) {
    stream << " previousApiHash=";
    appendHex64(stream, previousState->apiHash);
    stream << " previousSubmittedLight=";
    appendRenderPosition(stream, previousState->submittedPosition);
    stream << " previousOrigin=";
    appendOrigin(stream, previousState->renderOrigin);
  }

  return stream.str();
}
bool isEmissiveEntityItem(int itemId) {
  return itemId == kTorchBlockId || itemId == kRedstoneTorchOnBlockId || itemId == kLavaBucketItemId;
}

std::uint64_t makeEntityLightHash(int entityId) {
  return kEntityHeldTorchLightHashSeed ^ static_cast<std::uint64_t>(static_cast<std::uint32_t>(entityId));
}

remixapi_Float3D entityLightRadiance(int itemId) {
  if (itemId == kRedstoneTorchOnBlockId) return kRedstoneTorchLightRadiance;
  if (itemId == kLavaBucketItemId) return kLavaBucketLightRadiance;
  return kTorchLightRadiance;
}
}  // namespace
void RemixRenderer::setFirstPersonHeldItem(int itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setFirstPersonHeldItem");
  MCRTX_TRACY_SCOPE("RemixRenderer::setFirstPersonHeldItem");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  heldItemId_ = heldTorchLightsEnabled_ ? itemId : -1;
}

void RemixRenderer::setEntityLight(int entityId, double worldX, double worldY, double worldZ, int itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setEntityLight");
  MCRTX_TRACY_SCOPE("RemixRenderer::setEntityLight");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || entityId < 0) {
    return;
  }

  const bool supportsLightCreation = remix_.CreateLight != nullptr;
  if (!heldTorchLightsEnabled_ || !supportsLightCreation || !isEmissiveEntityItem(itemId)) {
    entityHeldTorchLightInputs_.erase(entityId);
    return;
  }

  MCRTX_TRACY_VALUE(entityHeldTorchLightInputs_.size());
  entityHeldTorchLightInputs_[entityId] = {worldX, worldY, worldZ, itemId};
}

void RemixRenderer::publishLightFrameLocked() {
  entityHeldTorchLightInputs_.swap(publishedEntityHeldTorchLightInputs_);
  publishedHeldItemId_ = heldItemId_;
}

bool RemixRenderer::createTorchLight(const TorchLightPlacement& placement, const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::createTorchLight");
  const WorldRenderPosition lightPosition = rebaseWorldPosition(
      placement.lightX,
      placement.lightY,
      placement.lightZ,
      renderOrigin);
  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      lightPosition.x,
      lightPosition.y,
      lightPosition.z,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &sphereInfo);

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &originInfo;
  lightInfo.hash = persistentLightHashForRenderOrigin(makeTorchLightHash(placement.blockPosition), renderOrigin);
  lightInfo.radiance = placement.radiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;
  lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

  remixapi_LightHandle lightHandle = nullptr;
  remixapi_ErrorCode result;
  result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.torch");
    return remix_.CreateLight(&lightInfo, &lightHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateLight failed: " + errorCodeToString(result));
    return false;
  }
  cancelDeferredLightDestroy(lightHandle);

  if (torchLightHashLogMode() != TorchLightHashLogMode::Disabled) {
    log(describeTorchLightHashSubmission(
        "create",
        placement,
        renderOrigin,
        lightPosition,
        lightInfo.hash,
        lightHandle,
        nullptr));
  }

  torchLights_[placement.blockPosition] = {lightHandle, renderOrigin, lightInfo.hash, lightPosition};
  torchLightPlacements_[placement.blockPosition] = placement;
  return true;
}

bool RemixRenderer::updateTorchLight(const TorchLightPlacement& placement, const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateTorchLight");
  const auto lightIt = torchLights_.find(placement.blockPosition);
  if (lightIt == torchLights_.end() || lightIt->second.handle == nullptr) {
    return createTorchLight(placement, renderOrigin);
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyTorchLight(placement.blockPosition);
    return createTorchLight(placement, renderOrigin);
  }

  const WorldRenderPosition lightPosition = rebaseWorldPosition(
      placement.lightX,
      placement.lightY,
      placement.lightZ,
      renderOrigin);
  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {lightPosition.x, lightPosition.y, lightPosition.z};
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &sphereInfo);

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &originInfo;
  lightInfo.hash = persistentLightHashForRenderOrigin(makeTorchLightHash(placement.blockPosition), renderOrigin);
  lightInfo.radiance = placement.radiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;
  lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.torch");
    return remix_.UpdateLightDefinition(lightIt->second.handle, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    // UpdateLightDefinition is the per-frame draw registration for
    // AutoInstancePersistentLights. On transient failure (e.g. queue full),
    // do not destroy and recreate the light, as that causes a 1-frame flicker.
    // Instead, just return true and try again next frame.
    return true;
  }

  const TorchLightHashLogMode logMode = torchLightHashLogMode();
  if (shouldLogTorchLightHashUpdate(logMode, lightIt->second, renderOrigin, lightPosition, lightInfo.hash)) {
    log(describeTorchLightHashSubmission(
        "update",
        placement,
        renderOrigin,
        lightPosition,
        lightInfo.hash,
        lightIt->second.handle,
        &lightIt->second));
  }

  torchLightPlacements_[placement.blockPosition] = placement;
  lightIt->second.renderOrigin = renderOrigin;
  lightIt->second.apiHash = lightInfo.hash;
  lightIt->second.submittedPosition = lightPosition;
  return true;
}

bool RemixRenderer::reconcileChunkTorchLights(
    ChunkMeshData& meshData,
    const std::vector<TorchLightPlacement>& desiredTorchLights) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileChunkTorchLights");
  MCRTX_TRACY_SCOPE("RemixRenderer::reconcileChunkTorchLights");
  MCRTX_TRACY_VALUE(desiredTorchLights.size());
  if (remix_.CreateLight == nullptr) {
    destroyChunkTorchLights(meshData);
    return true;
  }

  std::vector<WorldBlockPosition> createdLights;
  createdLights.reserve(desiredTorchLights.size());
  const WorldRenderOrigin renderOrigin = currentRenderOriginLocked();
  for (const TorchLightPlacement& placement : desiredTorchLights) {
    const bool existed = torchLights_.find(placement.blockPosition) != torchLights_.end();

    if (!updateTorchLight(placement, renderOrigin)) {
      for (const WorldBlockPosition& createdPosition : createdLights) {
        destroyTorchLight(createdPosition);
      }
      return false;
    }
    if (!existed) {
      createdLights.push_back(placement.blockPosition);
    }
  }

  for (const TorchLightPlacement& placement : meshData.torchLights) {
    if (findTorchLightPlacement(desiredTorchLights, placement.blockPosition) == nullptr) {
      destroyTorchLight(placement.blockPosition);
    }
  }

  meshData.torchLights = desiredTorchLights;
  return true;
}

bool RemixRenderer::refreshTorchLightDefinitions(const WorldRenderOrigin& renderOrigin) {
  std::vector<TorchLightPlacement> placements;
  placements.reserve(torchLightPlacements_.size());
  for (const auto& [position, placement] : torchLightPlacements_) {
    (void)position;
    placements.push_back(placement);
  }

  for (const TorchLightPlacement& placement : placements) {
    if (!updateTorchLight(placement, renderOrigin)) {
      return false;
    }
  }

  std::vector<PortalLightPlacement> portalPlacements;
  portalPlacements.reserve(portalLightPlacements_.size());
  for (const auto& [position, placement] : portalLightPlacements_) {
    (void)position;
    portalPlacements.push_back(placement);
  }

  for (const PortalLightPlacement& placement : portalPlacements) {
    if (!updatePortalLight(placement, renderOrigin)) {
      return false;
    }
  }

  return true;
}

bool RemixRenderer::updateEntityLight(
    int entityId,
    EntityHeldTorchLightState& state,
    const WorldRenderOrigin& renderOrigin) {
  if (remix_.CreateLight == nullptr || !isEmissiveEntityItem(state.itemId)) {
    if (state.handle != nullptr) {
      destroyLightHandle(state.handle);
      state.handle = nullptr;
    }
    return true;
  }

  const WorldRenderPosition lightPosition = rebaseWorldPosition(
      state.worldX,
      state.worldY,
      state.worldZ,
      renderOrigin);

  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      lightPosition.x,
      lightPosition.y,
      lightPosition.z,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &sphereInfo);

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &originInfo;
  lightInfo.hash = persistentLightHashForRenderOrigin(makeEntityLightHash(entityId), renderOrigin);
  lightInfo.radiance = entityLightRadiance(state.itemId);
  lightInfo.isDynamic = TRUE;
  lightInfo.ignoreViewModel = TRUE;
  lightInfo.ignoreFirstPersonPlayerShadow = TRUE;

  if (state.handle == nullptr) {
    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.entityLight");
      return remix_.CreateLight(&lightInfo, &state.handle);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      state.handle = nullptr;
      setError("CreateLight failed: " + errorCodeToString(result));
      return false;
    }
    cancelDeferredLightDestroy(state.handle);
    state.renderOrigin = renderOrigin;
    return true;
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyLightHandle(state.handle);
    state.handle = nullptr;
    return updateEntityLight(entityId, state, renderOrigin);
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.entityHeldTorch");
    return remix_.UpdateLightDefinition(state.handle, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    // On transient failure (e.g. queue full), do not destroy and recreate
    // the light, as that causes a flicker. Just return true.
    return true;
  }

  state.renderOrigin = renderOrigin;
  return true;
}

bool RemixRenderer::reconcileHeldItemTorchLight(
    int itemId,
    const CameraState& camera,
    const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileHeldItemTorchLight");
  MCRTX_TRACY_SCOPE("RemixRenderer::reconcileHeldItemTorchLight");

  const bool supportsLightCreation = remix_.CreateLight != nullptr;
  if (!supportsLightCreation || !isEmissiveEntityItem(itemId) || firstPersonBodyEnabled_) {
    destroyHeldItemTorchLight();
    return true;
  }

  const WorldRenderPosition lightPosition {
      static_cast<float>(camera.position[0]) + camera.forward[0] * kHeldTorchLightForwardOffset
          + camera.right[0] * kHeldTorchLightRightOffset
          + camera.up[0] * kHeldTorchLightUpOffset,
      static_cast<float>(camera.position[1]) + camera.forward[1] * kHeldTorchLightForwardOffset
          + camera.right[1] * kHeldTorchLightRightOffset
          + camera.up[1] * kHeldTorchLightUpOffset,
      static_cast<float>(camera.position[2]) + camera.forward[2] * kHeldTorchLightForwardOffset
          + camera.right[2] * kHeldTorchLightRightOffset
          + camera.up[2] * kHeldTorchLightUpOffset,
  };
  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {lightPosition.x, lightPosition.y, lightPosition.z};
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &sphereInfo);

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &originInfo;
  lightInfo.hash = persistentLightHashForRenderOrigin(kHeldTorchLightHash, renderOrigin);
  lightInfo.radiance = entityLightRadiance(itemId);
  lightInfo.isDynamic = TRUE;
  lightInfo.ignoreViewModel = !firstPersonBodyEnabled_;
  lightInfo.ignoreFirstPersonPlayerShadow = !firstPersonBodyEnabled_;

  if (heldItemTorchLightHandle_ == nullptr) {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.create");
    remixapi_ErrorCode result;
    result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.heldTorch");
      return remix_.CreateLight(&lightInfo, &heldItemTorchLightHandle_);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      heldItemTorchLightHandle_ = nullptr;
      setError("CreateLight failed: " + errorCodeToString(result));
      return false;
    }
    cancelDeferredLightDestroy(heldItemTorchLightHandle_);
    heldItemTorchLightRenderOrigin_ = renderOrigin;
    return true;
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.recreate");
    destroyHeldItemTorchLight();
    return reconcileHeldItemTorchLight(itemId, camera, renderOrigin);
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.update");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.heldTorch");
    return remix_.UpdateLightDefinition(heldItemTorchLightHandle_, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    // On transient failure (e.g. queue full), do not destroy and recreate
    // the light, as that causes a flicker. Just return true.
    return true;
  }

  heldItemTorchLightRenderOrigin_ = renderOrigin;
  return true;
}

void RemixRenderer::destroyTorchLight(const WorldBlockPosition& position) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyTorchLight");
  const auto lightIt = torchLights_.find(position);
  if (lightIt == torchLights_.end()) {
    torchLightPlacements_.erase(position);
    return;
  }

  destroyLightHandle(lightIt->second.handle);
  torchLights_.erase(lightIt);
  torchLightPlacements_.erase(position);
}

void RemixRenderer::destroyHeldItemTorchLight() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyHeldItemTorchLight");
  if (heldItemTorchLightHandle_ == nullptr) {
    heldItemTorchLightRenderOrigin_ = {};
    return;
  }

  destroyLightHandle(heldItemTorchLightHandle_);
  heldItemTorchLightHandle_ = nullptr;
  heldItemTorchLightRenderOrigin_ = {};
}

void RemixRenderer::clearHeldTorchLightsLocked() {
  heldItemId_ = -1;
  publishedHeldItemId_ = -1;
  entityHeldTorchLightInputs_.clear();
  publishedEntityHeldTorchLightInputs_.clear();
  destroyHeldItemTorchLight();
  while (!entityHeldTorchLights_.empty()) {
    destroyEntityHeldTorchLight(entityHeldTorchLights_.begin()->first);
  }
}

bool RemixRenderer::updateEntityLightsLocked(
    const std::unordered_map<int, EntityHeldTorchLightInput>& lightInputs,
    const WorldRenderOrigin& renderOrigin) {
  for (auto it = entityHeldTorchLights_.begin(); it != entityHeldTorchLights_.end();) {
    if (lightInputs.find(it->first) == lightInputs.end()) {
      if (it->second.handle != nullptr) {
        destroyLightHandle(it->second.handle);
      }
      it = entityHeldTorchLights_.erase(it);
    } else {
      ++it;
    }
  }

  for (const auto& [entityId, input] : lightInputs) {
    EntityHeldTorchLightState& state = entityHeldTorchLights_[entityId];
    state.worldX = input.worldX;
    state.worldY = input.worldY;
    state.worldZ = input.worldZ;
    state.itemId = input.itemId;
    if (!updateEntityLight(entityId, state, renderOrigin)) {
      return false;
    }
  }
  return true;
}

void RemixRenderer::destroyEntityHeldTorchLight(int entityId) {
  const auto lightIt = entityHeldTorchLights_.find(entityId);
  if (lightIt == entityHeldTorchLights_.end()) {
    return;
  }

  if (lightIt->second.handle != nullptr) {
    destroyLightHandle(lightIt->second.handle);
  }
  entityHeldTorchLights_.erase(lightIt);
}

void RemixRenderer::destroyChunkTorchLights(ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyChunkTorchLights");
  for (const TorchLightPlacement& placement : meshData.torchLights) {
    destroyTorchLight(placement.blockPosition);
  }
  meshData.torchLights.clear();
}

bool RemixRenderer::createPortalLight(const PortalLightPlacement& placement, const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::createPortalLight");
  const WorldRenderPosition lightPosition = rebaseWorldPosition(
      placement.lightX,
      placement.lightY,
      placement.lightZ,
      renderOrigin);

  remixapi_LightInfoRectEXT rectInfoFront {};
  rectInfoFront.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_RECT_EXT;
  rectInfoFront.position = {lightPosition.x, lightPosition.y, lightPosition.z};
  rectInfoFront.shaping_hasvalue = TRUE;
  rectInfoFront.shaping_value.coneAngleDegrees = 333.0f;
  rectInfoFront.shaping_value.coneSoftness = 0.0f;
  rectInfoFront.shaping_value.focusExponent = 0.0f;
  rectInfoFront.volumetricRadianceScale = 3.0f;
  
  remixapi_LightInfoRectEXT rectInfoBack = rectInfoFront;
  
  const float nudge = 0.05f;
  if (placement.isZAxis) {
    rectInfoFront.position.x += nudge;
    rectInfoFront.xAxis = {0.0f, 0.0f, 1.0f};
    rectInfoFront.xSize = 1.0f;
    rectInfoFront.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoFront.ySize = 1.0f;
    rectInfoFront.direction = {1.0f, 0.0f, 0.0f};
    rectInfoFront.shaping_value.direction = rectInfoFront.direction;
    
    rectInfoBack.position.x -= nudge;
    rectInfoBack.xAxis = {0.0f, 0.0f, -1.0f};
    rectInfoBack.xSize = 1.0f;
    rectInfoBack.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoBack.ySize = 1.0f;
    rectInfoBack.direction = {-1.0f, 0.0f, 0.0f};
    rectInfoBack.shaping_value.direction = rectInfoBack.direction;
  } else {
    rectInfoFront.position.z += nudge;
    rectInfoFront.xAxis = {1.0f, 0.0f, 0.0f};
    rectInfoFront.xSize = 1.0f;
    rectInfoFront.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoFront.ySize = 1.0f;
    rectInfoFront.direction = {0.0f, 0.0f, 1.0f};
    rectInfoFront.shaping_value.direction = rectInfoFront.direction;
    
    rectInfoBack.position.z -= nudge;
    rectInfoBack.xAxis = {-1.0f, 0.0f, 0.0f};
    rectInfoBack.xSize = 1.0f;
    rectInfoBack.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoBack.ySize = 1.0f;
    rectInfoBack.direction = {0.0f, 0.0f, -1.0f};
    rectInfoBack.shaping_value.direction = rectInfoBack.direction;
  }

  remixapi_LightInfoLocalOriginEXT originInfoFront = makeLightLocalOriginInfo(renderOrigin, &rectInfoFront);
  remixapi_LightInfo lightInfoFront {};
  lightInfoFront.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfoFront.pNext = &originInfoFront;
  lightInfoFront.hash = persistentLightHashForRenderOrigin(makePortalLightHash(placement.blockPosition), renderOrigin);
  lightInfoFront.radiance = placement.radiance;
  lightInfoFront.isDynamic = FALSE;
  lightInfoFront.ignoreViewModel = FALSE;
  lightInfoFront.ignoreFirstPersonPlayerShadow = FALSE;

  remixapi_LightInfoLocalOriginEXT originInfoBack = makeLightLocalOriginInfo(renderOrigin, &rectInfoBack);
  remixapi_LightInfo lightInfoBack = lightInfoFront;
  lightInfoBack.pNext = &originInfoBack;
  lightInfoBack.hash = lightInfoFront.hash ^ 0x123456789ABCDEF0ull;

  remixapi_LightHandle lightHandleFront = nullptr;
  remixapi_LightHandle lightHandleBack = nullptr;
  
  if (remix_.CreateLight(&lightInfoFront, &lightHandleFront) != REMIXAPI_ERROR_CODE_SUCCESS) return false;
  cancelDeferredLightDestroy(lightHandleFront);
  if (remix_.CreateLight(&lightInfoBack, &lightHandleBack) != REMIXAPI_ERROR_CODE_SUCCESS) return false;
  cancelDeferredLightDestroy(lightHandleBack);

  portalLights_[placement.blockPosition] = {lightHandleFront, lightHandleBack, renderOrigin, lightInfoFront.hash, lightInfoBack.hash, lightPosition};
  portalLightPlacements_[placement.blockPosition] = placement;
  return true;
}

bool RemixRenderer::updatePortalLight(const PortalLightPlacement& placement, const WorldRenderOrigin& renderOrigin) {
  const auto lightIt = portalLights_.find(placement.blockPosition);
  if (lightIt == portalLights_.end() || lightIt->second.handleFront == nullptr) {
    return createPortalLight(placement, renderOrigin);
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyPortalLight(placement.blockPosition);
    return createPortalLight(placement, renderOrigin);
  }

  const WorldRenderPosition lightPosition = rebaseWorldPosition(
      placement.lightX,
      placement.lightY,
      placement.lightZ,
      renderOrigin);

  remixapi_LightInfoRectEXT rectInfoFront {};
  rectInfoFront.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_RECT_EXT;
  rectInfoFront.position = {lightPosition.x, lightPosition.y, lightPosition.z};
  rectInfoFront.shaping_hasvalue = TRUE;
  rectInfoFront.shaping_value.coneAngleDegrees = 333.0f;
  rectInfoFront.shaping_value.coneSoftness = 0.0f;
  rectInfoFront.shaping_value.focusExponent = 0.0f;
  rectInfoFront.volumetricRadianceScale = 3.0f;
  
  remixapi_LightInfoRectEXT rectInfoBack = rectInfoFront;
  
  const float nudge = 0.05f;
  if (placement.isZAxis) {
    rectInfoFront.position.x += nudge;
    rectInfoFront.xAxis = {0.0f, 0.0f, 1.0f};
    rectInfoFront.xSize = 1.0f;
    rectInfoFront.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoFront.ySize = 1.0f;
    rectInfoFront.direction = {1.0f, 0.0f, 0.0f};
    rectInfoFront.shaping_value.direction = rectInfoFront.direction;
    
    rectInfoBack.position.x -= nudge;
    rectInfoBack.xAxis = {0.0f, 0.0f, -1.0f};
    rectInfoBack.xSize = 1.0f;
    rectInfoBack.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoBack.ySize = 1.0f;
    rectInfoBack.direction = {-1.0f, 0.0f, 0.0f};
    rectInfoBack.shaping_value.direction = rectInfoBack.direction;
  } else {
    rectInfoFront.position.z += nudge;
    rectInfoFront.xAxis = {1.0f, 0.0f, 0.0f};
    rectInfoFront.xSize = 1.0f;
    rectInfoFront.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoFront.ySize = 1.0f;
    rectInfoFront.direction = {0.0f, 0.0f, 1.0f};
    rectInfoFront.shaping_value.direction = rectInfoFront.direction;
    
    rectInfoBack.position.z -= nudge;
    rectInfoBack.xAxis = {-1.0f, 0.0f, 0.0f};
    rectInfoBack.xSize = 1.0f;
    rectInfoBack.yAxis = {0.0f, 1.0f, 0.0f};
    rectInfoBack.ySize = 1.0f;
    rectInfoBack.direction = {0.0f, 0.0f, -1.0f};
    rectInfoBack.shaping_value.direction = rectInfoBack.direction;
  }

  remixapi_LightInfoLocalOriginEXT originInfoFront = makeLightLocalOriginInfo(renderOrigin, &rectInfoFront);
  remixapi_LightInfo lightInfoFront {};
  lightInfoFront.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfoFront.pNext = &originInfoFront;
  lightInfoFront.hash = persistentLightHashForRenderOrigin(makePortalLightHash(placement.blockPosition), renderOrigin);
  lightInfoFront.radiance = placement.radiance;
  lightInfoFront.isDynamic = FALSE;
  lightInfoFront.ignoreViewModel = FALSE;
  lightInfoFront.ignoreFirstPersonPlayerShadow = FALSE;

  remixapi_LightInfoLocalOriginEXT originInfoBack = makeLightLocalOriginInfo(renderOrigin, &rectInfoBack);
  remixapi_LightInfo lightInfoBack = lightInfoFront;
  lightInfoBack.pNext = &originInfoBack;
  lightInfoBack.hash = lightInfoFront.hash ^ 0x123456789ABCDEF0ull;

  const remixapi_ErrorCode resultFront = remix_.UpdateLightDefinition(lightIt->second.handleFront, &lightInfoFront);
  const remixapi_ErrorCode resultBack = remix_.UpdateLightDefinition(lightIt->second.handleBack, &lightInfoBack);

  if (resultFront != REMIXAPI_ERROR_CODE_SUCCESS || resultBack != REMIXAPI_ERROR_CODE_SUCCESS) {
    return true; // Transient failure
  }

  lightIt->second.renderOrigin = renderOrigin;
  lightIt->second.apiHashFront = lightInfoFront.hash;
  lightIt->second.apiHashBack = lightInfoBack.hash;
  lightIt->second.submittedPosition = lightPosition;
  return true;
}

bool RemixRenderer::reconcileChunkPortalLights(
    ChunkMeshData& meshData,
    const std::vector<PortalLightPlacement>& desiredPortalLights) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileChunkPortalLights");
  if (remix_.CreateLight == nullptr) {
    destroyChunkPortalLights(meshData);
    return true;
  }

  std::vector<WorldBlockPosition> createdLights;
  createdLights.reserve(desiredPortalLights.size());
  const WorldRenderOrigin renderOrigin = currentRenderOriginLocked();
  for (const PortalLightPlacement& placement : desiredPortalLights) {
    const bool existed = portalLights_.find(placement.blockPosition) != portalLights_.end();

    if (!updatePortalLight(placement, renderOrigin)) {
      for (const WorldBlockPosition& createdPosition : createdLights) {
        destroyPortalLight(createdPosition);
      }
      return false;
    }
    if (!existed) {
      createdLights.push_back(placement.blockPosition);
    }
  }

  for (const PortalLightPlacement& placement : meshData.portalLights) {
    if (findPortalLightPlacement(desiredPortalLights, placement.blockPosition) == nullptr) {
      destroyPortalLight(placement.blockPosition);
    }
  }

  meshData.portalLights = desiredPortalLights;
  return true;
}

void RemixRenderer::destroyPortalLight(const WorldBlockPosition& position) {
  const auto it = portalLights_.find(position);
  if (it != portalLights_.end()) {
    if (it->second.handleFront != nullptr) {
      destroyLightHandle(it->second.handleFront);
    }
    if (it->second.handleBack != nullptr) {
      destroyLightHandle(it->second.handleBack);
    }
    portalLights_.erase(it);
  }
  portalLightPlacements_.erase(position);
}

void RemixRenderer::destroyChunkPortalLights(ChunkMeshData& meshData) {
  for (const PortalLightPlacement& placement : meshData.portalLights) {
    destroyPortalLight(placement.blockPosition);
  }
  meshData.portalLights.clear();
}

void RemixRenderer::reconcileParticleLights(
    const WorldRenderOrigin& renderOrigin,
    const std::vector<WorldRenderPosition>& flameParticleLightPositions) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileParticleLights");

  if (remix_.CreateLight == nullptr) {
    for (auto& light : activeFlameParticleLights_) {
      if (light.handle != nullptr) {
        destroyLightHandle(light.handle);
      }
    }
    activeFlameParticleLights_.clear();
    return;
  }

  // Destroy excess lights
  while (activeFlameParticleLights_.size() > flameParticleLightPositions.size()) {
    if (activeFlameParticleLights_.back().handle != nullptr) {
      destroyLightHandle(activeFlameParticleLights_.back().handle);
    }
    activeFlameParticleLights_.pop_back();
  }

  // Create or update lights
  for (std::size_t i = 0; i < flameParticleLightPositions.size(); ++i) {
    const WorldRenderPosition pos = rebaseWorldPosition(
        flameParticleLightPositions[i].x,
        flameParticleLightPositions[i].y,
        flameParticleLightPositions[i].z,
        renderOrigin);

    if (i >= activeFlameParticleLights_.size()) {
      TorchLightState newState {};
      activeFlameParticleLights_.push_back(newState);
    }

    TorchLightState& state = activeFlameParticleLights_[i];

    remixapi_LightInfoSphereEXT sphereInfo {};
    sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
    sphereInfo.position = {pos.x, pos.y, pos.z};
    sphereInfo.radius = 0.04f; // Increased radius for visibility
    sphereInfo.shaping_hasvalue = FALSE;
    sphereInfo.volumetricRadianceScale = 1.0f;

    remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &sphereInfo);

    remixapi_LightInfo lightInfo {};
    lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
    lightInfo.pNext = &originInfo;
    lightInfo.hash = 0x4D43525458464C00ull + i; // Stable hash per index
    lightInfo.radiance = {500.0f, 125.0f, 25.0f}; // Brighter orange pointlight
    lightInfo.isDynamic = TRUE; // Fast moving
    lightInfo.ignoreViewModel = FALSE;
    lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

    if (state.handle == nullptr) {
      if (remix_.CreateLight(&lightInfo, &state.handle) == REMIXAPI_ERROR_CODE_SUCCESS) {
        cancelDeferredLightDestroy(state.handle);
      }
    } else {
      if (remix_.UpdateLightDefinition != nullptr) {
        remix_.UpdateLightDefinition(state.handle, &lightInfo);
      } else {
        destroyLightHandle(state.handle);
        if (remix_.CreateLight(&lightInfo, &state.handle) == REMIXAPI_ERROR_CODE_SUCCESS) {
          cancelDeferredLightDestroy(state.handle);
        }
      }
    }

    state.renderOrigin = renderOrigin;
    state.apiHash = lightInfo.hash;
    state.submittedPosition = pos;
  }
}

bool RemixRenderer::createGlowstoneLight(
    const GlowstoneLightPlacement& placement,
    const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::createGlowstoneLight");
  if (remix_.CreateLight == nullptr) {
    return true;
  }

  const WorldRenderPosition centerPosition = rebaseWorldPosition(
      placement.blockPosition.x + 0.5f,
      placement.blockPosition.y + 0.5f,
      placement.blockPosition.z + 0.5f,
      renderOrigin);

  const float nudge = 0.51f; // Slightly outside the block
  const float rectSize = 1.0f;
  
  // BetaRT face mapping: 0: Z-, 1: Z+, 2: X-, 3: X+, 4: Y-, 5: Y+
  const remixapi_Float3D directions[6] = {
    {0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, 1.0f}, {-1.0f, 0.0f, 0.0f}, 
    {1.0f, 0.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}
  };
  const remixapi_Float3D xAxes[6] = {
    {-1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, 
    {0.0f, 0.0f, -1.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}
  };
  const remixapi_Float3D yAxes[6] = {
    {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, 
    {0.0f, 1.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, -1.0f}
  };

  std::uint64_t baseHash = light::makeGlowstoneLightHash(placement.blockPosition);
  
  GlowstoneLightState state {};
  state.renderOrigin = renderOrigin;
  state.submittedPosition = centerPosition;
  
  bool anyCreated = false;
  
  for (int i = 0; i < 6; ++i) {
    if ((placement.visibleFacesMask & (1 << i)) == 0) {
      continue;
    }
    
    remixapi_LightInfoRectEXT rectInfo {};
    rectInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_RECT_EXT;
    rectInfo.position = {
      centerPosition.x + directions[i].x * nudge,
      centerPosition.y + directions[i].y * nudge,
      centerPosition.z + directions[i].z * nudge
    };
    rectInfo.xAxis = xAxes[i];
    rectInfo.xSize = rectSize;
    rectInfo.yAxis = yAxes[i];
    rectInfo.ySize = rectSize;
    rectInfo.direction = directions[i];
    
    rectInfo.shaping_hasvalue = FALSE;
    rectInfo.shaping_value.direction = directions[i];
    rectInfo.shaping_value.coneAngleDegrees = 180.0f; // Half-sphere
    rectInfo.shaping_value.coneSoftness = 0.1f;
    rectInfo.shaping_value.focusExponent = 0.0f;
    rectInfo.volumetricRadianceScale = 1.0f;
    
    remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &rectInfo);
    remixapi_LightInfo lightInfo {};
    lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
    lightInfo.pNext = &originInfo;
    lightInfo.hash = persistentLightHashForRenderOrigin(baseHash ^ (std::uint64_t(i) << 56), renderOrigin);
    lightInfo.radiance = light::kGlowstoneLightRadiance;
    lightInfo.isDynamic = FALSE;
    lightInfo.ignoreViewModel = FALSE;
    lightInfo.ignoreFirstPersonPlayerShadow = FALSE;
    
    remixapi_LightHandle handle = nullptr;
    const remixapi_ErrorCode result = [&]() {
      MCRTX_TRACY_SCOPE("createGlowstoneLight.createLight");
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.glowstone");
      return remix_.CreateLight(&lightInfo, &handle);
    }();

    if (result == REMIXAPI_ERROR_CODE_SUCCESS && handle != nullptr) {
      cancelDeferredLightDestroy(handle);
      state.handles[i] = handle;
      state.apiHashes[i] = lightInfo.hash;
      anyCreated = true;
    }
  }

  glowstoneLights_[placement.blockPosition] = state;
  glowstoneLightPlacements_[placement.blockPosition] = placement;
  return anyCreated;
}

bool RemixRenderer::updateGlowstoneLight(
    const GlowstoneLightPlacement& placement,
    const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateGlowstoneLight");
  if (remix_.UpdateLightDefinition == nullptr) {
    destroyGlowstoneLight(placement.blockPosition);
    return createGlowstoneLight(placement, renderOrigin);
  }

  auto lightIt = glowstoneLights_.find(placement.blockPosition);
  if (lightIt == glowstoneLights_.end()) {
    return createGlowstoneLight(placement, renderOrigin);
  }

  GlowstoneLightState& state = lightIt->second;
  const WorldRenderPosition centerPosition = rebaseWorldPosition(
      placement.blockPosition.x + 0.5f,
      placement.blockPosition.y + 0.5f,
      placement.blockPosition.z + 0.5f,
      renderOrigin);

  const float nudge = 0.51f;
  const float rectSize = 1.0f;
  
  // BetaRT face mapping: 0: Z-, 1: Z+, 2: X-, 3: X+, 4: Y-, 5: Y+
  const remixapi_Float3D directions[6] = {
    {0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, 1.0f}, {-1.0f, 0.0f, 0.0f}, 
    {1.0f, 0.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}
  };
  const remixapi_Float3D xAxes[6] = {
    {-1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, 
    {0.0f, 0.0f, -1.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}
  };
  const remixapi_Float3D yAxes[6] = {
    {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, 
    {0.0f, 1.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, -1.0f}
  };

  std::uint64_t baseHash = light::makeGlowstoneLightHash(placement.blockPosition);
  
  bool anyFailed = false;
  
  for (int i = 0; i < 6; ++i) {
    if ((placement.visibleFacesMask & (1 << i)) == 0) {
      if (state.handles[i] != nullptr) {
        destroyLightHandle(state.handles[i]);
        state.handles[i] = nullptr;
      }
      continue;
    }
    
    remixapi_LightInfoRectEXT rectInfo {};
    rectInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_RECT_EXT;
    rectInfo.position = {
      centerPosition.x + directions[i].x * nudge,
      centerPosition.y + directions[i].y * nudge,
      centerPosition.z + directions[i].z * nudge
    };
    rectInfo.xAxis = xAxes[i];
    rectInfo.xSize = rectSize;
    rectInfo.yAxis = yAxes[i];
    rectInfo.ySize = rectSize;
    rectInfo.direction = directions[i];
    
    rectInfo.shaping_hasvalue = FALSE;
    rectInfo.shaping_value.direction = directions[i];
    rectInfo.shaping_value.coneAngleDegrees = 180.0f;
    rectInfo.shaping_value.coneSoftness = 0.1f;
    rectInfo.shaping_value.focusExponent = 0.0f;
    rectInfo.volumetricRadianceScale = 1.0f;
    
    remixapi_LightInfoLocalOriginEXT originInfo = makeLightLocalOriginInfo(renderOrigin, &rectInfo);
    remixapi_LightInfo lightInfo {};
    lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
    lightInfo.pNext = &originInfo;
    lightInfo.hash = persistentLightHashForRenderOrigin(baseHash ^ (std::uint64_t(i) << 56), renderOrigin);
    lightInfo.radiance = light::kGlowstoneLightRadiance;
    lightInfo.isDynamic = FALSE;
    lightInfo.ignoreViewModel = FALSE;
    lightInfo.ignoreFirstPersonPlayerShadow = FALSE;
    
    if (state.handles[i] == nullptr) {
      remixapi_LightHandle handle = nullptr;
      if (remix_.CreateLight(&lightInfo, &handle) == REMIXAPI_ERROR_CODE_SUCCESS) {
        cancelDeferredLightDestroy(handle);
        state.handles[i] = handle;
        state.apiHashes[i] = lightInfo.hash;
      } else {
        anyFailed = true;
      }
    } else {
      if (remix_.UpdateLightDefinition(state.handles[i], &lightInfo) == REMIXAPI_ERROR_CODE_SUCCESS) {
        state.apiHashes[i] = lightInfo.hash;
      } else {
        anyFailed = true;
      }
    }
  }

  glowstoneLightPlacements_[placement.blockPosition] = placement;
  state.renderOrigin = renderOrigin;
  state.submittedPosition = centerPosition;
  return !anyFailed;
}

bool RemixRenderer::reconcileChunkGlowstoneLights(
    ChunkMeshData& meshData,
    const std::vector<GlowstoneLightPlacement>& desiredGlowstoneLights) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileChunkGlowstoneLights");
  if (remix_.CreateLight == nullptr) {
    destroyChunkGlowstoneLights(meshData);
    return true;
  }

  std::vector<WorldBlockPosition> createdLights;
  createdLights.reserve(desiredGlowstoneLights.size());
  const WorldRenderOrigin renderOrigin = currentRenderOriginLocked();
  for (const GlowstoneLightPlacement& placement : desiredGlowstoneLights) {
    const bool existed = glowstoneLights_.find(placement.blockPosition) != glowstoneLights_.end();

    if (!updateGlowstoneLight(placement, renderOrigin)) {
      for (const WorldBlockPosition& createdPosition : createdLights) {
        destroyGlowstoneLight(createdPosition);
      }
      return false;
    }
    if (!existed) {
      createdLights.push_back(placement.blockPosition);
    }
  }

  for (const GlowstoneLightPlacement& placement : meshData.glowstoneLights) {
    if (light::findGlowstoneLightPlacement(desiredGlowstoneLights, placement.blockPosition) == nullptr) {
      destroyGlowstoneLight(placement.blockPosition);
    }
  }

  meshData.glowstoneLights = desiredGlowstoneLights;
  return true;
}

void RemixRenderer::destroyGlowstoneLight(const WorldBlockPosition& position) {
  const auto it = glowstoneLights_.find(position);
  if (it != glowstoneLights_.end()) {
    for (int i = 0; i < 6; ++i) {
      if (it->second.handles[i] != nullptr) {
        destroyLightHandle(it->second.handles[i]);
      }
    }
    glowstoneLights_.erase(it);
  }
  glowstoneLightPlacements_.erase(position);
}

void RemixRenderer::destroyChunkGlowstoneLights(ChunkMeshData& meshData) {
  for (const GlowstoneLightPlacement& placement : meshData.glowstoneLights) {
    destroyGlowstoneLight(placement.blockPosition);
  }
  meshData.glowstoneLights.clear();
}

bool RemixRenderer::refreshGlowstoneLightDefinitions(const WorldRenderOrigin& renderOrigin) {
  std::vector<GlowstoneLightPlacement> placements;
  placements.reserve(glowstoneLightPlacements_.size());
  for (const auto& [position, placement] : glowstoneLightPlacements_) {
    (void)position;
    placements.push_back(placement);
  }

  for (const GlowstoneLightPlacement& placement : placements) {
    if (!updateGlowstoneLight(placement, renderOrigin)) {
      return false;
    }
  }

  return true;
}

}  // namespace mcrtx





