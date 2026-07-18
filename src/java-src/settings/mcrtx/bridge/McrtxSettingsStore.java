package mcrtx.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public final class McrtxSettingsStore {
    public static final int CATEGORY_GAMEPLAY = 0;
    public static final int CATEGORY_GRAPHICS = 1;
    public static final int CATEGORY_DEBUG = 2;
    public static final int CATEGORY_MATERIAL = 3;
    public static final int DEFAULT_CATEGORY = CATEGORY_GAMEPLAY;

    private static final String QUICK_SETTINGS_CATEGORY_KEY = "MCRTX_QUICK_SETTINGS_CATEGORY";

    static final Object LOCK = new Object();

    private static boolean loaded;
    private static Map<String, String> settingsSnapshot;
    private static int quickSettingsCategory = DEFAULT_CATEGORY;

    private McrtxSettingsStore() {
    }

    public static int getQuickSettingsCategory() {
        synchronized (LOCK) {
            ensureLoadedLocked();
            return quickSettingsCategory;
        }
    }

    public static void setQuickSettingsCategory(int category) {
        synchronized (LOCK) {
            ensureLoadedLocked();
            int normalizedCategory = normalizeCategory(category);
            if (quickSettingsCategory == normalizedCategory) {
                return;
            }
            quickSettingsCategory = normalizedCategory;
            saveLocked();
        }
    }

    static void ensureLoadedLocked() {
        if (loaded) {
            return;
        }

        Map<String, String> fileValues = new TreeMap<String, String>(McrtxRuntimeConfig.loadFileValuesSnapshot());
        settingsSnapshot = fileValues;
        McrtxGameplaySettings.loadLocked(fileValues);
        McrtxGraphicsSettings.loadLocked(fileValues);
        McrtxDebugSettings.loadLocked(fileValues);
        boolean materialMigrationNeeded = McrtxMaterialSettings.loadLocked(fileValues);
        quickSettingsCategory = McrtxRuntimeSettingParser.readIntSetting(
                fileValues,
                QUICK_SETTINGS_CATEGORY_KEY,
                DEFAULT_CATEGORY,
                CATEGORY_GAMEPLAY,
                CATEGORY_MATERIAL);
        loaded = true;

        if (materialMigrationNeeded) {
            saveLocked();
        }
    }

    static void saveLocked() {
        Map<String, String> fileValues = new TreeMap<String, String>(settingsSnapshot);
        McrtxGameplaySettings.writeLocked(fileValues);
        McrtxGraphicsSettings.writeLocked(fileValues);
        McrtxDebugSettings.writeLocked(fileValues);
        McrtxMaterialSettings.writeLocked(fileValues);
        fileValues.put(QUICK_SETTINGS_CATEGORY_KEY, Integer.toString(quickSettingsCategory));
        writeFileValues(fileValues);
        settingsSnapshot = fileValues;
    }

    private static int normalizeCategory(int category) {
        if (category < CATEGORY_GAMEPLAY || category > CATEGORY_MATERIAL) {
            return DEFAULT_CATEGORY;
        }
        return category;
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
}
