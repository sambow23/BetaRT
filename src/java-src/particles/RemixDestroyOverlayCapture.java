import mcrtx.bridge.RemixParticleOverlayBridge;
import mcrtx.bridge.RemixLifecycleBridge;

public final class RemixDestroyOverlayCapture {
    private RemixDestroyOverlayCapture() {
    }

    public static void onDestroyOverlayRender(int blockX, int blockY, int blockZ, float destroyProgress) {
        fd attachedWorld = RemixChunkCapture.attachedWorld();
        if (!RemixLifecycleBridge.isInitialized() || attachedWorld == null) {
            return;
        }

        int blockId = attachedWorld.a(blockX, blockY, blockZ);
        if (blockId <= 0 || blockId >= uu.m.length) {
            return;
        }

        uu blockDefinition = uu.m[blockId];
        if (blockDefinition == null) {
            return;
        }

        int destroyStage = (int) (destroyProgress * 10.0f);
        if (destroyStage < 0) {
            destroyStage = 0;
        } else if (destroyStage > 9) {
            destroyStage = 9;
        }

        RemixParticleOverlayBridge.captureDestroyOverlay(
                blockX,
                blockY,
                blockZ,
                blockId,
                attachedWorld.e(blockX, blockY, blockZ),
                blockDefinition.b(),
                destroyStage);
    }
}
