package mcrtx.lod.format;

/**
 * The nine-byte LOD cell record, and the only definition of its layout.
 *
 * <p>A cell describes one column of ground at whatever scale its tile is drawn
 * at. The record is <b>identical at every detail level</b>: a cell covering two
 * blocks and a cell covering thirty-two hold the same fields in the same order,
 * which is what lets one reducer derive coarse tiles from fine ones and one
 * codec serve memory, disk and wire alike.
 *
 * <p><b>Two slabs, not one surface.</b> Ground and canopy are recorded
 * separately. Storing only "the highest solid block" is what makes a forest
 * render as a plateau of leaves standing on sheer leaf walls, because in a
 * forest the highest solid block <em>is</em> the canopy. Splitting them lets a
 * mesher draw real ground with a canopy layer above it, and lets a coarse cell
 * say "forty percent forested" instead of having to choose between all-canopy
 * and none.
 *
 * <p><b>Appearance is not stored; the ingredients for it are.</b> Earlier
 * caching stored resolved atlas tiles and a resolved colour, because the block
 * accessors that produce them need a loaded world and cached ground has none.
 * That works but welds the record to one client's texture atlas, so a file
 * cannot be shared and a server cannot produce one. Instead the record keeps
 * block id and metadata -- which resolve through the world-free
 * {@code Block.a(int side, int meta)} overload -- plus the two things that
 * genuinely need the world and cannot be re-derived from id alone: whether snow
 * sat on the surface, and the biome climate the tint comes from. Everything
 * else the client resolves against its own atlas at mesh time.
 *
 * <p><b>Climate rather than a colour or a biome id.</b> Beta stores no biome in
 * world data; biome is a function of seed and position, and grass and foliage
 * colour are functions of temperature and humidity. Keeping the two climate
 * bytes makes the record self-contained -- correct even on a server with a
 * custom generator, where re-deriving climate from the seed would be wrong --
 * for two bytes that compress to almost nothing, since both fields vary
 * smoothly across a tile.
 *
 * <p>Accessors are static over a {@code byte[]} and an offset rather than
 * methods on an object. A tile holds 1024 cells and a session holds thousands
 * of tiles, so a cell object per column is an allocation the field cannot
 * afford.
 */
public final class LodCell {
    /** Bytes one cell occupies. */
    public static final int BYTES = 9;

    /** Byte offsets within a cell record. */
    public static final int OFFSET_GROUND_Y = 0;
    public static final int OFFSET_GROUND_BLOCK = 1;
    public static final int OFFSET_GROUND_META = 2;
    public static final int OFFSET_WATER_Y = 3;
    public static final int OFFSET_CANOPY_TOP_Y = 4;
    public static final int OFFSET_CANOPY_BOTTOM_Y = 5;
    public static final int OFFSET_CANOPY = 6;
    public static final int OFFSET_TEMPERATURE = 7;
    public static final int OFFSET_HUMIDITY = 8;

    /**
     * Height byte meaning "no surface of this kind in this column".
     *
     * <p>Beta worlds are 128 blocks tall, so every real height fits in a byte
     * with this sentinel to spare.
     */
    public static final int NO_HEIGHT_BYTE = 0xFF;

    /** Height returned for an absent surface. Matches the field's -1 convention. */
    public static final int NO_HEIGHT = -1;

    /** Highest height a cell may record. */
    public static final int MAX_HEIGHT = 127;

    /** Metadata occupies the low nibble of the meta byte. */
    public static final int META_MASK = 0x0F;

    /**
     * Cell holds data, whether observed or derived. A cell without this is a
     * hole: the mesher must skip it rather than draw ground at y=0.
     *
     * <p>This is per cell rather than per tile because coarse tiles are
     * routinely partial -- a tile covering half a megametre may have thirty of
     * its thousand cells known -- and a tile-level flag cannot express that.
     */
    public static final int FLAG_KNOWN = 0x10;

    /**
     * A snow layer sat on the surface block.
     *
     * <p>Snow is excluded from being the surface itself, since the ground
     * beneath it is the real surface, but it changes how that ground looks:
     * grass under snow takes a different side texture. That is one of only two
     * appearance facts the world-free texture lookup cannot recover, so it is
     * carried explicitly.
     */
    public static final int FLAG_SNOW = 0x20;

    /** Temperature and humidity are valid. Producers without a biome source leave it clear. */
    public static final int FLAG_TINT = 0x40;

    /**
     * A tree trunk stood in this cell.
     *
     * <p>Logs are deliberately not part of the canopy slab -- letting them bound
     * it would run the foliage from the treetops down to the dirt and turn a
     * wood into a solid green block -- and they are not ground either, since the
     * scan steps over them to find the real surface. Without this flag they
     * therefore existed nowhere, and distant canopies floated with nothing
     * holding them up.
     *
     * <p>Only the fact is stored, not the extent: a trunk runs from the ground
     * to the underside of the leaves, and both of those are already in the cell.
     *
     * <p>This occupies what was the reserved flag bit, so a tile written before
     * it existed reads as trunkless rather than as corrupt -- which is why the
     * format version does not move. Older ground simply grows its trunks the
     * next time it is observed.
     */
    public static final int FLAG_TRUNK = 0x80;

