// Distant terrain LOD: region keys, downsampled column samples, mesh state.

#pragma once

#include <cstddef>
#include <cstdint>
#include <vector>

#include <remix/remix_c.h>

namespace mcrtx::lod {

// Every region is a fixed 32x32 grid of columns no matter how far away it is;
// the detail level only changes how many world blocks a column covers. Region
// size therefore doubles with each level, which keeps mesh size — and so
// rebuild cost and BLAS size — constant across the whole LOD field.
inline constexpr int kRegionColumns = 32;
inline constexpr int kColumnsPerRegion = kRegionColumns * kRegionColumns;

// Cells of the neighbouring regions read alongside a region's own, so the grid
// Java hands over is 34x34 for the 32x32 it emits.
//
// The apron is what makes a continuous surface possible at a region boundary. A
// corner vertex takes the average of the cells meeting at it, and a corner on
// the boundary has two of those four cells in the region next door. Without
// them the two regions average different sets at the same corner, land on
// different heights, and crack -- and a crack in distant terrain is a hole with
// sky behind it, not a seam. With them both sides average the same four cells
// and agree exactly, without either side needing to know the other exists.
//
// One cell is enough, and that is not a coincidence: both things the mesher
// derives at a corner -- the averaged height and the gradient the normal comes
// from -- read the 2x2 of cells touching that corner and nothing wider.
inline constexpr int kApronColumns = 1;
inline constexpr int kPaddedRegionColumns = kRegionColumns + 2 * kApronColumns;
inline constexpr int kPaddedColumnsPerRegion =
    kPaddedRegionColumns * kPaddedRegionColumns;

// Index of a cell in the padded grid. Cell coordinates run from -kApronColumns
// to kRegionColumns inclusive: [0, kRegionColumns) is the region proper, and
// the values either side of that are the apron.
inline constexpr int paddedColumnIndex(int columnX, int columnZ) noexcept {
  return (columnZ + kApronColumns) * kPaddedRegionColumns
      + (columnX + kApronColumns);
}

// Java packs each column into this many ints before handing over the region.
//
// Six rather than three because a column carries two surfaces: the ground, and
// the canopy standing above it. They cannot be folded into one, and the attempt
// is what made distant forests wrong -- a cell drawn at treetop height is a cell
// whose walls run all the way down to its neighbours, so a lone tree renders as
// a solid pillar of leaves and a wood renders as a plateau. The canopy needs its
// own top, its own bottom and its own appearance to be drawn as what it is: a
// slab floating over ground that is still visible underneath it.
// A seventh carries the trunk: whether one stood here, and what a log looks
// like. Its extent is not sent, because both ends of it are already here -- a
// trunk runs from the ground to the underside of the leaves.
inline constexpr int kWordsPerColumn = 7;

inline constexpr std::int16_t kNoSurface = -1;

// b1.7.3 worlds are 128 blocks tall, so every height fits in a signed 16-bit
// field with room for the -1 "nothing here" sentinel.
inline constexpr int kWorldHeight = 128;

// LOD surfaces sit below true terrain height so that a region and the
// full-detail chunks covering it can be drawn at the same time without fighting.
// The handover relies on that: LOD stays up until real chunk geometry exists,
// and during the overlap the real surface simply occludes the LOD.
//
// Half a block was enough while every cell was a flat top at its own sampled
// height, because that height was the highest block in the cell and the surface
// could never rise above it. The heightfield broke that assumption. A corner
// takes the average of the cells meeting at it, so a cell in a one-cell dip is
// pulled up toward its higher neighbours -- by as much as half the cliff
// threshold before the corner splits instead, which is two blocks at step 2.
// A sink of half a block no longer covers it, and the LOD then pokes through
// real terrain in the handover band rather than hiding under it.
//
// Sized against step 2, because that is the only level the handover band ever
// reaches; the coarser levels inherit a deeper sink than they need and do not
// care, being drawn hundreds of blocks away where two blocks is far less than
// a pixel.
inline constexpr float kSurfaceSinkBlocks = 2.0f;

// Detail levels the field can draw, finest first. Each level's regions cover
// twice the ground of the previous one's, which is what lets a finer level's
// footprint be an exact whole number of coarser regions -- the property the
// scheduler relies on to keep two detail levels off the same ground.
//
// This must agree with RemixLodBridge.LOD_RING_STEPS. It is not checked at
// runtime and it does not fail loudly if it drifts: a step Java submits that is
// missing here gets lodStepIndex() == -1, which costs the region its own
// material and its atlas UV layout rather than dropping it, so the symptom is a
// ring drawn at the wrong texture scale rather than a ring that is absent.
inline constexpr int kLodStepCount = 5;
inline constexpr int kLodSteps[kLodStepCount] = {2, 4, 8, 16, 32};

// Index of a step in kLodSteps, or -1 when the step is not one the field uses.
inline int lodStepIndex(int step) noexcept {
  for (int index = 0; index < kLodStepCount; ++index) {
    if (kLodSteps[index] == step) {
      return index;
    }
  }
  return -1;
}

// Depth of the curtain every region hangs from its outer edge, where there is
// no neighbouring column to measure a drop against.
//
// This is what closes the seam between two detail levels: neighbouring regions
// at different steps quantise height differently, so their surfaces meet at a
// step rather than flush, and the higher side's curtain is what fills the gap.
// The coarser the level, the further two neighbouring surfaces can disagree, so
// the curtain grows with the step -- while staying deep enough at fine levels
// to cover ordinary cliffs.
inline float edgeSkirtDepthBlocks(int step) noexcept {
  const float depth = 2.0f * static_cast<float>(step);
  return depth < 16.0f ? 16.0f : depth;
}

// Height difference between neighbouring cells past which the ground is a cliff
// to be drawn sheer, rather than a slope to be smoothed across.
//
// Minecraft terrain is full of genuine verticals -- a mesa rim, a mountain
// face, the lip of a ravine -- and rounding those into 45-degree ramps is a
// worse error than the staircase the heightfield exists to remove. Past this
// difference the cells at a corner keep their own heights and a wall carries
// the drop between them.
//
// The threshold grows with the step because a coarse cell already spans ground
// a fine one would have resolved as a slope: 16 blocks of rise across a step-16
// cell is a 45-degree hillside, while the same rise across a step-2 cell is a
// wall. It never falls below four blocks, so an ordinary hummock in the near
// rings is smoothed rather than walled.
//
// It is deliberately left uncapped at step 32, where it reaches a third of the
// world's height and might look like a rule that has stopped meaning anything.
// It has not: "steeper than 45 degrees" is the same statement at every scale,
// and the reason no wall is emitted out there is not the threshold but the
// ground. A step-32 cell is the mean of 1024 block columns, and measured over a
// real save and over synthetic terrain with twice its relief, neighbouring
// step-32 cells never differ by more than 25 blocks. Step 16 is already in that
// regime today -- its threshold fires on 0.1% of edges in a real save and on
// none in synthetic -- so the outermost ring is not a new regime, and capping
// the rule would only introduce sheer walls where the ground is a 38-degree
// hillside seen from two kilometres away.
inline float cliffThresholdBlocks(int step) noexcept {
  const float threshold = static_cast<float>(step);
  return threshold < 4.0f ? 4.0f : threshold;
}

// Most a curtain quad's texture may be squashed vertically before the curtain
// is split into another quad. The curtain is buried seam filler -- see
// buildRegionSurface -- so it trades exact texture scale for quad count, but
// not without limit: at a ring boundary several blocks of it can show.
inline constexpr float kMaxCurtainSquash = 4.0f;

// Quads one edge column's curtain is drawn with. Drawing it at block scale, as
// an ordinary skirt is, would cost one quad per cell width of depth: eight per
// edge column at step 2, where the curtain then outweighs every top face in
// the region put together.
inline int curtainQuadCount(int step) noexcept {
  const float depth = edgeSkirtDepthBlocks(step);
  const float perQuad = static_cast<float>(step) * kMaxCurtainSquash;
  int quads = 1;
  while (static_cast<float>(quads) * perQuad < depth) {
    ++quads;
  }
  return quads;
}

// One downsampled surface sample. A 2048-block field stays a few megabytes.
struct LodColumn {
  std::int16_t topY {kNoSurface};       // highest opaque block in the cell
  std::int16_t waterY {kNoSurface};     // water surface in the cell
  std::int16_t topTile {0};             // atlas tile for the upward face
  std::int16_t sideTile {0};            // atlas tile for skirts
  std::uint32_t color {0xFFFFFFFFu};    // biome / block tint, ARGB

