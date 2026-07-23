package mcrtx.bridge;

import org.lwjgl.opengl.GL11;

public final class RemixDynamicEntityBridge {
    private RemixDynamicEntityBridge() {
    }

    public static synchronized void beginDynamicEntityFrame() {
        if (RemixLifecycleBridge.isInitialized()) {
            nBeginDynamicEntityFrame();
        }
    }

    public static synchronized void beginDynamicEntity(int entityId, int hurtStage, int creeperFuseStage) {
        if (RemixLifecycleBridge.isInitialized()) {
            nBeginDynamicEntity(entityId, hurtStage, creeperFuseStage);
        }
    }

    public static synchronized void setDynamicEntityTexture(String texturePath) {
        if (!RemixLifecycleBridge.isInitialized() || texturePath == null || texturePath.isEmpty()) {
            return;
        }
        nSetDynamicEntityTexture(texturePath);
    }

    public static synchronized void setFirstPersonHeldItem(int itemId) {
        if (RemixLifecycleBridge.isInitialized()) {
            nSetFirstPersonHeldItem(itemId);
        }
    }

    public static synchronized void setEntityHeldTorch(
            int entityId,
            double worldX,
            double worldY,
            double worldZ,
            int itemId) {
        if (!RemixLifecycleBridge.isInitialized() || entityId < 0) {
            return;
        }
        nSetEntityHeldTorch(entityId, worldX, worldY, worldZ, itemId);
    }

    public static synchronized void setDynamicEntityBoneTransform(
            int boneIndex,
            float m00,
            float m01,
            float m02,
            double m03,
            float m10,
            float m11,
            float m12,
            double m13,
            float m20,
            float m21,
            float m22,
            double m23) {
        if (!RemixLifecycleBridge.isInitialized() || boneIndex < 0) {
            return;
        }
        nSetDynamicEntityBoneTransform(
                boneIndex,
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23);
    }

    public static synchronized void captureDynamicEntityQuad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int colorRgba,
            int boneIndex) {
        captureDynamicEntityQuad(
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                colorRgba,
                GL11.glIsEnabled(GL11.GL_BLEND),
                boneIndex);
    }

    public static synchronized void captureDynamicEntityQuad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int colorRgba,
            boolean blendEnabled,
            int boneIndex) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }
        nCaptureDynamicEntityQuad(
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                colorRgba,
                blendEnabled,
                boneIndex);
    }

    public static synchronized void captureDynamicEntityQuadBatch(
            float[] vertices,
            int quadCount,
            int colorRgba,
            int boneIndex) {
        captureDynamicEntityQuadBatch(
                vertices,
                quadCount,
                colorRgba,
                GL11.glIsEnabled(GL11.GL_BLEND),
                boneIndex);
    }

    public static synchronized void captureDynamicEntityQuadBatch(
            float[] vertices,
            int quadCount,
            int colorRgba,
            boolean blendEnabled,
            int boneIndex) {
        if (!RemixLifecycleBridge.isInitialized() || vertices == null || quadCount <= 0) {
            return;
        }
        nCaptureDynamicEntityQuadBatch(
                vertices,
                quadCount,
                colorRgba,
                blendEnabled,
                boneIndex);
    }

    public static synchronized void endDynamicEntity() {
        if (RemixLifecycleBridge.isInitialized()) {
            nEndDynamicEntity();
        }
    }

    private static native void nBeginDynamicEntityFrame();
    private static native void nBeginDynamicEntity(int entityId, int hurtStage, int creeperFuseStage);
    private static native void nSetDynamicEntityTexture(String texturePath);
    private static native void nSetFirstPersonHeldItem(int itemId);
    private static native void nSetEntityHeldTorch(
            int entityId,
            double worldX,
            double worldY,
            double worldZ,
            int itemId);
    private static native void nSetDynamicEntityBoneTransform(
            int boneIndex,
            float m00, float m01, float m02, double m03,
            float m10, float m11, float m12, double m13,
            float m20, float m21, float m22, double m23);
    private static native void nCaptureDynamicEntityQuad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int colorRgba,
            boolean blendEnabled,
            int boneIndex);
    private static native void nCaptureDynamicEntityQuadBatch(
            float[] vertices,
            int quadCount,
            int colorRgba,
            boolean blendEnabled,
            int boneIndex);
    private static native void nEndDynamicEntity();
}
