package mcrtx.bridge;

import java.nio.ByteBuffer;

public final class MinecraftRenderHooks {
    private static final int MAX_CAPTURED_BLOCKS_PER_CHUNK = 4096;
    public static final int REMIX_UI_STATE_NONE = RemixBridgeNative.REMIX_UI_STATE_NONE;
    public static final int REMIX_UI_STATE_BASIC = RemixBridgeNative.REMIX_UI_STATE_BASIC;
    public static final int REMIX_UI_STATE_ADVANCED = RemixBridgeNative.REMIX_UI_STATE_ADVANCED;
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

    private static volatile boolean initialized;
    private static boolean chunkBuildCaptureActive;
    private static int activeChunkRenderPass;
    private static int capturedChunkBlocks;
    private static String lastError = "";
    private static String lastReportedMessage = "";

    private MinecraftRenderHooks() {
    }

    public static synchronized boolean initializeForCurrentDisplay(int width, int height) {
        long hwnd = LwjglWindowHandleResolver.resolveCurrentHwnd();
        if (hwnd == 0L) {
            lastError = "Failed to resolve LWJGL window handle";
            report(lastError);
            return false;
        }
        return initialize(hwnd, width, height);
    }

    public static synchronized boolean reinitializeForCurrentDisplay(int width, int height) {
        shutdown();
        return initializeForCurrentDisplay(width, height);
    }

    public static synchronized boolean initialize(long hwnd, int width, int height) {
        if (initialized) {
            return true;
        }
        if (!RemixBridgeNative.isAvailable()) {
            lastError = RemixBridgeNative.loadError();
            report("JNI bridge unavailable: " + lastError);
            return false;
        }
        initialized = RemixBridgeNative.nInitialize(hwnd, width, height);
        if (!initialized) {
            lastError = RemixBridgeNative.nGetLastError();
            report("Renderer initialization failed: " + lastError);
        } else {
            lastError = "";
            report("Renderer initialized for hwnd=0x" + Long.toHexString(hwnd));
        }
        return initialized;
    }

