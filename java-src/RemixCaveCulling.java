import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class RemixCaveCulling {

    public static final int DIR_X_NEG = 0;
    public static final int DIR_X_POS = 1;
    public static final int DIR_Y_NEG = 2;
    public static final int DIR_Y_POS = 3;
    public static final int DIR_Z_NEG = 4;
    public static final int DIR_Z_POS = 5;

    public static class Pocket {
        public final long[] faceMasks = new long[24]; // 6 faces * 4 longs
        public final long[] blockMask = new long[64]; // 4096 bits
    }

    private static final Map<Long, Pocket[]> chunkPockets = new HashMap<Long, Pocket[]>();
    private static final Set<Long> visibleChunks = new HashSet<Long>();

    public static long getChunkKey(int x, int y, int z) {
        long cx = (x >> 4) & 0x3FFFFFFL;
        long cz = (z >> 4) & 0x3FFFFFFL;
        long cy = (y >> 4) & 0x7FFL;
        return (cx << 37) | (cz << 11) | cy;
    }

    public static void setPockets(int x, int y, int z, Pocket[] pockets) {
        chunkPockets.put(getChunkKey(x, y, z), pockets);
    }

    public static boolean isVisible(int x, int y, int z) {
        return visibleChunks.contains(getChunkKey(x, y, z));
    }

    public static void removeChunk(int x, int y, int z) {
        long key = getChunkKey(x, y, z);
        chunkPockets.remove(key);
        visibleChunks.remove(key);
    }

    public static void clear() {
        chunkPockets.clear();
        visibleChunks.clear();
    }

    private static long getNeighborKey(long key, int face) {
        long cx = (key >>> 37) & 0x3FFFFFFL;
        long cz = (key >>> 11) & 0x3FFFFFFL;
        long cy = key & 0x7FFL;

        if ((cx & 0x2000000L) != 0) cx |= 0xFFFFFFFFFC000000L;
        if ((cz & 0x2000000L) != 0) cz |= 0xFFFFFFFFFC000000L;

        if (face == DIR_X_NEG) cx--;
        else if (face == DIR_X_POS) cx++;
        else if (face == DIR_Y_NEG) cy--;
        else if (face == DIR_Y_POS) cy++;
        else if (face == DIR_Z_NEG) cz--;
        else if (face == DIR_Z_POS) cz++;

        cx &= 0x3FFFFFFL;
        cz &= 0x3FFFFFFL;
        cy &= 0x7FFL;
        return (cx << 37) | (cz << 11) | cy;
    }

    private static int getOppositeFace(int face) {
        if (face == DIR_X_NEG) return DIR_X_POS;
        if (face == DIR_X_POS) return DIR_X_NEG;
        if (face == DIR_Y_NEG) return DIR_Y_POS;
        if (face == DIR_Y_POS) return DIR_Y_NEG;
        if (face == DIR_Z_NEG) return DIR_Z_POS;
        if (face == DIR_Z_POS) return DIR_Z_NEG;
        return 0;
    }

    private static int extractX(long key) {
        long cx = (key >>> 37) & 0x3FFFFFFL;
        if ((cx & 0x2000000L) != 0) cx |= 0xFFFFFFFFFC000000L;
        return (int) cx;
    }

    private static int extractZ(long key) {
        long cz = (key >>> 11) & 0x3FFFFFFL;
        if ((cz & 0x2000000L) != 0) cz |= 0xFFFFFFFFFC000000L;
        return (int) cz;
    }

    private static int extractY(long key) {
        return (int) (key & 0x7FFL);
    }

    private static class PocketNode {
        final long chunkKey;
        final int index;
        PocketNode(long k, int i) { chunkKey = k; index = i; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PocketNode)) return false;
            PocketNode n = (PocketNode) o;
            return chunkKey == n.chunkKey && index == n.index;
        }
        @Override public int hashCode() {
            return (int)(chunkKey ^ (chunkKey >>> 32)) * 31 + index;
        }
    }

    public static void updateGlobalCulling(int playerX, int playerY, int playerZ) {
        if (chunkPockets.isEmpty()) return;

        Set<Long> newlyVisibleChunks = new HashSet<Long>();
        Set<PocketNode> visitedPockets = new HashSet<PocketNode>();
        Queue<PocketNode> queue = new ArrayDeque<PocketNode>();

        int skyCount = 0;
        // Add Sky pockets
        for (Map.Entry<Long, Pocket[]> entry : chunkPockets.entrySet()) {
            long chunkKey = entry.getKey().longValue();
            long cy = chunkKey & 0x7FFL;
            
            boolean isTopChunk = (cy == 7);
            boolean chunkAboveMissing = false;
            if (!isTopChunk) {
                long chunkAboveKey = getNeighborKey(chunkKey, DIR_Y_POS);
                if (!chunkPockets.containsKey(chunkAboveKey)) {
                    chunkAboveMissing = true;
                }
            }

            if (isTopChunk || chunkAboveMissing) { 
                Pocket[] pockets = entry.getValue();
                for (int i = 0; i < pockets.length; i++) {
                    Pocket p = pockets[i];
                    boolean touchesTop = false;
                    for (int j = 0; j < 4; j++) {
                        if (p.faceMasks[DIR_Y_POS * 4 + j] != 0) {
                            touchesTop = true;
                            break;
                        }
                    }
                    if (touchesTop) {
                        PocketNode pocketNode = new PocketNode(chunkKey, i);
                        visitedPockets.add(pocketNode);
                        queue.add(pocketNode);
                        newlyVisibleChunks.add(chunkKey);
                        skyCount++;
                    }
                }
            }
        }

        // Add Player pocket
        long playerChunkKey = getChunkKey(playerX, playerY, playerZ);
        boolean playerAdded = false;
        if (chunkPockets.containsKey(playerChunkKey)) {
            Pocket[] pockets = chunkPockets.get(playerChunkKey);
            int localX = playerX & 15;
            int localY = playerY & 15;
            int localZ = playerZ & 15;
            int bitIndex = localX | (localY << 4) | (localZ << 8);
            int wordIndex = bitIndex / 64;
            long bitMask = 1L << (bitIndex % 64);

            for (int i = 0; i < pockets.length; i++) {
                if ((pockets[i].blockMask[wordIndex] & bitMask) != 0) {
                    PocketNode pocketNode = new PocketNode(playerChunkKey, i);
                    if (visitedPockets.add(pocketNode)) {
                        queue.add(pocketNode);
                        newlyVisibleChunks.add(playerChunkKey);
                        playerAdded = true;
                    }
                }
            }
            newlyVisibleChunks.add(playerChunkKey); 
        }

        // BFS
        int bfsSteps = 0;
        while (!queue.isEmpty()) {
            PocketNode pocketNode = queue.poll();
            long chunkKey = pocketNode.chunkKey;
            int pocketIndex = pocketNode.index;
            bfsSteps++;

            Pocket[] pockets = chunkPockets.get(chunkKey);
            if (pockets == null || pocketIndex >= pockets.length) continue;
            Pocket p = pockets[pocketIndex];

            for (int face = 0; face < 6; face++) {
                boolean touchesFace = false;
                for (int j = 0; j < 4; j++) {
                    if (p.faceMasks[face * 4 + j] != 0) {
                        touchesFace = true;
                        break;
                    }
                }
                if (!touchesFace) continue;

                long neighborKey = getNeighborKey(chunkKey, face);
                Pocket[] neighborPockets = chunkPockets.get(neighborKey);
                if (neighborPockets == null) continue;

                // Any chunk touched by a visible air pocket MUST be rendered, 
                // because the player can see its boundary (either air or solid blocks).
                newlyVisibleChunks.add(neighborKey);

                int oppFace = getOppositeFace(face);

                for (int ni = 0; ni < neighborPockets.length; ni++) {
                    Pocket np = neighborPockets[ni];
                    
                    boolean intersects = false;
                    for (int j = 0; j < 4; j++) {
                        if ((p.faceMasks[face * 4 + j] & np.faceMasks[oppFace * 4 + j]) != 0) {
                            intersects = true;
                            break;
                        }
                    }

                    if (intersects) {
                        PocketNode nextNode = new PocketNode(neighborKey, ni);
                        if (visitedPockets.add(nextNode)) {
                            queue.add(nextNode);
                        }
                    }
                }
            }
        }

        // Diff with visibleChunks
        for (Long keyObj : chunkPockets.keySet()) {
            long chunkKey = keyObj.longValue();
            boolean isNowVisible = newlyVisibleChunks.contains(chunkKey);
            boolean wasVisible = visibleChunks.contains(chunkKey);

            if (isNowVisible && !wasVisible) {
                visibleChunks.add(chunkKey);
            } else if (!isNowVisible && wasVisible) {
                visibleChunks.remove(chunkKey);
            }
        }
    }

    public static Pocket[] computePockets(fd world, int originX, int originY, int originZ) {
        BitSet opaqueBlocks = new BitSet(4096);
        int opaqueCount = 0;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int blockId = world.a(originX + x, originY + y, originZ + z);
                    boolean opaque = true;
                    if (blockId == 0) {
                        opaque = false; // Air
                    } else {
                        switch (blockId) {
                            case 6: // Saplings
                            case 8: case 9: // Water
                            case 10: case 11: // Lava
                            case 18: // Leaves
                            case 20: // Glass
                            case 26: // Bed
                            case 27: case 28: case 66: // Rails
                            case 30: // Cobweb
                            case 31: case 32: // Tall Grass / Dead Bush
                            case 37: case 38: case 39: case 40: // Flowers / Mushrooms
                            case 50: // Torch
                            case 51: // Fire
                            case 52: // Mob Spawner
                            case 53: // Wooden Stairs
                            case 54: // Chest
                            case 55: // Redstone Wire
                            case 59: // Wheat
                            case 63: // Sign
                            case 64: // Wooden Door
                            case 65: // Ladder
                            case 67: // Cobblestone Stairs
                            case 68: // Wall Sign
                            case 69: // Lever
                            case 70: case 72: // Pressure Plates
                            case 71: // Iron Door
                            case 75: case 76: // Redstone Torches
                            case 77: // Stone Button
                            case 78: // Snow
                            case 83: // Sugar Cane
                            case 85: // Fence
                            case 90: // Portal
                            case 92: // Cake
                            case 93: case 94: // Redstone Repeater
                            case 96: // Trapdoor
                                opaque = false;
                                break;
                        }
                    }
                    if (opaque) {
                        opaqueBlocks.set(x | (y << 4) | (z << 8));
                        opaqueCount++;
                    }
                }
            }
        }

        if (opaqueCount == 4096) return new Pocket[0];

        List<Pocket> pockets = new ArrayList<Pocket>();
        BitSet visited = new BitSet(4096);
        int[] queue = new int[4096];

        for (int start = 0; start < 4096; start++) {
            if (!opaqueBlocks.get(start) && !visited.get(start)) {
                int head = 0, tail = 0;
                queue[tail++] = start;
                visited.set(start);

                Pocket pocket = new Pocket();

                while (head < tail) {
                    int node = queue[head++];
                    int x = node & 15;
                    int y = (node >> 4) & 15;
                    int z = (node >> 8) & 15;

                    int bitIndex = x | (y << 4) | (z << 8);
                    pocket.blockMask[bitIndex / 64] |= (1L << (bitIndex % 64));

                    if (x == 0) {
                        int faceBit = y + (z * 16);
                        pocket.faceMasks[DIR_X_NEG * 4 + (faceBit / 64)] |= (1L << (faceBit % 64));
                    }
                    if (x == 15) {
                        int faceBit = y + (z * 16);
                        pocket.faceMasks[DIR_X_POS * 4 + (faceBit / 64)] |= (1L << (faceBit % 64));
                    }
                    if (y == 0) {
                        int faceBit = x + (z * 16);
                        pocket.faceMasks[DIR_Y_NEG * 4 + (faceBit / 64)] |= (1L << (faceBit % 64));
                    }
                    if (y == 15) {
                        int faceBit = x + (z * 16);
                        pocket.faceMasks[DIR_Y_POS * 4 + (faceBit / 64)] |= (1L << (faceBit % 64));
                    }
                    if (z == 0) {
                        int faceBit = x + (y * 16);
                        pocket.faceMasks[DIR_Z_NEG * 4 + (faceBit / 64)] |= (1L << (faceBit % 64));
                    }
                    if (z == 15) {
                        int faceBit = x + (y * 16);
                        pocket.faceMasks[DIR_Z_POS * 4 + (faceBit / 64)] |= (1L << (faceBit % 64));
                    }

                    if (x > 0) {
                        int next = (x - 1) | (y << 4) | (z << 8);
                        if (!opaqueBlocks.get(next) && !visited.get(next)) { visited.set(next); queue[tail++] = next; }
                    }
                    if (x < 15) {
                        int next = (x + 1) | (y << 4) | (z << 8);
                        if (!opaqueBlocks.get(next) && !visited.get(next)) { visited.set(next); queue[tail++] = next; }
                    }
                    if (y > 0) {
                        int next = x | ((y - 1) << 4) | (z << 8);
                        if (!opaqueBlocks.get(next) && !visited.get(next)) { visited.set(next); queue[tail++] = next; }
                    }
                    if (y < 15) {
                        int next = x | ((y + 1) << 4) | (z << 8);
                        if (!opaqueBlocks.get(next) && !visited.get(next)) { visited.set(next); queue[tail++] = next; }
                    }
                    if (z > 0) {
                        int next = x | (y << 4) | ((z - 1) << 8);
                        if (!opaqueBlocks.get(next) && !visited.get(next)) { visited.set(next); queue[tail++] = next; }
                    }
                    if (z < 15) {
                        int next = x | (y << 4) | ((z + 1) << 8);
                        if (!opaqueBlocks.get(next) && !visited.get(next)) { visited.set(next); queue[tail++] = next; }
                    }
                }

                pockets.add(pocket);
            }
        }
        
        Pocket[] result = new Pocket[pockets.size()];
        for (int i = 0; i < pockets.size(); i++) {
            result[i] = pockets.get(i);
        }
        return result;
    }
}
