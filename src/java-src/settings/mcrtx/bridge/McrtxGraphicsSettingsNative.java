package mcrtx.bridge;

public final class McrtxGraphicsSettingsNative {
    private McrtxGraphicsSettingsNative() {
    }

    public static void setRtQuality(int quality) { if (ready()) nSetRtQuality(quality); }

    public static void setUpscalerConfig(
            int upscalerType,
            int dlssPreset,
            int xessPreset,
            int taauPreset,
            boolean rayReconstructionEnabled,
            boolean sparseRenderingEnabled) {
        if (ready()) {
            nSetUpscalerConfig(
                    upscalerType,
                    dlssPreset,
                    xessPreset,
                    taauPreset,
                    rayReconstructionEnabled,
                    sparseRenderingEnabled);
        }
    }

    public static void setRemixAtmosphereCloudsEnabled(boolean enabled) {
        if (ready()) {
            nSetRemixAtmosphereCloudsEnabled(enabled);
        }
    }

    private static boolean ready() {
        return RemixBridgeNative.isAvailable() && RemixLifecycleBridge.isInitialized();
    }

    private static native void nSetRtQuality(int quality);
    private static native void nSetUpscalerConfig(
            int upscalerType,
            int dlssPreset,
            int xessPreset,
            int taauPreset,
            boolean rayReconstructionEnabled,
            boolean sparseRenderingEnabled);
    private static native void nSetRemixAtmosphereCloudsEnabled(boolean enabled);
}
