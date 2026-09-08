package mcrtx.lod.format;

/**
 * Turns one chunk's raw block arrays into LOD cells.
 *
 * <p>This is <b>the</b> sampler. A client reading a loaded chunk, a worker
 * reading a region file, a server reading its own world and the offline bake
 * tool all call it with the same three arrays, so all four produce identical
 * cells for identical ground. Working on raw arrays rather than through chunk or
 * world objects is what makes that possible: the arrays are the one thing every
 * producer has, and the accessors are the thing none of them share.
 *
 * <p>A chunk is 16 blocks square and a canonical cell covers two, so a chunk
 * fills an 8x8 corner of a level-1 tile, and a level-1 tile is 4x4 chunks. The
 * sampler reads at block resolution -- 256 columns -- and folds each 2x2 down
 * through {@link LodReducer}, rather than picking one column in four. Sampling
 * on a lattice was worth it when extraction ran on the client thread each frame;
 * here it is not, and reading every column is what stops a two-block cell from
 * inheriting whichever corner the lattice happened to land on.
 *
 * <p><b>The column scan is where trees stop being terrain.</b> Scanning down for
 * the first solid block finds the canopy in any forest, which is why distant
 * woodland rendered as a plateau of leaves standing on sheer leaf-textured
 * walls. Here leaves accumulate into a canopy run, logs are stepped over, and
 * the scan continues to the real ground underneath. Both are recorded, and the
 * mesher decides what to do with them.
 */
public final class LodChunkSampler {
    /** Blocks along one edge of a chunk. */
    public static final int CHUNK_BLOCKS = 16;
    public static final int COLUMNS_PER_CHUNK = CHUNK_BLOCKS * CHUNK_BLOCKS;

    /** Cells a chunk fills at the canonical level, along one edge. */
    public static final int CELLS_PER_CHUNK_AXIS = CHUNK_BLOCKS / 2;

    /** Chunks along one edge of a canonical-level tile. */
    public static final int CHUNKS_PER_TILE_AXIS =
            LodTileKey.sizeBlocks(LodTileKey.CANONICAL_LEVEL) / CHUNK_BLOCKS;

    /** Bytes of block-resolution scratch one chunk needs. */
    public static final int BLOCK_CELLS_BYTES = COLUMNS_PER_CHUNK * LodCell.BYTES;

    public static final int WORLD_HEIGHT = 128;

    /**
     * Blocks searched above the heightmap before the scan starts.
     *
     * <p>The heightmap records the first block that stops skylight, which is at
     * or above the canopy for every block that matters -- but it is maintained
     * by the game rather than by us, it can lag an edit, and a producer reading a
     * region file gets whatever was written to disk. Starting a little higher
     * costs a handful of array reads per column against silently clipping the
     * top of a tree, so the margin is generous rather than tight.
     */
    private static final int HEIGHTMAP_MARGIN = 16;

    /**
     * Supplies biome climate for a column.
     *
     * <p>A callback rather than a dependency, because the four producers reach
     * biome data by four different routes -- the client through the world it is
     * already holding, a worker through a private generator, a server through
     * its own -- and none of those belong in a package that must compile without
     * the game. A producer with no biome source returns
     * {@link #CLIMATE_UNKNOWN}, and the cell is written without
     * {@link LodCell#FLAG_TINT} so a later producer can fill it in.
     */
    public interface ClimateSource {
        /**
         * @return temperature in the high byte and humidity in the low byte,
         *     each 0..255, or {@link #CLIMATE_UNKNOWN}.
         */
        int climateAt(int blockX, int blockZ);
    }

    /** Returned by a {@link ClimateSource} that cannot answer. */
    public static final int CLIMATE_UNKNOWN = -1;

    private LodChunkSampler() {
    }

