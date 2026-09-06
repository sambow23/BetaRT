package mcrtx.bridge;

public final class RemixChunkBridge {
    public static final int RECORD_WORDS = 23;
    private RemixChunkBridge() { }

    public static void resetTerrain(long world) {
        if (RemixLifecycleBridge.isInitialized()) {
            nResetTerrain(world);
        }
    }
    public static void allocateSection(int x, int y, int z, long world, long lifetime) {
        if (RemixLifecycleBridge.isInitialized()) {
            nAllocateSection(x, y, z, world, lifetime);
        }
    }
    public static void removeSection(int x, int y, int z, long world, long lifetime) {
        if (RemixLifecycleBridge.isInitialized()) {
            nRemoveSection(x, y, z, world, lifetime);
        }
    }
    public static boolean updateSection(int x, int y, int z, long world, long lifetime, long revision,
                                        int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int[] records) {
        return RemixLifecycleBridge.isInitialized() && nUpdateSection(x, y, z, world, lifetime, revision,
                minX, minY, minZ, maxX, maxY, maxZ, records);
    }
    private static native void nResetTerrain(long world);
    private static native void nAllocateSection(int x, int y, int z, long world, long lifetime);
    private static native void nRemoveSection(int x, int y, int z, long world, long lifetime);
    private static native boolean nUpdateSection(int x, int y, int z, long world, long lifetime, long revision,
                                                int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int[] records);
}
