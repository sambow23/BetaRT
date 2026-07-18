#pragma once

#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/scene/remix_renderer_scene.hpp"

namespace mcrtx::block_geometry {

void appendCrossedQuadGeometry(
    const ChunkBlockCell& cell,
    int worldX, int worldY, int worldZ,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendCropGeometry(
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

void appendCactusGeometry(
    bool renderBottom,
    bool renderTop,
    bool renderNorth,
    bool renderSouth,
    bool renderWest,
    bool renderEast,
    const ChunkBlockCell& cell,
    float localX, float localY, float localZ,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

}  // namespace mcrtx::block_geometry
