// Animated world-fire geometry and mesh lifecycle.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_block_geometry_effects.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <cstddef>
#include <cstdint>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::block_geometry;
using namespace mcrtx::chunk;

namespace {

constexpr std::uint64_t kFireMeshHashSeed = 0x4D43525458465200ull;

std::uint64_t makeFireMeshHash(std::uint64_t sequence) {
  return kFireMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

}  // namespace

void RemixRenderer::destroyFireMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyFireMesh");
  destroyMeshHandle(fireMeshHandle_);
  fireQuadCount_ = 0;
  lastFireRenderOrigin_ = {};
}

bool RemixRenderer::rebuildFireMesh(const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildFireMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildFireMesh");
  if (fireMaterialHandle_ == nullptr) {
    destroyFireMesh();
    return true;
  }

  const std::uint32_t frameIndex = static_cast<std::uint32_t>((GetTickCount64() / kFireAnimationFrameIntervalMilliseconds) % kFireAnimationFrameCount);
  if (fireMeshHandle_ != nullptr
      && lastFireAnimationFrame_ == frameIndex
      && lastFireChunkBuildCount_ == capturedChunkBuilds_
      && lastFireRenderOrigin_.enabled == renderOrigin.enabled
      && lastFireRenderOrigin_.x == renderOrigin.x
      && lastFireRenderOrigin_.y == renderOrigin.y
      && lastFireRenderOrigin_.z == renderOrigin.z) {
    return true;
  }

  const auto chunkOriginForWorld = [](int coordinate) {
    return coordinate >= 0
        ? (coordinate / kChunkDimension) * kChunkDimension
        : (((coordinate + 1) / kChunkDimension) - 1) * kChunkDimension;
  };

  const auto findWorldCell = [this, &chunkOriginForWorld](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    const int originX = chunkOriginForWorld(worldX);
    const int originY = chunkOriginForWorld(worldY);
    const int originZ = chunkOriginForWorld(worldZ);
    const int localX = worldX - originX;
    const int localY = worldY - originY;
    const int localZ = worldZ - originZ;
    const int cellIndex = blockIndex(localX, localY, localZ);

    for (int renderPass = 0; renderPass <= 1; ++renderPass) {
      const ChunkKey chunkKey {originX, originY, originZ, renderPass};
      const auto it = chunkMeshes_.find(chunkKey);
      if (it == chunkMeshes_.end() || !it->second.hasOccupancy || it->second.occupancy[cellIndex] == 0) {
        continue;
      }
      return &it->second.cells[cellIndex];
    }

    return nullptr;
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(256);
  indices.reserve(384);

  std::size_t fireCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildFireMesh.buildGeometry");
    for (const auto& [chunkKey, meshData] : chunkMeshes_) {
      if (chunkKey.renderPass != 0 || !meshData.hasOccupancy || meshData.fireCellIndices.empty()) {
        continue;
      }

      for (std::uint16_t fireCellIndex : meshData.fireCellIndices) {
        const ChunkBlockCell& cell = meshData.cells[fireCellIndex];
        if (!isFireRenderType(cell.renderType)) {
          continue;
        }

        const int localY = fireCellIndex / (kChunkDimension * kChunkDimension);
        const int localPlaneIndex = fireCellIndex % (kChunkDimension * kChunkDimension);
        const int localZ = localPlaneIndex / kChunkDimension;
        const int localX = localPlaneIndex % kChunkDimension;
        const int worldX = chunkKey.originX + localX;
        const int worldY = chunkKey.originY + localY;
        const int worldZ = chunkKey.originZ + localZ;
        const WorldRenderPosition firePosition = rebaseWorldPosition(
            static_cast<float>(worldX),
            static_cast<float>(worldY),
            static_cast<float>(worldZ),
            renderOrigin);
        appendFireGeometry(
            worldX,
            worldY,
            worldZ,
            findWorldCell(worldX, worldY - 1, worldZ) != nullptr,
            findWorldCell(worldX - 1, worldY, worldZ) != nullptr,
            findWorldCell(worldX + 1, worldY, worldZ) != nullptr,
            findWorldCell(worldX, worldY, worldZ - 1) != nullptr,
            findWorldCell(worldX, worldY, worldZ + 1) != nullptr,
            findWorldCell(worldX, worldY + 1, worldZ) != nullptr,
            firePosition.x,
            firePosition.y,
            firePosition.z,
            frameIndex,
            vertices,
            indices);
        ++fireCount;
      }
    }
  }
  MCRTX_TRACY_VALUE(fireCount);

  if (indices.empty()) {
    destroyFireMesh();
    lastFireAnimationFrame_ = frameIndex;
    lastFireChunkBuildCount_ = capturedChunkBuilds_;
    lastFireRenderOrigin_ = renderOrigin;
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = fireMaterialHandle_;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeFireMeshHash(nextFireMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildFireMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.fire");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyFireMesh();
  fireMeshHandle_ = newMeshHandle;
  fireQuadCount_ = fireCount;
  lastFireAnimationFrame_ = frameIndex;
  lastFireChunkBuildCount_ = capturedChunkBuilds_;
  lastFireRenderOrigin_ = renderOrigin;
  return true;
}


}  // namespace mcrtx
