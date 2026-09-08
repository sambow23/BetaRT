import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import mcrtx.bridge.RemixLodBridge;

/**
 * Driver for distant terrain: concentric rings of LOD regions, each ring at its
 * own detail level.
 *
 * <p>A ring is a square annulus of regions around the player. The innermost ring
 * has the smallest cells and the smallest regions; each ring outward doubles
 * both, so a region always holds the same 32x32 columns and only the ground it
 * covers grows. Ring i draws the band between ring i-1's edge and its own, which
 * keeps the on-screen size of a cell roughly constant across the whole field.
 *
 * <p><b>No ground is ever covered twice.</b> A path tracer does not merely
 * z-fight over duplicated coincident surfaces, it lights through them, so the
 * bands are made to tile exactly rather than to overlap and be sorted out later.
 * Each ring's footprint is snapped to the <em>next</em> ring's region grid, so
 * every coarse region is either wholly inside the finer footprint -- in which
 * case it is not part of the coarse ring at all -- or wholly outside it. There
 * is no third case, and so no partial overlap to resolve.
 *
 * <p><b>Ground comes from one place.</b> {@link LodStore} answers for all of it
 * -- chunks the player walked past, tiles read back from its own file, and
 * eventually tiles a server sent for ground nobody here has visited -- and the
 * scheduler does not know or care which. It asks for the tile covering a region
 * and either gets one or does not; a region it could not build is retried on the
 * next data sweep. That is a real simplification over choosing between a live
 * and a cached source per region, and it is what lets a new source be added
 * without the scheduler learning about it.
 *
 * <p><b>The client thread decides; the store's worker builds.</b> Meshing a
 * region and handing it to Remix costs a couple of milliseconds, and doing that
 * inside a tick is what put a visible hitch in the frame every time a stretch
 * of ground was revealed. The scheduler now posts a build and picks the result
 * up on a later tick, so the only thing a tick spends on a region is a map
 * lookup to see whether the ground is known at all.
 *
 * <p>Everything that decides what the field looks like still runs on the client
 * thread -- footprints, residency, handover, eviction -- and none of it is
 * shared with the worker. What crosses between them is one posted job, one
 * completion, and a generation counter that says whether the world the job was
 * started for is still the world being drawn.
 */
public final class RemixLodCapture {
    /** Ticks between coverage sweeps. Handover is not worth checking per frame. */
    private static final int COVERAGE_SWEEP_INTERVAL_TICKS = 5;

    /**
     * Ticks between sweeps that look for ground a region did not have when it
     * was last built.
     *
     * <p>A region built the moment its ring appeared is nearly always
     * incomplete, and one over ground nothing has sampled yet produces nothing
     * at all. Both are temporary: the sampler keeps feeding the store as the
     * player moves, and the store keeps deriving coarse tiles behind it. This
     * sweep is what turns that into terrain, by requeueing any region whose
     * build predates the latest change to the tiles it reads.
     */
    private static final int DATA_SWEEP_INTERVAL_TICKS = 40;

    /** Ticks between status lines in mcrtx.log while the field is changing. */
    private static final int STATUS_LOG_INTERVAL_TICKS = 200;


    /**
     * Range beyond which a region is never tested against chunk coverage.
     *
     * <p>Chunk meshes are only kept within the capture's eviction radius, about
     * twenty chunks, so nothing past this can possibly be covered. Without the
     * cutoff the outer rings would probe thousands of chunk columns every sweep
     * to be told "no" every time.
     */
    private static final int COVERAGE_PROBE_RANGE_BLOCKS = 512;

    /** Outcomes of one region build attempt. */
    private static final int BUILD_NO_SURFACE = 0;
    private static final int BUILD_SUBMITTED = 1;
    private static final int BUILD_REFUSED = 2;
    /** No tile stored for this ground yet. Costs a lookup, not a build. */
    private static final int BUILD_NO_DATA = 3;
    /**
     * The field was dropped, or the world changed, while the build was in
     * flight. Nothing was submitted and nothing is to be recorded.
     */
    private static final int BUILD_STALE = 4;

    /**
     * Builds allowed to be with the worker at once.
     *
     * <p>The per-tick budget bounds how fast builds are posted, not how fast
     * they finish, so on its own it lets a queue grow without limit whenever the
     * worker is busy loading or saving. This is the ceiling on that: past it the
     * scheduler stops posting and the queue entries simply wait, which is what
     * they would have done anyway.
     */
    private static final int MAX_BUILDS_IN_FLIGHT = 8;

    /**
     * Cost units one point of the per-tick build budget buys.
     *
     * <p>A build is now the same work at every level -- read one tile, convert
     * its 1024 cells -- because the ground is already downsampled by the time
     * the scheduler sees it. The old path re-sampled the world per region and so
     * cost more the coarser the level got, which is what the per-level charge
     * existed to even out. One budget point is one region, at any level.
     */
    private static final int BUILD_COST_UNITS_PER_BUDGET = 16;

    /** Inspections -- queue entries looked at and skipped -- per budget point. */
    private static final int INSPECTIONS_PER_BUDGET = 16;

    /**
     * Scratch for one region's columns, owned by the store's worker.
     *
     * <p>One array serves the whole field because the worker runs builds one at
     * a time. Nothing on the client thread touches it.
     */
    private static int[] workerColumns;

    /**
     * One detail level of the field: its own resident set, build queue and
     * eviction, all keyed by region origin at this ring's step.
     */
    private static final class Ring {
        /** World blocks per column, and so the ring's cell width. */
        final int step;
        /** Blocks along one edge of a region at this level. */
        final int regionSize;
        /** Chunk columns along one edge of a region. */
        final int regionChunks;
        /** Outer edge of the ring's band, in blocks from the player. */
        final int outerBlocks;
        /**
         * Grid the footprint's edges snap to: the next ring's region size. This
         * is what makes the boundary between two rings fall on a region edge of
         * the coarser one, so no coarse region is ever half covered by fine
         * ones.
         */
        final int alignBlocks;
        /** Chunk columns between coverage probes; keeps far rings affordable. */
        final int probeStride;
        final int probesPerRegion;
        /** Charge against the per-tick build budget for one region build. */
        final int buildCostUnits;

