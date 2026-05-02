package mcrtx.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
final class Lwjgl3MinecraftPlatform implements MinecraftPlatform {
    private static final int DEFAULT_GLFW_FOCUSED = 131073;
    private static final int DEFAULT_GLFW_PRESS = 1;
    private static final int DEFAULT_GLFW_REPEAT = 2;
    private static final int DEFAULT_GLFW_KEY_LEFT_ALT = 342;
    private static final int DEFAULT_GLFW_KEY_RIGHT_ALT = 346;
    private static final int DEFAULT_GLFW_KEY_X = 88;

    private final Method glfwGetCurrentContext;
    private final Method glfwGetWindowAttrib;
    private final Method glfwGetKey;
    private final Method glfwGetWin32Window;
    private final int glfwFocused;
    private final int glfwPress;
    private final int glfwRepeat;
    private final int glfwKeyLeftAlt;
    private final int glfwKeyRightAlt;
    private final int glfwKeyX;

    Lwjgl3MinecraftPlatform() {
        try {
            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Class<?> glfwNativeWin32Class = Class.forName("org.lwjgl.glfw.GLFWNativeWin32");

            glfwGetCurrentContext = glfwClass.getMethod("glfwGetCurrentContext");
            glfwGetWindowAttrib = glfwClass.getMethod("glfwGetWindowAttrib", Long.TYPE, Integer.TYPE);
            glfwGetKey = glfwClass.getMethod("glfwGetKey", Long.TYPE, Integer.TYPE);
            glfwGetWin32Window = glfwNativeWin32Class.getMethod("glfwGetWin32Window", Long.TYPE);

            glfwFocused = readStaticInt(glfwClass, "GLFW_FOCUSED", DEFAULT_GLFW_FOCUSED);
            glfwPress = readStaticInt(glfwClass, "GLFW_PRESS", DEFAULT_GLFW_PRESS);
            glfwRepeat = readStaticInt(glfwClass, "GLFW_REPEAT", DEFAULT_GLFW_REPEAT);
            glfwKeyLeftAlt = readStaticInt(glfwClass, "GLFW_KEY_LEFT_ALT", DEFAULT_GLFW_KEY_LEFT_ALT);
            glfwKeyRightAlt = readStaticInt(glfwClass, "GLFW_KEY_RIGHT_ALT", DEFAULT_GLFW_KEY_RIGHT_ALT);
            glfwKeyX = readStaticInt(glfwClass, "GLFW_KEY_X", DEFAULT_GLFW_KEY_X);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to initialize the LWJGL 3 platform backend", exception);
        }
    }

    @Override
    public String backendId() {
        return "lwjgl3";
    }

    @Override
    public long resolveCurrentWindowHandle() {
        return LwjglWindowHandleResolver.resolveCurrentHwnd();
    }

    @Override
    public boolean isWindowActive() {
        long window = getCurrentWindow();
        if (window == 0L) {
            return false;
        }

        try {
            Object focused = glfwGetWindowAttrib.invoke(null, Long.valueOf(window), Integer.valueOf(glfwFocused));
            return focused instanceof Number && ((Number) focused).intValue() != 0;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    @Override
    public boolean isKeyDown(MinecraftPlatformKey key) {
        if (key == null) {
            return false;
        }

        long window = getCurrentWindow();
        if (window == 0L) {
            return false;
        }

        int glfwKey;
        switch (key) {
            case LEFT_ALT:
                glfwKey = glfwKeyLeftAlt;
                break;
            case RIGHT_ALT:
                glfwKey = glfwKeyRightAlt;
                break;
            case X:
                glfwKey = glfwKeyX;
                break;
            default:
                return false;
        }

        try {
            Object state = glfwGetKey.invoke(null, Long.valueOf(window), Integer.valueOf(glfwKey));
            if (!(state instanceof Number)) {
                return false;
            }

            int keyState = ((Number) state).intValue();
            return keyState == glfwPress || keyState == glfwRepeat;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }

    private long getCurrentWindow() {
        try {
            Object window = glfwGetCurrentContext.invoke(null);
            if (window instanceof Number) {
                return ((Number) window).longValue();
            }
            return 0L;
        } catch (ReflectiveOperationException exception) {
            return 0L;
        }
    }

    private static int readStaticInt(Class<?> type, String fieldName, int fallback) {
        try {
            Field field = type.getField(fieldName);
            return field.getInt(null);
        } catch (ReflectiveOperationException exception) {
            return fallback;
        }
    }
}