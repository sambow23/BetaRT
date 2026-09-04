// Renderer initialization, standalone worker, shutdown, and runtime loading.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/lifecycle/remix_renderer_timing.hpp"
#if defined(_WIN32)
#include "mcrtx/platform/remix_window_internals.hpp"
#endif
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <chrono>
#include <cstdlib>
#include <functional>
#include <iostream>
#include <string_view>
#include <utility>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::renderer_detail;
#if defined(_WIN32)
using namespace mcrtx::window_detail;
#endif

namespace {

constexpr auto kStandaloneAutonomousFrameInterval = std::chrono::milliseconds(16);

std::uint64_t currentThreadId() {
  return static_cast<std::uint64_t>(std::hash<std::thread::id>{}(std::this_thread::get_id()));
}

std::string describeRequestedWindowMode() {
  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  return configuredWindowMode.empty() ? std::string("<default>") : configuredWindowMode;
}



}  // namespace

RemixRenderer& RemixRenderer::instance() {
  static RemixRenderer renderer;
  return renderer;
}

bool RemixRenderer::initialize(
  NativeWindowHandle sourceWindow,
  std::uint32_t width,
  std::uint32_t height,
  std::filesystem::path remixDllPath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::initialize");
  MCRTX_TRACY_SCOPE("RemixRenderer::initialize");
  TracyUniqueLock lock(mutex_);
  MCRTX_TRACY_LOCK_MARK(mutex_);

  if (initialized_) {
    return true;
  }

#if defined(_WIN32)
  if (sourceWindow == nullptr) {
    setError("initialize called with null HWND");
    return false;
  }

  sourceHwnd_ = sourceWindow;
#else
  (void)sourceWindow;
#endif
  width_ = width == 0 ? 1 : width;
  height_ = height == 0 ? 1 : height;
  camera_.aspect = static_cast<float>(width_) / static_cast<float>(height_);

#if defined(_WIN32)
  bool usedLegacySourceWindowEnvVar = false;
  singleNativeOutputWindow_ = shouldUseSingleNativeOutputWindow();
  standaloneOutputWindow_ = shouldUseStandaloneOutputWindow();
  overlayOutputWindow_ = shouldUseOverlayOutputWindow(&usedLegacySourceWindowEnvVar);
  if (usedLegacySourceWindowEnvVar) {
    log("MCRTX_USE_SOURCE_WINDOW is deprecated; using detached dual-window mode instead");
  }
#else
  singleNativeOutputWindow_ = true;
  standaloneOutputWindow_ = true;
  overlayOutputWindow_ = false;
#endif

  {
    const std::string evictRadiusStr = readEnvironmentVariable("MCRTX_EVICT_RADIUS");
    if (!evictRadiusStr.empty()) {
      try {
        const int parsed = std::stoi(evictRadiusStr);
        evictRadiusChunks_ = parsed > 0 ? parsed : 20;
        log("MCRTX_EVICT_RADIUS=" + evictRadiusStr + " -> evictRadiusChunks=" + std::to_string(evictRadiusChunks_));
      } catch (...) {
        log("MCRTX_EVICT_RADIUS=" + evictRadiusStr + " is non-numeric; using default evictRadiusChunks=" + std::to_string(evictRadiusChunks_));
      }
    }
  }
  log(
      [&]() {
        const char* effectiveMode = overlayOutputWindow_
            ? "overlay"
            : (singleNativeOutputWindow_
                ? "single-native"
                : (standaloneOutputWindow_ ? "standalone" : "dual"));
        const char* standaloneAsync = standaloneOutputWindow_
            ? " standaloneAsync=enabled"
            : " standaloneAsync=disabled";
        return "Window mode requested=" + describeRequestedWindowMode()
            + " effective=" + std::string(effectiveMode)
            + standaloneAsync;
      }());

  if (standaloneOutputWindow_) {
    lock.unlock();
    return startStandaloneWorker(std::move(remixDllPath));
  }

#if defined(_WIN32)
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

  if (!standaloneOutputWindow_ && remix_.DrawScreenOverlay == nullptr) {
    setError("Loaded Remix runtime does not support DrawScreenOverlay; update dxvk-remix-gmod/d3d9.dll to a build with screen overlay support");
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

  {
    const std::string syntheticUi = readEnvironmentVariable("MCRTX_UI_SYNTHETIC_TEST");
    syntheticUiTestEnabled_ = syntheticUi == "1" || syntheticUi == "true";
    const bool uiApiPresent = remix_.RegisterUITexture != nullptr
        && remix_.FreeUITexture != nullptr
        && remix_.SubmitUIDrawList != nullptr;
    log(std::string("Screen-space UI draw-list API ")
        + (uiApiPresent ? "available" : "UNAVAILABLE (runtime predates UI API)"));
    if (syntheticUiTestEnabled_ && !uiApiPresent) {
      log("MCRTX_UI_SYNTHETIC_TEST requested but the loaded runtime lacks the UI API; disabling test");
      syntheticUiTestEnabled_ = false;
    } else if (syntheticUiTestEnabled_) {
      log("MCRTX_UI_SYNTHETIC_TEST enabled: emitting a synthetic UI draw list each frame");
    }
  }

  {
    const std::string floatingOrigin = readEnvironmentVariable("MCRTX_FLOATING_ORIGIN");
    worldOriginRebaseEnabled_ = isTruthyEnvValue(floatingOrigin.c_str());
    if (worldOriginRebaseEnabled_) {
      log("MCRTX_FLOATING_ORIGIN enabled: rebasing world camera and terrain chunk transforms");
    }
  }

  // Minecraft submits world positions in blocks where 1 block = 1 meter.
  // Remix defaults assume 1 world unit = 1 cm (sceneScale = 1). Without this
  // override, every meters-aware subsystem (volumetrics froxel extent,
  // transmittance measurement distance, light volumetric radius falloff)
  // is off by 100x and produces no visible contribution.
  applyRemixConfigPreStartupLocked();

  if (!startup(presentationHwnd)) {
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

  // DLSS-RR's startup preset (updatePathTracerPreset(RayReconstruction) inside
  // Remix's initializer) forcibly overwrites these DI options via setDeferred,
  // clobbering anything we set before Startup. Re-apply after Startup so the
  // pending values land on top of the preset.
  applyRemixConfigPostStartupLocked();

  initializeTerrainMaterials();
  createPrimingMesh();

  initialized_ = true;
  log(overlayOutputWindow_
      ? "Remix renderer initialized in single-window overlay mode"
      : (singleNativeOutputWindow_
          ? "Remix renderer initialized in experimental single-native mode"
          : (standaloneOutputWindow_
              ? "Remix renderer initialized in standalone mode"
              : "Remix renderer initialized in dual-window mode")));
  return true;
#else
  setError("Native Linux rendering requires the standalone worker");
  return false;
#endif
}

void RemixRenderer::shutdown() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::shutdown");
  MCRTX_TRACY_SCOPE("RemixRenderer::shutdown");
  std::thread standaloneWorker;
  {
    TracyUniqueLock lock(mutex_);
    MCRTX_TRACY_LOCK_MARK(mutex_);
    if (standaloneWorkerActive_) {
      standaloneWorkerStopRequested_ = true;
      standaloneWorkerPresentRequested_ = true;
      standaloneWorkerEvent_.notify_all();
      standaloneWorker = std::move(standaloneWorker_);
    } else {
      shutdownLocked();
      return;
    }
  }

  if (standaloneWorker.joinable()) {
    standaloneWorker.join();
  }
}

bool RemixRenderer::startStandaloneWorker(std::filesystem::path remixDllPath) {
  MCRTX_TRACY_SCOPE("RemixRenderer::startStandaloneWorker");
  std::thread standaloneWorker;
  {
    TracyUniqueLock lock(mutex_);
    MCRTX_TRACY_LOCK_MARK(mutex_);
    standaloneWorkerInitReady_ = false;
    standaloneWorkerStopRequested_ = false;
    standaloneWorkerPresentRequested_ = false;
    standaloneWorkerThreadId_ = 0;
    standaloneWorkerActive_ = true;
    standaloneWorker_ = std::thread(&RemixRenderer::standaloneRenderWorkerMain, this, std::move(remixDllPath));
    standaloneWorkerEvent_.wait(lock, [this]() { return standaloneWorkerInitReady_; });
    if (initialized_) {
      return true;
    }
    standaloneWorker = std::move(standaloneWorker_);
  }

  if (standaloneWorker.joinable()) {
    standaloneWorker.join();
  }
  return false;
}

bool RemixRenderer::initializeStandaloneWorker(std::filesystem::path remixDllPath) {
  MCRTX_TRACY_SCOPE("RemixRenderer::initializeStandaloneWorker");
#if defined(_WIN32)
  if (!createOutputWindow(sourceHwnd_)) {
    return false;
  }
  NativeWindowHandle presentationWindow = outputHwnd_;
#else
  NativeWindowHandle presentationWindow = nullptr;
#endif

  if (remixDllPath.empty()) {
    remixDllPath = resolveRemixDllPath();
  }

  if (!loadRemix(remixDllPath)) {
    destroyOutputWindow();
    return false;
  }

#if !defined(_WIN32)
  if (remix_.PumpEvents == nullptr
      || remix_.GetWindowState == nullptr
      || remix_.PollMouseState == nullptr
      || remix_.IsKeyDown == nullptr
      || remix_.SetMouseGrabbed == nullptr
      || remix_.SetCursorPosition == nullptr
      || remix_.SetFullscreen == nullptr) {
    setError("Loaded Remix runtime does not provide the native Linux window and input API");
    resetLoadedRemix();
    return false;
  }
#endif

  {
    const std::string syntheticUi = readEnvironmentVariable("MCRTX_UI_SYNTHETIC_TEST");
    syntheticUiTestEnabled_ = syntheticUi == "1" || syntheticUi == "true";
    const bool uiApiPresent = remix_.RegisterUITexture != nullptr
        && remix_.FreeUITexture != nullptr
        && remix_.SubmitUIDrawList != nullptr;
    log(std::string("Screen-space UI draw-list API ")
        + (uiApiPresent ? "available" : "UNAVAILABLE (runtime predates UI API)"));
    if (syntheticUiTestEnabled_ && !uiApiPresent) {
      log("MCRTX_UI_SYNTHETIC_TEST requested but the loaded runtime lacks the UI API; disabling test");
      syntheticUiTestEnabled_ = false;
    } else if (syntheticUiTestEnabled_) {
      log("MCRTX_UI_SYNTHETIC_TEST enabled: emitting a synthetic UI draw list each frame");
    }
  }

  {
    const std::string floatingOrigin = readEnvironmentVariable("MCRTX_FLOATING_ORIGIN");
    worldOriginRebaseEnabled_ = isTruthyEnvValue(floatingOrigin.c_str());
    if (worldOriginRebaseEnabled_) {
      log("MCRTX_FLOATING_ORIGIN enabled: rebasing world camera and terrain chunk transforms");
    }
  }

#if defined(_WIN32)
  applyRemixConfigPreStartupLocked();
#endif

  if (!startup(presentationWindow)) {
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

#if !defined(_WIN32)
  pumpOutputWindowMessages();
#endif

  applyRemixConfigPostStartupLocked();

  initializeTerrainMaterials();
  createPrimingMesh();
  {
    TracyUniqueLock lock(mutex_);
    MCRTX_TRACY_LOCK_MARK(mutex_);
    initialized_ = true;
  }
  log("Remix renderer initialized in standalone mode (async worker threadId=" + std::to_string(currentThreadId()) + ")");
  return true;
}

void RemixRenderer::standaloneRenderWorkerMain(std::filesystem::path remixDllPath) {
  MCRTX_TRACY_SET_THREAD_NAME("mc-rtx Standalone Worker");
  MCRTX_TRACY_SCOPE("RemixRenderer::standaloneRenderWorkerMain");
  const std::uint64_t workerThreadId = currentThreadId();
  {
    TracyUniqueLock lock(mutex_);
    MCRTX_TRACY_LOCK_MARK(mutex_);
    standaloneWorkerThreadId_ = workerThreadId;
  }

  const bool initialized = initializeStandaloneWorker(std::move(remixDllPath));
  {
    TracyUniqueLock lock(mutex_);
    MCRTX_TRACY_LOCK_MARK(mutex_);
    standaloneWorkerInitReady_ = true;
    if (!initialized) {
      standaloneWorkerActive_ = false;
    }
  }
  standaloneWorkerEvent_.notify_all();
  if (!initialized) {
    return;
  }

  log("Standalone async render worker ready threadId=" + std::to_string(workerThreadId));
  auto nextStandaloneRenderAt = std::chrono::steady_clock::now();
  for (;;) {
    std::string perfSummary;
    bool presentOk = true;
    {
      TracyUniqueLock lock(mutex_);
      standaloneWorkerEvent_.wait_until(lock, nextStandaloneRenderAt, [this]() {
        return standaloneWorkerStopRequested_ || standaloneWorkerPresentRequested_;
      });
      MCRTX_TRACY_LOCK_MARK(mutex_);
      if (standaloneWorkerStopRequested_) {
        break;
      }
      standaloneWorkerPresentRequested_ = false;
    }

    if (std::chrono::steady_clock::now() < nextStandaloneRenderAt) {
      continue;
    }

    {
      const auto lockRequestedAt = std::chrono::steady_clock::now();
      TracyUniqueLock lock(mutex_, std::try_to_lock);
      if (!lock.owns_lock()) {
        std::this_thread::sleep_for(std::chrono::milliseconds(1));
        continue;
      }
      MCRTX_TRACY_LOCK_MARK(mutex_);
      presentOk = presentLocked(
          lock,
          perfSummary,
          toNanoseconds(std::chrono::steady_clock::now() - lockRequestedAt));
    }
    nextStandaloneRenderAt = std::chrono::steady_clock::now() + kStandaloneAutonomousFrameInterval;
    if (!perfSummary.empty()) {
      log(perfSummary);
    }
    if (!presentOk) {
      std::this_thread::sleep_for(std::chrono::milliseconds(1));
    }
  }

  {
    TracyUniqueLock lock(mutex_);
    MCRTX_TRACY_LOCK_MARK(mutex_);
    shutdownLocked();
    standaloneWorkerInitReady_ = false;
    standaloneWorkerStopRequested_ = false;
    standaloneWorkerPresentRequested_ = false;
    standaloneWorkerThreadId_ = 0;
    standaloneWorkerActive_ = false;
  }
  standaloneWorkerEvent_.notify_all();
  log("Standalone async render worker stopped");
}

void RemixRenderer::shutdownLocked() {
  renderSubmissionInFlight_ = false;
  flushDeferredDestroyQueuesLocked();
  if (initialized_ && remix_.Shutdown) {
    if (!standaloneOutputWindow_ && remix_.DrawScreenOverlay != nullptr) {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawScreenOverlay.shutdownClear");
      remix_.DrawScreenOverlay(
          nullptr,
          0,
          0,
          REMIXAPI_FORMAT_R8G8B8A8_UNORM,
          0.0f);
    }

    for (auto& [chunkKey, meshData] : chunkMeshes_) {
      destroyChunkMesh(meshData);
    }
    destroyCloudMesh();
    destroyFireMesh();
    destroyDestroyOverlayMesh();
    destroyBlockOutlineMesh();
    destroyParticleMesh();
    destroyMeshHandle(primingMeshHandle_);
    destroyDynamicEntityMeshes();
    while (!torchLights_.empty()) {
      destroyTorchLight(torchLights_.begin()->first);
    }
    destroyHeldItemTorchLight();
    while (!entityHeldTorchLights_.empty()) {
      destroyEntityHeldTorchLight(entityHeldTorchLights_.begin()->first);
    }
    for (auto& light : activeFlameParticleLights_) {
      if (light.handle != nullptr) {
        destroyLightHandle(light.handle);
      }
    }
    activeFlameParticleLights_.clear();
    destroyTerrainMaterials();
    {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "Shutdown");
      remix_.Shutdown();
    }
  }
  resetLoadedRemix();
  destroyOutputWindow();
  initialized_ = false;
  overlayOutputWindow_ = true;
  singleNativeOutputWindow_ = false;
  standaloneOutputWindow_ = false;
#if defined(_WIN32)
  sourceHwnd_ = nullptr;
#else
  nativeWindowState_ = {};
  nativeMouseState_ = {};
  nativeVirtualKeyDown_.fill(false);
  nativeMouseGrabbed_ = false;
  pendingMouseGrabUpdate_ = false;
  pendingCursorPositionUpdate_ = false;
  pendingFullscreenUpdate_ = false;
#endif
  publishedCamera_ = {};
  publishedCameraValid_ = false;
  chunkBuildActive_ = false;
  activeChunkBuild_ = {};
  activeChunkBlocks_.clear();
  chunkMeshes_.clear();
  dynamicEntityMeshes_.clear();
  dynamicEntityFrameInstances_.clear();
  dynamicEntityFrameInstanceCount_ = 0;
  publishedDynamicEntityFrameInstances_.clear();
  publishedDynamicEntityFrameInstanceCount_ = 0;
  destroyOverlayInstances_.clear();
  blockOutlineInstances_.clear();
  particleQuads_.clear();
  flameParticleLightPositions_.clear();
  dynamicEntityMaterialHandles_.clear();
  activeDynamicEntity_ = {};
  torchLights_.clear();
  torchLightPlacements_.clear();
  heldItemTorchLightHandle_ = nullptr;
  heldItemTorchLightRenderOrigin_ = {};
  entityHeldTorchLights_.clear();
  entityHeldTorchLightsSeenThisFrame_.clear();
  heldItemId_ = -1;
  deferredMeshDestroys_.clear();
  deferredLightDestroys_.clear();
  nextChunkMeshHash_ = 1;
  presentedFrames_ = 0;
  perfWindow_.reset();
  resetPerFramePerfCounters();
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  lastSubmittedCloudQuadCount_ = 0;
  lastSubmittedFireQuadCount_ = 0;
  lastSubmittedDynamicEntityQuadCount_ = 0;
  lastSubmittedDestroyOverlayCount_ = 0;
  lastSubmittedBlockOutlineCount_ = 0;
  lastSubmittedParticleQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  loggedPopulatedSubmissionSummaryCount_ = 0;
  terrainAtlasPath_.clear();
  cloudTexturePath_.clear();
  sunTexturePath_.clear();
  moonTexturePath_.clear();
  fireTexturePath_.clear();
  nextFireMeshHash_ = 1;
  nextDestroyOverlayMeshHash_ = 1;
  nextBlockOutlineMeshHash_ = 1;
  nextParticleMeshHash_ = 1;
  cloudQuadCount_ = 0;
  fireQuadCount_ = 0;
  destroyOverlayCount_ = 0;
  blockOutlineCount_ = 0;
  particleQuadCount_ = 0;
  lastFireAnimationFrame_ = 0xFFFFFFFFu;
  lastFireChunkBuildCount_ = 0xFFFFFFFFFFFFFFFFull;
  lastFireRenderOrigin_ = {};
  appliedRemixConfigValues_.clear();
  appliedGameStateValues_.clear();
  warnedMissingSetConfigVariable_ = false;
  warnedMissingSetGameValue_ = false;
  warnedMissingSetFogState_ = false;
  lastError_.clear();
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
  featureAvailability_ = {};
}

bool RemixRenderer::loadRemix(const std::filesystem::path& remixDllPath) {
  log("Loading Remix runtime from " + remixDllPath.string());
  const remixapi_ErrorCode result = remixapi_lib_loadRemixDllAndInitialize(remixDllPath.c_str(), &remix_, &remixDll_);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Failed to load Remix runtime: " + errorCodeToString(result));
    return false;
  }

  refreshFeatureAvailabilityLocked();
  return true;
}

