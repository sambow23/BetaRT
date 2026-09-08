import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import mcrtx.bridge.RemixLodBridge;
import mcrtx.lod.format.LodCell;
import mcrtx.lod.format.LodProvenance;
import mcrtx.lod.format.LodReducer;
import mcrtx.lod.format.LodStoreFile;
import mcrtx.lod.format.LodTileCodec;
import mcrtx.lod.format.LodTileKey;
import mcrtx.lod.format.LodWorldId;

/**
 * Every LOD tile the client knows about, in memory and on disk.
 *
 * <p>Replaces the per-chunk observation cache. The change that matters is not
 * the keying but what a stored record now <em>is</em>: a tile, at a level, in a
 * pyramid. Observations enter at {@link LodTileKey#CANONICAL_LEVEL} and every
 * coarser level is derived from them by {@link LodReducer}, so ground seen once
 * up close serves every detail level that will ever draw it -- which is the
 * property that made a per-step cache useless and a step-independent one
 * necessary.
 *
 * <p><b>The file is meant to be given away.</b> Its name and contents are keyed
 * on the world's seed and dimension rather than on where the save happens to sit
 * on this machine, so a file baked by another player, produced by a server, or
 * written by the offline tool drops into the cache directory and is accepted.
 * That is also why only <em>observations</em> are written: derived tiles are a
 * pure function of the canonical level, so storing them would inflate a shared
 * file with bytes the reader can compute. The header's level mask records which
 * levels a file actually carries, so a reader can tell "not present" from "never
 * written".
 *
 * <p><b>Reads never block and never build.</b> {@link #tile} answers from memory
 * or returns null; a coarse tile that has not been derived yet is queued for the
 * worker and answered on a later call. The scheduler already re-queues regions
 * whose data was not ready, so a miss costs a retry rather than a stall on the
 * frame path.
 *
 * <p>Threading: the client thread reads tiles; one background thread does all
 * file I/O, all compression, all derivation, the sampling of loaded chunks --
 * through {@link #postChunkSample} -- and, through {@link #postRegionBuild},
 * the meshing and submission of LOD regions. Every write into the map happens
 * on that worker. Stored blobs are immutable once published, so the client
 * thread can read the map while the worker adds to it.
 */
final class LodStore {
    /** Alongside mcrtx-runtime.env and mcrtx.log, in the instance's directory. */
    private static final String CACHE_DIRECTORY_NAME = "mcrtx-lod-cache";

    /**
     * Tiles held in memory, and so also the most ever written to disk.
     *
     * <p>A canonical tile encodes to roughly a kilobyte, so this is some twelve
     * megabytes of heap and about as much on disk. Past it the tiles furthest
     * from the player are dropped.
     *
     * <p>The number to compare it against is not the field's region count but
     * the pyramid underneath it. A coarse tile is derived from its four
     * children and they from theirs, all the way down to the canonical level,
     * and every step of that is stored -- so a level-5 tile is not one tile but
     * 341, of which 256 are canonical. Summing the pyramid, a fully explored
     * square of side S needs about {@code (S/64)^2 * 4/3} tiles: 5,400 at the
     * 4096-block square the 2048 setting covers, comfortably inside this, and
     * about 21,800 at the 8192-block square the 4096 setting covers, which is
     * not.
     *
     * <p>That ceiling is left where it is deliberately. Reaching it needs some
     * sixteen thousand generated chunks inside one 8 km square -- a world that
     * has been walked, not merely visited -- and raising the cap would spend
     * heap on every player to serve that one. What a player past it gets is
     * eviction of the tiles furthest away, which is the outermost ring
     * rebuilding itself as they move rather than anything incorrect.
     */
    private static final int MAX_TILES = 12288;

    /** Extra tiles dropped in one prune, so pruning is not a per-write event. */
    private static final int PRUNE_HEADROOM = 1024;

    /**
     * Inflated tiles kept ready for reading.
     *
     * <p>The resident field is a few hundred tiles and the mesher reads each one
     * whole, so keeping that many expanded costs a few megabytes and saves
     * inflating the same tile on every rebuild. Everything else stays compressed.
     */
    private static final int INFLATED_CACHE_SIZE = 384;

    private static final long SAVE_INTERVAL_MILLIS = 60000L;

    /** Coarse tiles the worker may derive before going back to sleep. */
    private static final int DERIVE_BATCH = 64;

    /** Derivation requests waiting on the worker. */
    private static final int MAX_PENDING_DERIVATIONS = 512;

    /**
     * Canonical tiles read out of the world's region files in one turn.
     *
     * <p>Measured at a median of 1.9 ms and a worst case of 4.2 ms a tile over a
     * real save, so four is somewhere between seven and seventeen milliseconds
     * of the worker's time before it looks at its other queues again. Larger
     * would amortise the region file's sector table over more tiles; it would
     * also be the length of a stall in the middle of a mesh the player is
     * waiting to see.
     */
    private static final int DISK_BATCH = 4;

    /**
     * Region-file reads waiting on the worker.
     *
     * <p>A single coarse tile expands to sixty-four canonical tiles beneath it,
     * so a sweep of the field asks for far more than the worker can serve and the
     * queue is normally full. That is fine and is why it is drained
     * nearest-to-player first rather than in order: the cap decides how much of
     * the field the ordering gets to choose from, not how much is eventually
     * read.
     */
    private static final int MAX_PENDING_DISK_READS = 512;

    /**
     * Canonical tiles remembered as already read from disk.
     *
     * <p>Roughly the tiles of a fully explored 4 km square, matching the resident
     * tile budget. Overflowing it means the player has travelled far enough that
     * the old entries are ground nobody is drawing, so clearing costs a re-read
     * only if they come back.
     */
    private static final int MAX_REMEMBERED_DISK_READS = 1 << 16;

