package mcrtx.bridge;

import java.util.Map;

public final class McrtxGraphicsSettings {
    public static final String REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY = "MCRTX_REMIX_ATMOSPHERE_CLOUDS_ENABLED";
    public static final String GAME_RAIN_PARTICLES_ENABLED_KEY = "MCRTX_GAME_RAIN_PARTICLES_ENABLED";
    public static final String NO_CULL_DISTANCE_KEY = "MCRTX_NO_CULL_DISTANCE";
    public static final String UPSCALER_TYPE_KEY = "MCRTX_UPSCALER_TYPE";
    public static final String DLSS_PRESET_KEY = "MCRTX_DLSS_PRESET";
    public static final String XESS_PRESET_KEY = "MCRTX_XESS_PRESET";
    public static final String TAAU_PRESET_KEY = "MCRTX_TAAU_PRESET";
    public static final String RAY_RECONSTRUCTION_ENABLED_KEY = "MCRTX_RAY_RECONSTRUCTION_ENABLED";
    public static final String SPARSE_RENDERING_ENABLED_KEY = "MCRTX_SPARSE_RENDERING_ENABLED";
    public static final String RT_QUALITY_KEY = "MCRTX_RT_QUALITY";

    public static final int MIN_NO_CULL_DISTANCE_BLOCKS = 0;
    public static final int MAX_NO_CULL_DISTANCE_BLOCKS = 200;
    public static final int DEFAULT_NO_CULL_DISTANCE_BLOCKS = 200;
    public static final boolean DEFAULT_REMIX_ATMOSPHERE_CLOUDS_ENABLED = false;
    public static final boolean DEFAULT_GAME_RAIN_PARTICLES_ENABLED = true;
    public static final boolean DEFAULT_SPARSE_RENDERING_ENABLED = true;

    public static final int UPSCALER_TYPE_NONE = 0;
    public static final int UPSCALER_TYPE_DLSS = 1;
    public static final int UPSCALER_TYPE_TAAU = 3;
    public static final int UPSCALER_TYPE_XESS = 4;

    public static final int DLSS_PRESET_AUTO = 4;
    public static final int DLSS_PRESET_QUALITY = 3;
    public static final int DLSS_PRESET_BALANCED = 2;
    public static final int DLSS_PRESET_PERFORMANCE = 1;
    public static final int DLSS_PRESET_ULTRA_PERFORMANCE = 0;
    public static final int DLSS_PRESET_DLAA = 5;

    public static final int XESS_PRESET_ULTRA_PERFORMANCE = 0;
    public static final int XESS_PRESET_PERFORMANCE = 1;
    public static final int XESS_PRESET_BALANCED = 2;
    public static final int XESS_PRESET_QUALITY = 3;
    public static final int XESS_PRESET_ULTRA_QUALITY = 4;
    public static final int XESS_PRESET_ULTRA_QUALITY_PLUS = 5;
    public static final int XESS_PRESET_NATIVE_AA = 6;

    public static final int TAAU_PRESET_ULTRA_PERFORMANCE = 0;
    public static final int TAAU_PRESET_PERFORMANCE = 1;
    public static final int TAAU_PRESET_BALANCED = 2;
    public static final int TAAU_PRESET_QUALITY = 3;
    public static final int TAAU_PRESET_FULLSCREEN = 4;

    public static final int RT_QUALITY_LOW = 0;
    public static final int RT_QUALITY_MEDIUM = 1;
    public static final int RT_QUALITY_HIGH = 2;
    public static final int RT_QUALITY_ULTRA = 3;
    public static final int RT_QUALITY_POTATO = 4;

    private static boolean remixAtmosphereCloudsEnabled = DEFAULT_REMIX_ATMOSPHERE_CLOUDS_ENABLED;
    private static boolean gameRainParticlesEnabled = DEFAULT_GAME_RAIN_PARTICLES_ENABLED;
    private static int noCullDistanceBlocks = DEFAULT_NO_CULL_DISTANCE_BLOCKS;
    private static int upscalerType = UPSCALER_TYPE_DLSS;
    private static int dlssPreset = DLSS_PRESET_AUTO;
    private static int xessPreset = XESS_PRESET_BALANCED;
    private static int taauPreset = TAAU_PRESET_BALANCED;
    private static boolean rayReconstructionEnabled = true;
    private static boolean sparseRenderingEnabled = DEFAULT_SPARSE_RENDERING_ENABLED;
    private static int rtQuality = RT_QUALITY_HIGH;

    private McrtxGraphicsSettings() {
    }

    public static boolean isRemixAtmosphereCloudsEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return remixAtmosphereCloudsEnabled; } }
    public static boolean isGameRainParticlesEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return gameRainParticlesEnabled; } }
    public static int getNoCullDistanceBlocks() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return noCullDistanceBlocks; } }
    public static int getUpscalerType() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return upscalerType; } }
    public static int getDlssPreset() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return dlssPreset; } }
    public static int getXessPreset() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return xessPreset; } }
    public static int getTaauPreset() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return taauPreset; } }
    public static boolean isRayReconstructionEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return rayReconstructionEnabled; } }
    public static boolean isSparseRenderingEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return sparseRenderingEnabled; } }
    public static int getRtQuality() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return rtQuality; } }

    public static void setRemixAtmosphereCloudsEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (remixAtmosphereCloudsEnabled == enabled) return; remixAtmosphereCloudsEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setGameRainParticlesEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (gameRainParticlesEnabled == enabled) return; gameRainParticlesEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setNoCullDistanceBlocks(int blockDistance) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = McrtxRuntimeSettingParser.clamp(blockDistance, MIN_NO_CULL_DISTANCE_BLOCKS, MAX_NO_CULL_DISTANCE_BLOCKS); if (noCullDistanceBlocks == value) return; noCullDistanceBlocks = value; McrtxSettingsStore.saveLocked(); } }
    public static void setUpscalerType(int type) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = normalizeUpscalerType(type); if (upscalerType == value) return; upscalerType = value; McrtxSettingsStore.saveLocked(); } }
    public static void setDlssPreset(int preset) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = normalizeDlssPreset(preset); if (dlssPreset == value) return; dlssPreset = value; McrtxSettingsStore.saveLocked(); } }
    public static void setXessPreset(int preset) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = normalizeXessPreset(preset); if (xessPreset == value) return; xessPreset = value; McrtxSettingsStore.saveLocked(); } }
    public static void setTaauPreset(int preset) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = normalizeTaauPreset(preset); if (taauPreset == value) return; taauPreset = value; McrtxSettingsStore.saveLocked(); } }
    public static void setRayReconstructionEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (rayReconstructionEnabled == enabled) return; rayReconstructionEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setSparseRenderingEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (sparseRenderingEnabled == enabled) return; sparseRenderingEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setRtQuality(int quality) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = normalizeRtQuality(quality); if (rtQuality == value) return; rtQuality = value; McrtxSettingsStore.saveLocked(); } }

    public static boolean shouldSubmitGameCloudLayer(boolean initialized, boolean remixCloudsEnabled) {
        return initialized && !remixCloudsEnabled;
    }

    public static boolean shouldClearGameCloudLayerAfterToggle(boolean previousEnabled, boolean nextEnabled) {
        return !previousEnabled && nextEnabled;
    }

    public static String formatCloudButtonLabel(boolean enabled) {
        return "Volumetric Clouds: " + (enabled ? "ON" : "OFF");
    }

    public static String formatUpscalerType(int type) {
        switch (normalizeUpscalerType(type)) {
            case UPSCALER_TYPE_NONE: return "None";
            case UPSCALER_TYPE_TAAU: return "TAAU";
            case UPSCALER_TYPE_XESS: return "XeSS";
            case UPSCALER_TYPE_DLSS:
            default: return "DLSS";
        }
    }

    public static String formatDlssPreset(int preset) {
        switch (normalizeDlssPreset(preset)) {
            case DLSS_PRESET_QUALITY: return "MaxQuality";
            case DLSS_PRESET_BALANCED: return "Balanced";
            case DLSS_PRESET_PERFORMANCE: return "MaxPerf";
            case DLSS_PRESET_ULTRA_PERFORMANCE: return "UltraPerf";
            case DLSS_PRESET_DLAA: return "FullResolution";
            case DLSS_PRESET_AUTO:
            default: return "Auto";
        }
    }

    public static String formatXessPreset(int preset) {
        switch (normalizeXessPreset(preset)) {
            case XESS_PRESET_ULTRA_PERFORMANCE: return "UltraPerf";
            case XESS_PRESET_PERFORMANCE: return "Performance";
            case XESS_PRESET_QUALITY: return "Quality";
            case XESS_PRESET_ULTRA_QUALITY: return "UltraQuality";
            case XESS_PRESET_ULTRA_QUALITY_PLUS: return "UltraQualityPlus";
            case XESS_PRESET_NATIVE_AA: return "NativeAA";
            case XESS_PRESET_BALANCED:
            default: return "Balanced";
        }
    }

    public static String formatTaauPreset(int preset) {
        switch (normalizeTaauPreset(preset)) {
            case TAAU_PRESET_ULTRA_PERFORMANCE: return "UltraPerformance";
            case TAAU_PRESET_PERFORMANCE: return "Performance";
            case TAAU_PRESET_QUALITY: return "Quality";
            case TAAU_PRESET_FULLSCREEN: return "Fullscreen";
            case TAAU_PRESET_BALANCED:
            default: return "Balanced";
        }
    }

    public static String formatRtQuality(int quality) {
        switch (normalizeRtQuality(quality)) {
            case RT_QUALITY_POTATO: return "Potato";
            case RT_QUALITY_LOW: return "Low";
            case RT_QUALITY_MEDIUM: return "Medium";
            case RT_QUALITY_ULTRA: return "Ultra";
            case RT_QUALITY_HIGH:
            default: return "High";
        }
    }

    static void loadLocked(Map<String, String> fileValues) {
        remixAtmosphereCloudsEnabled = McrtxRuntimeSettingParser.readBooleanSetting(
                fileValues, REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY, DEFAULT_REMIX_ATMOSPHERE_CLOUDS_ENABLED);
        gameRainParticlesEnabled = McrtxRuntimeSettingParser.readBooleanSetting(
                fileValues, GAME_RAIN_PARTICLES_ENABLED_KEY, DEFAULT_GAME_RAIN_PARTICLES_ENABLED);
        noCullDistanceBlocks = McrtxRuntimeSettingParser.readRoundedIntSetting(
                fileValues, NO_CULL_DISTANCE_KEY, DEFAULT_NO_CULL_DISTANCE_BLOCKS,
                MIN_NO_CULL_DISTANCE_BLOCKS, MAX_NO_CULL_DISTANCE_BLOCKS);
        upscalerType = readUpscalerType(fileValues, deriveDefaultUpscalerType(fileValues));
        dlssPreset = readDlssPreset(fileValues, DLSS_PRESET_AUTO);
        xessPreset = readXessPreset(fileValues, XESS_PRESET_BALANCED);
        taauPreset = readTaauPreset(fileValues, TAAU_PRESET_BALANCED);
        rayReconstructionEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, RAY_RECONSTRUCTION_ENABLED_KEY, true);
        sparseRenderingEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, SPARSE_RENDERING_ENABLED_KEY, DEFAULT_SPARSE_RENDERING_ENABLED);
        rtQuality = readRtQuality(fileValues, RT_QUALITY_HIGH);
    }

    static void writeLocked(Map<String, String> fileValues) {
        fileValues.put(REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(remixAtmosphereCloudsEnabled));
        fileValues.put(GAME_RAIN_PARTICLES_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(gameRainParticlesEnabled));
        fileValues.put(NO_CULL_DISTANCE_KEY, Integer.toString(noCullDistanceBlocks));
        fileValues.put(UPSCALER_TYPE_KEY, formatUpscalerType(upscalerType));
        fileValues.put(DLSS_PRESET_KEY, formatDlssPreset(dlssPreset));
        fileValues.put(XESS_PRESET_KEY, formatXessPreset(xessPreset));
        fileValues.put(TAAU_PRESET_KEY, formatTaauPreset(taauPreset));
        fileValues.put(RAY_RECONSTRUCTION_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(rayReconstructionEnabled));
        fileValues.put(SPARSE_RENDERING_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(sparseRenderingEnabled));
        fileValues.put(RT_QUALITY_KEY, formatRtQuality(rtQuality));
    }

    private static String configured(Map<String, String> values, String key) {
        String value = McrtxRuntimeSettingParser.readConfiguredValue(values, key);
        return value == null ? "" : value.trim();
    }

    private static int readUpscalerType(Map<String, String> values, int defaultValue) {
        String value = configured(values, UPSCALER_TYPE_KEY);
        if (value.equalsIgnoreCase("none") || value.equals("0")) return UPSCALER_TYPE_NONE;
        if (value.equalsIgnoreCase("dlss") || value.equals("1")) return UPSCALER_TYPE_DLSS;
        if (value.equalsIgnoreCase("taau") || value.equals("3")) return UPSCALER_TYPE_TAAU;
        if (value.equalsIgnoreCase("xess") || value.equals("4")) return UPSCALER_TYPE_XESS;
        return defaultValue;
    }

    private static int readDlssPreset(Map<String, String> values, int defaultValue) {
        String value = configured(values, DLSS_PRESET_KEY);
        if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("auto") || value.equals("4")) return DLSS_PRESET_AUTO;
        if (value.equalsIgnoreCase("quality") || value.equalsIgnoreCase("maxquality") || value.equals("3")) return DLSS_PRESET_QUALITY;
        if (value.equalsIgnoreCase("balanced") || value.equals("2")) return DLSS_PRESET_BALANCED;
        if (value.equalsIgnoreCase("performance") || value.equalsIgnoreCase("maxperf") || value.equals("1")) return DLSS_PRESET_PERFORMANCE;
        if (value.equalsIgnoreCase("ultraperformance") || value.equalsIgnoreCase("ultra performance") || value.equalsIgnoreCase("ultraperf") || value.equals("0")) return DLSS_PRESET_ULTRA_PERFORMANCE;
        if (value.equalsIgnoreCase("dlaa") || value.equalsIgnoreCase("fullresolution") || value.equalsIgnoreCase("full resolution") || value.equals("5")) return DLSS_PRESET_DLAA;
        if (value.equalsIgnoreCase("custom") || value.equalsIgnoreCase("off")) return DLSS_PRESET_AUTO;
        return defaultValue;
    }

    private static int readXessPreset(Map<String, String> values, int defaultValue) {
        String value = configured(values, XESS_PRESET_KEY);
        if (value.equalsIgnoreCase("ultraperformance") || value.equalsIgnoreCase("ultra performance") || value.equals("0")) return XESS_PRESET_ULTRA_PERFORMANCE;
        if (value.equalsIgnoreCase("performance") || value.equals("1")) return XESS_PRESET_PERFORMANCE;
        if (value.equalsIgnoreCase("balanced") || value.equals("2")) return XESS_PRESET_BALANCED;
        if (value.equalsIgnoreCase("quality") || value.equals("3")) return XESS_PRESET_QUALITY;
        if (value.equalsIgnoreCase("ultraquality") || value.equalsIgnoreCase("ultra quality") || value.equals("4")) return XESS_PRESET_ULTRA_QUALITY;
        if (value.equalsIgnoreCase("ultraqualityplus") || value.equalsIgnoreCase("ultra quality plus") || value.equals("5")) return XESS_PRESET_ULTRA_QUALITY_PLUS;
        if (value.equalsIgnoreCase("nativeaa") || value.equalsIgnoreCase("native aa") || value.equals("6")) return XESS_PRESET_NATIVE_AA;
        return defaultValue;
    }

    private static int readTaauPreset(Map<String, String> values, int defaultValue) {
        String value = configured(values, TAAU_PRESET_KEY);
        if (value.equalsIgnoreCase("ultraperformance") || value.equalsIgnoreCase("ultra performance") || value.equals("0")) return TAAU_PRESET_ULTRA_PERFORMANCE;
        if (value.equalsIgnoreCase("performance") || value.equals("1")) return TAAU_PRESET_PERFORMANCE;
        if (value.equalsIgnoreCase("balanced") || value.equals("2")) return TAAU_PRESET_BALANCED;
        if (value.equalsIgnoreCase("quality") || value.equals("3")) return TAAU_PRESET_QUALITY;
        if (value.equalsIgnoreCase("fullscreen") || value.equals("4")) return TAAU_PRESET_FULLSCREEN;
        return defaultValue;
    }

    private static int readRtQuality(Map<String, String> values, int defaultValue) {
        String value = configured(values, RT_QUALITY_KEY);
        if (value.equalsIgnoreCase("low") || value.equals("0")) return RT_QUALITY_LOW;
        if (value.equalsIgnoreCase("medium") || value.equals("1")) return RT_QUALITY_MEDIUM;
        if (value.equalsIgnoreCase("high") || value.equals("2")) return RT_QUALITY_HIGH;
        if (value.equalsIgnoreCase("ultra") || value.equals("3")) return RT_QUALITY_ULTRA;
        if (value.equalsIgnoreCase("potato") || value.equals("4")) return RT_QUALITY_POTATO;
        return defaultValue;
    }

    private static int deriveDefaultUpscalerType(Map<String, String> fileValues) {
        String legacyDlssSetting = fileValues.get(DLSS_PRESET_KEY);
        if (legacyDlssSetting != null && legacyDlssSetting.trim().equalsIgnoreCase("off")) {
            return UPSCALER_TYPE_NONE;
        }
        return UPSCALER_TYPE_DLSS;
    }

    private static int normalizeUpscalerType(int type) {
        if (type == UPSCALER_TYPE_NONE || type == UPSCALER_TYPE_DLSS || type == UPSCALER_TYPE_TAAU || type == UPSCALER_TYPE_XESS) return type;
        return UPSCALER_TYPE_DLSS;
    }
    private static int normalizeDlssPreset(int preset) { return preset >= DLSS_PRESET_ULTRA_PERFORMANCE && preset <= DLSS_PRESET_DLAA ? preset : DLSS_PRESET_AUTO; }
    private static int normalizeXessPreset(int preset) { return preset >= XESS_PRESET_ULTRA_PERFORMANCE && preset <= XESS_PRESET_NATIVE_AA ? preset : XESS_PRESET_BALANCED; }
    private static int normalizeTaauPreset(int preset) { return preset >= TAAU_PRESET_ULTRA_PERFORMANCE && preset <= TAAU_PRESET_FULLSCREEN ? preset : TAAU_PRESET_BALANCED; }
    private static int normalizeRtQuality(int quality) { return (quality >= RT_QUALITY_LOW && quality <= RT_QUALITY_ULTRA) || quality == RT_QUALITY_POTATO ? quality : RT_QUALITY_HIGH; }
}