    /**
     * Samples a chunk at block resolution.
     *
     * @param blocks Beta's 32,768-byte block array, indexed {@code x<<11|z<<7|y}
     * @param data the matching metadata nibble array, or null when unavailable
     * @param heightMap 256 bytes of skylight heights, or null to scan from the top
     * @param outCells {@link #BLOCK_CELLS_BYTES} of output, one cell per column
     */
    public static void sampleChunk(
            byte[] blocks,
            byte[] data,
            byte[] heightMap,
            int chunkX,
            int chunkZ,
            ClimateSource climate,
            byte[] outCells) {
        if (blocks == null || blocks.length < CHUNK_BLOCKS * CHUNK_BLOCKS * WORLD_HEIGHT) {
            throw new IllegalArgumentException("block array is not a whole chunk");
        }
        if (outCells == null || outCells.length != BLOCK_CELLS_BYTES) {
            throw new IllegalArgumentException("output must hold one cell per column");
        }

        int baseBlockX = chunkX * CHUNK_BLOCKS;
        int baseBlockZ = chunkZ * CHUNK_BLOCKS;

        for (int localZ = 0; localZ < CHUNK_BLOCKS; localZ++) {
            for (int localX = 0; localX < CHUNK_BLOCKS; localX++) {
                int outOffset = (localZ * CHUNK_BLOCKS + localX) * LodCell.BYTES;
                sampleColumn(
                        blocks,
                        data,
                        heightMap,
                        localX,
                        localZ,
                        baseBlockX + localX,
                        baseBlockZ + localZ,
                        climate,
                        outCells,
                        outOffset);
            }
        }
    }

    /**
     * Samples a chunk and folds it into the canonical-level tile that contains
     * it, writing the 8x8 cells the chunk covers and leaving the rest alone.
     *
     * @param tile a whole canonical-level tile, {@link LodTileKey#TILE_BYTES}
     * @param scratch {@link #BLOCK_CELLS_BYTES} of reusable scratch
     */
    public static void sampleChunkIntoTile(
            byte[] blocks,
            byte[] data,
            byte[] heightMap,
            int chunkX,
            int chunkZ,
            ClimateSource climate,
            byte[] tile,
            byte[] scratch) {
        if (tile == null || tile.length != LodTileKey.TILE_BYTES) {
            throw new IllegalArgumentException("tile buffer must be a whole tile");
        }

        sampleChunk(blocks, data, heightMap, chunkX, chunkZ, climate, scratch);

        // Where this chunk sits inside its tile. Floor arithmetic, because a
        // chunk at a negative coordinate still occupies a non-negative corner of
        // whichever tile contains it.
        int cellOriginX = Math.floorMod(chunkX, CHUNKS_PER_TILE_AXIS) * CELLS_PER_CHUNK_AXIS;
        int cellOriginZ = Math.floorMod(chunkZ, CHUNKS_PER_TILE_AXIS) * CELLS_PER_CHUNK_AXIS;

        byte[][] sources = new byte[LodReducer.CHILDREN_PER_CELL][];
        int[] offsets = new int[LodReducer.CHILDREN_PER_CELL];
        for (int i = 0; i < sources.length; i++) {
            sources[i] = scratch;
        }

        for (int cellZ = 0; cellZ < CELLS_PER_CHUNK_AXIS; cellZ++) {
            for (int cellX = 0; cellX < CELLS_PER_CHUNK_AXIS; cellX++) {
                int columnX = cellX << 1;
                int columnZ = cellZ << 1;
                offsets[0] = columnOffset(columnX, columnZ);
                offsets[1] = columnOffset(columnX + 1, columnZ);
                offsets[2] = columnOffset(columnX, columnZ + 1);
                offsets[3] = columnOffset(columnX + 1, columnZ + 1);

                LodReducer.reduceCell(
                        sources,
                        offsets,
                        LodReducer.CHILDREN_PER_CELL,
                        tile,
                        LodTileKey.cellOffset(cellOriginX + cellX, cellOriginZ + cellZ));
            }
        }
    }

    /** The canonical-level tile a chunk belongs to. */
    public static LodTileKey tileForChunk(int chunkX, int chunkZ) {
        return new LodTileKey(
                LodTileKey.CANONICAL_LEVEL,
                Math.floorDiv(chunkX, CHUNKS_PER_TILE_AXIS),
                Math.floorDiv(chunkZ, CHUNKS_PER_TILE_AXIS));
    }

    private static int columnOffset(int localX, int localZ) {
        return (localZ * CHUNK_BLOCKS + localX) * LodCell.BYTES;
    }

