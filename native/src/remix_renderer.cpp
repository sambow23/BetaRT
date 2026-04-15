#include "mcrtx/remix_renderer.hpp"

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
constexpr std::size_t kTerrainMaterialClassCount = 2;
constexpr std::uint8_t kOpaqueTerrainMaterialClass = 0;
constexpr std::uint8_t kCutoutTerrainMaterialClass = 1;
constexpr float kAtlasSizePixels = 256.0f;
constexpr float kAtlasTileSizePixels = 16.0f;
constexpr float kAtlasUvInsetPixels = 0.01f;
constexpr std::uint64_t kOpaqueTerrainMaterialHash = 0x4D435254584F5041ull;
constexpr std::uint64_t kCutoutTerrainMaterialHash = 0x4D43525458435554ull;

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

bool shouldCaptureBlock(int blockId, int renderType) {
  return blockId > 0 && renderType == 0;
}

bool usesCutoutMaterialForBlock(int blockId) {
  switch (blockId) {
    case 18:
    case 20:
    case 52:
      return true;
    default:
      return false;
  }
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
    for (const std::uint8_t tileIndex : cells[index].terrainTiles) {
      fingerprint ^= static_cast<std::uint64_t>(tileIndex);
      fingerprint *= 1099511628211ull;
    }
  }
  return fingerprint;
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
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const float tileMinU = static_cast<float>((terrainTileIndex & 0x0F) * 16) / kAtlasSizePixels;
  const float tileMinV = static_cast<float>(terrainTileIndex & 0xF0) / kAtlasSizePixels;
  const float tileMaxU = (static_cast<float>((terrainTileIndex & 0x0F) * 16) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;
  const float tileMaxV = (static_cast<float>(terrainTileIndex & 0xF0) + kAtlasTileSizePixels - kAtlasUvInsetPixels) / kAtlasSizePixels;

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = localX + kFaceVertexOffsets[faceIndex][vertexIndex][0];
    vertex.position[1] = localY + kFaceVertexOffsets[faceIndex][vertexIndex][1];
    vertex.position[2] = localZ + kFaceVertexOffsets[faceIndex][vertexIndex][2];
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    vertex.texcoord[0] = kFaceTexcoords[faceIndex][vertexIndex][0] == 0.0f ? tileMinU : tileMaxU;
    vertex.texcoord[1] = kFaceTexcoords[faceIndex][vertexIndex][1] == 0.0f ? tileMinV : tileMaxV;
    vertex.color = 0xFFFFFFFFu;
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
  nextChunkMeshHash_ = 1;
  presentedFrames_ = 0;
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  terrainAtlasPath_.clear();
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

bool RemixRenderer::beginChunkBuild(
    int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ, int renderPass) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return false;
  }

  if (renderPass != 0) {
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
  int texture5) {
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !chunkBuildActive_) {
    return;
  }

  ++activeChunkBuild_.blockCount;
  ++capturedBlocks_;
  if (blockId >= 0 && blockId < static_cast<int>(activeChunkBuild_.blockIdCounts.size())) {
    ++activeChunkBuild_.blockIdCounts[static_cast<std::size_t>(blockId)];
  }

  if (!shouldCaptureBlock(blockId, renderType) || activeChunkBlocks_.size() >= kMaxOpaqueBlocksPerChunk) {
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
  block.materialClass = usesCutoutMaterialForBlock(blockId) ? kCutoutTerrainMaterialClass : kOpaqueTerrainMaterialClass;
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
    materialInfo.hash = cutout ? kCutoutTerrainMaterialHash : kOpaqueTerrainMaterialHash;
    materialInfo.albedoTexture = terrainAtlasPath_.c_str();
    materialInfo.emissiveIntensity = 0.0f;
    materialInfo.emissiveColorConstant = {0.0f, 0.0f, 0.0f};
    materialInfo.filterMode = 1;
    materialInfo.wrapModeU = 1;
    materialInfo.wrapModeV = 1;

    remixapi_MaterialHandle materialHandle = nullptr;
    const remixapi_ErrorCode result = remix_.CreateMaterial(&materialInfo, &materialHandle);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      log(std::string("CreateMaterial failed for terrain material class ")
          + std::to_string(materialClass)
          + ": "
          + errorCodeToString(result));
      return false;
    }

    terrainMaterialHandles_[materialClass] = materialHandle;
    return true;
  };

  const bool opaqueCreated = createTerrainMaterial(kOpaqueTerrainMaterialClass, false);
  const bool cutoutCreated = createTerrainMaterial(kCutoutTerrainMaterialClass, true);
  if (opaqueCreated) {
    log("Initialized terrain atlas materials from " + terrainAtlasPath_.string());
  }
  if (!cutoutCreated) {
    log("Cutout terrain material unavailable; cutout faces will use fallback material");
  }
  return opaqueCreated;
}

