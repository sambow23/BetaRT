import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixDynamicEntityBridge;
import mcrtx.lwjglshim.OpenGlCompat;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

final class RemixDynamicModelCapture {
    private static final int QUAD_VERTEX_COUNT = 4;
    private static final int QUAD_STRIDE_FLOATS = 20;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final String SHEEP_FUR_TEXTURE_PATH = "/mob/sheep_fur.png";
    private static final String SPIDER_BODY_TEXTURE_PATH = "/mob/spider.png";
    private static final String SPIDER_EYES_TEXTURE_PATH = "/mob/spider_eyes.png";
    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final Map<Integer, TextureAlpha> TEXTURE_ALPHA_CACHE = new HashMap<Integer, TextureAlpha>();

    private static ByteBuffer textureReadBuffer;
    private static float[] modelPartQuadScratch = new float[QUAD_STRIDE_FLOATS * 16];
    private static final float[] tessellatorQuadScratch = new float[QUAD_STRIDE_FLOATS];

    static final class CachedMesh {
        final float[] quadData;
        final int quadCount;

        CachedMesh(float[] quadData, int quadCount) {
            this.quadData = quadData;
            this.quadCount = quadCount;
        }
    }

    static final class TextureAlpha {
        final boolean[] pixels;
        final int width;
        final int height;

        TextureAlpha(boolean[] pixels, int width, int height) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
        }

        boolean hasPixels() {
            return pixels.length != 0;
        }

