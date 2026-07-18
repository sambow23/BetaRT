package mcrtx.bridge;

import java.nio.ByteBuffer;

public final class RemixUiBridge {
    private RemixUiBridge() {
    }

    public static synchronized boolean drawScreenOverlay(
            ByteBuffer pixelData,
            int width,
            int height,
            int format,
            float opacity) {
        if (!RemixLifecycleBridge.isInitialized() || pixelData == null) {
            return false;
        }
        return nDrawScreenOverlay(pixelData, width, height, format, opacity);
    }

    public static synchronized boolean clearScreenOverlay() {
        if (!RemixLifecycleBridge.isInitialized()) {
            return true;
        }
        return nClearScreenOverlay();
    }

    public static synchronized boolean registerUiTexture(
            ByteBuffer pixelData,
            long id,
            int width,
            int height,
            int format) {
        if (!RemixLifecycleBridge.isInitialized() || pixelData == null) {
            return false;
        }
        return nRegisterUiTexture(pixelData, id, width, height, format);
    }

    public static synchronized boolean freeUiTexture(long id) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return true;
        }
        return nFreeUiTexture(id);
    }

    public static synchronized boolean submitUiDrawList(
            float[] vertexXYZUV,
            int[] vertexColor,
            int vertexCount,
            long[] cmdTextureIds,
            int[] cmdQuadCounts,
            int[] cmdFlags,
            int cmdCount,
            int displayWidth,
            int displayHeight) {
        if (!RemixLifecycleBridge.isInitialized()) {
            return false;
        }
        return nSubmitUiDrawList(
                vertexXYZUV, vertexColor, vertexCount,
                cmdTextureIds, cmdQuadCounts, cmdFlags, cmdCount,
                displayWidth, displayHeight);
    }

    private static native boolean nDrawScreenOverlay(
            ByteBuffer pixelData,
            int width,
            int height,
            int format,
            float opacity);
    private static native boolean nClearScreenOverlay();
    private static native boolean nRegisterUiTexture(
            ByteBuffer pixelData,
            long id,
            int width,
            int height,
            int format);
    private static native boolean nFreeUiTexture(long id);
    private static native boolean nSubmitUiDrawList(
            float[] vertexXYZUV,
            int[] vertexColor,
            int vertexCount,
            long[] cmdTextureIds,
            int[] cmdQuadCounts,
            int[] cmdFlags,
            int cmdCount,
            int displayWidth,
            int displayHeight);
}
