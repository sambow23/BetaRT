#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"
#include "mcrtx/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <chrono>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
namespace {

constexpr std::uint64_t kPerfLogIntervalFrames = 60;
constexpr auto kStandaloneAutonomousFrameInterval = std::chrono::milliseconds(16);
constexpr double kPi = 3.14159265358979323846;

std::uint64_t toNanoseconds(std::chrono::steady_clock::duration duration) {
  return static_cast<std::uint64_t>(std::chrono::duration_cast<std::chrono::nanoseconds>(duration).count());
}

std::string formatMilliseconds(std::uint64_t nanoseconds) {
  std::ostringstream stream;
  stream << std::fixed << std::setprecision(2)
         << (static_cast<double>(nanoseconds) / 1000000.0);
  return stream.str();
}

std::string formatAverageMilliseconds(const DurationPerfCounter& counter, std::uint64_t fallbackSamples) {
  const std::uint64_t divisor = counter.sampleCount != 0 ? counter.sampleCount : fallbackSamples;
  if (divisor == 0) {
    return "0.00";
  }
  return formatMilliseconds(counter.totalNanoseconds / divisor);
}

std::string formatAverageCount(const CountPerfCounter& counter, std::uint64_t fallbackSamples) {
  const std::uint64_t divisor = counter.sampleCount != 0 ? counter.sampleCount : fallbackSamples;
  std::ostringstream stream;
  stream << std::fixed << std::setprecision(2)
         << (divisor == 0 ? 0.0 : static_cast<double>(counter.totalCount) / static_cast<double>(divisor));
  return stream.str();
}

void appendDurationSummary(
    std::ostringstream& stream,
    const char* label,
    const DurationPerfCounter& counter,
    std::uint64_t fallbackSamples) {
  stream << ' ' << label << "AvgMs=" << formatAverageMilliseconds(counter, fallbackSamples)
         << ' ' << label << "MaxMs=" << formatMilliseconds(counter.maxNanoseconds);
}

void appendCountSummary(
    std::ostringstream& stream,
    const char* label,
    const CountPerfCounter& counter,
    std::uint64_t fallbackSamples) {
  stream << ' ' << label << "Avg=" << formatAverageCount(counter, fallbackSamples)
         << ' ' << label << "Max=" << counter.maxCount;
}

std::string describeRequestedWindowMode() {
  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  return configuredWindowMode.empty() ? std::string("<default>") : configuredWindowMode;
}

std::string formatConfigFloat(float value, int precision) {
  std::ostringstream stream;
  stream << std::fixed << std::setprecision(precision) << value;
  return stream.str();
}

}  // namespace

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
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::initialize");
  std::unique_lock<std::mutex> lock(mutex_);

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
  standaloneOutputWindow_ = shouldUseStandaloneOutputWindow();
  overlayOutputWindow_ = shouldUseOverlayOutputWindow(&usedLegacySourceWindowEnvVar);
  if (usedLegacySourceWindowEnvVar) {
    log("MCRTX_USE_SOURCE_WINDOW is deprecated; using detached dual-window mode instead");
  }

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
      "Window mode requested=" + describeRequestedWindowMode()
      + " effective=" + (standaloneOutputWindow_ ? std::string("standalone") : (overlayOutputWindow_ ? std::string("overlay") : std::string("dual")))
      + (standaloneOutputWindow_ ? " standaloneAsync=enabled" : " standaloneAsync=disabled"));

  if (standaloneOutputWindow_) {
    lock.unlock();
    return startStandaloneWorker(std::move(remixDllPath));
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

  if (!standaloneOutputWindow_ && remix_.DrawScreenOverlay == nullptr) {
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

  initialized_ = true;
  log(overlayOutputWindow_
      ? "Remix renderer initialized in single-window overlay mode"
      : (standaloneOutputWindow_
        ? "Remix renderer initialized in standalone mode"
        : "Remix renderer initialized in dual-window mode"));
  return true;
}

void RemixRenderer::shutdown() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::shutdown");
  std::thread standaloneWorker;
  {
    std::unique_lock<std::mutex> lock(mutex_);
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
  std::thread standaloneWorker;
  {
    std::unique_lock<std::mutex> lock(mutex_);
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

  applyRemixConfigPreStartupLocked();

  if (!startup(presentationHwnd)) {
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

  applyRemixConfigPostStartupLocked();

  initializeTerrainMaterials();
  {
    std::unique_lock<std::mutex> lock(mutex_);
    initialized_ = true;
  }
  log("Remix renderer initialized in standalone mode (async worker threadId=" + std::to_string(GetCurrentThreadId()) + ")");
  return true;
}

void RemixRenderer::standaloneRenderWorkerMain(std::filesystem::path remixDllPath) {
  const DWORD workerThreadId = GetCurrentThreadId();
  {
    std::unique_lock<std::mutex> lock(mutex_);
    standaloneWorkerThreadId_ = workerThreadId;
  }

  const bool initialized = initializeStandaloneWorker(std::move(remixDllPath));
  {
    std::unique_lock<std::mutex> lock(mutex_);
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
      std::unique_lock<std::mutex> lock(mutex_);
      standaloneWorkerEvent_.wait_until(lock, nextStandaloneRenderAt, [this]() {
        return standaloneWorkerStopRequested_ || standaloneWorkerPresentRequested_;
      });
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
      std::unique_lock<std::mutex> lock(mutex_, std::try_to_lock);
      if (!lock.owns_lock()) {
        Sleep(1);
        continue;
      }
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
      Sleep(1);
    }
  }

  {
    std::unique_lock<std::mutex> lock(mutex_);
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
    destroyParticleMesh();
    destroyDynamicEntityMeshes();
    while (!torchLights_.empty()) {
      destroyTorchLight(torchLights_.begin()->first);
    }
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
  standaloneOutputWindow_ = false;
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
  lastSubmittedParticleQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  terrainAtlasPath_.clear();
  cloudTexturePath_.clear();
  fireTexturePath_.clear();
  nextFireMeshHash_ = 1;
  nextDestroyOverlayMeshHash_ = 1;
  nextParticleMeshHash_ = 1;
  cloudQuadCount_ = 0;
  fireQuadCount_ = 0;
  destroyOverlayCount_ = 0;
  particleQuadCount_ = 0;
  lastFireAnimationFrame_ = 0xFFFFFFFFu;
  lastFireChunkBuildCount_ = 0xFFFFFFFFFFFFFFFFull;
  appliedRemixConfigValues_.clear();
  warnedMissingSetConfigVariable_ = false;
  warnedMissingSetFogState_ = false;
  lastError_.clear();
}

bool RemixRenderer::setConfigVariableLocked(std::string_view key, const std::string& value, bool logChange, bool force) {
  if (remix_.SetConfigVariable == nullptr) {
    if (!warnedMissingSetConfigVariable_) {
      warnedMissingSetConfigVariable_ = true;
      log("SetConfigVariable not available; runtime Remix config updates are disabled");
    }
    return false;
  }

  std::string keyString(key);
  if (!force) {
    const auto existing = appliedRemixConfigValues_.find(keyString);
    if (existing != appliedRemixConfigValues_.end() && existing->second == value) {
      return true;
    }
  }

  const remixapi_ErrorCode result = remix_.SetConfigVariable(keyString.c_str(), value.c_str());
  if (logChange || result != REMIXAPI_ERROR_CODE_SUCCESS) {
    log(std::string("SetConfigVariable ") + keyString + "=" + value + " -> " + errorCodeToString(result));
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    return false;
  }

  appliedRemixConfigValues_[std::move(keyString)] = value;
  return true;
}

bool RemixRenderer::setConfigFloatLocked(std::string_view key, float value, int precision, bool logChange, bool force) {
  return setConfigVariableLocked(key, formatConfigFloat(value, precision), logChange, force);
}

void RemixRenderer::applyRemixConfigPreStartupLocked() {
  setConfigVariableLocked("rtx.sceneScale", "0.01", true);
  setConfigVariableLocked("rtx.volumetrics.initialRISSampleCount", "32", true);
}

void RemixRenderer::applyRemixConfigPostStartupLocked() {
  setConfigVariableLocked("rtx.skyMode", "1", true, true);
  setConfigVariableLocked("rtx.di.initialSampleCount", "32", true, true);
  setConfigVariableLocked("rtx.di.enableBestLightSampling", "True", true, true);
  setConfigVariableLocked("rtx.di.enableDenoiserConfidence", "True", true, true);
  setConfigVariableLocked("rtx.di.enableDenoiserGradient", "True", true, true);
}

void RemixRenderer::updateAtmosphereConfigLocked(float celestialAngle, bool forceDarkAtmosphere) {
  if (forceDarkAtmosphere) {
    setConfigFloatLocked("rtx.atmosphere.sunElevation", -30.0f, 2, false);
    return;
  }

  const float wrappedAngle = celestialAngle - std::floor(celestialAngle);
  const double rotationRadians = static_cast<double>(wrappedAngle) * 2.0 * kPi;
  const double elevationRadians = std::asin(std::clamp(std::cos(rotationRadians), -1.0, 1.0));
  const float elevationDegrees = static_cast<float>(elevationRadians * 180.0 / kPi);
  const float sunRotationDegrees = std::sin(rotationRadians) < 0.0 ? 180.0f : 0.0f;
  setConfigFloatLocked("rtx.atmosphere.sunElevation", elevationDegrees, 2, false);
  setConfigFloatLocked("rtx.atmosphere.sunRotation", sunRotationDegrees, 2, false);
}

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

void RemixRenderer::resize(std::uint32_t width, std::uint32_t height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::resize");
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
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::drawScreenOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("drawScreenOverlay called before initialize");
    return false;
  }

  if (standaloneOutputWindow_) {
    return true;
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

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawScreenOverlay.draw");
    return remix_.DrawScreenOverlay(
        pixelData,
        width,
        height,
        format,
        std::clamp(opacity, 0.0f, 1.0f));
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("DrawScreenOverlay failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::clearScreenOverlay() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearScreenOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return true;
  }

  if (standaloneOutputWindow_) {
    return true;
  }

  if (remix_.DrawScreenOverlay == nullptr) {
    setError("DrawScreenOverlay is unavailable in the loaded Remix runtime");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawScreenOverlay.clear");
    return remix_.DrawScreenOverlay(
        nullptr,
        0,
        0,
        REMIXAPI_FORMAT_R8G8B8A8_UNORM,
        0.0f);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("DrawScreenOverlay clear failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

remixapi_UIState RemixRenderer::getUiState() const {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::getUiState");
  std::scoped_lock lock(mutex_);

  if (standaloneOutputWindow_) {
    return REMIXAPI_UI_STATE_NONE;
  }

  if (!initialized_ || remix_.GetUIState == nullptr) {
    return REMIXAPI_UI_STATE_NONE;
  }

  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "GetUIState");
  return remix_.GetUIState();
}

bool RemixRenderer::setUiState(remixapi_UIState state) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setUiState");
  std::scoped_lock lock(mutex_);

  if (standaloneOutputWindow_) {
    return state == REMIXAPI_UI_STATE_NONE;
  }

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

  remixapi_ErrorCode result;
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetUIState");
    result = remix_.SetUIState(state);
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetUIState failed: " + errorCodeToString(result));
    return false;
  }

  syncOutputWindowInteractivity(state);

  return true;
}

void RemixRenderer::updateCamera(const CameraState& camera) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateCamera");
  std::scoped_lock lock(mutex_);
  camera_ = camera;
}

bool RemixRenderer::present() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::present");
  const auto lockRequestedAt = std::chrono::steady_clock::now();
  std::unique_lock<std::mutex> lock(mutex_);
  const auto lockAcquiredAt = std::chrono::steady_clock::now();

  if (standaloneOutputWindow_) {
    if (!initialized_) {
      setError("present called before initialize");
      return false;
    }
    standaloneWorkerPresentRequested_ = true;
    standaloneWorkerEvent_.notify_all();
    return true;
  }

  std::string perfSummary;
  const bool ok = presentLocked(
      lock,
      perfSummary,
      toNanoseconds(lockAcquiredAt - lockRequestedAt));
  lock.unlock();
  if (!perfSummary.empty()) {
    log(perfSummary);
  }
  ::mcrtx::perf::onFramePresented();
  return ok;
}

void RemixRenderer::destroyMeshHandle(remixapi_MeshHandle& meshHandle) {
  if (meshHandle == nullptr) {
    return;
  }

  if (renderSubmissionInFlight_) {
    deferredMeshDestroys_.push_back(meshHandle);
  } else if (remix_.DestroyMesh != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMesh");
    remix_.DestroyMesh(meshHandle);
  }

  meshHandle = nullptr;
}

void RemixRenderer::destroyLightHandle(remixapi_LightHandle lightHandle) {
  if (lightHandle == nullptr) {
    return;
  }

  if (renderSubmissionInFlight_) {
    deferredLightDestroys_.push_back(lightHandle);
  } else if (remix_.DestroyLight != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyLight");
    remix_.DestroyLight(lightHandle);
  }
}

void RemixRenderer::flushDeferredDestroyQueuesLocked() {
  if (renderSubmissionInFlight_) {
    return;
  }

  if (remix_.DestroyMesh != nullptr) {
    for (remixapi_MeshHandle meshHandle : deferredMeshDestroys_) {
      if (meshHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMesh.deferred");
        remix_.DestroyMesh(meshHandle);
      }
    }
  }
  deferredMeshDestroys_.clear();

  if (remix_.DestroyLight != nullptr) {
    for (remixapi_LightHandle lightHandle : deferredLightDestroys_) {
      if (lightHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyLight.deferred");
        remix_.DestroyLight(lightHandle);
      }
    }
  }
  deferredLightDestroys_.clear();
}

bool RemixRenderer::prepareFrameSnapshotLocked(FrameRenderSnapshot& snapshot, bool& logNoCapturedScene) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::prepareFrameSnapshotLocked");
  if (!rebuildFireMesh()) {
    return false;
  }

  if (!rebuildDestroyOverlayMesh()) {
    return false;
  }

  if (!rebuildParticleMesh()) {
    return false;
  }

  snapshot = {};
  snapshot.camera = camera_;
  snapshot.chunkMeshes.reserve(chunkMeshes_.size());
  for (const auto& [chunkKey, meshData] : chunkMeshes_) {
    if (meshData.meshHandle == nullptr) {
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

  snapshot.dynamicEntities.reserve(dynamicEntityFrameInstances_.size());
  for (const DynamicEntityFrameInstance& frameInstance : dynamicEntityFrameInstances_) {
    if (frameInstance.meshHandle == nullptr || frameInstance.boneTransforms.empty()) {
      continue;
    }

    snapshot.submittedDynamicEntityQuads += frameInstance.quadCount;
    snapshot.dynamicEntities.push_back(frameInstance);
  }

  snapshot.cloudMeshHandle = cloudMeshHandle_;
  snapshot.fireMeshHandle = fireMeshHandle_;
  snapshot.destroyOverlayMeshHandle = destroyOverlayMeshHandle_;
  snapshot.particleMeshHandle = particleMeshHandle_;
  snapshot.cloudTransformX = cloudTransformX_;
  snapshot.cloudTransformY = cloudTransformY_;
  snapshot.cloudTransformZ = cloudTransformZ_;
  snapshot.submittedCloudQuads = cloudMeshHandle_ != nullptr ? cloudQuadCount_ : 0;
  snapshot.submittedFireQuads = fireMeshHandle_ != nullptr ? fireQuadCount_ : 0;
  snapshot.submittedDestroyOverlays = destroyOverlayMeshHandle_ != nullptr ? destroyOverlayCount_ : 0;
  snapshot.submittedParticleQuads = particleMeshHandle_ != nullptr ? particleQuadCount_ : 0;

  snapshot.torchLights.reserve(torchLights_.size());
  for (const auto& [position, lightHandle] : torchLights_) {
    (void)position;
    if (lightHandle != nullptr) {
      snapshot.torchLights.push_back(lightHandle);
    }
  }
  snapshot.submittedTorchLights = snapshot.torchLights.size();

  if (!snapshot.hasScene()) {
    logNoCapturedScene = presentedFrames_ < 4;
    return true;
  }

  renderSubmissionInFlight_ = true;
  return true;
}

bool RemixRenderer::presentLocked(std::unique_lock<std::mutex>& lock,
                                  std::string& perfSummary,
                                  std::uint64_t lockWaitNanoseconds) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::presentLocked");
  const auto lockAcquiredAt = std::chrono::steady_clock::now();

  if (!initialized_) {
    setError("present called before initialize");
    return false;
  }

  const auto outputWindowStart = std::chrono::steady_clock::now();
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.outputWindow");
    updateOutputWindowSize();
    pumpOutputWindowMessages();
  }
  const auto outputWindowEnd = std::chrono::steady_clock::now();

  FrameRenderSnapshot snapshot;
  bool logNoCapturedScene = false;
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.prepareSnapshot");
    if (!prepareFrameSnapshotLocked(snapshot, logNoCapturedScene)) {
      return false;
    }
  }

  if (!chunkBuildActive_) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.evictDistantChunks");
    const int cameraChunkX = static_cast<int>(camera_.position[0]) / kChunkDimension;
    const int cameraChunkZ = static_cast<int>(camera_.position[2]) / kChunkDimension;
    evictDistantChunks(cameraChunkX, cameraChunkZ, evictRadiusChunks_);
  }

  std::uint64_t lockHoldNanoseconds = toNanoseconds(std::chrono::steady_clock::now() - lockAcquiredAt);
  lock.unlock();

  const auto cameraSubmitStart = std::chrono::steady_clock::now();
  if (!submitCamera(snapshot.camera)) {
    lock.lock();
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    return false;
  }
  const auto cameraSubmitEnd = std::chrono::steady_clock::now();

  const auto geometrySubmitStart = std::chrono::steady_clock::now();
  if (!drawCapturedGeometry(snapshot)) {
    lock.lock();
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    return false;
  }
  const auto geometrySubmitEnd = std::chrono::steady_clock::now();

  const auto remixPresentStart = std::chrono::steady_clock::now();
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "Present");
    return remix_.Present(nullptr);
  }();
  const auto remixPresentEnd = std::chrono::steady_clock::now();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    lock.lock();
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    setError("Present failed: " + errorCodeToString(result));
    return false;
  }

  const auto uiSyncStart = std::chrono::steady_clock::now();
  remixapi_UIState uiState {};
  bool hasUiState = false;
  if (remix_.GetUIState != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "GetUIState.presentSync");
    uiState = remix_.GetUIState();
    hasUiState = true;
  }

  lock.lock();
  const auto secondLockAcquiredAt = std::chrono::steady_clock::now();
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "presentLocked.postPresent");
    renderSubmissionInFlight_ = false;
    flushDeferredDestroyQueuesLocked();
    if (hasUiState) {
      syncOutputWindowInteractivity(uiState);
    }

    perfCachedChunkMeshesThisFrame_ = snapshot.cachedChunkMeshes;
    perfSubmittedChunkMeshesThisFrame_ = snapshot.chunkMeshes.size();
    perfSubmittedChunkBlocksThisFrame_ = snapshot.submittedChunkBlocks;

    if (logNoCapturedScene) {
      log("No captured scene meshes available yet");
    }
  }

  if (presentedFrames_ < 8
      || snapshot.chunkMeshes.size() != lastSubmittedChunkCount_
      || snapshot.submittedChunkBlocks != lastSubmittedBlockCount_
      || snapshot.submittedCloudQuads != lastSubmittedCloudQuadCount_
      || snapshot.submittedFireQuads != lastSubmittedFireQuadCount_
      || snapshot.submittedDynamicEntityQuads != lastSubmittedDynamicEntityQuadCount_
      || snapshot.submittedDestroyOverlays != lastSubmittedDestroyOverlayCount_
      || snapshot.submittedParticleQuads != lastSubmittedParticleQuadCount_
      || snapshot.submittedTorchLights != lastSubmittedTorchLightCount_) {
    if (presentedFrames_ < 8) {
      std::ostringstream stream;
      stream << "Submitted " << snapshot.chunkMeshes.size()
             << " chunk meshes covering " << snapshot.submittedChunkBlocks
             << " blocks and " << snapshot.submittedCloudQuads
             << " cloud quads and " << snapshot.submittedFireQuads
             << " fire quads and " << snapshot.submittedDynamicEntityQuads
             << " dynamic entity quads and " << snapshot.submittedDestroyOverlays
             << " destroy overlays and "
             << snapshot.submittedParticleQuads << " particle quads and "
             << snapshot.submittedTorchLights
             << " torch lights";
      log(stream.str());
    }
  }

  lastSubmittedChunkCount_ = snapshot.chunkMeshes.size();
  lastSubmittedBlockCount_ = snapshot.submittedChunkBlocks;
  lastSubmittedCloudQuadCount_ = snapshot.submittedCloudQuads;
  lastSubmittedFireQuadCount_ = snapshot.submittedFireQuads;
  lastSubmittedDynamicEntityQuadCount_ = snapshot.submittedDynamicEntityQuads;
  lastSubmittedDestroyOverlayCount_ = snapshot.submittedDestroyOverlays;
  lastSubmittedParticleQuadCount_ = snapshot.submittedParticleQuads;
  lastSubmittedTorchLightCount_ = snapshot.submittedTorchLights;
  ++presentedFrames_;

  const auto frameEnd = std::chrono::steady_clock::now();
  const auto cameraSubmitNanoseconds = toNanoseconds(cameraSubmitEnd - cameraSubmitStart);
  const auto geometrySubmitNanoseconds = toNanoseconds(geometrySubmitEnd - geometrySubmitStart);
  const auto uiSyncNanoseconds = toNanoseconds(frameEnd - uiSyncStart);
  perfWindow_.frames += 1;
  perfWindow_.presentLockWait.add(lockWaitNanoseconds);
  lockHoldNanoseconds += toNanoseconds(frameEnd - secondLockAcquiredAt);
  perfWindow_.presentLockHold.add(lockHoldNanoseconds);
  perfWindow_.outputWindowWork.add(toNanoseconds(outputWindowEnd - outputWindowStart));
  perfWindow_.cameraSubmit.add(cameraSubmitNanoseconds);
  perfWindow_.geometrySubmit.add(geometrySubmitNanoseconds);
  perfWindow_.remixPresent.add(toNanoseconds(remixPresentEnd - remixPresentStart));
  perfWindow_.uiStateSync.add(uiSyncNanoseconds);
  perfWindow_.frameCaptureBlockCalls.add(perfCaptureBlockCallsThisFrame_);
  perfWindow_.frameCachedChunkMeshes.add(perfCachedChunkMeshesThisFrame_);
  perfWindow_.frameChunkBuilds.add(perfChunkBuildsThisFrame_);
  perfWindow_.frameChunkMeshRebuilds.add(perfChunkMeshRebuildsThisFrame_);
  perfWindow_.frameSubmittedChunkMeshes.add(perfSubmittedChunkMeshesThisFrame_);
  perfWindow_.frameSubmittedChunkBlocks.add(perfSubmittedChunkBlocksThisFrame_);
  perfWindow_.frameChunkBuildWork.add(perfChunkBuildWorkNanosThisFrame_);
  perfWindow_.frameChunkMeshRebuildWork.add(perfChunkMeshRebuildNanosThisFrame_);
  perfWindow_.frameNeighborRefreshWork.add(perfNeighborRefreshNanosThisFrame_);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.lockWait", lockWaitNanoseconds);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.lockHold", lockHoldNanoseconds);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.cameraSubmit", cameraSubmitNanoseconds);
    ::mcrtx::perf::recordDuration(
      ::mcrtx::perf::Side::Native, "presentLocked.geometrySubmit", geometrySubmitNanoseconds);

  NativePerfWindow summaryWindow {};
  const bool shouldLogSummary = perfWindow_.frames >= kPerfLogIntervalFrames;
  if (shouldLogSummary) {
    summaryWindow = perfWindow_;
    perfWindow_.reset();
  }

  resetPerFramePerfCounters();

  if (shouldLogSummary && isVerboseLoggingEnabled()) {
    std::ostringstream stream;
    stream << "perf native frames=" << summaryWindow.frames;
    appendDurationSummary(stream, "lockWait", summaryWindow.presentLockWait, summaryWindow.frames);
    appendDurationSummary(stream, "lockHold", summaryWindow.presentLockHold, summaryWindow.frames);
    appendDurationSummary(stream, "window", summaryWindow.outputWindowWork, summaryWindow.frames);
    appendDurationSummary(stream, "camera", summaryWindow.cameraSubmit, summaryWindow.frames);
    appendDurationSummary(stream, "draw", summaryWindow.geometrySubmit, summaryWindow.frames);
    appendDurationSummary(stream, "present", summaryWindow.remixPresent, summaryWindow.frames);
    appendDurationSummary(stream, "uiSync", summaryWindow.uiStateSync, summaryWindow.frames);
    appendDurationSummary(stream, "chunkBuild", summaryWindow.frameChunkBuildWork, summaryWindow.frames);
    appendDurationSummary(stream, "chunkRebuild", summaryWindow.frameChunkMeshRebuildWork, summaryWindow.frames);
    appendDurationSummary(stream, "neighborRefresh", summaryWindow.frameNeighborRefreshWork, summaryWindow.frames);
    appendCountSummary(stream, "captureBlocks", summaryWindow.frameCaptureBlockCalls, summaryWindow.frames);
    appendCountSummary(stream, "cachedChunks", summaryWindow.frameCachedChunkMeshes, summaryWindow.frames);
    appendCountSummary(stream, "chunkBuilds", summaryWindow.frameChunkBuilds, summaryWindow.frames);
    appendCountSummary(stream, "chunkRebuilds", summaryWindow.frameChunkMeshRebuilds, summaryWindow.frames);
    appendCountSummary(stream, "submittedChunks", summaryWindow.frameSubmittedChunkMeshes, summaryWindow.frames);
    appendCountSummary(stream, "submittedBlocks", summaryWindow.frameSubmittedChunkBlocks, summaryWindow.frames);
    perfSummary = stream.str();
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
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::startup");
  remixapi_StartupInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_STARTUP_INFO;
  info.hwnd = hwnd;
  info.disableSrgbConversionForOutput = FALSE;
  info.forceNoVkSwapchain = FALSE;
  info.editorModeEnabled = FALSE;

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "Startup");
    return remix_.Startup(&info);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Startup failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::drawCapturedGeometry(const FrameRenderSnapshot& snapshot) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::drawCapturedGeometry");
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
        static_cast<float>(chunkInstance.chunkKey.originX),
        static_cast<float>(chunkInstance.chunkKey.originY),
        static_cast<float>(chunkInstance.chunkKey.originZ));
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

  for (const DynamicEntityFrameInstance& frameInstance : snapshot.dynamicEntities) {

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
    instanceInfo.categoryFlags = frameInstance.entityId == kFirstPersonPlayerShadowEntityId
      ? REMIXAPI_INSTANCE_CATEGORY_BIT_THIRD_PERSON_PLAYER_MODEL
      : (frameInstance.entityId == kFirstPersonDynamicEntityId
          ? REMIXAPI_INSTANCE_CATEGORY_BIT_VIEW_MODEL
          : REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN);
    instanceInfo.mesh = frameInstance.meshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = [&]() {
      MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawInstance.entity");
      return remix_.DrawInstance(&instanceInfo);
    }();
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }
  }

  if (snapshot.cloudMeshHandle != nullptr) {
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

  if (snapshot.particleMeshHandle != nullptr) {
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
  const float nearPlane = camera.nearPlane > 0.001f ? camera.nearPlane : 0.05f;
  const float farPlane = camera.farPlane > nearPlane ? camera.farPlane : (nearPlane + 1024.0f);
  const float aspect = camera.aspect > 0.001f ? camera.aspect : 1.0f;
  const float viewModelNearPlane = std::min(nearPlane, 0.001f);
  const float viewModelFarPlane = farPlane > viewModelNearPlane ? farPlane : (viewModelNearPlane + 1024.0f);

  remixapi_CameraInfoParameterizedEXT params {};
  params.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO_PARAMETERIZED_EXT;
  params.position = {camera.position[0], camera.position[1], camera.position[2]};
  params.forward = {camera.forward[0], camera.forward[1], camera.forward[2]};
  params.up = {camera.up[0], camera.up[1], camera.up[2]};
  params.right = {camera.right[0], camera.right[1], camera.right[2]};
  params.fovYInDegrees = camera.fovYDegrees;
  params.aspect = aspect;
  params.nearPlane = nearPlane;
  params.farPlane = farPlane;

  remixapi_CameraInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO;
  info.pNext = &params;
  info.type = REMIXAPI_CAMERA_TYPE_WORLD;

  remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetupCamera.world");
    return remix_.SetupCamera(&info);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetupCamera(world) failed: " + errorCodeToString(result));
    return false;
  }

  params.nearPlane = viewModelNearPlane;
  params.farPlane = viewModelFarPlane;
  info.type = REMIXAPI_CAMERA_TYPE_VIEW_MODEL;
  result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetupCamera.viewmodel");
    return remix_.SetupCamera(&info);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetupCamera(viewmodel) failed: " + errorCodeToString(result));
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