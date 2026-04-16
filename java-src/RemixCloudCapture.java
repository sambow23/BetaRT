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

        bt cloudColor = world.c(partialTicks);
        float colorR = (float) cloudColor.a;
        float colorG = (float) cloudColor.b;
        float colorB = (float) cloudColor.c;
        if (minecraft.z.g) {
            float grayscale = (colorR * 30.0f + colorG * 59.0f + colorB * 11.0f) / 100.0f;
            float greenWeighted = (colorR * 30.0f + colorG * 70.0f) / 100.0f;
            float blueWeighted = (colorR * 30.0f + colorB * 70.0f) / 100.0f;
            colorR = grayscale;
            colorG = greenWeighted;
            colorB = blueWeighted;
        }

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
