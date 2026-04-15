package mcrtx.bridge;

public final class MinecraftRenderHooks {
    private static volatile boolean initialized;
    private static String lastError = "";
    private static String lastReportedMessage = "";

    private MinecraftRenderHooks() {
    }

    public static synchronized boolean initializeForCurrentDisplay(int width, int height) {
        long hwnd = LwjglWindowHandleResolver.resolveCurrentHwnd();
        if (hwnd == 0L) {
            lastError = "Failed to resolve LWJGL window handle";
            report(lastError);
            return false;
        }
        return initialize(hwnd, width, height);
    }

    public static synchronized boolean reinitializeForCurrentDisplay(int width, int height) {
        shutdown();
        return initializeForCurrentDisplay(width, height);
    }

    public static synchronized boolean initialize(long hwnd, int width, int height) {
        if (initialized) {
            return true;
        }
        if (!RemixBridgeNative.isAvailable()) {
            lastError = RemixBridgeNative.loadError();
            report("JNI bridge unavailable: " + lastError);
            return false;
        }
        initialized = RemixBridgeNative.nInitialize(hwnd, width, height);
        if (!initialized) {
            lastError = RemixBridgeNative.nGetLastError();
            report("Renderer initialization failed: " + lastError);
        } else {
            lastError = "";
            report("Renderer initialized for hwnd=0x" + Long.toHexString(hwnd));
        }
        return initialized;
    }

    public static synchronized void shutdown() {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nShutdown();
        initialized = false;
        report("Renderer shutdown complete");
    }

    public static synchronized void resize(int width, int height) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nResize(width, height);
    }

    public static synchronized void updateCamera(CameraPose cameraPose) {
        if (!initialized || cameraPose == null) {
            return;
        }
        RemixBridgeNative.nUpdateCamera(
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
        CameraPose cameraPose = new CameraPose();
        cameraPose.px = px;
        cameraPose.py = py;
        cameraPose.pz = pz;

        float forwardLength = length(fx, fy, fz);
        if (forwardLength <= 0.0f) {
            return;
        }
        cameraPose.fx = fx / forwardLength;
        cameraPose.fy = fy / forwardLength;
        cameraPose.fz = fz / forwardLength;

        float upx = 0.0f;
        float upy = 1.0f;
        float upz = 0.0f;
        if (Math.abs(cameraPose.fy) > 0.99f) {
            upx = 0.0f;
            upy = 0.0f;
            upz = 1.0f;
        }

        float rx = cameraPose.fy * upz - cameraPose.fz * upy;
        float ry = cameraPose.fz * upx - cameraPose.fx * upz;
        float rz = cameraPose.fx * upy - cameraPose.fy * upx;
        float rightLength = length(rx, ry, rz);
        if (rightLength <= 0.0f) {
            return;
        }
        cameraPose.rx = rx / rightLength;
        cameraPose.ry = ry / rightLength;
        cameraPose.rz = rz / rightLength;

        cameraPose.ux = cameraPose.ry * cameraPose.fz - cameraPose.rz * cameraPose.fy;
        cameraPose.uy = cameraPose.rz * cameraPose.fx - cameraPose.rx * cameraPose.fz;
        cameraPose.uz = cameraPose.rx * cameraPose.fy - cameraPose.ry * cameraPose.fx;
        float upLength = length(cameraPose.ux, cameraPose.uy, cameraPose.uz);
        if (upLength <= 0.0f) {
            return;
        }
        cameraPose.ux /= upLength;
        cameraPose.uy /= upLength;
        cameraPose.uz /= upLength;

        cameraPose.fovYDegrees = fovYDegrees;
        cameraPose.aspect = aspect;
        cameraPose.nearPlane = nearPlane;
        cameraPose.farPlane = farPlane;
        updateCamera(cameraPose);
    }

    public static synchronized boolean present() {
        return initialized && RemixBridgeNative.nPresent();
    }

    public static boolean beginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        return initialized
                && RemixBridgeNative.nBeginChunkBuild(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
    }

    public static void captureBlock(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nCaptureBlock(blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    public static void endChunkBuild(boolean emittedGeometry) {
        if (!initialized) {
            return;
        }
        RemixBridgeNative.nEndChunkBuild(emittedGeometry);
    }

    public static synchronized boolean isInitialized() {
        return initialized;
    }

    public static synchronized String lastError() {
        if (!lastError.isEmpty()) {
            return lastError;
        }
        if (!RemixBridgeNative.isAvailable()) {
            return RemixBridgeNative.loadError();
        }
        return RemixBridgeNative.nGetLastError();
    }

    private static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private static void report(String message) {
        if (message == null || message.isEmpty() || message.equals(lastReportedMessage)) {
            return;
        }
        lastReportedMessage = message;
        System.out.println("[mcrtx] " + message);
    }
}