import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.McrtxDebugSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public final class MinecraftRemixEntityHooks {
    private static boolean suppressNextModelPartCallList;

    private MinecraftRemixEntityHooks() {
    }

    public static void onLivingEntityFrameBegin() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEntityFrameBegin();
        } finally {
            HookProfiler.endHook("hook.onLivingEntityFrameBegin", __perf);
        }
    }

    public static void onLivingEntityRenderStart(sn entity, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEntityRenderStart(entity, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onLivingEntityRenderStart", __perf);
        }
    }

    public static void onLivingEntityRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEntityRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onLivingEntityRenderEnd", __perf);
        }
    }

    public static void onPickupParticleEntityRenderStart(sn entity) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPickupParticleEntityRenderStart(entity);
        } finally {
            HookProfiler.endHook("hook.onPickupParticleEntityRenderStart", __perf);
        }
    }

    public static void onPickupParticleEntityRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPickupParticleEntityRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onPickupParticleEntityRenderEnd", __perf);
        }
    }

    public static void onItemEntityRenderStart(sn entity) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onItemEntityRenderStart(entity);
        } finally {
            HookProfiler.endHook("hook.onItemEntityRenderStart", __perf);
        }
    }

    public static void onItemEntityRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onItemEntityRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onItemEntityRenderEnd", __perf);
        }
    }

    public static void onEntityFireOverlayStart(sn entity) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onEntityFireOverlayStart(entity);
        } finally {
            HookProfiler.endHook("hook.onEntityFireOverlayStart", __perf);
        }
    }

    public static void onEntityFireOverlayEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onEntityFireOverlayEnd();
        } finally {
            HookProfiler.endHook("hook.onEntityFireOverlayEnd", __perf);
        }
    }

    public static void onSignRenderStart(yk sign) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignRenderStart(sign);
        } finally {
            HookProfiler.endHook("hook.onSignRenderStart", __perf);
        }
    }

    public static void onSignRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onSignRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onSignRenderEnd", __perf);
        }
    }

    public static void onMovingPistonRenderStart(uk piston) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onMovingPistonRenderStart(piston);
        } finally {
            HookProfiler.endHook("hook.onMovingPistonRenderStart", __perf);
        }
    }

    public static void onMovingPistonRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onMovingPistonRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onMovingPistonRenderEnd", __perf);
        }
    }

    public static void onPaintingRender(qv painting) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPaintingRender(painting);
        } finally {
            HookProfiler.endHook("hook.onPaintingRender", __perf);
        }
    }

    public static boolean tryReplacePaintingRender(qv painting) {
        long __perf = HookProfiler.begin();
        try {
            boolean captured = RemixDynamicEntityCapture.capturePaintingRender(painting);
            return captured && McrtxDebugSettings.isPaintingVanillaSuppressionEnabled();
        } finally {
            HookProfiler.endHook("hook.tryReplacePaintingRender", __perf);
        }
    }

    public static void drawModelPartCallList(int list) {
        boolean suppress = suppressNextModelPartCallList;
        suppressNextModelPartCallList = false;
        if (suppress) {
            return;
        }
        GL11.glCallList(list);
    }

    public static boolean tryReplaceSignModelRender(rf signModel) {
        long __perf = HookProfiler.begin();
        try {
            if (RemixUiCapture.isActive()) {
                return false;
            }
            boolean captured = RemixDynamicEntityCapture.captureSignModelRender(signModel);
            return captured && McrtxDebugSettings.isSignVanillaSuppressionEnabled();
        } finally {
            HookProfiler.endHook("hook.tryReplaceSignModelRender", __perf);
        }
    }

    public static void renderSignText(sj fontRenderer, String text, int x, int y, int colorRgba) {
        if (!RemixUiCapture.isActive()
                && McrtxDebugSettings.isSignVanillaSuppressionEnabled()
                && McrtxDebugSettings.isSignTextCaptureEnabled()
                && RemixDynamicEntityCapture.captureSignTextRender(fontRenderer, text, x, y, colorRgba)) {
            return;
        }
        fontRenderer.b(text, x, y, colorRgba);
    }

    public static void onSignTextRender(
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
        } finally {
            HookProfiler.endHook("hook.onSignTextRender", __perf);
        }
    }

    public static void onFirstPersonRenderStart() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonRenderStart();
        } finally {
            HookProfiler.endHook("hook.onFirstPersonRenderStart", __perf);
        }
    }

    public static void onFirstPersonShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonShadowPlayerRender(minecraft, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onFirstPersonShadowPlayerRender", __perf);
        }
    }

    public static void onFirstPersonRenderEnd() {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonRenderEnd();
        } finally {
            HookProfiler.endHook("hook.onFirstPersonRenderEnd", __perf);
        }
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onFirstPersonItemRender(itemStack);
        } finally {
            HookProfiler.endHook("hook.onFirstPersonItemRender", __perf);
        }
    }

    public static void onPlayerEquippedItemRenderStart(gs player, iz itemStack, float partialTicks) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onPlayerEquippedItemRenderStart(player, itemStack, partialTicks);
        } finally {
            HookProfiler.endHook("hook.onPlayerEquippedItemRenderStart", __perf);
        }
    }

    public static void onLivingEquippedItemRenderStart(ls entity, iz itemStack) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onLivingEquippedItemRenderStart(entity, itemStack);
        } finally {
            HookProfiler.endHook("hook.onLivingEquippedItemRenderStart", __perf);
        }
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        long __perf = HookProfiler.begin();
        try {
            RemixDynamicEntityCapture.onEntityTextureBind(primaryTexture, fallbackTexture);
        } finally {
            HookProfiler.endHook("hook.onEntityTextureBind", __perf);
        }
    }

    public static void onModelPartRender(tz[] polygons, float scale) {
        long __perf = HookProfiler.begin();
        try {
            boolean captured;
            if (RemixUiCapture.isActive()) {
                captured = RemixUiCapture.onModelPart(polygons, scale);
            } else {
                captured = RemixDynamicEntityCapture.onModelPartRender(polygons, scale);
            }
            suppressNextModelPartCallList = captured;
        } finally {
            HookProfiler.endHook("hook.onModelPartRender", __perf);
        }
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        long __perf = HookProfiler.begin();
        try {
            RemixUiCapture.onTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
            RemixDynamicEntityCapture.onFirstPersonTessellatorDraw(
                    rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
            RemixParticleCapture.onTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
        } finally {
            HookProfiler.endHook("hook.onFirstPersonTessellatorDraw", __perf);
        }
    }
}
