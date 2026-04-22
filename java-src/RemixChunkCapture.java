import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.MinecraftRenderHooks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class RemixChunkCapture {
    private static final int CHUNK_DIMENSION = 16;
    private static final int MIN_RECAPTURE_SECTIONS_PER_PRESENT = 2;
    private static final int MAX_RECAPTURE_SECTIONS_PER_PRESENT = 8;
    private static final int BACKLOG_ADMISSION_PRESSURE_QUEUE_DEPTH = 128;
    private static final int BACKLOG_ADMISSION_PRESSURE_LIMIT = 8;
    private static final int BACKLOG_ADMISSION_RECOVERY_LIMIT = 4;
    private static final int BACKLOG_ADMISSION_CRITICAL_LIMIT = 2;
    private static final int OVERSIZED_REGION_SECTION_COUNT = 16;
    private static final int OVERSIZED_REGION_ADMISSION_LIMIT = 12;
    private static final int BACKLOG_RECOVERY_QUEUE_DEPTH = 256;
    private static final int BACKLOG_CRITICAL_QUEUE_DEPTH = 1024;
    private static final int BACKLOG_RECOVERY_MIN_BUDGET = 4;
    private static final int BACKLOG_CRITICAL_MIN_BUDGET = 6;
    private static final int DIRTY_REGION_FULL_SCAN_BLOCK_THRESHOLD = 2048;
    private static final double NEIGHBOR_REFRESH_RECOVERY_DISTANCE_SQ = 96.0 * 96.0;
    private static final double NEIGHBOR_REFRESH_CRITICAL_DISTANCE_SQ = 64.0 * 64.0;
    private static final long TARGET_RECAPTURE_FLUSH_NANOS = 4_500_000L;
    private static final long FAST_RECAPTURE_FLUSH_NANOS = 3_000_000L;
    private static final int WATER_STILL_BLOCK_ID = 8;
    private static final int WATER_FLOWING_BLOCK_ID = 9;
    private static final int LAVA_STILL_BLOCK_ID = 10;
    private static final int LAVA_FLOWING_BLOCK_ID = 11;
    private static final RemixWorldListener WORLD_LISTENER = new RemixWorldListener();
    private static final LinkedHashMap<DirtyChunkSection, DirtyChunkSection> PENDING_RECAPTURE_SECTIONS = new LinkedHashMap<DirtyChunkSection, DirtyChunkSection>();

    private static boolean loggedChunkBuild;
    private static boolean loggedWorldListenerAttach;
    private static fd attachedWorld;
    private static int lastPendingQueueDepthBeforeFlush;
    private static int lastPendingQueueDepthAfterFlush;
    private static int lastSectionsRecaptured;
    private static long lastFlushDurationNanos;
    private static int recaptureSectionsBudget = MAX_RECAPTURE_SECTIONS_PER_PRESENT;

    private RemixChunkCapture() {
    }

    private static DirtyChunkSection newDirtySectionForRegion(
            int originX,
            int originY,
            int originZ,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ) {
        return new DirtyChunkSection(originX, originY, originZ, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void clearPendingRecaptureForSection(int originX, int originY, int originZ) {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.remove(new DirtyChunkSection(originX, originY, originZ));
        }
    }

    private static boolean shouldUseFullSectionScan(DirtyChunkSection section) {
        return section.coversWholeSection() || section.dirtyBlockVolume() >= DIRTY_REGION_FULL_SCAN_BLOCK_THRESHOLD;
    }

    private static int effectiveSectionScanBlockCount(DirtyChunkSection section, boolean fullSectionScan) {
        return fullSectionScan ? CHUNK_DIMENSION * CHUNK_DIMENSION * CHUNK_DIMENSION : section.dirtyBlockVolume();
    }

    public static fd attachedWorld() {
        return attachedWorld;
    }

    public static void onChunkSectionUnload(int originX, int originY, int originZ) {
        clearPendingRecaptureForSection(originX, originY, originZ);
        MinecraftRenderHooks.unloadChunkSection(originX, originY, originZ);
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
        recaptureSectionsBudget = MAX_RECAPTURE_SECTIONS_PER_PRESENT;
        if (attachedWorld != null) {
            attachedWorld.a(WORLD_LISTENER);
            if (!loggedWorldListenerAttach) {
                loggedWorldListenerAttach = true;
                System.out.println("[mcrtx] world listener attached");
            }
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
        return MinecraftRenderHooks.beginChunkBuild(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
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
        MinecraftRenderHooks.endChunkBuild(emittedGeometry, false);
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

    private static boolean recaptureChunkSectionPass(
            fd world,
            DirtyChunkSection section,
            int renderPass,
            boolean allowNeighborRefresh,
            boolean fullSectionScan) {
        if (world == null || !MinecraftRenderHooks.isInitialized() || MinecraftRenderHooks.isChunkBuildCaptureActive()) {
            return false;
        }

        int scanMinX = fullSectionScan ? section.originX : section.dirtyMinX;
        int scanMinY = fullSectionScan ? section.originY : section.dirtyMinY;
        int scanMinZ = fullSectionScan ? section.originZ : section.dirtyMinZ;
        int scanMaxX = fullSectionScan ? section.originX + CHUNK_DIMENSION - 1 : section.dirtyMaxX;
        int scanMaxY = fullSectionScan ? section.originY + CHUNK_DIMENSION - 1 : section.dirtyMaxY;
        int scanMaxZ = fullSectionScan ? section.originZ + CHUNK_DIMENSION - 1 : section.dirtyMaxZ;

        long passStartNanos = System.nanoTime();
        boolean emittedGeometry = false;
        if (!MinecraftRenderHooks.beginChunkBuild(
                section.originX,
                section.originY,
                section.originZ,
                CHUNK_DIMENSION,
                CHUNK_DIMENSION,
                CHUNK_DIMENSION,
                renderPass,
                scanMinX,
                scanMinY,
                scanMinZ,
                scanMaxX,
                scanMaxY,
                scanMaxZ)) {
            return false;
        }
        long beginBuildEndNanos = System.nanoTime();
        int capturedBlocks = 0;

        for (int blockY = scanMinY; blockY <= scanMaxY; ++blockY) {
            if (blockY < 0 || blockY >= 128) {
                continue;
            }
            for (int blockZ = scanMinZ; blockZ <= scanMaxZ; ++blockZ) {
                for (int blockX = scanMinX; blockX <= scanMaxX; ++blockX) {
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
            capturedBlocks += 1;
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
            long scanBlocksEndNanos = System.nanoTime();

        MinecraftRenderHooks.endChunkBuild(emittedGeometry, true, allowNeighborRefresh);
            long endBuildEndNanos = System.nanoTime();
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.pass.beginBuild",
                beginBuildEndNanos - passStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.pass.scanBlocks",
                scanBlocksEndNanos - beginBuildEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.pass.endBuild",
                endBuildEndNanos - scanBlocksEndNanos);
            HookProfiler.recordCount("chunkRecapture.passCapturedBlocks", capturedBlocks);
        return true;
    }

        private static boolean recaptureChunkSection(fd world, DirtyChunkSection section, boolean allowNeighborRefresh) {
            boolean fullSectionScan = shouldUseFullSectionScan(section);
            if (!recaptureChunkSectionPass(world, section, 0, allowNeighborRefresh, fullSectionScan)) {
            return false;
        }

            return recaptureChunkSectionPass(world, section, 1, allowNeighborRefresh, fullSectionScan);
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

        List<DirtyChunkSection> candidateSections = new ArrayList<DirtyChunkSection>();

        for (int originY = originMinY; originY <= originMaxY; originY += CHUNK_DIMENSION) {
            for (int originZ = originMinZ; originZ <= originMaxZ; originZ += CHUNK_DIMENSION) {
                for (int originX = originMinX; originX <= originMaxX; originX += CHUNK_DIMENSION) {
                    candidateSections.add(newDirtySectionForRegion(
                            originX,
                            originY,
                            originZ,
                            minX,
                            minY,
                            minZ,
                            maxX,
                            maxY,
                            maxZ));
                }
            }
        }

        if (candidateSections.isEmpty()) {
            return;
        }

        int queueDepthBefore;
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            queueDepthBefore = PENDING_RECAPTURE_SECTIONS.size();
        }

        int admissionLimit = effectiveQueueAdmissionLimit(queueDepthBefore, candidateSections.size());
        if (admissionLimit < candidateSections.size()) {
            sortSectionsByCameraDistance(candidateSections);
            candidateSections = new ArrayList<DirtyChunkSection>(candidateSections.subList(0, admissionLimit));
        }

        int sectionsAdded = 0;
        int sectionsDeduped = 0;
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            for (DirtyChunkSection section : candidateSections) {
                DirtyChunkSection existing = PENDING_RECAPTURE_SECTIONS.get(section);
                if (existing == null) {
                    PENDING_RECAPTURE_SECTIONS.put(section, section);
                    sectionsAdded += 1;
                } else {
                    existing.mergeDirtyRegion(
                            section.dirtyMinX,
                            section.dirtyMinY,
                            section.dirtyMinZ,
                            section.dirtyMaxX,
                            section.dirtyMaxY,
                            section.dirtyMaxZ);
                    sectionsDeduped += 1;
                }
            }
        }

        int sectionsRequested = ((originMaxX - originMinX) / CHUNK_DIMENSION + 1)
                * ((originMaxY - originMinY) / CHUNK_DIMENSION + 1)
                * ((originMaxZ - originMinZ) / CHUNK_DIMENSION + 1);
        int sectionsDropped = sectionsRequested - candidateSections.size();
        HookProfiler.recordCount("chunkRecapture.sectionsQueuedRequested", sectionsRequested);
        HookProfiler.recordCount("chunkRecapture.sectionsQueuedAdded", sectionsAdded);
        HookProfiler.recordCount("chunkRecapture.sectionsQueuedDeduped", sectionsDeduped);
        HookProfiler.recordCount("chunkRecapture.sectionsQueuedDropped", sectionsDropped);
        HookProfiler.recordCount("chunkRecapture.queueAdmissionLimit", admissionLimit);
        HookProfiler.recordCount("chunkRecapture.queueDepthOnEnqueue", queueDepthBefore);
    }

    private static int effectiveQueueAdmissionLimit(int queueDepthBefore, int requestedSections) {
        if (requestedSections <= 0) {
            return 0;
        }

        int admissionLimit = requestedSections;
        if (requestedSections >= OVERSIZED_REGION_SECTION_COUNT) {
            admissionLimit = Math.min(admissionLimit, OVERSIZED_REGION_ADMISSION_LIMIT);
        }
        if (queueDepthBefore >= BACKLOG_CRITICAL_QUEUE_DEPTH) {
            return Math.min(admissionLimit, BACKLOG_ADMISSION_CRITICAL_LIMIT);
        }
        if (queueDepthBefore >= BACKLOG_RECOVERY_QUEUE_DEPTH) {
            return Math.min(admissionLimit, BACKLOG_ADMISSION_RECOVERY_LIMIT);
        }
        if (queueDepthBefore >= BACKLOG_ADMISSION_PRESSURE_QUEUE_DEPTH) {
            return Math.min(admissionLimit, BACKLOG_ADMISSION_PRESSURE_LIMIT);
        }
        return admissionLimit;
    }

    private static void sortSectionsByCameraDistance(List<DirtyChunkSection> sections) {
        final double cameraX = RemixCameraState.cameraPositionX;
        final double cameraY = RemixCameraState.cameraPositionY;
        final double cameraZ = RemixCameraState.cameraPositionZ;

        Collections.sort(sections, new Comparator<DirtyChunkSection>() {
            @Override
            public int compare(DirtyChunkSection left, DirtyChunkSection right) {
                double leftScore = sectionDistanceScore(left, cameraX, cameraY, cameraZ);
                double rightScore = sectionDistanceScore(right, cameraX, cameraY, cameraZ);
                if (leftScore < rightScore) {
                    return -1;
                }
                if (leftScore > rightScore) {
                    return 1;
                }
                if (left.originY != right.originY) {
                    return left.originY - right.originY;
                }
                if (left.originX != right.originX) {
                    return left.originX - right.originX;
                }
                return left.originZ - right.originZ;
            }
        });
    }

    private static boolean shouldScheduleNeighborRefresh(DirtyChunkSection section, int queueDepthBefore) {
        if (queueDepthBefore < BACKLOG_RECOVERY_QUEUE_DEPTH) {
            return true;
        }

        double distanceSq = sectionDistanceScore(
                section,
                RemixCameraState.cameraPositionX,
                RemixCameraState.cameraPositionY,
                RemixCameraState.cameraPositionZ);
        double cutoffSq = queueDepthBefore >= BACKLOG_CRITICAL_QUEUE_DEPTH
                ? NEIGHBOR_REFRESH_CRITICAL_DISTANCE_SQ
                : NEIGHBOR_REFRESH_RECOVERY_DISTANCE_SQ;
        return distanceSq <= cutoffSq;
    }

    private static int effectiveRecaptureBudget(int queueDepthBefore) {
        if (queueDepthBefore <= 0) {
            return 0;
        }
        if (recaptureSectionsBudget < MIN_RECAPTURE_SECTIONS_PER_PRESENT) {
            recaptureSectionsBudget = MIN_RECAPTURE_SECTIONS_PER_PRESENT;
        } else if (recaptureSectionsBudget > MAX_RECAPTURE_SECTIONS_PER_PRESENT) {
            recaptureSectionsBudget = MAX_RECAPTURE_SECTIONS_PER_PRESENT;
        }

        int effectiveBudget = recaptureSectionsBudget;
        if (queueDepthBefore >= BACKLOG_CRITICAL_QUEUE_DEPTH) {
            effectiveBudget = Math.max(effectiveBudget, BACKLOG_CRITICAL_MIN_BUDGET);
        } else if (queueDepthBefore >= BACKLOG_RECOVERY_QUEUE_DEPTH) {
            effectiveBudget = Math.max(effectiveBudget, BACKLOG_RECOVERY_MIN_BUDGET);
        }
        return Math.min(queueDepthBefore, effectiveBudget);
    }

    private static void tuneRecaptureBudget(long flushDurationNanos, int queueDepthBefore, int sectionsRecaptured) {
        if (sectionsRecaptured <= 0) {
            return;
        }

        if (queueDepthBefore >= BACKLOG_CRITICAL_QUEUE_DEPTH) {
            if (recaptureSectionsBudget < BACKLOG_CRITICAL_MIN_BUDGET) {
                recaptureSectionsBudget = BACKLOG_CRITICAL_MIN_BUDGET;
            } else if (flushDurationNanos < TARGET_RECAPTURE_FLUSH_NANOS
                    && recaptureSectionsBudget < MAX_RECAPTURE_SECTIONS_PER_PRESENT) {
                recaptureSectionsBudget += 1;
            }
            return;
        }

        if (queueDepthBefore >= BACKLOG_RECOVERY_QUEUE_DEPTH && recaptureSectionsBudget < BACKLOG_RECOVERY_MIN_BUDGET) {
            recaptureSectionsBudget = BACKLOG_RECOVERY_MIN_BUDGET;
            return;
        }

        if (flushDurationNanos > TARGET_RECAPTURE_FLUSH_NANOS
                && queueDepthBefore < BACKLOG_RECOVERY_QUEUE_DEPTH
                && recaptureSectionsBudget > MIN_RECAPTURE_SECTIONS_PER_PRESENT) {
            recaptureSectionsBudget -= 1;
            return;
        }

        if (flushDurationNanos < FAST_RECAPTURE_FLUSH_NANOS
                && queueDepthBefore > recaptureSectionsBudget
                && recaptureSectionsBudget < MAX_RECAPTURE_SECTIONS_PER_PRESENT) {
            recaptureSectionsBudget += 1;
        }
    }

    private static List<DirtyChunkSection> selectSectionsToRecapture(int budget) {
        List<DirtyChunkSection> queuedSections = new ArrayList<DirtyChunkSection>();
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            if (PENDING_RECAPTURE_SECTIONS.isEmpty() || budget <= 0) {
                return queuedSections;
            }
            queuedSections.addAll(PENDING_RECAPTURE_SECTIONS.values());
        }

        if (queuedSections.size() <= budget) {
            return queuedSections;
        }

        List<DirtyChunkSection> sectionsToRecapture = new ArrayList<DirtyChunkSection>(budget);
        int oldestReserve = 0;
        if (budget > 1) {
            oldestReserve = 1;
            if (queuedSections.size() >= BACKLOG_CRITICAL_QUEUE_DEPTH) {
                oldestReserve = Math.min(2, budget - 1);
            }
        }

        for (int index = 0; index < oldestReserve; index += 1) {
            sectionsToRecapture.add(queuedSections.remove(0));
        }

        sortSectionsByCameraDistance(queuedSections);

        int nearestBudget = budget - sectionsToRecapture.size();
        sectionsToRecapture.addAll(queuedSections.subList(0, nearestBudget));
        return sectionsToRecapture;
    }

    private static double sectionDistanceScore(DirtyChunkSection section, double cameraX, double cameraY, double cameraZ) {
        double centerX = section.originX + CHUNK_DIMENSION * 0.5;
        double centerY = section.originY + CHUNK_DIMENSION * 0.5;
        double centerZ = section.originZ + CHUNK_DIMENSION * 0.5;
        double dx = centerX - cameraX;
        double dy = centerY - cameraY;
        double dz = centerZ - cameraZ;
        return dx * dx + dy * dy + dz * dz;
    }

    public static void flushPendingChunkRecaptures() {
        long flushStartNanos = System.nanoTime();
        int queueDepthBefore = 0;
        int sectionsRecaptured = 0;
        if (attachedWorld == null || !MinecraftRenderHooks.isInitialized()) {
            synchronized (PENDING_RECAPTURE_SECTIONS) {
                lastPendingQueueDepthBeforeFlush = 0;
                lastPendingQueueDepthAfterFlush = PENDING_RECAPTURE_SECTIONS.size();
            }
            lastSectionsRecaptured = 0;
            lastFlushDurationNanos = System.nanoTime() - flushStartNanos;
            HookProfiler.recordCount("chunkRecapture.queueDepth", lastPendingQueueDepthAfterFlush);
            HookProfiler.recordCount("chunkRecapture.sectionsFlushed", 0L);
            return;
        }

        synchronized (PENDING_RECAPTURE_SECTIONS) {
            queueDepthBefore = PENDING_RECAPTURE_SECTIONS.size();
            if (queueDepthBefore == 0) {
                lastPendingQueueDepthBeforeFlush = 0;
                lastPendingQueueDepthAfterFlush = 0;
                lastSectionsRecaptured = 0;
                lastFlushDurationNanos = System.nanoTime() - flushStartNanos;
                HookProfiler.recordCount("chunkRecapture.queueDepth", 0L);
                HookProfiler.recordCount("chunkRecapture.sectionsFlushed", 0L);
                return;
            }
        }

        int sectionsBudget = effectiveRecaptureBudget(queueDepthBefore);
        List<DirtyChunkSection> sectionsToRecapture = selectSectionsToRecapture(sectionsBudget);
        long selectSectionsEndNanos = System.nanoTime();
        int sectionsWithNeighborRefresh = 0;
        int sectionsWithoutNeighborRefresh = 0;
        int sectionsFullScan = 0;
        int sectionsPartialScan = 0;
        int sectionScanBlocks = 0;

        for (DirtyChunkSection selectedSection : sectionsToRecapture) {
            if (MinecraftRenderHooks.isChunkBuildCaptureActive()) {
                break;
            }

            DirtyChunkSection section;
            synchronized (PENDING_RECAPTURE_SECTIONS) {
                section = PENDING_RECAPTURE_SECTIONS.remove(selectedSection);
            }
            if (section == null) {
                continue;
            }

            long sectionStartNanos = System.nanoTime();
            boolean allowNeighborRefresh = shouldScheduleNeighborRefresh(section, queueDepthBefore);
            boolean fullSectionScan = shouldUseFullSectionScan(section);
            if (!recaptureChunkSection(attachedWorld, section, allowNeighborRefresh)) {
                synchronized (PENDING_RECAPTURE_SECTIONS) {
                    PENDING_RECAPTURE_SECTIONS.put(section, section);
                }
                break;
            }
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.section",
                    System.nanoTime() - sectionStartNanos);

            if (fullSectionScan) {
                sectionsFullScan += 1;
            } else {
                sectionsPartialScan += 1;
            }
            sectionScanBlocks += effectiveSectionScanBlockCount(section, fullSectionScan);

            if (allowNeighborRefresh) {
                sectionsWithNeighborRefresh += 1;
            } else {
                sectionsWithoutNeighborRefresh += 1;
            }
            ++sectionsRecaptured;
        }
        long recaptureLoopEndNanos = System.nanoTime();

        if (sectionsWithNeighborRefresh > 0) {
            MinecraftRenderHooks.flushChunkNeighborRefreshes();
        }
        long neighborFlushEndNanos = System.nanoTime();
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            lastPendingQueueDepthBeforeFlush = queueDepthBefore;
            lastPendingQueueDepthAfterFlush = PENDING_RECAPTURE_SECTIONS.size();
        }

        lastSectionsRecaptured = sectionsRecaptured;
        lastFlushDurationNanos = neighborFlushEndNanos - flushStartNanos;
    tuneRecaptureBudget(lastFlushDurationNanos, queueDepthBefore, sectionsRecaptured);
        HookProfiler.recordCount("chunkRecapture.queueDepth", queueDepthBefore);
    HookProfiler.recordCount("chunkRecapture.sectionBudget", sectionsBudget);
        HookProfiler.recordCount("chunkRecapture.sectionsSelected", sectionsToRecapture.size());
        HookProfiler.recordCount("chunkRecapture.sectionsFlushed", sectionsRecaptured);
        HookProfiler.recordCount("chunkRecapture.sectionsFullScan", sectionsFullScan);
        HookProfiler.recordCount("chunkRecapture.sectionsPartialScan", sectionsPartialScan);
        HookProfiler.recordCount("chunkRecapture.sectionScanBlocks", sectionScanBlocks);
        HookProfiler.recordCount("chunkRecapture.sectionsNeighborRefreshed", sectionsWithNeighborRefresh);
        HookProfiler.recordCount("chunkRecapture.sectionsNeighborDeferred", sectionsWithoutNeighborRefresh);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.flush.selectSections",
                selectSectionsEndNanos - flushStartNanos);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.flush.recaptureLoop",
                recaptureLoopEndNanos - selectSectionsEndNanos);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.chunkRecapture.flush.neighborFlush",
                neighborFlushEndNanos - recaptureLoopEndNanos);
    }

    static void clearPendingRecaptures() {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.clear();
        }
        lastPendingQueueDepthBeforeFlush = 0;
        lastPendingQueueDepthAfterFlush = 0;
        lastSectionsRecaptured = 0;
        lastFlushDurationNanos = 0L;
        recaptureSectionsBudget = MAX_RECAPTURE_SECTIONS_PER_PRESENT;
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
