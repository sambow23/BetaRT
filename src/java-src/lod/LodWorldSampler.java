import java.util.concurrent.atomic.AtomicInteger;

import mcrtx.lod.format.LodChunkSampler;
import mcrtx.lod.format.LodProvenance;
import mcrtx.lod.format.LodTileKey;

/**
 * Samples loaded chunks into {@link LodStore}.
 *
 * <p>All the actual sampling lives in {@link LodChunkSampler}, which knows
 * nothing about Minecraft. This class exists only to hand it the three raw
 * arrays a chunk keeps and to answer for biome climate, so that the client, a
 * region-file worker, a server and the offline tool all run identical code over
 * identical inputs and cannot disagree about what the ground is.
 *
 * <p>Reading the arrays directly rather than through the chunk's accessors is
 * what makes that sharing possible -- the arrays are the one thing every
 * producer has -- and it is also far cheaper: a chunk is 256 columns, and going
 * through per-block virtual calls at that volume was never affordable.
 *
 * <p><b>The client thread picks the chunk; the store's worker samples it.</b>
 * Sampling ran on the client thread until it turned out to be the largest
 * thing that thread spent on distant terrain: a bulk biome query, a 256-column
 * scan and a deflate at the codec's highest level, up to four times a frame,
 * and in exactly the frames when new chunks were arriving. What the client
 * thread does now is decide that the chunk is loaded and hand over references
 * to its arrays. Those are plain byte buffers the game keeps for the chunk's
 * lifetime and that this class only reads, so the worker can scan them while
 * the game goes on editing blocks: a read that catches a block mid-edit is a
 * cell the next observation of that ground corrects, not a fault.
 *
 * <p>The world's own biome generator is <b>not</b> thread-safe -- it computes
 * into public arrays it owns -- which is why this class builds a generator of
 * its own for the bound world, exactly as {@link LodRegionFileSource} does,
 * and why the worker uses that one and never {@code world.a()}.
 */
final class LodWorldSampler {
    /**
     * Samples the worker may be holding before the sweep stops posting more.
     *
     * <p>The sweep offers a few chunks a frame and the worker spends about
     * half a millisecond on each, so it normally keeps up. This is the ceiling
     * for when it does not -- a load, a save, a burst of region builds -- so
     * the queue cannot grow by a frame's worth every frame until then.
     */
    private static final int MAX_PENDING_SAMPLES = 16;

    /**
     * Worker-only scratch. Samples run one at a time on the single worker, so
     * one of each serves every chunk. Nothing on the client thread touches
     * them.
     */
    private final byte[] columnScratch = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];
    private final LodClimateGrid climate = new LodClimateGrid();

    /**
     * Generator for the bound world, built on the client thread and handed to
     * each sample through a final field. {@code new xv} only reads the world's
     * seed, which is cheap and safe from the thread that owns the world, and
     * it is the one part of climate that needs the world at all.
     */
    private xv climateManager;
    private fd climateWorld;

    /** Samples posted and not yet run. Counted down by the worker. */
    private final AtomicInteger pending = new AtomicInteger();

    /** One chunk's sample, as it runs on the worker. */
    private final class ChunkSample implements Runnable {
        private final fd world;
        private final xv manager;
        private final byte[] blocks;
        private final byte[] data;
        private final byte[] heightMap;
        private final int chunkX;
        private final int chunkZ;

        ChunkSample(
                fd world,
                xv manager,
                byte[] blocks,
                byte[] data,
                byte[] heightMap,
                int chunkX,
                int chunkZ) {
            this.world = world;
            this.manager = manager;
            this.blocks = blocks;
            this.data = data;
            this.heightMap = heightMap;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        public void run() {
            try {
                if (!LodStore.isBoundTo(world)) {
                    // The world changed under this sample. Its tile belongs to
                    // the store that was bound when it was posted, not to
                    // whichever one is bound now.
                    return;
                }

                LodTileKey key = LodChunkSampler.tileForChunk(chunkX, chunkZ);
                byte[] tile = LodStore.tileForUpdate(key);
                if (tile == null) {
                    return;
                }

                climate.beginChunk(manager, chunkX, chunkZ);
                LodChunkSampler.sampleChunkIntoTile(
                        blocks, data, heightMap, chunkX, chunkZ, climate, tile, columnScratch);
                LodStore.putTile(key, tile, LodProvenance.LIVE, System.currentTimeMillis());
            } finally {
                pending.decrementAndGet();
            }
        }
    }

    /** True when the worker is behind on what the sweep has already posted. */
    boolean isSaturated() {
        return pending.get() >= MAX_PENDING_SAMPLES;
    }

    /**
     * Posts one loaded chunk to the worker.
     *
     * <p>Posting is the whole of what "sampled" means to the caller. The
     * write's outcome is not waited for, and a sample the store turns out to
     * hold already is still a chunk that has been looked at. It used to be
     * reported as a refusal, and the sweep took that to mean "try again", so
     * every restart of the sweep re-sampled every chunk in range over ground
     * the store already had: the same biome query, scan and deflate, a few
     * times a frame, for as long as the player kept walking.
     *
     * @return true when the chunk was loaded and handed over.
     */
    boolean sampleChunk(fd world, int chunkX, int chunkZ) {
        if (world == null || !LodStore.isReady()) {
            return false;
        }

        lm chunk = loadedChunkAt(world, chunkX, chunkZ);
        if (chunk == null) {
            return false;
        }

        if (world != climateWorld) {
            climateWorld = world;
            climateManager = new xv(world);
        }

        pending.incrementAndGet();
        LodStore.postChunkSample(new ChunkSample(
                world,
                climateManager,
                chunk.b,
                chunk.e == null ? null : chunk.e.a,
                chunk.h,
                chunkX,
                chunkZ));
        return true;
    }

    /** The chunk only if it is already loaded; never generates one. */
    private static lm loadedChunkAt(fd world, int chunkX, int chunkZ) {
        if (world.v == null || !world.v.a(chunkX, chunkZ)) {
            return null;
        }
        lm chunk = world.c(chunkX, chunkZ);
        if (chunk == null || chunk.b == null) {
            return null;
        }
        return chunk;
    }
}