        final Set<Long> wanted = new HashSet<Long>();
        final Set<Long> resident = new HashSet<Long>();
        final Set<Long> queued = new HashSet<Long>();
        final Set<Long> hidden = new HashSet<Long>();
        final ArrayDeque<Long> buildQueue = new ArrayDeque<Long>();
        /**
         * Regions posted to the worker and not yet answered for.
         *
         * <p>Distinct from {@code resident}: a region becomes resident when its
         * build lands, not when it is asked for. Keeping the two apart is what
         * stops the same region being posted twice while the first attempt is
         * still meshing.
         */
        final Set<Long> building = new HashSet<Long>();

        /**
         * Epoch of the newest tile each resident region was built from.
         *
         * <p>The scheduler no longer knows or cares where a region's ground came
         * from -- loaded chunks, a cache file, a server -- because the store
         * answers for all of them. What it still needs is to notice that the
         * answer has changed, and the epoch stamped on each tile is that
         * signal. A region whose recorded epoch has fallen behind the tiles
         * under it is rebuilt, which is how partially known ground turns into
         * complete terrain instead of staying half meshed forever.
         *
         * <p>Per tile rather than store-wide, and that is the difference
         * between a field that settles and one that never does. The store's
         * own epoch moves on every write anywhere -- every chunk the sampler
         * records, every coarse tile derived -- so measured against it, every
         * resident region was stale within a tick of the player's own ground
         * being sampled, and the whole field re-meshed in rotation for as long
         * as the player kept walking: a couple of hundred region builds, each
         * a few milliseconds on the worker and, wherever the bytes had
         * genuinely changed, a fresh mesh for Remix to upload on the frame.
         * Measured against the nine tiles a build actually reads, a region
         * rebuilds when its own ground changes and not when ground a kilometre
         * away does.
         */
        final Map<Long, Integer> regionBuildEpoch = new HashMap<Long, Integer>();

        boolean active;
        boolean hasFootprint;
        /** Footprint in blocks, half open: [minX, maxX) x [minZ, maxZ). */
        int minX;
        int minZ;
        int maxX;
        int maxZ;

        Ring(int step, int outerBlocks, int alignBlocks) {
            this.step = step;
            this.regionSize = RemixLodBridge.REGION_COLUMNS * step;
            this.regionChunks = this.regionSize / 16;
            this.outerBlocks = outerBlocks;
            this.alignBlocks = alignBlocks;
            this.probeStride = Math.max(1, this.regionChunks / 8);
            int probesPerAxis = this.regionChunks / this.probeStride;
            this.probesPerRegion = probesPerAxis * probesPerAxis;
            // Every level costs the same now. A build reads one tile -- always
            // 1024 cells, whatever ground they cover -- and converts it, where
            // the old path re-sampled the world and so cost more the coarser it
            // got. One budget point is one region at any level.
            this.buildCostUnits = BUILD_COST_UNITS_PER_BUDGET;
        }

        void forgetLocally() {
            wanted.clear();
            resident.clear();
            queued.clear();
            hidden.clear();
            building.clear();
            regionBuildEpoch.clear();
            buildQueue.clear();
            hasFootprint = false;
            active = false;
        }
    }

    private static final Ring[] RINGS = createRings();

    /** Scratch for the footprint recompute, so the tick path does not allocate. */
    private static final int[] NEXT_MIN_X = new int[RINGS.length];
    private static final int[] NEXT_MIN_Z = new int[RINGS.length];
    private static final boolean[] RING_MOVED = new boolean[RINGS.length];
    /** Scratch wanted set; one ring is refreshed at a time. */
    private static final Set<Long> SCRATCH_WANTED = new HashSet<Long>();

    /** Rings in use, counted from the finest. Driven by the distance setting. */
    private static int activeRingCount = ringCountForDistance(
            RemixLodBridge.LOD_RING_OUTER_BLOCKS[1]);

    /** Region builds allowed per tick, so a reveal never stalls the frame. */
    private static int buildBudgetPerTick = 1;

    /**
     * Fraction of a region's chunk probes that must have native geometry before
     * the LOD under it is hidden.
     *
     * <p>Not all of them: sections outside the frustum and beyond the anti-cull
     * distance are never captured, so a region behind the player would never
     * reach full coverage and its LOD would sit in view indefinitely.
     */
    private static float coverageHideFraction = 0.8f;

    /**
     * Regions this close are hidden regardless of measured coverage. Real
     * chunks are normally present at this range, so anything still showing here
     * is a coverage blind spot rather than terrain that needs standing in for.
     *
     * <p>Keep it small. LOD can only be built where the client already holds
     * chunks, which on a vanilla server is a radius of about ten chunks, so a
     * large always-hide radius hides the only band the field can cover and
     * distant terrain disappears entirely. Measured coverage is the rule that
     * matters; this is only a floor under it.
     */
    private static int alwaysHideWithinBlocks = 96;

    /**
     * Held across the moment a build lands, and across every drop of the field.
     *
     * <p>Without it a build that started before a world change could submit
     * after the clear meant to remove it, leaving one world's ground standing in
     * another. Under it the two orderings are the only ones possible: either the
     * build submitted before the clear and the clear dropped it, or the build
     * sees the new generation and never submits.
     */
    private static final Object SUBMIT_LOCK = new Object();

    /**
     * Bumped whenever the field is dropped. Written under {@link #SUBMIT_LOCK};
     * volatile so the completion path can read it without taking the lock.
     */
    private static volatile int buildGeneration;

    /** Builds the worker has finished, waiting for a tick to apply them. */
    private static final ArrayDeque<BuildResult> COMPLETED = new ArrayDeque<BuildResult>();

    /** Builds posted and not yet finished. Counted by the worker, read here. */
    private static final AtomicInteger buildsInFlight = new AtomicInteger();

    private static int coverageSweepCountdown;
    private static int dataSweepCountdown;
    private static int statusLogCountdown;

