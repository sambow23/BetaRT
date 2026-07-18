// Block-outline styles, capture, animation, geometry, and mesh lifecycle.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <cstddef>
#include <cstdint>
#include <unordered_set>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::chunk;
using namespace mcrtx::geometry;

namespace {

constexpr std::uint64_t kBlockOutlineMeshHashSeed = 0x4D43525458424F00ull;

std::uint64_t makeBlockOutlineMeshHash(std::uint64_t sequence) {
  return kBlockOutlineMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}
struct BlockOutlineStyleParameters {
  float inflate;
  float alpha;
  float edgeHalfThickness;
  bool filled;
  bool glow;
  bool rgbCycle;
  float colorR;
  float colorG;
  float colorB;
};

struct BlockOutlineAnimatedColor {
  float colorR;
  float colorG;
  float colorB;
  std::size_t materialIndex;
};

constexpr ULONGLONG kBlockOutlineRgbCycleIntervalMilliseconds = 180;

BlockOutlineAnimatedColor currentBlockOutlineRgbColor() {
  constexpr std::array<std::array<float, 3>, 6> kBlockOutlineRgbPalette {{
      {1.0f, 0.2f, 0.2f},
      {1.0f, 0.7f, 0.15f},
      {0.25f, 1.0f, 0.25f},
      {0.2f, 1.0f, 1.0f},
      {0.3f, 0.45f, 1.0f},
      {1.0f, 0.2f, 1.0f},
  }};

  const std::size_t materialIndex = static_cast<std::size_t>(
      (GetTickCount64() / kBlockOutlineRgbCycleIntervalMilliseconds) % kBlockOutlineRgbPalette.size());
  const std::array<float, 3>& color = kBlockOutlineRgbPalette[materialIndex];
  return {color[0], color[1], color[2], materialIndex};
}

BlockOutlineStyleParameters blockOutlineStyleParametersFor(int style) {
  switch (style) {
    case 0:
      return {0.004f, 0.30f, 0.010f, false, false, false, 0.0f, 0.0f, 0.0f};
    case 5:
      return {0.004f, 0.30f, 0.0025f, false, false, false, 0.0f, 0.0f, 0.0f};
    case 3:
      return {0.004f, 0.30f, 0.010f, false, true, false, 1.0f, 1.0f, 1.0f};
    case 4:
      return {0.004f, 0.30f, 0.010f, false, true, true, 1.0f, 0.2f, 0.2f};
    case 2:
      return {0.010f, 0.22f, 0.0f, true, false, false, 0.0f, 0.0f, 0.0f};
    case 1:
    default:
      return {0.006f, 0.55f, 0.018f, false, false, false, 0.0f, 0.0f, 0.0f};
  }
}

void appendBlockOutlineBoxGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::uint32_t outlineColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::array<std::int16_t, 6> outlineTerrainTiles {};
  appendBoxGeometry(
      minX,
      minY,
      minZ,
      maxX,
      maxY,
      maxZ,
      outlineTerrainTiles,
      outlineColor,
      vertices,
      indices);
}

void appendBlockOutlineFillGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::uint32_t outlineColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  appendBlockOutlineBoxGeometry(minX, minY, minZ, maxX, maxY, maxZ, outlineColor, vertices, indices);
}

void appendBlockOutlineWireGeometry(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    float edgeHalfThickness,
    std::uint32_t outlineColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float minYEdge0 = minY - edgeHalfThickness;
  const float minYEdge1 = minY + edgeHalfThickness;
  const float maxYEdge0 = maxY - edgeHalfThickness;
  const float maxYEdge1 = maxY + edgeHalfThickness;
  const float minZEdge0 = minZ - edgeHalfThickness;
  const float minZEdge1 = minZ + edgeHalfThickness;
  const float maxZEdge0 = maxZ - edgeHalfThickness;
  const float maxZEdge1 = maxZ + edgeHalfThickness;
  const float minXEdge0 = minX - edgeHalfThickness;
  const float minXEdge1 = minX + edgeHalfThickness;
  const float maxXEdge0 = maxX - edgeHalfThickness;
  const float maxXEdge1 = maxX + edgeHalfThickness;

  appendBlockOutlineBoxGeometry(minX, minYEdge0, minZEdge0, maxX, minYEdge1, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minX, minYEdge0, maxZEdge0, maxX, minYEdge1, maxZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minX, maxYEdge0, minZEdge0, maxX, maxYEdge1, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minX, maxYEdge0, maxZEdge0, maxX, maxYEdge1, maxZEdge1, outlineColor, vertices, indices);

  appendBlockOutlineBoxGeometry(minXEdge0, minY, minZEdge0, minXEdge1, maxY, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minXEdge0, minY, maxZEdge0, minXEdge1, maxY, maxZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, minY, minZEdge0, maxXEdge1, maxY, minZEdge1, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, minY, maxZEdge0, maxXEdge1, maxY, maxZEdge1, outlineColor, vertices, indices);

  appendBlockOutlineBoxGeometry(minXEdge0, minYEdge0, minZ, minXEdge1, minYEdge1, maxZ, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(minXEdge0, maxYEdge0, minZ, minXEdge1, maxYEdge1, maxZ, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, minYEdge0, minZ, maxXEdge1, minYEdge1, maxZ, outlineColor, vertices, indices);
  appendBlockOutlineBoxGeometry(maxXEdge0, maxYEdge0, minZ, maxXEdge1, maxYEdge1, maxZ, outlineColor, vertices, indices);
}


}  // namespace

void RemixRenderer::beginBlockOutlineFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginBlockOutlineFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  blockOutlineInstances_.clear();
}

