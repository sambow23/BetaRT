import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxGraphicsSettings;
import mcrtx.bridge.RemixSceneBridge;
import mcrtx.bridge.RemixLifecycleBridge;

public final class RemixCloudCapture {
    private static boolean gameCloudLayerClearedForRemixClouds;

    private RemixCloudCapture() {
    }

    public static void onCloudRender(net.minecraft.client.Minecraft minecraft, fd world, int cloudTick, float partialTicks, boolean fancy) {
        boolean initialized = RemixLifecycleBridge.isInitialized();
        boolean remixAtmosphereCloudsEnabled = McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled();
        if (!McrtxGraphicsSettings.shouldSubmitGameCloudLayer(initialized, remixAtmosphereCloudsEnabled)) {
            if (initialized && remixAtmosphereCloudsEnabled && !gameCloudLayerClearedForRemixClouds) {
                RemixSceneBridge.clearCloudLayer();
                gameCloudLayerClearedForRemixClouds = true;
            }
            return;
        }
        gameCloudLayerClearedForRemixClouds = false;

        long renderStartNanos = System.nanoTime();

        long atmosphereEndNanos = System.nanoTime();

        if (minecraft == null || world == null || world.t == null || world.t.c || minecraft.i == null) {
            RemixSceneBridge.clearCloudLayer();
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onCloudRender.atmosphereState",
                    atmosphereEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onCloudRender.clearLayer",
                    System.nanoTime() - atmosphereEndNanos);
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
        float celestialAngle = world.b(partialTicks);

        RemixSceneBridge.updateCloudLayer(
                fancy,
                cameraX,
                cameraY,
                cameraZ,
                cloudHeight,
                cloudScroll,
            celestialAngle,
                colorR,
                colorG,
                colorB);
        long submitLayerEndNanos = System.nanoTime();

        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onCloudRender.atmosphereState",
            atmosphereEndNanos - renderStartNanos);
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onCloudRender.submitLayer",
            submitLayerEndNanos - atmosphereEndNanos);
    }
}
