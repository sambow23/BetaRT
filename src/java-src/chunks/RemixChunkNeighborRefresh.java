import mcrtx.bridge.RemixChunkBridge;

final class RemixChunkNeighborRefresh {
    private static final double RECOVERY_DISTANCE_SQ = 96.0 * 96.0;
    private static final double CRITICAL_DISTANCE_SQ = 64.0 * 64.0;

    private RemixChunkNeighborRefresh() {
    }

    static boolean shouldSchedule(DirtyChunkSection section, int queueDepthBefore) {
        if (queueDepthBefore < RemixChunkRecaptureQueue.BACKLOG_RECOVERY_QUEUE_DEPTH) {
            return true;
        }

        double distanceSq = RemixChunkRecaptureQueue.sectionDistanceScore(
                section,
                RemixCameraState.cameraPositionX,
                RemixCameraState.cameraPositionY,
                RemixCameraState.cameraPositionZ);
        double cutoffSq = queueDepthBefore >= RemixChunkRecaptureQueue.BACKLOG_CRITICAL_QUEUE_DEPTH
                ? CRITICAL_DISTANCE_SQ
                : RECOVERY_DISTANCE_SQ;
        return distanceSq <= cutoffSq;
    }

    static void flushDeferred() {
        RemixChunkBridge.flushChunkNeighborRefreshes();
    }
}
