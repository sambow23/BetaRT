package mcrtx.bridge;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight profiler for Java-side hook / native-call timings.
 *
 * Disabled (no-op) unless the environment variable {@code MCRTX_PERF=1} is set.
 *
 * <p>Design: each thread owns a small ring buffer of (siteId, nanos) pairs.
 * Site names are interned once via {@link McrtxPerfNative#nRegisterPerfSite},
 * so every subsequent sample stores a dense {@code int} id and avoids JNI
 * string marshalling. Buffers flush in batch through
 * {@link McrtxPerfNative#nRecordJavaSampleBatch} either when they fill up or
 * at frame boundaries via {@link #flushAll()}.
 */
public final class HookProfiler {
    public static final int SIDE_HOOK = 0;
    public static final int SIDE_CALL = 1;
    public static final int SIDE_JNI = 2;
    public static final int SIDE_NATIVE = 3;
    public static final int SIDE_REMIX = 4;

    public static final boolean ENABLED = "1".equals(System.getenv("MCRTX_PERF"));

    private static final int BUFFER_CAPACITY = 8192;

    private static final AtomicBoolean WARNED_BATCH = new AtomicBoolean();
    private static final AtomicBoolean WARNED_REGISTER = new AtomicBoolean();
    private static final AtomicBoolean WARNED_COUNT = new AtomicBoolean();

    // (side << 32) | siteNameHashCode is too lossy; use a composite string key.
    private static final ConcurrentHashMap<String, Integer> SITE_IDS = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<ThreadBuffer> BUFFERS = new CopyOnWriteArrayList<>();

    private static final ThreadLocal<ThreadBuffer> LOCAL = ThreadLocal.withInitial(() -> {
        ThreadBuffer buffer = new ThreadBuffer();
        BUFFERS.add(buffer);
        return buffer;
    });

    private HookProfiler() {
    }

    private static final class ThreadBuffer {
        final int[] ids = new int[BUFFER_CAPACITY];
        final long[] nanos = new long[BUFFER_CAPACITY];
        int count;

        synchronized void append(int id, long elapsedNanos) {
            ids[count] = id;
            nanos[count] = elapsedNanos;
            count++;
            if (count >= BUFFER_CAPACITY) {
                flushLocked();
            }
        }

        synchronized void flush() {
            flushLocked();
        }

        private void flushLocked() {
            if (count == 0) return;
            try {
                McrtxPerfNative.nRecordJavaSampleBatch(ids, nanos, count);
            } catch (Throwable t) {
                warnOnce(WARNED_BATCH, "nRecordJavaSampleBatch", t);
            }
            count = 0;
        }
    }

    private static void warnOnce(AtomicBoolean flag, String method, Throwable t) {
        if (flag.compareAndSet(false, true)) {
            System.err.println("[mcrtx-perf] " + method + " unavailable (likely stale mcrtx_jni.dll): "
                    + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static int resolveSiteId(int side, String site) {
        // Composite key keeps the same name distinguishable across sides.
        String key = side + ":" + site;
        Integer cached = SITE_IDS.get(key);
        if (cached != null) return cached;
        int id;
        try {
            id = McrtxPerfNative.nRegisterPerfSite(side, site);
        } catch (Throwable t) {
            warnOnce(WARNED_REGISTER, "nRegisterPerfSite", t);
            return -1;
        }
        if (id < 0) return -1;
        Integer prior = SITE_IDS.putIfAbsent(key, id);
        return prior != null ? prior : id;
    }

    public static long begin() {
        return ENABLED ? System.nanoTime() : 0L;
    }

    public static void endHook(String site, long startNanos) {
        end(SIDE_HOOK, site, startNanos);
    }

    public static void endCall(String site, long startNanos) {
        end(SIDE_CALL, site, startNanos);
    }

    public static void end(int side, String site, long startNanos) {
        if (!ENABLED || startNanos == 0L) return;
        long elapsed = System.nanoTime() - startNanos;
        if (elapsed < 0L) elapsed = 0L;
        record(side, site, elapsed);
    }

    public static void record(int side, String site, long nanos) {
        if (!ENABLED) return;
        int id = resolveSiteId(side, site);
        if (id < 0) return;
        LOCAL.get().append(id, nanos);
    }

    /**
     * Drain every thread's pending samples through a single batched JNI call
     * per thread. Call at frame boundaries and on shutdown so samples land in
     * the window they were captured in.
     */
    public static void flushAll() {
        if (!ENABLED) return;
        for (ThreadBuffer buffer : BUFFERS) {
            buffer.flush();
        }
    }

    public static void recordCount(String site, long count) {
        if (!ENABLED) return;
        try {
            McrtxPerfNative.nRecordJavaCount(SIDE_HOOK, site, count);
        } catch (Throwable t) {
            warnOnce(WARNED_COUNT, "nRecordJavaCount", t);
        }
    }
}

