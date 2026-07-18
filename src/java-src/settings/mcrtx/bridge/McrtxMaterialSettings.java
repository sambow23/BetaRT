package mcrtx.bridge;

import java.util.Map;

public final class McrtxMaterialSettings {
    public static final String DISPLACEMENT_FACTOR_KEY = "MCRTX_DISPLACEMENT_FACTOR";
    public static final String SUBSURFACE_MEASUREMENT_DISTANCE_KEY = "MCRTX_SUBSURFACE_MEASUREMENT_DISTANCE";
    public static final String SUBSURFACE_RADIUS_SCALE_KEY = "MCRTX_SUBSURFACE_RADIUS_SCALE";
    public static final String SUBSURFACE_MAX_SAMPLE_RADIUS_KEY = "MCRTX_SUBSURFACE_MAX_SAMPLE_RADIUS";
    public static final String SUBSURFACE_VOLUMETRIC_ANISOTROPY_KEY = "MCRTX_SUBSURFACE_VOLUMETRIC_ANISOTROPY";
    public static final String SUBSURFACE_DIFFUSION_PROFILE_ENABLED_KEY = "MCRTX_SUBSURFACE_DIFFUSION_PROFILE_ENABLED";
    public static final String WATER_THIN_WALLED_ENABLED_KEY = "MCRTX_WATER_THIN_WALLED_ENABLED";
    public static final String WATER_MATERIAL_THICKNESS_KEY = "MCRTX_WATER_MATERIAL_THICKNESS";
    public static final String WATER_TRANSMITTANCE_RED_KEY = "MCRTX_WATER_TRANSMITTANCE_COLOR_R";
    public static final String WATER_TRANSMITTANCE_GREEN_KEY = "MCRTX_WATER_TRANSMITTANCE_COLOR_G";
    public static final String WATER_TRANSMITTANCE_BLUE_KEY = "MCRTX_WATER_TRANSMITTANCE_COLOR_B";
    public static final String WATER_TRANSMITTANCE_DISTANCE_KEY = "MCRTX_WATER_TRANSMITTANCE_MEASUREMENT_DISTANCE";
    public static final String WATER_REFRACTIVE_INDEX_KEY = "MCRTX_WATER_REFRACTIVE_INDEX";
    public static final String WATER_DIFFUSE_LAYER_ENABLED_KEY = "MCRTX_WATER_USE_DIFFUSE_LAYER";
    private static final String WATER_MATERIAL_THICKNESS_MIGRATION_KEY = "MCRTX_WATER_MATERIAL_THICKNESS_MIGRATED";

    public static final int MIN_DISPLACEMENT_FACTOR_HUNDREDTHS = 0;
    public static final int MAX_DISPLACEMENT_FACTOR_HUNDREDTHS = 400;
    public static final int DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS = 100;
    public static final int MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS = 0;
    public static final int MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS = 2500;
    public static final int DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS = 100;
    public static final int MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS = 0;
    public static final int MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS = 2500;
    public static final int DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS = 100;
    public static final int MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS = 0;
    public static final int MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS = 25600;
    public static final int DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS = 1600;
    public static final int MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS = -100;
    public static final int MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS = 99;
    public static final int DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS = 0;
    public static final boolean DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED = true;
    public static final boolean DEFAULT_WATER_THIN_WALLED_ENABLED = true;
    public static final int MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 1;
    public static final int MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 5000;
    public static final int DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 1000;
    public static final int MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS = 1;
    public static final int MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS = 100;
    public static final int DEFAULT_WATER_TRANSMITTANCE_RED_HUNDREDTHS = 74;
    public static final int DEFAULT_WATER_TRANSMITTANCE_GREEN_HUNDREDTHS = 90;
    public static final int DEFAULT_WATER_TRANSMITTANCE_BLUE_HUNDREDTHS = 100;
    public static final int MIN_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS = 1;
    public static final int MAX_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS = 2500;
    public static final int DEFAULT_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS = 150;
    public static final int MIN_WATER_REFRACTIVE_INDEX_THOUSANDTHS = 1000;
    public static final int MAX_WATER_REFRACTIVE_INDEX_THOUSANDTHS = 3000;
    public static final int DEFAULT_WATER_REFRACTIVE_INDEX_THOUSANDTHS = 1333;
    public static final boolean DEFAULT_WATER_DIFFUSE_LAYER_ENABLED = true;
    private static final int LEGACY_DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 1;

