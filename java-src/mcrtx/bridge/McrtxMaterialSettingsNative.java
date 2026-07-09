package mcrtx.bridge;

public final class McrtxMaterialSettingsNative {
    private McrtxMaterialSettingsNative() {
    }

    public static void setDisplacementFactor(float factor) { if (ready()) nSetDisplacementFactor(factor); }
    public static void setSubsurfaceMeasurementDistance(float distance) { if (ready()) nSetSubsurfaceMeasurementDistance(distance); }
    public static void setSubsurfaceRadiusScale(float scale) { if (ready()) nSetSubsurfaceRadiusScale(scale); }
    public static void setSubsurfaceMaxSampleRadius(float radius) { if (ready()) nSetSubsurfaceMaxSampleRadius(radius); }
    public static void setSubsurfaceVolumetricAnisotropy(float anisotropy) { if (ready()) nSetSubsurfaceVolumetricAnisotropy(anisotropy); }
    public static void setSubsurfaceDiffusionProfileEnabled(boolean enabled) { if (ready()) nSetSubsurfaceDiffusionProfileEnabled(enabled); }

    public static void setWaterTransmissionSettings(
            float red,
            float green,
            float blue,
            float measurementDistance,
            float refractiveIndex,
            boolean diffuseLayerEnabled,
            boolean thinWalledEnabled,
            float thickness) {
        if (ready()) {
            nSetWaterTransmissionSettings(
                    red,
                    green,
                    blue,
                    measurementDistance,
                    refractiveIndex,
                    diffuseLayerEnabled,
                    thinWalledEnabled,
                    thickness);
        }
    }

    private static boolean ready() {
        return RemixBridgeNative.isAvailable() && MinecraftRenderHooks.isInitialized();
    }

    private static native void nSetDisplacementFactor(float factor);
    private static native void nSetSubsurfaceMeasurementDistance(float distance);
    private static native void nSetSubsurfaceRadiusScale(float scale);
    private static native void nSetSubsurfaceMaxSampleRadius(float radius);
    private static native void nSetSubsurfaceVolumetricAnisotropy(float anisotropy);
    private static native void nSetSubsurfaceDiffusionProfileEnabled(boolean enabled);
    private static native void nSetWaterTransmissionSettings(
            float red,
            float green,
            float blue,
            float measurementDistance,
            float refractiveIndex,
            boolean diffuseLayerEnabled,
            boolean thinWalledEnabled,
            float thickness);
}
