#include "mcrtx/direct/runtime.hpp"
#include "mcrtx/direct/thread_affinity.hpp"
#include <algorithm>
#include <bit>
#include <cstdio>
#include <sstream>
#include <fstream>
#include <iomanip>
#include <utility>
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/runtime_config.hpp"

namespace mcrtx::direct {
namespace {
template<class T> T handle(std::uint64_t id) { return reinterpret_cast<T>(std::uintptr_t(id)); }
template<class T> T function(remixapi_HMODULE library, const char *name, bool required = true) {
#if defined(_WIN32)
  auto result = reinterpret_cast<T>(GetProcAddress(library, name));
#else
  auto result = reinterpret_cast<T>(dlsym(library, name));
#endif
  if (!result && required) throw std::runtime_error(std::string("Missing native extension: ") + name);
  return result;
}
std::filesystem::path texturePath(std::uint64_t texture) {
  std::ostringstream stream;
  stream << "0x" << std::hex << texture;
  return stream.str();
}
}

Runtime &Runtime::instance() { static Runtime runtime; return runtime; }
Runtime::~Runtime() { stop(); perf::shutdown(); }
void Runtime::check(remixapi_ErrorCode error) {
  if (error != REMIXAPI_ERROR_CODE_SUCCESS)
    throw std::runtime_error("Remix API failed: " + std::to_string(unsigned(error)));
}
void Runtime::setError(const std::string &error) {
  std::lock_guard lock(m_mutex);
  m_error = error;
}
std::string Runtime::error() const { std::lock_guard lock(m_mutex); return m_error; }

bool Runtime::start(const std::filesystem::path &library, unsigned width, unsigned height) {
  if (!width || !height || width > 16384 || height > 16384) return false;
  stop();
  m_mailbox.reset();
  m_terrain.clear();
  m_stop = false;
  m_ready = false;
  m_presented = m_fresh = 0;
  m_lastSequence = m_pendingSequence = 0;
  m_grabRequested = m_grabApplied = m_debugVisible = false;
  m_origin = {}; m_floatingOrigin = {}; m_view = {}; m_projection = {};
  m_retainedOrigin = {};
  m_observedPresentations = 0;
  m_asyncPresentation = false;
  {
    std::lock_guard lock(m_mutex);
    m_error.clear();
    m_window = {};
    m_metrics = {};
  }
  m_worker = std::thread(&Runtime::run, this, library, width, height);
  std::unique_lock lock(m_mutex);
  m_started.wait(lock, [&] { return m_ready; });
  return m_running;
}

void Runtime::stop() {
  m_stop = true;
  m_mailbox.stop();
  if (m_worker.joinable()) m_worker.join();
  m_running = false;
}

bool Runtime::submit(Bytes resources, Bytes frame) {
  if (!m_running) return false;
  try {
    validatePacket(resources, true);
    validatePacket(frame, false);
    return m_mailbox.publish(std::move(resources), std::move(frame));
  } catch (const std::exception &exception) {
    setError(exception.what());
    return false;
  }
}

std::array<std::uint64_t, 4> Runtime::counts() const {
  return {m_mailbox.published(), m_fresh.load(), m_presented.load(), m_mailbox.replaced()};
}

bool Runtime::control(unsigned operation, int value, float x, float y, std::string text) {
  std::lock_guard lock(m_mutex);
  if (!m_running || m_controls.size() >= 1024 || !std::isfinite(x) || !std::isfinite(y)) return false;
  if (operation < 1 || operation > 7) return false;
  m_controls.push_back([this, operation, value, x, y, text = std::move(text)] {
    switch (operation) {
    case 1: m_grabRequested = value != 0; break;
    case 2: check(m_api.SetFullscreen(value != 0)); break;
    case 3: check(m_textInput(value != 0)); break;
    case 4: check(m_api.SetUIState((value < 0 ? m_api.GetUIState() == REMIXAPI_UI_STATE_NONE : value != 0)
      ? REMIXAPI_UI_STATE_ADVANCED : REMIXAPI_UI_STATE_NONE)); break;
    case 5: check(m_api.SetCursorPosition(x, y)); break;
    case 6: check(m_api.RequestPresentedScreenshot(text.c_str())); break;
    case 7: check(m_api.SetConfigVariable("rtx.native.vsync", value ? "True" : "False")); break;
    }
  });
  return true;
}

std::vector<std::uint32_t> Runtime::poll(unsigned maxEvents) {
  std::lock_guard lock(m_mutex);
  const auto count = std::min<std::size_t>({m_input.size(), maxEvents, 4096});
  std::vector<std::uint32_t> values(9 + count * 8);
  values[0] = m_metrics.logicalWidth; values[1] = m_metrics.logicalHeight;
  values[2] = m_metrics.drawableWidth; values[3] = m_metrics.drawableHeight;
  values[4] = m_window.focused; values[5] = m_window.closeRequested || (!m_running && m_ready);
  values[6] = m_window.minimized;
  values[7] = m_window.guiWantsKeyboard | (m_window.guiWantsMouse << 1) | (unsigned(m_debugVisible) << 2);
  values[8] = static_cast<std::uint32_t>(count);
  for (std::size_t i = 0; i < count; ++i) {
    std::memcpy(values.data() + 9 + i * 8, &m_input.front(), 32);
    m_input.pop_front();
  }
  return values;
}

bool Runtime::events() {
  const bool debug = m_api.GetUIState() != REMIXAPI_UI_STATE_NONE;
  const bool grabbed = m_grabRequested && !debug;
  if (grabbed != m_grabApplied) {
    check(m_api.SetMouseGrabbed(grabbed));
    m_grabApplied = grabbed;
  }
  {
    std::lock_guard lock(m_mutex);
    if (debug != m_debugVisible) {
      m_input.clear();
      remixapi_InputEvent reset = {};
      reset.type = REMIXAPI_INPUT_RESET;
      m_input.push_back(reset);
      m_debugVisible = debug;
    }
  }
  check(m_api.PumpEvents());
  remixapi_WindowState state = {};
  remixapi_WindowMetrics metrics = {};
  check(m_api.GetWindowState(&state));
  check(m_getMetrics(&metrics));
  std::array<remixapi_InputEvent, 256> events;
  std::uint32_t count;
  bool changed = false;
  do {
    check(m_poll(events.data(), events.size(), &count));
    std::lock_guard lock(m_mutex);
    changed |= state.drawableWidth != m_window.drawableWidth || state.drawableHeight != m_window.drawableHeight ||
               state.minimized != m_window.minimized;
    m_window = state;
    m_metrics = metrics;
    for (unsigned i = 0; i < count; ++i) {
      if (m_input.size() >= 4096) {
        m_input.clear();
        remixapi_InputEvent reset = {};
        reset.type = REMIXAPI_INPUT_RESET;
        m_input.push_back(reset);
      }
      m_input.push_back(events[i]);
    }
  } while (count == events.size());
  return changed;
}

void Runtime::run(std::filesystem::path library, unsigned width, unsigned height) {
  try {
    const auto loadResult = remixapi_lib_loadRemixDllAndInitialize(library.c_str(), &m_api, &m_library);
#if !defined(_WIN32)
    if (loadResult == REMIXAPI_ERROR_CODE_LOAD_LIBRARY_FAILURE) {
      // The SDK helper leaves dlerror intact. Preserve the actual dependency or
      // symbol-version failure before another loader call can overwrite it.
      const char *reason = dlerror();
      throw std::runtime_error("Cannot load Remix library '" + library.string() + "': " +
                               (reason ? reason : "dynamic loader returned no details"));
    }
#endif
    check(loadResult);
    m_poll = function<PFN_remixapi_PollInputEvents>(m_library, "remixapi_PollInputEvents");
    m_getMetrics = function<PFN_remixapi_GetWindowMetrics>(m_library, "remixapi_GetWindowMetrics");
    m_textInput = function<PFN_remixapi_SetTextInputEnabled>(m_library, "remixapi_SetTextInputEnabled");
    m_presentCount = function<PFN_remixapi_GetPresentationCount>(m_library, "remixapi_GetPresentationCount");
    m_traceStats = function<PFN_remixapi_GetTracingStatisticsEXT>(m_library, "remixapi_GetTracingStatisticsEXT");
    m_denoiseStats = function<PFN_remixapi_GetDenoisingStatisticsEXT>(m_library, "remixapi_GetDenoisingStatisticsEXT");
#if MCRTX_HAS_SAMPLING_STATISTICS
    m_samplingStats = function<PFN_remixapi_GetSamplingStatisticsEXT>(m_library, "remixapi_GetSamplingStatisticsEXT", false);
#endif
#if MCRTX_HAS_LIGHTING_STATISTICS
    m_lightingStats = function<PFN_remixapi_GetLightingStatisticsEXT>(m_library, "remixapi_GetLightingStatisticsEXT", false);
#endif
#if MCRTX_HAS_LOCALLIGHT_STATISTICS
    m_localStats = function<PFN_remixapi_GetLocalLightStatisticsEXT>(m_library, "remixapi_GetLocalLightStatisticsEXT", false);
#endif
#if MCRTX_HAS_LOCALLIGHTGUIDING_STATISTICS
    m_localGuidingStats = function<PFN_remixapi_GetLocalLightGuidingStatisticsEXT>(m_library, "remixapi_GetLocalLightGuidingStatisticsEXT", false);
#endif
#if MCRTX_HAS_SHADOW_STATISTICS
    m_shadowStats = function<PFN_remixapi_GetShadowStatisticsEXT>(m_library, "remixapi_GetShadowStatisticsEXT", false);
#endif
#if MCRTX_HAS_SHARC_STATISTICS
    m_sharcStats = function<PFN_remixapi_GetSharcStatisticsEXT>(m_library, "remixapi_GetSharcStatisticsEXT", false);
#endif
    m_backendCaps = function<PFN_remixapi_GetBackendCapabilitiesEXT>(m_library, "remixapi_GetBackendCapabilitiesEXT", false);
#if MCRTX_HAS_RETAINED_INSTANCES
    m_createRetained = function<PFN_remixapi_CreateRetainedInstance>(m_library, "remixapi_CreateRetainedInstance", false);
    m_updateRetained = function<PFN_remixapi_UpdateRetainedInstance>(m_library, "remixapi_UpdateRetainedInstance", false);
    m_updateRetainedMany = function<PFN_remixapi_UpdateRetainedInstances>(m_library, "remixapi_UpdateRetainedInstances", false);
    m_destroyRetained = function<PFN_remixapi_DestroyRetainedInstances>(m_library, "remixapi_DestroyRetainedInstances", false);
    m_drawRetained = function<PFN_remixapi_DrawRetainedInstances>(m_library, "remixapi_DrawRetainedInstances", false);
    // Chunks are registered once and replayed by the runtime; MCRTX_RETAINED_TERRAIN=0 keeps per-frame draws.
    const auto retained = detail::readEnvironmentVariable("MCRTX_RETAINED_TERRAIN");
    m_useRetained = m_createRetained && m_updateRetained && m_updateRetainedMany && m_destroyRetained && m_drawRetained &&
                    (retained.empty() || detail::isTruthyEnvValue(retained.c_str()));
    std::fprintf(stderr, "[mcrtx] terrain submission: %s\n", m_useRetained ? "retained instances" : "per-frame draws");
#else
    m_useRetained = false;
#endif
    remixapi_StartupInfo startup = {};
    startup.sType = REMIXAPI_STRUCT_TYPE_STARTUP_INFO;
#if !defined(_WIN32)
    startup.windowTitle = "BetaRT";
    startup.windowWidth = width;
    startup.windowHeight = height;
#else
    (void)width; (void)height;
#endif
    check(m_api.Startup(&startup));
    const auto vsync = detail::readEnvironmentVariable("MCRTX_VSYNC");
    if (!vsync.empty()) check(m_api.SetConfigVariable("rtx.native.vsync", detail::isTruthyEnvValue(vsync.c_str()) ? "True" : "False"));
    const auto backend = detail::readEnvironmentVariable("MCRTX_RENDER_BACKEND");
    if (!backend.empty()) {
      const auto result = m_api.SetConfigVariable("rtx.native.backend", backend.c_str());
      if (result == REMIXAPI_ERROR_CODE_FEATURE_UNAVAILABLE) std::fprintf(stderr, "Requested path tracing unavailable; using raster (see Remix capability log).\n");
      else check(result);
    }
    for (const auto &[environment, setting] : {std::pair{"MCRTX_PT_SAMPLES", "samples"},
        {"MCRTX_PT_UPSCALER", "upscaler"}, {"MCRTX_PT_DLSS_QUALITY", "dlssQuality"},
        {"MCRTX_PT_DLSS_RR", "dlssRayReconstruction"},
        {"MCRTX_PT_TORCH_SAMPLER", "torchSampler"}, {"MCRTX_PT_RTXDI_PROPOSAL", "rtxdiProposal"},
        {"MCRTX_PT_RTXDI_CANDIDATES", "rtxdiCandidates"},
        {"MCRTX_PT_RTXDI_SECONDARY_CANDIDATES", "rtxdiSecondaryCandidates"},
        {"MCRTX_PT_RTXDI_TEMPORAL_REUSE", "rtxdiTemporalReuse"},
        {"MCRTX_PT_RTXDI_SPATIAL_SAMPLES", "rtxdiSpatialSamples"},
        {"MCRTX_PT_RTXDI_HISTORY_LENGTH", "rtxdiHistoryLength"},
        {"MCRTX_PT_LOCAL_LIGHTS", "localLights"}, {"MCRTX_PT_LOCAL_SAMPLES", "localSamples"},
        {"MCRTX_PT_LOCAL_DIRECT_CLAMP", "localDirectClamp"},
        {"MCRTX_PT_INDIRECT_CLAMP", "indirectClamp"},
        {"MCRTX_PT_LOCAL_FIREFLY_FILTER", "localFireflyFilter"},
        {"MCRTX_PT_LOCAL_DENOISER", "localDenoiser"}, {"MCRTX_PT_LOCAL_INTENSITY", "localIntensity"},
        {"MCRTX_PT_SEPARATE_LOCAL_LIGHTING", "separateLocalLighting"},
        {"MCRTX_PT_LOCAL_RANGE", "localRange"}, {"MCRTX_PT_LOCAL_GUIDING", "localGuiding"},
        {"MCRTX_PT_SIGMA", "sigma"}, {"MCRTX_PT_SUN_DIAMETER", "sunAngularDiameter"},
        {"MCRTX_PT_SHADOW_DEBUG", "shadowDebug"}, {"MCRTX_PT_BOUNCES", "bounces"}, {"MCRTX_PT_ACCUMULATE", "accumulate"},
        {"MCRTX_PT_RUSSIAN_ROULETTE", "russianRoulette"},
        {"MCRTX_PT_ROULETTE_MIN_BOUNCES", "rouletteMinBounces"},
        {"MCRTX_PT_FORCE_REBUILD", "forceRebuild"}, {"MCRTX_PT_DENOISER", "denoiser"}, {"MCRTX_PT_TAA", "taa"}, {"MCRTX_PT_RTXDI", "rtxdi"}, {"MCRTX_PT_SHARC", "sharc"},
        {"MCRTX_PT_EMISSIVE_SAMPLER", "emissiveSampler"}, {"MCRTX_PT_EMISSIVE_DOWNSCALE", "emissiveDownscale"},
        {"MCRTX_PT_SHARC_DOWNSCALE", "sharcDownscale"}, {"MCRTX_PT_SHARC_SCALE", "sharcSceneScale"},
        {"MCRTX_PT_SHARC_CAPACITY_LOG2", "sharcCapacityLog2"}, {"MCRTX_PT_SHARC_DEBUG", "sharcDebug"},
        {"MCRTX_PT_LIGHT_CANDIDATES", "lightCandidates"}, {"MCRTX_PT_INDIRECT_SAMPLING", "indirectSampling"}, {"MCRTX_PT_SKY", "sky"}}) {
      auto value = detail::readEnvironmentVariable(environment);
      // An explicit backend wins even if the legacy alias is also supplied.
      if (std::strcmp(environment, "MCRTX_PT_RTXDI") == 0 &&
          !detail::readEnvironmentVariable("MCRTX_PT_TORCH_SAMPLER").empty()) continue;
      if (!value.empty()) check(m_api.SetConfigVariable((std::string("rtx.native.pathtrace.") + setting).c_str(), value.c_str()));
    }
    // Runtime features BetaRT enables by default; =0 disables one. They apply at
    // every launch, over the loaded OpenRemix profile (the API cannot read it).
    // Runtimes without a setting keep their own default.
    for (const auto &[environment, key] : {std::pair{"MCRTX_ASYNC_PRESENT", "rtx.native.asyncPresent"},
        {"MCRTX_PERSISTENT_STATIC", "rtx.native.scene.persistentStatic"},
        {"MCRTX_PT_SHADOW_EARLY_OUT", "rtx.native.pathtrace.shadowEarlyOut"},
        {"MCRTX_PT_HARDWARE_FACE_CULLING", "rtx.native.pathtrace.hardwareFaceCulling"}}) {
      const auto value = detail::readEnvironmentVariable(environment);
      const bool enabled = value.empty() || detail::isTruthyEnvValue(value.c_str());
      const auto result = m_api.SetConfigVariable(key, enabled ? "1" : "0");
      if (result != REMIXAPI_ERROR_CODE_SUCCESS)
        std::fprintf(stderr, "[mcrtx] %s unavailable in this runtime (%u)\n", key, unsigned(result));
    }
    remixapi_LightInfoDistantEXT distant = {};
    distant.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_DISTANT_EXT;
    distant.angularDiameterDegrees = .533f;
    distant.direction = {-.423999f, -.847998f, -.317999f};
    remixapi_LightInfo daylight = {};
    daylight.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO; daylight.pNext = &distant;
    daylight.hash = 0x6fffffffffffffffull;
    check(m_api.CreateLight(&daylight, &m_daylight));
    events();
    // Apply after renderer startup so existing driver threads keep their policy.
    const auto affinity = setRenderThreadCpus(detail::readEnvironmentVariable("MCRTX_RENDER_CPUS"));
    if (!affinity.message.empty()) std::fprintf(stderr, "[mcrtx] %s\n", affinity.message.c_str());
    {
      std::lock_guard lock(m_mutex);
      m_running = true;
      m_ready = true;
    }
    m_started.notify_all();
    std::ofstream profile;
    const auto profilePath = detail::readEnvironmentVariable("MCRTX_PT_PROFILE");
    if (!profilePath.empty()) {
      profile.open(profilePath);
      profile << "time_ms,fresh,interval_ms,gpu_build_ms,gpu_trace_ms,upload_bytes,acceleration_bytes,blas_builds,blas_refits,compactions,static_builds,dynamic_builds,dynamic_refits,rebuild_reason,scene_generation,samples,accumulation,temporal_redraw,gpu_confidence_ms,gpu_nrd_ms,gpu_taa_ms,denoise_bytes,history_resets,denoiser,taa,rtxdi,sharc,emitters,gpu_light_prepare_ms,gpu_rtxdi_ms,gpu_sharc_update_ms,gpu_sharc_resolve_ms,lighting_bytes,cache_queries,cache_hits,cache_occupied,cache_resets,gpu_direct_nrd_ms,requested_sampler,sampler,requested_downscale,downscale,requested_indirect,indirect,lighting_width,lighting_height,light_candidates,sky,primary_shading_locations,transparent_records,unavailable_opaque,unavailable_transparent,gpu_candidates_ms,gpu_temporal_reuse_ms,gpu_spatial_reuse_ms,gpu_visibility_ms,gpu_reconstruction_ms,gpu_lightbvh_ms,lightbvh_builds,lightbvh_refits,sharc_sampled_queries,sharc_sampled_hits,sigma,gpu_sigma_ms,sigma_history_resets,sun_diameter_degrees,local_lights,local_active_lights,local_samples,local_nodes,local_tree_depth,local_bvh_builds,local_bvh_refits,local_bvh_cpu_ms,local_bvh_bytes,local_denoiser,gpu_local_nrd_ms,local_range,local_guiding,local_observed_samples,local_guided_samples,local_visible_samples,local_guiding_bytes,local_guiding_resets,submission_sequence,presentation_count\n";
    }
    auto lastPresent = std::chrono::steady_clock::now();
    auto profilePresent = lastPresent;
    while (!m_stop) {
      auto next = m_mailbox.take(std::chrono::milliseconds(8));
      if (m_stop) break;
      bool redraw = events();
      std::deque<std::function<void()>> controls;
      remixapi_WindowState state;
      {
        std::lock_guard lock(m_mutex);
        controls.swap(m_controls);
        state = m_window;
      }
      redraw |= !controls.empty();
      for (auto &command : controls) {
        // An unavailable optional control (e.g. screenshot format) does not stop gameplay.
        try { command(); } catch (const std::exception &error) {
          std::fprintf(stderr, "Optional Remix control failed: %s\n", error.what());
        }
      }
      if (state.closeRequested) break;
      if (next.frame) {
        for (const auto &resources : next.resources) { resourcePacket(resources); m_uploadBytes += resources.size(); }
        m_lastFrame = std::move(next.frame);
        m_pendingSequence = next.sequence;
      }
      const bool terrainChanged = terrainPublish();
      const auto now = std::chrono::steady_clock::now();
      redraw |= m_api.GetUIState() != REMIXAPI_UI_STATE_NONE &&
        now - lastPresent >= std::chrono::microseconds(16667);
      remixapi_TracingStatisticsEXT tracing = {};
      tracing.structSize = sizeof(tracing);
      check(m_traceStats(&tracing));
      const bool accumulation = tracing.accumulationEnabled != 0;
      remixapi_DenoisingStatisticsEXT denoising = {};
      denoising.structSize = sizeof(denoising);
      check(m_denoiseStats(&denoising));
      bool lightingRedraw = false;
#if MCRTX_HAS_LIGHTING_STATISTICS
      remixapi_LightingStatisticsEXT illumination{};
      illumination.structSize = sizeof(illumination);
      bool lightingAvailable = m_lightingStats && m_lightingStats(&illumination) == REMIXAPI_ERROR_CODE_SUCCESS;
      lightingRedraw = lightingAvailable && illumination.redrawRequested;
#endif
#if MCRTX_HAS_SAMPLING_STATISTICS
      remixapi_SamplingStatisticsEXT sampling{};
      sampling.structSize = sizeof(sampling);
      bool samplingAvailable = m_samplingStats && m_samplingStats(&sampling) == REMIXAPI_ERROR_CODE_SUCCESS;
#endif
      const bool temporal = denoising.redrawRequested != 0 || lightingRedraw;
      if (state.minimized || !state.drawableWidth || !state.drawableHeight) continue;
      if (m_lastFrame && (m_pendingSequence != m_lastSequence || terrainChanged || redraw || accumulation || tracing.redrawRequested || temporal)) {
        MCRTX_PERF_CPU_SCOPE(perf::Side::Native, "direct.frame");
        framePacket(*m_lastFrame);
        drawTerrain();
        check(m_api.DrawLightInstance(m_daylight));
        std::uint64_t before = 0, after = 0;
        check(m_presentCount(&before));
        // A count that advanced since the last Present returned means the runtime
        // presents asynchronously (rtx.native.asyncPresent): Present hands the frame
        // to its render thread and the count follows. There, success means handed off.
        m_asyncPresentation |= before != m_observedPresentations;
        check(m_api.Present(nullptr));
        check(m_presentCount(&after));
        m_observedPresentations = after;
        if (after == before && !m_asyncPresentation) continue;
        ++m_presented;
        const bool fresh = m_pendingSequence != m_lastSequence;
        if (fresh) {
          ++m_fresh;
          m_lastSequence = m_pendingSequence;
        }
        perf::recordCount(perf::Side::Native, "direct.fresh", fresh ? 1 : 0);
        perf::recordCount(perf::Side::Native, "direct.accumulationRedraw", !fresh && accumulation ? 1 : 0);
        check(m_traceStats(&tracing));
        check(m_denoiseStats(&denoising));
#if MCRTX_HAS_LIGHTING_STATISTICS
        lightingAvailable = m_lightingStats && m_lightingStats(&illumination) == REMIXAPI_ERROR_CODE_SUCCESS;
#endif
#if MCRTX_HAS_SAMPLING_STATISTICS
        samplingAvailable = m_samplingStats && m_samplingStats(&sampling) == REMIXAPI_ERROR_CODE_SUCCESS;
#endif
#if MCRTX_HAS_SHARC_STATISTICS
        remixapi_SharcStatisticsEXT cache{};
        cache.structSize = sizeof(cache);
        const bool cacheAvailable = m_sharcStats && m_sharcStats(&cache) == REMIXAPI_ERROR_CODE_SUCCESS;
#endif
        perf::recordCount(perf::Side::Native, "direct.temporalRedraw", !fresh && temporal ? 1 : 0);
        perf::recordCount(perf::Side::Native, "trace.samples", tracing.samplesPerPixel);
        perf::recordCount(perf::Side::Native, "trace.accumulation", tracing.accumulationCount);
        perf::recordCount(perf::Side::Native, "trace.generation", tracing.sceneGeneration);
        perf::recordCount(perf::Side::Native, "trace.blasBuilds", tracing.blasBuilds);
        perf::recordCount(perf::Side::Native, "trace.blasRefits", tracing.blasRefits);
        perf::recordCount(perf::Side::Native, "trace.compactions", tracing.blasCompactions);
        perf::recordCount(perf::Side::Native, "trace.staticBuilds", tracing.staticTlasBuilds);
        perf::recordCount(perf::Side::Native, "trace.dynamicBuilds", tracing.dynamicTlasBuilds);
        perf::recordCount(perf::Side::Native, "trace.dynamicRefits", tracing.dynamicTlasRefits);
        perf::recordCount(perf::Side::Native, "trace.rebuildReason", tracing.dynamicRebuildReason);
        perf::recordCount(perf::Side::Native, "trace.accelerationBytes", tracing.accelerationBytes);
        perf::recordCount(perf::Side::Native, "trace.gpuBuildUs", std::uint64_t(tracing.gpuBuildMilliseconds*1000));
        perf::recordCount(perf::Side::Native, "trace.gpuTraceUs", std::uint64_t(tracing.gpuTraceMilliseconds*1000));
        const auto completed = std::chrono::steady_clock::now();
        if (profile.is_open()) {
          const auto stamp = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
          profile << stamp << ',' << fresh << ',' << std::fixed << std::setprecision(6)
            << std::chrono::duration<double, std::milli>(completed-profilePresent).count() << ','
            << tracing.gpuBuildMilliseconds << ',' << tracing.gpuTraceMilliseconds << ',' << m_uploadBytes << ','
            << tracing.accelerationBytes << ',' << tracing.blasBuilds << ',' << tracing.blasRefits << ',' << tracing.blasCompactions << ','
            << tracing.staticTlasBuilds << ',' << tracing.dynamicTlasBuilds << ',' << tracing.dynamicTlasRefits << ','
            << tracing.dynamicRebuildReason << ',' << tracing.sceneGeneration << ',' << tracing.samplesPerPixel << ',' << tracing.accumulationCount << ',' << (!fresh && temporal ? 1 : 0) << ',' << denoising.gpuConfidenceMilliseconds << ',' << denoising.gpuDenoiseMilliseconds << ',' << denoising.gpuTaaMilliseconds << ',' << denoising.memoryBytes << ',' << denoising.historyResets << ',' << denoising.activeDenoiser << ',' << denoising.activeTaa;
#if MCRTX_HAS_LIGHTING_STATISTICS
          if (lightingAvailable) {
            profile << ',' << illumination.activeRtxdi << ',' << illumination.activeSharc << ',' << illumination.emitterCount << ',' << illumination.gpuLightPrepareMilliseconds << ',' << illumination.gpuRtxdiMilliseconds << ',' << illumination.gpuSharcUpdateMilliseconds << ',' << illumination.gpuSharcResolveMilliseconds << ',' << illumination.memoryBytes << ',' << illumination.cacheQueries << ',' << illumination.cacheHits << ',' << illumination.cacheOccupied << ',' << illumination.cacheResets << ',' << illumination.gpuDirectDenoiseMilliseconds;
          } else
#endif
#if MCRTX_HAS_SHARC_STATISTICS
          if (cacheAvailable) {
            // Keep unsupported systems and full-ray counters explicitly unavailable.
            // This SDK's query diagnostics sample pixels; report those separately.
            profile << ",unavailable," << cache.active << ",unavailable,unavailable,unavailable,";
            if (cache.gpuTimingsAvailable) profile << cache.gpuUpdateMilliseconds << ',' << cache.gpuResolveMilliseconds;
            else profile << "unavailable,unavailable";
            profile << ',' << cache.memoryBytes << ",unavailable,unavailable," << cache.occupiedEntries << ',' << cache.resets << ",unavailable";
          } else
#endif
          {
            for (unsigned i = 0; i < 13; ++i) profile << ",unavailable";
          }
#if MCRTX_HAS_SAMPLING_STATISTICS
          if (samplingAvailable) {
            profile << ',' << sampling.requestedSampler << ',' << sampling.activeSampler << ',' << sampling.requestedDownscale << ',' << sampling.activeDownscale
            << ',' << sampling.requestedIndirect << ',' << sampling.activeIndirect << ',' << sampling.lightingWidth << ',' << sampling.lightingHeight
            << ',' << sampling.lightCandidates << ',' << sampling.skyEnabled << ',' << sampling.primaryShadingLocations << ',' << sampling.transparentRecords
            << ',' << sampling.unavailableOpaque << ',' << sampling.unavailableTransparent << ',' << sampling.gpuCandidatesMilliseconds
            << ',' << sampling.gpuTemporalReuseMilliseconds << ',' << sampling.gpuSpatialReuseMilliseconds << ',' << sampling.gpuVisibilityMilliseconds
            << ',' << sampling.gpuReconstructionMilliseconds << ',' << sampling.gpuLightBvhMilliseconds << ',' << sampling.lightBvhBuilds << ',' << sampling.lightBvhRefits;
          } else
#endif
          {
            for (unsigned i = 0; i < 22; ++i) profile << ",unavailable";
          }
#if MCRTX_HAS_SHARC_STATISTICS
          if (cacheAvailable) profile << ',' << cache.queryAttempts << ',' << cache.queryHits;
          else
#endif
          profile << ",unavailable,unavailable";
#if MCRTX_HAS_SHADOW_STATISTICS
          remixapi_ShadowStatisticsEXT shadow{};
          shadow.structSize = sizeof(shadow);
          if (m_shadowStats && m_shadowStats(&shadow) == REMIXAPI_ERROR_CODE_SUCCESS) {
            profile << ',' << shadow.active << ',';
            if (shadow.gpuTimingsAvailable) profile << shadow.gpuSigmaMilliseconds;
            else profile << "unavailable";
            profile << ',' << shadow.historyResets << ',' << shadow.sunAngularDiameterDegrees;
          } else
#endif
          profile << ",unavailable,unavailable,unavailable,unavailable";
#if MCRTX_HAS_LOCALLIGHT_STATISTICS
          remixapi_LocalLightStatisticsEXT local{};
          local.structSize = sizeof(local);
          if (m_localStats && m_localStats(&local) == REMIXAPI_ERROR_CODE_SUCCESS) {
            profile << ',' << local.lightCount << ',' << local.activeLights << ',' << local.samples << ',' << local.nodeCount
                    << ',' << local.treeDepth << ',' << local.builds << ',' << local.refits << ',' << local.cpuUpdateMilliseconds
                    << ',' << local.memoryBytes << ',' << local.denoiserActive << ',';
            if (local.gpuTimingsAvailable) profile << local.gpuDenoiseMilliseconds;
            else profile << "unavailable";
          } else
#endif
          for (unsigned i = 0; i < 11; ++i) profile << ",unavailable";
#if MCRTX_HAS_LOCALLIGHTGUIDING_STATISTICS
          remixapi_LocalLightGuidingStatisticsEXT guiding{};
          guiding.structSize = sizeof(guiding);
          if (m_localGuidingStats && m_localGuidingStats(&guiding) == REMIXAPI_ERROR_CODE_SUCCESS) {
            profile << ',' << guiding.influenceRange << ',' << guiding.active << ',' << guiding.sampledQueries
                    << ',' << guiding.sampledGuided << ',' << guiding.sampledVisible << ',' << guiding.memoryBytes
                    << ',' << guiding.historyResets;
          } else
#endif
          for (unsigned i = 0; i < 7; ++i) profile << ",unavailable";
          profile << ',' << m_lastSequence << ',' << after << '\n';
        }
        profilePresent = completed; m_uploadBytes = 0;
        perf::onFramePresented();
        lastPresent = now;
      }
    }
  } catch (const std::exception &exception) {
    setError(exception.what());
  } catch (...) {
    setError("Native renderer failed");
  }
  m_running = false;
  m_mailbox.stop();
  m_terrain.clear();
  if (m_library) remixapi_lib_shutdownAndUnloadRemixDll(&m_api, m_library);
  m_library = nullptr;
  m_api = {};
  m_materials.clear();
  m_chunks.clear();
  m_torchLights.clear();
  m_lastFrame.reset();
  {
    std::lock_guard lock(m_mutex);
    m_controls.clear();
    m_input.clear();
    m_ready = true;
    m_window.closeRequested = true;
  }
  m_started.notify_all();
}

void Runtime::material(std::uint64_t id) {
  const auto &source = m_materials.at(id);
  const auto path = texturePath(source.texture);
  remixapi_MaterialInfoOpaqueEXT opaque = {};
  opaque.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  opaque.albedoConstant = {1, 1, 1};
  opaque.opacityConstant = 1;
  opaque.roughnessConstant = 1;
  opaque.metallicConstant = 0;
  const auto &normalPath=source.pbr[0], &roughnessPath=source.pbr[1];
  const auto &metallicPath=source.pbr[2], &authoredEmissionPath=source.pbr[3];
  opaque.roughnessTexture=roughnessPath.empty()?nullptr:roughnessPath.c_str();
  opaque.metallicTexture=metallicPath.empty()?nullptr:metallicPath.c_str();
  opaque.alphaTestType = 7;
  opaque.useDrawCallAlphaState = 1;
  remixapi_MaterialInfo info = {};
  info.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  info.hash = id;
  info.pNext = &opaque;
  // Reserved terrain material IDs encode block, intrinsic light level and
  // material class. Torches already have stable sphere lights; only suppress
  // their generated area lights, keeping authored/procedural visible emission.
  constexpr std::uint64_t terrainMaterialBase = 0x6000000000000000ull;
  const auto block = id >= terrainMaterialBase && id < terrainMaterialBase + 256 * 16 * 8
      ? (id - terrainMaterialBase) / (16 * 8) : 0;
  remixapi_MaterialInfoEmissionEXT emission{};
  emission.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_EMISSION_EXT;
  emission.suppressLightGeneration = 1;
  if (block == 50 || block == 76) opaque.pNext = &emission;
  info.albedoTexture = source.texture ? path.c_str() : nullptr;
  info.normalTexture=normalPath.empty()?nullptr:normalPath.c_str();
  const auto emissionPath=texturePath(source.emissionTexture);
  info.emissiveTexture=!authoredEmissionPath.empty()?authoredEmissionPath.c_str():source.emissionTexture?emissionPath.c_str():nullptr;
  info.emissiveColorConstant={source.emission[0],source.emission[1],source.emission[2]};
  info.emissiveIntensity=1;
  info.filterMode = source.filter;
  info.wrapModeU = source.wrapU;
  info.wrapModeV = source.wrapV;
  remixapi_MaterialHandle result;
  check(m_api.CreateMaterial(&info, &result));
}

void Runtime::resourcePacket(const Bytes &bytes) {
  MCRTX_PERF_SCOPE(perf::Side::Native, "direct.resources");
  perf::recordCount(perf::Side::Native, "direct.resourceBytes", bytes.size());
  if (bytes.empty()) return;
  Reader stream(bytes);
  stream.header();
  while (stream.remaining()) {
    const auto op = static_cast<Op>(stream.read<std::uint32_t>());
    auto data = stream.slice(stream.read<std::uint32_t>());
    const auto id = data.read<std::uint64_t>();
    switch (op) {
    case Op::Texture: {
      remixapi_TextureInfo info = {};
      info.sType = REMIXAPI_STRUCT_TYPE_TEXTURE_INFO;
      info.hash = id;
      info.width = data.read<std::uint32_t>();
      info.height = data.read<std::uint32_t>();
      info.depth = info.mipLevels = 1;
      info.format = REMIXAPI_FORMAT_R8G8B8A8_SRGB;
      info.data = data.data();
      info.dataSize = data.remaining();
      remixapi_TextureHandle result;
      check(m_api.CreateTexture(&info, &result));
      check(m_api.RegisterUITexture(id, info.width, info.height, info.format, info.data, info.dataSize));
      for (const auto &[materialId, source] : m_materials) {
        if (source.texture == id || source.emissionTexture == id) material(materialId);
      }
      break;
    }
    case Op::Material: {
      Material value;
      value.texture = data.read<std::uint64_t>();
      value.filter = data.read<std::uint32_t>();
      value.wrapU = data.read<std::uint32_t>();
      value.wrapV = data.read<std::uint32_t>();
      value.emissionTexture=data.read<std::uint64_t>();
      for(auto &component:value.emission)component=data.read<float>();
      for(auto &map:value.pbr) {
        const auto text=data.text();
        map=std::filesystem::path(std::u8string(text.begin(),text.end()));
      }
      m_materials[id] = value;
      material(id);
      break;
    }
    case Op::Mesh: {
      remixapi_MeshInfoSurfaceTriangles surface = {};
      surface.material = handle<remixapi_MaterialHandle>(data.read<std::uint64_t>());
      surface.vertices_count = data.read<std::uint32_t>();
      surface.indices_count = data.read<std::uint32_t>();
      std::vector<remixapi_HardcodedVertex> vertices(surface.vertices_count);
      std::vector<std::uint32_t> indices(surface.indices_count);
      for (auto &vertex : vertices) {
        for (float &position : vertex.position) position = data.read<float>();
        vertex.texcoord[0] = data.read<float>(); vertex.texcoord[1] = data.read<float>();
        vertex.color = data.read<std::uint32_t>();
        // The streamed xyz/uv/color format has no authored normals. Leave the
        // zero normal so Remix derives each triangle's geometric normal. An
        // invented +Y normal makes vertical faces change lighting at the
        // camera horizon when the renderer rejects backfacing shading normals.
      }
      for (auto &index : indices) index = data.read<std::uint32_t>();
      surface.vertices_values = vertices.data(); surface.indices_values = indices.data();
      remixapi_MeshInfo info = {};
      info.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
      info.hash = id; info.surfaces_count = 1; info.surfaces_values = &surface;
      remixapi_MeshHandle result;
      check(m_api.CreateMesh(&info, &result));
      break;
    }
    case Op::DestroyTexture:
      check(m_api.DestroyTexture(handle<remixapi_TextureHandle>(id)));
      check(m_api.FreeUITexture(id));
      break;
    case Op::DestroyMaterial:
      check(m_api.DestroyMaterial(handle<remixapi_MaterialHandle>(id)));
      m_materials.erase(id);
      break;
    case Op::DestroyMesh: check(m_api.DestroyMesh(handle<remixapi_MeshHandle>(id))); break;
    default: throw std::invalid_argument("Unexpected resource opcode");
    }
  }
}

void Runtime::framePacket(const Bytes &bytes) {
  perf::recordCount(perf::Side::Native, "direct.frameBytes", bytes.size());
  Reader stream(bytes);
  stream.header();
  check(m_api.SubmitUIDrawList(nullptr));
  remixapi_InstanceRasterStateEXT rasterState = {};
  rasterState.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_RASTER_STATE_EXT;
  bool hasRasterState = false;
  while (stream.remaining()) {
    const auto op = static_cast<Op>(stream.read<std::uint32_t>());
    auto data = stream.slice(stream.read<std::uint32_t>());
    switch (op) {
    case Op::Camera: {
      remixapi_CameraInfo info = {};
      info.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO;
      info.type = static_cast<remixapi_CameraType>(data.read<std::uint32_t>());
      for (auto &column : info.view) for (float &v : column) v = data.read<float>();
      for (auto &column : info.projection) for (float &v : column) v = data.read<float>();
      if (info.type == REMIXAPI_CAMERA_TYPE_WORLD) std::memcpy(m_projection.data(), info.projection, sizeof(info.projection));
      remixapi_CameraTemporalEXT temporal = {};
      temporal.sType = REMIXAPI_STRUCT_TYPE_CAMERA_TEMPORAL_EXT;
      temporal.worldEpoch = data.read<std::uint64_t>();
      temporal.historyResetEpoch = data.read<std::uint64_t>();
      remixapi_CameraEnvironmentEXT environment{};
      environment.sType=REMIXAPI_STRUCT_TYPE_CAMERA_ENVIRONMENT_EXT;
      environment.enabled=data.read<std::uint32_t>();
      environment.radiance={data.read<float>(),data.read<float>(),data.read<float>()};
      remixapi_LightInfoDistantEXT sun{};
      sun.sType=REMIXAPI_STRUCT_TYPE_LIGHT_INFO_DISTANT_EXT;
      sun.angularDiameterDegrees=.533f;
      sun.direction={data.read<float>(),data.read<float>(),data.read<float>()};
      remixapi_LightInfo sunlight{};
      sunlight.sType=REMIXAPI_STRUCT_TYPE_LIGHT_INFO;sunlight.pNext=&sun;
      sunlight.hash=0x6fffffffffffffffull;
      sunlight.radiance={data.read<float>(),data.read<float>(),data.read<float>()};
      if(!environment.enabled)sunlight.radiance={};
      if(info.type==REMIXAPI_CAMERA_TYPE_WORLD) {
        temporal.pNext=&environment;
        check(m_api.UpdateLightDefinition(m_daylight,&sunlight));
      }
      info.pNext = &temporal;
      check(m_api.SetupCamera(&info));
      break;
    }
    case Op::Draw: {
      remixapi_InstanceInfo info = {};
      info.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
      info.mesh = handle<remixapi_MeshHandle>(data.read<std::uint64_t>());
      remixapi_InstanceIdentityEXT identity = {};
      identity.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_IDENTITY_EXT;
      identity.instanceId = data.read<std::uint64_t>();
      identity.classification = data.read<std::uint32_t>(); identity.rasterVisible = data.read<std::uint32_t>();
      identity.pNext = hasRasterState ? &rasterState : nullptr;
      for (auto &row : info.transform.matrix) for (float &v : row) v = data.read<float>();
      info.categoryFlags = data.read<std::uint32_t>();
      info.doubleSided = data.read<std::uint32_t>();
      remixapi_InstanceInfoBlendEXT blend = {};
      blend.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
      blend.pNext = &identity;
      blend.tFactor = data.read<std::uint32_t>();
      blend.alphaTestCompareOp = data.read<std::uint32_t>();
      blend.alphaTestReferenceValue = data.read<std::uint32_t>();
      blend.alphaTestEnabled = blend.alphaTestCompareOp != 7;
      blend.alphaBlendEnabled = data.read<std::uint32_t>();
      blend.srcColorBlendFactor = blend.srcAlphaBlendFactor = data.read<std::uint32_t>();
      blend.dstColorBlendFactor = blend.dstAlphaBlendFactor = data.read<std::uint32_t>();
      const bool vertexColor = data.read<std::uint32_t>() != 0;
      blend.textureColorArg1Source = blend.textureAlphaArg1Source = 1; // texture
      blend.textureColorArg2Source = blend.textureAlphaArg2Source = vertexColor ? 2 : 3;
      blend.textureColorOperation = blend.textureAlphaOperation = 3; // modulation
      info.pNext = &blend;
      check(m_api.DrawInstance(&info));
      break;
    }
    case Op::Ui: {
      remixapi_UIDrawList info = {};
      info.sType = REMIXAPI_STRUCT_TYPE_UI_DRAW_LIST;
      info.displayWidth = data.read<std::uint32_t>(); info.displayHeight = data.read<std::uint32_t>();
      info.vertexCount = data.read<std::uint32_t>(); info.indexCount = data.read<std::uint32_t>();
      info.commandCount = data.read<std::uint32_t>();
      std::vector<remixapi_UIVertex> vertices(info.vertexCount);
      std::vector<std::uint32_t> indices(info.indexCount);
      std::vector<remixapi_UIDrawCommand> commands(info.commandCount);
      for (auto &vertex : vertices) {
        vertex.x = data.read<float>(); vertex.y = data.read<float>(); vertex.z = data.read<float>();
        vertex.u = data.read<float>(); vertex.v = data.read<float>(); vertex.color = data.read<std::uint32_t>();
      }
      for (auto &index : indices) index = data.read<std::uint32_t>();
      for (auto &cmd : commands) {
        cmd.textureId = data.read<std::uint64_t>(); cmd.indexCount = data.read<std::uint32_t>();
        cmd.indexOffset = data.read<std::uint32_t>(); cmd.vertexOffset = data.read<std::int32_t>();
        cmd.flags = data.read<std::uint32_t>();
      }
      info.pVertices = vertices.data(); info.pIndices = indices.data(); info.pCommands = commands.data();
      check(m_api.SubmitUIDrawList(&info));
      break;
    }
    case Op::TerrainView:
      for (auto &v : m_origin) v = data.read<double>();
      for (auto &v : m_floatingOrigin) v = data.read<double>();
      for (auto &v : m_view) v = data.read<float>();
      break;
    case Op::RasterState:
      rasterState.depthTest = data.read<std::uint32_t>();
      rasterState.depthWrite = data.read<std::uint32_t>();
      rasterState.depthCompare = data.read<std::uint32_t>();
      rasterState.disableColorWrites = data.read<std::uint32_t>();
      hasRasterState = true;
      break;
    default: throw std::invalid_argument("Unexpected frame opcode");
    }
  }
}

bool Runtime::terrainPublish() {
  m_terrain.pump(m_origin);
  bool changed = false;
  for (const auto &key : m_terrain.takeResidencyChanges()) {
    const auto found = m_chunks.find(key);
    if (found != m_chunks.end() && found->second.lifetime != m_terrain.lifetime(key)) {
#if MCRTX_HAS_RETAINED_INSTANCES
      releaseTerrain(found->second); // Before the meshes it names.
#endif
      for (const auto mesh : found->second.handles) if (mesh) check(m_api.DestroyMesh(mesh));
      m_chunks.erase(found);
      changed = true;
    }
  }
  const auto started = std::chrono::steady_clock::now();
  std::size_t bytes = 0;
  for (unsigned count = 0; count < 4 && bytes < 8 * 1024 * 1024; ++count) {
    auto result = m_terrain.takeCompleted();
    if (!result) break;
    if (result->failed) { m_terrain.retry(std::move(result)); break; }
    TerrainMesh next;
    next.lifetime = result->inputs.lifetime;
    next.world = result->inputs.world;
    const auto previous = m_chunks.find(result->inputs.key);
    const bool sameLifetime = previous != m_chunks.end() && previous->second.lifetime == next.lifetime &&
                              previous->second.world == next.world;
    for (const auto& source : result->torches) {
      std::uint64_t id = 0;
      if (sameLifetime) {
        const auto& old = previous->second.torches;
        const auto found = std::lower_bound(old.begin(), old.end(), source.cell,
            [](const Torch& torch, std::uint32_t cell) { return torch.source.cell < cell; });
        if (found != old.end() && found->source.cell == source.cell) id = found->id;
      }
      next.torches.push_back({source, id ? id : m_nextTerrainId++});
    }
    std::array<bool, 2> reused = {};
    for (unsigned pass = 0; pass < 2; ++pass) {
      next.fingerprints[pass] = result->passes[pass].fingerprint;
      next.identities[pass] = previous != m_chunks.end() && previous->second.lifetime == next.lifetime && previous->second.world == next.world
        ? previous->second.identities[pass] : m_nextTerrainId++;
      if (previous != m_chunks.end() && previous->second.lifetime == next.lifetime &&
          previous->second.fingerprints[pass] == next.fingerprints[pass]) {
        next.handles[pass] = previous->second.handles[pass];
        reused[pass] = true;
        continue;
      }
      std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
      for (const auto &source : result->passes[pass].surfacesToBuild) {
        if (source.indices.empty()) continue;
        // IDs 1..8 are diffuse classes; emissive variants are immutable per block/light level.
        const std::uint64_t materialId=source.emissionKey?0x6000000000000000ull+source.emissionKey*8+source.materialClass:1+source.materialClass;
        if (!m_materials.count(materialId)) {
          for (unsigned p = 0; p < 2; ++p) if (next.handles[p] && !reused[p]) check(m_api.DestroyMesh(next.handles[p]));
          m_terrain.retry(std::move(result));
          return changed;
        }
        remixapi_MeshInfoSurfaceTriangles surface = {};
        surface.vertices_values = source.vertices.data(); surface.vertices_count = source.vertices.size();
        surface.indices_values = source.indices.data(); surface.indices_count = source.indices.size();
        surface.material = handle<remixapi_MaterialHandle>(materialId);
        surfaces.push_back(surface);
      }
      next.fingerprints[pass] = result->passes[pass].fingerprint;
      if (surfaces.empty()) continue;
      remixapi_MeshInfo info = {};
      info.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
      info.hash = m_nextTerrainId++;
      info.surfaces_values = surfaces.data(); info.surfaces_count = surfaces.size();
      check(m_api.CreateMesh(&info, &next.handles[pass]));
    }
    bytes += result->bytes; m_uploadBytes += result->bytes;
    bool committed = m_terrain.finish(*result, [&] {
      auto &previous = m_chunks[result->inputs.key];
      // A rebuilt section keeps its retained instances (and so its temporal
      // identity) exactly when it keeps its identities above.
      if (sameLifetime) {
        next.retained = std::exchange(previous.retained, {});
        next.retainedMeshes = previous.retainedMeshes;
        next.visible = previous.visible;
      }
      std::swap(previous, next);
    });
#if MCRTX_HAS_RETAINED_INSTANCES
    // Point retained instances at the new meshes, and retire the previous
    // section's own ones, before any mesh they name is destroyed.
    if (committed && m_useRetained) retainTerrain(result->inputs.key, m_chunks[result->inputs.key]);
    if (committed) releaseTerrain(next);
#endif
    // Destroy discarded allocations or the previous committed section after swapping.
    for (unsigned p = 0; p < 2; ++p) if (next.handles[p] && !reused[p]) check(m_api.DestroyMesh(next.handles[p]));
    changed |= committed;
    if (std::chrono::steady_clock::now() - started > std::chrono::milliseconds(1)) break;
  }
  return changed;
}

remixapi_Transform Runtime::terrainTransform(const ChunkKey &key) const {
  remixapi_Transform transform = {};
  const double absolute[3] = {double(key.originX), double(key.originY), double(key.originZ)};
  for (unsigned row = 0; row < 3; ++row) {
    for (unsigned col = 0; col < 3; ++col) transform.matrix[row][col] = row == col ? 1.f : 0.f;
    transform.matrix[row][3] = float(absolute[row] - m_floatingOrigin[row]);
  }
  return transform;
}

void Runtime::terrainBlend(remixapi_InstanceInfoBlendEXT &blend, unsigned pass) {
  blend.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO_BLEND_EXT;
  blend.alphaTestEnabled = 1; blend.alphaTestCompareOp = 4; blend.alphaTestReferenceValue = 1;
  blend.srcColorBlendFactor = blend.srcAlphaBlendFactor = 6;
  blend.dstColorBlendFactor = blend.dstAlphaBlendFactor = 7;
  blend.alphaBlendEnabled = pass != 0;
}

bool Runtime::rasterBackend() const {
  if (!m_backendCaps) return false;
  remixapi_BackendCapabilitiesEXT capabilities = {};
  capabilities.structSize = sizeof(capabilities);
  return m_backendCaps(&capabilities) == REMIXAPI_ERROR_CODE_SUCCESS && capabilities.activeBackend == 0;
}

#if MCRTX_HAS_RETAINED_INSTANCES
void Runtime::retainTerrain(const ChunkKey &key, TerrainMesh &chunk) {
  for (unsigned pass = 0; pass < 2; ++pass) {
    auto &handle = chunk.retained[pass];
    const auto mesh = chunk.handles[pass];
    if (!mesh) {
      if (handle) check(m_destroyRetained(&handle, 1));
      handle = 0;
      chunk.retainedMeshes[pass] = nullptr;
      continue;
    }
    if (handle && chunk.retainedMeshes[pass] == mesh) continue;
    // No InstanceIdentityEXT: the runtime classifies the chunk static and keeps
    // its temporal identity with the handle, across mesh replacements.
    remixapi_InstanceInfoBlendEXT blend = {};
    terrainBlend(blend, pass);
    remixapi_InstanceInfo info = {};
    info.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    info.pNext = &blend;
    info.mesh = mesh;
    info.transform = terrainTransform(key);
    info.doubleSided = 1;
    if (handle) check(m_updateRetained(handle, &info)); // Keeps its visibility.
    else {
      check(m_createRetained(&info, &handle));
      if (!chunk.visible) {
        remixapi_RetainedInstanceUpdate hidden = {};
        hidden.handle = handle;
        hidden.flags = REMIXAPI_RETAINED_UPDATE_VISIBILITY;
        check(m_updateRetainedMany(&hidden, 1));
      }
    }
    chunk.retainedMeshes[pass] = mesh;
  }
}

void Runtime::releaseTerrain(TerrainMesh &chunk) {
  std::uint64_t handles[2];
  unsigned count = 0;
  for (auto &handle : chunk.retained)
    if (handle) handles[count++] = std::exchange(handle, 0);
  chunk.retainedMeshes = {};
  if (count) check(m_destroyRetained(handles, count));
}
#endif

void Runtime::drawTerrain() {
  for (auto& [id, light] : m_torchLights) light.seen = false;
#if MCRTX_HAS_RETAINED_INSTANCES
  // Retained chunks: frustum culling hides raster-only terrain. Traced terrain
  // must stay resident, and raster skips traced draws while tracing anyway.
  const bool cullRetained = m_useRetained && rasterBackend();
  const bool shifted = m_useRetained && m_floatingOrigin != m_retainedOrigin;
  std::vector<remixapi_RetainedInstanceUpdate> updates;
#endif
  for (auto &[key, chunk] : m_chunks) {
    // Submit all resident lights, independent of the raster frustum. API changes
    // happen only here, after terrainPublish has accepted a complete section.
    for (const auto& torch : chunk.torches) {
      auto& light = m_torchLights[torch.id];
      const std::array<float, 3> position = {
        float(double(key.originX) + torch.source.position[0] - m_floatingOrigin[0]),
        float(double(key.originY) + torch.source.position[1] - m_floatingOrigin[1]),
        float(double(key.originZ) + torch.source.position[2] - m_floatingOrigin[2])};
      if (!light.handle || light.position != position || light.radiance != torch.source.radiance || light.radius != torch.source.radius) {
        remixapi_LightInfoSphereEXT sphere{};
        sphere.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
        sphere.position = {position[0], position[1], position[2]};
        sphere.radius = torch.source.radius;
        remixapi_LightInfo info{};
        info.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
        info.pNext = &sphere;
        info.hash = torch.id;
        info.radiance = {torch.source.radiance[0], torch.source.radiance[1], torch.source.radiance[2]};
        if (light.handle) check(m_api.UpdateLightDefinition(light.handle, &info));
        else check(m_api.CreateLight(&info, &light.handle));
        light.position = position;
        light.radiance = torch.source.radiance;
        light.radius = torch.source.radius;
      }
      check(m_api.DrawLightInstance(light.handle));
      light.seen = true;
    }
    remixapi_InstanceInfo draw = {};
    draw.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    const double position[3] = {key.originX - m_origin[0], key.originY - m_origin[1], key.originZ - m_origin[2]};
    for (unsigned row = 0; row < 3; ++row) {
      for (unsigned col = 0; col < 3; ++col) draw.transform.matrix[row][col] = m_view[col * 4 + row];
      draw.transform.matrix[row][3] = static_cast<float>(m_view[12 + row] +
        m_view[row] * position[0] + m_view[4 + row] * position[1] + m_view[8 + row] * position[2]);
    }
    unsigned outside = 63;
    for (unsigned corner = 0; corner < 8; ++corner) {
      float view[4] = {0, 0, 0, 1}, clip[4] = {};
      for (unsigned row = 0; row < 3; ++row) {
        view[row] = draw.transform.matrix[row][3];
        for (unsigned col = 0; col < 3; ++col)
          view[row] += draw.transform.matrix[row][col] * ((corner & (1u << col)) ? 16.5f : -.5f);
      }
      for (unsigned row = 0; row < 4; ++row)
        for (unsigned col = 0; col < 4; ++col) clip[row] += m_projection[col * 4 + row] * view[col];
      outside &= unsigned(clip[0] < -clip[3]) | unsigned(clip[0] > clip[3]) << 1 |
          unsigned(clip[1] < -clip[3]) << 2 | unsigned(clip[1] > clip[3]) << 3 |
          unsigned(clip[2] < 0) << 4 | unsigned(clip[2] > clip[3]) << 5;
    }
#if MCRTX_HAS_RETAINED_INSTANCES
    if (m_useRetained) {
      const bool visible = !cullRetained || !outside;
      const auto transform = terrainTransform(key);
      for (const auto handle : chunk.retained) {
        if (!handle) continue;
        remixapi_RetainedInstanceUpdate update = {};
        update.handle = handle;
        if (shifted) {
          update.flags |= REMIXAPI_RETAINED_UPDATE_TRANSFORM;
          update.transform = transform;
        }
        if (visible != chunk.visible) {
          update.flags |= REMIXAPI_RETAINED_UPDATE_VISIBILITY;
          update.visible = visible;
        }
        if (update.flags) updates.push_back(update);
      }
      chunk.visible = visible;
      continue;
    }
#endif
    // Residency supplies all traced terrain; visibility only controls raster draws.
    remixapi_InstanceIdentityEXT identity = {};
    identity.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_IDENTITY_EXT;
    identity.classification = REMIXAPI_INSTANCE_STATIC_EXT; identity.rasterVisible = !outside;
    draw.transform = terrainTransform(key);
    draw.doubleSided = 1;
    remixapi_InstanceInfoBlendEXT blend = {};
    draw.pNext = &blend;
    for (unsigned pass = 0; pass < 2; ++pass) {
      terrainBlend(blend, pass);
      blend.pNext = &identity;
      draw.mesh = chunk.handles[pass];
      identity.instanceId = chunk.identities[pass];
      if (draw.mesh) check(m_api.DrawInstance(&draw));
    }
  }
#if MCRTX_HAS_RETAINED_INSTANCES
  if (m_useRetained) {
    if (!updates.empty()) check(m_updateRetainedMany(updates.data(), std::uint32_t(updates.size())));
    m_retainedOrigin = m_floatingOrigin;
    check(m_drawRetained(nullptr));
  }
#endif
  for (auto it = m_torchLights.begin(); it != m_torchLights.end();) {
    if (!it->second.seen) {
      check(m_api.DestroyLight(it->second.handle));
      it = m_torchLights.erase(it);
    } else ++it;
  }
}
}
