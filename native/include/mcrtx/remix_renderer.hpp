#pragma once

#include <array>
#include <cstddef>
#include <cstdint>
#include <filesystem>
#include <mutex>
#include <string>
#include <unordered_map>
#include <vector>

#include <windows.h>

#include <remix/remix_c.h>

namespace mcrtx {

struct CameraState {
  float position[3] {0.0f, 0.0f, 0.0f};
  float forward[3] {0.0f, 0.0f, 1.0f};
  float up[3] {0.0f, 1.0f, 0.0f};
  float right[3] {1.0f, 0.0f, 0.0f};
  float fovYDegrees {70.0f};
  float aspect {16.0f / 9.0f};
  float nearPlane {0.05f};
  float farPlane {1024.0f};
};

struct ChunkBuildState {
  int origin[3] {0, 0, 0};
  int size[3] {0, 0, 0};
  int renderPass {0};
  std::uint64_t blockCount {0};
  std::array<std::uint32_t, 256> blockIdCounts {};
};

struct CapturedBlockInstance {
  int position[3] {0, 0, 0};
  int blockId {0};
  int blockMetadata {0};
};

struct ChunkKey {
  int originX {0};
  int originY {0};
  int originZ {0};
  int renderPass {0};

  bool operator==(const ChunkKey& other) const noexcept {
    return originX == other.originX
        && originY == other.originY
        && originZ == other.originZ
        && renderPass == other.renderPass;
  }
};

struct ChunkKeyHash {
  std::size_t operator()(const ChunkKey& key) const noexcept;
};

struct ChunkMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t geometryFingerprint {0};
  std::size_t blockCount {0};
  std::array<std::uint8_t, 4096> occupancy {};
  bool hasOccupancy {false};
};

class RemixRenderer {
public:
  static RemixRenderer& instance();

  bool initialize(HWND sourceHwnd, std::uint32_t width, std::uint32_t height, std::filesystem::path remixDllPath = {});
  void shutdown();

  void resize(std::uint32_t width, std::uint32_t height);
  void updateCamera(const CameraState& camera);
  bool beginChunkBuild(int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ, int renderPass);
  void captureBlock(int blockX, int blockY, int blockZ, int blockId, int blockMetadata, int renderType);
  void endChunkBuild(bool emittedGeometry);
  bool present();

  bool isInitialized() const;
  std::string lastError() const;

private:
  RemixRenderer() = default;
  ~RemixRenderer() = default;
  RemixRenderer(const RemixRenderer&) = delete;
  RemixRenderer& operator=(const RemixRenderer&) = delete;

  bool loadRemix(const std::filesystem::path& remixDllPath);
  static std::filesystem::path resolveRemixDllPath();
  bool createOutputWindow(HWND sourceHwnd);
  void destroyOutputWindow();
  void pumpOutputWindowMessages();
  void updateOutputWindowSize() const;
  void resetLoadedRemix();
  bool startup(HWND hwnd);
  bool rebuildChunkMesh(const ChunkKey& chunkKey, const std::vector<CapturedBlockInstance>& blocks, ChunkMeshData& meshData);
  bool rebuildChunkMeshFromData(const ChunkKey& chunkKey, ChunkMeshData& meshData, bool forceRebuild);
  void destroyChunkMesh(ChunkMeshData& meshData);
  void refreshNeighborChunkMeshes(const ChunkKey& chunkKey);
  bool drawCapturedGeometry();
  bool submitCamera();
  void setError(std::string message);
  static void log(const std::string& message);

  mutable std::mutex mutex_;
  remixapi_Interface remix_ {};
  HMODULE remixDll_ {nullptr};
  HWND sourceHwnd_ {nullptr};
  HWND outputHwnd_ {nullptr};
  bool initialized_ {false};
  std::uint32_t width_ {1};
  std::uint32_t height_ {1};
  CameraState camera_ {};
  bool chunkBuildActive_ {false};
  ChunkBuildState activeChunkBuild_ {};
  std::vector<CapturedBlockInstance> activeChunkBlocks_ {};
  std::uint64_t capturedChunkBuilds_ {0};
  std::uint64_t capturedBlocks_ {0};
  std::uint64_t nextChunkMeshHash_ {1};
  std::uint64_t presentedFrames_ {0};
  std::size_t lastSubmittedChunkCount_ {0};
  std::size_t lastSubmittedBlockCount_ {0};
  std::unordered_map<ChunkKey, ChunkMeshData, ChunkKeyHash> chunkMeshes_ {};
  std::string lastError_;
};

}  // namespace mcrtx
