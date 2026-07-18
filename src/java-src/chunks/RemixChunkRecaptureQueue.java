import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.RemixLifecycleBridge;

final class RemixChunkRecaptureQueue {
    private static final int CHUNK_DIMENSION = 16;
    private static final int MIN_RECAPTURE_SECTIONS_PER_PRESENT = 2;
    private static final int MAX_RECAPTURE_SECTIONS_PER_PRESENT = 8;
    private static final int BACKLOG_ADMISSION_PRESSURE_QUEUE_DEPTH = 128;
    private static final int BACKLOG_ADMISSION_PRESSURE_LIMIT = 8;
    private static final int BACKLOG_ADMISSION_RECOVERY_LIMIT = 4;
    private static final int BACKLOG_ADMISSION_CRITICAL_LIMIT = 2;
    private static final int OVERSIZED_REGION_SECTION_COUNT = 16;
    private static final int OVERSIZED_REGION_ADMISSION_LIMIT = 12;
    static final int BACKLOG_RECOVERY_QUEUE_DEPTH = 256;
    static final int BACKLOG_CRITICAL_QUEUE_DEPTH = 1024;
    private static final int BACKLOG_RECOVERY_MIN_BUDGET = 4;
    private static final int BACKLOG_CRITICAL_MIN_BUDGET = 6;
    private static final int FULL_SECTION_BLOCK_COUNT = CHUNK_DIMENSION * CHUNK_DIMENSION * CHUNK_DIMENSION;
    private static final int STEADY_RECAPTURE_BLOCKS_PER_PRESENT = FULL_SECTION_BLOCK_COUNT;
    private static final int RECOVERY_RECAPTURE_BLOCKS_PER_PRESENT = FULL_SECTION_BLOCK_COUNT * 3;
    private static final int CRITICAL_RECAPTURE_BLOCKS_PER_PRESENT = FULL_SECTION_BLOCK_COUNT * 6;
    private static final long TARGET_RECAPTURE_FLUSH_NANOS = 4_500_000L;
    private static final long FAST_RECAPTURE_FLUSH_NANOS = 3_000_000L;
    private static final LinkedHashMap<DirtyChunkSection, DirtyChunkSection> PENDING_RECAPTURE_SECTIONS =
            new LinkedHashMap<DirtyChunkSection, DirtyChunkSection>();

    private static int lastPendingQueueDepthBeforeFlush;
    private static int lastPendingQueueDepthAfterFlush;
    private static int lastSectionsRecaptured;
    private static long lastFlushDurationNanos;
    private static int recaptureSectionsBudget = MAX_RECAPTURE_SECTIONS_PER_PRESENT;

    private RemixChunkRecaptureQueue() {
    }

