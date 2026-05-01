package org.lwjgl.opengl;

import java.awt.Canvas;
import java.awt.Toolkit;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import mcrtx.lwjglshim.GlfwBindings;

public final class Display {
    private static final int GLFW_TRUE = 1;
    private static final int GLFW_VISIBLE = 0x00020004;
    private static final int GLFW_RESIZABLE = 0x00020003;
    private static final int GLFW_FOCUSED = 0x00020001;

    private static final Object LOCK = new Object();
    private static final GlfwBindings BINDINGS = GlfwBindings.get();

    private static DisplayMode requestedMode = getDesktopDisplayMode();
    private static String title = "Minecraft";
    private static Canvas parent;
    private static boolean fullscreen;
    private static boolean created;
    private static boolean closeRequested;
    private static boolean active;
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
                BINDINGS.windowHint(GLFW_VISIBLE, GLFW_TRUE);
                BINDINGS.windowHint(GLFW_RESIZABLE, GLFW_TRUE);

                DisplayMode mode = initialDisplayMode();
                long monitor = fullscreen ? BINDINGS.getPrimaryMonitor() : 0L;
                windowHandle = BINDINGS.createWindow(mode.getWidth(), mode.getHeight(), title, monitor, 0L);
                if (windowHandle == 0L) {
                    throw new LWJGLException("glfwCreateWindow returned null");
                }

                BINDINGS.makeContextCurrent(windowHandle);
                BINDINGS.createCapabilities();
                BINDINGS.showWindow(windowHandle);
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
            return created && active;
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
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to toggle GLFW fullscreen mode", exception);
            }
        }
    }

    public static void setParent(Canvas parentCanvas) {
        synchronized (LOCK) {
            parent = parentCanvas;
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
                BINDINGS.pollEvents();
                int[] windowSize = BINDINGS.getWindowSize(windowHandle);
                Mouse.updateWindowHeight(windowSize[1]);
                closeRequested = BINDINGS.windowShouldClose(windowHandle);
                active = BINDINGS.isFocused(windowHandle, GLFW_FOCUSED);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to poll GLFW events", exception);
            }
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
                active = ((Boolean) args[1]).booleanValue();
            }
        });
    }
}