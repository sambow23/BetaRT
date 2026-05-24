import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.McrtxRuntimeSettings;

public final class RemixBlockOutlineCapture {
    private RemixBlockOutlineCapture() {
    }

    public static void onBlockOutlineRender(gs player, vf movingobjectposition, int renderMode, float partialTicks) {
        if (!MinecraftRenderHooks.isInitialized() || !McrtxRuntimeSettings.isBlockOutlineEnabled() || player == null || movingobjectposition == null || renderMode != 0) {
            return;
        }

        if (movingobjectposition.a != jg.a) {
            return;
        }

        fd attachedWorld = RemixChunkCapture.attachedWorld();
        if (attachedWorld == null) {
            return;
        }

        int blockX = movingobjectposition.b;
        int blockY = movingobjectposition.c;
        int blockZ = movingobjectposition.d;
        int blockId = attachedWorld.a(blockX, blockY, blockZ);
        if (blockId <= 0 || blockId >= uu.m.length || uu.m[blockId] == null) {
            return;
        }

        MinecraftRenderHooks.captureBlockOutline(blockX, blockY, blockZ);
    }
}