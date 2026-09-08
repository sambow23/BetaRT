import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;

import mcrtx.lod.format.LodChunkSampler;
import mcrtx.lod.format.LodTileKey;

/**
 * Reads canonical tiles straight out of a world's McRegion files.
 *
 * <p>This is the producer that makes the outer rings possible at all. The live
 * sampler can only see chunks the client holds, which reaches about twelve
 * chunks -- 192 blocks -- and that is the outer edge of the innermost ring, so
 * by construction no amount of waiting fills the second ring from live chunks.
 * The world's own save is the only place the rest of the ground exists without
 * the player walking to it.
 *
 * <p><b>Deliberately free of the world.</b> Nothing here touches the world, the
 * tile store or the client's biome generator; it needs only a region directory,
 * Beta's region-file and NBT classes, and the shared sampler. That is what lets
 * an offline harness point this exact code at a real save and check what comes
 * out, rather than proving only that it compiles -- and it is the same property
 * that would let an offline bake tool link it.
 *
 * <p><b>A tile, not a region file, is the unit of work.</b> A McRegion file is
 * 32x32 chunks and a canonical tile is 4x4, and four divides thirty-two, so a
 * tile's chunks never straddle two region files. Reading a whole region file at
 * once would be 1024 chunk decompressions -- seconds of work -- on the one
 * background thread that also meshes regions and saves the store. Sixteen chunks
 * is a slice small enough to interleave with that, and it is exactly the unit
 * the tile store accepts, so partial progress is always a whole tile rather than
 * a fragment nothing can draw.
 *
 * <p>Not thread-safe: it owns an open file handle and is meant to be driven by
 * the store's worker alone.
 */
final class LodRegionReader {
    /** Chunks along one edge of a McRegion file. */
    static final int CHUNKS_PER_REGION_AXIS = 32;

    /** Bytes a whole chunk's block array occupies in the save. */
    private static final int CHUNK_BLOCK_BYTES =
            LodChunkSampler.CHUNK_BLOCKS * LodChunkSampler.CHUNK_BLOCKS * LodChunkSampler.WORLD_HEIGHT;

    /**
     * Biome climate for the chunk about to be sampled.
     *
     * <p>{@link LodChunkSampler.ClimateSource} answers per column, but every
     * producer fills climate a chunk at a time because the generator's bulk call
     * is two orders of magnitude cheaper than 256 single-column ones. This is the
     * hook that says which chunk is next.
     */
    interface ChunkClimate extends LodChunkSampler.ClimateSource {
        /** Called once before each chunk's columns are read. */
        void beginChunk(int chunkX, int chunkZ);
    }

    private final File regionDirectory;

    /**
     * The one region file held open, and which one it is.
     *
     * <p>A single slot rather than a cache, because requests are drained
     * nearest-to-player first and a region file holds 64 canonical tiles, so
     * consecutive fills almost always want the file already open. Opening one is
     * not free -- Beta's reader walks 8 KB of sector table through unbuffered
     * four-byte reads -- which is why it is worth holding across a slice, and
     * {@link #close} is why it is not held past one.
     */
    private File openPath;
    private qj openRegion;

    LodRegionReader(File regionDirectory) {
        this.regionDirectory = regionDirectory;
    }

    /**
     * Reads the sixteen chunks under one canonical tile into it.
     *
     * <p>Chunks the save does not hold are skipped rather than written as holes.
     * A chunk owns one sixteenth of a tile and must not blank the rest, so the
     * buffer is added to and never cleared -- which is what lets a caller pass
     * either a blank tile or one that already holds something.
     *
     * @return how many chunks were read, or zero when the region file is absent
     *     or holds none of them.
     */
    int fillTile(LodTileKey key, ChunkClimate climate, byte[] tile, byte[] scratch) {
        if (key == null || key.level() != LodTileKey.CANONICAL_LEVEL) {
            // Only the canonical level is an observation; every coarser tile is
            // arithmetic over these and must not be written from disk directly.
            throw new IllegalArgumentException("region reads produce canonical tiles only");
        }

        int chunksPerTile = LodChunkSampler.CHUNKS_PER_TILE_AXIS;
        int baseChunkX = key.tileX() * chunksPerTile;
        int baseChunkZ = key.tileZ() * chunksPerTile;

        qj region = open(baseChunkX, baseChunkZ);
        if (region == null) {
            return 0;
        }

        int read = 0;
        for (int offsetZ = 0; offsetZ < chunksPerTile; offsetZ++) {
            for (int offsetX = 0; offsetX < chunksPerTile; offsetX++) {
                int chunkX = baseChunkX + offsetX;
                int chunkZ = baseChunkZ + offsetZ;
                if (readChunk(region, chunkX, chunkZ, climate, tile, scratch)) {
                    read++;
                }
            }
        }
        return read;
    }

