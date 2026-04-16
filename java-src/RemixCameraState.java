import java.nio.FloatBuffer;
import mcrtx.bridge.MatrixMath;
import mcrtx.bridge.MinecraftRenderHooks;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class RemixCameraState {
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final FloatBuffer VIEW_BUFFER = BufferUtils.createFloatBuffer(16);

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

    private static float fovYDegrees = 70.0f;
    private static float aspect = 1.0f;
    private static float nearPlane = 0.05f;
    private static float farPlane = 1024.0f;

    private static boolean frameViewCaptured;
    private static final float[] frameInverseViewMatrix = new float[16];

    private RemixCameraState() {
    }

    static void onFramePresented() {
        frameViewCaptured = false;
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
        RemixCameraState.fovYDegrees = 70.0f;
        RemixCameraState.aspect = aspect;
        RemixCameraState.nearPlane = 0.05f;
        RemixCameraState.farPlane = farPlane * 2.0f;
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

    /**
     * Reads GL_MODELVIEW at the moment dynamic entities are about to be
     * rendered, stores its inverse as the authoritative camera-to-world, and
     * submits the decomposed basis to Remix so the game camera and dynamic
     * entity transforms share a single source of truth -- otherwise view bob
     * and damage tilt leak into entity world positions and cause rubber-band
     * lag behind the camera.
     *
     * Beta's view matrix at this point contains only camera rotation; the
     * world-space translation is applied per-draw (chunks via the tessellator
     * offset, entities via glTranslated). So we take the rotation basis from
     * GL (bob/tilt included) and compose it with the entity-derived camera
     * position previously stashed by {@link #onCamera} to build a full
     * camera-to-world matrix.
     */
    public static void captureFrameView() {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }
        VIEW_BUFFER.clear();
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, VIEW_BUFFER);
        float[] view = new float[16];
        VIEW_BUFFER.get(view);

        float[] inverse = MatrixMath.invertAffineColumnMajor(view);
        inverse[12] = cameraPositionX;
        inverse[13] = cameraPositionY;
        inverse[14] = cameraPositionZ;
        inverse[15] = 1.0f;
        System.arraycopy(inverse, 0, frameInverseViewMatrix, 0, 16);
        frameViewCaptured = true;

        cameraRightX = inverse[0];
        cameraRightY = inverse[1];
        cameraRightZ = inverse[2];
        cameraUpX = inverse[4];
        cameraUpY = inverse[5];
        cameraUpZ = inverse[6];
        cameraForwardX = -inverse[8];
        cameraForwardY = -inverse[9];
        cameraForwardZ = -inverse[10];

        MinecraftRenderHooks.updateCamera(
                cameraPositionX,
                cameraPositionY,
                cameraPositionZ,
                cameraForwardX,
                cameraForwardY,
                cameraForwardZ,
                fovYDegrees,
                aspect,
                nearPlane,
                farPlane);
    }

    static float[] buildInverseViewMatrix() {
        if (frameViewCaptured) {
            return frameInverseViewMatrix.clone();
        }
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
