package mcrtx.bridge;

import java.util.Map;

final class McrtxRuntimeSettingParser {
    private McrtxRuntimeSettingParser() {
    }

    static boolean readBooleanSetting(Map<String, String> fileValues, String key, boolean defaultValue) {
        String configuredValue = readConfiguredValue(fileValues, key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        return McrtxRuntimeConfig.isTruthyValue(configuredValue);
    }

    static String readConfiguredValue(Map<String, String> fileValues, String key) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }
        return configuredValue;
    }

    static int readIntSetting(
            Map<String, String> fileValues,
            String key,
            int defaultValue,
            int minimumValue,
            int maximumValue) {
        String configuredValue = readConfiguredValue(fileValues, key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            return clamp(defaultValue, minimumValue, maximumValue);
        }
        try {
            return clamp(Integer.parseInt(configuredValue.trim()), minimumValue, maximumValue);
        } catch (NumberFormatException exception) {
            return clamp(defaultValue, minimumValue, maximumValue);
        }
    }

    static int readRoundedIntSetting(
            Map<String, String> fileValues,
            String key,
            int defaultValue,
            int minimumValue,
            int maximumValue) {
        String configuredValue = readConfiguredValue(fileValues, key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            return clamp(defaultValue, minimumValue, maximumValue);
        }
        try {
            return clamp(
                    (int) Math.round(Double.parseDouble(configuredValue.trim())),
                    minimumValue,
                    maximumValue);
        } catch (NumberFormatException exception) {
            return clamp(defaultValue, minimumValue, maximumValue);
        }
    }

    static int readScaledIntSetting(
            Map<String, String> fileValues,
            String key,
            int defaultValue,
            int minimumValue,
            int maximumValue,
            int scale) {
        String configuredValue = readConfiguredValue(fileValues, key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            return clamp(defaultValue, minimumValue, maximumValue);
        }
        try {
            int value = (int) Math.round(Double.parseDouble(configuredValue.trim()) * (double) scale);
            return clamp(value, minimumValue, maximumValue);
        } catch (NumberFormatException exception) {
            return clamp(defaultValue, minimumValue, maximumValue);
        }
    }

    static int clamp(int value, int minimumValue, int maximumValue) {
        if (value < minimumValue) {
            return minimumValue;
        }
        if (value > maximumValue) {
            return maximumValue;
        }
        return value;
    }
}
