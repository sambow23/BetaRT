// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"
#include "mcrtx/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

namespace {

void clearActiveDynamicEntityState(DynamicEntityBuildState& state) {
  state.entityId = -1;
  state.hurtStage = 0;
  state.creeperFuseStage = 0;
  state.maxBoneCount = 0;
  state.quadFingerprint = 0;
  state.currentTextureIndex = 0xFFFFFFFFu;
  state.currentTextureFingerprint = 0;
  state.texturePaths.clear();
  state.quadCount = 0;
  state.boneTransforms.clear();
  state.active = false;
}

// Appends one quad to the active dynamic entity build. Vertices are packed as
// four interleaved [x, y, z, u, v] tuples (20 floats), matching the capture
// hook's argument order. Caller must hold the renderer mutex and have already
// validated active-entity preconditions. Returns false when the quad buffer is
// full so batch callers can stop early.
bool appendDynamicEntityQuadLocked(DynamicEntityBuildState& state,
                                   const float* vertices,
                                   std::uint32_t colorRgba,
                                   bool blendEnabled,
                                   std::uint32_t boneIndex) {
  if (state.quadCount >= state.quads.size()) {
    return false;
  }

  DynamicEntityQuad& quad = state.quads[state.quadCount++];
  quad.positions = {
      vertices[0], vertices[1], vertices[2],
      vertices[5], vertices[6], vertices[7],
      vertices[10], vertices[11], vertices[12],
      vertices[15], vertices[16], vertices[17],
  };
  quad.texcoords = {
      vertices[3], vertices[4],
      vertices[8], vertices[9],
      vertices[13], vertices[14],
      vertices[18], vertices[19],
  };
  quad.color = colorRgba;
  quad.textureIndex = state.currentTextureIndex;
  quad.textureFingerprint = state.currentTextureFingerprint;
  quad.blendEnabled = blendEnabled;
  quad.boneIndex = boneIndex;
  state.maxBoneCount = std::max(state.maxBoneCount, boneIndex + 1);
  hashDynamicEntityQuad(state.quadFingerprint, quad);
  return true;
}

struct BlockOutlineStyleParameters {
  float inflate;
  float alpha;
  float edgeHalfThickness;
  bool filled;
  bool glow;
  bool rgbCycle;
  float colorR;
  float colorG;
  float colorB;
};

struct BlockOutlineAnimatedColor {
  float colorR;
  float colorG;
  float colorB;
  std::size_t materialIndex;
};

constexpr ULONGLONG kBlockOutlineRgbCycleIntervalMilliseconds = 180;

BlockOutlineAnimatedColor currentBlockOutlineRgbColor() {
  constexpr std::array<std::array<float, 3>, 6> kBlockOutlineRgbPalette {{
      {1.0f, 0.2f, 0.2f},
      {1.0f, 0.7f, 0.15f},
      {0.25f, 1.0f, 0.25f},
      {0.2f, 1.0f, 1.0f},
      {0.3f, 0.45f, 1.0f},
      {1.0f, 0.2f, 1.0f},
  }};

  const std::size_t materialIndex = static_cast<std::size_t>(
      (GetTickCount64() / kBlockOutlineRgbCycleIntervalMilliseconds) % kBlockOutlineRgbPalette.size());
  const std::array<float, 3>& color = kBlockOutlineRgbPalette[materialIndex];
  return {color[0], color[1], color[2], materialIndex};
}

BlockOutlineStyleParameters blockOutlineStyleParametersFor(int style) {
  switch (style) {
    case 0:
      return {0.004f, 0.30f, 0.010f, false, false, false, 0.0f, 0.0f, 0.0f};
    case 5:
      return {0.004f, 0.30f, 0.0025f, false, false, false, 0.0f, 0.0f, 0.0f};
    case 3:
      return {0.004f, 0.30f, 0.010f, false, true, false, 1.0f, 1.0f, 1.0f};
    case 4:
      return {0.004f, 0.30f, 0.010f, false, true, true, 1.0f, 0.2f, 0.2f};
    case 2:
      return {0.010f, 0.22f, 0.0f, true, false, false, 0.0f, 0.0f, 0.0f};
    case 1:
    default:
      return {0.006f, 0.55f, 0.018f, false, false, false, 0.0f, 0.0f, 0.0f};
  }
}

void appendBlockOutlineBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::uint32_t outlineColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::array<std::int16_t, 6> outlineTerrainTiles {};
  appendBoxGeometry(
      minX,
      minY,
      minZ,
      maxX,
      maxY,
      maxZ,
      outlineTerrainTiles,
      outlineColor,
      vertices,
      indices);
}

void appendBlockOutlineFillGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::uint32_t outlineColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendBlockOutlineBoxGeometry(minX, minY, minZ, maxX, maxY, maxZ, outlineColor, vertices, indices);
}

void appendBlockOutlineWireGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    float edgeHalfThickness,
    std::uint32_t outlineColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float minYEdge0 = minY - edgeHalfThickness;
  const float minYEdge1 = minY + edgeHalfThickness;
  const float maxYEdge0 = maxY - edgeHalfThickness;
  const float maxYEdge1 = maxY + edgeHalfThickness;
  const float minZEdge0 = minZ - edgeHalfThickness;
  const float minZEdge1 = minZ + edgeHalfThickness;
  const float maxZEdge0 = maxZ - edgeHalfThickness;
  const float maxZEdge1 = maxZ + edgeHalfThickness;
  const float minXEdge0 = minX - edgeHalfThickness;
  const float minXEdge1 = minX + edgeHalfThickness;
  const float maxXEdge0 = maxX - edgeHalfThickness;
  const float maxXEdge1 = maxX + edgeHalfThickness;

  appendBlockOutlineBoxGeometry(minX, minYEdge0, minZEdge0, maxX, minYEdge1, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minX, minYEdge0, maxZEdge0, maxX, minYEdge1, maxZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minX, maxYEdge0, minZEdge0, maxX, maxYEdge1, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minX, maxYEdge0, maxZEdge0, maxX, maxYEdge1, maxZEdge1, outlineColor, vertices, indices);

  appendBlockOutlineBoxGeometry(minXEdge0, minY, minZEdge0, minXEdge1, maxY, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minXEdge0, minY, maxZEdge0, minXEdge1, maxY, maxZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, minY, minZEdge0, maxXEdge1, maxY, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, minY, maxZEdge0, maxXEdge1, maxY, maxZEdge1, outlineColor, vertices, indices);

  appendBlockOutlineBoxGeometry(minXEdge0, minYEdge0, minZ, minXEdge1, minYEdge1, maxZ, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minXEdge0, maxYEdge0, minZ, minXEdge1, maxYEdge1, maxZ, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, minYEdge0, minZ, maxXEdge1, minYEdge1, maxZ, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, maxYEdge0, minZ, maxXEdge1, maxYEdge1, maxZ, outlineColor, vertices, indices);
}

bool isTorchLightItemId(int itemId) {
  return itemId == kTorchBlockId || itemId == kRedstoneTorchOnBlockId;
}

std::uint8_t dynamicEntityQuadAlpha(std::uint32_t colorRgba) {
  return static_cast<std::uint8_t>((colorRgba >> 24) & 0xFFu);
}

DynamicEntityMaterialClass dynamicEntityMaterialClassForQuad(const DynamicEntityQuad& quad) {
  return quad.blendEnabled || dynamicEntityQuadAlpha(quad.color) < 0xFFu
      ? DynamicEntityMaterialClass::Translucent
      : DynamicEntityMaterialClass::Cutout;
}

std::uint64_t makeEntityHeldTorchLightHash(int entityId) {
  return kEntityHeldTorchLightHashSeed ^ static_cast<std::uint64_t>(static_cast<std::uint32_t>(entityId));
}

remixapi_Float3D entityHeldTorchRadiance(int itemId) {
  return itemId == kRedstoneTorchOnBlockId ? kRedstoneTorchLightRadiance : kTorchLightRadiance;
}

}  // namespace

void RemixRenderer::updateCloudLayer(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
  float,
    float colorR,
    float colorG,
    float colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateCloudLayer");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  cloudTransformX_ = 0.0f;
  cloudTransformY_ = 0.0f;
  cloudTransformZ_ = 0.0f;

  if (cloudMeshHandle_ == nullptr || cloudMeshFancy_ != fancy) {
    log(std::string("Rebuilding cloud mesh: mode=") + (fancy ? "fancy" : "fast")
        + (cloudMeshHandle_ == nullptr ? " reason=missing" : " reason=mode-switch"));
  }

  rebuildCloudMesh(fancy, cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB);
}

