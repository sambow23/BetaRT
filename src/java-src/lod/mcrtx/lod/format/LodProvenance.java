package mcrtx.lod.format;

/**
 * Where a tile's data came from, and which source wins when two disagree.
 *
 * <p>The field has four producers and they are not equal. A client reading
 * loaded chunks sees the world as it is right now, including anything built
 * since the ground was last saved. A region file on disk is the world as it was
 * last written. A server's copy is authoritative about ground the client has
 * never visited but may lag what the player is standing in. A derived tile is
 * not an observation at all -- it is arithmetic over other tiles, and it must
 * never displace one.
 *
 * <p>Precedence is by class first and recency second. Comparing stamps across
 * classes would be wrong: a server tile sent a moment ago is still not better
 * evidence about the chunk under the player's feet than what that chunk
 * actually contains.
 */
public final class LodProvenance {
    /** Sampled from a chunk the client had loaded. */
    public static final int LIVE = 0;
    /** Read from the world's own region files. */
    public static final int DISK = 1;
    /** Received from a server. */
    public static final int SERVER = 2;
    /** Computed from finer tiles by {@link LodReducer}. */
    public static final int DERIVED = 3;

    private static final String[] NAMES = {"live", "disk", "server", "derived"};

    private LodProvenance() {
    }

    public static boolean isValid(int source) {
        return source >= LIVE && source <= DERIVED;
    }

    public static String name(int source) {
        return isValid(source) ? NAMES[source] : "unknown(" + source + ")";
    }

    /**
     * True when a candidate should replace what is already stored.
     *
     * <p>A stronger class always wins. Within a class the newer stamp wins, and
     * an equal stamp does not -- so re-observing unchanged ground is a no-op
     * rather than a write, which matters because a write marks the tile dirty
     * and costs a mesh rebuild it does not need.
     */
    public static boolean supersedes(
            int candidateSource, long candidateStamp, int existingSource, long existingStamp) {
        if (candidateSource != existingSource) {
            return candidateSource < existingSource;
        }
        return candidateStamp > existingStamp;
    }

    /**
     * The most a tile assembled from both sources may claim to be.
     *
     * <p>A tile is written whole but observed a chunk at a time, so a tile whose
     * gaps were filled from disk is part live and part disk and is not entitled
     * to the live label any more. Claiming the weaker of the two is what keeps
     * such a tile open to a real live observation of the same ground later --
     * claiming the stronger would lock the mixture in, because
     * {@link #supersedes} would then refuse the very write that would improve
     * it.
     */
    public static int weaker(int left, int right) {
        return left > right ? left : right;
    }
}
