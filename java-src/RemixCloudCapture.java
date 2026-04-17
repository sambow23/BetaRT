import mcrtx.bridge.MinecraftRenderHooks;

public final class RemixCloudCapture {
    private RemixCloudCapture() {
    }

    public static void onCloudRender(net.minecraft.client.Minecraft minecraft, fd world, int cloudTick, float partialTicks, boolean fancy) {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        if (minecraft == null || world == null || world.t == null || world.t.c || minecraft.i == null) {
            MinecraftRenderHooks.clearCloudLayer();
            return;
        }

        float colorR = 1.0f;
        float colorG = 1.0f;
        float colorB = 1.0f;

        ls entity = minecraft.i;
        float cameraX = (float) (entity.aJ + (entity.aM - entity.aJ) * (double) partialTicks);
        float cameraY = (float) (entity.bm + (entity.aN - entity.bm) * (double) partialTicks);
        float cameraZ = (float) (entity.aL + (entity.aO - entity.aL) * (double) partialTicks);
        float cloudHeight = world.t.d() + 0.33f;
        float cloudScroll = ((float) cloudTick + partialTicks) * 0.03f;

        MinecraftRenderHooks.updateCloudLayer(
                fancy,
                cameraX,
                cameraY,
                cameraZ,
                cloudHeight,
                cloudScroll,
                colorR,
                colorG,
                colorB);
    }
}