    static void queueRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (RemixChunkWorldState.attachedWorld() == null) {
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
                    RemixChunkWorldState.rememberKnownSection(originX, originY, originZ);
                    candidateSections.add(new DirtyChunkSection(
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
            candidateSections = new ArrayList<DirtyChunkSection>(
                    candidateSections.subList(0, admissionLimit));
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

    static void clearSection(int originX, int originY, int originZ) {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.remove(new DirtyChunkSection(originX, originY, originZ));
        }
    }

    static void resetForWorldChange() {
        synchronized (PENDING_RECAPTURE_SECTIONS) {
            PENDING_RECAPTURE_SECTIONS.clear();
        }
        recaptureSectionsBudget = MAX_RECAPTURE_SECTIONS_PER_PRESENT;
    }

    static void flush() {
        fd world = RemixChunkWorldState.attachedWorld();
        if (world != null && RemixLifecycleBridge.isInitialized()) {
            int playerX = (int) Math.floor(RemixCameraState.cameraPositionX);
            int playerY = (int) Math.floor(RemixCameraState.cameraPositionY);
            int playerZ = (int) Math.floor(RemixCameraState.cameraPositionZ);
            RemixCaveCulling.updateGlobalCulling(playerX, playerY, playerZ);
            RemixChunkWorldState.syncSectionVisibility();
        }

        long flushStartNanos = System.nanoTime();
        int queueDepthBefore = 0;
        int sectionsRecaptured = 0;
        if (world == null || !RemixLifecycleBridge.isInitialized()) {
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
        int scanBlockBudget = effectiveScanBlockBudget(queueDepthBefore);

        for (DirtyChunkSection selectedSection : sectionsToRecapture) {
            if (RemixChunkBridge.isChunkBuildCaptureActive()) {
                break;
            }

            boolean fullSectionScan = RemixChunkRecapturePass.shouldUseFullSectionScan(selectedSection);
            int estimatedScanBlocks = RemixChunkRecapturePass.effectiveScanBlockCount(
                    selectedSection, fullSectionScan);
            if (sectionsRecaptured > 0
                    && sectionScanBlocks + estimatedScanBlocks > scanBlockBudget) {
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
            boolean allowNeighborRefresh =
                    RemixChunkNeighborRefresh.shouldSchedule(section, queueDepthBefore);
            if (!RemixChunkRecapturePass.recapture(world, section, allowNeighborRefresh)) {
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
            sectionScanBlocks += RemixChunkRecapturePass.effectiveScanBlockCount(
                    section, fullSectionScan);
            if (allowNeighborRefresh) {
                sectionsWithNeighborRefresh += 1;
            } else {
                sectionsWithoutNeighborRefresh += 1;
            }
            sectionsRecaptured += 1;
        }
        long recaptureLoopEndNanos = System.nanoTime();

        if (sectionsWithNeighborRefresh > 0) {
            RemixChunkNeighborRefresh.flushDeferred();
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
        HookProfiler.recordCount("chunkRecapture.scanBlockBudget", scanBlockBudget);
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

    static void clear() {
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

    private static int effectiveScanBlockBudget(int queueDepthBefore) {
        if (queueDepthBefore >= BACKLOG_CRITICAL_QUEUE_DEPTH) {
            return CRITICAL_RECAPTURE_BLOCKS_PER_PRESENT;
        }
        if (queueDepthBefore >= BACKLOG_RECOVERY_QUEUE_DEPTH) {
            return RECOVERY_RECAPTURE_BLOCKS_PER_PRESENT;
        }
        return STEADY_RECAPTURE_BLOCKS_PER_PRESENT;
    }

    private static void tuneRecaptureBudget(
            long flushDurationNanos, int queueDepthBefore, int sectionsRecaptured) {
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

        if (queueDepthBefore >= BACKLOG_RECOVERY_QUEUE_DEPTH
                && recaptureSectionsBudget < BACKLOG_RECOVERY_MIN_BUDGET) {
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

        List<DirtyChunkSection> visibleSections = new ArrayList<DirtyChunkSection>();
        for (DirtyChunkSection section : queuedSections) {
            if (RemixCameraState.shouldCaptureChunkSection(
                    section.originX, section.originY, section.originZ)
                    && RemixCaveCulling.isVisible(
                            section.originX, section.originY, section.originZ)) {
                visibleSections.add(section);
            }
        }
        if (visibleSections.isEmpty()) {
            return new ArrayList<DirtyChunkSection>();
        }
        if (visibleSections.size() <= budget) {
            return visibleSections;
        }

        List<DirtyChunkSection> sectionsToRecapture = new ArrayList<DirtyChunkSection>(budget);
        int oldestReserve = 0;
        if (budget > 1) {
            oldestReserve = 1;
            if (visibleSections.size() >= BACKLOG_CRITICAL_QUEUE_DEPTH) {
                oldestReserve = Math.min(2, budget - 1);
            }
        }
        for (int index = 0; index < oldestReserve; index++) {
            sectionsToRecapture.add(visibleSections.remove(0));
        }

        sortSectionsByCameraDistance(visibleSections);
        int nearestBudget = budget - sectionsToRecapture.size();
        sectionsToRecapture.addAll(visibleSections.subList(0, nearestBudget));
        return sectionsToRecapture;
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

    static double sectionDistanceScore(
            DirtyChunkSection section, double cameraX, double cameraY, double cameraZ) {
        double centerX = section.originX + CHUNK_DIMENSION * 0.5;
        double centerY = section.originY + CHUNK_DIMENSION * 0.5;
        double centerZ = section.originZ + CHUNK_DIMENSION * 0.5;
        double dx = centerX - cameraX;
        double dy = centerY - cameraY;
        double dz = centerZ - cameraZ;
        return dx * dx + dy * dy + dz * dz;
    }
}
