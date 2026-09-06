package mcrtx.bridge;
public final class HookProfiler {
    public static final int SIDE_HOOK = 0;
    public static void record(int side, String key, long value) { }
    public static void recordCount(String key, long value) { }
}
