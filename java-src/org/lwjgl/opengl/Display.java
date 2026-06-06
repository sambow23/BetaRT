package org.lwjgl.opengl;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import mcrtx.bridge.McrtxRuntimeConfig;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.RemixBridgeNative;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import mcrtx.lwjglshim.GlfwBindings;

public final class Display {
    private static final int GLFW_FALSE = 0;
    private static final int GLFW_TRUE = 1;
    private static final int GLFW_VISIBLE = 0x00020004;
    private static final int GLFW_RESIZABLE = 0x00020003;
    private static final int GLFW_FOCUSED = 0x00020001;
    private static final boolean SINGLE_NATIVE_WINDOW_MODE = detectSingleNativeWindowMode();

    private static final Object LOCK = new Object();
    private static final GlfwBindings BINDINGS = GlfwBindings.get();

    private static DisplayMode requestedMode = getDesktopDisplayMode();
    private static String title = "Minecraft";
    private static Canvas parent;
    private static boolean fullscreen;
    private static boolean created;
    private static boolean closeRequested;
    private static boolean active;
    private static boolean embeddedParentWindow;
    private static long embeddedChildWindowHandle;
    private static long embeddedParentWindowHandle;
    private static long windowHandle;
    private static int windowedWidth;
    private static int windowedHeight;
    private static int windowedX;
    private static int windowedY;
    private static Object keyCallback;
    private static Object mouseButtonCallback;
    private static Object cursorPositionCallback;
    private static Object scrollCallback;
    private static Object focusCallback;

    private Display() {
    }

    public static void create() throws LWJGLException {
        synchronized (LOCK) {
            if (created) {
                return;
            }
            if (!BINDINGS.isAvailable()) {
                throw new LWJGLException("LWJGL 3 GLFW bindings are unavailable: " + BINDINGS.initError());
            }

            try {
                if (!BINDINGS.init()) {
                    throw new LWJGLException("glfwInit returned false");
                }
                BINDINGS.defaultWindowHints();
                BINDINGS.windowHint(GLFW_VISIBLE, shouldHideCompatibilityHost() ? GLFW_FALSE : GLFW_TRUE);
                BINDINGS.windowHint(GLFW_RESIZABLE, GLFW_TRUE);

                DisplayMode mode = initialDisplayMode();
                long monitor = fullscreen ? BINDINGS.getPrimaryMonitor() : 0L;
                windowHandle = BINDINGS.createWindow(mode.getWidth(), mode.getHeight(), title, monitor, 0L);
                if (windowHandle == 0L) {
                    throw new LWJGLException("glfwCreateWindow returned null");
                }

                BINDINGS.makeContextCurrent(windowHandle);
                BINDINGS.createCapabilities();
                embeddedParentWindow = attachToParentCanvas(mode);
                if (shouldHideCompatibilityHost()) {
                    BINDINGS.hideWindow(windowHandle);
                } else if (!embeddedParentWindow) {
                    BINDINGS.showWindow(windowHandle);
                }
                installCallbacks();
                created = true;
                active = true;
                closeRequested = false;
                windowedWidth = mode.getWidth();
                windowedHeight = mode.getHeight();
                Mouse.updateWindowHeight(mode.getHeight());
                Keyboard.create();
                Mouse.create();
            } catch (LWJGLException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new LWJGLException("Failed to create the GLFW compatibility display", exception);
            }
        }
    }

    public static void destroy() {
        synchronized (LOCK) {
            if (!created) {
                return;
            }
            try {
                BINDINGS.destroyWindow(windowHandle);
                BINDINGS.terminate();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to destroy the GLFW compatibility display", exception);
            } finally {
                created = false;
                closeRequested = false;
                active = false;
                embeddedParentWindow = false;
                embeddedChildWindowHandle = 0L;
                embeddedParentWindowHandle = 0L;
                windowHandle = 0L;
                keyCallback = null;
                mouseButtonCallback = null;
                cursorPositionCallback = null;
                scrollCallback = null;
                focusCallback = null;
                Keyboard.destroy();
                Mouse.destroy();
            }
        }
    }

    public static DisplayMode getDesktopDisplayMode() {
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new DisplayMode(screenSize.width, screenSize.height);
    }

    public static DisplayMode getDisplayMode() {
        synchronized (LOCK) {
            if (!created) {
                return requestedMode;
            }
            try {
                int[] windowSize = BINDINGS.getWindowSize(windowHandle);
                return new DisplayMode(windowSize[0], windowSize[1]);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to query the GLFW window size", exception);
            }
        }
    }

