package mcrtx.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class McrtxRuntimeConfig {
    private static final String CONFIG_FILE_NAME = "mcrtx-runtime.env";
    private static final Map<String, String> FILE_VALUES = loadFileValues();

    private McrtxRuntimeConfig() {
    }

    public static String getEnvironmentValue(String key) {
        String configuredValue = FILE_VALUES.get(key);
        if (configuredValue != null && !configuredValue.isEmpty()) {
            return configuredValue;
        }

        String environmentValue = System.getenv(key);
        if (environmentValue != null && !environmentValue.isEmpty()) {
            return environmentValue.trim();
        }

        return "";
    }

    public static boolean isTruthyEnvironmentValue(String key) {
        return isTruthyValue(getEnvironmentValue(key));
    }

    public static boolean isTruthyValue(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        char firstCharacter = value.charAt(0);
        return firstCharacter == '1'
                || firstCharacter == 't'
                || firstCharacter == 'T'
                || firstCharacter == 'y'
                || firstCharacter == 'Y';
    }

    private static Map<String, String> loadFileValues() {
        File configFile = new File(System.getProperty("user.dir"), CONFIG_FILE_NAME);
        if (!configFile.isFile()) {
            return Collections.emptyMap();
        }

        Map<String, String> values = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.US_ASCII));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = trimmedLine.substring(separatorIndex + 1).trim();
                if (!key.isEmpty()) {
                    values.put(key, value);
                }
            }
        } catch (IOException exception) {
            return Collections.emptyMap();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exception) {
                }
            }
        }

        return values.isEmpty() ? Collections.<String, String>emptyMap() : values;
    }
}