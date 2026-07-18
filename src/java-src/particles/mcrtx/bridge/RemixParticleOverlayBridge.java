package mcrtx.bridge;

public final class RemixParticleOverlayBridge {
    private RemixParticleOverlayBridge() {
    }

    public static synchronized void beginDestroyOverlayFrame() {
        if (RemixLifecycleBridge.isInitialized()) {
            nBeginDestroyOverlayFrame();
        }
    }

    public static synchronized void beginBlockOutlineFrame() {
        if (RemixLifecycleBridge.isInitialized()) {
            nBeginBlockOutlineFrame();
        }
    }

    public static synchronized void captureDestroyOverlay(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType,
            int destroyStage) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }
        nCaptureDestroyOverlay(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                destroyStage);
    }

    public static synchronized void captureBlockOutline(int blockX, int blockY, int blockZ) {
        if (RemixLifecycleBridge.isInitialized()) {
            nCaptureBlockOutline(blockX, blockY, blockZ);
        }
    }

    public static synchronized void beginParticleFrame() {
        if (RemixLifecycleBridge.isInitialized()) {
            nBeginParticleFrame();
        }
    }

    public static synchronized void captureParticleQuad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int colorRgba,
            int textureKind) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }
        nCaptureParticleQuad(
                x0, y0, z0, u0, v0,
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                colorRgba,
                textureKind);
    }

    private static native void nBeginDestroyOverlayFrame();
    private static native void nBeginBlockOutlineFrame();
    private static native void nCaptureDestroyOverlay(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType,
            int destroyStage);
    private static native void nCaptureBlockOutline(int blockX, int blockY, int blockZ);
    private static native void nBeginParticleFrame();
    private static native void nCaptureParticleQuad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int colorRgba,
            int textureKind);
}