    public static boolean isActive() {
        synchronized (LOCK) {
            return created && currentActiveState();
        }
    }

    public static boolean isCloseRequested() {
        synchronized (LOCK) {
            return closeRequested;
        }
    }

    public static void setDisplayMode(DisplayMode mode) {
        synchronized (LOCK) {
            requestedMode = mode;
            if (!created || mode == null) {
                return;
            }
            try {
                BINDINGS.setWindowSize(windowHandle, mode.getWidth(), mode.getHeight());
                Mouse.updateWindowHeight(mode.getHeight());
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to resize the GLFW compatibility display", exception);
            }
        }
    }

    public static void setFullscreen(boolean shouldBeFullscreen) {
        synchronized (LOCK) {
            fullscreen = shouldBeFullscreen;
            if (!created) {
                return;
            }
            try {
                if (shouldBeFullscreen) {
                    DisplayMode mode = getDesktopDisplayMode();
                    windowedWidth = getDisplayMode().getWidth();
                    windowedHeight = getDisplayMode().getHeight();
                    BINDINGS.setWindowMonitor(windowHandle, BINDINGS.getPrimaryMonitor(), 0, 0, mode.getWidth(), mode.getHeight(), 0);
                    Mouse.updateWindowHeight(mode.getHeight());
                } else {
                    int width = windowedWidth > 0 ? windowedWidth : requestedMode.getWidth();
                    int height = windowedHeight > 0 ? windowedHeight : requestedMode.getHeight();
                    BINDINGS.setWindowMonitor(windowHandle, 0L, windowedX, windowedY, width, height, 0);
                    Mouse.updateWindowHeight(height);
                }
                syncCompatibilityHostVisibility();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to toggle GLFW fullscreen mode", exception);
            }
        }
    }

