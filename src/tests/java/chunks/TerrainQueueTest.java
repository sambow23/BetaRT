import mcrtx.bridge.RemixChunkBridge;

public final class TerrainQueueTest {
    private static void check(boolean value, String message) {
        if (!value) { throw new AssertionError(message); }
    }
    private static void drain() {
        for (int i = 0; i < 1000; ++i) {
            RemixChunkRecaptureQueue.flush();
            if (RemixChunkRecaptureQueue.lastPendingQueueDepthAfterFlush() == 0) { return; }
        }
        throw new AssertionError("queue did not drain");
    }
    public static void main(String[] args) throws Exception {
        final fd world = new fd();
        RemixChunkWorldState.onWorldChanged(world);
        dk section = new dk(world, 0, 0, 0);
        RemixChunkWorldState.position(section);
        world.loaded = false;
        RemixChunkRecaptureQueue.flush();
        check(world.reads == 0, "unavailable world was read/generated");
        check(RemixChunkRecaptureQueue.lastPendingQueueDepthAfterFlush() == 1, "unavailable section lost dirty work");
        world.loaded = true;
        Thread.sleep(60);
        RemixChunkBridge.accept = false;
        for (int i = 0; i < 30; ++i) { RemixChunkRecaptureQueue.flush(); }
        check(RemixChunkBridge.updates > 0, "snapshot never attempted admission");
        check(RemixChunkRecaptureQueue.lastPendingQueueDepthAfterFlush() == 1, "backpressure lost dirty work");
        int reads = world.reads;
        RemixChunkRecaptureQueue.flush();
        check(world.reads == reads, "backpressure repeated snapshot work");
        RemixChunkBridge.accept = true;
        drain();
        check(RemixChunkBridge.lastRecords.length == 4096 * RemixChunkBridge.RECORD_WORDS, "initial snapshot is not full");
        int updates = RemixChunkBridge.updates;
        RemixChunkWorldState.position(section);
        drain();
        check(RemixChunkBridge.updates == updates, "unchanged allocation or camera rotation dirtied terrain");
        world.blocks[0] = 1;
        RemixChunkRecaptureQueue.queueRegion(0, 0, 0, 0, 0, 0);
        RemixChunkBlockCapture.duringCapture = new Runnable() {
            public void run() {
                world.blocks[0] = 0;
                RemixChunkRecaptureQueue.queueRegion(0, 0, 0, 0, 0, 0);
            }
        };
        drain();
        check(RemixChunkBridge.lastRecords[0] == 0, "invalidated snapshot published stale data");
        check(RemixChunkBridge.lastRecords.length == 8 * RemixChunkBridge.RECORD_WORDS, "partial bounds were lost");
        long before = RemixChunkWorldState.lifetime(RemixChunkSectionKey.encode(0, 0, 0));
        section.c = 16;
        RemixChunkWorldState.position(section);
        check(RemixChunkWorldState.lifetime(RemixChunkSectionKey.encode(0, 0, 0)) == 0, "reposition did not release residency");
        check(RemixChunkBridge.removals == 1, "reposition failed to notify unload");
        section.c = 0;
        RemixChunkWorldState.position(section);
        check(RemixChunkWorldState.lifetime(RemixChunkSectionKey.encode(0, 0, 0)) > before, "coordinate reuse did not change lifetime");
        RemixChunkWorldState.clearSections();
        drain();
        check(RemixChunkWorldState.lifetime(RemixChunkSectionKey.encode(0, 0, 0)) == 0, "world reset retained residency");
        for (int i = 0; i < 200; ++i) {
            RemixChunkWorldState.position(new dk(world, i * 16, 0, 0));
        }
        RemixChunkRecaptureQueue.queueRegion(0, 0, 0, 200 * 16, 15, 15);
        RemixChunkBridge.accept = false;
        RemixChunkRecaptureQueue.flush();
        check(RemixChunkRecaptureQueue.lastPendingQueueDepthAfterFlush() == 200, "large dirty region was truncated");
        RemixChunkBridge.accept = true;
        drain();
        RemixChunkWorldState.clearSections();
        final dk reused = new dk(world, 0, 0, 0);
        RemixChunkWorldState.position(reused);
        world.blocks[0] = 1;
        updates = RemixChunkBridge.updates;
        RemixChunkBlockCapture.duringCapture = new Runnable() {
            public void run() {
                RemixChunkWorldState.unload(reused);
                world.blocks[0] = 0;
                RemixChunkWorldState.position(reused);
            }
        };
        drain();
        check(RemixChunkBridge.updates == updates + 1 && RemixChunkBridge.lastRecords[0] == 0,
                "capture crossed a section lifetime or removed its successor");
        world.blocks[0] = 1;
        RemixChunkRecaptureQueue.queueSection(0, 0, 0);
        updates = RemixChunkBridge.updates;
        final fd nextWorld = new fd();
        RemixChunkBlockCapture.duringCapture = new Runnable() {
            public void run() {
                RemixChunkWorldState.onWorldChanged(nextWorld);
                RemixChunkWorldState.position(new dk(nextWorld, 0, 0, 0));
            }
        };
        drain();
        check(RemixChunkBridge.updates == updates + 1 && RemixChunkBridge.lastRecords[0] == 0,
                "capture crossed worlds or removed the new world's dirty work");
        System.out.println("Terrain residency, lossless queue, backpressure and snapshot restart checks passed");
    }
}
