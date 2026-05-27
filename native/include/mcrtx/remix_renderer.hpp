#pragma once

#include <array>
#include <chrono>
#include <condition_variable>
#include <cstddef>
#include <cstdint>
#include <filesystem>
#include <mutex>
#include <string>
#include <thread>
#include <unordered_map>
#include <unordered_set>
#include <vector>

#include <windows.h>

#include "mcrtx/tracy.hpp"

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
  int dirtyMin[3] {0, 0, 0};
  int dirtyMax[3] {0, 0, 0};
  int renderPass {0};
  std::uint64_t blockCount {0};
  std::array<std::uint32_t, 256> blockIdCounts {};
};

struct DurationPerfCounter {
  std::uint64_t sampleCount {0};
  std::uint64_t totalNanoseconds {0};
  std::uint64_t maxNanoseconds {0};

  void add(std::uint64_t nanoseconds) noexcept {
    ++sampleCount;
    totalNanoseconds += nanoseconds;
    if (nanoseconds > maxNanoseconds) {
      maxNanoseconds = nanoseconds;
    }
  }

  void reset() noexcept {
    sampleCount = 0;
    totalNanoseconds = 0;
    maxNanoseconds = 0;
  }
};

struct CountPerfCounter {
  std::uint64_t sampleCount {0};
  std::uint64_t totalCount {0};
  std::uint64_t maxCount {0};

  void add(std::uint64_t count) noexcept {
    ++sampleCount;
    totalCount += count;
    if (count > maxCount) {
      maxCount = count;
    }
  }

  void reset() noexcept {
    sampleCount = 0;
    totalCount = 0;
    maxCount = 0;
  }
};

struct NativePerfWindow {
  std::uint64_t frames {0};
  DurationPerfCounter presentLockWait {};
  DurationPerfCounter presentLockHold {};
  DurationPerfCounter outputWindowWork {};
  DurationPerfCounter cameraSubmit {};
  DurationPerfCounter geometrySubmit {};
  DurationPerfCounter remixPresent {};
  DurationPerfCounter uiStateSync {};
  DurationPerfCounter frameChunkBuildWork {};
  DurationPerfCounter frameChunkMeshRebuildWork {};
  DurationPerfCounter frameNeighborRefreshWork {};
  CountPerfCounter frameCaptureBlockCalls {};
  CountPerfCounter frameCachedChunkMeshes {};
  CountPerfCounter frameChunkBuilds {};
  CountPerfCounter frameChunkMeshRebuilds {};
  CountPerfCounter frameSubmittedChunkMeshes {};
  CountPerfCounter frameSubmittedChunkBlocks {};

  void reset() noexcept {
    frames = 0;
    presentLockWait.reset();
    presentLockHold.reset();
    outputWindowWork.reset();
    cameraSubmit.reset();
    geometrySubmit.reset();
    remixPresent.reset();
    uiStateSync.reset();
    frameChunkBuildWork.reset();
    frameChunkMeshRebuildWork.reset();
    frameNeighborRefreshWork.reset();
    frameCaptureBlockCalls.reset();
    frameCachedChunkMeshes.reset();
    frameChunkBuilds.reset();
    frameChunkMeshRebuilds.reset();
    frameSubmittedChunkMeshes.reset();
    frameSubmittedChunkBlocks.reset();
  }
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
  remixapi_Float3D radiance {0.0f, 0.0f, 0.0f};
};

struct ChunkMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t geometryFingerprint {0};
  std::uint64_t meshFingerprint {0};
  std::size_t blockCount {0};
  std::array<std::uint8_t, 4096> occupancy {};
  std::array<ChunkBlockCell, 4096> cells {};
  std::vector<std::uint16_t> fireCellIndices {};
  std::vector<TorchLightPlacement> torchLights {};
  bool hasOccupancy {false};
  bool hidden {false};
  std::array<bool, 6> faceCovered {};
};

struct DynamicEntityQuad {
  std::array<float, 12> positions {};
  std::array<float, 8> texcoords {};
  std::uint32_t color {0xFFFFFFFFu};
  std::uint32_t textureIndex {0};
  std::uint64_t textureFingerprint {0};
  bool blendEnabled {false};
  std::uint32_t boneIndex {0};
};

