import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.UiOverlayCapture;
import net.minecraft.client.Minecraft;
import java.util.Locale;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

/**
 * Thin dispatcher that receives every bytecode-patched callback from the Beta
 * client and routes it to the corresponding capture subsystem. The set of
 * public static methods on this class is the ABI the patcher targets and must
 * not change without updating {@code ClientPatchTool} in lockstep.
 */
public final class MinecraftRemixHooks {
    private static final int DEFAULT_REMIX_UI_STATE = MinecraftRenderHooks.REMIX_UI_STATE_ADVANCED;
    private static final int PERF_LOG_INTERVAL_FRAMES = 60;
    private static final boolean STANDALONE_WINDOW_MODE = detectStandaloneWindowMode();

    private static boolean loggedDisplayCreate;
    private static boolean loggedDisplayReset;
    private static boolean loggedPresent;
    private static boolean remixUiOpen;
    private static boolean remixUiHotkeyHeld;
    private static int preferredRemixUiState = DEFAULT_REMIX_UI_STATE;
    private static long perfFrameCount;
    private static long perfTotalFrameNanos;
    private static long perfMaxFrameNanos;
    private static long perfTotalRenderMethodNanos;
    private static long perfMaxRenderMethodNanos;
    private static long perfTotalPrePresentNanos;
    private static long perfMaxPrePresentNanos;
    private static long perfTotalFlushNanos;
    private static long perfMaxFlushNanos;
    private static long perfTotalPresentNanos;
    private static long perfMaxPresentNanos;
    private static long perfTotalPostPresentNanos;
    private static long perfMaxPostPresentNanos;
    private static long perfTotalQueueDepthBeforeFlush;
    private static int perfMaxQueueDepthBeforeFlush;
    private static long perfTotalQueueDepthAfterFlush;
    private static int perfMaxQueueDepthAfterFlush;
    private static long perfTotalSectionsRecaptured;
    private static int perfMaxSectionsRecaptured;
    private static long activeRenderMethodStartNanos;

    static {
        System.out.println("[mcrtx] MinecraftRemixHooks loaded");
    }

    private MinecraftRemixHooks() {
    }

    public static void onDisplayCreated(int width, int height) {
        if (!loggedDisplayCreate) {
            loggedDisplayCreate = true;
            System.out.println("[mcrtx] onDisplayCreated width=" + width + " height=" + height);
        }
        resetRemixUiTracking();
        resetPerfTracking();
        UiOverlayCapture.reset();
        MinecraftRenderHooks.initializeForCurrentDisplay(width, height);
    }

    public static void onShutdown() {
        System.out.println("[mcrtx] onShutdown");
        resetRemixUiTracking();
        resetPerfTracking();
        UiOverlayCapture.reset();
        MinecraftRenderHooks.shutdown();
    }

    public static void onDisplayReset(int width, int height) {
        if (!loggedDisplayReset) {
            loggedDisplayReset = true;
            System.out.println("[mcrtx] onDisplayReset width=" + width + " height=" + height);
        }
        resetRemixUiTracking();
        resetPerfTracking();
        UiOverlayCapture.reset();
        MinecraftRenderHooks.reinitializeForCurrentDisplay(width, height);
    }

    public static void onResize(int width, int height) {
        MinecraftRenderHooks.resize(width, height);
    }

    public static void onCamera(ls entity, float partialTicks, int width, int height, float farPlane, boolean thirdPersonActive) {
        RemixCameraState.onCamera(entity, partialTicks, width, height, farPlane, thirdPersonActive);
    }

    public static void onFrameViewCaptured() {
        RemixCameraState.captureFrameView();
    }

