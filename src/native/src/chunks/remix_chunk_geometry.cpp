// Neighbor-aware chunk block geometry dispatch.

#include "mcrtx/chunks/terrain_pipeline.hpp"
#include "mcrtx/chunks/remix_chunk_build.hpp"
#include "mcrtx/chunks/remix_block_geometry_effects.hpp"
#include "mcrtx/chunks/remix_block_geometry_fixtures.hpp"
#include "mcrtx/chunks/remix_block_geometry_fluids.hpp"
#include "mcrtx/chunks/remix_block_geometry_natural.hpp"
#include "mcrtx/chunks/remix_block_geometry_pistons.hpp"
#include "mcrtx/chunks/remix_block_geometry_redstone.hpp"
#include "mcrtx/chunks/remix_block_geometry_structures.hpp"
#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/core/remix_render_common.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/tracy.hpp"

#include <algorithm>
#include <array>
#include <bit>
#include <cstddef>
#include <cstdint>
#include <unordered_map>
#include <vector>

namespace mcrtx {

using namespace mcrtx::block_geometry;
using namespace mcrtx::chunk;
using namespace mcrtx::detail;
using namespace mcrtx::geometry;
void emitTerrainGeometry(const TerrainInputs& inputs, int pass, ChunkGeometryBuild& build) {
  const ChunkKey chunkKey {inputs.key.originX, inputs.key.originY, inputs.key.originZ, pass};
  const auto& meshData = *inputs.sections[13];
  auto& surfacesToBuild = build.surfacesToBuild;
  surfacesToBuild.reserve(8);
  std::unordered_map<std::uint32_t, std::size_t> surfaceIndexByHandle;

  const auto acquireSurface = [&surfacesToBuild, &surfaceIndexByHandle](std::uint8_t materialClass, const ChunkBlockCell &cell) -> SurfaceBuildBuffers& {
    const std::uint16_t emissionKey=cell.emissionLevel?std::uint16_t(cell.blockId*16+cell.emissionLevel):0;
    const std::uint32_t materialKey = materialClass | (std::uint32_t(emissionKey)<<8);
    const auto it = surfaceIndexByHandle.find(materialKey);
    if (it != surfaceIndexByHandle.end()) {
      return surfacesToBuild[it->second];
    }

    TerrainSurface surface;
    surface.materialClass = materialClass;
    surface.emissionKey=emissionKey;
    surface.vertices.reserve(512);
    surface.indices.reserve(768);
    const std::size_t surfaceIndex = surfacesToBuild.size();
    surfacesToBuild.push_back(std::move(surface));
    surfaceIndexByHandle.emplace(materialKey, surfaceIndex);
    return surfacesToBuild.back();
  };

  const auto hasFenceNeighbor = [&inputs, &chunkKey, &meshData, pass](int worldX, int worldY, int worldZ) -> bool {
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

    const TerrainSectionData* targetMesh = &meshData;
    if (!(neighborKey == chunkKey)) {
      const auto* neighbor = inputs.neighbor(neighborKey.originX, neighborKey.originY, neighborKey.originZ);
      if (neighbor == nullptr) {
        return false;
      }
      targetMesh = neighbor;
    }

    const int neighborIndex = blockIndex(localX, localY, localZ);
    if (targetMesh->occupancy[neighborIndex] == 0 || targetMesh->cells[neighborIndex].renderPass != pass) {
      return false;
    }

    const ChunkBlockCell& neighborCell = targetMesh->cells[neighborIndex];
    return neighborCell.blockId == kFenceBlockId && neighborCell.renderType == kFenceBlockRenderType;
  };

  const auto findWorldCell = [&inputs, &chunkKey, &meshData, pass](int worldX, int worldY, int worldZ) -> const ChunkBlockCell* {
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

    const TerrainSectionData* targetMesh = &meshData;
    if (!(neighborKey == chunkKey)) {
      const auto* neighbor = inputs.neighbor(neighborKey.originX, neighborKey.originY, neighborKey.originZ);
      if (neighbor == nullptr) {
        return nullptr;
      }
      targetMesh = neighbor;
    }

    const int neighborIndex = blockIndex(localX, localY, localZ);
    if (targetMesh->occupancy[neighborIndex] == 0 || targetMesh->cells[neighborIndex].renderPass != pass) {
      return nullptr;
    }

    return &targetMesh->cells[neighborIndex];
  };

  const auto hasSolidSupport = [&findWorldCell](int worldX, int worldY, int worldZ) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isSolidSupportBlock(*neighborCell);
  };

