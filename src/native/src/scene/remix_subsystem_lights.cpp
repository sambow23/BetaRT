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
bool isTorchLightItemId(int itemId) {
  return itemId == kTorchBlockId || itemId == kRedstoneTorchOnBlockId;
}
std::uint64_t makeEntityHeldTorchLightHash(int entityId) {
  return kEntityHeldTorchLightHashSeed ^ static_cast<std::uint64_t>(static_cast<std::uint32_t>(entityId));
}

remixapi_Float3D entityHeldTorchRadiance(int itemId) {
  return itemId == kRedstoneTorchOnBlockId ? kRedstoneTorchLightRadiance : kTorchLightRadiance;
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

void RemixRenderer::setEntityHeldTorch(int entityId, double worldX, double worldY, double worldZ, int itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setEntityHeldTorch");
  MCRTX_TRACY_SCOPE("RemixRenderer::setEntityHeldTorch");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || entityId < 0) {
    return;
  }

  if (!heldTorchLightsEnabled_) {
    destroyEntityHeldTorchLight(entityId);
    return;
  }

  const bool supportsLightCreation = remix_.CreateLight != nullptr;
  if (!supportsLightCreation || !isTorchLightItemId(itemId)) {
    destroyEntityHeldTorchLight(entityId);
    return;
  }

  entityHeldTorchLightsSeenThisFrame_.insert(entityId);
  MCRTX_TRACY_VALUE(entityHeldTorchLights_.size());

  EntityHeldTorchLightState& lightState = entityHeldTorchLights_[entityId];
  lightState.worldX = worldX;
  lightState.worldY = worldY;
  lightState.worldZ = worldZ;
  lightState.itemId = itemId;
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
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
    return false;
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

  for (auto& [entityId, state] : entityHeldTorchLights_) {
    if (!refreshEntityHeldTorchLightDefinition(entityId, state, renderOrigin)) {
      return false;
    }
  }

  return true;
}

bool RemixRenderer::refreshEntityHeldTorchLightDefinition(
    int entityId,
    EntityHeldTorchLightState& state,
    const WorldRenderOrigin& renderOrigin) {
  if (remix_.CreateLight == nullptr || !isTorchLightItemId(state.itemId)) {
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
  lightInfo.hash = persistentLightHashForRenderOrigin(makeEntityHeldTorchLightHash(entityId), renderOrigin);
  lightInfo.radiance = entityHeldTorchRadiance(state.itemId);
  lightInfo.isDynamic = TRUE;
  lightInfo.ignoreViewModel = FALSE;
  lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

  if (state.handle == nullptr) {
    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.entityHeldTorch");
      return remix_.CreateLight(&lightInfo, &state.handle);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      state.handle = nullptr;
      setError("CreateLight failed: " + errorCodeToString(result));
      return false;
    }
    state.renderOrigin = renderOrigin;
    return true;
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyLightHandle(state.handle);
    state.handle = nullptr;
    return refreshEntityHeldTorchLightDefinition(entityId, state, renderOrigin);
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.entityHeldTorch");
    return remix_.UpdateLightDefinition(state.handle, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
    return false;
  }

  state.renderOrigin = renderOrigin;
  return true;
}

bool RemixRenderer::reconcileHeldItemTorchLight(const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileHeldItemTorchLight");
  MCRTX_TRACY_SCOPE("RemixRenderer::reconcileHeldItemTorchLight");

  const bool supportsLightCreation = remix_.CreateLight != nullptr;
  const bool isTorch = heldItemId_ == kTorchBlockId;
  const bool isRedstoneTorch = heldItemId_ == kRedstoneTorchOnBlockId;
  if (!supportsLightCreation || (!isTorch && !isRedstoneTorch)) {
    destroyHeldItemTorchLight();
    return true;
  }

  const WorldRenderPosition lightPosition = rebaseWorldPosition(
      camera_.position[0] + camera_.forward[0] * kHeldTorchLightForwardOffset
          + camera_.right[0] * kHeldTorchLightRightOffset
          + camera_.up[0] * kHeldTorchLightUpOffset,
      camera_.position[1] + camera_.forward[1] * kHeldTorchLightForwardOffset
          + camera_.right[1] * kHeldTorchLightRightOffset
          + camera_.up[1] * kHeldTorchLightUpOffset,
      camera_.position[2] + camera_.forward[2] * kHeldTorchLightForwardOffset
          + camera_.right[2] * kHeldTorchLightRightOffset
          + camera_.up[2] * kHeldTorchLightUpOffset,
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
  lightInfo.hash = persistentLightHashForRenderOrigin(kHeldTorchLightHash, renderOrigin);
  lightInfo.radiance = isRedstoneTorch ? kRedstoneTorchLightRadiance : kTorchLightRadiance;
  lightInfo.isDynamic = TRUE;
  lightInfo.ignoreViewModel = TRUE;
  lightInfo.ignoreFirstPersonPlayerShadow = TRUE;

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
    heldItemTorchLightRenderOrigin_ = renderOrigin;
    return true;
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.recreate");
    destroyHeldItemTorchLight();
    return reconcileHeldItemTorchLight(renderOrigin);
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.update");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.heldTorch");
    return remix_.UpdateLightDefinition(heldItemTorchLightHandle_, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
    return false;
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
  destroyHeldItemTorchLight();
  while (!entityHeldTorchLights_.empty()) {
    destroyEntityHeldTorchLight(entityHeldTorchLights_.begin()->first);
  }
  entityHeldTorchLightsSeenThisFrame_.clear();
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
  entityHeldTorchLightsSeenThisFrame_.erase(entityId);
}

void RemixRenderer::destroyChunkTorchLights(ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyChunkTorchLights");
  for (const TorchLightPlacement& placement : meshData.torchLights) {
    destroyTorchLight(placement.blockPosition);
  }
  meshData.torchLights.clear();
}

}  // namespace mcrtx
