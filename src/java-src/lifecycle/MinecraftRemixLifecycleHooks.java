import java.io.File;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxRuntimeConfig;
import mcrtx.bridge.MinecraftPlatform;
import mcrtx.bridge.MinecraftPlatformKey;
import mcrtx.bridge.MinecraftPlatformRuntime;
import mcrtx.bridge.RemixLifecycleBridge;
import mcrtx.bridge.UiOverlayCapture;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

public final class MinecraftRemixLifecycleHooks {
    private static final int DEFAULT_REMIX_UI_STATE = RemixLifecycleBridge.REMIX_UI_STATE_ADVANCED;
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

    static {
        System.out.println("[mcrtx] MinecraftRemixHooks loaded");
    }

    private MinecraftRemixLifecycleHooks() {
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
            RemixLifecycleBridge.initializeForCurrentDisplay(width, height);
            McrtxSettingsCategories.applySavedSettings();
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
            RemixLifecycleBridge.shutdown();
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
                RemixLifecycleBridge.resize(width, height);
            } else {
                RemixLifecycleBridge.reinitializeForCurrentDisplay(width, height);
            }
            McrtxSettingsCategories.applySavedSettings();
        } finally {
            HookProfiler.endHook("hook.onDisplayReset", __perf);
        }
    }

    public static void onResize(int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            RemixLifecycleBridge.resize(width, height);
        } finally {
            HookProfiler.endHook("hook.onResize", __perf);
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
            HookProfiler.flushAll();
            long profilerFlushEndNanos = System.nanoTime();
            RemixLifecycleBridge.present();
            long presentEndNanos = System.nanoTime();
            RemixDynamicEntityCapture.onFramePresented();
            RemixCameraState.onFramePresented();
            long frameEndNanos = System.nanoTime();
            long prePresentNanos = renderMethodStartNanos > 0L
                    ? Math.max(0L, frameStartNanos - renderMethodStartNanos)
                    : 0L;
            long renderMethodNanos = renderMethodStartNanos > 0L
                    ? Math.max(0L, frameEndNanos - renderMethodStartNanos)
                    : 0L;
            McrtxHookPerfTracker.clearRenderMethodStartNanos();
            activeUiRenderBeginNanos = 0L;

            HookProfiler.record(
                    HookProfiler.SIDE_HOOK,
                    "hook.onPresent.chunkFlush",
                    flushEndNanos - frameStartNanos);
            HookProfiler.record(
                    HookProfiler.SIDE_HOOK,
                    "hook.onPresent.profilerFlush",
                    profilerFlushEndNanos - flushEndNanos);
            HookProfiler.record(
                    HookProfiler.SIDE_HOOK,
                    "hook.onPresent.present",
                    presentEndNanos - profilerFlushEndNanos);
            HookProfiler.record(
                    HookProfiler.SIDE_HOOK,
                    "hook.onPresent.post",
                    frameEndNanos - presentEndNanos);
            if (renderMethodStartNanos > 0L) {
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

    public static void onRemixUiTick(Minecraft minecraft) {
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

    public static void onFrameRenderStart() {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onFrameRenderStart();
            MinecraftRemixSceneHooks.updateAtmosphereForFrameStart();
            McrtxHookPerfTracker.onFrameRenderStart();
        } finally {
            HookProfiler.endHook("hook.onFrameRenderStart", __perf);
        }
    }

    static void markUiRenderBegin() {
        activeUiRenderBeginNanos = System.nanoTime();
    }

    static boolean isStandaloneWindowMode() {
        return STANDALONE_WINDOW_MODE;
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
        RemixLifecycleBridge.setRemixUiInputActive(false);
    }

    private static boolean detectStandaloneWindowMode() {
        String configuredMode = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_WINDOW_MODE");
        return configuredMode != null && configuredMode.equalsIgnoreCase("standalone");
    }

    private static boolean detectSingleNativeWindowMode() {
        String configuredMode = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_WINDOW_MODE");
        return configuredMode != null && configuredMode.equalsIgnoreCase("single-native");
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
        if (NATIVE_INPUT_BACKEND
                && RemixLifecycleBridge.isInitialized()
                && RemixLifecycleBridge.hasNativeWindowFocus()) {
            return true;
        }
        if (Display.isActive()) {
            return true;
        }
        return MinecraftPlatformRuntime.current().isWindowActive();
    }

    private static boolean isAltHotkeyDown(MinecraftPlatform platform) {
        if (NATIVE_INPUT_BACKEND && RemixLifecycleBridge.isInitialized()) {
            return RemixLifecycleBridge.isNativeVirtualKeyDown(WINDOWS_VK_MENU);
        }
        return platform.isKeyDown(MinecraftPlatformKey.LEFT_ALT)
                || platform.isKeyDown(MinecraftPlatformKey.RIGHT_ALT);
    }

    private static boolean isXHotkeyDown(MinecraftPlatform platform) {
        if (NATIVE_INPUT_BACKEND && RemixLifecycleBridge.isInitialized()) {
            return RemixLifecycleBridge.isNativeVirtualKeyDown(WINDOWS_VK_X);
        }
        return platform.isKeyDown(MinecraftPlatformKey.X);
    }

    private static boolean isBHotkeyDown(MinecraftPlatform platform) {
        if (NATIVE_INPUT_BACKEND && RemixLifecycleBridge.isInitialized()) {
            return RemixLifecycleBridge.isNativeVirtualKeyDown(WINDOWS_VK_B);
        }
        return platform.isKeyDown(MinecraftPlatformKey.B);
    }

    private static void syncQuickSettingsInput(Minecraft minecraft) {
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
            } else if (nowNanos - quickSettingsHotkeyReleaseStartedNanos
                    >= REMIX_UI_HOTKEY_RELEASE_DEBOUNCE_NANOS) {
                quickSettingsHotkeyLocked = false;
            }
        }

        quickSettingsLastAltHotkeyDown = altDown;
        quickSettingsLastBHotkeyDown = bDown;
        quickSettingsHotkeyHeld = hotkeyHeld;
    }

    private static boolean syncRemixUiInput(Minecraft minecraft, boolean allowHotkeyToggle) {
        MinecraftPlatform platform = MinecraftPlatformRuntime.current();
        int uiState = RemixLifecycleBridge.getUiState();
        boolean manualHotkeyToggleEnabled = allowHotkeyToggle && !NATIVE_INPUT_BACKEND;
        boolean altDown = isAltHotkeyDown(platform);
        boolean xDown = isXHotkeyDown(platform);
        boolean hotkeyHeld = altDown && xDown;
        boolean hotkeyFullyReleased = !altDown && !xDown;
        long nowNanos = System.nanoTime();

        if (altDown != remixUiLastAltHotkeyDown
                || xDown != remixUiLastXHotkeyDown
                || hotkeyHeld != remixUiHotkeyHeld) {
            logRemixUiHotkeyEvent(
                    "poll-change",
                    altDown,
                    xDown,
                    hotkeyHeld,
                    manualHotkeyToggleEnabled,
                    uiState,
                    nowNanos);
        }

        if (!hotkeyFullyReleased) {
            if (remixUiHotkeyReleaseStartedNanos != 0L) {
                logRemixUiHotkeyEvent(
                        "release-cancelled",
                        altDown,
                        xDown,
                        hotkeyHeld,
                        manualHotkeyToggleEnabled,
                        uiState,
                        nowNanos);
            }
            remixUiHotkeyReleaseStartedNanos = 0L;
        }

        if (manualHotkeyToggleEnabled && hotkeyHeld && !remixUiHotkeyHeld && !remixUiHotkeyLocked) {
            int targetState = uiState == RemixLifecycleBridge.REMIX_UI_STATE_NONE
                    ? preferredRemixUiState
                    : RemixLifecycleBridge.REMIX_UI_STATE_NONE;
            if (RemixLifecycleBridge.setUiState(targetState)) {
                uiState = targetState;
                remixUiHotkeyLocked = true;
                logRemixUiHotkeyEvent(
                        "toggle-success",
                        altDown,
                        xDown,
                        hotkeyHeld,
                        manualHotkeyToggleEnabled,
                        uiState,
                        nowNanos);
                System.out.println("[mcrtx] Remix UI hotkey toggled state=" + uiState);
            } else {
                logRemixUiHotkeyEvent(
                        "toggle-failed",
                        altDown,
                        xDown,
                        hotkeyHeld,
                        manualHotkeyToggleEnabled,
                        uiState,
                        nowNanos);
                System.out.println("[mcrtx] Remix UI hotkey failed: " + RemixLifecycleBridge.lastError());
            }
        } else if (manualHotkeyToggleEnabled
                && hotkeyHeld
                && !remixUiHotkeyHeld
                && remixUiHotkeyLocked) {
            logRemixUiHotkeyEvent(
                    "suppressed-rising-edge",
                    altDown,
                    xDown,
                    hotkeyHeld,
                    manualHotkeyToggleEnabled,
                    uiState,
                    nowNanos);
        } else if (hotkeyFullyReleased && remixUiHotkeyLocked) {
            if (remixUiHotkeyReleaseStartedNanos == 0L) {
                remixUiHotkeyReleaseStartedNanos = nowNanos;
                logRemixUiHotkeyEvent(
                        "release-timer-start",
                        altDown,
                        xDown,
                        hotkeyHeld,
                        manualHotkeyToggleEnabled,
                        uiState,
                        nowNanos);
            } else if (nowNanos - remixUiHotkeyReleaseStartedNanos
                    >= REMIX_UI_HOTKEY_RELEASE_DEBOUNCE_NANOS) {
                remixUiHotkeyLocked = false;
                logRemixUiHotkeyEvent(
                        "release-unlocked",
                        altDown,
                        xDown,
                        hotkeyHeld,
                        manualHotkeyToggleEnabled,
                        uiState,
                        nowNanos);
            }
        }

        remixUiLastAltHotkeyDown = altDown;
        remixUiLastXHotkeyDown = xDown;
        remixUiHotkeyHeld = hotkeyHeld;
        if (uiState != RemixLifecycleBridge.REMIX_UI_STATE_NONE) {
            preferredRemixUiState = uiState;
        }

        boolean uiOpen = uiState != RemixLifecycleBridge.REMIX_UI_STATE_NONE;
        RemixLifecycleBridge.setRemixUiInputActive(uiOpen);
        if (minecraft != null) {
            if (uiOpen) {
                minecraft.h();
            } else if (remixUiOpen && minecraft.r == null) {
                RemixLifecycleBridge.restoreIngameFocusIfNeeded();
            }
        }

        remixUiOpen = uiOpen;
        return uiOpen;
    }
}
