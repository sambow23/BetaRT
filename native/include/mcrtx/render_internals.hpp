#pragma once

#include <array>
#include <atomic>
#include <cstddef>
#include <cstdint>
#include <filesystem>
#include <string>
#include <string_view>
#include <vector>

#include <windows.h>

#include <remix/remix_c.h>

#include <mcrtx/remix_renderer.hpp>

// Internal declarations shared across the remix_* translation units.
// Historically these lived in a per-TU anonymous namespace inside
// remix_renderer.cpp; giving them external linkage in mcrtx::detail lets the
// implementation be split into cohesive .cpp files while keeping the same
// logical scope.
namespace mcrtx {
namespace detail {

// ---- Shared constants -----------------------------------------------------

constexpr std::size_t kMaxOpaqueBlocksPerChunk = 4096;
constexpr int kChunkDimension = 16;
constexpr int kBlocksPerChunk = kChunkDimension * kChunkDimension * kChunkDimension;
constexpr std::size_t kTerrainMaterialClassCount = 3;
constexpr std::uint8_t kOpaqueTerrainMaterialClass = 0;
constexpr std::uint8_t kCutoutTerrainMaterialClass = 1;
constexpr std::uint8_t kWaterTerrainMaterialClass = 2;
constexpr std::uint8_t kCubeBlockRenderType = 0;
constexpr std::uint8_t kCrossedQuadBlockRenderType = 1;
constexpr std::uint8_t kTorchBlockRenderType = 2;
constexpr std::uint8_t kFireBlockRenderType = 3;
constexpr std::uint8_t kDoorBlockRenderType = 7;
constexpr std::uint8_t kLadderBlockRenderType = 8;
constexpr std::uint8_t kRailBlockRenderType = 9;
constexpr std::uint8_t kStairBlockRenderType = 10;
constexpr std::uint8_t kFenceBlockRenderType = 11;
constexpr std::uint8_t kLiquidBlockRenderType = 4;
constexpr std::uint8_t kGrassBlockId = 2;
constexpr std::uint8_t kLeavesBlockId = 18;
constexpr std::uint8_t kDoubleSlabBlockId = 43;
constexpr std::uint8_t kSingleSlabBlockId = 44;
constexpr std::uint8_t kTallGrassBlockId = 31;
constexpr std::uint8_t kTorchBlockId = 50;
constexpr std::uint8_t kWoodStairsBlockId = 53;
constexpr std::uint8_t kWoodDoorBlockId = 64;
constexpr std::uint8_t kLadderBlockId = 65;
constexpr std::uint8_t kStoneStairsBlockId = 67;
constexpr std::uint8_t kIronDoorBlockId = 71;
constexpr std::uint8_t kGoldenRailBlockId = 27;
constexpr std::uint8_t kDetectorRailBlockId = 28;
constexpr std::uint8_t kRailBlockId = 66;
constexpr std::uint8_t kFenceBlockId = 85;
constexpr std::uint8_t kWaterStillBlockId = 8;
constexpr std::uint8_t kWaterFlowingBlockId = 9;
constexpr std::uint8_t kGrassOverlayTerrainTile = 38;
constexpr std::uint8_t kLeavesFancyTextureOak = 52;
constexpr std::uint8_t kLeavesFastTextureOak = 53;
constexpr std::uint8_t kLeavesFancyTextureBirchSpruce = 132;
constexpr std::uint8_t kLeavesFastTextureBirchSpruce = 133;
constexpr float kAtlasSizePixels = 256.0f;
constexpr float kAtlasTileSizePixels = 16.0f;
constexpr float kAtlasUvInsetPixels = 0.01f;
constexpr float kFireAtlasWidthPixels = 256.0f;
constexpr float kFireAtlasHeightPixels = 32.0f;
constexpr std::uint32_t kFireAnimationFrameCount = 16;
constexpr ULONGLONG kFireAnimationFrameIntervalMilliseconds = 50;
constexpr float kFireEmissiveIntensity = 1.35f;
inline constexpr remixapi_Float3D kFireEmissiveColor = {1.0f, 1.0f, 1.0f};
constexpr float kFaceOverlayBias = 0.001f;
constexpr std::uint64_t kOpaqueTerrainMaterialHash = 0x4D435254584F5041ull;
constexpr std::uint64_t kCutoutTerrainMaterialHash = 0x4D43525458435554ull;
constexpr std::uint64_t kWaterTerrainMaterialHash = 0x4D43525458575452ull;
constexpr std::uint64_t kCloudMaterialHash = 0x4D43525458434C44ull;
constexpr std::uint64_t kFireMaterialHash = 0x4D43525458464952ull;
constexpr std::uint64_t kDynamicEntityMaterialHashSeed = 0x4D43525458454E54ull;
constexpr std::uint64_t kParticleMaterialHashSeed = 0x4D43525458505443ull;
constexpr std::uint64_t kDynamicEntityMeshHashSeed = 0x4D43525458454E00ull;
constexpr std::uint64_t kDestroyOverlayMeshHashSeed = 0x4D43525458444F00ull;
constexpr std::uint64_t kParticleMeshHashSeed = 0x4D43525458505100ull;
constexpr std::uint64_t kFireMeshHashSeed = 0x4D43525458465200ull;
constexpr std::uint64_t kTorchLightHashSeed = 0x4D435254584C4954ull;
constexpr std::uint32_t kDefaultVertexColor = 0xFFFFFFFFu;
constexpr std::uint32_t kRtTextureArgNone = 0;
constexpr std::uint32_t kRtTextureArgTexture = 1;
constexpr std::uint32_t kRtTextureArgVertexColor0 = 2;
constexpr std::uint32_t kRtTextureOpSelectArg1 = 1;
constexpr std::uint32_t kRtTextureOpModulate = 3;
constexpr float kCloudAlpha = 0.8f;
constexpr float kFastCloudTileSize = 32.0f;
constexpr float kFastCloudRadius = 256.0f;
constexpr float kFastCloudUvScale = 1.0f / 2048.0f;
constexpr float kFancyCloudScale = 12.0f;
constexpr float kFancyCloudCellSize = 8.0f;
constexpr int kFancyCloudRadiusCells = 3;
constexpr float kFancyCloudThickness = 4.0f;
constexpr float kFancyCloudUvScale = 1.0f / 256.0f;
constexpr float kFancyCloudInset = 1.0f / 1024.0f;
constexpr float kTorchLightOffsetX = 0.5f;
constexpr float kTorchLightOffsetY = 0.70f;
constexpr float kTorchLightOffsetZ = 0.5f;
constexpr float kTorchLightRadius = 0.06f;
constexpr float kWallTorchLightHorizontalOffset = 0.27f;
constexpr float kWallTorchLightVerticalOffset = 0.22f;
inline constexpr remixapi_Float3D kTorchLightRadiance = {540.0f, 331.5f, 121.5f};

inline constexpr float kFaceVertexOffsets[6][4][3] = {
  {{0.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}},
  {{0.0f, 0.0f, 1.0f}, {0.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 0.0f, 1.0f}},
  {{0.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 1.0f}},
  {{1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 0.0f}},
  {{0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}},
  {{0.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 1.0f}},
};

inline constexpr float kFaceNormals[6][3] = {
  {0.0f, 0.0f, -1.0f},
  {0.0f, 0.0f, 1.0f},
  {-1.0f, 0.0f, 0.0f},
  {1.0f, 0.0f, 0.0f},
  {0.0f, -1.0f, 0.0f},
  {0.0f, 1.0f, 0.0f},
};

inline constexpr int kNativeFaceToMinecraftSide[6] = {2, 3, 4, 5, 0, 1};

inline constexpr float kFaceTexcoords[6][4][2] = {
  {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
  {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
  {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
  {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
  {{0.0f, 0.0f}, {0.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 0.0f}},
  {{0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}},
};

inline constexpr std::uint32_t kFaceIndices[6] = {0, 1, 2, 0, 2, 3};

inline constexpr int kNeighborOffsets[6][3] = {
  {0, 0, -1},
  {0, 0, 1},
  {-1, 0, 0},
  {1, 0, 0},
  {0, -1, 0},
  {0, 1, 0},
};

struct SurfaceBuildBuffers {
  remixapi_MaterialHandle materialHandle {nullptr};
  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  std::vector<float> blendWeights;
  std::vector<std::uint32_t> blendIndices;
};

// ---- Environment / module helpers ----------------------------------------

HMODULE getCurrentModuleHandle();
bool isTruthyEnvValue(const char* envValue);
std::string readEnvironmentVariable(const char* name);
bool equalsIgnoreCase(std::string_view left, std::string_view right);
bool shouldUseOverlayOutputWindow(bool* usedLegacyEnvVar = nullptr);
bool getSourceClientRectInScreenSpace(HWND sourceHwnd, RECT& rect);
LRESULT CALLBACK remixOutputWindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam);
bool ensureOutputWindowClassRegistered();
std::string errorCodeToString(remixapi_ErrorCode code);
std::filesystem::path getCurrentModuleDirectory();

extern std::atomic_bool g_outputWindowInteractive;
extern const wchar_t kRemixWindowClassName[];
extern const wchar_t kRemixWindowTitle[];

// ---- Chunk / block classification ----------------------------------------

std::uint32_t countUniqueBlockIds(const ChunkBuildState& chunkBuild);
ChunkKey makeChunkKey(const ChunkBuildState& chunkBuild);
bool isWaterBlock(int blockId);
bool isCrossedQuadRenderType(int renderType);
bool isFireRenderType(int renderType);
bool isTorchRenderType(int renderType);
bool isDoorRenderType(int renderType);
bool isLadderRenderType(int renderType);
bool isRailRenderType(int renderType);
bool isStairRenderType(int renderType);
bool isFenceRenderType(int renderType);
bool isSingleSlabBlockId(int blockId);
bool isStairBlockId(int blockId);
bool isDoorBlockId(int blockId);
bool isRailBlockId(int blockId);
bool isSupportedPass0RenderType(int renderType);
bool shouldCaptureBlock(int blockId, int renderType);
bool shouldCaptureBlock(int blockId, int renderType, int renderPass);
bool usesCutoutMaterialForBlock(int blockId, int renderType);
std::uint8_t materialClassForBlock(int blockId, int renderType);

// ---- Transforms and hashes -----------------------------------------------

remixapi_Transform makeTranslationTransform(float x, float y, float z);
std::uint64_t makeChunkMeshHash(const ChunkKey& key, std::uint64_t sequence);
std::uint64_t makeCloudMeshHash(std::uint64_t sequence);
std::uint64_t makeDynamicEntityMeshHash(std::uint64_t geometryFingerprint);
std::uint64_t makeDestroyOverlayMeshHash(std::uint64_t sequence);
std::uint64_t makeParticleMeshHash(std::uint64_t sequence);
std::uint64_t makeFireMeshHash(std::uint64_t sequence);
std::uint64_t mixHashComponent(std::uint64_t hash, std::uint32_t value);
std::uint64_t makeTorchLightHash(const WorldBlockPosition& position);

// ---- Torch light placement ------------------------------------------------

bool containsWorldBlockPosition(const std::vector<WorldBlockPosition>& positions, const WorldBlockPosition& position);
const TorchLightPlacement* findTorchLightPlacement(
    const std::vector<TorchLightPlacement>& placements,
    const WorldBlockPosition& position);
TorchLightPlacement makeTorchLightPlacement(
    const ChunkBlockCell& cell,
    int worldX,
    int worldY,
    int worldZ);

// ---- Color / packing / fingerprints --------------------------------------

std::uint8_t clampColorChannel(float value);
std::uint32_t packVertexColorRgba(float red, float green, float blue, float alpha);
std::uint64_t computeChunkFingerprint(
    const std::array<std::uint8_t, kBlocksPerChunk>& occupancy,
    const std::array<ChunkBlockCell, kBlocksPerChunk>& cells);
void hashDynamicEntityString(std::uint64_t& fingerprint, const std::string& value);
std::uint32_t computeDynamicEntityBoneCount(const std::vector<DynamicEntityQuad>& quads);
std::uint64_t computeDynamicEntityFingerprint(
    const std::vector<DynamicEntityQuad>& quads,
    std::uint32_t boneCount);
std::uint32_t packVertexColor(std::uint32_t rgbColor);

// ---- Tile / bounds helpers ------------------------------------------------

int normalizeTerrainTileIndex(std::int16_t terrainTileIndex);
bool usesFlippedTerrainTile(std::int16_t terrainTileIndex);
float maybeFlipTileU(float u, float tileMinU, float scaleU, bool flipU);
bool usesPartialCubeBounds(const ChunkBlockCell& cell);

std::array<float, 3> computeQuadNormal(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float x2, float y2, float z2);

int blockIndex(int x, int y, int z);

std::uint32_t faceTintColorForBlock(std::uint8_t blockId, int minecraftSide, std::uint32_t blockColor);
bool usesFancyLeavesTexture(const ChunkBlockCell& cell);
bool shouldCullFaceAgainstNeighbor(const ChunkBlockCell& cell, const ChunkBlockCell& neighborCell);

// ---- Geometry emitters ----------------------------------------------------

void appendFaceGeometry(
    int faceIndex,
    float localX,
    float localY,
    float localZ,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    float normalOffset,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendBoundsFaceGeometry(
    int faceIndex,
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    const std::array<std::int16_t, 6>& terrainTiles,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendWaterQuad(
    float x0, float y0, float z0, float u0, float v0,
    float x1, float y1, float z1, float u1, float v1,
    float x2, float y2, float z2, float u2, float v2,
    float x3, float y3, float z3, float u3, float v3,
    float normalX, float normalY, float normalZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendCloudQuad(
    float x0, float y0, float z0, float u0, float v0,
    float x1, float y1, float z1, float u1, float v1,
    float x2, float y2, float z2, float u2, float v2,
    float x3, float y3, float z3, float u3, float v3,
    float normalX, float normalY, float normalZ,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendCrossedQuadSheet(
    float x0, float y0, float z0, float u0, float v0,
    float x1, float y1, float z1, float u1, float v1,
    float x2, float y2, float z2, float u2, float v2,
    float x3, float y3, float z3, float u3, float v3,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendAnimatedFireSheet(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float x2, float y2, float z2,
    float x3, float y3, float z3,
    std::uint32_t frameIndex,
    bool alternateRow,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendFireGeometry(
    int worldX, int worldY, int worldZ,
    bool hasBase,
    bool westNeighbor, bool eastNeighbor,
    bool northNeighbor, bool southNeighbor,
    bool upNeighbor,
    float localX, float localY, float localZ,
    std::uint32_t frameIndex,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendCrossedQuadGeometry(
    const ChunkBlockCell& cell,
    int worldX, int worldY, int worldZ,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendSlabGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendStairGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendDoorGeometry(
    const ChunkBlockCell& cell,
    int resolvedMetadata,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendTorchGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendLadderGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendRailGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendFenceGeometry(
    bool connectWest, bool connectEast,
    bool connectNorth, bool connectSouth,
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendFastCloudGeometry(
    float cameraX, float cameraZ,
    float cloudHeight, float cloudScroll,
    float colorR, float colorG, float colorB,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendFancyCloudGeometry(
    float cameraX, float cameraY, float cameraZ,
    float cloudHeight, float cloudScroll,
    float colorR, float colorG, float colorB,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendWaterGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

}  // namespace detail
}  // namespace mcrtx
