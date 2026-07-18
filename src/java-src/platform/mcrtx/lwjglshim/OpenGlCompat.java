package mcrtx.lwjglshim;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
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

    /**
     * Whether GL15 pixel-buffer-object readback (glGenBuffers / glBindBuffer /
     * glBufferData / glGetBufferSubData / offset-based glReadPixels) resolved
     * against the runtime GL bindings. Callers fall back to a synchronous
     * readback when this is false.
     */
    public static boolean isPixelBufferReadbackSupported() {
        return BINDINGS.supportsPixelBuffers();
    }

    public static int genBuffer() {
        return BINDINGS.glGenBuffers();
    }

    public static void bindBuffer(int target, int buffer) {
        BINDINGS.glBindBuffer(target, buffer);
    }

    public static void bufferData(int target, long size, int usage) {
        BINDINGS.glBufferData(target, size, usage);
    }

    public static void readPixelsToBoundBuffer(int x, int y, int width, int height, int format, int type, long offset) {
        BINDINGS.glReadPixelsOffset(x, y, width, height, format, type, offset);
    }

    public static void getBufferSubData(int target, long offset, ByteBuffer data) {
        BINDINGS.glGetBufferSubData(target, offset, data);
    }

    public static void deleteBuffer(int buffer) {
        BINDINGS.glDeleteBuffers(buffer);
    }

    private static final class Bindings {
        private final Method glfwGetCurrentContext;
        private final Method glGetFloat;
        private final Method glGetCapabilities;
        private final Method glGetInteger;
        private final Method glGetString;
        private final Method glGenBuffers;
        private final Method glBindBuffer;
        private final Method glBufferData;
        private final Method glGetBufferSubData;
        private final Method glDeleteBuffers;
        private final Method glReadPixelsOffset;

        private Bindings(Method glfwGetCurrentContext, Method glGetFloat, Method glGetCapabilities, Method glGetInteger, Method glGetString,
                Method glGenBuffers, Method glBindBuffer, Method glBufferData, Method glGetBufferSubData, Method glDeleteBuffers, Method glReadPixelsOffset) {
            this.glfwGetCurrentContext = glfwGetCurrentContext;
            this.glGetFloat = glGetFloat;
            this.glGetCapabilities = glGetCapabilities;
            this.glGetInteger = glGetInteger;
            this.glGetString = glGetString;
            this.glGenBuffers = glGenBuffers;
            this.glBindBuffer = glBindBuffer;
            this.glBufferData = glBufferData;
            this.glGetBufferSubData = glGetBufferSubData;
            this.glDeleteBuffers = glDeleteBuffers;
            this.glReadPixelsOffset = glReadPixelsOffset;
        }

        public static Bindings load() {
            Method glfwGetCurrentContext = null;
            Method glGetFloat = null;
            Method glGetCapabilities = null;
            Method glGetInteger = null;
            Method glGetString = null;
            try {
                Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
                Class<?> glClass = Class.forName("org.lwjgl.opengl.GL");
                Class<?> gl11Class = Class.forName("org.lwjgl.opengl.GL11");
                glfwGetCurrentContext = glfwClass.getMethod("glfwGetCurrentContext");
                glGetFloat = gl11Class.getMethod("glGetFloatv", Integer.TYPE, FloatBuffer.class);
                glGetCapabilities = glClass.getMethod("getCapabilities");
                glGetInteger = gl11Class.getMethod("glGetIntegerv", Integer.TYPE, IntBuffer.class);
                glGetString = gl11Class.getMethod("glGetString", Integer.TYPE);
            } catch (ReflectiveOperationException exception) {
                // Leave core bindings null; callers guard on hasCurrentContext().
            }

            // Pixel-buffer-object readback resolves separately so its absence
            // never disables the core query bindings above.
            Method glGenBuffers = null;
            Method glBindBuffer = null;
            Method glBufferData = null;
            Method glGetBufferSubData = null;
            Method glDeleteBuffers = null;
            Method glReadPixelsOffset = null;
            try {
                Class<?> gl11Class = Class.forName("org.lwjgl.opengl.GL11");
                Class<?> gl15Class = Class.forName("org.lwjgl.opengl.GL15");
                glGenBuffers = gl15Class.getMethod("glGenBuffers");
                glBindBuffer = gl15Class.getMethod("glBindBuffer", Integer.TYPE, Integer.TYPE);
                glBufferData = gl15Class.getMethod("glBufferData", Integer.TYPE, Long.TYPE, Integer.TYPE);
                glGetBufferSubData = gl15Class.getMethod("glGetBufferSubData", Integer.TYPE, Long.TYPE, ByteBuffer.class);
                glDeleteBuffers = gl15Class.getMethod("glDeleteBuffers", Integer.TYPE);
                glReadPixelsOffset = gl11Class.getMethod("glReadPixels",
                        Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Long.TYPE);
            } catch (ReflectiveOperationException exception) {
                glGenBuffers = null;
                glBindBuffer = null;
                glBufferData = null;
                glGetBufferSubData = null;
                glDeleteBuffers = null;
                glReadPixelsOffset = null;
            }

            return new Bindings(glfwGetCurrentContext, glGetFloat, glGetCapabilities, glGetInteger, glGetString,
                    glGenBuffers, glBindBuffer, glBufferData, glGetBufferSubData, glDeleteBuffers, glReadPixelsOffset);
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

        public boolean supportsPixelBuffers() {
            return glGenBuffers != null && glBindBuffer != null && glBufferData != null
                    && glGetBufferSubData != null && glDeleteBuffers != null && glReadPixelsOffset != null;
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

        public int glGenBuffers() {
            try {
                Object value = glGenBuffers.invoke(null);
                return value instanceof Number ? ((Number) value).intValue() : 0;
            } catch (Exception exception) {
                throw asUnchecked(exception);
            }
        }

        public void glBindBuffer(int target, int buffer) {
            try {
                glBindBuffer.invoke(null, Integer.valueOf(target), Integer.valueOf(buffer));
            } catch (Exception exception) {
                throw asUnchecked(exception);
            }
        }

        public void glBufferData(int target, long size, int usage) {
            try {
                glBufferData.invoke(null, Integer.valueOf(target), Long.valueOf(size), Integer.valueOf(usage));
            } catch (Exception exception) {
                throw asUnchecked(exception);
            }
        }

        public void glGetBufferSubData(int target, long offset, ByteBuffer data) {
            try {
                glGetBufferSubData.invoke(null, Integer.valueOf(target), Long.valueOf(offset), data);
            } catch (Exception exception) {
                throw asUnchecked(exception);
            }
        }

        public void glDeleteBuffers(int buffer) {
            try {
                glDeleteBuffers.invoke(null, Integer.valueOf(buffer));
            } catch (Exception exception) {
                throw asUnchecked(exception);
            }
        }

        public void glReadPixelsOffset(int x, int y, int width, int height, int format, int type, long offset) {
            try {
                glReadPixelsOffset.invoke(null,
                        Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height),
                        Integer.valueOf(format), Integer.valueOf(type), Long.valueOf(offset));
            } catch (Exception exception) {
                throw asUnchecked(exception);
            }
        }

        private static RuntimeException asUnchecked(Exception exception) {
            if (exception instanceof RuntimeException) {
                return (RuntimeException) exception;
            }
            return new IllegalStateException("GL pixel buffer binding failed", exception);
        }
    }
}