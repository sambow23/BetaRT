import mcrtx.bridge.MatrixMath;
import org.lwjgl.opengl.GL11;

final class RemixUiModelCapture {
    private static final int UI_DRAW_FLAG_DEPTH_TEST = 0x1;
    private static final float UI_3D_EYE_Z_EPSILON = 1.0f;
    private static final float ITEM_LIGHT_AMBIENT = 0.40f;
    private static final float ITEM_LIGHT_DIFFUSE = 0.6f;
    private static final float[] ITEM_LIGHT0 = normalize3(-0.85f, -1.0f, 0.7f);
    private static final float[] ITEM_LIGHT1 = normalize3(-0.30f, -1.0f, 0.7f);
    private static final float BLOCK_FACE_LIGHT_TOP = 1.0f;
    private static final float BLOCK_FACE_LIGHT_LEFT = 0.8f;
    private static final float BLOCK_FACE_LIGHT_RIGHT = 0.6f;
    private static final float BLOCK_FACE_LIGHT_BOTTOM = 0.5f;
    private static final int VERTEX_STRIDE_INTS = 8;
    private static final int QUAD_DRAW_MODE = 7;
    private static final int VERTS_PER_TRIANGULATED_QUAD = 6;
    private static final int[] QUAD_CORNER_OFFSETS = {0, 1, 2, 5};
    private static final int VERTEX_NORMAL_OFFSET_INTS = 6;

    private RemixUiModelCapture() {
    }

    static void onTessellatorDraw(
            int[] rawVertexData,
            int rawVertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        if (!RemixUiCaptureSession.isCapturingUiOrNameTags() || rawVertexData == null) {
            return;
        }
        if (drawMode != QUAD_DRAW_MODE
                || rawVertexCount < VERTS_PER_TRIANGULATED_QUAD
                || rawVertexCount % VERTS_PER_TRIANGULATED_QUAD != 0) {
            return;
        }

        long textureId = hasTexture ? RemixUiTextureRegistry.currentTextureId() : 0L;
        float[] modelView = RemixUiProjection.captureModelView();
        float[] projection = RemixUiProjection.captureProjection();
        if (modelView == null || projection == null) {
            return;
        }
        projection = RemixNameTagCapture.adjustProjection(projection);
        float[] mvp = MatrixMath.multiplyColumnMajor(projection, modelView);
        if (RemixUiCaptureSession.isNameTagCaptureActive()
                && !RemixNameTagCapture.acceptAnchor(mvp, modelView)) {
            return;
        }

        int fallbackColor = hasColor ? 0 : RemixUiProjection.captureCurrentColorPacked();
        int quadCount = rawVertexCount / VERTS_PER_TRIANGULATED_QUAD;
        RemixUiDrawList drawList = RemixUiCaptureSession.drawList();
        drawList.ensureVertexCapacity(drawList.vertexCount() + quadCount * 4);

        float minEyeZ = Float.POSITIVE_INFINITY;
        float maxEyeZ = Float.NEGATIVE_INFINITY;
        for (int quad = 0; quad < quadCount; quad++) {
            int group = quad * VERTS_PER_TRIANGULATED_QUAD;
            for (int corner = 0; corner < 4; corner++) {
                int base = (group + QUAD_CORNER_OFFSETS[corner]) * VERTEX_STRIDE_INTS;
                float x = Float.intBitsToFloat(rawVertexData[base]);
                float y = Float.intBitsToFloat(rawVertexData[base + 1]);
                float z = Float.intBitsToFloat(rawVertexData[base + 2]);
                float eyeZ = RemixUiProjection.eyeSpaceZ(modelView, x, y, z);
                if (eyeZ < minEyeZ) {
                    minEyeZ = eyeZ;
                }
                if (eyeZ > maxEyeZ) {
                    maxEyeZ = eyeZ;
                }
                if (RemixUiCaptureSession.isNameTagCaptureActive()) {
                    RemixUiProjection.ProjectedPoint projected =
                            RemixUiProjection.projectToScreenPixels(mvp, x, y, z);
                    if (!RemixNameTagCapture.acceptProjectedVertex(projected)) {
                        return;
                    }
                }
            }
        }
        boolean is3D = (maxEyeZ - minEyeZ) > UI_3D_EYE_Z_EPSILON;

        for (int quad = 0; quad < quadCount; quad++) {
            int group = quad * VERTS_PER_TRIANGULATED_QUAD;
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
                int color = hasColor ? rawVertexData[base + 5] : fallbackColor;
                if (is3D) {
                    color = scaleColorRgb(color, lightFactor);
                }
                if (RemixUiCaptureSession.isNameTagCaptureActive()) {
                    color = RemixNameTagCapture.applyFade(color);
                }

                RemixUiProjection.ProjectedPoint projected =
                        RemixUiProjection.projectToScreenPixels(mvp, x, y, z);
                if (RemixUiCaptureSession.isNameTagCaptureActive()
                        && !RemixNameTagCapture.acceptProjectedVertex(projected)) {
                    return;
                }
                float depth = RemixUiCaptureSession.isNameTagCaptureActive()
                        ? RemixNameTagCapture.depthForLayer(false)
                        : (is3D
                                ? RemixUiProjection.mapUiDepth(
                                        RemixUiProjection.eyeSpaceZ(modelView, x, y, z))
                                : 0.0f);
                drawList.appendVertex(projected.x, projected.y, depth, u, v, color);
            }
        }

