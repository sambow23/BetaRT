package mcrtx.lwjglshim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class LegacyAL10 {
    private static final Bindings BINDINGS = Bindings.load();

    private LegacyAL10() {
    }

    public static void alBufferData(int buffer, int format, ByteBuffer data, int frequency) {
        BINDINGS.invokeVoid(BINDINGS.alBufferData, Integer.valueOf(buffer), Integer.valueOf(format), data, Integer.valueOf(frequency));
    }

    public static void alDeleteBuffers(IntBuffer buffers) {
        BINDINGS.invokeVoid(BINDINGS.alDeleteBuffers, buffers);
    }

    public static void alDeleteSources(IntBuffer sources) {
        BINDINGS.invokeVoid(BINDINGS.alDeleteSources, sources);
    }

    public static void alGenBuffers(IntBuffer buffers) {
        BINDINGS.invokeVoid(BINDINGS.alGenBuffers, buffers);
    }

    public static void alGenSources(IntBuffer sources) {
        BINDINGS.invokeVoid(BINDINGS.alGenSources, sources);
    }

    public static int alGetError() {
        return BINDINGS.invokeInt(BINDINGS.alGetError);
    }

    public static int alGetSourcei(int source, int parameter) {
        return BINDINGS.invokeInt(BINDINGS.alGetSourcei, Integer.valueOf(source), Integer.valueOf(parameter));
    }

    public static void alListener(int parameter, FloatBuffer values) {
        BINDINGS.invokeVoid(BINDINGS.alListenerfv, Integer.valueOf(parameter), values);
    }

    public static void alListenerf(int parameter, float value) {
        BINDINGS.invokeVoid(BINDINGS.alListenerf, Integer.valueOf(parameter), Float.valueOf(value));
    }

    public static void alSource(int source, int parameter, FloatBuffer values) {
        BINDINGS.invokeVoid(BINDINGS.alSourcefv, Integer.valueOf(source), Integer.valueOf(parameter), values);
    }

    public static void alSourcef(int source, int parameter, float value) {
        BINDINGS.invokeVoid(BINDINGS.alSourcef, Integer.valueOf(source), Integer.valueOf(parameter), Float.valueOf(value));
    }

    public static void alSourcei(int source, int parameter, int value) {
        BINDINGS.invokeVoid(BINDINGS.alSourcei, Integer.valueOf(source), Integer.valueOf(parameter), Integer.valueOf(value));
    }

    public static void alSourcePause(int source) {
        BINDINGS.invokeVoid(BINDINGS.alSourcePause, Integer.valueOf(source));
    }

    public static void alSourcePause(IntBuffer sources) {
        BINDINGS.invokeVoid(BINDINGS.alSourcePausev, sources);
    }

    public static void alSourcePlay(int source) {
        BINDINGS.invokeVoid(BINDINGS.alSourcePlay, Integer.valueOf(source));
    }

    public static void alSourcePlay(IntBuffer sources) {
        BINDINGS.invokeVoid(BINDINGS.alSourcePlayv, sources);
    }

    public static void alSourceQueueBuffers(int source, IntBuffer buffers) {
        BINDINGS.invokeVoid(BINDINGS.alSourceQueueBuffers, Integer.valueOf(source), buffers);
    }

    public static void alSourceRewind(int source) {
        BINDINGS.invokeVoid(BINDINGS.alSourceRewind, Integer.valueOf(source));
    }

    public static void alSourceRewind(IntBuffer sources) {
        BINDINGS.invokeVoid(BINDINGS.alSourceRewindv, sources);
    }

    public static void alSourceStop(int source) {
        BINDINGS.invokeVoid(BINDINGS.alSourceStop, Integer.valueOf(source));
    }

    public static void alSourceStop(IntBuffer sources) {
        BINDINGS.invokeVoid(BINDINGS.alSourceStopv, sources);
    }

    public static void alSourceUnqueueBuffers(int source, IntBuffer buffers) {
        BINDINGS.invokeVoid(BINDINGS.alSourceUnqueueBuffers, Integer.valueOf(source), buffers);
    }

    private static final class Bindings {
        private final Method alBufferData;
        private final Method alDeleteBuffers;
        private final Method alDeleteSources;
        private final Method alGenBuffers;
        private final Method alGenSources;
        private final Method alGetError;
        private final Method alGetSourcei;
        private final Method alListenerf;
        private final Method alListenerfv;
        private final Method alSourcef;
        private final Method alSourcefv;
        private final Method alSourcei;
        private final Method alSourcePause;
        private final Method alSourcePausev;
        private final Method alSourcePlay;
        private final Method alSourcePlayv;
        private final Method alSourceQueueBuffers;
        private final Method alSourceRewind;
        private final Method alSourceRewindv;
        private final Method alSourceStop;
        private final Method alSourceStopv;
        private final Method alSourceUnqueueBuffers;

        private Bindings(Class<?> al10Class) throws ReflectiveOperationException {
            alBufferData = al10Class.getMethod("alBufferData", Integer.TYPE, Integer.TYPE, ByteBuffer.class, Integer.TYPE);
            alDeleteBuffers = al10Class.getMethod("alDeleteBuffers", IntBuffer.class);
            alDeleteSources = al10Class.getMethod("alDeleteSources", IntBuffer.class);
            alGenBuffers = al10Class.getMethod("alGenBuffers", IntBuffer.class);
            alGenSources = al10Class.getMethod("alGenSources", IntBuffer.class);
            alGetError = al10Class.getMethod("alGetError");
            alGetSourcei = al10Class.getMethod("alGetSourcei", Integer.TYPE, Integer.TYPE);
            alListenerf = al10Class.getMethod("alListenerf", Integer.TYPE, Float.TYPE);
            alListenerfv = al10Class.getMethod("alListenerfv", Integer.TYPE, FloatBuffer.class);
            alSourcef = al10Class.getMethod("alSourcef", Integer.TYPE, Integer.TYPE, Float.TYPE);
            alSourcefv = al10Class.getMethod("alSourcefv", Integer.TYPE, Integer.TYPE, FloatBuffer.class);
            alSourcei = al10Class.getMethod("alSourcei", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            alSourcePause = al10Class.getMethod("alSourcePause", Integer.TYPE);
            alSourcePausev = al10Class.getMethod("alSourcePausev", IntBuffer.class);
            alSourcePlay = al10Class.getMethod("alSourcePlay", Integer.TYPE);
            alSourcePlayv = al10Class.getMethod("alSourcePlayv", IntBuffer.class);
            alSourceQueueBuffers = al10Class.getMethod("alSourceQueueBuffers", Integer.TYPE, IntBuffer.class);
            alSourceRewind = al10Class.getMethod("alSourceRewind", Integer.TYPE);
            alSourceRewindv = al10Class.getMethod("alSourceRewindv", IntBuffer.class);
            alSourceStop = al10Class.getMethod("alSourceStop", Integer.TYPE);
            alSourceStopv = al10Class.getMethod("alSourceStopv", IntBuffer.class);
            alSourceUnqueueBuffers = al10Class.getMethod("alSourceUnqueueBuffers", Integer.TYPE, IntBuffer.class);
        }

        public static Bindings load() {
            try {
                return new Bindings(Class.forName("org.lwjgl.openal.AL10"));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to initialize LegacyAL10 bindings", exception);
            }
        }

        public void invokeVoid(Method method, Object... arguments) {
            try {
                method.invoke(null, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Failed to invoke LegacyAL10 binding", exception);
            } catch (InvocationTargetException exception) {
                throw rethrow(exception.getCause());
            }
        }

        public int invokeInt(Method method, Object... arguments) {
            Object value = invoke(method, arguments);
            return value instanceof Number ? ((Number) value).intValue() : 0;
        }

        private Object invoke(Method method, Object... arguments) {
            try {
                return method.invoke(null, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Failed to invoke LegacyAL10 binding", exception);
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
            return new IllegalStateException("LegacyAL10 binding failed", cause);
        }
    }
}