void RemixRenderer::updateAtmosphereState(float celestialAngle, bool forceDarkAtmosphere) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateAtmosphereState");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  updateAtmosphereConfigLocked(celestialAngle, forceDarkAtmosphere);
}

void RemixRenderer::clearCloudLayer() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearCloudLayer");
  std::scoped_lock lock(mutex_);
  if (cloudMeshHandle_ != nullptr) {
    log("Clearing cloud mesh cache");
  }
  destroyCloudMesh();
}

void RemixRenderer::beginDynamicEntityFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDynamicEntityFrame");
  MCRTX_TRACY_SCOPE("RemixRenderer::beginDynamicEntityFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  MCRTX_TRACY_VALUE(dynamicEntityFrameInstanceCount_);
  clearDynamicEntityFrameInstances();
  clearActiveDynamicEntityState(activeDynamicEntity_);
  heldItemId_ = -1;
  entityHeldTorchLightsSeenThisFrame_.clear();
}

void RemixRenderer::beginDynamicEntity(int entityId, std::uint32_t hurtStage, std::uint32_t creeperFuseStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDynamicEntity");
  MCRTX_TRACY_SCOPE("RemixRenderer::beginDynamicEntity");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_) {
    clearActiveDynamicEntityState(activeDynamicEntity_);
    return;
  }

  clearActiveDynamicEntityState(activeDynamicEntity_);
  activeDynamicEntity_.entityId = entityId;
  activeDynamicEntity_.hurtStage = std::min(hurtStage, kDynamicEntityMaxHurtStage);
  activeDynamicEntity_.creeperFuseStage = std::min(creeperFuseStage, kDynamicEntityMaxCreeperFuseStage);
  activeDynamicEntity_.quadFingerprint = beginDynamicEntityFingerprint(
      activeDynamicEntity_.hurtStage,
      activeDynamicEntity_.creeperFuseStage);
  activeDynamicEntity_.active = entityId >= 0;
  activeDynamicEntity_.texturePaths.reserve(4);
  activeDynamicEntity_.boneTransforms.reserve(32);
}

void RemixRenderer::setDynamicEntityTexture(const std::string& texturePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityTexture");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active) {
    return;
  }

  for (std::size_t index = 0; index < activeDynamicEntity_.texturePaths.size(); ++index) {
    if (activeDynamicEntity_.texturePaths[index] == texturePath) {
      activeDynamicEntity_.currentTextureIndex = static_cast<std::uint32_t>(index);
      activeDynamicEntity_.currentTextureFingerprint = computeDynamicEntityTextureFingerprint(texturePath);
      return;
    }
  }

  activeDynamicEntity_.currentTextureIndex = static_cast<std::uint32_t>(activeDynamicEntity_.texturePaths.size());
  activeDynamicEntity_.currentTextureFingerprint = computeDynamicEntityTextureFingerprint(texturePath);
  activeDynamicEntity_.texturePaths.push_back(texturePath);
}

void RemixRenderer::setFirstPersonHeldItem(int itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setFirstPersonHeldItem");
  MCRTX_TRACY_SCOPE("RemixRenderer::setFirstPersonHeldItem");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  heldItemId_ = heldTorchLightsEnabled_ ? itemId : -1;
}

void RemixRenderer::setEntityHeldTorch(int entityId, float worldX, float worldY, float worldZ, int itemId) {
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

  const bool supportsLightCreation = remix_.CreateLight != nullptr || remix_.CreateLightBatched != nullptr;
  if (!supportsLightCreation || !isTorchLightItemId(itemId)) {
    destroyEntityHeldTorchLight(entityId);
    return;
  }

  entityHeldTorchLightsSeenThisFrame_.insert(entityId);
  MCRTX_TRACY_VALUE(entityHeldTorchLightHandles_.size());

  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      worldX,
      worldY,
      worldZ,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeEntityHeldTorchLightHash(entityId);
  lightInfo.radiance = entityHeldTorchRadiance(itemId);
  lightInfo.isDynamic = TRUE;
  lightInfo.ignoreViewModel = FALSE;
  lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

  const auto createLight = [&](remixapi_LightHandle& lightHandle) {
    if (remix_.CreateLightBatched != nullptr) {
      return [&]() {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLightBatched.entityHeldTorch");
        return remix_.CreateLightBatched(&lightInfo, &lightHandle);
      }();
    }

    return [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.entityHeldTorch");
      return remix_.CreateLight(&lightInfo, &lightHandle);
    }();
  };

  auto lightIt = entityHeldTorchLightHandles_.find(entityId);
  if (lightIt == entityHeldTorchLightHandles_.end() || lightIt->second == nullptr) {
    MCRTX_TRACY_SCOPE("setEntityHeldTorch.createLight");
    remixapi_LightHandle lightHandle = nullptr;
    const remixapi_ErrorCode result = createLight(lightHandle);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateLight failed: " + errorCodeToString(result));
      return;
    }

    entityHeldTorchLightHandles_[entityId] = lightHandle;
    return;
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    MCRTX_TRACY_SCOPE("setEntityHeldTorch.recreateLight");
    destroyEntityHeldTorchLight(entityId);
    remixapi_LightHandle lightHandle = nullptr;
    const remixapi_ErrorCode result = createLight(lightHandle);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateLight failed: " + errorCodeToString(result));
      return;
    }

    entityHeldTorchLightHandles_[entityId] = lightHandle;
    return;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("setEntityHeldTorch.updateLight");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.entityHeldTorch");
    return remix_.UpdateLightDefinition(lightIt->second, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
  }
}

void RemixRenderer::setPlayerShadowsEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setPlayerShadowsEnabled");
  std::scoped_lock lock(mutex_);

  playerShadowsEnabled_ = enabled;
  if (!initialized_) {
    return;
  }

  setConfigVariableLocked(
      "rtx.playerModel.enablePrimaryShadows",
      enabled ? "True" : "False",
      true,
      true);
}

void RemixRenderer::setHeldTorchLightsEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setHeldTorchLightsEnabled");
  std::scoped_lock lock(mutex_);

  heldTorchLightsEnabled_ = enabled;
  if (!initialized_ || enabled) {
    return;
  }

  clearHeldTorchLightsLocked();
}

void RemixRenderer::setDynamicEntityRenderingEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityRenderingEnabled");
  std::scoped_lock lock(mutex_);

  dynamicEntityRenderingEnabled_ = enabled;
  if (enabled) {
    return;
  }

  clearActiveDynamicEntityState(activeDynamicEntity_);
  clearDynamicEntityFrameInstances();
}

void RemixRenderer::setBlockOutlineEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setBlockOutlineEnabled");
  std::scoped_lock lock(mutex_);
  blockOutlineEnabled_ = enabled;
}

void RemixRenderer::setBlockOutlineStyle(int style) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setBlockOutlineStyle");
  std::scoped_lock lock(mutex_);
  if (style < 0 || style > 5) {
    style = 1;
  }
  blockOutlineStyle_ = style;
}

void RemixRenderer::setBlockOutlineEmissiveIntensity(float intensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setBlockOutlineEmissiveIntensity");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(intensity)) {
    intensity = 4.5f;
  }

  if (intensity < 0.0f) {
    intensity = 0.0f;
  } else if (intensity > 10.0f) {
    intensity = 10.0f;
  }

  if (std::abs(blockOutlineEmissiveIntensity_ - intensity) < 0.001f) {
    return;
  }

  blockOutlineEmissiveIntensity_ = intensity;
  if (!initialized_) {
    return;
  }

  destroyBlockOutlineMesh();
  destroyBlockOutlineMaterials();
  createBlockOutlineMaterials();
  if (!blockOutlineInstances_.empty()) {
    rebuildBlockOutlineMesh();
  }
}