        drawList.appendCommand(
                textureId,
                quadCount,
                RemixUiCaptureSession.isNameTagCaptureActive()
                        ? RemixNameTagCapture.commandFlags()
                        : (is3D ? UI_DRAW_FLAG_DEPTH_TEST : 0));
    }

    static boolean onModelPart(tz[] polygons, float scale) {
        if (!RemixUiCaptureSession.isActive() || polygons == null || polygons.length == 0) {
            return false;
        }
        long textureId = RemixUiTextureRegistry.currentTextureId();
        if (textureId == 0L) {
            return false;
        }
        float[] modelView = RemixUiProjection.captureModelView();
        float[] projection = RemixUiProjection.captureProjection();
        if (modelView == null || projection == null) {
            return false;
        }
        float[] mvp = MatrixMath.multiplyColumnMajor(projection, modelView);
        int baseColor = RemixUiProjection.captureCurrentColorPacked();
        RemixUiDrawList drawList = RemixUiCaptureSession.drawList();
        drawList.ensureVertexCapacity(drawList.vertexCount() + polygons.length * 4);
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

            float x0 = (float) v0.a.a * scale;
            float y0 = (float) v0.a.b * scale;
            float z0 = (float) v0.a.c * scale;
            float x1 = (float) v1.a.a * scale;
            float y1 = (float) v1.a.b * scale;
            float z1 = (float) v1.a.c * scale;
            float x2 = (float) v2.a.a * scale;
            float y2 = (float) v2.a.b * scale;
            float z2 = (float) v2.a.c * scale;
            float x3 = (float) v3.a.a * scale;
            float y3 = (float) v3.a.b * scale;
            float z3 = (float) v3.a.c * scale;

            int litColor = bakeItemLighting(
                    baseColor,
                    modelView,
                    x1 - x0,
                    y1 - y0,
                    z1 - z0,
                    x2 - x0,
                    y2 - y0,
                    z2 - z0);
            appendModelVertex(drawList, mvp, modelView, x0, y0, z0, v0.b, v0.c, litColor);
            appendModelVertex(drawList, mvp, modelView, x1, y1, z1, v1.b, v1.c, litColor);
            appendModelVertex(drawList, mvp, modelView, x2, y2, z2, v2.b, v2.c, litColor);
            appendModelVertex(drawList, mvp, modelView, x3, y3, z3, v3.b, v3.c, litColor);
            quadCount++;
        }

        drawList.appendCommand(textureId, quadCount, UI_DRAW_FLAG_DEPTH_TEST);
        return quadCount > 0;
    }

    private static void appendModelVertex(
            RemixUiDrawList drawList,
            float[] mvp,
            float[] modelView,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color) {
        RemixUiProjection.ProjectedPoint projected =
                RemixUiProjection.projectToScreenPixels(mvp, x, y, z);
        drawList.appendVertex(
                projected.x,
                projected.y,
                RemixUiProjection.mapUiDepth(RemixUiProjection.eyeSpaceZ(modelView, x, y, z)),
                u,
                v,
                color);
    }

    private static int bakeItemLighting(
            int baseColor,
            float[] modelView,
            float ex1,
            float ey1,
            float ez1,
            float ex2,
            float ey2,
            float ez2) {
        float nx = ey1 * ez2 - ez1 * ey2;
        float ny = ez1 * ex2 - ex1 * ez2;
        float nz = ex1 * ey2 - ey1 * ex2;
        return scaleColorRgb(baseColor, itemLightFactor(modelView, nx, ny, nz));
    }

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
        return brightness > 1.0f ? 1.0f : brightness;
    }

    private static float blockItemFaceLightFactor(float[] modelView, float nx, float ny, float nz) {
        if (ny >= 0.5f) {
            return BLOCK_FACE_LIGHT_TOP;
        }
        if (ny <= -0.5f) {
            return BLOCK_FACE_LIGHT_BOTTOM;
        }
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
            return new float[] {0.0f, 0.0f, 1.0f};
        }
        return new float[] {x / len, y / len, z / len};
    }
}