void RemixRenderer::captureBlockOutline(int blockX, int blockY, int blockZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureBlockOutline");
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !blockOutlineEnabled_) {
    return;
  }

  BlockOutlineInstance outline;
  outline.blockX = blockX;
  outline.blockY = blockY;
  outline.blockZ = blockZ;
  blockOutlineInstances_.push_back(outline);
}

void RemixRenderer::destroyBlockOutlineMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyBlockOutlineMesh");
  destroyMeshHandle(blockOutlineMeshHandle_);
  blockOutlineCount_ = 0;
}

bool RemixRenderer::rebuildBlockOutlineMesh(const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildBlockOutlineMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildBlockOutlineMesh");
  if (!blockOutlineEnabled_ || blockOutlineInstances_.empty()) {
    destroyBlockOutlineMesh();
    return true;
  }
  MCRTX_TRACY_VALUE(blockOutlineInstances_.size());

  const BlockOutlineStyleParameters styleParameters = blockOutlineStyleParametersFor(blockOutlineStyle_);
  const BlockOutlineAnimatedColor animatedRgbColor = styleParameters.rgbCycle
      ? currentBlockOutlineRgbColor()
      : BlockOutlineAnimatedColor {
            styleParameters.colorR,
            styleParameters.colorG,
            styleParameters.colorB,
            0,
        };
  remixapi_MaterialHandle outlineMaterial = destroyOverlayMaterialHandle_;
  if (styleParameters.rgbCycle) {
    outlineMaterial = blockOutlineRgbMaterialHandles_[animatedRgbColor.materialIndex];
    if (outlineMaterial == nullptr) {
      outlineMaterial = blockOutlineGlowMaterialHandle_;
    }
  } else if (styleParameters.glow) {
    outlineMaterial = blockOutlineGlowMaterialHandle_;
  }
  if (outlineMaterial == nullptr) {
    outlineMaterial = destroyOverlayMaterialHandle_;
  }

  if (outlineMaterial == nullptr) {
    destroyBlockOutlineMesh();
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
  vertices.reserve(blockOutlineInstances_.size() * 288);
  indices.reserve(blockOutlineInstances_.size() * 432);

  std::unordered_set<WorldBlockPosition, WorldBlockPositionHash> seenOutlines;
  seenOutlines.reserve(blockOutlineInstances_.size());
  const std::uint32_t outlineColor = packVertexColorRgba(
      animatedRgbColor.colorR,
      animatedRgbColor.colorG,
      animatedRgbColor.colorB,
      styleParameters.alpha);

  std::size_t outlineCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildBlockOutlineMesh.buildGeometry");
  for (const BlockOutlineInstance& outline : blockOutlineInstances_) {
    const WorldBlockPosition position {outline.blockX, outline.blockY, outline.blockZ};
    if (!seenOutlines.insert(position).second) {
      continue;
    }

    WorldRenderPosition minPosition = rebaseWorldPosition(
        static_cast<float>(outline.blockX),
        static_cast<float>(outline.blockY),
        static_cast<float>(outline.blockZ),
        renderOrigin);
    WorldRenderPosition maxPosition = rebaseWorldPosition(
        static_cast<float>(outline.blockX) + 1.0f,
        static_cast<float>(outline.blockY) + 1.0f,
        static_cast<float>(outline.blockZ) + 1.0f,
        renderOrigin);
    float minX = minPosition.x - styleParameters.inflate;
    float minY = minPosition.y - styleParameters.inflate;
    float minZ = minPosition.z - styleParameters.inflate;
    float maxX = maxPosition.x + styleParameters.inflate;
    float maxY = maxPosition.y + styleParameters.inflate;
    float maxZ = maxPosition.z + styleParameters.inflate;

    if (const ChunkBlockCell* worldCell = findWorldCell(outline.blockX, outline.blockY, outline.blockZ); worldCell != nullptr) {
      minPosition = rebaseWorldPosition(
          static_cast<float>(outline.blockX) + worldCell->bounds[0],
          static_cast<float>(outline.blockY) + worldCell->bounds[1],
          static_cast<float>(outline.blockZ) + worldCell->bounds[2],
          renderOrigin);
      maxPosition = rebaseWorldPosition(
          static_cast<float>(outline.blockX) + worldCell->bounds[3],
          static_cast<float>(outline.blockY) + worldCell->bounds[4],
          static_cast<float>(outline.blockZ) + worldCell->bounds[5],
          renderOrigin);
      minX = minPosition.x - styleParameters.inflate;
      minY = minPosition.y - styleParameters.inflate;
      minZ = minPosition.z - styleParameters.inflate;
      maxX = maxPosition.x + styleParameters.inflate;
      maxY = maxPosition.y + styleParameters.inflate;
      maxZ = maxPosition.z + styleParameters.inflate;
    }

    if (styleParameters.filled) {
      appendBlockOutlineFillGeometry(minX, minY, minZ, maxX, maxY, maxZ, outlineColor, vertices, indices);
    } else {
      appendBlockOutlineWireGeometry(
          minX,
          minY,
          minZ,
          maxX,
          maxY,
          maxZ,
          styleParameters.edgeHalfThickness,
          outlineColor,
          vertices,
          indices);
    }
    ++outlineCount;
  }
  }

  if (indices.empty()) {
    destroyBlockOutlineMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = outlineMaterial;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeBlockOutlineMeshHash(nextBlockOutlineMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildBlockOutlineMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.blockOutline");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    destroyBlockOutlineMesh();
    return false;
  }

  destroyBlockOutlineMesh();
  blockOutlineMeshHandle_ = newMeshHandle;
  blockOutlineCount_ = outlineCount;
  return true;
}


}  // namespace mcrtx
