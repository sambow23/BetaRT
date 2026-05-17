package mcrtx.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public final class McrtxRuntimeSettings {
    public static final String PLAYER_SHADOWS_ENABLED_KEY = "MCRTX_PLAYER_SHADOWS_ENABLED";
    public static final String HELD_TORCH_LIGHTS_ENABLED_KEY = "MCRTX_HELD_TORCH_LIGHTS_ENABLED";
    public static final String UPSCALER_TYPE_KEY = "MCRTX_UPSCALER_TYPE";
    public static final String DLSS_PRESET_KEY = "MCRTX_DLSS_PRESET";
    public static final String XESS_PRESET_KEY = "MCRTX_XESS_PRESET";
    public static final String TAAU_PRESET_KEY = "MCRTX_TAAU_PRESET";
    public static final String RAY_RECONSTRUCTION_ENABLED_KEY = "MCRTX_RAY_RECONSTRUCTION_ENABLED";
    public static final String RT_QUALITY_KEY = "MCRTX_RT_QUALITY";

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

    private static final Object LOCK = new Object();

    private static boolean loaded;
    private static boolean playerShadowsEnabled = true;
    private static boolean heldTorchLightsEnabled = true;
    private static int upscalerType = UPSCALER_TYPE_DLSS;
    private static int dlssPreset = DLSS_PRESET_AUTO;
    private static int xessPreset = XESS_PRESET_BALANCED;
    private static int taauPreset = TAAU_PRESET_BALANCED;
    private static boolean rayReconstructionEnabled = true;
    private static int rtQuality = RT_QUALITY_HIGH;

    private McrtxRuntimeSettings() {
    }

    public static boolean isPlayerShadowsEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return playerShadowsEnabled;
        }
    }

    public static boolean isHeldTorchLightsEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return heldTorchLightsEnabled;
        }
    }

    public static void setPlayerShadowsEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (playerShadowsEnabled == enabled) {
                return;
            }
            playerShadowsEnabled = enabled;
            saveLocked();
        }
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (heldTorchLightsEnabled == enabled) {
                return;
            }
            heldTorchLightsEnabled = enabled;
            saveLocked();
        }
    }

    public static int getUpscalerType() {
        synchronized (LOCK) {
            ensureLoaded();
            return upscalerType;
        }
    }

    public static void setUpscalerType(int type) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedType = normalizeUpscalerType(type);
            if (upscalerType == normalizedType) {
                return;
            }
            upscalerType = normalizedType;
            saveLocked();
        }
    }

    public static int getDlssPreset() {
        synchronized (LOCK) {
            ensureLoaded();
            return dlssPreset;
        }
    }

    public static void setDlssPreset(int preset) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedPreset = normalizeDlssPreset(preset);
            if (dlssPreset == normalizedPreset) {
                return;
            }
            dlssPreset = normalizedPreset;
            saveLocked();
        }
    }

    public static int getXessPreset() {
        synchronized (LOCK) {
            ensureLoaded();
            return xessPreset;
        }
    }

    public static void setXessPreset(int preset) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedPreset = normalizeXessPreset(preset);
            if (xessPreset == normalizedPreset) {
                return;
            }
            xessPreset = normalizedPreset;
            saveLocked();
        }
    }

    public static int getTaauPreset() {
        synchronized (LOCK) {
            ensureLoaded();
            return taauPreset;
        }
    }

    public static void setTaauPreset(int preset) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedPreset = normalizeTaauPreset(preset);
            if (taauPreset == normalizedPreset) {
                return;
            }
            taauPreset = normalizedPreset;
            saveLocked();
        }
    }

    public static boolean isRayReconstructionEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return rayReconstructionEnabled;
        }
    }

    public static int getRtQuality() {
        synchronized (LOCK) {
            ensureLoaded();
            return rtQuality;
        }
    }

    public static void setRayReconstructionEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (rayReconstructionEnabled == enabled) {
                return;
            }
            rayReconstructionEnabled = enabled;
            saveLocked();
        }
    }

    public static void setRtQuality(int quality) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedQuality = normalizeRtQuality(quality);
            if (rtQuality == normalizedQuality) {
                return;
            }
            rtQuality = normalizedQuality;
            saveLocked();
        }
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        Map<String, String> fileValues = McrtxRuntimeConfig.loadFileValuesSnapshot();
        playerShadowsEnabled = readBooleanSetting(fileValues, PLAYER_SHADOWS_ENABLED_KEY, true);
        heldTorchLightsEnabled = readBooleanSetting(fileValues, HELD_TORCH_LIGHTS_ENABLED_KEY, true);
        upscalerType = readUpscalerTypeSetting(fileValues, UPSCALER_TYPE_KEY, deriveDefaultUpscalerType(fileValues));
        dlssPreset = readDlssPresetSetting(fileValues, DLSS_PRESET_KEY, DLSS_PRESET_AUTO);
        xessPreset = readXessPresetSetting(fileValues, XESS_PRESET_KEY, XESS_PRESET_BALANCED);
        taauPreset = readTaauPresetSetting(fileValues, TAAU_PRESET_KEY, TAAU_PRESET_BALANCED);
        rayReconstructionEnabled = readBooleanSetting(fileValues, RAY_RECONSTRUCTION_ENABLED_KEY, true);
        rtQuality = readRtQualitySetting(fileValues, RT_QUALITY_KEY, RT_QUALITY_HIGH);
        loaded = true;
    }

    private static boolean readBooleanSetting(Map<String, String> fileValues, String key, boolean defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        return McrtxRuntimeConfig.isTruthyValue(configuredValue);
    }

    private static int readDlssPresetSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("on") || trimmed.equalsIgnoreCase("auto") || trimmed.equals("4")) {
            return DLSS_PRESET_AUTO;
        }
        if (trimmed.equalsIgnoreCase("quality") || trimmed.equalsIgnoreCase("maxquality") || trimmed.equals("3")) {
            return DLSS_PRESET_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("balanced") || trimmed.equals("2")) {
            return DLSS_PRESET_BALANCED;
        }
        if (trimmed.equalsIgnoreCase("performance") || trimmed.equalsIgnoreCase("maxperf") || trimmed.equals("1")) {
            return DLSS_PRESET_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("ultraperformance") || trimmed.equalsIgnoreCase("ultra performance") || trimmed.equalsIgnoreCase("ultraperf") || trimmed.equals("0")) {
            return DLSS_PRESET_ULTRA_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("dlaa") || trimmed.equalsIgnoreCase("fullresolution") || trimmed.equalsIgnoreCase("full resolution") || trimmed.equals("5")) {
            return DLSS_PRESET_DLAA;
        }
        if (trimmed.equalsIgnoreCase("custom") || trimmed.equalsIgnoreCase("off")) {
            return DLSS_PRESET_AUTO;
        }
        return defaultValue;
    }

    private static int readUpscalerTypeSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("none") || trimmed.equals("0")) {
            return UPSCALER_TYPE_NONE;
        }
        if (trimmed.equalsIgnoreCase("dlss") || trimmed.equals("1")) {
            return UPSCALER_TYPE_DLSS;
        }
        if (trimmed.equalsIgnoreCase("taau") || trimmed.equals("3")) {
            return UPSCALER_TYPE_TAAU;
        }
        if (trimmed.equalsIgnoreCase("xess") || trimmed.equals("4")) {
            return UPSCALER_TYPE_XESS;
        }
        return defaultValue;
    }

    private static int readXessPresetSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("ultraperformance") || trimmed.equalsIgnoreCase("ultra performance") || trimmed.equals("0")) {
            return XESS_PRESET_ULTRA_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("performance") || trimmed.equals("1")) {
            return XESS_PRESET_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("balanced") || trimmed.equals("2")) {
            return XESS_PRESET_BALANCED;
        }
        if (trimmed.equalsIgnoreCase("quality") || trimmed.equals("3")) {
            return XESS_PRESET_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("ultraquality") || trimmed.equalsIgnoreCase("ultra quality") || trimmed.equals("4")) {
            return XESS_PRESET_ULTRA_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("ultraqualityplus") || trimmed.equalsIgnoreCase("ultra quality plus") || trimmed.equals("5")) {
            return XESS_PRESET_ULTRA_QUALITY_PLUS;
        }
        if (trimmed.equalsIgnoreCase("nativeaa") || trimmed.equalsIgnoreCase("native aa") || trimmed.equals("6")) {
            return XESS_PRESET_NATIVE_AA;
        }
        return defaultValue;
    }

    private static int readTaauPresetSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("ultraperformance") || trimmed.equalsIgnoreCase("ultra performance") || trimmed.equals("0")) {
            return TAAU_PRESET_ULTRA_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("performance") || trimmed.equals("1")) {
            return TAAU_PRESET_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("balanced") || trimmed.equals("2")) {
            return TAAU_PRESET_BALANCED;
        }
        if (trimmed.equalsIgnoreCase("quality") || trimmed.equals("3")) {
            return TAAU_PRESET_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("fullscreen") || trimmed.equals("4")) {
            return TAAU_PRESET_FULLSCREEN;
        }
        return defaultValue;
    }

    private static int readRtQualitySetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("low") || trimmed.equals("0")) {
            return RT_QUALITY_LOW;
        }
        if (trimmed.equalsIgnoreCase("medium") || trimmed.equals("1")) {
            return RT_QUALITY_MEDIUM;
        }
        if (trimmed.equalsIgnoreCase("high") || trimmed.equals("2")) {
            return RT_QUALITY_HIGH;
        }
        if (trimmed.equalsIgnoreCase("ultra") || trimmed.equals("3")) {
            return RT_QUALITY_ULTRA;
        }
        if (trimmed.equalsIgnoreCase("potato") || trimmed.equals("4")) {
            return RT_QUALITY_POTATO;
        }
        return defaultValue;
    }

    private static void saveLocked() {
        Map<String, String> fileValues = new TreeMap<String, String>(McrtxRuntimeConfig.loadFileValuesSnapshot());
        fileValues.put(PLAYER_SHADOWS_ENABLED_KEY, formatBoolean(playerShadowsEnabled));
        fileValues.put(HELD_TORCH_LIGHTS_ENABLED_KEY, formatBoolean(heldTorchLightsEnabled));
        fileValues.put(UPSCALER_TYPE_KEY, formatUpscalerType(upscalerType));
        fileValues.put(DLSS_PRESET_KEY, formatDlssPreset(dlssPreset));
        fileValues.put(XESS_PRESET_KEY, formatXessPreset(xessPreset));
        fileValues.put(TAAU_PRESET_KEY, formatTaauPreset(taauPreset));
        fileValues.put(RAY_RECONSTRUCTION_ENABLED_KEY, formatBoolean(rayReconstructionEnabled));
        fileValues.put(RT_QUALITY_KEY, formatRtQuality(rtQuality));
        writeFileValues(fileValues);
    }

    private static void writeFileValues(Map<String, String> values) {
        File configFile = McrtxRuntimeConfig.resolveConfigFile();
        File parentDirectory = configFile.getParentFile();
        if (parentDirectory != null && !parentDirectory.isDirectory()) {
            parentDirectory.mkdirs();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.US_ASCII));
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }

                key = key.trim();
                if (key.isEmpty()) {
                    continue;
                }

                String value = entry.getValue();
                if (value == null) {
                    value = "";
                }

                writer.write(key);
                writer.write('=');
                writer.write(value.trim());
                writer.newLine();
            }
        } catch (IOException exception) {
            System.out.println("[mcrtx] Failed to save runtime settings: " + exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException exception) {
                }
            }
        }
    }

    private static String formatBoolean(boolean enabled) {
        return enabled ? "1" : "0";
    }

    private static int deriveDefaultUpscalerType(Map<String, String> fileValues) {
        String legacyDlssSetting = fileValues.get(DLSS_PRESET_KEY);
        if (legacyDlssSetting != null && legacyDlssSetting.trim().equalsIgnoreCase("off")) {
            return UPSCALER_TYPE_NONE;
        }
        return UPSCALER_TYPE_DLSS;
    }

    private static int normalizeUpscalerType(int type) {
        if (type == UPSCALER_TYPE_NONE || type == UPSCALER_TYPE_DLSS || type == UPSCALER_TYPE_TAAU || type == UPSCALER_TYPE_XESS) {
            return type;
        }
        return UPSCALER_TYPE_DLSS;
    }

    private static int normalizeDlssPreset(int preset) {
        if (preset >= DLSS_PRESET_ULTRA_PERFORMANCE && preset <= DLSS_PRESET_DLAA) {
            return preset;
        }
        return DLSS_PRESET_AUTO;
    }

    private static int normalizeXessPreset(int preset) {
        if (preset >= XESS_PRESET_ULTRA_PERFORMANCE && preset <= XESS_PRESET_NATIVE_AA) {
            return preset;
        }
        return XESS_PRESET_BALANCED;
    }

    private static int normalizeTaauPreset(int preset) {
        if (preset >= TAAU_PRESET_ULTRA_PERFORMANCE && preset <= TAAU_PRESET_FULLSCREEN) {
            return preset;
        }
        return TAAU_PRESET_BALANCED;
    }

    private static int normalizeRtQuality(int quality) {
        if ((quality >= RT_QUALITY_LOW && quality <= RT_QUALITY_ULTRA) || quality == RT_QUALITY_POTATO) {
            return quality;
        }
        return RT_QUALITY_HIGH;
    }

    private static String formatUpscalerType(int type) {
        switch (normalizeUpscalerType(type)) {
            case UPSCALER_TYPE_NONE:
                return "None";
            case UPSCALER_TYPE_TAAU:
                return "TAAU";
            case UPSCALER_TYPE_XESS:
                return "XeSS";
            case UPSCALER_TYPE_DLSS:
            default:
                return "DLSS";
        }
    }

    private static String formatDlssPreset(int preset) {
        switch (normalizeDlssPreset(preset)) {
            case DLSS_PRESET_QUALITY:
                return "MaxQuality";
            case DLSS_PRESET_BALANCED:
                return "Balanced";
            case DLSS_PRESET_PERFORMANCE:
                return "MaxPerf";
            case DLSS_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerf";
            case DLSS_PRESET_DLAA:
                return "FullResolution";
            case DLSS_PRESET_AUTO:
            default:
                return "Auto";
        }
    }

    private static String formatXessPreset(int preset) {
        switch (normalizeXessPreset(preset)) {
            case XESS_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerf";
            case XESS_PRESET_PERFORMANCE:
                return "Performance";
            case XESS_PRESET_QUALITY:
                return "Quality";
            case XESS_PRESET_ULTRA_QUALITY:
                return "UltraQuality";
            case XESS_PRESET_ULTRA_QUALITY_PLUS:
                return "UltraQualityPlus";
            case XESS_PRESET_NATIVE_AA:
                return "NativeAA";
            case XESS_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static String formatTaauPreset(int preset) {
        switch (normalizeTaauPreset(preset)) {
            case TAAU_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerformance";
            case TAAU_PRESET_PERFORMANCE:
                return "Performance";
            case TAAU_PRESET_QUALITY:
                return "Quality";
            case TAAU_PRESET_FULLSCREEN:
                return "Fullscreen";
            case TAAU_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static String formatRtQuality(int quality) {
        switch (normalizeRtQuality(quality)) {
            case RT_QUALITY_POTATO:
                return "Potato";
            case RT_QUALITY_LOW:
                return "Low";
            case RT_QUALITY_MEDIUM:
                return "Medium";
            case RT_QUALITY_ULTRA:
                return "Ultra";
            case RT_QUALITY_HIGH:
            default:
                return "High";
        }
    }
}