package mcrtx.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.lwjgl.opengl.Display;

final class LwjglWindowHandleResolver {
    private LwjglWindowHandleResolver() {
    }

    static long resolveCurrentHwnd() {
        try {
            Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
            getImplementation.setAccessible(true);
            Object implementation = getImplementation.invoke(null);
            long hwnd = invokeHandleGetter(implementation);
            if (hwnd != 0L) {
                return hwnd;
            }
            return readHandleField(implementation);
        } catch (ReflectiveOperationException | SecurityException exception) {
            return resolveCompatibilityDisplayHwnd();
        }
    }

    private static long resolveCompatibilityDisplayHwnd() {
        try {
            Method windowHandleMethod = Display.class.getMethod("windowHandle");
            Object windowHandle = windowHandleMethod.invoke(null);
            if (!(windowHandle instanceof Number)) {
                return 0L;
            }

            long glfwWindow = ((Number) windowHandle).longValue();
            if (glfwWindow == 0L) {
                return 0L;
            }

            Class<?> glfwNativeWin32Class = Class.forName("org.lwjgl.glfw.GLFWNativeWin32");
            Method glfwGetWin32Window = glfwNativeWin32Class.getMethod("glfwGetWin32Window", Long.TYPE);
            Object hwnd = glfwGetWin32Window.invoke(null, Long.valueOf(glfwWindow));
            return hwnd instanceof Number ? ((Number) hwnd).longValue() : 0L;
        } catch (ReflectiveOperationException | SecurityException exception) {
            return 0L;
        }
    }

    private static long invokeHandleGetter(Object implementation) throws ReflectiveOperationException {
        if (implementation == null) {
            return 0L;
        }
        Method getHwnd = implementation.getClass().getDeclaredMethod("getHwnd");
        getHwnd.setAccessible(true);
        Object hwnd = getHwnd.invoke(implementation);
        if (hwnd instanceof Number) {
            return ((Number) hwnd).longValue();
        }
        return 0L;
    }

    private static long readHandleField(Object implementation) throws IllegalAccessException {
        if (implementation == null) {
            return 0L;
        }
        Class<?> type = implementation.getClass();
        while (type != null) {
            try {
                Field hwnd = type.getDeclaredField("hwnd");
                hwnd.setAccessible(true);
                return hwnd.getLong(implementation);
            } catch (NoSuchFieldException exception) {
                type = type.getSuperclass();
            }
        }
        return 0L;
    }
}