package mcrtx.bridge;

public final class RemixLodBridge {
    /** Columns along one edge of a region, at every detail level. */
    public static final int REGION_COLUMNS = 32;
    public static final int COLUMNS_PER_REGION = REGION_COLUMNS * REGION_COLUMNS;

    /**
     * Columns of the neighbouring regions handed over alongside a region's own.
     *
     * <p>The mesher draws the ground as a continuous surface with a vertex at
     * every cell corner, at the average height of the cells meeting there. Two
     * of the four cells meeting at a corner on the region boundary belong to the
     * region next door, so without them the two regions average different sets
     * at the same corner and land on different heights -- and a crack in distant
     * terrain is a hole with sky behind it, not a seam. One cell of border is
     * enough because nothing the mesher derives at a corner reads wider than the
     * 2x2 touching it.
     */
    public static final int APRON_COLUMNS = 1;
    public static final int PADDED_REGION_COLUMNS = REGION_COLUMNS + 2 * APRON_COLUMNS;
    public static final int PADDED_COLUMNS_PER_REGION =
            PADDED_REGION_COLUMNS * PADDED_REGION_COLUMNS;
    /**
     * Ints per column: ground heights, ground tiles, ground colour, the same
     * three for the canopy, and one for the trunk.
     *
     * <p>The canopy needs its own heights rather than sharing the ground's
     * because it is drawn as a slab standing above the ground, not as the
     * ground's surface. Folding the two together is what made distant forests
     * render as pillars and plateaus: a cell raised to treetop height gets walls
     * hung from it down to every lower neighbour.
     *
     * <p>The trunk needs only the one word -- whether a trunk stood here, and
     * what a log looks like -- because its extent is already in the six before
     * it: a trunk runs from the ground to the underside of the leaves.
     *
     * <p>This number is the format both sides of the JNI boundary agree on, and
     * changing it changes {@link #REGION_WORDS}, which the native side checks
     * the incoming array against. A jar and an {@code mcrtx_jni.dll} built from
     * different values of it do not fail loudly: every region is refused on the
     * length check and distant terrain silently draws nothing. Moving it means
     * shipping both artifacts together, everywhere the game loads them from.
     */
    public static final int WORDS_PER_COLUMN = 7;
    /** Ints in one handover: the padded grid rather than the emitted one. */
    public static final int REGION_WORDS = PADDED_COLUMNS_PER_REGION * WORDS_PER_COLUMN;

    public static final int RENDER_PASS_OPAQUE = 0;
    public static final int RENDER_PASS_TRANSLUCENT = 1;

    /** Sentinel written into a height field when the column has no such surface. */
    public static final int NO_SURFACE = -1;

    /**
     * Detail levels of the field, finest first: world blocks covered by one
     * column. A region is always {@link #REGION_COLUMNS} columns square, so the
     * step also sets the region size -- 64, 128, 256, 512 and 1024 blocks.
     *
     * <p>Each level covers twice the ground of the one before it. That is what
     * lets a finer level's footprint be an exact whole number of coarser
     * regions, which is how the scheduler keeps two detail levels off the same
     * ground without either overlapping or leaving a gap.
     *
     * <p>Step 32 is the last level this list can hold. A step is a detail level
     * in {@link mcrtx.lod.format.LodTileKey}'s pyramid -- step {@code 2^level}
     * -- and that pyramid stops at {@code MAX_LEVEL}, which is 5. The ring
     * arithmetic itself would tile perfectly well at step 64 and a radius of
     * 8192; what stops it is that a step-64 tile is a level the store cannot
     * key, cannot derive and cannot write to a cache file. Going further is a
     * format change, not a table change.
     */
    public static final int[] LOD_RING_STEPS = {2, 4, 8, 16, 32};

    /**
     * Outer edge of each ring, in blocks from the player. Ring i draws the band
     * between the previous ring's edge and its own.
     *
     * <p>Two constraints tie these to the steps above, and both are checked at
     * runtime. Twice a ring's radius must be a whole number of the next ring's
     * regions, so the boundary between them always falls on a region edge --
     * with that, a coarse region is either entirely inside the finer ring's
     * footprint or entirely outside it, never half covered. And each ring must
     * clear the one inside it by at least two of its own regions, so the
     * footprints nest however the player is placed on the grid.
     *
     * <p>The outermost radius is exactly twice the one inside it, which is the
     * only value that satisfies both at step 32: the clearance rule alone
     * demands at least 2048 + 2 * 1024, and that is 4096 on the nose.
     */
    public static final int[] LOD_RING_OUTER_BLOCKS = {192, 512, 1024, 2048, 4096};

    /** Region size in blocks at ring {@code index}. */
    public static int ringRegionSize(int index) {
        return REGION_COLUMNS * LOD_RING_STEPS[index];
    }

    private RemixLodBridge() {
    }

