import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.RemixLifecycleBridge;

final class RemixChunkRecapturePass {
    private static final int CHUNK_DIMENSION = 16;
    private static final int DIRTY_REGION_FULL_SCAN_BLOCK_THRESHOLD = 2048;

    private RemixChunkRecapturePass() {
    }

    static boolean recapture(fd world, DirtyChunkSection section, boolean allowNeighborRefresh) {
        RemixCaveCulling.Pocket[] pockets = RemixCaveCulling.computePockets(
                world, section.originX, section.originY, section.originZ);
        RemixCaveCulling.setPockets(section.originX, section.originY, section.originZ, pockets);

        if (!RemixCaveCulling.isVisible(section.originX, section.originY, section.originZ)) {
            return true;
        }

        boolean fullSectionScan = shouldUseFullSectionScan(section);
        if (!capturePass(world, section, 0, allowNeighborRefresh, fullSectionScan)) {
            return false;
        }
        return capturePass(world, section, 1, allowNeighborRefresh, fullSectionScan);
    }

    static boolean shouldUseFullSectionScan(DirtyChunkSection section) {
        return section.coversWholeSection()
                || section.dirtyBlockVolume() >= DIRTY_REGION_FULL_SCAN_BLOCK_THRESHOLD;
    }

    static int effectiveScanBlockCount(DirtyChunkSection section, boolean fullSectionScan) {
        return fullSectionScan
                ? CHUNK_DIMENSION * CHUNK_DIMENSION * CHUNK_DIMENSION
                : section.dirtyBlockVolume();
    }

    private static boolean capturePass(
            fd world,
            DirtyChunkSection section,
            int renderPass,
            boolean allowNeighborRefresh,
            boolean fullSectionScan) {
        if (world == null
                || !RemixLifecycleBridge.isInitialized()
                || RemixChunkBridge.isChunkBuildCaptureActive()) {
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
        if (!RemixChunkBridge.beginChunkBuild(
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
        RemixChunkWorldState.markSectionResident(
                section.originX, section.originY, section.originZ);
        long beginBuildEndNanos = System.nanoTime();
        int capturedBlocks = 0;

        for (int blockY = scanMinY; blockY <= scanMaxY; blockY++) {
            if (blockY < 0 || blockY >= 128) {
                continue;
            }
            for (int blockZ = scanMinZ; blockZ <= scanMaxZ; blockZ++) {
                for (int blockX = scanMinX; blockX <= scanMaxX; blockX++) {
                    int blockId = world.a(blockX, blockY, blockZ);
                    if (blockId <= 0 || blockId >= uu.m.length) {
                        continue;
                    }
                    uu blockDefinition = uu.m[blockId];
                    if (blockDefinition == null) {
                        continue;
                    }

                    emittedGeometry = true;
                    capturedBlocks += 1;
                    RemixChunkBlockCapture.captureWorldBlock(
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

        RemixChunkBridge.endChunkBuild(emittedGeometry, true, allowNeighborRefresh);
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
}