  // The canopy slab, or kNoSurface when the cell holds no foliage worth
  // drawing. Java applies the coverage threshold and merges the canopy into the
  // ground at the coarse levels, so by the time a column arrives here a canopy
  // is either present and worth its own geometry or absent entirely.
  std::int16_t canopyTopY {kNoSurface};
  std::int16_t canopyBottomY {kNoSurface};
  std::int16_t canopyTopTile {0};
  std::int16_t canopySideTile {0};
  std::uint32_t canopyColor {0xFFFFFFFFu};

  // A tree stood in this cell on a visible trunk. Logs are neither ground nor
  // canopy -- the scan steps over them so the foliage keeps an underside and the
  // ground beneath stays real -- so without this they were drawn nowhere and
  // distant woods floated.
  bool hasTrunk {false};
  std::int16_t trunkTile {0};

  bool hasSurface(int renderPass) const noexcept {
    return renderPass == 0 ? topY >= 0 : waterY >= 0;
  }

  bool hasCanopy() const noexcept {
    return canopyTopY >= 0 && canopyBottomY >= 0 && canopyBottomY <= canopyTopY;
  }

  // Height of the surface this render pass draws, as a face position.
  float surfaceHeight(int renderPass) const noexcept {
    const float top = renderPass == 0
        ? static_cast<float>(topY)
        : static_cast<float>(waterY);
    return top + 1.0f - kSurfaceSinkBlocks;
  }