void RemixRenderer::setDisplacementFactor(float factor) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDisplacementFactor");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(factor)) {
    factor = 1.0f;
  }

  if (factor < 0.0f) {
    factor = 0.0f;
  } else if (factor > 4.0f) {
    factor = 4.0f;
  }

  if (std::abs(displacementFactor_ - factor) < 0.001f) {
    return;
  }

  displacementFactor_ = factor;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceMeasurementDistance(float distance) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceMeasurementDistance");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(distance)) {
    distance = 1.0f;
  }

  if (distance < 0.0f) {
    distance = 0.0f;
  } else if (distance > 10.0f) {
    distance = 10.0f;
  }

  if (std::abs(subsurfaceMeasurementDistance_ - distance) < 0.001f) {
    return;
  }

  subsurfaceMeasurementDistance_ = distance;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceRadiusScale(float scale) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceRadiusScale");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(scale)) {
    scale = 1.0f;
  }

  if (scale < 0.0f) {
    scale = 0.0f;
  } else if (scale > 10.0f) {
    scale = 10.0f;
  }

  if (std::abs(subsurfaceRadiusScale_ - scale) < 0.001f) {
    return;
  }

  subsurfaceRadiusScale_ = scale;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceMaxSampleRadius(float radius) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceMaxSampleRadius");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(radius)) {
    radius = 16.0f;
  }

  if (radius < 0.0f) {
    radius = 0.0f;
  } else if (radius > 64.0f) {
    radius = 64.0f;
  }

  if (std::abs(subsurfaceMaxSampleRadius_ - radius) < 0.001f) {
    return;
  }

  subsurfaceMaxSampleRadius_ = radius;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceVolumetricAnisotropy(float anisotropy) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceVolumetricAnisotropy");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(anisotropy)) {
    anisotropy = 0.0f;
  }

  if (anisotropy < -1.0f) {
    anisotropy = -1.0f;
  } else if (anisotropy > 1.0f) {
    anisotropy = 1.0f;
  }

  if (std::abs(subsurfaceVolumetricAnisotropy_ - anisotropy) < 0.001f) {
    return;
  }

  subsurfaceVolumetricAnisotropy_ = anisotropy;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::setSubsurfaceDiffusionProfileEnabled(bool enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setSubsurfaceDiffusionProfileEnabled");
  std::scoped_lock lock(mutex_);

  if (subsurfaceDiffusionProfileEnabled_ == enabled) {
    return;
  }

  subsurfaceDiffusionProfileEnabled_ = enabled;
  if (!initialized_) {
    return;
  }

  rebuildMaterialDependentMeshesLocked();
}

void RemixRenderer::rebuildMaterialDependentMeshesLocked() {
  destroyBlockOutlineMesh();

  destroyTerrainMaterials();
  initializeTerrainMaterials();

  for (auto& [chunkKey, meshData] : chunkMeshes_) {
    if (meshData.hasOccupancy) {
      rebuildChunkMeshFromData(chunkKey, meshData, true);
    }
  }

  if (!destroyOverlayInstances_.empty()) {
    rebuildDestroyOverlayMesh();
  }

  if (!blockOutlineInstances_.empty()) {
    rebuildBlockOutlineMesh();
  }

  if (!particleQuads_.empty()) {
    rebuildParticleMesh();
  }

  rebuildFireMesh();
}

void RemixRenderer::setViewModelFovDegrees(float fovYDegrees) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setViewModelFovDegrees");
  std::scoped_lock lock(mutex_);

  if (!std::isfinite(fovYDegrees)) {
    fovYDegrees = 70.0f;
  }

  if (fovYDegrees < 1.0f) {
    fovYDegrees = 1.0f;
  } else if (fovYDegrees > 179.0f) {
    fovYDegrees = 179.0f;
  }

  viewModelFovDegrees_ = fovYDegrees;
}

void RemixRenderer::setRtQuality(int rtQuality) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setRtQuality");
  std::scoped_lock lock(mutex_);

  if ((rtQuality < kRtQualityLow || rtQuality > kRtQualityUltra) && rtQuality != kRtQualityPotato) {
    rtQuality = kRtQualityHigh;
  }

  rtQuality_ = rtQuality;
  if (!initialized_) {
    return;
  }

  applyRtQualityConfigLocked();
}

void RemixRenderer::setUpscalerConfig(int upscalerType, int dlssPreset, int xessPreset, int taauPreset, bool rayReconstructionEnabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setUpscalerConfig");
  std::scoped_lock lock(mutex_);

  if (upscalerType != 0 && upscalerType != 1 && upscalerType != 3 && upscalerType != 4) {
    upscalerType = 1;
  }
  if (dlssPreset < 0 || dlssPreset > 5) {
    dlssPreset = 4;
  }
  if (xessPreset < 0 || xessPreset > 6) {
    xessPreset = 2;
  }
  if (taauPreset < 0 || taauPreset > 4) {
    taauPreset = 2;
  }

  upscalerType_ = upscalerType;
  dlssPreset_ = dlssPreset;
  xessPreset_ = xessPreset;
  taauPreset_ = taauPreset;
  rayReconstructionEnabled_ = rayReconstructionEnabled;
  if (!initialized_) {
    return;
  }

  applyUpscalerConfigLocked();
}

void RemixRenderer::setDynamicEntityBoneTransform(std::uint32_t boneIndex, const remixapi_Transform& transform) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setDynamicEntityBoneTransform");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size()) {
    activeDynamicEntity_.boneTransforms.resize(static_cast<std::size_t>(boneIndex) + 1);
  }
  activeDynamicEntity_.boneTransforms[boneIndex] = transform;
}

void RemixRenderer::captureDynamicEntityQuad(
    float x0,
    float y0,
    float z0,
    float u0,
    float v0,
    float x1,
    float y1,
    float z1,
    float u1,
    float v1,
    float x2,
    float y2,
    float z2,
    float u2,
    float v2,
    float x3,
    float y3,
    float z3,
    float u3,
    float v3,
    std::uint32_t colorRgba,
    bool blendEnabled,
    std::uint32_t boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDynamicEntityQuad");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active || activeDynamicEntity_.currentTextureIndex == 0xFFFFFFFFu) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size() || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  const float vertices[20] = {
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3,
  };
  appendDynamicEntityQuadLocked(activeDynamicEntity_, vertices, colorRgba, blendEnabled, boneIndex);
}

void RemixRenderer::captureDynamicEntityQuadBatch(
    const float* vertices,
    std::uint32_t quadCount,
    std::uint32_t colorRgba,
    bool blendEnabled,
    std::uint32_t boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDynamicEntityQuadBatch");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !dynamicEntityRenderingEnabled_ || !activeDynamicEntity_.active || activeDynamicEntity_.currentTextureIndex == 0xFFFFFFFFu) {
    return;
  }

  if (vertices == nullptr || quadCount == 0) {
    return;
  }

  if (boneIndex >= activeDynamicEntity_.boneTransforms.size() || boneIndex >= REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return;
  }

  for (std::uint32_t quadIndex = 0; quadIndex < quadCount; ++quadIndex) {
    if (!appendDynamicEntityQuadLocked(activeDynamicEntity_, vertices + static_cast<std::size_t>(quadIndex) * 20u, colorRgba, blendEnabled, boneIndex)) {
      break;
    }
  }
}

void RemixRenderer::endDynamicEntity() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::endDynamicEntity");
  MCRTX_TRACY_SCOPE("RemixRenderer::endDynamicEntity");
  std::scoped_lock lock(mutex_);

  if (!dynamicEntityRenderingEnabled_) {
    clearActiveDynamicEntityState(activeDynamicEntity_);
    return;
  }

  if (!activeDynamicEntity_.active) {
    return;
  }

  MCRTX_TRACY_VALUE(activeDynamicEntity_.quadCount);
  if (activeDynamicEntity_.quadCount != 0) {
    const std::uint32_t boneCount = activeDynamicEntity_.maxBoneCount;
    MCRTX_TRACY_VALUE(boneCount);
    if (boneCount != 0 && boneCount <= activeDynamicEntity_.boneTransforms.size()) {
      {
        MCRTX_TRACY_SCOPE("endDynamicEntity.findOrCreateMesh");
      if (DynamicEntityMeshData* meshData = findOrCreateDynamicEntityMesh(activeDynamicEntity_); meshData != nullptr) {
        DynamicEntityFrameInstance* frameInstance = nullptr;
        if (dynamicEntityFrameInstanceCount_ < dynamicEntityFrameInstances_.size()) {
          frameInstance = &dynamicEntityFrameInstances_[dynamicEntityFrameInstanceCount_];
        } else {
          dynamicEntityFrameInstances_.emplace_back();
          frameInstance = &dynamicEntityFrameInstances_.back();
        }
        frameInstance->entityId = activeDynamicEntity_.entityId;
        frameInstance->meshHandle = meshData->meshHandle;
        frameInstance->quadCount = meshData->quadCount;
        activeDynamicEntity_.boneTransforms.resize(boneCount);
        frameInstance->boneTransforms = std::move(activeDynamicEntity_.boneTransforms);
        ++dynamicEntityFrameInstanceCount_;
      }
      }
    }
  }

  clearActiveDynamicEntityState(activeDynamicEntity_);
}

void RemixRenderer::beginDestroyOverlayFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDestroyOverlayFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  destroyOverlayInstances_.clear();
}

void RemixRenderer::beginBlockOutlineFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginBlockOutlineFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  blockOutlineInstances_.clear();
}

