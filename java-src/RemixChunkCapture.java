import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.MinecraftRenderHooks;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReentrantLock;

public final class RemixChunkCapture {
    private static final int CHUNK_DIMENSION = 16;
    private static final int MAX_RECAPTURE_SECTIONS_PER_TICK = 8;
    private static final int WATER_STILL_BLOCK_ID = 8;
    private static final int WATER_FLOWING_BLOCK_ID = 9;
    private static final int LAVA_STILL_BLOCK_ID = 10;
    private static final int LAVA_FLOWING_BLOCK_ID = 11;
    private static final RemixWorldListener WORLD_LISTENER = new RemixWorldListener();
    private static final LinkedHashSet<DirtyChunkSection> PENDING_RECAPTURE_SECTIONS = new LinkedHashSet<DirtyChunkSection>();

    // Serializes vanilla chunk builds (render thread) with worker recaptures.
    // Vanilla mutates singleton block bounds via uu.a(world, x, y, z) and the
    // Java-side capture state (MinecraftRenderHooks.chunkBuildCaptureActive,
    // capturedChunkBlocks) is not thread-safe. Worker acquires per section
    // to let vanilla interleave between worker sections.
    private static final ReentrantLock CHUNK_BUILD_LOCK = new ReentrantLock();

    private static final Object WORKER_SIGNAL = new Object();
    private static volatile Thread recaptureWorker;
    private static volatile boolean workerRunning;
    private static final boolean WORKER_ENABLED = isWorkerEnabled();

    private static boolean loggedChunkBuild;
    private static boolean loggedWorldListenerAttach;
    private static volatile fd attachedWorld;
    private static int lastPendingQueueDepthBeforeFlush;
    private static int lastPendingQueueDepthAfterFlush;
    private static int lastSectionsRecaptured;
    private static long lastFlushDurationNanos;

    private RemixChunkCapture() {
    }

    private static boolean isWorkerEnabled() {
        String value = System.getenv("MCRTX_CHUNK_RECAPTURE_WORKER");
        return value == null || !(value.equals("0") || value.equalsIgnoreCase("false"));
    }

