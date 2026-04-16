import mcrtx.bridge.MinecraftRenderHooks;

public final class RemixCameraState {
    static float cameraPositionX;
    static float cameraPositionY;
    static float cameraPositionZ;
    static float cameraForwardX = 0.0f;
    static float cameraForwardY = 0.0f;
    static float cameraForwardZ = 1.0f;
    static float cameraUpX = 0.0f;
    static float cameraUpY = 1.0f;
    static float cameraUpZ = 0.0f;
    static float cameraRightX = 1.0f;
    static float cameraRightY = 0.0f;
    static float cameraRightZ = 0.0f;

    private RemixCameraState() {
    }

    public static void onCamera(ls entity, float partialTicks, int width, int height, float farPlane) {
        if (entity == null) {
            return;
        }
        bt position = entity.e(partialTicks);
        bt forward = entity.f(partialTicks);
        cameraPositionX = (float) position.a;
        cameraPositionY = (float) (position.b + (double) entity.w());
        cameraPositionZ = (float) position.c;

        float fx = (float) forward.a;
        float fy = (float) forward.b;
        float fz = (float) forward.c;
        float forwardLength = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (forwardLength > 0.0f) {
            cameraForwardX = fx / forwardLength;
            cameraForwardY = fy / forwardLength;
            cameraForwardZ = fz / forwardLength;
        }

        float upx = 0.0f;
        float upy = 1.0f;
        float upz = 0.0f;
        if (Math.abs(cameraForwardY) > 0.99f) {
            upx = 0.0f;
            upy = 0.0f;
            upz = 1.0f;
        }

        float rx = cameraForwardY * upz - cameraForwardZ * upy;
        float ry = cameraForwardZ * upx - cameraForwardX * upz;
        float rz = cameraForwardX * upy - cameraForwardY * upx;
        float rightLength = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rightLength > 0.0f) {
            cameraRightX = rx / rightLength;
            cameraRightY = ry / rightLength;
            cameraRightZ = rz / rightLength;
        }

        float ux = cameraRightY * cameraForwardZ - cameraRightZ * cameraForwardY;
        float uy = cameraRightZ * cameraForwardX - cameraRightX * cameraForwardZ;
        float uz = cameraRightX * cameraForwardY - cameraRightY * cameraForwardX;
        float upLength = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (upLength > 0.0f) {
            cameraUpX = ux / upLength;
            cameraUpY = uy / upLength;
            cameraUpZ = uz / upLength;
        }

        float aspect = height <= 0 ? 1.0f : (float) width / (float) height;
        MinecraftRenderHooks.updateCamera(
                cameraPositionX,
                cameraPositionY,
                cameraPositionZ,
                cameraForwardX,
                cameraForwardY,
                cameraForwardZ,
                70.0f,
                aspect,
                0.05f,
                farPlane * 2.0f);
    }

    static float[] buildInverseViewMatrix() {
        float[] matrix = new float[16];
        matrix[0] = cameraRightX;
        matrix[1] = cameraRightY;
        matrix[2] = cameraRightZ;
        matrix[3] = 0.0f;
        matrix[4] = cameraUpX;
        matrix[5] = cameraUpY;
        matrix[6] = cameraUpZ;
        matrix[7] = 0.0f;
        matrix[8] = -cameraForwardX;
        matrix[9] = -cameraForwardY;
        matrix[10] = -cameraForwardZ;
        matrix[11] = 0.0f;
        matrix[12] = cameraPositionX;
        matrix[13] = cameraPositionY;
        matrix[14] = cameraPositionZ;
        matrix[15] = 1.0f;
        return matrix;
    }
}