    /** Flags occupy the high nibble. */
    public static final int FLAG_MASK = 0xF0;

    /** Canopy coverage occupies the low nibble of the canopy byte, 0..15. */
    public static final int COVERAGE_MASK = 0x0F;
    public static final int MAX_COVERAGE = 15;

    /** Canopy kind occupies the high nibble. */
    public static final int KIND_OAK = 0;
    public static final int KIND_SPRUCE = 1;
    public static final int KIND_BIRCH = 2;
    public static final int KIND_OTHER = 15;

    private LodCell() {
    }

    // ---- heights -------------------------------------------------------

    /** Highest solid ground block, or {@link #NO_HEIGHT}. */
    public static int groundY(byte[] cells, int offset) {
        return decodeHeight(cells[offset + OFFSET_GROUND_Y]);
    }

    /** Water surface block, or {@link #NO_HEIGHT}. */
    public static int waterY(byte[] cells, int offset) {
        return decodeHeight(cells[offset + OFFSET_WATER_Y]);
    }

    /** Highest leaf or log block, or {@link #NO_HEIGHT}. */
    public static int canopyTopY(byte[] cells, int offset) {
        return decodeHeight(cells[offset + OFFSET_CANOPY_TOP_Y]);
    }

    /** Lowest block of the canopy run, or {@link #NO_HEIGHT}. */
    public static int canopyBottomY(byte[] cells, int offset) {
        return decodeHeight(cells[offset + OFFSET_CANOPY_BOTTOM_Y]);
    }

    /**
     * Turns a stored height byte into a height or {@link #NO_HEIGHT}.
     *
     * <p>Anything outside the world's range reads as absent rather than
     * throwing: a record may have been written by a producer with a different
     * idea of world height, and a missing column is recoverable where an
     * exception on the render path is not.
     */
    public static int decodeHeight(byte stored) {
        int value = stored & 0xFF;
        return value > MAX_HEIGHT ? NO_HEIGHT : value;
    }

    /** Clamps a height into a storable byte, mapping anything out of range to absent. */
    public static byte encodeHeight(int height) {
        if (height < 0 || height > MAX_HEIGHT) {
            return (byte) NO_HEIGHT_BYTE;
        }
        return (byte) height;
    }

    // ---- ground --------------------------------------------------------

    public static int groundBlock(byte[] cells, int offset) {
        return cells[offset + OFFSET_GROUND_BLOCK] & 0xFF;
    }

    public static int groundMeta(byte[] cells, int offset) {
        return cells[offset + OFFSET_GROUND_META] & META_MASK;
    }

    public static int flags(byte[] cells, int offset) {
        return cells[offset + OFFSET_GROUND_META] & FLAG_MASK;
    }

    public static boolean isKnown(byte[] cells, int offset) {
        return (cells[offset + OFFSET_GROUND_META] & FLAG_KNOWN) != 0;
    }

    public static boolean hasSnow(byte[] cells, int offset) {
        return (cells[offset + OFFSET_GROUND_META] & FLAG_SNOW) != 0;
    }

    public static boolean hasTint(byte[] cells, int offset) {
        return (cells[offset + OFFSET_GROUND_META] & FLAG_TINT) != 0;
    }

    public static boolean hasTrunk(byte[] cells, int offset) {
        return (cells[offset + OFFSET_GROUND_META] & FLAG_TRUNK) != 0;
    }

    // ---- canopy --------------------------------------------------------

    /** Fraction of the cell under canopy, 0..15. Zero means no canopy geometry. */
    public static int canopyCoverage(byte[] cells, int offset) {
        return cells[offset + OFFSET_CANOPY] & COVERAGE_MASK;
    }

    public static int canopyKind(byte[] cells, int offset) {
        return (cells[offset + OFFSET_CANOPY] >> 4) & 0x0F;
    }

    // ---- climate -------------------------------------------------------

    /** Biome temperature scaled to 0..255. Meaningless unless {@link #hasTint} holds. */
    public static int temperature(byte[] cells, int offset) {
        return cells[offset + OFFSET_TEMPERATURE] & 0xFF;
    }

    /** Biome humidity scaled to 0..255. Meaningless unless {@link #hasTint} holds. */
    public static int humidity(byte[] cells, int offset) {
        return cells[offset + OFFSET_HUMIDITY] & 0xFF;
    }

    /** Scales a 0..1 climate value into its stored byte. */
    public static byte encodeClimate(float value) {
        int scaled = (int) (value * 255.0f + 0.5f);
        if (scaled < 0) {
            scaled = 0;
        } else if (scaled > 255) {
            scaled = 255;
        }
        return (byte) scaled;
    }

