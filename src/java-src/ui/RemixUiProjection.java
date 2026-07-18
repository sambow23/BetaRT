import java.nio.FloatBuffer;
import mcrtx.lwjglshim.OpenGlCompat;
import org.lwjgl.BufferUtils;

final class RemixUiProjection {
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final int GL_PROJECTION_MATRIX = 0x0BA7;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final float UI_DEPTH_EYE_NEAR = -1900.0f;
    private static final float UI_DEPTH_EYE_FAR = -2100.0f;
    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);

    static final class ProjectedPoint {
        final float x;
        final float y;
        final float depth;
        final boolean projectable;
        final boolean insideClip;

        ProjectedPoint(float x, float y, float depth, boolean projectable, boolean insideClip) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.projectable = projectable;
            this.insideClip = insideClip;
        }
    }

    private RemixUiProjection() {
    }

    static float[] captureModelView() {
        return captureMatrix(GL_MODELVIEW_MATRIX);
    }

    static float[] captureProjection() {
        return captureMatrix(GL_PROJECTION_MATRIX);
    }

    static int captureCurrentColorPacked() {
        COLOR_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_CURRENT_COLOR, COLOR_BUFFER)) {
            return 0xFFFFFFFF;
        }
        int r = clampColorByte(COLOR_BUFFER.get(0));
        int g = clampColorByte(COLOR_BUFFER.get(1));
        int b = clampColorByte(COLOR_BUFFER.get(2));
        int a = clampColorByte(COLOR_BUFFER.get(3));
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    static float eyeSpaceZ(float[] modelView, float x, float y, float z) {
        return modelView[2] * x + modelView[6] * y + modelView[10] * z + modelView[14];
    }

    static ProjectedPoint projectToScreenPixels(float[] mvp, float x, float y, float z) {
        float clipX = mvp[0] * x + mvp[4] * y + mvp[8] * z + mvp[12];
        float clipY = mvp[1] * x + mvp[5] * y + mvp[9] * z + mvp[13];
        float clipZ = mvp[2] * x + mvp[6] * y + mvp[10] * z + mvp[14];
        float clipW = mvp[3] * x + mvp[7] * y + mvp[11] * z + mvp[15];

        boolean finite = Float.isFinite(clipX)
                && Float.isFinite(clipY)
                && Float.isFinite(clipZ)
                && Float.isFinite(clipW);
        boolean projectable = finite && clipW > 1.0e-6f;
        if (!projectable) {
            return new ProjectedPoint(0.0f, 0.0f, 1.0f, false, false);
        }

        boolean insideClip = clipX >= -clipW
                && clipX <= clipW
                && clipY >= -clipW
                && clipY <= clipW
                && clipZ >= -clipW
                && clipZ <= clipW;
        float invW = 1.0f / clipW;
        float ndcX = clipX * invW;
        float ndcY = clipY * invW;
        float ndcZ = clipZ * invW;
        float depth = ndcZ * 0.5f + 0.5f;
        if (depth < 0.0f) {
            depth = 0.0f;
        } else if (depth > 1.0f) {
            depth = 1.0f;
        }

        return new ProjectedPoint(
                (ndcX * 0.5f + 0.5f) * RemixUiCaptureSession.displayWidth(),
                (0.5f - ndcY * 0.5f) * RemixUiCaptureSession.displayHeight(),
                depth,
                true,
                insideClip);
    }

    static float mapUiDepth(float eyeZ) {
        float depth = (eyeZ - UI_DEPTH_EYE_NEAR) / (UI_DEPTH_EYE_FAR - UI_DEPTH_EYE_NEAR);
        if (depth < 0.0f) {
            return 0.0f;
        }
        if (depth > 1.0f) {
            return 1.0f;
        }
        return depth;
    }

    private static float[] captureMatrix(int matrixName) {
        MATRIX_BUFFER.clear();
        if (!OpenGlCompat.getFloat(matrixName, MATRIX_BUFFER)) {
            return null;
        }
        float[] matrix = new float[16];
        MATRIX_BUFFER.get(matrix);
        return matrix;
    }

    private static int clampColorByte(float value) {
        int scaled = (int) (value * 255.0f + 0.5f);
        if (scaled < 0) {
            return 0;
        }
        if (scaled > 255) {
            return 255;
        }
        return scaled;
    }
}
