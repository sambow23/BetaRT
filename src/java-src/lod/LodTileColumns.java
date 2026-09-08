import mcrtx.bridge.RemixLodBridge;
import mcrtx.lod.format.LodBlocks;
import mcrtx.lod.format.LodCell;
import mcrtx.lod.format.LodTileKey;

/**
 * Converts a stored tile into the packed columns the native side reads today.
 *
 * <p>A bridge between the new data model and the old mesher, and deliberately a
 * temporary one. The native side still expects three ints per column -- two
 * heights, two atlas tiles and a colour -- which is exactly the information the
 * old per-chunk cache used to store directly. Now that a cell keeps block id,
 * metadata, canopy and biome climate instead, appearance is resolved here, on
 * the client, against the client's own atlas. That is the whole point of the
 * change: the stored bytes no longer depend on one install's texture pack, so a
 * cache file can be shared and a server can produce one.
 *
 * <p><b>Region and tile are the same rectangle.</b> A region at step {@code S}
 * is 32 columns of {@code S} blocks, and a tile at level {@code log2(S)} is 32
 * cells of {@code S} blocks. The scheduler already snaps region origins to that
 * grid, so the mapping is exact and needs no interpolation. What crosses to the
 * native side is one cell wider on every side than that: the mesher averages a
 * corner over the cells meeting at it, and on the boundary two of those belong
 * to the tile next door.
 *
 * <p><b>Ground and canopy are handed over separately</b>, and that is the point.
 * An earlier pass here raised a forested cell's ground to treetop height and
 * gave it a leaf texture, which sounds reasonable and is not: the mesher hangs
 * walls from every cell down to its lower neighbours, so a lone tree became a
 * solid pillar of leaves and a wood became a plateau with sheer green sides.
 * A canopy has to arrive as its own pair of heights for the mesher to be able to
 * draw it as a slab with an underside.
 */
final class LodTileColumns {
    /** Beta's grass block, whose colour comes from the biome. */
    private static final int GRASS_BLOCK = 2;
    /**
     * The grass block's faces in terrain.png: the greyscale top the biome tint
     * is applied to, and the dirt-with-a-green-strip side.
     *
     * <p>Spelled out here because grass is the one block whose world-free
     * texture lookup answers wrongly. Beta's grass overrides only the
     * world-aware overload -- the one that looks for snow above -- and leaves
     * {@code a(int side)} to the base class, which answers with the block's
     * single texture index. For grass that index is the side, so every face
     * including the top came back as 3, and a field of it read as furrowed
     * dirt from above.
     */
    private static final int GRASS_TOP_TEXTURE = 0;
    private static final int GRASS_SIDE_TEXTURE = 3;
    /** Beta's snow block, borrowed for the appearance of snow-covered ground. */
    private static final int SNOW_BLOCK = 80;

    private static final int TOP_SIDE = 1;
    private static final int NORTH_SIDE = 2;

    /**
     * Coverage at or above which a cell is drawn as canopy rather than ground.
     *
     * <p>Half. Below it the ground shows through and a scattering of trees does
     * not lift the whole cell; at or above it the cell is more forest than not.
     */
    private static final int CANOPY_SURFACE_COVERAGE = 8;

    /**
     * Step at which the canopy stops being its own slab and becomes the ground.
     *
     * <p>Beta's trees are about five blocks across, so a step-4 cell is roughly
     * one tree and a step-8 cell is four. Below that a canopy is an object with
     * an inside and an edge; at and above it, it is terrain.
     */
    private static final int CANOPY_MERGE_STEP = 8;

    private static final int WHITE = 0xFFFFFFFF;

    private LodTileColumns() {
    }

    /** The tile is not in the store yet; nothing was read and nothing written. */
    static final int FILL_NO_TILE = 0;
    /** The tile exists but holds no surface anywhere -- open sky, or void. */
    static final int FILL_EMPTY = 1;
    /** Columns were written and at least one has a surface. */
    static final int FILL_OK = 2;

