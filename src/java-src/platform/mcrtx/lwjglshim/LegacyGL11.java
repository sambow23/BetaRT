package mcrtx.lwjglshim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class LegacyGL11 {
    private static final int GL_BYTE = 5120;
    private static final int GL_UNSIGNED_BYTE = 5121;
    private static final int GL_FLOAT = 5126;

    private static final Bindings BINDINGS = Bindings.load();

    private LegacyGL11() {
    }

    public static void glAlphaFunc(int function, float reference) {
        BINDINGS.invokeVoid(BINDINGS.glAlphaFunc, Integer.valueOf(function), Float.valueOf(reference));
    }

    public static void glBindTexture(int target, int texture) {
        BINDINGS.invokeVoid(BINDINGS.glBindTexture, Integer.valueOf(target), Integer.valueOf(texture));
    }

    public static void glBlendFunc(int sourceFactor, int destinationFactor) {
        BINDINGS.invokeVoid(BINDINGS.glBlendFunc, Integer.valueOf(sourceFactor), Integer.valueOf(destinationFactor));
    }

    public static void glCallList(int list) {
        BINDINGS.invokeVoid(BINDINGS.glCallList, Integer.valueOf(list));
    }

    public static void glCallLists(IntBuffer lists) {
        BINDINGS.invokeVoid(BINDINGS.glCallLists, lists);
    }

    public static void glClear(int mask) {
        BINDINGS.invokeVoid(BINDINGS.glClear, Integer.valueOf(mask));
    }

    public static void glClearColor(float red, float green, float blue, float alpha) {
        BINDINGS.invokeVoid(BINDINGS.glClearColor, Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue), Float.valueOf(alpha));
    }

    public static void glClearDepth(double depth) {
        BINDINGS.invokeVoid(BINDINGS.glClearDepth, Double.valueOf(depth));
    }

    public static void glColor3f(float red, float green, float blue) {
        BINDINGS.invokeVoid(BINDINGS.glColor3f, Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue));
    }

    public static void glColor4f(float red, float green, float blue, float alpha) {
        BINDINGS.invokeVoid(BINDINGS.glColor4f, Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue), Float.valueOf(alpha));
    }

    public static void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        BINDINGS.invokeVoid(BINDINGS.glColorMask, Boolean.valueOf(red), Boolean.valueOf(green), Boolean.valueOf(blue), Boolean.valueOf(alpha));
    }

    public static void glColorMaterial(int face, int mode) {
        BINDINGS.invokeVoid(BINDINGS.glColorMaterial, Integer.valueOf(face), Integer.valueOf(mode));
    }

    public static void glColorPointer(int size, int type, int stride, long pointer) {
        BINDINGS.invokeVoid(BINDINGS.glColorPointerOffset, Integer.valueOf(size), Integer.valueOf(type), Integer.valueOf(stride), Long.valueOf(pointer));
    }

    public static void glColorPointer(int size, boolean unsigned, int stride, ByteBuffer pointer) {
        int type = unsigned ? GL_UNSIGNED_BYTE : GL_BYTE;
        BINDINGS.invokeVoid(BINDINGS.glColorPointerBuffer, Integer.valueOf(size), Integer.valueOf(type), Integer.valueOf(stride), pointer);
    }

    public static void glCullFace(int mode) {
        BINDINGS.invokeVoid(BINDINGS.glCullFace, Integer.valueOf(mode));
    }

    public static void glDeleteLists(int list, int range) {
        BINDINGS.invokeVoid(BINDINGS.glDeleteLists, Integer.valueOf(list), Integer.valueOf(range));
    }

    public static void glDeleteTextures(int texture) {
        BINDINGS.invokeVoid(BINDINGS.glDeleteTexturesSingle, Integer.valueOf(texture));
    }

    public static void glDeleteTextures(IntBuffer textures) {
        BINDINGS.invokeVoid(BINDINGS.glDeleteTexturesBuffer, textures);
    }

    public static void glDepthFunc(int function) {
        BINDINGS.invokeVoid(BINDINGS.glDepthFunc, Integer.valueOf(function));
    }

    public static void glDepthMask(boolean flag) {
        BINDINGS.invokeVoid(BINDINGS.glDepthMask, Boolean.valueOf(flag));
    }

    public static void glDisable(int capability) {
        BINDINGS.invokeVoid(BINDINGS.glDisable, Integer.valueOf(capability));
    }

    public static void glDisableClientState(int capability) {
        BINDINGS.invokeVoid(BINDINGS.glDisableClientState, Integer.valueOf(capability));
    }

    public static void glDrawArrays(int mode, int first, int count) {
        BINDINGS.invokeVoid(BINDINGS.glDrawArrays, Integer.valueOf(mode), Integer.valueOf(first), Integer.valueOf(count));
    }

    public static void glEnable(int capability) {
        BINDINGS.invokeVoid(BINDINGS.glEnable, Integer.valueOf(capability));
    }

    public static void glEnableClientState(int capability) {
        BINDINGS.invokeVoid(BINDINGS.glEnableClientState, Integer.valueOf(capability));
    }

    public static void glEndList() {
        BINDINGS.invokeVoid(BINDINGS.glEndList);
    }

    public static void glFog(int parameterName, FloatBuffer parameters) {
        BINDINGS.invokeVoid(BINDINGS.glFog, Integer.valueOf(parameterName), parameters);
    }

    public static void glFogf(int parameterName, float parameter) {
        BINDINGS.invokeVoid(BINDINGS.glFogf, Integer.valueOf(parameterName), Float.valueOf(parameter));
    }

    public static void glFogi(int parameterName, int parameter) {
        BINDINGS.invokeVoid(BINDINGS.glFogi, Integer.valueOf(parameterName), Integer.valueOf(parameter));
    }

    public static int glGenLists(int range) {
        return BINDINGS.invokeInt(BINDINGS.glGenLists, Integer.valueOf(range));
    }

    public static void glGenTextures(IntBuffer textures) {
        BINDINGS.invokeVoid(BINDINGS.glGenTextures, textures);
    }

    public static int glGetError() {
        return BINDINGS.invokeInt(BINDINGS.glGetError);
    }

    public static void glGetFloat(int parameterName, FloatBuffer parameters) {
        BINDINGS.invokeVoid(BINDINGS.glGetFloat, Integer.valueOf(parameterName), parameters);
    }

    public static String glGetString(int name) {
        return BINDINGS.invokeString(BINDINGS.glGetString, Integer.valueOf(name));
    }

    public static void glLight(int light, int parameterName, FloatBuffer parameters) {
        BINDINGS.invokeVoid(BINDINGS.glLight, Integer.valueOf(light), Integer.valueOf(parameterName), parameters);
    }

    public static void glLightModel(int parameterName, FloatBuffer parameters) {
        BINDINGS.invokeVoid(BINDINGS.glLightModel, Integer.valueOf(parameterName), parameters);
    }

    public static void glLineWidth(float width) {
        BINDINGS.invokeVoid(BINDINGS.glLineWidth, Float.valueOf(width));
    }

    public static void glLoadIdentity() {
        BINDINGS.invokeVoid(BINDINGS.glLoadIdentity);
    }

    public static void glMatrixMode(int mode) {
        BINDINGS.invokeVoid(BINDINGS.glMatrixMode, Integer.valueOf(mode));
    }

    public static void glNewList(int list, int mode) {
        BINDINGS.invokeVoid(BINDINGS.glNewList, Integer.valueOf(list), Integer.valueOf(mode));
    }

    public static void glNormal3f(float normalX, float normalY, float normalZ) {
        BINDINGS.invokeVoid(BINDINGS.glNormal3f, Float.valueOf(normalX), Float.valueOf(normalY), Float.valueOf(normalZ));
    }

    public static void glNormalPointer(int type, int stride, long pointer) {
        BINDINGS.invokeVoid(BINDINGS.glNormalPointerOffset, Integer.valueOf(type), Integer.valueOf(stride), Long.valueOf(pointer));
    }

    public static void glNormalPointer(int stride, ByteBuffer pointer) {
        BINDINGS.invokeVoid(BINDINGS.glNormalPointerBuffer, Integer.valueOf(GL_BYTE), Integer.valueOf(stride), pointer);
    }

    public static void glOrtho(double left, double right, double bottom, double top, double nearValue, double farValue) {
        BINDINGS.invokeVoid(BINDINGS.glOrtho, Double.valueOf(left), Double.valueOf(right), Double.valueOf(bottom), Double.valueOf(top), Double.valueOf(nearValue), Double.valueOf(farValue));
    }

    public static void glPixelStorei(int parameterName, int parameter) {
        BINDINGS.invokeVoid(BINDINGS.glPixelStorei, Integer.valueOf(parameterName), Integer.valueOf(parameter));
    }

    public static void glPolygonOffset(float factor, float units) {
        BINDINGS.invokeVoid(BINDINGS.glPolygonOffset, Float.valueOf(factor), Float.valueOf(units));
    }

    public static void glPopMatrix() {
        BINDINGS.invokeVoid(BINDINGS.glPopMatrix);
    }

    public static void glPushMatrix() {
        BINDINGS.invokeVoid(BINDINGS.glPushMatrix);
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        BINDINGS.invokeVoid(
                BINDINGS.glReadPixels,
                Integer.valueOf(x),
                Integer.valueOf(y),
                Integer.valueOf(width),
                Integer.valueOf(height),
                Integer.valueOf(format),
                Integer.valueOf(type),
                pixels);
    }

    public static void glRotatef(float angle, float x, float y, float z) {
        BINDINGS.invokeVoid(BINDINGS.glRotatef, Float.valueOf(angle), Float.valueOf(x), Float.valueOf(y), Float.valueOf(z));
    }

    public static void glScaled(double x, double y, double z) {
        BINDINGS.invokeVoid(BINDINGS.glScaled, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z));
    }

    public static void glScalef(float x, float y, float z) {
        BINDINGS.invokeVoid(BINDINGS.glScalef, Float.valueOf(x), Float.valueOf(y), Float.valueOf(z));
    }

    public static void glShadeModel(int mode) {
        BINDINGS.invokeVoid(BINDINGS.glShadeModel, Integer.valueOf(mode));
    }

    public static void glTexCoordPointer(int size, int type, int stride, long pointer) {
        BINDINGS.invokeVoid(BINDINGS.glTexCoordPointerOffset, Integer.valueOf(size), Integer.valueOf(type), Integer.valueOf(stride), Long.valueOf(pointer));
    }

    public static void glTexCoordPointer(int size, int stride, FloatBuffer pointer) {
        BINDINGS.invokeVoid(BINDINGS.glTexCoordPointerBuffer, Integer.valueOf(size), Integer.valueOf(GL_FLOAT), Integer.valueOf(stride), pointer);
    }

    public static void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        BINDINGS.invokeVoid(
                BINDINGS.glTexImage2D,
                Integer.valueOf(target),
                Integer.valueOf(level),
                Integer.valueOf(internalFormat),
                Integer.valueOf(width),
                Integer.valueOf(height),
                Integer.valueOf(border),
                Integer.valueOf(format),
                Integer.valueOf(type),
                pixels);
    }

    public static void glTexParameteri(int target, int parameterName, int parameter) {
        BINDINGS.invokeVoid(BINDINGS.glTexParameteri, Integer.valueOf(target), Integer.valueOf(parameterName), Integer.valueOf(parameter));
    }

    public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, ByteBuffer pixels) {
        BINDINGS.invokeVoid(
                BINDINGS.glTexSubImage2D,
                Integer.valueOf(target),
                Integer.valueOf(level),
                Integer.valueOf(xOffset),
                Integer.valueOf(yOffset),
                Integer.valueOf(width),
                Integer.valueOf(height),
                Integer.valueOf(format),
                Integer.valueOf(type),
                pixels);
    }

    public static void glTranslatef(float x, float y, float z) {
        BINDINGS.invokeVoid(BINDINGS.glTranslatef, Float.valueOf(x), Float.valueOf(y), Float.valueOf(z));
    }

    public static void glVertexPointer(int size, int type, int stride, long pointer) {
        BINDINGS.invokeVoid(BINDINGS.glVertexPointerOffset, Integer.valueOf(size), Integer.valueOf(type), Integer.valueOf(stride), Long.valueOf(pointer));
    }

    public static void glVertexPointer(int size, int stride, FloatBuffer pointer) {
        BINDINGS.invokeVoid(BINDINGS.glVertexPointerBuffer, Integer.valueOf(size), Integer.valueOf(GL_FLOAT), Integer.valueOf(stride), pointer);
    }

    public static void glViewport(int x, int y, int width, int height) {
        BINDINGS.invokeVoid(BINDINGS.glViewport, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height));
    }

    private static final class Bindings {
        private final Method glAlphaFunc;
        private final Method glBindTexture;
        private final Method glBlendFunc;
        private final Method glCallList;
        private final Method glCallLists;
        private final Method glClear;
        private final Method glClearColor;
        private final Method glClearDepth;
        private final Method glColor3f;
        private final Method glColor4f;
        private final Method glColorMask;
        private final Method glColorMaterial;
        private final Method glColorPointerBuffer;
        private final Method glColorPointerOffset;
        private final Method glCullFace;
        private final Method glDeleteLists;
        private final Method glDeleteTexturesSingle;
        private final Method glDeleteTexturesBuffer;
        private final Method glDepthFunc;
        private final Method glDepthMask;
        private final Method glDisable;
        private final Method glDisableClientState;
        private final Method glDrawArrays;
        private final Method glEnable;
        private final Method glEnableClientState;
        private final Method glEndList;
        private final Method glFog;
        private final Method glFogf;
        private final Method glFogi;
        private final Method glGenLists;
        private final Method glGenTextures;
        private final Method glGetError;
        private final Method glGetFloat;
        private final Method glGetString;
        private final Method glLight;
        private final Method glLightModel;
        private final Method glLineWidth;
        private final Method glLoadIdentity;
        private final Method glMatrixMode;
        private final Method glNewList;
        private final Method glNormal3f;
        private final Method glNormalPointerBuffer;
        private final Method glNormalPointerOffset;
        private final Method glOrtho;
        private final Method glPixelStorei;
        private final Method glPolygonOffset;
        private final Method glPopMatrix;
        private final Method glPushMatrix;
        private final Method glReadPixels;
        private final Method glRotatef;
        private final Method glScaled;
        private final Method glScalef;
        private final Method glShadeModel;
        private final Method glTexCoordPointerBuffer;
        private final Method glTexCoordPointerOffset;
        private final Method glTexImage2D;
        private final Method glTexParameteri;
        private final Method glTexSubImage2D;
        private final Method glTranslatef;
        private final Method glVertexPointerBuffer;
        private final Method glVertexPointerOffset;
        private final Method glViewport;

        private Bindings(Class<?> gl11Class) throws ReflectiveOperationException {
            glAlphaFunc = gl11Class.getMethod("glAlphaFunc", Integer.TYPE, Float.TYPE);
            glBindTexture = gl11Class.getMethod("glBindTexture", Integer.TYPE, Integer.TYPE);
            glBlendFunc = gl11Class.getMethod("glBlendFunc", Integer.TYPE, Integer.TYPE);
            glCallList = gl11Class.getMethod("glCallList", Integer.TYPE);
            glCallLists = gl11Class.getMethod("glCallLists", IntBuffer.class);
            glClear = gl11Class.getMethod("glClear", Integer.TYPE);
            glClearColor = gl11Class.getMethod("glClearColor", Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE);
            glClearDepth = gl11Class.getMethod("glClearDepth", Double.TYPE);
            glColor3f = gl11Class.getMethod("glColor3f", Float.TYPE, Float.TYPE, Float.TYPE);
            glColor4f = gl11Class.getMethod("glColor4f", Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE);
            glColorMask = gl11Class.getMethod("glColorMask", Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE);
            glColorMaterial = gl11Class.getMethod("glColorMaterial", Integer.TYPE, Integer.TYPE);
            glColorPointerBuffer = gl11Class.getMethod("glColorPointer", Integer.TYPE, Integer.TYPE, Integer.TYPE, ByteBuffer.class);
            glColorPointerOffset = gl11Class.getMethod("glColorPointer", Integer.TYPE, Integer.TYPE, Integer.TYPE, Long.TYPE);
            glCullFace = gl11Class.getMethod("glCullFace", Integer.TYPE);
            glDeleteLists = gl11Class.getMethod("glDeleteLists", Integer.TYPE, Integer.TYPE);
            glDeleteTexturesSingle = gl11Class.getMethod("glDeleteTextures", Integer.TYPE);
            glDeleteTexturesBuffer = gl11Class.getMethod("glDeleteTextures", IntBuffer.class);
            glDepthFunc = gl11Class.getMethod("glDepthFunc", Integer.TYPE);
            glDepthMask = gl11Class.getMethod("glDepthMask", Boolean.TYPE);
            glDisable = gl11Class.getMethod("glDisable", Integer.TYPE);
            glDisableClientState = gl11Class.getMethod("glDisableClientState", Integer.TYPE);
            glDrawArrays = gl11Class.getMethod("glDrawArrays", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            glEnable = gl11Class.getMethod("glEnable", Integer.TYPE);
            glEnableClientState = gl11Class.getMethod("glEnableClientState", Integer.TYPE);
            glEndList = gl11Class.getMethod("glEndList");
            glFog = resolveMethod(gl11Class, new String[] {"glFogfv", "glFog"}, Integer.TYPE, FloatBuffer.class);
            glFogf = gl11Class.getMethod("glFogf", Integer.TYPE, Float.TYPE);
            glFogi = gl11Class.getMethod("glFogi", Integer.TYPE, Integer.TYPE);
            glGenLists = gl11Class.getMethod("glGenLists", Integer.TYPE);
            glGenTextures = gl11Class.getMethod("glGenTextures", IntBuffer.class);
            glGetError = gl11Class.getMethod("glGetError");
            glGetFloat = resolveMethod(gl11Class, new String[] {"glGetFloatv", "glGetFloat"}, Integer.TYPE, FloatBuffer.class);
            glGetString = gl11Class.getMethod("glGetString", Integer.TYPE);
            glLight = resolveMethod(gl11Class, new String[] {"glLightfv", "glLight"}, Integer.TYPE, Integer.TYPE, FloatBuffer.class);
            glLightModel = resolveMethod(gl11Class, new String[] {"glLightModelfv", "glLightModel"}, Integer.TYPE, FloatBuffer.class);
            glLineWidth = gl11Class.getMethod("glLineWidth", Float.TYPE);
            glLoadIdentity = gl11Class.getMethod("glLoadIdentity");
            glMatrixMode = gl11Class.getMethod("glMatrixMode", Integer.TYPE);
            glNewList = gl11Class.getMethod("glNewList", Integer.TYPE, Integer.TYPE);
            glNormal3f = gl11Class.getMethod("glNormal3f", Float.TYPE, Float.TYPE, Float.TYPE);
            glNormalPointerBuffer = gl11Class.getMethod("glNormalPointer", Integer.TYPE, Integer.TYPE, ByteBuffer.class);
            glNormalPointerOffset = gl11Class.getMethod("glNormalPointer", Integer.TYPE, Integer.TYPE, Long.TYPE);
            glOrtho = gl11Class.getMethod("glOrtho", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
            glPixelStorei = gl11Class.getMethod("glPixelStorei", Integer.TYPE, Integer.TYPE);
            glPolygonOffset = gl11Class.getMethod("glPolygonOffset", Float.TYPE, Float.TYPE);
            glPopMatrix = gl11Class.getMethod("glPopMatrix");
            glPushMatrix = gl11Class.getMethod("glPushMatrix");
            glReadPixels = gl11Class.getMethod("glReadPixels", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, ByteBuffer.class);
            glRotatef = gl11Class.getMethod("glRotatef", Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE);
            glScaled = gl11Class.getMethod("glScaled", Double.TYPE, Double.TYPE, Double.TYPE);
            glScalef = gl11Class.getMethod("glScalef", Float.TYPE, Float.TYPE, Float.TYPE);
            glShadeModel = gl11Class.getMethod("glShadeModel", Integer.TYPE);
            glTexCoordPointerBuffer = gl11Class.getMethod("glTexCoordPointer", Integer.TYPE, Integer.TYPE, Integer.TYPE, FloatBuffer.class);
            glTexCoordPointerOffset = gl11Class.getMethod("glTexCoordPointer", Integer.TYPE, Integer.TYPE, Integer.TYPE, Long.TYPE);
            glTexImage2D = gl11Class.getMethod("glTexImage2D", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, ByteBuffer.class);
            glTexParameteri = gl11Class.getMethod("glTexParameteri", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            glTexSubImage2D = gl11Class.getMethod("glTexSubImage2D", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, ByteBuffer.class);
            glTranslatef = gl11Class.getMethod("glTranslatef", Float.TYPE, Float.TYPE, Float.TYPE);
            glVertexPointerBuffer = gl11Class.getMethod("glVertexPointer", Integer.TYPE, Integer.TYPE, Integer.TYPE, FloatBuffer.class);
            glVertexPointerOffset = gl11Class.getMethod("glVertexPointer", Integer.TYPE, Integer.TYPE, Integer.TYPE, Long.TYPE);
            glViewport = gl11Class.getMethod("glViewport", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        }

        public static Bindings load() {
            try {
                return new Bindings(Class.forName("org.lwjgl.opengl.GL11"));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to initialize LegacyGL11 bindings", exception);
            }
        }

        private static Method resolveMethod(Class<?> owner, String[] names, Class<?>... parameterTypes)
                throws NoSuchMethodException {
            NoSuchMethodException lastException = null;
            for (String name : names) {
                try {
                    return owner.getMethod(name, parameterTypes);
                } catch (NoSuchMethodException exception) {
                    lastException = exception;
                }
            }

            if (lastException != null) {
                throw lastException;
            }

            throw new NoSuchMethodException(owner.getName());
        }

        public void invokeVoid(Method method, Object... arguments) {
            try {
                method.invoke(null, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Failed to invoke LegacyGL11 binding", exception);
            } catch (InvocationTargetException exception) {
                throw rethrow(exception.getCause());
            }
        }

        public int invokeInt(Method method, Object... arguments) {
            Object value = invoke(method, arguments);
            return value instanceof Number ? ((Number) value).intValue() : 0;
        }

        public long invokeLong(Method method, Object... arguments) {
            Object value = invoke(method, arguments);
            return value instanceof Number ? ((Number) value).longValue() : 0L;
        }

        public String invokeString(Method method, Object... arguments) {
            Object value = invoke(method, arguments);
            return value instanceof String ? (String) value : null;
        }

        private Object invoke(Method method, Object... arguments) {
            try {
                return method.invoke(null, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Failed to invoke LegacyGL11 binding", exception);
            } catch (InvocationTargetException exception) {
                throw rethrow(exception.getCause());
            }
        }

        private RuntimeException rethrow(Throwable cause) {
            if (cause instanceof RuntimeException) {
                return (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            return new IllegalStateException("LegacyGL11 binding failed", cause);
        }
    }
}