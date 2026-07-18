package mcrtx.bridge;

final class McrtxPerfNative {
    private McrtxPerfNative() {
    }

    static native void nRecordJavaSample(int side, String site, long nanoseconds);
    static native void nRecordJavaCount(int side, String site, long count);
    static native void nFlushJavaFrame();
    static native int nRegisterPerfSite(int side, String site);
    static native void nRecordJavaSampleBatch(int[] siteIds, long[] nanos, int count);
}
