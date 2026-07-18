package mcrtx.bridge;

public final class RemixSceneBridge {
    private RemixSceneBridge() {
    }

    public static synchronized void updateCamera(CameraPose cameraPose) {
        if (!RemixLifecycleBridge.isInitialized() || cameraPose == null) {
            return;
        }
        nUpdateCamera(
                cameraPose.px,
                cameraPose.py,
                cameraPose.pz,
                cameraPose.fx,
                cameraPose.fy,
                cameraPose.fz,
                cameraPose.ux,
                cameraPose.uy,
                cameraPose.uz,
                cameraPose.rx,
                cameraPose.ry,
                cameraPose.rz,
                cameraPose.fovYDegrees,
                cameraPose.aspect,
                cameraPose.nearPlane,
                cameraPose.farPlane);
    }

    public static synchronized void updateCamera(
            float px,
            float py,
            float pz,
            float fx,
            float fy,
            float fz,
            float fovYDegrees,
            float aspect,
            float nearPlane,
            float farPlane) {
        // The patched client still invokes this legacy path from its original
        // camera update site, but that path only knows the player-eye pose and
        // will overwrite the authoritative GL-captured camera used for detached
        // third-person views. Keep the ABI for the patched bytecode, but let
        // RemixCameraState drive camera submission instead.
    }

    public static synchronized void updateCloudLayer(
            boolean fancy,
            float cameraX,
            float cameraY,
            float cameraZ,
            float cloudHeight,
            float cloudScroll,
            float celestialAngle,
            float colorR,
            float colorG,
            float colorB) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }
        nUpdateCloudLayer(
                fancy,
                cameraX,
                cameraY,
                cameraZ,
                cloudHeight,
                cloudScroll,
                celestialAngle,
                colorR,
                colorG,
                colorB);
    }

    public static synchronized void updateAtmosphereState(float celestialAngle, boolean forceDarkAtmosphere) {
        if (RemixLifecycleBridge.isInitialized()) {
            nUpdateAtmosphereState(celestialAngle, forceDarkAtmosphere);
        }
    }

    public static synchronized void updateFogState(
            int fogMode,
            float colorR,
            float colorG,
            float colorB,
            float fogScale,
            float fogEnd,
            float fogDensity) {
        if (RemixLifecycleBridge.isInitialized()) {
            nUpdateFogState(fogMode, colorR, colorG, colorB, fogScale, fogEnd, fogDensity);
        }
    }

    public static synchronized void clearCloudLayer() {
        if (RemixLifecycleBridge.isInitialized()) {
            nClearCloudLayer();
        }
    }

    public static synchronized void clearWorldScene() {
        if (RemixLifecycleBridge.isInitialized()) {
            nClearWorldScene();
        }
    }

    public static synchronized void setScreenTint(float r, float g, float b, float a) {
        if (RemixLifecycleBridge.isInitialized()) {
            nSetScreenTint(r, g, b, a);
        }
    }

    private static native void nUpdateCamera(
            double px, double py, double pz,
            float fx, float fy, float fz,
            float ux, float uy, float uz,
            float rx, float ry, float rz,
            float fovYDegrees,
            float aspect,
            float nearPlane,
            float farPlane);
    private static native void nUpdateCloudLayer(
            boolean fancy,
            float cameraX,
            float cameraY,
            float cameraZ,
            float cloudHeight,
            float cloudScroll,
            float celestialAngle,
            float colorR,
            float colorG,
            float colorB);
    private static native void nUpdateAtmosphereState(float celestialAngle, boolean forceDarkAtmosphere);
    private static native void nUpdateFogState(
            int fogMode,
            float colorR,
            float colorG,
            float colorB,
            float fogScale,
            float fogEnd,
            float fogDensity);
    private static native void nClearCloudLayer();
    private static native void nClearWorldScene();
    private static native void nSetScreenTint(float r, float g, float b, float a);
}
