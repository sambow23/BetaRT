package mcrtx.bridge;

import java.awt.Canvas;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;

public final class RemixBridgeNative {
    public static final int REMIX_UI_STATE_NONE = 0;
    public static final int REMIX_UI_STATE_BASIC = 1;
    public static final int REMIX_UI_STATE_ADVANCED = 2;
    public static final int SCREEN_OVERLAY_FORMAT_RGBA8 = 37;
    public static final int SCREEN_OVERLAY_FORMAT_BGRA8 = 44;

    private static final boolean AVAILABLE;
    private static final String LOAD_ERROR;

    static {
        boolean available = false;
        String loadError = "";
        try {
            loadNativeLibrary();
            available = true;
        } catch (Throwable throwable) {
            loadError = throwable.toString();
        }
        AVAILABLE = available;
        LOAD_ERROR = loadError;
    }

    private RemixBridgeNative() {
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    public static String loadError() {
        return LOAD_ERROR;
    }

    private static void loadNativeLibrary() {
        StringBuilder attempts = new StringBuilder();

        String explicitPath = System.getenv("MCRTX_JNI_PATH");
        if (tryLoadAbsolute(explicitPath, attempts)) {
            return;
        }

        String jarAdjacentPath = resolveJarAdjacentDllPath();
        if (tryLoadAbsolute(jarAdjacentPath, attempts)) {
            return;
        }

        String workingDirectoryPath = new File(System.getProperty("user.dir"), "mcrtx_jni.dll").getAbsolutePath();
        if (tryLoadAbsolute(workingDirectoryPath, attempts)) {
            return;
        }

        try {
            System.loadLibrary("mcrtx_jni");
            return;
        } catch (Throwable throwable) {
            if (attempts.length() != 0) {
                attempts.append(" | ");
            }
            attempts.append("System.loadLibrary failed: ").append(throwable.toString());
        }

        throw new UnsatisfiedLinkError(attempts.toString());
    }

    private static boolean tryLoadAbsolute(String absolutePath, StringBuilder attempts) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            return false;
        }

        File file = new File(absolutePath);
        if (!file.isFile()) {
            if (attempts.length() != 0) {
                attempts.append(" | ");
            }
            attempts.append("Missing ").append(file.getAbsolutePath());
            return false;
        }