    public static void setParent(Canvas parentCanvas) {
        synchronized (LOCK) {
            parent = parentCanvas;
        }
        if (SINGLE_NATIVE_WINDOW_MODE && parentCanvas != null) {
            hideSingleNativeAwtHost();
            parentCanvas.addHierarchyListener(event -> {
                if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    hideSingleNativeAwtHost();
                }
            });
        }
    }

    private static DisplayMode initialDisplayMode() {
        if (parent != null) {
            int parentWidth = parent.getWidth();
            int parentHeight = parent.getHeight();
            if (parentWidth > 0 && parentHeight > 0) {
                return new DisplayMode(parentWidth, parentHeight);
            }
        }

        return requestedMode == null ? getDesktopDisplayMode() : requestedMode;
    }

    public static void setTitle(String newTitle) {
        synchronized (LOCK) {
            title = newTitle == null ? "Minecraft" : newTitle;
            if (!created) {
                return;
            }
            try {
                BINDINGS.setWindowTitle(windowHandle, title);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to update the GLFW window title", exception);
            }
        }
    }

    public static void swapBuffers() {
        synchronized (LOCK) {
            if (!created) {
                return;
            }
            try {
                BINDINGS.swapBuffers(windowHandle);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to swap GLFW buffers", exception);
            }
        }
    }

    public static void update() {
        synchronized (LOCK) {
            if (!created) {
                return;
            }
            try {
                syncCompatibilityHostVisibility();
                syncEmbeddedParentSize();
                restoreEmbeddedFocusIfNeeded();
                BINDINGS.pollEvents();
                int[] windowSize = BINDINGS.getWindowSize(windowHandle);
                Mouse.updateWindowHeight(windowSize[1]);
                if (SINGLE_NATIVE_WINDOW_MODE) {
                    Keyboard.pollNativeState();
                    Mouse.pollNativeState();
                    syncSingleNativeWindowSize();
                    if (RemixBridgeNative.isAvailable() && RemixBridgeNative.nIsOutputCloseRequested()) {
                        MinecraftRenderHooks.requestShutdown();
                    }
                }
                closeRequested = BINDINGS.windowShouldClose(windowHandle);
                active = currentActiveState();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to poll GLFW events", exception);
            }
        }
    }

    public static void requestInputFocus() {
        synchronized (LOCK) {
            if (SINGLE_NATIVE_WINDOW_MODE) {
                requestSingleNativeInputFocus();
                return;
            }
            requestEmbeddedInputFocus(true);
        }
    }

    public static GlfwBindings bindings() {
        return BINDINGS;
    }

    public static long windowHandle() {
        synchronized (LOCK) {
            return windowHandle;
        }
    }

    public static long resolveNativePresentationWindowHandle() {
        synchronized (LOCK) {
            if (!created || !RemixBridgeNative.isAvailable()) {
                return 0L;
            }

            if (parent != null) {
                long parentWindow = RemixBridgeNative.nResolveAwtWindowHandle(parent);
                if (parentWindow != 0L) {
                    return parentWindow;
                }
            }

            if (windowHandle == 0L) {
                return 0L;
            }

            try {
                return BINDINGS.getWin32Window(windowHandle);
            } catch (Exception exception) {
                return 0L;
            }
        }
    }

    public static boolean isSingleNativeWindowMode() {
        return SINGLE_NATIVE_WINDOW_MODE;
    }

    private static void requestSingleNativeInputFocus() {
        if (!RemixBridgeNative.isAvailable()) {
            return;
        }

        if (parent != null) {
            parent.requestFocusInWindow();
            Window owningWindow = findOwningWindow(parent);
            if (owningWindow != null) {
                owningWindow.requestFocus();
            }
        }

        long presentationWindow = resolveNativePresentationWindowHandle();
        if (presentationWindow != 0L) {
            RemixBridgeNative.nFocusWindow(presentationWindow);
        }
    }

    private static boolean attachToParentCanvas(DisplayMode mode) throws Exception {
        if (SINGLE_NATIVE_WINDOW_MODE || parent == null || !RemixBridgeNative.isAvailable()) {
            return false;
        }

        long parentWindow = RemixBridgeNative.nResolveAwtWindowHandle(parent);
        long childWindow = BINDINGS.getWin32Window(windowHandle);
        if (parentWindow == 0L || childWindow == 0L) {
            return false;
        }

        if (!RemixBridgeNative.nEmbedCompatibilityWindow(childWindow, parentWindow, mode.getWidth(), mode.getHeight())) {
            return false;
        }

        embeddedChildWindowHandle = childWindow;
        embeddedParentWindowHandle = parentWindow;
        return true;
    }

    private static void syncEmbeddedParentSize() throws Exception {
        if (!embeddedParentWindow || parent == null) {
            return;
        }

        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();
        if (parentWidth <= 0 || parentHeight <= 0) {
            return;
        }

        int[] windowSize = BINDINGS.getWindowSize(windowHandle);
        if (windowSize[0] == parentWidth && windowSize[1] == parentHeight) {
            return;
        }

        BINDINGS.setWindowSize(windowHandle, parentWidth, parentHeight);
        windowedWidth = parentWidth;
        windowedHeight = parentHeight;
        Mouse.updateWindowHeight(parentHeight);
    }

    private static void restoreEmbeddedFocusIfNeeded() {
        requestEmbeddedInputFocus(false);
    }

    private static void requestEmbeddedInputFocus(boolean force) {
        if (!embeddedParentWindow || parent == null || windowHandle == 0L) {
            return;
        }

        if (!force && active) {
            return;
        }

        boolean parentWindowActive = isParentWindowActive();
        boolean nativeWindowActive = RemixBridgeNative.isAvailable() && RemixBridgeNative.nHasWindowFocus();
        if (!parentWindowActive && !nativeWindowActive) {
            return;
        }

        try {
            parent.requestFocusInWindow();
            Window owningWindow = findOwningWindow(parent);
            if (owningWindow != null) {
                owningWindow.requestFocus();
            }

            long childWindow = embeddedChildWindowHandle != 0L
                    ? embeddedChildWindowHandle
                    : BINDINGS.getWin32Window(windowHandle);
            if (childWindow != 0L && RemixBridgeNative.nFocusWindow(childWindow)) {
                active = true;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to restore focus to the embedded GLFW compatibility window", exception);
        }
    }

    private static boolean currentActiveState() {
        if (!created) {
            return false;
        }

        try {
            if (SINGLE_NATIVE_WINDOW_MODE) {
                if (RemixBridgeNative.isAvailable() && RemixBridgeNative.nHasWindowFocus()) {
                    return true;
                }
                return BINDINGS.isFocused(windowHandle, GLFW_FOCUSED);
            }

            if (!embeddedParentWindow) {
                return BINDINGS.isFocused(windowHandle, GLFW_FOCUSED);
            }

            if (RemixBridgeNative.isAvailable() && RemixBridgeNative.nHasWindowFocus()) {
                return true;
            }

            if (embeddedChildWindowHandle == 0L || embeddedParentWindowHandle == 0L) {
                return isParentWindowActive();
            }

            return RemixBridgeNative.nIsEmbeddedWindowActive(embeddedChildWindowHandle, embeddedParentWindowHandle);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to query GLFW compatibility window focus state", exception);
        }
    }

    private static boolean isParentWindowActive() {
        if (parent == null) {
            return false;
        }

        if (parent.isFocusOwner() || parent.hasFocus()) {
            return true;
        }

        Window owningWindow = findOwningWindow(parent);
        if (owningWindow == null) {
            return false;
        }

        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        return owningWindow == focusManager.getActiveWindow() || owningWindow == focusManager.getFocusedWindow();
    }

    private static Window findOwningWindow(Component component) {
        Component current = component;
        while (current != null && !(current instanceof Window)) {
            current = current.getParent();
        }
        return current instanceof Window ? (Window) current : null;
    }

    private static boolean detectSingleNativeWindowMode() {
        String configuredMode = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_WINDOW_MODE");
        return configuredMode != null && configuredMode.equalsIgnoreCase("single-native");
    }

    private static boolean shouldHideCompatibilityHost() {
        return SINGLE_NATIVE_WINDOW_MODE || parent != null;
    }

    private static void syncCompatibilityHostVisibility() throws Exception {
        if (!created || windowHandle == 0L) {
            return;
        }

        if (shouldHideCompatibilityHost()) {
            BINDINGS.hideWindow(windowHandle);
        }

        if (SINGLE_NATIVE_WINDOW_MODE) {
            hideSingleNativeAwtHost();
        }
    }

    private static void hideSingleNativeAwtHost() {
        Window owningWindow = findOwningWindow(parent);
        if (owningWindow != null && owningWindow.isVisible()) {
            owningWindow.setVisible(false);
        }
    }

    private static void syncSingleNativeWindowSize() {
        if (!RemixBridgeNative.isAvailable() || parent == null) {
            return;
        }
        int width = RemixBridgeNative.nGetOutputWindowWidth();
        int height = RemixBridgeNative.nGetOutputWindowHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (parent.getWidth() != width || parent.getHeight() != height) {
            parent.setSize(width, height);
        }
    }

    private static void installCallbacks() throws Exception {
        keyCallback = BINDINGS.installCallback(windowHandle, "glfwSetKeyCallback", "org.lwjgl.glfw.GLFWKeyCallbackI", new GlfwBindings.CallbackHandler() {
            @Override
            public void invoke(Object[] args) {
                Keyboard.handleGlfwKey(((Number) args[1]).intValue(), ((Number) args[3]).intValue(), ((Number) args[4]).intValue());
            }
        });
        mouseButtonCallback = BINDINGS.installCallback(windowHandle, "glfwSetMouseButtonCallback", "org.lwjgl.glfw.GLFWMouseButtonCallbackI", new GlfwBindings.CallbackHandler() {
            @Override
            public void invoke(Object[] args) {
                Mouse.handleButton(((Number) args[1]).intValue(), ((Number) args[2]).intValue());
            }
        });
        cursorPositionCallback = BINDINGS.installCallback(windowHandle, "glfwSetCursorPosCallback", "org.lwjgl.glfw.GLFWCursorPosCallbackI", new GlfwBindings.CallbackHandler() {
            @Override
            public void invoke(Object[] args) {
                Mouse.handleCursorPosition(((Number) args[1]).doubleValue(), ((Number) args[2]).doubleValue());
            }
        });
        scrollCallback = BINDINGS.installCallback(windowHandle, "glfwSetScrollCallback", "org.lwjgl.glfw.GLFWScrollCallbackI", new GlfwBindings.CallbackHandler() {
            @Override
            public void invoke(Object[] args) {
                Mouse.handleScroll(((Number) args[2]).doubleValue());
            }
        });
        focusCallback = BINDINGS.installCallback(windowHandle, "glfwSetWindowFocusCallback", "org.lwjgl.glfw.GLFWWindowFocusCallbackI", new GlfwBindings.CallbackHandler() {
            @Override
            public void invoke(Object[] args) {
                boolean focused = ((Boolean) args[1]).booleanValue();
                active = embeddedParentWindow ? (focused || isParentWindowActive()) : focused;
            }
        });
    }
}