    public static void onPresent() {
        long frameStartNanos = System.nanoTime();
        long renderMethodStartNanos = activeRenderMethodStartNanos;
        RemixChunkCapture.flushPendingChunkRecaptures();
        long flushEndNanos = System.nanoTime();
        if (!loggedPresent) {
            loggedPresent = true;
            System.out.println("[mcrtx] onPresent");
        }
        MinecraftRenderHooks.present();
        long presentEndNanos = System.nanoTime();
        RemixDynamicEntityCapture.onFramePresented();
        RemixCameraState.onFramePresented();
        long frameEndNanos = System.nanoTime();
        long prePresentNanos = renderMethodStartNanos > 0L ? Math.max(0L, frameStartNanos - renderMethodStartNanos) : 0L;
        long renderMethodNanos = renderMethodStartNanos > 0L ? Math.max(0L, frameEndNanos - renderMethodStartNanos) : 0L;
        activeRenderMethodStartNanos = 0L;
        recordPresentPerf(
                frameEndNanos - frameStartNanos,
            renderMethodNanos,
            prePresentNanos,
                flushEndNanos - frameStartNanos,
                presentEndNanos - flushEndNanos,
                frameEndNanos - presentEndNanos,
                RemixChunkCapture.lastFlushDurationNanos(),
                RemixChunkCapture.lastPendingQueueDepthBeforeFlush(),
                RemixChunkCapture.lastPendingQueueDepthAfterFlush(),
                RemixChunkCapture.lastSectionsRecaptured());
    }

    public static void onUiRenderBegin(int width, int height) {
        if (STANDALONE_WINDOW_MODE) {
            return;
        }
        UiOverlayCapture.begin(width, height);
    }

    public static void onUiRenderEnd() {
        if (STANDALONE_WINDOW_MODE) {
            return;
        }
        UiOverlayCapture.end();
    }

    public static void onRemixUiTick(net.minecraft.client.Minecraft minecraft) {
        if (STANDALONE_WINDOW_MODE) {
            return;
        }
        syncRemixUiInput(minecraft, true);
    }

    public static boolean isWindowInteractionActive() {
        return Display.isActive() || (!STANDALONE_WINDOW_MODE && remixUiOpen);
    }

    public static void onWorldChanged(fd world) {
        RemixChunkCapture.onWorldChanged(world);
    }

    public static void onCloudRender(net.minecraft.client.Minecraft minecraft, fd world, int cloudTick, float partialTicks, boolean fancy) {
        RemixCloudCapture.onCloudRender(minecraft, world, cloudTick, partialTicks, fancy);
    }

    public static void onFogState(ls entity, boolean thickFog, int renderLayer, boolean forceStartAtCamera, float viewDistance, float colorR, float colorG, float colorB) {
        RemixFogCapture.onFogState(entity, thickFog, renderLayer, forceStartAtCamera, viewDistance, colorR, colorG, colorB);
    }

    public static void onLivingEntityFrameBegin() {
        RemixDynamicEntityCapture.onLivingEntityFrameBegin();
    }

    public static void onDestroyOverlayRender(int blockX, int blockY, int blockZ, float destroyProgress) {
        RemixDestroyOverlayCapture.onDestroyOverlayRender(blockX, blockY, blockZ, destroyProgress);
    }

