package mcrtx.bridge;

import java.util.Map;

public final class McrtxLodSettings {
    public static final String LOD_ENABLED_KEY = "MCRTX_LOD_ENABLED";
    public static final String LOD_DEBUG_VIEW_ENABLED_KEY = "MCRTX_LOD_DEBUG_VIEW_ENABLED";
    public static final String LOD_DISTANCE_KEY = "MCRTX_LOD_DISTANCE";
    public static final String LOD_HANDOVER_DISTANCE_KEY = "MCRTX_LOD_HANDOVER_DISTANCE";
    public static final String LOD_COVERAGE_HIDE_FRACTION_KEY = "MCRTX_LOD_COVERAGE_HIDE_FRACTION";
    public static final String LOD_BUILD_BUDGET_KEY = "MCRTX_LOD_BUILD_BUDGET";
    public static final String LOD_EXTENDED_FOG_KEY = "MCRTX_LOD_EXTENDED_FOG";
    public static final String LOD_CACHE_ENABLED_KEY = "MCRTX_LOD_CACHE_ENABLED";

    /**
     * Set once the stored handover distance has been moved off the old default.
     * That default hid every region the field could actually build, so leaving
     * a saved copy of it in place would keep distant terrain invisible for
     * anyone who had already run the old build.
     */
    private static final String LOD_HANDOVER_MIGRATION_KEY = "MCRTX_LOD_HANDOVER_MIGRATED";
    private static final int LEGACY_DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS = 192;

    /**
     * Distances the field can end at: the outer edge of each detail ring.
     *
     * <p>The field is concentric rings of regions, each at its own detail level,
     * and a ring is all or nothing -- half a ring would leave a band of ground
     * that no level covers. So the distance setting selects a ring rather than a
     * number of blocks, and every value it can hold is one of these.
     */
    private static final int[] LOD_DISTANCE_STOPS = RemixLodBridge.LOD_RING_OUTER_BLOCKS;

    public static final boolean DEFAULT_LOD_ENABLED = true;
    public static final boolean DEFAULT_LOD_DEBUG_VIEW_ENABLED = false;
    public static final boolean DEFAULT_LOD_EXTENDED_FOG = true;
    /**
     * Persist observed ground to disk and read it back for regions with no
     * loaded chunks.
     *
     * <p>On by default because without it the two outer rings never populate:
     * they cover ground a client is never sent, so their only possible source
     * is something that outlived a previous session.
     */
    public static final boolean DEFAULT_LOD_CACHE_ENABLED = true;
    public static final int MIN_LOD_DISTANCE_BLOCKS = LOD_DISTANCE_STOPS[0];
    public static final int MAX_LOD_DISTANCE_BLOCKS =
            LOD_DISTANCE_STOPS[LOD_DISTANCE_STOPS.length - 1];
    /**
     * Three rings by default: 2, 4 and 8 blocks per column out to 1024.
     *
     * <p>That is 139 regions, so even if every one of them held water -- a
     * second, translucent instance -- the field stays inside the instance budget
     * with room to spare. The two outer stops reach 2048 and 4096 and are one
     * and two slider notches away; they are left off by default because their
     * regions cover ground no vanilla server ever sends and that a single-player
     * save only holds if the player has walked it, so they would mostly be
     * empty. What they cost when they are <em>not</em> empty is modest and
     * measured -- a step-32 region meshes to about 2,300 triangles against a
     * step-2 region's 6,000, because coarse ground is smooth and the coarse
     * levels draw their canopies as ground -- so the reason they are off is
     * that most players would see nothing for them, not that they are ruinous.
     */
    public static final int DEFAULT_LOD_DISTANCE_BLOCKS = LOD_DISTANCE_STOPS[2];
    public static final int MIN_LOD_HANDOVER_DISTANCE_BLOCKS = 0;
    public static final int MAX_LOD_HANDOVER_DISTANCE_BLOCKS = 512;
    /**
     * Distant terrain can only be built where the client already holds chunks,
     * so hiding a wide radius around the player hides most of what the field
     * has. Handover is decided by measured chunk coverage; this is the floor.
     */
    public static final int DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS = 96;
    public static final int MIN_LOD_COVERAGE_HIDE_HUNDREDTHS = 0;
    public static final int MAX_LOD_COVERAGE_HIDE_HUNDREDTHS = 100;
    public static final int DEFAULT_LOD_COVERAGE_HIDE_HUNDREDTHS = 80;
    public static final int MIN_LOD_BUILD_BUDGET = 1;
    public static final int MAX_LOD_BUILD_BUDGET = 8;
    public static final int DEFAULT_LOD_BUILD_BUDGET = 1;

