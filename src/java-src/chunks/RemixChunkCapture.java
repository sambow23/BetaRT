public final class RemixChunkCapture {
    private RemixChunkCapture() {
    }

    public static fd attachedWorld() {
        return RemixChunkWorldState.attachedWorld();
    }

    public static void onChunkSectionUnload(int originX, int originY, int originZ) {
        RemixChunkWorldState.onChunkSectionUnload(originX, originY, originZ);
    }

    public static void onWorldChanged(fd world) {
        RemixChunkWorldState.onWorldChanged(world);
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        return RemixChunkBuildSession.begin(
                originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        RemixChunkBuildSession.captureBlock(
                blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        RemixChunkBuildSession.end(emittedGeometry);
    }

    static void queueRecaptureRegion(
            int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        RemixChunkRecaptureQueue.queueRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static void flushPendingChunkRecaptures() {
        RemixChunkRecaptureQueue.flush();
    }

    static void clearPendingRecaptures() {
        RemixChunkRecaptureQueue.clear();
    }

    static int lastPendingQueueDepthBeforeFlush() {
        return RemixChunkRecaptureQueue.lastPendingQueueDepthBeforeFlush();
    }

    static int lastPendingQueueDepthAfterFlush() {
        return RemixChunkRecaptureQueue.lastPendingQueueDepthAfterFlush();
    }

    static int lastSectionsRecaptured() {
        return RemixChunkRecaptureQueue.lastSectionsRecaptured();
    }

    static long lastFlushDurationNanos() {
        return RemixChunkRecaptureQueue.lastFlushDurationNanos();
    }
}
