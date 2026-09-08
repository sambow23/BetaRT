import java.util.HashSet;
import java.util.Set;


/**
 * Feeds loaded chunks into {@link LodStore}, a few per tick.
 *
 * <p>The client only ever holds chunks within its view distance, so this is the
 * one source that sees ground at full fidelity and the only one that notices a
 * player building something. It is also the narrowest: everything past the view
 * distance has to come from the store's file, from a region-file reader, or from
 * a server.
 *
 * <p><b>Nearest first.</b> Chunks are visited on a precomputed spiral so the
 * ground the player is standing on is recorded before the edge of the view
 * distance. When the player crosses a chunk boundary the sweep restarts from the
 * new centre, which is what keeps sampling ahead of movement instead of
 * finishing a stale square first.
 *
 * <p>Each chunk is sampled once a session. Terrain barely changes, and one pass
 * is what refreshes the store against edits made while the client was closed;
 * the store itself drops a write whose bytes match what it already holds, so
 * even a redundant pass costs nothing downstream. Once means once
 * <em>posted</em>: the sample runs on the store's worker and its outcome is
 * not waited for. It used to be, and an unchanged write came back as a
 * refusal, so every restart of the sweep re-sampled every chunk in range over
 * ground the store already held.
 */
final class LodSampling {
    /**
     * Chunks out from the player that are worth trying.
     *
     * <p>Beta's client is sent about ten chunks in each direction, so anything
     * past a dozen is a lookup that always answers "not loaded".
     */
    private static final int SAMPLE_RADIUS_CHUNKS = 12;

    /** Chunks sampled in one tick. Each is 256 columns of downward scanning. */
    private static final int CHUNK_BUDGET_PER_TICK = 4;

    /**
     * Chunks stepped over in one tick while looking for one worth sampling.
     *
     * <p>Most candidates are already sampled or not loaded, and rejecting those
     * is a set lookup, so the walk runs well ahead of the sampling.
     */
    private static final int SCAN_BUDGET_PER_TICK = 256;

    /**
     * Session-sampled chunks remembered before the set is dropped.
     *
     * <p>Only an optimisation -- forgetting means re-sampling ground that will
     * produce identical bytes and be discarded by the store -- so it is cheaper
     * to cap it than to evict from it carefully.
     */
    private static final int MAX_REMEMBERED = 1 << 15;

    /** Visit order: offsets from the player's chunk, nearest first. */
    private static final int[] SWEEP = buildSweep();

    private static final Set<Long> SAMPLED = new HashSet<Long>();
    private static final LodWorldSampler SAMPLER = new LodWorldSampler();

    private static int cursor;
    private static int sweepChunkX = Integer.MIN_VALUE;
    private static int sweepChunkZ = Integer.MIN_VALUE;

    private LodSampling() {
    }

    /** Forgets what has been sampled. Called when the world changes. */
    static void reset() {
        SAMPLED.clear();
        cursor = 0;
        sweepChunkX = Integer.MIN_VALUE;
        sweepChunkZ = Integer.MIN_VALUE;
    }

    static int sampledCount() {
        return SAMPLED.size();
    }

    /** Spends one tick's sampling budget around the player. */
    static void tick(fd world, int playerChunkX, int playerChunkZ) {
        if (world == null || !LodStore.isReady()) {
            return;
        }

        if (playerChunkX != sweepChunkX || playerChunkZ != sweepChunkZ) {
            // The player moved to a new chunk, so the nearest-first order is
            // stale. Restarting costs a few set lookups and keeps the sweep
            // pointed at the ground that matters.
            sweepChunkX = playerChunkX;
            sweepChunkZ = playerChunkZ;
            cursor = 0;
        }

        if (SAMPLED.size() > MAX_REMEMBERED) {
            SAMPLED.clear();
        }

        if (SAMPLER.isSaturated()) {
            // The worker has not caught up with what was posted already. The
            // sweep resumes from where it stopped once it has.
            return;
        }

        int chunkBudget = CHUNK_BUDGET_PER_TICK;
        int scanBudget = SCAN_BUDGET_PER_TICK;

        while (chunkBudget > 0 && scanBudget > 0 && cursor < SWEEP.length) {
            int packed = SWEEP[cursor++];
            scanBudget--;

            int chunkX = playerChunkX + (short) (packed >> 16);
            int chunkZ = playerChunkZ + (short) packed;

            Long key = Long.valueOf(chunkKey(chunkX, chunkZ));
            if (SAMPLED.contains(key)) {
                continue;
            }

            if (!SAMPLER.sampleChunk(world, chunkX, chunkZ)) {
                // Not loaded, or the store is not ready. Either way this is not
                // remembered, so a later sweep tries again once the chunk
                // arrives.
                continue;
            }

            SAMPLED.add(key);
            chunkBudget--;
        }
    }

    /**
     * Offsets within the sample radius, ordered by how far out they sit.
     *
     * <p>Built once. Ordering by Chebyshev distance gives concentric squares,
     * which matches how chunks actually arrive around a player.
     */
    private static int[] buildSweep() {
        int side = SAMPLE_RADIUS_CHUNKS * 2 + 1;
        int[] sweep = new int[side * side];
        int index = 0;
        for (int ring = 0; ring <= SAMPLE_RADIUS_CHUNKS; ring++) {
            for (int offsetZ = -ring; offsetZ <= ring; offsetZ++) {
                for (int offsetX = -ring; offsetX <= ring; offsetX++) {
                    // Only the outer edge of each square is new; the interior
                    // belongs to a smaller ring already emitted.
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != ring) {
                        continue;
                    }
                    sweep[index++] = ((offsetX & 0xFFFF) << 16) | (offsetZ & 0xFFFF);
                }
            }
        }
        return sweep;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

}