    /**
     * Walks one column from the sky down, recording water, canopy and ground.
     *
     * <p>The scan stops at the first block that counts as ground; everything
     * above it has already been folded into the other fields. Order matters:
     * water is recorded and stepped over so a lake bed is still found beneath it,
     * canopy is accumulated and stepped over, snow sets a flag and is stepped
     * over because the block under it is the real surface, and the excluded
     * table is stepped over because none of it is a full cube.
     */
    private static void sampleColumn(
            byte[] blocks,
            byte[] data,
            byte[] heightMap,
            int localX,
            int localZ,
            int blockX,
            int blockZ,
            ClimateSource climate,
            byte[] outCells,
            int outOffset) {
        int columnBase = (localX << 11) | (localZ << 7);

        int startY = WORLD_HEIGHT - 1;
        if (heightMap != null && heightMap.length >= COLUMNS_PER_CHUNK) {
            int mapped = (heightMap[(localZ << 4) | localX] & 0xFF) + HEIGHTMAP_MARGIN;
            if (mapped < startY) {
                startY = mapped;
            }
        }

        int groundY = LodCell.NO_HEIGHT;
        int groundBlock = LodBlocks.AIR;
        int groundMeta = 0;
        int waterY = LodCell.NO_HEIGHT;
        int canopyTopY = LodCell.NO_HEIGHT;
        int canopyBottomY = LodCell.NO_HEIGHT;
        int canopyKind = LodCell.KIND_OAK;
        boolean snow = false;
        boolean trunk = false;

        for (int y = startY; y >= 0; y--) {
            int blockId = blocks[columnBase | y] & 0xFF;
            if (blockId == LodBlocks.AIR) {
                continue;
            }

            if (LodBlocks.isWater(blockId)) {
                if (y > waterY) {
                    waterY = y;
                }
                continue;
            }

            if (LodBlocks.isCanopy(blockId)) {
                // Only the leaf mass bounds the canopy slab. A trunk is stepped
                // over like any other non-ground block -- but it is remembered,
                // because a canopy with nothing under it reads as floating.
                if (LodBlocks.isCanopyMass(blockId)) {
                    if (canopyTopY == LodCell.NO_HEIGHT) {
                        canopyTopY = y;
                        canopyKind = LodBlocks.canopyKind(metadataAt(data, columnBase | y));
                    }
                    canopyBottomY = y;
                } else {
                    trunk = true;
                }
                continue;
            }

            if (blockId == LodBlocks.SNOW_LAYER) {
                snow = true;
                continue;
            }

            if (LodBlocks.isNonSurface(blockId)) {
                continue;
            }

            groundY = y;
            groundBlock = blockId;
            groundMeta = metadataAt(data, columnBase | y);
            break;
        }

        int flags = LodCell.FLAG_KNOWN;
        if (snow) {
            flags |= LodCell.FLAG_SNOW;
        }
        // Only worth recording where there is a canopy to hold up. A log with no
        // leaves above it is a fence post or part of a building, and drawing a
        // trunk for it would put stray posts across the landscape.
        if (trunk && canopyTopY != LodCell.NO_HEIGHT) {
            flags |= LodCell.FLAG_TRUNK;
        }

        int temperature = 0;
        int humidity = 0;
        if (climate != null) {
            int packed = climate.climateAt(blockX, blockZ);
            if (packed != CLIMATE_UNKNOWN) {
                flags |= LodCell.FLAG_TINT;
                temperature = (packed >> 8) & 0xFF;
                humidity = packed & 0xFF;
            }
        }

        // A block-resolution column is either under canopy or it is not; the
        // fractional coverage a coarse cell carries is produced by reduction.
        int coverage = canopyTopY == LodCell.NO_HEIGHT ? 0 : LodCell.MAX_COVERAGE;

        LodCell.write(
                outCells,
                outOffset,
                groundY,
                groundBlock,
                groundMeta,
                flags,
                waterY,
                canopyTopY,
                canopyBottomY,
                coverage,
                canopyKind,
                temperature,
                humidity);
    }

    /**
     * Metadata for a block index, from Beta's packed nibble array.
     *
     * <p>Even indices take the low nibble and odd ones the high nibble. A
     * producer without the array gets zero, which is the correct default for
     * every block whose appearance does not depend on metadata.
     */
    private static int metadataAt(byte[] data, int blockIndex) {
        if (data == null) {
            return 0;
        }
        int nibbleIndex = blockIndex >> 1;
        if (nibbleIndex < 0 || nibbleIndex >= data.length) {
            return 0;
        }
        int packed = data[nibbleIndex] & 0xFF;
        return (blockIndex & 1) == 0 ? (packed & 0x0F) : ((packed >> 4) & 0x0F);
    }
}
