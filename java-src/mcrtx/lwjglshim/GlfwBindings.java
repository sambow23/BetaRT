package mcrtx.lwjglshim;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class GlfwBindings {
    public interface CallbackHandler {
        void invoke(Object[] args);
    }

    private static final GlfwBindings INSTANCE = new GlfwBindings();

    private final Class<?> glfwClass;
    private final Method glfwInit;
    private final Method glfwTerminate;
    private final Method glfwDefaultWindowHints;
    private final Method glfwWindowHint;
    private final Method glfwCreateWindow;
    private final Method glfwDestroyWindow;
    private final Method glfwMakeContextCurrent;
    private final Method glfwSwapBuffers;
    private final Method glfwPollEvents;
    private final Method glfwWindowShouldClose;
    private final Method glfwSetWindowShouldClose;
    private final Method glfwSetWindowTitle;
    private final Method glfwSetWindowSize;
    private final Method glfwGetWindowSize;
    private final Method glfwGetWindowAttrib;
    private final Method glfwGetPrimaryMonitor;
    private final Method glfwGetVideoMode;
    private final Method glfwSetWindowMonitor;
    private final Method glfwShowWindow;
    private final Method glfwHideWindow;
    private final Method glfwGetWin32Window;
    private final Method glfwGetCursorPos;
    private final Method glfwSetCursorPos;
    private final Method glfwSetInputMode;
    private final Method glfwGetKey;
    private final Method glfwGetMouseButton;
    private final Method createCapabilities;
    private final String initError;

    private GlfwBindings() {
        Class<?> resolvedGlfwClass = null;
        Method resolvedGlfwInit = null;
        Method resolvedGlfwTerminate = null;
        Method resolvedGlfwDefaultWindowHints = null;
        Method resolvedGlfwWindowHint = null;
        Method resolvedGlfwCreateWindow = null;
        Method resolvedGlfwDestroyWindow = null;
        Method resolvedGlfwMakeContextCurrent = null;
        Method resolvedGlfwSwapBuffers = null;
        Method resolvedGlfwPollEvents = null;
        Method resolvedGlfwWindowShouldClose = null;
        Method resolvedGlfwSetWindowShouldClose = null;
        Method resolvedGlfwSetWindowTitle = null;
        Method resolvedGlfwSetWindowSize = null;
        Method resolvedGlfwGetWindowSize = null;
        Method resolvedGlfwGetWindowAttrib = null;
        Method resolvedGlfwGetPrimaryMonitor = null;
        Method resolvedGlfwGetVideoMode = null;
        Method resolvedGlfwSetWindowMonitor = null;
        Method resolvedGlfwShowWindow = null;
        Method resolvedGlfwHideWindow = null;
        Method resolvedGlfwGetWin32Window = null;
        Method resolvedGlfwGetCursorPos = null;
        Method resolvedGlfwSetCursorPos = null;
        Method resolvedGlfwSetInputMode = null;
        Method resolvedGlfwGetKey = null;
        Method resolvedGlfwGetMouseButton = null;
        Method resolvedCreateCapabilities = null;
        String resolvedInitError = "";

        try {
            resolvedGlfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            resolvedGlfwInit = resolvedGlfwClass.getMethod("glfwInit");
            resolvedGlfwTerminate = resolvedGlfwClass.getMethod("glfwTerminate");
            resolvedGlfwDefaultWindowHints = resolvedGlfwClass.getMethod("glfwDefaultWindowHints");
            resolvedGlfwWindowHint = resolvedGlfwClass.getMethod("glfwWindowHint", Integer.TYPE, Integer.TYPE);
            resolvedGlfwCreateWindow = resolvedGlfwClass.getMethod("glfwCreateWindow", Integer.TYPE, Integer.TYPE, CharSequence.class, Long.TYPE, Long.TYPE);
            resolvedGlfwDestroyWindow = resolvedGlfwClass.getMethod("glfwDestroyWindow", Long.TYPE);
            resolvedGlfwMakeContextCurrent = resolvedGlfwClass.getMethod("glfwMakeContextCurrent", Long.TYPE);
            resolvedGlfwSwapBuffers = resolvedGlfwClass.getMethod("glfwSwapBuffers", Long.TYPE);
            resolvedGlfwPollEvents = resolvedGlfwClass.getMethod("glfwPollEvents");
            resolvedGlfwWindowShouldClose = resolvedGlfwClass.getMethod("glfwWindowShouldClose", Long.TYPE);
            resolvedGlfwSetWindowShouldClose = resolvedGlfwClass.getMethod("glfwSetWindowShouldClose", Long.TYPE, Boolean.TYPE);
            resolvedGlfwSetWindowTitle = resolvedGlfwClass.getMethod("glfwSetWindowTitle", Long.TYPE, CharSequence.class);
            resolvedGlfwSetWindowSize = resolvedGlfwClass.getMethod("glfwSetWindowSize", Long.TYPE, Integer.TYPE, Integer.TYPE);
            resolvedGlfwGetWindowSize = resolvedGlfwClass.getMethod("glfwGetWindowSize", Long.TYPE, int[].class, int[].class);
            resolvedGlfwGetWindowAttrib = resolvedGlfwClass.getMethod("glfwGetWindowAttrib", Long.TYPE, Integer.TYPE);
            resolvedGlfwGetPrimaryMonitor = resolvedGlfwClass.getMethod("glfwGetPrimaryMonitor");
            resolvedGlfwGetVideoMode = resolvedGlfwClass.getMethod("glfwGetVideoMode", Long.TYPE);
            resolvedGlfwSetWindowMonitor = resolvedGlfwClass.getMethod("glfwSetWindowMonitor", Long.TYPE, Long.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            resolvedGlfwShowWindow = resolvedGlfwClass.getMethod("glfwShowWindow", Long.TYPE);
            resolvedGlfwHideWindow = resolvedGlfwClass.getMethod("glfwHideWindow", Long.TYPE);
            resolvedGlfwGetWin32Window = Class.forName("org.lwjgl.glfw.GLFWNativeWin32").getMethod("glfwGetWin32Window", Long.TYPE);
            resolvedGlfwGetCursorPos = resolvedGlfwClass.getMethod("glfwGetCursorPos", Long.TYPE, double[].class, double[].class);
            resolvedGlfwSetCursorPos = resolvedGlfwClass.getMethod("glfwSetCursorPos", Long.TYPE, Double.TYPE, Double.TYPE);
            resolvedGlfwSetInputMode = resolvedGlfwClass.getMethod("glfwSetInputMode", Long.TYPE, Integer.TYPE, Integer.TYPE);
            resolvedGlfwGetKey = resolvedGlfwClass.getMethod("glfwGetKey", Long.TYPE, Integer.TYPE);
            resolvedGlfwGetMouseButton = resolvedGlfwClass.getMethod("glfwGetMouseButton", Long.TYPE, Integer.TYPE);
            resolvedCreateCapabilities = Class.forName("org.lwjgl.opengl.GL").getMethod("createCapabilities");
        } catch (ReflectiveOperationException exception) {
            resolvedInitError = exception.toString();
        }

        glfwClass = resolvedGlfwClass;
        glfwInit = resolvedGlfwInit;
        glfwTerminate = resolvedGlfwTerminate;
        glfwDefaultWindowHints = resolvedGlfwDefaultWindowHints;
        glfwWindowHint = resolvedGlfwWindowHint;
        glfwCreateWindow = resolvedGlfwCreateWindow;
        glfwDestroyWindow = resolvedGlfwDestroyWindow;
        glfwMakeContextCurrent = resolvedGlfwMakeContextCurrent;
        glfwSwapBuffers = resolvedGlfwSwapBuffers;
        glfwPollEvents = resolvedGlfwPollEvents;
        glfwWindowShouldClose = resolvedGlfwWindowShouldClose;
        glfwSetWindowShouldClose = resolvedGlfwSetWindowShouldClose;
        glfwSetWindowTitle = resolvedGlfwSetWindowTitle;
        glfwSetWindowSize = resolvedGlfwSetWindowSize;
        glfwGetWindowSize = resolvedGlfwGetWindowSize;
        glfwGetWindowAttrib = resolvedGlfwGetWindowAttrib;
        glfwGetPrimaryMonitor = resolvedGlfwGetPrimaryMonitor;
        glfwGetVideoMode = resolvedGlfwGetVideoMode;
        glfwSetWindowMonitor = resolvedGlfwSetWindowMonitor;
        glfwShowWindow = resolvedGlfwShowWindow;
        glfwHideWindow = resolvedGlfwHideWindow;
        glfwGetWin32Window = resolvedGlfwGetWin32Window;
        glfwGetCursorPos = resolvedGlfwGetCursorPos;
        glfwSetCursorPos = resolvedGlfwSetCursorPos;
        glfwSetInputMode = resolvedGlfwSetInputMode;
        glfwGetKey = resolvedGlfwGetKey;
        glfwGetMouseButton = resolvedGlfwGetMouseButton;
        createCapabilities = resolvedCreateCapabilities;
        initError = resolvedInitError;
    }

    public static GlfwBindings get() {
        return INSTANCE;
    }

    public boolean isAvailable() {
        return glfwClass != null;
    }

    public String initError() {
        return initError;
    }

    public boolean init() throws ReflectiveOperationException {
        return ((Boolean) glfwInit.invoke(null)).booleanValue();
    }

    public void terminate() throws ReflectiveOperationException {
        glfwTerminate.invoke(null);
    }

    public void defaultWindowHints() throws ReflectiveOperationException {
        glfwDefaultWindowHints.invoke(null);
    }

    public void windowHint(int hint, int value) throws ReflectiveOperationException {
        glfwWindowHint.invoke(null, Integer.valueOf(hint), Integer.valueOf(value));
    }

    public long createWindow(int width, int height, String title, long monitor, long share) throws ReflectiveOperationException {
        Object window = glfwCreateWindow.invoke(null, Integer.valueOf(width), Integer.valueOf(height), title, Long.valueOf(monitor), Long.valueOf(share));
        return window instanceof Number ? ((Number) window).longValue() : 0L;
    }

    public void destroyWindow(long window) throws ReflectiveOperationException {
        glfwDestroyWindow.invoke(null, Long.valueOf(window));
    }

    public void makeContextCurrent(long window) throws ReflectiveOperationException {
        glfwMakeContextCurrent.invoke(null, Long.valueOf(window));
    }

    public void swapBuffers(long window) throws ReflectiveOperationException {
        glfwSwapBuffers.invoke(null, Long.valueOf(window));
    }

    public void pollEvents() throws ReflectiveOperationException {
        glfwPollEvents.invoke(null);
    }

    public boolean windowShouldClose(long window) throws ReflectiveOperationException {
        return ((Boolean) glfwWindowShouldClose.invoke(null, Long.valueOf(window))).booleanValue();
    }

    public void setWindowShouldClose(long window, boolean shouldClose) throws ReflectiveOperationException {
        glfwSetWindowShouldClose.invoke(null, Long.valueOf(window), Boolean.valueOf(shouldClose));
    }

    public void setWindowTitle(long window, String title) throws ReflectiveOperationException {
        glfwSetWindowTitle.invoke(null, Long.valueOf(window), title);
    }

    public void setWindowSize(long window, int width, int height) throws ReflectiveOperationException {
        glfwSetWindowSize.invoke(null, Long.valueOf(window), Integer.valueOf(width), Integer.valueOf(height));
    }

    public int[] getWindowSize(long window) throws ReflectiveOperationException {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize.invoke(null, Long.valueOf(window), width, height);
        return new int[] { width[0], height[0] };
    }

    public boolean isFocused(long window, int focusedAttrib) throws ReflectiveOperationException {
        Object result = glfwGetWindowAttrib.invoke(null, Long.valueOf(window), Integer.valueOf(focusedAttrib));
        return result instanceof Number && ((Number) result).intValue() != 0;
    }

    public long getPrimaryMonitor() throws ReflectiveOperationException {
        Object monitor = glfwGetPrimaryMonitor.invoke(null);
        return monitor instanceof Number ? ((Number) monitor).longValue() : 0L;
    }

    public int[] getVideoMode(long monitor) throws ReflectiveOperationException {
        Object videoMode = glfwGetVideoMode.invoke(null, Long.valueOf(monitor));
        if (videoMode == null) {
            return new int[] { 854, 480, 60 };
        }

        Method widthMethod = videoMode.getClass().getMethod("width");
        Method heightMethod = videoMode.getClass().getMethod("height");
        Method refreshRateMethod = videoMode.getClass().getMethod("refreshRate");
        return new int[] {
                ((Number) widthMethod.invoke(videoMode)).intValue(),
                ((Number) heightMethod.invoke(videoMode)).intValue(),
                ((Number) refreshRateMethod.invoke(videoMode)).intValue()
        };
    }

    public void setWindowMonitor(long window, long monitor, int x, int y, int width, int height, int refreshRate) throws ReflectiveOperationException {
        glfwSetWindowMonitor.invoke(null, Long.valueOf(window), Long.valueOf(monitor), Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(refreshRate));
    }

    public void showWindow(long window) throws ReflectiveOperationException {
        glfwShowWindow.invoke(null, Long.valueOf(window));
    }

    public void hideWindow(long window) throws ReflectiveOperationException {
        glfwHideWindow.invoke(null, Long.valueOf(window));
    }

    public long getWin32Window(long window) throws ReflectiveOperationException {
        Object hwnd = glfwGetWin32Window.invoke(null, Long.valueOf(window));
        return hwnd instanceof Number ? ((Number) hwnd).longValue() : 0L;
    }

    public double[] getCursorPos(long window) throws ReflectiveOperationException {
        double[] x = new double[1];
        double[] y = new double[1];
        glfwGetCursorPos.invoke(null, Long.valueOf(window), x, y);
        return new double[] { x[0], y[0] };
    }

    public void setCursorPos(long window, double x, double y) throws ReflectiveOperationException {
        glfwSetCursorPos.invoke(null, Long.valueOf(window), Double.valueOf(x), Double.valueOf(y));
    }

    public void setInputMode(long window, int mode, int value) throws ReflectiveOperationException {
        glfwSetInputMode.invoke(null, Long.valueOf(window), Integer.valueOf(mode), Integer.valueOf(value));
    }

    public int getKey(long window, int key) throws ReflectiveOperationException {
        Object value = glfwGetKey.invoke(null, Long.valueOf(window), Integer.valueOf(key));
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    public int getMouseButton(long window, int button) throws ReflectiveOperationException {
        Object value = glfwGetMouseButton.invoke(null, Long.valueOf(window), Integer.valueOf(button));
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    public void createCapabilities() throws ReflectiveOperationException {
        createCapabilities.invoke(null);
    }

    public Object installCallback(long window, String setterName, String interfaceName, final CallbackHandler callbackHandler) throws ReflectiveOperationException {
        Class<?> callbackInterface = Class.forName(interfaceName);
        Object proxy = Proxy.newProxyInstance(
                callbackInterface.getClassLoader(),
                new Class<?>[] { callbackInterface },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxyInstance, Method method, Object[] args) {
                        if (callbackHandler != null && "invoke".equals(method.getName())) {
                            callbackHandler.invoke(args);
                            return null;
                        }
                        if (method.isDefault()) {
                            try {
                                return invokeDefaultMethod(proxyInstance, method, args);
                            } catch (Throwable throwable) {
                                throw new IllegalStateException("Failed to invoke default callback method '" + method.getName() + "'", throwable);
                            }
                        }
                        if ("toString".equals(method.getName())) {
                            return interfaceName + " proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return Integer.valueOf(System.identityHashCode(proxyInstance));
                        }
                        if ("equals".equals(method.getName())) {
                            return Boolean.valueOf(proxyInstance == args[0]);
                        }
                        return null;
                    }
                });
        Method setter = glfwClass.getMethod(setterName, Long.TYPE, callbackInterface);
        setter.invoke(null, Long.valueOf(window), proxy);
        return proxy;
    }

    private static Object invokeDefaultMethod(Object proxyInstance, Method method, Object[] args) throws Throwable {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
        constructor.setAccessible(true);
        MethodHandles.Lookup lookup = constructor.newInstance(method.getDeclaringClass(), Integer.valueOf(
                MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE));
        MethodHandle handle = lookup.unreflectSpecial(method, method.getDeclaringClass()).bindTo(proxyInstance);
        return handle.invokeWithArguments(args == null ? new Object[0] : args);
    }
}