    public static void onParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        RemixParticleCapture.onParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
    }

    public static void onLivingEntityRenderStart(sn entity) {
        RemixDynamicEntityCapture.onLivingEntityRenderStart(entity);
    }

    public static void onLivingEntityRenderEnd() {
        RemixDynamicEntityCapture.onLivingEntityRenderEnd();
    }

    public static void onSignRenderStart(yk sign) {
        RemixDynamicEntityCapture.onSignRenderStart(sign);
    }

    public static void onSignRenderEnd() {
        RemixDynamicEntityCapture.onSignRenderEnd();
    }

    public static void onPaintingRender(qv painting) {
        RemixDynamicEntityCapture.onPaintingRender(painting);
    }

    public static void onSignTextRender(String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        RemixDynamicEntityCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
    }

    public static void onFirstPersonRenderStart() {
        RemixDynamicEntityCapture.onFirstPersonRenderStart();
    }

    public static void onFirstPersonShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        RemixDynamicEntityCapture.onFirstPersonShadowPlayerRender(minecraft, partialTicks);
    }

    public static void onFirstPersonRenderEnd() {
        RemixDynamicEntityCapture.onFirstPersonRenderEnd();
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        RemixDynamicEntityCapture.onFirstPersonItemRender(itemStack);
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        RemixDynamicEntityCapture.onEntityTextureBind(primaryTexture, fallbackTexture);
    }

    public static void onModelPartRender(tz[] polygons, float scale) {
        RemixDynamicEntityCapture.onModelPartRender(polygons, scale);
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        RemixDynamicEntityCapture.onFirstPersonTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
        RemixParticleCapture.onTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
    }

    public static void onWeatherTextureBind(String texturePath) {
        RemixParticleCapture.onWeatherTextureBind(texturePath);
    }

    public static void onWeatherRenderEnd() {
        RemixParticleCapture.onWeatherRenderEnd();
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        return RemixChunkCapture.onChunkBuildBegin(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        RemixChunkCapture.onChunkBlock(blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        RemixChunkCapture.onChunkBuildEnd(emittedGeometry);
    }

    private static void resetRemixUiTracking() {
        remixUiOpen = false;
        remixUiHotkeyHeld = false;
        preferredRemixUiState = DEFAULT_REMIX_UI_STATE;
    }

    private static void resetPerfTracking() {
        perfFrameCount = 0L;
        perfTotalFrameNanos = 0L;
        perfMaxFrameNanos = 0L;
        perfTotalRenderMethodNanos = 0L;
        perfMaxRenderMethodNanos = 0L;
        perfTotalPrePresentNanos = 0L;
        perfMaxPrePresentNanos = 0L;
        perfTotalFlushNanos = 0L;
        perfMaxFlushNanos = 0L;
        perfTotalPresentNanos = 0L;
        perfMaxPresentNanos = 0L;
        perfTotalPostPresentNanos = 0L;
        perfMaxPostPresentNanos = 0L;
        perfTotalQueueDepthBeforeFlush = 0L;
        perfMaxQueueDepthBeforeFlush = 0;
        perfTotalQueueDepthAfterFlush = 0L;
        perfMaxQueueDepthAfterFlush = 0;
        perfTotalSectionsRecaptured = 0L;
        perfMaxSectionsRecaptured = 0;
        activeRenderMethodStartNanos = 0L;
    }

    public static void onFrameRenderStart() {
        RemixParticleCapture.onFrameRenderStart();
        activeRenderMethodStartNanos = System.nanoTime();
    }

    private static void recordPresentPerf(
            long frameNanos,
            long renderMethodNanos,
            long prePresentNanos,
            long hookFlushNanos,
            long presentNanos,
            long postPresentNanos,
            long chunkFlushNanos,
            int queueDepthBeforeFlush,
            int queueDepthAfterFlush,
            int sectionsRecaptured) {
        long flushNanos = chunkFlushNanos > 0L ? chunkFlushNanos : hookFlushNanos;
        ++perfFrameCount;
        perfTotalFrameNanos += frameNanos;
        perfMaxFrameNanos = Math.max(perfMaxFrameNanos, frameNanos);
        perfTotalRenderMethodNanos += renderMethodNanos;
        perfMaxRenderMethodNanos = Math.max(perfMaxRenderMethodNanos, renderMethodNanos);
        perfTotalPrePresentNanos += prePresentNanos;
        perfMaxPrePresentNanos = Math.max(perfMaxPrePresentNanos, prePresentNanos);
        perfTotalFlushNanos += flushNanos;
        perfMaxFlushNanos = Math.max(perfMaxFlushNanos, flushNanos);
        perfTotalPresentNanos += presentNanos;
        perfMaxPresentNanos = Math.max(perfMaxPresentNanos, presentNanos);
        perfTotalPostPresentNanos += postPresentNanos;
        perfMaxPostPresentNanos = Math.max(perfMaxPostPresentNanos, postPresentNanos);
        perfTotalQueueDepthBeforeFlush += queueDepthBeforeFlush;
        perfMaxQueueDepthBeforeFlush = Math.max(perfMaxQueueDepthBeforeFlush, queueDepthBeforeFlush);
        perfTotalQueueDepthAfterFlush += queueDepthAfterFlush;
        perfMaxQueueDepthAfterFlush = Math.max(perfMaxQueueDepthAfterFlush, queueDepthAfterFlush);
        perfTotalSectionsRecaptured += sectionsRecaptured;
        perfMaxSectionsRecaptured = Math.max(perfMaxSectionsRecaptured, sectionsRecaptured);

        if (perfFrameCount < PERF_LOG_INTERVAL_FRAMES) {
            return;
        }

        StringBuilder summary = new StringBuilder(256);
        summary.append("[mcrtx] perf java frames=")
                .append(perfFrameCount)
                .append(" frameAvgMs=")
                .append(formatAverageMillis(perfTotalFrameNanos, perfFrameCount))
                .append(" frameMaxMs=")
                .append(formatMillis(perfMaxFrameNanos))
            .append(" renderMethodAvgMs=")
            .append(formatAverageMillis(perfTotalRenderMethodNanos, perfFrameCount))
            .append(" renderMethodMaxMs=")
            .append(formatMillis(perfMaxRenderMethodNanos))
            .append(" prePresentAvgMs=")
            .append(formatAverageMillis(perfTotalPrePresentNanos, perfFrameCount))
            .append(" prePresentMaxMs=")
            .append(formatMillis(perfMaxPrePresentNanos))
                .append(" flushAvgMs=")
                .append(formatAverageMillis(perfTotalFlushNanos, perfFrameCount))
                .append(" flushMaxMs=")
                .append(formatMillis(perfMaxFlushNanos))
                .append(" presentAvgMs=")
                .append(formatAverageMillis(perfTotalPresentNanos, perfFrameCount))
                .append(" presentMaxMs=")
                .append(formatMillis(perfMaxPresentNanos))
                .append(" postAvgMs=")
                .append(formatAverageMillis(perfTotalPostPresentNanos, perfFrameCount))
                .append(" queueBeforeAvg=")
                .append(formatAverageCount(perfTotalQueueDepthBeforeFlush, perfFrameCount))
                .append(" queueBeforeMax=")
                .append(perfMaxQueueDepthBeforeFlush)
                .append(" queueAfterAvg=")
                .append(formatAverageCount(perfTotalQueueDepthAfterFlush, perfFrameCount))
                .append(" queueAfterMax=")
                .append(perfMaxQueueDepthAfterFlush)
                .append(" sectionsAvg=")
                .append(formatAverageCount(perfTotalSectionsRecaptured, perfFrameCount))
                .append(" sectionsMax=")
                .append(perfMaxSectionsRecaptured);
        System.out.println(summary.toString());
        resetPerfTracking();
    }

    private static String formatAverageMillis(long totalNanos, long sampleCount) {
        if (sampleCount <= 0L) {
            return "0.00";
        }
        return formatDouble((double) totalNanos / (double) sampleCount / 1000000.0);
    }

    private static String formatAverageCount(long totalCount, long sampleCount) {
        if (sampleCount <= 0L) {
            return "0.0";
        }
        return formatDouble((double) totalCount / (double) sampleCount);
    }

    private static String formatMillis(long nanos) {
        return formatDouble((double) nanos / 1000000.0);
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static boolean detectStandaloneWindowMode() {
        String configuredMode = System.getenv("MCRTX_WINDOW_MODE");
        return configuredMode != null && configuredMode.equalsIgnoreCase("standalone");
    }

    private static boolean syncRemixUiInput(net.minecraft.client.Minecraft minecraft, boolean allowHotkeyToggle) {
        int uiState = MinecraftRenderHooks.getUiState();
        boolean altDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        boolean xDown = Keyboard.isKeyDown(Keyboard.KEY_X);
        boolean hotkeyHeld = altDown && xDown;

        if (allowHotkeyToggle && hotkeyHeld && !remixUiHotkeyHeld) {
            int targetState = uiState == MinecraftRenderHooks.REMIX_UI_STATE_NONE
                    ? preferredRemixUiState
                    : MinecraftRenderHooks.REMIX_UI_STATE_NONE;
            if (MinecraftRenderHooks.setUiState(targetState)) {
                uiState = targetState;
                System.out.println("[mcrtx] Remix UI hotkey toggled state=" + uiState);
            } else {
                System.out.println("[mcrtx] Remix UI hotkey failed: " + MinecraftRenderHooks.lastError());
            }
        }

        remixUiHotkeyHeld = hotkeyHeld;
        if (uiState != MinecraftRenderHooks.REMIX_UI_STATE_NONE) {
            preferredRemixUiState = uiState;
        }

        boolean uiOpen = uiState != MinecraftRenderHooks.REMIX_UI_STATE_NONE;
        if (minecraft != null) {
            if (uiOpen) {
                minecraft.h();
            } else if (remixUiOpen && minecraft.r == null) {
                minecraft.g();
            }
        }

        remixUiOpen = uiOpen;
        return uiOpen;
    }
}
