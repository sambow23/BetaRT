// Destroy-overlay capture, block-aware geometry, and mesh lifecycle.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/chunks/remix_block_geometry_fixtures.hpp"
#include "mcrtx/chunks/remix_block_geometry_natural.hpp"
#include "mcrtx/chunks/remix_block_geometry_redstone.hpp"
#include "mcrtx/chunks/remix_block_geometry_structures.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <iostream>
#include <string>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::block_geometry;
using namespace mcrtx::chunk;
using namespace mcrtx::geometry;

namespace {

constexpr std::uint64_t kDestroyOverlayMeshHashSeed = 0x4D43525458444F00ull;

std::uint64_t makeDestroyOverlayMeshHash(std::uint64_t sequence) {
  return kDestroyOverlayMeshHashSeed | (sequence & 0x0000FFFFFFFFFFFFull);
}

}  // namespace

void RemixRenderer::beginDestroyOverlayFrame() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::beginDestroyOverlayFrame");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  destroyOverlayInstances_.clear();
}

void RemixRenderer::captureDestroyOverlay(
    int blockX,
    int blockY,
    int blockZ,
    int blockId,
    int blockMetadata,
    int renderType,
    int destroyStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::captureDestroyOverlay");
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return;
  }

  DestroyOverlayInstance overlay;
  overlay.blockX = blockX;
  overlay.blockY = blockY;
  overlay.blockZ = blockZ;
  overlay.blockId = blockId;
  overlay.blockMetadata = blockMetadata;
  overlay.renderType = renderType;
  overlay.destroyStage = destroyStage;
  destroyOverlayInstances_.push_back(overlay);
}

void RemixRenderer::destroyDestroyOverlayMesh() {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::destroyDestroyOverlayMesh");
  destroyMeshHandle(destroyOverlayMeshHandle_);
  destroyOverlayCount_ = 0;
}

