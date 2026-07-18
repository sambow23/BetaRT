// Renderer UI overlays, textures, draw lists, and UI state.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <bit>
#include <cstddef>
#include <cstdint>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

void RemixRenderer::setScreenTint(float r, float g, float b, float a) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setScreenTint");
  std::scoped_lock lock(mutex_);
  if (!initialized_) {
    return;
  }
  if (remix_.SetScreenTint == nullptr) {
    if (!warnedMissingSetScreenTint_) {
      warnedMissingSetScreenTint_ = true;
      log("SetScreenTint not available; fullscreen tint updates are disabled");
    }
    return;
  }
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetScreenTint");
    return remix_.SetScreenTint(r, g, b, a);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetScreenTint failed: " + errorCodeToString(result));
  }
}

bool RemixRenderer::drawScreenOverlay(
    const void* pixelData,
    std::uint32_t width,
    std::uint32_t height,
    remixapi_Format format,
    float opacity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::drawScreenOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("drawScreenOverlay called before initialize");
    return false;
  }

  if (standaloneOutputWindow_) {
    return true;
  }

  if (remix_.DrawScreenOverlay == nullptr) {
    setError("DrawScreenOverlay is unavailable in the loaded Remix runtime");
    return false;
  }

  if (pixelData == nullptr || width == 0 || height == 0) {
    setError("DrawScreenOverlay requires non-null pixel data and non-zero dimensions");
    return false;
  }

  if (format != REMIXAPI_FORMAT_R8G8B8A8_UNORM
      && format != REMIXAPI_FORMAT_B8G8R8A8_UNORM) {
    setError("DrawScreenOverlay only supports RGBA8 and BGRA8 overlay buffers");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawScreenOverlay.draw");
    return remix_.DrawScreenOverlay(
        pixelData,
        width,
        height,
        format,
        std::clamp(opacity, 0.0f, 1.0f));
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("DrawScreenOverlay failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::clearScreenOverlay() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearScreenOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return true;
  }

  if (standaloneOutputWindow_) {
    return true;
  }

  if (remix_.DrawScreenOverlay == nullptr) {
    setError("DrawScreenOverlay is unavailable in the loaded Remix runtime");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DrawScreenOverlay.clear");
    return remix_.DrawScreenOverlay(
        nullptr,
        0,
        0,
        REMIXAPI_FORMAT_R8G8B8A8_UNORM,
        0.0f);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("DrawScreenOverlay clear failed: " + errorCodeToString(result));
    return false;
  }

  return true;
}

bool RemixRenderer::registerUiTexture(
    std::uint64_t   id,
    std::uint32_t   width,
    std::uint32_t   height,
    remixapi_Format format,
    const void*     pixelData,
    std::uint64_t   dataSize) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::registerUiTexture");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("registerUiTexture called before initialize");
    return false;
  }
  if (standaloneOutputWindow_) {
    return true;
  }
  if (remix_.RegisterUITexture == nullptr) {
    setError("RegisterUITexture is unavailable in the loaded Remix runtime");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "RegisterUITexture");
    return remix_.RegisterUITexture(id, width, height, format, pixelData, dataSize);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("RegisterUITexture failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::freeUiTexture(std::uint64_t id) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::freeUiTexture");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return true;
  }
  if (standaloneOutputWindow_) {
    return true;
  }
  if (remix_.FreeUITexture == nullptr) {
    setError("FreeUITexture is unavailable in the loaded Remix runtime");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "FreeUITexture");
    return remix_.FreeUITexture(id);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("FreeUITexture failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::submitUiDrawList(const remixapi_UIDrawList* drawList) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::submitUiDrawList");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    setError("submitUiDrawList called before initialize");
    return false;
  }
  if (standaloneOutputWindow_) {
    return true;
  }
  if (remix_.SubmitUIDrawList == nullptr) {
    setError("SubmitUIDrawList is unavailable in the loaded Remix runtime");
    return false;
  }

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SubmitUIDrawList");
    return remix_.SubmitUIDrawList(drawList);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SubmitUIDrawList failed: " + errorCodeToString(result));
    return false;
  }
  return true;
}