    private static boolean enabled = true;
    private static boolean loggedFirstRegion;
    private static boolean loggedEmptyExtract;
    private static boolean loggedSubmitFailure;
    private static int lastRebuildToken;
    private static int lastCacheEpoch;
    private static fd lastWorld;
    private static String lastStatusLine = "";

    /** One finished build, as the worker leaves it for the next tick. */
    private static final class BuildResult {
        final int generation;
        final int ringIndex;
        final long regionKey;
        final int outcome;
        final int windowEpoch;

        BuildResult(int generation, int ringIndex, long regionKey, int outcome, int windowEpoch) {
            this.generation = generation;
            this.ringIndex = ringIndex;
            this.regionKey = regionKey;
            this.outcome = outcome;
            this.windowEpoch = windowEpoch;
        }
    }

    /**
     * One region build, as it runs on the store's worker.
     *
     * <p>Everything it needs is copied in at post time. It reads no scheduler
     * state and writes none: the only thing it leaves behind is a
     * {@link BuildResult}, which the next tick applies.
     */
    private static final class RegionBuild implements Runnable {
        private final int generation;
        private final int ringIndex;
        private final long regionKey;
        private final int step;
        private final int windowEpoch;

        RegionBuild(int generation, int ringIndex, long regionKey, int step, int windowEpoch) {
            this.generation = generation;
            this.ringIndex = ringIndex;
            this.regionKey = regionKey;
            this.step = step;
            this.windowEpoch = windowEpoch;
        }

        public void run() {
            int outcome = BUILD_REFUSED;
            try {
                outcome = runRegionBuild(generation, regionKey, step);
            } catch (Throwable failure) {
                RemixLodBridge.log("lod: region build failed: " + failure);
            } finally {
                // Counted down here rather than where the result is applied,
                // because a result whose generation has moved on is dropped
                // without ever being looked at, and the ceiling on in-flight
                // builds has to come back down either way.
                buildsInFlight.decrementAndGet();
                synchronized (COMPLETED) {
                    COMPLETED.addLast(new BuildResult(
                            generation, ringIndex, regionKey, outcome, windowEpoch));
                }
            }
        }
    }

    private RemixLodCapture() {
    }

    /**
     * Longest prefix of the step ladder the field can actually draw.
     *
     * <p>Two things have to hold of a step, and neither was worth stating while
     * the ladder was four fixed entries that nobody was going to touch. They
     * became worth stating the moment it grew a fifth and turned into something
     * that gets extended.
     *
     * <p>The first is that every step is exactly twice the one before it. Both
     * radius rules in {@link #createRings()} rest on it without saying so: it is
     * what makes a ring's {@code alignBlocks} twice its own region size, and so
     * makes {@code widthGrid} equal to that region size, which is the only
     * reading under which the first rule means "a whole number of regions". Any
     * other ratio does not fail loudly -- it half-covers coarse regions at the
     * ring boundary, which is the single thing the whole scheme exists to
     * prevent.
     *
     * <p>The second is that a step is a level {@link mcrtx.lod.format.LodTileKey}
     * can key. A step is {@code 2^level} in that pyramid and the pyramid stops
     * at {@code MAX_LEVEL}, so a step past it is the quiet failure rather than
     * the loud one: {@code levelForStep} returns -1, every region at the level
     * reports no tile, and the ring sits in the build queue forever drawing
     * nothing.
     *
     * <p>Truncating rather than refusing outright, for the same reason the
     * radius rules adjust rather than throw: the rings inside a bad entry are
     * still exactly correct, and a player is better served by a shorter field
     * than by no distant terrain and a line in a log they will not read.
     */
    private static int usableRingCount() {
        int[] steps = RemixLodBridge.LOD_RING_STEPS;
        int usable = Math.min(steps.length, RemixLodBridge.LOD_RING_OUTER_BLOCKS.length);
        for (int index = 0; index < usable; index++) {
            String problem = null;
            if (index > 0 && steps[index] != 2 * steps[index - 1]) {
                problem = "is not twice the step inside it (" + steps[index - 1] + ")";
            } else if (LodTileColumns.levelForStep(steps[index]) < 0) {
                problem = "is past LodTileKey.MAX_LEVEL, so no tile can ever exist for it";
            }
            if (problem != null) {
                RemixLodBridge.log("lod: ring step " + steps[index] + " " + problem
                        + "; the field stops at " + index + " rings");
                return index;
            }
        }
        return usable;
    }

    /**
     * Builds the ring table, checking the properties the exact tiling rests on
     * and repairing the table rather than drawing a broken field if any fails.
     */
    private static Ring[] createRings() {
        int[] steps = RemixLodBridge.LOD_RING_STEPS;
        int ringCount = usableRingCount();
        Ring[] rings = new Ring[ringCount];
        int previousOuter = 0;
        for (int index = 0; index < ringCount; index++) {
            int regionSize = RemixLodBridge.ringRegionSize(index);
            // The outermost ring has no coarser neighbour to align against, so
            // it aligns to its own grid. "Outermost" is the last usable ring
            // rather than the last in the table: a ring aligned to a grid the
            // field never draws would leave its own footprint edges off its own
            // region grid.
            int alignBlocks = index + 1 < ringCount
                    ? RemixLodBridge.ringRegionSize(index + 1)
                    : regionSize;
            int outer = RemixLodBridge.LOD_RING_OUTER_BLOCKS[index];

            // A footprint is centred by snapping both its edges to alignBlocks.
            // Both edges then move together -- and the footprint keeps a fixed
            // size, so the region count never breathes -- only if the width is
            // a whole number of that grid. Half of alignBlocks is this ring's
            // own region size, so this reads as "a whole number of regions".
            int widthGrid = alignBlocks / 2;
            int roundedOuter = ((outer + widthGrid - 1) / widthGrid) * widthGrid;

            // Each ring must also clear the one inside it by two of its own
            // regions, or the inner footprint can poke outside the outer one as
            // the player moves across the grid, and the band between them stops
            // being covered by anything.
            int minimumOuter = previousOuter + 2 * regionSize;
            if (roundedOuter < minimumOuter) {
                roundedOuter = ((minimumOuter + widthGrid - 1) / widthGrid) * widthGrid;
            }
            if (roundedOuter != outer) {
                RemixLodBridge.log("lod: ring step " + steps[index] + " radius adjusted "
                        + outer + " -> " + roundedOuter + " blocks to keep rings tiling");
            }

            rings[index] = new Ring(steps[index], roundedOuter, alignBlocks);
            previousOuter = roundedOuter;
        }
        return rings;
    }

