#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"

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
std::size_t ChunkKeyHash::operator()(const ChunkKey& key) const noexcept {
  std::size_t hash = 0;
  hash ^= static_cast<std::size_t>(key.originX) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  hash ^= static_cast<std::size_t>(key.originY) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  hash ^= static_cast<std::size_t>(key.originZ) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  hash ^= static_cast<std::size_t>(key.renderPass) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  return hash;
}

std::size_t WorldBlockPositionHash::operator()(const WorldBlockPosition& position) const noexcept {
  std::size_t hash = 0;
  hash ^= static_cast<std::size_t>(position.x) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  hash ^= static_cast<std::size_t>(position.y) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  hash ^= static_cast<std::size_t>(position.z) + 0x9e3779b9u + (hash << 6) + (hash >> 2);
  return hash;
}

RemixRenderer& RemixRenderer::instance() {
  static RemixRenderer renderer;
  return renderer;
}

bool RemixRenderer::initialize(
  HWND hwnd,
  std::uint32_t width,
  std::uint32_t height,
  std::filesystem::path remixDllPath) {
  std::scoped_lock lock(mutex_);

  if (initialized_) {
    return true;
  }

  if (hwnd == nullptr) {
    setError("initialize called with null HWND");
    return false;
  }

  sourceHwnd_ = hwnd;
  width_ = width == 0 ? 1 : width;
  height_ = height == 0 ? 1 : height;
  camera_.aspect = static_cast<float>(width_) / static_cast<float>(height_);

  bool usedLegacySourceWindowEnvVar = false;
  overlayOutputWindow_ = shouldUseOverlayOutputWindow(&usedLegacySourceWindowEnvVar);
  if (usedLegacySourceWindowEnvVar) {
    log("MCRTX_USE_SOURCE_WINDOW is deprecated; using detached dual-window mode instead");
  }

  if (!createOutputWindow(sourceHwnd_)) {
    return false;
  }
  HWND presentationHwnd = outputHwnd_;

  if (remixDllPath.empty()) {
    remixDllPath = resolveRemixDllPath();
  }

  if (!loadRemix(remixDllPath)) {
    destroyOutputWindow();
    return false;
  }

  if (remix_.DrawScreenOverlay == nullptr) {
    setError("Loaded Remix runtime does not support DrawScreenOverlay; update dxvk-remix-gmod/d3d9.dll to a build with screen overlay support");
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

  // Minecraft submits world positions in blocks where 1 block = 1 meter.
  // Remix defaults assume 1 world unit = 1 cm (sceneScale = 1). Without this
  // override, every meters-aware subsystem (volumetrics froxel extent,
  // transmittance measurement distance, light volumetric radius falloff)
  // is off by 100x and produces no visible contribution.
  if (remix_.SetConfigVariable != nullptr) {
    const auto applyConfig = [this](const char* key, const char* value) {
      const remixapi_ErrorCode result = remix_.SetConfigVariable(key, value);
      log(std::string("SetConfigVariable ") + key + "=" + value + " -> " + errorCodeToString(result));
    };
    // force these settings otherwise it's boil city
    applyConfig("rtx.sceneScale", "0.01");
    applyConfig("rtx.volumetrics.initialRISSampleCount", "32");
  } else {
    log("SetConfigVariable not available; cannot apply scene scale / volumetric tuning");
  }

  if (!startup(presentationHwnd)) {
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

  // DLSS-RR's startup preset (updatePathTracerPreset(RayReconstruction) inside
  // Remix's initializer) forcibly overwrites these DI options via setDeferred,
  // clobbering anything we set before Startup. Re-apply after Startup so the
  // pending values land on top of the preset.
  if (remix_.SetConfigVariable != nullptr) {
    const auto applyConfig = [this](const char* key, const char* value) {
      const remixapi_ErrorCode result = remix_.SetConfigVariable(key, value);
      log(std::string("SetConfigVariable ") + key + "=" + value + " -> " + errorCodeToString(result));
    };
    applyConfig("rtx.di.initialSampleCount", "32");
    applyConfig("rtx.di.enableBestLightSampling", "True");
    applyConfig("rtx.di.enableDenoiserConfidence", "True");
    applyConfig("rtx.di.enableDenoiserGradient", "True");
  }

  initializeTerrainMaterials();

  initialized_ = true;
  log(overlayOutputWindow_
      ? "Remix renderer initialized in single-window overlay mode"
      : "Remix renderer initialized in dual-window mode");
  return true;
}

void RemixRenderer::shutdown() {
  std::scoped_lock lock(mutex_);

  if (initialized_ && remix_.Shutdown) {
    for (auto& [chunkKey, meshData] : chunkMeshes_) {
      destroyChunkMesh(meshData);
    }
    destroyCloudMesh();
    destroyFireMesh();
    destroyTerrainMaterials();
    remix_.Shutdown();
  }
  resetLoadedRemix();
  destroyOutputWindow();
  initialized_ = false;
  overlayOutputWindow_ = true;
  sourceHwnd_ = nullptr;
  chunkBuildActive_ = false;
  activeChunkBuild_ = {};
  activeChunkBlocks_.clear();
  chunkMeshes_.clear();
  dynamicEntityMeshes_.clear();
  dynamicEntityFrameInstances_.clear();
  destroyOverlayInstances_.clear();
  particleQuads_.clear();
  dynamicEntityMaterialHandles_.clear();
  activeDynamicEntity_ = {};
  torchLights_.clear();
  nextChunkMeshHash_ = 1;
  presentedFrames_ = 0;
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  lastSubmittedCloudQuadCount_ = 0;
  lastSubmittedFireQuadCount_ = 0;
  lastSubmittedDynamicEntityQuadCount_ = 0;
  lastSubmittedDestroyOverlayCount_ = 0;
  lastSubmittedParticleQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  terrainAtlasPath_.clear();
  cloudTexturePath_.clear();
  fireTexturePath_.clear();
  nextCloudMeshHash_ = 1;
  nextFireMeshHash_ = 1;
  nextDestroyOverlayMeshHash_ = 1;
  nextParticleMeshHash_ = 1;
  cloudQuadCount_ = 0;
  fireQuadCount_ = 0;
  destroyOverlayCount_ = 0;
  particleQuadCount_ = 0;
  lastFireAnimationFrame_ = 0xFFFFFFFFu;
  lastFireChunkBuildCount_ = 0xFFFFFFFFFFFFFFFFull;
  lastError_.clear();
}

void RemixRenderer::resize(std::uint32_t width, std::uint32_t height) {
  std::scoped_lock lock(mutex_);
  width_ = width == 0 ? 1 : width;
  height_ = height == 0 ? 1 : height;
  camera_.aspect = static_cast<float>(width_) / static_cast<float>(height_);
  updateOutputWindowSize();
}

bool RemixRenderer::drawScreenOverlay(
    const void* pixelData,
    std::uint32_t width,
    std::uint32_t height,
    remixapi_Format format,
    float opacity) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("drawScreenOverlay called before initialize");
    return false;
  }

  if (remix_.DrawScreenOverlay == nullptr) {
    setError("DrawScreenOverlay is unavailable in the loaded Remix runtime");
    return false;
  }

  if (pixelData == nullptr || width == 0 || height == 0) {
    setError("DrawScreenOverlay requires non-null pixel data and non-zero dimensions");
    return false;
  }

  if (format != REMIXAPI_FORMAT_R8G8B8A8_UNORM
      && format != REMIXAPI_FORMAT_B8G8R8A8_UNORM) {
    setError("DrawScreenOverlay only supports RGBA8 and BGRA8 overlay buffers");
    return false;
  }

  const remixapi_ErrorCode result = remix_.DrawScreenOverlay(
      pixelData,
      width,
      height,
      format,
      std::clamp(opacity, 0.0f, 1.0f));
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("DrawScreenOverlay failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::clearScreenOverlay() {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return true;
  }

  if (remix_.DrawScreenOverlay == nullptr) {
    setError("DrawScreenOverlay is unavailable in the loaded Remix runtime");
    return false;
  }

  const remixapi_ErrorCode result = remix_.DrawScreenOverlay(
      nullptr,
      0,
      0,
      REMIXAPI_FORMAT_R8G8B8A8_UNORM,
      0.0f);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("DrawScreenOverlay clear failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

remixapi_UIState RemixRenderer::getUiState() const {
  std::scoped_lock lock(mutex_);

  if (!initialized_ || remix_.GetUIState == nullptr) {
    return REMIXAPI_UI_STATE_NONE;
  }

  return remix_.GetUIState();
}

bool RemixRenderer::setUiState(remixapi_UIState state) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("setUiState called before initialize");
    return false;
  }

  if (remix_.SetUIState == nullptr) {
    setError("SetUIState is unavailable in the loaded Remix runtime");
    return false;
  }

  if (state != REMIXAPI_UI_STATE_NONE
      && state != REMIXAPI_UI_STATE_BASIC
      && state != REMIXAPI_UI_STATE_ADVANCED) {
    setError("setUiState received an unsupported Remix UI state");
    return false;
  }

  const remixapi_ErrorCode result = remix_.SetUIState(state);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetUIState failed: " + errorCodeToString(result));
    return false;
  }

  syncOutputWindowInteractivity(state);

  return true;
}