        try {
            System.load(file.getAbsolutePath());
            return true;
        } catch (Throwable throwable) {
            if (attempts.length() != 0) {
                attempts.append(" | ");
            }
            attempts.append(file.getAbsolutePath()).append(": ").append(throwable.toString());
            return false;
        }
    }

    private static String resolveJarAdjacentDllPath() {
        try {
            URL location = RemixBridgeNative.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return "";
            }

            File source = new File(location.toURI());
            File directory = source.isDirectory() ? source : source.getParentFile();
            if (directory == null) {
                return "";
            }

            return new File(directory, "mcrtx_jni.dll").getAbsolutePath();
        } catch (URISyntaxException exception) {
            return "";
        }
    }

    public static native boolean nInitialize(long hwnd, int width, int height);

    public static native long nResolveAwtWindowHandle(Canvas canvas);

    public static native boolean nEmbedCompatibilityWindow(long childHwnd, long parentHwnd, int width, int height);

    public static native boolean nFocusWindow(long hwnd);

    public static native boolean nIsEmbeddedWindowActive(long childHwnd, long parentHwnd);

    public static native void nShutdown();

    public static native void nResize(int width, int height);

    public static native void nUpdateCamera(
            float px, float py, float pz,
            float fx, float fy, float fz,
            float ux, float uy, float uz,
            float rx, float ry, float rz,
            float fovYDegrees,
            float aspect,
            float nearPlane,
            float farPlane);

        public static native void nUpdateCloudLayer(
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

        public static native void nUpdateAtmosphereState(
            float celestialAngle,
            boolean forceDarkAtmosphere);

        public static native void nUpdateFogState(
            int fogMode,
            float colorR,
            float colorG,
            float colorB,
            float fogScale,
            float fogEnd,
            float fogDensity);

        public static native void nClearCloudLayer();

            public static native void nBeginDynamicEntityFrame();

            public static native void nBeginDynamicEntity(int entityId);

            public static native void nSetDynamicEntityTexture(String texturePath);

            public static native void nSetFirstPersonHeldItem(int itemId);

            public static native void nSetDynamicEntityBoneTransform(
                int boneIndex,
                float m00, float m01, float m02, float m03,
                float m10, float m11, float m12, float m13,
                float m20, float m21, float m22, float m23);

            public static native void nCaptureDynamicEntityQuad(
                float x0, float y0, float z0, float u0, float v0,
                float x1, float y1, float z1, float u1, float v1,
                float x2, float y2, float z2, float u2, float v2,
                float x3, float y3, float z3, float u3, float v3,
                int colorRgba,
                int boneIndex);

            public static native void nEndDynamicEntity();

            public static native void nBeginDestroyOverlayFrame();

            public static native void nCaptureDestroyOverlay(
                int blockX,
                int blockY,
                int blockZ,
                int blockId,
                int blockMetadata,
                int renderType,
                int destroyStage);

            public static native void nBeginParticleFrame();

            public static native void nCaptureParticleQuad(
                float x0, float y0, float z0, float u0, float v0,
                float x1, float y1, float z1, float u1, float v1,
                float x2, float y2, float z2, float u2, float v2,
                float x3, float y3, float z3, float u3, float v3,
                int colorRgba,
                int textureKind);

            public static native void nClearWorldScene();

    public static native void nUnloadChunkSection(int originX, int originY, int originZ);

            public static native void nSetChunkSectionHidden(int originX, int originY, int originZ, boolean hidden);

    public static native boolean nBeginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int dirtyMinX,
            int dirtyMinY,
            int dirtyMinZ,
            int dirtyMaxX,
            int dirtyMaxY,
            int dirtyMaxZ,
            int renderPass);

    public static native void nCaptureBlock(
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType,
            int texture0,
            int texture1,
            int texture2,
            int texture3,
            int texture4,
            int texture5,
            float boundsMinX,
            float boundsMinY,
            float boundsMinZ,
            float boundsMaxX,
            float boundsMaxY,
            float boundsMaxZ,
            int blockColorRgb,
            int liquidVisibilityMask,
            float liquidHeight0,
            float liquidHeight1,
            float liquidHeight2,
            float liquidHeight3,
            float liquidFlowAngle);

    public static native void nEndChunkBuild(boolean emittedGeometry, boolean deferNeighborRefresh, boolean allowNeighborRefresh);

    public static native void nFlushChunkNeighborRefreshes();

    public static native boolean nDrawScreenOverlay(
            ByteBuffer pixelData,
            int width,
            int height,
            int format,
            float opacity);

    public static native boolean nClearScreenOverlay();

    public static native void nSetScreenTint(float r, float g, float b, float a);

    public static native int nGetUiState();

    public static native boolean nSetUiState(int state);

    public static native boolean nHasWindowFocus();

    public static native boolean nIsVirtualKeyDown(int virtualKey);

    public static native boolean nPollNativeMouseState(int[] stateOut);

    public static native boolean nSetNativeMouseGrabbed(boolean grabbed);

    public static native boolean nSetNativeCursorPosition(int x, int y);

    public static native boolean nPresent();

    public static native String nGetLastError();

    public static native void nRecordJavaSample(int side, String site, long nanoseconds);

    public static native void nRecordJavaCount(int side, String site, long count);

    public static native void nFlushJavaFrame();

    public static native int nRegisterPerfSite(int side, String site);

    public static native void nRecordJavaSampleBatch(int[] siteIds, long[] nanos, int count);
}
