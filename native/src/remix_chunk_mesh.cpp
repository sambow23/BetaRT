// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;
bool RemixRenderer::beginChunkBuild(
    int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ, int renderPass) {
  std::scoped_lock lock(mutex_);

  if (!initialized_) {
    return false;
  }

  if (renderPass != 0 && renderPass != 1) {
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
  int texture5,
  float boundsMinX,
  float boundsMinY,
  float boundsMinZ,
  float boundsMaxX,
  float boundsMaxY,
  float boundsMaxZ,
  int blockColorRgb,
  int liquidVisibilityMask,
  float liquidHeight0,
  float liquidHeight1,
  float liquidHeight2,
  float liquidHeight3,
  float liquidFlowAngle) {
  std::scoped_lock lock(mutex_);

  if (!initialized_ || !chunkBuildActive_) {
    return;
  }

  ++activeChunkBuild_.blockCount;
  ++capturedBlocks_;
  if (blockId >= 0 && blockId < static_cast<int>(activeChunkBuild_.blockIdCounts.size())) {
    ++activeChunkBuild_.blockIdCounts[static_cast<std::size_t>(blockId)];
  }

  if (!shouldCaptureBlock(blockId, renderType, activeChunkBuild_.renderPass) || activeChunkBlocks_.size() >= kMaxOpaqueBlocksPerChunk) {
    return;
  }

  CapturedBlockInstance block {};
  block.position[0] = blockX;
  block.position[1] = blockY;
  block.position[2] = blockZ;
  block.blockId = blockId;
  block.blockMetadata = blockMetadata;
  block.terrainTiles = {
      static_cast<std::int16_t>(texture0),
      static_cast<std::int16_t>(texture1),
      static_cast<std::int16_t>(texture2),
      static_cast<std::int16_t>(texture3),
      static_cast<std::int16_t>(texture4),
      static_cast<std::int16_t>(texture5),
  };
  block.renderType = renderType;
  block.materialClass = materialClassForBlock(blockId, renderType);
    block.bounds = {boundsMinX, boundsMinY, boundsMinZ, boundsMaxX, boundsMaxY, boundsMaxZ};
  block.liquidVisibilityMask = static_cast<std::uint8_t>(liquidVisibilityMask & 0x3F);
  block.liquidHeights = {liquidHeight0, liquidHeight1, liquidHeight2, liquidHeight3};
  block.liquidFlowAngle = liquidFlowAngle;
  block.blockColor = static_cast<std::uint32_t>(blockColorRgb) & 0x00FFFFFFu;
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

void RemixRenderer::destroyChunkMeshHandle(ChunkMeshData& meshData) {
  if (meshData.meshHandle != nullptr && remix_.DestroyMesh != nullptr) {
    remix_.DestroyMesh(meshData.meshHandle);
  }
  meshData.meshHandle = nullptr;
  meshData.meshHash = 0;
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
    cells[occupancyIndex].blockId = static_cast<std::uint8_t>(block.blockId & 0xFF);
    cells[occupancyIndex].blockMetadata = static_cast<std::uint8_t>(block.blockMetadata & 0xFF);
    cells[occupancyIndex].renderType = static_cast<std::uint8_t>(block.renderType & 0xFF);
    cells[occupancyIndex].bounds = block.bounds;
    cells[occupancyIndex].liquidVisibilityMask = block.liquidVisibilityMask;
    cells[occupancyIndex].liquidHeights = block.liquidHeights;
    cells[occupancyIndex].liquidFlowAngle = block.liquidFlowAngle;
    cells[occupancyIndex].blockColor = block.blockColor;
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

  std::vector<SurfaceBuildBuffers> surfacesToBuild;
  surfacesToBuild.reserve(8);
  std::unordered_map<std::uintptr_t, std::size_t> surfaceIndexByHandle;
  std::vector<TorchLightPlacement> desiredTorchLights;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](remixapi_MaterialHandle materialHandle) -> SurfaceBuildBuffers& {
    const std::uintptr_t materialKey = reinterpret_cast<std::uintptr_t>(materialHandle);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    SurfaceBuildBuffers surface;
    surface.materialHandle = materialHandle;
    surface.vertices.reserve(512);
    surface.indices.reserve(768);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  const auto hasFenceNeighbor = [this, &chunkKey, &meshData](int worldX, int worldY, int worldZ) -> bool {
    ChunkKey neighborKey = chunkKey;
    int localX = worldX - chunkKey.originX;
    int localY = worldY - chunkKey.originY;
    int localZ = worldZ - chunkKey.originZ;

    while (localX < 0) {
      neighborKey.originX -= kChunkDimension;
      localX += kChunkDimension;
    }
    while (localX >= kChunkDimension) {
      neighborKey.originX += kChunkDimension;
      localX -= kChunkDimension;
    }
    while (localY < 0) {
      neighborKey.originY -= kChunkDimension;
      localY += kChunkDimension;
    }
    while (localY >= kChunkDimension) {
      neighborKey.originY += kChunkDimension;
      localY -= kChunkDimension;
    }
    while (localZ < 0) {
      neighborKey.originZ -= kChunkDimension;
      localZ += kChunkDimension;
    }
    while (localZ >= kChunkDimension) {
      neighborKey.originZ += kChunkDimension;
      localZ -= kChunkDimension;
    }

    const ChunkMeshData* targetMesh = &meshData;
    if (!(neighborKey == chunkKey)) {
      const auto neighborIt = chunkMeshes_.find(neighborKey);
      if (neighborIt == chunkMeshes_.end() || !neighborIt->second.hasOccupancy) {
        return false;
      }
      targetMesh = &neighborIt->second;
    }

    const int neighborIndex = blockIndex(localX, localY, localZ);
    if (targetMesh->occupancy[neighborIndex] == 0) {
      return false;
    }

    const ChunkBlockCell& neighborCell = targetMesh->cells[neighborIndex];
    return neighborCell.blockId == kFenceBlockId && neighborCell.renderType == kFenceBlockRenderType;
  };

  const auto findWorldCell = [this, &chunkKey, &meshData](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
    ChunkKey neighborKey = chunkKey;
    int localX = worldX - chunkKey.originX;
    int localY = worldY - chunkKey.originY;
    int localZ = worldZ - chunkKey.originZ;

    while (localX < 0) {
      neighborKey.originX -= kChunkDimension;
      localX += kChunkDimension;
    }
    while (localX >= kChunkDimension) {
      neighborKey.originX += kChunkDimension;
      localX -= kChunkDimension;
    }
    while (localY < 0) {
      neighborKey.originY -= kChunkDimension;
      localY += kChunkDimension;
    }
    while (localY >= kChunkDimension) {
      neighborKey.originY += kChunkDimension;
      localY -= kChunkDimension;
    }
    while (localZ < 0) {
      neighborKey.originZ -= kChunkDimension;
      localZ += kChunkDimension;
    }
    while (localZ >= kChunkDimension) {
      neighborKey.originZ += kChunkDimension;
      localZ -= kChunkDimension;
    }

    const ChunkMeshData* targetMesh = &meshData;
    if (!(neighborKey == chunkKey)) {
      const auto neighborIt = chunkMeshes_.find(neighborKey);
      if (neighborIt == chunkMeshes_.end() || !neighborIt->second.hasOccupancy) {
        return nullptr;
      }
      targetMesh = &neighborIt->second;
    }

    const int neighborIndex = blockIndex(localX, localY, localZ);
    if (targetMesh->occupancy[neighborIndex] == 0) {
      return nullptr;
    }

    return &targetMesh->cells[neighborIndex];
  };

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

        if (cell.renderType == kLiquidBlockRenderType && materialClass == kWaterTerrainMaterialClass) {
          SurfaceBuildBuffers& waterSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendWaterGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              waterSurface.vertices,
              waterSurface.indices);
          continue;
        }

        if (isCrossedQuadRenderType(cell.renderType)) {
          SurfaceBuildBuffers& floraSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendCrossedQuadGeometry(
              cell,
              chunkKey.originX + localX,
              chunkKey.originY + localY,
              chunkKey.originZ + localZ,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              floraSurface.vertices,
              floraSurface.indices);
          continue;
        }

        if (isFireRenderType(cell.renderType)) {
          continue;
        }

        if (isTorchRenderType(cell.renderType)) {
          desiredTorchLights.push_back(makeTorchLightPlacement(
            cell,
            chunkKey.originX + localX,
            chunkKey.originY + localY,
            chunkKey.originZ + localZ));
          SurfaceBuildBuffers& torchSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendTorchGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              torchSurface.vertices,
              torchSurface.indices);
          continue;
        }

        if (isLadderRenderType(cell.renderType)) {
          SurfaceBuildBuffers& ladderSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendLadderGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              ladderSurface.vertices,
              ladderSurface.indices);
          continue;
        }

        if (isRailRenderType(cell.renderType) && isRailBlockId(cell.blockId)) {
          SurfaceBuildBuffers& railSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendRailGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              railSurface.vertices,
              railSurface.indices);
          continue;
        }

        if (isStairRenderType(cell.renderType) && isStairBlockId(cell.blockId)) {
          SurfaceBuildBuffers& stairSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendStairGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              stairSurface.vertices,
              stairSurface.indices);
          continue;
        }

        if (isDoorRenderType(cell.renderType) && isDoorBlockId(cell.blockId)) {
          const int worldX = chunkKey.originX + localX;
          const int worldY = chunkKey.originY + localY;
          const int worldZ = chunkKey.originZ + localZ;
          const ChunkBlockCell* pairedDoorCell = (cell.blockMetadata & 8) != 0
              ? findWorldCell(worldX, worldY - 1, worldZ)
              : findWorldCell(worldX, worldY + 1, worldZ);
          int resolvedDoorMetadata = cell.blockMetadata & 0xF;
          if ((resolvedDoorMetadata & 8) != 0) {
            if (pairedDoorCell != nullptr && pairedDoorCell->blockId == cell.blockId) {
              resolvedDoorMetadata = pairedDoorCell->blockMetadata & 0xF;
            } else {
              resolvedDoorMetadata &= 7;
            }
          } else if (pairedDoorCell != nullptr
              && pairedDoorCell->blockId == cell.blockId
              && (pairedDoorCell->blockMetadata & 4) != 0) {
            resolvedDoorMetadata = (resolvedDoorMetadata & 3) | 4;
          }

          SurfaceBuildBuffers& doorSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendDoorGeometry(
              cell,
              resolvedDoorMetadata,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              doorSurface.vertices,
              doorSurface.indices);
          continue;
        }

        if (isFenceRenderType(cell.renderType) && cell.blockId == kFenceBlockId) {
          const int worldX = chunkKey.originX + localX;
          const int worldY = chunkKey.originY + localY;
          const int worldZ = chunkKey.originZ + localZ;
          SurfaceBuildBuffers& fenceSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendFenceGeometry(
              hasFenceNeighbor(worldX - 1, worldY, worldZ),
              hasFenceNeighbor(worldX + 1, worldY, worldZ),
              hasFenceNeighbor(worldX, worldY, worldZ - 1),
              hasFenceNeighbor(worldX, worldY, worldZ + 1),
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              fenceSurface.vertices,
              fenceSurface.indices);
          continue;
        }

        if (cell.renderType == kCubeBlockRenderType && usesPartialCubeBounds(cell)) {
          SurfaceBuildBuffers& partialCubeSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
          appendBoxGeometry(
              static_cast<float>(localX) + cell.bounds[0],
              static_cast<float>(localY) + cell.bounds[1],
              static_cast<float>(localZ) + cell.bounds[2],
              static_cast<float>(localX) + cell.bounds[3],
              static_cast<float>(localY) + cell.bounds[4],
              static_cast<float>(localZ) + cell.bounds[5],
              cell.terrainTiles,
              kDefaultVertexColor,
              partialCubeSurface.vertices,
              partialCubeSurface.indices);
          continue;
        }

        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
          const int minecraftSide = kNativeFaceToMinecraftSide[faceIndex];
          const int neighborX = localX + kNeighborOffsets[faceIndex][0];
          const int neighborY = localY + kNeighborOffsets[faceIndex][1];
          const int neighborZ = localZ + kNeighborOffsets[faceIndex][2];

          bool faceOccluded = false;
          const bool neighborInsideChunk =
              neighborX >= 0 && neighborX < kChunkDimension
              && neighborY >= 0 && neighborY < kChunkDimension
              && neighborZ >= 0 && neighborZ < kChunkDimension;
          if (neighborInsideChunk) {
            const int neighborIndex = blockIndex(neighborX, neighborY, neighborZ);
            if (meshData.occupancy[neighborIndex] != 0) {
              faceOccluded = shouldCullFaceAgainstNeighbor(cell, meshData.cells[neighborIndex]);
            }
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
              const int neighborIndex = blockIndex(wrappedX, wrappedY, wrappedZ);
              if (neighborIt->second.occupancy[neighborIndex] != 0) {
                faceOccluded = shouldCullFaceAgainstNeighbor(cell, neighborIt->second.cells[neighborIndex]);
              }
            }
          }

          if (faceOccluded) {
            continue;
          }

          SurfaceBuildBuffers& faceSurface = acquireSurface(terrainMaterialHandles_[materialClass]);
            appendFaceGeometry(
              faceIndex,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              cell.terrainTiles[minecraftSide],
              packVertexColor(faceTintColorForBlock(cell.blockId, minecraftSide, cell.blockColor)),
              0.0f,
              faceSurface.vertices,
              faceSurface.indices);

          if (cell.blockId == kGrassBlockId
              && minecraftSide >= 2
              && minecraftSide <= 5
              && normalizeTerrainTileIndex(cell.terrainTiles[minecraftSide]) == 3) {
            SurfaceBuildBuffers& overlaySurface = acquireSurface(terrainMaterialHandles_[kCutoutTerrainMaterialClass]);
            appendFaceGeometry(
                faceIndex,
                static_cast<float>(localX),
                static_cast<float>(localY),
                static_cast<float>(localZ),
                kGrassOverlayTerrainTile,
                packVertexColor(cell.blockColor),
                kFaceOverlayBias,
                overlaySurface.vertices,
                overlaySurface.indices);
          }
        }
      }
    }
  }

  std::vector<remixapi_MeshInfoSurfaceTriangles> surfaces;
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

  if (surfaces.empty()) {
    destroyChunkMesh(meshData);
    return true;
  }

  remixapi_MeshInfo meshInfo {};
  meshInfo.sType = REMIXAPI_STRUCT_TYPE_MESH_INFO;
  meshInfo.hash = makeChunkMeshHash(chunkKey, nextChunkMeshHash_++);
  meshInfo.surfaces_values = surfaces.data();
  meshInfo.surfaces_count = static_cast<std::uint32_t>(surfaces.size());

  remixapi_MeshHandle newMeshHandle = nullptr;
  const remixapi_ErrorCode result = remix_.CreateMesh(&meshInfo, &newMeshHandle);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("CreateMesh failed: " + errorCodeToString(result));
    return false;
  }

  if (!reconcileChunkTorchLights(meshData, desiredTorchLights)) {
    if (newMeshHandle != nullptr && remix_.DestroyMesh != nullptr) {
      remix_.DestroyMesh(newMeshHandle);
    }
    return false;
  }

  destroyChunkMeshHandle(meshData);
  meshData.meshHandle = newMeshHandle;
  meshData.meshHash = meshInfo.hash;
  return true;
}

void RemixRenderer::destroyChunkMesh(ChunkMeshData& meshData) {
  destroyChunkMeshHandle(meshData);
  destroyChunkTorchLights(meshData);
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


}  // namespace mcrtx