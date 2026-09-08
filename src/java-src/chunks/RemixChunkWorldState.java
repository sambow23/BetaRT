import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.RemixSceneBridge;

final class RemixChunkWorldState {
    private static final RemixWorldListener WORLD_LISTENER = new RemixWorldListener();
    private static final Set<Long> KNOWN_CHUNK_SECTIONS = new HashSet<Long>();
    private static final Set<Long> HAS_NATIVE_MESH_SECTIONS = new HashSet<Long>();
    private static final Set<Long> RESIDENT_CHUNK_SECTIONS = new HashSet<Long>();

    /**
     * How many sections of each chunk column currently have native geometry.
     * Distant terrain uses this to decide when real chunks have actually taken
     * over an area, so the LOD covering it can be hidden without leaving a hole
     * while the chunk meshes are still building.
     */
    private static final Map<Long, Integer> MESHED_SECTIONS_PER_COLUMN = new HashMap<Long, Integer>();

    private static fd attachedWorld;
    private static boolean loggedWorldListenerAttach;

    private RemixChunkWorldState() {
    }

    static fd attachedWorld() {
        return attachedWorld;
    }

    static void rememberKnownSection(int originX, int originY, int originZ) {
        KNOWN_CHUNK_SECTIONS.add(Long.valueOf(RemixChunkSectionKey.encode(originX, originY, originZ)));
    }

    static void markSectionResident(int originX, int originY, int originZ) {
        Long key = Long.valueOf(RemixChunkSectionKey.encode(originX, originY, originZ));
        if (HAS_NATIVE_MESH_SECTIONS.add(key)) {
            addColumnMesh(originX >> 4, originZ >> 4, 1);
        }
        RESIDENT_CHUNK_SECTIONS.add(key);
    }

    /** True once any section of this chunk column has native geometry. */
    static boolean columnHasNativeMesh(int chunkX, int chunkZ) {
        return MESHED_SECTIONS_PER_COLUMN.containsKey(columnKey(chunkX, chunkZ));
    }

    private static void addColumnMesh(int chunkX, int chunkZ, int delta) {
        Long key = columnKey(chunkX, chunkZ);
        Integer current = MESHED_SECTIONS_PER_COLUMN.get(key);
        int next = (current == null ? 0 : current.intValue()) + delta;
        if (next <= 0) {
            MESHED_SECTIONS_PER_COLUMN.remove(key);
        } else {
            MESHED_SECTIONS_PER_COLUMN.put(key, Integer.valueOf(next));
        }
    }

    private static Long columnKey(int chunkX, int chunkZ) {
        return Long.valueOf(((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL));
    }

    static void onChunkSectionUnload(int originX, int originY, int originZ) {
        RemixChunkRecaptureQueue.clearSection(originX, originY, originZ);
        forgetSection(originX, originY, originZ);
        RemixChunkBridge.unloadChunkSection(originX, originY, originZ);
        RemixCaveCulling.removeChunk(originX, originY, originZ);
    }

    static void onWorldChanged(fd world) {
        if (attachedWorld == world) {
            return;
        }

        RemixChunkRecaptureQueue.resetForWorldChange();
        KNOWN_CHUNK_SECTIONS.clear();
        HAS_NATIVE_MESH_SECTIONS.clear();
        RESIDENT_CHUNK_SECTIONS.clear();
        MESHED_SECTIONS_PER_COLUMN.clear();
        RemixSceneBridge.clearWorldScene();
        RemixChunkBridge.resetCaptureState();
        RemixCaveCulling.clear();

        if (attachedWorld != null) {
            attachedWorld.b(WORLD_LISTENER);
        }

        attachedWorld = world;
        if (attachedWorld != null) {
            attachedWorld.a(WORLD_LISTENER);
            if (!loggedWorldListenerAttach) {
                loggedWorldListenerAttach = true;
                System.out.println("[mcrtx] world listener attached");
            }
        }
    }

    static void syncSectionVisibility() {
        if (KNOWN_CHUNK_SECTIONS.isEmpty()) {
            return;
        }

        List<Long> newlyVisibleSections = new ArrayList<Long>();
        List<Long> newlyHiddenSections = new ArrayList<Long>();
        for (Long keyObject : KNOWN_CHUNK_SECTIONS) {
            long key = keyObject.longValue();
            int originX = RemixChunkSectionKey.originX(key);
            int originY = RemixChunkSectionKey.originY(key);
            int originZ = RemixChunkSectionKey.originZ(key);
            boolean shouldBeResident = RemixCaveCulling.isVisible(originX, originY, originZ)
                    && RemixCameraState.shouldCaptureChunkSection(originX, originY, originZ);
            boolean isResident = RESIDENT_CHUNK_SECTIONS.contains(keyObject);
            if (shouldBeResident && !isResident) {
                newlyVisibleSections.add(keyObject);
            } else if (!shouldBeResident && isResident) {
                newlyHiddenSections.add(keyObject);
            }
        }

        for (Long keyObject : newlyHiddenSections) {
            long key = keyObject.longValue();
            int originX = RemixChunkSectionKey.originX(key);
            int originY = RemixChunkSectionKey.originY(key);
            int originZ = RemixChunkSectionKey.originZ(key);
            RESIDENT_CHUNK_SECTIONS.remove(keyObject);
            RemixChunkBridge.setChunkSectionHidden(originX, originY, originZ, true);
        }

        for (Long keyObject : newlyVisibleSections) {
            long key = keyObject.longValue();
            int originX = RemixChunkSectionKey.originX(key);
            int originY = RemixChunkSectionKey.originY(key);
            int originZ = RemixChunkSectionKey.originZ(key);
            if (HAS_NATIVE_MESH_SECTIONS.contains(keyObject)) {
                RemixChunkBridge.setChunkSectionHidden(originX, originY, originZ, false);
                RESIDENT_CHUNK_SECTIONS.add(keyObject);
            } else {
                RemixChunkRecaptureQueue.queueRegion(
                        originX, originY, originZ,
                        originX + 15, originY + 15, originZ + 15);
            }
        }
    }

    private static void forgetSection(int originX, int originY, int originZ) {
        Long key = Long.valueOf(RemixChunkSectionKey.encode(originX, originY, originZ));
        KNOWN_CHUNK_SECTIONS.remove(key);
        if (HAS_NATIVE_MESH_SECTIONS.remove(key)) {
            addColumnMesh(originX >> 4, originZ >> 4, -1);
        }
        RESIDENT_CHUNK_SECTIONS.remove(key);
    }
}