    private static void clearPendingRecaptureForSection(int originX, int originY, int originZ) {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.remove(new DirtyChunkSection(originX, originY, originZ));
        }
    }

    public static fd attachedWorld() {
        return attachedWorld;
    }

    public static void onWorldChanged(fd world) {
        if (attachedWorld == world) {
            return;
        }

        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.clear();
        }
        MinecraftRenderHooks.clearWorldScene();

        if (attachedWorld != null) {
            attachedWorld.b(WORLD_LISTENER);
        }

        attachedWorld = world;
        if (attachedWorld != null) {
            attachedWorld.a(WORLD_LISTENER);
            if (!loggedWorldListenerAttach) {
                loggedWorldListenerAttach = true;
                System.out.println("[mcrtx] world listener attached");
            }
            startWorkerIfNeeded();
        }
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        if (!loggedChunkBuild) {
            loggedChunkBuild = true;
            System.out.println("[mcrtx] chunk build hook active");
        }

        // A live vanilla rebuild for this section makes any queued recapture redundant.
        clearPendingRecaptureForSection(originX, originY, originZ);
        // Serialize with the worker thread to avoid racing on shared block
        // singleton bounds and MinecraftRenderHooks capture state.
        CHUNK_BUILD_LOCK.lock();
        boolean ok = false;
        try {
            ok = MinecraftRenderHooks.beginChunkBuild(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
        } finally {
            if (!ok) {
                CHUNK_BUILD_LOCK.unlock();
            }
        }
        return ok;
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        uu blockDefinition = blockId >= 0 && blockId < uu.m.length ? uu.m[blockId] : null;
        if (blockDefinition == null || blockAccess == null) {
            return;
        }

        blockDefinition.a(blockAccess, blockX, blockY, blockZ);

        int liquidVisibilityMask = 0x3F;
        float liquidHeight0 = 1.0f;
        float liquidHeight1 = 1.0f;
        float liquidHeight2 = 1.0f;
        float liquidHeight3 = 1.0f;
        float liquidFlowAngle = -1000.0f;

        if (isLiquidBlock(blockId) && renderType == 4) {
            liquidVisibilityMask = computeLiquidVisibilityMask(blockDefinition, blockAccess, blockX, blockY, blockZ);
            liquidHeight0 = computeLiquidCornerHeight(blockAccess, blockX, blockY, blockZ, blockDefinition.bA);
            liquidHeight1 = computeLiquidCornerHeight(blockAccess, blockX + 1, blockY, blockZ, blockDefinition.bA);
            liquidHeight2 = computeLiquidCornerHeight(blockAccess, blockX + 1, blockY, blockZ + 1, blockDefinition.bA);
            liquidHeight3 = computeLiquidCornerHeight(blockAccess, blockX, blockY, blockZ + 1, blockDefinition.bA);
            liquidFlowAngle = (float) rp.a(blockAccess, blockX, blockY, blockZ, blockDefinition.bA);
        }

        MinecraftRenderHooks.captureBlock(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 0),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 1),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 2),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 3),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 4),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 5),
                (float) blockDefinition.bs,
                (float) blockDefinition.bt,
                (float) blockDefinition.bu,
                (float) blockDefinition.bv,
                (float) blockDefinition.bw,
                (float) blockDefinition.bx,
                blockDefinition.b(blockAccess, blockX, blockY, blockZ),
                liquidVisibilityMask,
                liquidHeight0,
                liquidHeight1,
                liquidHeight2,
                liquidHeight3,
                liquidFlowAngle);
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        try {
            // Normal per-frame chunk build: neighbors must refresh immediately so
            // the chunk renders correctly this frame.
            MinecraftRenderHooks.endChunkBuild(emittedGeometry, false);
        } finally {
            if (CHUNK_BUILD_LOCK.isHeldByCurrentThread()) {
                CHUNK_BUILD_LOCK.unlock();
            }
        }
    }

    private static void captureWorldBlock(
            fd world,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        uu blockDefinition = blockId >= 0 && blockId < uu.m.length ? uu.m[blockId] : null;
        if (blockDefinition == null || world == null) {
            return;
        }

        xp blockAccess = world;
        blockDefinition.a(blockAccess, blockX, blockY, blockZ);

        int liquidVisibilityMask = 0x3F;
        float liquidHeight0 = 1.0f;
        float liquidHeight1 = 1.0f;
        float liquidHeight2 = 1.0f;
        float liquidHeight3 = 1.0f;
        float liquidFlowAngle = -1000.0f;

        if (isLiquidBlock(blockId) && renderType == 4) {
            liquidVisibilityMask = computeLiquidVisibilityMask(blockDefinition, world, blockX, blockY, blockZ);
            liquidHeight0 = computeLiquidCornerHeight(world, blockX, blockY, blockZ, blockDefinition.bA);
            liquidHeight1 = computeLiquidCornerHeight(world, blockX + 1, blockY, blockZ, blockDefinition.bA);
            liquidHeight2 = computeLiquidCornerHeight(world, blockX + 1, blockY, blockZ + 1, blockDefinition.bA);
            liquidHeight3 = computeLiquidCornerHeight(world, blockX, blockY, blockZ + 1, blockDefinition.bA);
            liquidFlowAngle = (float) rp.a(world, blockX, blockY, blockZ, blockDefinition.bA);
        }

        MinecraftRenderHooks.captureBlock(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 0),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 1),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 2),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 3),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 4),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 5),
                (float) blockDefinition.bs,
                (float) blockDefinition.bt,
                (float) blockDefinition.bu,
                (float) blockDefinition.bv,
                (float) blockDefinition.bw,
                (float) blockDefinition.bx,
                blockDefinition.b(blockAccess, blockX, blockY, blockZ),
                liquidVisibilityMask,
                liquidHeight0,
                liquidHeight1,
                liquidHeight2,
                liquidHeight3,
                liquidFlowAngle);
    }

    private static boolean isWaterBlock(int blockId) {
        return blockId == WATER_STILL_BLOCK_ID || blockId == WATER_FLOWING_BLOCK_ID;
    }

    private static boolean isLavaBlock(int blockId) {
        return blockId == LAVA_STILL_BLOCK_ID || blockId == LAVA_FLOWING_BLOCK_ID;
    }

    private static boolean isLiquidBlock(int blockId) {
        return isWaterBlock(blockId) || isLavaBlock(blockId);
    }

    private static int computeLiquidVisibilityMask(uu blockDefinition, xp blockAccess, int blockX, int blockY, int blockZ) {
        int visibilityMask = 0;
        if (blockDefinition.b(blockAccess, blockX, blockY - 1, blockZ, 0)) {
            visibilityMask |= 1 << 0;
        }
        if (blockDefinition.b(blockAccess, blockX, blockY + 1, blockZ, 1)) {
            visibilityMask |= 1 << 1;
        }
        if (blockDefinition.b(blockAccess, blockX, blockY, blockZ - 1, 2)) {
            visibilityMask |= 1 << 2;
        }
        if (blockDefinition.b(blockAccess, blockX, blockY, blockZ + 1, 3)) {
            visibilityMask |= 1 << 3;
        }
        if (blockDefinition.b(blockAccess, blockX - 1, blockY, blockZ, 4)) {
            visibilityMask |= 1 << 4;
        }
        if (blockDefinition.b(blockAccess, blockX + 1, blockY, blockZ, 5)) {
            visibilityMask |= 1 << 5;
        }
        return visibilityMask;
    }

    private static float computeLiquidCornerHeight(xp blockAccess, int blockX, int blockY, int blockZ, ln material) {
        int sampleWeight = 0;
        float accumulatedHeight = 0.0f;

        for (int sampleIndex = 0; sampleIndex < 4; sampleIndex++) {
            int sampleX = blockX - (sampleIndex & 1);
            int sampleZ = blockZ - (sampleIndex >> 1 & 1);
            if (blockAccess.f(sampleX, blockY + 1, sampleZ) == material) {
                return 1.0f;
            }

            ln sampleMaterial = blockAccess.f(sampleX, blockY, sampleZ);
            if (sampleMaterial == material) {
                int sampleLevel = blockAccess.e(sampleX, blockY, sampleZ);
                if (sampleLevel >= 8 || sampleLevel == 0) {
                    accumulatedHeight += rp.d(sampleLevel) * 10.0f;
                    sampleWeight += 10;
                }
                accumulatedHeight += rp.d(sampleLevel);
                sampleWeight += 1;
                continue;
            }

            if (!sampleMaterial.a()) {
                accumulatedHeight += 1.0f;
                sampleWeight += 1;
            }
        }

        if (sampleWeight == 0) {
            return 1.0f;
        }

        return 1.0f - accumulatedHeight / (float) sampleWeight;
    }

    private static boolean recaptureChunkSectionPass(fd world, int originX, int originY, int originZ, int renderPass) {
        if (world == null || !MinecraftRenderHooks.isInitialized() || MinecraftRenderHooks.isChunkBuildCaptureActive()) {
            return false;
        }

        boolean emittedGeometry = false;
        if (!MinecraftRenderHooks.beginChunkBuild(originX, originY, originZ, CHUNK_DIMENSION, CHUNK_DIMENSION, CHUNK_DIMENSION, renderPass)) {
            return false;
        }

        for (int blockY = originY; blockY < originY + CHUNK_DIMENSION; ++blockY) {
            if (blockY < 0 || blockY >= 128) {
                continue;
            }
            for (int blockZ = originZ; blockZ < originZ + CHUNK_DIMENSION; ++blockZ) {
                for (int blockX = originX; blockX < originX + CHUNK_DIMENSION; ++blockX) {
                    int blockId = world.a(blockX, blockY, blockZ);
                    if (blockId <= 0 || blockId >= uu.m.length) {
                        continue;
                    }

                    uu blockDefinition = uu.m[blockId];
                    if (blockDefinition == null) {
                        continue;
                    }

                    if (blockDefinition.b_() != renderPass) {
                        continue;
                    }

                    emittedGeometry = true;
                    captureWorldBlock(
                            world,
                            blockX,
                            blockY,
                            blockZ,
                            blockId,
                            world.e(blockX, blockY, blockZ),
                            blockDefinition.b());
                }
            }
        }

        MinecraftRenderHooks.endChunkBuild(emittedGeometry, true);
        return true;
    }

    private static boolean recaptureChunkSection(fd world, int originX, int originY, int originZ) {
        if (!recaptureChunkSectionPass(world, originX, originY, originZ, 0)) {
            return false;
        }

        return recaptureChunkSectionPass(world, originX, originY, originZ, 1);
    }

    static void queueRecaptureRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (attachedWorld == null) {
            return;
        }

        if (maxY < 0 || minY >= 128) {
            return;
        }

        minY = Math.max(minY, 0);
        maxY = Math.min(maxY, 127);

        int originMinX = (minX >> 4) << 4;
        int originMinY = (minY >> 4) << 4;
        int originMinZ = (minZ >> 4) << 4;
        int originMaxX = (maxX >> 4) << 4;
        int originMaxY = (maxY >> 4) << 4;
        int originMaxZ = (maxZ >> 4) << 4;

        for (int originY = originMinY; originY <= originMaxY; originY += CHUNK_DIMENSION) {
            for (int originZ = originMinZ; originZ <= originMaxZ; originZ += CHUNK_DIMENSION) {
                for (int originX = originMinX; originX <= originMaxX; originX += CHUNK_DIMENSION) {
                    synchronized (PENDING_RECAPTURE_SECTIONS) {
                        PENDING_RECAPTURE_SECTIONS.add(new DirtyChunkSection(originX, originY, originZ));
                    }
                }
            }
        }
        signalWorker();
    }

    // ------------------------------------------------------------------
    // Chunk recapture worker
    //
    // Runs on its own thread so the heavy "drain dirty sections -> capture
    // blocks -> rebuild meshes" work doesn't add to the render thread's
    // frame time. Synchronizes with vanilla chunk builds via
    // CHUNK_BUILD_LOCK, taking the lock only around a single section at a
    // time so vanilla can interleave.
    // ------------------------------------------------------------------

    static void startWorkerIfNeeded() {
        if (!WORKER_ENABLED) {
            return;
        }
        if (recaptureWorker != null && recaptureWorker.isAlive()) {
            return;
        }
        workerRunning = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runWorkerLoop();
            }
        }, "mcrtx-chunk-recapture");
        thread.setDaemon(true);
        recaptureWorker = thread;
        thread.start();
    }

    static void stopWorker() {
        workerRunning = false;
        signalWorker();
        Thread worker = recaptureWorker;
        if (worker != null) {
            try {
                worker.join(1000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            recaptureWorker = null;
        }
    }

    private static void signalWorker() {
        synchronized (WORKER_SIGNAL) {
            WORKER_SIGNAL.notifyAll();
        }
    }

    private static void runWorkerLoop() {
        while (workerRunning) {
            try {
                synchronized (WORKER_SIGNAL) {
                    while (workerRunning && queueIsEmpty()) {
                        WORKER_SIGNAL.wait(250L);
                    }
                }
                if (!workerRunning) {
                    break;
                }
                drainOnce();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                System.err.println("[mcrtx] chunk recapture worker error: " + t);
                t.printStackTrace();
            }
        }
    }

    private static boolean queueIsEmpty() {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            return PENDING_RECAPTURE_SECTIONS.isEmpty();
        }
    }

    private static void drainOnce() {
        long drainStartNanos = System.nanoTime();
        int queueDepthBefore;
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            queueDepthBefore = PENDING_RECAPTURE_SECTIONS.size();
        }

        fd world = attachedWorld;
        if (world == null || !MinecraftRenderHooks.isInitialized()) {
            synchronized (PENDING_RECAPTURE_SECTIONS) {
                lastPendingQueueDepthBeforeFlush = queueDepthBefore;
                lastPendingQueueDepthAfterFlush = PENDING_RECAPTURE_SECTIONS.size();
            }
            lastSectionsRecaptured = 0;
            lastFlushDurationNanos = System.nanoTime() - drainStartNanos;
            HookProfiler.recordCount("chunkRecapture.queueDepth", queueDepthBefore);
            HookProfiler.recordCount("chunkRecapture.sectionsFlushed", 0L);
            return;
        }

        int sectionsRecaptured = 0;
        for (int i = 0; i < MAX_RECAPTURE_SECTIONS_PER_TICK && workerRunning; ++i) {
            DirtyChunkSection section;
            synchronized (PENDING_RECAPTURE_SECTIONS) {
                Iterator<DirtyChunkSection> iterator = PENDING_RECAPTURE_SECTIONS.iterator();
                if (!iterator.hasNext()) {
                    break;
                }
                section = iterator.next();
                iterator.remove();
            }

            long sectionStartNanos = System.nanoTime();
            CHUNK_BUILD_LOCK.lock();
            try {
                fd currentWorld = attachedWorld;
                if (currentWorld == null || !MinecraftRenderHooks.isInitialized()) {
                    break;
                }
                if (!recaptureChunkSection(currentWorld, section.originX, section.originY, section.originZ)) {
                    // Recapture bailed out -- leave it dropped; will be
                    // re-queued by the world listener if the section is
                    // still dirty.
                    break;
                }
            } finally {
                CHUNK_BUILD_LOCK.unlock();
            }
            HookProfiler.record(HookProfiler.SIDE_HOOK, "chunkRecapture.workerSection",
                    System.nanoTime() - sectionStartNanos);
            ++sectionsRecaptured;
        }

        if (sectionsRecaptured > 0) {
            MinecraftRenderHooks.flushChunkNeighborRefreshes();
        }

        synchronized (PENDING_RECAPTURE_SECTIONS) {
            lastPendingQueueDepthBeforeFlush = queueDepthBefore;
            lastPendingQueueDepthAfterFlush = PENDING_RECAPTURE_SECTIONS.size();
        }
        lastSectionsRecaptured = sectionsRecaptured;
        lastFlushDurationNanos = System.nanoTime() - drainStartNanos;
        HookProfiler.recordCount("chunkRecapture.queueDepth", queueDepthBefore);
        HookProfiler.recordCount("chunkRecapture.sectionsFlushed", sectionsRecaptured);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "chunkRecapture.workerDrain",
                lastFlushDurationNanos);
    }

    static void clearPendingRecaptures() {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.clear();
        }
        lastPendingQueueDepthBeforeFlush = 0;
        lastPendingQueueDepthAfterFlush = 0;
        lastSectionsRecaptured = 0;
        lastFlushDurationNanos = 0L;
    }

    static int lastPendingQueueDepthBeforeFlush() {
        return lastPendingQueueDepthBeforeFlush;
    }

    static int lastPendingQueueDepthAfterFlush() {
        return lastPendingQueueDepthAfterFlush;
    }

    static int lastSectionsRecaptured() {
        return lastSectionsRecaptured;
    }

    static long lastFlushDurationNanos() {
        return lastFlushDurationNanos;
    }
}