    public static void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (!enabled) {
            clear();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Furthest edge of the LOD field, in blocks. This is what decides how many
     * rings are drawn: every ring whose band starts inside the requested
     * distance is switched on, and the field ends at that ring's own edge.
     */
    public static void setDistanceBlocks(int blocks) {
        int rings = ringCountForDistance(blocks);
        if (rings == activeRingCount) {
            return;
        }
        activeRingCount = rings;
        rebuildAll();
    }

    public static int getDistanceBlocks() {
        if (RINGS.length == 0) {
            return 0;
        }
        return RINGS[activeRingCount - 1].outerBlocks;
    }

    /**
     * Rings a requested distance switches on; always at least the finest.
     *
     * <p>Counted against the rings that were actually built rather than against
     * the whole radius table, so a distance the settings page can still offer
     * after {@link #createRings()} truncated the ladder selects the furthest
     * ring that exists instead of indexing past it.
     */
    public static int ringCountForDistance(int blocks) {
        int[] outer = RemixLodBridge.LOD_RING_OUTER_BLOCKS;
        int available = Math.min(outer.length, RINGS.length);
        int rings = 1;
        for (int index = 1; index < available; index++) {
            // A ring earns its place once the requested distance reaches past
            // where the ring inside it stops.
            if (blocks > outer[index - 1]) {
                rings = index + 1;
            }
        }
        return rings;
    }

    public static int activeRingCount() {
        return activeRingCount;
    }

    /** Radius inside which LOD always defers to full-detail chunk geometry. */
    public static void setHandoverDistanceBlocks(int blocks) {
        alwaysHideWithinBlocks = Math.max(0, blocks);
    }

    public static void setCoverageHideFraction(float fraction) {
        if (fraction < 0.0f) {
            fraction = 0.0f;
        } else if (fraction > 1.0f) {
            fraction = 1.0f;
        }
        coverageHideFraction = fraction;
    }

    public static void setBuildBudgetPerTick(int budget) {
        buildBudgetPerTick = Math.max(1, budget);
    }

    public static int residentRegionCount() {
        int total = 0;
        for (int index = 0; index < RINGS.length; index++) {
            total += RINGS[index].resident.size();
        }
        return total;
    }

    /** Tiles the store currently holds, across every level. */
    public static int storedTileCount() {
        return LodStore.tileCount();
    }

    /** Regions holding geometry that chunk handover has not hidden. */
    public static int visibleRegionCount() {
        int total = 0;
        for (int index = 0; index < RINGS.length; index++) {
            total += RINGS[index].resident.size() - RINGS[index].hidden.size();
        }
        return total;
    }

    /**
     * Switches the disk cache on or off. Everything resident is dropped either
     * way: with it off the cached regions have to go, and with it on the empty
     * ground they were skipped over is worth another look.
     */
    public static void setCacheEnabled(boolean value) {
        if (LodStore.isEnabled() == value) {
            return;
        }
        LodStore.setEnabled(value);
        LodSampling.reset();
        rebuildAll();
    }

    public static boolean isCacheEnabled() {
        return LodStore.isEnabled();
    }

    public static void onWorldChanged() {
        // The world about to be left is still the bound one, so this is the last
        // chance to write what was observed in it.
        LodStore.flush();
        LodSampling.reset();
        clear();
    }

    /**
     * Drops every region so the next ticks rebuild them. Needed when something
     * baked into the mesh changes, such as the debug material.
     */
    public static void rebuildAll() {
        clear();
    }

    public static void clear() {
        // Both halves under the lock, so a build in flight cannot slip its mesh
        // in between the generation moving and the regions being dropped.
        synchronized (SUBMIT_LOCK) {
            clearLocalState();
            RemixLodBridge.clearRegions();
        }
        lastRebuildToken = RemixLodBridge.rebuildToken();
    }

    /**
     * Forgets what this scheduler believes the native side holds, without
     * telling the native side to drop anything.
     *
     * <p>Moving the generation on is part of forgetting. A build posted before
     * this point is meshing ground for a field that no longer exists, and the
     * generation is what tells it so before it submits.
     */
    private static void clearLocalState() {
        synchronized (SUBMIT_LOCK) {
            buildGeneration++;
            for (int index = 0; index < RINGS.length; index++) {
                RINGS[index].forgetLocally();
            }
        }
        coverageSweepCountdown = 0;
        dataSweepCountdown = 0;
        statusLogCountdown = 0;
        lastStatusLine = "";
    }

    /** Call once per client tick with the player's block position. */
    public static void tick(fd world, double playerX, double playerZ) {
        if (!enabled || world == null) {
            return;
        }

        // Rebuilding the LOD materials destroys every region mesh with them,
        // and that happens on paths this scheduler never hears about: a texture
        // pack change, a material setting. Without this the regions stay
        // resident here, are never resubmitted, and the field goes blank for
        // the rest of the session.
        int rebuildToken = RemixLodBridge.rebuildToken();
        if (rebuildToken != lastRebuildToken) {
            lastRebuildToken = rebuildToken;
            clearLocalState();
        }

        // A dimension swap can hand this a different world object by a path the
        // world-change hook does not report, which LodStore.bind already says of
        // itself. That mattered little while builds were synchronous; with them
        // in flight across ticks it decides which world a mesh belongs to, so
        // the field is dropped here rather than left to blend two worlds.
        if (world != lastWorld) {
            lastWorld = world;
            clear();
        }

        int playerBlockX = (int) Math.floor(playerX);
        int playerBlockZ = (int) Math.floor(playerZ);

        LodStore.bind(world);
        LodStore.setPlayerPosition(playerBlockX, playerBlockZ);

        // The store loads on a background thread, so the regions passed over
        // while it was still reading have to be offered it once it is there.
        int storeEpoch = LodStore.epoch();

        // Before anything reads residency, so a region that landed since the
        // last tick is resident by the time the footprint is recomputed.
        applyCompletedBuilds();

        if (storeEpoch != lastCacheEpoch) {
            lastCacheEpoch = storeEpoch;
            queueRegionsWithoutGeometry();
        }

        refreshFootprints(playerBlockX, playerBlockZ);

        if (--dataSweepCountdown <= 0) {
            dataSweepCountdown = DATA_SWEEP_INTERVAL_TICKS;
            queueRegionsWithStaleData();
        }

        drainBuildQueue();
        LodSampling.tick(world, playerBlockX >> 4, playerBlockZ >> 4);
        LodStore.maybeSave();

        if (--coverageSweepCountdown <= 0) {
            coverageSweepCountdown = COVERAGE_SWEEP_INTERVAL_TICKS;
            syncRegionHandover(playerX, playerZ);
        }

        if (--statusLogCountdown <= 0) {
            statusLogCountdown = STATUS_LOG_INTERVAL_TICKS;
            logStatusIfChanged();
        }
    }

    /**
     * Recomputes where every ring sits and, when anything moved, rebuilds the
     * wanted sets.
     *
     * <p>A ring's footprint is the square of side twice its radius whose edges
     * are snapped to the next ring's region grid. Snapping both edges with the
     * same rounding keeps the width exactly twice the radius, so the footprint
     * jumps a whole region at a time and never changes size, and every edge
     * lands where a coarser region begins.
     */
    private static void refreshFootprints(int playerBlockX, int playerBlockZ) {
        boolean changed = false;
        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            boolean active = index < activeRingCount;
            int minX = ring.minX;
            int minZ = ring.minZ;
            if (active) {
                minX = roundToGrid(playerBlockX - ring.outerBlocks, ring.alignBlocks);
                minZ = roundToGrid(playerBlockZ - ring.outerBlocks, ring.alignBlocks);
            }
            NEXT_MIN_X[index] = minX;
            NEXT_MIN_Z[index] = minZ;
            RING_MOVED[index] = active != ring.active || !ring.hasFootprint
                    || minX != ring.minX || minZ != ring.minZ;
            changed = changed || RING_MOVED[index];
        }

        if (!changed) {
            return;
        }

        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            ring.active = index < activeRingCount;
            ring.minX = NEXT_MIN_X[index];
            ring.minZ = NEXT_MIN_Z[index];
            ring.maxX = ring.minX + 2 * ring.outerBlocks;
            ring.maxZ = ring.minZ + 2 * ring.outerBlocks;
            ring.hasFootprint = true;
        }