void RemixRenderer::captureDestroyOverlay(
    int blockX,
    int blockY,
    int blockZ,
    int blockId,
    int blockMetadata,
    int renderType,
    int destroyStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDestroyOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  DestroyOverlayInstance overlay;
  overlay.blockX = blockX;
  overlay.blockY = blockY;
  overlay.blockZ = blockZ;
  overlay.blockId = blockId;
  overlay.blockMetadata = blockMetadata;
  overlay.renderType = renderType;
  overlay.destroyStage = destroyStage;
  destroyOverlayInstances_.push_back(overlay);
}

void RemixRenderer::captureBlockOutline(int blockX, int blockY, int blockZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureBlockOutline");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !blockOutlineEnabled_) {
    return;
  }

  BlockOutlineInstance outline;
  outline.blockX = blockX;
  outline.blockY = blockY;
  outline.blockZ = blockZ;
  blockOutlineInstances_.push_back(outline);
}

void RemixRenderer::beginParticleFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginParticleFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  particleQuads_.clear();
}

void RemixRenderer::captureParticleQuad(
    float x0,
    float y0,
    float z0,
    float u0,
    float v0,
    float x1,
    float y1,
    float z1,
    float u1,
    float v1,
    float x2,
    float y2,
    float z2,
    float u2,
    float v2,
    float x3,
    float y3,
    float z3,
    float u3,
    float v3,
    std::uint32_t colorRgba,
    std::uint32_t textureKind) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureParticleQuad");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  ParticleQuad quad;
  quad.positions = {
      x0, y0, z0,
      x1, y1, z1,
      x2, y2, z2,
      x3, y3, z3,
  };
  quad.texcoords = {
      u0, v0,
      u1, v1,
      u2, v2,
      u3, v3,
  };
  quad.color = colorRgba;
  quad.textureKind = textureKind;
  particleQuads_.push_back(std::move(quad));
}

void RemixRenderer::clearWorldScene() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearWorldScene");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  for (auto& [chunkKey, meshData] : chunkMeshes_) {
    (void)chunkKey;
    destroyChunkMesh(meshData);
  }
  chunkMeshes_.clear();

  destroyFireMesh();
  destroyDestroyOverlayMesh();
  destroyBlockOutlineMesh();
  destroyParticleMesh();
  destroyDynamicEntityMeshes();
  clearDynamicEntityFrameInstances();
  destroyOverlayInstances_.clear();
  blockOutlineInstances_.clear();
  particleQuads_.clear();
  while (!entityHeldTorchLightHandles_.empty()) {
    destroyEntityHeldTorchLight(entityHeldTorchLightHandles_.begin()->first);
  }
  entityHeldTorchLightsSeenThisFrame_.clear();
  activeDynamicEntity_ = {};
  activeChunkBlocks_.clear();
  activeChunkBuild_ = {};
  chunkBuildActive_ = false;
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  lastSubmittedCloudQuadCount_ = 0;
  lastSubmittedFireQuadCount_ = 0;
  lastSubmittedDynamicEntityQuadCount_ = 0;
  lastSubmittedDestroyOverlayCount_ = 0;
  lastSubmittedBlockOutlineCount_ = 0;
  lastSubmittedParticleQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  lastFireAnimationFrame_ = 0xFFFFFFFFu;
  lastFireChunkBuildCount_ = 0xFFFFFFFFFFFFFFFFull;

  log("Cleared cached world scene state");
}

bool RemixRenderer::createTorchLight(const TorchLightPlacement& placement) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::createTorchLight");
  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      placement.lightX,
      placement.lightY,
      placement.lightZ,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeTorchLightHash(placement.blockPosition);
  lightInfo.radiance = placement.radiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;
  lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

  remixapi_LightHandle lightHandle = nullptr;
  remixapi_ErrorCode result;
  if (remix_.CreateLightBatched != nullptr) {
    result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLightBatched.torch");
      return remix_.CreateLightBatched(&lightInfo, &lightHandle);
    }();
  } else {
    result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.torch");
      return remix_.CreateLight(&lightInfo, &lightHandle);
    }();
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateLight failed: " + errorCodeToString(result));
    return false;
  }

  torchLights_[placement.blockPosition] = lightHandle;
  return true;
}