    /** Releases the region file. Safe to call when none is open. */
    void close() {
        qj region = openRegion;
        openRegion = null;
        openPath = null;
        if (region == null) {
            return;
        }
        try {
            region.b();
        } catch (Throwable failure) {
            // A handle we are done with. Nothing downstream cares whether the
            // close succeeded, and there is no recovery to attempt.
        }
    }

    /** The region file a chunk belongs to, whether or not it exists. */
    static File regionFileFor(File regionDirectory, int chunkX, int chunkZ) {
        // Beta names region files by an arithmetic shift of the chunk
        // coordinate, which floors for negatives -- the same convention the game
        // uses, and the reason this is a shift rather than a division.
        return new File(
                regionDirectory,
                "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mcr");
    }

    /**
     * The region file holding a chunk, opening it if needed.
     *
     * <p>Existence is checked before opening because Beta's reader opens for
     * read <em>and write</em> and writes an empty 8 KB sector table into any file
     * that is not there. Most of what a distant-terrain reader asks about is
     * ground nobody has generated, so without this check the first minute of play
     * would litter the player's save with hundreds of empty region files.
     *
     * @return null when the save holds no such region.
     */
    private qj open(int chunkX, int chunkZ) {
        File path = regionFileFor(regionDirectory, chunkX, chunkZ);
        if (path.equals(openPath)) {
            return openRegion;
        }
        close();

        if (!path.isFile()) {
            return null;
        }
        try {
            openRegion = new qj(path);
            openPath = path;
        } catch (Throwable failure) {
            // A region file that will not open is ground this source cannot
            // supply; the live sampler still covers it if the player goes there.
            openRegion = null;
            openPath = null;
        }
        return openRegion;
    }

    /**
     * Samples one chunk of the save into the tile.
     *
     * <p>Deliberately not going through the game's chunk loader: that would
     * build an {@code lm}, register tile entities and spawn the chunk's entities
     * into a world this thread has no business touching. The three arrays the
     * sampler wants are in the NBT already.
     *
     * @return true when the chunk was there and was sampled.
     */
    private boolean readChunk(
            qj region, int chunkX, int chunkZ, ChunkClimate climate, byte[] tile, byte[] scratch) {
        nu root;
        try {
            // The region file's own lock covers the raw sector read; the stream
            // it hands back inflates lazily, so the decompression happens off it.
            DataInputStream input = region.a(
                    chunkX & (CHUNKS_PER_REGION_AXIS - 1), chunkZ & (CHUNKS_PER_REGION_AXIS - 1));
            if (input == null) {
                return false;
            }
            try {
                // The cast picks the plain-NBT overload over the gzip one. Both
                // accept a DataInputStream, so without it this does not compile
                // -- and the gzip overload is for level.dat, not for chunk data a
                // region file has already decompressed.
                root = as.a((DataInput) input);
            } finally {
                input.close();
            }
        } catch (Throwable failure) {
            // A torn or corrupt chunk. The save is being written by the game
            // while this reads it, so an occasional unreadable chunk is expected
            // rather than exceptional; it costs one chunk of one tile.
            return false;
        }

        if (root == null) {
            return false;
        }

        byte[] blocks;
        byte[] data;
        byte[] heightMap;
        try {
            nu level = root.k("Level");
            blocks = level.j("Blocks");
            data = level.j("Data");
            heightMap = level.j("HeightMap");
        } catch (Throwable failure) {
            // Tags of the wrong type. Same treatment as a corrupt chunk.
            return false;
        }

        if (blocks == null || blocks.length < CHUNK_BLOCK_BYTES) {
            return false;
        }
        if (data != null && data.length < CHUNK_BLOCK_BYTES / 2) {
            // Metadata only decides appearance, so a short array is dropped and
            // the cell falls back to zero rather than the chunk being lost.
            data = null;
        }
        if (heightMap != null && heightMap.length < LodChunkSampler.COLUMNS_PER_CHUNK) {
            // Without it the sampler scans from the world ceiling, which is
            // slower but reaches the same answer.
            heightMap = null;
        }

        if (climate != null) {
            climate.beginChunk(chunkX, chunkZ);
        }

        LodChunkSampler.sampleChunkIntoTile(
                blocks, data, heightMap, chunkX, chunkZ, climate, tile, scratch);
        return true;
    }
}