    private static int displacementFactorHundredths = DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS;
    private static int subsurfaceMeasurementDistanceHundredths = DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS;
    private static int subsurfaceRadiusScaleHundredths = DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS;
    private static int subsurfaceMaxSampleRadiusHundredths = DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS;
    private static int subsurfaceVolumetricAnisotropyHundredths = DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS;
    private static boolean subsurfaceDiffusionProfileEnabled = DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED;
    private static boolean waterThinWalledEnabled = DEFAULT_WATER_THIN_WALLED_ENABLED;
    private static int waterMaterialThicknessThousandths = DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
    private static int waterTransmittanceRedHundredths = DEFAULT_WATER_TRANSMITTANCE_RED_HUNDREDTHS;
    private static int waterTransmittanceGreenHundredths = DEFAULT_WATER_TRANSMITTANCE_GREEN_HUNDREDTHS;
    private static int waterTransmittanceBlueHundredths = DEFAULT_WATER_TRANSMITTANCE_BLUE_HUNDREDTHS;
    private static int waterTransmittanceDistanceHundredths = DEFAULT_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS;
    private static int waterRefractiveIndexThousandths = DEFAULT_WATER_REFRACTIVE_INDEX_THOUSANDTHS;
    private static boolean waterDiffuseLayerEnabled = DEFAULT_WATER_DIFFUSE_LAYER_ENABLED;

    private McrtxMaterialSettings() {
    }

