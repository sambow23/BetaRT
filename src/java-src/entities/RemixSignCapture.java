import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixDynamicEntityBridge;
import org.lwjgl.opengl.GL11;

final class RemixSignCapture {
    private static final float FONT_GLYPH_SIZE = 7.99f;
    private static final float FONT_ATLAS_SIZE = 128.0f;
    private static final float FONT_GLYPH_TEXEL_SIZE = 8.0f;
    private static final float SIGN_TEXT_DEPTH_OFFSET = -0.30f;
    private static final String SIGN_TEXT_TEXTURE_PATH = "/mcrtx_alias/sign_text/font/default.png";
    private static final String SIGN_TEXTURE_PATH = "/item/sign.png";
    private static final Map<ps, RemixDynamicModelCapture.CachedMesh> MODEL_MESH_CACHE =
            new IdentityHashMap<ps, RemixDynamicModelCapture.CachedMesh>();

    private static boolean signRenderActive;
    private static boolean movingPistonRenderActive;
    private static volatile boolean signCaptureEnabled = true;
    private static volatile boolean signTextCaptureEnabled = true;
    private static Field modelPartPolygonsField;
    private static Field fontCharacterWidthsField;
    private static int cachedFontTextureId = -1;

    private RemixSignCapture() {
    }

    static void onSignRenderStart(yk sign) {
        if (!canCaptureSigns() || sign == null) {
            return;
        }
        RemixDynamicEntitySession.ensureFrame();
        signRenderActive = true;
        int entityId = RemixDynamicEntitySession.stableTileEntityId(sign.e, sign.f, sign.g, 0x5349474E);
        RemixDynamicEntitySession.beginEntity(entityId, 0, 0, 0.0f, SIGN_TEXTURE_PATH);
    }

    static void onSignRenderEnd() {
        signRenderActive = false;
        RemixLivingEntityCapture.onRenderEnd();
    }

    static void onMovingPistonRenderStart(uk piston) {
        if (!RemixDynamicEntitySession.canCapture() || piston == null) {
            return;
        }
        RemixDynamicEntitySession.ensureFrame();
        movingPistonRenderActive = true;
        int entityId = RemixDynamicEntitySession.stableTileEntityId(
                piston.e, piston.f, piston.g, 0x50495354);
        RemixDynamicEntitySession.beginEntity(
                entityId, 0, 0, 0.0f, RemixHeldItemCapture.TERRAIN_TEXTURE_PATH);
    }

    static void onMovingPistonRenderEnd() {
        movingPistonRenderActive = false;
        RemixLivingEntityCapture.onRenderEnd();
    }

    static boolean shouldSuppressMovingPistonVanillaDraw() {
        return movingPistonRenderActive && RemixDynamicEntitySession.isEntityActive();
    }

    static boolean captureSignModelRender(rf signModel) {
        if (!signRenderActive || !RemixDynamicEntitySession.isEntityActive() || signModel == null) {
            return false;
        }
        RemixDynamicModelCapture.CachedMesh signBoardMesh =
                resolveVisibleCachedModelPartMesh(signModel.a, 0.0625f);
        RemixDynamicModelCapture.CachedMesh signPostMesh =
                resolveVisibleCachedModelPartMesh(signModel.b, 0.0625f);
        return captureCachedSignModel(signBoardMesh, signPostMesh);
    }

    static boolean captureSignTextRender(
            sj fontRenderer, String text, int x, int y, int colorRgba) {
        if (!canCaptureSignText()
                || !signRenderActive
                || !RemixDynamicEntitySession.isEntityActive()
                || fontRenderer == null) {
            return false;
        }
        int[] characterWidths = resolveFontCharacterWidths(fontRenderer);
        if (characterWidths == null || characterWidths.length == 0) {
            return false;
        }
        onSignTextRender(text, x, y, colorRgba, false, characterWidths);
        return true;
    }