    private static boolean lodEnabled = DEFAULT_LOD_ENABLED;
    private static boolean lodDebugViewEnabled = DEFAULT_LOD_DEBUG_VIEW_ENABLED;
    private static boolean lodExtendedFog = DEFAULT_LOD_EXTENDED_FOG;
    private static boolean lodCacheEnabled = DEFAULT_LOD_CACHE_ENABLED;
    private static int lodDistanceBlocks = DEFAULT_LOD_DISTANCE_BLOCKS;
    private static int lodHandoverDistanceBlocks = DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS;
    private static int lodCoverageHideHundredths = DEFAULT_LOD_COVERAGE_HIDE_HUNDREDTHS;
    private static int lodBuildBudgetPerTick = DEFAULT_LOD_BUILD_BUDGET;

    private McrtxLodSettings() {
    }

    public static boolean isLodEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodEnabled; } }
    public static boolean isLodDebugViewEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodDebugViewEnabled; } }
    public static boolean isLodExtendedFogEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodExtendedFog; } }
    public static boolean isLodCacheEnabled() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodCacheEnabled; } }
    public static int getLodDistanceBlocks() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodDistanceBlocks; } }
    public static int getLodHandoverDistanceBlocks() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodHandoverDistanceBlocks; } }
    public static int getLodCoverageHideHundredths() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodCoverageHideHundredths; } }
    public static float getLodCoverageHideFraction() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return (float) lodCoverageHideHundredths / 100.0f; } }
    public static int getLodBuildBudgetPerTick() { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); return lodBuildBudgetPerTick; } }

    public static void setLodEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (lodEnabled == enabled) return; lodEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setLodDebugViewEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (lodDebugViewEnabled == enabled) return; lodDebugViewEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setLodExtendedFogEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (lodExtendedFog == enabled) return; lodExtendedFog = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setLodCacheEnabled(boolean enabled) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); if (lodCacheEnabled == enabled) return; lodCacheEnabled = enabled; McrtxSettingsStore.saveLocked(); } }
    public static void setLodDistanceBlocks(int blocks) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = snapLodDistance(blocks); if (lodDistanceBlocks == value) return; lodDistanceBlocks = value; McrtxSettingsStore.saveLocked(); } }
    public static void setLodHandoverDistanceBlocks(int blocks) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = McrtxRuntimeSettingParser.clamp(blocks, MIN_LOD_HANDOVER_DISTANCE_BLOCKS, MAX_LOD_HANDOVER_DISTANCE_BLOCKS); if (lodHandoverDistanceBlocks == value) return; lodHandoverDistanceBlocks = value; McrtxSettingsStore.saveLocked(); } }
    public static void setLodCoverageHideHundredths(int hundredths) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = McrtxRuntimeSettingParser.clamp(hundredths, MIN_LOD_COVERAGE_HIDE_HUNDREDTHS, MAX_LOD_COVERAGE_HIDE_HUNDREDTHS); if (lodCoverageHideHundredths == value) return; lodCoverageHideHundredths = value; McrtxSettingsStore.saveLocked(); } }
    public static void setLodBuildBudgetPerTick(int budget) { synchronized (McrtxSettingsStore.LOCK) { McrtxSettingsStore.ensureLoadedLocked(); int value = McrtxRuntimeSettingParser.clamp(budget, MIN_LOD_BUILD_BUDGET, MAX_LOD_BUILD_BUDGET); if (lodBuildBudgetPerTick == value) return; lodBuildBudgetPerTick = value; McrtxSettingsStore.saveLocked(); } }

    /** Number of distances the field can end at. */
    public static int lodDistanceStopCount() {
        return LOD_DISTANCE_STOPS.length;
    }

    /** The distance at a stop, finest first. */
    public static int lodDistanceStopAt(int index) {
        if (index < 0) {
            index = 0;
        } else if (index >= LOD_DISTANCE_STOPS.length) {
            index = LOD_DISTANCE_STOPS.length - 1;
        }
        return LOD_DISTANCE_STOPS[index];
    }

    /**
     * Which stop a distance sits at.
     *
     * <p>The UI wants this rather than the distance itself, because the stops
     * are a geometric ladder -- each is twice the last -- and a slider laid out
     * in blocks would crowd the first three into the left eighth of its track
     * and give the outermost half of it. Positioning by stop gives every rung
     * the same width, which is also the honest picture: one notch is one detail
     * ring, whatever radius it happens to reach.
     */
    public static int lodDistanceStopIndex(int blocks) {
        int snapped = snapLodDistance(blocks);
        for (int index = 0; index < LOD_DISTANCE_STOPS.length; index++) {
            if (LOD_DISTANCE_STOPS[index] == snapped) {
                return index;
            }
        }
        return 0;
    }

    /** Snaps a distance to the nearest ring edge, which is where the field can end. */
    public static int snapLodDistance(int blocks) {
        int nearest = LOD_DISTANCE_STOPS[0];
        int nearestDelta = Math.abs(blocks - nearest);
        for (int index = 1; index < LOD_DISTANCE_STOPS.length; index++) {
            int delta = Math.abs(blocks - LOD_DISTANCE_STOPS[index]);
            if (delta < nearestDelta) {
                nearest = LOD_DISTANCE_STOPS[index];
                nearestDelta = delta;
            }
        }
        return nearest;
    }

    /** @return true if a stored value was migrated and the file needs rewriting. */
    static boolean loadLocked(Map<String, String> fileValues) {
        lodEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, LOD_ENABLED_KEY, DEFAULT_LOD_ENABLED);
        lodExtendedFog = McrtxRuntimeSettingParser.readBooleanSetting(
                fileValues, LOD_EXTENDED_FOG_KEY, DEFAULT_LOD_EXTENDED_FOG);
        lodCacheEnabled = McrtxRuntimeSettingParser.readBooleanSetting(
                fileValues, LOD_CACHE_ENABLED_KEY, DEFAULT_LOD_CACHE_ENABLED);
        lodDebugViewEnabled = McrtxRuntimeSettingParser.readBooleanSetting(fileValues, LOD_DEBUG_VIEW_ENABLED_KEY, DEFAULT_LOD_DEBUG_VIEW_ENABLED);
        lodDistanceBlocks = snapLodDistance(McrtxRuntimeSettingParser.readRoundedIntSetting(
                fileValues, LOD_DISTANCE_KEY, DEFAULT_LOD_DISTANCE_BLOCKS,
                MIN_LOD_DISTANCE_BLOCKS, MAX_LOD_DISTANCE_BLOCKS));
        lodHandoverDistanceBlocks = McrtxRuntimeSettingParser.readRoundedIntSetting(
                fileValues, LOD_HANDOVER_DISTANCE_KEY, DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS,
                MIN_LOD_HANDOVER_DISTANCE_BLOCKS, MAX_LOD_HANDOVER_DISTANCE_BLOCKS);
        lodCoverageHideHundredths = McrtxRuntimeSettingParser.readScaledIntSetting(
                fileValues, LOD_COVERAGE_HIDE_FRACTION_KEY, DEFAULT_LOD_COVERAGE_HIDE_HUNDREDTHS,
                MIN_LOD_COVERAGE_HIDE_HUNDREDTHS, MAX_LOD_COVERAGE_HIDE_HUNDREDTHS, 100);
        lodBuildBudgetPerTick = McrtxRuntimeSettingParser.readIntSetting(
                fileValues, LOD_BUILD_BUDGET_KEY, DEFAULT_LOD_BUILD_BUDGET,
                MIN_LOD_BUILD_BUDGET, MAX_LOD_BUILD_BUDGET);

        boolean migrationNeeded = !McrtxRuntimeSettingParser.readBooleanSetting(
                        fileValues, LOD_HANDOVER_MIGRATION_KEY, false)
                && lodHandoverDistanceBlocks == LEGACY_DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS;
        if (migrationNeeded) {
            lodHandoverDistanceBlocks = DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS;
        }
        return migrationNeeded;
    }

    static void writeLocked(Map<String, String> fileValues) {
        fileValues.put(LOD_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(lodEnabled));
        fileValues.put(LOD_EXTENDED_FOG_KEY, McrtxRuntimeSettingFormatter.formatBoolean(lodExtendedFog));
        fileValues.put(LOD_CACHE_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(lodCacheEnabled));
        fileValues.put(LOD_HANDOVER_MIGRATION_KEY, McrtxRuntimeSettingFormatter.formatBoolean(true));
        fileValues.put(LOD_DEBUG_VIEW_ENABLED_KEY, McrtxRuntimeSettingFormatter.formatBoolean(lodDebugViewEnabled));
        fileValues.put(LOD_DISTANCE_KEY, Integer.toString(lodDistanceBlocks));
        fileValues.put(LOD_HANDOVER_DISTANCE_KEY, Integer.toString(lodHandoverDistanceBlocks));
        fileValues.put(LOD_COVERAGE_HIDE_FRACTION_KEY, McrtxRuntimeSettingFormatter.formatHundredthsValue(lodCoverageHideHundredths));
        fileValues.put(LOD_BUILD_BUDGET_KEY, Integer.toString(lodBuildBudgetPerTick));
    }
}
