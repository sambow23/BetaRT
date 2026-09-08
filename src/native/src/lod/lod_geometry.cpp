// Quad emission for downsampled LOD terrain.

#include "mcrtx/lod/lod_geometry.hpp"

#include "mcrtx/chunks/remix_chunk_policy.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <cmath>
#include <cstring>

namespace mcrtx::lod {

using namespace mcrtx::detail;

namespace {

// Pre-tiled LOD atlas layout: 16x16 slots of 128 px in a 2048 px atlas. Each
// slot repeats its 16 px terrain tile, and UVs address the middle 64 px -- four
// tiles, matching step 4 -- leaving a 32 px gutter of continued pattern so mip
// levels never pull in a neighbouring slot.
constexpr float kLodSlotsPerAxis = 16.0f;
constexpr float kLodContentOffset = 32.0f / 2048.0f;
constexpr float kLodContentSpan = 64.0f / 2048.0f;

// Native face indices follow kFaceNormals in remix_render_common.hpp:
// 0 = -Z, 1 = +Z, 2 = -X, 3 = +X, 4 = -Y, 5 = +Y.
constexpr int kFaceNegZ = 0;
constexpr int kFacePosZ = 1;
constexpr int kFaceNegX = 2;
constexpr int kFacePosX = 3;
constexpr int kFaceDown = 4;
constexpr int kFaceUp = 5;

// Height difference below which two cells meeting along an edge are treated as
// flush and no wall is emitted between them. Well under a pixel at LOD range,
// and comfortably above the rounding a corner average introduces.
constexpr float kFlushEpsilon = 1.0f / 512.0f;

// Neighbour offsets in column-grid space, paired with the face that looks
// toward that neighbour.
constexpr int kSkirtNeighbors[4][3] = {
  {0, -1, kFaceNegZ},
  {0, 1, kFacePosZ},
  {-1, 0, kFaceNegX},
  {1, 0, kFacePosX},
};

// Corners of the emitted grid: one more than the cells along each edge. Corner
// (i, j) sits at the meeting point of cells (i-1, j-1) through (i, j), all four
// of which the apron guarantees are present.
constexpr int kCornerGridSide = kRegionColumns + 1;
constexpr int kCornerGridCells = kCornerGridSide * kCornerGridSide;

constexpr int cornerIndex(int i, int j) noexcept {
  return j * kCornerGridSide + i;
}

std::uint64_t mixHash(std::uint64_t hash, std::uint64_t value) noexcept {
  hash ^= value + 0x9E3779B97F4A7C15ull + (hash << 6) + (hash >> 2);
  return hash;
}

// True when the column's ground sits at or below its own water surface.
//
// Distant water is registered as an opaque material -- opacityConstant 1 with
// useDrawCallAlphaState off, see initializeLodMaterials -- because refraction
// resolves to nothing at LOD range. Opaque water means nothing under it is ever
// shaded, so a submerged sea floor is geometry that is paid for in full and
// seen by no ray.
bool isSubmerged(const LodColumn& column) noexcept {
  return column.waterY >= 0 && column.waterY >= column.topY;
}

// True when the column is submerged and so is every neighbour that could let a
// ray in from the side, which is what makes dropping its opaque geometry safe
// rather than merely usually safe.
//
// The apron carries the neighbours of the edge columns too, so this now answers
// for the whole region rather than leaving a rim of sea floor around it. An
// apron cell the store had no tile for arrives with every height unset, which
// reads as not submerged and so keeps its neighbour's floor -- the conservative
// answer, and the one that needs no separate "unknown" flag to express.
bool isEnclosedUnderWater(
    const std::vector<LodColumn>& columns,
    int columnX,
    int columnZ) noexcept {
  if (!isSubmerged(columns[paddedColumnIndex(columnX, columnZ)])) {
    return false;
  }
  for (const auto& neighbor : kSkirtNeighbors) {
    if (!isSubmerged(columns[paddedColumnIndex(
            columnX + neighbor[0], columnZ + neighbor[1])])) {
      return false;
    }
  }
  return true;
}

// The UV window one atlas slot occupies, resolved once per face.
struct SlotUv {
  float minU {0.0f};
  float minV {0.0f};
  float spanU {0.0f};
  float spanV {0.0f};
};

SlotUv slotUvFor(
    bool useLodAtlas,
    std::int16_t terrainTileIndex,
    float vSpanScale) noexcept {
  const int terrainTile = chunk::normalizeTerrainTileIndex(terrainTileIndex);
  SlotUv uv {};
  if (useLodAtlas) {
    // Slot column and row fall out of the same masks the terrain atlas uses,
    // because both atlases are a 16x16 grid.
    uv.minU = static_cast<float>(terrainTile & 0x0F) / kLodSlotsPerAxis + kLodContentOffset;
    uv.minV = static_cast<float>(terrainTile & 0xF0) / 256.0f + kLodContentOffset;
    uv.spanU = kLodContentSpan;
    // Vertical faces cover a height that has nothing to do with the cell width,
    // so their V range is scaled to the number of blocks they actually span.
    // Without this a tall cliff skirt stretches four tiles over its whole
    // height, which is the same wrong-scale artifact the LOD atlas removes from
    // the top faces.
    uv.spanV = kLodContentSpan * vSpanScale;
  } else {
    uv.minU = static_cast<float>((terrainTile & 0x0F) * 16) / kAtlasSizePixels;
    uv.minV = static_cast<float>(terrainTile & 0xF0) / kAtlasSizePixels;
    const float usableTileSize = kAtlasTileSizePixels - kAtlasUvInsetPixels;
    uv.spanU = usableTileSize / kAtlasSizePixels;
    uv.spanV = usableTileSize / kAtlasSizePixels;
  }
  return uv;
}

// One cell's patch of the ground heightfield: four corners at their own heights
// and their own normals, still four unshared vertices.
//
// The vertices stay unshared even though neighbouring cells agree on the corner
// heights, because each cell addresses its own window into the pre-tiled atlas
// -- sharing a vertex would mean sharing a UV, and the cell either side of it
// wants a different slot. Shading is continuous anyway: the normal is a
// property of the corner rather than of the cell, so both copies of a shared
// corner carry the same one and the seam does not show.
void appendLodTopPatch(
    bool useLodAtlas,
    float minX,
    float minZ,
    float maxX,
    float maxZ,
    const float cornerY[4],
    const float cornerNormal[4][3],
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const SlotUv uv = slotUvFor(useLodAtlas, terrainTileIndex, 1.0f);

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = kFaceVertexOffsets[kFaceUp][vertexIndex][0] == 0.0f ? minX : maxX;
    vertex.position[1] = cornerY[vertexIndex];
    vertex.position[2] = kFaceVertexOffsets[kFaceUp][vertexIndex][2] == 0.0f ? minZ : maxZ;
    vertex.normal[0] = cornerNormal[vertexIndex][0];
    vertex.normal[1] = cornerNormal[vertexIndex][1];
    vertex.normal[2] = cornerNormal[vertexIndex][2];
    vertex.texcoord[0] = uv.minU + kFaceTexcoords[kFaceUp][vertexIndex][0] * uv.spanU;
    vertex.texcoord[1] = uv.minV + kFaceTexcoords[kFaceUp][vertexIndex][1] * uv.spanV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  // Four corners at four heights are not coplanar, so which diagonal the patch
  // is split along changes its shape. Splitting along the pair that differs
  // least puts the fold where the surface is already flattest, which keeps a
  // ridge running diagonally across a cell from being cut into a notch.
  static constexpr std::uint32_t kFlippedDiagonal[6] = {0, 1, 3, 1, 2, 3};
  const bool flip = std::fabs(cornerY[0] - cornerY[2])
      > std::fabs(cornerY[1] - cornerY[3]);
  for (int corner = 0; corner < 6; ++corner) {
    indices.push_back(baseVertex + (flip ? kFlippedDiagonal[corner] : kFaceIndices[corner]));
  }
}

// One vertical wall whose top and bottom edges may each be sloped.
//
// Cells no longer meet at a single height along a shared edge -- each end of
// the edge has its own -- so the wall closing a cliff is a trapezoid rather
// than a rectangle. Winding, vertex order and UV layout still come from the
// shared face tables, so a wall is laid out like every other face in the field;
// only each vertex's Y is chosen by which end of the edge it sits at.
void appendLodWallQuad(
    bool useLodAtlas,
    int faceIndex,
    float planeX,
    float planeZ,
    float spanMin,
    float spanMax,
    float topAtMin,
    float topAtMax,
    float bottomAtMin,
    float bottomAtMax,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    float vSpanScale,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const SlotUv uv = slotUvFor(useLodAtlas, terrainTileIndex, vSpanScale);

  // The +-Z faces run along X and the +-X faces along Z; the other horizontal
  // axis is the plane the wall stands in.
  const bool spansX = faceIndex == kFaceNegZ || faceIndex == kFacePosZ;

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    const float* offsets = kFaceVertexOffsets[faceIndex][vertexIndex];
    const bool atFarEnd = (spansX ? offsets[0] : offsets[2]) != 0.0f;
    const float top = atFarEnd ? topAtMax : topAtMin;
    const float bottom = atFarEnd ? bottomAtMax : bottomAtMin;

    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = spansX
        ? (offsets[0] == 0.0f ? spanMin : spanMax)
        : planeX;
    vertex.position[1] = offsets[1] == 0.0f ? bottom : top;
    vertex.position[2] = spansX
        ? planeZ
        : (offsets[2] == 0.0f ? spanMin : spanMax);
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];
    vertex.texcoord[0] = uv.minU + kFaceTexcoords[faceIndex][vertexIndex][0] * uv.spanU;
    vertex.texcoord[1] = uv.minV + kFaceTexcoords[faceIndex][vertexIndex][1] * uv.spanV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

// A wall cut into `segments` stacked quads, each a linear slice of the whole.
// The caller picks the count because a cliff and the region-edge curtain want
// different rules: a cliff is scenery and keeps its texture at block scale, the
// curtain is buried filler and trades scale for quad count.
void appendSplitWall(
    bool useLodAtlas,
    int faceIndex,
    float planeX,
    float planeZ,
    float spanMin,
    float spanMax,
    float topAtMin,
    float topAtMax,
    float bottomAtMin,
    float bottomAtMax,
    int segments,
    float vSpanScale,
    std::int16_t terrainTileIndex,
    std::uint32_t vertexColor,
    std::vector<remixapi_HardcodedVertex>& vertices,
    std::vector<std::uint32_t>& indices) {
  const float dropAtMin = topAtMin - bottomAtMin;
  const float dropAtMax = topAtMax - bottomAtMax;
  const float inverse = 1.0f / static_cast<float>(segments);

  for (int segment = 0; segment < segments; ++segment) {
    const float startFraction = static_cast<float>(segment) * inverse;
    const float endFraction = static_cast<float>(segment + 1) * inverse;
    appendLodWallQuad(
        useLodAtlas,
        faceIndex,
        planeX, planeZ,
        spanMin, spanMax,
        topAtMin - dropAtMin * startFraction,
        topAtMax - dropAtMax * startFraction,
        topAtMin - dropAtMin * endFraction,
        topAtMax - dropAtMax * endFraction,
        terrainTileIndex,
        vertexColor,
        vSpanScale,
        vertices,
        indices);
  }
}

// The heightfield, sampled at the corners of the emitted grid.
//
// Held as a corner field rather than as per-cell heights because the corner is
// where agreement has to happen: two cells sharing a corner read the same entry
// here and so land on exactly the same vertex position, which is what makes the
// surface continuous without sharing a vertex between them.
struct CornerField {
  float height[kCornerGridCells];
  float normal[kCornerGridCells][3];
  // False where no cell touching the corner has a surface -- open sky, or
  // ground nothing has ever observed.
  bool known[kCornerGridCells];
  // False where cells meeting at the corner disagree by more than the cliff
  // threshold. There each keeps its own height instead of the average, and the
  // wall pass closes the step between them.
  bool smooth[kCornerGridCells];
};

void buildCornerField(
    const std::vector<LodColumn>& columns,
    int renderPass,
    int step,
    CornerField& field) {
  const float cliffThreshold = cliffThresholdBlocks(step);
  const float cellWidth = static_cast<float>(step);

  for (int j = 0; j < kCornerGridSide; ++j) {
    for (int i = 0; i < kCornerGridSide; ++i) {
      // The 2x2 of cells meeting at this corner. Every one of them is in the
      // buffer: interior corners read the region's own cells, and the corners
      // on the boundary read the apron.
      float heights[2][2] {};
      bool present[2][2] {};
      int count = 0;
      float sum = 0.0f;
      for (int deltaZ = 0; deltaZ < 2; ++deltaZ) {
        for (int deltaX = 0; deltaX < 2; ++deltaX) {
          const LodColumn& cell =
              columns[paddedColumnIndex(i - 1 + deltaX, j - 1 + deltaZ)];
          if (!cell.hasSurface(renderPass)) {
            continue;
          }
          present[deltaZ][deltaX] = true;
          heights[deltaZ][deltaX] = cell.surfaceHeight(renderPass);
          sum += heights[deltaZ][deltaX];
          ++count;
        }
      }

      const int index = cornerIndex(i, j);
      field.known[index] = count > 0;
      field.height[index] = count > 0 ? sum / static_cast<float>(count) : 0.0f;
      field.normal[index][0] = 0.0f;
      field.normal[index][1] = 1.0f;
      field.normal[index][2] = 0.0f;
      field.smooth[index] = false;
      if (count == 0) {
        continue;
      }

      // Cliff test over the four edge-adjacent pairs rather than over the whole
      // spread of the 2x2. A slope running diagonally puts twice its per-axis
      // rise between opposite corners, and calling that a cliff would leave
      // every diagonal hillside the staircase this is meant to remove.
      float worstStep = 0.0f;
      for (int side = 0; side < 2; ++side) {
        if (present[side][0] && present[side][1]) {
          worstStep = std::max(worstStep, std::fabs(heights[side][0] - heights[side][1]));
        }
        if (present[0][side] && present[1][side]) {
          worstStep = std::max(worstStep, std::fabs(heights[0][side] - heights[1][side]));
        }
      }
      field.smooth[index] = worstStep <= cliffThreshold;
      if (!field.smooth[index]) {
        // A cliff corner keeps the flat upward normal. The drop is carried by
        // a wall with a normal of its own, and tilting the plateau's rim into
        // it would light the top of a mesa as though it were the side.
        continue;
      }

      // Slope of the surface at the corner, from the same 2x2: the difference
      // between the mean height of the cells on either side of the corner,
      // over the distance between their centres. This is the whole point of
      // the pass. Every top face used to take the flat +Y normal, so every
      // hillside caught the light identically and the field read as a textured
      // plane however much relief it had.
      float axisMean[2][2] {};
      int axisCount[2][2] {};
      for (int deltaZ = 0; deltaZ < 2; ++deltaZ) {
        for (int deltaX = 0; deltaX < 2; ++deltaX) {
          if (!present[deltaZ][deltaX]) {
            continue;
          }
          axisMean[0][deltaX] += heights[deltaZ][deltaX];
          ++axisCount[0][deltaX];
          axisMean[1][deltaZ] += heights[deltaZ][deltaX];
          ++axisCount[1][deltaZ];
        }
      }

      float slope[2] {0.0f, 0.0f};
      for (int axis = 0; axis < 2; ++axis) {
        // One side of the corner having no surface leaves the slope along that
        // axis unknowable rather than zero, but zero is the honest answer to
        // draw: a shoreline cell tilting toward ground that is not there would
        // be inventing relief out of a hole in the data.
        if (axisCount[axis][0] == 0 || axisCount[axis][1] == 0) {
          continue;
        }
        const float low = axisMean[axis][0] / static_cast<float>(axisCount[axis][0]);
        const float high = axisMean[axis][1] / static_cast<float>(axisCount[axis][1]);
        slope[axis] = (high - low) / cellWidth;
      }

      const float length =
          std::sqrt(slope[0] * slope[0] + 1.0f + slope[1] * slope[1]);
      field.normal[index][0] = -slope[0] / length;
      field.normal[index][1] = 1.0f / length;
      field.normal[index][2] = -slope[1] / length;
    }
  }
}

// Corner indices of one cell, in kFaceVertexOffsets[kFaceUp] order:
// (min, min), (max, min), (max, max), (min, max).
void cellCornerIndices(int columnX, int columnZ, int out[4]) noexcept {
  out[0] = cornerIndex(columnX, columnZ);
  out[1] = cornerIndex(columnX + 1, columnZ);
  out[2] = cornerIndex(columnX + 1, columnZ + 1);
  out[3] = cornerIndex(columnX, columnZ + 1);
}

// Heights this cell draws its four corners at: the smoothed field where the
// corner is smooth, and the cell's own flat height where it is a cliff corner.
//
// Both cells either side of a cliff corner make that choice from the same 2x2,
// so the decision is symmetric and the disagreement that follows is exactly the
// step a wall is emitted for -- never a crack nobody closes.
void cellCornerHeights(
    const CornerField& field,
    int columnX,
    int columnZ,
    float ownHeight,
    float out[4]) noexcept {
  int indices[4];
  cellCornerIndices(columnX, columnZ, indices);
  for (int corner = 0; corner < 4; ++corner) {
    const int index = indices[corner];
    out[corner] = field.known[index] && field.smooth[index]
        ? field.height[index]
        : ownHeight;
  }
}

void cellCornerNormals(
    const CornerField& field,
    int columnX,
    int columnZ,
    float out[4][3]) noexcept {
  int indices[4];
  cellCornerIndices(columnX, columnZ, indices);
  for (int corner = 0; corner < 4; ++corner) {
    for (int axis = 0; axis < 3; ++axis) {
      out[corner][axis] = field.normal[indices[corner]][axis];
    }
  }
}

}  // namespace

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
    std::vector<std::uint32_t>& indices) {
  const std::uint32_t baseVertex = static_cast<std::uint32_t>(vertices.size());
  const SlotUv uv = slotUvFor(useLodAtlas, terrainTileIndex, vSpanScale);

  for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
    remixapi_HardcodedVertex vertex {};
    vertex.position[0] = kFaceVertexOffsets[faceIndex][vertexIndex][0] == 0.0f ? minX : maxX;
    vertex.position[1] = kFaceVertexOffsets[faceIndex][vertexIndex][1] == 0.0f ? minY : maxY;
    vertex.position[2] = kFaceVertexOffsets[faceIndex][vertexIndex][2] == 0.0f ? minZ : maxZ;
    vertex.normal[0] = kFaceNormals[faceIndex][0];
    vertex.normal[1] = kFaceNormals[faceIndex][1];
    vertex.normal[2] = kFaceNormals[faceIndex][2];

    const float unitU = kFaceTexcoords[faceIndex][vertexIndex][0];
    const float unitV = kFaceTexcoords[faceIndex][vertexIndex][1];
    vertex.texcoord[0] = uv.minU + unitU * uv.spanU;
    vertex.texcoord[1] = uv.minV + unitV * uv.spanV;
    vertex.color = vertexColor;
    vertices.push_back(vertex);
  }

  for (const std::uint32_t baseIndex : kFaceIndices) {
    indices.push_back(baseVertex + baseIndex);
  }
}