constexpr std::uint32_t kDynamicEntityMaxHurtStage = 10;
constexpr std::uint32_t kDynamicEntityMaxCreeperFuseStage = 10;
constexpr std::size_t kDynamicEntityMaterialVariantCount =
  (static_cast<std::size_t>(kDynamicEntityMaxHurtStage) + 1u)
  * (static_cast<std::size_t>(kDynamicEntityMaxCreeperFuseStage) + 1u)
  * 2u;

enum class DynamicEntityMaterialClass : std::uint8_t {
  Cutout = 0,
  Translucent = 1,
};

struct DynamicEntityBuildState {
  int entityId {-1};
  std::uint32_t hurtStage {0};
  std::uint32_t creeperFuseStage {0};
  std::uint32_t maxBoneCount {0};
  std::uint64_t quadFingerprint {0};
  std::uint32_t currentTextureIndex {0xFFFFFFFFu};
  std::uint64_t currentTextureFingerprint {0};
  std::vector<std::string> texturePaths {};
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
  int entityId {-1};
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

struct BlockOutlineInstance {
  int blockX {0};
  int blockY {0};
  int blockZ {0};
};

struct ParticleQuad {
  std::array<float, 12> positions {};
  std::array<float, 8> texcoords {};
  std::uint32_t color {0xFFFFFFFFu};
  std::uint32_t textureKind {0};
};

struct ChunkRenderInstance {
  ChunkKey chunkKey {};
  remixapi_MeshHandle meshHandle {nullptr};
  std::size_t blockCount {0};
};

struct FrameRenderSnapshot {
  CameraState camera {};
  std::vector<ChunkRenderInstance> chunkMeshes {};
  std::vector<DynamicEntityFrameInstance> dynamicEntities {};
  std::vector<remixapi_LightHandle> torchLights {};
  remixapi_MeshHandle cloudMeshHandle {nullptr};
  remixapi_MeshHandle fireMeshHandle {nullptr};
  remixapi_MeshHandle destroyOverlayMeshHandle {nullptr};
  remixapi_MeshHandle blockOutlineMeshHandle {nullptr};
  remixapi_MeshHandle particleMeshHandle {nullptr};
  float cloudTransformX {0.0f};
  float cloudTransformY {0.0f};
  float cloudTransformZ {0.0f};
  std::size_t cachedChunkMeshes {0};
  std::size_t submittedChunkBlocks {0};
  std::size_t submittedCloudQuads {0};
  std::size_t submittedFireQuads {0};
  std::size_t submittedDynamicEntityQuads {0};
  std::size_t submittedDynamicEntityDrawCalls {0};
  std::size_t submittedDynamicEntityFallbackDrawCalls {0};
  std::size_t submittedDynamicEntityInstancedDrawCalls {0};
  std::size_t submittedDynamicEntityInstancedTransforms {0};
  std::size_t submittedDynamicEntityRigidCandidates {0};
  std::size_t submittedDynamicEntitySingletonRigidFallbacks {0};
  std::size_t submittedDynamicEntitySkinnedFallbacks {0};
  std::size_t submittedDestroyOverlays {0};
  std::size_t submittedBlockOutlines {0};
  std::size_t submittedParticleQuads {0};
  std::size_t submittedTorchLights {0};

