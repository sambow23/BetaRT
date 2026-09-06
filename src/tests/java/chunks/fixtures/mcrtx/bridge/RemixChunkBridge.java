package mcrtx.bridge;
public final class RemixChunkBridge {
    public static final int RECORD_WORDS = 23;
    public static boolean accept = true;
    public static int updates, removals, allocations;
    public static long lastRevision;
    public static int[] lastRecords;
    public static void resetTerrain(long world) { }
    public static void allocateSection(int x, int y, int z, long world, long lifetime) { ++allocations; }
    public static void removeSection(int x, int y, int z, long world, long lifetime) { ++removals; }
    public static boolean updateSection(int x, int y, int z, long world, long lifetime, long revision,
                                       int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int[] records) {
        ++updates;
        if (!accept) { return false; }
        lastRevision = revision;
        lastRecords = records.clone();
        return true;
    }
}