    /**
     * Fills {@code out} from the tile covering a region.
     *
     * <p>The distinction between {@link #FILL_NO_TILE} and {@link #FILL_EMPTY}
     * matters to the scheduler's budget rather than to the picture. A missing
     * tile is the ordinary case on a fresh world -- nothing has been sampled or
     * derived there yet -- and it costs a map lookup, so charging it against the
     * per-tick build budget would let a field full of misses starve the handful
     * of regions that could actually be built. An empty tile is a real answer
     * that cost real work, and is charged for.
     */
    static int fill(int originX, int originZ, int step, int[] out) {
        if (out == null || out.length != RemixLodBridge.REGION_WORDS) {
            return FILL_NO_TILE;
        }

        int level = levelForStep(step);
        if (level < 0) {
            return FILL_NO_TILE;
        }

        int size = LodTileKey.sizeBlocks(level);
        int tileX = Math.floorDiv(originX, size);
        int tileZ = Math.floorDiv(originZ, size);

        byte[] center = LodStore.tile(new LodTileKey(level, tileX, tileZ));
        if (center == null) {
            return FILL_NO_TILE;
        }

        // The eight tiles around it, for the apron. Read without asking for a
        // derivation: a neighbour that is drawn is being derived on its own
        // account anyway, because the region covering it wants it too, and one
        // that is not is ground nobody renders -- so queueing it would only push
        // tiles that are drawn further down the worker's list. A neighbour that
        // is missing leaves its apron cells unknown, and the region-edge curtain
        // covers that boundary exactly as it did before the apron existed.
        byte[][] window = new byte[9][];
        for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
            for (int deltaX = -1; deltaX <= 1; deltaX++) {
                int slot = (deltaZ + 1) * 3 + (deltaX + 1);
                window[slot] = deltaX == 0 && deltaZ == 0
                        ? center
                        : LodStore.tileIfResident(
                                new LodTileKey(level, tileX + deltaX, tileZ + deltaZ));
            }
        }

        // Whether the canopy gets geometry of its own, or simply becomes the
        // ground surface.
        //
        // At step 2 and 4 a cell is narrower than a tree, so a wood has interior
        // cells and edge cells and the difference is what makes it read as a
        // wood: foliage standing above ground you can still see under. From step
        // 8 outward a single cell is wider than several trees, the underside is
        // never visible, and a canopy slab would cost a box per cell to draw
        // what a raised surface already says.
        boolean mergeCanopy = step >= CANOPY_MERGE_STEP;

