package org.lwjgl.util.glu;

import java.lang.reflect.Method;

public final class GLU {
    private static final Method GL_FRUSTUM = loadGlFrustum();

    private GLU() {
    }

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        if (aspect == 0.0f || zNear <= 0.0f || zFar <= zNear) {
            return;
        }

        double halfHeight = Math.tan(fovy * Math.PI / 360.0) * zNear;
        double halfWidth = halfHeight * aspect;
        if (GL_FRUSTUM == null) {
            return;
        }

        try {
            GL_FRUSTUM.invoke(null, -halfWidth, halfWidth, -halfHeight, halfHeight, (double) zNear, (double) zFar);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to invoke GL11.glFrustum", exception);
        }
    }

    public static String gluErrorString(int errorCode) {
        switch (errorCode) {
            case 0:
                return "No error";
            case 1280:
                return "Invalid enum";
            case 1281:
                return "Invalid value";
            case 1282:
                return "Invalid operation";
            case 1283:
                return "Stack overflow";
            case 1284:
                return "Stack underflow";
            case 1285:
                return "Out of memory";
            case 1286:
                return "Invalid framebuffer operation";
            default:
                return "Unknown error 0x" + Integer.toHexString(errorCode);
        }
    }

    private static Method loadGlFrustum() {
        try {
            return Class.forName("org.lwjgl.opengl.GL11")
                    .getMethod("glFrustum", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}