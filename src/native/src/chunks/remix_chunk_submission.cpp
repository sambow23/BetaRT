// Chunk surface finalization, Remix mesh creation, and commit.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_build.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <cstdint>
#include <vector>

namespace mcrtx {

using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
bool RemixRenderer::rebuildChunkMeshFromData(
    const ChunkKey& chunkKey,
    ChunkMeshData& meshData,
    bool forceRebuild) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildChunkMeshFromData");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildChunkMeshFromData");
  (void)forceRebuild;
  MCRTX_TRACY_VALUE(meshData.blockCount);

  if (!meshData.hasOccupancy || meshData.blockCount == 0) {
    destroyChunkMesh(meshData);
    return true;
  }

  ChunkGeometryBuild build;
  emitChunkGeometry(chunkKey, meshData, build);
  auto& surfacesToBuild = build.surfacesToBuild;
  auto& desiredTorchLights = build.desiredTorchLights;
  MCRTX_TRACY_VALUE(desiredTorchLights.size());

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
  {
    MCRTX_TRACY_SCOPE("rebuildChunkMeshFromData.finalizeSurfaces");
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
  }
  MCRTX_TRACY_VALUE(surfaces.size());

  if (surfaces.empty()) {
    destroyChunkMesh(meshData);
    meshData.meshFingerprint = 0;
    meshData.faceCovered = {};
    return true;
  }

  const std::uint64_t meshFingerprint = computeChunkMeshFingerprint(surfacesToBuild);
  if (meshData.meshHandle != nullptr && meshData.meshFingerprint == meshFingerprint) {
    MCRTX_TRACY_SCOPE("rebuildChunkMeshFromData.reuseExistingMesh");
    if (!reconcileChunkTorchLights(meshData, desiredTorchLights)) {
      return false;
    }
    // Face coverage is already up to date from the previous build.
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeChunkMeshHash(chunkKey, meshFingerprint);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildChunkMeshFromData.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.chunk");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  {
    MCRTX_TRACY_SCOPE("rebuildChunkMeshFromData.reconcileTorchLights");
    if (!reconcileChunkTorchLights(meshData, desiredTorchLights)) {
      destroyMeshHandle(newMeshHandle);
      return false;
    }
  }

  destroyChunkMeshHandle(meshData);
  meshData.meshHandle = newMeshHandle;
  meshData.meshHash = meshInfo.hash;
  meshData.meshFingerprint = meshFingerprint;
  if (chunkKey.renderPass == 0) {
    computeFaceCoverage(meshData);
  }
  return true;
}

}  // namespace mcrtx
