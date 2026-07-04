package mcrtx.bridge;

import java.util.Map;

final class McrtxRuntimeSettingParser {
    private McrtxRuntimeSettingParser() {
    }

    static boolean readBooleanSetting(Map<String, String> fileValues, String key, boolean defaultValue) {
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
}
