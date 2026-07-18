import net.minecraft.client.Minecraft;

public final class RemixDynamicEntityCapture {
    private RemixDynamicEntityCapture() {
    }

    public static void onLivingEntityFrameBegin() {
        RemixLivingEntityCapture.onFrameBegin();
    }

    public static void onLivingEntityRenderStart(sn entity, float partialTicks) {
        RemixLivingEntityCapture.onRenderStart(entity, partialTicks);
    }

    public static void onLivingEntityRenderEnd() {
        RemixLivingEntityCapture.onRenderEnd();
    }

    public static void onPickupParticleEntityRenderStart(sn entity) {
        RemixItemEntityCapture.onPickupRenderStart(entity);
    }

    public static void onPickupParticleEntityRenderEnd() {
        RemixItemEntityCapture.onPickupRenderEnd();
    }

    public static void onItemEntityRenderStart(sn entity) {
        RemixItemEntityCapture.onRenderStart(entity);
    }

    public static void onItemEntityRenderEnd() {
        RemixItemEntityCapture.onRenderEnd();
    }

    public static void onEntityFireOverlayStart(sn entity) {
        RemixEntityFireCapture.onRenderStart(entity);
    }

    public static void onEntityFireOverlayEnd() {
        RemixEntityFireCapture.onRenderEnd();
    }

    public static void onSignRenderStart(yk sign) {
        RemixSignCapture.onSignRenderStart(sign);
    }

    public static void onSignRenderEnd() {
        RemixSignCapture.onSignRenderEnd();
    }

    public static void onMovingPistonRenderStart(uk piston) {
        RemixSignCapture.onMovingPistonRenderStart(piston);
    }

    public static void onMovingPistonRenderEnd() {
        RemixSignCapture.onMovingPistonRenderEnd();
    }

    public static boolean shouldSuppressMovingPistonVanillaDraw() {
        return RemixSignCapture.shouldSuppressMovingPistonVanillaDraw();
    }

    public static boolean captureSignModelRender(rf signModel) {
        return RemixSignCapture.captureSignModelRender(signModel);
    }

    public static boolean captureSignTextRender(
            sj fontRenderer, String text, int x, int y, int colorRgba) {
        return RemixSignCapture.captureSignTextRender(fontRenderer, text, x, y, colorRgba);
    }

    public static void onPaintingRender(qv painting) {
        RemixPaintingCapture.onRender(painting);
    }

    public static boolean capturePaintingRender(qv painting) {
        return RemixPaintingCapture.captureRender(painting);
    }

    public static void onSignTextRender(
            String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        RemixSignCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
    }

    public static void onFirstPersonRenderStart() {
        RemixFirstPersonCapture.onRenderStart();
    }

    public static void onFirstPersonShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        RemixFirstPersonCapture.onShadowPlayerRender(minecraft, partialTicks);
    }

    public static void onFirstPersonRenderEnd() {
        RemixFirstPersonCapture.onRenderEnd();
    }

    public static boolean isFirstPersonActive() {
        return RemixFirstPersonCapture.isActive();
    }

    public static boolean shouldSuppressVanillaTessellatorDraw() {
        return RemixFirstPersonCapture.shouldSuppressVanillaDraw();
    }

    public static boolean shouldSuppressVanillaModelPartDraw() {
        return RemixFirstPersonCapture.shouldSuppressVanillaDraw();
    }

    public static void setPlayerShadowsEnabled(boolean enabled) {
        RemixFirstPersonCapture.setPlayerShadowsEnabled(enabled);
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        RemixHeldItemCapture.setHeldTorchLightsEnabled(enabled);
    }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) {
        RemixDynamicEntitySession.setRenderingEnabled(enabled);
    }

    public static void setLivingEntityRenderingEnabled(boolean enabled) {
        RemixLivingEntityCapture.setEnabled(enabled);
    }

    public static void setItemEntityRenderingEnabled(boolean enabled) {
        RemixItemEntityCapture.setEnabled(enabled);
    }

    public static void setSignCaptureEnabled(boolean enabled) {
        RemixSignCapture.setSignCaptureEnabled(enabled);
    }

    public static void setSignTextCaptureEnabled(boolean enabled) {
        RemixSignCapture.setSignTextCaptureEnabled(enabled);
    }

    public static void onFramePresented() {
        RemixDynamicEntitySession.onFramePresented();
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        RemixHeldItemCapture.onFirstPersonItemRender(itemStack);
    }

    public static void onPlayerEquippedItemRenderStart(gs player, iz itemStack, float partialTicks) {
        RemixHeldItemCapture.onPlayerEquippedItemRenderStart(player, itemStack, partialTicks);
    }

    public static void onLivingEquippedItemRenderStart(ls entity, iz itemStack) {
        RemixHeldItemCapture.onLivingEquippedItemRenderStart(entity, itemStack);
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        RemixDynamicEntitySession.bindEntityTexture(primaryTexture, fallbackTexture);
    }

    public static boolean onModelPartRender(tz[] polygons, float scale) {
        return RemixDynamicModelCapture.captureModelPart(polygons, scale);
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        RemixDynamicModelCapture.captureTessellatorDraw(
                rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
    }

    static boolean shouldCaptureModelPart(ps modelPart) {
        return RemixSignCapture.shouldCaptureModelPart(modelPart);
    }

    static float[] sanitizeDynamicModelPartColor(
            String texturePath, float red, float green, float blue, float alpha) {
        return RemixDynamicModelCapture.sanitizeModelPartColor(
                texturePath, red, green, blue, alpha);
    }
}