bool RemixRenderer::updateTorchLight(const TorchLightPlacement& placement) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateTorchLight");
  const auto lightIt = torchLights_.find(placement.blockPosition);
  if (lightIt == torchLights_.end() || lightIt->second == nullptr) {
    return createTorchLight(placement);
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyTorchLight(placement.blockPosition);
    return createTorchLight(placement);
  }

  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {placement.lightX, placement.lightY, placement.lightZ};
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeTorchLightHash(placement.blockPosition);
  lightInfo.radiance = placement.radiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;
  lightInfo.ignoreFirstPersonPlayerShadow = FALSE;

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "UpdateLightDefinition.torch");
    return remix_.UpdateLightDefinition(lightIt->second, &lightInfo);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
    return false;
  }

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
  for (const TorchLightPlacement& placement : desiredTorchLights) {
    const bool existed = torchLights_.find(placement.blockPosition) != torchLights_.end();
    if (!updateTorchLight(placement)) {
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

bool RemixRenderer::reconcileHeldItemTorchLight() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::reconcileHeldItemTorchLight");
  MCRTX_TRACY_SCOPE("RemixRenderer::reconcileHeldItemTorchLight");

  const bool supportsLightCreation = remix_.CreateLight != nullptr || remix_.CreateLightBatched != nullptr;
  const bool isTorch = heldItemId_ == kTorchBlockId;
  const bool isRedstoneTorch = heldItemId_ == kRedstoneTorchOnBlockId;
  if (!supportsLightCreation || (!isTorch && !isRedstoneTorch)) {
    destroyHeldItemTorchLight();
    return true;
  }

  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      camera_.position[0] + camera_.forward[0] * kHeldTorchLightForwardOffset
          + camera_.right[0] * kHeldTorchLightRightOffset
          + camera_.up[0] * kHeldTorchLightUpOffset,
      camera_.position[1] + camera_.forward[1] * kHeldTorchLightForwardOffset
          + camera_.right[1] * kHeldTorchLightRightOffset
          + camera_.up[1] * kHeldTorchLightUpOffset,
      camera_.position[2] + camera_.forward[2] * kHeldTorchLightForwardOffset
          + camera_.right[2] * kHeldTorchLightRightOffset
          + camera_.up[2] * kHeldTorchLightUpOffset,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = kHeldTorchLightHash;
  lightInfo.radiance = isRedstoneTorch ? kRedstoneTorchLightRadiance : kTorchLightRadiance;
  lightInfo.isDynamic = TRUE;
  lightInfo.ignoreViewModel = TRUE;
  lightInfo.ignoreFirstPersonPlayerShadow = TRUE;

  if (heldItemTorchLightHandle_ == nullptr) {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.create");
    remixapi_ErrorCode result;
    if (remix_.CreateLightBatched != nullptr) {
      result = [&]() {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLightBatched.heldTorch");
        return remix_.CreateLightBatched(&lightInfo, &heldItemTorchLightHandle_);
      }();
    } else {
      result = [&]() {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateLight.heldTorch");
        return remix_.CreateLight(&lightInfo, &heldItemTorchLightHandle_);
      }();
    }
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      heldItemTorchLightHandle_ = nullptr;
      setError("CreateLight failed: " + errorCodeToString(result));
      return false;
    }
    return true;
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    MCRTX_TRACY_SCOPE("reconcileHeldItemTorchLight.recreate");
    destroyHeldItemTorchLight();
    return reconcileHeldItemTorchLight();
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

  return true;
}

bool RemixRenderer::rebuildCloudMesh(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
    float colorR,
    float colorG,
    float colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildCloudMesh");
  if (cloudMaterialHandle_ == nullptr) {
    destroyCloudMesh();
    return true;
  }

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(fancy ? 4096 : 2048);
  indices.reserve(fancy ? 6144 : 3072);

  if (fancy) {
    appendFancyCloudGeometry(cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  } else {
    appendFastCloudGeometry(cameraX, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  }

  if (indices.empty()) {
    destroyCloudMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = cloudMaterialHandle_;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = fancy ? 0x4D43525458434C46ull : 0x4D43525458434C30ull;
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  // Cloud geometry is rebuilt from live world-space phase math. Reuse a stable
  // per-mode hash, but destroy the previous cloud mesh first so Remix never has
  // two different live meshes with the same identity at once.
  destroyCloudMesh();

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.cloud");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  cloudMeshHandle_ = newMeshHandle;
  cloudMeshFancy_ = fancy;
  cloudMeshPhaseX_ = 0;
  cloudMeshPhaseZ_ = 0;
  cloudQuadCount_ = indices.size() / 6;
  if (isVerboseLoggingEnabled()) {
    log(std::string("Cloud mesh ready: mode=") + (fancy ? "fancy" : "fast")
        + " quads=" + std::to_string(cloudQuadCount_)
        + " hash=0x" + [&]() {
            std::ostringstream stream;
            stream << std::hex << meshInfo.hash;
            return stream.str();
          }());
  }
  return true;
}

DynamicEntityMeshData* RemixRenderer::findOrCreateDynamicEntityMesh(const DynamicEntityBuildState& buildState) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::findOrCreateDynamicEntityMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::findOrCreateDynamicEntityMesh");
  if (buildState.quadCount == 0) {
    return nullptr;
  }

  MCRTX_TRACY_VALUE(buildState.quadCount);

  const std::uint32_t boneCount = buildState.maxBoneCount;
  if (boneCount == 0
      || boneCount > buildState.boneTransforms.size()
      || boneCount > REMIXAPI_INSTANCE_INFO_MAX_BONES_COUNT) {
    return nullptr;
  }

  const std::uint64_t geometryFingerprint = finalizeDynamicEntityFingerprint(buildState.quadFingerprint, boneCount);
  const std::uint64_t meshKey = makeDynamicEntityMeshKey(buildState.entityId, geometryFingerprint);
  if (const auto existing = dynamicEntityMeshes_.find(meshKey); existing != dynamicEntityMeshes_.end()) {
    return &existing->second;
  }

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(8);
  std::unordered_map<std::uintptr_t, std::size_t> surfaceIndexByHandle;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](remixapi_MaterialHandle materialHandle) -> SurfaceBuildBuffers& {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(materialHandle);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    SurfaceBuildBuffers surface;
    surface.materialHandle = materialHandle;
    surface.vertices.reserve(256);
    surface.indices.reserve(384);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  std::size_t quadCount = 0;
  {
    MCRTX_TRACY_SCOPE("findOrCreateDynamicEntityMesh.buildSurfaces");
    for (std::size_t quadIndex = 0; quadIndex < buildState.quadCount; ++quadIndex) {
      const DynamicEntityQuad& quad = buildState.quads[quadIndex];
      if (quad.textureIndex >= buildState.texturePaths.size()) {
        continue;
      }
      const DynamicEntityMaterialClass materialClass = dynamicEntityMaterialClassForQuad(quad);
      remixapi_MaterialHandle materialHandle = acquireDynamicEntityMaterial(
          buildState.texturePaths[quad.textureIndex],
          materialClass,
        buildState.hurtStage,
        buildState.creeperFuseStage);
      if (materialHandle == nullptr) {
        continue;
      }

      const auto normal = computeQuadNormal(
          quad.positions[0], quad.positions[1], quad.positions[2],
          quad.positions[3], quad.positions[4], quad.positions[5],
          quad.positions[6], quad.positions[7], quad.positions[8]);
      SurfaceBuildBuffers& surface = acquireSurface(materialHandle);
      const std::size_t vertexBase = surface.vertices.size();
      appendCloudQuad(
          quad.positions[0], quad.positions[1], quad.positions[2], quad.texcoords[0], quad.texcoords[1],
          quad.positions[3], quad.positions[4], quad.positions[5], quad.texcoords[2], quad.texcoords[3],
          quad.positions[6], quad.positions[7], quad.positions[8], quad.texcoords[4], quad.texcoords[5],
          quad.positions[9], quad.positions[10], quad.positions[11], quad.texcoords[6], quad.texcoords[7],
          normal[0], normal[1], normal[2],
          quad.color,
          surface.vertices,
          surface.indices);
      const std::size_t addedVertices = surface.vertices.size() - vertexBase;
      surface.blendWeights.insert(surface.blendWeights.end(), addedVertices, 1.0f);
      surface.blendIndices.insert(surface.blendIndices.end(), addedVertices, quad.boneIndex);
      ++quadCount;
    }
  }
  MCRTX_TRACY_VALUE(quadCount);

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  {
    MCRTX_TRACY_SCOPE("findOrCreateDynamicEntityMesh.finalizeSurfaces");
    surfaces.reserve(surfacesToBuild.size());
    for (const SurfaceBuildBuffers& surfaceBuild : surfacesToBuild) {
      if (surfaceBuild.indices.empty()) {
        continue;
      }

      remixapi_MeshInfoSurfaceTriangles surface {};
      surface.vertices_values = surfaceBuild.vertices.data();
      surface.vertices_count = surfaceBuild.vertices.size();
      surface.indices_values = surfaceBuild.indices.data();
      surface.indices_count = surfaceBuild.indices.size();
      surface.skinning_hasvalue = TRUE;
      surface.skinning_value.bonesPerVertex = 1;
      surface.skinning_value.blendWeights_values = surfaceBuild.blendWeights.data();
      surface.skinning_value.blendWeights_count = static_cast<std::uint32_t>(surfaceBuild.blendWeights.size());
      surface.skinning_value.blendIndices_values = surfaceBuild.blendIndices.data();
      surface.skinning_value.blendIndices_count = static_cast<std::uint32_t>(surfaceBuild.blendIndices.size());
      surface.material = surfaceBuild.materialHandle;
      surfaces.push_back(surface);
    }
  }
  MCRTX_TRACY_VALUE(surfaces.size());

  if (surfaces.empty()) {
    return nullptr;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeDynamicEntityMeshHash(meshKey);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle meshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("findOrCreateDynamicEntityMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.entity");
    return remix_.CreateMesh(&meshInfo, &meshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return nullptr;
  }

  DynamicEntityMeshData meshData;
  meshData.meshHandle = meshHandle;
  meshData.meshHash = meshInfo.hash;
  meshData.geometryFingerprint = meshKey;
  meshData.quadCount = quadCount;
  meshData.boneCount = boneCount;
  const auto [it, inserted] = dynamicEntityMeshes_.emplace(meshKey, std::move(meshData));
  if (!inserted) {
    destroyMeshHandle(meshHandle);
  }
  return &it->second;
}

void RemixRenderer::destroyCloudMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyCloudMesh");
  destroyMeshHandle(cloudMeshHandle_);
  cloudMeshFancy_ = false;
  cloudQuadCount_ = 0;
}

void RemixRenderer::destroyFireMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyFireMesh");
  destroyMeshHandle(fireMeshHandle_);
  fireQuadCount_ = 0;
}

void RemixRenderer::destroyDestroyOverlayMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDestroyOverlayMesh");
  destroyMeshHandle(destroyOverlayMeshHandle_);
  destroyOverlayCount_ = 0;
}

void RemixRenderer::destroyBlockOutlineMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyBlockOutlineMesh");
  destroyMeshHandle(blockOutlineMeshHandle_);
  blockOutlineCount_ = 0;
}

void RemixRenderer::destroyParticleMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyParticleMesh");
  destroyMeshHandle(particleMeshHandle_);
  particleQuadCount_ = 0;
}

void RemixRenderer::destroyTorchLight(const WorldBlockPosition& position) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyTorchLight");
  const auto lightIt = torchLights_.find(position);
  if (lightIt == torchLights_.end()) {
    return;
  }

  destroyLightHandle(lightIt->second);
  torchLights_.erase(lightIt);
}

void RemixRenderer::destroyHeldItemTorchLight() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyHeldItemTorchLight");
  if (heldItemTorchLightHandle_ == nullptr) {
    return;
  }

  destroyLightHandle(heldItemTorchLightHandle_);
  heldItemTorchLightHandle_ = nullptr;
}

void RemixRenderer::clearHeldTorchLightsLocked() {
  heldItemId_ = -1;
  destroyHeldItemTorchLight();
  while (!entityHeldTorchLightHandles_.empty()) {
    destroyEntityHeldTorchLight(entityHeldTorchLightHandles_.begin()->first);
  }
  entityHeldTorchLightsSeenThisFrame_.clear();
}

void RemixRenderer::destroyEntityHeldTorchLight(int entityId) {
  const auto lightIt = entityHeldTorchLightHandles_.find(entityId);
  if (lightIt == entityHeldTorchLightHandles_.end()) {
    return;
  }

  if (lightIt->second != nullptr) {
    destroyLightHandle(lightIt->second);
  }
  entityHeldTorchLightHandles_.erase(lightIt);
}

void RemixRenderer::destroyChunkTorchLights(ChunkMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyChunkTorchLights");
  for (const TorchLightPlacement& placement : meshData.torchLights) {
    destroyTorchLight(placement.blockPosition);
  }
  meshData.torchLights.clear();
}

