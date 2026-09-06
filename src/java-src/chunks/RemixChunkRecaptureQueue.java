import java.util.LinkedHashMap;
import java.util.Map;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.RemixLifecycleBridge;

final class RemixChunkRecaptureQueue {
    private static final long SNAPSHOT_BUDGET_NANOS = 2_000_000L;
    private static final int SLICE_BLOCKS = 256;
    private static final Map<Long, DirtyChunkSection> PENDING = new LinkedHashMap<Long, DirtyChunkSection>();
    private static DirtyChunkSection collecting;
    private static long revision;
    private static long selection;
    private static long frames;
    private static int lastBefore, lastAfter, lastSections;
    private static long lastDuration;

    private RemixChunkRecaptureQueue() { }

    static void queueSection(int x, int y, int z) {
        queueBounds(x, y, z, x + 15, y + 15, z + 15);
    }

    static void queueRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        // Fluids, doors, textures and bounds can depend on adjacent Minecraft blocks.
        queueBounds(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
    }

    private static void queueBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        minY = Math.max(0, minY);
        maxY = Math.min(127, maxY);
        for (int y = (minY >> 4) << 4; y <= maxY; y += 16) {
            for (int z = (minZ >> 4) << 4; z <= maxZ; z += 16) {
                for (int x = (minX >> 4) << 4; x <= maxX; x += 16) {
                    Long key = Long.valueOf(RemixChunkSectionKey.encode(x, y, z));
                    if (RemixChunkWorldState.lifetime(key.longValue()) == 0) {
                        continue;
                    }
                    DirtyChunkSection pending = PENDING.get(key);
                    if (pending == null) {
                        pending = new DirtyChunkSection(x, y, z, minX, minY, minZ, maxX, maxY, maxZ);
                        PENDING.put(key, pending);
                    } else {
                        pending.mergeDirtyRegion(minX, minY, minZ, maxX, maxY, maxZ);
                    }
                    pending.revision = ++revision;
                }
            }
        }
    }

    static void clearSection(int x, int y, int z) {
        DirtyChunkSection removed = PENDING.remove(RemixChunkSectionKey.encode(x, y, z));
        if (collecting == removed) { collecting = null; }
    }
    static void resetForWorldChange() { PENDING.clear(); collecting = null; }
    static void clear() { resetForWorldChange(); }
    static int lastPendingQueueDepthBeforeFlush() { return lastBefore; }
    static int lastPendingQueueDepthAfterFlush() { return lastAfter; }
    static int lastSectionsRecaptured() { return lastSections; }
    static long lastFlushDurationNanos() { return lastDuration; }

    private static DirtyChunkSection select(long now) {
        DirtyChunkSection best = null;
        double score = Double.POSITIVE_INFINITY;
        boolean oldest = (selection + 1) % 8 == 0;
        for (DirtyChunkSection section : PENDING.values()) {
            if (section.retryAfter > now) {
                continue;
            }
            double dx = section.originX + 8 - RemixCameraState.cameraPositionX;
            double dy = section.originY + 8 - RemixCameraState.cameraPositionY;
            double dz = section.originZ + 8 - RemixCameraState.cameraPositionZ;
            double distance = dx * dx + dy * dy + dz * dz;
            if (best == null || distance < score) {
                best = section;
                score = distance;
            }
            if (oldest) {
                break;
            }
        }
        if (best != null) {
            ++selection;
        }
        return best;
    }

    private static boolean available(fd world, DirtyChunkSection section) {
        for (int z = section.originZ - 16; z <= section.originZ + 16; z += 16) {
            for (int x = section.originX - 16; x <= section.originX + 16; x += 16) {
                if (!world.i(x, 64, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    static void flush() {
        long start = System.nanoTime();
        lastBefore = PENDING.size();
        lastSections = 0;
        fd world = RemixChunkWorldState.attachedWorld();
        if (world != null && RemixLifecycleBridge.isInitialized()) {
            RemixChunkBridge.resetTerrain(RemixChunkWorldState.generation());
            while (System.nanoTime() - start < SNAPSHOT_BUDGET_NANOS) {
                DirtyChunkSection section = collecting == null ? select(System.nanoTime()) : collecting;
                if (section == null) {
                    break;
                }
                collecting = section;
                if (!available(world, section)) {
                    collecting = null;
                    section.records = null;
                    section.cursor = 0;
                    section.retryAfter = System.nanoTime() + 50_000_000L;
                    continue;
                }
                if (section.records == null || section.snapshotRevision != section.revision) {
                    section.snapshotBounds = new DirtyChunkSection(section.originX, section.originY, section.originZ,
                            section.dirtyMinX, section.dirtyMinY, section.dirtyMinZ, section.dirtyMaxX, section.dirtyMaxY, section.dirtyMaxZ);
                    section.records = new int[section.dirtyBlockVolume() * RemixChunkBridge.RECORD_WORDS];
                    section.cursor = 0;
                    section.snapshotRevision = section.revision;
                    section.snapshotGeneration = RemixChunkWorldState.generation();
                    section.snapshotLifetime = RemixChunkWorldState.lifetime(
                            RemixChunkSectionKey.encode(section.originX, section.originY, section.originZ));
                }
                DirtyChunkSection scan = section.snapshotBounds;
                int width = scan.dirtyMaxX - scan.dirtyMinX + 1;
                int depth = scan.dirtyMaxZ - scan.dirtyMinZ + 1;
                int end = Math.min(section.cursor + SLICE_BLOCKS, scan.dirtyBlockVolume());
                for (; section.cursor < end; ++section.cursor) {
                    int x = scan.dirtyMinX + section.cursor % width;
                    int z = scan.dirtyMinZ + section.cursor / width % depth;
                    int y = scan.dirtyMinY + section.cursor / (width * depth);
                    int id = world.a(x, y, z);
                    if (id > 0 && id < uu.m.length && uu.m[id] != null) {
                        RemixChunkBlockCapture.captureWorldBlock(world, x, y, z, id, world.e(x, y, z),
                                uu.m[id].b(), section.records, section.cursor * RemixChunkBridge.RECORD_WORDS);
                    }
                }
                if (world != RemixChunkWorldState.attachedWorld()
                        || section.snapshotGeneration != RemixChunkWorldState.generation()) {
                    break;
                }
                long key = RemixChunkSectionKey.encode(section.originX, section.originY, section.originZ);
                if (PENDING.get(Long.valueOf(key)) != section
                        || section.snapshotLifetime != RemixChunkWorldState.lifetime(key)) {
                    if (collecting == section) { collecting = null; }
                    continue;
                }
                if (section.snapshotRevision != section.revision) {
                    continue;
                }
                if (section.cursor == scan.dirtyBlockVolume()) {
                    RemixChunkBridge.allocateSection(section.originX, section.originY, section.originZ,
                            section.snapshotGeneration, section.snapshotLifetime);
                    boolean accepted = RemixChunkBridge.updateSection(section.originX, section.originY, section.originZ,
                            section.snapshotGeneration, section.snapshotLifetime, section.snapshotRevision,
                            scan.dirtyMinX - section.originX, scan.dirtyMinY - section.originY, scan.dirtyMinZ - section.originZ,
                            scan.dirtyMaxX - section.originX, scan.dirtyMaxY - section.originY, scan.dirtyMaxZ - section.originZ,
                            section.records);
                    if (accepted && section.snapshotRevision == section.revision) {
                        PENDING.remove(Long.valueOf(key));
                        collecting = null;
                        ++lastSections;
                    } else {
                        break;
                    }
                }
            }
        }
        lastAfter = PENDING.size();
        lastDuration = System.nanoTime() - start;
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.terrainSnapshot", lastDuration);
        HookProfiler.recordCount("terrain.pendingSnapshots", lastAfter);
        HookProfiler.recordCount("terrain.snapshots", lastSections);
        HookProfiler.recordCount("terrain.snapshotOverruns", lastDuration > SNAPSHOT_BUDGET_NANOS ? 1 : 0);
        if (++frames % 120 == 0) {
            System.out.println("[mcrtx] Terrain snapshots: resident=" + RemixChunkWorldState.residentCount()
                    + " pending=" + lastAfter + " snapshotNs=" + lastDuration);
        }
    }
}
