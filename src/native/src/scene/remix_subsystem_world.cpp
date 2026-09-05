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
constexpr std::uint64_t kCloudMeshHashSeed = 0x434C000000000000ull;
constexpr std::uint64_t kCloudMeshFancyBit = 0x0000800000000000ull;

std::uint64_t makeCloudMeshHash(bool fancy) {
  return kCloudMeshHashSeed
      | (fancy ? kCloudMeshFancyBit : 0);
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

  cloudLayer_ = {true, fancy, cameraX, cameraY, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB};
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
  cloudLayer_ = {};
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

  cloudLayer_ = {};
  publishedCloudLayer_ = {};
  destroyCloudMesh();
  destroyFireMesh();
  destroyDestroyOverlayMesh();
  destroyBlockOutlineMesh();
  destroyLightLevelOverlayMesh();
  destroyParticleMesh();
  destroyDynamicEntityMeshes();
  clearDynamicEntityFrameInstances();
  destroyOverlayInstances_.clear();
  blockOutlineInstances_.clear();
  lightLevelMarkers_.clear();
  particleQuads_.clear();
  publishedParticleQuads_.clear();
  flameParticleLightPositions_.clear();
  publishedFlameParticleLightPositions_.clear();
  while (!entityHeldTorchLights_.empty()) {
    destroyEntityHeldTorchLight(entityHeldTorchLights_.begin()->first);
  }
  entityHeldTorchLightInputs_.clear();
  publishedEntityHeldTorchLightInputs_.clear();
  heldItemId_ = -1;
  publishedHeldItemId_ = -1;
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
  (void)cameraY;
  if (renderSubmissionInFlight_) {
    setError("Cannot replace a cloud mesh during frame submission");
    return false;
  }
  const auto material = fancy ? fancyCloudMaterialHandle_ : cloudMaterialHandle_;
  if (material == nullptr || (fancy && !cloudMask_.valid())) {
    destroyCloudMesh();
    return true;
  }

  const double phaseX = std::floor((double(cameraX) + cloudScroll) / kFancyCloudScale);
  const double phaseZ = std::floor(double(cameraZ) / kFancyCloudScale + 0.33);
  if (!std::isfinite(phaseX) || !std::isfinite(phaseZ) || !std::isfinite(cloudHeight)) {
    setError("Invalid cloud position");
    return false;
  }
  const int wrappedX = (static_cast<int>(std::fmod(phaseX, 256.0)) + 256) % 256;
  const int wrappedZ = (static_cast<int>(std::fmod(phaseZ, 256.0)) + 256) % 256;
  const std::array<float, 3> color {colorR, colorG, colorB};
  cloudTransformX_ = fancy ? static_cast<float>(phaseX * kFancyCloudScale - cloudScroll
      - (renderOrigin.enabled ? renderOrigin.x : 0)) : 0.0f;
  cloudTransformY_ = fancy ? cloudHeight - (renderOrigin.enabled ? renderOrigin.y : 0) : 0.0f;
  cloudTransformZ_ = fancy ? static_cast<float>((phaseZ - 0.33) * kFancyCloudScale
      - (renderOrigin.enabled ? renderOrigin.z : 0)) : 0.0f;
  if (fancy && cloudMeshPrepared_ && cloudMeshFancy_ && cloudMeshPhaseX_ == wrappedX
      && cloudMeshPhaseZ_ == wrappedZ && cloudMeshColor_ == color) {
    return true;
  }
  const auto cacheMesh = [&]() {
    cloudMeshPrepared_ = true;
    cloudMeshFancy_ = fancy;
    cloudMeshPhaseX_ = wrappedX;
    cloudMeshPhaseZ_ = wrappedZ;
    cloudMeshColor_ = color;
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(fancy ? 4096 : 2048);
  indices.reserve(fancy ? 6144 : 3072);

  if (fancy) {
    appendFancyCloudGeometry(cloudMask_, wrappedX, wrappedZ, color, vertices, indices);
  } else {
    appendFastCloudGeometry(cameraX, cameraZ, cloudHeight, cloudScroll, colorR, colorG, colorB, vertices, indices);
  }

  if (!fancy && renderOrigin.enabled) {
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
    cacheMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = material;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeCloudMeshHash(fancy);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  // Frame preparation owns replacement of this stable API handle.
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
  cacheMesh();
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
  cloudMeshPrepared_ = false;
  cloudMeshFancy_ = false;
  cloudQuadCount_ = 0;
}

}  // namespace mcrtx
