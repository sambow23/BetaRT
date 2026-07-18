#pragma once

#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx::block_geometry {

void appendRepeaterGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendRailGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendRedstoneDustGeometry(
    const ChunkBlockCell& cell,
    bool connectWest,
    bool connectEast,
    bool connectNorth,
    bool connectSouth,
    bool climbWest,
    bool climbEast,
    bool climbNorth,
    bool climbSouth,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

}  // namespace mcrtx::block_geometry
