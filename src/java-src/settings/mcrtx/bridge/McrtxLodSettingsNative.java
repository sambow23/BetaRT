package mcrtx.bridge;

public final class McrtxLodSettingsNative {
    private McrtxLodSettingsNative() {
    }

    public static void setLodDebugViewEnabled(boolean enabled) {
        if (RemixBridgeNative.isAvailable() && RemixLifecycleBridge.isInitialized()) {
            nSetLodDebugViewEnabled(enabled);
        }
    }

    private static native void nSetLodDebugViewEnabled(boolean enabled);
}
