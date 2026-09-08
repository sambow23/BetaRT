package mcrtx.lod.format;

/**
 * Identity of one LOD tile: a detail level and a position on that level's grid.
 *
 * <p>A tile is always {@link #TILE_CELLS} cells square. What a level changes is
 * only how much ground a cell covers -- {@code 2^level} blocks -- and therefore
 * how much ground the tile covers. Holding the cell count fixed is what keeps
 * mesh size, and so acceleration-structure size and rebuild cost, constant
 * across the whole field: a tile at the horizon costs the same to build as one
 * underfoot.
 *
 * <p>Because each level's cells are exactly twice the width of the level below,
 * <b>a tile is exactly the 2x2 block of tiles beneath it</b>. That single
 * property carries most of the design: coarse tiles can be derived from fine
 * ones by a pure function ({@link LodReducer}), a level's footprint can be made
 * to tile exactly against its neighbour's with no overlap, and the store needs
 * one key space rather than one per detail level.
 *
 * <p>This class is the whole of that arithmetic. Nothing else should compute a
 * tile origin or a step from a level by hand.
 */
public final class LodTileKey {
    /** Cells along one edge of a tile, at every level. */
    public static final int TILE_CELLS = 32;
    public static final int CELLS_PER_TILE = TILE_CELLS * TILE_CELLS;

    /** Bytes one tile's cell array occupies. */
    public static final int TILE_BYTES = CELLS_PER_TILE * LodCell.BYTES;

    /**
     * Finest level, at one block per cell.
     *
     * <p>Level 0 is transient by design: chunks are sampled at block resolution
     * and immediately reduced into level 1, which is the finest level anything
     * stores or draws. Keeping level 0 on disk would quadruple the footprint
     * for detail no ring ever renders, and it can always be re-derived from the
     * world in the time it takes to read a region file.
     */
    public static final int MIN_LEVEL = 0;

    /** Finest level that is stored and rendered. */
    public static final int CANONICAL_LEVEL = 1;

    /**
     * Coarsest level the format admits.
     *
     * <p>Level 5 covers 1024 blocks a tile and is not currently drawn; the
     * format allows it so an outer band can be enabled without a format change.
     */
    public static final int MAX_LEVEL = 5;

    private final int level;
    private final int tileX;
    private final int tileZ;

    public LodTileKey(int level, int tileX, int tileZ) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException("level out of range: " + level);
        }
        this.level = level;
        this.tileX = tileX;
        this.tileZ = tileZ;
    }

    public int level() {
        return level;
    }

    public int tileX() {
        return tileX;
    }

    public int tileZ() {
        return tileZ;
    }

    /** World blocks covered by one cell at this level. */
    public int step() {
        return step(level);
    }

    /** World blocks along one edge of this tile. */
    public int sizeBlocks() {
        return sizeBlocks(level);
    }

    /** Block coordinate of the tile's minimum corner. */
    public int originX() {
        return tileX * sizeBlocks(level);
    }

    public int originZ() {
        return tileZ * sizeBlocks(level);
    }

    /** Distance in blocks from this tile's centre to a point, along the longer axis. */
    public int chebyshevDistanceToCenter(int blockX, int blockZ) {
        int half = sizeBlocks(level) >> 1;
        int centerX = originX() + half;
        int centerZ = originZ() + half;
        int deltaX = Math.abs(blockX - centerX);
        int deltaZ = Math.abs(blockZ - centerZ);
        return deltaX > deltaZ ? deltaX : deltaZ;
    }

    /** The coarser tile containing this one, or null at {@link #MAX_LEVEL}. */
    public LodTileKey parent() {
        if (level >= MAX_LEVEL) {
            return null;
        }
        return new LodTileKey(level + 1, tileX >> 1, tileZ >> 1);
    }

    /**
     * One of the four finer tiles this one covers, or null at {@link #MIN_LEVEL}.
     *
     * @param quadrant index in {@link LodReducer}'s order: {@code quadrantZ * 2 + quadrantX}.
     */
    public LodTileKey child(int quadrant) {
        if (level <= MIN_LEVEL) {
            return null;
        }
        if (quadrant < 0 || quadrant > 3) {
            throw new IllegalArgumentException("quadrant out of range: " + quadrant);
        }
        return new LodTileKey(level - 1, (tileX << 1) | (quadrant & 1), (tileZ << 1) | ((quadrant >> 1) & 1));
    }

    public long packed() {
        return pack(level, tileX, tileZ);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LodTileKey)) {
            return false;
        }
        LodTileKey key = (LodTileKey) other;
        return level == key.level && tileX == key.tileX && tileZ == key.tileZ;
    }

    @Override
    public int hashCode() {
        long packed = packed();
        return (int) (packed ^ (packed >>> 32));
    }

    @Override
    public String toString() {
        return "L" + level + "[" + tileX + "," + tileZ + "]";
    }

    // ---- static level arithmetic ---------------------------------------

    /** World blocks covered by one cell at {@code level}. */
    public static int step(int level) {
        return 1 << level;
    }

    /** World blocks along one edge of a tile at {@code level}. */
    public static int sizeBlocks(int level) {
        return TILE_CELLS << level;
    }

    /** Tile grid coordinate containing a block coordinate. Correct for negatives. */
    public static int tileCoordinate(int blockCoordinate, int level) {
        return Math.floorDiv(blockCoordinate, sizeBlocks(level));
    }

    /** The tile at {@code level} containing a block position. */
    public static LodTileKey containing(int level, int blockX, int blockZ) {
        return new LodTileKey(level, tileCoordinate(blockX, level), tileCoordinate(blockZ, level));
    }

    /** Index of a cell within a tile's row-major-in-Z grid. */
    public static int cellIndex(int cellX, int cellZ) {
        return cellZ * TILE_CELLS + cellX;
    }

    /** Byte offset of a cell within a tile's array. */
    public static int cellOffset(int cellX, int cellZ) {
        return cellIndex(cellX, cellZ) * LodCell.BYTES;
    }

    // ---- packing -------------------------------------------------------
    //
    // Tile coordinates get 30 signed bits each and the level the top four,
    // filling a long exactly. Thirty bits reaches +/-536 million tiles, which at
    // the finest stored level is +/-34 billion blocks -- comfortably past the
    // point where Beta's own world arithmetic gives out. Packing matters because
    // the resident set and build queues are keyed by tile on the client thread
    // every frame, and a boxed key object per lookup is an allocation the field
    // does not need.

    private static final int COORDINATE_BITS = 30;
    private static final long COORDINATE_MASK = (1L << COORDINATE_BITS) - 1L;

    public static long pack(int level, int tileX, int tileZ) {
        return ((long) (level & 0x0F) << 60)
                | ((tileX & COORDINATE_MASK) << COORDINATE_BITS)
                | (tileZ & COORDINATE_MASK);
    }

    public static int unpackLevel(long packed) {
        return (int) ((packed >>> 60) & 0x0F);
    }

    public static int unpackTileX(long packed) {
        return signExtend((int) ((packed >>> COORDINATE_BITS) & COORDINATE_MASK));
    }

    public static int unpackTileZ(long packed) {
        return signExtend((int) (packed & COORDINATE_MASK));
    }

    public static LodTileKey unpack(long packed) {
        return new LodTileKey(unpackLevel(packed), unpackTileX(packed), unpackTileZ(packed));
    }

    /** Restores the sign of a coordinate stored in the low 30 bits. */
    private static int signExtend(int value) {
        return (value << (32 - COORDINATE_BITS)) >> (32 - COORDINATE_BITS);
    }
}
