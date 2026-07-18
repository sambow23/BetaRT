import mcrtx.bridge.MatrixMath;

final class RemixUiFontCapture {
    private static final float FONT_GLYPH_SIZE = 7.99f;
    private static final float FONT_ATLAS_SIZE = 128.0f;
    private static final String FONT_COLOR_CODES = "0123456789abcdef";
    private static final char FONT_COLOR_PREFIX = '\u00a7';

    private RemixUiFontCapture() {
    }

    static void capture(
            String text,
            int x,
            int y,
            int color,
            boolean shadow,
            int[] charWidths,
            int fontTextureGlId) {
        if (!RemixUiCaptureSession.isCapturingUiOrNameTags()
                || text == null
                || text.isEmpty()
                || charWidths == null) {
            return;
        }
        if (RemixUiCaptureSession.isNameTagCaptureDiscarded()) {
            return;
        }
        if (fontTextureGlId <= 0 || !RemixUiTextureRegistry.ensureUploadedById(fontTextureGlId)) {
            if (RemixUiCaptureSession.isNameTagCaptureActive()) {
                RemixNameTagCapture.discard();
            }
            return;
        }
        long textureId = fontTextureGlId & 0xFFFFFFFFL;

        float[] modelView = RemixUiProjection.captureModelView();
        float[] projection = RemixUiProjection.captureProjection();
        if (modelView == null || projection == null) {
            if (RemixUiCaptureSession.isNameTagCaptureActive()) {
                RemixNameTagCapture.discard();
            }
            return;
        }
        projection = RemixNameTagCapture.adjustProjection(projection);
        float[] mvp = MatrixMath.multiplyColumnMajor(projection, modelView);
        if (RemixUiCaptureSession.isNameTagCaptureActive()
                && !RemixNameTagCapture.acceptAnchor(mvp, modelView)) {
            return;
        }

        int currentColor = darkenAndPack(color, shadow);
        int currentAlpha = currentColor >>> 24;
        RemixUiDrawList drawList = RemixUiCaptureSession.drawList();
        drawList.ensureVertexCapacity(drawList.vertexCount() + text.length() * 4);
        int glyphQuads = 0;
        float cursorX = x;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            while (ch == FONT_COLOR_PREFIX && i + 1 < text.length()) {
                int code = FONT_COLOR_CODES.indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (code < 0) {
                    code = 15;
                }
                currentColor = paletteColor(code, shadow, currentAlpha);
                i += 2;
                if (i >= text.length()) {
                    break;
                }
                ch = text.charAt(i);
            }
            if (i >= text.length()) {
                break;
            }

            int glyphIndex = fp.a.indexOf(ch);
            if (glyphIndex < 0) {
                continue;
            }
            int glyphId = glyphIndex + 32;
            if (glyphId < 0 || glyphId >= charWidths.length) {
                continue;
            }

            float atlasX = (glyphId % 16) * 8.0f;
            float atlasY = (glyphId / 16) * 8.0f;
            float u0 = atlasX / FONT_ATLAS_SIZE;
            float v0 = atlasY / FONT_ATLAS_SIZE;
            float u1 = (atlasX + FONT_GLYPH_SIZE) / FONT_ATLAS_SIZE;
            float v1 = (atlasY + FONT_GLYPH_SIZE) / FONT_ATLAS_SIZE;
            float x0 = cursorX;
            float x1 = cursorX + FONT_GLYPH_SIZE;
            float y0 = y;
            float y1 = y + FONT_GLYPH_SIZE;

            if (!appendGlyphVertex(drawList, mvp, x0, y1, u0, v1, currentColor)
                    || !appendGlyphVertex(drawList, mvp, x1, y1, u1, v1, currentColor)
                    || !appendGlyphVertex(drawList, mvp, x1, y0, u1, v0, currentColor)
                    || !appendGlyphVertex(drawList, mvp, x0, y0, u0, v0, currentColor)) {
                RemixNameTagCapture.discard();
                return;
            }
            glyphQuads++;
            cursorX += charWidths[glyphId];
        }

        drawList.appendCommand(textureId, glyphQuads, RemixNameTagCapture.commandFlags());
    }

    private static boolean appendGlyphVertex(
            RemixUiDrawList drawList,
            float[] mvp,
            float x,
            float y,
            float u,
            float v,
            int color) {
        RemixUiProjection.ProjectedPoint projected =
                RemixUiProjection.projectToScreenPixels(mvp, x, y, 0.0f);
        if (!projected.projectable) {
            return false;
        }
        drawList.appendVertex(
                projected.x,
                projected.y,
                RemixUiCaptureSession.isNameTagCaptureActive()
                        ? RemixNameTagCapture.depthForLayer(true)
                        : 0.0f,
                u,
                v,
                RemixUiCaptureSession.isNameTagCaptureActive()
                        ? RemixNameTagCapture.applyFade(color)
                        : color);
        return true;
    }

    private static int darkenAndPack(int color, boolean shadow) {
        if (shadow) {
            int alphaBits = color & 0xFF000000;
            color = (color & 0xFCFCFC) >> 2;
            color += alphaBits;
        }
        int a = (color >> 24) & 0xFF;
        if (a == 0) {
            a = 255;
        }
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    private static int paletteColor(int code, boolean shadow, int alpha) {
        int base = (code >> 3 & 1) * 85;
        int r = (code >> 2 & 1) * 170 + base;
        int g = (code >> 1 & 1) * 170 + base;
        int b = (code & 1) * 170 + base;
        if (code == 6) {
            r += 85;
        }
        if (shadow) {
            r /= 4;
            g /= 4;
            b /= 4;
        }
        return r | (g << 8) | (b << 16) | (alpha << 24);
    }
}
