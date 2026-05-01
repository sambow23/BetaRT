package mcrtx.lwjglshim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.IntBuffer;

public final class LegacyARBOcclusionQuery {
    private static final Bindings BINDINGS = Bindings.load();

    private LegacyARBOcclusionQuery() {
    }

    public static void glGenQueriesARB(IntBuffer ids) {
        BINDINGS.invokeVoid(BINDINGS.glGenQueriesARB, ids);
    }

    public static void glDeleteQueriesARB(IntBuffer ids) {
        BINDINGS.invokeVoid(BINDINGS.glDeleteQueriesARB, ids);
    }

    public static void glBeginQueryARB(int target, int id) {
        BINDINGS.invokeVoid(BINDINGS.glBeginQueryARB, Integer.valueOf(target), Integer.valueOf(id));
    }

    public static void glEndQueryARB(int target) {
        BINDINGS.invokeVoid(BINDINGS.glEndQueryARB, Integer.valueOf(target));
    }

    public static void glGetQueryObjectuARB(int id, int pname, IntBuffer params) {
        BINDINGS.invokeVoid(BINDINGS.glGetQueryObjectuivARB, Integer.valueOf(id), Integer.valueOf(pname), params);
    }

    private static final class Bindings {
        private final Method glGenQueriesARB;
        private final Method glDeleteQueriesARB;
        private final Method glBeginQueryARB;
        private final Method glEndQueryARB;
        private final Method glGetQueryObjectuivARB;

        private Bindings(Class<?> arbClass) throws ReflectiveOperationException {
            glGenQueriesARB = arbClass.getMethod("glGenQueriesARB", IntBuffer.class);
            glDeleteQueriesARB = arbClass.getMethod("glDeleteQueriesARB", IntBuffer.class);
            glBeginQueryARB = arbClass.getMethod("glBeginQueryARB", Integer.TYPE, Integer.TYPE);
            glEndQueryARB = arbClass.getMethod("glEndQueryARB", Integer.TYPE);
            glGetQueryObjectuivARB = arbClass.getMethod("glGetQueryObjectuivARB", Integer.TYPE, Integer.TYPE, IntBuffer.class);
        }

        public static Bindings load() {
            try {
                return new Bindings(Class.forName("org.lwjgl.opengl.ARBOcclusionQuery"));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to initialize LegacyARBOcclusionQuery bindings", exception);
            }
        }

        public void invokeVoid(Method method, Object... arguments) {
            try {
                method.invoke(null, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Failed to invoke LegacyARBOcclusionQuery binding", exception);
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
            return new IllegalStateException("LegacyARBOcclusionQuery binding failed", cause);
        }
    }
}