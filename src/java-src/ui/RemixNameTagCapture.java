import mcrtx.bridge.McrtxGameplaySettings;

final class RemixNameTagCapture {
    private static final int UI_DRAW_FLAG_DEPTH_TEST = 0x1;
    private static final float NAME_TAG_DEPTH_BIAS = 0.00005f;
    private static final float NAME_TAG_FADE_START_DISTANCE = 32.0f;
    private static final float NAME_TAG_FADE_END_DISTANCE = 64.0f;

    private static float anchorDepth = 1.0f;
    private static float alphaScale = 1.0f;

    private RemixNameTagCapture() {
    }

    static void begin() {
        anchorDepth = 1.0f;
        alphaScale = 1.0f;
    }

    static void reset() {
        begin();
    }

    static float[] adjustProjection(float[] projection) {
        if (!RemixUiCaptureSession.isNameTagCaptureActive()
                || projection == null
                || RemixUiCaptureSession.displayWidth() <= 0
                || RemixUiCaptureSession.displayHeight() <= 0) {
            return projection;
        }

        float fovYDegrees = McrtxGameplaySettings.getGameplayFovDegrees();
        if (fovYDegrees < 1.0f) {
            fovYDegrees = 1.0f;
        } else if (fovYDegrees > 179.0f) {
            fovYDegrees = 179.0f;
        }
        float yScale = 1.0f / (float) Math.tan(Math.toRadians(fovYDegrees) * 0.5);
        float aspect = (float) RemixUiCaptureSession.displayWidth()
                / (float) RemixUiCaptureSession.displayHeight();
        float[] adjusted = projection.clone();
        adjusted[0] = yScale / aspect;
        adjusted[5] = yScale;
        return adjusted;
    }

    static boolean acceptAnchor(float[] mvp, float[] modelView) {
        RemixUiProjection.ProjectedPoint projected =
                RemixUiProjection.projectToScreenPixels(mvp, 0.0f, 0.0f, 0.0f);
        if (!projected.projectable || !projected.insideClip) {
            discard();
            return false;
        }
        anchorDepth = projected.depth;
        alphaScale = computeNameTagAlphaScale(modelView);
        if (alphaScale <= 0.001f) {
            discard();
            return false;
        }
        return true;
    }

    static boolean acceptProjectedVertex(RemixUiProjection.ProjectedPoint point) {
        if (point.projectable) {
            return true;
        }
        discard();
        return false;
    }

    static int commandFlags() {
        return RemixUiCaptureSession.isNameTagCaptureActive() ? UI_DRAW_FLAG_DEPTH_TEST : 0;
    }

    static float depthForLayer(boolean textLayer) {
        float depth = anchorDepth + (textLayer ? -NAME_TAG_DEPTH_BIAS : NAME_TAG_DEPTH_BIAS);
        if (depth < 0.0f) {
            return 0.0f;
        }
        if (depth > 1.0f) {
            return 1.0f;
        }
        return depth;
    }

    static int applyFade(int color) {
        if (!RemixUiCaptureSession.isNameTagCaptureActive() || alphaScale >= 0.999f) {
            return color;
        }
        int alpha = (color >>> 24) & 0xFF;
        int fadedAlpha = Math.round(alpha * alphaScale);
        if (fadedAlpha < 0) {
            fadedAlpha = 0;
        } else if (fadedAlpha > 255) {
            fadedAlpha = 255;
        }
        return (color & 0x00FFFFFF) | (fadedAlpha << 24);
    }

    static void discard() {
        RemixUiCaptureSession.discardActiveNameTagCapture();
    }

    private static float computeNameTagAlphaScale(float[] modelView) {
        if (modelView == null) {
            return 1.0f;
        }
        float eyeX = modelView[12];
        float eyeY = modelView[13];
        float eyeZ = modelView[14];
        float distance = (float) Math.sqrt(eyeX * eyeX + eyeY * eyeY + eyeZ * eyeZ);
        if (distance <= NAME_TAG_FADE_START_DISTANCE) {
            return 1.0f;
        }
        if (distance >= NAME_TAG_FADE_END_DISTANCE) {
            return 0.0f;
        }
        float t = (distance - NAME_TAG_FADE_START_DISTANCE)
                / (NAME_TAG_FADE_END_DISTANCE - NAME_TAG_FADE_START_DISTANCE);
        return 1.0f - t;
    }
}