    /** Recovers a 0..1 climate value from its stored byte. */
    public static float decodeClimate(int stored) {
        return (stored & 0xFF) / 255.0f;
    }

    // ---- writing -------------------------------------------------------

    /**
     * Writes a complete cell. Every field is passed explicitly so that a
     * half-populated cell is not expressible: a producer that does not know a
     * field passes its absent value, and the record never carries a stale one
     * left over from whatever occupied the buffer before.
     */
    public static void write(
            byte[] cells,
            int offset,
            int groundY,
            int groundBlock,
            int groundMeta,
            int flags,
            int waterY,
            int canopyTopY,
            int canopyBottomY,
            int canopyCoverage,
            int canopyKind,
            int temperature,
            int humidity) {
        cells[offset + OFFSET_GROUND_Y] = encodeHeight(groundY);
        cells[offset + OFFSET_GROUND_BLOCK] = (byte) (groundBlock & 0xFF);
        cells[offset + OFFSET_GROUND_META] = (byte) ((groundMeta & META_MASK) | (flags & FLAG_MASK));
        cells[offset + OFFSET_WATER_Y] = encodeHeight(waterY);
        cells[offset + OFFSET_CANOPY_TOP_Y] = encodeHeight(canopyTopY);
        cells[offset + OFFSET_CANOPY_BOTTOM_Y] = encodeHeight(canopyBottomY);
        cells[offset + OFFSET_CANOPY] =
                (byte) ((canopyCoverage & COVERAGE_MASK) | ((canopyKind & 0x0F) << 4));
        cells[offset + OFFSET_TEMPERATURE] = (byte) (temperature & 0xFF);
        cells[offset + OFFSET_HUMIDITY] = (byte) (humidity & 0xFF);
    }

    /** Blanks a cell to the unknown state: no ground, no water, no canopy, no flags. */
    public static void clear(byte[] cells, int offset) {
        cells[offset + OFFSET_GROUND_Y] = (byte) NO_HEIGHT_BYTE;
        cells[offset + OFFSET_GROUND_BLOCK] = 0;
        cells[offset + OFFSET_GROUND_META] = 0;
        cells[offset + OFFSET_WATER_Y] = (byte) NO_HEIGHT_BYTE;
        cells[offset + OFFSET_CANOPY_TOP_Y] = (byte) NO_HEIGHT_BYTE;
        cells[offset + OFFSET_CANOPY_BOTTOM_Y] = (byte) NO_HEIGHT_BYTE;
        cells[offset + OFFSET_CANOPY] = 0;
        cells[offset + OFFSET_TEMPERATURE] = 0;
        cells[offset + OFFSET_HUMIDITY] = 0;
    }

    /** Copies one cell between buffers. */
    public static void copy(byte[] source, int sourceOffset, byte[] target, int targetOffset) {
        System.arraycopy(source, sourceOffset, target, targetOffset, BYTES);
    }

    /** True when two cells are byte-identical. Used by tests and by dirty tracking. */
    public static boolean equal(byte[] left, int leftOffset, byte[] right, int rightOffset) {
        for (int i = 0; i < BYTES; i++) {
            if (left[leftOffset + i] != right[rightOffset + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * True when every cell in a tile has been observed.
     *
     * <p>Lets a producer be skipped rather than run: a tile with no gaps has
     * nothing a second source could add, and asking one for it anyway costs a
     * file read to arrive at bytes identical to the ones already held. Written to
     * leave on the first hole, which is the common case and the cheap one.
     */
    public static boolean allKnown(byte[] cells) {
        if (cells == null) {
            return false;
        }
        for (int offset = 0; offset + BYTES <= cells.length; offset += BYTES) {
            if (!isKnown(cells, offset)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies cells into {@code target} wherever it has none of its own.
     *
     * <p>The tile is the unit a producer writes but the cell is the unit it
     * knows: the live sampler records a chunk at a time and so leaves fifteen
     * sixteenths of a tile untouched, and the region-file reader can only supply
     * chunks the save actually holds. Filling in the gaps is how those partial
     * answers add up to a whole tile instead of taking turns blanking each
     * other.
     *
     * <p><b>Directional on purpose.</b> A cell already known is never replaced,
     * however good the incoming source looks, because the caller has already
     * decided this source is the weaker of the two by choosing to merge rather
     * than to overwrite. That is what keeps ground the player built since the
     * last save from being reverted by the copy of it that is still on disk.
     *
     * @return how many cells were filled; zero means the merge changed nothing
     *     and the caller has no reason to store the result.
     */
    public static int fillUnknown(byte[] target, byte[] source) {
        if (target == null || source == null) {
            return 0;
        }
        int cells = Math.min(target.length, source.length) / BYTES;
        int filled = 0;
        for (int index = 0; index < cells; index++) {
            int offset = index * BYTES;
            if (isKnown(target, offset) || !isKnown(source, offset)) {
                continue;
            }
            copy(source, offset, target, offset);
            filled++;
        }
        return filled;
    }
}