  const auto hasRedstoneConnection = [&findWorldCell](int worldX, int worldY, int worldZ, int direction) {
    const ChunkBlockCell* neighborCell = findWorldCell(worldX, worldY, worldZ);
    return neighborCell != nullptr && isRedstoneConnectionCell(*neighborCell, direction);
  };

  {
    MCRTX_TRACY_SCOPE("rebuildChunkMeshFromData.emitGeometry");
    for (int localY = 0; localY < kChunkDimension; ++localY) {
      for (int localZ = 0; localZ < kChunkDimension; ++localZ) {
        for (int localX = 0; localX < kChunkDimension; ++localX) {
        const int cellIndex = blockIndex(localX, localY, localZ);
        if (meshData.occupancy[cellIndex] == 0 || meshData.cells[cellIndex].renderPass != pass) {
          continue;
        }

        const ChunkBlockCell& cell = meshData.cells[cellIndex];
        const std::size_t materialClass = cell.materialClass < kTerrainMaterialClassCount
          ? cell.materialClass
          : kOpaqueTerrainMaterialClass;

        if (cell.renderType == kLiquidBlockRenderType
            && (materialClass == kWaterTerrainMaterialClass || materialClass == kLavaTerrainMaterialClass)) {
          SurfaceBuildBuffers& liquidSurface = acquireSurface(materialClass,cell);
          appendWaterGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              liquidSurface.vertices,
              liquidSurface.indices);
          continue;
        }

        if (isCrossedQuadRenderType(cell.renderType)) {
          SurfaceBuildBuffers& floraSurface = acquireSurface(materialClass,cell);
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

        if (isCropRenderType(cell.renderType) && isCropBlockId(cell.blockId)) {
          SurfaceBuildBuffers& cropSurface = acquireSurface(materialClass,cell);
          appendCropGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              cropSurface.vertices,
              cropSurface.indices);
          continue;
        }

        if (isFireRenderType(cell.renderType)) {
          auto &fire = acquireSurface(kCutoutTerrainMaterialClass,cell);
          const auto begin = fire.vertices.size();
          const int x = chunkKey.originX + localX, y = chunkKey.originY + localY, z = chunkKey.originZ + localZ;
          appendFireGeometry(x, y, z, hasSolidSupport(x, y - 1, z),
              hasSolidSupport(x - 1, y, z), hasSolidSupport(x + 1, y, z),
              hasSolidSupport(x, y, z - 1), hasSolidSupport(x, y, z + 1), hasSolidSupport(x, y + 1, z),
              float(localX), float(localY), float(localZ), 0, fire.vertices, fire.indices);
          // The direct client animates vanilla terrain tiles 31/47 on the CPU.
          // Convert the former 16-frame fire-sheet coordinates to these atlas tiles.
          for (std::size_t i = begin; i < fire.vertices.size(); ++i) {
            auto &vertex = fire.vertices[i];
            const float row = vertex.texcoord[1] >= .5f ? 1.f : 0.f;
            vertex.texcoord[0] = (15.f + vertex.texcoord[0] * 16.f) / 16.f;
            vertex.texcoord[1] = (1.f + row + (vertex.texcoord[1] * 2.f - row)) / 16.f;
          }
          continue;
        }

        if (isTorchRenderType(cell.renderType)) {
          SurfaceBuildBuffers& torchSurface = acquireSurface(materialClass,cell);
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
          SurfaceBuildBuffers& ladderSurface = acquireSurface(materialClass,cell);
          appendLadderGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              ladderSurface.vertices,
              ladderSurface.indices);
          continue;
        }

            if (isRedstoneDustRenderType(cell.renderType) && isRedstoneDustBlockId(cell.blockId)) {
              const int worldX = chunkKey.originX + localX;
              const int worldY = chunkKey.originY + localY;
              const int worldZ = chunkKey.originZ + localZ;
              const bool blockedAbove = hasSolidSupport(worldX, worldY + 1, worldZ);
              bool connectWest = hasRedstoneConnection(worldX - 1, worldY, worldZ, 1)
                || (!hasSolidSupport(worldX - 1, worldY, worldZ) && hasRedstoneConnection(worldX - 1, worldY - 1, worldZ, -1));
              bool connectEast = hasRedstoneConnection(worldX + 1, worldY, worldZ, 3)
                || (!hasSolidSupport(worldX + 1, worldY, worldZ) && hasRedstoneConnection(worldX + 1, worldY - 1, worldZ, -1));
              bool connectNorth = hasRedstoneConnection(worldX, worldY, worldZ - 1, 2)
                || (!hasSolidSupport(worldX, worldY, worldZ - 1) && hasRedstoneConnection(worldX, worldY - 1, worldZ - 1, -1));
              bool connectSouth = hasRedstoneConnection(worldX, worldY, worldZ + 1, 0)
                || (!hasSolidSupport(worldX, worldY, worldZ + 1) && hasRedstoneConnection(worldX, worldY - 1, worldZ + 1, -1));

              const bool climbWest = !blockedAbove && hasSolidSupport(worldX - 1, worldY, worldZ)
                && hasRedstoneConnection(worldX - 1, worldY + 1, worldZ, -1);
              const bool climbEast = !blockedAbove && hasSolidSupport(worldX + 1, worldY, worldZ)
                && hasRedstoneConnection(worldX + 1, worldY + 1, worldZ, -1);
              const bool climbNorth = !blockedAbove && hasSolidSupport(worldX, worldY, worldZ - 1)
                && hasRedstoneConnection(worldX, worldY + 1, worldZ - 1, -1);
              const bool climbSouth = !blockedAbove && hasSolidSupport(worldX, worldY, worldZ + 1)
                && hasRedstoneConnection(worldX, worldY + 1, worldZ + 1, -1);

              connectWest = connectWest || climbWest;
              connectEast = connectEast || climbEast;
              connectNorth = connectNorth || climbNorth;
              connectSouth = connectSouth || climbSouth;

              SurfaceBuildBuffers& redstoneSurface = acquireSurface(materialClass,cell);
              appendRedstoneDustGeometry(
                cell,
                connectWest,
                connectEast,
                connectNorth,
                connectSouth,
                climbWest,
                climbEast,
                climbNorth,
                climbSouth,
                static_cast<float>(localX),
                static_cast<float>(localY),
                static_cast<float>(localZ),
                redstoneSurface.vertices,
                redstoneSurface.indices);
              continue;
            }

        if (isRailRenderType(cell.renderType) && isRailBlockId(cell.blockId)) {
          SurfaceBuildBuffers& railSurface = acquireSurface(materialClass,cell);
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
          SurfaceBuildBuffers& stairSurface = acquireSurface(materialClass,cell);
          appendStairGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              stairSurface.vertices,
              stairSurface.indices);
          continue;
        }

        if (isLeverOrButtonRenderType(cell.renderType)) {
          SurfaceBuildBuffers& controlSurface = acquireSurface(materialClass,cell);
          if (isLeverBlockId(cell.blockId)) {
            appendLeverGeometry(
                cell,
                static_cast<float>(localX),
                static_cast<float>(localY),
                static_cast<float>(localZ),
                controlSurface.vertices,
                controlSurface.indices);
          } else if (isButtonBlockId(cell.blockId)) {
            appendBoxGeometry(
                static_cast<float>(localX) + cell.bounds[0],
                static_cast<float>(localY) + cell.bounds[1],
                static_cast<float>(localZ) + cell.bounds[2],
                static_cast<float>(localX) + cell.bounds[3],
                static_cast<float>(localY) + cell.bounds[4],
                static_cast<float>(localZ) + cell.bounds[5],
                cell.terrainTiles,
                kDefaultVertexColor,
                controlSurface.vertices,
                controlSurface.indices);
          }
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

          SurfaceBuildBuffers& doorSurface = acquireSurface(materialClass,cell);
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
          SurfaceBuildBuffers& fenceSurface = acquireSurface(materialClass,cell);
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

        if (isCactusRenderType(cell.renderType) && isCactusBlockId(cell.blockId)) {
          const int worldX = chunkKey.originX + localX;
          const int worldY = chunkKey.originY + localY;
          const int worldZ = chunkKey.originZ + localZ;
          const ChunkBlockCell* belowCell = findWorldCell(worldX, worldY - 1, worldZ);
          const ChunkBlockCell* aboveCell = findWorldCell(worldX, worldY + 1, worldZ);
          const ChunkBlockCell* northCell = findWorldCell(worldX, worldY, worldZ - 1);
          const ChunkBlockCell* southCell = findWorldCell(worldX, worldY, worldZ + 1);
          const ChunkBlockCell* westCell = findWorldCell(worldX - 1, worldY, worldZ);
          const ChunkBlockCell* eastCell = findWorldCell(worldX + 1, worldY, worldZ);

          SurfaceBuildBuffers& cactusSurface = acquireSurface(materialClass,cell);
          appendCactusGeometry(
              belowCell == nullptr || !isSolidSupportBlock(*belowCell),
              aboveCell == nullptr || !isSolidSupportBlock(*aboveCell),
              northCell == nullptr || !isSolidSupportBlock(*northCell),
              southCell == nullptr || !isSolidSupportBlock(*southCell),
              westCell == nullptr || !isSolidSupportBlock(*westCell),
              eastCell == nullptr || !isSolidSupportBlock(*eastCell),
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              cactusSurface.vertices,
              cactusSurface.indices);
          continue;
        }

        if (isBedRenderType(cell.renderType) && isBedBlockId(cell.blockId)) {
          SurfaceBuildBuffers& bedSurface = acquireSurface(materialClass,cell);
          appendBedGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              bedSurface.vertices,
              bedSurface.indices);
          continue;
        }

        if (isRepeaterRenderType(cell.renderType) && isRepeaterBlockId(cell.blockId)) {
          SurfaceBuildBuffers& repeaterSurface = acquireSurface(materialClass,cell);
          appendRepeaterGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              repeaterSurface.vertices,
              repeaterSurface.indices);
          continue;
        }