bool RemixRenderer::submitUiDrawListFromArrays(
    const float*         vertexXYZUV,
    const std::uint32_t* vertexColor,
    std::uint32_t        vertexCount,
    const std::uint64_t* cmdTextureIds,
    const std::int32_t*  cmdQuadCounts,
    const std::uint32_t* cmdFlags,
    std::uint32_t        cmdCount,
    std::uint32_t        displayWidth,
    std::uint32_t        displayHeight) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::submitUiDrawListFromArrays");

  // An empty list clears the UI for this frame.
  if (vertexCount == 0 || cmdCount == 0) {
    remixapi_UIDrawList empty {};
    empty.sType = REMIXAPI_STRUCT_TYPE_UI_DRAW_LIST;
    empty.displayWidth = displayWidth;
    empty.displayHeight = displayHeight;
    return submitUiDrawList(&empty);
  }

  std::vector<remixapi_UIVertex> vertices(vertexCount);
  for (std::uint32_t i = 0; i < vertexCount; ++i) {
    vertices[i].x = vertexXYZUV[i * 5 + 0];
    vertices[i].y = vertexXYZUV[i * 5 + 1];
    vertices[i].z = vertexXYZUV[i * 5 + 2];
    vertices[i].u = vertexXYZUV[i * 5 + 3];
    vertices[i].v = vertexXYZUV[i * 5 + 4];
    vertices[i].color = vertexColor[i];
  }

  std::vector<uint32_t> indices;
  std::vector<remixapi_UIDrawCommand> commands;
  commands.reserve(cmdCount);

  uint32_t vertexCursor = 0;
  for (std::uint32_t c = 0; c < cmdCount; ++c) {
    const int32_t quadCount = cmdQuadCounts[c];
    if (quadCount <= 0) {
      continue;
    }

    remixapi_UIDrawCommand cmd {};
    cmd.textureId = cmdTextureIds[c];
    cmd.indexOffset = static_cast<uint32_t>(indices.size());
    cmd.indexCount = static_cast<uint32_t>(quadCount) * 6;
    cmd.vertexOffset = 0;
    cmd.flags = cmdFlags != nullptr ? cmdFlags[c] : 0u;

    for (int32_t q = 0; q < quadCount; ++q) {
      const uint32_t base = vertexCursor + static_cast<uint32_t>(q) * 4;
      indices.push_back(base + 0);
      indices.push_back(base + 1);
      indices.push_back(base + 2);
      indices.push_back(base + 0);
      indices.push_back(base + 2);
      indices.push_back(base + 3);
    }
    vertexCursor += static_cast<uint32_t>(quadCount) * 4;
    commands.push_back(cmd);
  }

  if (vertexCursor > vertexCount || commands.empty()) {
    setError("submitUiDrawListFromArrays: command quad counts exceed vertex array");
    return false;
  }

  remixapi_UIDrawList drawList {};
  drawList.sType = REMIXAPI_STRUCT_TYPE_UI_DRAW_LIST;
  drawList.pNext = nullptr;
  drawList.displayWidth = displayWidth;
  drawList.displayHeight = displayHeight;
  drawList.pVertices = vertices.data();
  drawList.vertexCount = static_cast<uint32_t>(vertices.size());
  drawList.pIndices = indices.data();
  drawList.indexCount = static_cast<uint32_t>(indices.size());
  drawList.pCommands = commands.data();
  drawList.commandCount = static_cast<uint32_t>(commands.size());

  return submitUiDrawList(&drawList);
}

