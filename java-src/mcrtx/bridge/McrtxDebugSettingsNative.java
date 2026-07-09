package mcrtx.bridge;

public final class McrtxDebugSettingsNative {
    private McrtxDebugSettingsNative() {
    }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) {
        if (RemixBridgeNative.isAvailable() && MinecraftRenderHooks.isInitialized()) {
            nSetDynamicEntityRenderingEnabled(enabled);
        }
    }

    private static native void nSetDynamicEntityRenderingEnabled(boolean enabled);
}