void RemixRenderer::refreshFeatureAvailabilityLocked() {
  featureAvailability_ = {};
  if (remix_.GetFeatureAvailability == nullptr) {
    log("Loaded Remix runtime does not provide feature availability");
    return;
  }

  const remixapi_ErrorCode result = remix_.GetFeatureAvailability(&featureAvailability_);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    featureAvailability_ = {};
    log("GetFeatureAvailability failed: " + errorCodeToString(result));
    return;
  }

  log(
      "Remix features compiled=" + std::to_string(featureAvailability_.compiledFeatures)
      + " available=" + std::to_string(featureAvailability_.availableFeatures)
      + " dlssFgMaxInterpolatedFrames="
      + std::to_string(featureAvailability_.dlssFrameGenerationMaxInterpolatedFrames));
}

bool RemixRenderer::startup(NativeWindowHandle presentationWindow) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::startup");
  remixapi_StartupInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_STARTUP_INFO;
#if defined(_WIN32)
  info.hwnd = presentationWindow;
#else
  (void)presentationWindow;
  info.windowTitle = "BetaRT";
  info.windowWidth = width_;
  info.windowHeight = height_;
  info.fullscreen = outputWindowFullscreen_ ? TRUE : FALSE;
#endif
  info.disableSrgbConversionForOutput = FALSE;
#if defined(_WIN32)
  info.forceNoVkSwapchain = FALSE;
#endif
  info.editorModeEnabled = FALSE;

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "Startup");
    return remix_.Startup(&info);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Startup failed: " + errorCodeToString(result));
    return false;
  }
  refreshFeatureAvailabilityLocked();
  return true;
}

void RemixRenderer::setError(std::string message) {
  lastError_ = std::move(message);
  log(lastError_);
}

void RemixRenderer::log(const std::string& message) {
#if defined(_WIN32)
  OutputDebugStringA(("[mcrtx] " + message + "\n").c_str());
#endif
  std::cerr << "[mcrtx] " << message << std::endl;
}


}  // namespace mcrtx
