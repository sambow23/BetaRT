// Definitions for helpers declared in mcrtx/render_internals.hpp.
// Auto-extracted from remix_renderer.cpp during the monolith split.

#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string_view>
#include <unordered_map>
#include <vector>

namespace mcrtx {
namespace detail {

namespace {

constexpr wchar_t kRuntimeConfigFileName[] = L"mcrtx-runtime.env";

std::string trimAsciiWhitespace(std::string value) {
  const std::size_t first = value.find_first_not_of(" \t\r\n");
  if (first == std::string::npos) {
    return {};
  }

  const std::size_t last = value.find_last_not_of(" \t\r\n");
  return value.substr(first, last - first + 1);
}

std::unordered_map<std::string, std::string> loadRuntimeConfigValues() {
  std::unordered_map<std::string, std::string> values;
  const std::filesystem::path configPath = getRuntimeConfigPath();
  if (configPath.empty() || !std::filesystem::is_regular_file(configPath)) {
    return values;
  }

  std::ifstream stream(configPath);
  if (!stream.is_open()) {
    return values;
  }

  std::string line;
  while (std::getline(stream, line)) {
    const std::string trimmedLine = trimAsciiWhitespace(line);
    if (trimmedLine.empty() || trimmedLine[0] == '#') {
      continue;
    }

    const std::size_t separatorIndex = trimmedLine.find('=');
    if (separatorIndex == std::string::npos || separatorIndex == 0) {
      continue;
    }

    const std::string key = trimAsciiWhitespace(trimmedLine.substr(0, separatorIndex));
    const std::string value = trimAsciiWhitespace(trimmedLine.substr(separatorIndex + 1));
    if (!key.empty()) {
      values[key] = value;
    }
  }

  return values;
}

const std::unordered_map<std::string, std::string>& runtimeConfigValues() {
  static const std::unordered_map<std::string, std::string> values = loadRuntimeConfigValues();
  return values;
}

}  // namespace

std::atomic_bool g_outputWindowInteractive {false};
std::atomic_bool g_nativeMouseCursorHidden {false};
std::atomic_long g_nativeMouseWheelDelta {0};
const wchar_t kRemixWindowClassName[] = L"MCRTXRemixOutputWindow";
const wchar_t kRemixWindowTitle[] = L"mc-rtx Remix Output";

// Shared constants, arrays, and the SurfaceBuildBuffers struct live in
// include/mcrtx/render_internals.hpp so they are visible to every TU that
// includes the header. The duplicates that used to live here (extracted from
// the former anonymous namespace in remix_renderer.cpp) would trip ODR.
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

bool isTruthyEnvValue(const char* envValue) {
  if (envValue == nullptr || envValue[0] == '\0') {
    return false;
  }

  const char firstCharacter = envValue[0];
  return firstCharacter == '1'
      || firstCharacter == 't'
      || firstCharacter == 'T'
      || firstCharacter == 'y'
      || firstCharacter == 'Y';
}

std::string readEnvironmentVariable(const char* name) {
  const auto& configuredValues = runtimeConfigValues();
  const auto configuredValue = configuredValues.find(name);
  if (configuredValue != configuredValues.end() && !configuredValue->second.empty()) {
    return configuredValue->second;
  }

  char* envValue = nullptr;
  std::size_t envValueLength = 0;
  if (_dupenv_s(&envValue, &envValueLength, name) != 0 || envValue == nullptr || envValueLength == 0) {
    return {};
  }

  std::string value(envValue);
  std::free(envValue);
  return value;
}

std::filesystem::path getRuntimeConfigPath() {
  std::vector<wchar_t> buffer(MAX_PATH);
  DWORD length = GetCurrentDirectoryW(static_cast<DWORD>(buffer.size()), buffer.data());
  if (length == 0) {
    return {};
  }

  if (length >= buffer.size()) {
    buffer.resize(length + 1);
    length = GetCurrentDirectoryW(static_cast<DWORD>(buffer.size()), buffer.data());
    if (length == 0 || length >= buffer.size()) {
      return {};
    }
  }

  return std::filesystem::path(std::wstring(buffer.data(), length)) / kRuntimeConfigFileName;
}

bool isVerboseLoggingEnabled() {
  static const bool enabled = []() {
    const std::string value = readEnvironmentVariable("MCRTX_VERBOSE_LOG");
    return isTruthyEnvValue(value.c_str());
  }();
  return enabled;
}

bool equalsIgnoreCase(std::string_view left, std::string_view right) {
  if (left.size() != right.size()) {
    return false;
  }

  for (std::size_t index = 0; index < left.size(); ++index) {
    const unsigned char leftChar = static_cast<unsigned char>(left[index]);
    const unsigned char rightChar = static_cast<unsigned char>(right[index]);
    if (std::tolower(leftChar) != std::tolower(rightChar)) {
      return false;
    }
  }

  return true;
}

bool shouldUseSingleNativeOutputWindow() {
  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  return !configuredWindowMode.empty() && equalsIgnoreCase(configuredWindowMode, "single-native");
}

bool shouldUseOverlayOutputWindow(bool* usedLegacyEnvVar) {
  if (usedLegacyEnvVar != nullptr) {
    *usedLegacyEnvVar = false;
  }

  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  if (!configuredWindowMode.empty()) {
    const std::string_view mode(configuredWindowMode);
    if (equalsIgnoreCase(mode, "dual")
        || equalsIgnoreCase(mode, "detached")
        || equalsIgnoreCase(mode, "separate")
        || equalsIgnoreCase(mode, "standalone")
        || equalsIgnoreCase(mode, "single-native")) {
      return false;
    }

    if (equalsIgnoreCase(mode, "single")
        || equalsIgnoreCase(mode, "overlay")
        || equalsIgnoreCase(mode, "attached")) {
      return true;
    }
  }

  const std::string legacySourceWindowFlag = readEnvironmentVariable("MCRTX_USE_SOURCE_WINDOW");
  if (isTruthyEnvValue(legacySourceWindowFlag.c_str())) {
    if (usedLegacyEnvVar != nullptr) {
      *usedLegacyEnvVar = true;
    }
    return false;
  }

  return true;
}

bool shouldUseStandaloneOutputWindow() {
  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  return !configuredWindowMode.empty()
      && equalsIgnoreCase(configuredWindowMode, "standalone");
}

bool getSourceClientRectInScreenSpace(HWND sourceHwnd, RECT& rect) {
  RECT clientRect {};
  if (!GetClientRect(sourceHwnd, &clientRect)) {
    return false;
  }

  POINT topLeft {clientRect.left, clientRect.top};
  POINT bottomRight {clientRect.right, clientRect.bottom};
  if (!ClientToScreen(sourceHwnd, &topLeft) || !ClientToScreen(sourceHwnd, &bottomRight)) {
    return false;
  }

  rect.left = topLeft.x;
  rect.top = topLeft.y;
  rect.right = bottomRight.x;
  rect.bottom = bottomRight.y;
  return true;
}

LRESULT CALLBACK remixOutputWindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
  switch (message) {
    case WM_SETCURSOR:
      if (g_nativeMouseCursorHidden.load(std::memory_order_relaxed)) {
        SetCursor(nullptr);
        return TRUE;
      }
      return DefWindowProcW(hwnd, message, wParam, lParam);
    case WM_MOUSEWHEEL:
      g_nativeMouseWheelDelta.fetch_add(GET_WHEEL_DELTA_WPARAM(wParam), std::memory_order_relaxed);
      return 0;
    case WM_NCHITTEST:
      if (!g_outputWindowInteractive.load(std::memory_order_relaxed)) {
        return HTTRANSPARENT;
      }
      return DefWindowProcW(hwnd, message, wParam, lParam);
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

bool isLavaBlock(int blockId) {
  return blockId == kLavaStillBlockId || blockId == kLavaFlowingBlockId;
}

bool isLiquidBlock(int blockId) {
  return isWaterBlock(blockId) || isLavaBlock(blockId);
}

bool isCrossedQuadRenderType(int renderType) {
  return renderType == kCrossedQuadBlockRenderType;
}

bool isFireRenderType(int renderType) {
  return renderType == kFireBlockRenderType;
}

bool isTorchRenderType(int renderType) {
  return renderType == kTorchBlockRenderType;
}

bool isRedstoneDustRenderType(int renderType) {
  return renderType == kRedstoneDustBlockRenderType;
}

bool isCropRenderType(int renderType) {
  return renderType == kCropBlockRenderType;
}

bool isDoorRenderType(int renderType) {
  return renderType == kDoorBlockRenderType;
}

bool isLadderRenderType(int renderType) {
  return renderType == kLadderBlockRenderType;
}

bool isRailRenderType(int renderType) {
  return renderType == kRailBlockRenderType;
}

bool isStairRenderType(int renderType) {
  return renderType == kStairBlockRenderType;
}

bool isFenceRenderType(int renderType) {
  return renderType == kFenceBlockRenderType;
}

bool isLeverOrButtonRenderType(int renderType) {
  return renderType == kLeverOrButtonBlockRenderType;
}

bool isCactusRenderType(int renderType) {
  return renderType == kCactusBlockRenderType;
}

bool isBedRenderType(int renderType) {
  return renderType == kBedBlockRenderType;
}

bool isRepeaterRenderType(int renderType) {
  return renderType == kRepeaterBlockRenderType;
}

bool isPistonBaseRenderType(int renderType) {
  return renderType == kPistonBaseBlockRenderType;
}

bool isPistonHeadRenderType(int renderType) {
  return renderType == kPistonHeadBlockRenderType;
}

bool isRedstoneDustBlockId(int blockId) {
  return blockId == kRedstoneDustBlockId;
}

bool isCropBlockId(int blockId) {
  return blockId == kCropsBlockId;
}

bool isCactusBlockId(int blockId) {
  return blockId == kCactusBlockId;
}

bool isBedBlockId(int blockId) {
  return blockId == kBedBlockId;
}

bool isSingleSlabBlockId(int blockId) {
  return blockId == kSingleSlabBlockId;
}

bool isStairBlockId(int blockId) {
  return blockId == kWoodStairsBlockId || blockId == kStoneStairsBlockId;
}

bool isDoorBlockId(int blockId) {
  return blockId == kWoodDoorBlockId || blockId == kIronDoorBlockId;
}

bool isRailBlockId(int blockId) {
  return blockId == kGoldenRailBlockId || blockId == kDetectorRailBlockId || blockId == kRailBlockId;
}

bool isLeverBlockId(int blockId) {
  return blockId == kLeverBlockId;
}

bool isButtonBlockId(int blockId) {
  return blockId == kStoneButtonBlockId;
}

bool isRepeaterBlockId(int blockId) {
  return blockId == kRepeaterIdleBlockId || blockId == kRepeaterPoweredBlockId;
}

bool isPistonBaseBlockId(int blockId) {
  return blockId == kStickyPistonBlockId || blockId == kPistonBaseBlockId;
}

bool isPistonHeadBlockId(int blockId) {
  return blockId == kPistonHeadBlockId;
}

bool isRedstoneConnectionCell(const ChunkBlockCell& cell, int direction) {
  if (cell.blockId == kRedstoneDustBlockId) {
    return true;
  }

  if (cell.blockId == kRepeaterIdleBlockId || cell.blockId == kRepeaterPoweredBlockId) {
    if (direction < 0) {
      return false;
    }
    static constexpr int kRepeaterDirections[4] = {2, 3, 0, 1};
    return direction == kRepeaterDirections[cell.blockMetadata & 3];
  }

  switch (cell.blockId) {
    case kLeverBlockId:
    case kStonePressurePlateBlockId:
    case kWoodPressurePlateBlockId:
    case kRedstoneTorchOffBlockId:
    case kRedstoneTorchOnBlockId:
    case kStoneButtonBlockId:
    case kDetectorRailBlockId:
      return true;
    default:
      return false;
  }
}

bool isSupportedPass0RenderType(int renderType) {
  switch (renderType) {
    case kCubeBlockRenderType:
    case kCrossedQuadBlockRenderType:
    case kFireBlockRenderType:
    case kTorchBlockRenderType:
    case kRedstoneDustBlockRenderType:
    case kCropBlockRenderType:
    case kDoorBlockRenderType:
    case kLadderBlockRenderType:
    case kRailBlockRenderType:
    case kStairBlockRenderType:
    case kFenceBlockRenderType:
    case kLeverOrButtonBlockRenderType:
    case kCactusBlockRenderType:
    case kBedBlockRenderType:
    case kRepeaterBlockRenderType:
    case kPistonBaseBlockRenderType:
    case kPistonHeadBlockRenderType:
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
    return isSupportedPass0RenderType(renderType)
        || (renderType == kLiquidBlockRenderType && isLavaBlock(blockId));
  }
  if (renderPass == 1) {
    return (renderType == kLiquidBlockRenderType && isWaterBlock(blockId))
        || (renderType == kCubeBlockRenderType && (blockId == kIceBlockId || blockId == kNetherPortalBlockId));
  }
  return false;
}

bool usesCutoutMaterialForBlock(int blockId, int renderType) {
  if (isCrossedQuadRenderType(renderType)
      || isFireRenderType(renderType)
      || isTorchRenderType(renderType)
      || isRedstoneDustRenderType(renderType)
      || isCropRenderType(renderType)
      || isCactusRenderType(renderType)
      || isDoorRenderType(renderType)
      || isBedRenderType(renderType)
      || isLadderRenderType(renderType)
      || isRailRenderType(renderType)
      || isRepeaterRenderType(renderType)) {
    return true;
  }

  switch (blockId) {
    case 18:
    case 20:
    case 52:
    case kNetherPortalBlockId:
    case kTrapdoorBlockId:
      return true;
    default:
      return false;
  }
}

std::uint8_t materialClassForBlock(int blockId, int blockMetadata, int renderType) {
  if (isWaterBlock(blockId)) {
    return kWaterTerrainMaterialClass;
  }
  if (isLavaBlock(blockId)) {
    return kLavaTerrainMaterialClass;
  }
  if (blockId == kIceBlockId) {
    return kIceTerrainMaterialClass;
  }
  if (blockId == kNetherPortalBlockId) {
    return kPortalTerrainMaterialClass;
  }
  if (blockId == kRedstoneDustBlockId && renderType == kRedstoneDustBlockRenderType && blockMetadata > 0) {
    return kPoweredRedstoneTerrainMaterialClass;
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
  std::uint64_t hash = 0x4D43525458484B30ull;
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.originX));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.originY));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.originZ));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(key.renderPass));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(sequence));
  hash = mixHashComponent(hash, static_cast<std::uint32_t>(sequence >> 32));
  return hash;
}

