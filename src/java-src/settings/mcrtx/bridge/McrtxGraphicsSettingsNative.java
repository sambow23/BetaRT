package mcrtx.bridge;

public final class McrtxGraphicsSettingsNative {
    public static final int FEATURE_DLSS_SUPER_RESOLUTION = 1 << 0;
    public static final int FEATURE_DLSS_RAY_RECONSTRUCTION = 1 << 1;
    public static final int FEATURE_NRD = 1 << 2;
    public static final int FEATURE_TAAU = 1 << 3;
    public static final int FEATURE_DLSS_FRAME_GENERATION = 1 << 4;

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

    public static void setFrameGenerationConfig(boolean enabled, int multiplier) {
        if (ready()) {
            nSetFrameGenerationConfig(enabled, multiplier);
        }
    }

    public static int getCompiledFeatureMask() {
        return RemixBridgeNative.isAvailable() ? nGetCompiledFeatureMask() : 0;
    }

    public static int getAvailableFeatureMask() {
        return RemixBridgeNative.isAvailable() ? nGetAvailableFeatureMask() : 0;
    }

    public static int getDlssFrameGenerationMaxInterpolatedFrames() {
        return RemixBridgeNative.isAvailable() ? nGetDlssFrameGenerationMaxInterpolatedFrames() : 0;
    }

    public static void setRemixAtmosphereCloudsEnabled(boolean enabled) {
        if (ready()) {
            nSetRemixAtmosphereCloudsEnabled(enabled);
        }
    }

    public static void setAerialPerspectiveEnabled(boolean enabled) {
        if (ready()) {
            nSetAerialPerspectiveEnabled(enabled);
        }
    }

    public static void setAerialPerspectiveStrength(int strength) {
        if (ready()) {
            nSetAerialPerspectiveStrength(strength);
        }
    }

    public static void setAerialPerspectiveSceneShadowEnabled(boolean enabled) {
        if (ready()) {
            nSetAerialPerspectiveSceneShadowEnabled(enabled);
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
    private static native void nSetFrameGenerationConfig(boolean enabled, int multiplier);
    private static native int nGetCompiledFeatureMask();
    private static native int nGetAvailableFeatureMask();
    private static native int nGetDlssFrameGenerationMaxInterpolatedFrames();
    private static native void nSetRemixAtmosphereCloudsEnabled(boolean enabled);
    private static native void nSetAerialPerspectiveEnabled(boolean enabled);
    private static native void nSetAerialPerspectiveStrength(int strength);
    private static native void nSetAerialPerspectiveSceneShadowEnabled(boolean enabled);
}
