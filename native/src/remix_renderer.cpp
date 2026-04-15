#include "mcrtx/remix_renderer.hpp"

#include <algorithm>
#include <bit>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <iostream>
#include <sstream>
#include <vector>

namespace mcrtx {

namespace {

constexpr wchar_t kRemixWindowClassName[] = L"MCRTXRemixOutputWindow";
constexpr wchar_t kRemixWindowTitle[] = L"mc-rtx Remix Output";
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
constexpr std::uint8_t kLadderBlockRenderType = 8;
constexpr std::uint8_t kRailBlockRenderType = 9;
constexpr std::uint8_t kFenceBlockRenderType = 11;
constexpr std::uint8_t kLiquidBlockRenderType = 4;
constexpr std::uint8_t kGrassBlockId = 2;
constexpr std::uint8_t kLeavesBlockId = 18;
constexpr std::uint8_t kTallGrassBlockId = 31;
constexpr std::uint8_t kTorchBlockId = 50;
constexpr std::uint8_t kLadderBlockId = 65;
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
constexpr float kFaceOverlayBias = 0.001f;
constexpr std::uint64_t kOpaqueTerrainMaterialHash = 0x4D435254584F5041ull;
constexpr std::uint64_t kCutoutTerrainMaterialHash = 0x4D43525458435554ull;
constexpr std::uint64_t kWaterTerrainMaterialHash = 0x4D43525458575452ull;
constexpr std::uint64_t kCloudMaterialHash = 0x4D43525458434C44ull;
constexpr std::uint64_t kDynamicEntityMaterialHashSeed = 0x4D43525458454E54ull;
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
constexpr remixapi_Float3D kTorchLightRadiance = {540.0f, 331.5f, 121.5f};

struct SurfaceBuildBuffers {
  remixapi_MaterialHandle materialHandle {nullptr};
  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
};

constexpr float kFaceVertexOffsets[6][4][3] = {
  {{0.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}},
  {{0.0f, 0.0f, 1.0f}, {0.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 0.0f, 1.0f}},
  {{0.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 1.0f}},
  {{1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 0.0f}},
  {{0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}},
  {{0.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 1.0f}},
};

constexpr float kFaceNormals[6][3] = {
  {0.0f, 0.0f, -1.0f},
  {0.0f, 0.0f, 1.0f},
  {-1.0f, 0.0f, 0.0f},
  {1.0f, 0.0f, 0.0f},
  {0.0f, -1.0f, 0.0f},
  {0.0f, 1.0f, 0.0f},
};

constexpr int kNativeFaceToMinecraftSide[6] = {
  2,
  3,
  4,
  5,
  0,
  1,
};

constexpr float kFaceTexcoords[6][4][2] = {
  {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
  {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
  {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
  {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
  {{0.0f, 0.0f}, {0.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 0.0f}},
  {{0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}},
};

constexpr std::uint32_t kFaceIndices[6] = {0, 1, 2, 0, 2, 3};

constexpr int kNeighborOffsets[6][3] = {
  {0, 0, -1},
  {0, 0, 1},
  {-1, 0, 0},
  {1, 0, 0},
  {0, -1, 0},
  {0, 1, 0},
};

HMODULE getCurrentModuleHandle() {
  HMODULE moduleHandle = nullptr;
  if (!GetModuleHandleExW(
          GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
          reinterpret_cast<LPCWSTR>(&getCurrentModuleHandle),
          &moduleHandle)) {
    return nullptr;
  }
  return moduleHandle;
}

LRESULT CALLBACK remixOutputWindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
  switch (message) {
    case WM_CLOSE:
      ShowWindow(hwnd, SW_HIDE);
      return 0;
    default:
      return DefWindowProcW(hwnd, message, wParam, lParam);
  }
}

bool ensureOutputWindowClassRegistered() {
  static bool registered = false;
  if (registered) {
    return true;
  }

  WNDCLASSEXW windowClass {};
  windowClass.cbSize = sizeof(windowClass);
  windowClass.lpfnWndProc = remixOutputWindowProc;
  windowClass.hInstance = getCurrentModuleHandle();
  windowClass.hCursor = LoadCursorW(nullptr, MAKEINTRESOURCEW(32512));
  windowClass.hbrBackground = reinterpret_cast<HBRUSH>(COLOR_WINDOW + 1);
  windowClass.lpszClassName = kRemixWindowClassName;

  const ATOM atom = RegisterClassExW(&windowClass);
  if (atom == 0 && GetLastError() != ERROR_CLASS_ALREADY_EXISTS) {
    return false;
  }

  registered = true;
  return true;
}

std::string errorCodeToString(remixapi_ErrorCode code) {
  std::ostringstream stream;
  stream << "remixapi error " << static_cast<int>(code);
  return stream.str();
}

std::filesystem::path getCurrentModuleDirectory() {
  HMODULE moduleHandle = nullptr;
  if (!GetModuleHandleExW(
          GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
          reinterpret_cast<LPCWSTR>(&getCurrentModuleDirectory),
          &moduleHandle)) {
    return {};
  }

  std::wstring buffer(MAX_PATH, L'\0');
  DWORD length = 0;
  while (true) {
    length = GetModuleFileNameW(moduleHandle, buffer.data(), static_cast<DWORD>(buffer.size()));
    if (length == 0) {
      return {};
    }
    if (length < buffer.size() - 1) {
      break;
    }
    buffer.resize(buffer.size() * 2);
  }

  buffer.resize(length);
  return std::filesystem::path(buffer).parent_path();
}

std::uint32_t countUniqueBlockIds(const ChunkBuildState& chunkBuild) {
  std::uint32_t unique = 0;
  for (const std::uint32_t count : chunkBuild.blockIdCounts) {
    if (count != 0) {
      ++unique;
    }
  }
  return unique;
}

ChunkKey makeChunkKey(const ChunkBuildState& chunkBuild) {
  return ChunkKey {
      .originX = chunkBuild.origin[0],
      .originY = chunkBuild.origin[1],
      .originZ = chunkBuild.origin[2],
      .renderPass = chunkBuild.renderPass,
  };
}

bool isWaterBlock(int blockId) {
  return blockId == kWaterStillBlockId || blockId == kWaterFlowingBlockId;
}

bool isCrossedQuadRenderType(int renderType) {
  return renderType == kCrossedQuadBlockRenderType;
}

bool isTorchRenderType(int renderType) {
  return renderType == kTorchBlockRenderType;
}

bool isLadderRenderType(int renderType) {
  return renderType == kLadderBlockRenderType;
}

bool isRailRenderType(int renderType) {
  return renderType == kRailBlockRenderType;
}

bool isFenceRenderType(int renderType) {
  return renderType == kFenceBlockRenderType;
}

bool isRailBlockId(int blockId) {
  return blockId == kGoldenRailBlockId || blockId == kDetectorRailBlockId || blockId == kRailBlockId;
}

bool isSupportedPass0RenderType(int renderType) {
  switch (renderType) {
    case kCubeBlockRenderType:
    case kCrossedQuadBlockRenderType:
    case kTorchBlockRenderType:
    case kLadderBlockRenderType:
    case kRailBlockRenderType:
    case kFenceBlockRenderType:
      return true;
    default:
      return false;
  }
}

bool shouldCaptureBlock(int blockId, int renderType) {
  return blockId > 0 && isSupportedPass0RenderType(renderType);
}

bool shouldCaptureBlock(int blockId, int renderType, int renderPass) {
  if (blockId <= 0) {
    return false;
  }
  if (renderPass == 0) {
    return isSupportedPass0RenderType(renderType);
  }
  if (renderPass == 1) {
    return renderType == kLiquidBlockRenderType && isWaterBlock(blockId);
  }
  return false;
}

bool usesCutoutMaterialForBlock(int blockId, int renderType) {
  if (isCrossedQuadRenderType(renderType)
      || isTorchRenderType(renderType)
      || isLadderRenderType(renderType)
      || isRailRenderType(renderType)) {
    return true;
  }

  switch (blockId) {
    case 18:
    case 20:
    case 52:
      return true;
    default:
      return false;
  }
}

std::uint8_t materialClassForBlock(int blockId, int renderType) {
  if (isWaterBlock(blockId)) {
    return kWaterTerrainMaterialClass;
  }
  return usesCutoutMaterialForBlock(blockId, renderType) ? kCutoutTerrainMaterialClass : kOpaqueTerrainMaterialClass;
}

remixapi_Transform makeTranslationTransform(float x, float y, float z) {
  remixapi_Transform transform {};
  transform.matrix[0][0] = 1.0f;
  transform.matrix[1][1] = 1.0f;
  transform.matrix[2][2] = 1.0f;
  transform.matrix[0][3] = x;
  transform.matrix[1][3] = y;
  transform.matrix[2][3] = z;
  return transform;
}

std::uint64_t makeChunkMeshHash(const ChunkKey& key, std::uint64_t sequence) {
  (void)key;
  return 0x4D43525458000000ull | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t makeCloudMeshHash(std::uint64_t sequence) {
  return 0x4D43525458434C00ull | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t makeDynamicEntityMeshHash(std::uint64_t sequence) {
  return 0x4D43525458454E00ull | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t mixHashComponent(std::uint64_t hash, std::uint32_t value) {
  hash ^= static_cast<std::uint64_t>(value) + 0x9e3779b97f4a7c15ull + (hash << 6) + (hash >> 2);
  return hash;
}

std::uint64_t makeTorchLightHash(const WorldBlockPosition& position) {
  std::uint64_t hash = kTorchLightHashSeed;
  hash = mixHashComponent(hash, std::bit_cast<std::uint32_t>(position.x));
  hash = mixHashComponent(hash, std::bit_cast<std::uint32_t>(position.y));
  hash = mixHashComponent(hash, std::bit_cast<std::uint32_t>(position.z));
  return hash;
}

bool containsWorldBlockPosition(const std::vector<WorldBlockPosition>& positions, const WorldBlockPosition& position) {
  return std::find(positions.begin(), positions.end(), position) != positions.end();
}

const TorchLightPlacement* findTorchLightPlacement(
    const std::vector<TorchLightPlacement>& placements,
    const WorldBlockPosition& position) {
  const auto it = std::find_if(
      placements.begin(),
      placements.end(),
      [&position](const TorchLightPlacement& placement) {
        return placement.blockPosition == position;
      });
  return it == placements.end() ? nullptr : &(*it);
}

TorchLightPlacement makeTorchLightPlacement(
    const ChunkBlockCell& cell,
    int worldX,
    int worldY,
    int worldZ) {
  TorchLightPlacement placement;
  placement.blockPosition = WorldBlockPosition {
      .x = worldX,
      .y = worldY,
      .z = worldZ,
  };
  placement.lightX = static_cast<float>(worldX) + kTorchLightOffsetX;
  placement.lightY = static_cast<float>(worldY) + kTorchLightOffsetY;
  placement.lightZ = static_cast<float>(worldZ) + kTorchLightOffsetZ;

  switch (cell.blockMetadata & 7) {
    case 1:
      placement.lightX -= kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    case 2:
      placement.lightX += kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    case 3:
      placement.lightZ -= kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    case 4:
      placement.lightZ += kWallTorchLightHorizontalOffset;
      placement.lightY += kWallTorchLightVerticalOffset;
      break;
    default:
      break;
  }

  return placement;
}

std::uint8_t clampColorChannel(float value) {
  const float clampedValue = std::clamp(value, 0.0f, 1.0f);
  return static_cast<std::uint8_t>(std::lround(clampedValue * 255.0f));
}

std::uint32_t packVertexColorRgba(float red, float green, float blue, float alpha) {
  return (static_cast<std::uint32_t>(clampColorChannel(alpha)) << 24)
      | (static_cast<std::uint32_t>(clampColorChannel(red)) << 16)
      | (static_cast<std::uint32_t>(clampColorChannel(green)) << 8)
      | static_cast<std::uint32_t>(clampColorChannel(blue));
}

std::uint64_t computeChunkFingerprint(
    const std::array<std::uint8_t, kBlocksPerChunk>& occupancy,
    const std::array<ChunkBlockCell, kBlocksPerChunk>& cells) {
  std::uint64_t fingerprint = 1469598103934665603ull;
  for (std::size_t index = 0; index < occupancy.size(); ++index) {
    const std::uint8_t occupied = occupancy[index];
    fingerprint ^= static_cast<std::uint64_t>(occupied);
    fingerprint *= 1099511628211ull;
    if (occupied == 0) {
      continue;
    }

    fingerprint ^= static_cast<std::uint64_t>(cells[index].materialClass);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].blockId);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].blockMetadata);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].renderType);
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].liquidVisibilityMask);
    fingerprint *= 1099511628211ull;
    for (const float liquidHeight : cells[index].liquidHeights) {
      fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(liquidHeight));
      fingerprint *= 1099511628211ull;
    }
    fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(cells[index].liquidFlowAngle));
    fingerprint *= 1099511628211ull;
    fingerprint ^= static_cast<std::uint64_t>(cells[index].blockColor);
    fingerprint *= 1099511628211ull;
    for (const std::uint8_t tileIndex : cells[index].terrainTiles) {
      fingerprint ^= static_cast<std::uint64_t>(tileIndex);
      fingerprint *= 1099511628211ull;
    }
  }
  return fingerprint;
}