void buildRegionSurface(
    const RegionKey& key,
    const std::vector<LodColumn>& columns,
    geometry::SurfaceBuildBuffers& surface,
    bool useLodAtlas) {
  if (columns.size() != static_cast<std::size_t>(kPaddedColumnsPerRegion)) {
    return;
  }

  const float step = static_cast<float>(key.step);
  const int renderPass = key.renderPass;
  // A column on the region edge has no neighbour to measure against, so its
  // curtain just needs to be deep enough that whatever sits beyond the edge
  // overlaps it rather than leaving a hole. That "whatever" is a region of the
  // same detail level, or -- at a ring boundary -- one of the next level, whose
  // heights are quantised differently and so can disagree by more. The depth
  // grows with the step for that reason. Dropping all the way to the world
  // floor would cost a stack of quads per edge column for geometry nothing
  // ever sees.
  //
  // The apron does not retire the curtain, even though same-level neighbours
  // now agree exactly along a shared edge. Two things still need it: a ring
  // boundary, where the neighbour is a coarser region the apron says nothing
  // about, and a region built while its neighbour's tile was still missing,
  // whose boundary corners averaged over two cells where the neighbour will
  // later average over four.
  const float edgeSkirtDepth = edgeSkirtDepthBlocks(key.step);

  // The curtain is seam filler rather than scenery: all but its topmost sliver
  // is buried in whatever stands beyond the region edge. Drawing it at block
  // scale costs one quad per cell width of depth -- eight per edge column at
  // step 2, which on its own matches every top face in the region put together
  // -- so it is drawn in as few quads as a bounded amount of vertical squash
  // allows instead. Each quad still starts at the top of the tile, so the part
  // of the texture that shows where the curtain peeks out is the part that is
  // least distorted.
  const int curtainQuads = curtainQuadCount(key.step);

  CornerField field;
  buildCornerField(columns, renderPass, key.step, field);

  // A generous reserve: one top face per column plus two walls on average.
  surface.vertices.reserve(surface.vertices.size() + kColumnsPerRegion * 12);
  surface.indices.reserve(surface.indices.size() + kColumnsPerRegion * 18);

  // What the top-face pass worked out, kept for the two passes after it. The
  // wall pass asks about both cells either side of every edge, so recomputing
  // any of this there would repeat the submerged-neighbour scan and the corner
  // lookups four times per cell.
  //
  // `hasSurface` and `drawn` differ only for a sea-floor cell sealed under
  // water: it has a height the wall pass has to measure a drop against, but no
  // geometry of its own.
  bool hasSurface[kColumnsPerRegion] {};
  bool drawn[kColumnsPerRegion] {};
  float cornerHeights[kColumnsPerRegion][4] {};

  // Ground, as a continuous heightfield.
  //
  // One flat-topped box per cell was the obvious thing and it is what made
  // distant terrain read as a staircase: every cell sat at an integer Y with
  // vertical walls down to its neighbours, so a hillside that rises two blocks
  // over sixty was drawn as thirty steps. A vertex per corner at the average of
  // the cells meeting there costs the same two triangles and describes an
  // actual slope.
  for (int columnZ = 0; columnZ < kRegionColumns; ++columnZ) {
    for (int columnX = 0; columnX < kRegionColumns; ++columnX) {
      const LodColumn& column = columns[paddedColumnIndex(columnX, columnZ)];
      if (!column.hasSurface(renderPass)) {
        continue;
      }

      // Corner heights are resolved for every cell that has a surface, not only
      // for the ones that draw one: the wall pass measures the drop to a cell
      // whose own top face was skipped, and reading an unresolved height there
      // would hang a wall down to the world floor.
      const int cellIndex = columnZ * kRegionColumns + columnX;
      hasSurface[cellIndex] = true;
      cellCornerHeights(
          field, columnX, columnZ, column.surfaceHeight(renderPass),
          cornerHeights[cellIndex]);

      // Sea floor with water on every side of it is never shaded, because the
      // water above it is opaque at this range. Skipping it drops the whole
      // interior of an ocean region -- top faces and the walls between them --
      // and with it, often, the region's entire opaque mesh and its instance.
      if (renderPass == 0 && isEnclosedUnderWater(columns, columnX, columnZ)) {
        continue;
      }
      drawn[cellIndex] = true;

      float normals[4][3];
      cellCornerNormals(field, columnX, columnZ, normals);

      appendLodTopPatch(
          useLodAtlas,
          static_cast<float>(columnX) * step,
          static_cast<float>(columnZ) * step,
          static_cast<float>(columnX + 1) * step,
          static_cast<float>(columnZ + 1) * step,
          cornerHeights[cellIndex],
          normals,
          column.topTile,
          column.color,
          surface.vertices,
          surface.indices);
    }
  }

  // Walls, wherever the heightfield is not continuous across a shared edge.
  //
  // Each interior edge is visited once, from the cell on its low-coordinate
  // side, which is what keeps a cliff from being walled twice -- once from
  // each neighbour -- into a pair of coincident surfaces a path tracer would
  // light through. Where the corner heights agree there is nothing to close and
  // no quad is emitted, and on smooth ground that is every edge in the region:
  // this is where the triangles the heightfield spends on slopes come back.
  for (int columnZ = 0; columnZ < kRegionColumns; ++columnZ) {
    for (int columnX = 0; columnX < kRegionColumns; ++columnX) {
      const int cellIndex = columnZ * kRegionColumns + columnX;
      for (int axis = 0; axis < 2; ++axis) {
        const bool alongX = axis == 0;
        const int neighborX = alongX ? columnX + 1 : columnX;
        const int neighborZ = alongX ? columnZ : columnZ + 1;
        if (neighborX >= kRegionColumns || neighborZ >= kRegionColumns) {
          continue;
        }
        const int neighborIndex = neighborZ * kRegionColumns + neighborX;
        if (!hasSurface[cellIndex] || !hasSurface[neighborIndex]) {
          continue;
        }

        // Corner pairs along the shared edge, ordered from the low end of the
        // axis the edge runs along to the high end. Corners are numbered in
        // kFaceVertexOffsets[kFaceUp] order: (min, min), (max, min), (max, max),
        // (min, max).
        const int ownLow = alongX ? 1 : 3;
        const int ownHigh = 2;
        const int otherLow = 0;
        const int otherHigh = alongX ? 3 : 1;

        const float ownAtMin = cornerHeights[cellIndex][ownLow];
        const float ownAtMax = cornerHeights[cellIndex][ownHigh];
        const float otherAtMin = cornerHeights[neighborIndex][otherLow];
        const float otherAtMax = cornerHeights[neighborIndex][otherHigh];

        const float gapAtMin = ownAtMin - otherAtMin;
        const float gapAtMax = ownAtMax - otherAtMax;
        if (std::fabs(gapAtMin) < kFlushEpsilon && std::fabs(gapAtMax) < kFlushEpsilon) {
          continue;
        }

        // The wall belongs to whichever side stands higher: it is that cell's
        // outward face, wears that cell's side texture and tint, and is only
        // drawn if that cell is drawn at all.
        const bool ownIsHigher = gapAtMin + gapAtMax > 0.0f;
        const int higherIndex = ownIsHigher ? cellIndex : neighborIndex;
        if (!drawn[higherIndex]) {
          continue;
        }
        const LodColumn& higher = ownIsHigher
            ? columns[paddedColumnIndex(columnX, columnZ)]
            : columns[paddedColumnIndex(neighborX, neighborZ)];

        const float topAtMin = std::max(ownAtMin, otherAtMin);
        const float topAtMax = std::max(ownAtMax, otherAtMax);
        const float bottomAtMin = std::min(ownAtMin, otherAtMin);
        const float bottomAtMax = std::min(ownAtMax, otherAtMax);

        const int faceIndex = alongX
            ? (ownIsHigher ? kFacePosX : kFaceNegX)
            : (ownIsHigher ? kFacePosZ : kFaceNegZ);
        const float planeX = alongX ? static_cast<float>(columnX + 1) * step : 0.0f;
        const float planeZ = alongX ? 0.0f : static_cast<float>(columnZ + 1) * step;
        // The wall runs along Z when it stands in an X plane, and along X when
        // it stands in a Z plane.
        const float spanMin = alongX
            ? static_cast<float>(columnZ) * step
            : static_cast<float>(columnX) * step;
        const float spanMax = spanMin + step;

        // Split the wall into segments no taller than the cell is wide, so a
        // segment spans at most the tiling baked into one atlas slot and the
        // texture stays at block scale however deep the drop is. Cliffs inside
        // a region are scenery, unlike the curtain, so they keep it.
        const float deepest = std::max(topAtMin - bottomAtMin, topAtMax - bottomAtMax);
        int segments = static_cast<int>(std::ceil(deepest / step));
        if (segments < 1) {
          segments = 1;
        }
        const float meanDrop =
            ((topAtMin - bottomAtMin) + (topAtMax - bottomAtMax)) * 0.5f;

        appendSplitWall(
            useLodAtlas,
            faceIndex,
            planeX, planeZ,
            spanMin, spanMax,
            topAtMin, topAtMax,
            bottomAtMin, bottomAtMax,
            segments,
            meanDrop / (static_cast<float>(segments) * step),
            higher.sideTile,
            higher.color,
            surface.vertices,
            surface.indices);
      }
    }
  }

  // The curtain around the region's outer edge, hung from the same corner
  // heights the top faces end at so it starts flush with them.
  for (int edge = 0; edge < 4; ++edge) {
    const int faceIndex = kSkirtNeighbors[edge][2];
    const bool alongX = faceIndex == kFaceNegZ || faceIndex == kFacePosZ;
    for (int along = 0; along < kRegionColumns; ++along) {
      const int columnX = alongX ? along : (faceIndex == kFaceNegX ? 0 : kRegionColumns - 1);
      const int columnZ = alongX ? (faceIndex == kFaceNegZ ? 0 : kRegionColumns - 1) : along;
      const int cellIndex = columnZ * kRegionColumns + columnX;
      if (!drawn[cellIndex]) {
        continue;
      }

      // Corners along this edge, low end of the spanning axis first.
      int lowCorner = 0;
      int highCorner = 0;
      switch (faceIndex) {
        case kFaceNegZ: lowCorner = 0; highCorner = 1; break;
        case kFacePosZ: lowCorner = 3; highCorner = 2; break;
        case kFaceNegX: lowCorner = 0; highCorner = 3; break;
        default:        lowCorner = 1; highCorner = 2; break;
      }
      const float topAtMin = cornerHeights[cellIndex][lowCorner];
      const float topAtMax = cornerHeights[cellIndex][highCorner];

      const float planeX = alongX
          ? 0.0f
          : static_cast<float>(faceIndex == kFaceNegX ? columnX : columnX + 1) * step;
      const float planeZ = alongX
          ? static_cast<float>(faceIndex == kFaceNegZ ? columnZ : columnZ + 1) * step
          : 0.0f;
      const float spanMin = alongX
          ? static_cast<float>(columnX) * step
          : static_cast<float>(columnZ) * step;
      const float spanMax = spanMin + step;

      appendSplitWall(
          useLodAtlas,
          faceIndex,
          planeX, planeZ,
          spanMin, spanMax,
          topAtMin, topAtMax,
          topAtMin - edgeSkirtDepth, topAtMax - edgeSkirtDepth,
          curtainQuads,
          1.0f,
          columns[paddedColumnIndex(columnX, columnZ)].sideTile,
          columns[paddedColumnIndex(columnX, columnZ)].color,
          surface.vertices,
          surface.indices);
    }
  }

  // Canopy, drawn as a slab of its own rather than as the ground's surface.
  //
  // This is what the second pair of heights in a column is for. Raising a
  // forested cell's ground to treetop height and handing it a leaf texture is
  // the obvious shortcut, and it is exactly what made distant woodland wrong:
  // the wall logic above then hangs faces from the treetops down to every lower
  // neighbour, so a lone tree renders as a solid pillar of leaves and a wood
  // renders as a plateau with sheer green sides. Given a box of its own the
  // canopy has an underside, the ground stays visible beneath it, and a tree
  // reads as foliage standing in the air.
  //
  // Deliberately still boxes at integer heights while the ground below is a
  // heightfield. A canopy is a discontinuous thing -- a wood has an edge, and
  // smoothing across it would drape the treetops down to the field like a sheet
  // rather than ending them.
  //
  // Opaque pass only. A canopy over water is still a canopy, and the translucent
  // pass draws nothing but the water surface itself.
  if (renderPass == 0) {
    for (int columnZ = 0; columnZ < kRegionColumns; ++columnZ) {
      for (int columnX = 0; columnX < kRegionColumns; ++columnX) {
        const LodColumn& column = columns[paddedColumnIndex(columnX, columnZ)];
        if (!column.hasCanopy()) {
          continue;
        }

        const float minX = static_cast<float>(columnX) * step;
        const float minZ = static_cast<float>(columnZ) * step;
        const float maxX = minX + step;
        const float maxZ = minZ + step;
        const float canopyTop = column.canopyTop();
        const float canopyBottom = column.canopyBottom();

        appendLodFace(
            useLodAtlas,
            kFaceUp,
            minX, canopyTop, minZ,
            maxX, canopyTop, maxZ,
            column.canopyTopTile,
            column.canopyColor,
            1.0f,
            surface.vertices,
            surface.indices);

        bool bareNeighbor = false;
        for (const auto& neighbor : kSkirtNeighbors) {
          const int neighborX = columnX + neighbor[0];
          const int neighborZ = columnZ + neighbor[1];
          const int faceIndex = neighbor[2];

          // A neighbour beyond the region edge is treated as forested, apron or
          // no apron. A wood that crosses the boundary would otherwise grow a
          // wall of leaves down its middle if the apron happened to be missing;
          // a wood that genuinely ends on the boundary loses one row of sides,
          // which is the cheaper mistake at this range and the one the region
          // next door covers anyway.
          float neighborTop = canopyTop;
          const bool inRegion = neighborX >= 0 && neighborX < kRegionColumns
              && neighborZ >= 0 && neighborZ < kRegionColumns;
          if (inRegion) {
            const LodColumn& other = columns[paddedColumnIndex(neighborX, neighborZ)];
            if (other.hasCanopy()) {
              neighborTop = other.canopyTop();
            } else {
              neighborTop = canopyBottom;
              bareNeighbor = true;
            }
          }
          if (neighborTop >= canopyTop) {
            continue;
          }

          const float wallMinX = faceIndex == kFacePosX ? maxX : minX;
          const float wallMaxX = faceIndex == kFaceNegX ? minX : maxX;
          const float wallMinZ = faceIndex == kFacePosZ ? maxZ : minZ;
          const float wallMaxZ = faceIndex == kFaceNegZ ? minZ : maxZ;
          const float wallBottom = neighborTop > canopyBottom ? neighborTop : canopyBottom;

          // Split like a cliff so the leaf texture stays at block scale however
          // deep the canopy is.
          float segmentTop = canopyTop;
          while (segmentTop > wallBottom) {
            const float segmentBottom = std::max(wallBottom, segmentTop - step);
            const float segmentHeight = segmentTop - segmentBottom;
            if (segmentHeight <= 0.0f) {
              break;
            }

            appendLodFace(
                useLodAtlas,
                faceIndex,
                wallMinX, segmentBottom, wallMinZ,
                wallMaxX, segmentTop, wallMaxZ,
                column.canopySideTile,
                column.canopyColor,
                segmentHeight / step,
                surface.vertices,
                surface.indices);

            segmentTop = segmentBottom;
          }
        }

        // The trunk holding the slab up.
        //
        // Drawn one block wide and centred rather than filling the cell, because
        // a trunk is one block wide in the world however much ground a cell
        // covers -- a cell-wide trunk at step 4 would be a four-block pillar,
        // which is a worse lie than the floating canopy this replaces. It runs
        // from the ground to the underside of the leaves; both ends are already
        // known, so nothing about the extent had to be sent.
        //
        // Sides only. The top is inside the foliage and the bottom is in the
        // ground, so neither is ever seen.
        if (column.hasTrunk && column.hasSurface(renderPass)) {
          const float groundTop = column.surfaceHeight(renderPass);
          if (canopyBottom > groundTop) {
            const float halfWidth = std::min(0.5f, step * 0.25f);
            const float centerX = (minX + maxX) * 0.5f;
            const float centerZ = (minZ + maxZ) * 0.5f;
            const float trunkMinX = centerX - halfWidth;
            const float trunkMaxX = centerX + halfWidth;
            const float trunkMinZ = centerZ - halfWidth;
            const float trunkMaxZ = centerZ + halfWidth;

            for (const auto& neighbor : kSkirtNeighbors) {
              const int faceIndex = neighbor[2];
              const float wallMinX = faceIndex == kFacePosX ? trunkMaxX : trunkMinX;
              const float wallMaxX = faceIndex == kFaceNegX ? trunkMinX : trunkMaxX;
              const float wallMinZ = faceIndex == kFacePosZ ? trunkMaxZ : trunkMinZ;
              const float wallMaxZ = faceIndex == kFaceNegZ ? trunkMinZ : trunkMaxZ;

              // Split at block scale like any other wall, so the bark does not
              // stretch up a tall trunk.
              float segmentTop = canopyBottom;
              while (segmentTop > groundTop) {
                const float segmentBottom = std::max(groundTop, segmentTop - step);
                const float segmentHeight = segmentTop - segmentBottom;
                if (segmentHeight <= 0.0f) {
                  break;
                }
                appendLodFace(
                    useLodAtlas,
                    faceIndex,
                    wallMinX, segmentBottom, wallMinZ,
                    wallMaxX, segmentTop, wallMaxZ,
                    column.trunkTile,
                    0xFFFFFFFFu,
                    segmentHeight / step,
                    surface.vertices,
                    surface.indices);
                segmentTop = segmentBottom;
              }
            }
          }
        }

        // The underside only shows where a wood ends. Inside one, every
        // neighbour's slab covers it, and drawing it there would add a quad per
        // forested cell for geometry no ray reaches.
        if (bareNeighbor) {
          appendLodFace(
              useLodAtlas,
              kFaceDown,
              minX, canopyBottom, minZ,
              maxX, canopyBottom, maxZ,
              column.canopyTopTile,
              column.canopyColor,
              1.0f,
              surface.vertices,
              surface.indices);
        }
      }
    }
  }
}

std::uint64_t computeRegionFingerprint(const geometry::SurfaceBuildBuffers& surface) {
  std::uint64_t hash = 1469598103934665603ull;
  hash = mixHash(hash, surface.vertices.size());
  hash = mixHash(hash, surface.indices.size());

  for (const remixapi_HardcodedVertex& vertex : surface.vertices) {
    std::uint32_t bits = 0;
    for (int axis = 0; axis < 3; ++axis) {
      std::memcpy(&bits, &vertex.position[axis], sizeof(bits));
      hash = mixHash(hash, bits);
    }
    std::memcpy(&bits, &vertex.texcoord[0], sizeof(bits));
    hash = mixHash(hash, bits);
    hash = mixHash(hash, vertex.color);
  }
  return hash;
}

}  // namespace mcrtx::lod
