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

    private static final Object LOCK = new Object();

    private static boolean loaded;
    private static boolean playerShadowsEnabled = true;
    private static boolean heldTorchLightsEnabled = true;

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

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        Map<String, String> fileValues = McrtxRuntimeConfig.loadFileValuesSnapshot();
        playerShadowsEnabled = readBooleanSetting(fileValues, PLAYER_SHADOWS_ENABLED_KEY, true);
        heldTorchLightsEnabled = readBooleanSetting(fileValues, HELD_TORCH_LIGHTS_ENABLED_KEY, true);
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

    private static void saveLocked() {
        Map<String, String> fileValues = new TreeMap<String, String>(McrtxRuntimeConfig.loadFileValuesSnapshot());
        fileValues.put(PLAYER_SHADOWS_ENABLED_KEY, formatBoolean(playerShadowsEnabled));
        fileValues.put(HELD_TORCH_LIGHTS_ENABLED_KEY, formatBoolean(heldTorchLightsEnabled));
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
}