std::uint32_t packVertexColor(std::uint32_t rgbColor) {
  return 0xFF000000u | (rgbColor & 0x00FFFFFFu);
}

std::array<float, 3> computeQuadNormal(
    float x0,
    float y0,
    float z0,
    float x1,
    float y1,
    float z1,
    float x2,
    float y2,
    float z2) {
  const float edgeAx = x1 - x0;
  const float edgeAy = y1 - y0;
  const float edgeAz = z1 - z0;
  const float edgeBx = x2 - x0;
  const float edgeBy = y2 - y0;
  const float edgeBz = z2 - z0;

  float normalX = edgeAy * edgeBz - edgeAz * edgeBy;
  float normalY = edgeAz * edgeBx - edgeAx * edgeBz;
  float normalZ = edgeAx * edgeBy - edgeAy * edgeBx;
  const float normalLength = std::sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
  if (normalLength <= 0.00001f) {
    return {0.0f, 1.0f, 0.0f};
  }

  return {normalX / normalLength, normalY / normalLength, normalZ / normalLength};
}

int blockIndex(int x, int y, int z) {
  return x + kChunkDimension * (z + kChunkDimension * y);
}

void appendFaceGeometry(
    int faceIndex,
    float localX,
    float localY,
    float localZ,
    std::uint8_t terrainTileIndex,
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
    std::uint8_t terrainTileIndex,
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
    const std::array<std::uint8_t, 6>& terrainTiles,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

std::uint32_t faceTintColorForBlock(std::uint8_t blockId, int minecraftSide, std::uint32_t blockColor) {
  if (blockId == kGrassBlockId && minecraftSide == 1) {
    return blockColor;
  }

  if (blockId == kLeavesBlockId) {
    return blockColor;
  }

  return 0x00FFFFFFu;
}

bool usesFancyLeavesTexture(const ChunkBlockCell& cell) {
  if (cell.blockId != kLeavesBlockId) {
    return false;
  }

  const std::uint8_t terrainTile = cell.terrainTiles[0];
  return terrainTile == kLeavesFancyTextureOak || terrainTile == kLeavesFancyTextureBirchSpruce;
}

bool shouldCullFaceAgainstNeighbor(const ChunkBlockCell& cell, const ChunkBlockCell& neighborCell) {
  if (cell.renderType != kCubeBlockRenderType || neighborCell.renderType != kCubeBlockRenderType) {
    return false;
  }

  if (cell.blockId == kLeavesBlockId
      && neighborCell.blockId == kLeavesBlockId
      && usesFancyLeavesTexture(cell)) {
    return false;
  }

  return true;
}

void appendWaterQuad(
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
    float normalX,
    float normalY,
    float normalZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const std::array<std::array<float, 5>, 4> vertexData = {{
      {{x0, y0, z0, u0, v0}},
      {{x1, y1, z1, u1, v1}},
      {{x2, y2, z2, u2, v2}},
      {{x3, y3, z3, u3, v3}},
  }};

  for (const auto& data : vertexData) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = data[0];
    vertex.position[1] = data[1];
    vertex.position[2] = data[2];
    vertex.normal[0] = normalX;
    vertex.normal[1] = normalY;
    vertex.normal[2] = normalZ;
    vertex.texcoord[0] = data[3];
    vertex.texcoord[1] = data[4];
    vertex.color = kDefaultVertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void appendCloudQuad(
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
    float normalX,
    float normalY,
    float normalZ,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const std::array<std::array<float, 5>, 4> vertexData = {{
      {{x0, y0, z0, u0, v0}},
      {{x1, y1, z1, u1, v1}},
      {{x2, y2, z2, u2, v2}},
      {{x3, y3, z3, u3, v3}},
  }};

  for (const auto& data : vertexData) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = data[0];
    vertex.position[1] = data[1];
    vertex.position[2] = data[2];
    vertex.normal[0] = normalX;
    vertex.normal[1] = normalY;
    vertex.normal[2] = normalZ;
    vertex.texcoord[0] = data[3];
    vertex.texcoord[1] = data[4];
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void appendCrossedQuadSheet(
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
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const auto normal = computeQuadNormal(x0, y0, z0, x1, y1, z1, x2, y2, z2);
  appendCloudQuad(
      x0,
      y0,
      z0,
      u0,
      v0,
      x1,
      y1,
      z1,
      u1,
      v1,
      x2,
      y2,
      z2,
      u2,
      v2,
      x3,
      y3,
      z3,
      u3,
      v3,
      normal[0],
      normal[1],
      normal[2],
      vertexColor,
      vertices,
      indices);
  appendCloudQuad(
      x3,
      y3,
      z3,
      u3,
      v3,
      x2,
      y2,
      z2,
      u2,
      v2,
      x1,
      y1,
      z1,
      u1,
      v1,
      x0,
      y0,
      z0,
      u0,
      v0,
      -normal[0],
      -normal[1],
      -normal[2],
      vertexColor,
      vertices,
      indices);
}

void appendCrossedQuadGeometry(
    const ChunkBlockCell& cell,
  int worldX,
  int worldY,
  int worldZ,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint8_t terrainTileIndex = cell.terrainTiles[0];
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  float offsetX = 0.0f;
  float offsetY = 0.0f;
  float offsetZ = 0.0f;
  if (cell.blockId == kTallGrassBlockId) {
    std::uint64_t seed = static_cast<std::uint64_t>(worldX * 3129871)
      ^ static_cast<std::uint64_t>(worldZ) * 116129781ull
      ^ static_cast<std::uint64_t>(worldY);
    seed = seed * seed * 42317861ull + seed * 11ull;
    offsetX = ((static_cast<float>((seed >> 16) & 0x0F) / 15.0f) - 0.5f) * 0.5f;
    offsetY = ((static_cast<float>((seed >> 20) & 0x0F) / 15.0f) - 1.0f) * 0.2f;
    offsetZ = ((static_cast<float>((seed >> 24) & 0x0F) / 15.0f) - 0.5f) * 0.5f;
  }

  const float centerX = localX + 0.5f + offsetX;
  const float baseY = localY + offsetY;
  const float centerZ = localZ + 0.5f + offsetZ;
  const float minX = centerX - 0.45f;
  const float maxX = centerX + 0.45f;
  const float minZ = centerZ - 0.45f;
  const float maxZ = centerZ + 0.45f;
  const std::uint32_t vertexColor = packVertexColor(cell.blockColor);

  appendCrossedQuadSheet(
      minX,
      baseY + 1.0f,
      minZ,
      tileMinU,
      tileMinV,
      minX,
      baseY + 0.0f,
      minZ,
      tileMinU,
      tileMaxV,
      maxX,
      baseY + 0.0f,
      maxZ,
      tileMaxU,
      tileMaxV,
      maxX,
      baseY + 1.0f,
      maxZ,
      tileMaxU,
      tileMinV,
      vertexColor,
      vertices,
      indices);
  appendCrossedQuadSheet(
      minX,
      baseY + 1.0f,
      maxZ,
      tileMinU,
      tileMinV,
      minX,
      baseY + 0.0f,
      maxZ,
      tileMinU,
      tileMaxV,
      maxX,
      baseY + 0.0f,
      minZ,
      tileMaxU,
      tileMaxV,
      maxX,
      baseY + 1.0f,
      minZ,
      tileMaxU,
      tileMinV,
      vertexColor,
      vertices,
      indices);
}

void appendBoundsFaceGeometry(
    int faceIndex,
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::uint8_t terrainTileIndex,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float usableTileSize = kAtlasTileSizePixels - kAtlasUvInsetPixels;
  const float scaleU = usableTileSize / kAtlasSizePixels;
  const float scaleV = usableTileSize / kAtlasSizePixels;
  const float blockOriginX = std::floor(minX);
  const float blockOriginY = std::floor(minY);
  const float blockOriginZ = std::floor(minZ);

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    const float px = kFaceVertexOffsets[faceIndex][vertexIndex][0] == 0.0f ? minX : maxX;
    const float py = kFaceVertexOffsets[faceIndex][vertexIndex][1] == 0.0f ? minY : maxY;
    const float pz = kFaceVertexOffsets[faceIndex][vertexIndex][2] == 0.0f ? minZ : maxZ;
    const float relX = std::clamp(px - blockOriginX, 0.0f, 1.0f);
    const float relY = std::clamp(py - blockOriginY, 0.0f, 1.0f);
    const float relZ = std::clamp(pz - blockOriginZ, 0.0f, 1.0f);
    vertex.position[0] = px;
    vertex.position[1] = py;
    vertex.position[2] = pz;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];

    float u = tileMinU;
    float v = tileMinV;
    if (faceIndex == 0) {
      u = tileMinU + (1.0f - relX) * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 1) {
      u = tileMinU + relX * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 2) {
      u = tileMinU + relZ * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 3) {
      u = tileMinU + (1.0f - relZ) * scaleU;
      v = tileMinV + (1.0f - relY) * scaleV;
    } else if (faceIndex == 4 || faceIndex == 5) {
      u = tileMinU + relX * scaleU;
      v = tileMinV + relZ * scaleV;
    }

    vertex.texcoord[0] = u;
    vertex.texcoord[1] = v;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void appendBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    const std::array<std::uint8_t, 6>& terrainTiles,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
    const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
    appendBoundsFaceGeometry(
        faceIndex,
        minX,
        minY,
        minZ,
        maxX,
        maxY,
        maxZ,
        terrainTiles[minecraftSide],
        vertexColor,
        vertices,
        indices);
  }
}

void appendTorchGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint8_t terrainTileIndex = cell.terrainTiles[0];
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + 15.99f) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + 15.99f) / kAtlasSizePixels;
  const float capMinU = tileMinU + 7.0f / 256.0f;
  const float capMaxU = tileMinU + 9.0f / 256.0f;
  const float capMinV = tileMinV + 6.0f / 256.0f;
  const float capMaxV = tileMinV + 8.0f / 256.0f;

  float anchorX = localX;
  float anchorY = localY;
  float anchorZ = localZ;
  float leanX = 0.0f;
  float leanZ = 0.0f;
  const int metadata = cell.blockMetadata & 7;
  if (metadata == 1) {
    anchorX -= 0.1f;
    anchorY += 0.2f;
    leanX = -0.4f;
  } else if (metadata == 2) {
    anchorX += 0.1f;
    anchorY += 0.2f;
    leanX = 0.4f;
  } else if (metadata == 3) {
    anchorY += 0.2f;
    anchorZ -= 0.1f;
    leanZ = -0.4f;
  } else if (metadata == 4) {
    anchorY += 0.2f;
    anchorZ += 0.1f;
    leanZ = 0.4f;
  }

  const float centerX = anchorX + 0.5f;
  const float centerZ = anchorZ + 0.5f;
  const float topCenterX = centerX + leanX * 0.375f;
  const float topCenterZ = centerZ + leanZ * 0.375f;
  const float topY = anchorY + 0.625f;
  const float bodyTopY = anchorY + 1.0f;
  const float bodyBottomY = anchorY + 0.0f;
  const float halfWidth = 0.0625f;

  const auto appendAutoQuad = [&vertices, &indices](
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
                                  float v3) {
    const auto normal = computeQuadNormal(x0, y0, z0, x1, y1, z1, x2, y2, z2);
    appendCloudQuad(
        x0,
        y0,
        z0,
        u0,
        v0,
        x1,
        y1,
        z1,
        u1,
        v1,
        x2,
        y2,
        z2,
        u2,
        v2,
        x3,
        y3,
        z3,
        u3,
        v3,
        normal[0],
        normal[1],
        normal[2],
        kDefaultVertexColor,
        vertices,
        indices);
  };

  appendAutoQuad(
      topCenterX - halfWidth,
      topY,
      topCenterZ - halfWidth,
      capMinU,
      capMinV,
      topCenterX - halfWidth,
      topY,
      topCenterZ + halfWidth,
      capMinU,
      capMaxV,
      topCenterX + halfWidth,
      topY,
      topCenterZ + halfWidth,
      capMaxU,
      capMaxV,
      topCenterX + halfWidth,
      topY,
      topCenterZ - halfWidth,
      capMaxU,
      capMinV);

  appendAutoQuad(
      centerX - halfWidth,
      bodyTopY,
      centerZ - 0.5f,
      tileMinU,
      tileMinV,
      centerX - halfWidth + leanX,
      bodyBottomY,
      centerZ - 0.5f + leanZ,
      tileMinU,
      tileMaxV,
      centerX - halfWidth + leanX,
      bodyBottomY,
      centerZ + 0.5f + leanZ,
      tileMaxU,
      tileMaxV,
      centerX - halfWidth,
      bodyTopY,
      centerZ + 0.5f,
      tileMaxU,
      tileMinV);

  appendAutoQuad(
      centerX + halfWidth,
      bodyTopY,
      centerZ + 0.5f,
      tileMinU,
      tileMinV,
      centerX + halfWidth + leanX,
      bodyBottomY,
      centerZ + 0.5f + leanZ,
      tileMinU,
      tileMaxV,
      centerX + halfWidth + leanX,
      bodyBottomY,
      centerZ - 0.5f + leanZ,
      tileMaxU,
      tileMaxV,
      centerX + halfWidth,
      bodyTopY,
      centerZ - 0.5f,
      tileMaxU,
      tileMinV);

  appendAutoQuad(
      centerX - 0.5f,
      bodyTopY,
      centerZ + halfWidth,
      tileMinU,
      tileMinV,
      centerX - 0.5f + leanX,
      bodyBottomY,
      centerZ + halfWidth + leanZ,
      tileMinU,
      tileMaxV,
      centerX + 0.5f + leanX,
      bodyBottomY,
      centerZ + halfWidth + leanZ,
      tileMaxU,
      tileMaxV,
      centerX + 0.5f,
      bodyTopY,
      centerZ + halfWidth,
      tileMaxU,
      tileMinV);

  appendAutoQuad(
      centerX + 0.5f,
      bodyTopY,
      centerZ - halfWidth,
      tileMinU,
      tileMinV,
      centerX + 0.5f + leanX,
      bodyBottomY,
      centerZ - halfWidth + leanZ,
      tileMinU,
      tileMaxV,
      centerX - 0.5f + leanX,
      bodyBottomY,
      centerZ - halfWidth + leanZ,
      tileMaxU,
      tileMaxV,
      centerX - 0.5f,
      bodyTopY,
      centerZ - halfWidth,
      tileMaxU,
      tileMinV);
}

void appendLadderGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint8_t terrainTileIndex = cell.terrainTiles[0];
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const int metadata = cell.blockMetadata & 7;
  const float epsilon = 0.05f;

  if (metadata == 5) {
    appendCrossedQuadSheet(
        localX + epsilon,
        localY + 1.0f,
        localZ + 1.0f,
        tileMinU,
        tileMinV,
        localX + epsilon,
        localY + 0.0f,
        localZ + 1.0f,
        tileMinU,
        tileMaxV,
        localX + epsilon,
        localY + 0.0f,
        localZ + 0.0f,
        tileMaxU,
        tileMaxV,
        localX + epsilon,
        localY + 1.0f,
        localZ + 0.0f,
        tileMaxU,
        tileMinV,
        kDefaultVertexColor,
        vertices,
        indices);
  } else if (metadata == 4) {
    appendCrossedQuadSheet(
        localX + 1.0f - epsilon,
        localY + 0.0f,
        localZ + 1.0f,
        tileMaxU,
        tileMaxV,
        localX + 1.0f - epsilon,
        localY + 1.0f,
        localZ + 1.0f,
        tileMaxU,
        tileMinV,
        localX + 1.0f - epsilon,
        localY + 1.0f,
        localZ + 0.0f,
        tileMinU,
        tileMinV,
        localX + 1.0f - epsilon,
        localY + 0.0f,
        localZ + 0.0f,
        tileMinU,
        tileMaxV,
        kDefaultVertexColor,
        vertices,
        indices);
  } else if (metadata == 3) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        localY + 0.0f,
        localZ + epsilon,
        tileMaxU,
        tileMaxV,
        localX + 1.0f,
        localY + 1.0f,
        localZ + epsilon,
        tileMaxU,
        tileMinV,
        localX + 0.0f,
        localY + 1.0f,
        localZ + epsilon,
        tileMinU,
        tileMinV,
        localX + 0.0f,
        localY + 0.0f,
        localZ + epsilon,
        tileMinU,
        tileMaxV,
        kDefaultVertexColor,
        vertices,
        indices);
  } else if (metadata == 2) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        localY + 1.0f,
        localZ + 1.0f - epsilon,
        tileMinU,
        tileMinV,
        localX + 1.0f,
        localY + 0.0f,
        localZ + 1.0f - epsilon,
        tileMinU,
        tileMaxV,
        localX + 0.0f,
        localY + 0.0f,
        localZ + 1.0f - epsilon,
        tileMaxU,
        tileMaxV,
        localX + 0.0f,
        localY + 1.0f,
        localZ + 1.0f - epsilon,
        tileMaxU,
        tileMinV,
        kDefaultVertexColor,
        vertices,
        indices);
  }
}

void appendRailGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint8_t terrainTileIndex = cell.terrainTiles[0];
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  int metadata = cell.blockMetadata;
  if (cell.blockId == kGoldenRailBlockId || cell.blockId == kDetectorRailBlockId) {
    metadata &= 7;
  }

  float x0 = localX + 1.0f;
  float x1 = localX + 1.0f;
  float x2 = localX + 0.0f;
  float x3 = localX + 0.0f;
  float z0 = localZ + 0.0f;
  float z1 = localZ + 1.0f;
  float z2 = localZ + 1.0f;
  float z3 = localZ + 0.0f;
  float y0 = localY + 0.0625f;
  float y1 = localY + 0.0625f;
  float y2 = localY + 0.0625f;
  float y3 = localY + 0.0625f;

  if (metadata == 1 || metadata == 2 || metadata == 3 || metadata == 7) {
    x0 = localX + 1.0f;
    x1 = localX + 0.0f;
    x2 = localX + 0.0f;
    x3 = localX + 1.0f;
    z0 = localZ + 1.0f;
    z1 = localZ + 1.0f;
    z2 = localZ + 0.0f;
    z3 = localZ + 0.0f;
  } else if (metadata == 8) {
    x0 = localX + 0.0f;
    x1 = localX + 0.0f;
    x2 = localX + 1.0f;
    x3 = localX + 1.0f;
    z0 = localZ + 1.0f;
    z1 = localZ + 0.0f;
    z2 = localZ + 0.0f;
    z3 = localZ + 1.0f;
  } else if (metadata == 9) {
    x0 = localX + 0.0f;
    x1 = localX + 1.0f;
    x2 = localX + 1.0f;
    x3 = localX + 0.0f;
    z0 = localZ + 0.0f;
    z1 = localZ + 0.0f;
    z2 = localZ + 1.0f;
    z3 = localZ + 1.0f;
  }

  if (metadata == 2 || metadata == 4) {
    y0 += 1.0f;
    y3 += 1.0f;
  } else if (metadata == 3 || metadata == 5) {
    y1 += 1.0f;
    y2 += 1.0f;
  }

  appendCrossedQuadSheet(
      x0,
      y0,
      z0,
      tileMaxU,
      tileMinV,
      x1,
      y1,
      z1,
      tileMaxU,
      tileMaxV,
      x2,
      y2,
      z2,
      tileMinU,
      tileMaxV,
      x3,
      y3,
      z3,
      tileMinU,
      tileMinV,
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendFenceGeometry(
    bool connectWest,
    bool connectEast,
    bool connectNorth,
    bool connectSouth,
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendBoxGeometry(
      localX + 0.375f,
      localY + 0.0f,
      localZ + 0.375f,
      localX + 0.625f,
      localY + 1.0f,
      localZ + 0.625f,
      cell.terrainTiles,
      kDefaultVertexColor,
      vertices,
      indices);

  bool connectX = connectWest || connectEast;
  bool connectZ = connectNorth || connectSouth;
  if (!connectX && !connectZ) {
    connectX = true;
  }

  const float xMin = connectWest ? 0.0f : 0.4375f;
  const float xMax = connectEast ? 1.0f : 0.5625f;
  const float zMin = connectNorth ? 0.0f : 0.4375f;
  const float zMax = connectSouth ? 1.0f : 0.5625f;

  if (connectX) {
    appendBoxGeometry(
        localX + xMin,
        localY + 0.75f,
        localZ + 0.4375f,
        localX + xMax,
        localY + 0.9375f,
        localZ + 0.5625f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
    appendBoxGeometry(
        localX + xMin,
        localY + 0.375f,
        localZ + 0.4375f,
        localX + xMax,
        localY + 0.5625f,
        localZ + 0.5625f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }

  if (connectZ) {
    appendBoxGeometry(
        localX + 0.4375f,
        localY + 0.75f,
        localZ + zMin,
        localX + 0.5625f,
        localY + 0.9375f,
        localZ + zMax,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
    appendBoxGeometry(
        localX + 0.4375f,
        localY + 0.375f,
        localZ + zMin,
        localX + 0.5625f,
        localY + 0.5625f,
        localZ + zMax,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }
}

void appendFastCloudGeometry(
    float cameraX,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
    float colorR,
    float colorG,
    float colorB,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t vertexColor = packVertexColorRgba(colorR, colorG, colorB, kCloudAlpha);
  const float uOffset = (cameraX + cloudScroll) * kFastCloudUvScale;
  const float vOffset = cameraZ * kFastCloudUvScale;

  for (float x = -kFastCloudRadius; x < kFastCloudRadius; x += kFastCloudTileSize) {
    for (float z = -kFastCloudRadius; z < kFastCloudRadius; z += kFastCloudTileSize) {
      const float worldX0 = cameraX + x;
      const float worldX1 = worldX0 + kFastCloudTileSize;
      const float worldZ0 = cameraZ + z;
      const float worldZ1 = worldZ0 + kFastCloudTileSize;
      const float u0 = x * kFastCloudUvScale + uOffset;
      const float u1 = (x + kFastCloudTileSize) * kFastCloudUvScale + uOffset;
      const float v0 = z * kFastCloudUvScale + vOffset;
      const float v1 = (z + kFastCloudTileSize) * kFastCloudUvScale + vOffset;

      appendCloudQuad(
          worldX0,
          cloudHeight,
          worldZ1,
          u0,
          v1,
          worldX1,
          cloudHeight,
          worldZ1,
          u1,
          v1,
          worldX1,
          cloudHeight,
          worldZ0,
          u1,
          v0,
          worldX0,
          cloudHeight,
          worldZ0,
          u0,
          v0,
          0.0f,
          1.0f,
          0.0f,
          vertexColor,
          vertices,
          indices);
    }
  }
}

void appendFancyCloudGeometry(
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
    float colorR,
    float colorG,
    float colorB,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float bottomY = cloudHeight;
  const float topY = cloudHeight + kFancyCloudThickness - kFancyCloudInset;
  const float relativeCloudHeight = cloudHeight - cameraY;
  const float xPhase = (cameraX + cloudScroll) / kFancyCloudScale;
  const float zPhase = cameraZ / kFancyCloudScale + 0.33f;
  const float floorX = std::floor(xPhase);
  const float floorZ = std::floor(zPhase);
  const float fracX = xPhase - floorX;
  const float fracZ = zPhase - floorZ;
  const float baseU = floorX * kFancyCloudUvScale;
  const float baseV = floorZ * kFancyCloudUvScale;

  const std::uint32_t bottomColor = packVertexColorRgba(colorR * 0.7f, colorG * 0.7f, colorB * 0.7f, kCloudAlpha);
  const std::uint32_t topColor = packVertexColorRgba(colorR, colorG, colorB, kCloudAlpha);
  const std::uint32_t xSideColor = packVertexColorRgba(colorR * 0.9f, colorG * 0.9f, colorB * 0.9f, kCloudAlpha);
  const std::uint32_t zSideColor = packVertexColorRgba(colorR * 0.8f, colorG * 0.8f, colorB * 0.8f, kCloudAlpha);

  for (int cellX = -kFancyCloudRadiusCells + 1; cellX <= kFancyCloudRadiusCells; ++cellX) {
    for (int cellZ = -kFancyCloudRadiusCells + 1; cellZ <= kFancyCloudRadiusCells; ++cellZ) {
      const float tileX = static_cast<float>(cellX) * kFancyCloudCellSize;
      const float tileZ = static_cast<float>(cellZ) * kFancyCloudCellSize;
      const float x0 = cameraX + (tileX - fracX) * kFancyCloudScale;
      const float x1 = cameraX + (tileX + kFancyCloudCellSize - fracX) * kFancyCloudScale;
      const float z0 = cameraZ + (tileZ - fracZ) * kFancyCloudScale;
      const float z1 = cameraZ + (tileZ + kFancyCloudCellSize - fracZ) * kFancyCloudScale;
      const float u0 = tileX * kFancyCloudUvScale + baseU;
      const float u1 = (tileX + kFancyCloudCellSize) * kFancyCloudUvScale + baseU;
      const float v0 = tileZ * kFancyCloudUvScale + baseV;
      const float v1 = (tileZ + kFancyCloudCellSize) * kFancyCloudUvScale + baseV;

      if (relativeCloudHeight > -kFancyCloudThickness - 1.0f) {
        appendCloudQuad(
            x0,
            bottomY,
            z1,
            u0,
            v1,
            x1,
            bottomY,
            z1,
            u1,
            v1,
            x1,
            bottomY,
            z0,
            u1,
            v0,
            x0,
            bottomY,
            z0,
            u0,
            v0,
            0.0f,
            -1.0f,
            0.0f,
            bottomColor,
            vertices,
            indices);
      }

      if (relativeCloudHeight <= kFancyCloudThickness + 1.0f) {
        appendCloudQuad(
            x0,
            topY,
            z1,
            u0,
            v1,
            x1,
            topY,
            z1,
            u1,
            v1,
            x1,
            topY,
            z0,
            u1,
            v0,
            x0,
            topY,
            z0,
            u0,
            v0,
            0.0f,
            1.0f,
            0.0f,
            topColor,
            vertices,
            indices);
      }

      if (cellX > -1) {
        for (int strip = 0; strip < static_cast<int>(kFancyCloudCellSize); ++strip) {
          const float stripBase = tileX + static_cast<float>(strip);
          const float worldX = cameraX + (stripBase - fracX) * kFancyCloudScale;
          const float stripU = (stripBase + 0.5f) * kFancyCloudUvScale + baseU;
          appendCloudQuad(
              worldX,
              bottomY,
              z1,
              stripU,
              v1,
              worldX,
              topY,
              z1,
              stripU,
              v1,
              worldX,
              topY,
              z0,
              stripU,
              v0,
              worldX,
              bottomY,
              z0,
              stripU,
              v0,
              -1.0f,
              0.0f,
              0.0f,
              xSideColor,
              vertices,
              indices);
        }
      }

      if (cellX <= 1) {
        for (int strip = 0; strip < static_cast<int>(kFancyCloudCellSize); ++strip) {
          const float stripBase = tileX + static_cast<float>(strip) + 1.0f - kFancyCloudInset;
          const float worldX = cameraX + (stripBase - fracX) * kFancyCloudScale;
          const float stripU = (tileX + static_cast<float>(strip) + 0.5f) * kFancyCloudUvScale + baseU;
          appendCloudQuad(
              worldX,
              bottomY,
              z1,
              stripU,
              v1,
              worldX,
              topY,
              z1,
              stripU,
              v1,
              worldX,
              topY,
              z0,
              stripU,
              v0,
              worldX,
              bottomY,
              z0,
              stripU,
              v0,
              1.0f,
              0.0f,
              0.0f,
              xSideColor,
              vertices,
              indices);
        }
      }

      if (cellZ > -1) {
        for (int strip = 0; strip < static_cast<int>(kFancyCloudCellSize); ++strip) {
          const float stripBase = tileZ + static_cast<float>(strip);
          const float worldZ = cameraZ + (stripBase - fracZ) * kFancyCloudScale;
          const float stripV = (stripBase + 0.5f) * kFancyCloudUvScale + baseV;
          appendCloudQuad(
              x0,
              topY,
              worldZ,
              u0,
              stripV,
              x1,
              topY,
              worldZ,
              u1,
              stripV,
              x1,
              bottomY,
              worldZ,
              u1,
              stripV,
              x0,
              bottomY,
              worldZ,
              u0,
              stripV,
              0.0f,
              0.0f,
              -1.0f,
              zSideColor,
              vertices,
              indices);
        }
      }

      if (cellZ <= 1) {
        for (int strip = 0; strip < static_cast<int>(kFancyCloudCellSize); ++strip) {
          const float stripBase = tileZ + static_cast<float>(strip) + 1.0f - kFancyCloudInset;
          const float worldZ = cameraZ + (stripBase - fracZ) * kFancyCloudScale;
          const float stripV = (tileZ + static_cast<float>(strip) + 0.5f) * kFancyCloudUvScale + baseV;
          appendCloudQuad(
              x0,
              topY,
              worldZ,
              u0,
              stripV,
              x1,
              topY,
              worldZ,
              u1,
              stripV,
              x1,
              bottomY,
              worldZ,
              u1,
              stripV,
              x0,
              bottomY,
              worldZ,
              u0,
              stripV,
              0.0f,
              0.0f,
              1.0f,
              zSideColor,
              vertices,
              indices);
        }
      }
    }
  }
}

void appendWaterGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float heightNorthWest = cell.liquidHeights[0];
  const float heightNorthEast = cell.liquidHeights[1];
  const float heightSouthEast = cell.liquidHeights[2];
  const float heightSouthWest = cell.liquidHeights[3];

  if ((cell.liquidVisibilityMask & (1 << 1)) != 0) {
    std::uint8_t terrainTileIndex = cell.terrainTiles[1];
    double uvCenterU = static_cast<double>((terrainTileIndex & 0x0F) * 16 + 8) / kAtlasSizePixels;
    double uvCenterV = static_cast<double>((terrainTileIndex & 0xF0) + 8) / kAtlasSizePixels;
    float flowAngle = cell.liquidFlowAngle;
    if (flowAngle > -999.0f) {
      terrainTileIndex = cell.terrainTiles[2];
      uvCenterU = static_cast<double>((terrainTileIndex & 0x0F) * 16 + 16) / kAtlasSizePixels;
      uvCenterV = static_cast<double>((terrainTileIndex & 0xF0) + 16) / kAtlasSizePixels;
    } else {
      flowAngle = 0.0f;
    }

    const float sinAngle = std::sin(flowAngle) * 8.0f / kAtlasSizePixels;
    const float cosAngle = std::cos(flowAngle) * 8.0f / kAtlasSizePixels;
    appendWaterQuad(
        localX + 0.0f,
        localY + heightNorthWest,
        localZ + 0.0f,
        static_cast<float>(uvCenterU - cosAngle - sinAngle),
        static_cast<float>(uvCenterV - cosAngle + sinAngle),
        localX + 0.0f,
        localY + heightSouthWest,
        localZ + 1.0f,
        static_cast<float>(uvCenterU - cosAngle + sinAngle),
        static_cast<float>(uvCenterV + cosAngle + sinAngle),
        localX + 1.0f,
        localY + heightSouthEast,
        localZ + 1.0f,
        static_cast<float>(uvCenterU + cosAngle + sinAngle),
        static_cast<float>(uvCenterV + cosAngle - sinAngle),
        localX + 1.0f,
        localY + heightNorthEast,
        localZ + 0.0f,
        static_cast<float>(uvCenterU + cosAngle - sinAngle),
        static_cast<float>(uvCenterV - cosAngle - sinAngle),
        0.0f,
        1.0f,
        0.0f,
        vertices,
        indices);
  }

  if ((cell.liquidVisibilityMask & (1 << 0)) != 0) {
    appendFaceGeometry(
        4,
        localX,
        localY,
        localZ,
        cell.terrainTiles[0],
        kDefaultVertexColor,
        0.0f,
        vertices,
        indices);
  }

  for (int sideIndex = 0; sideIndex < 4; ++sideIndex) {
    const int minecraftSide = sideIndex + 2;
    if ((cell.liquidVisibilityMask & (1 << minecraftSide)) == 0) {
      continue;
    }

    const std::uint8_t terrainTileIndex = cell.terrainTiles[minecraftSide];
    const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
    const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
    const float tileBaseV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
    const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

    float edgeHeightA = 0.0f;
    float edgeHeightB = 0.0f;
    float x0 = 0.0f;
    float x1 = 0.0f;
    float z0 = 0.0f;
    float z1 = 0.0f;
    float normalX = 0.0f;
    float normalZ = 0.0f;

    if (sideIndex == 0) {
      edgeHeightA = heightNorthWest;
      edgeHeightB = heightNorthEast;
      x0 = localX + 0.0f;
      x1 = localX + 1.0f;
      z0 = localZ + 0.0f;
      z1 = localZ + 0.0f;
      normalZ = -1.0f;
    } else if (sideIndex == 1) {
      edgeHeightA = heightSouthEast;
      edgeHeightB = heightSouthWest;
      x0 = localX + 1.0f;
      x1 = localX + 0.0f;
      z0 = localZ + 1.0f;
      z1 = localZ + 1.0f;
      normalZ = 1.0f;
    } else if (sideIndex == 2) {
      edgeHeightA = heightSouthWest;
      edgeHeightB = heightNorthWest;
      x0 = localX + 0.0f;
      x1 = localX + 0.0f;
      z0 = localZ + 1.0f;
      z1 = localZ + 0.0f;
      normalX = -1.0f;
    } else {
      edgeHeightA = heightNorthEast;
      edgeHeightB = heightSouthEast;
      x0 = localX + 1.0f;
      x1 = localX + 1.0f;
      z0 = localZ + 0.0f;
      z1 = localZ + 1.0f;
      normalX = 1.0f;
    }

    const float tileMinVA = tileBaseV + (1.0f - edgeHeightA) * kAtlasTileSizePixels / kAtlasSizePixels;
    const float tileMinVB = tileBaseV + (1.0f - edgeHeightB) * kAtlasTileSizePixels / kAtlasSizePixels;
    appendWaterQuad(
        x0,
        localY + edgeHeightA,
        z0,
        tileMinU,
        tileMinVA,
        x1,
        localY + edgeHeightB,
        z1,
        tileMaxU,
        tileMinVB,
        x1,
        localY + 0.0f,
        z1,
        tileMaxU,
        tileMaxV,
        x0,
        localY + 0.0f,
        z0,
        tileMinU,
        tileMaxV,
        normalX,
        0.0f,
        normalZ,
        vertices,
        indices);
  }
}

void appendFaceGeometry(
    int faceIndex,
    float localX,
    float localY,
    float localZ,
    std::uint8_t terrainTileIndex,
    std::uint32_t vertexColor,
    float normalOffset,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = localX + kFaceVertexOffsets[faceIndex][vertexIndex][0] + kFaceNormals[faceIndex][0] * normalOffset;
    vertex.position[1] = localY + kFaceVertexOffsets[faceIndex][vertexIndex][1] + kFaceNormals[faceIndex][1] * normalOffset;
    vertex.position[2] = localZ + kFaceVertexOffsets[faceIndex][vertexIndex][2] + kFaceNormals[faceIndex][2] * normalOffset;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    vertex.texcoord[0] = kFaceTexcoords[faceIndex][vertexIndex][0] == 0.0f ? tileMinU : tileMaxU;
    vertex.texcoord[1] = kFaceTexcoords[faceIndex][vertexIndex][1] == 0.0f ? tileMinV : tileMaxV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
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
  std::scoped_lock lock(mutex_);

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

  if (!createOutputWindow(sourceHwnd_)) {
    return false;
  }

  if (remixDllPath.empty()) {
    remixDllPath = resolveRemixDllPath();
  }

  if (!loadRemix(remixDllPath)) {
    destroyOutputWindow();
    return false;
  }

  if (!startup(outputHwnd_)) {
    resetLoadedRemix();
    destroyOutputWindow();
    return false;
  }

  initializeTerrainMaterials();

  initialized_ = true;
  log("Remix renderer initialized in separate output window");
  return true;
}

void RemixRenderer::shutdown() {
  std::scoped_lock lock(mutex_);

  if (initialized_ && remix_.Shutdown) {
    for (auto& [chunkKey, meshData] : chunkMeshes_) {
      destroyChunkMesh(meshData);
    }
    destroyCloudMesh();
    destroyTerrainMaterials();
    remix_.Shutdown();
  }
  resetLoadedRemix();
  destroyOutputWindow();
  initialized_ = false;
  sourceHwnd_ = nullptr;
  chunkBuildActive_ = false;
  activeChunkBuild_ = {};
  activeChunkBlocks_.clear();
  chunkMeshes_.clear();
  dynamicEntityMeshes_.clear();
  dynamicEntityMaterialHandles_.clear();
  activeDynamicEntity_ = {};
  torchLights_.clear();
  nextChunkMeshHash_ = 1;
  presentedFrames_ = 0;
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  lastSubmittedCloudQuadCount_ = 0;
  lastSubmittedDynamicEntityQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  terrainAtlasPath_.clear();
  cloudTexturePath_.clear();
  nextCloudMeshHash_ = 1;
  nextDynamicEntityMeshHash_ = 1;
  cloudQuadCount_ = 0;
  lastError_.clear();
}

void RemixRenderer::resize(std::uint32_t width, std::uint32_t height) {
  std::scoped_lock lock(mutex_);
  width_ = width == 0 ? 1 : width;
  height_ = height == 0 ? 1 : height;
  camera_.aspect = static_cast<float>(width_) / static_cast<float>(height_);
  updateOutputWindowSize();
}

void RemixRenderer::updateCamera(const CameraState& camera) {
  std::scoped_lock lock(mutex_);
  camera_ = camera;
}

void RemixRenderer::updateCloudLayer(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
    float colorR,
    float colorG,
    float colorB) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  rebuildCloudMesh(fancy, cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB);
}

void RemixRenderer::clearCloudLayer() {
  std::scoped_lock lock(mutex_);
  destroyCloudMesh();
}

void RemixRenderer::beginDynamicEntityFrame() {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  destroyDynamicEntityMeshes();
  activeDynamicEntity_ = {};
}

void RemixRenderer::beginDynamicEntity(int entityId) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  activeDynamicEntity_ = {};
  activeDynamicEntity_.entityId = entityId;
  activeDynamicEntity_.active = entityId >= 0;
  activeDynamicEntity_.quads.reserve(256);
}

void RemixRenderer::setDynamicEntityTexture(const std::string& texturePath) {
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !activeDynamicEntity_.active) {
    return;
  }

  activeDynamicEntity_.currentTexturePath = texturePath;
}

void RemixRenderer::captureDynamicEntityQuad(
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
    std::uint32_t colorRgba) {
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !activeDynamicEntity_.active || activeDynamicEntity_.currentTexturePath.empty()) {
    return;
  }

  DynamicEntityQuad quad;
  quad.positions = {
      x0, y0, z0,
      x1, y1, z1,
      x2, y2, z2,
      x3, y3, z3,
  };
  quad.texcoords = {
      u0, v0,
      u1, v1,
      u2, v2,
      u3, v3,
  };
  quad.color = colorRgba;
  quad.texturePath = activeDynamicEntity_.currentTexturePath;
  activeDynamicEntity_.quads.push_back(std::move(quad));
}