        boolean isTexturePixelOpaque(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return false;
            }
            return pixels.length == 0 || pixels[y * width + x];
        }

        boolean isLogicalPixelOpaque(int x, int y) {
            if (x < 0 || x >= 256 || y < 0 || y >= 256) {
                return false;
            }
            if (pixels.length == 0) {
                return true;
            }
            int pixelX = (x * width) / 256;
            int pixelY = (y * height) / 256;
            return pixels[pixelY * width + pixelX];
        }
    }

    private RemixDynamicModelCapture() {
    }

    static float[] captureModelViewMatrix() {
        MODEL_VIEW_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER)) {
            return null;
        }
        float[] modelView = new float[16];
        MODEL_VIEW_BUFFER.get(modelView);
        return modelView;
    }

    static float[] captureCurrentColor() {
        COLOR_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_CURRENT_COLOR, COLOR_BUFFER)) {
            return null;
        }
        float[] currentColor = new float[4];
        COLOR_BUFFER.get(currentColor);
        return currentColor;
    }

    static TextureAlpha textureAlpha(int textureId) {
        TextureAlpha cached = TEXTURE_ALPHA_CACHE.get(Integer.valueOf(textureId));
        if (cached != null) {
            return cached;
        }

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (width <= 0 || height <= 0) {
            TextureAlpha empty = new TextureAlpha(new boolean[0], 0, 0);
            TEXTURE_ALPHA_CACHE.put(Integer.valueOf(textureId), empty);
            return empty;
        }

        int capacity = width * height * 4;
        if (textureReadBuffer == null || textureReadBuffer.capacity() < capacity) {
            textureReadBuffer = BufferUtils.createByteBuffer(capacity);
        } else {
            textureReadBuffer.clear();
        }

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureReadBuffer);
        boolean[] pixels = new boolean[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (textureReadBuffer.get(i * 4 + 3) & 0xFF) > 128;
        }

        TextureAlpha alpha = new TextureAlpha(pixels, width, height);
        TEXTURE_ALPHA_CACHE.put(Integer.valueOf(textureId), alpha);
        return alpha;
    }

    static boolean isRenderableQuad(tz polygon) {
        if (polygon == null || polygon.a == null || polygon.a.length != QUAD_VERTEX_COUNT) {
            return false;
        }
        for (int vertexIndex = 0; vertexIndex < QUAD_VERTEX_COUNT; vertexIndex++) {
            ib vertex = polygon.a[vertexIndex];
            if (vertex == null || vertex.a == null) {
                return false;
            }
        }
        return true;
    }

    static CachedMesh buildCachedMesh(tz[] polygons, float scale) {
        if (polygons == null || polygons.length == 0) {
            return null;
        }

        int quadCount = 0;
        for (tz polygon : polygons) {
            if (isRenderableQuad(polygon)) {
                quadCount += 1;
            }
        }
        if (quadCount == 0) {
            return null;
        }

        float[] quadData = new float[quadCount * QUAD_STRIDE_FLOATS];
        int quadWriteIndex = 0;
        for (tz polygon : polygons) {
            if (!isRenderableQuad(polygon)) {
                continue;
            }
            int base = quadWriteIndex * QUAD_STRIDE_FLOATS;
            for (int vertexIndex = 0; vertexIndex < QUAD_VERTEX_COUNT; vertexIndex++) {
                ib vertex = polygon.a[vertexIndex];
                int vertexBase = base + vertexIndex * 5;
                quadData[vertexBase] = (float) vertex.a.a * scale;
                quadData[vertexBase + 1] = (float) vertex.a.b * scale;
                quadData[vertexBase + 2] = (float) vertex.a.c * scale;
                quadData[vertexBase + 3] = vertex.b;
                quadData[vertexBase + 4] = vertex.c;
            }
            quadWriteIndex += 1;
        }
        return new CachedMesh(quadData, quadCount);
    }

    static boolean emitCachedMesh(CachedMesh mesh, int colorRgba, int boneIndex) {
        if (mesh == null || mesh.quadCount == 0) {
            return false;
        }
        RemixDynamicEntityBridge.captureDynamicEntityQuadBatch(mesh.quadData, mesh.quadCount, colorRgba, boneIndex);
        return true;
    }

    static float[] sanitizeModelPartColor(String texturePath, float red, float green, float blue, float alpha) {
        if (SHEEP_FUR_TEXTURE_PATH.equals(texturePath)) {
            return new float[] { red, green, blue, alpha };
        }
        float capturedAlpha = isSpiderTexture(texturePath)
                ? 1.0f
                : alpha;
        return ColorMath.sanitizeDynamicEntityColor(red, green, blue, capturedAlpha);
    }

    private static boolean isSpiderTexture(String texturePath) {
        return SPIDER_BODY_TEXTURE_PATH.equals(texturePath)
                || SPIDER_EYES_TEXTURE_PATH.equals(texturePath);
    }

    static boolean captureModelPart(tz[] polygons, float scale) {
        String activeTexture = RemixDynamicEntitySession.activeCaptureTexture();
        if (activeTexture.isEmpty() || polygons == null || polygons.length == 0) {
            return false;
        }
        if (SPIDER_EYES_TEXTURE_PATH.equals(activeTexture)) {
            return true;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return false;
        }

        try {
            long renderStartNanos = System.nanoTime();
            float[] modelView = captureModelViewMatrix();
            if (modelView == null) {
                return false;
            }
            float[] color = captureCurrentColor();
            if (color == null) {
                return false;
            }
            long stateReadEndNanos = System.nanoTime();

            RemixCameraState.PreciseTransform modelToWorld =
                    RemixFirstPersonCapture.modelToWorldTransform(modelView);
            boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
            boolean capturedBlendEnabled = blendEnabled && !isSpiderTexture(activeTexture);
            float[] capturedColor = sanitizeModelPartColor(
                    activeTexture, color[0], color[1], color[2], color[3]);
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

            int quadCount = packModelPartQuads(polygons, scale);
            if (quadCount > 0) {
                RemixDynamicEntityBridge.captureDynamicEntityQuadBatch(
                        modelPartQuadScratch, quadCount, colorRgba, capturedBlendEnabled, boneIndex);
            }
            long quadEmitEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onModelPartRender.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onModelPartRender.setupTransform",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onModelPartRender.emitQuads",
                    quadEmitEndNanos - setupEndNanos);
            return quadCount > 0;
        } catch (RuntimeException exception) {
            RemixDynamicEntitySession.handleFailure(exception);
            return false;
        }
    }

    private static int packModelPartQuads(tz[] polygons, float scale) {
        int requiredFloats = polygons.length * QUAD_STRIDE_FLOATS;
        if (modelPartQuadScratch.length < requiredFloats) {
            modelPartQuadScratch = new float[requiredFloats];
        }

        int quadCount = 0;
        for (tz polygon : polygons) {
            if (!isRenderableQuad(polygon)) {
                continue;
            }
            int base = quadCount * QUAD_STRIDE_FLOATS;
            for (int vertexIndex = 0; vertexIndex < QUAD_VERTEX_COUNT; vertexIndex++) {
                ib vertex = polygon.a[vertexIndex];
                int vertexBase = base + vertexIndex * 5;
                modelPartQuadScratch[vertexBase] = (float) vertex.a.a * scale;
                modelPartQuadScratch[vertexBase + 1] = (float) vertex.a.b * scale;
                modelPartQuadScratch[vertexBase + 2] = (float) vertex.a.c * scale;
                modelPartQuadScratch[vertexBase + 3] = vertex.b;
                modelPartQuadScratch[vertexBase + 4] = vertex.c;
            }
            quadCount += 1;
        }
        return quadCount;
    }

    static void captureTessellatorDraw(int[] rawVertexData, int vertexCount, int drawMode,
            boolean hasTexture, boolean hasColor) {
        if (RemixFirstPersonCapture.isShadowCaptureActive()) {
            return;
        }
        String activeTexture = RemixDynamicEntitySession.activeCaptureTexture();
        if (activeTexture.isEmpty()) {
            return;
        }
        if (rawVertexData == null || vertexCount < 6 || drawMode != 7 || !hasTexture || vertexCount % 6 != 0) {
            return;
        }

        if (RemixFirstPersonCapture.isActive() && vertexCount == 6) {
            RemixFirstPersonCapture.resetVoxelCapture();
        }
        if (RemixFirstPersonCapture.isActive() && vertexCount == 96) {
            if (!RemixFirstPersonCapture.hasGeneratedVoxels()) {
                try {
                    float[] modelView = captureModelViewMatrix();
                    if (modelView == null) {
                        return;
                    }
                    float[] currentColor = captureCurrentColor();
                    if (currentColor == null) {
                        return;
                    }
                    RemixCameraState.PreciseTransform modelToWorld =
                            RemixCameraState.buildModelToWorldTransform(modelView);
                    int fallbackColorRgba = ColorMath.sanitizePackedColor(ColorMath.packColor(
                            currentColor[0], currentColor[1], currentColor[2], currentColor[3]));
                    int boneIndex = RemixDynamicEntitySession.allocateBoneIndex();
                    if (boneIndex >= 0) {
                        RemixDynamicEntitySession.submitBoneTransform(boneIndex, modelToWorld);
                        generateVoxelMesh(rawVertexData, hasColor, fallbackColorRgba, boneIndex);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                RemixFirstPersonCapture.markVoxelsGenerated();
            }
            return;
        }

        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        try {
            long renderStartNanos = System.nanoTime();
            float[] modelView = captureModelViewMatrix();
            if (modelView == null) {
                return;
            }
            float[] currentColor = captureCurrentColor();
            if (currentColor == null) {
                return;
            }
            long stateReadEndNanos = System.nanoTime();

            RemixCameraState.PreciseTransform modelToWorld =
                    RemixCameraState.buildModelToWorldTransform(modelView);
            int fallbackColorRgba = ColorMath.sanitizePackedColor(ColorMath.packColor(
                    currentColor[0], currentColor[1], currentColor[2], currentColor[3]));
            int boneIndex = RemixDynamicEntitySession.allocateBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            RemixDynamicEntitySession.submitBoneTransform(boneIndex, modelToWorld);
            long setupEndNanos = System.nanoTime();

            for (int vertexIndex = 0; vertexIndex + 5 < vertexCount; vertexIndex += 6) {
                int quadColor = hasColor
                        ? ColorMath.sanitizePackedColor(ColorMath.unpackTessellatorColor(
                                rawVertexData, vertexIndex * 8 + 5))
                        : fallbackColorRgba;
                float[] quad = readTessellatorQuad(rawVertexData, vertexIndex);
                if (RemixEntityFireCapture.isActive()) {
                    for (int corner = 0; corner < 4; corner++) {
                        int base = corner * 5;
                        float[] uv = RemixEntityFireCapture.remapUv(quad[base + 3], quad[base + 4]);
                        quad[base + 3] = uv[0];
                        quad[base + 4] = uv[1];
                    }
                }
                RemixDynamicEntityBridge.captureDynamicEntityQuad(
                        quad[0], quad[1], quad[2], quad[3], quad[4],
                        quad[5], quad[6], quad[7], quad[8], quad[9],
                        quad[10], quad[11], quad[12], quad[13], quad[14],
                        quad[15], quad[16], quad[17], quad[18], quad[19],
                        quadColor, boneIndex);
            }
            long quadEmitEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonTessellatorDraw.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonTessellatorDraw.setupTransform",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonTessellatorDraw.emitQuads",
                    quadEmitEndNanos - setupEndNanos);
        } catch (RuntimeException exception) {
            RemixDynamicEntitySession.handleFailure(exception);
        }
    }

    private static float[] readTessellatorQuad(int[] rawVertexData, int vertexIndex) {
        int[] corners = new int[] { vertexIndex, vertexIndex + 1, vertexIndex + 2, vertexIndex + 5 };
        float[] quad = tessellatorQuadScratch;
        for (int corner = 0; corner < corners.length; corner++) {
            int sourceBase = corners[corner] * 8;
            int targetBase = corner * 5;
            quad[targetBase] = Float.intBitsToFloat(rawVertexData[sourceBase]);
            quad[targetBase + 1] = Float.intBitsToFloat(rawVertexData[sourceBase + 1]);
            quad[targetBase + 2] = Float.intBitsToFloat(rawVertexData[sourceBase + 2]);
            quad[targetBase + 3] = Float.intBitsToFloat(rawVertexData[sourceBase + 3]);
            quad[targetBase + 4] = Float.intBitsToFloat(rawVertexData[sourceBase + 4]);
        }
        return quad;
    }

    private static void generateVoxelMesh(
            int[] rawVertexData, boolean hasColor, int fallbackColorRgba, int boneIndex) {
        int textureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        TextureAlpha alpha = textureAlpha(textureId);
        if (!alpha.hasPixels()) {
            return;
        }

        float u0 = Float.intBitsToFloat(rawVertexData[3]);
        float v2 = Float.intBitsToFloat(rawVertexData[2 * 8 + 4]);
        int logicalXBase = Math.round(u0 * 256.0f) - 15;
        int logicalYBase = Math.round(v2 * 256.0f);
        int quadColor = hasColor
                ? ColorMath.sanitizePackedColor(ColorMath.unpackTessellatorColor(rawVertexData, 5))
                : fallbackColorRgba;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (!alpha.isLogicalPixelOpaque(logicalXBase + x, logicalYBase + y)) {
                    continue;
                }
                boolean leftOpaque = x > 0
                        && alpha.isLogicalPixelOpaque(logicalXBase + x - 1, logicalYBase + y);
                boolean rightOpaque = x < 15
                        && alpha.isLogicalPixelOpaque(logicalXBase + x + 1, logicalYBase + y);
                boolean topOpaque = y > 0
                        && alpha.isLogicalPixelOpaque(logicalXBase + x, logicalYBase + y - 1);
                boolean bottomOpaque = y < 15
                        && alpha.isLogicalPixelOpaque(logicalXBase + x, logicalYBase + y + 1);

                float minX = 1.0f - ((x + 1) / 16.0f);
                float maxX = 1.0f - (x / 16.0f);
                float minY = 1.0f - ((y + 1) / 16.0f);
                float maxY = 1.0f - (y / 16.0f);
                float frontZ = 0.0f;
                float backZ = -0.0625f;
                float centerU = (logicalXBase + x + 0.5f) / 256.0f;
                float centerV = (logicalYBase + y + 0.5f) / 256.0f;
                float leftU = (logicalXBase + x) / 256.0f;
                float rightU = (logicalXBase + x + 1) / 256.0f;
                float topV = (logicalYBase + y) / 256.0f;
                float bottomV = (logicalYBase + y + 1) / 256.0f;

                if (!leftOpaque) {
                    RemixDynamicEntityBridge.captureDynamicEntityQuad(
                            maxX, maxY, backZ, centerU, topV,
                            maxX, maxY, frontZ, centerU, topV,
                            maxX, minY, frontZ, centerU, bottomV,
                            maxX, minY, backZ, centerU, bottomV,
                            quadColor, boneIndex);
                }
                if (!rightOpaque) {
                    RemixDynamicEntityBridge.captureDynamicEntityQuad(
                            minX, minY, backZ, centerU, bottomV,
                            minX, minY, frontZ, centerU, bottomV,
                            minX, maxY, frontZ, centerU, topV,
                            minX, maxY, backZ, centerU, topV,
                            quadColor, boneIndex);
                }
                if (!topOpaque) {
                    RemixDynamicEntityBridge.captureDynamicEntityQuad(
                            minX, maxY, frontZ, rightU, centerV,
                            maxX, maxY, frontZ, leftU, centerV,
                            maxX, maxY, backZ, leftU, centerV,
                            minX, maxY, backZ, rightU, centerV,
                            quadColor, boneIndex);
                }
                if (!bottomOpaque) {
                    RemixDynamicEntityBridge.captureDynamicEntityQuad(
                            maxX, minY, frontZ, leftU, centerV,
                            minX, minY, frontZ, rightU, centerV,
                            minX, minY, backZ, rightU, centerV,
                            maxX, minY, backZ, leftU, centerV,
                            quadColor, boneIndex);
                }
            }
        }
    }
}
