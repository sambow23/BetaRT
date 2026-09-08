import java.io.File;

import mcrtx.bridge.RemixLodBridge;
import mcrtx.lod.format.LodChunkSampler;
import mcrtx.lod.format.LodTileKey;

/**
 * The world's own save, as a source of LOD tiles.
 *
 * <p>{@link LodSampling} can only record chunks the client is holding, which is
 * about twelve chunks in each direction. That is the outer edge of the innermost
 * ring, so the second ring can never fill from live chunks however long the
 * player stands still -- and chunks load and unload as the player moves, so even
 * the tiles that do exist are mostly holes. This reads the region files
 * underneath instead, which is what turns a handful of resident tiles in a ring
 * into all of them without anyone walking a kilometre.
 *
 * <p>What this class decides is <em>whether</em> a world can be read and
 * <em>with what</em>; {@link LodRegionReader} does the reading and
 * {@link LodStore} owns the queue. The split is what keeps the reader free of
 * the world, and so testable against a real save offline.
 *
 * <p><b>Two worlds are refused.</b> On a server there is no save directory at
 * all, so this simply does not apply and the live sampler stays the only source.
 * In the Nether it is worse than useless: the sampler records the highest solid
 * block in a column, and in the Nether that is the bedrock ceiling, so reading it
 * would roof the whole dimension over in stone. Nothing is better than that.
 *
 * <p>Threading: {@link #bind} runs on the client thread and publishes a session;
 * everything else runs on the store's worker and touches only what the session
 * holds. The world's biome generator is <b>not</b> thread-safe -- it computes
 * into public arrays it owns -- so the session carries a generator of its own
 * rather than borrowing {@code world.a()}.
 */
final class LodRegionFileSource {
    /**
     * One bound world's reading apparatus.
     *
     * <p>Published whole through a volatile reference, so the worker either sees
     * a fully built session or the previous one, never a half-configured one.
     */
    private static final class Session {
        /** Ours alone. Sharing the world's would race with the client thread. */
        final xv climate;
        final LodRegionReader reader;
        final LodClimateGrid climateGrid = new LodClimateGrid();
        final byte[] scratch = new byte[LodChunkSampler.BLOCK_CELLS_BYTES];

        Session(File regionDirectory, xv climate) {
            this.climate = climate;
            this.reader = new LodRegionReader(regionDirectory);
        }
    }

    /** Bridges the sampler's per-column climate callback to the session's. */
    private static final class SessionClimate implements LodRegionReader.ChunkClimate {
        private final Session session;

        SessionClimate(Session session) {
            this.session = session;
        }

        public void beginChunk(int chunkX, int chunkZ) {
            session.climateGrid.beginChunk(session.climate, chunkX, chunkZ);
        }

        public int climateAt(int blockX, int blockZ) {
            return session.climateGrid.climateAt(blockX, blockZ);
        }
    }

    /** Written by the client thread on bind, read by the worker. */
    private static volatile Session published;

    /**
     * The session the worker is actually using, and the file handle with it.
     *
     * <p>Worker-only. The client thread must never close the reader: it would be
     * pulling a file handle out from under a read in progress, and closing is
     * exactly the thing the worker is in the middle of when that matters.
     */
    private static Session active;
    private static SessionClimate activeClimate;

    /**
     * Which world the last binding line described.
     *
     * <p>Binding happens on every store rebind and on every dimension change, and
     * a line each would bury the store's own. Comparing the line rather than
     * counting them means a world that becomes readable, or stops being, still
     * says so.
     */
    private static String loggedBinding;

    /** So a save that will not read reports once rather than once per tile. */
    private static boolean loggedFailure;

    private LodRegionFileSource() {
    }

    // ------------------------------------------------------------- lifecycle

    /**
     * Points the source at a world, or at nothing.
     *
     * <p>Called from {@link LodStore#bind} on the client thread. Building the
     * generator here rather than on the worker is deliberate: {@code new xv} only
     * reads the world's seed, which is cheap and safe to touch from the thread
     * that owns the world, where doing it lazily on the worker would read world
     * state from a thread that has no claim on it.
     */
    static void bind(fd world) {
        published = null;
        if (world == null) {
            return;
        }

        int dimension = world.t != null ? world.t.g : 0;
        if (dimension != 0) {
            // See the class comment: a highest-solid-block heightfield of the
            // Nether is its ceiling. Written as "not the overworld" rather than
            // "not dimension -1" because any dimension roofed the same way would
            // read the same way, and there is nothing here that could tell.
            log("lod disk: dimension " + dimension + " has no readable surface, skipping");
            return;
        }

        wt saveHandler = world.w;
        if (!(saveHandler instanceof fm)) {
            // A multiplayer world's save handler is a stub with no directory
            // behind it. Quietly nothing to do: the live sampler is the whole
            // source there, exactly as it was before this class existed.
            log("lod disk: no save directory, live chunks only");
            return;
        }

        File save = ((fm) saveHandler).a();
        if (save == null) {
            return;
        }
        File regionDirectory = new File(save, "region");
        if (!regionDirectory.isDirectory()) {
            log("lod disk: no region directory under " + save.getAbsolutePath());
            return;
        }

        published = new Session(regionDirectory, new xv(world));
        log("lod disk: reading " + regionDirectory.getAbsolutePath());
    }

    /** Whether a bound world can be read at all. Cheap; called per request. */
    static boolean isAvailable() {
        return published != null;
    }

    // ---------------------------------------------------------------- worker

    /**
     * Reads one canonical tile out of the world's region files.
     *
     * @param tile a whole tile the reader writes into. Only the chunks the save
     *     holds are touched, so the rest is left as the caller had it.
     * @return how many of the tile's sixteen chunks the save actually held.
     */
    static int fill(LodTileKey key, byte[] tile) {
        Session session = published;
        if (session != active) {
            // The world changed under the worker. Whatever file the previous
            // session had open belongs to a save nobody is looking at now, and
            // the worker is the only thread allowed to let go of it.
            releaseFiles();
            active = session;
            activeClimate = session == null ? null : new SessionClimate(session);
        }
        if (session == null || tile == null) {
            return 0;
        }

        try {
            return session.reader.fillTile(key, activeClimate, tile, session.scratch);
        } catch (Throwable failure) {
            if (!loggedFailure) {
                loggedFailure = true;
                RemixLodBridge.log("lod disk: read of " + key + " failed: " + failure);
            }
            return 0;
        }
    }

    /**
     * Closes the region file the worker holds open.
     *
     * <p>Called when the queue drains rather than after every tile, because
     * opening one costs a walk of 8 KB of sector table through unbuffered
     * four-byte reads and consecutive tiles nearly always want the same file.
     * Not holding it while idle is what keeps the player able to delete a world
     * they have just left: on Windows an open handle refuses that.
     */
    static void releaseFiles() {
        Session session = active;
        if (session != null) {
            session.reader.close();
        }
    }

    private static void log(String message) {
        if (message.equals(loggedBinding)) {
            return;
        }
        loggedBinding = message;
        RemixLodBridge.log(message);
    }
}
