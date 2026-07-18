import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixLifecycleBridge;
import net.minecraft.client.Minecraft;

public final class MinecraftRemixParticleHooks {
    private MinecraftRemixParticleHooks() {
    }

    public static void onDestroyOverlayRender(int blockX, int blockY, int blockZ, float destroyProgress) {
        long __perf = HookProfiler.begin();
        try {
            RemixDestroyOverlayCapture.onDestroyOverlayRender(blockX, blockY, blockZ, destroyProgress);
        } finally {
            HookProfiler.endHook("hook.onDestroyOverlayRender", __perf);
        }
    }

    public static void onBlockOutlineRender(gs player, vf movingobjectposition, int renderMode, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixBlockOutlineCapture.onBlockOutlineRender(player, movingobjectposition, renderMode, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onBlockOutlineRender", __perf);
        }
    }

    public static void onBlockOutlineRender(vf movingobjectposition, int renderMode) {
        long __perf = HookProfiler.begin();
        try {
            Minecraft minecraft = RemixLifecycleBridge.getRememberedMinecraft();
            gs player = minecraft != null && minecraft.h instanceof gs ? (gs) minecraft.h : null;
            RemixBlockOutlineCapture.onBlockOutlineRender(
                    player,
                    movingobjectposition,
                    renderMode,
                    MinecraftRemixSceneHooks.lastCameraPartialTicks());
        } finally {
            HookProfiler.endHook("hook.onBlockOutlineRender", __perf);
        }
    }

    public static void onParticleRender(
            xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.captureParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.onParticleRender", __perf);
        }
    }

    public static boolean captureParticleRender(
            xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            return RemixParticleCapture.captureParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.captureParticleRender", __perf);
        }
    }

    public static void onAnimatedParticleRender(
            xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.captureAnimatedParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.onAnimatedParticleRender", __perf);
        }
    }

    public static boolean captureAnimatedParticleRender(
            xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        long __perf = HookProfiler.begin();
        try {
            return RemixParticleCapture.captureAnimatedParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
        } finally {
            HookProfiler.endHook("hook.captureAnimatedParticleRender", __perf);
        }
    }

    public static void onWeatherTextureBind(String texturePath) {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onWeatherTextureBind(texturePath);
        } finally {
            HookProfiler.endHook("hook.onWeatherTextureBind", __perf);
        }
    }

    public static void onWeatherRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixParticleCapture.onWeatherRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onWeatherRenderEnd", __perf);
        }
    }
}
