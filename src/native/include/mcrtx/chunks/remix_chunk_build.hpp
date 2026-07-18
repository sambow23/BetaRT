#pragma once

#include <vector>

#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx {

struct ChunkGeometryBuild {
  std::vector<geometry::SurfaceBuildBuffers> surfacesToBuild;
  std::vector<TorchLightPlacement> desiredTorchLights;
};

}  // namespace mcrtx
