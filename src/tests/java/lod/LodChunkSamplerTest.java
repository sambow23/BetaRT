import mcrtx.lod.format.LodBlocks;
import mcrtx.lod.format.LodCell;
import mcrtx.lod.format.LodChunkSampler;
import mcrtx.lod.format.LodTileKey;

/**
 * Covers turning raw chunk arrays into cells.
 *
 * <p>The forest cases are the point. Distant woodland rendered as a plateau of
 * leaves on sheer leaf-textured walls because the old scan stopped at the first
 * solid block, and in a forest that block is the canopy. These tests pin the
 * behaviour that replaces it: leaves become a canopy run, trunks are stepped
 * over, and the ground underneath is still found.
 */
public final class LodChunkSamplerTest {
    private static final int STONE = 1;
    private static final int GRASS = 2;
    private static final int DIRT = 3;

    private static final int GROUND_Y = 64;

    public static void main(String[] args) {
        groundIsFoundBeneathACanopy();
        trunkDoesNotBoundTheCanopySlab();
        trunkIsRecordedOnlyUnderACanopy();
        waterIsRecordedWithTheBedBeneathIt();
        snowFlagsTheGroundRatherThanBecomingIt();
        nonSurfaceBlocksAreSteppedOver();
        emptyColumnIsKnownButHasNoSurfaces();
        heightMapMarginDoesNotClipTallTrees();
        climateIsOptional();
        chunkFoldsIntoTheRightCellsOfItsTile();
        chunkFoldProducesFractionalCoverage();
        tileForChunkHandlesNegativeChunks();

        System.out.println("LodChunkSamplerTest: all checks passed");
    }

    // ---- world building ------------------------------------------------

    private static int index(int x, int y, int z) {
        return (x << 11) | (z << 7) | y;
    }