void RemixRenderer::endDynamicEntity() {
  std::scoped_lock lock(mutex_);

  if (!activeDynamicEntity_.active) {
    return;
  }

  if (!activeDynamicEntity_.quads.empty()) {
    rebuildDynamicEntityMesh(activeDynamicEntity_.entityId, activeDynamicEntity_.quads);
  }

  activeDynamicEntity_ = {};
}

bool RemixRenderer::beginChunkBuild(
    int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ, int renderPass) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return false;
  }

  if (renderPass != 0 && renderPass != 1) {
    return false;
  }

  chunkBuildActive_ = true;
  activeChunkBuild_ = {};
  activeChunkBuild_.origin[0] = originX;
  activeChunkBuild_.origin[1] = originY;
  activeChunkBuild_.origin[2] = originZ;
  activeChunkBuild_.size[0] = sizeX;
  activeChunkBuild_.size[1] = sizeY;
  activeChunkBuild_.size[2] = sizeZ;
  activeChunkBuild_.renderPass = renderPass;
  activeChunkBlocks_.clear();
  return true;
}

void RemixRenderer::captureBlock(
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
  float liquidFlowAngle) {
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !chunkBuildActive_) {
    return;
  }

  ++activeChunkBuild_.blockCount;
  ++capturedBlocks_;
  if (blockId >= 0 && blockId < static_cast<int>(activeChunkBuild_.blockIdCounts.size())) {
    ++activeChunkBuild_.blockIdCounts[static_cast<std::size_t>(blockId)];
  }

  if (!shouldCaptureBlock(blockId, renderType, activeChunkBuild_.renderPass) || activeChunkBlocks_.size() >= kMaxOpaqueBlocksPerChunk) {
    return;
  }

  CapturedBlockInstance block {};
  block.position[0] = blockX;
  block.position[1] = blockY;
  block.position[2] = blockZ;
  block.blockId = blockId;
  block.blockMetadata = blockMetadata;
  block.terrainTiles = {
      static_cast<std::uint8_t>(texture0 & 0xFF),
      static_cast<std::uint8_t>(texture1 & 0xFF),
      static_cast<std::uint8_t>(texture2 & 0xFF),
      static_cast<std::uint8_t>(texture3 & 0xFF),
      static_cast<std::uint8_t>(texture4 & 0xFF),
      static_cast<std::uint8_t>(texture5 & 0xFF),
  };
  block.renderType = renderType;
  block.materialClass = materialClassForBlock(blockId, renderType);
  block.liquidVisibilityMask = static_cast<std::uint8_t>(liquidVisibilityMask & 0x3F);
  block.liquidHeights = {liquidHeight0, liquidHeight1, liquidHeight2, liquidHeight3};
  block.liquidFlowAngle = liquidFlowAngle;
  block.blockColor = static_cast<std::uint32_t>(blockColorRgb) & 0x00FFFFFFu;
  activeChunkBlocks_.push_back(block);
}

void RemixRenderer::endChunkBuild(bool emittedGeometry) {
  std::scoped_lock lock(mutex_);

  if (!chunkBuildActive_) {
    return;
  }

  const ChunkKey chunkKey = makeChunkKey(activeChunkBuild_);
  if (emittedGeometry && !activeChunkBlocks_.empty()) {
    ChunkMeshData& meshData = chunkMeshes_[chunkKey];
    if (!rebuildChunkMesh(chunkKey, activeChunkBlocks_, meshData)) {
      activeChunkBlocks_.clear();
      chunkBuildActive_ = false;
      activeChunkBuild_ = {};
      return;
    }
    refreshNeighborChunkMeshes(chunkKey);
  } else {
    auto chunkIt = chunkMeshes_.find(chunkKey);
    if (chunkIt != chunkMeshes_.end()) {
      destroyChunkMesh(chunkIt->second);
      chunkMeshes_.erase(chunkIt);
      refreshNeighborChunkMeshes(chunkKey);
    }
  }

  ++capturedChunkBuilds_;
  const bool shouldLog = capturedChunkBuilds_ <= 8 || capturedChunkBuilds_ % 128 == 0;
  if (shouldLog) {
    std::ostringstream stream;
    stream << "chunk build origin=("
           << activeChunkBuild_.origin[0] << ", "
           << activeChunkBuild_.origin[1] << ", "
           << activeChunkBuild_.origin[2] << ") size=("
           << activeChunkBuild_.size[0] << ", "
           << activeChunkBuild_.size[1] << ", "
           << activeChunkBuild_.size[2] << ") pass="
           << activeChunkBuild_.renderPass
           << " blocks=" << activeChunkBuild_.blockCount
           << " capturedOpaqueBlocks=" << activeChunkBlocks_.size()
           << " uniqueBlockIds=" << countUniqueBlockIds(activeChunkBuild_)
           << " emittedGeometry=" << (emittedGeometry ? "true" : "false")
           << " storedChunks=" << chunkMeshes_.size()
           << " totalCapturedBlocks=" << capturedBlocks_;
    log(stream.str());
  }

  chunkBuildActive_ = false;
  activeChunkBuild_ = {};
  activeChunkBlocks_.clear();
}

bool RemixRenderer::present() {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("present called before initialize");
    return false;
  }

  pumpOutputWindowMessages();

  if (!submitCamera()) {
    return false;
  }

  if (!drawCapturedGeometry()) {
    return false;
  }

  const remixapi_ErrorCode result = remix_.Present(nullptr);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Present failed: " + errorCodeToString(result));
    return false;
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

std::filesystem::path RemixRenderer::resolveRemixDllPath() {
  std::vector<std::filesystem::path> attemptedPaths;

  if (const char* envValue = std::getenv("MCRTX_REMIX_DLL"); envValue != nullptr && envValue[0] != '\0') {
    std::filesystem::path envPath = std::filesystem::u8path(envValue);
    attemptedPaths.push_back(envPath);
    if (std::filesystem::exists(envPath)) {
      log("Using Remix runtime from MCRTX_REMIX_DLL: " + envPath.string());
      return envPath;
    }
  }

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / "d3d9.dll");
    attemptedPaths.push_back(moduleDirectory / "bin" / "d3d9.dll");
  }

  attemptedPaths.push_back(std::filesystem::path(L"d3d9.dll"));
  attemptedPaths.push_back(std::filesystem::path(L"bin") / "d3d9.dll");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      log("Using Remix runtime DLL: " + path.string());
      return path;
    }
  }

  std::ostringstream stream;
  stream << "Could not find Remix runtime d3d9.dll. Tried:";
  for (const auto& path : attemptedPaths) {
    stream << " " << path.string();
  }
  log(stream.str());

  return std::filesystem::path(L"d3d9.dll");
}

std::filesystem::path RemixRenderer::resolveTerrainAtlasPath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"terrain.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"terrain.png");
    attemptedPaths.push_back(moduleDirectory / L"terrain.dds");
    attemptedPaths.push_back(moduleDirectory / L"terrain.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"terrain.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"terrain.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"terrain.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"terrain.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveCloudTexturePath() {
  std::vector<std::filesystem::path> attemptedPaths;

  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"clouds.dds");
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"clouds.png");
    attemptedPaths.push_back(moduleDirectory / L"clouds.dds");
    attemptedPaths.push_back(moduleDirectory / L"clouds.png");
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"clouds.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"clouds.png");
  attemptedPaths.push_back(std::filesystem::current_path() / L"clouds.dds");
  attemptedPaths.push_back(std::filesystem::current_path() / L"clouds.png");

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

std::filesystem::path RemixRenderer::resolveDynamicEntityTexturePath(const std::string& texturePath) {
  if (texturePath.empty()) {
    return {};
  }

  std::string normalized = texturePath;
  if (!normalized.empty() && normalized.front() == '/') {
    normalized.erase(normalized.begin());
  }

  std::filesystem::path relativePath(normalized);
  relativePath.make_preferred();
  std::filesystem::path ddsPath = relativePath;
  ddsPath.replace_extension(L".dds");

  std::vector<std::filesystem::path> attemptedPaths;
  const std::filesystem::path moduleDirectory = getCurrentModuleDirectory();
  if (!moduleDirectory.empty()) {
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"entities" / ddsPath);
    attemptedPaths.push_back(moduleDirectory / L"mcrtx_assets" / L"entities" / relativePath);
  }

  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"entities" / ddsPath);
  attemptedPaths.push_back(std::filesystem::current_path() / L"mcrtx_assets" / L"entities" / relativePath);

  for (const auto& path : attemptedPaths) {
    if (std::filesystem::exists(path)) {
      return path;
    }
  }

  return {};
}