std::uint64_t makeDynamicEntityMeshKey(int entityId, std::uint64_t geometryFingerprint) {
  if (entityId == kFirstPersonDynamicEntityId) {
    return geometryFingerprint ^ 0x564945574D4F4445ull;
  }
  return geometryFingerprint;
}

std::uint64_t makeDynamicEntityMeshHash(std::uint64_t geometryFingerprint) {
  return kDynamicEntityMeshHashSeed ^ geometryFingerprint;
}

std::uint64_t makeDestroyOverlayMeshHash(std::uint64_t sequence) {
  return kDestroyOverlayMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t makeBlockOutlineMeshHash(std::uint64_t sequence) {
  return kBlockOutlineMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t makeParticleMeshHash(std::uint64_t sequence) {
  return kParticleMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t makeFireMeshHash(std::uint64_t sequence) {
  return kFireMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

std::uint64_t mixHashComponent(std::uint64_t hash, std::uint32_t value) {
  hash ^= static_cast<std::uint64_t>(value) + 0x9e3779b97f4a7c15ull + (hash << 6) + (hash >> 2);
  return hash;
}

std::uint64_t computeChunkMeshFingerprint(const std::vector<SurfaceBuildBuffers>& surfaces) {
  std::uint64_t fingerprint = 0x4D435254584D4553ull;
  fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(surfaces.size()));

  for (const SurfaceBuildBuffers& surface : surfaces) {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(surface.materialHandle);
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(materialKey));
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(materialKey >> 32));
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(surface.vertices.size()));
    fingerprint = mixHashComponent(fingerprint, static_cast<std::uint32_t>(surface.indices.size()));

    for (const remixapi_HardcodedVertex& vertex : surface.vertices) {
      for (float position : vertex.position) {
        fingerprint = mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(position));
      }
      for (float normal : vertex.normal) {
        fingerprint = mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(normal));
      }
      for (float texcoord : vertex.texcoord) {
        fingerprint = mixHashComponent(fingerprint, std::bit_cast<std::uint32_t>(texcoord));
      }
      fingerprint = mixHashComponent(fingerprint, vertex.color);
    }

    for (std::uint32_t index : surface.indices) {
      fingerprint = mixHashComponent(fingerprint, index);
    }
  }

  return fingerprint;
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
  placement.radiance = cell.blockId == kRedstoneTorchOnBlockId
      ? kRedstoneTorchLightRadiance
      : kTorchLightRadiance;

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
    for (const std::int16_t tileIndex : cells[index].terrainTiles) {
      fingerprint ^= static_cast<std::uint64_t>(static_cast<std::uint16_t>(tileIndex));
      fingerprint *= 1099511628211ull;
    }
    for (const float boundValue : cells[index].bounds) {
      fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(boundValue));
      fingerprint *= 1099511628211ull;
    }
  }
  return fingerprint;
}

void hashDynamicEntityString(std::uint64_t& fingerprint, const std::string& value) {
  for (const unsigned char character : value) {
    fingerprint ^= static_cast<std::uint64_t>(character);
    fingerprint *= 1099511628211ull;
  }
  fingerprint ^= 0xFFull;
  fingerprint *= 1099511628211ull;
}

std::uint32_t computeDynamicEntityBoneCount(const std::vector<DynamicEntityQuad>& quads) {
  std::uint32_t boneCount = 0;
  for (const DynamicEntityQuad& quad : quads) {
    boneCount = std::max(boneCount, quad.boneIndex + 1);
  }
  return boneCount;
}

std::uint64_t computeDynamicEntityFingerprint(
    const std::vector<DynamicEntityQuad>& quads,
    std::uint32_t boneCount,
    std::uint32_t hurtStage,
    std::uint32_t creeperFuseStage) {
  std::uint64_t fingerprint = 1469598103934665603ull;
  fingerprint ^= static_cast<std::uint64_t>(boneCount);
  fingerprint *= 1099511628211ull;
  fingerprint ^= static_cast<std::uint64_t>(std::min(hurtStage, kDynamicEntityMaxHurtStage));
  fingerprint *= 1099511628211ull;
  fingerprint ^= static_cast<std::uint64_t>(std::min(creeperFuseStage, kDynamicEntityMaxCreeperFuseStage));
  fingerprint *= 1099511628211ull;
  for (const DynamicEntityQuad& quad : quads) {
    fingerprint ^= static_cast<std::uint64_t>(quad.boneIndex);
    fingerprint *= 1099511628211ull;
    for (const float position : quad.positions) {
      fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(position));
      fingerprint *= 1099511628211ull;
    }
    for (const float texcoord : quad.texcoords) {
      fingerprint ^= static_cast<std::uint64_t>(std::bit_cast<std::uint32_t>(texcoord));
      fingerprint *= 1099511628211ull;
    }
    fingerprint ^= static_cast<std::uint64_t>(quad.color);
    fingerprint *= 1099511628211ull;
    fingerprint ^= quad.blendEnabled ? 1ull : 0ull;
    fingerprint *= 1099511628211ull;
    hashDynamicEntityString(fingerprint, quad.texturePath);
  }
  return fingerprint;
}

std::uint32_t packVertexColor(std::uint32_t rgbColor) {
  return 0xFF000000u | (rgbColor & 0x00FFFFFFu);
}

int normalizeTerrainTileIndex(std::int16_t terrainTileIndex) {
  return std::abs(static_cast<int>(terrainTileIndex));
}

bool usesFlippedTerrainTile(std::int16_t terrainTileIndex) {
  return terrainTileIndex < 0;
}

float maybeFlipTileU(float u, float tileMinU, float scaleU, bool flipU) {
  if (!flipU) {
    return u;
  }

  return tileMinU + scaleU - (u - tileMinU);
}

