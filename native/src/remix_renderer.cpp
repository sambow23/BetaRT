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
constexpr std::size_t kMaxOpaqueBlocksPerChunk = 1024;

constexpr remixapi_HardcodedVertex kCubeVertices[] = {
  {{0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, -1.0f}, {0.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, -1.0f}, {1.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 1.0f, 0.0f}, {0.0f, 0.0f, -1.0f}, {1.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 1.0f, 0.0f}, {0.0f, 0.0f, -1.0f}, {0.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {1.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {1.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 1.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 0.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {0.0f, 0.0f}, 0xFFFFFFFFu},
  {{0.0f, 1.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {1.0f, 0.0f}, 0xFFFFFFFFu},
  {{0.0f, 1.0f, 1.0f}, {-1.0f, 0.0f, 0.0f}, {1.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 0.0f, 1.0f}, {-1.0f, 0.0f, 0.0f}, {0.0f, 1.0f}, 0xFFFFFFFFu},
  {{1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 1.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 1.0f, 1.0f}, {1.0f, 0.0f, 0.0f}, {1.0f, 1.0f}, 0xFFFFFFFFu},
  {{1.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 0.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 0.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {1.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 0.0f, 1.0f}, {0.0f, -1.0f, 0.0f}, {1.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 0.0f, 1.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {1.0f, 0.0f}, 0xFFFFFFFFu},
  {{1.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 0.0f}, {1.0f, 1.0f}, 0xFFFFFFFFu},
  {{0.0f, 1.0f, 1.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 1.0f}, 0xFFFFFFFFu},
};

constexpr std::uint32_t kCubeIndices[] = {
  0, 1, 2, 0, 2, 3,
  4, 6, 5, 4, 7, 6,
  8, 9, 10, 8, 10, 11,
  12, 14, 13, 12, 15, 14,
  16, 18, 17, 16, 19, 18,
  20, 21, 22, 20, 22, 23,
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
  std::uint64_t hash = 0x4D43525458000000ull;
  hash ^= (static_cast<std::uint64_t>(static_cast<std::uint32_t>(key.originX)) << 32);
  hash ^= (static_cast<std::uint64_t>(static_cast<std::uint32_t>(key.originY)) << 16);
  hash ^= static_cast<std::uint64_t>(static_cast<std::uint32_t>(key.originZ));
  hash ^= static_cast<std::uint64_t>(static_cast<std::uint32_t>(key.renderPass)) << 48;
  hash ^= sequence;
  return hash;
}

void appendCubeGeometry(
    float localX,
    float localY,
    float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  vertices.reserve(vertices.size() + std::size(kCubeVertices));
  indices.reserve(indices.size() + std::size(kCubeIndices));

  for (const remixapi_HardcodedVertex& baseVertexData : kCubeVertices) {
    remixapi_HardcodedVertex vertex = baseVertexData;
    vertex.position[0] += localX;
    vertex.position[1] += localY;
    vertex.position[2] += localZ;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kCubeIndices) {
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
    int blockX, int blockY, int blockZ, int blockId, int blockMetadata, int renderType) {
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
  } else {
    auto chunkIt = chunkMeshes_.find(chunkKey);
    if (chunkIt != chunkMeshes_.end()) {
      destroyChunkMesh(chunkIt->second);
      chunkMeshes_.erase(chunkIt);
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
    return true;
  }

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(blocks.size() * std::size(kCubeVertices));
  indices.reserve(blocks.size() * std::size(kCubeIndices));

  for (const CapturedBlockInstance& block : blocks) {
    appendCubeGeometry(
        static_cast<float>(block.position[0] - chunkKey.originX),
        static_cast<float>(block.position[1] - chunkKey.originY),
        static_cast<float>(block.position[2] - chunkKey.originZ),
        vertices,
        indices);
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = nullptr;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeChunkMeshHash(chunkKey, nextChunkMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMesh(&meshInfo, &newMeshHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyChunkMesh(meshData);
  meshData.meshHandle = newMeshHandle;
  meshData.meshHash = meshInfo.hash;
  meshData.blockCount = blocks.size();
  return true;
}

void RemixRenderer::destroyChunkMesh(ChunkMeshData& meshData) {
  if (meshData.meshHandle != nullptr && remix_.DestroyMesh != nullptr) {
    remix_.DestroyMesh(meshData.meshHandle);
  }
  meshData = {};
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
  remixapi_CameraInfoParameterizedEXT params {};
  params.sType = REMIXAPI_STRUCT_TYPE_CAMERA_INFO_PARAMETERIZED_EXT;
  params.position = {camera_.position[0], camera_.position[1], camera_.position[2]};
  params.forward = {camera_.forward[0], camera_.forward[1], camera_.forward[2]};
  params.up = {camera_.up[0], camera_.up[1], camera_.up[2]};
  params.right = {camera_.right[0], camera_.right[1], camera_.right[2]};
  params.fovYInDegrees = camera_.fovYDegrees;
  params.aspect = camera_.aspect;
  params.nearPlane = camera_.nearPlane;
  params.farPlane = camera_.farPlane;

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