void RemixRenderer::clearDynamicEntityFrameInstances() {
  dynamicEntityFrameInstanceCount_ = 0;
}

void RemixRenderer::destroyDynamicEntityMeshes() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDynamicEntityMeshes");
  clearDynamicEntityFrameInstances();
  for (auto& [geometryFingerprint, meshData] : dynamicEntityMeshes_) {
    (void)geometryFingerprint;
    destroyDynamicEntityMesh(meshData);
  }
  dynamicEntityMeshes_.clear();
  dynamicEntityFrameInstances_.clear();
}

void RemixRenderer::destroyDynamicEntityMesh(DynamicEntityMeshData& meshData) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDynamicEntityMesh");
  destroyMeshHandle(meshData.meshHandle);
  meshData.meshHash = 0;
  meshData.geometryFingerprint = 0;
  meshData.quadCount = 0;
  meshData.boneCount = 0;
}

bool RemixRenderer::rebuildDestroyOverlayMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildDestroyOverlayMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildDestroyOverlayMesh");
  if (destroyOverlayInstances_.empty()) {
    destroyDestroyOverlayMesh();
    return true;
  }
  MCRTX_TRACY_VALUE(destroyOverlayInstances_.size());

  remixapi_MaterialHandle overlayMaterial = destroyOverlayMaterialHandle_;
  if (overlayMaterial == nullptr) {
    overlayMaterial = terrainMaterialHandles_[kCutoutTerrainMaterialClass];
  }
  if (overlayMaterial == nullptr) {
    log("Destroy overlay skipped because the cutout terrain material handle is null");
    destroyDestroyOverlayMesh();
    return true;
  }

  const auto chunkOriginForWorld = [](int coordinate) {
    return coordinate >= 0
        ? (coordinate / kChunkDimension) * kChunkDimension
        : (((coordinate + 1) / kChunkDimension) - 1) * kChunkDimension;
  };

  const auto findWorldCell = [this, &chunkOriginForWorld](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    const int originX = chunkOriginForWorld(worldX);
    const int originY = chunkOriginForWorld(worldY);
    const int originZ = chunkOriginForWorld(worldZ);
    const int localX = worldX - originX;
    const int localY = worldY - originY;
    const int localZ = worldZ - originZ;
    const int cellIndex = blockIndex(localX, localY, localZ);

    for (int renderPass = 0; renderPass <= 1; ++renderPass) {
      const ChunkKey chunkKey {originX, originY, originZ, renderPass};
      const auto it = chunkMeshes_.find(chunkKey);
      if (it == chunkMeshes_.end() || !it->second.hasOccupancy || it->second.occupancy[cellIndex] == 0) {
        continue;
      }
      return &it->second.cells[cellIndex];
    }

    return nullptr;
  };

  const auto hasFenceNeighbor = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr
        && neighborCell->blockId == kFenceBlockId
        && neighborCell->renderType == kFenceBlockRenderType;
  };

  const auto hasSolidSupport = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isSolidSupportBlock(*neighborCell);
  };

  const auto hasRedstoneConnection = [&findWorldCell](int worldX, int worldY, int worldZ, int direction) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isRedstoneConnectionCell(*neighborCell, direction);
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(destroyOverlayInstances_.size() * 24);
  indices.reserve(destroyOverlayInstances_.size() * 36);

  std::size_t overlayCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildDestroyOverlayMesh.buildGeometry");
  for (const DestroyOverlayInstance& overlay : destroyOverlayInstances_) {
    const int destroyTile = 240 + std::clamp(overlay.destroyStage, 0, 9);
    ChunkBlockCell resolvedCell {};
    if (const ChunkBlockCell* worldCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ); worldCell != nullptr) {
      resolvedCell = *worldCell;
    } else {
      resolvedCell.blockId = static_cast<std::uint8_t>(overlay.blockId);
      resolvedCell.blockMetadata = static_cast<std::uint8_t>(overlay.blockMetadata);
      resolvedCell.renderType = static_cast<std::uint8_t>(overlay.renderType);
    }
    resolvedCell.terrainTiles.fill(static_cast<std::int16_t>(destroyTile));

    const float localX = static_cast<float>(overlay.blockX);
    const float localY = static_cast<float>(overlay.blockY);
    const float localZ = static_cast<float>(overlay.blockZ);

    if (resolvedCell.renderType == kLiquidBlockRenderType) {
      continue;
    }

    if (isCrossedQuadRenderType(resolvedCell.renderType)) {
      appendCrossedQuadGeometry(
          resolvedCell,
          overlay.blockX,
          overlay.blockY,
          overlay.blockZ,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isTorchRenderType(resolvedCell.renderType)) {
      appendTorchGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isLadderRenderType(resolvedCell.renderType)) {
      appendLadderGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

        if (isRedstoneDustRenderType(resolvedCell.renderType) && isRedstoneDustBlockId(resolvedCell.blockId)) {
          const bool blockedAbove = hasSolidSupport(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
          bool connectWest = hasRedstoneConnection(overlay.blockX - 1, overlay.blockY, overlay.blockZ, 1)
            || (!hasSolidSupport(overlay.blockX - 1, overlay.blockY, overlay.blockZ)
              && hasRedstoneConnection(overlay.blockX - 1, overlay.blockY - 1, overlay.blockZ, -1));
          bool connectEast = hasRedstoneConnection(overlay.blockX + 1, overlay.blockY, overlay.blockZ, 3)
            || (!hasSolidSupport(overlay.blockX + 1, overlay.blockY, overlay.blockZ)
              && hasRedstoneConnection(overlay.blockX + 1, overlay.blockY - 1, overlay.blockZ, -1));
          bool connectNorth = hasRedstoneConnection(overlay.blockX, overlay.blockY, overlay.blockZ - 1, 2)
            || (!hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ - 1)
              && hasRedstoneConnection(overlay.blockX, overlay.blockY - 1, overlay.blockZ - 1, -1));
          bool connectSouth = hasRedstoneConnection(overlay.blockX, overlay.blockY, overlay.blockZ + 1, 0)
            || (!hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ + 1)
              && hasRedstoneConnection(overlay.blockX, overlay.blockY - 1, overlay.blockZ + 1, -1));

          const bool climbWest = !blockedAbove && hasSolidSupport(overlay.blockX - 1, overlay.blockY, overlay.blockZ)
            && hasRedstoneConnection(overlay.blockX - 1, overlay.blockY + 1, overlay.blockZ, -1);
          const bool climbEast = !blockedAbove && hasSolidSupport(overlay.blockX + 1, overlay.blockY, overlay.blockZ)
            && hasRedstoneConnection(overlay.blockX + 1, overlay.blockY + 1, overlay.blockZ, -1);
          const bool climbNorth = !blockedAbove && hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ - 1)
            && hasRedstoneConnection(overlay.blockX, overlay.blockY + 1, overlay.blockZ - 1, -1);
          const bool climbSouth = !blockedAbove && hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ + 1)
            && hasRedstoneConnection(overlay.blockX, overlay.blockY + 1, overlay.blockZ + 1, -1);

          connectWest = connectWest || climbWest;
          connectEast = connectEast || climbEast;
          connectNorth = connectNorth || climbNorth;
          connectSouth = connectSouth || climbSouth;

          appendRedstoneDustGeometry(
            resolvedCell,
            connectWest,
            connectEast,
            connectNorth,
            connectSouth,
            climbWest,
            climbEast,
            climbNorth,
            climbSouth,
            localX,
            localY,
            localZ,
            vertices,
            indices);
          ++overlayCount;
          continue;
        }

    if (isRailRenderType(resolvedCell.renderType) && isRailBlockId(resolvedCell.blockId)) {
      appendRailGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isStairRenderType(resolvedCell.renderType) && isStairBlockId(resolvedCell.blockId)) {
      appendStairGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isLeverOrButtonRenderType(resolvedCell.renderType)) {
      if (isLeverBlockId(resolvedCell.blockId)) {
        appendLeverGeometry(
            resolvedCell,
            localX,
            localY,
            localZ,
            vertices,
            indices);
      } else if (isButtonBlockId(resolvedCell.blockId)) {
        appendBoxGeometry(
            localX + resolvedCell.bounds[0],
            localY + resolvedCell.bounds[1],
            localZ + resolvedCell.bounds[2],
            localX + resolvedCell.bounds[3],
            localY + resolvedCell.bounds[4],
            localZ + resolvedCell.bounds[5],
            resolvedCell.terrainTiles,
            kDefaultVertexColor,
            vertices,
            indices);
      }
      ++overlayCount;
      continue;
    }

    if (isDoorRenderType(resolvedCell.renderType) && isDoorBlockId(resolvedCell.blockId)) {
      const ChunkBlockCell* pairedDoorCell = (resolvedCell.blockMetadata & 8) != 0
          ? findWorldCell(overlay.blockX, overlay.blockY - 1, overlay.blockZ)
          : findWorldCell(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
      int resolvedDoorMetadata = resolvedCell.blockMetadata & 0xF;
      if ((resolvedDoorMetadata & 8) != 0) {
        if (pairedDoorCell != nullptr && pairedDoorCell->blockId == resolvedCell.blockId) {
          resolvedDoorMetadata = pairedDoorCell->blockMetadata & 0xF;
        } else {
          resolvedDoorMetadata &= 7;
        }
      } else if (pairedDoorCell != nullptr
          && pairedDoorCell->blockId == resolvedCell.blockId
          && (pairedDoorCell->blockMetadata & 4) != 0) {
        resolvedDoorMetadata = (resolvedDoorMetadata & 3) | 4;
      }

      appendDoorGeometry(
          resolvedCell,
          resolvedDoorMetadata,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isFenceRenderType(resolvedCell.renderType) && resolvedCell.blockId == kFenceBlockId) {
      appendFenceGeometry(
          hasFenceNeighbor(overlay.blockX - 1, overlay.blockY, overlay.blockZ),
          hasFenceNeighbor(overlay.blockX + 1, overlay.blockY, overlay.blockZ),
          hasFenceNeighbor(overlay.blockX, overlay.blockY, overlay.blockZ - 1),
          hasFenceNeighbor(overlay.blockX, overlay.blockY, overlay.blockZ + 1),
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isCactusRenderType(resolvedCell.renderType) && isCactusBlockId(resolvedCell.blockId)) {
      const ChunkBlockCell* belowCell = findWorldCell(overlay.blockX, overlay.blockY - 1, overlay.blockZ);
      const ChunkBlockCell* aboveCell = findWorldCell(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
      const ChunkBlockCell* northCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ - 1);
      const ChunkBlockCell* southCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ + 1);
      const ChunkBlockCell* westCell = findWorldCell(overlay.blockX - 1, overlay.blockY, overlay.blockZ);
      const ChunkBlockCell* eastCell = findWorldCell(overlay.blockX + 1, overlay.blockY, overlay.blockZ);

      appendCactusGeometry(
          belowCell == nullptr || !isSolidSupportBlock(*belowCell),
          aboveCell == nullptr || !isSolidSupportBlock(*aboveCell),
          northCell == nullptr || !isSolidSupportBlock(*northCell),
          southCell == nullptr || !isSolidSupportBlock(*southCell),
          westCell == nullptr || !isSolidSupportBlock(*westCell),
          eastCell == nullptr || !isSolidSupportBlock(*eastCell),
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isBedRenderType(resolvedCell.renderType) && isBedBlockId(resolvedCell.blockId)) {
      appendBedGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (resolvedCell.renderType == kCubeBlockRenderType && usesPartialCubeBounds(resolvedCell)) {
      appendBoxGeometry(
          localX + resolvedCell.bounds[0],
          localY + resolvedCell.bounds[1],
          localZ + resolvedCell.bounds[2],
          localX + resolvedCell.bounds[3],
          localY + resolvedCell.bounds[4],
          localZ + resolvedCell.bounds[5],
          resolvedCell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    bool emittedFace = false;
    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      const ChunkBlockCell* neighborCell = findWorldCell(
          overlay.blockX + kNeighborOffsets[faceIndex][0],
          overlay.blockY + kNeighborOffsets[faceIndex][1],
          overlay.blockZ + kNeighborOffsets[faceIndex][2]);
      if (neighborCell != nullptr && shouldCullFaceAgainstNeighbor(resolvedCell, *neighborCell)) {
        continue;
      }

      const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
      appendFaceGeometry(
          faceIndex,
          localX,
          localY,
          localZ,
          resolvedCell.terrainTiles[minecraftSide],
          kDefaultVertexColor,
          kFaceOverlayBias,
          vertices,
          indices);
      emittedFace = true;
    }

    if (emittedFace) {
      ++overlayCount;
    }
  }
  }

  if (indices.empty()) {
    destroyDestroyOverlayMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = overlayMaterial;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeDestroyOverlayMeshHash(nextDestroyOverlayMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildDestroyOverlayMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.destroy");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyDestroyOverlayMesh();
  destroyOverlayMeshHandle_ = newMeshHandle;
  destroyOverlayCount_ = overlayCount;
  if (isVerboseLoggingEnabled()) {
    log(
        std::string("Rebuilt destroy overlay mesh overlays=") + std::to_string(overlayCount)
        + " vertices=" + std::to_string(vertices.size())
        + " indices=" + std::to_string(indices.size()));
  }
  return true;
}

bool RemixRenderer::rebuildBlockOutlineMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildBlockOutlineMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildBlockOutlineMesh");
  if (!blockOutlineEnabled_ || blockOutlineInstances_.empty()) {
    destroyBlockOutlineMesh();
    return true;
  }
  MCRTX_TRACY_VALUE(blockOutlineInstances_.size());

  const BlockOutlineStyleParameters styleParameters = blockOutlineStyleParametersFor(blockOutlineStyle_);
  const BlockOutlineAnimatedColor animatedRgbColor = styleParameters.rgbCycle
      ? currentBlockOutlineRgbColor()
      : BlockOutlineAnimatedColor {
            styleParameters.colorR,
            styleParameters.colorG,
            styleParameters.colorB,
            0,
        };
  remixapi_MaterialHandle outlineMaterial = destroyOverlayMaterialHandle_;
  if (styleParameters.rgbCycle) {
    outlineMaterial = blockOutlineRgbMaterialHandles_[animatedRgbColor.materialIndex];
    if (outlineMaterial == nullptr) {
      outlineMaterial = blockOutlineGlowMaterialHandle_;
    }
  } else if (styleParameters.glow) {
    outlineMaterial = blockOutlineGlowMaterialHandle_;
  }
  if (outlineMaterial == nullptr) {
    outlineMaterial = destroyOverlayMaterialHandle_;
  }

  if (outlineMaterial == nullptr) {
    destroyBlockOutlineMesh();
    return true;
  }

  const auto chunkOriginForWorld = [](int coordinate) {
    return coordinate >= 0
        ? (coordinate / kChunkDimension) * kChunkDimension
        : (((coordinate + 1) / kChunkDimension) - 1) * kChunkDimension;
  };

  const auto findWorldCell = [this, &chunkOriginForWorld](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    const int originX = chunkOriginForWorld(worldX);
    const int originY = chunkOriginForWorld(worldY);
    const int originZ = chunkOriginForWorld(worldZ);
    const int localX = worldX - originX;
    const int localY = worldY - originY;
    const int localZ = worldZ - originZ;
    const int cellIndex = blockIndex(localX, localY, localZ);

    for (int renderPass = 0; renderPass <= 1; ++renderPass) {
      const ChunkKey chunkKey {originX, originY, originZ, renderPass};
      const auto it = chunkMeshes_.find(chunkKey);
      if (it == chunkMeshes_.end() || !it->second.hasOccupancy || it->second.occupancy[cellIndex] == 0) {
        continue;
      }
      return &it->second.cells[cellIndex];
    }

    return nullptr;
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(blockOutlineInstances_.size() * 288);
  indices.reserve(blockOutlineInstances_.size() * 432);

  std::unordered_set<WorldBlockPosition, WorldBlockPositionHash> seenOutlines;
  seenOutlines.reserve(blockOutlineInstances_.size());
  const std::uint32_t outlineColor = packVertexColorRgba(
      animatedRgbColor.colorR,
      animatedRgbColor.colorG,
      animatedRgbColor.colorB,
      styleParameters.alpha);

  std::size_t outlineCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildBlockOutlineMesh.buildGeometry");
  for (const BlockOutlineInstance& outline : blockOutlineInstances_) {
    const WorldBlockPosition position {outline.blockX, outline.blockY, outline.blockZ};
    if (!seenOutlines.insert(position).second) {
      continue;
    }

    float minX = static_cast<float>(outline.blockX) - styleParameters.inflate;
    float minY = static_cast<float>(outline.blockY) - styleParameters.inflate;
    float minZ = static_cast<float>(outline.blockZ) - styleParameters.inflate;
    float maxX = static_cast<float>(outline.blockX) + 1.0f + styleParameters.inflate;
    float maxY = static_cast<float>(outline.blockY) + 1.0f + styleParameters.inflate;
    float maxZ = static_cast<float>(outline.blockZ) + 1.0f + styleParameters.inflate;

    if (const ChunkBlockCell* worldCell = findWorldCell(outline.blockX, outline.blockY, outline.blockZ); worldCell != nullptr) {
      minX = static_cast<float>(outline.blockX) + worldCell->bounds[0] - styleParameters.inflate;
      minY = static_cast<float>(outline.blockY) + worldCell->bounds[1] - styleParameters.inflate;
      minZ = static_cast<float>(outline.blockZ) + worldCell->bounds[2] - styleParameters.inflate;
      maxX = static_cast<float>(outline.blockX) + worldCell->bounds[3] + styleParameters.inflate;
      maxY = static_cast<float>(outline.blockY) + worldCell->bounds[4] + styleParameters.inflate;
      maxZ = static_cast<float>(outline.blockZ) + worldCell->bounds[5] + styleParameters.inflate;
    }

    if (styleParameters.filled) {
      appendBlockOutlineFillGeometry(minX, minY, minZ, maxX, maxY, maxZ, outlineColor, vertices, indices);
    } else {
      appendBlockOutlineWireGeometry(
          minX,
          minY,
          minZ,
          maxX,
          maxY,
          maxZ,
          styleParameters.edgeHalfThickness,
          outlineColor,
          vertices,
          indices);
    }
    ++outlineCount;
  }
  }

  if (indices.empty()) {
    destroyBlockOutlineMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = outlineMaterial;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeBlockOutlineMeshHash(nextBlockOutlineMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildBlockOutlineMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.blockOutline");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    destroyBlockOutlineMesh();
    return false;
  }

  destroyBlockOutlineMesh();
  blockOutlineMeshHandle_ = newMeshHandle;
  blockOutlineCount_ = outlineCount;
  return true;
}

bool RemixRenderer::rebuildFireMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildFireMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildFireMesh");
  if (fireMaterialHandle_ == nullptr) {    destroyFireMesh();
    return true;
  }

  const std::uint32_t frameIndex = static_cast<std::uint32_t>((GetTickCount64() / kFireAnimationFrameIntervalMilliseconds) % kFireAnimationFrameCount);
  if (fireMeshHandle_ != nullptr
      && lastFireAnimationFrame_ == frameIndex
      && lastFireChunkBuildCount_ == capturedChunkBuilds_) {
    return true;
  }

  const auto chunkOriginForWorld = [](int coordinate) {
    return coordinate >= 0
        ? (coordinate / kChunkDimension) * kChunkDimension
        : (((coordinate + 1) / kChunkDimension) - 1) * kChunkDimension;
  };

  const auto findWorldCell = [this, &chunkOriginForWorld](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    const int originX = chunkOriginForWorld(worldX);
    const int originY = chunkOriginForWorld(worldY);
    const int originZ = chunkOriginForWorld(worldZ);
    const int localX = worldX - originX;
    const int localY = worldY - originY;
    const int localZ = worldZ - originZ;
    const int cellIndex = blockIndex(localX, localY, localZ);

    for (int renderPass = 0; renderPass <= 1; ++renderPass) {
      const ChunkKey chunkKey {originX, originY, originZ, renderPass};
      const auto it = chunkMeshes_.find(chunkKey);
      if (it == chunkMeshes_.end() || !it->second.hasOccupancy || it->second.occupancy[cellIndex] == 0) {
        continue;
      }
      return &it->second.cells[cellIndex];
    }

    return nullptr;
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(256);
  indices.reserve(384);

  std::size_t fireCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildFireMesh.buildGeometry");
  for (const auto& [chunkKey, meshData] : chunkMeshes_) {
    if (chunkKey.renderPass != 0 || !meshData.hasOccupancy || meshData.fireCellIndices.empty()) {
      continue;
    }

    for (std::uint16_t fireCellIndex : meshData.fireCellIndices) {
      const ChunkBlockCell& cell = meshData.cells[fireCellIndex];
      if (!isFireRenderType(cell.renderType)) {
        continue;
      }

      const int localY = fireCellIndex / (kChunkDimension * kChunkDimension);
      const int localPlaneIndex = fireCellIndex % (kChunkDimension * kChunkDimension);
      const int localZ = localPlaneIndex / kChunkDimension;
      const int localX = localPlaneIndex % kChunkDimension;
      const int worldX = chunkKey.originX + localX;
      const int worldY = chunkKey.originY + localY;
      const int worldZ = chunkKey.originZ + localZ;
      appendFireGeometry(
          worldX,
          worldY,
          worldZ,
          findWorldCell(worldX, worldY - 1, worldZ) != nullptr,
          findWorldCell(worldX - 1, worldY, worldZ) != nullptr,
          findWorldCell(worldX + 1, worldY, worldZ) != nullptr,
          findWorldCell(worldX, worldY, worldZ - 1) != nullptr,
          findWorldCell(worldX, worldY, worldZ + 1) != nullptr,
          findWorldCell(worldX, worldY + 1, worldZ) != nullptr,
          static_cast<float>(worldX),
          static_cast<float>(worldY),
          static_cast<float>(worldZ),
          frameIndex,
          vertices,
          indices);
      ++fireCount;
    }
  }
  }
  MCRTX_TRACY_VALUE(fireCount);

  if (indices.empty()) {
    destroyFireMesh();
    lastFireAnimationFrame_ = frameIndex;
    lastFireChunkBuildCount_ = capturedChunkBuilds_;
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = fireMaterialHandle_;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeFireMeshHash(nextFireMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildFireMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.fire");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyFireMesh();
  fireMeshHandle_ = newMeshHandle;
  fireQuadCount_ = fireCount;
  lastFireAnimationFrame_ = frameIndex;
  lastFireChunkBuildCount_ = capturedChunkBuilds_;
  return true;
}

bool RemixRenderer::rebuildParticleMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildParticleMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildParticleMesh");
  if (particleQuads_.empty()) {
    destroyParticleMesh();
    return true;
  }
  MCRTX_TRACY_VALUE(particleQuads_.size());

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(4);
  std::unordered_map<std::uintptr_t, std::size_t> surfaceIndexByHandle;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](remixapi_MaterialHandle materialHandle) -> SurfaceBuildBuffers& {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(materialHandle);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    SurfaceBuildBuffers surface;
    surface.materialHandle = materialHandle;
    surface.vertices.reserve(256);
    surface.indices.reserve(384);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  std::size_t quadCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildParticleMesh.buildSurfaces");
    for (const ParticleQuad& quad : particleQuads_) {
      remixapi_MaterialHandle materialHandle = acquireParticleMaterial(quad.textureKind);
      if (materialHandle == nullptr) {
        continue;
      }

      const auto normal = computeQuadNormal(
          quad.positions[0], quad.positions[1], quad.positions[2],
          quad.positions[3], quad.positions[4], quad.positions[5],
          quad.positions[6], quad.positions[7], quad.positions[8]);
      SurfaceBuildBuffers& surface = acquireSurface(materialHandle);
      appendCloudQuad(
          quad.positions[0], quad.positions[1], quad.positions[2], quad.texcoords[0], quad.texcoords[1],
          quad.positions[3], quad.positions[4], quad.positions[5], quad.texcoords[2], quad.texcoords[3],
          quad.positions[6], quad.positions[7], quad.positions[8], quad.texcoords[4], quad.texcoords[5],
          quad.positions[9], quad.positions[10], quad.positions[11], quad.texcoords[6], quad.texcoords[7],
          normal[0], normal[1], normal[2],
          quad.color,
          surface.vertices,
          surface.indices);
      ++quadCount;
    }
  }
  MCRTX_TRACY_VALUE(quadCount);

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  {
    MCRTX_TRACY_SCOPE("rebuildParticleMesh.finalizeSurfaces");
    surfaces.reserve(surfacesToBuild.size());
    for (const SurfaceBuildBuffers& surfaceBuild : surfacesToBuild) {
      if (surfaceBuild.indices.empty()) {
        continue;
      }

      remixapi_MeshInfoSurfaceTriangles surface {};
      surface.vertices_values = surfaceBuild.vertices.data();
      surface.vertices_count = surfaceBuild.vertices.size();
      surface.indices_values = surfaceBuild.indices.data();
      surface.indices_count = surfaceBuild.indices.size();
      surface.skinning_hasvalue = FALSE;
      surface.material = surfaceBuild.materialHandle;
      surfaces.push_back(surface);
    }
  }
  MCRTX_TRACY_VALUE(surfaces.size());

  if (surfaces.empty()) {
    destroyParticleMesh();
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeParticleMeshHash(nextParticleMeshHash_++);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.particle");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyParticleMesh();
  particleMeshHandle_ = newMeshHandle;
  particleQuadCount_ = quadCount;
  return true;
}


}  // namespace mcrtx