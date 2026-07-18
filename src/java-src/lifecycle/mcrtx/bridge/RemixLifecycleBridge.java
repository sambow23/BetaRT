package mcrtx.bridge;

import java.awt.Canvas;
import net.minecraft.client.Minecraft;

public final class RemixLifecycleBridge {
    public static final int REMIX_UI_STATE_NONE = RemixBridgeNative.REMIX_UI_STATE_NONE;
    public static final int REMIX_UI_STATE_BASIC = RemixBridgeNative.REMIX_UI_STATE_BASIC;
    public static final int REMIX_UI_STATE_ADVANCED = RemixBridgeNative.REMIX_UI_STATE_ADVANCED;

    private static volatile boolean initialized;
    private static volatile boolean remixUiInputActive;
    private static Minecraft currentMinecraft;
    private static String lastError = "";
    private static String lastReportedMessage = "";

    private RemixLifecycleBridge() {
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
        initialized = nInitialize(hwnd, width, height);
        if (!initialized) {
            lastError = nGetLastError();
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
        nShutdown();
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
                nFocusWindow(hwnd);
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
        if (initialized) {
            nResize(width, height);
        }
    }

    public static synchronized int getUiState() {
        return initialized ? nGetUiState() : REMIX_UI_STATE_NONE;
    }

    public static synchronized boolean setUiState(int state) {
        if (!initialized) {
            return false;
        }
        boolean result = nSetUiState(state);
        lastError = result ? "" : nGetLastError();
        return result;
    }

    public static synchronized boolean hasNativeWindowFocus() {
        return initialized && nHasWindowFocus();
    }

    public static synchronized boolean isNativeVirtualKeyDown(int virtualKey) {
        return initialized && nIsVirtualKeyDown(virtualKey);
    }

    public static synchronized boolean present() {
        return initialized && nPresent();
    }

    public static synchronized boolean requestPresentedScreenshot(String absolutePath) {
        if (!initialized || absolutePath == null || absolutePath.length() == 0) {
            return false;
        }
        boolean accepted = nRequestPresentedScreenshot(absolutePath);
        lastError = accepted ? "" : nGetLastError();
        return accepted;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static synchronized String lastError() {
        if (!lastError.isEmpty()) {
            return lastError;
        }
        if (!RemixBridgeNative.isAvailable()) {
            return RemixBridgeNative.loadError();
        }
        return nGetLastError();
    }

    public static long resolveAwtWindowHandle(Canvas canvas) {
        return RemixBridgeNative.isAvailable() ? nResolveAwtWindowHandle(canvas) : 0L;
    }

    public static boolean embedCompatibilityWindow(long childHwnd, long parentHwnd, int width, int height) {
        return RemixBridgeNative.isAvailable()
                && nEmbedCompatibilityWindow(childHwnd, parentHwnd, width, height);
    }

    public static boolean focusWindow(long hwnd) {
        return RemixBridgeNative.isAvailable() && nFocusWindow(hwnd);
    }

    public static boolean isEmbeddedWindowActive(long childHwnd, long parentHwnd) {
        return RemixBridgeNative.isAvailable() && nIsEmbeddedWindowActive(childHwnd, parentHwnd);
    }

    public static boolean isOutputCloseRequested() {
        return RemixBridgeNative.isAvailable() && nIsOutputCloseRequested();
    }

    public static int getOutputWindowWidth() {
        return RemixBridgeNative.isAvailable() ? nGetOutputWindowWidth() : 0;
    }

    public static int getOutputWindowHeight() {
        return RemixBridgeNative.isAvailable() ? nGetOutputWindowHeight() : 0;
    }

    public static boolean setOutputWindowFullscreen(boolean fullscreen) {
        return RemixBridgeNative.isAvailable() && nSetOutputWindowFullscreen(fullscreen);
    }

    public static boolean pollNativeMouseState(int[] stateOut) {
        return RemixBridgeNative.isAvailable() && nPollNativeMouseState(stateOut);
    }

    public static boolean setNativeMouseGrabbed(boolean grabbed) {
        return RemixBridgeNative.isAvailable() && nSetNativeMouseGrabbed(grabbed);
    }

    public static boolean setNativeCursorPosition(int x, int y) {
        return RemixBridgeNative.isAvailable() && nSetNativeCursorPosition(x, y);
    }

    private static void report(String message) {
        if (message == null || message.isEmpty() || message.equals(lastReportedMessage)) {
            return;
        }
        lastReportedMessage = message;
        System.out.println("[mcrtx] " + message);
    }

    private static native boolean nInitialize(long hwnd, int width, int height);
    private static native long nResolveAwtWindowHandle(Canvas canvas);
    private static native boolean nEmbedCompatibilityWindow(long childHwnd, long parentHwnd, int width, int height);
    private static native boolean nFocusWindow(long hwnd);
    private static native boolean nIsEmbeddedWindowActive(long childHwnd, long parentHwnd);
    private static native void nShutdown();
    private static native void nResize(int width, int height);
    private static native int nGetUiState();
    private static native boolean nSetUiState(int state);
    private static native boolean nHasWindowFocus();
    private static native boolean nIsOutputCloseRequested();
    private static native int nGetOutputWindowWidth();
    private static native int nGetOutputWindowHeight();
    private static native boolean nSetOutputWindowFullscreen(boolean fullscreen);
    private static native boolean nIsVirtualKeyDown(int virtualKey);
    private static native boolean nPollNativeMouseState(int[] stateOut);
    private static native boolean nSetNativeMouseGrabbed(boolean grabbed);
    private static native boolean nSetNativeCursorPosition(int x, int y);
    private static native boolean nPresent();
    private static native boolean nRequestPresentedScreenshot(String absolutePath);
    private static native String nGetLastError();
}
