import mcrtx.bridge.McrtxMaterialSettings;
import mcrtx.bridge.McrtxMaterialSettingsNative;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class McrtxMaterialSettingsUi implements McrtxSettingsCategoryUi {
    private static final int DISPLACEMENT_FACTOR_SLIDER_ID = 22;
    private static final int SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID = 23;
    private static final int SUBSURFACE_RADIUS_SCALE_SLIDER_ID = 24;
    private static final int SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID = 25;
    private static final int SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID = 26;
    private static final int WATER_MATERIAL_THICKNESS_SLIDER_ID = 27;
    private static final int SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID = 28;
    private static final int WATER_THIN_WALL_BUTTON_ID = 29;
    private static final int WATER_TRANSMITTANCE_RED_SLIDER_ID = 33;
    private static final int WATER_TRANSMITTANCE_GREEN_SLIDER_ID = 34;
    private static final int WATER_TRANSMITTANCE_BLUE_SLIDER_ID = 35;
    private static final int WATER_TRANSMITTANCE_DISTANCE_SLIDER_ID = 36;
    private static final int WATER_REFRACTIVE_INDEX_SLIDER_ID = 37;
    private static final int WATER_DIFFUSE_LAYER_BUTTON_ID = 38;
    private static final int WATER_DIFFUSE_LAYER_SCALE_SLIDER_ID = 39;
    private static final int RESET_DEFAULTS_BUTTON_ID = 308;

    public String getName() { return "Material"; }

    public void addControls(McrtxQuickSettingsScreen screen) {
        addSlider(screen, DISPLACEMENT_FACTOR_SLIDER_ID, Slider.DISPLACEMENT);
        addSlider(screen, SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID, Slider.SSS_DISTANCE);
        addSlider(screen, SUBSURFACE_RADIUS_SCALE_SLIDER_ID, Slider.SSS_RADIUS_SCALE);
        addSlider(screen, SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID, Slider.SSS_MAX_RADIUS);
        addSlider(screen, SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID, Slider.SSS_ANISOTROPY);
        screen.addControl(button(screen, SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID, getDiffusionLabel()));
        addSlider(screen, WATER_TRANSMITTANCE_RED_SLIDER_ID, Slider.WATER_RED);
        addSlider(screen, WATER_TRANSMITTANCE_GREEN_SLIDER_ID, Slider.WATER_GREEN);
        addSlider(screen, WATER_TRANSMITTANCE_BLUE_SLIDER_ID, Slider.WATER_BLUE);
        addSlider(screen, WATER_TRANSMITTANCE_DISTANCE_SLIDER_ID, Slider.WATER_DISTANCE);
        addSlider(screen, WATER_REFRACTIVE_INDEX_SLIDER_ID, Slider.WATER_IOR);
        screen.addControl(button(screen, WATER_DIFFUSE_LAYER_BUTTON_ID, getWaterDiffuseLabel()));
        if (McrtxMaterialSettings.isWaterDiffuseLayerEnabled()) addSlider(screen, WATER_DIFFUSE_LAYER_SCALE_SLIDER_ID, Slider.WATER_DIFFUSE_SCALE);
        screen.addControl(button(screen, WATER_THIN_WALL_BUTTON_ID, getWaterModeLabel()));
        if (McrtxMaterialSettings.isWaterThinWalledEnabled()) addSlider(screen, WATER_MATERIAL_THICKNESS_SLIDER_ID, Slider.WATER_THICKNESS);
        screen.addControl(button(screen, RESET_DEFAULTS_BUTTON_ID, "Reset Material Defaults"));
    }

    public int handleButton(int id) {
        if (id == SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID) {
            setSubsurfaceDiffusionEnabled(!McrtxMaterialSettings.isSubsurfaceDiffusionProfileEnabled());
            return UPDATE_REFRESH;
        }
        if (id == WATER_THIN_WALL_BUTTON_ID) {
            McrtxMaterialSettings.setWaterThinWalledEnabled(!McrtxMaterialSettings.isWaterThinWalledEnabled());
            applyWater();
            return UPDATE_REBUILD;
        }
        if (id == WATER_DIFFUSE_LAYER_BUTTON_ID) {
            McrtxMaterialSettings.setWaterDiffuseLayerEnabled(!McrtxMaterialSettings.isWaterDiffuseLayerEnabled());
            applyWater();
            return UPDATE_REBUILD;
        }
        if (id == RESET_DEFAULTS_BUTTON_ID) {
            resetDefaults();
            return UPDATE_REBUILD;
        }
        return UPDATE_NONE;
    }

    public void refreshButtons(McrtxQuickSettingsScreen screen) {
        setLabel(screen, SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID, getDiffusionLabel());
        setLabel(screen, WATER_THIN_WALL_BUTTON_ID, getWaterModeLabel());
        setLabel(screen, WATER_DIFFUSE_LAYER_BUTTON_ID, getWaterDiffuseLabel());
    }

    public void applySavedSettings() {
        McrtxMaterialSettingsNative.setDisplacementFactor(McrtxMaterialSettings.getDisplacementFactor());
        McrtxMaterialSettingsNative.setSubsurfaceMeasurementDistance(McrtxMaterialSettings.getSubsurfaceMeasurementDistance());
        McrtxMaterialSettingsNative.setSubsurfaceRadiusScale(McrtxMaterialSettings.getSubsurfaceRadiusScale());
        McrtxMaterialSettingsNative.setSubsurfaceMaxSampleRadius(McrtxMaterialSettings.getSubsurfaceMaxSampleRadius());
        McrtxMaterialSettingsNative.setSubsurfaceVolumetricAnisotropy(McrtxMaterialSettings.getSubsurfaceVolumetricAnisotropy());
        McrtxMaterialSettingsNative.setSubsurfaceDiffusionProfileEnabled(McrtxMaterialSettings.isSubsurfaceDiffusionProfileEnabled());
        applyWater();
    }

    private static void addSlider(McrtxQuickSettingsScreen screen, int id, int mode) {
        screen.addControl(new Slider(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, mode));
    }
    private static ke button(McrtxQuickSettingsScreen screen, int id, String label) { return new ke(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, label); }
    private static void setLabel(McrtxQuickSettingsScreen screen, int id, String label) { ke button = screen.findButton(id); if (button != null) button.e = label; }
    private static String toggle(boolean enabled) { return enabled ? "ON" : "OFF"; }
    private static String getDiffusionLabel() { return "SSS Diffusion: " + toggle(McrtxMaterialSettings.isSubsurfaceDiffusionProfileEnabled()); }
    private static String getWaterDiffuseLabel() { return "Water Diffuse Layer: " + toggle(McrtxMaterialSettings.isWaterDiffuseLayerEnabled()); }
    private static String getWaterModeLabel() { return McrtxMaterialSettings.isWaterThinWalledEnabled() ? "Water Mode: Thin-Walled" : "Water Mode: Refractive"; }

    private static void setValue(int mode, int value) {
        if (mode == Slider.DISPLACEMENT) { McrtxMaterialSettings.setDisplacementFactorHundredths(value); McrtxMaterialSettingsNative.setDisplacementFactor(McrtxMaterialSettings.getDisplacementFactor()); }
        else if (mode == Slider.SSS_DISTANCE) { McrtxMaterialSettings.setSubsurfaceMeasurementDistanceHundredths(value); McrtxMaterialSettingsNative.setSubsurfaceMeasurementDistance(McrtxMaterialSettings.getSubsurfaceMeasurementDistance()); }
        else if (mode == Slider.SSS_RADIUS_SCALE) { McrtxMaterialSettings.setSubsurfaceRadiusScaleHundredths(value); McrtxMaterialSettingsNative.setSubsurfaceRadiusScale(McrtxMaterialSettings.getSubsurfaceRadiusScale()); }
        else if (mode == Slider.SSS_MAX_RADIUS) { McrtxMaterialSettings.setSubsurfaceMaxSampleRadiusHundredths(value); McrtxMaterialSettingsNative.setSubsurfaceMaxSampleRadius(McrtxMaterialSettings.getSubsurfaceMaxSampleRadius()); }
        else if (mode == Slider.SSS_ANISOTROPY) { McrtxMaterialSettings.setSubsurfaceVolumetricAnisotropyHundredths(value); McrtxMaterialSettingsNative.setSubsurfaceVolumetricAnisotropy(McrtxMaterialSettings.getSubsurfaceVolumetricAnisotropy()); }
        else if (mode == Slider.WATER_RED) McrtxMaterialSettings.setWaterTransmittanceRedHundredths(value);
        else if (mode == Slider.WATER_GREEN) McrtxMaterialSettings.setWaterTransmittanceGreenHundredths(value);
        else if (mode == Slider.WATER_BLUE) McrtxMaterialSettings.setWaterTransmittanceBlueHundredths(value);
        else if (mode == Slider.WATER_DISTANCE) McrtxMaterialSettings.setWaterTransmittanceDistanceHundredths(value);
        else if (mode == Slider.WATER_IOR) McrtxMaterialSettings.setWaterRefractiveIndexThousandths(value);
        else if (mode == Slider.WATER_DIFFUSE_SCALE) McrtxMaterialSettings.setWaterDiffuseLayerScaleHundredths(value);
        else McrtxMaterialSettings.setWaterMaterialThicknessThousandths(value);
        if (mode >= Slider.WATER_RED) applyWater();
    }

    private static void setSubsurfaceDiffusionEnabled(boolean enabled) {
        McrtxMaterialSettings.setSubsurfaceDiffusionProfileEnabled(enabled);
        McrtxMaterialSettingsNative.setSubsurfaceDiffusionProfileEnabled(enabled);
    }

    private static void applyWater() {
        McrtxMaterialSettingsNative.setWaterTransmissionSettings(
                McrtxMaterialSettings.getWaterTransmittanceRed(),
                McrtxMaterialSettings.getWaterTransmittanceGreen(),
                McrtxMaterialSettings.getWaterTransmittanceBlue(),
                McrtxMaterialSettings.getWaterTransmittanceDistance(),
                McrtxMaterialSettings.getWaterRefractiveIndex(),
                McrtxMaterialSettings.isWaterDiffuseLayerEnabled(),
                McrtxMaterialSettings.getWaterDiffuseLayerScale(),
                McrtxMaterialSettings.isWaterThinWalledEnabled(),
                McrtxMaterialSettings.getWaterMaterialThickness());
    }

    private static void resetDefaults() {
        setValue(Slider.SSS_DISTANCE, McrtxMaterialSettings.DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS);
        setValue(Slider.SSS_RADIUS_SCALE, McrtxMaterialSettings.DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS);
        setValue(Slider.SSS_MAX_RADIUS, McrtxMaterialSettings.DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS);
        setValue(Slider.SSS_ANISOTROPY, McrtxMaterialSettings.DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS);
        setSubsurfaceDiffusionEnabled(McrtxMaterialSettings.DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED);
        McrtxMaterialSettings.setWaterTransmittanceRedHundredths(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_RED_HUNDREDTHS);
        McrtxMaterialSettings.setWaterTransmittanceGreenHundredths(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_GREEN_HUNDREDTHS);
        McrtxMaterialSettings.setWaterTransmittanceBlueHundredths(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_BLUE_HUNDREDTHS);
        McrtxMaterialSettings.setWaterTransmittanceDistanceHundredths(McrtxMaterialSettings.DEFAULT_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS);
        McrtxMaterialSettings.setWaterRefractiveIndexThousandths(McrtxMaterialSettings.DEFAULT_WATER_REFRACTIVE_INDEX_THOUSANDTHS);
        McrtxMaterialSettings.setWaterDiffuseLayerEnabled(McrtxMaterialSettings.DEFAULT_WATER_DIFFUSE_LAYER_ENABLED);
        McrtxMaterialSettings.setWaterDiffuseLayerScaleHundredths(McrtxMaterialSettings.DEFAULT_WATER_DIFFUSE_LAYER_SCALE_HUNDREDTHS);
        McrtxMaterialSettings.setWaterThinWalledEnabled(McrtxMaterialSettings.DEFAULT_WATER_THIN_WALLED_ENABLED);
        McrtxMaterialSettings.setWaterMaterialThicknessThousandths(McrtxMaterialSettings.DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS);
        applyWater();
    }

    private static String formatHundredths(int value) { int absolute = Math.abs(value); String result = Integer.toString(absolute / 100) + "." + (absolute % 100 < 10 ? "0" : "") + Integer.toString(absolute % 100); return value < 0 ? "-" + result : result; }
    private static String formatThousandths(int value) { int absolute = Math.abs(value); int fraction = absolute % 1000; String result = Integer.toString(absolute / 1000) + "." + (fraction < 100 ? "0" : "") + (fraction < 10 ? "0" : "") + Integer.toString(fraction); return value < 0 ? "-" + result : result; }

    private static final class Slider extends ke {
        static final int DISPLACEMENT = 0;
        static final int SSS_DISTANCE = 1;
        static final int SSS_RADIUS_SCALE = 2;
        static final int SSS_MAX_RADIUS = 3;
        static final int SSS_ANISOTROPY = 4;
        static final int WATER_RED = 5;
        static final int WATER_GREEN = 6;
        static final int WATER_BLUE = 7;
        static final int WATER_DISTANCE = 8;
        static final int WATER_IOR = 9;
        static final int WATER_THICKNESS = 10;
        static final int WATER_DIFFUSE_SCALE = 11;

        private final int mode;
        private final int minimum;
        private final int maximum;
        private boolean dragging;
        private float position;

        Slider(int id, int x, int y, int width, int height, int mode) {
            super(id, x, y, width, height, "");
            this.mode = mode;
            if (mode == DISPLACEMENT) { minimum = McrtxMaterialSettings.MIN_DISPLACEMENT_FACTOR_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_DISPLACEMENT_FACTOR_HUNDREDTHS; }
            else if (mode == SSS_DISTANCE) { minimum = McrtxMaterialSettings.MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS; }
            else if (mode == SSS_RADIUS_SCALE) { minimum = McrtxMaterialSettings.MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS; }
            else if (mode == SSS_MAX_RADIUS) { minimum = McrtxMaterialSettings.MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS; }
            else if (mode == SSS_ANISOTROPY) { minimum = McrtxMaterialSettings.MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS; }
            else if (mode == WATER_DISTANCE) { minimum = McrtxMaterialSettings.MIN_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS; }
            else if (mode == WATER_IOR) { minimum = McrtxMaterialSettings.MIN_WATER_REFRACTIVE_INDEX_THOUSANDTHS; maximum = McrtxMaterialSettings.MAX_WATER_REFRACTIVE_INDEX_THOUSANDTHS; }
            else if (mode == WATER_THICKNESS) { minimum = McrtxMaterialSettings.MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS; maximum = McrtxMaterialSettings.MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS; }
            else if (mode == WATER_DIFFUSE_SCALE) { minimum = McrtxMaterialSettings.MIN_WATER_DIFFUSE_LAYER_SCALE_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_WATER_DIFFUSE_LAYER_SCALE_HUNDREDTHS; }
            else { minimum = McrtxMaterialSettings.MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS; maximum = McrtxMaterialSettings.MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS; }
            sync();
        }

        protected int a(boolean hovered) { return 0; }
        protected void b(Minecraft minecraft, int mouseX, int mouseY) { if (!this.h) return; if (dragging) update(mouseX); else sync(); GL11.glColor4f(1, 1, 1, 1); int thumbX = this.c + (int) (position * (float) (this.a - 8)); this.b(thumbX, this.d, 0, 66, 4, 20); this.b(thumbX + 4, this.d, 196, 66, 4, 20); }
        public boolean c(Minecraft minecraft, int mouseX, int mouseY) { if (!super.c(minecraft, mouseX, mouseY)) return false; dragging = true; update(mouseX); return true; }
        public void a(int mouseX, int mouseY) { dragging = false; sync(); }

        private int value() {
            if (mode == DISPLACEMENT) return McrtxMaterialSettings.getDisplacementFactorHundredths();
            if (mode == SSS_DISTANCE) return McrtxMaterialSettings.getSubsurfaceMeasurementDistanceHundredths();
            if (mode == SSS_RADIUS_SCALE) return McrtxMaterialSettings.getSubsurfaceRadiusScaleHundredths();
            if (mode == SSS_MAX_RADIUS) return McrtxMaterialSettings.getSubsurfaceMaxSampleRadiusHundredths();
            if (mode == SSS_ANISOTROPY) return McrtxMaterialSettings.getSubsurfaceVolumetricAnisotropyHundredths();
            if (mode == WATER_RED) return McrtxMaterialSettings.getWaterTransmittanceRedHundredths();
            if (mode == WATER_GREEN) return McrtxMaterialSettings.getWaterTransmittanceGreenHundredths();
            if (mode == WATER_BLUE) return McrtxMaterialSettings.getWaterTransmittanceBlueHundredths();
            if (mode == WATER_DISTANCE) return McrtxMaterialSettings.getWaterTransmittanceDistanceHundredths();
            if (mode == WATER_IOR) return McrtxMaterialSettings.getWaterRefractiveIndexThousandths();
            if (mode == WATER_DIFFUSE_SCALE) return McrtxMaterialSettings.getWaterDiffuseLayerScaleHundredths();
            return McrtxMaterialSettings.getWaterMaterialThicknessThousandths();
        }
        private void sync() { int value = value(); position = (float) (value - minimum) / (float) (maximum - minimum); label(value); }
        private void update(int mouseX) { float next = (float) (mouseX - (this.c + 4)) / (float) (this.a - 8); if (next < 0) next = 0; if (next > 1) next = 1; int value = minimum + Math.round(next * (float) (maximum - minimum)); position = (float) (value - minimum) / (float) (maximum - minimum); setValue(mode, value); label(value); }
        private void label(int value) {
            if (mode == DISPLACEMENT) this.e = "Displacement Factor: " + formatHundredths(value) + "x";
            else if (mode == SSS_DISTANCE) this.e = "SSS Distance: " + formatHundredths(value);
            else if (mode == SSS_RADIUS_SCALE) this.e = "SSS Radius Scale: " + formatHundredths(value) + "x";
            else if (mode == SSS_MAX_RADIUS) this.e = "SSS Max Radius: " + formatHundredths(value);
            else if (mode == SSS_ANISOTROPY) this.e = "SSS Anisotropy: " + formatHundredths(value);
            else if (mode == WATER_RED) this.e = "Water Red: " + formatHundredths(value);
            else if (mode == WATER_GREEN) this.e = "Water Green: " + formatHundredths(value);
            else if (mode == WATER_BLUE) this.e = "Water Blue: " + formatHundredths(value);
            else if (mode == WATER_DISTANCE) this.e = "Water Distance: " + formatHundredths(value);
            else if (mode == WATER_IOR) this.e = "Water IOR: " + formatThousandths(value);
            else if (mode == WATER_DIFFUSE_SCALE) this.e = "Water Diffuse Scale: " + formatHundredths(value) + "x";
            else this.e = "Water Thickness: " + formatThousandths(value);
        }
    }
}
