package mcrtx.bridge;

import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

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
    private static final int PISTON_BASE_BLOCK_RENDER_TYPE = 16;
    private static final int PISTON_HEAD_BLOCK_RENDER_TYPE = 17;

    private static volatile boolean initialized;
    private static volatile boolean remixUiInputActive;
    private static Minecraft currentMinecraft;
    private static boolean chunkBuildCaptureActive;
    private static int activeChunkRenderPass;
    private static int capturedChunkBlocks;
    private static String lastError = "";
    private static String lastReportedMessage = "";

    private MinecraftRenderHooks() {
    }

    public static synchronized boolean initializeForCurrentDisplay(int width, int height) {
        long hwnd = MinecraftPlatformRuntime.current().resolveCurrentWindowHandle();
        if (hwnd == 0L) {
            lastError = "Failed to resolve the active platform window handle for backend '"
                    + MinecraftPlatformRuntime.currentBackendSelection()
                    + "'";
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
            remixUiInputActive = false;
            currentMinecraft = null;
            return;
        }
        RemixBridgeNative.nShutdown();
        initialized = false;
        remixUiInputActive = false;
        currentMinecraft = null;
        report("Renderer shutdown complete");
    }

    public static synchronized void rememberMinecraftInstance(Minecraft minecraft) {
        if (minecraft != null) {
            currentMinecraft = minecraft;
        }
    }

    public static synchronized Minecraft getRememberedMinecraft() {
        return currentMinecraft;
    }

    public static synchronized void requestShutdown() {
        if (currentMinecraft != null) {
            currentMinecraft.f();
        }
    }

    public static synchronized boolean restoreIngameFocusIfNeeded() {
        if (currentMinecraft == null) {
            return false;
        }

        if (RemixBridgeNative.isAvailable()) {
            long hwnd = MinecraftPlatformRuntime.current().resolveCurrentWindowHandle();
            if (hwnd != 0L) {
                RemixBridgeNative.nFocusWindow(hwnd);
            }
        }
        currentMinecraft.g();
        return currentMinecraft.N;
    }

    public static synchronized void setRemixUiInputActive(boolean active) {
        remixUiInputActive = active;
    }

    public static boolean isRemixUiInputActive() {
        return remixUiInputActive;
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

    public static synchronized boolean hasNativeWindowFocus() {
        if (!initialized) {
            return false;
        }
        return RemixBridgeNative.nHasWindowFocus();
    }

    public static synchronized boolean isNativeVirtualKeyDown(int virtualKey) {
        if (!initialized) {
            return false;
        }
        return RemixBridgeNative.nIsVirtualKeyDown(virtualKey);
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

    public static synchronized void updateFogState(
            int fogMode,
            float colorR,
            float colorG,
            float colorB,
            float fogScale,
            float fogEnd,
            float fogDensity) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nUpdateFogState(fogMode, colorR, colorG, colorB, fogScale, fogEnd, fogDensity);
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

    public static synchronized void beginDynamicEntity(int entityId, int hurtStage, int creeperFuseStage) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nBeginDynamicEntity(entityId, hurtStage, creeperFuseStage);
    }

    public static synchronized void setDynamicEntityTexture(String texturePath) {
        if (!initialized || texturePath == null || texturePath.isEmpty()) {
            return;
        }
        RemixBridgeNative.nSetDynamicEntityTexture(texturePath);
    }

    public static synchronized void setFirstPersonHeldItem(int itemId) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetFirstPersonHeldItem(itemId);
    }

    public static synchronized void setEntityHeldTorch(int entityId, float worldX, float worldY, float worldZ, int itemId) {
        if (!initialized || entityId < 0) {
            return;
        }
        RemixBridgeNative.nSetEntityHeldTorch(entityId, worldX, worldY, worldZ, itemId);
    }

    public static synchronized void setPlayerShadowsEnabled(boolean enabled) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetPlayerShadowsEnabled(enabled);
    }

    public static synchronized void setHeldTorchLightsEnabled(boolean enabled) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetHeldTorchLightsEnabled(enabled);
    }

    public static synchronized void setDynamicEntityRenderingEnabled(boolean enabled) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetDynamicEntityRenderingEnabled(enabled);
    }

    public static synchronized void setBlockOutlineEnabled(boolean enabled) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetBlockOutlineEnabled(enabled);
    }

    public static synchronized void setBlockOutlineStyle(int style) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetBlockOutlineStyle(style);
    }

    public static synchronized void setBlockOutlineEmissiveIntensity(float intensity) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetBlockOutlineEmissiveIntensity(intensity);
    }

    public static synchronized void setDisplacementFactor(float factor) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetDisplacementFactor(factor);
    }

    public static synchronized void setSubsurfaceMeasurementDistance(float distance) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetSubsurfaceMeasurementDistance(distance);
    }

    public static synchronized void setSubsurfaceRadiusScale(float scale) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetSubsurfaceRadiusScale(scale);
    }

    public static synchronized void setSubsurfaceMaxSampleRadius(float radius) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetSubsurfaceMaxSampleRadius(radius);
    }

    public static synchronized void setSubsurfaceVolumetricAnisotropy(float anisotropy) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetSubsurfaceVolumetricAnisotropy(anisotropy);
    }

    public static synchronized void setSubsurfaceDiffusionProfileEnabled(boolean enabled) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetSubsurfaceDiffusionProfileEnabled(enabled);
    }

    public static synchronized void setViewModelFovDegrees(int fovDegrees) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetViewModelFovDegrees((float) fovDegrees);
    }

    public static synchronized void setRtQuality(int rtQuality) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetRtQuality(rtQuality);
    }

    public static synchronized void setUpscalerConfig(int upscalerType, int dlssPreset, int xessPreset, int taauPreset, boolean rayReconstructionEnabled) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetUpscalerConfig(upscalerType, dlssPreset, xessPreset, taauPreset, rayReconstructionEnabled);
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
            captureDynamicEntityQuad(
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                colorRgba,
                GL11.glIsEnabled(GL11.GL_BLEND),
                boneIndex);
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
                boolean blendEnabled,
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
                blendEnabled,
                boneIndex);
    }

    public static synchronized void captureDynamicEntityQuadBatch(
            float[] vertices,
            int quadCount,
            int colorRgba,
            int boneIndex) {
        if (!initialized || vertices == null || quadCount <= 0) {
            return;
        }
        RemixBridgeNative.nCaptureDynamicEntityQuadBatch(
                vertices,
                quadCount,
                colorRgba,
                GL11.glIsEnabled(GL11.GL_BLEND),
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

    public static synchronized void beginBlockOutlineFrame() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nBeginBlockOutlineFrame();
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

    public static synchronized void captureBlockOutline(int blockX, int blockY, int blockZ) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nCaptureBlockOutline(blockX, blockY, blockZ);
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

    public static synchronized void unloadChunkSection(int originX, int originY, int originZ) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nUnloadChunkSection(originX, originY, originZ);
    }

    public static synchronized void setChunkSectionHidden(int originX, int originY, int originZ, boolean hidden) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetChunkSectionHidden(originX, originY, originZ, hidden);
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

    public static synchronized boolean registerUiTexture(
            ByteBuffer pixelData,
            long id,
            int width,
            int height,
            int format) {
        if (!initialized || pixelData == null) {
            return false;
        }
        return RemixBridgeNative.nRegisterUiTexture(pixelData, id, width, height, format);
    }

    public static synchronized boolean freeUiTexture(long id) {
        if (!initialized) {
            return true;
        }
        return RemixBridgeNative.nFreeUiTexture(id);
    }

    public static synchronized boolean submitUiDrawList(
            float[] vertexXYZUV,
            int[] vertexColor,
            int vertexCount,
            long[] cmdTextureIds,
            int[] cmdQuadCounts,
            int[] cmdFlags,
            int cmdCount,
            int displayWidth,
            int displayHeight) {
        if (!initialized) {
            return false;
        }
        return RemixBridgeNative.nSubmitUiDrawList(
                vertexXYZUV, vertexColor, vertexCount,
                cmdTextureIds, cmdQuadCounts, cmdFlags, cmdCount,
                displayWidth, displayHeight);
    }

    public static synchronized void setScreenTint(float r, float g, float b, float a) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nSetScreenTint(r, g, b, a);
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
        if (!initialized) {
            chunkBuildCaptureActive = false;
            activeChunkRenderPass = 0;
            capturedChunkBlocks = 0;
            return false;
        }

        boolean active = RemixBridgeNative.nBeginChunkBuild(
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

    public static void endChunkBuild(boolean emittedGeometry, boolean deferNeighborRefresh) {
        endChunkBuild(emittedGeometry, deferNeighborRefresh, true);
    }

    public static void endChunkBuild(boolean emittedGeometry, boolean deferNeighborRefresh, boolean allowNeighborRefresh) {
        if (!initialized || !chunkBuildCaptureActive) {
            return;
        }
        RemixBridgeNative.nEndChunkBuild(capturedChunkBlocks > 0, deferNeighborRefresh, allowNeighborRefresh);
        chunkBuildCaptureActive = false;
        activeChunkRenderPass = 0;
        capturedChunkBlocks = 0;
    }

    public static void flushChunkNeighborRefreshes() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nFlushChunkNeighborRefreshes();
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