void RemixRenderer::updateCamera(const CameraState& camera) {
  std::scoped_lock lock(mutex_);
  camera_ = camera;
}

bool RemixRenderer::present() {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("present called before initialize");
    return false;
  }

  updateOutputWindowSize();
  pumpOutputWindowMessages();

  if (!submitCamera()) {
    return false;
  }

  if (!drawCapturedGeometry()) {
    return false;
  }

  const remixapi_ErrorCode result = remix_.Present(nullptr);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Present failed: " + errorCodeToString(result));
    return false;
  }

  if (remix_.GetUIState != nullptr) {
    syncOutputWindowInteractivity(remix_.GetUIState());
  }

  return true;
}

bool RemixRenderer::isInitialized() const {
  std::scoped_lock lock(mutex_);
  return initialized_;
}

std::string RemixRenderer::lastError() const {
  std::scoped_lock lock(mutex_);
  return lastError_;
}

void RemixRenderer::resetLoadedRemix() {
  if (remixDll_ != nullptr) {
    remixapi_lib_shutdownAndUnloadRemixDll(&remix_, remixDll_);
  }

  remix_ = {};
  remixDll_ = nullptr;
}

bool RemixRenderer::loadRemix(const std::filesystem::path& remixDllPath) {
  log("Loading Remix runtime from " + remixDllPath.string());
  const remixapi_ErrorCode result = remixapi_lib_loadRemixDllAndInitialize(remixDllPath.c_str(), &remix_, &remixDll_);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Failed to load Remix DLL: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::startup(HWND hwnd) {
  remixapi_StartupInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_STARTUP_INFO;
  info.hwnd = hwnd;
  info.disableSrgbConversionForOutput = FALSE;
  info.forceNoVkSwapchain = FALSE;
  info.editorModeEnabled = FALSE;

  const remixapi_ErrorCode result = remix_.Startup(&info);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Startup failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::drawCapturedGeometry() {
  if (!rebuildFireMesh()) {
    return false;
  }

  if (!rebuildDestroyOverlayMesh()) {
    return false;
  }

  if (!rebuildParticleMesh()) {
    return false;
  }

  if (chunkMeshes_.empty()
      && cloudMeshHandle_ == nullptr
      && fireMeshHandle_ == nullptr
      && dynamicEntityFrameInstances_.empty()
      && destroyOverlayMeshHandle_ == nullptr
      && particleMeshHandle_ == nullptr
      && torchLights_.empty()) {
    if (presentedFrames_ < 4) {
      log("No captured scene meshes available yet");
    }
    ++presentedFrames_;
    return true;
  }

  std::size_t submittedChunks = 0;
  std::size_t submittedBlocks = 0;
  std::size_t submittedCloudQuads = 0;
  std::size_t submittedFireQuads = 0;
  std::size_t submittedDynamicEntityQuads = 0;
  std::size_t submittedDestroyOverlays = 0;
  std::size_t submittedParticleQuads = 0;
  std::size_t submittedTorchLights = 0;
  for (const auto& [chunkKey, meshData] : chunkMeshes_) {
    if (meshData.meshHandle == nullptr) {
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
    if (chunkKey.renderPass == 1) {
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
    instanceInfo.mesh = meshData.meshHandle;
    instanceInfo.transform = makeTranslationTransform(
        static_cast<float>(chunkKey.originX),
        static_cast<float>(chunkKey.originY),
        static_cast<float>(chunkKey.originZ));
    instanceInfo.doubleSided = chunkKey.renderPass == 1 ? TRUE : FALSE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    ++submittedChunks;
    submittedBlocks += meshData.blockCount;
  }

  for (const DynamicEntityFrameInstance& frameInstance : dynamicEntityFrameInstances_) {
    if (frameInstance.meshHandle == nullptr || frameInstance.boneTransforms.empty()) {
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

    remixapi_InstanceInfoBoneTransformsEXT boneTransformsInfo {};
    boneTransformsInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BONE_TRANSFORMS_EXT;
    boneTransformsInfo.pNext = &blendInfo;
    boneTransformsInfo.boneTransforms_values = frameInstance.boneTransforms.data();
    boneTransformsInfo.boneTransforms_count = static_cast<std::uint32_t>(frameInstance.boneTransforms.size());

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &boneTransformsInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = frameInstance.meshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedDynamicEntityQuads += frameInstance.quadCount;
  }

  if (cloudMeshHandle_ != nullptr) {
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
    instanceInfo.mesh = cloudMeshHandle_;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedCloudQuads = cloudQuadCount_;
  }

  if (fireMeshHandle_ != nullptr) {
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
    instanceInfo.mesh = fireMeshHandle_;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedFireQuads = fireQuadCount_;
  }

  if (destroyOverlayMeshHandle_ != nullptr) {
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
    instanceInfo.mesh = destroyOverlayMeshHandle_;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedDestroyOverlays = destroyOverlayCount_;
  }

  if (particleMeshHandle_ != nullptr) {
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
    instanceInfo.mesh = particleMeshHandle_;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedParticleQuads = particleQuadCount_;
  }

  if (!torchLights_.empty()) {
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
      pathStream << "; CreateLightBatched="
                 << (remix_.CreateLightBatched != nullptr ? "yes" : "no")
                 << "; torch lights registered=" << torchLights_.size();
      log(pathStream.str());
    }
    if (remix_.AutoInstancePersistentLights != nullptr) {
      const remixapi_ErrorCode result = remix_.AutoInstancePersistentLights();
      if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
        setError("AutoInstancePersistentLights failed: " + errorCodeToString(result));
        return false;
      }
      submittedTorchLights = torchLights_.size();
    } else if (remix_.DrawLightInstance != nullptr) {
      for (const auto& [position, lightHandle] : torchLights_) {
        (void)position;
        if (lightHandle == nullptr) {
          continue;
        }

        const remixapi_ErrorCode result = remix_.DrawLightInstance(lightHandle);
        if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
          setError("DrawLightInstance failed: " + errorCodeToString(result));
          return false;
        }

        ++submittedTorchLights;
      }
    }
  }

  if (presentedFrames_ < 8
      || submittedChunks != lastSubmittedChunkCount_
      || submittedBlocks != lastSubmittedBlockCount_
      || submittedCloudQuads != lastSubmittedCloudQuadCount_
      || submittedFireQuads != lastSubmittedFireQuadCount_
      || submittedDynamicEntityQuads != lastSubmittedDynamicEntityQuadCount_
      || submittedDestroyOverlays != lastSubmittedDestroyOverlayCount_
      || submittedParticleQuads != lastSubmittedParticleQuadCount_
      || submittedTorchLights != lastSubmittedTorchLightCount_) {
    // Per-frame submission counts are too noisy for the standard log; only
    // emit them during the first few frames so we can confirm the scene is
    // flowing. After that, deltas get swallowed to keep the log readable.
    if (presentedFrames_ < 8) {
      std::ostringstream stream;
      stream << "Submitted " << submittedChunks
             << " chunk meshes covering " << submittedBlocks
             << " blocks and " << submittedCloudQuads
            << " cloud quads and " << submittedFireQuads
            << " fire quads and " << submittedDynamicEntityQuads
             << " dynamic entity quads and " << submittedDestroyOverlays
             << " destroy overlays and "
             << submittedParticleQuads << " particle quads and "
             << submittedTorchLights
             << " torch lights";
      log(stream.str());
    }
  }

  lastSubmittedChunkCount_ = submittedChunks;
  lastSubmittedBlockCount_ = submittedBlocks;
  lastSubmittedCloudQuadCount_ = submittedCloudQuads;
  lastSubmittedFireQuadCount_ = submittedFireQuads;
  lastSubmittedDynamicEntityQuadCount_ = submittedDynamicEntityQuads;
  lastSubmittedDestroyOverlayCount_ = submittedDestroyOverlays;
  lastSubmittedParticleQuadCount_ = submittedParticleQuads;
  lastSubmittedTorchLightCount_ = submittedTorchLights;
  ++presentedFrames_;
  return true;
}

bool RemixRenderer::submitCamera() {
  const float nearPlane = camera_.nearPlane > 0.001f ? camera_.nearPlane : 0.05f;
  const float farPlane = camera_.farPlane > nearPlane ? camera_.farPlane : (nearPlane + 1024.0f);
  const float aspect = camera_.aspect > 0.001f ? camera_.aspect : 1.0f;

  remixapi_CameraInfoParameterizedEXT params {};
  params.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO_PARAMETERIZED_EXT;
  params.position = {camera_.position[0], camera_.position[1], camera_.position[2]};
  params.forward = {camera_.forward[0], camera_.forward[1], camera_.forward[2]};
  params.up = {camera_.up[0], camera_.up[1], camera_.up[2]};
  params.right = {camera_.right[0], camera_.right[1], camera_.right[2]};
  params.fovYInDegrees = camera_.fovYDegrees;
  params.aspect = aspect;
  params.nearPlane = nearPlane;
  params.farPlane = farPlane;

  remixapi_CameraInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO;
  info.pNext = &params;

  const remixapi_ErrorCode result = remix_.SetupCamera(&info);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetupCamera failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

void RemixRenderer::setError(std::string message) {
  lastError_ = std::move(message);
  log(lastError_);
}

void RemixRenderer::log(const std::string& message) {
  OutputDebugStringA(("[mcrtx] " + message + "\n").c_str());
  std::cerr << "[mcrtx] " << message << std::endl;
}


}  // namespace mcrtx