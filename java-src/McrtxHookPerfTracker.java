import java.util.Locale;

final class McrtxHookPerfTracker {
    private static final int PERF_LOG_INTERVAL_FRAMES = 60;

    private static long perfFrameCount;
    private static long perfTotalFrameNanos;
    private static long perfMaxFrameNanos;
    private static long perfTotalRenderMethodNanos;
    private static long perfMaxRenderMethodNanos;
    private static long perfTotalPrePresentNanos;
    private static long perfMaxPrePresentNanos;
    private static long perfTotalFlushNanos;
    private static long perfMaxFlushNanos;
    private static long perfTotalPresentNanos;
    private static long perfMaxPresentNanos;
    private static long perfTotalPostPresentNanos;
    private static long perfMaxPostPresentNanos;
    private static long perfTotalQueueDepthBeforeFlush;
    private static int perfMaxQueueDepthBeforeFlush;
    private static long perfTotalQueueDepthAfterFlush;
    private static int perfMaxQueueDepthAfterFlush;
    private static long perfTotalSectionsRecaptured;
    private static int perfMaxSectionsRecaptured;
    private static long activeRenderMethodStartNanos;

    private McrtxHookPerfTracker() {
    }

    static void reset() {
        perfFrameCount = 0L;
        perfTotalFrameNanos = 0L;
        perfMaxFrameNanos = 0L;
        perfTotalRenderMethodNanos = 0L;
        perfMaxRenderMethodNanos = 0L;
        perfTotalPrePresentNanos = 0L;
        perfMaxPrePresentNanos = 0L;
        perfTotalFlushNanos = 0L;
        perfMaxFlushNanos = 0L;
        perfTotalPresentNanos = 0L;
        perfMaxPresentNanos = 0L;
        perfTotalPostPresentNanos = 0L;
        perfMaxPostPresentNanos = 0L;
        perfTotalQueueDepthBeforeFlush = 0L;
        perfMaxQueueDepthBeforeFlush = 0;
        perfTotalQueueDepthAfterFlush = 0L;
        perfMaxQueueDepthAfterFlush = 0;
        perfTotalSectionsRecaptured = 0L;
        perfMaxSectionsRecaptured = 0;
        activeRenderMethodStartNanos = 0L;
    }

    static void onFrameRenderStart() {
        activeRenderMethodStartNanos = System.nanoTime();
    }

    static long renderMethodStartNanos() {
        return activeRenderMethodStartNanos;
    }

    static void clearRenderMethodStartNanos() {
        activeRenderMethodStartNanos = 0L;
    }

    static void recordPresent(
            long frameNanos,
            long renderMethodNanos,
            long prePresentNanos,
            long hookFlushNanos,
            long presentNanos,
            long postPresentNanos,
            long chunkFlushNanos,
            int queueDepthBeforeFlush,
            int queueDepthAfterFlush,
            int sectionsRecaptured,
            boolean verboseLogging) {
        long flushNanos = chunkFlushNanos > 0L ? chunkFlushNanos : hookFlushNanos;
        ++perfFrameCount;
        perfTotalFrameNanos += frameNanos;
        perfMaxFrameNanos = Math.max(perfMaxFrameNanos, frameNanos);
        perfTotalRenderMethodNanos += renderMethodNanos;
        perfMaxRenderMethodNanos = Math.max(perfMaxRenderMethodNanos, renderMethodNanos);
        perfTotalPrePresentNanos += prePresentNanos;
        perfMaxPrePresentNanos = Math.max(perfMaxPrePresentNanos, prePresentNanos);
        perfTotalFlushNanos += flushNanos;
        perfMaxFlushNanos = Math.max(perfMaxFlushNanos, flushNanos);
        perfTotalPresentNanos += presentNanos;
        perfMaxPresentNanos = Math.max(perfMaxPresentNanos, presentNanos);
        perfTotalPostPresentNanos += postPresentNanos;
        perfMaxPostPresentNanos = Math.max(perfMaxPostPresentNanos, postPresentNanos);
        perfTotalQueueDepthBeforeFlush += queueDepthBeforeFlush;
        perfMaxQueueDepthBeforeFlush = Math.max(perfMaxQueueDepthBeforeFlush, queueDepthBeforeFlush);
        perfTotalQueueDepthAfterFlush += queueDepthAfterFlush;
        perfMaxQueueDepthAfterFlush = Math.max(perfMaxQueueDepthAfterFlush, queueDepthAfterFlush);
        perfTotalSectionsRecaptured += sectionsRecaptured;
        perfMaxSectionsRecaptured = Math.max(perfMaxSectionsRecaptured, sectionsRecaptured);

        if (perfFrameCount < PERF_LOG_INTERVAL_FRAMES) {
            return;
        }

        StringBuilder summary = new StringBuilder(256);
        summary.append("[mcrtx] perf java frames=")
                .append(perfFrameCount)
                .append(" frameAvgMs=")
                .append(formatAverageMillis(perfTotalFrameNanos, perfFrameCount))
                .append(" frameMaxMs=")
                .append(formatMillis(perfMaxFrameNanos))
                .append(" renderMethodAvgMs=")
                .append(formatAverageMillis(perfTotalRenderMethodNanos, perfFrameCount))
                .append(" renderMethodMaxMs=")
                .append(formatMillis(perfMaxRenderMethodNanos))
                .append(" prePresentAvgMs=")
                .append(formatAverageMillis(perfTotalPrePresentNanos, perfFrameCount))
                .append(" prePresentMaxMs=")
                .append(formatMillis(perfMaxPrePresentNanos))
                .append(" flushAvgMs=")
                .append(formatAverageMillis(perfTotalFlushNanos, perfFrameCount))
                .append(" flushMaxMs=")
                .append(formatMillis(perfMaxFlushNanos))
                .append(" presentAvgMs=")
                .append(formatAverageMillis(perfTotalPresentNanos, perfFrameCount))
                .append(" presentMaxMs=")
                .append(formatMillis(perfMaxPresentNanos))
                .append(" postAvgMs=")
                .append(formatAverageMillis(perfTotalPostPresentNanos, perfFrameCount))
                .append(" queueBeforeAvg=")
                .append(formatAverageCount(perfTotalQueueDepthBeforeFlush, perfFrameCount))
                .append(" queueBeforeMax=")
                .append(perfMaxQueueDepthBeforeFlush)
                .append(" queueAfterAvg=")
                .append(formatAverageCount(perfTotalQueueDepthAfterFlush, perfFrameCount))
                .append(" queueAfterMax=")
                .append(perfMaxQueueDepthAfterFlush)
                .append(" sectionsAvg=")
                .append(formatAverageCount(perfTotalSectionsRecaptured, perfFrameCount))
                .append(" sectionsMax=")
                .append(perfMaxSectionsRecaptured);
        if (verboseLogging) {
            System.out.println(summary.toString());
        }
        reset();
    }

    private static String formatAverageMillis(long totalNanos, long sampleCount) {
        if (sampleCount <= 0L) {
            return "0.00";
        }
        return formatDouble((double) totalNanos / (double) sampleCount / 1000000.0);
    }

    private static String formatAverageCount(long totalCount, long sampleCount) {
        if (sampleCount <= 0L) {
            return "0.0";
        }
        return formatDouble((double) totalCount / (double) sampleCount);
    }

    private static String formatMillis(long nanos) {
        return formatDouble((double) nanos / 1000000.0);
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
