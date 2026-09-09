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
  bool events();
  void setError(const std::string &message);
  void material(std::uint64_t id);
  static void check(remixapi_ErrorCode error);
  struct Material {
    std::uint64_t texture;
    std::uint32_t filter, wrapU, wrapV;
  };
  struct TerrainMesh {
    std::array<remixapi_MeshHandle, 2> handles {};
    std::array<std::uint64_t, 2> fingerprints {};
    std::uint64_t lifetime = 0, world = 0;
    std::array<std::uint64_t, 2> identities {};
  };
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
  remixapi_LightHandle m_daylight = nullptr;
  std::unordered_map<std::uint64_t, Material> m_materials;
  std::unordered_map<ChunkKey, TerrainMesh, ChunkKeyHash> m_chunks;
  TerrainPipeline m_terrain;
  std::array<double, 3> m_origin {}, m_floatingOrigin {};
  std::array<float, 16> m_view {};
  std::array<float, 16> m_projection {};
  std::shared_ptr<const Bytes> m_lastFrame;
  std::uint64_t m_nextTerrainId = 0x7000000000000000ull;
  std::atomic<std::uint64_t> m_presented {0}, m_fresh {0};
  std::uint64_t m_lastSequence = 0;
  std::uint64_t m_pendingSequence = 0, m_uploadBytes = 0;
};
}