bool RemixRenderer::createOutputWindow(HWND sourceHwnd) {
  if (outputHwnd_ != nullptr) {
    updateOutputWindowSize();
    ShowWindow(outputHwnd_, SW_SHOWNOACTIVATE);
    return true;
  }

  if (!ensureOutputWindowClassRegistered()) {
    setError("Failed to register Remix output window class");
    return false;
  }

  RECT sourceRect {};
  const bool hasSourceRect = GetWindowRect(sourceHwnd, &sourceRect) == TRUE;

  RECT windowRect {0, 0, static_cast<LONG>(width_), static_cast<LONG>(height_)};
  AdjustWindowRectEx(&windowRect, WS_OVERLAPPEDWINDOW, FALSE, WS_EX_APPWINDOW);

  const int outerWidth = windowRect.right - windowRect.left;
  const int outerHeight = windowRect.bottom - windowRect.top;
  const int windowX = hasSourceRect ? sourceRect.right + 24 : CW_USEDEFAULT;
  const int windowY = hasSourceRect ? sourceRect.top : CW_USEDEFAULT;

  outputHwnd_ = CreateWindowExW(
      WS_EX_APPWINDOW,
      kRemixWindowClassName,
      kRemixWindowTitle,
      WS_OVERLAPPEDWINDOW,
      windowX,
      windowY,
      outerWidth,
      outerHeight,
      nullptr,
      nullptr,
      getCurrentModuleHandle(),
      nullptr);

  if (outputHwnd_ == nullptr) {
    setError("Failed to create Remix output window");
    return false;
  }

  ShowWindow(outputHwnd_, SW_SHOWNOACTIVATE);
  UpdateWindow(outputHwnd_);
  log("Created Remix output window");
  return true;
}

void RemixRenderer::destroyOutputWindow() {
  if (outputHwnd_ == nullptr) {
    return;
  }

  DestroyWindow(outputHwnd_);
  outputHwnd_ = nullptr;
}

void RemixRenderer::pumpOutputWindowMessages() {
  if (outputHwnd_ == nullptr) {
    return;
  }

  MSG message {};
  while (PeekMessageW(&message, outputHwnd_, 0, 0, PM_REMOVE)) {
    TranslateMessage(&message);
    DispatchMessageW(&message);
  }
}

void RemixRenderer::updateOutputWindowSize() const {
  if (outputHwnd_ == nullptr) {
    return;
  }

  RECT windowRect {0, 0, static_cast<LONG>(width_), static_cast<LONG>(height_)};
  AdjustWindowRectEx(&windowRect, WS_OVERLAPPEDWINDOW, FALSE, WS_EX_APPWINDOW);
  const int outerWidth = windowRect.right - windowRect.left;
  const int outerHeight = windowRect.bottom - windowRect.top;

  SetWindowPos(
      outputHwnd_,
      nullptr,
      0,
      0,
      outerWidth,
      outerHeight,
      SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE);
}

bool RemixRenderer::initializeTerrainMaterials() {
  destroyTerrainMaterials();
  terrainAtlasPath_ = resolveTerrainAtlasPath();
  cloudTexturePath_ = resolveCloudTexturePath();
  if (terrainAtlasPath_.empty()) {
    log("Terrain atlas asset not found; continuing without Remix materials");
    return false;
  }

  const auto createTerrainMaterial = [this](std::size_t materialClass, bool cutout) {
    remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
    opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
    opaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
    opaqueInfo.opacityConstant = 1.0f;
    opaqueInfo.roughnessConstant = 1.0f;
    opaqueInfo.metallicConstant = 0.0f;
    opaqueInfo.useDrawCallAlphaState = FALSE;
    opaqueInfo.alphaTestType = cutout ? 4 : 7;
    opaqueInfo.alphaReferenceValue = cutout ? 1 : 0;

    remixapi_MaterialInfo materialInfo {};
    materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
    materialInfo.pNext = &opaqueInfo;
  materialInfo.hash = materialClass == kWaterTerrainMaterialClass
    ? kWaterTerrainMaterialHash
    : (cutout ? kCutoutTerrainMaterialHash : kOpaqueTerrainMaterialHash);
    materialInfo.albedoTexture = terrainAtlasPath_.c_str();
    materialInfo.emissiveIntensity = 0.0f;
    materialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
    materialInfo.filterMode = 0;
    materialInfo.wrapModeU = 1;
    materialInfo.wrapModeV = 1;

    remixapi_MaterialHandle materialHandle = nullptr;
    const remixapi_ErrorCode result = remix_.CreateMaterial(&materialInfo, &materialHandle);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("CreateMaterial failed: " + errorCodeToString(result));
      return false;
    }

    terrainMaterialHandles_[materialClass] = materialHandle;
    return true;
  };

  const bool opaqueCreated = createTerrainMaterial(kOpaqueTerrainMaterialClass, false);
  const bool cutoutCreated = createTerrainMaterial(kCutoutTerrainMaterialClass, true);
  const bool waterCreated = createTerrainMaterial(kWaterTerrainMaterialClass, false);
  if (opaqueCreated) {
    log("Initialized terrain atlas materials from " + terrainAtlasPath_.string());
  }
  if (!cutoutCreated) {
    log("Cutout terrain material unavailable; cutout faces will use fallback material");
  }
  if (!waterCreated) {
    log("Water terrain material unavailable; water faces will be skipped");
  }

  if (cloudTexturePath_.empty()) {
    log("Cloud texture asset not found; cloud layer will be skipped");
    return opaqueCreated;
  }

  remixapi_MaterialInfoOpaqueEXT cloudOpaqueInfo {};
  cloudOpaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  cloudOpaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
  cloudOpaqueInfo.opacityConstant = 1.0f;
  cloudOpaqueInfo.roughnessConstant = 1.0f;
  cloudOpaqueInfo.metallicConstant = 0.0f;
  cloudOpaqueInfo.useDrawCallAlphaState = FALSE;
  cloudOpaqueInfo.alphaTestType = 4;
  cloudOpaqueInfo.alphaReferenceValue = 2;

  remixapi_MaterialInfo cloudMaterialInfo {};
  cloudMaterialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  cloudMaterialInfo.pNext = &cloudOpaqueInfo;
  cloudMaterialInfo.hash = kCloudMaterialHash;
  cloudMaterialInfo.albedoTexture = cloudTexturePath_.c_str();
  cloudMaterialInfo.emissiveIntensity = 0.0f;
  cloudMaterialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
  cloudMaterialInfo.filterMode = 0;
  cloudMaterialInfo.wrapModeU = 1;
  cloudMaterialInfo.wrapModeV = 1;

  const remixapi_ErrorCode cloudResult = remix_.CreateMaterial(&cloudMaterialInfo, &cloudMaterialHandle_);
  if (cloudResult != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(cloudResult));
    log("Cloud material unavailable; cloud layer will be skipped");
    cloudMaterialHandle_ = nullptr;
  } else {
    log("Initialized cloud material from " + cloudTexturePath_.string());
  }
  return opaqueCreated;
}

void RemixRenderer::destroyTerrainMaterials() {
  destroyCloudMesh();
  destroyDynamicEntityMeshes();

  if (remix_.DestroyMaterial != nullptr) {
    for (auto& [texturePath, materialHandle] : dynamicEntityMaterialHandles_) {
      if (materialHandle != nullptr) {
        remix_.DestroyMaterial(materialHandle);
      }
    }
  }
  dynamicEntityMaterialHandles_.clear();

  if (remix_.DestroyMaterial != nullptr && cloudMaterialHandle_ != nullptr) {
    remix_.DestroyMaterial(cloudMaterialHandle_);
    cloudMaterialHandle_ = nullptr;
  }

  if (remix_.DestroyMaterial == nullptr) {
    terrainMaterialHandles_ = {};
    return;
  }

  for (remixapi_MaterialHandle& materialHandle : terrainMaterialHandles_) {
    if (materialHandle != nullptr) {
      remix_.DestroyMaterial(materialHandle);
      materialHandle = nullptr;
    }
  }
}

remixapi_MaterialHandle RemixRenderer::acquireDynamicEntityMaterial(const std::string& texturePath) {
  const auto existing = dynamicEntityMaterialHandles_.find(texturePath);
  if (existing != dynamicEntityMaterialHandles_.end()) {
    return existing->second;
  }

  const std::filesystem::path resolvedTexturePath = resolveDynamicEntityTexturePath(texturePath);
  if (resolvedTexturePath.empty()) {
    return nullptr;
  }

  remixapi_MaterialInfoOpaqueEXT opaqueInfo {};
  opaqueInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO_OPAQUE_EXT;
  opaqueInfo.albedoConstant = {1.0f, 1.0f, 1.0f};
  opaqueInfo.opacityConstant = 1.0f;
  opaqueInfo.roughnessConstant = 1.0f;
  opaqueInfo.metallicConstant = 0.0f;
  opaqueInfo.useDrawCallAlphaState = FALSE;
  opaqueInfo.alphaTestType = 4;
  opaqueInfo.alphaReferenceValue = 1;

  remixapi_MaterialInfo materialInfo {};
  materialInfo.sType = REMIXAPI_STRUCT_TYPE_MATERIAL_INFO;
  materialInfo.pNext = &opaqueInfo;
  materialInfo.hash = kDynamicEntityMaterialHashSeed ^ static_cast<std::uint64_t>(std::hash<std::string> {}(texturePath));
  materialInfo.albedoTexture = resolvedTexturePath.c_str();
  materialInfo.emissiveIntensity = 0.0f;
  materialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
  materialInfo.filterMode = 0;
  materialInfo.wrapModeU = 1;
  materialInfo.wrapModeV = 1;

  remixapi_MaterialHandle materialHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMaterial(&materialInfo, &materialHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMaterial failed: " + errorCodeToString(result));
    return nullptr;
  }

  dynamicEntityMaterialHandles_.emplace(texturePath, materialHandle);
  return materialHandle;
}

bool RemixRenderer::createTorchLight(const TorchLightPlacement& placement) {
  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {
      placement.lightX,
      placement.lightY,
      placement.lightZ,
  };
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeTorchLightHash(placement.blockPosition);
  lightInfo.radiance = kTorchLightRadiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;

  remixapi_LightHandle lightHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateLight(&lightInfo, &lightHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateLight failed: " + errorCodeToString(result));
    return false;
  }

  torchLights_[placement.blockPosition] = lightHandle;
  return true;
}

