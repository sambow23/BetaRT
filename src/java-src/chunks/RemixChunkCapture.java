public final class RemixChunkCapture {
    private RemixChunkCapture() {
    }

    public static fd attachedWorld() {
        return RemixChunkWorldState.attachedWorld();
    }

    public static void onWorldChanged(fd world) {
        RemixChunkWorldState.onWorldChanged(world);
    }

    public static void onChunkUpdateStart(int originX, int originY, int originZ) {
        RemixChunkRecaptureQueue.queueSection(originX, originY, originZ);
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
