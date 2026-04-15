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

struct ChunkBlockCell {
  std::array<std::uint8_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::uint8_t blockId {0};
  std::uint8_t blockMetadata {0};
  std::uint8_t renderType {0};
  std::uint8_t liquidVisibilityMask {0x3F};
  std::array<float, 4> liquidHeights {1.0f, 1.0f, 1.0f, 1.0f};
  float liquidFlowAngle {-1000.0f};
  std::uint32_t blockColor {0x00FFFFFFu};
};

struct CapturedBlockInstance {
  int position[3] {0, 0, 0};
  int blockId {0};
  int blockMetadata {0};
  int renderType {0};
  std::array<std::uint8_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::uint8_t liquidVisibilityMask {0x3F};
  std::array<float, 4> liquidHeights {1.0f, 1.0f, 1.0f, 1.0f};
  float liquidFlowAngle {-1000.0f};
  std::uint32_t blockColor {0x00FFFFFFu};
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

struct WorldBlockPosition {
  int x {0};
  int y {0};
  int z {0};

  bool operator==(const WorldBlockPosition& other) const noexcept {
    return x == other.x && y == other.y && z == other.z;
  }
};

struct WorldBlockPositionHash {
  std::size_t operator()(const WorldBlockPosition& position) const noexcept;
};

struct TorchLightPlacement {
  WorldBlockPosition blockPosition {};
  float lightX {0.0f};
  float lightY {0.0f};
  float lightZ {0.0f};
};

struct ChunkMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t geometryFingerprint {0};
  std::size_t blockCount {0};
  std::array<std::uint8_t, 4096> occupancy {};
  std::array<ChunkBlockCell, 4096> cells {};
  std::vector<TorchLightPlacement> torchLights {};
  bool hasOccupancy {false};
};

struct DynamicEntityQuad {
  std::array<float, 12> positions {};
  std::array<float, 8> texcoords {};
  std::uint32_t color {0xFFFFFFFFu};
  std::string texturePath {};
};

struct DynamicEntityBuildState {
  int entityId {-1};
  std::string currentTexturePath {};
  std::vector<DynamicEntityQuad> quads {};
  bool active {false};
};

struct DynamicEntityMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::size_t quadCount {0};
};

class RemixRenderer {
public:
  static RemixRenderer& instance();

  bool initialize(HWND sourceHwnd, std::uint32_t width, std::uint32_t height, std::filesystem::path remixDllPath = {});
  void shutdown();

  void resize(std::uint32_t width, std::uint32_t height);
  void updateCamera(const CameraState& camera);
  void updateCloudLayer(
      bool fancy,
      float cameraX,
      float cameraY,
      float cameraZ,
      float cloudHeight,
      float cloudScroll,
      float colorR,
      float colorG,
      float colorB);
  void clearCloudLayer();
  void beginDynamicEntityFrame();
  void beginDynamicEntity(int entityId);
  void setDynamicEntityTexture(const std::string& texturePath);
  void captureDynamicEntityQuad(
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
      std::uint32_t colorRgba);
  void endDynamicEntity();
  bool beginChunkBuild(int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ, int renderPass);
  void captureBlock(
      int blockX,
      int blockY,
      int blockZ,
      int blockId,
      int blockMetadata,
      int renderType,
      int texture0,
      int texture1,
      int texture2,
      int texture3,
      int texture4,
      int texture5,
      int blockColorRgb,
      int liquidVisibilityMask,
      float liquidHeight0,
      float liquidHeight1,
      float liquidHeight2,
      float liquidHeight3,
      float liquidFlowAngle);
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
  bool initializeTerrainMaterials();
  void destroyTerrainMaterials();
  void resetLoadedRemix();
  bool startup(HWND hwnd);
  remixapi_MaterialHandle acquireDynamicEntityMaterial(const std::string& texturePath);
  bool createTorchLight(const TorchLightPlacement& placement);
  bool updateTorchLight(const TorchLightPlacement& placement);
  bool reconcileChunkTorchLights(ChunkMeshData& meshData, const std::vector<TorchLightPlacement>& desiredTorchLights);
  bool rebuildCloudMesh(
      bool fancy,
      float cameraX,
      float cameraY,
      float cameraZ,
      float cloudHeight,
      float cloudScroll,
      float colorR,
      float colorG,
      float colorB);
  bool rebuildDynamicEntityMesh(int entityId, const std::vector<DynamicEntityQuad>& quads);
  bool rebuildChunkMesh(const ChunkKey& chunkKey, const std::vector<CapturedBlockInstance>& blocks, ChunkMeshData& meshData);
  bool rebuildChunkMeshFromData(const ChunkKey& chunkKey, ChunkMeshData& meshData, bool forceRebuild);
  static std::filesystem::path resolveCloudTexturePath();
  static std::filesystem::path resolveDynamicEntityTexturePath(const std::string& texturePath);
  static std::filesystem::path resolveTerrainAtlasPath();
  void destroyCloudMesh();
  void destroyChunkMeshHandle(ChunkMeshData& meshData);
  void destroyChunkTorchLights(ChunkMeshData& meshData);
  void destroyTorchLight(const WorldBlockPosition& position);
  void destroyDynamicEntityMeshes();
  void destroyDynamicEntityMesh(DynamicEntityMeshData& meshData);
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
  std::size_t lastSubmittedCloudQuadCount_ {0};
  std::size_t lastSubmittedDynamicEntityQuadCount_ {0};
  std::size_t lastSubmittedTorchLightCount_ {0};
  std::filesystem::path terrainAtlasPath_ {};
  std::filesystem::path cloudTexturePath_ {};
  std::array<remixapi_MaterialHandle, 3> terrainMaterialHandles_ {};
  remixapi_MaterialHandle cloudMaterialHandle_ {nullptr};
  remixapi_MeshHandle cloudMeshHandle_ {nullptr};
  std::uint64_t nextCloudMeshHash_ {1};
  std::uint64_t nextDynamicEntityMeshHash_ {1};
  std::size_t cloudQuadCount_ {0};
  DynamicEntityBuildState activeDynamicEntity_ {};
  std::unordered_map<int, DynamicEntityMeshData> dynamicEntityMeshes_ {};
  std::unordered_map<std::string, remixapi_MaterialHandle> dynamicEntityMaterialHandles_ {};
  std::unordered_map<ChunkKey, ChunkMeshData, ChunkKeyHash> chunkMeshes_ {};
  std::unordered_map<WorldBlockPosition, remixapi_LightHandle, WorldBlockPositionHash> torchLights_ {};
  std::string lastError_;
};

}  // namespace mcrtx
