import mcrtx.bridge.McrtxDebugSettings;
import mcrtx.bridge.McrtxDebugSettingsNative;

final class McrtxDebugSettingsUi implements McrtxSettingsCategoryUi {
    private static final int DYNAMIC_ENTITY_RENDERING_BUTTON_ID = 13;
    private static final int PAINTING_VANILLA_SUPPRESSION_BUTTON_ID = 14;
    private static final int MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID = 15;
    private static final int WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID = 16;
    private static final int LIVING_ENTITY_RENDERING_BUTTON_ID = 17;
    private static final int ITEM_ENTITY_RENDERING_BUTTON_ID = 18;
    private static final int SIGN_CAPTURE_BUTTON_ID = 19;
    private static final int SIGN_TEXT_CAPTURE_BUTTON_ID = 20;
    private static final int SIGN_VANILLA_SUPPRESSION_BUTTON_ID = 21;

    public String getName() { return "Debug"; }

    public void addControls(McrtxQuickSettingsScreen screen) {
        screen.addControl(button(screen, DYNAMIC_ENTITY_RENDERING_BUTTON_ID, "Dynamic Entities: " + toggle(McrtxDebugSettings.isDynamicEntityRenderingEnabled())));
        screen.addControl(button(screen, LIVING_ENTITY_RENDERING_BUTTON_ID, "Living Entities: " + toggle(McrtxDebugSettings.isLivingEntityRenderingEnabled())));
        screen.addControl(button(screen, ITEM_ENTITY_RENDERING_BUTTON_ID, "Item Entities: " + toggle(McrtxDebugSettings.isItemEntityRenderingEnabled())));
        screen.addControl(button(screen, PAINTING_VANILLA_SUPPRESSION_BUTTON_ID, "Replace Paintings: " + toggle(McrtxDebugSettings.isPaintingVanillaSuppressionEnabled())));
        screen.addControl(button(screen, MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID, "Replace Moving Pistons: " + toggle(McrtxDebugSettings.isMovingPistonVanillaSuppressionEnabled())));
        screen.addControl(button(screen, WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID, "Suppress World Raster: " + toggle(McrtxDebugSettings.isWorldRasterVanillaSuppressionEnabled())));
        screen.addControl(button(screen, SIGN_CAPTURE_BUTTON_ID, "Capture Signs: " + toggle(McrtxDebugSettings.isSignCaptureEnabled())));
        screen.addControl(button(screen, SIGN_TEXT_CAPTURE_BUTTON_ID, "Capture Sign Text: " + toggle(McrtxDebugSettings.isSignTextCaptureEnabled())));
        screen.addControl(button(screen, SIGN_VANILLA_SUPPRESSION_BUTTON_ID, "Replace Signs: " + toggle(McrtxDebugSettings.isSignVanillaSuppressionEnabled())));
    }

    public int handleButton(int id) {
        if (id == DYNAMIC_ENTITY_RENDERING_BUTTON_ID) setDynamicEntityRenderingEnabled(!McrtxDebugSettings.isDynamicEntityRenderingEnabled());
        else if (id == LIVING_ENTITY_RENDERING_BUTTON_ID) setLivingEntityRenderingEnabled(!McrtxDebugSettings.isLivingEntityRenderingEnabled());
        else if (id == ITEM_ENTITY_RENDERING_BUTTON_ID) setItemEntityRenderingEnabled(!McrtxDebugSettings.isItemEntityRenderingEnabled());
        else if (id == PAINTING_VANILLA_SUPPRESSION_BUTTON_ID) McrtxDebugSettings.setPaintingVanillaSuppressionEnabled(!McrtxDebugSettings.isPaintingVanillaSuppressionEnabled());
        else if (id == MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID) McrtxDebugSettings.setMovingPistonVanillaSuppressionEnabled(!McrtxDebugSettings.isMovingPistonVanillaSuppressionEnabled());
        else if (id == WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID) McrtxDebugSettings.setWorldRasterVanillaSuppressionEnabled(!McrtxDebugSettings.isWorldRasterVanillaSuppressionEnabled());
        else if (id == SIGN_CAPTURE_BUTTON_ID) setSignCaptureEnabled(!McrtxDebugSettings.isSignCaptureEnabled());
        else if (id == SIGN_TEXT_CAPTURE_BUTTON_ID) setSignTextCaptureEnabled(!McrtxDebugSettings.isSignTextCaptureEnabled());
        else if (id == SIGN_VANILLA_SUPPRESSION_BUTTON_ID) McrtxDebugSettings.setSignVanillaSuppressionEnabled(!McrtxDebugSettings.isSignVanillaSuppressionEnabled());
        else return UPDATE_NONE;
        return UPDATE_REFRESH;
    }