bool RemixRenderer::rebuildDestroyOverlayMesh(const WorldRenderOrigin& renderOrigin) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Native, "RemixRenderer::rebuildDestroyOverlayMesh");
  MCRTX_TRACY_SCOPE("RemixRenderer::rebuildDestroyOverlayMesh");
  if (destroyOverlayInstances_.empty()) {
    destroyDestroyOverlayMesh();
    return true;
  }
  MCRTX_TRACY_VALUE(destroyOverlayInstances_.size());

  remixapi_MaterialHandle overlayMaterial = destroyOverlayMaterialHandle_;
  if (overlayMaterial == nullptr) {
    overlayMaterial = terrainMaterialHandles_[kCutoutTerrainMaterialClass];
  }
  if (overlayMaterial == nullptr) {
    log("Destroy overlay skipped because the cutout terrain material handle is null");
    destroyDestroyOverlayMesh();
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

  const auto hasFenceNeighbor = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr
        && neighborCell->blockId == kFenceBlockId
        && neighborCell->renderType == kFenceBlockRenderType;
  };

  const auto hasSolidSupport = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isSolidSupportBlock(*neighborCell);
  };

  const auto hasRedstoneConnection = [&findWorldCell](int worldX, int worldY, int worldZ, int direction) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isRedstoneConnectionCell(*neighborCell, direction);
  };

  std::vector<remixapi_HardcodedVertex> vertices;
  std::vector<std::uint32_t> indices;
  vertices.reserve(destroyOverlayInstances_.size() * 24);
  indices.reserve(destroyOverlayInstances_.size() * 36);

  std::size_t overlayCount = 0;
  {
    MCRTX_TRACY_SCOPE("rebuildDestroyOverlayMesh.buildGeometry");
  for (const DestroyOverlayInstance& overlay : destroyOverlayInstances_) {
    const int destroyTile = 240 + std::clamp(overlay.destroyStage, 0, 9);
    ChunkBlockCell resolvedCell {};
    if (const ChunkBlockCell* worldCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ); worldCell != nullptr) {
      resolvedCell = *worldCell;
    } else {
      resolvedCell.blockId = static_cast<std::uint8_t>(overlay.blockId);
      resolvedCell.blockMetadata = static_cast<std::uint8_t>(overlay.blockMetadata);
      resolvedCell.renderType = static_cast<std::uint8_t>(overlay.renderType);
    }
    resolvedCell.terrainTiles.fill(static_cast<std::int16_t>(destroyTile));

    const WorldRenderPosition localPosition = rebaseWorldPosition(
        static_cast<float>(overlay.blockX),
        static_cast<float>(overlay.blockY),
        static_cast<float>(overlay.blockZ),
        renderOrigin);
    const float localX = localPosition.x;
    const float localY = localPosition.y;
    const float localZ = localPosition.z;

    if (resolvedCell.renderType == kLiquidBlockRenderType) {
      continue;
    }

    if (isCrossedQuadRenderType(resolvedCell.renderType)) {
      appendCrossedQuadGeometry(
          resolvedCell,
          overlay.blockX,
          overlay.blockY,
          overlay.blockZ,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isTorchRenderType(resolvedCell.renderType)) {
      appendTorchGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isLadderRenderType(resolvedCell.renderType)) {
      appendLadderGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

        if (isRedstoneDustRenderType(resolvedCell.renderType) && isRedstoneDustBlockId(resolvedCell.blockId)) {
          const bool blockedAbove = hasSolidSupport(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
          bool connectWest = hasRedstoneConnection(overlay.blockX - 1, overlay.blockY, overlay.blockZ, 1)
            || (!hasSolidSupport(overlay.blockX - 1, overlay.blockY, overlay.blockZ)
              && hasRedstoneConnection(overlay.blockX - 1, overlay.blockY - 1, overlay.blockZ, -1));
          bool connectEast = hasRedstoneConnection(overlay.blockX + 1, overlay.blockY, overlay.blockZ, 3)
            || (!hasSolidSupport(overlay.blockX + 1, overlay.blockY, overlay.blockZ)
              && hasRedstoneConnection(overlay.blockX + 1, overlay.blockY - 1, overlay.blockZ, -1));
          bool connectNorth = hasRedstoneConnection(overlay.blockX, overlay.blockY, overlay.blockZ - 1, 2)
            || (!hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ - 1)
              && hasRedstoneConnection(overlay.blockX, overlay.blockY - 1, overlay.blockZ - 1, -1));
          bool connectSouth = hasRedstoneConnection(overlay.blockX, overlay.blockY, overlay.blockZ + 1, 0)
            || (!hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ + 1)
              && hasRedstoneConnection(overlay.blockX, overlay.blockY - 1, overlay.blockZ + 1, -1));

          const bool climbWest = !blockedAbove && hasSolidSupport(overlay.blockX - 1, overlay.blockY, overlay.blockZ)
            && hasRedstoneConnection(overlay.blockX - 1, overlay.blockY + 1, overlay.blockZ, -1);
          const bool climbEast = !blockedAbove && hasSolidSupport(overlay.blockX + 1, overlay.blockY, overlay.blockZ)
            && hasRedstoneConnection(overlay.blockX + 1, overlay.blockY + 1, overlay.blockZ, -1);
          const bool climbNorth = !blockedAbove && hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ - 1)
            && hasRedstoneConnection(overlay.blockX, overlay.blockY + 1, overlay.blockZ - 1, -1);
          const bool climbSouth = !blockedAbove && hasSolidSupport(overlay.blockX, overlay.blockY, overlay.blockZ + 1)
            && hasRedstoneConnection(overlay.blockX, overlay.blockY + 1, overlay.blockZ + 1, -1);

          connectWest = connectWest || climbWest;
          connectEast = connectEast || climbEast;
          connectNorth = connectNorth || climbNorth;
          connectSouth = connectSouth || climbSouth;

          appendRedstoneDustGeometry(
            resolvedCell,
            connectWest,
            connectEast,
            connectNorth,
            connectSouth,
            climbWest,
            climbEast,
            climbNorth,
            climbSouth,
            localX,
            localY,
            localZ,
            vertices,
            indices);
          ++overlayCount;
          continue;
        }

    if (isRailRenderType(resolvedCell.renderType) && isRailBlockId(resolvedCell.blockId)) {
      appendRailGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isStairRenderType(resolvedCell.renderType) && isStairBlockId(resolvedCell.blockId)) {
      appendStairGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isLeverOrButtonRenderType(resolvedCell.renderType)) {
      if (isLeverBlockId(resolvedCell.blockId)) {
        appendLeverGeometry(
            resolvedCell,
            localX,
            localY,
            localZ,
            vertices,
            indices);
      } else if (isButtonBlockId(resolvedCell.blockId)) {
        appendBoxGeometry(
            localX + resolvedCell.bounds[0],
            localY + resolvedCell.bounds[1],
            localZ + resolvedCell.bounds[2],
            localX + resolvedCell.bounds[3],
            localY + resolvedCell.bounds[4],
            localZ + resolvedCell.bounds[5],
            resolvedCell.terrainTiles,
            kDefaultVertexColor,
            vertices,
            indices);
      }
      ++overlayCount;
      continue;
    }

    if (isDoorRenderType(resolvedCell.renderType) && isDoorBlockId(resolvedCell.blockId)) {
      const ChunkBlockCell* pairedDoorCell = (resolvedCell.blockMetadata & 8) != 0
          ? findWorldCell(overlay.blockX, overlay.blockY - 1, overlay.blockZ)
          : findWorldCell(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
      int resolvedDoorMetadata = resolvedCell.blockMetadata & 0xF;
      if ((resolvedDoorMetadata & 8) != 0) {
        if (pairedDoorCell != nullptr && pairedDoorCell->blockId == resolvedCell.blockId) {
          resolvedDoorMetadata = pairedDoorCell->blockMetadata & 0xF;
        } else {
          resolvedDoorMetadata &= 7;
        }
      } else if (pairedDoorCell != nullptr
          && pairedDoorCell->blockId == resolvedCell.blockId
          && (pairedDoorCell->blockMetadata & 4) != 0) {
        resolvedDoorMetadata = (resolvedDoorMetadata & 3) | 4;
      }

      appendDoorGeometry(
          resolvedCell,
          resolvedDoorMetadata,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isFenceRenderType(resolvedCell.renderType) && resolvedCell.blockId == kFenceBlockId) {
      appendFenceGeometry(
          hasFenceNeighbor(overlay.blockX - 1, overlay.blockY, overlay.blockZ),
          hasFenceNeighbor(overlay.blockX + 1, overlay.blockY, overlay.blockZ),
          hasFenceNeighbor(overlay.blockX, overlay.blockY, overlay.blockZ - 1),
          hasFenceNeighbor(overlay.blockX, overlay.blockY, overlay.blockZ + 1),
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isCactusRenderType(resolvedCell.renderType) && isCactusBlockId(resolvedCell.blockId)) {
      const ChunkBlockCell* belowCell = findWorldCell(overlay.blockX, overlay.blockY - 1, overlay.blockZ);
      const ChunkBlockCell* aboveCell = findWorldCell(overlay.blockX, overlay.blockY + 1, overlay.blockZ);
      const ChunkBlockCell* northCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ - 1);
      const ChunkBlockCell* southCell = findWorldCell(overlay.blockX, overlay.blockY, overlay.blockZ + 1);
      const ChunkBlockCell* westCell = findWorldCell(overlay.blockX - 1, overlay.blockY, overlay.blockZ);
      const ChunkBlockCell* eastCell = findWorldCell(overlay.blockX + 1, overlay.blockY, overlay.blockZ);

      appendCactusGeometry(
          belowCell == nullptr || !isSolidSupportBlock(*belowCell),
          aboveCell == nullptr || !isSolidSupportBlock(*aboveCell),
          northCell == nullptr || !isSolidSupportBlock(*northCell),
          southCell == nullptr || !isSolidSupportBlock(*southCell),
          westCell == nullptr || !isSolidSupportBlock(*westCell),
          eastCell == nullptr || !isSolidSupportBlock(*eastCell),
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (isBedRenderType(resolvedCell.renderType) && isBedBlockId(resolvedCell.blockId)) {
      appendBedGeometry(
          resolvedCell,
          localX,
          localY,
          localZ,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    if (resolvedCell.renderType == kCubeBlockRenderType && usesPartialCubeBounds(resolvedCell)) {
      appendBoxGeometry(
          localX + resolvedCell.bounds[0],
          localY + resolvedCell.bounds[1],
          localZ + resolvedCell.bounds[2],
          localX + resolvedCell.bounds[3],
          localY + resolvedCell.bounds[4],
          localZ + resolvedCell.bounds[5],
          resolvedCell.terrainTiles,
          kDefaultVertexColor,
          vertices,
          indices);
      ++overlayCount;
      continue;
    }

    bool emittedFace = false;
    for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
      const ChunkBlockCell* neighborCell = findWorldCell(
          overlay.blockX + kNeighborOffsets[faceIndex][0],
          overlay.blockY + kNeighborOffsets[faceIndex][1],
          overlay.blockZ + kNeighborOffsets[faceIndex][2]);
      if (neighborCell != nullptr && shouldCullFaceAgainstNeighbor(resolvedCell, *neighborCell)) {
        continue;
      }

      const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
      appendFaceGeometry(
          faceIndex,
          localX,
          localY,
          localZ,
          resolvedCell.terrainTiles[minecraftSide],
          kDefaultVertexColor,
          kFaceOverlayBias,
          vertices,
          indices);
      emittedFace = true;
    }

    if (emittedFace) {
      ++overlayCount;
    }
  }
  }

  if (indices.empty()) {
    destroyDestroyOverlayMesh();
    return true;
  }

  remixapi_MeshInfoSurfaceTriangles surface {};
  surface.vertices_values = vertices.data();
  surface.vertices_count = vertices.size();
  surface.indices_values = indices.data();
  surface.indices_count = indices.size();
  surface.skinning_hasvalue = FALSE;
  surface.material = overlayMaterial;

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeDestroyOverlayMeshHash(nextDestroyOverlayMeshHash_++);
  meshInfo.surfaces_values = &surface;
  meshInfo.surfaces_count = 1;

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = [&]() {
    MCRTX_TRACY_SCOPE("rebuildDestroyOverlayMesh.createMesh");
    MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Remix, "CreateMesh.destroy");
    return remix_.CreateMesh(&meshInfo, &newMeshHandle);
  }();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  destroyDestroyOverlayMesh();
  destroyOverlayMeshHandle_ = newMeshHandle;
  destroyOverlayCount_ = overlayCount;
  if (isVerboseLoggingEnabled()) {
    log(
        std::string("Rebuilt destroy overlay mesh overlays=") + std::to_string(overlayCount)
        + " vertices=" + std::to_string(vertices.size())
        + " indices=" + std::to_string(indices.size()));
  }
  return true;
}


}  // namespace mcrtx