    /** One stored tile. The blob is never mutated once published. */
    private static final class Entry {
        final byte[] blob;
        final int source;
        final long stamp;
        /**
         * Value of {@link #epoch} when this entry was published.
         *
         * <p>What the scheduler compares a region's build against, in place of
         * the store-wide epoch. That one moves on every write anywhere in the
         * store, so measured against it a region was stale within a tick of
         * any chunk being sampled and the whole field re-meshed in rotation
         * for as long as the player walked. A tile's own epoch moves only when
         * that tile does.
         */
        final int epoch;
        /**
         * Every cell known. Decided once, while the cells are in hand, so that
         * asking whether the save could still add to a tile never has to
         * inflate it to find out.
         */
        final boolean complete;

        Entry(byte[] blob, int source, long stamp, int epoch, boolean complete) {
            this.blob = blob;
            this.source = source;
            this.stamp = stamp;
            this.epoch = epoch;
            this.complete = complete;
        }
    }

    /** One world's tiles, and where they live on disk. */
    private static final class Store {
        final LodWorldId worldId;
        final String label;
        final File file;
        final ConcurrentHashMap<Long, Entry> tiles = new ConcurrentHashMap<Long, Entry>();
        volatile boolean loaded;
        volatile boolean dirty;
        volatile long lastSaveMillis;

        Store(LodWorldId worldId, String label, File file) {
            this.worldId = worldId;
            this.label = label;
            this.file = file;
            this.lastSaveMillis = System.currentTimeMillis();
        }
    }

    private static final Object WORK_LOCK = new Object();
    private static final ArrayDeque<Store> SAVE_REQUESTS = new ArrayDeque<Store>();
    private static final ArrayDeque<Long> DERIVE_REQUESTS = new ArrayDeque<Long>();
    /**
     * Region builds handed over by the scheduler.
     *
     * <p>The store owns this queue rather than the scheduler owning a thread,
     * because everything a build reads is already here and one worker is the
     * whole point: a second thread would have two of them meshing regions and
     * submitting to Remix at once, which the submission path is not built for.
     */
    private static final ArrayDeque<Runnable> BUILD_REQUESTS = new ArrayDeque<Runnable>();

    /** Chunk samples handed over by {@link LodWorldSampler}. See {@link #postChunkSample}. */
    private static final ArrayDeque<Runnable> SAMPLE_REQUESTS = new ArrayDeque<Runnable>();

    /**
     * Canonical tiles the world's own save might be able to supply.
     *
     * <p>Not a queue, because it is drained by distance from the player rather
     * than in arrival order and the player moves: any priority fixed at
     * insertion would be stale by the time the entry came up. A set rather than
     * a list because deriving one coarse tile offers sixty-four candidates and a
     * batch offers thousands, so the duplicate check is the hot operation here
     * and scanning a few hundred entries for each of them is not free.
     */
    private static final Set<Long> DISK_REQUESTS = new LinkedHashSet<Long>();

    /**
     * Tiles this session has already asked the save about.
     *
     * <p>Without it the worker thrashes forever on unexplored ground: a region
     * file that holds nothing for a tile produces nothing, the scheduler asks
     * again on its next sweep, and the read is repeated for as long as the player
     * looks that way. Remembering the attempt is what makes a miss cost once.
     */
    private static final Set<Long> DISK_ATTEMPTED = new HashSet<Long>();

    /**
     * Set when the world changed, so the worker lets go of the previous save's
     * region file. The client thread must not close it itself: the worker may be
     * reading through that very handle, and a close under it would turn a tile
     * into a failure. Not urgent, but it must not wait for the next read either
     * -- there may never be one, and until it happens the player cannot delete
     * the world they just left.
     */
    private static boolean releaseDiskFiles;

    private static Store loadRequest;
    private static Thread worker;
    private static boolean shutdownHookInstalled;

