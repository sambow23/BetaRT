package org.lwjgl.opengl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GLContext {
    private static final ContextCapabilities DEFAULT_CAPABILITIES = new ContextCapabilities(false, false, false);
    private static final Bindings BINDINGS = Bindings.load();

    private GLContext() {
    }

    public static ContextCapabilities getCapabilities() {
        if (!BINDINGS.hasCurrentContext()) {
            return DEFAULT_CAPABILITIES;
        }

        Object capabilities = BINDINGS.getCapabilities();
        if (capabilities == null) {
            return DEFAULT_CAPABILITIES;
        }

        return new ContextCapabilities(
                BINDINGS.readBoolean(capabilities, "GL_ARB_occlusion_query"),
                BINDINGS.readBoolean(capabilities, "GL_ARB_vertex_buffer_object"),
                BINDINGS.readBoolean(capabilities, "GL_NV_fog_distance"));
    }

    private static final class Bindings {
        private final Method glfwGetCurrentContext;
        private final Method glGetCapabilities;

        private Bindings(Method glfwGetCurrentContext, Method glGetCapabilities) {
            this.glfwGetCurrentContext = glfwGetCurrentContext;
            this.glGetCapabilities = glGetCapabilities;
        }

        public static Bindings load() {
            try {
                Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
                Class<?> glClass = Class.forName("org.lwjgl.opengl.GL");
                return new Bindings(
                        glfwClass.getMethod("glfwGetCurrentContext"),
                        glClass.getMethod("getCapabilities"));
            } catch (ReflectiveOperationException exception) {
                return new Bindings(null, null);
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

        public Object getCapabilities() {
            if (glGetCapabilities == null) {
                return null;
            }
            try {
                return glGetCapabilities.invoke(null);
            } catch (Exception exception) {
                return null;
            }
        }

        public boolean readBoolean(Object capabilities, String fieldName) {
            try {
                Field field = capabilities.getClass().getField(fieldName);
                return field.getBoolean(capabilities);
            } catch (Exception exception) {
                return false;
            }
        }
    }
}