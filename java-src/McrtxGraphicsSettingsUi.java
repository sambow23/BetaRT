import mcrtx.bridge.McrtxGraphicsSettings;
import mcrtx.bridge.McrtxGraphicsSettingsNative;
import mcrtx.bridge.MinecraftRenderHooks;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class McrtxGraphicsSettingsUi implements McrtxSettingsCategoryUi {
    private static final int UPSCALER_BUTTON_ID = 3;
    private static final int UPSCALER_PRESET_BUTTON_ID = 4;
    private static final int RAY_RECONSTRUCTION_BUTTON_ID = 5;
    private static final int RT_QUALITY_BUTTON_ID = 6;
    private static final int NO_CULL_DISTANCE_SLIDER_ID = 9;
    private static final int REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID = 30;
    private static final int GAME_RAIN_PARTICLES_BUTTON_ID = 31;
    private static final int SPARSE_RENDERING_BUTTON_ID = 32;

    public String getName() { return "Graphics"; }

    public void addControls(McrtxQuickSettingsScreen screen) {
        screen.addControl(button(screen, UPSCALER_BUTTON_ID, getUpscalerLabel()));
        screen.addControl(button(screen, UPSCALER_PRESET_BUTTON_ID, getUpscalerPresetLabel()));
        if (shouldShowDlssOptions()) {
            screen.addControl(button(screen, RAY_RECONSTRUCTION_BUTTON_ID, getRayReconstructionLabel()));
            screen.addControl(button(screen, SPARSE_RENDERING_BUTTON_ID, getSparseRenderingLabel()));
        }
        screen.addControl(button(screen, RT_QUALITY_BUTTON_ID, getRtQualityLabel()));
        screen.addControl(button(screen, REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID, McrtxGraphicsSettings.formatCloudButtonLabel(McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled())));
        screen.addControl(button(screen, GAME_RAIN_PARTICLES_BUTTON_ID, getGameRainLabel()));
        screen.addControl(new NoCullSlider(NO_CULL_DISTANCE_SLIDER_ID, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT));
    }

    public int handleButton(int buttonId) {
        if (buttonId == UPSCALER_BUTTON_ID) { cycleUpscalerType(); return UPDATE_REBUILD; }
        if (buttonId == UPSCALER_PRESET_BUTTON_ID) { cycleUpscalerPreset(); return UPDATE_REFRESH; }
        if (buttonId == RAY_RECONSTRUCTION_BUTTON_ID) { toggleRayReconstruction(); return UPDATE_REFRESH; }
        if (buttonId == SPARSE_RENDERING_BUTTON_ID) { toggleSparseRendering(); return UPDATE_REFRESH; }
        if (buttonId == RT_QUALITY_BUTTON_ID) { cycleRtQuality(); return UPDATE_REFRESH; }
        if (buttonId == REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID) {
            setRemixAtmosphereCloudsEnabled(!McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled());
            return UPDATE_REFRESH;
        }
        if (buttonId == GAME_RAIN_PARTICLES_BUTTON_ID) {
            McrtxGraphicsSettings.setGameRainParticlesEnabled(!McrtxGraphicsSettings.isGameRainParticlesEnabled());
            return UPDATE_REFRESH;
        }
        return UPDATE_NONE;
    }

    public void refreshButtons(McrtxQuickSettingsScreen screen) {
        setLabel(screen, UPSCALER_BUTTON_ID, getUpscalerLabel());
        setLabel(screen, UPSCALER_PRESET_BUTTON_ID, getUpscalerPresetLabel());
        setLabel(screen, RAY_RECONSTRUCTION_BUTTON_ID, getRayReconstructionLabel());
        setLabel(screen, SPARSE_RENDERING_BUTTON_ID, getSparseRenderingLabel());
        setLabel(screen, RT_QUALITY_BUTTON_ID, getRtQualityLabel());
        setLabel(screen, REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID, McrtxGraphicsSettings.formatCloudButtonLabel(McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled()));
        setLabel(screen, GAME_RAIN_PARTICLES_BUTTON_ID, getGameRainLabel());
    }

    public void applySavedSettings() {
        McrtxGraphicsSettingsNative.setRemixAtmosphereCloudsEnabled(McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled());
        RemixCameraState.setNoCullDistanceBlocks(McrtxGraphicsSettings.getNoCullDistanceBlocks());
        applyRtQuality();
        applyUpscaler();
    }

    private static ke button(McrtxQuickSettingsScreen screen, int id, String label) {
        return new ke(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, label);
    }
    private static void setLabel(McrtxQuickSettingsScreen screen, int id, String label) { ke button = screen.findButton(id); if (button != null) button.e = label; }
    private static String toggle(boolean enabled) { return enabled ? "ON" : "OFF"; }
    private static boolean shouldShowDlssOptions() { return McrtxGraphicsSettings.getUpscalerType() == McrtxGraphicsSettings.UPSCALER_TYPE_DLSS; }
    private static String getUpscalerLabel() { return "Upscaler: " + describeUpscaler(McrtxGraphicsSettings.getUpscalerType()); }
    private static String getRayReconstructionLabel() { return "Ray Reconstruction: " + toggle(McrtxGraphicsSettings.isRayReconstructionEnabled()); }
    private static String getSparseRenderingLabel() { return "Sparse Rendering: " + toggle(McrtxGraphicsSettings.isSparseRenderingEnabled()); }
    private static String getRtQualityLabel() { return "PT Quality: " + describeRtQuality(McrtxGraphicsSettings.getRtQuality()); }
    private static String getGameRainLabel() { return "Game Rain: " + toggle(McrtxGraphicsSettings.isGameRainParticlesEnabled()); }

    private static String getUpscalerPresetLabel() {
        int type = McrtxGraphicsSettings.getUpscalerType();
        if (type == McrtxGraphicsSettings.UPSCALER_TYPE_DLSS) return "Preset: " + describeDlss(McrtxGraphicsSettings.getDlssPreset());
        if (type == McrtxGraphicsSettings.UPSCALER_TYPE_XESS) return "Preset: " + describeXess(McrtxGraphicsSettings.getXessPreset());
        if (type == McrtxGraphicsSettings.UPSCALER_TYPE_TAAU) return "Preset: " + describeTaau(McrtxGraphicsSettings.getTaauPreset());
        return "Preset: N/A";
    }

    private static void setRemixAtmosphereCloudsEnabled(boolean enabled) {
        boolean previous = McrtxGraphicsSettings.isRemixAtmosphereCloudsEnabled();
        McrtxGraphicsSettings.setRemixAtmosphereCloudsEnabled(enabled);
        if (McrtxGraphicsSettings.shouldClearGameCloudLayerAfterToggle(previous, enabled)) MinecraftRenderHooks.clearCloudLayer();
        McrtxGraphicsSettingsNative.setRemixAtmosphereCloudsEnabled(enabled);
    }

    private static void setNoCullDistance(int blocks) {
        McrtxGraphicsSettings.setNoCullDistanceBlocks(blocks);
        RemixCameraState.setNoCullDistanceBlocks(blocks);
    }

    private static void cycleUpscalerType() {
        int type = McrtxGraphicsSettings.getUpscalerType();
        if (type == McrtxGraphicsSettings.UPSCALER_TYPE_NONE) type = McrtxGraphicsSettings.UPSCALER_TYPE_DLSS;
        else if (type == McrtxGraphicsSettings.UPSCALER_TYPE_DLSS) type = McrtxGraphicsSettings.UPSCALER_TYPE_XESS;
        else if (type == McrtxGraphicsSettings.UPSCALER_TYPE_XESS) type = McrtxGraphicsSettings.UPSCALER_TYPE_TAAU;
        else type = McrtxGraphicsSettings.UPSCALER_TYPE_NONE;
        McrtxGraphicsSettings.setUpscalerType(type);
        applyUpscaler();
    }

    private static void cycleUpscalerPreset() {
        int type = McrtxGraphicsSettings.getUpscalerType();
        if (type == McrtxGraphicsSettings.UPSCALER_TYPE_DLSS) cycleDlss();
        else if (type == McrtxGraphicsSettings.UPSCALER_TYPE_XESS) cycleXess();
        else if (type == McrtxGraphicsSettings.UPSCALER_TYPE_TAAU) cycleTaau();
        applyUpscaler();
    }

    private static void cycleDlss() {
        int value = McrtxGraphicsSettings.getDlssPreset();
        if (value == McrtxGraphicsSettings.DLSS_PRESET_AUTO) value = McrtxGraphicsSettings.DLSS_PRESET_QUALITY;
        else if (value == McrtxGraphicsSettings.DLSS_PRESET_QUALITY) value = McrtxGraphicsSettings.DLSS_PRESET_BALANCED;
        else if (value == McrtxGraphicsSettings.DLSS_PRESET_BALANCED) value = McrtxGraphicsSettings.DLSS_PRESET_PERFORMANCE;
        else if (value == McrtxGraphicsSettings.DLSS_PRESET_PERFORMANCE) value = McrtxGraphicsSettings.DLSS_PRESET_ULTRA_PERFORMANCE;
        else if (value == McrtxGraphicsSettings.DLSS_PRESET_ULTRA_PERFORMANCE) value = McrtxGraphicsSettings.DLSS_PRESET_DLAA;
        else value = McrtxGraphicsSettings.DLSS_PRESET_AUTO;
        McrtxGraphicsSettings.setDlssPreset(value);
    }

    private static void cycleXess() {
        int value = McrtxGraphicsSettings.getXessPreset();
        value = value >= McrtxGraphicsSettings.XESS_PRESET_NATIVE_AA ? McrtxGraphicsSettings.XESS_PRESET_ULTRA_PERFORMANCE : value + 1;
        McrtxGraphicsSettings.setXessPreset(value);
    }

    private static void cycleTaau() {
        int value = McrtxGraphicsSettings.getTaauPreset();
        value = value >= McrtxGraphicsSettings.TAAU_PRESET_FULLSCREEN ? McrtxGraphicsSettings.TAAU_PRESET_ULTRA_PERFORMANCE : value + 1;
        McrtxGraphicsSettings.setTaauPreset(value);
    }

    private static void toggleRayReconstruction() {
        if (!shouldShowDlssOptions()) return;
        McrtxGraphicsSettings.setRayReconstructionEnabled(!McrtxGraphicsSettings.isRayReconstructionEnabled());
        applyUpscaler();
    }
    private static void toggleSparseRendering() {
        if (!shouldShowDlssOptions()) return;
        McrtxGraphicsSettings.setSparseRenderingEnabled(!McrtxGraphicsSettings.isSparseRenderingEnabled());
        applyUpscaler();
    }

    private static void cycleRtQuality() {
        int value = McrtxGraphicsSettings.getRtQuality();
        if (value == McrtxGraphicsSettings.RT_QUALITY_LOW) value = McrtxGraphicsSettings.RT_QUALITY_MEDIUM;
        else if (value == McrtxGraphicsSettings.RT_QUALITY_MEDIUM) value = McrtxGraphicsSettings.RT_QUALITY_HIGH;
        else if (value == McrtxGraphicsSettings.RT_QUALITY_HIGH) value = McrtxGraphicsSettings.RT_QUALITY_ULTRA;
        else if (value == McrtxGraphicsSettings.RT_QUALITY_ULTRA) value = McrtxGraphicsSettings.RT_QUALITY_POTATO;
        else value = McrtxGraphicsSettings.RT_QUALITY_LOW;
        McrtxGraphicsSettings.setRtQuality(value);
        applyRtQuality();
    }

    private static void applyRtQuality() { McrtxGraphicsSettingsNative.setRtQuality(McrtxGraphicsSettings.getRtQuality()); }
    private static void applyUpscaler() {
        McrtxGraphicsSettingsNative.setUpscalerConfig(
                McrtxGraphicsSettings.getUpscalerType(), McrtxGraphicsSettings.getDlssPreset(),
                McrtxGraphicsSettings.getXessPreset(), McrtxGraphicsSettings.getTaauPreset(),
                McrtxGraphicsSettings.isRayReconstructionEnabled(), McrtxGraphicsSettings.isSparseRenderingEnabled());
    }

    private static String describeUpscaler(int value) { if (value == McrtxGraphicsSettings.UPSCALER_TYPE_NONE) return "None"; if (value == McrtxGraphicsSettings.UPSCALER_TYPE_XESS) return "XeSS"; if (value == McrtxGraphicsSettings.UPSCALER_TYPE_TAAU) return "TAAU"; return "DLSS"; }
    private static String describeRtQuality(int value) { if (value == McrtxGraphicsSettings.RT_QUALITY_POTATO) return "Potato"; if (value == McrtxGraphicsSettings.RT_QUALITY_LOW) return "Low"; if (value == McrtxGraphicsSettings.RT_QUALITY_MEDIUM) return "Medium"; if (value == McrtxGraphicsSettings.RT_QUALITY_ULTRA) return "Ultra"; return "High"; }
    private static String describeDlss(int value) { if (value == McrtxGraphicsSettings.DLSS_PRESET_QUALITY) return "Quality"; if (value == McrtxGraphicsSettings.DLSS_PRESET_BALANCED) return "Balanced"; if (value == McrtxGraphicsSettings.DLSS_PRESET_PERFORMANCE) return "Performance"; if (value == McrtxGraphicsSettings.DLSS_PRESET_ULTRA_PERFORMANCE) return "Ultra Performance"; if (value == McrtxGraphicsSettings.DLSS_PRESET_DLAA) return "DLAA"; return "Auto"; }
    private static String describeXess(int value) { if (value == McrtxGraphicsSettings.XESS_PRESET_ULTRA_PERFORMANCE) return "Ultra Performance"; if (value == McrtxGraphicsSettings.XESS_PRESET_PERFORMANCE) return "Performance"; if (value == McrtxGraphicsSettings.XESS_PRESET_QUALITY) return "Quality"; if (value == McrtxGraphicsSettings.XESS_PRESET_ULTRA_QUALITY) return "Ultra Quality"; if (value == McrtxGraphicsSettings.XESS_PRESET_ULTRA_QUALITY_PLUS) return "Ultra Quality Plus"; if (value == McrtxGraphicsSettings.XESS_PRESET_NATIVE_AA) return "Native AA"; return "Balanced"; }
    private static String describeTaau(int value) { if (value == McrtxGraphicsSettings.TAAU_PRESET_ULTRA_PERFORMANCE) return "Ultra Performance"; if (value == McrtxGraphicsSettings.TAAU_PRESET_PERFORMANCE) return "Performance"; if (value == McrtxGraphicsSettings.TAAU_PRESET_QUALITY) return "Quality"; if (value == McrtxGraphicsSettings.TAAU_PRESET_FULLSCREEN) return "Fullscreen"; return "Balanced"; }

    private static final class NoCullSlider extends ke {
        private boolean dragging;
        private float position;
        NoCullSlider(int id, int x, int y, int width, int height) { super(id, x, y, width, height, ""); sync(); }
        protected int a(boolean hovered) { return 0; }
        protected void b(Minecraft minecraft, int mouseX, int mouseY) { if (!this.h) return; if (dragging) update(mouseX); else sync(); GL11.glColor4f(1, 1, 1, 1); int thumbX = this.c + (int) (position * (float) (this.a - 8)); this.b(thumbX, this.d, 0, 66, 4, 20); this.b(thumbX + 4, this.d, 196, 66, 4, 20); }
        public boolean c(Minecraft minecraft, int mouseX, int mouseY) { if (!super.c(minecraft, mouseX, mouseY)) return false; dragging = true; update(mouseX); return true; }
        public void a(int mouseX, int mouseY) { dragging = false; sync(); }
        private void sync() { int value = McrtxGraphicsSettings.getNoCullDistanceBlocks(); position = (float) (value - McrtxGraphicsSettings.MIN_NO_CULL_DISTANCE_BLOCKS) / (float) (McrtxGraphicsSettings.MAX_NO_CULL_DISTANCE_BLOCKS - McrtxGraphicsSettings.MIN_NO_CULL_DISTANCE_BLOCKS); this.e = "Anti-Cull Distance: " + value + " Blocks"; }
        private void update(int mouseX) { float next = (float) (mouseX - (this.c + 4)) / (float) (this.a - 8); if (next < 0) next = 0; if (next > 1) next = 1; int value = McrtxGraphicsSettings.MIN_NO_CULL_DISTANCE_BLOCKS + Math.round(next * (float) (McrtxGraphicsSettings.MAX_NO_CULL_DISTANCE_BLOCKS - McrtxGraphicsSettings.MIN_NO_CULL_DISTANCE_BLOCKS)); position = (float) (value - McrtxGraphicsSettings.MIN_NO_CULL_DISTANCE_BLOCKS) / (float) (McrtxGraphicsSettings.MAX_NO_CULL_DISTANCE_BLOCKS - McrtxGraphicsSettings.MIN_NO_CULL_DISTANCE_BLOCKS); setNoCullDistance(value); this.e = "Anti-Cull Distance: " + value + " Blocks"; }
    }
}