    /**
     * Recently inflated tiles, most recently used last.
     *
     * <p>Guarded by its own monitor rather than made concurrent: it is touched
     * once per tile read, which is far rarer than the map lookups around it.
     */
    private static final Map<Long, byte[]> INFLATED =
            new LinkedHashMap<Long, byte[]>(INFLATED_CACHE_SIZE * 2, 0.75f, true) {
                private static final long serialVersionUID = 1L;

                protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
                    return size() > INFLATED_CACHE_SIZE;
                }
            };

    private static volatile Store current;
    private static volatile fd boundWorld;
    private static volatile boolean enabled = true;

    /**
     * Bumped when a store finishes loading or a derivation lands, so the
     * scheduler can retry what it passed over. Written from both threads.
     */
    private static final AtomicInteger epoch = new AtomicInteger();

    /** Counts what the save actually produced, for the status line. */
    private static final AtomicInteger diskTilesRead = new AtomicInteger();

    private static volatile int playerBlockX;
    private static volatile int playerBlockZ;
    private static boolean loggedIdentity;

    private LodStore() {
    }

    // ------------------------------------------------------------- lifecycle

    static void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (!value) {
            flush();
            current = null;
            boundWorld = null;
            clearInflated();
            LodRegionFileSource.bind(null);
            synchronized (WORK_LOCK) {
                DISK_REQUESTS.clear();
                releaseDiskFiles = true;
                WORK_LOCK.notifyAll();
            }
            ensureWorker();
        } else {
            // Force the next bind to reload, since the world reference is stale.
            boundWorld = null;
        }
        epoch.incrementAndGet();
    }

    static boolean isEnabled() {
        return enabled;
    }

    static boolean isReady() {
        Store store = current;
        return enabled && store != null && store.loaded;
    }

    static int epoch() {
        return epoch.get();
    }

    static int tileCount() {
        Store store = current;
        return store == null ? 0 : store.tiles.size();
    }

    /**
     * Tiles the world's own save improved this session.
     *
     * <p>Counts merges that changed something rather than reads attempted, since
     * a read over ground the live sampler had already covered is a no-op and
     * counting it would report progress that is not there. From inside the game a
     * field that will not fill looks the same whether the save is being read and
     * holds nothing, or is not being read at all -- a world on a server, a world
     * in the Nether, a world whose directory moved -- and this is the number that
     * tells those apart.
     */
    static int diskTilesRead() {
        return diskTilesRead.get();
    }

    /** Canonical tiles still waiting on the save. */
    static int diskReadsPending() {
        synchronized (WORK_LOCK) {
            return DISK_REQUESTS.size();
        }
    }

    /**
     * Binds the store to whatever world the scheduler is looking at.
     *
     * <p>Compared by reference every frame rather than driven off the world
     * change hook, because a dimension swap can hand the scheduler a different
     * world object by a path the hook does not report.
     */
    static void bind(fd world) {
        if (world == boundWorld) {
            return;
        }
        boundWorld = world;

        Store previous = current;
        if (previous != null) {
            requestSave(previous);
        }
        current = null;
        clearInflated();
        synchronized (WORK_LOCK) {
            DERIVE_REQUESTS.clear();
            DISK_REQUESTS.clear();
            // Attempts belong to the world that was bound. Another world's save
            // has its own region files, and one of them holding nothing says
            // nothing about whether this one does.
            DISK_ATTEMPTED.clear();
            releaseDiskFiles = true;
            WORK_LOCK.notifyAll();
        }
        diskTilesRead.set(0);
        epoch.incrementAndGet();

        // Before the store is published, so a tile read cannot ask a source that
        // is still pointed at the previous world.
        LodRegionFileSource.bind(enabled ? world : null);

        if (world == null || !enabled) {
            return;
        }

        LodWorldId worldId = identify(world);
        String label = labelFor(world);
        File file = new File(cacheDirectory(), worldId.fileName());
        Store store = new Store(worldId, label, file);
        current = store;

        if (!loggedIdentity) {
            loggedIdentity = true;
            RemixLodBridge.log("lod store: " + worldId + " -> " + file.getAbsolutePath());
        }
        requestLoad(store);
    }

    static void flush() {
        Store store = current;
        if (store != null) {
            requestSave(store);
        }
    }

    /** Lets pruning keep the tiles nearest the player. */
    static void setPlayerPosition(int blockX, int blockZ) {
        playerBlockX = blockX;
        playerBlockZ = blockZ;
    }

    /** Periodic save, called from the scheduler's tick. */
    static void maybeSave() {
        Store store = current;
        if (store == null || !store.dirty) {
            return;
        }
        if (System.currentTimeMillis() - store.lastSaveMillis < SAVE_INTERVAL_MILLIS) {
            return;
        }
        requestSave(store);
    }

    // ------------------------------------------------------------ read paths

    /**
     * Cells for a tile, or null when it is not available yet.
     *
     * <p>A coarse tile absent from the store is queued for derivation and
     * answered on a later call. Never blocks, never derives inline: this is on
     * the frame path.
     */
    static byte[] tile(LodTileKey key) {
        return tile(key, true);
    }

    /**
     * Cells for a tile only if the store can answer without deriving one.
     *
     * <p>For the apron: the mesher reads a one-cell border of the neighbouring
     * tiles so that two regions agree on the heights along the boundary they
     * share, and a border it cannot have simply falls back to the region-edge
     * curtain. Asking for those tiles the ordinary way would queue a derivation
     * per neighbour of every region built -- work for ground that in the common
     * case is either already being derived on its own account, because the
     * neighbouring region wants it too, or outside the field entirely and drawn
     * by nobody. Either way it would land ahead of tiles that are being drawn.
     */
    static byte[] tileIfResident(LodTileKey key) {
        return tile(key, false);
    }

    private static byte[] tile(LodTileKey key, boolean deriveIfMissing) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return null;
        }

        Long packed = Long.valueOf(key.packed());
        boolean canonical = key.level() <= LodTileKey.CANONICAL_LEVEL;

        byte[] cells = lookupInflated(packed);
        if (cells == null) {
            Entry entry = store.tiles.get(packed);
            if (entry != null) {
                cells = decodeCells(entry);
                if (cells != null) {
                    storeInflated(packed, cells);
                }
            }
        }

        if (deriveIfMissing) {
            if (cells != null) {
                // Present is not the same as complete, and only a canonical tile
                // can be topped up: the live sampler fills one a chunk at a time
                // and chunks unload before it gets all sixteen, so the tiles the
                // innermost ring draws are typically a third full. A third-full
                // tile is the hole in the picture, not a missing one, and it is
                // the case the region reader exists to close.
                if (canonical) {
                    requestDiskRead(packed, cells);
                }
            } else if (canonical) {
                // The innermost ring draws canonical tiles directly, so it never
                // goes through a derivation and would otherwise be the one ring
                // the save could not supply.
                requestDiskRead(packed, null);
            } else {
                requestDerive(packed);
            }
        }
        return cells;
    }

    /** True when a tile's cells are already available without deriving. */
    static boolean hasTile(LodTileKey key) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return false;
        }
        return store.tiles.containsKey(Long.valueOf(key.packed()));
    }

    /**
     * Whether a tile can be built from right now, asking for it if not.
     *
     * <p>What the scheduler's per-tick probe calls instead of {@link #tile}.
     * That one inflates the tile to answer, and the probe runs up to sixteen
     * times a frame on the render thread over ground that thread will never
     * mesh: the worker inflates the same tile a moment later when it builds
     * the region, and caches it. Answering from the map alone keeps the frame
     * path to a lookup. The side effects are kept, because they are the point:
     * a coarse tile that is missing is queued for derivation and a canonical
     * one for a read of the save, and this is the only place the field ever
     * asks for either.
     */
    static boolean probeTile(LodTileKey key) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return false;
        }
        Long packed = Long.valueOf(key.packed());
        boolean canonical = key.level() <= LodTileKey.CANONICAL_LEVEL;
        Entry entry = store.tiles.get(packed);
        if (entry != null) {
            if (canonical && !entry.complete) {
                requestDiskRead(packed, null);
            }
            return true;
        }
        if (canonical) {
            requestDiskRead(packed, null);
        } else {
            requestDerive(packed);
        }
        return false;
    }

    /**
     * Asks the save again about a canonical tile that is still partial.
     *
     * <p>{@link #requestDiskRead} turns a request away when its queue is full
     * and leaves it to the scheduler to ask again. The scheduler used to do
     * that by re-probing every region on every sweep; now that it only
     * re-probes regions whose tiles have changed, a tile the save was refused
     * for and that nothing else has touched would never be asked about again.
     * This is the sweep's way of asking without rebuilding anything: a lookup,
     * and nothing more for a tile that has no gaps left.
     */
    static void requestDiskReadIfPartial(long packed) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return;
        }
        Long boxed = Long.valueOf(packed);
        Entry entry = store.tiles.get(boxed);
        if (entry != null && entry.complete) {
            return;
        }
        requestDiskRead(boxed, null);
    }

    /**
     * Epoch at which a tile was last published, or zero when the store holds
     * no such tile. See {@link Entry#epoch} for why this exists.
     */
    static int tileEpoch(long packed) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return 0;
        }
        Entry entry = store.tiles.get(Long.valueOf(packed));
        return entry == null ? 0 : entry.epoch;
    }

    /**
     * Whether a job posted for {@code world} is still writing into that
     * world's store. A sample that outlived a world change must not land its
     * tile in whichever world is bound now.
     */
    static boolean isBoundTo(fd world) {
        return world != null && world == boundWorld && isReady();
    }

    // ----------------------------------------------------------- write paths

    /**
     * Stores an observation, if it beats what is already there.
     *
     * <p>Writing marks the tile dirty and drops every coarse tile above it, so
     * the next read of an ancestor derives it again from the new data. Ancestors
     * are dropped rather than recomputed here because most of them are not being
     * drawn, and recomputing a level-5 tile because one chunk moved would cost
     * far more than letting the next reader ask for it.
     */
    static boolean putTile(LodTileKey key, byte[] cells, int source, long stamp) {
        Store store = current;
        if (!enabled || store == null) {
            return false;
        }
        if (cells == null || cells.length != LodTileKey.TILE_BYTES) {
            return false;
        }

        Long packed = Long.valueOf(key.packed());
        Entry existing = store.tiles.get(packed);
        if (existing != null
                && !LodProvenance.supersedes(source, stamp, existing.source, existing.stamp)) {
            return false;
        }
        return write(store, key, cells, source, stamp);
    }

    /**
     * Fills a tile's unknown cells from a source too weak to replace it.
     *
     * <p>{@link #putTile} is the right shape for a producer that can answer for
     * a whole tile, and the wrong shape for one that can only answer for part of
     * it. The live sampler records a chunk at a time, and chunks load and unload
     * as the player moves, so its tiles are typically a third full; the save
     * holds the rest but is weaker evidence and so can never win the provenance
     * check. Merging is what lets the weaker source close the gaps without
     * touching what the stronger one already knows -- which is exactly the
     * difference between distant terrain that is solid and distant terrain that
     * is a scatter of patches.
     *
     * <p>The result is stored under the weaker of the two sources, because the
     * tile is now a mixture and must stay open to a real observation of the same
     * ground later.
     *
     * @return true when the merge added something and was stored.
     */
    static boolean mergeTile(LodTileKey key, byte[] cells, int source, long stamp) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return false;
        }
        if (cells == null || cells.length != LodTileKey.TILE_BYTES) {
            return false;
        }

        Entry existing = store.tiles.get(Long.valueOf(key.packed()));
        byte[] merged = tileForUpdate(key);
        if (merged == null || LodCell.fillUnknown(merged, cells) == 0) {
            // Every cell this source could supply was already known. Storing the
            // result would be a write, a dirty file and a mesh rebuild for bytes
            // identical to the ones already there.
            return false;
        }

        return write(
                store,
                key,
                merged,
                existing == null ? source : LodProvenance.weaker(existing.source, source),
                stamp);
    }

    /** Publishes a tile, with the decision about whether it should have been already made. */
    private static boolean write(Store store, LodTileKey key, byte[] cells, int source, long stamp) {
        Long packed = Long.valueOf(key.packed());
        Entry existing = store.tiles.get(packed);
        byte[] blob = LodTileCodec.encode(key, source, stamp, cells);

        // Re-observing ground that has not changed produces identical bytes.
        // Storing them anyway would dirty the file, drop every derived ancestor
        // and cost a mesh rebuild, all to arrive back where we started -- and
        // re-observation is the common case, since the sampler sweeps the same
        // chunks around a player who has not moved far.
        if (existing != null && LodTileCodec.sameContent(existing.blob, blob)) {
            return false;
        }

        store.tiles.put(packed, new Entry(
                blob, source, stamp, epoch.incrementAndGet(), LodCell.allKnown(cells)));
        storeInflated(packed, cells.clone());

        // Derived tiles are recomputed on demand, so they never make the file
        // dirty; only observations are worth writing to disk.
        if (source != LodProvenance.DERIVED) {
            store.dirty = true;
            invalidateAncestors(store, key);
        }
        return true;
    }

    /**
     * Merges a chunk's cells into its canonical tile.
     *
     * <p>A tile is four chunks square, so a chunk owns one sixteenth of it and
     * must not blank the rest. The existing tile is read, the chunk's corner
     * overwritten, and the whole written back.
     *
     * @return the tile buffer the caller should sample into, already holding
     *     whatever was known before, or null when the store is not ready.
     */
    static byte[] tileForUpdate(LodTileKey key) {
        Store store = current;
        if (!enabled || store == null || !store.loaded) {
            return null;
        }

        byte[] existing = tile(key);
        if (existing != null) {
            return existing.clone();
        }

        byte[] blank = new byte[LodTileKey.TILE_BYTES];
        // A fresh tile is all holes until something is sampled into it; the
        // zeroed KNOWN flag already says so, so no explicit clear is needed.
        return blank;
    }

    /** Drops every coarse tile covering this one, so they derive again. */
    private static void invalidateAncestors(Store store, LodTileKey key) {
        LodTileKey ancestor = key.parent();
        while (ancestor != null) {
            Long packed = Long.valueOf(ancestor.packed());
            Entry entry = store.tiles.get(packed);
            if (entry != null && entry.source == LodProvenance.DERIVED) {
                store.tiles.remove(packed);
                removeInflated(packed);
            }
            ancestor = ancestor.parent();
        }
    }

    // ------------------------------------------------------------ derivation

    private static void requestDerive(Long packed) {
        synchronized (WORK_LOCK) {
            if (DERIVE_REQUESTS.size() >= MAX_PENDING_DERIVATIONS) {
                // The scheduler asks again for anything it still wants, so
                // dropping the oldest request loses nothing but a retry.
                DERIVE_REQUESTS.pollFirst();
            }
            if (!DERIVE_REQUESTS.contains(packed)) {
                DERIVE_REQUESTS.addLast(packed);
                WORK_LOCK.notifyAll();
            }
        }
        ensureWorker();
    }

    // ------------------------------------------------------------- disk reads

    /**
     * Asks the world's own save about a canonical tile, at most once a session.
     *
     * <p>Cheap enough for the frame path: a volatile read rejects every world
     * that has no save to read, and everything else is one lock and two hash
     * lookups -- the same cost {@link #requestDerive} already pays beside it.
     */
    private static void requestDiskRead(Long packed, byte[] known) {
        if (!LodRegionFileSource.isAvailable()) {
            return;
        }
        if (LodCell.allKnown(known)) {
            // A tile with no gaps has nothing the save could add, and on a store
            // that has been filled in over several sessions most tiles are in
            // that state. Skipping them is the difference between the reader
            // costing a few seconds of idle worker time and costing minutes.
            return;
        }
        synchronized (WORK_LOCK) {
            if (DISK_ATTEMPTED.contains(packed) || DISK_REQUESTS.contains(packed)) {
                return;
            }
            if (DISK_REQUESTS.size() >= MAX_PENDING_DISK_READS) {
                // Refused rather than made room for: the queue is drained
                // nearest-first, so what is in it is already the best of what has
                // been asked for, and the scheduler asks again for anything it
                // still wants.
                return;
            }
            DISK_REQUESTS.add(packed);
            WORK_LOCK.notifyAll();
        }
        ensureWorker();
    }

    /**
     * Takes the pending reads closest to the player.
     *
     * <p>Linear in the queue rather than sorted, because the distances change
     * every time the player moves and a scan of a few hundred entries is nothing
     * beside the milliseconds of file reading it is choosing between. Called with
     * {@link #WORK_LOCK} held.
     */
    private static List<Long> takeNearestDiskRequests(int count) {
        int centerX = playerBlockX;
        int centerZ = playerBlockZ;
        List<Long> taken = new ArrayList<Long>(count);
        for (int round = 0; round < count && !DISK_REQUESTS.isEmpty(); round++) {
            Long packed = null;
            int bestDistance = 0;
            for (Long candidate : DISK_REQUESTS) {
                int candidateDistance = distance(candidate, centerX, centerZ);
                if (packed == null || candidateDistance < bestDistance) {
                    bestDistance = candidateDistance;
                    packed = candidate;
                }
            }
            DISK_REQUESTS.remove(packed);

            // Marked before the read rather than after, so a tile the save turns
            // out to hold nothing for is not asked about again, and so the read
            // itself cannot re-queue the tile it is in the middle of filling.
            if (DISK_ATTEMPTED.size() >= MAX_REMEMBERED_DISK_READS) {
                DISK_ATTEMPTED.clear();
            }
            DISK_ATTEMPTED.add(packed);
            taken.add(packed);
        }
        return taken;
    }

    /**
     * Reads a few canonical tiles out of the world's region files.
     *
     * <p>Merged rather than stored outright: the tile usually already holds
     * whatever the live sampler managed to catch, and live is the better
     * evidence for the cells it covers.
     */
    private static void runDiskReads(List<Long> requests) {
        Store store = current;
        if (store == null || !store.loaded) {
            return;
        }

        byte[] fresh = new byte[LodTileKey.TILE_BYTES];
        for (Long packed : requests) {
            if (store != current) {
                return;
            }
            LodTileKey key = LodTileKey.unpack(packed.longValue());
            // A fresh buffer per tile is a blank slate rather than the previous
            // tile's cells: the reader writes only the chunks the save holds, so
            // anything left over would be read back as this tile's ground.
            Arrays.fill(fresh, (byte) 0);
            if (LodRegionFileSource.fill(key, fresh) > 0
                    && mergeTile(key, fresh, LodProvenance.DISK, System.currentTimeMillis())) {
                diskTilesRead.incrementAndGet();
            }
        }

        boolean drained;
        synchronized (WORK_LOCK) {
            drained = DISK_REQUESTS.isEmpty();
        }
        if (drained) {
            LodRegionFileSource.releaseFiles();
        }
    }

    /**
     * Builds one coarse tile from its children, recursing down to the canonical
     * level where the observations live.
     *
     * @return the derived cells, or null when no descendant holds anything --
     *     ground nobody has ever seen derives to nothing rather than to a tile
     *     of holes, so the store is not filled with empty coarse tiles.
     */
    private static byte[] derive(Store store, LodTileKey key) {
        if (key.level() <= LodTileKey.CANONICAL_LEVEL) {
            Long packed = Long.valueOf(key.packed());
            Entry entry = store.tiles.get(packed);

            // This is where a coarse tile discovers what it is missing, and so
            // the one place that knows which ground is worth asking the save
            // about. Asked for even when the tile is present, because a present
            // tile is usually a partial one -- the live sampler fills a tile a
            // chunk at a time and rarely gets all sixteen -- and its gaps are
            // exactly what the save can close.
            byte[] cells = entry == null ? null : decodeCells(entry);
            requestDiskRead(packed, cells);
            return cells;
        }

        Entry existing = store.tiles.get(Long.valueOf(key.packed()));
        if (existing != null) {
            return decodeCells(existing);
        }

        byte[][] children = new byte[4][];
        boolean any = false;
        for (int quadrant = 0; quadrant < 4; quadrant++) {
            children[quadrant] = derive(store, key.child(quadrant));
            any |= children[quadrant] != null;
        }
        if (!any) {
            return null;
        }

        byte[] cells = new byte[LodTileKey.TILE_BYTES];
        LodReducer.reduceTile(children, cells);

        long now = System.currentTimeMillis();
        byte[] blob = LodTileCodec.encode(key, LodProvenance.DERIVED, now, cells);
        store.tiles.put(
                Long.valueOf(key.packed()),
                new Entry(
                        blob, LodProvenance.DERIVED, now, epoch.incrementAndGet(),
                        LodCell.allKnown(cells)));
        return cells;
    }

    // ------------------------------------------------------------- inflation

    private static byte[] decodeCells(Entry entry) {
        LodTileCodec.Decoded decoded = LodTileCodec.decode(entry.blob, 0);
        return decoded == null ? null : decoded.cells;
    }

    private static byte[] lookupInflated(Long packed) {
        synchronized (INFLATED) {
            return INFLATED.get(packed);
        }
    }

    private static void storeInflated(Long packed, byte[] cells) {
        synchronized (INFLATED) {
            INFLATED.put(packed, cells);
        }
    }

    private static void removeInflated(Long packed) {
        synchronized (INFLATED) {
            INFLATED.remove(packed);
        }
    }

    private static void clearInflated() {
        synchronized (INFLATED) {
            INFLATED.clear();
        }
    }

    // -------------------------------------------------------- world identity

    /**
     * Which world this is.
     *
     * <p>Seed from the world info, dimension from the provider. Deliberately
     * nothing about where the save sits: identity that travels with the world is
     * what lets a cache file be shared, and the earlier path-based identity is
     * exactly what stopped it.
     */
    private static LodWorldId identify(fd world) {
        long seed = 0L;
        ei info = world.x();
        if (info != null) {
            seed = info.b();
        }
        int dimension = world.t != null ? world.t.g : 0;
        return new LodWorldId(seed, dimension);
    }

    /** A readable name for the world, stored in the file but never compared. */
    private static String labelFor(fd world) {
        wt saveHandler = world.w;
        if (saveHandler instanceof fm) {
            File directory = ((fm) saveHandler).a();
            if (directory != null) {
                return directory.getName();
            }
        }
        return "world";
    }

    private static File cacheDirectory() {
        return new File(System.getProperty("user.dir", "."), CACHE_DIRECTORY_NAME);
    }

    // ------------------------------------------------------------- background

    private static void requestLoad(Store store) {
        ensureWorker();
        synchronized (WORK_LOCK) {
            loadRequest = store;
            WORK_LOCK.notifyAll();
        }
    }

    /**
     * Runs one region build on the worker.
     *
     * <p>Builds go ahead of saves and derivations because they are the only
     * work here anyone is waiting to see: a save is housekeeping and a
     * derivation feeds a build that has already been told to come back later,
     * where a build is a piece of the world that is currently a hole. Neither
     * starves -- the scheduler posts at most a few builds a tick, so the queue
     * empties between them.
     */
    static void postRegionBuild(Runnable job) {
        if (job == null) {
            return;
        }
        ensureWorker();
        synchronized (WORK_LOCK) {
            BUILD_REQUESTS.addLast(job);
            WORK_LOCK.notifyAll();
        }
    }

    /**
     * Runs one chunk sample on the worker.
     *
     * <p>Sampling ran on the client thread, and it was the largest thing that
     * thread spent on distant terrain: per chunk a bulk biome query, a scan of
     * 256 columns and a deflate at the codec's highest level, up to four of
     * them a frame -- and in exactly the frames when new chunks were arriving,
     * which is when the frame could least afford them. The client thread now
     * only picks the chunk and hands over its arrays.
     *
     * <p>Behind builds, ahead of saves and derivations. A sample is what makes
     * the next build possible, but a build is ground that is a hole right now,
     * and the build queue drains between the scheduler's postings anyway.
     */
    static void postChunkSample(Runnable job) {
        if (job == null) {
            return;
        }
        ensureWorker();
        synchronized (WORK_LOCK) {
            SAMPLE_REQUESTS.addLast(job);
            WORK_LOCK.notifyAll();
        }
    }

    private static void requestSave(Store store) {
        if (!store.dirty) {
            return;
        }
        ensureWorker();
        synchronized (WORK_LOCK) {
            if (!SAVE_REQUESTS.contains(store)) {
                SAVE_REQUESTS.addLast(store);
            }
            WORK_LOCK.notifyAll();
        }
    }

    private static void ensureWorker() {
        boolean installHook = false;
        synchronized (WORK_LOCK) {
            if (worker == null) {
                worker = new Thread(new Runnable() {
                    public void run() {
                        workerLoop();
                    }
                }, "mcrtx-lod-store");
                worker.setDaemon(true);
                worker.setPriority(Thread.MIN_PRIORITY);
                worker.start();
            }
            if (!shutdownHookInstalled) {
                shutdownHookInstalled = true;
                installHook = true;
            }
        }
        if (installHook) {
            installShutdownHook();
        }
    }

    private static void installShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    // The worker is a daemon and will not outlive the process,
                    // so the last stretch of a session is only kept if the write
                    // happens here.
                    Store store = current;
                    if (store != null && store.dirty) {
                        saveStore(store);
                    }
                }
            }, "mcrtx-lod-store-flush"));
        } catch (Throwable failure) {
            // Already shutting down; nothing useful to do about it.
        }
    }

    private static void workerLoop() {
        while (true) {
            Store toLoad = null;
            Store toSave = null;
            Runnable toRun = null;
            List<Long> toDerive = null;
            List<Long> toRead = null;
            boolean toRelease = false;

            synchronized (WORK_LOCK) {
                while (loadRequest == null && SAVE_REQUESTS.isEmpty()
                        && BUILD_REQUESTS.isEmpty() && SAMPLE_REQUESTS.isEmpty()
                        && DERIVE_REQUESTS.isEmpty()
                        && DISK_REQUESTS.isEmpty() && !releaseDiskFiles) {
                    try {
                        WORK_LOCK.wait();
                    } catch (InterruptedException interrupted) {
                        return;
                    }
                }
                if (releaseDiskFiles) {
                    releaseDiskFiles = false;
                    toRelease = true;
                }
                if (loadRequest != null) {
                    toLoad = loadRequest;
                    loadRequest = null;
                } else if (!BUILD_REQUESTS.isEmpty()) {
                    toRun = BUILD_REQUESTS.pollFirst();
                } else if (!SAMPLE_REQUESTS.isEmpty()) {
                    toRun = SAMPLE_REQUESTS.pollFirst();
                } else if (!SAVE_REQUESTS.isEmpty()) {
                    toSave = SAVE_REQUESTS.pollFirst();
                } else if (!DERIVE_REQUESTS.isEmpty()) {
                    toDerive = new ArrayList<Long>(DERIVE_BATCH);
                    for (int i = 0; i < DERIVE_BATCH && !DERIVE_REQUESTS.isEmpty(); i++) {
                        toDerive.add(DERIVE_REQUESTS.pollFirst());
                    }
                } else {
                    // Last, and re-checked from the top after every slice. Disk
                    // reads are the only work here that has no end -- an explored
                    // world always has more ground on it than the field can hold
                    // -- so anything above them would starve if they were not the
                    // thing that yields.
                    toRead = takeNearestDiskRequests(DISK_BATCH);
                }
            }

            try {
                if (toRelease) {
                    LodRegionFileSource.releaseFiles();
                }
                if (toLoad != null) {
                    loadStore(toLoad);
                }
                if (toRun != null) {
                    toRun.run();
                }
                if (toSave != null) {
                    saveStore(toSave);
                }
                if (toDerive != null) {
                    runDerivations(toDerive);
                }
                if (toRead != null) {
                    runDiskReads(toRead);
                }
            } catch (Throwable failure) {
                RemixLodBridge.log("lod store: background task failed: " + failure);
            }
        }
    }

    private static void runDerivations(List<Long> requests) {
        Store store = current;
        if (store == null || !store.loaded) {
            return;
        }
        // Each tile derived stamps its own epoch as it is published, and that
        // moves the store's epoch with it, so there is nothing to signal here.
        for (Long packed : requests) {
            if (store != current) {
                return;
            }
            derive(store, LodTileKey.unpack(packed.longValue()));
        }
    }

    // ------------------------------------------------------------------- I/O

    /**
     * Reads a store's file into it, then makes it readable.
     *
     * <p>Tiles already present were written from live chunks while the read was
     * in flight, so the file must not replace them; {@link #putTile}'s provenance
     * check is what settles that, and it is why loading goes through it.
     */
    private static void loadStore(Store store) {
        long startMillis = System.currentTimeMillis();
        int accepted = 0;
        int rejected = 0;
        String problem = null;

        if (store.file.isFile()) {
            InputStream input = null;
            try {
                input = new BufferedInputStream(new FileInputStream(store.file));
                byte[] contents = readFully(input);

                LodStoreFile header = LodStoreFile.decode(contents);
                if (header == null) {
                    problem = "not a lod cache file";
                } else if (!header.worldId().equals(store.worldId)) {
                    // Two worlds landed on one name. The stored identity is the
                    // arbiter; the store starts empty rather than draping one
                    // world's ground over another's.
                    problem = "belongs to " + header.worldId();
                } else if (!header.isReadable()) {
                    problem = "tile format v" + header.tileCodecVersion() + ", this build reads v"
                            + LodTileCodec.VERSION;
                } else {
                    int offset = header.headerBytes();
                    while (offset < contents.length) {
                        LodTileCodec.Decoded decoded = LodTileCodec.decode(contents, offset);
                        if (decoded == null) {
                            rejected++;
                            break;
                        }
                        offset += decoded.consumed;
                        Entry entry = new Entry(
                                LodTileCodec.encode(
                                        decoded.key, decoded.source, decoded.stampMillis, decoded.cells),
                                decoded.source,
                                decoded.stampMillis,
                                epoch.incrementAndGet(),
                                LodCell.allKnown(decoded.cells));
                        Long packed = Long.valueOf(decoded.key.packed());
                        Entry existing = store.tiles.get(packed);
                        if (existing == null
                                || LodProvenance.supersedes(
                                        decoded.source, decoded.stampMillis,
                                        existing.source, existing.stamp)) {
                            store.tiles.put(packed, entry);
                            accepted++;
                        }
                    }
                }
            } catch (Throwable failure) {
                problem = String.valueOf(failure);
            } finally {
                closeQuietly(input);
            }
        }

        store.loaded = true;
        epoch.incrementAndGet();

        StringBuilder line = new StringBuilder(96);
        line.append("lod store: loaded ").append(accepted).append(" tiles in ")
                .append(System.currentTimeMillis() - startMillis).append(" ms");
        if (rejected > 0) {
            line.append(", ").append(rejected).append(" unreadable");
        }
        if (problem != null) {
            line.append(" (").append(problem).append(')');
        }
        RemixLodBridge.log(line.toString());
    }

    /**
     * Writes the store's observations through a temporary file and renames.
     *
     * <p>Derived tiles are skipped: they are a pure function of what is written,
     * so storing them would bloat a file that is meant to be shared for bytes
     * the reader can compute in milliseconds.
     */
    private static void saveStore(Store store) {
        store.dirty = false;
        store.lastSaveMillis = System.currentTimeMillis();

        List<Long> keys = new ArrayList<Long>(store.tiles.size());
        for (Map.Entry<Long, Entry> entry : store.tiles.entrySet()) {
            if (entry.getValue().source != LodProvenance.DERIVED) {
                keys.add(entry.getKey());
            }
        }
        prune(keys);

        int levelMask = 0;
        for (Long packed : keys) {
            levelMask |= 1 << LodTileKey.unpackLevel(packed.longValue());
        }

        File directory = store.file.getParentFile();
        if (directory != null && !directory.isDirectory() && !directory.mkdirs()) {
            RemixLodBridge.log("lod store: cannot create " + directory.getAbsolutePath());
            return;
        }

        File temporary = new File(store.file.getAbsolutePath() + ".tmp");
        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(temporary));
            output.write(
                    new LodStoreFile(
                            store.worldId, store.label, levelMask, keys.size(), LodTileCodec.VERSION)
                            .encode());

            for (Long packed : keys) {
                Entry entry = store.tiles.get(packed);
                if (entry != null) {
                    output.write(entry.blob);
                }
            }
            output.close();
            output = null;

            // Rename over the previous file, so an interrupted write leaves the
            // old cache intact rather than a half-written one.
            if (store.file.exists() && !store.file.delete()) {
                RemixLodBridge.log("lod store: cannot replace " + store.file.getName());
                return;
            }
            if (!temporary.renameTo(store.file)) {
                RemixLodBridge.log("lod store: cannot rename into " + store.file.getName());
            }
        } catch (Throwable failure) {
            RemixLodBridge.log("lod store: save failed: " + failure);
            store.dirty = true;
        } finally {
            closeQuietly(output);
        }
    }

    /**
     * Drops the tiles furthest from the player until the list fits the budget.
     *
     * <p>Distance is measured at each tile's own scale, so a coarse tile is not
     * penalised for covering ground far away -- covering ground far away is what
     * it is for.
     */
    private static void prune(List<Long> keys) {
        if (keys.size() <= MAX_TILES) {
            return;
        }

        final int centerX = playerBlockX;
        final int centerZ = playerBlockZ;
        Collections.sort(keys, new Comparator<Long>() {
            public int compare(Long left, Long right) {
                return Integer.compare(distance(left, centerX, centerZ), distance(right, centerX, centerZ));
            }
        });

        int target = Math.min(keys.size(), MAX_TILES - PRUNE_HEADROOM);
        if (target < 0) {
            target = 0;
        }
        Store store = current;
        List<Long> dropped = new ArrayList<Long>(keys.size() - target);
        for (int index = keys.size() - 1; index >= target; index--) {
            Long packed = keys.remove(index);
            dropped.add(packed);
            if (store != null) {
                store.tiles.remove(packed);
                removeInflated(packed);
            }
        }

        // Forgetting a tile and forgetting that the save was asked about it are
        // the same act. Keeping the attempt would leave a permanent hole where a
        // player walked away and came back: the tile is gone from memory and the
        // one source that could rebuild it has been told never to try again.
        synchronized (WORK_LOCK) {
            DISK_ATTEMPTED.removeAll(dropped);
        }
    }

    private static int distance(Long packed, int centerX, int centerZ) {
        LodTileKey key = LodTileKey.unpack(packed.longValue());
        return key.chebyshevDistanceToCenter(centerX, centerZ);
    }

    private static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[1 << 16];
        int length = 0;
        while (true) {
            if (length == buffer.length) {
                byte[] grown = new byte[buffer.length << 1];
                System.arraycopy(buffer, 0, grown, 0, length);
                buffer = grown;
            }
            int read = input.read(buffer, length, buffer.length - length);
            if (read < 0) {
                break;
            }
            length += read;
        }
        byte[] exact = new byte[length];
        System.arraycopy(buffer, 0, exact, 0, length);
        return exact;
    }

    private static void closeQuietly(java.io.Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException ignored) {
            // Nothing useful to do; the caller already reported any real failure.
        }
    }
}