        // A coarse ring jumps a whole region at a time, so it stays put through
        // most of the moves that shift the fine rings. Only the rings that moved
        // -- and the one immediately outside each of them, whose hole those
        // moves cut -- need their wanted sets recomputed. Rebuilding all four
        // every time would throw away the queue the outer rings are still
        // working through and make them re-probe ground they already know
        // nothing about.
        for (int index = 0; index < RINGS.length; index++) {
            if (RING_MOVED[index] || (index > 0 && RING_MOVED[index - 1])) {
                refreshRing(index);
            }
        }

        // The field moved, so regions that found no chunk data before may sit
        // over loaded ground now. Do not wait out the sweep interval for it.
        dataSweepCountdown = 0;
    }

    /**
     * Recomputes one ring's wanted set, evicts what fell out of it, and queues
     * what is newly in it.
     *
     * <p>The hole punched by the ring inside this one is what stops two detail
     * levels drawing the same ground. It is tested against region origins alone,
     * which is only sound because the inner footprint's edges are snapped to
     * this ring's region size: a region whose origin is inside the hole is
     * inside it entirely.
     */
    private static void refreshRing(int index) {
        Ring ring = RINGS[index];
        Set<Long> wanted = SCRATCH_WANTED;
        wanted.clear();

        if (ring.active) {
            boolean hasHole = false;
            int holeMinX = 0;
            int holeMinZ = 0;
            int holeMaxX = 0;
            int holeMaxZ = 0;
            if (index > 0 && RINGS[index - 1].active) {
                Ring inner = RINGS[index - 1];
                hasHole = true;
                holeMinX = inner.minX;
                holeMinZ = inner.minZ;
                holeMaxX = inner.maxX;
                holeMaxZ = inner.maxZ;
            }

            for (int originZ = ring.minZ; originZ < ring.maxZ; originZ += ring.regionSize) {
                for (int originX = ring.minX; originX < ring.maxX; originX += ring.regionSize) {
                    if (hasHole
                            && originX >= holeMinX && originX < holeMaxX
                            && originZ >= holeMinZ && originZ < holeMaxZ) {
                        continue;
                    }
                    wanted.add(Long.valueOf(regionKey(originX, originZ)));
                }
            }
        }

        // Evict what left the footprint, and queue only what is newly inside it.
        // Requeueing everything instead would throw away the ring's knowledge of
        // which of its regions have no chunk data under them, and it would spend
        // the whole inspection budget rediscovering that on every move. Regions
        // that are merely still empty are the data sweep's job.
        List<Long> stale = new ArrayList<Long>();
        for (Long key : ring.wanted) {
            if (!wanted.contains(key)) {
                stale.add(key);
            }
        }
        for (int staleIndex = 0; staleIndex < stale.size(); staleIndex++) {
            Long key = stale.get(staleIndex);
            if (ring.resident.remove(key)) {
                long value = key.longValue();
                RemixLodBridge.unloadRegion(unpackOriginX(value), unpackOriginZ(value), ring.step);
            }
            ring.hidden.remove(key);
            ring.regionBuildEpoch.remove(key);
            // Left in the deque; the drain drops queue entries that are no
            // longer wanted, which is cheaper than searching the deque here.
            ring.queued.remove(key);
        }

        for (Long key : wanted) {
            if (!ring.wanted.contains(key)
                    && !ring.resident.contains(key)
                    && !ring.building.contains(key)
                    && ring.queued.add(key)) {
                ring.buildQueue.addLast(key);
            }
        }

        ring.wanted.clear();
        ring.wanted.addAll(wanted);
    }

    /**
     * Queues every region whose tiles have changed since the mesh it currently
     * has was built from them.
     *
     * <p>This is the retry path as much as the refresh path: a region that
     * found nothing is one with no stored epoch, so it comes back the moment a
     * single chunk under it loads.
     */
    private static void queueRegionsWithStaleData() {
        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            if (!ring.active) {
                continue;
            }
            for (Long key : ring.wanted) {
                if (ring.queued.contains(key) || ring.building.contains(key)) {
                    continue;
                }

                // A region with no recorded epoch has no mesh yet, so it is
                // always worth another attempt; one whose epoch has fallen
                // behind was built before the store learned something new
                // about the ground it reads. Nine map lookups per region --
                // its tile and the apron's -- a few times a second is what
                // that costs, against a build for every region every time.
                Integer builtAt = ring.regionBuildEpoch.get(key);
                if (builtAt != null) {
                    long value = key.longValue();
                    int originX = unpackOriginX(value);
                    int originZ = unpackOriginZ(value);
                    if (builtAt.intValue() == LodTileColumns.windowEpoch(originX, originZ, ring.step)) {
                        // Up to date as far as the store knows -- but the store
                        // may still owe this ground a read of the save it had
                        // to turn away, and nothing but the sweep asks twice.
                        LodTileColumns.retryDiskRead(originX, originZ, ring.step);
                        continue;
                    }
                }

                ring.queued.add(key);
                ring.buildQueue.addLast(key);
            }
        }
    }

    /**
     * Queues every wanted region that has no mesh yet.
     *
     * <p>Only used when the cache finishes loading. Regions with no chunks under
     * them are dropped from the queue rather than retried, and the data sweep
     * only brings them back when a chunk arrives -- which, out where the cache
     * is the point, never happens. Without this the entire outer field would
     * stay empty until the player walked far enough to move the footprint.
     */
    private static void queueRegionsWithoutGeometry() {
        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            if (!ring.active) {
                continue;
            }
            for (Long key : ring.wanted) {
                if (ring.resident.contains(key) || ring.queued.contains(key)
                        || ring.building.contains(key)) {
                    continue;
                }
                ring.queued.add(key);
                ring.buildQueue.addLast(key);
            }
        }
    }

    /**
     * Spends the per-tick build budget across the rings, finest first.
     *
     * <p>Finest first because the near band is both the most visible and the
     * one that has to catch up after the field moves: when the innermost
     * footprint advances, the ground it takes over has to be re-meshed at the
     * finer level before it looks right again.
     *
     * <p>What a tick spends here is a probe and a post. The probe is the free
     * miss the budget depends on: over ground the store holds no tile for --
     * most of a fresh queue, and most of the outer field forever -- it costs a
     * map lookup and the entry is left to come back on a later sweep, rather
     * than taking a build slot to find out there was nothing to build.
     */
    private static void drainBuildQueue() {
        int budget = buildBudgetPerTick * BUILD_COST_UNITS_PER_BUDGET;
        int inspections = buildBudgetPerTick * INSPECTIONS_PER_BUDGET;

        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            if (!ring.active) {
                continue;
            }
            while (budget > 0 && inspections > 0 && !ring.buildQueue.isEmpty()) {
                if (buildsInFlight.get() >= MAX_BUILDS_IN_FLIGHT) {
                    // The worker is behind. Leaving the entries queued costs
                    // nothing and they are looked at again next tick.
                    return;
                }

                Long key = ring.buildQueue.pollFirst();
                ring.queued.remove(key);
                if (!ring.wanted.contains(key) || ring.building.contains(key)) {
                    // The field moved out from under this entry while it
                    // waited, or the worker already has it.
                    continue;
                }
                inspections--;
                long value = key.longValue();
                int originX = unpackOriginX(value);
                int originZ = unpackOriginZ(value);

                // One source now. Whether this ground was walked, read off disk
                // or sent by a server is the store's business; the scheduler
                // asks whether there is a tile and either there is or there is
                // not. Nothing recorded either way, so the next sweep retries.
                if (!LodTileColumns.hasTileFor(originX, originZ, ring.step)) {
                    if (!loggedEmptyExtract) {
                        loggedEmptyExtract = true;
                        RemixLodBridge.log("lod: first region had no tile at "
                                + originX + "," + originZ + " step " + ring.step
                                + " (not sampled or derived yet?)");
                    }
                    continue;
                }

                // Stamped at post time rather than when the build lands: a
                // tile that changes while the region is meshing is then newer
                // than the record, and the next sweep rebuilds it.
                ring.building.add(key);
                buildsInFlight.incrementAndGet();
                LodStore.postRegionBuild(new RegionBuild(
                        buildGeneration, index, value, ring.step,
                        LodTileColumns.windowEpoch(originX, originZ, ring.step)));
                budget -= ring.buildCostUnits;
            }
            if (budget <= 0 || inspections <= 0) {
                break;
            }
        }
    }

    /**
     * Meshes one region and hands it to Remix. Runs on the store's worker.
     *
     * <p>The generation is checked with the submission itself rather than
     * before the work, because the work is where the time goes: a build that
     * turns out to be for a world the player has left has still cost nothing
     * anyone can see, where a check taken early would be stale by the time it
     * mattered.
     */
    private static int runRegionBuild(int generation, long regionKey, int step) {
        int originX = unpackOriginX(regionKey);
        int originZ = unpackOriginZ(regionKey);

        if (workerColumns == null) {
            workerColumns = new int[RemixLodBridge.REGION_WORDS];
        }
        int filled = LodTileColumns.fill(originX, originZ, step, workerColumns);
        if (filled == LodTileColumns.FILL_NO_TILE) {
            // The tile went out of the store between the probe and here.
            return BUILD_NO_DATA;
        }
        if (filled == LodTileColumns.FILL_EMPTY) {
            return BUILD_NO_SURFACE;
        }

        synchronized (SUBMIT_LOCK) {
            if (generation != buildGeneration) {
                return BUILD_STALE;
            }
            boolean submitted = RemixLodBridge.submitRegion(
                    originX, originZ, step,
                    RemixLodBridge.RENDER_PASS_OPAQUE, workerColumns);
            RemixLodBridge.submitRegion(
                    originX, originZ, step,
                    RemixLodBridge.RENDER_PASS_TRANSLUCENT, workerColumns);
            return submitted ? BUILD_SUBMITTED : BUILD_REFUSED;
        }
    }

    /**
     * Takes what the worker finished into the scheduler's own bookkeeping.
     *
     * <p>Residency is recorded here rather than where the build was posted,
     * because until a build lands there is nothing resident to record: a region
     * the store turned out to have nothing for, or one dropped because the world
     * changed under it, must not be left marked as drawn.
     */
    private static void applyCompletedBuilds() {
        while (true) {
            BuildResult result;
            synchronized (COMPLETED) {
                result = COMPLETED.pollFirst();
            }
            if (result == null) {
                return;
            }
            if (result.generation != buildGeneration) {
                // The field was dropped while this was in flight. Nothing to
                // undo: the build checked the same generation under the same
                // lock the clear took, so either it never submitted or the
                // clear removed what it submitted.
                continue;
            }

            Ring ring = RINGS[result.ringIndex];
            Long key = Long.valueOf(result.regionKey);
            ring.building.remove(key);

            int originX = unpackOriginX(result.regionKey);
            int originZ = unpackOriginZ(result.regionKey);

            if (!ring.wanted.contains(key)) {
                // The footprint moved while this was meshing. The eviction pass
                // could not unload it, because a region only becomes resident
                // when its build lands and this one had not landed yet, so the
                // mesh is dropped here instead of being left with no owner.
                if (result.outcome == BUILD_SUBMITTED) {
                    RemixLodBridge.unloadRegion(originX, originZ, ring.step);
                }
                continue;
            }

            if (result.outcome == BUILD_SUBMITTED) {
                ring.resident.add(key);
                ring.regionBuildEpoch.put(key, Integer.valueOf(result.windowEpoch));
                // A region built over ground that chunks already cover should
                // be hidden on the next tick, not after a full sweep interval.
                coverageSweepCountdown = 0;
                if (!loggedFirstRegion) {
                    loggedFirstRegion = true;
                    RemixLodBridge.log("lod region hook active; " + activeRingCount
                            + " rings, " + getDistanceBlocks() + " blocks, "
                            + wantedRegionCount() + " regions");
                }
            } else if (result.outcome == BUILD_NO_SURFACE) {
                // A tile that exists but holds nothing to stand on -- open sky,
                // or ground entirely below the world. Recording the epoch stops
                // it being retried every sweep; a refused submission is left
                // unrecorded, because that one is worth retrying.
                ring.regionBuildEpoch.put(key, Integer.valueOf(result.windowEpoch));
            } else if (result.outcome == BUILD_REFUSED && !loggedSubmitFailure) {
                loggedSubmitFailure = true;
                RemixLodBridge.log("lod: native refused region at "
                        + originX + "," + originZ + " step " + ring.step);
            }
        }
    }

    /**
     * Hides a region once full-detail chunks have actually taken over the
     * ground it covers, and shows it again when they have not.
     *
     * <p>Distance alone is the wrong trigger: chunks arrive before their meshes
     * are built, so dropping the LOD on proximity leaves a hole for as long as
     * the chunk build queue takes to catch up. The LOD surface is sunk slightly
     * below true terrain height, so the two overlapping for a few frames is
     * harmless -- real geometry simply occludes it.
     */
    private static void syncRegionHandover(double playerX, double playerZ) {
        double handoverBlocks = effectiveHandoverBlocks();

        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            if (!ring.active || ring.resident.isEmpty()) {
                continue;
            }
            for (Long key : ring.resident) {
                long value = key.longValue();
                int originX = unpackOriginX(value);
                int originZ = unpackOriginZ(value);
                double gap = regionGapBlocks(originX, originZ, ring.regionSize, playerX, playerZ);

                // The distance trigger measures the region's *far* corner, not
                // its near edge. Hiding on the near edge hides a whole region
                // the moment any part of it comes close, and a region is 64
                // blocks across at step 2 and 128 at step 4 -- so a step-4
                // region could have its far corner nearly 280 blocks out and
                // still be hidden on the strength of one nearby corner. Chunk
                // geometry does not reach that far, so nothing drew the rest of
                // it and the ground opened into a hole. Requiring the whole
                // region to be inside the handover radius costs a little
                // overlap, which is free: the LOD surface is sunk below true
                // terrain height and real geometry simply occludes it.
                double far = regionFarBlocks(originX, originZ, ring.regionSize, playerX, playerZ);

                boolean covered = far <= handoverBlocks
                        || (gap <= COVERAGE_PROBE_RANGE_BLOCKS
                                && isCoveredByChunkGeometry(ring, originX, originZ));
                boolean hidden = ring.hidden.contains(key);
                if (covered == hidden) {
                    continue;
                }

                RemixLodBridge.setRegionHidden(originX, originZ, ring.step, covered);
                if (covered) {
                    ring.hidden.add(key);
                } else {
                    ring.hidden.remove(key);
                }
            }
        }
    }

    /** True when enough of this region's chunk probes already have native geometry. */
    private static boolean isCoveredByChunkGeometry(Ring ring, int originX, int originZ) {
        int baseChunkX = originX >> 4;
        int baseChunkZ = originZ >> 4;

        int meshed = 0;
        for (int offsetZ = 0; offsetZ < ring.regionChunks; offsetZ += ring.probeStride) {
            for (int offsetX = 0; offsetX < ring.regionChunks; offsetX += ring.probeStride) {
                if (RemixChunkWorldState.columnHasNativeMesh(
                        baseChunkX + offsetX, baseChunkZ + offsetZ)) {
                    meshed++;
                }
            }
        }

        return meshed >= (int) Math.ceil(ring.probesPerRegion * coverageHideFraction);
    }

    /** Distance from the player to a region's nearest edge; zero when inside it. */
    private static double regionGapBlocks(
            int originX, int originZ, int regionSize, double playerX, double playerZ) {
        double dx = axisGap(playerX, originX, originX + regionSize);
        double dz = axisGap(playerZ, originZ, originZ + regionSize);
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Radius inside which distant terrain always defers to real chunks.
     *
     * <p>Capped by what vanilla is actually drawing. The configured value suits
     * the default render distance, but on a shorter one it reaches well past
     * where vanilla stops, leaving a ring containing neither real chunks nor
     * LOD -- which is why the field appeared to start far too far out once the
     * render distance was turned down.
     */
    private static double effectiveHandoverBlocks() {
        double configured = alwaysHideWithinBlocks;
        float vanillaViewDistance = RemixFogCapture.lastViewDistanceBlocks();
        if (vanillaViewDistance > 0.0f && vanillaViewDistance < configured) {
            return vanillaViewDistance;
        }
        return configured;
    }

    /**
     * Distance to the furthest corner of a region.
     *
     * <p>The question the handover actually asks is "is every part of this
     * region covered", and only the corner furthest from the player can answer
     * it. {@link #regionGapBlocks} answers the different question of whether the
     * region is near at all, which is the right test for whether probing its
     * chunk coverage is worth the lookups.
     */
    private static double regionFarBlocks(
            int originX, int originZ, int regionSize, double playerX, double playerZ) {
        double dx = axisFarReach(playerX, originX, originX + regionSize);
        double dz = axisFarReach(playerZ, originZ, originZ + regionSize);
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Distance along one axis to whichever end of the span is further away. */
    private static double axisFarReach(double value, double min, double max) {
        double toMin = Math.abs(value - min);
        double toMax = Math.abs(value - max);
        return toMin > toMax ? toMin : toMax;
    }

    private static double axisGap(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0;
    }

    private static int wantedRegionCount() {
        int total = 0;
        for (int index = 0; index < RINGS.length; index++) {
            total += RINGS[index].wanted.size();
        }
        return total;
    }

    /**
     * Writes one line to mcrtx.log whenever the shape of the field changes.
     *
     * <p>Distant terrain fails silently by nature -- an empty field and a fully
     * hidden one look identical from inside the game -- so the counts that tell
     * those apart are worth a line in a log that survives the session. The
     * per-ring breakdown is what makes a wrong ring layout diagnosable without
     * the game in front of you: a ring with regions wanted but none resident is
     * ground with no chunk data, and one with regions resident but none visible
     * is a handover that has swallowed the whole band.
     */
    private static void logStatusIfChanged() {
        StringBuilder status = new StringBuilder(256);
        status.append("lod status: resident=").append(residentRegionCount())
                .append(" visible=").append(visibleRegionCount())
                .append(" hidden=").append(residentRegionCount() - visibleRegionCount())
                .append(" queued=").append(queuedRegionCount())
                .append(" building=").append(buildsInFlight.get())
                .append(" wanted=").append(wantedRegionCount())
                .append(" rings=").append(activeRingCount)
                .append(" handover=").append(alwaysHideWithinBlocks)
                .append(" distance=").append(getDistanceBlocks())
                .append(" tris=").append(RemixLodBridge.triangleCount(true))
                .append("/").append(RemixLodBridge.triangleCount(false));

        // How much ground the store is holding, and how much of it this session
        // put there. From inside the game a field that never populates looks the
        // same whether the store loaded nothing or the sampler is not running,
        // and these two numbers are what tell them apart.
        status.append(" store=");
        if (!LodStore.isEnabled()) {
            status.append("off");
        } else if (!LodStore.isReady()) {
            status.append("loading");
        } else {
            status.append(LodStore.tileCount()).append("tiles");
        }
        status.append(" sampled=").append(LodSampling.sampledCount()).append("chunks");

        // And how much of it came from the world's own save rather than from
        // walking. A ring that fills only where the player has been says the
        // region reader is not running -- a server, the Nether, a moved save --
        // and nothing else in this line would distinguish that from a save that
        // genuinely holds no ground out there.
        status.append(" disk=").append(LodStore.diskTilesRead()).append("tiles");
        int pendingDiskReads = LodStore.diskReadsPending();
        if (pendingDiskReads > 0) {
            status.append("+").append(pendingDiskReads).append("q");
        }

        for (int index = 0; index < RINGS.length; index++) {
            Ring ring = RINGS[index];
            if (!ring.active) {
                continue;
            }
            int inner = index > 0 ? RINGS[index - 1].outerBlocks : 0;
            status.append(" | s").append(ring.step)
                    .append(" ").append(inner).append("-").append(ring.outerBlocks)
                    .append(" want=").append(ring.wanted.size())
                    .append(" res=").append(ring.resident.size())
                    .append(" vis=").append(ring.resident.size() - ring.hidden.size())
                    .append(" q=").append(ring.buildQueue.size());
        }

        String line = status.toString();
        if (line.equals(lastStatusLine)) {
            return;
        }
        lastStatusLine = line;
        RemixLodBridge.log(line);
    }

    private static int queuedRegionCount() {
        int total = 0;
        for (int index = 0; index < RINGS.length; index++) {
            total += RINGS[index].buildQueue.size();
        }
        return total;
    }

    /** Nearest multiple of {@code grid}, rounding half up and away from zero correctly. */
    private static int roundToGrid(int value, int grid) {
        return Math.floorDiv(value + (grid / 2), grid) * grid;
    }

    private static long regionKey(int originX, int originZ) {
        return ((long) originX << 32) | (originZ & 0xFFFFFFFFL);
    }

    private static int unpackOriginX(long key) {
        return (int) (key >> 32);
    }

    private static int unpackOriginZ(long key) {
        return (int) key;
    }
}
