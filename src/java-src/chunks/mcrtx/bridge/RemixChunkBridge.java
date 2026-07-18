package mcrtx.bridge;

public final class RemixChunkBridge {
    private static final int MAX_CAPTURED_BLOCKS_PER_CHUNK = 4096;
    private static final int ICE_BLOCK_ID = 79;
    private static final int WATER_STILL_BLOCK_ID = 8;
    private static final int WATER_FLOWING_BLOCK_ID = 9;
    private static final int LAVA_STILL_BLOCK_ID = 10;
    private static final int LAVA_FLOWING_BLOCK_ID = 11;
    private static final int NETHER_PORTAL_BLOCK_ID = 90;
    private static final int DOOR_BLOCK_RENDER_TYPE = 7;
    private static final int CROP_BLOCK_RENDER_TYPE = 6;
    private static final int LEVER_OR_BUTTON_BLOCK_RENDER_TYPE = 12;
    private static final int CACTUS_BLOCK_RENDER_TYPE = 13;
    private static final int STAIRS_BLOCK_RENDER_TYPE = 10;
    private static final int BED_BLOCK_RENDER_TYPE = 14;
    private static final int REPEATER_BLOCK_RENDER_TYPE = 15;
    private static final int PISTON_BASE_BLOCK_RENDER_TYPE = 16;
    private static final int PISTON_HEAD_BLOCK_RENDER_TYPE = 17;

    private static boolean chunkBuildCaptureActive;
    private static int activeChunkRenderPass;
    private static int capturedChunkBlocks;

    private RemixChunkBridge() {
    }

    public static synchronized void resetCaptureState() {
        chunkBuildCaptureActive = false;
        activeChunkRenderPass = 0;
        capturedChunkBlocks = 0;
    }

    public static synchronized void unloadChunkSection(int originX, int originY, int originZ) {
        if (RemixLifecycleBridge.isInitialized()) {
            nUnloadChunkSection(originX, originY, originZ);
        }
    }

    public static synchronized void setChunkSectionHidden(int originX, int originY, int originZ, boolean hidden) {
        if (RemixLifecycleBridge.isInitialized()) {
            nSetChunkSectionHidden(originX, originY, originZ, hidden);
        }
    }

