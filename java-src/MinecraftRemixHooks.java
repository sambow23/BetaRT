import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxRuntimeConfig;
import mcrtx.bridge.McrtxRuntimeSettings;
import mcrtx.bridge.MinecraftPlatform;
import mcrtx.bridge.MinecraftPlatformKey;
import mcrtx.bridge.MinecraftPlatformRuntime;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.UiOverlayCapture;
import net.minecraft.client.Minecraft;
import java.util.Locale;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * Thin dispatcher that receives every bytecode-patched callback from the Beta
 * client and routes it to the corresponding capture subsystem. The set of
 * public static methods on this class is the ABI the patcher targets and must
 * not change without updating {@code ClientPatchTool} in lockstep.
 */
public final class MinecraftRemixHooks {
    private static final int DEFAULT_REMIX_UI_STATE = MinecraftRenderHooks.REMIX_UI_STATE_ADVANCED;
    private static final int PERF_LOG_INTERVAL_FRAMES = 60;
    private static final int WINDOWS_VK_MENU = 0x12;
    private static final int WINDOWS_VK_X = 0x58;
    private static final int MCRTX_OPTIONS_BUTTON_ID = 102;
    private static final long REMIX_UI_HOTKEY_RELEASE_DEBOUNCE_NANOS = 150_000_000L;
    private static final boolean STANDALONE_WINDOW_MODE = detectStandaloneWindowMode();
    private static final boolean VERBOSE_LOGGING = detectVerboseLoggingEnabled();
    private static final boolean VERBOSE_INPUT_LOGGING = detectVerboseInputLoggingEnabled();
    private static final boolean NATIVE_INPUT_BACKEND = detectNativeInputBackend();

    private static boolean loggedDisplayCreate;
    private static boolean loggedDisplayReset;
    private static boolean loggedPresent;
    private static boolean loggedPlatformBackend;
    private static boolean remixUiOpen;
    private static boolean remixUiHotkeyHeld;
    private static boolean remixUiHotkeyLocked;
    private static boolean remixUiLastAltHotkeyDown;
    private static boolean remixUiLastXHotkeyDown;
    private static long remixUiHotkeyReleaseStartedNanos;
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
    private static long activeUiRenderBeginNanos;
    private static float lastCameraPartialTicks;

    static {
        System.out.println("[mcrtx] MinecraftRemixHooks loaded");
    }

    private MinecraftRemixHooks() {
    }

