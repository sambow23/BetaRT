package mcrtx.bridge;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public final class RemixBridgeNative {
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

    public static native boolean nBeginChunkBuild(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
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
            int blockColorRgb);

    public static native void nEndChunkBuild(boolean emittedGeometry);

    public static native boolean nPresent();

    public static native String nGetLastError();
}