bool RemixRenderer::updateTorchLight(const TorchLightPlacement& placement) {
  const auto lightIt = torchLights_.find(placement.blockPosition);
  if (lightIt == torchLights_.end() || lightIt->second == nullptr) {
    return createTorchLight(placement);
  }

  if (remix_.UpdateLightDefinition == nullptr) {
    destroyTorchLight(placement.blockPosition);
    return createTorchLight(placement);
  }

  remixapi_LightInfoSphereEXT sphereInfo {};
  sphereInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO_SPHERE_EXT;
  sphereInfo.position = {placement.lightX, placement.lightY, placement.lightZ};
  sphereInfo.radius = kTorchLightRadius;
  sphereInfo.shaping_hasvalue = FALSE;
  sphereInfo.volumetricRadianceScale = 1.0f;

  remixapi_LightInfo lightInfo {};
  lightInfo.sType = REMIXAPI_STRUCT_TYPE_LIGHT_INFO;
  lightInfo.pNext = &sphereInfo;
  lightInfo.hash = makeTorchLightHash(placement.blockPosition);
  lightInfo.radiance = kTorchLightRadiance;
  lightInfo.isDynamic = FALSE;
  lightInfo.ignoreViewModel = FALSE;

  const remixapi_ErrorCode result = remix_.UpdateLightDefinition(lightIt->second, &lightInfo);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("UpdateLightDefinition failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::reconcileChunkTorchLights(
    ChunkMeshData& meshData,
    const std::vector<TorchLightPlacement>& desiredTorchLights) {
  if (remix_.CreateLight == nullptr) {
    destroyChunkTorchLights(meshData);
    return true;
  }

  std::vector<WorldBlockPosition> createdLights;
  createdLights.reserve(desiredTorchLights.size());
  for (const TorchLightPlacement& placement : desiredTorchLights) {
    const bool existed = torchLights_.find(placement.blockPosition) != torchLights_.end();
    if (!updateTorchLight(placement)) {
      for (const WorldBlockPosition& createdPosition : createdLights) {
        destroyTorchLight(createdPosition);
      }
      return false;
    }
    if (!existed) {
      createdLights.push_back(placement.blockPosition);
    }
  }

  for (const TorchLightPlacement& placement : meshData.torchLights) {
    if (findTorchLightPlacement(desiredTorchLights, placement.blockPosition) == nullptr) {
      destroyTorchLight(placement.blockPosition);
    }
  }

  meshData.torchLights = desiredTorchLights;
  return true;
}

bool RemixRenderer::rebuildCloudMesh(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
    float colorR,
    float colorG,
    float colorB) {
  if (cloudMaterialHandle_ == nullptr) {
    destroyCloudMesh();
    return true;
  }

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(fancy ? 4096 : 2048);
  indices.reserve(fancy ? 6144 : 3072);

  if (fancy) {
    appendFancyCloudGeometry(cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  } else {
    appendFastCloudGeometry(cameraX, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  }

  if (indices.empty()) {
    destroyCloudMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = cloudMaterialHandle_;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeCloudMeshHash(nextCloudMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMesh(&meshInfo, &newMeshHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyCloudMesh();
  cloudMeshHandle_ = newMeshHandle;
  cloudQuadCount_ = indices.size() / 6;
  return true;
}

bool RemixRenderer::rebuildDynamicEntityMesh(int entityId, const std::vector<DynamicEntityQuad>& quads) {
  if (quads.empty()) {
    return true;
  }

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(8);
  std::unordered_map<std::uintptr_t, std::size_t> surfaceIndexByHandle;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](remixapi_MaterialHandle materialHandle) -> SurfaceBuildBuffers& {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(materialHandle);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    SurfaceBuildBuffers surface;
    surface.materialHandle = materialHandle;
    surface.vertices.reserve(256);
    surface.indices.reserve(384);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  std::size_t quadCount = 0;
  for (const DynamicEntityQuad& quad : quads) {
    remixapi_MaterialHandle materialHandle = acquireDynamicEntityMaterial(quad.texturePath);
    if (materialHandle == nullptr) {
      continue;
    }

    const auto normal = computeQuadNormal(
        quad.positions[0], quad.positions[1], quad.positions[2],
        quad.positions[3], quad.positions[4], quad.positions[5],
        quad.positions[6], quad.positions[7], quad.positions[8]);
    SurfaceBuildBuffers& surface = acquireSurface(materialHandle);
    appendCloudQuad(
        quad.positions[0], quad.positions[1], quad.positions[2], quad.texcoords[0], quad.texcoords[1],
        quad.positions[3], quad.positions[4], quad.positions[5], quad.texcoords[2], quad.texcoords[3],
        quad.positions[6], quad.positions[7], quad.positions[8], quad.texcoords[4], quad.texcoords[5],
        quad.positions[9], quad.positions[10], quad.positions[11], quad.texcoords[6], quad.texcoords[7],
        normal[0], normal[1], normal[2],
        quad.color,
        surface.vertices,
        surface.indices);
    ++quadCount;
  }

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  surfaces.reserve(surfacesToBuild.size());
  for (const SurfaceBuildBuffers& surfaceBuild : surfacesToBuild) {
    if (surfaceBuild.indices.empty()) {
      continue;
    }

    remixapi_MeshInfoSurfaceTriangles surface {};
    surface.vertices_values = surfaceBuild.vertices.data();
    surface.vertices_count = surfaceBuild.vertices.size();
    surface.indices_values = surfaceBuild.indices.data();
    surface.indices_count = surfaceBuild.indices.size();
    surface.skinning_hasvalue = FALSE;
    surface.material = surfaceBuild.materialHandle;
    surfaces.push_back(surface);
  }

  if (surfaces.empty()) {
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeDynamicEntityMeshHash(nextDynamicEntityMeshHash_++);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle meshHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMesh(&meshInfo, &meshHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  DynamicEntityMeshData& meshData = dynamicEntityMeshes_[entityId];
  destroyDynamicEntityMesh(meshData);
  meshData.meshHandle = meshHandle;
  meshData.meshHash = meshInfo.hash;
  meshData.quadCount = quadCount;
  return true;
}

void RemixRenderer::destroyCloudMesh() {
  if (cloudMeshHandle_ != nullptr && remix_.DestroyMesh != nullptr) {
    remix_.DestroyMesh(cloudMeshHandle_);
  }
  cloudMeshHandle_ = nullptr;
  cloudQuadCount_ = 0;
}

void RemixRenderer::destroyChunkMeshHandle(ChunkMeshData& meshData) {
  if (meshData.meshHandle != nullptr && remix_.DestroyMesh != nullptr) {
    remix_.DestroyMesh(meshData.meshHandle);
  }
  meshData.meshHandle = nullptr;
  meshData.meshHash = 0;
}

void RemixRenderer::destroyTorchLight(const WorldBlockPosition& position) {
  const auto lightIt = torchLights_.find(position);
  if (lightIt == torchLights_.end()) {
    return;
  }

  if (lightIt->second != nullptr && remix_.DestroyLight != nullptr) {
    remix_.DestroyLight(lightIt->second);
  }
  torchLights_.erase(lightIt);
}

void RemixRenderer::destroyChunkTorchLights(ChunkMeshData& meshData) {
  for (const TorchLightPlacement& placement : meshData.torchLights) {
    destroyTorchLight(placement.blockPosition);
  }
  meshData.torchLights.clear();
}

void RemixRenderer::destroyDynamicEntityMeshes() {
  for (auto& [entityId, meshData] : dynamicEntityMeshes_) {
    destroyDynamicEntityMesh(meshData);
  }
  dynamicEntityMeshes_.clear();
}

void RemixRenderer::destroyDynamicEntityMesh(DynamicEntityMeshData& meshData) {
  if (meshData.meshHandle != nullptr && remix_.DestroyMesh != nullptr) {
    remix_.DestroyMesh(meshData.meshHandle);
  }
  meshData.meshHandle = nullptr;
  meshData.meshHash = 0;
  meshData.quadCount = 0;
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
  remixapi_StartupInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_STARTUP_INFO;
  info.hwnd = hwnd;
  info.disableSrgbConversionForOutput = FALSE;
  info.forceNoVkSwapchain = FALSE;
  info.editorModeEnabled = FALSE;

  const remixapi_ErrorCode result = remix_.Startup(&info);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("Startup failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::rebuildChunkMesh(
    const ChunkKey& chunkKey,
    const std::vector<CapturedBlockInstance>& blocks,
    ChunkMeshData& meshData) {
  if (blocks.empty()) {
    destroyChunkMesh(meshData);
    meshData.geometryFingerprint = 0;
    meshData.blockCount = 0;
    meshData.occupancy.fill(0);
    meshData.cells.fill(ChunkBlockCell {});
    meshData.hasOccupancy = false;
    return true;
  }

  std::array<std::uint8_t, kBlocksPerChunk> occupancy {};
  std::array<ChunkBlockCell, kBlocksPerChunk> cells {};
  std::size_t occupiedBlocks = 0;
  for (const CapturedBlockInstance& block : blocks) {
    const int localX = block.position[0] - chunkKey.originX;
    const int localY = block.position[1] - chunkKey.originY;
    const int localZ = block.position[2] - chunkKey.originZ;
    if (localX < 0 || localX >= kChunkDimension
        || localY < 0 || localY >= kChunkDimension
        || localZ < 0 || localZ >= kChunkDimension) {
      continue;
    }
    const int occupancyIndex = blockIndex(localX, localY, localZ);
    if (occupancy[occupancyIndex] == 0) {
      occupancy[occupancyIndex] = 1;
      ++occupiedBlocks;
    }
    cells[occupancyIndex].terrainTiles = block.terrainTiles;
    cells[occupancyIndex].materialClass = block.materialClass < kTerrainMaterialClassCount
        ? block.materialClass
        : kOpaqueTerrainMaterialClass;
    cells[occupancyIndex].blockId = static_cast<std::uint8_t>(block.blockId & 0xFF);
      cells[occupancyIndex].blockMetadata = static_cast<std::uint8_t>(block.blockMetadata & 0xFF);
    cells[occupancyIndex].renderType = static_cast<std::uint8_t>(block.renderType & 0xFF);
    cells[occupancyIndex].liquidVisibilityMask = block.liquidVisibilityMask;
    cells[occupancyIndex].liquidHeights = block.liquidHeights;
    cells[occupancyIndex].liquidFlowAngle = block.liquidFlowAngle;
    cells[occupancyIndex].blockColor = block.blockColor;
  }

  if (occupiedBlocks == 0) {
    destroyChunkMesh(meshData);
    meshData.geometryFingerprint = 0;
    meshData.blockCount = 0;
    meshData.occupancy.fill(0);
    meshData.cells.fill(ChunkBlockCell {});
    meshData.hasOccupancy = false;
    return true;
  }

  const std::uint64_t geometryFingerprint = computeChunkFingerprint(occupancy, cells);
  if (meshData.blockCount == occupiedBlocks
      && meshData.geometryFingerprint == geometryFingerprint
      && meshData.hasOccupancy) {
    return true;
  }

  meshData.geometryFingerprint = geometryFingerprint;
  meshData.blockCount = occupiedBlocks;
  meshData.occupancy = occupancy;
  meshData.cells = cells;
  meshData.hasOccupancy = true;
  return rebuildChunkMeshFromData(chunkKey, meshData, false);
}

bool RemixRenderer::rebuildChunkMeshFromData(
    const ChunkKey& chunkKey,
    ChunkMeshData& meshData,
    bool forceRebuild) {
  if (!meshData.hasOccupancy || meshData.blockCount == 0) {
    destroyChunkMesh(meshData);
    return true;
  }

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(8);
  std::unordered_map<std::uintptr_t, std::size_t> surfaceIndexByHandle;
  std::vector<TorchLightPlacement> desiredTorchLights;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](remixapi_MaterialHandle materialHandle) -> SurfaceBuildBuffers& {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(materialHandle);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    SurfaceBuildBuffers surface;
    surface.materialHandle = materialHandle;
    surface.vertices.reserve(512);
    surface.indices.reserve(768);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  const auto hasFenceNeighbor = [this, &chunkKey, &meshData](int worldX, int worldY, int worldZ) -> bool {
    ChunkKey neighborKey = chunkKey;
    int localX = worldX - chunkKey.originX;
    int localY = worldY - chunkKey.originY;
    int localZ = worldZ - chunkKey.originZ;

    while (localX < 0) {
      neighborKey.originX -= kChunkDimension;
      localX += kChunkDimension;
    }
    while (localX >= kChunkDimension) {
      neighborKey.originX += kChunkDimension;
      localX -= kChunkDimension;
    }
    while (localY < 0) {
      neighborKey.originY -= kChunkDimension;
      localY += kChunkDimension;
    }
    while (localY >= kChunkDimension) {
      neighborKey.originY += kChunkDimension;
      localY -= kChunkDimension;
    }
    while (localZ < 0) {
      neighborKey.originZ -= kChunkDimension;
      localZ += kChunkDimension;
    }
    while (localZ >= kChunkDimension) {
      neighborKey.originZ += kChunkDimension;
      localZ -= kChunkDimension;
    }

    const ChunkMeshData* targetMesh = &meshData;
    if (!(neighborKey == chunkKey)) {
      const auto neighborIt = chunkMeshes_.find(neighborKey);
      if (neighborIt == chunkMeshes_.end() || !neighborIt->second.hasOccupancy) {
        return false;
      }
      targetMesh = &neighborIt->second;
    }

    const int neighborIndex = blockIndex(localX, localY, localZ);
    if (targetMesh->occupancy[neighborIndex] == 0) {
      return false;
    }

    const ChunkBlockCell& neighborCell = targetMesh->cells[neighborIndex];
    return neighborCell.blockId == kFenceBlockId && neighborCell.renderType == kFenceBlockRenderType;
  };

  for (int localY = 0; localY < kChunkDimension; ++localY) {
    for (int localZ = 0; localZ < kChunkDimension; ++localZ) {
      for (int localX = 0; localX < kChunkDimension; ++localX) {
        const int cellIndex = blockIndex(localX, localY, localZ);
        if (meshData.occupancy[cellIndex] == 0) {
          continue;
        }

        const ChunkBlockCell& cell = meshData.cells[cellIndex];
        const std::size_t materialClass = cell.materialClass < kTerrainMaterialClassCount
          ? cell.materialClass
          : kOpaqueTerrainMaterialClass;

        if (cell.renderType == kLiquidBlockRenderType && materialClass == kWaterTerrainMaterialClass) {
          SurfaceBuildBuffers& waterSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendWaterGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              waterSurface.vertices,
              waterSurface.indices);
          continue;
        }

        if (isCrossedQuadRenderType(cell.renderType)) {
          SurfaceBuildBuffers& floraSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendCrossedQuadGeometry(
              cell,
              chunkKey.originX + localX,
              chunkKey.originY + localY,
              chunkKey.originZ + localZ,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              floraSurface.vertices,
              floraSurface.indices);
          continue;
        }

        if (isTorchRenderType(cell.renderType)) {
          desiredTorchLights.push_back(makeTorchLightPlacement(
            cell,
            chunkKey.originX + localX,
            chunkKey.originY + localY,
            chunkKey.originZ + localZ));
          SurfaceBuildBuffers& torchSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendTorchGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              torchSurface.vertices,
              torchSurface.indices);
          continue;
        }

        if (isLadderRenderType(cell.renderType)) {
          SurfaceBuildBuffers& ladderSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendLadderGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              ladderSurface.vertices,
              ladderSurface.indices);
          continue;
        }

        if (isRailRenderType(cell.renderType) && isRailBlockId(cell.blockId)) {
          SurfaceBuildBuffers& railSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendRailGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              railSurface.vertices,
              railSurface.indices);
          continue;
        }

        if (isFenceRenderType(cell.renderType) && cell.blockId == kFenceBlockId) {
          const int worldX = chunkKey.originX + localX;
          const int worldY = chunkKey.originY + localY;
          const int worldZ = chunkKey.originZ + localZ;
          SurfaceBuildBuffers& fenceSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendFenceGeometry(
              hasFenceNeighbor(worldX - 1, worldY, worldZ),
              hasFenceNeighbor(worldX + 1, worldY, worldZ),
              hasFenceNeighbor(worldX, worldY, worldZ - 1),
              hasFenceNeighbor(worldX, worldY, worldZ + 1),
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              fenceSurface.vertices,
              fenceSurface.indices);
          continue;
        }

        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
          const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
          const int neighborX = localX + kNeighborOffsets[faceIndex][0];
          const int neighborY = localY + kNeighborOffsets[faceIndex][1];
          const int neighborZ = localZ + kNeighborOffsets[faceIndex][2];

          bool faceOccluded = false;
          const bool neighborInsideChunk =
              neighborX >= 0 && neighborX < kChunkDimension
              && neighborY >= 0 && neighborY < kChunkDimension
              && neighborZ >= 0 && neighborZ < kChunkDimension;
          if (neighborInsideChunk) {
            const int neighborIndex = blockIndex(neighborX, neighborY, neighborZ);
            if (meshData.occupancy[neighborIndex] != 0) {
              faceOccluded = shouldCullFaceAgainstNeighbor(cell, meshData.cells[neighborIndex]);
            }
          } else {
            ChunkKey neighborKey = chunkKey;
            int wrappedX = neighborX;
            int wrappedY = neighborY;
            int wrappedZ = neighborZ;

            if (wrappedX < 0) {
              neighborKey.originX -= kChunkDimension;
              wrappedX += kChunkDimension;
            } else if (wrappedX >= kChunkDimension) {
              neighborKey.originX += kChunkDimension;
              wrappedX -= kChunkDimension;
            }

            if (wrappedY < 0) {
              neighborKey.originY -= kChunkDimension;
              wrappedY += kChunkDimension;
            } else if (wrappedY >= kChunkDimension) {
              neighborKey.originY += kChunkDimension;
              wrappedY -= kChunkDimension;
            }

            if (wrappedZ < 0) {
              neighborKey.originZ -= kChunkDimension;
              wrappedZ += kChunkDimension;
            } else if (wrappedZ >= kChunkDimension) {
              neighborKey.originZ += kChunkDimension;
              wrappedZ -= kChunkDimension;
            }

            const auto neighborIt = chunkMeshes_.find(neighborKey);
            if (neighborIt != chunkMeshes_.end() && neighborIt->second.hasOccupancy) {
              const int neighborIndex = blockIndex(wrappedX, wrappedY, wrappedZ);
              if (neighborIt->second.occupancy[neighborIndex] != 0) {
                faceOccluded = shouldCullFaceAgainstNeighbor(cell, neighborIt->second.cells[neighborIndex]);
              }
            }
          }

          if (faceOccluded) {
            continue;
          }

          SurfaceBuildBuffers& faceSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
            appendFaceGeometry(
              faceIndex,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              cell.terrainTiles[minecraftSide],
              packVertexColor(faceTintColorForBlock(cell.blockId, minecraftSide, cell.blockColor)),
              0.0f,
              faceSurface.vertices,
              faceSurface.indices);

          if (cell.blockId == kGrassBlockId
              && minecraftSide >= 2
              && minecraftSide <= 5
              && cell.terrainTiles[minecraftSide] == 3) {
            SurfaceBuildBuffers& overlaySurface = acquireSurface(terrainMaterialHandles_[kCutoutTerrainMaterialClass]);
            appendFaceGeometry(
                faceIndex,
                static_cast<float>(localX),
                static_cast<float>(localY),
                static_cast<float>(localZ),
                kGrassOverlayTerrainTile,
                packVertexColor(cell.blockColor),
                kFaceOverlayBias,
                overlaySurface.vertices,
                overlaySurface.indices);
          }
        }
      }
    }
  }

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  surfaces.reserve(surfacesToBuild.size());
  for (const SurfaceBuildBuffers& surfaceBuild : surfacesToBuild) {
    if (surfaceBuild.indices.empty()) {
      continue;
    }

    remixapi_MeshInfoSurfaceTriangles surface {};
    surface.vertices_values = surfaceBuild.vertices.data();
    surface.vertices_count = surfaceBuild.vertices.size();
    surface.indices_values = surfaceBuild.indices.data();
    surface.indices_count = surfaceBuild.indices.size();
    surface.skinning_hasvalue = FALSE;
    surface.material = surfaceBuild.materialHandle;
    surfaces.push_back(surface);
  }

  if (surfaces.empty()) {
    destroyChunkMesh(meshData);
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeChunkMeshHash(chunkKey, nextChunkMeshHash_++);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMesh(&meshInfo, &newMeshHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  if (!reconcileChunkTorchLights(meshData, desiredTorchLights)) {
    if (newMeshHandle != nullptr && remix_.DestroyMesh != nullptr) {
      remix_.DestroyMesh(newMeshHandle);
    }
    return false;
  }

  destroyChunkMeshHandle(meshData);
  meshData.meshHandle = newMeshHandle;
  meshData.meshHash = meshInfo.hash;
  return true;
}

void RemixRenderer::destroyChunkMesh(ChunkMeshData& meshData) {
  destroyChunkMeshHandle(meshData);
  destroyChunkTorchLights(meshData);
}

void RemixRenderer::refreshNeighborChunkMeshes(const ChunkKey& chunkKey) {
  for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
    ChunkKey neighborKey = chunkKey;
    neighborKey.originX += kNeighborOffsets[faceIndex][0] * kChunkDimension;
    neighborKey.originY += kNeighborOffsets[faceIndex][1] * kChunkDimension;
    neighborKey.originZ += kNeighborOffsets[faceIndex][2] * kChunkDimension;

    const auto neighborIt = chunkMeshes_.find(neighborKey);
    if (neighborIt == chunkMeshes_.end()) {
      continue;
    }

    if (!neighborIt->second.hasOccupancy || neighborIt->second.blockCount == 0) {
      continue;
    }

    if (!rebuildChunkMeshFromData(neighborKey, neighborIt->second, true)) {
      return;
    }
  }
}

bool RemixRenderer::drawCapturedGeometry() {
  if (chunkMeshes_.empty() && cloudMeshHandle_ == nullptr && dynamicEntityMeshes_.empty() && torchLights_.empty()) {
    if (presentedFrames_ < 4) {
      log("No captured scene meshes available yet");
    }
    ++presentedFrames_;
    return true;
  }

  std::size_t submittedChunks = 0;
  std::size_t submittedBlocks = 0;
  std::size_t submittedCloudQuads = 0;
  std::size_t submittedDynamicEntityQuads = 0;
  std::size_t submittedTorchLights = 0;
  for (const auto& [chunkKey, meshData] : chunkMeshes_) {
    if (meshData.meshHandle == nullptr) {
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
    if (chunkKey.renderPass == 1) {
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
    instanceInfo.mesh = meshData.meshHandle;
    instanceInfo.transform = makeTranslationTransform(
        static_cast<float>(chunkKey.originX),
        static_cast<float>(chunkKey.originY),
        static_cast<float>(chunkKey.originZ));
    instanceInfo.doubleSided = chunkKey.renderPass == 1 ? TRUE : FALSE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    ++submittedChunks;
    submittedBlocks += meshData.blockCount;
  }

  for (const auto& [entityId, meshData] : dynamicEntityMeshes_) {
    if (meshData.meshHandle == nullptr) {
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

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.pNext = &blendInfo;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = meshData.meshHandle;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedDynamicEntityQuads += meshData.quadCount;
  }

  if (cloudMeshHandle_ != nullptr) {
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
    instanceInfo.mesh = cloudMeshHandle_;
    instanceInfo.transform = makeTranslationTransform(0.0f, 0.0f, 0.0f);
    instanceInfo.doubleSided = TRUE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    submittedCloudQuads = cloudQuadCount_;
  }

  if (!torchLights_.empty()) {
    if (remix_.AutoInstancePersistentLights != nullptr) {
      const remixapi_ErrorCode result = remix_.AutoInstancePersistentLights();
      if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
        setError("AutoInstancePersistentLights failed: " + errorCodeToString(result));
        return false;
      }
      submittedTorchLights = torchLights_.size();
    } else if (remix_.DrawLightInstance != nullptr) {
      for (const auto& [position, lightHandle] : torchLights_) {
        (void)position;
        if (lightHandle == nullptr) {
          continue;
        }

        const remixapi_ErrorCode result = remix_.DrawLightInstance(lightHandle);
        if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
          setError("DrawLightInstance failed: " + errorCodeToString(result));
          return false;
        }

        ++submittedTorchLights;
      }
    }
  }

  if (presentedFrames_ < 8
      || submittedChunks != lastSubmittedChunkCount_
      || submittedBlocks != lastSubmittedBlockCount_
      || submittedCloudQuads != lastSubmittedCloudQuadCount_
      || submittedDynamicEntityQuads != lastSubmittedDynamicEntityQuadCount_
      || submittedTorchLights != lastSubmittedTorchLightCount_) {
    std::ostringstream stream;
    stream << "Submitted " << submittedChunks
           << " chunk meshes covering " << submittedBlocks
           << " blocks and " << submittedCloudQuads
           << " cloud quads and " << submittedDynamicEntityQuads
           << " dynamic entity quads and " << submittedTorchLights
           << " torch lights";
    log(stream.str());
  }

  lastSubmittedChunkCount_ = submittedChunks;
  lastSubmittedBlockCount_ = submittedBlocks;
  lastSubmittedCloudQuadCount_ = submittedCloudQuads;
  lastSubmittedDynamicEntityQuadCount_ = submittedDynamicEntityQuads;
  lastSubmittedTorchLightCount_ = submittedTorchLights;
  ++presentedFrames_;
  return true;
}

bool RemixRenderer::submitCamera() {
  const float nearPlane = camera_.nearPlane > 0.001f ? camera_.nearPlane : 0.05f;
  const float farPlane = camera_.farPlane > nearPlane ? camera_.farPlane : (nearPlane + 1024.0f);
  const float aspect = camera_.aspect > 0.001f ? camera_.aspect : 1.0f;

  remixapi_CameraInfoParameterizedEXT params {};
  params.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO_PARAMETERIZED_EXT;
  params.position = {camera_.position[0], camera_.position[1], camera_.position[2]};
  params.forward = {camera_.forward[0], camera_.forward[1], camera_.forward[2]};
  params.up = {camera_.up[0], camera_.up[1], camera_.up[2]};
  params.right = {camera_.right[0], camera_.right[1], camera_.right[2]};
  params.fovYInDegrees = camera_.fovYDegrees;
  params.aspect = aspect;
  params.nearPlane = nearPlane;
  params.farPlane = farPlane;

  remixapi_CameraInfo info {};
  info.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO;
  info.pNext = &params;

  const remixapi_ErrorCode result = remix_.SetupCamera(&info);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetupCamera failed: " + errorCodeToString(result));
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