    public static synchronized void shutdown() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nShutdown();
        initialized = false;
        report("Renderer shutdown complete");
    }

    public static synchronized void resize(int width, int height) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nResize(width, height);
    }

    public static synchronized int getUiState() {
        if (!initialized) {
            return REMIX_UI_STATE_NONE;
        }
        return RemixBridgeNative.nGetUiState();
    }

    public static synchronized boolean setUiState(int state) {
        if (!initialized) {
            return false;
        }
        boolean result = RemixBridgeNative.nSetUiState(state);
        if (!result) {
            lastError = RemixBridgeNative.nGetLastError();
        } else {
            lastError = "";
        }
        return result;
    }

    public static synchronized void updateCamera(CameraPose cameraPose) {
        if (!initialized || cameraPose == null) {
            return;
        }
        RemixBridgeNative.nUpdateCamera(
                cameraPose.px,
                cameraPose.py,
                cameraPose.pz,
                cameraPose.fx,
                cameraPose.fy,
                cameraPose.fz,
                cameraPose.ux,
                cameraPose.uy,
                cameraPose.uz,
                cameraPose.rx,
                cameraPose.ry,
                cameraPose.rz,
                cameraPose.fovYDegrees,
                cameraPose.aspect,
                cameraPose.nearPlane,
                cameraPose.farPlane);
    }

    public static synchronized void updateCamera(
            float px,
            float py,
            float pz,
            float fx,
            float fy,
            float fz,
            float fovYDegrees,
            float aspect,
            float nearPlane,
            float farPlane) {
        // The patched client still invokes this legacy path from its original
        // camera update site, but that path only knows the player-eye pose and
        // will overwrite the authoritative GL-captured camera used for detached
        // third-person views. Keep the ABI for the patched bytecode, but let
        // RemixCameraState drive camera submission instead.
    }

    public static synchronized void updateCloudLayer(
            boolean fancy,
            float cameraX,
            float cameraY,
            float cameraZ,
            float cloudHeight,
            float cloudScroll,
            float celestialAngle,
            float colorR,
            float colorG,
            float colorB) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nUpdateCloudLayer(
                fancy,
                cameraX,
                cameraY,
                cameraZ,
                cloudHeight,
                cloudScroll,
                celestialAngle,
                colorR,
                colorG,
                colorB);
    }

    public static synchronized void updateAtmosphereState(float celestialAngle, boolean forceDarkAtmosphere) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nUpdateAtmosphereState(celestialAngle, forceDarkAtmosphere);
    }

    public static synchronized void clearCloudLayer() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nClearCloudLayer();
    }

    public static synchronized void beginDynamicEntityFrame() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nBeginDynamicEntityFrame();
    }

    public static synchronized void beginDynamicEntity(int entityId) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nBeginDynamicEntity(entityId);
    }

    public static synchronized void setDynamicEntityTexture(String texturePath) {
        if (!initialized || texturePath == null || texturePath.isEmpty()) {
            return;
        }
        RemixBridgeNative.nSetDynamicEntityTexture(texturePath);
    }

    public static synchronized void setDynamicEntityBoneTransform(
            int boneIndex,
            float m00,
            float m01,
            float m02,
            float m03,
            float m10,
            float m11,
            float m12,
            float m13,
            float m20,
            float m21,
            float m22,
            float m23) {
        if (!initialized || boneIndex < 0) {
            return;
        }
        RemixBridgeNative.nSetDynamicEntityBoneTransform(
                boneIndex,
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23);
    }

    public static synchronized void captureDynamicEntityQuad(
            float x0,
            float y0,
            float z0,
            float u0,
            float v0,
            float x1,
            float y1,
            float z1,
            float u1,
            float v1,
            float x2,
            float y2,
            float z2,
            float u2,
            float v2,
            float x3,
            float y3,
            float z3,
            float u3,
            float v3,
            int colorRgba,
            int boneIndex) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nCaptureDynamicEntityQuad(
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                colorRgba,
                boneIndex);
    }

    public static synchronized void endDynamicEntity() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nEndDynamicEntity();
    }

    public static synchronized void beginDestroyOverlayFrame() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nBeginDestroyOverlayFrame();
    }

    public static synchronized void captureDestroyOverlay(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType,
            int destroyStage) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nCaptureDestroyOverlay(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                destroyStage);
    }

    public static synchronized void beginParticleFrame() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nBeginParticleFrame();
    }

    public static synchronized void captureParticleQuad(
            float x0,
            float y0,
            float z0,
            float u0,
            float v0,
            float x1,
            float y1,
            float z1,
            float u1,
            float v1,
            float x2,
            float y2,
            float z2,
            float u2,
            float v2,
            float x3,
            float y3,
            float z3,
            float u3,
            float v3,
            int colorRgba,
            int textureKind) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nCaptureParticleQuad(
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                colorRgba,
                textureKind);
    }

    public static synchronized void clearWorldScene() {
        if (!initialized) {
            chunkBuildCaptureActive = false;
            activeChunkRenderPass = 0;
            capturedChunkBlocks = 0;
            return;
        }

        RemixBridgeNative.nClearWorldScene();
        chunkBuildCaptureActive = false;
        activeChunkRenderPass = 0;
        capturedChunkBlocks = 0;
    }

    public static synchronized boolean drawScreenOverlay(
            ByteBuffer pixelData,
            int width,
            int height,
            int format,
            float opacity) {
        if (!initialized || pixelData == null) {
            return false;
        }
        return RemixBridgeNative.nDrawScreenOverlay(pixelData, width, height, format, opacity);
    }

    public static synchronized boolean clearScreenOverlay() {
        if (!initialized) {
            return true;
        }
        return RemixBridgeNative.nClearScreenOverlay();
    }

    public static synchronized boolean present() {
        return initialized && RemixBridgeNative.nPresent();
    }

    public static boolean beginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        if (!initialized) {
            chunkBuildCaptureActive = false;
            activeChunkRenderPass = 0;
            capturedChunkBlocks = 0;
            return false;
        }

        boolean active = RemixBridgeNative.nBeginChunkBuild(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
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
        if (!initialized || !chunkBuildCaptureActive) {
            return;
        }
        if (!shouldCaptureBlock(blockId, renderType) || capturedChunkBlocks >= MAX_CAPTURED_BLOCKS_PER_CHUNK) {
            return;
        }
        capturedChunkBlocks += 1;
        RemixBridgeNative.nCaptureBlock(
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

    public static void endChunkBuild(boolean emittedGeometry) {
        if (!initialized || !chunkBuildCaptureActive) {
            return;
        }
        RemixBridgeNative.nEndChunkBuild(capturedChunkBlocks > 0);
        chunkBuildCaptureActive = false;
        activeChunkRenderPass = 0;
        capturedChunkBlocks = 0;
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
                    || renderType == 11)
                    || (renderType == 4 && (blockId == LAVA_STILL_BLOCK_ID || blockId == LAVA_FLOWING_BLOCK_ID));
        }
        if (activeChunkRenderPass == 1) {
            return (renderType == 4 && (blockId == WATER_STILL_BLOCK_ID || blockId == WATER_FLOWING_BLOCK_ID))
                    || (renderType == 0 && (blockId == ICE_BLOCK_ID || blockId == NETHER_PORTAL_BLOCK_ID));
        }
        return false;
    }

    public static synchronized boolean isInitialized() {
        return initialized;
    }

    public static synchronized String lastError() {
        if (!lastError.isEmpty()) {
            return lastError;
        }
        if (!RemixBridgeNative.isAvailable()) {
            return RemixBridgeNative.loadError();
        }
        return RemixBridgeNative.nGetLastError();
    }

    private static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private static void report(String message) {
        if (message == null || message.isEmpty() || message.equals(lastReportedMessage)) {
            return;
        }
        lastReportedMessage = message;
        System.out.println("[mcrtx] " + message);
    }
}