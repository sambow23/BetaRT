import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxRuntimeConfig;
import mcrtx.bridge.McrtxRuntimeSettings;
import mcrtx.bridge.MinecraftPlatform;
import mcrtx.bridge.MinecraftPlatformKey;
import mcrtx.bridge.MinecraftPlatformRuntime;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.UiOverlayCapture;
import net.minecraft.client.Minecraft;
import java.io.File;
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
    private static final int WINDOWS_VK_MENU = 0x12;
    private static final int WINDOWS_VK_X = 0x58;
    private static final int WINDOWS_VK_B = 0x42;
    private static final long REMIX_UI_HOTKEY_RELEASE_DEBOUNCE_NANOS = 150_000_000L;
    private static final boolean STANDALONE_WINDOW_MODE = detectStandaloneWindowMode();
    private static final boolean SINGLE_NATIVE_WINDOW_MODE = detectSingleNativeWindowMode();
    private static final boolean VERBOSE_LOGGING = detectVerboseLoggingEnabled();
    private static final boolean VERBOSE_INPUT_LOGGING = detectVerboseInputLoggingEnabled();
    private static final boolean NATIVE_INPUT_BACKEND = detectNativeInputBackend();

    private static boolean loggedDisplayCreate;
    private static boolean loggedDisplayReset;
    private static boolean loggedPresent;
    private static boolean loggedPlatformBackend;
    private static boolean suppressNextModelPartCallList;
    private static boolean worldRasterRenderActive;
    private static boolean remixUiOpen;
    private static boolean remixUiHotkeyHeld;
    private static boolean remixUiHotkeyLocked;
    private static boolean remixUiLastAltHotkeyDown;
    private static boolean remixUiLastXHotkeyDown;
    private static long remixUiHotkeyReleaseStartedNanos;
    private static boolean quickSettingsHotkeyHeld;
    private static boolean quickSettingsHotkeyLocked;
    private static boolean quickSettingsLastAltHotkeyDown;
    private static boolean quickSettingsLastBHotkeyDown;
    private static long quickSettingsHotkeyReleaseStartedNanos;
    private static int preferredRemixUiState = DEFAULT_REMIX_UI_STATE;
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
            McrtxHookPerfTracker.reset();
            UiOverlayCapture.reset();
            RemixUiCapture.reset();
            MinecraftRenderHooks.initializeForCurrentDisplay(width, height);
            McrtxHookSettingsUi.applySavedMcrtxSettings();
        } finally {
            HookProfiler.endHook("hook.onDisplayCreated", __perf);
        }
    }

    public static void onShutdown() {
        long __perf = HookProfiler.begin();
        try {
            System.out.println("[mcrtx] onShutdown");
            resetRemixUiTracking();
            McrtxHookPerfTracker.reset();
            UiOverlayCapture.reset();
            RemixUiCapture.reset();
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
            McrtxHookPerfTracker.reset();
            UiOverlayCapture.reset();
            RemixUiCapture.reset();
            if (SINGLE_NATIVE_WINDOW_MODE) {
                MinecraftRenderHooks.resize(width, height);
            } else {
                MinecraftRenderHooks.reinitializeForCurrentDisplay(width, height);
            }
            McrtxHookSettingsUi.applySavedMcrtxSettings();
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
            long renderMethodStartNanos = McrtxHookPerfTracker.renderMethodStartNanos();
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
            McrtxHookPerfTracker.clearRenderMethodStartNanos();
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

            McrtxHookPerfTracker.recordPresent(
                    frameEndNanos - frameStartNanos,
                renderMethodNanos,
                prePresentNanos,
                    flushEndNanos - frameStartNanos,
                    presentEndNanos - profilerFlushEndNanos,
                    frameEndNanos - presentEndNanos,
                    RemixChunkCapture.lastFlushDurationNanos(),
                    RemixChunkCapture.lastPendingQueueDepthBeforeFlush(),
                    RemixChunkCapture.lastPendingQueueDepthAfterFlush(),
                    RemixChunkCapture.lastSectionsRecaptured(),
                    VERBOSE_LOGGING);
        } finally {
            HookProfiler.endHook("hook.onPresent", __perf);
        }
    }

    public static String onScreenshot(File minecraftDir, int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            return McrtxHookScreenshotHelper.requestPresentedScreenshot(minecraftDir, width, height);
        } catch (Exception exception) {
            exception.printStackTrace();
            return "Failed to save: " + exception;
        } finally {
            HookProfiler.endHook("hook.onScreenshot", __perf);
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
            Minecraft minecraft = MinecraftRenderHooks.getRememberedMinecraft();
            if (minecraft != null && minecraft.z != null && minecraft.z.z && minecraft.r == null) {
                RemixUiCapture.clear(width, height);
                return;
            }
            RemixUiCapture.begin(width, height);
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
            if (!RemixUiCapture.isActive()) {
                Minecraft minecraft = MinecraftRenderHooks.getRememberedMinecraft();
                if (minecraft != null && minecraft.z != null && minecraft.z.z && minecraft.r == null) {
                    RemixUiCapture.clear(minecraft.d, minecraft.e);
                    return;
                }
            }
            RemixUiCapture.end();
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
            syncQuickSettingsInput(minecraft);
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
            RemixParticleCapture.captureParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.onParticleRender", __perf);
        }
    }

    public static boolean captureParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            return RemixParticleCapture.captureParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.captureParticleRender", __perf);
        }
    }

    public static void onAnimatedParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.captureAnimatedParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.onAnimatedParticleRender", __perf);
        }
    }

    public static boolean captureAnimatedParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            return RemixParticleCapture.captureAnimatedParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.captureAnimatedParticleRender", __perf);
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
        if (shouldSuppressWorldRasterVanillaDraw()) {
            return;
        }
        if (shouldSuppressCapturedVanillaTessellatorDraw()) {
            return;
        }
        GL11.glDrawArrays(mode, first, count);
    }

    public static void drawModelPartCallList(int list) {
        boolean suppress = suppressNextModelPartCallList;
        suppressNextModelPartCallList = false;
        if (suppress) {
            return;
        }
        GL11.glCallList(list);
    }

    public static void onWorldRasterRenderStart() {
        worldRasterRenderActive = true;
    }

    public static void onWorldRasterRenderEnd() {
        worldRasterRenderActive = false;
    }

    public static boolean shouldSuppressWorldRasterDisplayLists() {
        return shouldSuppressWorldRasterVanillaDraw();
    }

    public static void onNameTagRenderBegin() {
        long __perf = HookProfiler.begin();
        try {
            Minecraft minecraft = MinecraftRenderHooks.getRememberedMinecraft();
            if (minecraft == null) {
                return;
            }
            RemixUiCapture.beginNameTagCapture(minecraft.d, minecraft.e);
        } finally {
            HookProfiler.endHook("hook.onNameTagRenderBegin", __perf);
        }
    }

    public static void onNameTagRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixUiCapture.endNameTagCapture();
        } finally {
            HookProfiler.endHook("hook.onNameTagRenderEnd", __perf);
        }
    }

    public static boolean captureFontStringAndMaybeSuppress(
            String text,
            int x,
            int y,
            int colorRgba,
            boolean shadow,
            int[] characterWidths,
            int fontTextureGlId) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
            RemixUiCapture.onFontString(text, x, y, colorRgba, shadow, characterWidths, fontTextureGlId);
            return RemixUiCapture.isActive()
                    && text != null
                    && !text.isEmpty()
                    && characterWidths != null
                    && fontTextureGlId > 0;
        } finally {
            HookProfiler.endHook("hook.captureFontStringAndMaybeSuppress", __perf);
        }
    }

    public static boolean tryReplaceSignModelRender(rf signModel) {
        long __perf = HookProfiler.begin();
        try {
            if (RemixUiCapture.isActive()) {
                // GUI phase (edit-sign screen): let the sign model parts render
                // normally so they flow through the model-part hook into the
                // screen-space UI pass instead of the off-screen world capture.
                return false;
            }
            boolean captured = RemixDynamicEntityCapture.captureSignModelRender(signModel);
            return captured && McrtxRuntimeSettings.isSignVanillaSuppressionEnabled();
        } finally {
            HookProfiler.endHook("hook.tryReplaceSignModelRender", __perf);
        }
    }

    public static void renderSignText(sj fontRenderer, String text, int x, int y, int colorRgba) {
        if (!RemixUiCapture.isActive()
                && McrtxRuntimeSettings.isSignVanillaSuppressionEnabled()
                && McrtxRuntimeSettings.isSignTextCaptureEnabled()
                && RemixDynamicEntityCapture.captureSignTextRender(fontRenderer, text, x, y, colorRgba)) {
            return;
        }
        fontRenderer.b(text, x, y, colorRgba);
    }

    public static void onSignTextRender(String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths, int fontTextureGlId) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
            RemixUiCapture.onFontString(text, x, y, colorRgba, shadow, characterWidths, fontTextureGlId);
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
            boolean captured;
            if (RemixUiCapture.isActive()) {
                // GUI phase: 3D model parts (inventory player preview) render
                // into the screen-space UI pass, not the world.
                captured = RemixUiCapture.onModelPart(polygons, scale);
            } else {
                captured = RemixDynamicEntityCapture.onModelPartRender(polygons, scale);
            }
            suppressNextModelPartCallList = captured;
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
            RemixUiCapture.onTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
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

    public static boolean isPlayerShadowsEnabled() {
        return McrtxRuntimeSettings.isPlayerShadowsEnabled();
    }

    public static boolean isHeldTorchLightsEnabled() {
        return McrtxRuntimeSettings.isHeldTorchLightsEnabled();
    }

    public static boolean isDynamicEntityRenderingEnabled() {
        return McrtxRuntimeSettings.isDynamicEntityRenderingEnabled();
    }

    public static boolean isLivingEntityRenderingEnabled() {
        return McrtxRuntimeSettings.isLivingEntityRenderingEnabled();
    }

    public static boolean isItemEntityRenderingEnabled() {
        return McrtxRuntimeSettings.isItemEntityRenderingEnabled();
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

    public static int getBlockOutlineEmissiveIntensityHundredths() {
        return McrtxRuntimeSettings.getBlockOutlineEmissiveIntensityHundredths();
    }

    public static int getDisplacementFactorHundredths() {
        return McrtxRuntimeSettings.getDisplacementFactorHundredths();
    }

    public static int getSubsurfaceMeasurementDistanceHundredths() {
        return McrtxRuntimeSettings.getSubsurfaceMeasurementDistanceHundredths();
    }

    public static int getSubsurfaceRadiusScaleHundredths() {
        return McrtxRuntimeSettings.getSubsurfaceRadiusScaleHundredths();
    }

    public static int getSubsurfaceMaxSampleRadiusHundredths() {
        return McrtxRuntimeSettings.getSubsurfaceMaxSampleRadiusHundredths();
    }

    public static int getSubsurfaceVolumetricAnisotropyHundredths() {
        return McrtxRuntimeSettings.getSubsurfaceVolumetricAnisotropyHundredths();
    }

    public static int getWaterMaterialThicknessThousandths() {
        return McrtxRuntimeSettings.getWaterMaterialThicknessThousandths();
    }

    public static boolean isWaterThinWalledEnabled() {
        return McrtxRuntimeSettings.isWaterThinWalledEnabled();
    }

    public static boolean isSubsurfaceDiffusionProfileEnabled() {
        return McrtxRuntimeSettings.isSubsurfaceDiffusionProfileEnabled();
    }

    public static boolean isRemixAtmosphereCloudsEnabled() {
        return McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled();
    }

    public static boolean isGameRainParticlesEnabled() {
        return McrtxRuntimeSettings.isGameRainParticlesEnabled();
    }

    public static boolean shouldShowWaterMaterialThicknessSlider() {
        return McrtxHookSettingsUi.shouldShowWaterMaterialThicknessSlider();
    }

    public static boolean shouldShowBlockOutlineIntensitySlider() {
        return McrtxHookSettingsUi.shouldShowBlockOutlineIntensitySlider();
    }

    public static String getPlayerShadowsButtonLabel() {
        return McrtxHookSettingsUi.getPlayerShadowsButtonLabel();
    }

    public static String getHeldTorchLightsButtonLabel() {
        return McrtxHookSettingsUi.getHeldTorchLightsButtonLabel();
    }

    public static String getDynamicEntityRenderingButtonLabel() {
        return McrtxHookSettingsUi.getDynamicEntityRenderingButtonLabel();
    }

    public static String getLivingEntityRenderingButtonLabel() {
        return McrtxHookSettingsUi.getLivingEntityRenderingButtonLabel();
    }

    public static String getItemEntityRenderingButtonLabel() {
        return McrtxHookSettingsUi.getItemEntityRenderingButtonLabel();
    }

    public static String getPaintingVanillaSuppressionButtonLabel() {
        return McrtxHookSettingsUi.getPaintingVanillaSuppressionButtonLabel();
    }

    public static String getMovingPistonVanillaSuppressionButtonLabel() {
        return McrtxHookSettingsUi.getMovingPistonVanillaSuppressionButtonLabel();
    }

    public static String getWorldRasterVanillaSuppressionButtonLabel() {
        return McrtxHookSettingsUi.getWorldRasterVanillaSuppressionButtonLabel();
    }

    public static String getSignCaptureButtonLabel() {
        return McrtxHookSettingsUi.getSignCaptureButtonLabel();
    }

    public static String getSignTextCaptureButtonLabel() {
        return McrtxHookSettingsUi.getSignTextCaptureButtonLabel();
    }

    public static String getSignVanillaSuppressionButtonLabel() {
        return McrtxHookSettingsUi.getSignVanillaSuppressionButtonLabel();
    }

    public static String getRtQualityButtonLabel() {
        return McrtxHookSettingsUi.getRtQualityButtonLabel();
    }

    public static String getUpscalerButtonLabel() {
        return McrtxHookSettingsUi.getUpscalerButtonLabel();
    }

    public static String getUpscalerPresetButtonLabel() {
        return McrtxHookSettingsUi.getUpscalerPresetButtonLabel();
    }

    public static String getRayReconstructionButtonLabel() {
        return McrtxHookSettingsUi.getRayReconstructionButtonLabel();
    }

    public static String getSparseRenderingButtonLabel() {
        return McrtxHookSettingsUi.getSparseRenderingButtonLabel();
    }

    public static String getBlockOutlineButtonLabel() {
        return McrtxHookSettingsUi.getBlockOutlineButtonLabel();
    }

    public static String getBlockOutlineStyleButtonLabel() {
        return McrtxHookSettingsUi.getBlockOutlineStyleButtonLabel();
    }

    public static String getSubsurfaceDiffusionProfileButtonLabel() {
        return McrtxHookSettingsUi.getSubsurfaceDiffusionProfileButtonLabel();
    }

    public static String getWaterThinWallButtonLabel() {
        return McrtxHookSettingsUi.getWaterThinWallButtonLabel();
    }

    public static String getRemixAtmosphereCloudsButtonLabel() {
        return McrtxHookSettingsUi.getRemixAtmosphereCloudsButtonLabel();
    }

    public static String getGameRainParticlesButtonLabel() {
        return McrtxHookSettingsUi.getGameRainParticlesButtonLabel();
    }

    public static void setPlayerShadowsEnabled(boolean enabled) {
        McrtxHookSettingsUi.setPlayerShadowsEnabled(enabled);
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        McrtxHookSettingsUi.setHeldTorchLightsEnabled(enabled);
    }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) {
        McrtxHookSettingsUi.setDynamicEntityRenderingEnabled(enabled);
    }

    public static void setLivingEntityRenderingEnabled(boolean enabled) {
        McrtxHookSettingsUi.setLivingEntityRenderingEnabled(enabled);
    }

    public static void setItemEntityRenderingEnabled(boolean enabled) {
        McrtxHookSettingsUi.setItemEntityRenderingEnabled(enabled);
    }

    public static boolean isPaintingVanillaSuppressionEnabled() {
        return McrtxRuntimeSettings.isPaintingVanillaSuppressionEnabled();
    }

    public static boolean isMovingPistonVanillaSuppressionEnabled() {
        return McrtxRuntimeSettings.isMovingPistonVanillaSuppressionEnabled();
    }

    public static boolean isWorldRasterVanillaSuppressionEnabled() {
        return McrtxRuntimeSettings.isWorldRasterVanillaSuppressionEnabled();
    }

    public static boolean isSignCaptureEnabled() {
        return McrtxRuntimeSettings.isSignCaptureEnabled();
    }

    public static boolean isSignTextCaptureEnabled() {
        return McrtxRuntimeSettings.isSignTextCaptureEnabled();
    }

    public static boolean isSignVanillaSuppressionEnabled() {
        return McrtxRuntimeSettings.isSignVanillaSuppressionEnabled();
    }

    public static void setPaintingVanillaSuppressionEnabled(boolean enabled) {
        McrtxHookSettingsUi.setPaintingVanillaSuppressionEnabled(enabled);
    }

    public static void setMovingPistonVanillaSuppressionEnabled(boolean enabled) {
        McrtxHookSettingsUi.setMovingPistonVanillaSuppressionEnabled(enabled);
    }

    public static void setWorldRasterVanillaSuppressionEnabled(boolean enabled) {
        McrtxHookSettingsUi.setWorldRasterVanillaSuppressionEnabled(enabled);
    }

    public static void setSignCaptureEnabled(boolean enabled) {
        McrtxHookSettingsUi.setSignCaptureEnabled(enabled);
    }

    public static void setSignTextCaptureEnabled(boolean enabled) {
        McrtxHookSettingsUi.setSignTextCaptureEnabled(enabled);
    }

    public static void setSignVanillaSuppressionEnabled(boolean enabled) {
        McrtxHookSettingsUi.setSignVanillaSuppressionEnabled(enabled);
    }

    public static void setRemixAtmosphereCloudsEnabled(boolean enabled) {
        McrtxHookSettingsUi.setRemixAtmosphereCloudsEnabled(enabled);
    }

    public static void setGameRainParticlesEnabled(boolean enabled) {
        McrtxHookSettingsUi.setGameRainParticlesEnabled(enabled);
    }

    public static boolean isSparseRenderingEnabled() {
        return McrtxRuntimeSettings.isSparseRenderingEnabled();
    }

    public static void setGameplayFovDegrees(int fovDegrees) {
        McrtxHookSettingsUi.setGameplayFovDegrees(fovDegrees);
    }

    public static void setViewModelFovDegrees(int fovDegrees) {
        McrtxHookSettingsUi.setViewModelFovDegrees(fovDegrees);
    }

    public static void setNoCullDistanceBlocks(int blockDistance) {
        McrtxHookSettingsUi.setNoCullDistanceBlocks(blockDistance);
    }

    public static void setBlockOutlineEnabled(boolean enabled) {
        McrtxHookSettingsUi.setBlockOutlineEnabled(enabled);
    }

    public static void setBlockOutlineEmissiveIntensityHundredths(int intensityHundredths) {
        McrtxHookSettingsUi.setBlockOutlineEmissiveIntensityHundredths(intensityHundredths);
    }

    public static void setDisplacementFactorHundredths(int factorHundredths) {
        McrtxHookSettingsUi.setDisplacementFactorHundredths(factorHundredths);
    }

    public static void setSubsurfaceMeasurementDistanceHundredths(int distanceHundredths) {
        McrtxHookSettingsUi.setSubsurfaceMeasurementDistanceHundredths(distanceHundredths);
    }

    public static void setSubsurfaceRadiusScaleHundredths(int scaleHundredths) {
        McrtxHookSettingsUi.setSubsurfaceRadiusScaleHundredths(scaleHundredths);
    }

    public static void setSubsurfaceMaxSampleRadiusHundredths(int radiusHundredths) {
        McrtxHookSettingsUi.setSubsurfaceMaxSampleRadiusHundredths(radiusHundredths);
    }

    public static void setSubsurfaceVolumetricAnisotropyHundredths(int anisotropyHundredths) {
        McrtxHookSettingsUi.setSubsurfaceVolumetricAnisotropyHundredths(anisotropyHundredths);
    }

    public static void setSubsurfaceDiffusionProfileEnabled(boolean enabled) {
        McrtxHookSettingsUi.setSubsurfaceDiffusionProfileEnabled(enabled);
    }

    public static void setWaterThinWalledEnabled(boolean enabled) {
        McrtxHookSettingsUi.setWaterThinWalledEnabled(enabled);
    }

    public static void setWaterMaterialThicknessThousandths(int thicknessThousandths) {
        McrtxHookSettingsUi.setWaterMaterialThicknessThousandths(thicknessThousandths);
    }

    public static void resetSubsurfaceSettingsToDefaults() {
        McrtxHookSettingsUi.resetSubsurfaceSettingsToDefaults();
    }

    public static void cycleBlockOutlineStyle() {
        McrtxHookSettingsUi.cycleBlockOutlineStyle();
    }

    public static void cycleRtQuality() {
        McrtxHookSettingsUi.cycleRtQuality();
    }

    public static void cycleUpscalerType() {
        McrtxHookSettingsUi.cycleUpscalerType();
    }

    public static void cycleUpscalerPreset() {
        McrtxHookSettingsUi.cycleUpscalerPreset();
    }

    public static boolean shouldShowRayReconstructionOption() {
        return McrtxHookSettingsUi.shouldShowRayReconstructionOption();
    }

    public static boolean shouldShowSparseRenderingOption() {
        return McrtxHookSettingsUi.shouldShowSparseRenderingOption();
    }

    public static void toggleRayReconstructionEnabled() {
        McrtxHookSettingsUi.toggleRayReconstructionEnabled();
    }

    public static void toggleSparseRenderingEnabled() {
        McrtxHookSettingsUi.toggleSparseRenderingEnabled();
    }

    private static void resetRemixUiTracking() {
        remixUiOpen = false;
        remixUiHotkeyHeld = false;
        remixUiHotkeyLocked = false;
        remixUiLastAltHotkeyDown = false;
        remixUiLastXHotkeyDown = false;
        remixUiHotkeyReleaseStartedNanos = 0L;
        quickSettingsHotkeyHeld = false;
        quickSettingsHotkeyLocked = false;
        quickSettingsLastAltHotkeyDown = false;
        quickSettingsLastBHotkeyDown = false;
        quickSettingsHotkeyReleaseStartedNanos = 0L;
        preferredRemixUiState = DEFAULT_REMIX_UI_STATE;
        MinecraftRenderHooks.setRemixUiInputActive(false);
    }

    private static boolean shouldSuppressWorldRasterVanillaDraw() {
        return McrtxRuntimeSettings.isWorldRasterVanillaSuppressionEnabled()
                && worldRasterRenderActive
                && !RemixUiCapture.isActive()
                && !RemixDynamicEntityCapture.isFirstPersonActive()
                && !RemixParticleCapture.isWeatherTessellatorCaptureActive();
    }

    private static boolean shouldSuppressCapturedVanillaTessellatorDraw() {
        return RemixUiCapture.isActive()
                || RemixDynamicEntityCapture.shouldSuppressVanillaTessellatorDraw()
                || RemixParticleCapture.shouldSuppressVanillaTessellatorDraw();
    }

    public static void onFrameRenderStart() {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onFrameRenderStart();
            Minecraft minecraft = MinecraftRenderHooks.getRememberedMinecraft();
            if (minecraft != null && minecraft.f != null && minecraft.f.t != null) {
                MinecraftRenderHooks.updateAtmosphereState(
                        minecraft.f.b(lastCameraPartialTicks),
                        minecraft.f.t instanceof wd);
            }
            McrtxHookPerfTracker.onFrameRenderStart();
        } finally {
            HookProfiler.endHook("hook.onFrameRenderStart", __perf);
        }
    }

    private static boolean detectStandaloneWindowMode() {
        String configuredMode = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_WINDOW_MODE");
        return configuredMode != null
                && configuredMode.equalsIgnoreCase("standalone");
    }

    private static boolean detectSingleNativeWindowMode() {
        String configuredMode = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_WINDOW_MODE");
        return configuredMode != null
                && configuredMode.equalsIgnoreCase("single-native");
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

    private static String formatMillis(long nanos) {
        return String.format(java.util.Locale.ROOT, "%.2f", (double) nanos / 1000000.0);
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

    private static boolean isBHotkeyDown(MinecraftPlatform platform) {
        if (NATIVE_INPUT_BACKEND && MinecraftRenderHooks.isInitialized()) {
            return MinecraftRenderHooks.isNativeVirtualKeyDown(WINDOWS_VK_B);
        }
        return platform.isKeyDown(MinecraftPlatformKey.B);
    }

    private static void syncQuickSettingsInput(net.minecraft.client.Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }

        MinecraftPlatform platform = MinecraftPlatformRuntime.current();
        boolean altDown = isAltHotkeyDown(platform);
        boolean bDown = isBHotkeyDown(platform);
        boolean hotkeyHeld = altDown && bDown;
        boolean hotkeyFullyReleased = !altDown && !bDown;
        boolean quickPanelOpen = minecraft.r instanceof McrtxQuickSettingsScreen;
        boolean canToggle = minecraft.r == null || quickPanelOpen;
        long nowNanos = System.nanoTime();

        if (!hotkeyFullyReleased) {
            quickSettingsHotkeyReleaseStartedNanos = 0L;
        }

        if (hotkeyHeld && !quickSettingsHotkeyHeld && !quickSettingsHotkeyLocked && canToggle) {
            if (quickPanelOpen) {
                minecraft.a((da) null);
            } else {
                minecraft.a(new McrtxQuickSettingsScreen());
            }
            quickSettingsHotkeyLocked = true;
        } else if (hotkeyFullyReleased && quickSettingsHotkeyLocked) {
            if (quickSettingsHotkeyReleaseStartedNanos == 0L) {
                quickSettingsHotkeyReleaseStartedNanos = nowNanos;
            } else if (nowNanos - quickSettingsHotkeyReleaseStartedNanos >= REMIX_UI_HOTKEY_RELEASE_DEBOUNCE_NANOS) {
                quickSettingsHotkeyLocked = false;
            }
        }

        quickSettingsLastAltHotkeyDown = altDown;
        quickSettingsLastBHotkeyDown = bDown;
        quickSettingsHotkeyHeld = hotkeyHeld;
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
