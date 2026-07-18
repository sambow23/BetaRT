/** Hook-facing facade for UI, name-tag, font, and model capture. */
public final class RemixUiCapture {
    private RemixUiCapture() {
    }

    public static void begin(int width, int height) {
        if (RemixUiCaptureSession.begin(width, height)) {
            RemixNameTagCapture.reset();
        }
    }

    public static void clear(int width, int height) {
        if (RemixUiCaptureSession.clear(width, height)) {
            RemixNameTagCapture.reset();
        }
    }

    public static void beginNameTagCapture(int width, int height) {
        RemixUiCaptureSession.beginNameTagCapture(width, height);
        if (RemixUiCaptureSession.isNameTagCaptureActive()) {
            RemixNameTagCapture.begin();
        }
    }

    public static void endNameTagCapture() {
        RemixUiCaptureSession.endNameTagCapture();
    }

    public static boolean isCapturingUiOrNameTags() {
        return RemixUiCaptureSession.isCapturingUiOrNameTags();
    }

    public static boolean hasPendingNameTagDraws() {
        return RemixUiCaptureSession.hasPendingNameTagDraws();
    }

    public static void onTessellatorDraw(
            int[] rawVertexData,
            int rawVertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        RemixUiModelCapture.onTessellatorDraw(
                rawVertexData,
                rawVertexCount,
                drawMode,
                hasTexture,
                hasColor);
    }

    public static void onFontString(
            String text,
            int x,
            int y,
            int color,
            boolean shadow,
            int[] charWidths,
            int fontTextureGlId) {
        RemixUiFontCapture.capture(text, x, y, color, shadow, charWidths, fontTextureGlId);
    }

    public static boolean isActive() {
        return RemixUiCaptureSession.isActive();
    }

    public static boolean onModelPart(tz[] polygons, float scale) {
        return RemixUiModelCapture.onModelPart(polygons, scale);
    }

    public static void end() {
        RemixUiCaptureSession.end();
    }

    public static void reset() {
        RemixUiCaptureSession.reset();
        RemixNameTagCapture.reset();
        RemixUiTextureRegistry.reset();
    }
}