bool usesPartialCubeBounds(const ChunkBlockCell& cell) {
  return cell.bounds[0] > 0.0f
      || cell.bounds[1] > 0.0f
      || cell.bounds[2] > 0.0f
      || cell.bounds[3] < 1.0f
      || cell.bounds[4] < 1.0f
      || cell.bounds[5] < 1.0f;
}

bool isSolidSupportBlock(const ChunkBlockCell& cell) {
  return cell.renderType == kCubeBlockRenderType
      && !usesPartialCubeBounds(cell)
      && cell.blockId != kIceBlockId
      && !usesCutoutMaterialForBlock(cell.blockId, cell.renderType);
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

  const int terrainTile = normalizeTerrainTileIndex(cell.terrainTiles[0]);
  return terrainTile == kLeavesFancyTextureOak || terrainTile == kLeavesFancyTextureBirchSpruce;
}

bool shouldCullFaceAgainstNeighbor(const ChunkBlockCell& cell, const ChunkBlockCell& neighborCell) {
  if (cell.renderType != kCubeBlockRenderType || neighborCell.renderType != kCubeBlockRenderType) {
    return false;
  }

  if (usesPartialCubeBounds(cell) || usesPartialCubeBounds(neighborCell)) {
    return false;
  }

  if (cell.blockId == kLeavesBlockId
      && neighborCell.blockId == kLeavesBlockId
      && usesFancyLeavesTexture(cell)) {
    return false;
  }

  if (usesCutoutMaterialForBlock(neighborCell.blockId, neighborCell.renderType) || neighborCell.blockId == kIceBlockId) {
    if (cell.blockId != neighborCell.blockId) {
      return false;
    }
  } else if (!isSolidSupportBlock(neighborCell)) {
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

void appendAnimatedFireSheet(
    float x0,
    float y0,
    float z0,
    float x1,
    float y1,
    float z1,
    float x2,
    float y2,
    float z2,
    float x3,
    float y3,
    float z3,
    std::uint32_t frameIndex,
    bool alternateRow,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t clampedFrame = frameIndex % kFireAnimationFrameCount;
  const float tileMinU = (static_cast<float>(clampedFrame) * kAtlasTileSizePixels) / kFireAtlasWidthPixels;
  const float tileMinV = alternateRow ? (kAtlasTileSizePixels / kFireAtlasHeightPixels) : 0.0f;
  const float tileMaxU = ((static_cast<float>(clampedFrame) * kAtlasTileSizePixels) + 15.99f) / kFireAtlasWidthPixels;
  const float tileMaxV = tileMinV + ((kAtlasTileSizePixels - kAtlasUvInsetPixels) / kFireAtlasHeightPixels);

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
  appendCrossedQuadSheet(
      x3,
      y3,
      z3,
      tileMinU,
      tileMinV,
      x2,
      y2,
      z2,
      tileMinU,
      tileMaxV,
      x1,
      y1,
      z1,
      tileMaxU,
      tileMaxV,
      x0,
      y0,
      z0,
      tileMaxU,
      tileMinV,
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendFireGeometry(
    int worldX,
    int worldY,
    int worldZ,
    bool hasBase,
    bool westNeighbor,
    bool eastNeighbor,
    bool northNeighbor,
    bool southNeighbor,
    bool upNeighbor,
    float localX,
    float localY,
    float localZ,
    std::uint32_t frameIndex,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  if (hasBase) {
    const float height = 1.4f;
    appendAnimatedFireSheet(
        localX + 0.2f,
        localY + height,
        localZ + 1.0f,
        localX + 0.7f,
        localY,
        localZ + 1.0f,
        localX + 0.7f,
        localY,
        localZ + 0.0f,
        localX + 0.2f,
        localY + height,
        localZ + 0.0f,
        frameIndex,
        false,
        vertices,
        indices);
      appendAnimatedFireSheet(
        localX + 0.8f,
        localY + height,
        localZ + 0.0f,
        localX + 0.3f,
        localY,
        localZ + 0.0f,
        localX + 0.3f,
        localY,
        localZ + 1.0f,
        localX + 0.8f,
        localY + height,
        localZ + 1.0f,
        frameIndex,
        false,
        vertices,
        indices);
      appendAnimatedFireSheet(
        localX + 1.0f,
        localY + height,
        localZ + 0.8f,
        localX + 1.0f,
        localY,
        localZ + 0.3f,
        localX + 0.0f,
        localY,
        localZ + 0.3f,
        localX + 0.0f,
        localY + height,
        localZ + 0.8f,
        frameIndex,
        true,
        vertices,
        indices);
      appendAnimatedFireSheet(
        localX + 0.0f,
        localY + height,
        localZ + 0.2f,
        localX + 0.0f,
        localY,
        localZ + 0.7f,
        localX + 1.0f,
        localY,
        localZ + 0.7f,
        localX + 1.0f,
        localY + height,
        localZ + 0.2f,
        frameIndex,
        false,
        vertices,
        indices);
    return;
  }

  const float sideInset = 0.2f;
  const float topY = 1.4625f;
  const float bottomY = 0.0625f;

  if (westNeighbor) {
    appendAnimatedFireSheet(
        localX + sideInset,
        localY + topY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + sideInset,
        localY + topY,
        localZ + 0.0f,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (eastNeighbor) {
      appendAnimatedFireSheet(
        localX + 0.8f,
        localY + topY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.8f,
        localY + topY,
        localZ + 1.0f,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (northNeighbor) {
      appendAnimatedFireSheet(
        localX + 0.0f,
        localY + topY,
        localZ + sideInset,
        localX + 0.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 0.0f,
        localX + 1.0f,
        localY + topY,
        localZ + sideInset,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (southNeighbor) {
      appendAnimatedFireSheet(
        localX + 1.0f,
        localY + topY,
        localZ + 0.8f,
        localX + 1.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + bottomY,
        localZ + 1.0f,
        localX + 0.0f,
        localY + topY,
        localZ + 0.8f,
        frameIndex,
        false,
        vertices,
        indices);
  }
  if (upNeighbor) {
    const bool rotateTop = ((worldX + worldY + worldZ + 1) & 1) == 0;
    if (rotateTop) {
      appendAnimatedFireSheet(
          localX + 0.0f,
          localY + 0.8f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 0.8f,
          localZ + 1.0f,
            frameIndex,
            false,
          vertices,
          indices);
          appendAnimatedFireSheet(
          localX + 1.0f,
          localY + 0.8f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 0.8f,
          localZ + 0.0f,
            frameIndex,
            true,
          vertices,
          indices);
    } else {
          appendAnimatedFireSheet(
          localX + 0.0f,
          localY + 0.8f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 0.8f,
          localZ + 1.0f,
            frameIndex,
            false,
          vertices,
          indices);
          appendAnimatedFireSheet(
          localX + 1.0f,
          localY + 0.8f,
          localZ + 0.0f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 1.0f,
          localZ + 1.0f,
          localX + 0.0f,
          localY + 0.8f,
          localZ + 0.0f,
          frameIndex,
          true,
          vertices,
          indices);
    }
  }
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
  const int terrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
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

    void appendCropGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
      const int terrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
      const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
      const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
      const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      const std::uint32_t vertexColor = packVertexColor(cell.blockColor);

      const float baseY = localY - 0.0625f;
      const float centerX = localX + 0.5f;
      const float centerZ = localZ + 0.5f;

      appendCrossedQuadSheet(
        centerX - 0.25f,
        baseY + 1.0f,
        centerZ - 0.5f,
        tileMinU,
        tileMinV,
        centerX - 0.25f,
        baseY,
        centerZ - 0.5f,
        tileMinU,
        tileMaxV,
        centerX - 0.25f,
        baseY,
        centerZ + 0.5f,
        tileMaxU,
        tileMaxV,
        centerX - 0.25f,
        baseY + 1.0f,
        centerZ + 0.5f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
      appendCrossedQuadSheet(
        centerX + 0.25f,
        baseY + 1.0f,
        centerZ + 0.5f,
        tileMinU,
        tileMinV,
        centerX + 0.25f,
        baseY,
        centerZ + 0.5f,
        tileMinU,
        tileMaxV,
        centerX + 0.25f,
        baseY,
        centerZ - 0.5f,
        tileMaxU,
        tileMaxV,
        centerX + 0.25f,
        baseY + 1.0f,
        centerZ - 0.5f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
      appendCrossedQuadSheet(
        centerX - 0.5f,
        baseY + 1.0f,
        centerZ - 0.25f,
        tileMinU,
        tileMinV,
        centerX - 0.5f,
        baseY,
        centerZ - 0.25f,
        tileMinU,
        tileMaxV,
        centerX + 0.5f,
        baseY,
        centerZ - 0.25f,
        tileMaxU,
        tileMaxV,
        centerX + 0.5f,
        baseY + 1.0f,
        centerZ - 0.25f,
        tileMaxU,
        tileMinV,
        vertexColor,
        vertices,
        indices);
      appendCrossedQuadSheet(
        centerX + 0.5f,
        baseY + 1.0f,
        centerZ + 0.25f,
        tileMinU,
        tileMinV,
        centerX + 0.5f,
        baseY,
        centerZ + 0.25f,
        tileMinU,
        tileMaxV,
        centerX - 0.5f,
        baseY,
        centerZ + 0.25f,
        tileMaxU,
        tileMaxV,
        centerX - 0.5f,
        baseY + 1.0f,
        centerZ + 0.25f,
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
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
  const bool flipU = usesFlippedTerrainTile(terrainTileIndex);
  const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
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

    vertex.texcoord[0] = maybeFlipTileU(u, tileMinU, scaleU, flipU);
    vertex.texcoord[1] = v;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void appendRotatedBoundsFaceGeometry(
    int faceIndex,
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::int16_t terrainTileIndex,
    int rotation,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
  const bool flipU = usesFlippedTerrainTile(terrainTileIndex);
  const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
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

    float normalizedU = 0.0f;
    float normalizedV = 0.0f;
    switch (faceIndex) {
      case 0:
        normalizedU = 1.0f - relX;
        normalizedV = 1.0f - relY;
        break;
      case 1:
        normalizedU = relX;
        normalizedV = 1.0f - relY;
        break;
      case 2:
        normalizedU = relZ;
        normalizedV = 1.0f - relY;
        break;
      case 3:
        normalizedU = 1.0f - relZ;
        normalizedV = 1.0f - relY;
        break;
      case 4:
        normalizedU = 1.0f - relX;
        normalizedV = 1.0f - relZ;
        break;
      case 5:
      default:
        normalizedU = relX;
        normalizedV = relZ;
        break;
    }

    if (rotation == 1) {
      const float rotatedU = normalizedV;
      const float rotatedV = 1.0f - normalizedU;
      normalizedU = rotatedU;
      normalizedV = rotatedV;
    } else if (rotation == 2) {
      const float rotatedU = 1.0f - normalizedV;
      const float rotatedV = normalizedU;
      normalizedU = rotatedU;
      normalizedV = rotatedV;
    } else if (rotation == 3) {
      normalizedU = 1.0f - normalizedU;
      normalizedV = 1.0f - normalizedV;
    }

    vertex.position[0] = px;
    vertex.position[1] = py;
    vertex.position[2] = pz;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    vertex.texcoord[0] = maybeFlipTileU(tileMinU + normalizedU * scaleU, tileMinU, scaleU, flipU);
    vertex.texcoord[1] = tileMinV + normalizedV * scaleV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

std::array<int, 6> pistonFaceRotationsForFacing(int facing) {
  std::array<int, 6> rotations = {0, 0, 0, 0, 0, 0};
  switch (facing) {
    case 0:
      rotations[0] = 3;
      rotations[1] = 3;
      rotations[2] = 3;
      rotations[3] = 3;
      break;
    case 2:
      rotations[2] = 2;
      rotations[3] = 1;
      break;
    case 3:
      rotations[3] = 2;
      rotations[2] = 1;
      rotations[4] = 3;
      rotations[5] = 3;
      break;
    case 4:
      rotations[0] = 1;
      rotations[1] = 2;
      rotations[4] = 1;
      rotations[5] = 2;
      break;
    case 5:
      rotations[0] = 2;
      rotations[1] = 1;
      rotations[4] = 2;
      rotations[5] = 1;
      break;
    case 1:
    default:
      break;
  }
  return rotations;
}

void appendPistonBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    const std::array<std::int16_t, 6>& terrainTiles,
    const std::array<int, 6>& faceRotations,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
    const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
    appendRotatedBoundsFaceGeometry(
        faceIndex,
        minX,
        minY,
        minZ,
        maxX,
        maxY,
        maxZ,
        terrainTiles[minecraftSide],
        faceRotations[faceIndex],
        vertexColor,
        vertices,
        indices);
  }
}

void appendDoubleSidedTexturedQuad(
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
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const auto normal = computeQuadNormal(x3, y3, z3, x2, y2, z2, x1, y1, z1);
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
      normal[0],
      normal[1],
      normal[2],
      kDefaultVertexColor,
      vertices,
      indices);
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
      -normal[0],
      -normal[1],
      -normal[2],
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendPistonRodGeometry(
    int facing,
    float localX,
    float localY,
    float localZ,
    std::int16_t terrainTile,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(terrainTile);
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = tileMinU + (kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileBandMaxV = tileMinV + (4.0f - kAtlasUvInsetPixels) / kAtlasSizePixels;

  float rodMinX = localX + 0.375f;
  float rodMaxX = localX + 0.625f;
  float rodMinY = localY + 0.25f;
  float rodMaxY = localY + 1.25f;
  float rodMinZ = localZ + 0.375f;
  float rodMaxZ = localZ + 0.625f;
  enum class RodAxis {
    Vertical,
    NorthSouth,
    EastWest,
  };
  RodAxis axis = RodAxis::Vertical;

  switch (facing) {
    case 1:
      rodMinY = localY - 0.25f;
      rodMaxY = localY + 0.75f;
      axis = RodAxis::Vertical;
      break;
    case 2:
      rodMinX = localX + 0.375f;
      rodMaxX = localX + 0.625f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ + 0.25f;
      rodMaxZ = localZ + 1.25f;
      axis = RodAxis::NorthSouth;
      break;
    case 3:
      rodMinX = localX + 0.375f;
      rodMaxX = localX + 0.625f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ - 0.25f;
      rodMaxZ = localZ + 0.75f;
      axis = RodAxis::NorthSouth;
      break;
    case 4:
      rodMinX = localX + 0.25f;
      rodMaxX = localX + 1.25f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ + 0.375f;
      rodMaxZ = localZ + 0.625f;
      axis = RodAxis::EastWest;
      break;
    case 5:
      rodMinX = localX - 0.25f;
      rodMaxX = localX + 0.75f;
      rodMinY = localY + 0.375f;
      rodMaxY = localY + 0.625f;
      rodMinZ = localZ + 0.375f;
      rodMaxZ = localZ + 0.625f;
      axis = RodAxis::EastWest;
      break;
    case 0:
    default:
      axis = RodAxis::Vertical;
      break;
  }

  const auto appendRodQuad = [&](float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3) {
    appendDoubleSidedTexturedQuad(
        x0,
        y0,
        z0,
        tileMinU,
        tileMinV,
        x1,
        y1,
        z1,
        tileMinU,
        tileBandMaxV,
        x2,
        y2,
        z2,
        tileMaxU,
        tileBandMaxV,
        x3,
        y3,
        z3,
        tileMaxU,
        tileMinV,
        vertices,
        indices);
  };

  if (axis == RodAxis::Vertical) {
    appendRodQuad(rodMinX, rodMaxY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ, rodMinX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMaxX, rodMaxY, rodMinZ);
    appendRodQuad(rodMinX, rodMaxY, rodMaxZ, rodMinX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMinZ, rodMaxX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMaxY, rodMinZ);
  } else if (axis == RodAxis::NorthSouth) {
    appendRodQuad(rodMinX, rodMaxY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ, rodMinX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMaxX, rodMaxY, rodMinZ);
    appendRodQuad(rodMinX, rodMaxY, rodMaxZ, rodMinX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ);
  } else {
    appendRodQuad(rodMinX, rodMaxY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMaxX, rodMinY, rodMinZ, rodMaxX, rodMaxY, rodMinZ);
    appendRodQuad(rodMaxX, rodMaxY, rodMaxZ, rodMaxX, rodMinY, rodMaxZ, rodMinX, rodMinY, rodMaxZ, rodMinX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMinX, rodMaxY, rodMaxZ, rodMinX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMinZ, rodMaxX, rodMaxY, rodMaxZ);
    appendRodQuad(rodMaxX, rodMinY, rodMaxZ, rodMaxX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMinZ, rodMinX, rodMinY, rodMaxZ);
  }
}

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

  void appendSlabGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    appendBoxGeometry(
        localX,
        localY,
        localZ,
        localX + 1.0f,
        localY + 0.5f,
        localZ + 1.0f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }

  void appendStairGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    const int metadata = cell.blockMetadata & 3;

    if (metadata == 0) {
      appendBoxGeometry(
          localX,
          localY,
          localZ,
          localX + 0.5f,
          localY + 0.5f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      appendBoxGeometry(
          localX + 0.5f,
          localY,
          localZ,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      return;
    }

    if (metadata == 1) {
      appendBoxGeometry(
          localX,
          localY,
          localZ,
          localX + 0.5f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      appendBoxGeometry(
          localX + 0.5f,
          localY,
          localZ,
          localX + 1.0f,
          localY + 0.5f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      return;
    }

    if (metadata == 2) {
      appendBoxGeometry(
          localX,
          localY,
          localZ,
          localX + 1.0f,
          localY + 0.5f,
          localZ + 0.5f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      appendBoxGeometry(
          localX,
          localY,
          localZ + 0.5f,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      return;
    }

    appendBoxGeometry(
        localX,
        localY,
        localZ,
        localX + 1.0f,
        localY + 1.0f,
        localZ + 0.5f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
    appendBoxGeometry(
        localX,
        localY,
        localZ + 0.5f,
        localX + 1.0f,
        localY + 0.5f,
        localZ + 1.0f,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }

  void appendDoorGeometry(
      const ChunkBlockCell& cell,
      int resolvedMetadata,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    const int doorShape = (resolvedMetadata & 4) == 0
        ? ((resolvedMetadata - 1) & 3)
        : (resolvedMetadata & 3);
    constexpr float kDoorThickness = 0.1875f;

    float minX = localX;
    float maxX = localX + 1.0f;
    float minZ = localZ;
    float maxZ = localZ + 1.0f;

    if (doorShape == 0) {
      maxZ = localZ + kDoorThickness;
    } else if (doorShape == 1) {
      minX = localX + 1.0f - kDoorThickness;
    } else if (doorShape == 2) {
      minZ = localZ + 1.0f - kDoorThickness;
    } else {
      maxX = localX + kDoorThickness;
    }

    appendBoxGeometry(
        minX,
        localY,
        minZ,
        maxX,
        localY + 1.0f,
        maxZ,
        cell.terrainTiles,
        kDefaultVertexColor,
        vertices,
        indices);
  }

  void appendBedGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    static constexpr int kFootConnectedSide[4] = {3, 4, 2, 5};
    static constexpr int kHeadConnectedSide[4] = {2, 5, 3, 4};
    static constexpr int kFlippedSideByFacing[4] = {5, 3, 4, 2};
    static constexpr float kTopUvOrder[4][4][2] = {
        {{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}},
        {{0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}},
        {{0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}},
        {{1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}},
    };

    const int facing = cell.blockMetadata & 3;
    const bool isHead = (cell.blockMetadata & 8) != 0;
    const int hiddenMinecraftSide = isHead ? kHeadConnectedSide[facing] : kFootConnectedSide[facing];
    const int flippedMinecraftSide = kFlippedSideByFacing[facing];

    const float minX = localX + cell.bounds[0];
    const float minY = localY + cell.bounds[1];
    const float minZ = localZ + cell.bounds[2];
    const float maxX = localX + cell.bounds[3];
    const float maxY = localY + cell.bounds[4];
    const float maxZ = localZ + cell.bounds[5];

    const auto appendBedQuad = [&vertices, &indices](
                                   float x0, float y0, float z0, float u0, float v0,
                                   float x1, float y1, float z1, float u1, float v1,
                                   float x2, float y2, float z2, float u2, float v2,
                                   float x3, float y3, float z3, float u3, float v3) {
                      const auto normal = computeQuadNormal(x3, y3, z3, x2, y2, z2, x1, y1, z1);
      appendCloudQuad(
                        x3, y3, z3, u3, v3,
                        x2, y2, z2, u2, v2,
                        x1, y1, z1, u1, v1,
                        x0, y0, z0, u0, v0,
          normal[0], normal[1], normal[2],
          kDefaultVertexColor,
          vertices,
          indices);
    };

    const auto appendMappedBedFace = [&](std::int16_t terrainTileIndex, const float uvOrder[4][2]) {
      const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
      const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
      const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
      const float tileMaxU = (static_cast<float>((terrainTile & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels)
          / kAtlasSizePixels;
      const float tileMaxV = (static_cast<float>(terrainTile & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels)
          / kAtlasSizePixels;

      const auto mapU = [tileMinU, tileMaxU](float normalized) {
        return tileMinU + (tileMaxU - tileMinU) * normalized;
      };
      const auto mapV = [tileMinV, tileMaxV](float normalized) {
        return tileMinV + (tileMaxV - tileMinV) * normalized;
      };

      appendBedQuad(
          maxX, maxY, maxZ, mapU(uvOrder[0][0]), mapV(uvOrder[0][1]),
          maxX, maxY, minZ, mapU(uvOrder[1][0]), mapV(uvOrder[1][1]),
          minX, maxY, minZ, mapU(uvOrder[2][0]), mapV(uvOrder[2][1]),
          minX, maxY, maxZ, mapU(uvOrder[3][0]), mapV(uvOrder[3][1]));
    };

    appendBedQuad(
        minX, minY, maxZ, static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) / kAtlasSizePixels,
        (static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels,
        minX, minY, minZ, static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) / kAtlasSizePixels,
        static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) / kAtlasSizePixels,
        maxX, minY, minZ, (static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels,
        static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) / kAtlasSizePixels,
        maxX, minY, maxZ, (static_cast<float>((normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels,
        (static_cast<float>(normalizeTerrainTileIndex(cell.terrainTiles[0]) & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels);

    appendMappedBedFace(cell.terrainTiles[1], kTopUvOrder[facing]);

    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
      if (minecraftSide == 0 || minecraftSide == 1 || minecraftSide == hiddenMinecraftSide) {
        continue;
      }

      std::int16_t terrainTileIndex = cell.terrainTiles[minecraftSide];
      if (minecraftSide == flippedMinecraftSide && terrainTileIndex > 0) {
        terrainTileIndex = static_cast<std::int16_t>(-terrainTileIndex);
      }

      appendBoundsFaceGeometry(
          faceIndex,
          minX,
          minY,
          minZ,
          maxX,
          maxY,
          maxZ,
          terrainTileIndex,
          kDefaultVertexColor,
          vertices,
          indices);
    }
  }

  void appendCactusGeometry(
      bool renderBottom,
      bool renderTop,
      bool renderNorth,
      bool renderSouth,
      bool renderWest,
      bool renderEast,
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    constexpr float kCactusInset = 0.0625f;

    if (renderBottom) {
      appendBoundsFaceGeometry(
          4,
          localX,
          localY,
          localZ,
          localX + 1.0f,
          localY,
          localZ + 1.0f,
          cell.terrainTiles[0],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderTop) {
      appendBoundsFaceGeometry(
          5,
          localX,
          localY + 1.0f,
          localZ,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles[1],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderNorth) {
      appendBoundsFaceGeometry(
          0,
          localX,
          localY,
          localZ + kCactusInset,
          localX + 1.0f,
          localY + 1.0f,
          localZ + kCactusInset,
          cell.terrainTiles[2],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderSouth) {
      appendBoundsFaceGeometry(
          1,
          localX,
          localY,
          localZ + 1.0f - kCactusInset,
          localX + 1.0f,
          localY + 1.0f,
          localZ + 1.0f - kCactusInset,
          cell.terrainTiles[3],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderWest) {
      appendBoundsFaceGeometry(
          2,
          localX + kCactusInset,
          localY,
          localZ,
          localX + kCactusInset,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles[4],
          kDefaultVertexColor,
          vertices,
          indices);
    }

    if (renderEast) {
      appendBoundsFaceGeometry(
          3,
          localX + 1.0f - kCactusInset,
          localY,
          localZ,
          localX + 1.0f - kCactusInset,
          localY + 1.0f,
          localZ + 1.0f,
          cell.terrainTiles[5],
          kDefaultVertexColor,
          vertices,
          indices);
    }
  }

  void appendLeverGeometry(
      const ChunkBlockCell& cell,
      float localX,
      float localY,
      float localZ,
      std::vector<remixapi_HardcodedVertex>& vertices,
      std::vector<std::uint32_t>& indices) {
    const std::array<std::int16_t, 6> leverTiles = {
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
        kCobblestoneTerrainTile,
    };

    const int metadata = cell.blockMetadata & 7;
    if (metadata < 1 || metadata > 6) {
      return;
    }

    float baseMinX = localX;
    float baseMinY = localY;
    float baseMinZ = localZ;
    float baseMaxX = localX + 1.0f;
    float baseMaxY = localY + 1.0f;
    float baseMaxZ = localZ + 1.0f;

    constexpr float kLeverPlateLongHalf = 0.25f;
    constexpr float kLeverPlateShortHalf = 0.1875f;
    constexpr float kLeverPlateThickness = 0.1875f;

    if (metadata == 5) {
      baseMinX = localX + 0.5f - kLeverPlateShortHalf;
      baseMaxX = localX + 0.5f + kLeverPlateShortHalf;
      baseMinY = localY;
      baseMaxY = localY + kLeverPlateThickness;
      baseMinZ = localZ + 0.5f - kLeverPlateLongHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateLongHalf;
    } else if (metadata == 6) {
      baseMinX = localX + 0.5f - kLeverPlateLongHalf;
      baseMaxX = localX + 0.5f + kLeverPlateLongHalf;
      baseMinY = localY;
      baseMaxY = localY + kLeverPlateThickness;
      baseMinZ = localZ + 0.5f - kLeverPlateShortHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateShortHalf;
    } else if (metadata == 4) {
      baseMinX = localX + 0.5f - kLeverPlateShortHalf;
      baseMaxX = localX + 0.5f + kLeverPlateShortHalf;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ + 1.0f - kLeverPlateThickness;
      baseMaxZ = localZ + 1.0f;
    } else if (metadata == 3) {
      baseMinX = localX + 0.5f - kLeverPlateShortHalf;
      baseMaxX = localX + 0.5f + kLeverPlateShortHalf;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ;
      baseMaxZ = localZ + kLeverPlateThickness;
    } else if (metadata == 2) {
      baseMinX = localX + 1.0f - kLeverPlateThickness;
      baseMaxX = localX + 1.0f;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ + 0.5f - kLeverPlateShortHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateShortHalf;
    } else if (metadata == 1) {
      baseMinX = localX;
      baseMaxX = localX + kLeverPlateThickness;
      baseMinY = localY + 0.5f - kLeverPlateLongHalf;
      baseMaxY = localY + 0.5f + kLeverPlateLongHalf;
      baseMinZ = localZ + 0.5f - kLeverPlateShortHalf;
      baseMaxZ = localZ + 0.5f + kLeverPlateShortHalf;
    }

    appendBoxGeometry(
        baseMinX,
        baseMinY,
        baseMinZ,
        baseMaxX,
        baseMaxY,
        baseMaxZ,
        leverTiles,
        kDefaultVertexColor,
        vertices,
        indices);

    const bool powered = (cell.blockMetadata & 8) != 0;
    const int handleTerrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);

    struct LeverVertex {
      float x;
      float y;
      float z;
    };

    auto rotateX = [](LeverVertex& vertex, float angle) {
      const float sinAngle = std::sin(angle);
      const float cosAngle = std::cos(angle);
      const float rotatedY = vertex.y * cosAngle + vertex.z * sinAngle;
      const float rotatedZ = vertex.z * cosAngle - vertex.y * sinAngle;
      vertex.y = rotatedY;
      vertex.z = rotatedZ;
    };

    auto rotateY = [](LeverVertex& vertex, float angle) {
      const float sinAngle = std::sin(angle);
      const float cosAngle = std::cos(angle);
      const float rotatedX = vertex.x * cosAngle + vertex.z * sinAngle;
      const float rotatedZ = -vertex.x * sinAngle + vertex.z * cosAngle;
      vertex.x = rotatedX;
      vertex.z = rotatedZ;
    };

    std::array<LeverVertex, 8> leverVertices = {{
        {-0.0625f, 0.0f, -0.0625f},
        {0.0625f, 0.0f, -0.0625f},
        {0.0625f, 0.0f, 0.0625f},
        {-0.0625f, 0.0f, 0.0625f},
        {-0.0625f, 0.625f, -0.0625f},
        {0.0625f, 0.625f, -0.0625f},
        {0.0625f, 0.625f, 0.0625f},
        {-0.0625f, 0.625f, 0.0625f},
    }};

    for (LeverVertex& vertex : leverVertices) {
      vertex.z += powered ? -0.0625f : 0.0625f;
      rotateX(vertex, powered ? 0.69813174f : -0.69813174f);

      if (metadata == 6) {
        rotateY(vertex, 1.5707964f);
      }

      if (metadata < 5) {
        vertex.y -= 0.375f;
        rotateX(vertex, 1.5707964f);

        if (metadata == 3) {
          rotateY(vertex, 3.1415927f);
        } else if (metadata == 2) {
          rotateY(vertex, 1.5707964f);
        } else if (metadata == 1) {
          rotateY(vertex, -1.5707964f);
        }

        vertex.x += localX + 0.5f;
        vertex.y += localY + 0.5f;
        vertex.z += localZ + 0.5f;
      } else {
        vertex.x += localX + 0.5f;
        vertex.y += localY + 0.125f;
        vertex.z += localZ + 0.5f;
      }
    }

    const std::array<std::array<int, 4>, 6> faceVertexIndices = {{
        {{0, 1, 2, 3}},
        {{7, 6, 5, 4}},
        {{1, 0, 4, 5}},
        {{2, 1, 5, 6}},
        {{3, 2, 6, 7}},
        {{0, 3, 7, 4}},
    }};

    auto appendLeverQuad = [&vertices, &indices](
                               const LeverVertex& v0,
                               const LeverVertex& v1,
                               const LeverVertex& v2,
                               const LeverVertex& v3,
                               float uMin,
                               float uMax,
                               float vMin,
                               float vMax) {
      const auto normal = computeQuadNormal(v3.x, v3.y, v3.z, v2.x, v2.y, v2.z, v1.x, v1.y, v1.z);
      appendCloudQuad(
          v3.x, v3.y, v3.z, uMin, vMin,
          v2.x, v2.y, v2.z, uMax, vMin,
          v1.x, v1.y, v1.z, uMax, vMax,
          v0.x, v0.y, v0.z, uMin, vMax,
          normal[0], normal[1], normal[2],
          kDefaultVertexColor,
          vertices,
          indices);
    };

    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      float uMin;
      float uMax;
      float vMin;
      float vMax;

      if (faceIndex <= 1) {
        uMin = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 7.0f) / kAtlasSizePixels;
        uMax = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 8.99f) / kAtlasSizePixels;
        vMin = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 6.0f) / kAtlasSizePixels;
        vMax = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 7.99f) / kAtlasSizePixels;
      } else {
        uMin = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 7.0f) / kAtlasSizePixels;
        uMax = (static_cast<float>((handleTerrainTileIndex & 0x0F) * 16) + 8.99f) / kAtlasSizePixels;
        vMin = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 6.0f) / kAtlasSizePixels;
        vMax = (static_cast<float>(handleTerrainTileIndex & 0xF0) + 15.99f) / kAtlasSizePixels;
      }

      const auto& face = faceVertexIndices[faceIndex];
      appendLeverQuad(
          leverVertices[face[0]],
          leverVertices[face[1]],
          leverVertices[face[2]],
          leverVertices[face[3]],
          uMin,
          uMax,
          vMin,
          vMax);
    }
  }

void appendTorchGeometry(
  float anchorX,
  float anchorY,
  float anchorZ,
  float leanX,
  float leanZ,
  std::int16_t terrainTile,
  std::vector<remixapi_HardcodedVertex>& vertices,
  std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(terrainTile);
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + 15.99f) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + 15.99f) / kAtlasSizePixels;
  const float capMinU = tileMinU + 7.0f / 256.0f;
  const float capMaxU = tileMinU + 9.0f / 256.0f;
  const float capMinV = tileMinV + 6.0f / 256.0f;
  const float capMaxV = tileMinV + 8.0f / 256.0f;
  const float centerX = anchorX + 0.5f;
  const float centerZ = anchorZ + 0.5f;
  const float minX = centerX - 0.5f;
  const float maxX = centerX + 0.5f;
  const float minZ = centerZ - 0.5f;
  const float maxZ = centerZ + 0.5f;
  const float halfWidth = 0.0625f;
  const float capY = anchorY + 0.625f;
  const float topCenterX = centerX + leanX * 0.375f;
  const float topCenterZ = centerZ + leanZ * 0.375f;
  const float bodyTopY = anchorY + 1.0f;
  const float bodyBottomY = anchorY;

  appendDoubleSidedTexturedQuad(
      topCenterX - halfWidth,
      capY,
      topCenterZ - halfWidth,
      capMinU,
      capMinV,
      topCenterX - halfWidth,
      capY,
      topCenterZ + halfWidth,
      capMinU,
      capMaxV,
      topCenterX + halfWidth,
      capY,
      topCenterZ + halfWidth,
      capMaxU,
      capMaxV,
      topCenterX + halfWidth,
      capY,
      topCenterZ - halfWidth,
      capMaxU,
      capMinV,
      vertices,
      indices);

  appendDoubleSidedTexturedQuad(
      centerX - halfWidth,
      bodyTopY,
      minZ,
      tileMinU,
      tileMinV,
      centerX - halfWidth + leanX,
      bodyBottomY,
      minZ + leanZ,
      tileMinU,
      tileMaxV,
      centerX - halfWidth + leanX,
      bodyBottomY,
      maxZ + leanZ,
      tileMaxU,
      tileMaxV,
      centerX - halfWidth,
      bodyTopY,
      maxZ,
      tileMaxU,
      tileMinV,
      vertices,
      indices);

  appendDoubleSidedTexturedQuad(
      centerX + halfWidth,
      bodyTopY,
      maxZ,
      tileMinU,
      tileMinV,
      centerX + halfWidth + leanX,
      bodyBottomY,
      maxZ + leanZ,
      tileMinU,
      tileMaxV,
      centerX + halfWidth + leanX,
      bodyBottomY,
      minZ + leanZ,
      tileMaxU,
      tileMaxV,
      centerX + halfWidth,
      bodyTopY,
      minZ,
      tileMaxU,
      tileMinV,
      vertices,
      indices);

  appendDoubleSidedTexturedQuad(
      minX,
      bodyTopY,
      centerZ + halfWidth,
      tileMinU,
      tileMinV,
      minX + leanX,
      bodyBottomY,
      centerZ + halfWidth + leanZ,
      tileMinU,
      tileMaxV,
      maxX + leanX,
      bodyBottomY,
      centerZ + halfWidth + leanZ,
      tileMaxU,
      tileMaxV,
      maxX,
      bodyTopY,
      centerZ + halfWidth,
      tileMaxU,
      tileMinV,
      vertices,
      indices);

  appendDoubleSidedTexturedQuad(
      maxX,
      bodyTopY,
      centerZ - halfWidth,
      tileMinU,
      tileMinV,
      maxX + leanX,
      bodyBottomY,
      centerZ - halfWidth + leanZ,
      tileMinU,
      tileMaxV,
      minX + leanX,
      bodyBottomY,
      centerZ - halfWidth + leanZ,
      tileMaxU,
      tileMaxV,
      minX,
      bodyTopY,
      centerZ - halfWidth,
      tileMaxU,
      tileMinV,
      vertices,
      indices);
}

void appendTorchGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
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
    appendTorchGeometry(
      anchorX,
      anchorY,
      anchorZ,
      leanX,
      leanZ,
      cell.terrainTiles[0],
      vertices,
      indices);
}

void appendLadderGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
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

void appendPortalGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float minX = localX + cell.bounds[0];
  const float minY = localY + cell.bounds[1];
  const float minZ = localZ + cell.bounds[2];
  const float maxX = localX + cell.bounds[3];
  const float maxY = localY + cell.bounds[4];
  const float maxZ = localZ + cell.bounds[5];
  const float xThickness = cell.bounds[3] - cell.bounds[0];
  const float zThickness = cell.bounds[5] - cell.bounds[2];

  if (xThickness <= zThickness) {
    const float portalX = (minX + maxX) * 0.5f;
    appendCrossedQuadSheet(
        portalX,
        maxY,
        maxZ,
        0.0f,
        0.0f,
        portalX,
        minY,
        maxZ,
        0.0f,
        1.0f,
        portalX,
        minY,
        minZ,
        1.0f,
        1.0f,
        portalX,
        maxY,
        minZ,
        1.0f,
        0.0f,
        kDefaultVertexColor,
        vertices,
        indices);
    return;
  }

  const float portalZ = (minZ + maxZ) * 0.5f;
  appendCrossedQuadSheet(
      maxX,
      maxY,
      portalZ,
      0.0f,
      0.0f,
      maxX,
      minY,
      portalZ,
      0.0f,
      1.0f,
      minX,
      minY,
      portalZ,
      1.0f,
      1.0f,
      minX,
      maxY,
      portalZ,
      1.0f,
      0.0f,
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendRepeaterGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendBoxGeometry(
      localX + cell.bounds[0],
      localY + cell.bounds[1],
      localZ + cell.bounds[2],
      localX + cell.bounds[3],
      localY + cell.bounds[4],
      localZ + cell.bounds[5],
      cell.terrainTiles,
      kDefaultVertexColor,
      vertices,
      indices);

  const auto appendTorchPost = [&](float offsetX, float offsetZ) {
    appendTorchGeometry(
      localX + offsetX,
      localY - 0.1875f,
      localZ + offsetZ,
      0.0f,
      0.0f,
      cell.terrainTiles[0],
        vertices,
        indices);
  };

  static constexpr float kRepeaterTorchOffsets[4] = {-0.0625f, 0.0625f, 0.1875f, 0.3125f};
  const int facing = cell.blockMetadata & 3;
  const int delay = (cell.blockMetadata >> 2) & 3;

  float firstOffsetX = 0.0f;
  float firstOffsetZ = 0.0f;
  float secondOffsetX = 0.0f;
  float secondOffsetZ = 0.0f;
  switch (facing) {
    case 0:
      secondOffsetZ = -0.3125f;
      firstOffsetZ = kRepeaterTorchOffsets[delay];
      break;
    case 2:
      secondOffsetZ = 0.3125f;
      firstOffsetZ = -kRepeaterTorchOffsets[delay];
      break;
    case 3:
      secondOffsetX = -0.3125f;
      firstOffsetX = kRepeaterTorchOffsets[delay];
      break;
    case 1:
    default:
      secondOffsetX = 0.3125f;
      firstOffsetX = -kRepeaterTorchOffsets[delay];
      break;
  }

  appendTorchPost(firstOffsetX, firstOffsetZ);
  appendTorchPost(secondOffsetX, secondOffsetZ);

  const int topTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[1]);
  const float tileMinU = static_cast<float>((topTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(topTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((topTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(topTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  const float minX = localX;
  const float maxX = localX + 1.0f;
  const float minZ = localZ;
  const float maxZ = localZ + 1.0f;
  const float topY = localY + 0.125f + 0.0005f;

  float x0 = minX;
  float z0 = minZ;
  float x1 = minX;
  float z1 = maxZ;
  float x2 = maxX;
  float z2 = maxZ;
  float x3 = maxX;
  float z3 = minZ;
  switch (facing & 3) {
    case 1:
      x0 = maxX;
      z0 = minZ;
      x1 = minX;
      z1 = minZ;
      x2 = minX;
      z2 = maxZ;
      x3 = maxX;
      z3 = maxZ;
      break;
    case 2:
      x0 = maxX;
      z0 = maxZ;
      x1 = maxX;
      z1 = minZ;
      x2 = minX;
      z2 = minZ;
      x3 = minX;
      z3 = maxZ;
      break;
    case 3:
      x0 = minX;
      z0 = maxZ;
      x1 = maxX;
      z1 = maxZ;
      x2 = maxX;
      z2 = minZ;
      x3 = minX;
      z3 = minZ;
      break;
    default:
      break;
  }

  appendCloudQuad(
      x0,
      topY,
      z0,
      tileMinU,
      tileMinV,
      x1,
      topY,
      z1,
      tileMinU,
      tileMaxV,
      x2,
      topY,
      z2,
      tileMaxU,
      tileMaxV,
      x3,
      topY,
      z3,
      tileMaxU,
      tileMinV,
      0.0f,
      1.0f,
      0.0f,
      kDefaultVertexColor,
      vertices,
      indices);
  appendCloudQuad(
      x3,
      topY,
      z3,
      tileMaxU,
      tileMinV,
      x2,
      topY,
      z2,
      tileMaxU,
      tileMaxV,
      x1,
      topY,
      z1,
      tileMinU,
      tileMaxV,
      x0,
      topY,
      z0,
      tileMinU,
      tileMinV,
      0.0f,
      -1.0f,
      0.0f,
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendPistonBaseGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendPistonBoxGeometry(
      localX + cell.bounds[0],
      localY + cell.bounds[1],
      localZ + cell.bounds[2],
      localX + cell.bounds[3],
      localY + cell.bounds[4],
      localZ + cell.bounds[5],
      cell.terrainTiles,
      pistonFaceRotationsForFacing(cell.blockMetadata & 7),
      kDefaultVertexColor,
      vertices,
      indices);
}

void appendPistonHeadGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int facing = cell.blockMetadata & 7;
  appendPistonBoxGeometry(
      localX + cell.bounds[0],
      localY + cell.bounds[1],
      localZ + cell.bounds[2],
      localX + cell.bounds[3],
      localY + cell.bounds[4],
      localZ + cell.bounds[5],
      cell.terrainTiles,
      pistonFaceRotationsForFacing(facing),
      kDefaultVertexColor,
      vertices,
      indices);

  static constexpr int kOppositeFace[6] = {1, 0, 3, 2, 5, 4};
  std::int16_t sideTile = cell.terrainTiles[2];
  for (int face = 0; face < 6; ++face) {
    if (face != facing && face != kOppositeFace[facing]) {
      sideTile = cell.terrainTiles[face];
      break;
    }
  }
  appendPistonRodGeometry(
      facing,
      localX,
      localY,
      localZ,
      sideTile,
      vertices,
      indices);
}

void appendRailGeometry(
    const ChunkBlockCell& cell,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int terrainTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
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

void appendRedstoneDustGeometry(
    const ChunkBlockCell& cell,
    bool connectWest,
    bool connectEast,
    bool connectNorth,
    bool connectSouth,
    bool climbWest,
    bool climbEast,
    bool climbNorth,
    bool climbSouth,
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const int crossTileIndex = normalizeTerrainTileIndex(cell.terrainTiles[0]);
  const int lineTileIndex = crossTileIndex + 1;
  const int powerLevel = cell.blockMetadata & 0x0F;
  const float power = static_cast<float>(powerLevel) / 15.0f;
  float red = power * 0.6f + 0.4f;
  if (powerLevel == 0) {
    red = 0.3f;
  }
  float green = std::max(power * power * 0.7f - 0.5f, 0.0f);
  float blue = std::max(power * power * 0.6f - 0.7f, 0.0f);
  const std::uint32_t vertexColor = packVertexColorRgba(red, green, blue, 1.0f);

  const auto computeTileUv = [](int terrainTileIndex, float& tileMinU, float& tileMaxU, float& tileMinV, float& tileMaxV) {
    tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
    tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
    tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
    tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  };

  const bool lineX = (connectWest || connectEast) && !connectNorth && !connectSouth;
  const bool lineZ = (connectNorth || connectSouth) && !connectWest && !connectEast;
  const int topTileIndex = (lineX || lineZ) ? lineTileIndex : crossTileIndex;

  float topMinU = 0.0f;
  float topMaxU = 0.0f;
  float topMinV = 0.0f;
  float topMaxV = 0.0f;
  computeTileUv(topTileIndex, topMinU, topMaxU, topMinV, topMaxV);

  float xMin = localX + 0.0f;
  float xMax = localX + 1.0f;
  float zMin = localZ + 0.0f;
  float zMax = localZ + 1.0f;
  constexpr float kConnectionInset = 0.3125f;
  constexpr float kConnectionUvInset = 5.0f / 256.0f;

  if (!(lineX || lineZ) && (connectWest || connectEast || connectNorth || connectSouth)) {
    if (!connectWest) {
      xMin += kConnectionInset;
      topMinU += kConnectionUvInset;
    }
    if (!connectEast) {
      xMax -= kConnectionInset;
      topMaxU -= kConnectionUvInset;
    }
    if (!connectNorth) {
      zMin += kConnectionInset;
      topMinV += kConnectionUvInset;
    }
    if (!connectSouth) {
      zMax -= kConnectionInset;
      topMaxV -= kConnectionUvInset;
    }
  }

  const float topY = localY + 0.015625f;
  if (lineZ) {
    appendCrossedQuadSheet(
        xMax,
        topY,
        zMax,
        topMaxU,
        topMaxV,
        xMax,
        topY,
        zMin,
        topMinU,
        topMaxV,
        xMin,
        topY,
        zMin,
        topMinU,
        topMinV,
        xMin,
        topY,
        zMax,
        topMaxU,
        topMinV,
        vertexColor,
        vertices,
        indices);
  } else {
    appendCrossedQuadSheet(
        xMax,
        topY,
        zMax,
        topMaxU,
        topMaxV,
        xMax,
        topY,
        zMin,
        topMaxU,
        topMinV,
        xMin,
        topY,
        zMin,
        topMinU,
        topMinV,
        xMin,
        topY,
        zMax,
        topMinU,
        topMaxV,
        vertexColor,
        vertices,
        indices);
  }

  float sideMinU = 0.0f;
  float sideMaxU = 0.0f;
  float sideMinV = 0.0f;
  float sideMaxV = 0.0f;
  computeTileUv(topTileIndex, sideMinU, sideMaxU, sideMinV, sideMaxV);

  const float wallLowY = localY + 0.0f;
  const float wallHighY = localY + 1.021875f;
  const float wallInset = 0.015625f;

  if (climbWest) {
    appendCrossedQuadSheet(
        localX + wallInset,
        wallHighY,
        localZ + 1.0f,
        sideMaxU,
        sideMinV,
        localX + wallInset,
        wallLowY,
        localZ + 1.0f,
        sideMinU,
        sideMinV,
        localX + wallInset,
        wallLowY,
        localZ + 0.0f,
        sideMinU,
        sideMaxV,
        localX + wallInset,
        wallHighY,
        localZ + 0.0f,
        sideMaxU,
        sideMaxV,
        vertexColor,
        vertices,
        indices);
  }

  if (climbEast) {
    appendCrossedQuadSheet(
        localX + 1.0f - wallInset,
        wallLowY,
        localZ + 1.0f,
        sideMinU,
        sideMaxV,
        localX + 1.0f - wallInset,
        wallHighY,
        localZ + 1.0f,
        sideMaxU,
        sideMaxV,
        localX + 1.0f - wallInset,
        wallHighY,
        localZ + 0.0f,
        sideMaxU,
        sideMinV,
        localX + 1.0f - wallInset,
        wallLowY,
        localZ + 0.0f,
        sideMinU,
        sideMinV,
        vertexColor,
        vertices,
        indices);
  }

  if (climbNorth) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        wallLowY,
        localZ + wallInset,
        sideMinU,
        sideMaxV,
        localX + 1.0f,
        wallHighY,
        localZ + wallInset,
        sideMaxU,
        sideMaxV,
        localX + 0.0f,
        wallHighY,
        localZ + wallInset,
        sideMaxU,
        sideMinV,
        localX + 0.0f,
        wallLowY,
        localZ + wallInset,
        sideMinU,
        sideMinV,
        vertexColor,
        vertices,
        indices);
  }

  if (climbSouth) {
    appendCrossedQuadSheet(
        localX + 1.0f,
        wallHighY,
        localZ + 1.0f - wallInset,
        sideMaxU,
        sideMinV,
        localX + 1.0f,
        wallLowY,
        localZ + 1.0f - wallInset,
        sideMinU,
        sideMinV,
        localX + 0.0f,
        wallLowY,
        localZ + 1.0f - wallInset,
        sideMinU,
        sideMaxV,
        localX + 0.0f,
        wallHighY,
        localZ + 1.0f - wallInset,
        sideMaxU,
        sideMaxV,
        vertexColor,
        vertices,
        indices);
  }
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
  const float anchorX = std::floor((cameraX + cloudScroll) / kFastCloudTileSize) * kFastCloudTileSize - cloudScroll;
  const float anchorZ = std::floor(cameraZ / kFastCloudTileSize) * kFastCloudTileSize;

  for (float x = -kFastCloudRadius; x < kFastCloudRadius; x += kFastCloudTileSize) {
    for (float z = -kFastCloudRadius; z < kFastCloudRadius; z += kFastCloudTileSize) {
      const float worldX0 = anchorX + x;
      const float worldX1 = worldX0 + kFastCloudTileSize;
      const float worldZ0 = anchorZ + z;
      const float worldZ1 = worldZ0 + kFastCloudTileSize;
      const float u0 = (worldX0 + cloudScroll) * kFastCloudUvScale;
      const float u1 = (worldX1 + cloudScroll) * kFastCloudUvScale;
      const float v0 = worldZ0 * kFastCloudUvScale;
      const float v1 = worldZ1 * kFastCloudUvScale;

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
  (void)cameraY;
  constexpr int kMinCloudCell = -kFancyCloudRadiusCells + 1;
  constexpr int kMaxCloudCell = kFancyCloudRadiusCells;
  const float bottomY = cloudHeight;
  const float topY = cloudHeight + kFancyCloudThickness - kFancyCloudInset;
  const float topCapY = topY + kFancyCloudInset;
  const float xPhase = (cameraX + cloudScroll) / kFancyCloudScale;
  const float zPhase = cameraZ / kFancyCloudScale + 0.33f;
  const float floorX = std::floor(xPhase);
  const float floorZ = std::floor(zPhase);
  const float fracX = xPhase - floorX;
  const float fracZ = zPhase - floorZ;
  const float baseU = floorX * kFancyCloudUvScale;
  const float baseV = floorZ * kFancyCloudUvScale;
  const float patchTileMinX = static_cast<float>(kMinCloudCell) * kFancyCloudCellSize;
  const float patchTileMaxX = static_cast<float>(kMaxCloudCell) * kFancyCloudCellSize + kFancyCloudCellSize;
  const float patchTileMinZ = static_cast<float>(kMinCloudCell) * kFancyCloudCellSize;
  const float patchTileMaxZ = static_cast<float>(kMaxCloudCell) * kFancyCloudCellSize + kFancyCloudCellSize;
  const float patchMinX = cameraX + (patchTileMinX - fracX) * kFancyCloudScale;
  const float patchMaxX = cameraX + (patchTileMaxX - fracX) * kFancyCloudScale;
  const float patchMinZ = cameraZ + (patchTileMinZ - fracZ) * kFancyCloudScale;
  const float patchMaxZ = cameraZ + (patchTileMaxZ - fracZ) * kFancyCloudScale;
  const float patchMinU = patchTileMinX * kFancyCloudUvScale + baseU;
  const float patchMaxU = patchTileMaxX * kFancyCloudUvScale + baseU;
  const float patchMinV = patchTileMinZ * kFancyCloudUvScale + baseV;
  const float patchMaxV = patchTileMaxZ * kFancyCloudUvScale + baseV;

  const std::uint32_t bottomColor = packVertexColorRgba(colorR * 0.7f, colorG * 0.7f, colorB * 0.7f, kCloudAlpha);
  const std::uint32_t topColor = packVertexColorRgba(colorR, colorG, colorB, kCloudAlpha);
  const std::uint32_t xSideColor = packVertexColorRgba(colorR * 0.9f, colorG * 0.9f, colorB * 0.9f, kCloudAlpha);
  const std::uint32_t zSideColor = packVertexColorRgba(colorR * 0.8f, colorG * 0.8f, colorB * 0.8f, kCloudAlpha);

  appendCloudQuad(
      patchMinX,
      topCapY,
      patchMaxZ,
      patchMinU,
      patchMaxV,
      patchMaxX,
      topCapY,
      patchMaxZ,
      patchMaxU,
      patchMaxV,
      patchMaxX,
      topCapY,
      patchMinZ,
      patchMaxU,
      patchMinV,
      patchMinX,
      topCapY,
      patchMinZ,
      patchMinU,
      patchMinV,
      0.0f,
      1.0f,
      0.0f,
      topColor,
      vertices,
      indices);

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

      if (cellX > kMinCloudCell) {
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

      if (cellX < kMaxCloudCell) {
        for (int strip = 0; strip < static_cast<int>(kFancyCloudCellSize); ++strip) {
          const float stripBase = tileX + static_cast<float>(strip) + 1.0f + kFancyCloudInset;
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

      if (cellZ > kMinCloudCell) {
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

      if (cellZ < kMaxCloudCell) {
        for (int strip = 0; strip < static_cast<int>(kFancyCloudCellSize); ++strip) {
          const float stripBase = tileZ + static_cast<float>(strip) + 1.0f + kFancyCloudInset;
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
  const bool isWaterMaterial = cell.materialClass == kWaterTerrainMaterialClass;
  const bool isLavaMaterial = cell.materialClass == kLavaTerrainMaterialClass;
  const int stillTerrainTileIndex = isLavaMaterial ? kLavaStillTerrainTile : kWaterStillTerrainTile;
  const int flowingTerrainTileIndex = isLavaMaterial ? kLavaFlowingTerrainTile : kWaterFlowingTerrainTile;
  const auto canonicalizeLiquidTerrainTile =
      [isWaterMaterial, isLavaMaterial, stillTerrainTileIndex, flowingTerrainTileIndex](
          std::int16_t terrainTileIndex,
          bool preferFlowing) {
        const int normalizedTerrainTileIndex = normalizeTerrainTileIndex(terrainTileIndex);
        if (!isWaterMaterial && !isLavaMaterial) {
          return normalizedTerrainTileIndex;
        }
        if (normalizedTerrainTileIndex != stillTerrainTileIndex
            && normalizedTerrainTileIndex != flowingTerrainTileIndex) {
          return normalizedTerrainTileIndex;
        }
        return preferFlowing ? flowingTerrainTileIndex : stillTerrainTileIndex;
      };

  const auto remapLiquidUv = [&cell](int terrainTileIndex, float atlasU, float atlasV) {
    const float tileOriginU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
    const float tileOriginV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
    const float tileSpan = kAtlasTileSizePixels / kAtlasSizePixels;
    const float localTileU = (atlasU - tileOriginU) / tileSpan;
    const float localTileV = (atlasV - tileOriginV) / tileSpan;
    const bool useFlowTile = (cell.materialClass == kWaterTerrainMaterialClass && terrainTileIndex == kWaterFlowingTerrainTile)
        || (cell.materialClass == kLavaTerrainMaterialClass && terrainTileIndex == kLavaFlowingTerrainTile);
    return std::array<float, 2> {
        localTileU * 0.5f + (useFlowTile ? 0.5f : 0.0f),
        localTileV,
    };
  };

  const auto appendLiquidQuad = [&cell, &remapLiquidUv, &vertices, &indices](
                                   int terrainTileIndex,
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
                                   float normalZ) {
    const auto uv0 = remapLiquidUv(terrainTileIndex, u0, v0);
    const auto uv1 = remapLiquidUv(terrainTileIndex, u1, v1);
    const auto uv2 = remapLiquidUv(terrainTileIndex, u2, v2);
    const auto uv3 = remapLiquidUv(terrainTileIndex, u3, v3);
    appendWaterQuad(
        x0,
        y0,
        z0,
        uv0[0],
        uv0[1],
        x1,
        y1,
        z1,
        uv1[0],
        uv1[1],
        x2,
        y2,
        z2,
        uv2[0],
        uv2[1],
        x3,
        y3,
        z3,
        uv3[0],
        uv3[1],
        normalX,
        normalY,
        normalZ,
        vertices,
        indices);
      if (cell.materialClass == kLavaTerrainMaterialClass) {
        appendWaterQuad(
          x3,
          y3,
          z3,
          uv3[0],
          uv3[1],
          x2,
          y2,
          z2,
          uv2[0],
          uv2[1],
          x1,
          y1,
          z1,
          uv1[0],
          uv1[1],
          x0,
          y0,
          z0,
          uv0[0],
          uv0[1],
          -normalX,
          -normalY,
          -normalZ,
          vertices,
          indices);
      }
  };

  const float heightNorthWest = cell.liquidHeights[0];
  const float heightNorthEast = cell.liquidHeights[1];
  const float heightSouthEast = cell.liquidHeights[2];
  const float heightSouthWest = cell.liquidHeights[3];

  if ((cell.liquidVisibilityMask & (1 << 1)) != 0) {
    int terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[1], false);
    double uvCenterU = static_cast<double>((terrainTileIndex & 0x0F) * 16 + 8) / kAtlasSizePixels;
    double uvCenterV = static_cast<double>((terrainTileIndex & 0xF0) + 8) / kAtlasSizePixels;
    float flowAngle = cell.liquidFlowAngle;
    if (flowAngle > -999.0f) {
      terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[2], true);
      uvCenterU = static_cast<double>((terrainTileIndex & 0x0F) * 16 + 8) / kAtlasSizePixels;
      uvCenterV = static_cast<double>((terrainTileIndex & 0xF0) + 8) / kAtlasSizePixels;
    } else {
      flowAngle = 0.0f;
    }

    const float sinAngle = std::sin(flowAngle) * 8.0f / kAtlasSizePixels;
    const float cosAngle = std::cos(flowAngle) * 8.0f / kAtlasSizePixels;
    appendLiquidQuad(
      terrainTileIndex,
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
        0.0f);
  }

  if ((cell.liquidVisibilityMask & (1 << 0)) != 0) {
      const int terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[0], false);
      const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
      const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
      const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
      appendLiquidQuad(
        terrainTileIndex,
        localX,
        localY,
        localZ,
        tileMinU,
        tileMinV,
        localX,
        localY,
        localZ + 1.0f,
        tileMinU,
        tileMaxV,
        localX + 1.0f,
        localY,
        localZ + 1.0f,
        tileMaxU,
        tileMaxV,
        localX + 1.0f,
        localY,
        localZ,
        tileMaxU,
        tileMinV,
        0.0f,
        -1.0f,
        0.0f);
  }

  for (int sideIndex = 0; sideIndex < 4; ++sideIndex) {
    const int minecraftSide = sideIndex + 2;
    if ((cell.liquidVisibilityMask & (1 << minecraftSide)) == 0) {
      continue;
    }

    const int terrainTileIndex = canonicalizeLiquidTerrainTile(cell.terrainTiles[minecraftSide], true);
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
  appendLiquidQuad(
    terrainTileIndex,
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
        normalZ);
  }
}

void appendFaceGeometry(
    int faceIndex,
    float localX,
    float localY,
    float localZ,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    float normalOffset,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const int terrainTile = normalizeTerrainTileIndex(terrainTileIndex);
  const bool flipU = usesFlippedTerrainTile(terrainTileIndex);
  const float tileMinU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTile & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTile & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = localX + kFaceVertexOffsets[faceIndex][vertexIndex][0] + kFaceNormals[faceIndex][0] * normalOffset;
    vertex.position[1] = localY + kFaceVertexOffsets[faceIndex][vertexIndex][1] + kFaceNormals[faceIndex][1] * normalOffset;
    vertex.position[2] = localZ + kFaceVertexOffsets[faceIndex][vertexIndex][2] + kFaceNormals[faceIndex][2] * normalOffset;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    const float u = kFaceTexcoords[faceIndex][vertexIndex][0] == 0.0f ? tileMinU : tileMaxU;
    vertex.texcoord[0] = flipU ? (u == tileMinU ? tileMaxU : tileMinU) : u;
    vertex.texcoord[1] = kFaceTexcoords[faceIndex][vertexIndex][1] == 0.0f ? tileMinV : tileMaxV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

}  // namespace detail
}  // namespace mcrtx