        int cells = LodTileKey.TILE_CELLS;
        int apron = RemixLodBridge.APRON_COLUMNS;
        boolean anySurface = false;
        int writeIndex = 0;
        for (int paddedZ = -apron; paddedZ < cells + apron; paddedZ++) {
            for (int paddedX = -apron; paddedX < cells + apron; paddedX++) {
                int slotX = paddedX < 0 ? 0 : (paddedX >= cells ? 2 : 1);
                int slotZ = paddedZ < 0 ? 0 : (paddedZ >= cells ? 2 : 1);
                byte[] source = window[slotZ * 3 + slotX];

                boolean surface;
                if (source == null) {
                    // Unknown ground, and deliberately written as a column with
                    // no surfaces at all rather than flagged separately. Every
                    // consumer on the native side already reads "no ground, no
                    // water, no canopy" as nothing to draw and nothing to
                    // average into a corner, which is what unknown has to mean.
                    surface = writeEmptyColumn(out, writeIndex);
                } else {
                    int offset = LodTileKey.cellOffset(
                            Math.floorMod(paddedX, cells), Math.floorMod(paddedZ, cells));
                    surface = writeColumn(source, offset, mergeCanopy, out, writeIndex);
                }

                // Only the cells the region actually emits decide whether it is
                // worth submitting. A region with nothing in it but a forested
                // apron is still a region with nothing in it.
                if (surface && slotX == 1 && slotZ == 1) {
                    anySurface = true;
                }
                writeIndex += RemixLodBridge.WORDS_PER_COLUMN;
            }
        }
        return anySurface ? FILL_OK : FILL_EMPTY;
    }

    /**
     * Resolves one cell into the {@link RemixLodBridge#WORDS_PER_COLUMN} ints a
     * column occupies.
     *
     * @return whether the cell holds any surface at all
     */
    private static boolean writeColumn(
            byte[] cells, int offset, boolean mergeCanopy, int[] out, int writeIndex) {
        if (!LodCell.isKnown(cells, offset)) {
            return writeEmptyColumn(out, writeIndex);
        }

        int topY = LodCell.groundY(cells, offset);
        int waterY = LodCell.waterY(cells, offset);
        int topTile = 0;
        int sideTile = 0;
        int color = WHITE;

        int canopyTopY = LodCell.NO_HEIGHT;
        int canopyBottomY = LodCell.NO_HEIGHT;
        int canopyTopTile = 0;
        int canopySideTile = 0;
        int canopyColor = WHITE;
        boolean trunk = false;
        int trunkTile = 0;

        if (topY != LodCell.NO_HEIGHT) {
            int blockId = surfaceBlockOf(cells, offset);
            int meta = LodCell.groundMeta(cells, offset);
            topTile = textureOf(blockId, meta, TOP_SIDE);
            sideTile = textureOf(blockId, meta, NORTH_SIDE);
            color = groundColor(cells, offset, blockId);
        }

        boolean forested = LodCell.canopyCoverage(cells, offset) >= CANOPY_SURFACE_COVERAGE
                && LodCell.canopyTopY(cells, offset) != LodCell.NO_HEIGHT;
        if (forested) {
            int canopyMeta = speciesMeta(LodCell.canopyKind(cells, offset));
            if (mergeCanopy) {
                // Coarse levels: a cell is wider than a tree is, so the canopy
                // is the terrain surface and drawing it as a separate slab would
                // cost geometry to say the same thing. The ground rises to the
                // treetops instead.
                topY = LodCell.canopyTopY(cells, offset);
                topTile = textureOf(LodBlocks.LEAVES, canopyMeta, TOP_SIDE);
                sideTile = textureOf(LodBlocks.LEAVES, canopyMeta, NORTH_SIDE);
                color = foliageColor(cells, offset);
            } else {
                canopyTopY = LodCell.canopyTopY(cells, offset);
                canopyBottomY = LodCell.canopyBottomY(cells, offset);
                if (canopyBottomY == LodCell.NO_HEIGHT || canopyBottomY > canopyTopY) {
                    canopyBottomY = canopyTopY;
                }
                canopyTopTile = textureOf(LodBlocks.LEAVES, canopyMeta, TOP_SIDE);
                canopySideTile = textureOf(LodBlocks.LEAVES, canopyMeta, NORTH_SIDE);
                canopyColor = foliageColor(cells, offset);

                // A trunk only makes sense under a canopy that is being drawn as
                // an object in its own right. Where the canopy has merged into
                // the ground there is no gap beneath it for a trunk to occupy.
                if (LodCell.hasTrunk(cells, offset)) {
                    trunk = true;
                    trunkTile = textureOf(LodBlocks.LOG, canopyMeta, NORTH_SIDE);
                }
            }
        }

        out[writeIndex] = RemixLodBridge.packHeights(topY, waterY);
        out[writeIndex + 1] = RemixLodBridge.packTiles(topTile, sideTile);
        out[writeIndex + 2] = color;
        out[writeIndex + 3] = RemixLodBridge.packHeights(canopyTopY, canopyBottomY);
        out[writeIndex + 4] = RemixLodBridge.packTiles(canopyTopTile, canopySideTile);
        out[writeIndex + 5] = canopyColor;
        out[writeIndex + 6] = (trunkTile & 0xFFFF) | (trunk ? (1 << 16) : 0);

        return topY != LodCell.NO_HEIGHT
                || waterY != LodCell.NO_HEIGHT
                || canopyTopY != LodCell.NO_HEIGHT;
    }

    /**
     * Writes a column with no surfaces of any kind. Always returns false.
     *
     * <p>Every word is written, including the ones that only mean anything when
     * a surface exists. The buffer is scratch reused across regions, so a word
     * left alone here is not zero -- it is whatever the region built before this
     * one put there, read back as though it belonged to this ground.
     */
    private static boolean writeEmptyColumn(int[] out, int writeIndex) {
        int noSurfaces = RemixLodBridge.packHeights(LodCell.NO_HEIGHT, LodCell.NO_HEIGHT);
        out[writeIndex] = noSurfaces;
        out[writeIndex + 1] = 0;
        out[writeIndex + 2] = WHITE;
        out[writeIndex + 3] = noSurfaces;
        out[writeIndex + 4] = 0;
        out[writeIndex + 5] = WHITE;
        out[writeIndex + 6] = 0;
        return false;
    }

    /**
     * Whether the store can answer for the ground under a region right now.
     *
     * <p>The scheduler probes here before it hands a build to the worker,
     * because a miss is the ordinary case over ground nothing has sampled yet
     * and costs a map lookup, where a build costs a point of the per-tick
     * budget and a turn on the worker. Going through {@link LodStore#probeTile}
     * rather than a plain containment test is deliberate: that probe is what
     * queues a coarse tile for derivation, and it is the only thing in the
     * field that asks for one. It answers without inflating the tile, which
     * {@link LodStore#tile} would do -- on the render thread, for a tile the
     * worker is about to inflate and cache on its own account.
     */
    static boolean hasTileFor(int originX, int originZ, int step) {
        int level = levelForStep(step);
        if (level < 0) {
            return false;
        }
        int size = LodTileKey.sizeBlocks(level);
        return LodStore.probeTile(new LodTileKey(
                level, Math.floorDiv(originX, size), Math.floorDiv(originZ, size)));
    }

    /**
     * Asks the save again about the tile under a region, if it is a canonical
     * tile with gaps. Only the finest ring reads canonical tiles directly;
     * every coarser ring's tiles are derived, and derivation asks for their
     * children on its own account.
     */
    static void retryDiskRead(int originX, int originZ, int step) {
        int level = levelForStep(step);
        if (level != LodTileKey.CANONICAL_LEVEL) {
            return;
        }
        int size = LodTileKey.sizeBlocks(level);
        LodStore.requestDiskReadIfPartial(LodTileKey.pack(
                level, Math.floorDiv(originX, size), Math.floorDiv(originZ, size)));
    }

    /**
     * Epoch of the newest tile a build of this region reads: its own, and the
     * eight around it whose edge cells make up the apron.
     *
     * <p>Absent tiles count as zero, which is what makes a neighbour arriving
     * later read as a change: the corners along that boundary were averaged
     * over the two cells this side of it and are averaged over four once the
     * neighbour is there, and the region has to be re-meshed to move them.
     */
    static int windowEpoch(int originX, int originZ, int step) {
        int level = levelForStep(step);
        if (level < 0) {
            return 0;
        }
        int size = LodTileKey.sizeBlocks(level);
        int tileX = Math.floorDiv(originX, size);
        int tileZ = Math.floorDiv(originZ, size);
        int newest = 0;
        for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
            for (int deltaX = -1; deltaX <= 1; deltaX++) {
                int epoch = LodStore.tileEpoch(
                        LodTileKey.pack(level, tileX + deltaX, tileZ + deltaZ));
                if (epoch > newest) {
                    newest = epoch;
                }
            }
        }
        return newest;
    }

    /** Detail level for a region step, or -1 when the step is not a power of two. */
    static int levelForStep(int step) {
        if (step <= 0 || (step & (step - 1)) != 0) {
            return -1;
        }
        int level = Integer.numberOfTrailingZeros(step);
        return level > LodTileKey.MAX_LEVEL ? -1 : level;
    }

    /**
     * The block a cell's surface should be drawn as.
     *
     * <p>Snow is the one appearance fact the world-free texture lookup cannot
     * recover: Beta's grass takes a different side texture when snow sits on it,
     * and that depends on the block above rather than on the block itself.
     * Drawing snowy ground as the snow block gets both faces right without
     * needing the world, which is the point of storing the flag.
     */
    private static int surfaceBlockOf(byte[] cells, int offset) {
        if (LodCell.hasSnow(cells, offset)) {
            return SNOW_BLOCK;
        }
        return LodCell.groundBlock(cells, offset);
    }

    /**
     * Atlas tile for a block face, through the world-free overload.
     *
     * <p>{@code uu.a(int side, int meta)} needs no world, which is what lets a
     * tile sampled on a server or read off disk resolve its appearance here.
     */
    private static int textureOf(int blockId, int meta, int side) {
        if (blockId == GRASS_BLOCK) {
            // See GRASS_TOP_TEXTURE: the generic lookup below cannot tell the
            // top of a grass block from its side.
            return side == TOP_SIDE ? GRASS_TOP_TEXTURE : GRASS_SIDE_TEXTURE;
        }
        if (blockId <= 0 || blockId >= uu.m.length) {
            return 0;
        }
        uu block = uu.m[blockId];
        if (block == null) {
            return 0;
        }
        try {
            return block.a(side, meta);
        } catch (Throwable failure) {
            // A block whose texture lookup expects state we do not have draws
            // untextured rather than taking the field down.
            return 0;
        }
    }

    /**
     * Beta's leaf and log metadata for a stored canopy kind.
     *
     * <p>Both species lookups are keyed by metadata rather than by block id --
     * {@code bk.a} answers 132 for spruce leaves and {@code vg.a} answers 116
     * and 117 for spruce and birch trunks -- so a canopy built with a metadata
     * of zero comes back oak whatever grew there. The kind is already carried
     * per cell for the foliage colour, and its three named values are numbered
     * to match the species bits Beta stores in the block, so it can be handed
     * to the lookup as-is. {@link LodCell#KIND_OTHER} asks for oak, which is
     * what both lookups fall through to.
     */
    private static int speciesMeta(int canopyKind) {
        return canopyKind == LodCell.KIND_SPRUCE || canopyKind == LodCell.KIND_BIRCH ? canopyKind : 0;
    }

    private static int groundColor(byte[] cells, int offset, int blockId) {
        if (blockId != GRASS_BLOCK || !LodCell.hasTint(cells, offset)) {
            return WHITE;
        }
        double temperature = LodCell.decodeClimate(LodCell.temperature(cells, offset));
        double humidity = LodCell.decodeClimate(LodCell.humidity(cells, offset));
        try {
            return 0xFF000000 | ia.a(temperature, humidity);
        } catch (Throwable failure) {
            return WHITE;
        }
    }

    /**
     * Foliage colour for a canopy cell.
     *
     * <p>Spruce and birch carry fixed colours in Beta rather than biome-derived
     * ones, which is why the kind is stored at all.
     */
    private static int foliageColor(byte[] cells, int offset) {
        try {
            int kind = LodCell.canopyKind(cells, offset);
            if (kind == LodCell.KIND_SPRUCE) {
                return 0xFF000000 | jh.a();
            }
            if (kind == LodCell.KIND_BIRCH) {
                return 0xFF000000 | jh.b();
            }
            if (!LodCell.hasTint(cells, offset)) {
                return WHITE;
            }
            double temperature = LodCell.decodeClimate(LodCell.temperature(cells, offset));
            double humidity = LodCell.decodeClimate(LodCell.humidity(cells, offset));
            return 0xFF000000 | jh.a(temperature, humidity);
        } catch (Throwable failure) {
            return WHITE;
        }
    }
}
