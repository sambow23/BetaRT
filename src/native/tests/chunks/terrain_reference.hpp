#pragma once
#include "mcrtx/chunks/terrain_pipeline.hpp"
#include "mcrtx/core/remix_render_common.hpp"
namespace mcrtx {
struct ReferenceTerrainSection {
  bool hasOccupancy {true};
  std::array<std::uint8_t, 4096> occupancy {};
  std::array<ChunkBlockCell, 4096> cells {};
};
struct ReferenceTerrainBuild {
  std::vector<geometry::SurfaceBuildBuffers> surfacesToBuild;
  std::vector<TorchLightPlacement> desiredTorchLights;
  std::vector<PortalLightPlacement> desiredPortalLights;
  std::vector<GlowstoneLightPlacement> desiredGlowstoneLights;
};
class ReferenceTerrainEmitter {
public:
  std::unordered_map<ChunkKey, ReferenceTerrainSection, ChunkKeyHash> chunkMeshes_;
  std::array<remixapi_MaterialHandle, detail::kTerrainMaterialClassCount> terrainMaterialHandles_ {};
  void emit(const ChunkKey& key, ReferenceTerrainSection& section, ReferenceTerrainBuild& build);
};
}
