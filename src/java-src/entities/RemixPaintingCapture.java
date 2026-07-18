import mcrtx.bridge.ColorMath;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixDynamicEntityBridge;
import org.lwjgl.opengl.GL11;

final class RemixPaintingCapture {
    private static final String PAINTING_TEXTURE_PATH = "/art/kz.png";

    private RemixPaintingCapture() {
    }

    static void onRender(qv painting) {
        captureRender(painting);
    }

    static boolean captureRender(qv painting) {
        if (!RemixDynamicEntitySession.canCapture() || painting == null || painting.e == null) {
            return false;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return false;
        }

        RemixDynamicEntitySession.ensureFrame();
        try {
            long renderStartNanos = System.nanoTime();
            float[] modelView = RemixDynamicModelCapture.captureModelViewMatrix();
            if (modelView == null) {
                return false;
            }
            RemixCameraState.PreciseTransform modelToWorld =
                    RemixCameraState.buildModelToWorldTransform(modelView);
            long stateReadEndNanos = System.nanoTime();

            RemixDynamicEntityBridge.beginDynamicEntity(painting.aD, 0, 0);
            RemixDynamicEntityBridge.setDynamicEntityTexture(PAINTING_TEXTURE_PATH);
            RemixDynamicEntitySession.submitBoneTransform(0, modelToWorld);
            long setupEndNanos = System.nanoTime();
            captureGeometry(painting, 0);
            long captureEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPaintingRender.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPaintingRender.setupEntity",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPaintingRender.captureGeometry",
                    captureEndNanos - setupEndNanos);
            return true;
        } finally {
            RemixDynamicEntityBridge.endDynamicEntity();
        }
    }

    private static void captureGeometry(qv painting, int boneIndex) {
        iq motive = painting.e;
        float startX = -motive.B / 2.0f;
        float startY = -motive.C / 2.0f;
        float frontZ = -0.5f;
        float backZ = 0.5f;

        for (int tileX = 0; tileX < motive.B / 16; tileX++) {
            for (int tileY = 0; tileY < motive.C / 16; tileY++) {
                float maxX = startX + (tileX + 1) * 16.0f;
                float minX = startX + tileX * 16.0f;
                float maxY = startY + (tileY + 1) * 16.0f;
                float minY = startY + tileY * 16.0f;
                int segmentColor = paintingSegmentColor(
                        painting, (maxX + minX) * 0.5f, (maxY + minY) * 0.5f);

                float frontMinU = (motive.D + motive.B - tileX * 16.0f) / 256.0f;
                float frontMaxU = (motive.D + motive.B - (tileX + 1) * 16.0f) / 256.0f;
                float frontMinV = (motive.E + motive.C - tileY * 16.0f) / 256.0f;
                float frontMaxV = (motive.E + motive.C - (tileY + 1) * 16.0f) / 256.0f;
                float backMinU = 0.75f;
                float backMaxU = 0.8125f;
                float backMinV = 0.0f;
                float backMaxV = 0.0625f;
                float edgeMinU = 0.751953125f;
                float edgeMaxU = 0.751953125f;
                float edgeMinV = 0.0f;
                float edgeMaxV = 0.0625f;
                float sideMinU = 0.001953125f;
                float sideMaxU = 0.001953125f;

                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        maxX, minY, frontZ, frontMaxU, frontMinV,
                        minX, minY, frontZ, frontMinU, frontMinV,
                        minX, maxY, frontZ, frontMinU, frontMaxV,
                        maxX, maxY, frontZ, frontMaxU, frontMaxV,
                        segmentColor, boneIndex);
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        maxX, maxY, backZ, backMinU, backMinV,
                        minX, maxY, backZ, backMaxU, backMinV,
                        minX, minY, backZ, backMaxU, backMaxV,
                        maxX, minY, backZ, backMinU, backMaxV,
                        segmentColor, boneIndex);
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        maxX, maxY, frontZ, backMinU, sideMinU,
                        minX, maxY, frontZ, backMaxU, sideMinU,
                        minX, maxY, backZ, backMaxU, sideMaxU,
                        maxX, maxY, backZ, backMinU, sideMaxU,
                        segmentColor, boneIndex);
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        maxX, minY, backZ, backMinU, sideMinU,
                        minX, minY, backZ, backMaxU, sideMinU,
                        minX, minY, frontZ, backMaxU, sideMaxU,
                        maxX, minY, frontZ, backMinU, sideMaxU,
                        segmentColor, boneIndex);
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        maxX, maxY, backZ, edgeMaxU, edgeMinV,
                        maxX, minY, backZ, edgeMaxU, edgeMaxV,
                        maxX, minY, frontZ, edgeMinU, edgeMaxV,
                        maxX, maxY, frontZ, edgeMinU, edgeMinV,
                        segmentColor, boneIndex);
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        minX, maxY, frontZ, edgeMaxU, edgeMinV,
                        minX, minY, frontZ, edgeMaxU, edgeMaxV,
                        minX, minY, backZ, edgeMinU, edgeMaxV,
                        minX, maxY, backZ, edgeMinU, edgeMinV,
                        segmentColor, boneIndex);
            }
        }
    }

    private static int paintingSegmentColor(qv painting, float centerX, float centerY) {
        return ColorMath.packColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