    public static boolean beginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        return beginChunkBuild(
                originX,
                originY,
                originZ,
                sizeX,
                sizeY,
                sizeZ,
                renderPass,
                originX,
                originY,
                originZ,
                originX + sizeX - 1,
                originY + sizeY - 1,
                originZ + sizeZ - 1);
    }

    public static boolean beginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass,
            int dirtyMinX,
            int dirtyMinY,
            int dirtyMinZ,
            int dirtyMaxX,
            int dirtyMaxY,
            int dirtyMaxZ) {
        if (!RemixLifecycleBridge.isInitialized()) {
            resetCaptureState();
            return false;
        }

        boolean active = nBeginChunkBuild(
                originX,
                originY,
                originZ,
                sizeX,
                sizeY,
                sizeZ,
                dirtyMinX,
                dirtyMinY,
                dirtyMinZ,
                dirtyMaxX,
                dirtyMaxY,
                dirtyMaxZ,
                renderPass);
        chunkBuildCaptureActive = active;
        activeChunkRenderPass = active ? renderPass : 0;
        capturedChunkBlocks = 0;
        return active;
    }

    public static void captureBlock(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType,
            int texture0,
            int texture1,
            int texture2,
            int texture3,
            int texture4,
            int texture5,
            float boundsMinX,
            float boundsMinY,
            float boundsMinZ,
            float boundsMaxX,
            float boundsMaxY,
            float boundsMaxZ,
            int blockColorRgb,
            int liquidVisibilityMask,
            float liquidHeight0,
            float liquidHeight1,
            float liquidHeight2,
            float liquidHeight3,
            float liquidFlowAngle) {
        if (!RemixLifecycleBridge.isInitialized() || !chunkBuildCaptureActive) {
            return;
        }
        if (!shouldCaptureBlock(blockId, renderType) || capturedChunkBlocks >= MAX_CAPTURED_BLOCKS_PER_CHUNK) {
            return;
        }
        capturedChunkBlocks += 1;
        nCaptureBlock(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                texture0,
                texture1,
                texture2,
                texture3,
                texture4,
                texture5,
                boundsMinX,
                boundsMinY,
                boundsMinZ,
                boundsMaxX,
                boundsMaxY,
                boundsMaxZ,
                blockColorRgb,
                liquidVisibilityMask,
                liquidHeight0,
                liquidHeight1,
                liquidHeight2,
                liquidHeight3,
                liquidFlowAngle);
    }

    public static void endChunkBuild(boolean emittedGeometry, boolean deferNeighborRefresh) {
        endChunkBuild(emittedGeometry, deferNeighborRefresh, true);
    }

    public static void endChunkBuild(
            boolean emittedGeometry,
            boolean deferNeighborRefresh,
            boolean allowNeighborRefresh) {
        if (!RemixLifecycleBridge.isInitialized() || !chunkBuildCaptureActive) {
            return;
        }
        nEndChunkBuild(capturedChunkBlocks > 0, deferNeighborRefresh, allowNeighborRefresh);
        resetCaptureState();
    }

    public static void flushChunkNeighborRefreshes() {
        if (RemixLifecycleBridge.isInitialized()) {
            nFlushChunkNeighborRefreshes();
        }
    }

    public static synchronized boolean isChunkBuildCaptureActive() {
        return chunkBuildCaptureActive;
    }

    private static boolean shouldCaptureBlock(int blockId, int renderType) {
        if (blockId <= 0) {
            return false;
        }
        if (activeChunkRenderPass == 0) {
            return (renderType == 0
                    || renderType == 1
                    || renderType == 2
                    || renderType == 3
                    || renderType == 5
                    || renderType == CROP_BLOCK_RENDER_TYPE
                    || renderType == DOOR_BLOCK_RENDER_TYPE
                    || renderType == 8
                    || renderType == 9
                    || renderType == STAIRS_BLOCK_RENDER_TYPE
                    || renderType == LEVER_OR_BUTTON_BLOCK_RENDER_TYPE
                    || renderType == CACTUS_BLOCK_RENDER_TYPE
                    || renderType == BED_BLOCK_RENDER_TYPE
                    || renderType == REPEATER_BLOCK_RENDER_TYPE
                    || renderType == PISTON_BASE_BLOCK_RENDER_TYPE
                    || renderType == PISTON_HEAD_BLOCK_RENDER_TYPE
                    || renderType == 11)
                    || (renderType == 4 && (blockId == LAVA_STILL_BLOCK_ID || blockId == LAVA_FLOWING_BLOCK_ID));
        }
        if (activeChunkRenderPass == 1) {
            return (renderType == 4 && (blockId == WATER_STILL_BLOCK_ID || blockId == WATER_FLOWING_BLOCK_ID))
                    || (renderType == 0 && (blockId == ICE_BLOCK_ID || blockId == NETHER_PORTAL_BLOCK_ID));
        }
        return false;
    }

    private static native void nUnloadChunkSection(int originX, int originY, int originZ);
    private static native void nSetChunkSectionHidden(int originX, int originY, int originZ, boolean hidden);
    private static native boolean nBeginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int dirtyMinX,
            int dirtyMinY,
            int dirtyMinZ,
            int dirtyMaxX,
            int dirtyMaxY,
            int dirtyMaxZ,
            int renderPass);
    private static native void nCaptureBlock(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType,
            int texture0,
            int texture1,
            int texture2,
            int texture3,
            int texture4,
            int texture5,
            float boundsMinX,
            float boundsMinY,
            float boundsMinZ,
            float boundsMaxX,
            float boundsMaxY,
            float boundsMaxZ,
            int blockColorRgb,
            int liquidVisibilityMask,
            float liquidHeight0,
            float liquidHeight1,
            float liquidHeight2,
            float liquidHeight3,
            float liquidFlowAngle);
    private static native void nEndChunkBuild(
            boolean emittedGeometry,
            boolean deferNeighborRefresh,
            boolean allowNeighborRefresh);
    private static native void nFlushChunkNeighborRefreshes();
}
