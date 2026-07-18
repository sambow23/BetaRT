#pragma once

#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx::block_geometry {

void appendFireGeometry(
    int worldX, int worldY, int worldZ,
    bool hasBase,
    bool westNeighbor, bool eastNeighbor,
    bool northNeighbor, bool southNeighbor,
    bool upNeighbor,
    float localX, float localY, float localZ,
    std::uint32_t frameIndex,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendPortalGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

}  // namespace mcrtx::block_geometry