  bool hasScene() const noexcept {
    return !chunkMeshes.empty()
        || !dynamicEntities.empty()
        || cloudMeshHandle != nullptr
        || fireMeshHandle != nullptr
        || destroyOverlayMeshHandle != nullptr
          || blockOutlineMeshHandle != nullptr
        || particleMeshHandle != nullptr
        || !torchLights.empty();
  }
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
      float celestialAngle,
      float colorR,
      float colorG,
      float colorB);
  void updateAtmosphereState(float celestialAngle, bool forceDarkAtmosphere);
  void updateFogState(
      std::uint32_t fogMode,
      float colorR,
      float colorG,
      float colorB,
      float fogScale,
      float fogEnd,
      float fogDensity);
  void clearCloudLayer();
  void beginDynamicEntityFrame();
  void beginDynamicEntity(int entityId, std::uint32_t hurtStage, std::uint32_t creeperFuseStage);
  void setDynamicEntityTexture(const std::string& texturePath);
  void setFirstPersonHeldItem(int itemId);
  void setEntityHeldTorch(int entityId, float worldX, float worldY, float worldZ, int itemId);
  void setPlayerShadowsEnabled(bool enabled);
  void setHeldTorchLightsEnabled(bool enabled);
  void setBlockOutlineEnabled(bool enabled);
  void setBlockOutlineStyle(int style);
  void setBlockOutlineEmissiveIntensity(float intensity);
  void setViewModelFovDegrees(float fovYDegrees);
  void setRtQuality(int rtQuality);
  void setUpscalerConfig(int upscalerType, int dlssPreset, int xessPreset, int taauPreset, bool rayReconstructionEnabled);
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
      bool blendEnabled,
      std::uint32_t boneIndex);
  void endDynamicEntity();
  void beginDestroyOverlayFrame();
  void beginBlockOutlineFrame();
  void captureDestroyOverlay(
      int blockX,
      int blockY,
      int blockZ,
      int blockId,
      int blockMetadata,
      int renderType,
      int destroyStage);
  void captureBlockOutline(int blockX, int blockY, int blockZ);
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
  void unloadChunkSection(int originX, int originY, int originZ);
  void setChunkSectionHidden(int originX, int originY, int originZ, bool hidden);
  bool beginChunkBuild(
      int originX,
      int originY,
      int originZ,
      int sizeX,
      int sizeY,
      int sizeZ,
      int dirtyMinX,
      int dirtyMinY,
      int dirtyMinZ,
      int dirtyMaxX,
      int dirtyMaxY,
      int dirtyMaxZ,
      int renderPass);
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
  void endChunkBuild(bool emittedGeometry, bool deferNeighborRefresh = false, bool allowNeighborRefresh = true);
  void flushChunkNeighborRefreshes();
      void setScreenTint(float r, float g, float b, float a);
      bool drawScreenOverlay(
        const void* pixelData,
        std::uint32_t width,
        std::uint32_t height,
        remixapi_Format format,
        float opacity);
      bool clearScreenOverlay();
  remixapi_UIState getUiState() const;
  bool setUiState(remixapi_UIState state);
      bool hasWindowFocus() const;
      bool isVirtualKeyDown(std::uint32_t virtualKey) const;
      bool pollNativeMouseState(
        std::int32_t& x,
        std::int32_t& y,
        std::int32_t& deltaX,
        std::int32_t& deltaY,
        std::int32_t& dWheel,
        std::uint32_t& buttonsMask,
        std::int32_t& windowHeight);
      bool setNativeMouseGrabbed(bool grabbed);
      bool setNativeCursorPosition(std::int32_t x, std::int32_t y);
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
  bool setConfigVariableLocked(std::string_view key, const std::string& value, bool logChange, bool force = false);
  bool setConfigFloatLocked(std::string_view key, float value, int precision, bool logChange, bool force = false);
  void applyRtQualityConfigLocked();
  void applyUpscalerConfigLocked();
  void applyRemixConfigPreStartupLocked();
  void applyRemixConfigPostStartupLocked();
  void updateAtmosphereConfigLocked(float celestialAngle, bool forceDarkAtmosphere);
  bool hasWindowFocusLocked() const;
  HWND resolveNativeMouseWindowLocked() const;
  bool getNativeMouseClientRectLocked(HWND mouseWindow, RECT& clientRect, RECT& clientRectScreenSpace) const;
  void releaseNativeMouseGrabLocked(HWND mouseWindow);
  bool applyNativeMouseGrabLocked(HWND mouseWindow, const RECT& clientRect, const RECT& clientRectScreenSpace);
  bool createOutputWindow(HWND sourceHwnd);
  void destroyOutputWindow();
  void pumpOutputWindowMessages();
  void updateOutputWindowSize();
  void syncOutputWindowInteractivity(remixapi_UIState uiState);
  bool initializeTerrainMaterials();
  void createBlockOutlineMaterials();
  void destroyTerrainMaterials();
  void destroyBlockOutlineMaterials();
  void resetLoadedRemix();
  bool startup(HWND hwnd);
  bool createPrimingMesh();
  remixapi_MaterialHandle acquireDynamicEntityMaterial(
      const std::string& texturePath,
      DynamicEntityMaterialClass materialClass,
      std::uint32_t hurtStage,
      std::uint32_t creeperFuseStage);
  remixapi_MaterialHandle acquireParticleMaterial(std::uint32_t textureKind);
  bool createTorchLight(const TorchLightPlacement& placement);
  bool updateTorchLight(const TorchLightPlacement& placement);
  bool reconcileChunkTorchLights(ChunkMeshData& meshData, const std::vector<TorchLightPlacement>& desiredTorchLights);
  bool reconcileHeldItemTorchLight();
  void destroyEntityHeldTorchLight(int entityId);
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
  bool rebuildChunkMesh(
      const ChunkBuildState& chunkBuild,
      const std::vector<CapturedBlockInstance>& blocks,
      ChunkMeshData& meshData);
  bool rebuildChunkMeshFromData(const ChunkKey& chunkKey, ChunkMeshData& meshData, bool forceRebuild);
  bool rebuildFireMesh();
  bool rebuildDestroyOverlayMesh();
  bool rebuildBlockOutlineMesh();
  static std::filesystem::path resolveCloudTexturePath();
  static std::filesystem::path resolveFireTexturePath();
  static std::filesystem::path resolveWaterTexturePath();
  static std::filesystem::path resolveLavaTexturePath();
  static std::filesystem::path resolvePortalTexturePath();
  static std::filesystem::path resolveLavaEmissiveTexturePath();
  static std::filesystem::path resolveRedstoneEmissiveTexturePath();
  static std::filesystem::path resolveDynamicEntityTexturePath(const std::string& texturePath);
  static std::filesystem::path resolveParticleTexturePath(std::uint32_t textureKind);
  static std::filesystem::path resolveTerrainAtlasPath();
  static std::filesystem::path resolveTerrainEmissiveTexturePath();
  void destroyCloudMesh();
  void destroyFireMesh();
  void destroyDestroyOverlayMesh();
  void destroyBlockOutlineMesh();
  void destroyParticleMesh();
  void destroyMeshHandle(remixapi_MeshHandle& meshHandle);
  void destroyLightHandle(remixapi_LightHandle lightHandle);
  void flushDeferredDestroyQueuesLocked();
  void destroyChunkMeshHandle(ChunkMeshData& meshData);
  void destroyChunkTorchLights(ChunkMeshData& meshData);
  void destroyTorchLight(const WorldBlockPosition& position);
  void destroyHeldItemTorchLight();
  void clearHeldTorchLightsLocked();
  void clearDynamicEntityFrameInstances();
  void destroyDynamicEntityMeshes();
  void destroyDynamicEntityMesh(DynamicEntityMeshData& meshData);
  void destroyChunkMesh(ChunkMeshData& meshData);
  bool rebuildParticleMesh();
  void refreshNeighborChunkMeshes(const ChunkKey& chunkKey);
  void evictDistantChunks(int cameraChunkX, int cameraChunkZ, int evictRadiusChunks);
  void computeFaceCoverage(ChunkMeshData& meshData);
  bool isChunkBuried(const ChunkKey& chunkKey) const;
  bool prepareFrameSnapshotLocked(FrameRenderSnapshot& snapshot, bool& logNoCapturedScene);
  bool drawCapturedGeometry(FrameRenderSnapshot& snapshot);
  bool submitCamera(const CameraState& camera);
  bool startStandaloneWorker(std::filesystem::path remixDllPath);
  bool initializeStandaloneWorker(std::filesystem::path remixDllPath);
  void standaloneRenderWorkerMain(std::filesystem::path remixDllPath);
  bool presentLocked(TracyUniqueLock& lock,
                     std::string& perfSummary,
                     std::uint64_t lockWaitNanoseconds);
  void resetPerFramePerfCounters() noexcept;
  void shutdownLocked();
  void setError(std::string message);
  static void log(const std::string& message);

  mutable MCRTX_TRACY_LOCKABLE_N(std::mutex, mutex_, "RemixRenderer::mutex_");
  TracyConditionVariable standaloneWorkerEvent_ {};
  std::thread standaloneWorker_ {};
  remixapi_Interface remix_ {};
  HMODULE remixDll_ {nullptr};
  HWND sourceHwnd_ {nullptr};
  HWND outputHwnd_ {nullptr};
  bool outputWindowInteractive_ {false};
  bool nativeMouseGrabbed_ {false};
  bool nativeMouseGrabActive_ {false};
  bool nativeMouseLastCursorValid_ {false};
  POINT nativeMouseLastCursorPos_ {};
  bool overlayOutputWindow_ {true};
  bool singleNativeOutputWindow_ {false};
  bool standaloneOutputWindow_ {false};
  bool standaloneWorkerActive_ {false};
  bool standaloneWorkerInitReady_ {false};
  bool standaloneWorkerStopRequested_ {false};
  bool standaloneWorkerPresentRequested_ {false};
  DWORD standaloneWorkerThreadId_ {0};
  bool renderSubmissionInFlight_ {false};
  bool initialized_ {false};
  std::uint32_t width_ {1};
  std::uint32_t height_ {1};
  CameraState camera_ {};
  float viewModelFovDegrees_ {70.0f};
  bool chunkBuildActive_ {false};
  ChunkBuildState activeChunkBuild_ {};
  std::vector<CapturedBlockInstance> activeChunkBlocks_ {};
  std::uint64_t capturedChunkBuilds_ {0};
  std::uint64_t capturedBlocks_ {0};
  std::uint64_t nextChunkMeshHash_ {1};
  std::uint64_t presentedFrames_ {0};
  NativePerfWindow perfWindow_ {};
  std::uint64_t perfCaptureBlockCallsThisFrame_ {0};
  std::uint64_t perfChunkBuildsThisFrame_ {0};
  std::uint64_t perfChunkMeshRebuildsThisFrame_ {0};
  std::uint64_t perfChunkBuildWorkNanosThisFrame_ {0};
  std::uint64_t perfChunkMeshRebuildNanosThisFrame_ {0};
  std::uint64_t perfNeighborRefreshNanosThisFrame_ {0};
  std::uint64_t perfCachedChunkMeshesThisFrame_ {0};
  std::uint64_t perfSubmittedChunkMeshesThisFrame_ {0};
  std::uint64_t perfSubmittedChunkBlocksThisFrame_ {0};
  std::size_t lastSubmittedChunkCount_ {0};
  std::size_t lastSubmittedBlockCount_ {0};
  std::size_t lastSubmittedCloudQuadCount_ {0};
  std::size_t lastSubmittedFireQuadCount_ {0};
  std::size_t lastSubmittedDynamicEntityQuadCount_ {0};
  std::size_t lastSubmittedDynamicEntityDrawCallCount_ {0};
  std::size_t lastSubmittedDynamicEntityFallbackDrawCallCount_ {0};
  std::size_t lastSubmittedDynamicEntityInstancedDrawCallCount_ {0};
  std::size_t lastSubmittedDynamicEntityInstancedTransformCount_ {0};
  std::size_t lastSubmittedDynamicEntityRigidCandidateCount_ {0};
  std::size_t lastSubmittedDynamicEntitySingletonRigidFallbackCount_ {0};
  std::size_t lastSubmittedDynamicEntitySkinnedFallbackCount_ {0};
  std::size_t lastSubmittedDestroyOverlayCount_ {0};
  std::size_t lastSubmittedBlockOutlineCount_ {0};
  std::size_t lastSubmittedParticleQuadCount_ {0};
  std::size_t lastSubmittedTorchLightCount_ {0};
  std::uint32_t loggedPopulatedSubmissionSummaryCount_ {0};
  bool loggedLightSubmissionPath_ {false};
  std::filesystem::path terrainAtlasPath_ {};
  std::filesystem::path terrainEmissiveTexturePath_ {};
  std::filesystem::path cloudTexturePath_ {};
  std::filesystem::path fireTexturePath_ {};
  std::filesystem::path waterTexturePath_ {};
  std::filesystem::path lavaTexturePath_ {};
  std::filesystem::path portalTexturePath_ {};
  std::filesystem::path lavaEmissiveTexturePath_ {};
  std::filesystem::path redstoneEmissiveTexturePath_ {};
  std::array<remixapi_MaterialHandle, 7> terrainMaterialHandles_ {};
  remixapi_MaterialHandle cloudMaterialHandle_ {nullptr};
  remixapi_MaterialHandle destroyOverlayMaterialHandle_ {nullptr};
  remixapi_MaterialHandle blockOutlineGlowMaterialHandle_ {nullptr};
  std::array<remixapi_MaterialHandle, 6> blockOutlineRgbMaterialHandles_ {};
  remixapi_MaterialHandle fireMaterialHandle_ {nullptr};
  remixapi_MeshHandle cloudMeshHandle_ {nullptr};
  remixapi_MeshHandle fireMeshHandle_ {nullptr};
  remixapi_MeshHandle destroyOverlayMeshHandle_ {nullptr};
  remixapi_MeshHandle blockOutlineMeshHandle_ {nullptr};
  std::uint64_t nextFireMeshHash_ {1};
  std::uint64_t nextDestroyOverlayMeshHash_ {1};
  std::uint64_t nextBlockOutlineMeshHash_ {1};
  std::uint64_t nextParticleMeshHash_ {1};
  std::size_t cloudQuadCount_ {0};
  bool cloudMeshFancy_ {false};
  std::int64_t cloudMeshPhaseX_ {0};
  std::int64_t cloudMeshPhaseZ_ {0};
  float cloudTransformX_ {0.0f};
  float cloudTransformY_ {0.0f};
  float cloudTransformZ_ {0.0f};
  std::size_t fireQuadCount_ {0};
  std::size_t destroyOverlayCount_ {0};
  std::size_t blockOutlineCount_ {0};
  std::size_t particleQuadCount_ {0};
  std::uint32_t lastFireAnimationFrame_ {0xFFFFFFFFu};
  std::uint64_t lastFireChunkBuildCount_ {0xFFFFFFFFFFFFFFFFull};
  DynamicEntityBuildState activeDynamicEntity_ {};
  std::unordered_map<std::uint64_t, DynamicEntityMeshData> dynamicEntityMeshes_ {};
  std::vector<DynamicEntityFrameInstance> dynamicEntityFrameInstances_ {};
  std::size_t dynamicEntityFrameInstanceCount_ {0};
  std::vector<DestroyOverlayInstance> destroyOverlayInstances_ {};
  std::vector<BlockOutlineInstance> blockOutlineInstances_ {};
  std::vector<ParticleQuad> particleQuads_ {};
  std::unordered_map<std::string, std::array<remixapi_MaterialHandle, kDynamicEntityMaterialVariantCount>> dynamicEntityMaterialHandles_ {};
  std::unordered_map<std::uint32_t, remixapi_MaterialHandle> particleMaterialHandles_ {};
  remixapi_MeshHandle particleMeshHandle_ {nullptr};
  remixapi_MeshHandle primingMeshHandle_ {nullptr};
  int evictRadiusChunks_ {20};
  std::unordered_map<ChunkKey, ChunkMeshData, ChunkKeyHash> chunkMeshes_ {};
  std::unordered_set<ChunkKey, ChunkKeyHash> pendingNeighborRefresh_ {};
  std::unordered_set<ChunkKey, ChunkKeyHash> recentlyRebuiltChunks_ {};
  std::unordered_map<WorldBlockPosition, remixapi_LightHandle, WorldBlockPositionHash> torchLights_ {};
  remixapi_LightHandle heldItemTorchLightHandle_ {nullptr};
  std::unordered_map<int, remixapi_LightHandle> entityHeldTorchLightHandles_ {};
  std::unordered_set<int> entityHeldTorchLightsSeenThisFrame_ {};
  int heldItemId_ {-1};
  bool playerShadowsEnabled_ {true};
  bool heldTorchLightsEnabled_ {true};
  bool blockOutlineEnabled_ {true};
  static constexpr int kBlockOutlineStyleSubtle = 0;
  static constexpr int kBlockOutlineStyleBold = 1;
  static constexpr int kBlockOutlineStyleSolid = 2;
  static constexpr int kBlockOutlineStyleGlow = 3;
  static constexpr int kBlockOutlineStyleRgb = 4;
  static constexpr int kBlockOutlineStyleThin = 5;
  int blockOutlineStyle_ {kBlockOutlineStyleBold};
  float blockOutlineEmissiveIntensity_ {4.5f};
  static constexpr int kRtQualityLow = 0;
  static constexpr int kRtQualityMedium = 1;
  static constexpr int kRtQualityHigh = 2;
  static constexpr int kRtQualityUltra = 3;
  static constexpr int kRtQualityPotato = 4;
  int upscalerType_ {1};
  int dlssPreset_ {4};
  int xessPreset_ {2};
  int taauPreset_ {2};
  bool rayReconstructionEnabled_ {true};
  int rtQuality_ {kRtQualityHigh};
  std::vector<remixapi_MeshHandle> deferredMeshDestroys_ {};
  std::vector<remixapi_LightHandle> deferredLightDestroys_ {};
  std::unordered_map<std::string, std::string> appliedRemixConfigValues_ {};
  bool warnedMissingSetConfigVariable_ {false};
  bool warnedMissingSetFogState_ {false};
  bool warnedMissingSetScreenTint_ {false};
  std::string lastError_;
};

}  // namespace mcrtx
