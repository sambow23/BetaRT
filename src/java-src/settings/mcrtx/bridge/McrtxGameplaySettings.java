package mcrtx.bridge;

import java.util.Map;

public final class McrtxGameplaySettings {
    public static final String PLAYER_SHADOWS_ENABLED_KEY = "MCRTX_PLAYER_SHADOWS_ENABLED";
    public static final String HELD_TORCH_LIGHTS_ENABLED_KEY = "MCRTX_HELD_TORCH_LIGHTS_ENABLED";
    public static final String GAMEPLAY_FOV_KEY = "MCRTX_GAMEPLAY_FOV";
    public static final String VIEW_MODEL_FOV_KEY = "MCRTX_VIEWMODEL_FOV";
    public static final String BLOCK_OUTLINE_ENABLED_KEY = "MCRTX_BLOCK_OUTLINE_ENABLED";
    public static final String BLOCK_OUTLINE_STYLE_KEY = "MCRTX_BLOCK_OUTLINE_STYLE";
    public static final String BLOCK_OUTLINE_EMISSIVE_INTENSITY_KEY = "MCRTX_BLOCK_OUTLINE_EMISSIVE_INTENSITY";

    public static final int MIN_GAMEPLAY_FOV_DEGREES = 30;
    public static final int MAX_GAMEPLAY_FOV_DEGREES = 120;
    public static final int DEFAULT_GAMEPLAY_FOV_DEGREES = 70;
    public static final int MIN_VIEW_MODEL_FOV_DEGREES = 30;
    public static final int MAX_VIEW_MODEL_FOV_DEGREES = 120;
    public static final int DEFAULT_VIEW_MODEL_FOV_DEGREES = 70;
    public static final int MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS = 0;
    public static final int MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS = 1000;
    public static final int DEFAULT_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS = 450;

    public static final int BLOCK_OUTLINE_STYLE_SUBTLE = 0;
    public static final int BLOCK_OUTLINE_STYLE_BOLD = 1;
    public static final int BLOCK_OUTLINE_STYLE_SOLID = 2;
    public static final int BLOCK_OUTLINE_STYLE_GLOW = 3;
    public static final int BLOCK_OUTLINE_STYLE_RGB = 4;
    public static final int BLOCK_OUTLINE_STYLE_THIN = 5;

    private static boolean playerShadowsEnabled = true;
    private static boolean heldTorchLightsEnabled = true;
    private static int gameplayFovDegrees = DEFAULT_GAMEPLAY_FOV_DEGREES;
    private static int viewModelFovDegrees = DEFAULT_VIEW_MODEL_FOV_DEGREES;
    private static boolean blockOutlineEnabled = true;
    private static int blockOutlineStyle = BLOCK_OUTLINE_STYLE_BOLD;
    private static int blockOutlineEmissiveIntensityHundredths = DEFAULT_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS;

    private McrtxGameplaySettings() {
    }