    static void onSignTextRender(
            String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        if (!signRenderActive
                || !RemixDynamicEntitySession.isEntityActive()
                || text == null
                || text.isEmpty()
                || characterWidths == null
                || characterWidths.length == 0
                || shadow
                || !GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        try {
            RemixDynamicEntitySession.setEntityTexture(SIGN_TEXT_TEXTURE_PATH);
            int textureId = resolveSignTextTextureId();
            RemixDynamicModelCapture.TextureAlpha alpha =
                    RemixDynamicModelCapture.textureAlpha(textureId);
            boolean canCapturePerPixel = alpha.width == 128
                    && alpha.height == 128
                    && alpha.hasPixels();

            float[] modelView = RemixDynamicModelCapture.captureModelViewMatrix();
            if (modelView == null) {
                return;
            }
            RemixCameraState.PreciseTransform modelToWorld =
                    RemixCameraState.buildModelToWorldTransform(modelView);
            int sanitizedColor = ColorMath.forceOpaqueAlpha(ColorMath.sanitizePackedColor(colorRgba));
            int boneIndex = RemixDynamicEntitySession.allocateBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            RemixDynamicEntitySession.submitBoneTransform(boneIndex, modelToWorld);

            float cursorX = x;
            for (int index = 0; index < text.length(); index++) {
                while (text.length() > index + 1 && text.charAt(index) == '\u00a7') {
                    index += 2;
                    if (index >= text.length()) {
                        return;
                    }
                }

                int glyphIndex = fp.a.indexOf(text.charAt(index));
                if (glyphIndex < 0) {
                    continue;
                }
                int glyphId = glyphIndex + 32;
                if (glyphId < 0 || glyphId >= characterWidths.length) {
                    continue;
                }

                float glyphMinX = cursorX;
                float glyphMaxX = cursorX + FONT_GLYPH_SIZE;
                float glyphMinY = y;
                float glyphMaxY = y + FONT_GLYPH_SIZE;
                float atlasX = (glyphId % 16) * 8.0f;
                float atlasY = (glyphId / 16) * 8.0f;
                float u0 = atlasX / FONT_ATLAS_SIZE;
                float v0 = atlasY / FONT_ATLAS_SIZE;
                float u1 = (atlasX + FONT_GLYPH_SIZE) / FONT_ATLAS_SIZE;
                float v1 = (atlasY + FONT_GLYPH_SIZE) / FONT_ATLAS_SIZE;

                if (canCapturePerPixel) {
                    captureSignGlyph(
                            glyphMinX, glyphMinY, atlasX, atlasY,
                            sanitizedColor, boneIndex, alpha);
                } else {
                    RemixDynamicEntityBridge.captureDynamicEntityQuad(
                            glyphMinX, glyphMaxY, SIGN_TEXT_DEPTH_OFFSET, u0, v1,
                            glyphMaxX, glyphMaxY, SIGN_TEXT_DEPTH_OFFSET, u1, v1,
                            glyphMaxX, glyphMinY, SIGN_TEXT_DEPTH_OFFSET, u1, v0,
                            glyphMinX, glyphMinY, SIGN_TEXT_DEPTH_OFFSET, u0, v0,
                            sanitizedColor, false, boneIndex);
                }
                cursorX += characterWidths[glyphId];
            }
        } catch (RuntimeException exception) {
            RemixDynamicEntitySession.handleFailure(exception);
        }
    }

    static boolean shouldCaptureModelPart(ps modelPart) {
        return modelPart != null && modelPart.h && !modelPart.i;
    }

    static void setSignCaptureEnabled(boolean enabled) {
        signCaptureEnabled = enabled;
        if (enabled || !signRenderActive) {
            return;
        }
        signRenderActive = false;
        RemixDynamicEntitySession.clearEntityState();
    }

    static void setSignTextCaptureEnabled(boolean enabled) {
        signTextCaptureEnabled = enabled;
    }

    static void onFramePresented() {
        signRenderActive = false;
    }

    static void resetActiveCapture() {
        signRenderActive = false;
    }

    private static boolean canCaptureSigns() {
        return signCaptureEnabled && RemixDynamicEntitySession.canCapture();
    }

    private static boolean canCaptureSignText() {
        return signTextCaptureEnabled && canCaptureSigns();
    }

    private static int resolveSignTextTextureId() {
        int boundTextureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        if (boundTextureId > 0) {
            RemixDynamicModelCapture.TextureAlpha alpha =
                    RemixDynamicModelCapture.textureAlpha(boundTextureId);
            if (alpha.width == 128 && alpha.height == 128 && alpha.hasPixels()) {
                cachedFontTextureId = boundTextureId;
            }
        }
        return cachedFontTextureId > 0 ? cachedFontTextureId : boundTextureId;
    }

    private static void captureSignGlyph(
            float glyphMinX,
            float glyphMinY,
            float atlasX,
            float atlasY,
            int colorRgba,
            int boneIndex,
            RemixDynamicModelCapture.TextureAlpha alpha) {
        float pixelSpan = FONT_GLYPH_SIZE / FONT_GLYPH_TEXEL_SIZE;
        int atlasPixelBaseX = Math.round(atlasX);
        int atlasPixelBaseY = Math.round(atlasY);
        for (int pixelY = 0; pixelY < 8; pixelY++) {
            for (int pixelX = 0; pixelX < 8; pixelX++) {
                if (!alpha.isTexturePixelOpaque(
                        atlasPixelBaseX + pixelX, atlasPixelBaseY + pixelY)) {
                    continue;
                }
                float quadMinX = glyphMinX + pixelX * pixelSpan;
                float quadMaxX = quadMinX + pixelSpan;
                float quadMinY = glyphMinY + pixelY * pixelSpan;
                float quadMaxY = quadMinY + pixelSpan;
                float centerU = (atlasPixelBaseX + pixelX + 0.5f) / FONT_ATLAS_SIZE;
                float centerV = (atlasPixelBaseY + pixelY + 0.5f) / FONT_ATLAS_SIZE;
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        quadMinX, quadMaxY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        quadMaxX, quadMaxY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        quadMaxX, quadMinY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        quadMinX, quadMinY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        colorRgba, false, boneIndex);
            }
        }
    }