void RemixRenderer::submitSyntheticUiTest() {
  if (!syntheticUiTestEnabled_ || standaloneOutputWindow_) {
    return;
  }
  if (remix_.SubmitUIDrawList == nullptr || remix_.RegisterUITexture == nullptr) {
    return;
  }

  constexpr std::uint64_t kSyntheticTextureId = 1;

  // Register a 64x64 magenta/cyan checkerboard once. RGBA8, R in the low byte.
  if (!syntheticUiTextureRegistered_) {
    constexpr std::uint32_t kDim = 64;
    constexpr std::uint32_t kCell = 8;
    std::vector<std::uint32_t> pixels(static_cast<std::size_t>(kDim) * kDim);
    for (std::uint32_t y = 0; y < kDim; ++y) {
      for (std::uint32_t x = 0; x < kDim; ++x) {
        const bool even = ((x / kCell) + (y / kCell)) % 2 == 0;
        // 0xAABBGGRR packing (R low byte). Magenta vs cyan, full alpha.
        pixels[static_cast<std::size_t>(y) * kDim + x] =
            even ? 0xFFFF00FFu /* magenta */ : 0xFFFFFF00u /* cyan */;
      }
    }
    const remixapi_ErrorCode result = remix_.RegisterUITexture(
        kSyntheticTextureId, kDim, kDim,
        REMIXAPI_FORMAT_R8G8B8A8_UNORM,
        pixels.data(),
        static_cast<std::uint64_t>(pixels.size()) * sizeof(std::uint32_t));
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      log("MCRTX_UI_SYNTHETIC_TEST: RegisterUITexture failed: " + errorCodeToString(result));
      syntheticUiTestEnabled_ = false;
      return;
    }
    syntheticUiTextureRegistered_ = true;
    log("MCRTX_UI_SYNTHETIC_TEST: registered checkerboard UI texture");
  }

  auto makeVertex = [](float x, float y, float u, float v, std::uint32_t color) {
    remixapi_UIVertex vert {};
    vert.x = x;
    vert.y = y;
    vert.u = u;
    vert.v = v;
    vert.color = color;
    return vert;
  };

  // Quad A: solid, vertex-colored green (textureId 0 = built-in white).
  // Quad B: checkerboard texture (textureId 1), white vertex color.
  const float ax0 = 40.0f, ay0 = 40.0f, ax1 = 200.0f, ay1 = 200.0f;
  const float bx0 = 220.0f, by0 = 40.0f, bx1 = 380.0f, by1 = 200.0f;
  const std::uint32_t kGreen = 0xC000FF00u; // 0xAABBGGRR: green, ~75% alpha
  const std::uint32_t kWhite = 0xFFFFFFFFu;

  const std::vector<remixapi_UIVertex> vertices {
    makeVertex(ax0, ay0, 0.0f, 0.0f, kGreen),
    makeVertex(ax1, ay0, 1.0f, 0.0f, kGreen),
    makeVertex(ax1, ay1, 1.0f, 1.0f, kGreen),
    makeVertex(ax0, ay1, 0.0f, 1.0f, kGreen),
    makeVertex(bx0, by0, 0.0f, 0.0f, kWhite),
    makeVertex(bx1, by0, 1.0f, 0.0f, kWhite),
    makeVertex(bx1, by1, 1.0f, 1.0f, kWhite),
    makeVertex(bx0, by1, 0.0f, 1.0f, kWhite),
  };
  const std::vector<std::uint32_t> indices {
    0, 1, 2, 0, 2, 3,
    4, 5, 6, 4, 6, 7,
  };

  remixapi_UIDrawCommand commands[2] {};
  commands[0].textureId = 0;                  // built-in white
  commands[0].indexCount = 6;
  commands[0].indexOffset = 0;
  commands[0].vertexOffset = 0;
  commands[1].textureId = kSyntheticTextureId; // checkerboard
  commands[1].indexCount = 6;
  commands[1].indexOffset = 6;
  commands[1].vertexOffset = 0;

  remixapi_UIDrawList drawList {};
  drawList.sType = REMIXAPI_STRUCT_TYPE_UI_DRAW_LIST;
  drawList.pNext = nullptr;
  drawList.displayWidth = width_;
  drawList.displayHeight = height_;
  drawList.pVertices = vertices.data();
  drawList.vertexCount = static_cast<std::uint32_t>(vertices.size());
  drawList.pIndices = indices.data();
  drawList.indexCount = static_cast<std::uint32_t>(indices.size());
  drawList.pCommands = commands;
  drawList.commandCount = 2;

  const remixapi_ErrorCode result = remix_.SubmitUIDrawList(&drawList);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    log("MCRTX_UI_SYNTHETIC_TEST: SubmitUIDrawList failed: " + errorCodeToString(result));
    syntheticUiTestEnabled_ = false;
  }
}

remixapi_UIState RemixRenderer::getUiState() const {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::getUiState");
  std::scoped_lock lock(mutex_);

  if (standaloneOutputWindow_) {
    return REMIXAPI_UI_STATE_NONE;
  }

  if (!initialized_ || remix_.GetUIState == nullptr) {
    return REMIXAPI_UI_STATE_NONE;
  }

  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "GetUIState");
  return remix_.GetUIState();
}

bool RemixRenderer::setUiState(remixapi_UIState state) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::setUiState");
  std::scoped_lock lock(mutex_);

  if (standaloneOutputWindow_) {
    return state == REMIXAPI_UI_STATE_NONE;
  }

  if (!initialized_) {
    setError("setUiState called before initialize");
    return false;
  }

  if (remix_.SetUIState == nullptr) {
    setError("SetUIState is unavailable in the loaded Remix runtime");
    return false;
  }

  if (state != REMIXAPI_UI_STATE_NONE
      && state != REMIXAPI_UI_STATE_BASIC
      && state != REMIXAPI_UI_STATE_ADVANCED) {
    setError("setUiState received an unsupported Remix UI state");
    return false;
  }

  remixapi_ErrorCode result;
  {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "SetUIState");
    result = remix_.SetUIState(state);
  }
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("SetUIState failed: " + errorCodeToString(result));
    return false;
  }

  syncOutputWindowInteractivity(state);

  return true;
}


}  // namespace mcrtx
