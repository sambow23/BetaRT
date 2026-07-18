import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxDebugSettings;
import mcrtx.bridge.RemixChunkBridge;
import mcrtx.bridge.RemixSceneBridge;
import org.lwjgl.opengl.GL11;

public final class MinecraftRemixSceneHooks {
    private static boolean worldRasterRenderActive;
    private static float lastCameraPartialTicks;

    private MinecraftRemixSceneHooks() {
    }

    public static void onCamera(
            ls entity,
            float partialTicks,
            int width,
            int height,
            float farPlane,
            boolean thirdPersonActive) {
        long __perf = HookProfiler.begin();
        try {
            lastCameraPartialTicks = partialTicks;
            RemixCameraState.onCamera(entity, partialTicks, width, height, farPlane, thirdPersonActive);
        } finally {
            HookProfiler.endHook("hook.onCamera", __perf);
        }
    }

    public static void onFrameViewCaptured() {
        long __perf = HookProfiler.begin();
        try {
            RemixCameraState.captureFrameView();
        } finally {
            HookProfiler.endHook("hook.onFrameViewCaptured", __perf);
        }
    }

    public static boolean shouldRenderBoundingBox(yn frustum, eq bounds) {
        long __perf = HookProfiler.begin();
        try {
            if (bounds == null) {
                return true;
            }
            if (RemixCameraState.isWithinNoCullDistance(bounds.a, bounds.b, bounds.c, bounds.d, bounds.e, bounds.f)) {
                return true;
            }
            return frustum == null || frustum.a(bounds);
        } finally {
            HookProfiler.endHook("hook.shouldRenderBoundingBox", __perf);
        }
    }

    public static void onWorldChanged(fd world) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onWorldChanged(world);
        } finally {
            HookProfiler.endHook("hook.onWorldChanged", __perf);
        }
    }

    public static void clearWorldScene() {
        long __perf = HookProfiler.begin();
        try {
            RemixSceneBridge.clearWorldScene();
            RemixChunkBridge.resetCaptureState();
        } finally {
            HookProfiler.endHook("hook.clearWorldScene", __perf);
        }
    }

    public static void onCloudRender(
            net.minecraft.client.Minecraft minecraft,
            fd world,
            int cloudTick,
            float partialTicks,
            boolean fancy) {
        long __perf = HookProfiler.begin();
        try {
            RemixCloudCapture.onCloudRender(minecraft, world, cloudTick, partialTicks, fancy);
        } finally {
            HookProfiler.endHook("hook.onCloudRender", __perf);
        }
    }

    public static void onFogState(
            ls entity,
            boolean thickFog,
            int renderLayer,
            boolean forceStartAtCamera,
            float viewDistance,
            float colorR,
            float colorG,
            float colorB) {
        long __perf = HookProfiler.begin();
        try {
            RemixFogCapture.onFogState(
                    entity,
                    thickFog,
                    renderLayer,
                    forceStartAtCamera,
                    viewDistance,
                    colorR,
                    colorG,
                    colorB);
        } finally {
            HookProfiler.endHook("hook.onFogState", __perf);
        }
    }

    public static void drawTessellator(int mode, int first, int count) {
        if (McrtxDebugSettings.isMovingPistonVanillaSuppressionEnabled()
                && RemixDynamicEntityCapture.shouldSuppressMovingPistonVanillaDraw()) {
            return;
        }
        if (shouldSuppressWorldRasterVanillaDraw()) {
            return;
        }
        if (shouldSuppressCapturedVanillaTessellatorDraw()) {
            return;
        }
        GL11.glDrawArrays(mode, first, count);
    }

    public static void onWorldRasterRenderStart() {
        worldRasterRenderActive = true;
    }

    public static void onWorldRasterRenderEnd() {
        worldRasterRenderActive = false;
    }

    public static boolean shouldSuppressWorldRasterDisplayLists() {
        return shouldSuppressWorldRasterVanillaDraw();
    }

    static float lastCameraPartialTicks() {
        return lastCameraPartialTicks;
    }

    static void updateAtmosphereForFrameStart() {
        net.minecraft.client.Minecraft minecraft = mcrtx.bridge.RemixLifecycleBridge.getRememberedMinecraft();
        if (minecraft != null && minecraft.f != null && minecraft.f.t != null) {
            RemixSceneBridge.updateAtmosphereState(
                    minecraft.f.b(lastCameraPartialTicks),
                    minecraft.f.t instanceof wd);
        }
    }

    private static boolean shouldSuppressWorldRasterVanillaDraw() {
        return McrtxDebugSettings.isWorldRasterVanillaSuppressionEnabled()
                && worldRasterRenderActive
                && !RemixUiCapture.isActive()
                && !RemixDynamicEntityCapture.isFirstPersonActive()
                && !RemixParticleCapture.isWeatherTessellatorCaptureActive();
    }

    private static boolean shouldSuppressCapturedVanillaTessellatorDraw() {
        return RemixUiCapture.isActive()
                || RemixDynamicEntityCapture.shouldSuppressVanillaTessellatorDraw()
                || RemixParticleCapture.shouldSuppressVanillaTessellatorDraw();
    }
}
