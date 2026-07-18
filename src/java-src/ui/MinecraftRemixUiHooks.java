import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixLifecycleBridge;
import net.minecraft.client.Minecraft;

public final class MinecraftRemixUiHooks {
    private MinecraftRemixUiHooks() {
    }

    public static void onUiRenderBegin(int width, int height) {
        long __perf = HookProfiler.begin();
        try {
            MinecraftRemixLifecycleHooks.markUiRenderBegin();
            if (MinecraftRemixLifecycleHooks.isStandaloneWindowMode()) {
                return;
            }
            Minecraft minecraft = RemixLifecycleBridge.getRememberedMinecraft();
            if (minecraft != null && minecraft.z != null && minecraft.z.z && minecraft.r == null) {
                RemixUiCapture.clear(width, height);
                return;
            }
            RemixUiCapture.begin(width, height);
        } finally {
            HookProfiler.endHook("hook.onUiRenderBegin", __perf);
        }
    }

    public static void onUiRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            if (MinecraftRemixLifecycleHooks.isStandaloneWindowMode()) {
                return;
            }
            if (!RemixUiCapture.isActive()) {
                Minecraft minecraft = RemixLifecycleBridge.getRememberedMinecraft();
                if (minecraft != null && minecraft.z != null && minecraft.z.z && minecraft.r == null) {
                    RemixUiCapture.clear(minecraft.d, minecraft.e);
                    return;
                }
            }
            RemixUiCapture.end();
        } finally {
            HookProfiler.endHook("hook.onUiRenderEnd", __perf);
        }
    }

    public static void onNameTagRenderBegin() {
        long __perf = HookProfiler.begin();
        try {
            Minecraft minecraft = RemixLifecycleBridge.getRememberedMinecraft();
            if (minecraft == null) {
                return;
            }
            RemixUiCapture.beginNameTagCapture(minecraft.d, minecraft.e);
        } finally {
            HookProfiler.endHook("hook.onNameTagRenderBegin", __perf);
        }
    }

    public static void onNameTagRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixUiCapture.endNameTagCapture();
        } finally {
            HookProfiler.endHook("hook.onNameTagRenderEnd", __perf);
        }
    }

    public static boolean captureFontStringAndMaybeSuppress(
            String text,
            int x,
            int y,
            int colorRgba,
            boolean shadow,
            int[] characterWidths,
            int fontTextureGlId) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
            RemixUiCapture.onFontString(text, x, y, colorRgba, shadow, characterWidths, fontTextureGlId);
            return RemixUiCapture.isActive()
                    && text != null
                    && !text.isEmpty()
                    && characterWidths != null
                    && fontTextureGlId > 0;
        } finally {
            HookProfiler.endHook("hook.captureFontStringAndMaybeSuppress", __perf);
        }
    }
}
