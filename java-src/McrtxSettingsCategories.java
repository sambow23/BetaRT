import mcrtx.bridge.McrtxSettingsStore;

final class McrtxSettingsCategories {
    private static final McrtxSettingsCategoryUi GAMEPLAY = new McrtxGameplaySettingsUi();
    private static final McrtxSettingsCategoryUi GRAPHICS = new McrtxGraphicsSettingsUi();
    private static final McrtxSettingsCategoryUi DEBUG = new McrtxDebugSettingsUi();
    private static final McrtxSettingsCategoryUi MATERIAL = new McrtxMaterialSettingsUi();

    private McrtxSettingsCategories() {
    }

    static McrtxSettingsCategoryUi get(int category) {
        if (category == McrtxSettingsStore.CATEGORY_GRAPHICS) return GRAPHICS;
        if (category == McrtxSettingsStore.CATEGORY_DEBUG) return DEBUG;
        if (category == McrtxSettingsStore.CATEGORY_MATERIAL) return MATERIAL;
        return GAMEPLAY;
    }

    static void applySavedSettings() {
        GAMEPLAY.applySavedSettings();
        DEBUG.applySavedSettings();
        MATERIAL.applySavedSettings();
        GRAPHICS.applySavedSettings();
    }
}
