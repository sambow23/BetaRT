import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

import mcrtx.bridge.MatrixMath;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.RemixBridgeNative;
import mcrtx.lwjglshim.OpenGlCompat;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Captures Minecraft's 2D GUI rendering (in-game HUD and GuiScreen menus) from
 * the shared Tessellator and forwards it to the Remix runtime as a native
 * screen-space UI draw list. Replaces the framebuffer-readback overlay path
 * (UiOverlayCapture).
 *
 * <p>The Tessellator is the single 2D draw chokepoint: every GUI quad flows
 * through {@code nw.a()}, which is hooked to call
 * {@link #onTessellatorDraw}. During the UI phase (between {@link #begin} and
 * {@link #end}) each quad is transformed to display-pixel space via the current
 * GL modelview/projection and accumulated; {@link #end} submits the frame.
 */
public final class RemixUiCapture {
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final int GL_PROJECTION_MATRIX = 0x0BA7;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final int GL_TEXTURE_BINDING_2D = 0x8069;

    // Bitmap font layout (FontRenderer sj): a 16x16 grid of 8px cells in a
    // 128px atlas; each glyph quad spans 7.99px so sampling stays inside its
    // cell. Mirrors the per-character display lists sj bakes at startup.
    private static final float FONT_GLYPH_SIZE = 7.99f;
    private static final float FONT_ATLAS_SIZE = 128.0f;
    private static final String FONT_COLOR_CODES = "0123456789abcdef";
    private static final char FONT_COLOR_PREFIX = '\u00a7';

    // REMIXAPI_UI_DRAW_FLAG_DEPTH_TEST: depth-test/write a 3D screen-space draw.
    private static final int UI_DRAW_FLAG_DEPTH_TEST = 0x1;

    // UI 3D depth window. The GUI runs under glOrtho(0,w,h,0,1000,3000) with a
    // glTranslatef(0,0,-2000) base, so item/player geometry sits at eye-space
    // z near -2000 spanning only tens of units. Routing that through the ortho's
    // 2000-unit range collapses every face to ~0.5 depth, so front and back
    // faces no longer separate. Instead map eye-space z through this tight
    // window centred on the GUI plane so the small item span fills [0,1] and the
    // depth test resolves self-occlusion. Near = closest (smallest depth).
    private static final float UI_DEPTH_EYE_NEAR = -1900.0f;
    private static final float UI_DEPTH_EYE_FAR = -2100.0f;

    // A Tessellator batch whose vertices span more than this in eye-space z is
    // treated as 3D geometry (a rotated block item icon) and routed through the
    // depth-tested path. Flat 2D UI quads are coplanar at the GUI plane, so
    // their eye-z range is ~0 and they keep painter ordering.
    private static final float UI_3D_EYE_Z_EPSILON = 1.0f;

    // Standard inventory item lighting, baked per-quad from the face normal
    // (the model vertex stream carries no GL_LIGHTING contribution; vanilla
    // relies on two directional lights set up by RenderHelper). Vanilla sets the
    // light positions under a glRotatef(180, 1,0,0), so the effective eye-space
    // directions are the raw vectors with Y and Z negated. The two visible side
    // faces have mirror-X normals, so their brightness split is driven by the
    // combined light X: a net-negative X makes the left face brighter (the
    // vanilla look), while the average side level is set by the ambient. The
    // lights favour the left and the ambient is lifted slightly so the right
    // face stays readable without matching the left.
        private static final float ITEM_LIGHT_AMBIENT = 0.40f;
    private static final float ITEM_LIGHT_DIFFUSE = 0.6f;
        private static final float[] ITEM_LIGHT0 = normalize3(-0.85f, -1.0f, 0.7f);
        private static final float[] ITEM_LIGHT1 = normalize3(-0.30f, -1.0f, 0.7f);
        // Vanilla block rendering uses coarse per-face weights: top 1.0, one side
        // 0.8, the other 0.6, bottom 0.5. The inventory block renderer exposes
            // axis-aligned model-space face normals. Preserve top/bottom directly from
            // that model-space normal, and only use the transformed X sign to split the
            // two visible side faces after the inventory rotations.
            private static final float BLOCK_FACE_LIGHT_TOP = 1.0f;
            private static final float BLOCK_FACE_LIGHT_LEFT = 0.8f;
            private static final float BLOCK_FACE_LIGHT_RIGHT = 0.6f;
            private static final float BLOCK_FACE_LIGHT_BOTTOM = 0.5f;

    // Tessellator vertex buffer: 8 ints (32 bytes) per vertex. With
    // convertQuadsToTriangles enabled (the build default), quad mode (7) packs
    // 6 vertices per quad (two triangles); the four unique corners are at
    // group offsets 0, 1, 2, 5. The packed face normal is int 6 (three signed
    // bytes), set per face during block-item rendering.
    private static final int VERTEX_STRIDE_INTS = 8;
    private static final int QUAD_DRAW_MODE = 7;
    private static final int VERTS_PER_TRIANGULATED_QUAD = 6;
    private static final int[] QUAD_CORNER_OFFSETS = {0, 1, 2, 5};
    private static final int VERTEX_NORMAL_OFFSET_INTS = 6;

    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);

    private static boolean active;
    private static int displayWidth;
    private static int displayHeight;

    // Per-frame accumulation. xyzuv holds 5 floats (x, y, z, u, v) per vertex
    // (z is normalized depth [0,1], 0 for flat 2D); colors one packed RGBA8 per
    // vertex; command arrays one entry per run of quads sharing a texture and
    // depth mode.
    private static float[] xyzuv = new float[5 * 1024];
    private static int[] colors = new int[1024];
    private static int vertexCount;
    private static long[] cmdTextureIds = new long[256];
    private static int[] cmdQuadCounts = new int[256];
    private static int[] cmdFlags = new int[256];
    private static int cmdCount;

    private static final Set<Long> uploadedTextures = new HashSet<>();
    private static ByteBuffer textureReadBuffer;

    private RemixUiCapture() {
    }

    public static void begin(int width, int height) {
        if (active) {
            return;
        }
        if (!MinecraftRenderHooks.isInitialized() || width <= 0 || height <= 0) {
            return;
        }
        displayWidth = width;
        displayHeight = height;
        vertexCount = 0;
        cmdCount = 0;
        active = true;
    }

    public static void onTessellatorDraw(
            int[] rawVertexData,
            int rawVertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        if (!active || rawVertexData == null) {
            return;
        }
        if (drawMode != QUAD_DRAW_MODE
                || rawVertexCount < VERTS_PER_TRIANGULATED_QUAD
                || rawVertexCount % VERTS_PER_TRIANGULATED_QUAD != 0) {
            return;
        }

        long textureId = 0L;
        if (hasTexture && GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            int glTextureId = GL11.glGetInteger(GL_TEXTURE_BINDING_2D);
            if (glTextureId > 0 && ensureTextureUploaded(glTextureId)) {
                textureId = glTextureId & 0xFFFFFFFFL;
            }
        }

        float[] modelView = captureMatrix(GL_MODELVIEW_MATRIX);
        float[] projection = captureMatrix(GL_PROJECTION_MATRIX);
        if (modelView == null || projection == null) {
            return;
        }
        float[] mvp = MatrixMath.multiplyColumnMajor(projection, modelView);

        int fallbackColor = hasColor ? 0 : captureCurrentColorPacked();

        int quadCount = rawVertexCount / VERTS_PER_TRIANGULATED_QUAD;
        ensureVertexCapacity(vertexCount + quadCount * 4);

        // 3D block item icons render through the Tessellator under a rotated
        // transform, so their vertices span a range in eye-space z. Flat 2D UI
        // is coplanar at the GUI plane (range ~0). Pre-scan to decide whether
        // this batch needs depth testing.
        float minEyeZ = Float.POSITIVE_INFINITY;
        float maxEyeZ = Float.NEGATIVE_INFINITY;
        for (int quad = 0; quad < quadCount; quad++) {
            int group = quad * VERTS_PER_TRIANGULATED_QUAD;
            for (int corner = 0; corner < 4; corner++) {
                int base = (group + QUAD_CORNER_OFFSETS[corner]) * VERTEX_STRIDE_INTS;
                float x = Float.intBitsToFloat(rawVertexData[base]);
                float y = Float.intBitsToFloat(rawVertexData[base + 1]);
                float z = Float.intBitsToFloat(rawVertexData[base + 2]);
                float eyeZ = eyeSpaceZ(modelView, x, y, z);
                if (eyeZ < minEyeZ) {
                    minEyeZ = eyeZ;
                }
                if (eyeZ > maxEyeZ) {
                    maxEyeZ = eyeZ;
                }
            }
        }
        boolean is3D = (maxEyeZ - minEyeZ) > UI_3D_EYE_Z_EPSILON;

        for (int quad = 0; quad < quadCount; quad++) {
            int group = quad * VERTS_PER_TRIANGULATED_QUAD;

            // For 3D draws, bake the standard item lighting per quad from its
            // face normal so block icons are shaded instead of fullbright. Use
            // the packed per-face normal the renderer baked into the vertex
            // stream (int 6, three signed bytes) — the true outward normal.
            float lightFactor = 1.0f;
            if (is3D) {
                int packed = rawVertexData[group * VERTEX_STRIDE_INTS + VERTEX_NORMAL_OFFSET_INTS];
                float nx = (byte) (packed & 0xFF);
                float ny = (byte) ((packed >> 8) & 0xFF);
                float nz = (byte) ((packed >> 16) & 0xFF);
                lightFactor = blockItemFaceLightFactor(modelView, nx, ny, nz);
            }

            for (int corner = 0; corner < 4; corner++) {
                int vertexIndex = group + QUAD_CORNER_OFFSETS[corner];
                int base = vertexIndex * VERTEX_STRIDE_INTS;

                float x = Float.intBitsToFloat(rawVertexData[base]);
                float y = Float.intBitsToFloat(rawVertexData[base + 1]);
                float z = Float.intBitsToFloat(rawVertexData[base + 2]);
                float u = Float.intBitsToFloat(rawVertexData[base + 3]);
                float v = Float.intBitsToFloat(rawVertexData[base + 4]);
                // glColorPointer reads the int at offset 20 as R,G,B,A bytes,
                // i.e. R in the low byte — exactly the remixapi_UIVertex layout.
                int color = hasColor ? rawVertexData[base + 5] : fallbackColor;
                if (is3D) {
                    color = scaleColorRgb(color, lightFactor);
                }

                // Ortho GUI projection keeps w == 1, so the transformed point
                // is already in NDC.
                float[] ndc = MatrixMath.transformPointColumnMajor(mvp, x, y, z);
                float px = (ndc[0] * 0.5f + 0.5f) * displayWidth;
                float py = (0.5f - ndc[1] * 0.5f) * displayHeight;

                int out = vertexCount * 5;
                xyzuv[out] = px;
                xyzuv[out + 1] = py;
                xyzuv[out + 2] = is3D ? mapUiDepth(eyeSpaceZ(modelView, x, y, z)) : 0.0f;
                xyzuv[out + 3] = u;
                xyzuv[out + 4] = v;
                colors[vertexCount] = color;
                vertexCount++;
            }
        }

        appendCommand(textureId, quadCount, is3D ? UI_DRAW_FLAG_DEPTH_TEST : 0);
    }

    /**
     * Captures a text string drawn by the bitmap FontRenderer ({@code sj}).
     *
     * <p>Text bypasses {@link #onTessellatorDraw}: the FontRenderer bakes 256
     * per-glyph display lists at startup and renders strings via
     * {@code glCallLists}, which replays recorded GL commands without
     * re-entering the Tessellator. This method is invoked from the
     * {@code sj.renderString} hook and rebuilds the glyph quads directly from
     * the atlas layout, matching the FontRenderer's geometry, shadow handling
     * and {@code \u00a7} colour codes.
     *
     * @param fontTextureGlId the FontRenderer's atlas GL texture id ({@code sj.a})
     */
    public static void onFontString(
            String text,
            int x,
            int y,
            int color,
            boolean shadow,
            int[] charWidths,
            int fontTextureGlId) {
        if (!active || text == null || text.isEmpty() || charWidths == null) {
            return;
        }
        if (fontTextureGlId <= 0 || !ensureTextureUploadedById(fontTextureGlId)) {
            return;
        }
        long textureId = fontTextureGlId & 0xFFFFFFFFL;

        float[] modelView = captureMatrix(GL_MODELVIEW_MATRIX);
        float[] projection = captureMatrix(GL_PROJECTION_MATRIX);
        if (modelView == null || projection == null) {
            return;
        }
        float[] mvp = MatrixMath.multiplyColumnMajor(projection, modelView);

        int currentColor = darkenAndPack(color, shadow);
        int currentAlpha = currentColor >>> 24;

        ensureVertexCapacity(vertexCount + text.length() * 4);
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

            // Corner order matches the Tessellator glyph quad (BL, BR, TR, TL),
            // consumed by the native index generator as (0,1,2,0,2,3).
            appendGlyphVertex(mvp, x0, y1, u0, v1, currentColor);
            appendGlyphVertex(mvp, x1, y1, u1, v1, currentColor);
            appendGlyphVertex(mvp, x1, y0, u1, v0, currentColor);
            appendGlyphVertex(mvp, x0, y0, u0, v0, currentColor);
            glyphQuads++;

            cursorX += charWidths[glyphId];
        }

        appendCommand(textureId, glyphQuads, 0);
    }

    /** True while a UI capture frame is open (between {@link #begin}/{@link #end}). */
    public static boolean isActive() {
        return active;
    }

    /**
     * Captures a 3D model part (bitmap-skinned box geometry) drawn in
     * screen space — the inventory player preview and, later, 3D block item
     * icons. The model vertices are transformed by the current GL
     * modelview/projection into screen pixels plus normalized depth, so the
     * runtime can depth-test the parts against each other. Per-quad lighting is
     * baked from the face normal (vanilla relies on GL_LIGHTING, which the
     * vertex stream does not carry).
     *
     * <p>Invoked from the model-part hook only while {@link #isActive()} (the
     * GUI phase); in-world entity rendering keeps its existing world-space path.
     */
    public static void onModelPart(tz[] polygons, float scale) {
        if (!active || polygons == null || polygons.length == 0) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }
        int glTextureId = GL11.glGetInteger(GL_TEXTURE_BINDING_2D);
        if (glTextureId <= 0 || !ensureTextureUploaded(glTextureId)) {
            return;
        }
        long textureId = glTextureId & 0xFFFFFFFFL;

        float[] modelView = captureMatrix(GL_MODELVIEW_MATRIX);
        float[] projection = captureMatrix(GL_PROJECTION_MATRIX);
        if (modelView == null || projection == null) {
            return;
        }
        float[] mvp = MatrixMath.multiplyColumnMajor(projection, modelView);
        int baseColor = captureCurrentColorPacked();

        ensureVertexCapacity(vertexCount + polygons.length * 4);
        int quadCount = 0;

        for (tz polygon : polygons) {
            if (polygon == null || polygon.a == null || polygon.a.length != 4) {
                continue;
            }
            ib v0 = polygon.a[0];
            ib v1 = polygon.a[1];
            ib v2 = polygon.a[2];
            ib v3 = polygon.a[3];
            if (v0 == null || v1 == null || v2 == null || v3 == null
                    || v0.a == null || v1.a == null || v2.a == null || v3.a == null) {
                continue;
            }

            float x0 = (float) v0.a.a * scale, y0 = (float) v0.a.b * scale, z0 = (float) v0.a.c * scale;
            float x1 = (float) v1.a.a * scale, y1 = (float) v1.a.b * scale, z1 = (float) v1.a.c * scale;
            float x2 = (float) v2.a.a * scale, y2 = (float) v2.a.b * scale, z2 = (float) v2.a.c * scale;
            float x3 = (float) v3.a.a * scale, y3 = (float) v3.a.b * scale, z3 = (float) v3.a.c * scale;

            int litColor = bakeItemLighting(baseColor, modelView,
                    x1 - x0, y1 - y0, z1 - z0,
                    x2 - x0, y2 - y0, z2 - z0);

            appendModelVertex(mvp, modelView, x0, y0, z0, v0.b, v0.c, litColor);
            appendModelVertex(mvp, modelView, x1, y1, z1, v1.b, v1.c, litColor);
            appendModelVertex(mvp, modelView, x2, y2, z2, v2.b, v2.c, litColor);
            appendModelVertex(mvp, modelView, x3, y3, z3, v3.b, v3.c, litColor);
            quadCount++;
        }

        appendCommand(textureId, quadCount, UI_DRAW_FLAG_DEPTH_TEST);
    }

    public static void end() {
        if (!active) {
            return;
        }
        active = false;
        // Submit even when empty so the runtime clears the previous frame's UI.
        MinecraftRenderHooks.submitUiDrawList(
                xyzuv, colors, vertexCount,
                cmdTextureIds, cmdQuadCounts, cmdFlags, cmdCount,
                displayWidth, displayHeight);
    }

    public static void reset() {
        active = false;
        vertexCount = 0;
        cmdCount = 0;
        uploadedTextures.clear();
    }

    private static void appendCommand(long textureId, int quadCount, int flags) {
        if (quadCount <= 0) {
            return;
        }
        if (cmdCount > 0 && cmdTextureIds[cmdCount - 1] == textureId && cmdFlags[cmdCount - 1] == flags) {
            cmdQuadCounts[cmdCount - 1] += quadCount;
            return;
        }
        if (cmdCount == cmdTextureIds.length) {
            int grown = cmdTextureIds.length * 2;
            long[] newTex = new long[grown];
            int[] newQuads = new int[grown];
            int[] newFlags = new int[grown];
            System.arraycopy(cmdTextureIds, 0, newTex, 0, cmdCount);
            System.arraycopy(cmdQuadCounts, 0, newQuads, 0, cmdCount);
            System.arraycopy(cmdFlags, 0, newFlags, 0, cmdCount);
            cmdTextureIds = newTex;
            cmdQuadCounts = newQuads;
            cmdFlags = newFlags;
        }
        cmdTextureIds[cmdCount] = textureId;
        cmdQuadCounts[cmdCount] = quadCount;
        cmdFlags[cmdCount] = flags;
        cmdCount++;
    }

    private static void appendGlyphVertex(float[] mvp, float gx, float gy, float u, float v, int color) {
        float[] ndc = MatrixMath.transformPointColumnMajor(mvp, gx, gy, 0.0f);
        float px = (ndc[0] * 0.5f + 0.5f) * displayWidth;
        float py = (0.5f - ndc[1] * 0.5f) * displayHeight;
        int out = vertexCount * 5;
        xyzuv[out] = px;
        xyzuv[out + 1] = py;
        xyzuv[out + 2] = 0.0f;
        xyzuv[out + 3] = u;
        xyzuv[out + 4] = v;
        colors[vertexCount] = color;
        vertexCount++;
    }

    private static void appendModelVertex(float[] mvp, float[] modelView,
            float x, float y, float z, float u, float v, int color) {
        float[] ndc = MatrixMath.transformPointColumnMajor(mvp, x, y, z);
        float px = (ndc[0] * 0.5f + 0.5f) * displayWidth;
        float py = (0.5f - ndc[1] * 0.5f) * displayHeight;
        // Depth from eye-space z mapped through the tight UI window (see
        // UI_DEPTH_EYE_NEAR/FAR) so the item's small z-span fills [0,1].
        float vkz = mapUiDepth(eyeSpaceZ(modelView, x, y, z));
        int out = vertexCount * 5;
        xyzuv[out] = px;
        xyzuv[out + 1] = py;
        xyzuv[out + 2] = vkz;
        xyzuv[out + 3] = u;
        xyzuv[out + 4] = v;
        colors[vertexCount] = color;
        vertexCount++;
    }

    /**
     * Bakes two-directional item lighting into a packed RGBA8 colour from a
     * quad's face normal. The normal (model-space cross product of two edges)
     * is taken to eye space and oriented toward the camera so every visible
     * face is shaded consistently regardless of winding or the inventory's
     * mirrored scale.
     */
    private static int bakeItemLighting(int baseColor, float[] modelView,
            float ex1, float ey1, float ez1, float ex2, float ey2, float ez2) {
        // Face normal = edge1 x edge2 (matches the renderer's per-face normal).
        float nx = ey1 * ez2 - ez1 * ey2;
        float ny = ez1 * ex2 - ex1 * ez2;
        float nz = ex1 * ey2 - ey1 * ex2;
        return scaleColorRgb(baseColor, itemLightFactor(modelView, nx, ny, nz));
    }

    /**
     * Two-directional item-light brightness [ambient, 1] for a model-space face
     * normal. The normal is taken to eye space via the modelview (exactly as
     * fixed-function lighting does, including any mirrored scale) and dotted
     * with the eye-space light directions; no reorientation is applied, so the
     * result matches vanilla's RenderHelper shading.
     */
    private static float itemLightFactor(float[] modelView, float nx, float ny, float nz) {
        float enx = modelView[0] * nx + modelView[4] * ny + modelView[8] * nz;
        float eny = modelView[1] * nx + modelView[5] * ny + modelView[9] * nz;
        float enz = modelView[2] * nx + modelView[6] * ny + modelView[10] * nz;
        float len = (float) Math.sqrt(enx * enx + eny * eny + enz * enz);
        if (len < 1e-6f) {
            return 1.0f;
        }
        enx /= len;
        eny /= len;
        enz /= len;

        float d0 = enx * ITEM_LIGHT0[0] + eny * ITEM_LIGHT0[1] + enz * ITEM_LIGHT0[2];
        float d1 = enx * ITEM_LIGHT1[0] + eny * ITEM_LIGHT1[1] + enz * ITEM_LIGHT1[2];
        float brightness = ITEM_LIGHT_AMBIENT
                + ITEM_LIGHT_DIFFUSE * Math.max(0.0f, d0)
                + ITEM_LIGHT_DIFFUSE * Math.max(0.0f, d1);
        if (brightness > 1.0f) {
            brightness = 1.0f;
        }
        return brightness;
    }

    private static float blockItemFaceLightFactor(float[] modelView, float nx, float ny, float nz) {
        // The packed normal from the Tessellator is the original axis-aligned
        // model-space face normal (top/bottom/side). If we classify top/bottom
        // after transforming it, the inventory rotations can make the top face
        // look like a side face, which is what darkened the top previously.
        if (ny >= 0.5f) {
            return BLOCK_FACE_LIGHT_TOP;
        }
        if (ny <= -0.5f) {
            return BLOCK_FACE_LIGHT_BOTTOM;
        }

        // For the remaining side faces, use the transformed X sign to decide
        // which side is screen-left vs screen-right under the current inventory
        // rotation.
        float enx = modelView[0] * nx + modelView[4] * ny + modelView[8] * nz;
        float eny = modelView[1] * nx + modelView[5] * ny + modelView[9] * nz;
        float enz = modelView[2] * nx + modelView[6] * ny + modelView[10] * nz;
        float len = (float) Math.sqrt(enx * enx + eny * eny + enz * enz);
        if (len < 1e-6f) {
            return BLOCK_FACE_LIGHT_RIGHT;
        }
        enx /= len;
        return enx < 0.0f ? BLOCK_FACE_LIGHT_LEFT : BLOCK_FACE_LIGHT_RIGHT;
    }

    private static int scaleColorRgb(int color, float factor) {
        int r = (int) ((color & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) (((color >> 16) & 0xFF) * factor);
        int a = (color >>> 24) & 0xFF;
        if (r > 255) {
            r = 255;
        }
        if (g > 255) {
            g = 255;
        }
        if (b > 255) {
            b = 255;
        }
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    private static float[] normalize3(float x, float y, float z) {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len < 1e-6f) {
            return new float[] { 0.0f, 0.0f, 1.0f };
        }
        return new float[] { x / len, y / len, z / len };
    }

    /** Eye-space z (modelview row 2) of a model-space point. */
    private static float eyeSpaceZ(float[] modelView, float x, float y, float z) {
        return modelView[2] * x + modelView[6] * y + modelView[10] * z + modelView[14];
    }

    /**
     * Maps eye-space z through the tight UI depth window (UI_DEPTH_EYE_NEAR ->
     * UI_DEPTH_EYE_FAR) to a normalized [0,1] depth so small item z-spans use
     * the full depth range and the runtime depth test resolves self-occlusion.
     */
    private static float mapUiDepth(float eyeZ) {
        float vkz = (eyeZ - UI_DEPTH_EYE_NEAR) / (UI_DEPTH_EYE_FAR - UI_DEPTH_EYE_NEAR);
        if (vkz < 0.0f) {
            return 0.0f;
        }
        if (vkz > 1.0f) {
            return 1.0f;
        }
        return vkz;
    }

    /**
     * Applies the FontRenderer's colour rules to a packed ARGB colour and
     * repacks it into the RGBA8 layout the runtime expects (R in the low byte).
     * Shadow text darkens RGB to a quarter; a zero alpha is treated as opaque.
     */
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

    /**
     * Reproduces the 16-entry FontRenderer colour palette used by {@code \u00a7}
     * codes, keeping the running alpha. Shadow text uses the darkened palette.
     */
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

    private static boolean ensureTextureUploadedById(int glTextureId) {
        long key = glTextureId & 0xFFFFFFFFL;
        if (uploadedTextures.contains(key)) {
            return true;
        }
        int previous = GL11.glGetInteger(GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureId);
        boolean ok = ensureTextureUploaded(glTextureId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
        return ok;
    }

    private static boolean ensureTextureUploaded(int glTextureId) {
        long key = glTextureId & 0xFFFFFFFFL;
        if (uploadedTextures.contains(key)) {
            return true;
        }

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (width <= 0 || height <= 0) {
            return false;
        }

        int capacity = width * height * 4;
        if (textureReadBuffer == null || textureReadBuffer.capacity() < capacity) {
            textureReadBuffer = BufferUtils.createByteBuffer(capacity);
        } else {
            textureReadBuffer.clear();
        }

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureReadBuffer);
        textureReadBuffer.rewind();

        boolean ok = MinecraftRenderHooks.registerUiTexture(
                textureReadBuffer, key, width, height, RemixBridgeNative.SCREEN_OVERLAY_FORMAT_RGBA8);
        if (ok) {
            uploadedTextures.add(key);
        }
        return ok;
    }

    private static float[] captureMatrix(int matrixName) {
        MATRIX_BUFFER.clear();
        if (!OpenGlCompat.getFloat(matrixName, MATRIX_BUFFER)) {
            return null;
        }
        float[] matrix = new float[16];
        MATRIX_BUFFER.get(matrix);
        return matrix;
    }

    private static int captureCurrentColorPacked() {
        COLOR_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_CURRENT_COLOR, COLOR_BUFFER)) {
            return 0xFFFFFFFF;
        }
        int r = clampColorByte(COLOR_BUFFER.get(0));
        int g = clampColorByte(COLOR_BUFFER.get(1));
        int b = clampColorByte(COLOR_BUFFER.get(2));
        int a = clampColorByte(COLOR_BUFFER.get(3));
        return r | (g << 8) | (b << 16) | (a << 24);
    }

    private static int clampColorByte(float value) {
        int scaled = (int) (value * 255.0f + 0.5f);
        if (scaled < 0) {
            return 0;
        }
        if (scaled > 255) {
            return 255;
        }
        return scaled;
    }

    private static void ensureVertexCapacity(int requiredVertices) {
        if (requiredVertices <= colors.length) {
            return;
        }
        int grown = colors.length;
        while (grown < requiredVertices) {
            grown *= 2;
        }
        float[] newXyzuv = new float[grown * 5];
        int[] newColors = new int[grown];
        System.arraycopy(xyzuv, 0, newXyzuv, 0, vertexCount * 5);
        System.arraycopy(colors, 0, newColors, 0, vertexCount);
        xyzuv = newXyzuv;
        colors = newColors;
    }
}
