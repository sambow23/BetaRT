package mcrtx.lwjglshim;

import java.lang.reflect.Method;
import org.lwjgl.LWJGLException;

public final class OpenAlCompat {
    private static final Object LOCK = new Object();
    private static final Bindings BINDINGS = Bindings.load();

    private static boolean created;
    private static long deviceHandle;
    private static long contextHandle;

    private OpenAlCompat() {
    }

    public static void create() throws LWJGLException {
        synchronized (LOCK) {
            if (created) {
                return;
            }
            if (!BINDINGS.available()) {
                throw new LWJGLException("LWJGL 3 OpenAL bindings are unavailable: " + BINDINGS.failureReason());
            }

            try {
                deviceHandle = BINDINGS.openDevice();
                if (deviceHandle == 0L) {
                    throw new LWJGLException("alcOpenDevice returned null");
                }

                contextHandle = BINDINGS.createContext(deviceHandle);
                if (contextHandle == 0L) {
                    throw new LWJGLException("alcCreateContext returned null");
                }

                if (!BINDINGS.makeContextCurrent(contextHandle)) {
                    throw new LWJGLException("alcMakeContextCurrent returned false");
                }

                Object alcCapabilities = BINDINGS.createAlcCapabilities(deviceHandle);
                BINDINGS.createAlCapabilities(alcCapabilities);
                created = true;
            } catch (LWJGLException exception) {
                safeDestroy();
                throw exception;
            } catch (Exception exception) {
                safeDestroy();
                throw new LWJGLException("Failed to initialize the OpenAL compatibility context", exception);
            }
        }
    }

    public static boolean isCreated() {
        synchronized (LOCK) {
            return created;
        }
    }

    public static void destroy() {
        synchronized (LOCK) {
            safeDestroy();
        }
    }

    private static void safeDestroy() {
        if (!BINDINGS.available()) {
            created = false;
            deviceHandle = 0L;
            contextHandle = 0L;
            return;
        }

        try {
            BINDINGS.clearCurrentContext();
        } catch (Exception exception) {
        }

        try {
            BINDINGS.clearCurrentCapabilities();
        } catch (Exception exception) {
        }

        if (contextHandle != 0L) {
            try {
                BINDINGS.destroyContext(contextHandle);
            } catch (Exception exception) {
            }
        }

        if (deviceHandle != 0L) {
            try {
                BINDINGS.closeDevice(deviceHandle);
            } catch (Exception exception) {
            }
        }

        created = false;
        deviceHandle = 0L;
        contextHandle = 0L;
    }

    private static final class Bindings {
        private final Method alcOpenDevice;
        private final Method alcCreateContext;
        private final Method alcMakeContextCurrent;
        private final Method alcDestroyContext;
        private final Method alcCloseDevice;
        private final Method alcCreateCapabilities;
        private final Method alCreateCapabilities;
        private final Method alSetCurrentCapabilities;
        private final String failureReason;

        private Bindings(
                Method alcOpenDevice,
                Method alcCreateContext,
                Method alcMakeContextCurrent,
                Method alcDestroyContext,
                Method alcCloseDevice,
                Method alcCreateCapabilities,
                Method alCreateCapabilities,
                Method alSetCurrentCapabilities,
                String failureReason) {
            this.alcOpenDevice = alcOpenDevice;
            this.alcCreateContext = alcCreateContext;
            this.alcMakeContextCurrent = alcMakeContextCurrent;
            this.alcDestroyContext = alcDestroyContext;
            this.alcCloseDevice = alcCloseDevice;
            this.alcCreateCapabilities = alcCreateCapabilities;
            this.alCreateCapabilities = alCreateCapabilities;
            this.alSetCurrentCapabilities = alSetCurrentCapabilities;
            this.failureReason = failureReason;
        }

        public static Bindings load() {
            try {
                Class<?> alc10Class = Class.forName("org.lwjgl.openal.ALC10");
                Class<?> alcClass = Class.forName("org.lwjgl.openal.ALC");
                Class<?> alClass = Class.forName("org.lwjgl.openal.AL");
                Class<?> alcCapabilitiesClass = Class.forName("org.lwjgl.openal.ALCCapabilities");
                Class<?> alCapabilitiesClass = Class.forName("org.lwjgl.openal.ALCapabilities");
                Class<?> intBufferClass = Class.forName("java.nio.IntBuffer");
                Method openDevice = alc10Class.getMethod("alcOpenDevice", CharSequence.class);
                Method createContext = alc10Class.getMethod("alcCreateContext", Long.TYPE, intBufferClass);
                Method makeContextCurrent = alc10Class.getMethod("alcMakeContextCurrent", Long.TYPE);
                Method destroyContext = alc10Class.getMethod("alcDestroyContext", Long.TYPE);
                Method closeDevice = alc10Class.getMethod("alcCloseDevice", Long.TYPE);
                Method createAlcCapabilities = alcClass.getMethod("createCapabilities", Long.TYPE);
                Method createAlCapabilities = alClass.getMethod("createCapabilities", alcCapabilitiesClass);
                Method setCurrentCapabilities = findOptionalMethod(alClass, alCapabilitiesClass, "setCurrentProcess", "setCurrentThread");
                return new Bindings(
                        openDevice,
                        createContext,
                        makeContextCurrent,
                        destroyContext,
                        closeDevice,
                        createAlcCapabilities,
                        createAlCapabilities,
                        setCurrentCapabilities,
                        "");
            } catch (ReflectiveOperationException exception) {
                return new Bindings(null, null, null, null, null, null, null, null, exception.toString());
            }
        }

        private static Method findOptionalMethod(Class<?> owner, Class<?> parameterType, String... methodNames) {
            for (int index = 0; index < methodNames.length; index += 1) {
                try {
                    return owner.getMethod(methodNames[index], parameterType);
                } catch (ReflectiveOperationException exception) {
                }
            }
            return null;
        }

        public boolean available() {
            return alcOpenDevice != null;
        }

        public String failureReason() {
            return failureReason;
        }

        public long openDevice() throws Exception {
            Object result = alcOpenDevice.invoke(null, new Object[] { null });
            return result instanceof Number ? ((Number) result).longValue() : 0L;
        }

        public long createContext(long device) throws Exception {
            Object result = alcCreateContext.invoke(null, Long.valueOf(device), null);
            return result instanceof Number ? ((Number) result).longValue() : 0L;
        }

        public boolean makeContextCurrent(long context) throws Exception {
            Object result = alcMakeContextCurrent.invoke(null, Long.valueOf(context));
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        }

        public void destroyContext(long context) throws Exception {
            alcDestroyContext.invoke(null, Long.valueOf(context));
        }

        public boolean closeDevice(long device) throws Exception {
            Object result = alcCloseDevice.invoke(null, Long.valueOf(device));
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        }

        public Object createAlcCapabilities(long device) throws Exception {
            return alcCreateCapabilities.invoke(null, Long.valueOf(device));
        }

        public void createAlCapabilities(Object alcCapabilities) throws Exception {
            alCreateCapabilities.invoke(null, alcCapabilities);
        }

        public void clearCurrentContext() throws Exception {
            alcMakeContextCurrent.invoke(null, Long.valueOf(0L));
        }

        public void clearCurrentCapabilities() throws Exception {
            if (alSetCurrentCapabilities != null) {
                alSetCurrentCapabilities.invoke(null, new Object[] { null });
            }
        }
    }
}