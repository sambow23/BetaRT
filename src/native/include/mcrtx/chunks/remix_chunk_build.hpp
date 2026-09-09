#pragma once

#include <vector>

#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/chunks/terrain_types.hpp"

namespace mcrtx {

struct TerrainSurface : geometry::SurfaceBuildBuffers {
  std::uint8_t materialClass {0};
};

struct ChunkGeometryBuild {
  std::uint64_t fingerprint {0};
  std::vector<TerrainSurface> surfacesToBuild;
};

}  // namespace mcrtx