    public static void onDisplayCreated(int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            if (!loggedDisplayCreate) {
                loggedDisplayCreate = true;
                System.out.println("[mcrtx] onDisplayCreated width=" + width + " height=" + height);
            }
            if (!loggedPlatformBackend) {
                loggedPlatformBackend = true;
                System.out.println(
                        "[mcrtx] platform backend="
                                + MinecraftPlatformRuntime.currentBackendSelection()
                                + " status="
                                + MinecraftPlatformRuntime.selectionStatus());
                System.out.println("[mcrtx] input backend=" + (NATIVE_INPUT_BACKEND ? "native" : "platform"));
            }
            resetRemixUiTracking();
            resetPerfTracking();
            UiOverlayCapture.reset();
            MinecraftRenderHooks.initializeForCurrentDisplay(width, height);
            applySavedMcrtxSettings();
        } finally {
            HookProfiler.endHook("hook.onDisplayCreated", __perf);
        }
    }

    public static void onShutdown() {
        long __perf = HookProfiler.begin();
        try {
            System.out.println("[mcrtx] onShutdown");
            resetRemixUiTracking();
            resetPerfTracking();
            UiOverlayCapture.reset();
            MinecraftRenderHooks.shutdown();
        } finally {
            HookProfiler.endHook("hook.onShutdown", __perf);
            HookProfiler.flushAll();
        }
    }

    public static void onDisplayReset(int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            if (!loggedDisplayReset) {
                loggedDisplayReset = true;
                System.out.println("[mcrtx] onDisplayReset width=" + width + " height=" + height);
            }
            resetRemixUiTracking();
            resetPerfTracking();
            UiOverlayCapture.reset();
            MinecraftRenderHooks.reinitializeForCurrentDisplay(width, height);
            applySavedMcrtxSettings();
        } finally {
            HookProfiler.endHook("hook.onDisplayReset", __perf);
        }
    }

    public static void onResize(int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            MinecraftRenderHooks.resize(width, height);
        } finally {
            HookProfiler.endHook("hook.onResize", __perf);
        }
    }

    public static void onCamera(ls entity, float partialTicks, int width, int height, float farPlane, boolean thirdPersonActive) {
        long __perf = HookProfiler.begin();
        try {
            lastCameraPartialTicks = partialTicks;
            RemixCameraState.onCamera(entity, partialTicks, width, height, farPlane, thirdPersonActive);
        } finally {
            HookProfiler.endHook("hook.onCamera", __perf);
        }
    }

    public static void onFrameViewCaptured() {
        long __perf = HookProfiler.begin();
        try {
            RemixCameraState.captureFrameView();
        } finally {
            HookProfiler.endHook("hook.onFrameViewCaptured", __perf);
        }
    }

    public static void onPresent() {
        long __perf = HookProfiler.begin();
        try {
            long frameStartNanos = System.nanoTime();
            long renderMethodStartNanos = activeRenderMethodStartNanos;
            long uiRenderBeginNanos = activeUiRenderBeginNanos;
            RemixChunkCapture.flushPendingChunkRecaptures();
            long flushEndNanos = System.nanoTime();
            if (!loggedPresent) {
                loggedPresent = true;
                System.out.println("[mcrtx] onPresent");
            }
            // Drain pending Java profiler samples so they land in the same
            // native flush window as the remix.Present that follows.
            HookProfiler.flushAll();
            long profilerFlushEndNanos = System.nanoTime();
            MinecraftRenderHooks.present();
            long presentEndNanos = System.nanoTime();
            RemixDynamicEntityCapture.onFramePresented();
            RemixCameraState.onFramePresented();
            long frameEndNanos = System.nanoTime();
            long prePresentNanos = renderMethodStartNanos > 0L ? Math.max(0L, frameStartNanos - renderMethodStartNanos) : 0L;
            long renderMethodNanos = renderMethodStartNanos > 0L ? Math.max(0L, frameEndNanos - renderMethodStartNanos) : 0L;
            activeRenderMethodStartNanos = 0L;
            activeUiRenderBeginNanos = 0L;

            // onPresent sub-site breakdown. Uses the raw timestamps captured
            // above so there's no extra instrumentation overhead.
                HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPresent.chunkFlush",
                    flushEndNanos - frameStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPresent.profilerFlush",
                    profilerFlushEndNanos - flushEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPresent.present",
                    presentEndNanos - profilerFlushEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPresent.post",
                    frameEndNanos - presentEndNanos);
            if (renderMethodStartNanos > 0L) {
                // renderMethod is the parent of onPresent; its prePresent phase
                // covers the portion before onPresent fires.
                HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.renderMethod", renderMethodNanos);
                HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.renderMethod.prePresent", prePresentNanos);
                if (uiRenderBeginNanos > 0L && uiRenderBeginNanos >= renderMethodStartNanos) {
                    long preUiNanos = Math.max(0L, uiRenderBeginNanos - renderMethodStartNanos);
                    long uiPhaseNanos = Math.max(0L, frameStartNanos - uiRenderBeginNanos);
                    HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.renderMethod.preUi", preUiNanos);
                    HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.renderMethod.uiPhase", uiPhaseNanos);
                }
            }

            recordPresentPerf(
                    frameEndNanos - frameStartNanos,
                renderMethodNanos,
                prePresentNanos,
                    flushEndNanos - frameStartNanos,
                    presentEndNanos - profilerFlushEndNanos,
                    frameEndNanos - presentEndNanos,
                    RemixChunkCapture.lastFlushDurationNanos(),
                    RemixChunkCapture.lastPendingQueueDepthBeforeFlush(),
                    RemixChunkCapture.lastPendingQueueDepthAfterFlush(),
                    RemixChunkCapture.lastSectionsRecaptured());
        } finally {
            HookProfiler.endHook("hook.onPresent", __perf);
        }
    }

    public static void onUiRenderBegin(int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            // Capture the UI phase start so onPresent can split renderMethod
            // into a pre-UI scene phase and a UI phase.
            activeUiRenderBeginNanos = System.nanoTime();
            if (STANDALONE_WINDOW_MODE) {
                return;
            }
            UiOverlayCapture.begin(width, height);
        } finally {
            HookProfiler.endHook("hook.onUiRenderBegin", __perf);
        }
    }

    public static void onUiRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            if (STANDALONE_WINDOW_MODE) {
                return;
            }
            UiOverlayCapture.end();
        } finally {
            HookProfiler.endHook("hook.onUiRenderEnd", __perf);
        }
    }

    public static void onRemixUiTick(net.minecraft.client.Minecraft minecraft) {
        long __perf = HookProfiler.begin();
        try {
            if (STANDALONE_WINDOW_MODE) {
                return;
            }
            syncRemixUiInput(minecraft, true);
        } finally {
            HookProfiler.endHook("hook.onRemixUiTick", __perf);
        }
    }

    public static boolean isWindowInteractionActive() {
        long __perf = HookProfiler.begin();
        try {
            return currentWindowActive() || (!STANDALONE_WINDOW_MODE && remixUiOpen);
        } finally {
            HookProfiler.endHook("hook.isWindowInteractionActive", __perf);
        }
    }

    public static boolean shouldRenderBoundingBox(yn frustum, eq bounds) {
        long __perf = HookProfiler.begin();
        try {
            if (bounds == null) {
                return true;
            }
            if (RemixCameraState.isWithinNoCullDistance(bounds.a, bounds.b, bounds.c, bounds.d, bounds.e, bounds.f)) {
                return true;
            }
            return frustum == null || frustum.a(bounds);
        } finally {
            HookProfiler.endHook("hook.shouldRenderBoundingBox", __perf);
        }
    }

    public static void onWorldChanged(fd world) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onWorldChanged(world);
        } finally {
            HookProfiler.endHook("hook.onWorldChanged", __perf);
        }
    }

    public static void clearWorldScene() {
        long __perf = HookProfiler.begin();
        try {
            MinecraftRenderHooks.clearWorldScene();
        } finally {
            HookProfiler.endHook("hook.clearWorldScene", __perf);
        }
    }

    public static void onChunkSectionUnload(int originX, int originY, int originZ) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkSectionUnload(originX, originY, originZ);
        } finally {
            HookProfiler.endHook("hook.onChunkSectionUnload", __perf);
        }
    }

    public static void onCloudRender(net.minecraft.client.Minecraft minecraft, fd world, int cloudTick, float partialTicks, boolean fancy) {
        long __perf = HookProfiler.begin();
        try {
            RemixCloudCapture.onCloudRender(minecraft, world, cloudTick, partialTicks, fancy);
        } finally {
            HookProfiler.endHook("hook.onCloudRender", __perf);
        }
    }

    public static void onFogState(ls entity, boolean thickFog, int renderLayer, boolean forceStartAtCamera, float viewDistance, float colorR, float colorG, float colorB) {
        long __perf = HookProfiler.begin();
        try {
            RemixFogCapture.onFogState(entity, thickFog, renderLayer, forceStartAtCamera, viewDistance, colorR, colorG, colorB);
        } finally {
            HookProfiler.endHook("hook.onFogState", __perf);
        }
    }

    public static void onLivingEntityFrameBegin() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEntityFrameBegin();
        } finally {
            HookProfiler.endHook("hook.onLivingEntityFrameBegin", __perf);
        }
    }

    public static void onDestroyOverlayRender(int blockX, int blockY, int blockZ, float destroyProgress) {
        long __perf = HookProfiler.begin();
        try {
            RemixDestroyOverlayCapture.onDestroyOverlayRender(blockX, blockY, blockZ, destroyProgress);
        } finally {
            HookProfiler.endHook("hook.onDestroyOverlayRender", __perf);
        }
    }

    public static void onBlockOutlineRender(gs player, vf movingobjectposition, int renderMode, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixBlockOutlineCapture.onBlockOutlineRender(player, movingobjectposition, renderMode, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onBlockOutlineRender", __perf);
        }
    }

    public static void onBlockOutlineRender(vf movingobjectposition, int renderMode) {
        long __perf = HookProfiler.begin();
        try {
            Minecraft minecraft = MinecraftRenderHooks.getRememberedMinecraft();
            gs player = minecraft != null && minecraft.h instanceof gs ? (gs) minecraft.h : null;
            RemixBlockOutlineCapture.onBlockOutlineRender(player, movingobjectposition, renderMode, lastCameraPartialTicks);
        } finally {
            HookProfiler.endHook("hook.onBlockOutlineRender", __perf);
        }
    }

    public static void onParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.onParticleRender", __perf);
        }
    }

    public static void onAnimatedParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onAnimatedParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.onAnimatedParticleRender", __perf);
        }
    }

    public static void onLivingEntityRenderStart(sn entity, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEntityRenderStart(entity, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onLivingEntityRenderStart", __perf);
        }
    }

    public static void onLivingEntityRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEntityRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onLivingEntityRenderEnd", __perf);
        }
    }

    public static void onPickupParticleEntityRenderStart(sn entity) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPickupParticleEntityRenderStart(entity);
        } finally {
            HookProfiler.endHook("hook.onPickupParticleEntityRenderStart", __perf);
        }
    }

    public static void onPickupParticleEntityRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPickupParticleEntityRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onPickupParticleEntityRenderEnd", __perf);
        }
    }

    public static void onItemEntityRenderStart(sn entity) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onItemEntityRenderStart(entity);
        } finally {
            HookProfiler.endHook("hook.onItemEntityRenderStart", __perf);
        }
    }

    public static void onItemEntityRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onItemEntityRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onItemEntityRenderEnd", __perf);
        }
    }

    public static void onEntityFireOverlayStart(sn entity) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onEntityFireOverlayStart(entity);
        } finally {
            HookProfiler.endHook("hook.onEntityFireOverlayStart", __perf);
        }
    }

    public static void onEntityFireOverlayEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onEntityFireOverlayEnd();
        } finally {
            HookProfiler.endHook("hook.onEntityFireOverlayEnd", __perf);
        }
    }

    public static void onSignRenderStart(yk sign) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignRenderStart(sign);
        } finally {
            HookProfiler.endHook("hook.onSignRenderStart", __perf);
        }
    }

    public static void onSignRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onSignRenderEnd", __perf);
        }
    }

    public static void onMovingPistonRenderStart(uk piston) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onMovingPistonRenderStart(piston);
        } finally {
            HookProfiler.endHook("hook.onMovingPistonRenderStart", __perf);
        }
    }

    public static void onMovingPistonRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onMovingPistonRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onMovingPistonRenderEnd", __perf);
        }
    }

    public static void onPaintingRender(qv painting) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPaintingRender(painting);
        } finally {
            HookProfiler.endHook("hook.onPaintingRender", __perf);
        }
    }

    public static boolean tryReplacePaintingRender(qv painting) {
        long __perf = HookProfiler.begin();
        try {
            boolean captured = RemixDynamicEntityCapture.capturePaintingRender(painting);
            return captured && McrtxRuntimeSettings.isPaintingVanillaSuppressionEnabled();
        } finally {
            HookProfiler.endHook("hook.tryReplacePaintingRender", __perf);
        }
    }

    public static void drawTessellator(int mode, int first, int count) {
        if (McrtxRuntimeSettings.isMovingPistonVanillaSuppressionEnabled()
                && RemixDynamicEntityCapture.shouldSuppressMovingPistonVanillaDraw()) {
            return;
        }
        GL11.glDrawArrays(mode, first, count);
    }

    public static void onSignTextRender(String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
        } finally {
            HookProfiler.endHook("hook.onSignTextRender", __perf);
        }
    }

    public static void onFirstPersonRenderStart() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonRenderStart();
        } finally {
            HookProfiler.endHook("hook.onFirstPersonRenderStart", __perf);
        }
    }

    public static void onFirstPersonShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonShadowPlayerRender(minecraft, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onFirstPersonShadowPlayerRender", __perf);
        }
    }

    public static void onFirstPersonRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onFirstPersonRenderEnd", __perf);
        }
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonItemRender(itemStack);
        } finally {
            HookProfiler.endHook("hook.onFirstPersonItemRender", __perf);
        }
    }

    public static void onPlayerEquippedItemRenderStart(gs player, iz itemStack, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPlayerEquippedItemRenderStart(player, itemStack, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onPlayerEquippedItemRenderStart", __perf);
        }
    }

    public static void onLivingEquippedItemRenderStart(ls entity, iz itemStack) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEquippedItemRenderStart(entity, itemStack);
        } finally {
            HookProfiler.endHook("hook.onLivingEquippedItemRenderStart", __perf);
        }
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onEntityTextureBind(primaryTexture, fallbackTexture);
        } finally {
            HookProfiler.endHook("hook.onEntityTextureBind", __perf);
        }
    }

    public static void onModelPartRender(tz[] polygons, float scale) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onModelPartRender(polygons, scale);
        } finally {
            HookProfiler.endHook("hook.onModelPartRender", __perf);
        }
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
            RemixParticleCapture.onTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
        } finally {
            HookProfiler.endHook("hook.onFirstPersonTessellatorDraw", __perf);
        }
    }

    public static void onWeatherTextureBind(String texturePath) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onWeatherTextureBind(texturePath);
        } finally {
            HookProfiler.endHook("hook.onWeatherTextureBind", __perf);
        }
    }

    public static void onWeatherRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onWeatherRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onWeatherRenderEnd", __perf);
        }
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        long __perf = HookProfiler.begin();
        try {
            return RemixChunkCapture.onChunkBuildBegin(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
        } finally {
            HookProfiler.endHook("hook.onChunkBuildBegin", __perf);
        }
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkBlock(blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
        } finally {
            HookProfiler.endHook("hook.onChunkBlock", __perf);
        }
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkBuildEnd(emittedGeometry);
        } finally {
            HookProfiler.endHook("hook.onChunkBuildEnd", __perf);
        }
    }

    public static void configureMcrtxOptionsScreen(co screen) {
        if (screen == null) {
            return;
        }

        ke doneButton = null;
        for (Object entry : screen.e) {
            if (!(entry instanceof ke)) {
                continue;
            }

            ke button = (ke) entry;
            if (button.f == MCRTX_OPTIONS_BUTTON_ID) {
                return;
            }
            if (button.f == 200) {
                doneButton = button;
            }
        }

        if (doneButton != null) {
            doneButton.d += 24;
        }

        screen.e.add(new ke(
                MCRTX_OPTIONS_BUTTON_ID,
                screen.c / 2 - 100,
                screen.d / 6 + 168,
                "BetaRT"));
    }

    public static boolean handleMcrtxOptionsButton(co screen, ke button) {
        if (screen == null || button == null || !button.g || button.f != MCRTX_OPTIONS_BUTTON_ID) {
            return false;
        }

        screen.b.a(new McrtxOptionsScreen(screen));
        return true;
    }

    public static boolean isPlayerShadowsEnabled() {
        return McrtxRuntimeSettings.isPlayerShadowsEnabled();
    }

    public static boolean isHeldTorchLightsEnabled() {
        return McrtxRuntimeSettings.isHeldTorchLightsEnabled();
    }

    public static boolean isDynamicEntityRenderingEnabled() {
        return McrtxRuntimeSettings.isDynamicEntityRenderingEnabled();
    }

    public static int getGameplayFovDegrees() {
        return McrtxRuntimeSettings.getGameplayFovDegrees();
    }

    public static int getViewModelFovDegrees() {
        return McrtxRuntimeSettings.getViewModelFovDegrees();
    }

    public static int getNoCullDistanceBlocks() {
        return McrtxRuntimeSettings.getNoCullDistanceBlocks();
    }

    public static boolean isBlockOutlineEnabled() {
        return McrtxRuntimeSettings.isBlockOutlineEnabled();
    }

    public static int getBlockOutlineStyle() {
        return McrtxRuntimeSettings.getBlockOutlineStyle();
    }

    public static int getBlockOutlineEmissiveIntensityTenths() {
        return McrtxRuntimeSettings.getBlockOutlineEmissiveIntensityTenths();
    }

    public static boolean shouldShowBlockOutlineIntensitySlider() {
        return isBlockOutlineEnabled() && isBlockOutlineEmissiveStyle(getBlockOutlineStyle());
    }

    public static String getPlayerShadowsButtonLabel() {
        return "Player Shadows: " + formatToggleState(isPlayerShadowsEnabled());
    }

    public static String getHeldTorchLightsButtonLabel() {
        return "Held Torch Lights: " + formatToggleState(isHeldTorchLightsEnabled());
    }

    public static String getDynamicEntityRenderingButtonLabel() {
        return "Dynamic Entities: " + formatToggleState(isDynamicEntityRenderingEnabled());
    }

    public static String getPaintingVanillaSuppressionButtonLabel() {
        return "Replace Paintings: " + formatToggleState(isPaintingVanillaSuppressionEnabled());
    }

    public static String getMovingPistonVanillaSuppressionButtonLabel() {
        return "Replace Moving Pistons: " + formatToggleState(isMovingPistonVanillaSuppressionEnabled());
    }

    public static String getRtQualityButtonLabel() {
        return "PT Quality: " + describeRtQuality(McrtxRuntimeSettings.getRtQuality());
    }

    public static String getUpscalerButtonLabel() {
        return "Upscaler: " + describeUpscalerType(McrtxRuntimeSettings.getUpscalerType());
    }

    public static String getUpscalerPresetButtonLabel() {
        int upscalerType = McrtxRuntimeSettings.getUpscalerType();
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
                return "Preset: " + describeDlssPreset(McrtxRuntimeSettings.getDlssPreset());
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                return "Preset: " + describeXessPreset(McrtxRuntimeSettings.getXessPreset());
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                return "Preset: " + describeTaauPreset(McrtxRuntimeSettings.getTaauPreset());
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
            default:
                return "Preset: N/A";
        }
    }

    public static String getRayReconstructionButtonLabel() {
        return "Ray Reconstruction: " + formatToggleState(McrtxRuntimeSettings.isRayReconstructionEnabled());
    }

    public static String getBlockOutlineButtonLabel() {
        return "Block Outline: " + formatToggleState(isBlockOutlineEnabled());
    }

    public static String getBlockOutlineStyleButtonLabel() {
        return "Outline Style: " + describeBlockOutlineStyle(getBlockOutlineStyle());
    }

    public static void setPlayerShadowsEnabled(boolean enabled) {
        McrtxRuntimeSettings.setPlayerShadowsEnabled(enabled);
        RemixDynamicEntityCapture.setPlayerShadowsEnabled(enabled);
        MinecraftRenderHooks.setPlayerShadowsEnabled(enabled);
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        McrtxRuntimeSettings.setHeldTorchLightsEnabled(enabled);
        RemixDynamicEntityCapture.setHeldTorchLightsEnabled(enabled);
        MinecraftRenderHooks.setHeldTorchLightsEnabled(enabled);
    }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) {
        McrtxRuntimeSettings.setDynamicEntityRenderingEnabled(enabled);
        RemixDynamicEntityCapture.setDynamicEntityRenderingEnabled(enabled);
        MinecraftRenderHooks.setDynamicEntityRenderingEnabled(enabled);
    }

    public static boolean isPaintingVanillaSuppressionEnabled() {
        return McrtxRuntimeSettings.isPaintingVanillaSuppressionEnabled();
    }

    public static boolean isMovingPistonVanillaSuppressionEnabled() {
        return McrtxRuntimeSettings.isMovingPistonVanillaSuppressionEnabled();
    }

    public static void setPaintingVanillaSuppressionEnabled(boolean enabled) {
        McrtxRuntimeSettings.setPaintingVanillaSuppressionEnabled(enabled);
    }

    public static void setMovingPistonVanillaSuppressionEnabled(boolean enabled) {
        McrtxRuntimeSettings.setMovingPistonVanillaSuppressionEnabled(enabled);
    }

    public static void setGameplayFovDegrees(int fovDegrees) {
        McrtxRuntimeSettings.setGameplayFovDegrees(fovDegrees);
    }

    public static void setViewModelFovDegrees(int fovDegrees) {
        McrtxRuntimeSettings.setViewModelFovDegrees(fovDegrees);
        MinecraftRenderHooks.setViewModelFovDegrees(fovDegrees);
    }

    public static void setNoCullDistanceBlocks(int blockDistance) {
        McrtxRuntimeSettings.setNoCullDistanceBlocks(blockDistance);
        RemixCameraState.setNoCullDistanceBlocks(blockDistance);
    }

    public static void setBlockOutlineEnabled(boolean enabled) {
        McrtxRuntimeSettings.setBlockOutlineEnabled(enabled);
        MinecraftRenderHooks.setBlockOutlineEnabled(enabled);
    }

    public static void setBlockOutlineEmissiveIntensityTenths(int intensityTenths) {
        McrtxRuntimeSettings.setBlockOutlineEmissiveIntensityTenths(intensityTenths);
        MinecraftRenderHooks.setBlockOutlineEmissiveIntensity(McrtxRuntimeSettings.getBlockOutlineEmissiveIntensity());
    }

    public static void cycleBlockOutlineStyle() {
        int style = McrtxRuntimeSettings.getBlockOutlineStyle();
        switch (style) {
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID:
            default:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE);
                break;
        }
        MinecraftRenderHooks.setBlockOutlineStyle(McrtxRuntimeSettings.getBlockOutlineStyle());
    }

    public static void cycleRtQuality() {
        int rtQuality = McrtxRuntimeSettings.getRtQuality();
        switch (rtQuality) {
            case McrtxRuntimeSettings.RT_QUALITY_LOW:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_MEDIUM);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_MEDIUM:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_HIGH);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_HIGH:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_ULTRA);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_ULTRA:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_POTATO);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_POTATO:
            default:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_LOW);
                break;
        }
        applyRtQualitySettings();
    }

    public static void cycleUpscalerType() {
        int upscalerType = McrtxRuntimeSettings.getUpscalerType();
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_DLSS);
                break;
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_XESS);
                break;
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_TAAU);
                break;
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
            default:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_NONE);
                break;
        }
        applyUpscalerSettings();
    }

    public static void cycleUpscalerPreset() {
        int upscalerType = McrtxRuntimeSettings.getUpscalerType();
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
                cycleDlssPreset();
                return;
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                cycleXessPreset();
                return;
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                cycleTaauPreset();
                return;
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
            default:
                return;
        }
    }

    public static boolean shouldShowRayReconstructionOption() {
        return McrtxRuntimeSettings.getUpscalerType() == McrtxRuntimeSettings.UPSCALER_TYPE_DLSS;
    }

    public static void toggleRayReconstructionEnabled() {
        if (!shouldShowRayReconstructionOption()) {
            return;
        }
        McrtxRuntimeSettings.setRayReconstructionEnabled(!McrtxRuntimeSettings.isRayReconstructionEnabled());
        applyUpscalerSettings();
    }

    private static void resetRemixUiTracking() {
        remixUiOpen = false;
        remixUiHotkeyHeld = false;
        remixUiHotkeyLocked = false;
        remixUiLastAltHotkeyDown = false;
        remixUiLastXHotkeyDown = false;
        remixUiHotkeyReleaseStartedNanos = 0L;
        preferredRemixUiState = DEFAULT_REMIX_UI_STATE;
        MinecraftRenderHooks.setRemixUiInputActive(false);
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
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onFrameRenderStart();
            activeRenderMethodStartNanos = System.nanoTime();
        } finally {
            HookProfiler.endHook("hook.onFrameRenderStart", __perf);
        }
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
        if (VERBOSE_LOGGING) {
            System.out.println(summary.toString());
        }
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
        String configuredMode = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_WINDOW_MODE");
        return configuredMode != null
                && configuredMode.equalsIgnoreCase("standalone");
    }

    private static boolean detectVerboseLoggingEnabled() {
        return McrtxRuntimeConfig.isTruthyEnvironmentValue("MCRTX_VERBOSE_LOG");
    }

    private static boolean detectVerboseInputLoggingEnabled() {
        String value = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_VERBOSE_INPUT_LOG");
        if (value == null || value.isEmpty()) {
            value = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_VERBOSE_LOG");
        }
        return McrtxRuntimeConfig.isTruthyValue(value);
    }

    private static boolean detectNativeInputBackend() {
        String configuredBackend = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_INPUT_BACKEND");
        return configuredBackend != null && configuredBackend.equalsIgnoreCase("native");
    }

    private static void applySavedMcrtxSettings() {
        boolean playerShadowsEnabled = McrtxRuntimeSettings.isPlayerShadowsEnabled();
        boolean heldTorchLightsEnabled = McrtxRuntimeSettings.isHeldTorchLightsEnabled();
        boolean dynamicEntityRenderingEnabled = McrtxRuntimeSettings.isDynamicEntityRenderingEnabled();
        RemixDynamicEntityCapture.setPlayerShadowsEnabled(playerShadowsEnabled);
        RemixDynamicEntityCapture.setHeldTorchLightsEnabled(heldTorchLightsEnabled);
        RemixDynamicEntityCapture.setDynamicEntityRenderingEnabled(dynamicEntityRenderingEnabled);
        MinecraftRenderHooks.setPlayerShadowsEnabled(playerShadowsEnabled);
        MinecraftRenderHooks.setHeldTorchLightsEnabled(heldTorchLightsEnabled);
        MinecraftRenderHooks.setDynamicEntityRenderingEnabled(dynamicEntityRenderingEnabled);
        MinecraftRenderHooks.setBlockOutlineEnabled(McrtxRuntimeSettings.isBlockOutlineEnabled());
        MinecraftRenderHooks.setBlockOutlineStyle(McrtxRuntimeSettings.getBlockOutlineStyle());
        MinecraftRenderHooks.setBlockOutlineEmissiveIntensity(McrtxRuntimeSettings.getBlockOutlineEmissiveIntensity());
        RemixCameraState.setNoCullDistanceBlocks(McrtxRuntimeSettings.getNoCullDistanceBlocks());
        MinecraftRenderHooks.setViewModelFovDegrees(McrtxRuntimeSettings.getViewModelFovDegrees());
        applyRtQualitySettings();
        applyUpscalerSettings();
    }

    private static String formatToggleState(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private static String describeBlockOutlineStyle(int style) {
        switch (style) {
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE:
                return "Subtle";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN:
                return "Thin";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW:
                return "Glow";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB:
                return "RGB";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID:
                return "Solid Fill";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD:
            default:
                return "Bold";
        }
    }

    private static boolean isBlockOutlineEmissiveStyle(int style) {
        return style == McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW
                || style == McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB;
    }

    private static void cycleDlssPreset() {
        int preset = McrtxRuntimeSettings.getDlssPreset();
        switch (preset) {
            case McrtxRuntimeSettings.DLSS_PRESET_AUTO:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_QUALITY);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_QUALITY:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_BALANCED);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_BALANCED:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_DLAA);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_DLAA:
            default:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_AUTO);
                break;
        }
        applyUpscalerSettings();
    }

    private static void cycleXessPreset() {
        int preset = McrtxRuntimeSettings.getXessPreset();
        switch (preset) {
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_BALANCED);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_BALANCED:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_QUALITY);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_QUALITY:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA:
            default:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE);
                break;
        }
        applyUpscalerSettings();
    }

    private static void cycleTaauPreset() {
        int preset = McrtxRuntimeSettings.getTaauPreset();
        switch (preset) {
            case McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_BALANCED);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_BALANCED:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_QUALITY);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_QUALITY:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN:
            default:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE);
                break;
        }
        applyUpscalerSettings();
    }

    private static void applyRtQualitySettings() {
        MinecraftRenderHooks.setRtQuality(McrtxRuntimeSettings.getRtQuality());
    }

    private static void applyUpscalerSettings() {
        MinecraftRenderHooks.setUpscalerConfig(
                McrtxRuntimeSettings.getUpscalerType(),
                McrtxRuntimeSettings.getDlssPreset(),
                McrtxRuntimeSettings.getXessPreset(),
                McrtxRuntimeSettings.getTaauPreset(),
                McrtxRuntimeSettings.isRayReconstructionEnabled());
    }

    private static String describeRtQuality(int rtQuality) {
        switch (rtQuality) {
            case McrtxRuntimeSettings.RT_QUALITY_POTATO:
                return "Potato";
            case McrtxRuntimeSettings.RT_QUALITY_LOW:
                return "Low";
            case McrtxRuntimeSettings.RT_QUALITY_MEDIUM:
                return "Medium";
            case McrtxRuntimeSettings.RT_QUALITY_ULTRA:
                return "Ultra";
            case McrtxRuntimeSettings.RT_QUALITY_HIGH:
            default:
                return "High";
        }
    }

    private static String describeUpscalerType(int upscalerType) {
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
                return "None";
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                return "XeSS";
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                return "TAAU";
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
            default:
                return "DLSS";
        }
    }

    private static String describeDlssPreset(int preset) {
        switch (preset) {
            case McrtxRuntimeSettings.DLSS_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.DLSS_PRESET_BALANCED:
                return "Balanced";
            case McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE:
                return "Ultra Performance";
            case McrtxRuntimeSettings.DLSS_PRESET_DLAA:
                return "DLAA";
            case McrtxRuntimeSettings.DLSS_PRESET_AUTO:
            default:
                return "Auto";
        }
    }

    private static String describeXessPreset(int preset) {
        switch (preset) {
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE:
                return "Ultra Performance";
            case McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.XESS_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY:
                return "Ultra Quality";
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS:
                return "Ultra Quality Plus";
            case McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA:
                return "Native AA";
            case McrtxRuntimeSettings.XESS_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static String describeTaauPreset(int preset) {
        switch (preset) {
            case McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE:
                return "Ultra Performance";
            case McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.TAAU_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN:
                return "Fullscreen";
            case McrtxRuntimeSettings.TAAU_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static void logRemixUiHotkeyEvent(
            String reason,
            boolean altDown,
            boolean xDown,
            boolean hotkeyHeld,
            boolean allowHotkeyToggle,
            int uiState,
            long nowNanos) {
        if (!VERBOSE_INPUT_LOGGING) {
            return;
        }

        String releaseAgeMillis = remixUiHotkeyReleaseStartedNanos == 0L
                ? "-"
                : formatMillis(Math.max(0L, nowNanos - remixUiHotkeyReleaseStartedNanos));
        System.out.println(
                "[mcrtx][ui-hotkey] "
                        + reason
                        + " alt="
                        + altDown
                        + " x="
                        + xDown
                        + " held="
                        + hotkeyHeld
                        + " prevHeld="
                        + remixUiHotkeyHeld
                        + " allowToggle="
                        + allowHotkeyToggle
                        + " locked="
                        + remixUiHotkeyLocked
                        + " releaseMs="
                        + releaseAgeMillis
                        + " uiState="
                        + uiState);
    }

    private static boolean currentWindowActive() {
        if (NATIVE_INPUT_BACKEND && MinecraftRenderHooks.isInitialized() && MinecraftRenderHooks.hasNativeWindowFocus()) {
            return true;
        }

        if (Display.isActive()) {
            return true;
        }

        return MinecraftPlatformRuntime.current().isWindowActive();
    }

    private static boolean isAltHotkeyDown(MinecraftPlatform platform) {
        if (NATIVE_INPUT_BACKEND && MinecraftRenderHooks.isInitialized()) {
            return MinecraftRenderHooks.isNativeVirtualKeyDown(WINDOWS_VK_MENU);
        }
        return platform.isKeyDown(MinecraftPlatformKey.LEFT_ALT)
                || platform.isKeyDown(MinecraftPlatformKey.RIGHT_ALT);
    }

    private static boolean isXHotkeyDown(MinecraftPlatform platform) {
        if (NATIVE_INPUT_BACKEND && MinecraftRenderHooks.isInitialized()) {
            return MinecraftRenderHooks.isNativeVirtualKeyDown(WINDOWS_VK_X);
        }
        return platform.isKeyDown(MinecraftPlatformKey.X);
    }

    private static boolean syncRemixUiInput(net.minecraft.client.Minecraft minecraft, boolean allowHotkeyToggle) {
        MinecraftPlatform platform = MinecraftPlatformRuntime.current();
        int uiState = MinecraftRenderHooks.getUiState();
        boolean manualHotkeyToggleEnabled = allowHotkeyToggle && !NATIVE_INPUT_BACKEND;
        boolean altDown = isAltHotkeyDown(platform);
        boolean xDown = isXHotkeyDown(platform);
        boolean hotkeyHeld = altDown && xDown;
        boolean hotkeyFullyReleased = !altDown && !xDown;
        long nowNanos = System.nanoTime();

        if (altDown != remixUiLastAltHotkeyDown || xDown != remixUiLastXHotkeyDown || hotkeyHeld != remixUiHotkeyHeld) {
            logRemixUiHotkeyEvent("poll-change", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
        }

        if (!hotkeyFullyReleased) {
            if (remixUiHotkeyReleaseStartedNanos != 0L) {
                logRemixUiHotkeyEvent("release-cancelled", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
            }
            remixUiHotkeyReleaseStartedNanos = 0L;
        }

        if (manualHotkeyToggleEnabled && hotkeyHeld && !remixUiHotkeyHeld && !remixUiHotkeyLocked) {
            int targetState = uiState == MinecraftRenderHooks.REMIX_UI_STATE_NONE
                    ? preferredRemixUiState
                    : MinecraftRenderHooks.REMIX_UI_STATE_NONE;
            if (MinecraftRenderHooks.setUiState(targetState)) {
                uiState = targetState;
                remixUiHotkeyLocked = true;
                logRemixUiHotkeyEvent("toggle-success", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
                System.out.println("[mcrtx] Remix UI hotkey toggled state=" + uiState);
            } else {
                logRemixUiHotkeyEvent("toggle-failed", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
                System.out.println("[mcrtx] Remix UI hotkey failed: " + MinecraftRenderHooks.lastError());
            }
        } else if (manualHotkeyToggleEnabled && hotkeyHeld && !remixUiHotkeyHeld && remixUiHotkeyLocked) {
            logRemixUiHotkeyEvent("suppressed-rising-edge", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
        } else if (hotkeyFullyReleased && remixUiHotkeyLocked) {
            if (remixUiHotkeyReleaseStartedNanos == 0L) {
                remixUiHotkeyReleaseStartedNanos = nowNanos;
                logRemixUiHotkeyEvent("release-timer-start", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
            } else if (nowNanos - remixUiHotkeyReleaseStartedNanos >= REMIX_UI_HOTKEY_RELEASE_DEBOUNCE_NANOS) {
                remixUiHotkeyLocked = false;
                logRemixUiHotkeyEvent("release-unlocked", altDown, xDown, hotkeyHeld, manualHotkeyToggleEnabled, uiState, nowNanos);
            }
        }

        remixUiLastAltHotkeyDown = altDown;
        remixUiLastXHotkeyDown = xDown;
        remixUiHotkeyHeld = hotkeyHeld;
        if (uiState != MinecraftRenderHooks.REMIX_UI_STATE_NONE) {
            preferredRemixUiState = uiState;
        }

        boolean uiOpen = uiState != MinecraftRenderHooks.REMIX_UI_STATE_NONE;
        MinecraftRenderHooks.setRemixUiInputActive(uiOpen);
        if (minecraft != null) {
            if (uiOpen) {
                minecraft.h();
            } else if (remixUiOpen && minecraft.r == null) {
                MinecraftRenderHooks.restoreIngameFocusIfNeeded();
            }
        }

        remixUiOpen = uiOpen;
        return uiOpen;
    }
}
