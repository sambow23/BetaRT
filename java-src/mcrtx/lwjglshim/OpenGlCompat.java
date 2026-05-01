package mcrtx.lwjglshim;

import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class OpenGlCompat {
    private static final Bindings BINDINGS = Bindings.load();

    private OpenGlCompat() {
    }

    public static String tryGetString(int name) {
        if (!BINDINGS.hasCurrentContext()) {
            return null;
        }
        if (!BINDINGS.hasCapabilities()) {
            return null;
        }

        try {
            Object value = BINDINGS.glGetString(name);
            return value instanceof String ? (String) value : null;
        } catch (Exception exception) {
            return null;
        }
    }

    public static boolean hasCurrentContext() {
        return BINDINGS.hasCurrentContext();
    }

    public static boolean getInteger(int name, IntBuffer output) {
        if (output == null || !BINDINGS.hasCurrentContext() || !BINDINGS.hasCapabilities()) {
            return false;
        }

        try {
            BINDINGS.glGetInteger(name, output);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static boolean getFloat(int name, FloatBuffer output) {
        if (output == null || !BINDINGS.hasCurrentContext() || !BINDINGS.hasCapabilities()) {
            return false;
        }

        try {
            BINDINGS.glGetFloat(name, output);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static final class Bindings {
        private final Method glfwGetCurrentContext;
        private final Method glGetFloat;
        private final Method glGetCapabilities;
        private final Method glGetInteger;
        private final Method glGetString;

        private Bindings(Method glfwGetCurrentContext, Method glGetFloat, Method glGetCapabilities, Method glGetInteger, Method glGetString) {
            this.glfwGetCurrentContext = glfwGetCurrentContext;
            this.glGetFloat = glGetFloat;
            this.glGetCapabilities = glGetCapabilities;
            this.glGetInteger = glGetInteger;
            this.glGetString = glGetString;
        }

        public static Bindings load() {
            try {
                Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
                Class<?> glClass = Class.forName("org.lwjgl.opengl.GL");
                Class<?> gl11Class = Class.forName("org.lwjgl.opengl.GL11");
                return new Bindings(
                        glfwClass.getMethod("glfwGetCurrentContext"),
                        gl11Class.getMethod("glGetFloatv", Integer.TYPE, FloatBuffer.class),
                        glClass.getMethod("getCapabilities"),
                        gl11Class.getMethod("glGetIntegerv", Integer.TYPE, IntBuffer.class),
                        gl11Class.getMethod("glGetString", Integer.TYPE));
            } catch (ReflectiveOperationException exception) {
                return new Bindings(null, null, null, null, null);
            }
        }

        public boolean hasCurrentContext() {
            if (glfwGetCurrentContext == null) {
                return false;
            }
            try {
                Object value = glfwGetCurrentContext.invoke(null);
                return value instanceof Number && ((Number) value).longValue() != 0L;
            } catch (Exception exception) {
                return false;
            }
        }

        public boolean hasCapabilities() {
            if (glGetCapabilities == null) {
                return false;
            }
            try {
                return glGetCapabilities.invoke(null) != null;
            } catch (Exception exception) {
                return false;
            }
        }

        public Object glGetString(int name) throws Exception {
            return glGetString.invoke(null, Integer.valueOf(name));
        }

        public void glGetInteger(int name, IntBuffer output) throws Exception {
            glGetInteger.invoke(null, Integer.valueOf(name), output);
        }

        public void glGetFloat(int name, FloatBuffer output) throws Exception {
            glGetFloat.invoke(null, Integer.valueOf(name), output);
        }
    }
}