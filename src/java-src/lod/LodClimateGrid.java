import mcrtx.lod.format.LodChunkSampler;

/**
 * One chunk's biome climate, copied out of a world's biome generator.
 *
 * <p>Shared by the client sampler and the region-file reader so that the
 * x-major correction below lives in one place. Both producers must agree about
 * it exactly: a tile the client sampled and a tile read off disk sit next to
 * each other in the same field, and a tint mirrored in one of them would show as
 * a seam along the boundary rather than as an obvious mistake.
 *
 * <p><b>Copied, never read in place.</b> The generator computes into public
 * arrays it owns, so anything else that touches the world overwrites them
 * between the call and the read. That is also why a background producer must
 * hold its own generator rather than borrow the world's: the arrays are the
 * shared state, and there is no lock on them.
 */
final class LodClimateGrid implements LodChunkSampler.ClimateSource {
    private static final int CHUNK_BLOCKS = LodChunkSampler.CHUNK_BLOCKS;

    private final int[] climate = new int[CHUNK_BLOCKS * CHUNK_BLOCKS];
    private boolean valid;

    /**
     * Fills the grid for one chunk, or marks it unavailable.
     *
     * <p>The generator writes temperature and humidity into two arrays it owns,
     * indexed <b>x-major</b> -- the outer loop runs over the X extent -- which is
     * the opposite of the Z-major order cells use everywhere else. Getting that
     * backwards would mirror every biome tint about the chunk's diagonal, which
     * is subtle enough on rolling terrain to survive a long time unnoticed.
     *
     * <p>One bulk call per chunk beats 256 single-column calls by two orders of
     * magnitude, and the copy is what makes the bulk call safe to use.
     */
    void beginChunk(xv manager, int chunkX, int chunkZ) {
        valid = false;
        if (manager == null) {
            return;
        }

        try {
            manager.a(
                    (kd[]) null,
                    chunkX * CHUNK_BLOCKS,
                    chunkZ * CHUNK_BLOCKS,
                    CHUNK_BLOCKS,
                    CHUNK_BLOCKS);
        } catch (Throwable failure) {
            // A generator that will not answer leaves cells untinted rather than
            // wrongly tinted; a later producer can still fill them in.
            return;
        }

        double[] temperature = manager.a;
        double[] humidity = manager.b;
        if (temperature == null || humidity == null
                || temperature.length < climate.length
                || humidity.length < climate.length) {
            return;
        }

        for (int localX = 0; localX < CHUNK_BLOCKS; localX++) {
            for (int localZ = 0; localZ < CHUNK_BLOCKS; localZ++) {
                int source = localX * CHUNK_BLOCKS + localZ;
                climate[localZ * CHUNK_BLOCKS + localX] =
                        (scale(temperature[source]) << 8) | scale(humidity[source]);
            }
        }
        valid = true;
    }

    public int climateAt(int blockX, int blockZ) {
        if (!valid) {
            return LodChunkSampler.CLIMATE_UNKNOWN;
        }
        int localX = blockX & (CHUNK_BLOCKS - 1);
        int localZ = blockZ & (CHUNK_BLOCKS - 1);
        return climate[localZ * CHUNK_BLOCKS + localX];
    }

    private static int scale(double value) {
        int scaled = (int) (value * 255.0 + 0.5);
        if (scaled < 0) {
            return 0;
        }
        return scaled > 255 ? 255 : scaled;
    }
}
