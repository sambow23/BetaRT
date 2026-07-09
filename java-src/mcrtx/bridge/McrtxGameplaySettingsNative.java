package mcrtx.bridge;

public final class McrtxGameplaySettingsNative {
    private McrtxGameplaySettingsNative() {
    }

    public static void setPlayerShadowsEnabled(boolean enabled) { if (ready()) nSetPlayerShadowsEnabled(enabled); }
    public static void setHeldTorchLightsEnabled(boolean enabled) { if (ready()) nSetHeldTorchLightsEnabled(enabled); }
    public static void setBlockOutlineEnabled(boolean enabled) { if (ready()) nSetBlockOutlineEnabled(enabled); }
    public static void setBlockOutlineStyle(int style) { if (ready()) nSetBlockOutlineStyle(style); }
    public static void setBlockOutlineEmissiveIntensity(float intensity) { if (ready()) nSetBlockOutlineEmissiveIntensity(intensity); }
    public static void setViewModelFovDegrees(int fovDegrees) { if (ready()) nSetViewModelFovDegrees((float) fovDegrees); }

    private static boolean ready() {
        return RemixBridgeNative.isAvailable() && MinecraftRenderHooks.isInitialized();
    }

    private static native void nSetPlayerShadowsEnabled(boolean enabled);
    private static native void nSetHeldTorchLightsEnabled(boolean enabled);
    private static native void nSetBlockOutlineEnabled(boolean enabled);
    private static native void nSetBlockOutlineStyle(int style);
    private static native void nSetBlockOutlineEmissiveIntensity(float intensity);
    private static native void nSetViewModelFovDegrees(float fovYDegrees);
}