void RemixRenderer::destroyTerrainMaterials() {
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

  if (!forceRebuild
      && meshData.meshHandle != nullptr
      && meshData.blockCount != 0) {
    return true;
  }

  std::array<std::vector<remixapi_HardcodedVertex>, kTerrainMaterialClassCount> verticesByMaterial;
  std::array<std::vector<std::uint32_t>, kTerrainMaterialClassCount> indicesByMaterial;
  for (std::size_t materialClass = 0; materialClass < kTerrainMaterialClassCount; ++materialClass) {
    verticesByMaterial[materialClass].reserve(meshData.blockCount * 12);
    indicesByMaterial[materialClass].reserve(meshData.blockCount * 18);
  }

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

        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
          const int neighborX = localX + kNeighborOffsets[faceIndex][0];
          const int neighborY = localY + kNeighborOffsets[faceIndex][1];
          const int neighborZ = localZ + kNeighborOffsets[faceIndex][2];

          bool faceOccluded = false;
          const bool neighborInsideChunk =
              neighborX >= 0 && neighborX < kChunkDimension
              && neighborY >= 0 && neighborY < kChunkDimension
              && neighborZ >= 0 && neighborZ < kChunkDimension;
          if (neighborInsideChunk) {
            faceOccluded = meshData.occupancy[blockIndex(neighborX, neighborY, neighborZ)] != 0;
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
              faceOccluded = neighborIt->second.occupancy[blockIndex(wrappedX, wrappedY, wrappedZ)] != 0;
            }
          }

          if (faceOccluded) {
            continue;
          }

          appendFaceGeometry(
              faceIndex,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              cell.terrainTiles[kNativeFaceToMinecraftSide[faceIndex]],
              verticesByMaterial[materialClass],
              indicesByMaterial[materialClass]);
        }
      }
    }
  }

  std::array<remixapi_MeshInfoSurfaceTriangles, kTerrainMaterialClassCount> surfaces {};
  std::size_t surfaceCount = 0;
  for (std::size_t materialClass = 0; materialClass < kTerrainMaterialClassCount; ++materialClass) {
    if (indicesByMaterial[materialClass].empty()) {
      continue;
    }

    remixapi_MeshInfoSurfaceTriangles surface {};
    surface.vertices_values = verticesByMaterial[materialClass].data();
    surface.vertices_count = verticesByMaterial[materialClass].size();
    surface.indices_values = indicesByMaterial[materialClass].data();
    surface.indices_count = indicesByMaterial[materialClass].size();
    surface.skinning_hasvalue = FALSE;
    surface.material = terrainMaterialHandles_[materialClass];
    surfaces[surfaceCount++] = surface;
  }

  if (surfaceCount == 0) {
    destroyChunkMesh(meshData);
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeChunkMeshHash(chunkKey, nextChunkMeshHash_++);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = surfaceCount;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMesh(&meshInfo, &newMeshHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyChunkMesh(meshData);
  meshData.meshHandle = newMeshHandle;
  meshData.meshHash = meshInfo.hash;
  return true;
}

void RemixRenderer::destroyChunkMesh(ChunkMeshData& meshData) {
  if (meshData.meshHandle != nullptr && remix_.DestroyMesh != nullptr) {
    remix_.DestroyMesh(meshData.meshHandle);
  }
  meshData.meshHandle = nullptr;
  meshData.meshHash = 0;
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
  if (chunkMeshes_.empty()) {
    if (presentedFrames_ < 4) {
      log("No captured chunk meshes available yet");
    }
    ++presentedFrames_;
    return true;
  }

  std::size_t submittedChunks = 0;
  std::size_t submittedBlocks = 0;
  for (const auto& [chunkKey, meshData] : chunkMeshes_) {
    if (meshData.meshHandle == nullptr) {
      continue;
    }

    remixapi_InstanceInfo instanceInfo {};
    instanceInfo.sType = REMIXAPI_STRUCT_TYPE_INSTANCE_INFO;
    instanceInfo.categoryFlags = REMIXAPI_INSTANCE_CATEGORY_BIT_TERRAIN;
    instanceInfo.mesh = meshData.meshHandle;
    instanceInfo.transform = makeTranslationTransform(
        static_cast<float>(chunkKey.originX),
        static_cast<float>(chunkKey.originY),
        static_cast<float>(chunkKey.originZ));
    instanceInfo.doubleSided = FALSE;

    const remixapi_ErrorCode result = remix_.DrawInstance(&instanceInfo);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("DrawInstance failed: " + errorCodeToString(result));
      return false;
    }

    ++submittedChunks;
    submittedBlocks += meshData.blockCount;
  }

  if (presentedFrames_ < 8
      || submittedChunks != lastSubmittedChunkCount_
      || submittedBlocks != lastSubmittedBlockCount_) {
    std::ostringstream stream;
    stream << "Submitted " << submittedChunks
           << " chunk meshes covering " << submittedBlocks
           << " blocks";
    log(stream.str());
  }

  lastSubmittedChunkCount_ = submittedChunks;
  lastSubmittedBlockCount_ = submittedBlocks;
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