    public void refreshButtons(McrtxQuickSettingsScreen screen) {
        setLabel(screen, DYNAMIC_ENTITY_RENDERING_BUTTON_ID, "Dynamic Entities: " + toggle(McrtxDebugSettings.isDynamicEntityRenderingEnabled()));
        setLabel(screen, LIVING_ENTITY_RENDERING_BUTTON_ID, "Living Entities: " + toggle(McrtxDebugSettings.isLivingEntityRenderingEnabled()));
        setLabel(screen, ITEM_ENTITY_RENDERING_BUTTON_ID, "Item Entities: " + toggle(McrtxDebugSettings.isItemEntityRenderingEnabled()));
        setLabel(screen, PAINTING_VANILLA_SUPPRESSION_BUTTON_ID, "Replace Paintings: " + toggle(McrtxDebugSettings.isPaintingVanillaSuppressionEnabled()));
        setLabel(screen, MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID, "Replace Moving Pistons: " + toggle(McrtxDebugSettings.isMovingPistonVanillaSuppressionEnabled()));
        setLabel(screen, WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID, "Suppress World Raster: " + toggle(McrtxDebugSettings.isWorldRasterVanillaSuppressionEnabled()));
        setLabel(screen, SIGN_CAPTURE_BUTTON_ID, "Capture Signs: " + toggle(McrtxDebugSettings.isSignCaptureEnabled()));
        setLabel(screen, SIGN_TEXT_CAPTURE_BUTTON_ID, "Capture Sign Text: " + toggle(McrtxDebugSettings.isSignTextCaptureEnabled()));
        setLabel(screen, SIGN_VANILLA_SUPPRESSION_BUTTON_ID, "Replace Signs: " + toggle(McrtxDebugSettings.isSignVanillaSuppressionEnabled()));
    }

    public void applySavedSettings() {
        boolean dynamic = McrtxDebugSettings.isDynamicEntityRenderingEnabled();
        boolean living = McrtxDebugSettings.isLivingEntityRenderingEnabled();
        boolean item = McrtxDebugSettings.isItemEntityRenderingEnabled();
        boolean signs = McrtxDebugSettings.isSignCaptureEnabled();
        boolean signText = McrtxDebugSettings.isSignTextCaptureEnabled();
        RemixDynamicEntityCapture.setDynamicEntityRenderingEnabled(dynamic);
        RemixDynamicEntityCapture.setLivingEntityRenderingEnabled(living);
        RemixDynamicEntityCapture.setItemEntityRenderingEnabled(item);
        RemixDynamicEntityCapture.setSignCaptureEnabled(signs);
        RemixDynamicEntityCapture.setSignTextCaptureEnabled(signText);
        McrtxDebugSettingsNative.setDynamicEntityRenderingEnabled(dynamic);
    }

    private static void setDynamicEntityRenderingEnabled(boolean enabled) { McrtxDebugSettings.setDynamicEntityRenderingEnabled(enabled); RemixDynamicEntityCapture.setDynamicEntityRenderingEnabled(enabled); McrtxDebugSettingsNative.setDynamicEntityRenderingEnabled(enabled); }
    private static void setLivingEntityRenderingEnabled(boolean enabled) { McrtxDebugSettings.setLivingEntityRenderingEnabled(enabled); RemixDynamicEntityCapture.setLivingEntityRenderingEnabled(enabled); }
    private static void setItemEntityRenderingEnabled(boolean enabled) { McrtxDebugSettings.setItemEntityRenderingEnabled(enabled); RemixDynamicEntityCapture.setItemEntityRenderingEnabled(enabled); }
    private static void setSignCaptureEnabled(boolean enabled) { McrtxDebugSettings.setSignCaptureEnabled(enabled); RemixDynamicEntityCapture.setSignCaptureEnabled(enabled); }
    private static void setSignTextCaptureEnabled(boolean enabled) { McrtxDebugSettings.setSignTextCaptureEnabled(enabled); RemixDynamicEntityCapture.setSignTextCaptureEnabled(enabled); }
    private static String toggle(boolean enabled) { return enabled ? "ON" : "OFF"; }
    private static ke button(McrtxQuickSettingsScreen screen, int id, String label) { return new ke(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, label); }
    private static void setLabel(McrtxQuickSettingsScreen screen, int id, String label) { ke button = screen.findButton(id); if (button != null) button.e = label; }
}
