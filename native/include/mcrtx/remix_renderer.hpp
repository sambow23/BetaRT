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
  std::array<std::int16_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::uint8_t blockId {0};
  std::uint8_t blockMetadata {0};
  std::uint8_t renderType {0};
  std::array<float, 6> bounds {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
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
  std::array<std::int16_t, 6> terrainTiles {};
  std::uint8_t materialClass {0};
  std::array<float, 6> bounds {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
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
  std::uint32_t boneIndex {0};
};

struct DynamicEntityBuildState {
  int entityId {-1};
  std::string currentTexturePath {};
  std::vector<DynamicEntityQuad> quads {};
  std::vector<remixapi_Transform> boneTransforms {};
  bool active {false};
};

struct DynamicEntityMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t geometryFingerprint {0};
  std::size_t quadCount {0};
  std::uint32_t boneCount {0};
};

struct DynamicEntityFrameInstance {
  remixapi_MeshHandle meshHandle {nullptr};
  std::size_t quadCount {0};
  std::vector<remixapi_Transform> boneTransforms {};
};

struct DestroyOverlayInstance {
  int blockX {0};
  int blockY {0};
  int blockZ {0};
  int blockId {0};
  int blockMetadata {0};
  int renderType {0};
  int destroyStage {0};
};

struct ParticleQuad {
  std::array<float, 12> positions {};
  std::array<float, 8> texcoords {};
  std::uint32_t color {0xFFFFFFFFu};
  std::uint32_t textureKind {0};
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
  void setDynamicEntityBoneTransform(std::uint32_t boneIndex, const remixapi_Transform& transform);
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
        std::uint32_t colorRgba,
        std::uint32_t boneIndex);
  void endDynamicEntity();
  void beginDestroyOverlayFrame();
  void captureDestroyOverlay(
      int blockX,
      int blockY,
      int blockZ,
      int blockId,
      int blockMetadata,
      int renderType,
      int destroyStage);
  void beginParticleFrame();
  void captureParticleQuad(
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
      std::uint32_t textureKind);
  void clearWorldScene();
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
      float boundsMinX,
      float boundsMinY,
      float boundsMinZ,
      float boundsMaxX,
      float boundsMaxY,
      float boundsMaxZ,
      int blockColorRgb,
      int liquidVisibilityMask,
      float liquidHeight0,
      float liquidHeight1,
      float liquidHeight2,
      float liquidHeight3,
      float liquidFlowAngle);
  void endChunkBuild(bool emittedGeometry);
      bool drawScreenOverlay(
        const void* pixelData,
        std::uint32_t width,
        std::uint32_t height,
        remixapi_Format format,
        float opacity);
      bool clearScreenOverlay();
  remixapi_UIState getUiState() const;
  bool setUiState(remixapi_UIState state);
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
  void updateOutputWindowSize();
  void syncOutputWindowInteractivity(remixapi_UIState uiState);
  bool initializeTerrainMaterials();
  void destroyTerrainMaterials();
  void resetLoadedRemix();
  bool startup(HWND hwnd);
  remixapi_MaterialHandle acquireDynamicEntityMaterial(const std::string& texturePath);
  remixapi_MaterialHandle acquireParticleMaterial(std::uint32_t textureKind);
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
  DynamicEntityMeshData* findOrCreateDynamicEntityMesh(const DynamicEntityBuildState& buildState);
  bool rebuildChunkMesh(const ChunkKey& chunkKey, const std::vector<CapturedBlockInstance>& blocks, ChunkMeshData& meshData);
  bool rebuildChunkMeshFromData(const ChunkKey& chunkKey, ChunkMeshData& meshData, bool forceRebuild);
  bool rebuildFireMesh();
  bool rebuildDestroyOverlayMesh();
  static std::filesystem::path resolveCloudTexturePath();
  static std::filesystem::path resolveFireTexturePath();
  static std::filesystem::path resolveWaterTexturePath();
  static std::filesystem::path resolveLavaTexturePath();
  static std::filesystem::path resolveDynamicEntityTexturePath(const std::string& texturePath);
  static std::filesystem::path resolveParticleTexturePath(std::uint32_t textureKind);
  static std::filesystem::path resolveTerrainAtlasPath();
  void destroyCloudMesh();
  void destroyFireMesh();
  void destroyDestroyOverlayMesh();
  void destroyParticleMesh();
  void destroyChunkMeshHandle(ChunkMeshData& meshData);
  void destroyChunkTorchLights(ChunkMeshData& meshData);
  void destroyTorchLight(const WorldBlockPosition& position);
  void clearDynamicEntityFrameInstances();
  void destroyDynamicEntityMeshes();
  void destroyDynamicEntityMesh(DynamicEntityMeshData& meshData);
  void destroyChunkMesh(ChunkMeshData& meshData);
  bool rebuildParticleMesh();
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
  bool outputWindowInteractive_ {false};
  bool overlayOutputWindow_ {true};
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
  std::size_t lastSubmittedFireQuadCount_ {0};
  std::size_t lastSubmittedDynamicEntityQuadCount_ {0};
  std::size_t lastSubmittedDestroyOverlayCount_ {0};
  std::size_t lastSubmittedParticleQuadCount_ {0};
  std::size_t lastSubmittedTorchLightCount_ {0};
  bool loggedLightSubmissionPath_ {false};
  std::filesystem::path terrainAtlasPath_ {};
  std::filesystem::path cloudTexturePath_ {};
  std::filesystem::path fireTexturePath_ {};
  std::filesystem::path waterTexturePath_ {};
  std::filesystem::path lavaTexturePath_ {};
  std::array<remixapi_MaterialHandle, 4> terrainMaterialHandles_ {};
  remixapi_MaterialHandle cloudMaterialHandle_ {nullptr};
  remixapi_MaterialHandle fireMaterialHandle_ {nullptr};
  remixapi_MeshHandle cloudMeshHandle_ {nullptr};
  remixapi_MeshHandle fireMeshHandle_ {nullptr};
  remixapi_MeshHandle destroyOverlayMeshHandle_ {nullptr};
  std::uint64_t nextCloudMeshHash_ {1};
  std::uint64_t nextFireMeshHash_ {1};
  std::uint64_t nextDestroyOverlayMeshHash_ {1};
  std::uint64_t nextParticleMeshHash_ {1};
  std::size_t cloudQuadCount_ {0};
  std::size_t fireQuadCount_ {0};
  std::size_t destroyOverlayCount_ {0};
  std::size_t particleQuadCount_ {0};
  std::uint32_t lastFireAnimationFrame_ {0xFFFFFFFFu};
  std::uint64_t lastFireChunkBuildCount_ {0xFFFFFFFFFFFFFFFFull};
  DynamicEntityBuildState activeDynamicEntity_ {};
  std::unordered_map<std::uint64_t, DynamicEntityMeshData> dynamicEntityMeshes_ {};
  std::vector<DynamicEntityFrameInstance> dynamicEntityFrameInstances_ {};
  std::vector<DestroyOverlayInstance> destroyOverlayInstances_ {};
  std::vector<ParticleQuad> particleQuads_ {};
  std::unordered_map<std::string, remixapi_MaterialHandle> dynamicEntityMaterialHandles_ {};
  std::unordered_map<std::uint32_t, remixapi_MaterialHandle> particleMaterialHandles_ {};
  remixapi_MeshHandle particleMeshHandle_ {nullptr};
  std::unordered_map<ChunkKey, ChunkMeshData, ChunkKeyHash> chunkMeshes_ {};
  std::unordered_map<WorldBlockPosition, remixapi_LightHandle, WorldBlockPositionHash> torchLights_ {};
  std::string lastError_;
};

}  // namespace mcrtx