    public static int getDisplacementFactorHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return displacementFactorHundredths; } }
    public static float getDisplacementFactor() { return (float) getDisplacementFactorHundredths() / 100.0f; }
    public static int getSubsurfaceMeasurementDistanceHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return subsurfaceMeasurementDistanceHundredths; } }
    public static float getSubsurfaceMeasurementDistance() { return (float) getSubsurfaceMeasurementDistanceHundredths() / 100.0f; }
    public static int getSubsurfaceRadiusScaleHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return subsurfaceRadiusScaleHundredths; } }
    public static float getSubsurfaceRadiusScale() { return (float) getSubsurfaceRadiusScaleHundredths() / 100.0f; }
    public static int getSubsurfaceMaxSampleRadiusHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return subsurfaceMaxSampleRadiusHundredths; } }
    public static float getSubsurfaceMaxSampleRadius() { return (float) getSubsurfaceMaxSampleRadiusHundredths() / 100.0f; }
    public static int getSubsurfaceVolumetricAnisotropyHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return subsurfaceVolumetricAnisotropyHundredths; } }
    public static float getSubsurfaceVolumetricAnisotropy() { return (float) getSubsurfaceVolumetricAnisotropyHundredths() / 100.0f; }
    public static boolean isSubsurfaceDiffusionProfileEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return subsurfaceDiffusionProfileEnabled; } }
    public static boolean isWaterThinWalledEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterThinWalledEnabled; } }
    public static int getWaterMaterialThicknessThousandths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterMaterialThicknessThousandths; } }
    public static float getWaterMaterialThickness() { return (float) getWaterMaterialThicknessThousandths() / 1000.0f; }
    public static int getWaterTransmittanceRedHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterTransmittanceRedHundredths; } }
    public static float getWaterTransmittanceRed() { return (float) getWaterTransmittanceRedHundredths() / 100.0f; }
    public static int getWaterTransmittanceGreenHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterTransmittanceGreenHundredths; } }
    public static float getWaterTransmittanceGreen() { return (float) getWaterTransmittanceGreenHundredths() / 100.0f; }
    public static int getWaterTransmittanceBlueHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterTransmittanceBlueHundredths; } }
    public static float getWaterTransmittanceBlue() { return (float) getWaterTransmittanceBlueHundredths() / 100.0f; }
    public static int getWaterTransmittanceDistanceHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterTransmittanceDistanceHundredths; } }
    public static float getWaterTransmittanceDistance() { return (float) getWaterTransmittanceDistanceHundredths() / 100.0f; }
    public static int getWaterRefractiveIndexThousandths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterRefractiveIndexThousandths; } }
    public static float getWaterRefractiveIndex() { return (float) getWaterRefractiveIndexThousandths() / 1000.0f; }
    public static boolean isWaterDiffuseLayerEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return waterDiffuseLayerEnabled; } }

    public static void setDisplacementFactorHundredths(int value) { setHundredths(0, value); }
    public static void setSubsurfaceMeasurementDistanceHundredths(int value) { setHundredths(1, value); }
    public static void setSubsurfaceRadiusScaleHundredths(int value) { setHundredths(2, value); }
    public static void setSubsurfaceMaxSampleRadiusHundredths(int value) { setHundredths(3, value); }
    public static void setSubsurfaceVolumetricAnisotropyHundredths(int value) { setHundredths(4, value); }
    public static void setWaterTransmittanceRedHundredths(int value) { setHundredths(5, value); }
    public static void setWaterTransmittanceGreenHundredths(int value) { setHundredths(6, value); }
    public static void setWaterTransmittanceBlueHundredths(int value) { setHundredths(7, value); }
    public static void setWaterTransmittanceDistanceHundredths(int value) { setHundredths(8, value); }

    public static void setSubsurfaceDiffusionProfileEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (subsurfaceDiffusionProfileEnabled == enabled) return; subsurfaceDiffusionProfileEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setWaterThinWalledEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (waterThinWalledEnabled == enabled) return; waterThinWalledEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setWaterDiffuseLayerEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (waterDiffuseLayerEnabled == enabled) return; waterDiffuseLayerEnabled = enabled; McrtxSettingsStore.saveLocked(); } }

    public static void setWaterMaterialThicknessThousandths(int value) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int normalized = McrtxRuntimeSettingParser.clamp(value, MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS, MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS);
            if (waterMaterialThicknessThousandths == normalized) return;
            waterMaterialThicknessThousandths = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static void setWaterRefractiveIndexThousandths(int value) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int normalized = McrtxRuntimeSettingParser.clamp(value, MIN_WATER_REFRACTIVE_INDEX_THOUSANDTHS, MAX_WATER_REFRACTIVE_INDEX_THOUSANDTHS);
            if (waterRefractiveIndexThousandths == normalized) return;
            waterRefractiveIndexThousandths = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }

    static boolean loadLocked(Map<String, String> values) {
        displacementFactorHundredths = readDisplacementFactor(values);
        subsurfaceMeasurementDistanceHundredths = readHundredths(values, SUBSURFACE_MEASUREMENT_DISTANCE_KEY, DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS, MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS, MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS);
        subsurfaceRadiusScaleHundredths = readHundredths(values, SUBSURFACE_RADIUS_SCALE_KEY, DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS, MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS, MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS);
        subsurfaceMaxSampleRadiusHundredths = readHundredths(values, SUBSURFACE_MAX_SAMPLE_RADIUS_KEY, DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS, MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS, MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS);
        subsurfaceVolumetricAnisotropyHundredths = readHundredths(values, SUBSURFACE_VOLUMETRIC_ANISOTROPY_KEY, DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS, MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS, MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS);
        subsurfaceDiffusionProfileEnabled = McrtxRuntimeSettingParser.readBooleanSetting(values, SUBSURFACE_DIFFUSION_PROFILE_ENABLED_KEY, DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED);
        waterThinWalledEnabled = McrtxRuntimeSettingParser.readBooleanSetting(values, WATER_THIN_WALLED_ENABLED_KEY, DEFAULT_WATER_THIN_WALLED_ENABLED);
        waterMaterialThicknessThousandths = McrtxRuntimeSettingParser.readScaledIntSetting(values, WATER_MATERIAL_THICKNESS_KEY, DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS, MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS, MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS, 1000);
        waterTransmittanceRedHundredths = readHundredths(values, WATER_TRANSMITTANCE_RED_KEY, DEFAULT_WATER_TRANSMITTANCE_RED_HUNDREDTHS, MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS, MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS);
        waterTransmittanceGreenHundredths = readHundredths(values, WATER_TRANSMITTANCE_GREEN_KEY, DEFAULT_WATER_TRANSMITTANCE_GREEN_HUNDREDTHS, MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS, MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS);
        waterTransmittanceBlueHundredths = readHundredths(values, WATER_TRANSMITTANCE_BLUE_KEY, DEFAULT_WATER_TRANSMITTANCE_BLUE_HUNDREDTHS, MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS, MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS);
        waterTransmittanceDistanceHundredths = readHundredths(values, WATER_TRANSMITTANCE_DISTANCE_KEY, DEFAULT_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS, MIN_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS, MAX_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS);
        waterRefractiveIndexThousandths = McrtxRuntimeSettingParser.readScaledIntSetting(values, WATER_REFRACTIVE_INDEX_KEY, DEFAULT_WATER_REFRACTIVE_INDEX_THOUSANDTHS, MIN_WATER_REFRACTIVE_INDEX_THOUSANDTHS, MAX_WATER_REFRACTIVE_INDEX_THOUSANDTHS, 1000);
        waterDiffuseLayerEnabled = McrtxRuntimeSettingParser.readBooleanSetting(values, WATER_DIFFUSE_LAYER_ENABLED_KEY, DEFAULT_WATER_DIFFUSE_LAYER_ENABLED);

        boolean migrationNeeded = !McrtxRuntimeSettingParser.readBooleanSetting(values, WATER_MATERIAL_THICKNESS_MIGRATION_KEY, false)
                && values.containsKey(WATER_MATERIAL_THICKNESS_KEY)
                && waterMaterialThicknessThousandths == LEGACY_DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
        if (migrationNeeded) {
            waterMaterialThicknessThousandths = DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
        }
        return migrationNeeded;
    }

    static void writeLocked(Map<String, String> values) {
        values.put(DISPLACEMENT_FACTOR_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(displacementFactorHundredths));
        values.put(SUBSURFACE_MEASUREMENT_DISTANCE_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(subsurfaceMeasurementDistanceHundredths));
        values.put(SUBSURFACE_RADIUS_SCALE_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(subsurfaceRadiusScaleHundredths));
        values.put(SUBSURFACE_MAX_SAMPLE_RADIUS_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(subsurfaceMaxSampleRadiusHundredths));
        values.put(SUBSURFACE_VOLUMETRIC_ANISOTROPY_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(subsurfaceVolumetricAnisotropyHundredths));
        values.put(SUBSURFACE_DIFFUSION_PROFILE_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(subsurfaceDiffusionProfileEnabled));
        values.put(WATER_THIN_WALLED_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(waterThinWalledEnabled));
        values.put(WATER_MATERIAL_THICKNESS_KEY, McrtxRuntimeSettingFormatter.formatThousandthsValue(waterMaterialThicknessThousandths));
        values.put(WATER_TRANSMITTANCE_RED_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(waterTransmittanceRedHundredths));
        values.put(WATER_TRANSMITTANCE_GREEN_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(waterTransmittanceGreenHundredths));
        values.put(WATER_TRANSMITTANCE_BLUE_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(waterTransmittanceBlueHundredths));
        values.put(WATER_TRANSMITTANCE_DISTANCE_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(waterTransmittanceDistanceHundredths));
        values.put(WATER_REFRACTIVE_INDEX_KEY, McrtxRuntimeSettingFormatter.formatThousandthsValue(waterRefractiveIndexThousandths));
        values.put(WATER_DIFFUSE_LAYER_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(waterDiffuseLayerEnabled));
        values.put(WATER_MATERIAL_THICKNESS_MIGRATION_KEY, McrtxRuntimeSettingFormatter.formatBoolean(true));
    }

    private static int readHundredths(Map<String, String> values, String key, int defaultValue, int minimum, int maximum) {
        return McrtxRuntimeSettingParser.readScaledIntSetting(values, key, defaultValue, minimum, maximum, 100);
    }

    private static int readDisplacementFactor(Map<String, String> values) {
        String configuredValue = McrtxRuntimeSettingParser.readConfiguredValue(values, DISPLACEMENT_FACTOR_KEY);
        if (configuredValue == null || configuredValue.isEmpty()) return DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS;
        try {
            double parsed = Double.parseDouble(configuredValue.trim());
            int value;
            if (parsed > (double) MAX_DISPLACEMENT_FACTOR_HUNDREDTHS / 100.0 && parsed <= 40.0 && Math.rint(parsed) == parsed) {
                value = (int) Math.round(parsed * 10.0);
            } else {
                value = (int) Math.round(parsed * 100.0);
            }
            return McrtxRuntimeSettingParser.clamp(value, MIN_DISPLACEMENT_FACTOR_HUNDREDTHS, MAX_DISPLACEMENT_FACTOR_HUNDREDTHS);
        } catch (NumberFormatException exception) {
            return DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS;
        }
    }

    private static void setHundredths(int setting, int value) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int minimum;
            int maximum;
            int current;
            if (setting == 0) { minimum = MIN_DISPLACEMENT_FACTOR_HUNDREDTHS; maximum = MAX_DISPLACEMENT_FACTOR_HUNDREDTHS; current = displacementFactorHundredths; }
            else if (setting == 1) { minimum = MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS; maximum = MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS; current = subsurfaceMeasurementDistanceHundredths; }
            else if (setting == 2) { minimum = MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS; maximum = MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS; current = subsurfaceRadiusScaleHundredths; }
            else if (setting == 3) { minimum = MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS; maximum = MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS; current = subsurfaceMaxSampleRadiusHundredths; }
            else if (setting == 4) { minimum = MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS; maximum = MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS; current = subsurfaceVolumetricAnisotropyHundredths; }
            else if (setting >= 5 && setting <= 7) { minimum = MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS; maximum = MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS; current = setting == 5 ? waterTransmittanceRedHundredths : setting == 6 ? waterTransmittanceGreenHundredths : waterTransmittanceBlueHundredths; }
            else { minimum = MIN_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS; maximum = MAX_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS; current = waterTransmittanceDistanceHundredths; }
            int normalized = McrtxRuntimeSettingParser.clamp(value, minimum, maximum);
            if (current == normalized) return;
            if (setting == 0) displacementFactorHundredths = normalized;
            else if (setting == 1) subsurfaceMeasurementDistanceHundredths = normalized;
            else if (setting == 2) subsurfaceRadiusScaleHundredths = normalized;
            else if (setting == 3) subsurfaceMaxSampleRadiusHundredths = normalized;
            else if (setting == 4) subsurfaceVolumetricAnisotropyHundredths = normalized;
            else if (setting == 5) waterTransmittanceRedHundredths = normalized;
            else if (setting == 6) waterTransmittanceGreenHundredths = normalized;
            else if (setting == 7) waterTransmittanceBlueHundredths = normalized;
            else waterTransmittanceDistanceHundredths = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }
}
