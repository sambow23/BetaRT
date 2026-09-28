#pragma once
#include "mcrtx/direct/protocol.hpp"
#include "mcrtx/chunks/terrain_pipeline.hpp"
#include <remix/remix_native_ext.h>
#include <atomic>
#include <filesystem>
#include <functional>
#include <string>
#include <thread>
#include <unordered_map>

namespace mcrtx::direct {
class Runtime {
public:
  static Runtime &instance();
  ~Runtime();
  bool start(const std::filesystem::path &library, unsigned width, unsigned height);
  void stop();
  bool submit(Bytes resources, Bytes frame);
  bool control(unsigned operation, int value, float x, float y, std::string text = {});
  std::string error() const;
  std::vector<std::uint32_t> poll(unsigned maxEvents);
  TerrainPipeline &terrain() { return m_terrain; }
  std::array<std::uint64_t, 4> counts() const;
private:
  void run(std::filesystem::path library, unsigned width, unsigned height);
  void resourcePacket(const Bytes &bytes);
  void framePacket(const Bytes &bytes);
  bool terrainPublish();
  void drawTerrain();
  remixapi_Transform terrainTransform(const ChunkKey &key) const;
  static void terrainBlend(remixapi_InstanceInfoBlendEXT &blend, unsigned pass);
  bool rasterBackend() const;
  bool events();
  void setError(const std::string &message);
  void material(std::uint64_t id);
  static void check(remixapi_ErrorCode error);
  struct Material {
    std::uint64_t texture;
    std::uint32_t filter, wrapU, wrapV;
    std::uint64_t emissionTexture=0;
    std::array<float,3> emission{};
    std::array<std::filesystem::path,4> pbr{};
  };
  struct Torch { TerrainTorch source; std::uint64_t id = 0; };
  struct TerrainMesh {
    std::array<remixapi_MeshHandle, 2> handles {};
    std::array<std::uint64_t, 2> fingerprints {};
    std::uint64_t lifetime = 0, world = 0;
    std::array<std::uint64_t, 2> identities {};
    std::vector<Torch> torches;
    // Retained instances (MCRTX_RETAINED_TERRAIN): one per pass, replayed by the
    // runtime each frame. The runtime derives temporal identity from the handle,
    // which lets static chunks join its persistent static set.
    std::array<std::uint64_t, 2> retained {};
    std::array<remixapi_MeshHandle, 2> retainedMeshes {};
    bool visible = true;
  };
#if MCRTX_HAS_RETAINED_INSTANCES
  void retainTerrain(const ChunkKey &key, TerrainMesh &chunk);
  void releaseTerrain(TerrainMesh &chunk);
#endif
  mutable std::mutex m_mutex;
  std::condition_variable m_started;
  std::thread m_worker;
  std::atomic<bool> m_stop {false}, m_running {false};
  bool m_ready = false;
  bool m_grabRequested = false, m_grabApplied = false, m_debugVisible = false;
  std::string m_error;
  Mailbox m_mailbox;
  std::deque<std::function<void()>> m_controls;
  std::deque<remixapi_InputEvent> m_input;
  remixapi_WindowState m_window {};
  remixapi_WindowMetrics m_metrics {};
  remixapi_Interface m_api {};
  remixapi_HMODULE m_library = nullptr;
  PFN_remixapi_PollInputEvents m_poll = nullptr;
  PFN_remixapi_GetWindowMetrics m_getMetrics = nullptr;
  PFN_remixapi_SetTextInputEnabled m_textInput = nullptr;
  PFN_remixapi_GetPresentationCount m_presentCount = nullptr;
  PFN_remixapi_GetTracingStatisticsEXT m_traceStats = nullptr;
  PFN_remixapi_GetDenoisingStatisticsEXT m_denoiseStats = nullptr;
#if MCRTX_HAS_LOCALLIGHT_STATISTICS
  PFN_remixapi_GetLocalLightStatisticsEXT m_localStats = nullptr;
#endif
#if MCRTX_HAS_LOCALLIGHTGUIDING_STATISTICS
  PFN_remixapi_GetLocalLightGuidingStatisticsEXT m_localGuidingStats = nullptr;
#endif
#if MCRTX_HAS_SHADOW_STATISTICS
  PFN_remixapi_GetShadowStatisticsEXT m_shadowStats = nullptr;
#endif
#if MCRTX_HAS_SHARC_STATISTICS
  PFN_remixapi_GetSharcStatisticsEXT m_sharcStats = nullptr;
#endif
#if MCRTX_HAS_LIGHTING_STATISTICS
  PFN_remixapi_GetLightingStatisticsEXT m_lightingStats = nullptr;
#endif
#if MCRTX_HAS_SAMPLING_STATISTICS
  PFN_remixapi_GetSamplingStatisticsEXT m_samplingStats = nullptr;
#endif
  PFN_remixapi_GetBackendCapabilitiesEXT m_backendCaps = nullptr;
#if MCRTX_HAS_RETAINED_INSTANCES
  PFN_remixapi_CreateRetainedInstance m_createRetained = nullptr;
  PFN_remixapi_UpdateRetainedInstance m_updateRetained = nullptr;
  PFN_remixapi_UpdateRetainedInstances m_updateRetainedMany = nullptr;
  PFN_remixapi_DestroyRetainedInstances m_destroyRetained = nullptr;
  PFN_remixapi_DrawRetainedInstances m_drawRetained = nullptr;
#endif
  bool m_useRetained = false;
  std::array<double, 3> m_retainedOrigin {};
  remixapi_LightHandle m_daylight = nullptr;
  std::unordered_map<std::uint64_t, Material> m_materials;
  std::unordered_map<ChunkKey, TerrainMesh, ChunkKeyHash> m_chunks;
  struct TorchLight {
    remixapi_LightHandle handle = nullptr;
    std::array<float, 3> position {}, radiance {};
    float radius = 0;
    bool seen = false;
  };
  std::unordered_map<std::uint64_t, TorchLight> m_torchLights;
  TerrainPipeline m_terrain;
  std::array<double, 3> m_origin {}, m_floatingOrigin {};
  std::array<float, 16> m_view {};
  std::array<float, 16> m_projection {};
  std::shared_ptr<const Bytes> m_lastFrame;
  std::uint64_t m_nextTerrainId = 0x7000000000000000ull;
  std::atomic<std::uint64_t> m_presented {0}, m_fresh {0};
  std::uint64_t m_lastSequence = 0;
  std::uint64_t m_pendingSequence = 0, m_uploadBytes = 0;
  // Presentation count after the last Present, and whether it has advanced
  // between Presents (asynchronous presentation).
  std::uint64_t m_observedPresentations = 0;
  bool m_asyncPresentation = false;
};
}