  // Top and underside of the canopy slab, as face positions.
  //
  // Drawn slightly inside the leaves it stands for, both faces pulled toward
  // each other by the same sink the ground uses -- and for the same reason. A
  // LOD region and the chunks covering it are drawn together during handover,
  // and a path tracer does not merely z-fight over two surfaces in the same
  // place, it lights through them. The ground avoids that by sitting below true
  // terrain; the canopy has to sit inside the real foliage, or a wood in the
  // handover band renders with its own leaves twice.
  float canopyTop() const noexcept {
    return static_cast<float>(canopyTopY) + 1.0f - kSurfaceSinkBlocks;
  }

  // The underside gives up its share of the sink rather than let a thin canopy
  // collapse. A single layer of leaves is one block thick, and taking half a
  // block off each face would leave a slab of zero height -- its top and bottom
  // in the same place, which is precisely the coincident-surface problem the
  // sink exists to avoid.
  float canopyBottom() const noexcept {
    const float sunk = static_cast<float>(canopyBottomY) + kSurfaceSinkBlocks;
    return sunk < canopyTop() ? sunk : static_cast<float>(canopyBottomY);
  }
};

struct RegionKey {
  int originX {0};     // block coordinate of the region's min corner
  int originZ {0};
  int step {0};        // world blocks covered by one column (4, 8, 16, ...)
  int renderPass {0};  // 0 opaque, 1 translucent — blending is per instance

  bool operator==(const RegionKey& other) const noexcept {
    return originX == other.originX
        && originZ == other.originZ
        && step == other.step
        && renderPass == other.renderPass;
  }
};

struct RegionKeyHash {
  std::size_t operator()(const RegionKey& key) const noexcept {
    std::uint64_t hash = 1469598103934665603ull;
    const auto mix = [&hash](std::uint64_t value) {
      hash ^= value;
      hash *= 1099511628211ull;
    };
    mix(static_cast<std::uint32_t>(key.originX));
    mix(static_cast<std::uint32_t>(key.originZ));
    mix(static_cast<std::uint32_t>(key.step));
    mix(static_cast<std::uint32_t>(key.renderPass));
    return static_cast<std::size_t>(hash);
  }
};

struct RegionMeshData {
  remixapi_MeshHandle meshHandle {nullptr};
  std::uint64_t meshHash {0};
  std::uint64_t fingerprint {0};
  // Triangles this region put in the acceleration structure. Kept only so the
  // field can report what it actually costs: region counts say nothing about
  // it, because one region can hold ten times the geometry of its neighbour.
  std::uint32_t triangleCount {0};
  bool hidden {false};
};

// Snapshot entry consumed by the present thread.
struct LodRenderInstance {
  RegionKey key {};
  remixapi_MeshHandle meshHandle {nullptr};
};

inline int regionSizeBlocks(int step) noexcept {
  return kRegionColumns * step;
}

// Snaps a world coordinate down to the origin of the region containing it.
inline int snapToRegionOrigin(int blockCoordinate, int step) noexcept {
  const int size = regionSizeBlocks(step);
  const int snapped = (blockCoordinate / size) * size;
  return blockCoordinate < 0 && snapped != blockCoordinate ? snapped - size : snapped;
}

}  // namespace mcrtx::lod
