package mcrtx.bridge;

public final class McrtxCloudMode {
    public static final String REMIX_ATMOSPHERE_CLOUDS_ENABLED_KEY = "MCRTX_REMIX_ATMOSPHERE_CLOUDS_ENABLED";
    public static final boolean DEFAULT_REMIX_ATMOSPHERE_CLOUDS_ENABLED = false;

    private McrtxCloudMode() {
    }

    public static boolean shouldSubmitGameCloudLayer(boolean initialized, boolean remixAtmosphereCloudsEnabled) {
        return initialized && !remixAtmosphereCloudsEnabled;
    }

    public static boolean shouldClearGameCloudLayerAfterToggle(boolean previousEnabled, boolean nextEnabled) {
        return !previousEnabled && nextEnabled;
    }

    public static String formatButtonLabel(boolean remixAtmosphereCloudsEnabled) {
        return "Remix Clouds: " + (remixAtmosphereCloudsEnabled ? "ON" : "OFF");
    }
}