    /**
     * Hands a fully downsampled region to the native side in one crossing.
     * {@code packedColumns} holds {@link #WORDS_PER_COLUMN} ints per column,
     * row-major in Z then X, packed by {@link #packHeights} and
     * {@link #packTiles}.
     *
     * <p>The grid is {@link #PADDED_REGION_COLUMNS} square: the region's own
     * {@link #REGION_COLUMNS} columns with the apron around them, starting at
     * the column one before the region's origin.
     *
     * <p><b>Deliberately not synchronized</b>, unlike the rest of this class.
     * The native side meshes the region inside this call -- several
     * milliseconds at step 2 -- and it runs on the store's worker, while the
     * render thread calls {@link #rebuildToken} every frame and
     * {@link #unloadRegion} and {@link #setRegionHidden} whenever the field
     * moves. Holding the class monitor across the mesh made every one of those
     * wait out a build in progress, which was a hitch on the frame each time a
     * region was revealed. The native side takes its own mutex around every
     * touch of the region map, and the scheduler already serialises a submit
     * against {@link #clearRegions} under its own lock, so the monitor bought
     * nothing here but the stall. The only shared state left is the two
     * one-shot log flags, and a race on those costs a repeated log line.
     */
    public static boolean submitRegion(
            int originX, int originZ, int step, int renderPass, int[] packedColumns) {
        if (!RemixLifecycleBridge.isInitialized()) {
            if (!loggedNotInitialized) {
                loggedNotInitialized = true;
                log("lod: region submitted before the renderer was initialized");
            }
            return false;
        }
        if (packedColumns == null || packedColumns.length != REGION_WORDS) {
            if (!loggedWrongPayloadLength) {
                loggedWrongPayloadLength = true;
                log("lod: region payload is "
                        + (packedColumns == null ? "null" : Integer.toString(packedColumns.length))
                        + " ints, expected " + REGION_WORDS);
            }
            return false;
        }
        return nSubmitLodRegion(originX, originZ, step, renderPass, packedColumns);
    }

    // Both refusals above hand back the same bare false the native side does, so
    // without these the scheduler's "native refused" line names a boundary the
    // call never reached. One-shot: a refusal repeats for every region wanted.
    private static boolean loggedNotInitialized;
    private static boolean loggedWrongPayloadLength;

    public static synchronized void unloadRegion(int originX, int originZ, int step) {
        if (RemixLifecycleBridge.isInitialized()) {
            nUnloadLodRegion(originX, originZ, step);
        }
    }

    public static synchronized void setRegionHidden(
            int originX, int originZ, int step, boolean hidden) {
        if (RemixLifecycleBridge.isInitialized()) {
            nSetLodRegionHidden(originX, originZ, step, hidden);
        }
    }

    public static synchronized void clearRegions() {
        if (RemixLifecycleBridge.isInitialized()) {
            nClearLodRegions();
        }
    }

    public static synchronized int regionCount() {
        if (!RemixLifecycleBridge.isInitialized()) {
            return 0;
        }
        return nLodRegionCount();
    }

    /**
     * Triangles the field currently holds. Region counts say nothing about what
     * the field costs -- an ocean region and a mountain region are one region
     * each and an order of magnitude apart -- so this is the number to read
     * when judging a change to how regions are meshed.
     *
     * @param visibleOnly count only the regions that are actually submitted,
     *     skipping the ones handed over to full-detail chunk geometry.
     */
    public static synchronized int triangleCount(boolean visibleOnly) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return 0;
        }
        return nLodTriangleCount(visibleOnly);
    }

    /**
     * Counter the native side bumps whenever it drops every region mesh on its
     * own account, which it does each time the LOD materials are rebuilt.
     *
     * <p>The scheduler tracks what it believes is resident, so it has to hear
     * about that: otherwise the regions are gone from Remix but still resident
     * here, and nothing ever resubmits them.
     */
    public static synchronized int rebuildToken() {
        if (!RemixLifecycleBridge.isInitialized()) {
            return 0;
        }
        return nLodRebuildToken();
    }

    public static int packHeights(int topY, int waterY) {
        return (topY & 0xFFFF) | ((waterY & 0xFFFF) << 16);
    }

    public static int packTiles(int topTile, int sideTile) {
        return (topTile & 0xFFFF) | ((sideTile & 0xFFFF) << 16);
    }

    /** Routes a diagnostic through the native logger so it reaches mcrtx.log. */
    public static synchronized void log(String message) {
        System.out.println("[mcrtx] " + message);
        if (RemixLifecycleBridge.isInitialized()) {
            nLog(message);
        }
    }

    private static native void nLog(String message);

    private static native boolean nSubmitLodRegion(
            int originX, int originZ, int step, int renderPass, int[] packedColumns);

    private static native void nUnloadLodRegion(int originX, int originZ, int step);

    private static native void nSetLodRegionHidden(
            int originX, int originZ, int step, boolean hidden);

    private static native void nClearLodRegions();

    private static native int nLodRegionCount();

    private static native int nLodTriangleCount(boolean visibleOnly);

    private static native int nLodRebuildToken();
}