        if (isPistonBaseRenderType(cell.renderType) && isPistonBaseBlockId(cell.blockId)) {
          SurfaceBuildBuffers& pistonSurface = acquireSurface(materialClass,cell);
          appendPistonBaseGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              pistonSurface.vertices,
              pistonSurface.indices);
          continue;
        }

        if (isPistonHeadRenderType(cell.renderType) && isPistonHeadBlockId(cell.blockId)) {
          SurfaceBuildBuffers& pistonSurface = acquireSurface(materialClass,cell);
          appendPistonHeadGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              pistonSurface.vertices,
              pistonSurface.indices);
          continue;
        }

        if (cell.blockId == kNetherPortalBlockId) {
          SurfaceBuildBuffers& portalSurface = acquireSurface(materialClass,cell);
          appendPortalGeometry(
              cell,
              static_cast<float>(localX),
              static_cast<float>(localY),
              static_cast<float>(localZ),
              portalSurface.vertices,
              portalSurface.indices);
          continue;
        }

        if (cell.renderType == kCubeBlockRenderType && usesPartialCubeBounds(cell)) {
          SurfaceBuildBuffers& partialCubeSurface = acquireSurface(materialClass,cell);
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
            if (meshData.occupancy[neighborIndex] != 0 && meshData.cells[neighborIndex].renderPass == pass) {
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

            const auto* neighbor = inputs.neighbor(neighborKey.originX, neighborKey.originY, neighborKey.originZ);
            if (neighbor != nullptr) {
              const int neighborIndex = blockIndex(wrappedX, wrappedY, wrappedZ);
              if (neighbor->occupancy[neighborIndex] != 0 && neighbor->cells[neighborIndex].renderPass == pass) {
                faceOccluded = shouldCullFaceAgainstNeighbor(cell, neighbor->cells[neighborIndex]);
              }
            }
          }

          if (faceOccluded) {
            continue;
          }


          SurfaceBuildBuffers& faceSurface = acquireSurface(materialClass,cell);
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
            SurfaceBuildBuffers& overlaySurface = acquireSurface(kCutoutTerrainMaterialClass,cell);
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
  }

}

}  // namespace mcrtx
