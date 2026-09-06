import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.RemixSceneBridge;

final class RemixChunkWorldState {
    private static final RemixWorldListener WORLD_LISTENER = new RemixWorldListener();
    private static final Map<dk, Long> OWNERS = new IdentityHashMap<dk, Long>();
    private static final Map<Long, Long> LIFETIMES = new HashMap<Long, Long>();
    private static final Map<Long, Integer> REFERENCES = new HashMap<Long, Integer>();
    private static fd attachedWorld;
    private static long generation;
    private static long nextLifetime;

    private RemixChunkWorldState() { }
    static fd attachedWorld() { return attachedWorld; }
    static int residentCount() { return LIFETIMES.size(); }
    static long generation() { return generation; }
    static long lifetime(long key) {
        Long value = LIFETIMES.get(key);
        return value == null ? 0 : value.longValue();
    }

    static void position(dk renderer) {
        if (renderer.a == null || renderer.a != attachedWorld) {
            return;
        }
        long key = RemixChunkSectionKey.encode(renderer.c, renderer.d, renderer.e);
        Long old = OWNERS.get(renderer);
        if (old != null && old.longValue() == key) {
            return;
        }
        unload(renderer);
        OWNERS.put(renderer, Long.valueOf(key));
        Integer references = REFERENCES.get(key);
        REFERENCES.put(key, references == null ? 1 : references.intValue() + 1);
        if (!LIFETIMES.containsKey(key)) {
            LIFETIMES.put(key, Long.valueOf(++nextLifetime));
            RemixChunkBridge.resetTerrain(generation);
            RemixChunkBridge.allocateSection(renderer.c, renderer.d, renderer.e, generation, nextLifetime);
            RemixChunkRecaptureQueue.queueSection(renderer.c, renderer.d, renderer.e);
        }
    }

    static void unload(dk renderer) {
        Long key = OWNERS.remove(renderer);
        if (key == null) {
            return;
        }
        int references = REFERENCES.get(key).intValue() - 1;
        if (references > 0) {
            REFERENCES.put(key, Integer.valueOf(references));
            return;
        }
        REFERENCES.remove(key);
        long lifetime = lifetime(key.longValue());
        LIFETIMES.remove(key);
        int x = RemixChunkSectionKey.originX(key), y = RemixChunkSectionKey.originY(key), z = RemixChunkSectionKey.originZ(key);
        RemixChunkRecaptureQueue.clearSection(x, y, z);
        RemixChunkBridge.removeSection(x, y, z, generation, lifetime);
    }

    static void clearSections() {
        ++generation;
        OWNERS.clear();
        REFERENCES.clear();
        LIFETIMES.clear();
        RemixChunkRecaptureQueue.resetForWorldChange();
        RemixSceneBridge.clearWorldScene();
        RemixChunkBridge.resetTerrain(generation);
    }

    static void onWorldChanged(fd world) {
        if (attachedWorld == world) {
            return;
        }
        clearSections();
        if (attachedWorld != null) {
            attachedWorld.b(WORLD_LISTENER);
        }
        attachedWorld = world;
        if (world != null) {
            world.a(WORLD_LISTENER);
        }
    }
}
