package mcrtx.bridge;

import java.util.Map;

public final class McrtxDebugSettings {
    public static final String DYNAMIC_ENTITY_RENDERING_ENABLED_KEY = "MCRTX_DYNAMIC_ENTITY_RENDERING_ENABLED";
    public static final String LIVING_ENTITY_RENDERING_ENABLED_KEY = "MCRTX_LIVING_ENTITY_RENDERING_ENABLED";
    public static final String ITEM_ENTITY_RENDERING_ENABLED_KEY = "MCRTX_ITEM_ENTITY_RENDERING_ENABLED";
    public static final String PAINTING_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_PAINTING_VANILLA_SUPPRESSION_ENABLED";
    public static final String MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED";
    public static final String WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED";
    public static final String SIGN_CAPTURE_ENABLED_KEY = "MCRTX_SIGN_CAPTURE_ENABLED";
    public static final String SIGN_TEXT_CAPTURE_ENABLED_KEY = "MCRTX_SIGN_TEXT_CAPTURE_ENABLED";
    public static final String SIGN_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_SIGN_VANILLA_SUPPRESSION_ENABLED";

    private static boolean dynamicEntityRenderingEnabled = true;
    private static boolean livingEntityRenderingEnabled = true;
    private static boolean itemEntityRenderingEnabled = true;
    private static boolean paintingVanillaSuppressionEnabled;
    private static boolean movingPistonVanillaSuppressionEnabled;
    private static boolean worldRasterVanillaSuppressionEnabled;
    private static boolean signCaptureEnabled = true;
    private static boolean signTextCaptureEnabled = true;
    private static boolean signVanillaSuppressionEnabled;

    private McrtxDebugSettings() {
    }

    public static boolean isDynamicEntityRenderingEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return dynamicEntityRenderingEnabled; } }
    public static boolean isLivingEntityRenderingEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return livingEntityRenderingEnabled; } }
    public static boolean isItemEntityRenderingEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return itemEntityRenderingEnabled; } }
    public static boolean isPaintingVanillaSuppressionEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return paintingVanillaSuppressionEnabled; } }
    public static boolean isMovingPistonVanillaSuppressionEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return movingPistonVanillaSuppressionEnabled; } }
    public static boolean isWorldRasterVanillaSuppressionEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return worldRasterVanillaSuppressionEnabled; } }
    public static boolean isSignCaptureEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return signCaptureEnabled; } }
    public static boolean isSignTextCaptureEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return signTextCaptureEnabled; } }
    public static boolean isSignVanillaSuppressionEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return signVanillaSuppressionEnabled; } }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (dynamicEntityRenderingEnabled == enabled) return; dynamicEntityRenderingEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setLivingEntityRenderingEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (livingEntityRenderingEnabled == enabled) return; livingEntityRenderingEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setItemEntityRenderingEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (itemEntityRenderingEnabled == enabled) return; itemEntityRenderingEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setPaintingVanillaSuppressionEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (paintingVanillaSuppressionEnabled == enabled) return; paintingVanillaSuppressionEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setMovingPistonVanillaSuppressionEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (movingPistonVanillaSuppressionEnabled == enabled) return; movingPistonVanillaSuppressionEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setWorldRasterVanillaSuppressionEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (worldRasterVanillaSuppressionEnabled == enabled) return; worldRasterVanillaSuppressionEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setSignCaptureEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (signCaptureEnabled == enabled) return; signCaptureEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setSignTextCaptureEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (signTextCaptureEnabled == enabled) return; signTextCaptureEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setSignVanillaSuppressionEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (signVanillaSuppressionEnabled == enabled) return; signVanillaSuppressionEnabled = enabled; McrtxSettingsStore.saveLocked(); } }

    static void loadLocked(Map<String, String> fileValues) {
        dynamicEntityRenderingEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, DYNAMIC_ENTITY_RENDERING_ENABLED_KEY, true);
        livingEntityRenderingEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, LIVING_ENTITY_RENDERING_ENABLED_KEY, true);
        itemEntityRenderingEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, ITEM_ENTITY_RENDERING_ENABLED_KEY, true);
        paintingVanillaSuppressionEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, PAINTING_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        movingPistonVanillaSuppressionEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        worldRasterVanillaSuppressionEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        signCaptureEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, SIGN_CAPTURE_ENABLED_KEY, true);
        signTextCaptureEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, SIGN_TEXT_CAPTURE_ENABLED_KEY, true);
        signVanillaSuppressionEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, SIGN_VANILLA_SUPPRESSION_ENABLED_KEY, false);
    }

    static void writeLocked(Map<String, String> fileValues) {
        fileValues.put(DYNAMIC_ENTITY_RENDERING_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(dynamicEntityRenderingEnabled));
        fileValues.put(LIVING_ENTITY_RENDERING_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(livingEntityRenderingEnabled));
        fileValues.put(ITEM_ENTITY_RENDERING_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(itemEntityRenderingEnabled));
        fileValues.put(PAINTING_VANILLA_SUPPRESSION_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(paintingVanillaSuppressionEnabled));
        fileValues.put(MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(movingPistonVanillaSuppressionEnabled));
        fileValues.put(WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(worldRasterVanillaSuppressionEnabled));
        fileValues.put(SIGN_CAPTURE_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(signCaptureEnabled));
        fileValues.put(SIGN_TEXT_CAPTURE_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(signTextCaptureEnabled));
        fileValues.put(SIGN_VANILLA_SUPPRESSION_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(signVanillaSuppressionEnabled));
    }
}
