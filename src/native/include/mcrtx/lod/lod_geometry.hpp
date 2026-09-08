// Quad emission for downsampled LOD terrain.

#pragma once

#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

#include "mcrtx/core/remix_geometry_common.hpp"
#include "mcrtx/lod/lod_types.hpp"

namespace mcrtx::lod {

// Emits one axis-aligned face spanning arbitrary bounds.
//
// With the pre-tiled LOD atlas the face maps onto a slot whose texture already
// repeats the block texture step times, so a single quad shows the block
// pattern at its true size. Without it, the face falls back to stretching one
// terrain tile, which renders the texture at cell scale.
//
// This deliberately does not reuse appendBoundsFaceGeometry: that clamps UVs to
// the one block containing the face origin, which is correct for full-detail
// geometry and wrong for a cell covering several blocks.
void appendLodFace(
    bool useLodAtlas,
    int faceIndex,
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    float vSpanScale,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices);

// Turns a finished column grid into quads: one upward face per column, plus a
// skirt wherever a neighbouring column's surface drops away. Positions are
// local to the region origin; the instance transform places the region.
void buildRegionSurface(
    const RegionKey& key,
    const std::vector<LodColumn>& columns,
    geometry::SurfaceBuildBuffers& surface,
    bool useLodAtlas);

// Cheap content hash so an unchanged rebuild can keep its existing mesh handle
// instead of forcing Remix to rebuild the BLAS.
std::uint64_t computeRegionFingerprint(const geometry::SurfaceBuildBuffers& surface);

}  // namespace mcrtx::lod