    public static boolean isPlayerShadowsEnabled() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return playerShadowsEnabled;
        }
    }

    public static void setPlayerShadowsEnabled(boolean enabled) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            if (playerShadowsEnabled == enabled) {
                return;
            }
            playerShadowsEnabled = enabled;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static boolean isHeldTorchLightsEnabled() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return heldTorchLightsEnabled;
        }
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            if (heldTorchLightsEnabled == enabled) {
                return;
            }
            heldTorchLightsEnabled = enabled;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static int getGameplayFovDegrees() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return gameplayFovDegrees;
        }
    }

    public static void setGameplayFovDegrees(int fovDegrees) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int normalized = McrtxRuntimeSettingParser.clamp(
                    fovDegrees, MIN_GAMEPLAY_FOV_DEGREES, MAX_GAMEPLAY_FOV_DEGREES);
            if (gameplayFovDegrees == normalized) {
                return;
            }
            gameplayFovDegrees = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static int getViewModelFovDegrees() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return viewModelFovDegrees;
        }
    }

    public static void setViewModelFovDegrees(int fovDegrees) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int normalized = McrtxRuntimeSettingParser.clamp(
                    fovDegrees, MIN_VIEW_MODEL_FOV_DEGREES, MAX_VIEW_MODEL_FOV_DEGREES);
            if (viewModelFovDegrees == normalized) {
                return;
            }
            viewModelFovDegrees = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static boolean isBlockOutlineEnabled() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return blockOutlineEnabled;
        }
    }

    public static void setBlockOutlineEnabled(boolean enabled) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            if (blockOutlineEnabled == enabled) {
                return;
            }
            blockOutlineEnabled = enabled;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static int getBlockOutlineStyle() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return blockOutlineStyle;
        }
    }

    public static void setBlockOutlineStyle(int style) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int normalized = normalizeBlockOutlineStyle(style);
            if (blockOutlineStyle == normalized) {
                return;
            }
            blockOutlineStyle = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }

    public static int getBlockOutlineEmissiveIntensityHundredths() {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            return blockOutlineEmissiveIntensityHundredths;
        }
    }

    public static float getBlockOutlineEmissiveIntensity() {
        return (float) getBlockOutlineEmissiveIntensityHundredths() / 100.0f;
    }

    public static void setBlockOutlineEmissiveIntensityHundredths(int intensityHundredths) {
        synchronized (McrtxSettingsStore.LOCK) {
            McrtxSettingsStore.ensureLoadedLocked();
            int normalized = McrtxRuntimeSettingParser.clamp(
                    intensityHundredths,
                    MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS,
                    MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS);
            if (blockOutlineEmissiveIntensityHundredths == normalized) {
                return;
            }
            blockOutlineEmissiveIntensityHundredths = normalized;
            McrtxSettingsStore.saveLocked();
        }
    }

    static void loadLocked(Map<String, String> fileValues) {
        playerShadowsEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, PLAYER_SHADOWS_ENABLED_KEY, true);
        heldTorchLightsEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, HELD_TORCH_LIGHTS_ENABLED_KEY, true);
        gameplayFovDegrees = McrtxRuntimeSettingParser.readRoundedIntSetting(
                fileValues, GAMEPLAY_FOV_KEY, DEFAULT_GAMEPLAY_FOV_DEGREES,
                MIN_GAMEPLAY_FOV_DEGREES, MAX_GAMEPLAY_FOV_DEGREES);
        viewModelFovDegrees = McrtxRuntimeSettingParser.readRoundedIntSetting(
                fileValues, VIEW_MODEL_FOV_KEY, DEFAULT_VIEW_MODEL_FOV_DEGREES,
                MIN_VIEW_MODEL_FOV_DEGREES, MAX_VIEW_MODEL_FOV_DEGREES);
        blockOutlineEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, BLOCK_OUTLINE_ENABLED_KEY, true);
        blockOutlineStyle = readBlockOutlineStyle(fileValues);
        blockOutlineEmissiveIntensityHundredths = McrtxRuntimeSettingParser.readScaledIntSetting(
                fileValues,
                BLOCK_OUTLINE_EMISSIVE_INTENSITY_KEY,
                DEFAULT_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS,
                MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS,
                MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS,
                100);
    }

    static void writeLocked(Map<String, String> fileValues) {
        fileValues.put(PLAYER_SHADOWS_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(playerShadowsEnabled));
        fileValues.put(HELD_TORCH_LIGHTS_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(heldTorchLightsEnabled));
        fileValues.put(GAMEPLAY_FOV_KEY, Integer.toString(gameplayFovDegrees));
        fileValues.put(VIEW_MODEL_FOV_KEY, Integer.toString(viewModelFovDegrees));
        fileValues.put(BLOCK_OUTLINE_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(blockOutlineEnabled));
        fileValues.put(BLOCK_OUTLINE_STYLE_KEY, formatBlockOutlineStyle(blockOutlineStyle));
        fileValues.put(
                BLOCK_OUTLINE_EMISSIVE_INTENSITY_KEY,
                McrtxRuntimeSettingFormatter.formatHundredthsValue(blockOutlineEmissiveIntensityHundredths));
    }

    private static int readBlockOutlineStyle(Map<String, String> fileValues) {
        String configuredValue = McrtxRuntimeSettingParser.readConfiguredValue(fileValues, BLOCK_OUTLINE_STYLE_KEY);
        if (configuredValue == null || configuredValue.isEmpty()) {
            return BLOCK_OUTLINE_STYLE_BOLD;
        }
        String value = configuredValue.trim();
        if (value.equalsIgnoreCase("subtle") || value.equalsIgnoreCase("classic") || value.equals("0")) return BLOCK_OUTLINE_STYLE_SUBTLE;
        if (value.equalsIgnoreCase("bold") || value.equals("1")) return BLOCK_OUTLINE_STYLE_BOLD;
        if (value.equalsIgnoreCase("solid") || value.equals("2")) return BLOCK_OUTLINE_STYLE_SOLID;
        if (value.equalsIgnoreCase("glow") || value.equals("3")) return BLOCK_OUTLINE_STYLE_GLOW;
        if (value.equalsIgnoreCase("rgb") || value.equals("4")) return BLOCK_OUTLINE_STYLE_RGB;
        if (value.equalsIgnoreCase("thin") || value.equals("5")) return BLOCK_OUTLINE_STYLE_THIN;
        return BLOCK_OUTLINE_STYLE_BOLD;
    }

    private static int normalizeBlockOutlineStyle(int style) {
        if (style < BLOCK_OUTLINE_STYLE_SUBTLE || style > BLOCK_OUTLINE_STYLE_THIN) {
            return BLOCK_OUTLINE_STYLE_BOLD;
        }
        return style;
    }

    private static String formatBlockOutlineStyle(int style) {
        switch (normalizeBlockOutlineStyle(style)) {
            case BLOCK_OUTLINE_STYLE_SUBTLE: return "Subtle";
            case BLOCK_OUTLINE_STYLE_GLOW: return "Glow";
            case BLOCK_OUTLINE_STYLE_RGB: return "RGB";
            case BLOCK_OUTLINE_STYLE_THIN: return "Thin";
            case BLOCK_OUTLINE_STYLE_SOLID: return "Solid";
            case BLOCK_OUTLINE_STYLE_BOLD:
            default: return "Bold";
        }
    }
}