    private static boolean captureCachedSignModel(
            RemixDynamicModelCapture.CachedMesh firstMesh,
            RemixDynamicModelCapture.CachedMesh secondMesh) {
        if (firstMesh == null && secondMesh == null) {
            return false;
        }
        RemixDynamicEntitySession.setEntityTexture(SIGN_TEXTURE_PATH);

        try {
            long renderStartNanos = System.nanoTime();
            float[] modelView = RemixDynamicModelCapture.captureModelViewMatrix();
            if (modelView == null) {
                return false;
            }
            float[] color = RemixDynamicModelCapture.captureCurrentColor();
            if (color == null) {
                return false;
            }
            long stateReadEndNanos = System.nanoTime();

            RemixCameraState.PreciseTransform modelToWorld =
                    RemixCameraState.buildModelToWorldTransform(modelView);
            float[] capturedColor = RemixDynamicModelCapture.sanitizeModelPartColor(
                    RemixDynamicEntitySession.activeEntityTexture(),
                    color[0], color[1], color[2], color[3]);
            capturedColor = ColorMath.applyHurtIndicator(
                    capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3],
                    RemixDynamicEntitySession.activeHurtStage());
            capturedColor = ColorMath.applyCreeperFuseIndicator(
                    capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3],
                    RemixDynamicEntitySession.activeCreeperFuseProgress());
            int colorRgba = ColorMath.packColor(
                    capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3]);
            int boneIndex = RemixDynamicEntitySession.allocateBoneIndex();
            if (boneIndex < 0) {
                return false;
            }
            RemixDynamicEntitySession.submitBoneTransform(boneIndex, modelToWorld);
            long setupEndNanos = System.nanoTime();

            boolean emitted = false;
            emitted |= RemixDynamicModelCapture.emitCachedMesh(firstMesh, colorRgba, boneIndex);
            emitted |= RemixDynamicModelCapture.emitCachedMesh(secondMesh, colorRgba, boneIndex);
            long quadEmitEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.captureSignModelRender.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.captureSignModelRender.setupTransform",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.captureSignModelRender.emitQuads",
                    quadEmitEndNanos - setupEndNanos);
            return emitted;
        } catch (RuntimeException exception) {
            RemixDynamicEntitySession.handleFailure(exception);
            return false;
        }
    }

    private static RemixDynamicModelCapture.CachedMesh resolveVisibleCachedModelPartMesh(
            ps modelPart, float scale) {
        if (!shouldCaptureModelPart(modelPart)) {
            return null;
        }
        RemixDynamicModelCapture.CachedMesh cached = MODEL_MESH_CACHE.get(modelPart);
        if (cached != null) {
            return cached;
        }
        RemixDynamicModelCapture.CachedMesh built =
                RemixDynamicModelCapture.buildCachedMesh(resolveModelPartPolygons(modelPart), scale);
        if (built != null) {
            MODEL_MESH_CACHE.put(modelPart, built);
        }
        return built;
    }

    private static tz[] resolveModelPartPolygons(ps modelPart) {
        if (modelPart == null) {
            return null;
        }
        try {
            Field polygonsField = modelPartPolygonsField;
            if (polygonsField == null || !polygonsField.getDeclaringClass().isInstance(modelPart)) {
                polygonsField = ps.class.getDeclaredField("k");
                polygonsField.setAccessible(true);
                modelPartPolygonsField = polygonsField;
            }
            return (tz[]) polygonsField.get(modelPart);
        } catch (ReflectiveOperationException exception) {
            RemixDynamicEntitySession.handleFailure(
                    new RuntimeException("Failed to access sign model polygons", exception));
            return null;
        }
    }

    private static int[] resolveFontCharacterWidths(sj fontRenderer) {
        try {
            Field widthsField = fontCharacterWidthsField;
            if (widthsField == null || !widthsField.getDeclaringClass().isInstance(fontRenderer)) {
                widthsField = sj.class.getDeclaredField("b");
                widthsField.setAccessible(true);
                fontCharacterWidthsField = widthsField;
            }
            return (int[]) widthsField.get(fontRenderer);
        } catch (ReflectiveOperationException exception) {
            RemixDynamicEntitySession.handleFailure(
                    new RuntimeException("Failed to access sign font widths", exception));
            return null;
        }
    }
}
