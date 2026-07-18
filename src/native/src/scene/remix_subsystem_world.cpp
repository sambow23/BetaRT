// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

namespace {

using mcrtx::geometry::appendCloudQuad;

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

}  // namespace

void RemixRenderer::updateCloudLayer(
    bool fancy,
    float cameraX,
    float cameraY,
    float cameraZ,
    float cloudHeight,
    float cloudScroll,
  float,
    float colorR,
    float colorG,
    float colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateCloudLayer");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  cloudTransformX_ = 0.0f;
  cloudTransformY_ = 0.0f;
  cloudTransformZ_ = 0.0f;

  if (cloudMeshHandle_ == nullptr || cloudMeshFancy_ != fancy) {
    log(std::string("Rebuilding cloud mesh: mode=") + (fancy ? "fancy" : "fast")
        + (cloudMeshHandle_ == nullptr ? " reason=missing" : " reason=mode-switch"));
  }

  rebuildCloudMesh(fancy, cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, currentRenderOriginLocked());
}

void RemixRenderer::updateAtmosphereState(float celestialAngle, bool forceDarkAtmosphere) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::updateAtmosphereState");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  updateAtmosphereConfigLocked(celestialAngle, forceDarkAtmosphere);
}

void RemixRenderer::clearCloudLayer() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearCloudLayer");
  std::scoped_lock lock(mutex_);
  if (cloudMeshHandle_ != nullptr) {
    log("Clearing cloud mesh cache");
  }
  destroyCloudMesh();
}

void RemixRenderer::clearWorldScene() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::clearWorldScene");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  for (auto& [chunkKey, meshData] : chunkMeshes_) {
    (void)chunkKey;
    destroyChunkMesh(meshData);
  }
  chunkMeshes_.clear();

  destroyFireMesh();
  destroyDestroyOverlayMesh();
  destroyBlockOutlineMesh();
  destroyParticleMesh();
  destroyDynamicEntityMeshes();
  clearDynamicEntityFrameInstances();
  destroyOverlayInstances_.clear();
  blockOutlineInstances_.clear();
  particleQuads_.clear();
  while (!entityHeldTorchLights_.empty()) {
    destroyEntityHeldTorchLight(entityHeldTorchLights_.begin()->first);
  }
  entityHeldTorchLightsSeenThisFrame_.clear();
  activeDynamicEntity_ = {};
  activeChunkBlocks_.clear();
  activeChunkBuild_ = {};
  chunkBuildActive_ = false;
  lastSubmittedChunkCount_ = 0;
  lastSubmittedBlockCount_ = 0;
  lastSubmittedCloudQuadCount_ = 0;
  lastSubmittedFireQuadCount_ = 0;
  lastSubmittedDynamicEntityQuadCount_ = 0;
  lastSubmittedDestroyOverlayCount_ = 0;
  lastSubmittedBlockOutlineCount_ = 0;
  lastSubmittedParticleQuadCount_ = 0;
  lastSubmittedTorchLightCount_ = 0;
  lastFireAnimationFrame_ = 0xFFFFFFFFu;
  lastFireChunkBuildCount_ = 0xFFFFFFFFFFFFFFFFull;

  log("Cleared cached world scene state");
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
    float colorB,
    const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildCloudMesh");
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

  if (renderOrigin.enabled) {
    for (remixapi_HardcodedVertex& vertex : vertices) {
      const WorldRenderPosition position = rebaseWorldPosition(
          vertex.position[0],
          vertex.position[1],
          vertex.position[2],
          renderOrigin);
      vertex.position[0] = position.x;
      vertex.position[1] = position.y;
      vertex.position[2] = position.z;
    }
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
  meshInfo.hash = fancy ? 0x4D43525458434C46ull : 0x4D43525458434C30ull;
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  // Cloud geometry is rebuilt from live world-space phase math. Reuse a stable
  // per-mode hash, but destroy the previous cloud mesh first so Remix never has
  // two different live meshes with the same identity at once.
  destroyCloudMesh();

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.cloud");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  cloudMeshHandle_ = newMeshHandle;
  cloudMeshFancy_ = fancy;
  cloudMeshPhaseX_ = 0;
  cloudMeshPhaseZ_ = 0;
  cloudQuadCount_ = indices.size() / 6;
  if (isVerboseLoggingEnabled()) {
    log(std::string("Cloud mesh ready: mode=") + (fancy ? "fancy" : "fast")
        + " quads=" + std::to_string(cloudQuadCount_)
        + " hash=0x" + [&]() {
            std::ostringstream stream;
            stream << std::hex << meshInfo.hash;
            return stream.str();
          }());
  }
  return true;
}

void RemixRenderer::destroyCloudMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyCloudMesh");
  destroyMeshHandle(cloudMeshHandle_);
  cloudMeshFancy_ = false;
  cloudQuadCount_ = 0;
}

}  // namespace mcrtx