    /** A chunk of stone, dirt and grass with a flat surface at {@link #GROUND_Y}. */
    private static byte[] flatChunk() {
        byte[] blocks = new byte[16 * 16 * 128];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < GROUND_Y - 3; y++) {
                    blocks[index(x, y, z)] = (byte) STONE;
                }
                for (int y = GROUND_Y - 3; y < GROUND_Y; y++) {
                    blocks[index(x, y, z)] = (byte) DIRT;
                }
                blocks[index(x, GROUND_Y, z)] = (byte) GRASS;
            }
        }
        return blocks;
    }

    private static byte[] sample(byte[] blocks, byte[] data, byte[] heightMap) {
        byte[] cells = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];
        LodChunkSampler.sampleChunk(blocks, data, heightMap, 0, 0, null, cells);
        return cells;
    }

    private static int columnOffset(int localX, int localZ) {
        return (localZ * 16 + localX) * LodCell.BYTES;
    }

    // ---- column scanning -----------------------------------------------

    private static void groundIsFoundBeneathACanopy() {
        byte[] blocks = flatChunk();
        // An ordinary oak: trunk to 68, leaves above it.
        for (int y = GROUND_Y + 1; y <= 68; y++) {
            blocks[index(8, y, 8)] = (byte) LodBlocks.LOG;
        }
        for (int y = 69; y <= 70; y++) {
            blocks[index(8, y, 8)] = (byte) LodBlocks.LEAVES;
        }

        byte[] cells = sample(blocks, null, null);
        int offset = columnOffset(8, 8);

        // The whole point: the surface is the ground, not the treetop.
        require(LodCell.groundY(cells, offset) == GROUND_Y,
                "ground under a tree, got " + LodCell.groundY(cells, offset));
        require(LodCell.groundBlock(cells, offset) == GRASS, "ground block under a tree");
        require(LodCell.canopyTopY(cells, offset) == 70, "canopy top");
        require(LodCell.canopyCoverage(cells, offset) == LodCell.MAX_COVERAGE,
                "a block-resolution column under leaves is fully covered");
        require(LodCell.canopyKind(cells, offset) == LodCell.KIND_OAK, "oak by default metadata");

        // A column beside the tree is plain ground with no canopy at all.
        int bare = columnOffset(0, 8);
        require(LodCell.groundY(cells, bare) == GROUND_Y, "bare ground");
        require(LodCell.canopyTopY(cells, bare) == LodCell.NO_HEIGHT, "no canopy beside the tree");
        require(LodCell.canopyCoverage(cells, bare) == 0, "no coverage beside the tree");
    }

    private static void trunkDoesNotBoundTheCanopySlab() {
        byte[] blocks = flatChunk();
        for (int y = GROUND_Y + 1; y <= 68; y++) {
            blocks[index(4, y, 4)] = (byte) LodBlocks.LOG;
        }
        for (int y = 69; y <= 71; y++) {
            blocks[index(4, y, 4)] = (byte) LodBlocks.LEAVES;
        }

        byte[] cells = sample(blocks, null, null);
        int offset = columnOffset(4, 4);

        // Letting the trunk bound the slab would run the canopy from the
        // treetops down to the dirt, and a forest would render as a solid block.
        require(LodCell.canopyTopY(cells, offset) == 71, "canopy top is the leaf mass");
        require(LodCell.canopyBottomY(cells, offset) == 69,
                "canopy bottom is the lowest leaf, not the trunk base, got "
                        + LodCell.canopyBottomY(cells, offset));

        // A trunk with no leaves above it is not canopy, and is still stepped
        // over rather than becoming the surface.
        byte[] trunkOnly = flatChunk();
        for (int y = GROUND_Y + 1; y <= 68; y++) {
            trunkOnly[index(2, y, 2)] = (byte) LodBlocks.LOG;
        }
        byte[] trunkCells = sample(trunkOnly, null, null);
        int trunkOffset = columnOffset(2, 2);
        require(LodCell.groundY(trunkCells, trunkOffset) == GROUND_Y, "ground under a bare trunk");
        require(LodCell.canopyCoverage(trunkCells, trunkOffset) == 0, "a bare trunk is not coverage");
    }

    private static void trunkIsRecordedOnlyUnderACanopy() {
        byte[] blocks = flatChunk();
        // A whole tree: trunk, then leaves above it.
        for (int y = GROUND_Y + 1; y <= 68; y++) {
            blocks[index(6, y, 6)] = (byte) LodBlocks.LOG;
        }
        for (int y = 69; y <= 70; y++) {
            blocks[index(6, y, 6)] = (byte) LodBlocks.LEAVES;
        }
        // A bare post: logs with nothing above. Part of a building, most likely.
        for (int y = GROUND_Y + 1; y <= 68; y++) {
            blocks[index(12, y, 12)] = (byte) LodBlocks.LOG;
        }
        // Floating leaves with no trunk under them.
        for (int y = 69; y <= 70; y++) {
            blocks[index(14, y, 14)] = (byte) LodBlocks.LEAVES;
        }

        byte[] cells = sample(blocks, null, null);

        // The canopy is drawn as a slab standing above the ground, so something
        // has to hold it up; without this the woods floated.
        require(LodCell.hasTrunk(cells, columnOffset(6, 6)), "a tree has a trunk");
        require(LodCell.canopyTopY(cells, columnOffset(6, 6)) == 70, "and still a canopy");
        require(LodCell.groundY(cells, columnOffset(6, 6)) == GROUND_Y, "and still real ground");

        // A log with no leaves over it would otherwise scatter stray posts
        // across the landscape.
        require(!LodCell.hasTrunk(cells, columnOffset(12, 12)), "a bare post is not a trunk");
        require(!LodCell.hasTrunk(cells, columnOffset(14, 14)), "leaves alone are not a trunk");
        require(!LodCell.hasTrunk(cells, columnOffset(0, 0)), "bare ground has no trunk");
    }

    private static void waterIsRecordedWithTheBedBeneathIt() {
        byte[] blocks = flatChunk();
        // Carve a pool four deep and fill it.
        for (int y = GROUND_Y - 3; y <= GROUND_Y; y++) {
            blocks[index(1, y, 1)] = (byte) LodBlocks.WATER_STILL;
        }

        byte[] cells = sample(blocks, null, null);
        int offset = columnOffset(1, 1);
        require(LodCell.waterY(cells, offset) == GROUND_Y, "water surface is the highest water block");
        require(LodCell.groundY(cells, offset) == GROUND_Y - 4,
                "the bed beneath the water is still found, got " + LodCell.groundY(cells, offset));
    }

    private static void snowFlagsTheGroundRatherThanBecomingIt() {
        byte[] blocks = flatChunk();
        blocks[index(3, GROUND_Y + 1, 3)] = (byte) LodBlocks.SNOW_LAYER;

        byte[] cells = sample(blocks, null, null);
        int offset = columnOffset(3, 3);
        require(LodCell.groundY(cells, offset) == GROUND_Y, "snow does not raise the surface");
        require(LodCell.groundBlock(cells, offset) == GRASS, "the block under snow is the surface");
        require(LodCell.hasSnow(cells, offset), "snow is recorded as a flag");
        require(!LodCell.hasSnow(cells, columnOffset(0, 0)), "bare ground is not snowy");
    }

    private static void nonSurfaceBlocksAreSteppedOver() {
        byte[] blocks = flatChunk();
        blocks[index(5, GROUND_Y + 1, 5)] = (byte) 37;  // flower
        blocks[index(6, GROUND_Y + 1, 6)] = (byte) 50;  // torch
        blocks[index(7, GROUND_Y + 1, 7)] = (byte) 85;  // fence

        byte[] cells = sample(blocks, null, null);
        require(LodCell.groundY(cells, columnOffset(5, 5)) == GROUND_Y, "a flower is not the surface");
        require(LodCell.groundY(cells, columnOffset(6, 6)) == GROUND_Y, "a torch is not the surface");
        require(LodCell.groundY(cells, columnOffset(7, 7)) == GROUND_Y, "a fence is not the surface");

        // Lava is deliberately not excluded: at LOD range it reads as a surface.
        byte[] lava = flatChunk();
        lava[index(9, GROUND_Y + 1, 9)] = (byte) LodBlocks.LAVA_STILL;
        byte[] lavaCells = sample(lava, null, null);
        require(LodCell.groundY(lavaCells, columnOffset(9, 9)) == GROUND_Y + 1, "lava is a surface");
    }

    private static void emptyColumnIsKnownButHasNoSurfaces() {
        byte[] blocks = new byte[16 * 16 * 128];
        byte[] cells = sample(blocks, null, null);
        int offset = columnOffset(0, 0);

        // Reading an empty column is knowledge, not absence of it: the mesher
        // must be able to tell "nothing here" from "not looked at yet".
        require(LodCell.isKnown(cells, offset), "an air column is known");
        require(LodCell.groundY(cells, offset) == LodCell.NO_HEIGHT, "no ground");
        require(LodCell.waterY(cells, offset) == LodCell.NO_HEIGHT, "no water");
        require(LodCell.canopyTopY(cells, offset) == LodCell.NO_HEIGHT, "no canopy");
    }

    private static void heightMapMarginDoesNotClipTallTrees() {
        byte[] blocks = flatChunk();
        for (int y = 69; y <= 74; y++) {
            blocks[index(10, y, 10)] = (byte) LodBlocks.LEAVES;
        }

        // A heightmap that has not caught up with the tree: it still names the
        // flat ground. The scan starts above it by a margin for exactly this.
        byte[] staleHeightMap = new byte[256];
        for (int i = 0; i < staleHeightMap.length; i++) {
            staleHeightMap[i] = (byte) (GROUND_Y + 1);
        }

        byte[] cells = sample(blocks, null, staleHeightMap);
        int offset = columnOffset(10, 10);
        require(LodCell.canopyTopY(cells, offset) == 74,
                "the margin clears a stale heightmap, got " + LodCell.canopyTopY(cells, offset));
        require(LodCell.groundY(cells, offset) == GROUND_Y, "ground still found");
    }

    private static void climateIsOptional() {
        byte[] blocks = flatChunk();
        byte[] withoutClimate = sample(blocks, null, null);
        require(!LodCell.hasTint(withoutClimate, 0), "no tint without a climate source");

        byte[] cells = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];
        LodChunkSampler.sampleChunk(blocks, null, null, 2, 3, new LodChunkSampler.ClimateSource() {
            public int climateAt(int blockX, int blockZ) {
                // Confirms the sampler hands over world coordinates, not chunk-local ones.
                require(blockX >= 32 && blockX < 48, "world X passed to climate, got " + blockX);
                require(blockZ >= 48 && blockZ < 64, "world Z passed to climate, got " + blockZ);
                return (200 << 8) | 100;
            }
        }, cells);

        require(LodCell.hasTint(cells, 0), "tint flag set");
        require(LodCell.temperature(cells, 0) == 200, "temperature carried");
        require(LodCell.humidity(cells, 0) == 100, "humidity carried");

        // A source that cannot answer leaves the cell untinted for a later
        // producer to fill, rather than writing a wrong colour.
        byte[] unknown = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];
        LodChunkSampler.sampleChunk(blocks, null, null, 0, 0, new LodChunkSampler.ClimateSource() {
            public int climateAt(int blockX, int blockZ) {
                return LodChunkSampler.CLIMATE_UNKNOWN;
            }
        }, unknown);
        require(!LodCell.hasTint(unknown, 0), "unknown climate leaves the cell untinted");
    }

    // ---- folding into tiles --------------------------------------------

    private static void chunkFoldsIntoTheRightCellsOfItsTile() {
        byte[] tile = new byte[LodTileKey.TILE_BYTES];
        for (int i = 0; i < LodTileKey.CELLS_PER_TILE; i++) {
            LodCell.clear(tile, i * LodCell.BYTES);
        }
        byte[] scratch = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];

        // Chunk (1,2) sits in tile L1(0,0), which spans chunks 0..3 on each axis,
        // so it fills cells 8..15 in X and 16..23 in Z.
        LodChunkSampler.sampleChunkIntoTile(flatChunk(), null, null, 1, 2, null, tile, scratch);

        require(LodCell.isKnown(tile, LodTileKey.cellOffset(8, 16)), "first cell of the chunk written");
        require(LodCell.isKnown(tile, LodTileKey.cellOffset(15, 23)), "last cell of the chunk written");
        require(LodCell.groundY(tile, LodTileKey.cellOffset(8, 16)) == GROUND_Y, "folded ground height");

        // Everything outside the chunk's own 8x8 is untouched, so sampling one
        // chunk never blanks a neighbour's cells in the same tile.
        require(!LodCell.isKnown(tile, LodTileKey.cellOffset(7, 16)), "cell before the chunk untouched");
        require(!LodCell.isKnown(tile, LodTileKey.cellOffset(16, 16)), "cell after the chunk untouched");
        require(!LodCell.isKnown(tile, LodTileKey.cellOffset(8, 15)), "cell above the chunk untouched");
    }

    private static void chunkFoldProducesFractionalCoverage() {
        byte[] blocks = flatChunk();
        // Two of the four columns under one canonical cell carry leaves.
        for (int y = 69; y <= 70; y++) {
            blocks[index(8, y, 8)] = (byte) LodBlocks.LEAVES;
            blocks[index(9, y, 8)] = (byte) LodBlocks.LEAVES;
        }

        byte[] tile = new byte[LodTileKey.TILE_BYTES];
        byte[] scratch = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];
        LodChunkSampler.sampleChunkIntoTile(blocks, null, null, 0, 0, null, tile, scratch);

        // Columns (8,8),(9,8),(8,9),(9,9) fold into cell (4,4). Half of them are
        // forested, so the cell reports half coverage -- the fractional value
        // that lets a coarse mesher raise ground instead of choosing between a
        // full canopy plateau and nothing at all.
        int offset = LodTileKey.cellOffset(4, 4);
        require(LodCell.canopyCoverage(tile, offset) == 8,
                "half-covered cell, got " + LodCell.canopyCoverage(tile, offset));
        require(LodCell.canopyTopY(tile, offset) == 70, "canopy top survives the fold");
        require(LodCell.groundY(tile, offset) == GROUND_Y, "ground survives the fold");
    }

    private static void tileForChunkHandlesNegativeChunks() {
        require(LodChunkSampler.CHUNKS_PER_TILE_AXIS == 4, "a canonical tile is four chunks across");

        LodTileKey positive = LodChunkSampler.tileForChunk(5, 9);
        require(positive.level() == LodTileKey.CANONICAL_LEVEL, "canonical level");
        require(positive.tileX() == 1 && positive.tileZ() == 2, "positive chunk maps to its tile");

        // Truncating division would put chunk -1 in tile 0 alongside chunk 0,
        // and the two would overwrite each other's cells.
        LodTileKey negative = LodChunkSampler.tileForChunk(-1, -1);
        require(negative.tileX() == -1 && negative.tileZ() == -1, "negative chunk floors to its own tile");
        require(negative.originX() == -64, "negative tile origin");

        byte[] tile = new byte[LodTileKey.TILE_BYTES];
        byte[] scratch = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];
        LodChunkSampler.sampleChunkIntoTile(flatChunk(), null, null, -1, -1, null, tile, scratch);
        // Chunk -1 is the last of its tile, so it occupies the final 8x8 corner.
        require(LodCell.isKnown(tile, LodTileKey.cellOffset(31, 31)), "last cell of the tile written");
        require(!LodCell.isKnown(tile, LodTileKey.cellOffset(23, 23)), "cells before it untouched");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
