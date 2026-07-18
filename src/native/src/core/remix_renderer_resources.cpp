// Renderer hash support, resource destruction, and priming mesh lifecycle.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <cstddef>
#include <cstdint>

namespace mcrtx {

using namespace mcrtx::detail;

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

void RemixRenderer::destroyMeshHandle(remixapi_MeshHandle& meshHandle) {
  if (meshHandle == nullptr) {
    return;
  }

  if (renderSubmissionInFlight_) {
    deferredMeshDestroys_.push_back(meshHandle);
  } else if (remix_.DestroyMesh != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMesh");
    remix_.DestroyMesh(meshHandle);
  }

  meshHandle = nullptr;
}

void RemixRenderer::destroyLightHandle(remixapi_LightHandle lightHandle) {
  if (lightHandle == nullptr) {
    return;
  }

  if (renderSubmissionInFlight_) {
    deferredLightDestroys_.push_back(lightHandle);
  } else if (remix_.DestroyLight != nullptr) {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyLight");
    remix_.DestroyLight(lightHandle);
  }
}

void RemixRenderer::flushDeferredDestroyQueuesLocked() {
  if (renderSubmissionInFlight_) {
    return;
  }

  if (remix_.DestroyMesh != nullptr) {
    for (remixapi_MeshHandle meshHandle : deferredMeshDestroys_) {
      if (meshHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyMesh.deferred");
        remix_.DestroyMesh(meshHandle);
      }
    }
  }
  deferredMeshDestroys_.clear();

  if (remix_.DestroyLight != nullptr) {
    for (remixapi_LightHandle lightHandle : deferredLightDestroys_) {
      if (lightHandle != nullptr) {
        MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "DestroyLight.deferred");
        remix_.DestroyLight(lightHandle);
      }
    }
  }
  deferredLightDestroys_.clear();
}

bool RemixRenderer::createPrimingMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::createPrimingMesh");

  destroyMeshHandle(primingMeshHandle_);

  const remixapi_MaterialHandle materialHandle = terrainMaterialHandles_[kOpaqueTerrainMaterialClass];
  if (materialHandle == nullptr) {
    log("Skipping priming mesh creation because the opaque terrain material is unavailable");
    return false;
  }

  constexpr std::uint64_t kPrimingMeshHash = 0x5052494D494E4700ull;
  const auto makePrimingVertex = [](float x, float y, float z, float u, float v) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = x;
    vertex.position[1] = y;
    vertex.position[2] = z;
    vertex.normal[0] = 0.0f;
    vertex.normal[1] = 1.0f;
    vertex.normal[2] = 0.0f;
    vertex.texcoord[0] = u;
    vertex.texcoord[1] = v;
    vertex.color = 0xffffffffu;
    return vertex;
  };
  const std::array<remixapi_HardcodedVertex, 3> kPrimingVertices {
      makePrimingVertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
      makePrimingVertex(0.01f, 0.0f, 0.0f, 1.0f, 0.0f),
      makePrimingVertex(0.0f, 0.0f, 0.01f, 0.0f, 1.0f),
  };
  constexpr std::array<std::uint32_t, 3> kPrimingIndices {0u, 1u, 2u};

  remixapi_MeshInfoSurfaceTriangles surfaceInfo {};
  surfaceInfo.vertices_values = kPrimingVertices.data();
  surfaceInfo.vertices_count = kPrimingVertices.size();
  surfaceInfo.indices_values = kPrimingIndices.data();
  surfaceInfo.indices_count = kPrimingIndices.size();
  surfaceInfo.skinning_hasvalue = FALSE;
  surfaceInfo.material = materialHandle;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = kPrimingMeshHash;
  meshInfo.surfaces_values = &surfaceInfo;
  meshInfo.surfaces_count = 1;

  const remixapi_ErrorCode result = [&]() {
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.priming");
    return remix_.CreateMesh(&meshInfo, &primingMeshHandle_);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    primingMeshHandle_ = nullptr;
    log("Failed to create priming mesh: " + errorCodeToString(result));
    return false;
  }

  log("Created priming mesh for empty-scene overlay presentation");
  return true;
}


}  // namespace mcrtx
