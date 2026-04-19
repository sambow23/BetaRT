import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.MatrixMath;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class RemixDynamicEntityCapture {
    private static final int MAX_DYNAMIC_BONES = 256;
    private static final int FIRST_PERSON_DYNAMIC_ENTITY_ID = Integer.MAX_VALUE - 1;
    private static final int FIRST_PERSON_PLAYER_SHADOW_ENTITY_ID = Integer.MAX_VALUE - 2;
    private static final int TILE_ENTITY_ID_NAMESPACE = 0x40000000;
    private static final float FONT_GLYPH_SIZE = 7.99f;
    private static final float FONT_ATLAS_SIZE = 128.0f;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final String FIRST_PERSON_PLAYER_SHADOW_TEXTURE_ALIAS_PREFIX = "/mcrtx_alias/firstperson_shadow/";
    private static final String FONT_TEXTURE_PATH = "/font/default.png";
    private static final String PAINTING_TEXTURE_PATH = "/art/kz.png";
    private static final String SIGN_TEXTURE_PATH = "/item/sign.png";
    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final float[] firstPersonShadowOverlayInverse = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private static boolean dynamicCaptureFrameActive;
    private static boolean signRenderActive;
    private static boolean dynamicEntityActive;
    private static int activeDynamicEntityId = -1;
    private static String activeDynamicEntityTexture = "";
    private static int nextDynamicBoneIndex;
    private static boolean firstPersonActive;
    private static String activeFirstPersonTexture = "";
    private static boolean firstPersonShadowCaptureActive;
    private static boolean firstPersonShadowCaptureAvailable = true;
    private static boolean loggedDynamicEntityHookFailure;
    private static boolean loggedDynamicEntityBoneOverflow;
    private static boolean loggedFirstPersonShadowCaptureFailure;

    private RemixDynamicEntityCapture() {
    }

    public static void onLivingEntityFrameBegin() {
        long beginNanos = System.nanoTime();
        ensureDynamicCaptureFrame();
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onLivingEntityFrameBegin.ensureFrame",
                System.nanoTime() - beginNanos);
    }

    public static void onLivingEntityRenderStart(sn entity) {
        if (!MinecraftRenderHooks.isInitialized() || entity == null) {
            return;
        }
        dynamicEntityActive = true;
        activeDynamicEntityId = entity.aD;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(entity.aD);
    }

    public static void onLivingEntityRenderEnd() {
        if (!dynamicEntityActive) {
            return;
        }
        MinecraftRenderHooks.endDynamicEntity();
        dynamicEntityActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void onSignRenderStart(yk sign) {
        if (!MinecraftRenderHooks.isInitialized() || sign == null) {
            return;
        }

        ensureDynamicCaptureFrame();
        signRenderActive = true;
        dynamicEntityActive = true;
        activeDynamicEntityId = stableTileEntityId(sign.e, sign.f, sign.g, 0x5349474E);
        activeDynamicEntityTexture = SIGN_TEXTURE_PATH;
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(activeDynamicEntityId);
        MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
    }

    public static void onSignRenderEnd() {
        signRenderActive = false;
        onLivingEntityRenderEnd();
    }

    public static void onPaintingRender(qv painting) {
        if (!MinecraftRenderHooks.isInitialized() || painting == null || painting.e == null) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        ensureDynamicCaptureFrame();

        try {
            long renderStartNanos = System.nanoTime();
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);
            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            long stateReadEndNanos = System.nanoTime();

            MinecraftRenderHooks.beginDynamicEntity(painting.aD);
            MinecraftRenderHooks.setDynamicEntityTexture(PAINTING_TEXTURE_PATH);
            submitDynamicBoneTransform(0, modelToWorld);
            long setupEndNanos = System.nanoTime();
            capturePaintingGeometry(painting, 0);
            long captureEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPaintingRender.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPaintingRender.setupEntity",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onPaintingRender.captureGeometry",
                    captureEndNanos - setupEndNanos);
        } finally {
            MinecraftRenderHooks.endDynamicEntity();
        }
    }

    public static void onSignTextRender(String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        if (!signRenderActive || !dynamicEntityActive || text == null || text.isEmpty() || characterWidths == null || characterWidths.length == 0) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        try {
            if (!FONT_TEXTURE_PATH.equals(activeDynamicEntityTexture)) {
                activeDynamicEntityTexture = FONT_TEXTURE_PATH;
                MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
            }

            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            int packedColor = shadow ? applyFontShadow(colorRgba) : colorRgba;
            int sanitizedColor = ColorMath.sanitizePackedColor(packedColor);
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);

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

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        glyphMinX, glyphMaxY, 0.0f, u0, v1,
                        glyphMaxX, glyphMaxY, 0.0f, u1, v1,
                        glyphMaxX, glyphMinY, 0.0f, u1, v0,
                        glyphMinX, glyphMinY, 0.0f, u0, v0,
                        sanitizedColor,
                        boneIndex);

                cursorX += characterWidths[glyphId];
            }
        } catch (RuntimeException exception) {
            handleHookFailure(exception);
        }
    }

    public static void onFirstPersonRenderStart() {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        ensureDynamicCaptureFrame();
        firstPersonActive = true;
        activeFirstPersonTexture = "/mob/char.png";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(FIRST_PERSON_DYNAMIC_ENTITY_ID);
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
    }

    public static void onFirstPersonShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        if (!firstPersonShadowCaptureAvailable || !MinecraftRenderHooks.isInitialized() || minecraft == null || !(minecraft.h instanceof gs)) {
            return;
        }

        long renderStartNanos = System.nanoTime();

        bw renderer = th.a.a(minecraft.h);
        if (!(renderer instanceof ds)) {
            return;
        }
        long lookupRendererEndNanos = System.nanoTime();

        ensureDynamicCaptureFrame();
        dynamicEntityActive = true;
        activeDynamicEntityId = FIRST_PERSON_PLAYER_SHADOW_ENTITY_ID;
        activeDynamicEntityTexture = makeFirstPersonShadowTextureAlias("/mob/char.png");
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(FIRST_PERSON_PLAYER_SHADOW_ENTITY_ID);
        MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
        firstPersonShadowCaptureActive = true;

        try {
            gs player = (gs) minecraft.h;
            ls viewEntity = minecraft.i != null ? minecraft.i : player;
            th.a.a(minecraft.f, minecraft.p, minecraft.q, viewEntity, minecraft.z, partialTicks);
            double previousRenderOriginX = th.b;
            double previousRenderOriginY = th.c;
            double previousRenderOriginZ = th.d;
            double viewX = viewEntity.bl + (viewEntity.aM - viewEntity.bl) * (double) partialTicks;
            double viewY = viewEntity.bm + (viewEntity.aN - viewEntity.bm) * (double) partialTicks;
            double viewZ = viewEntity.bn + (viewEntity.aO - viewEntity.bn) * (double) partialTicks;
            th.b = viewX;
            th.c = viewY;
            th.d = viewZ;
            double worldX = player.bl + (player.aM - player.bl) * (double) partialTicks;
            double worldY = player.bm + (player.aN - player.bm) * (double) partialTicks;
            double worldZ = player.bn + (player.aO - player.bn) * (double) partialTicks;
            double renderX = worldX - th.b;
            double renderY = worldY - th.c;
            double renderZ = worldZ - th.d;
            float interpolatedYaw = player.aU + (player.aS - player.aU) * partialTicks;
            float brightness = player.a(partialTicks);
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] overlayModelView = new float[16];
            MODEL_VIEW_BUFFER.get(overlayModelView);
            float[] overlayInverse = MatrixMath.invertAffineColumnMajor(overlayModelView);
            System.arraycopy(overlayInverse, 0, firstPersonShadowOverlayInverse, 0, firstPersonShadowOverlayInverse.length);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            long setupEndNanos = System.nanoTime();
            long shadowRenderEndNanos;
            try {
                GL11.glColorMask(false, false, false, false);
                GL11.glDepthMask(false);
                GL11.glColor3f(brightness, brightness, brightness);
                ((ds) renderer).a(player, renderX, renderY, renderZ, interpolatedYaw, partialTicks);
                shadowRenderEndNanos = System.nanoTime();
            } finally {
                th.b = previousRenderOriginX;
                th.c = previousRenderOriginY;
                th.d = previousRenderOriginZ;
                GL11.glPopAttrib();
            }

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonShadowPlayerRender.lookupRenderer",
                    lookupRendererEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonShadowPlayerRender.setupCapture",
                    setupEndNanos - lookupRendererEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonShadowPlayerRender.renderShadow",
                    shadowRenderEndNanos - setupEndNanos);
        } catch (RuntimeException exception) {
            disableFirstPersonShadowCapture(exception);
            return;
        } finally {
            firstPersonShadowCaptureActive = false;
            if (dynamicEntityActive && activeDynamicEntityId == FIRST_PERSON_PLAYER_SHADOW_ENTITY_ID) {
                MinecraftRenderHooks.endDynamicEntity();
                dynamicEntityActive = false;
                activeDynamicEntityId = -1;
                activeDynamicEntityTexture = "";
                nextDynamicBoneIndex = 0;
            }
        }
    }

    public static void onFirstPersonRenderEnd() {
        if (!firstPersonActive) {
            return;
        }

        MinecraftRenderHooks.endDynamicEntity();
        firstPersonActive = false;
        activeFirstPersonTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void onFramePresented() {
        dynamicCaptureFrameActive = false;
        signRenderActive = false;
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        if (!firstPersonActive || itemStack == null) {
            return;
        }

        activeFirstPersonTexture = itemStack.c < 256 ? "/terrain.png" : "/gui/items.png";
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        if (!dynamicEntityActive) {
            return;
        }
        String resolvedTexture = normalizeDynamicTexturePath(primaryTexture, fallbackTexture);
        if (firstPersonShadowCaptureActive) {
            resolvedTexture = makeFirstPersonShadowTextureAlias(resolvedTexture);
        }
        if (resolvedTexture.isEmpty() || resolvedTexture.equals(activeDynamicEntityTexture)) {
            return;
        }
        activeDynamicEntityTexture = resolvedTexture;
        MinecraftRenderHooks.setDynamicEntityTexture(resolvedTexture);
    }

    public static void onModelPartRender(tz[] polygons, float scale) {
        String activeTexture = activeCaptureTexture();
        if (activeTexture.isEmpty() || polygons == null || polygons.length == 0) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }
        try {
            long renderStartNanos = System.nanoTime();
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL_CURRENT_COLOR, COLOR_BUFFER);
            float[] color = new float[4];
            COLOR_BUFFER.get(color);
            long stateReadEndNanos = System.nanoTime();

            float[] modelToWorld;
            if (firstPersonShadowCaptureActive) {
                float[] overlayNeutralModelView = MatrixMath.multiplyColumnMajor(firstPersonShadowOverlayInverse, modelView);
                modelToWorld = MatrixMath.multiplyColumnMajor(buildCameraTranslationMatrix(), overlayNeutralModelView);
            } else {
                modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            }
            float[] capturedColor = ColorMath.sanitizeDynamicEntityColor(color[0], color[1], color[2], color[3]);
            int colorRgba = ColorMath.packColor(capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3]);
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);
            long setupEndNanos = System.nanoTime();

            for (tz polygon : polygons) {
                if (polygon == null || polygon.a == null || polygon.a.length != 4) {
                    continue;
                }

                float[][] positions = new float[4][3];
                float[][] texcoords = new float[4][2];
                for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                    ib vertex = polygon.a[vertexIndex];
                    if (vertex == null || vertex.a == null) {
                        continue;
                    }
                    positions[vertexIndex][0] = (float) vertex.a.a * scale;
                    positions[vertexIndex][1] = (float) vertex.a.b * scale;
                    positions[vertexIndex][2] = (float) vertex.a.c * scale;
                    texcoords[vertexIndex][0] = vertex.b;
                    texcoords[vertexIndex][1] = vertex.c;
                }

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        positions[0][0], positions[0][1], positions[0][2], texcoords[0][0], texcoords[0][1],
                        positions[1][0], positions[1][1], positions[1][2], texcoords[1][0], texcoords[1][1],
                        positions[2][0], positions[2][1], positions[2][2], texcoords[2][0], texcoords[2][1],
                        positions[3][0], positions[3][1], positions[3][2], texcoords[3][0], texcoords[3][1],
                        colorRgba,
                        boneIndex);
            }
            long quadEmitEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onModelPartRender.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onModelPartRender.setupTransform",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onModelPartRender.emitQuads",
                    quadEmitEndNanos - setupEndNanos);
        } catch (RuntimeException exception) {
            handleHookFailure(exception);
        }
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        if (firstPersonShadowCaptureActive) {
            return;
        }
        String activeTexture = activeCaptureTexture();
        if (activeTexture.isEmpty()) {
            return;
        }
        if (rawVertexData == null || vertexCount < 6 || drawMode != 7 || !hasTexture) {
            return;
        }
        if (vertexCount % 6 != 0) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        try {
            long renderStartNanos = System.nanoTime();
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL_CURRENT_COLOR, COLOR_BUFFER);
            float[] currentColor = new float[4];
            COLOR_BUFFER.get(currentColor);
            long stateReadEndNanos = System.nanoTime();

            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            int fallbackColorRgba = ColorMath.sanitizePackedColor(ColorMath.packColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]));
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);
            long setupEndNanos = System.nanoTime();

            for (int vertexIndex = 0; vertexIndex + 5 < vertexCount; vertexIndex += 6) {
                int quadColor = hasColor ? ColorMath.sanitizePackedColor(ColorMath.unpackTessellatorColor(rawVertexData, vertexIndex * 8 + 5)) : fallbackColorRgba;

                float p0x = Float.intBitsToFloat(rawVertexData[vertexIndex * 8]);
                float p0y = Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 1]);
                float p0z = Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 2]);
                float p1x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8]);
                float p1y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 1]);
                float p1z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 2]);
                float p2x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8]);
                float p2y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 1]);
                float p2z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 2]);
                float p3x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8]);
                float p3y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 1]);
                float p3z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 2]);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        p0x, p0y, p0z, Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 3]), Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 4]),
                        p1x, p1y, p1z, Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 4]),
                        p2x, p2y, p2z, Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 4]),
                        p3x, p3y, p3z, Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 4]),
                        quadColor,
                        boneIndex);
            }
            long quadEmitEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonTessellatorDraw.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonTessellatorDraw.setupTransform",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onFirstPersonTessellatorDraw.emitQuads",
                    quadEmitEndNanos - setupEndNanos);
        } catch (RuntimeException exception) {
            handleHookFailure(exception);
        }
    }

    private static void handleHookFailure(RuntimeException exception) {
        MinecraftRenderHooks.endDynamicEntity();
        if (!loggedDynamicEntityHookFailure) {
            loggedDynamicEntityHookFailure = true;
            System.err.println("[mcrtx] dynamic entity capture disabled after hook failure");
            exception.printStackTrace();
        }
        dynamicEntityActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityTexture = "";
        signRenderActive = false;
        firstPersonActive = false;
        activeFirstPersonTexture = "";
        firstPersonShadowCaptureActive = false;
    }

    private static void disableFirstPersonShadowCapture(RuntimeException exception) {
        firstPersonShadowCaptureAvailable = false;
        if (!loggedFirstPersonShadowCaptureFailure) {
            loggedFirstPersonShadowCaptureFailure = true;
            System.err.println("[mcrtx] disabling first-person shadow capture after hook failure");
            exception.printStackTrace();
        }
    }

    private static int applyFontShadow(int colorRgba) {
        int alpha = colorRgba & 0xFF000000;
        return ((colorRgba & 0x00FCFCFC) >> 2) | alpha;
    }

    private static void capturePaintingGeometry(qv painting, int boneIndex) {
        iq motive = painting.e;
        float startX = -motive.B / 2.0f;
        float startY = -motive.C / 2.0f;
        float frontZ = -0.5f;
        float backZ = 0.5f;

        for (int tileX = 0; tileX < motive.B / 16; tileX++) {
            for (int tileY = 0; tileY < motive.C / 16; tileY++) {
                float maxX = startX + (tileX + 1) * 16.0f;
                float minX = startX + tileX * 16.0f;
                float maxY = startY + (tileY + 1) * 16.0f;
                float minY = startY + tileY * 16.0f;
                int segmentColor = paintingSegmentColor(painting, (maxX + minX) * 0.5f, (maxY + minY) * 0.5f);

                float frontMinU = (motive.D + motive.B - tileX * 16.0f) / 256.0f;
                float frontMaxU = (motive.D + motive.B - (tileX + 1) * 16.0f) / 256.0f;
                float frontMinV = (motive.E + motive.C - tileY * 16.0f) / 256.0f;
                float frontMaxV = (motive.E + motive.C - (tileY + 1) * 16.0f) / 256.0f;
                float backMinU = 0.75f;
                float backMaxU = 0.8125f;
                float backMinV = 0.0f;
                float backMaxV = 0.0625f;
                float edgeMinU = 0.751953125f;
                float edgeMaxU = 0.751953125f;
                float edgeMinV = 0.0f;
                float edgeMaxV = 0.0625f;
                float sideMinU = 0.001953125f;
                float sideMaxU = 0.001953125f;

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        maxX, minY, frontZ, frontMaxU, frontMinV,
                        minX, minY, frontZ, frontMinU, frontMinV,
                        minX, maxY, frontZ, frontMinU, frontMaxV,
                        maxX, maxY, frontZ, frontMaxU, frontMaxV,
                        segmentColor,
                        boneIndex);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        maxX, maxY, backZ, backMinU, backMinV,
                        minX, maxY, backZ, backMaxU, backMinV,
                        minX, minY, backZ, backMaxU, backMaxV,
                        maxX, minY, backZ, backMinU, backMaxV,
                        segmentColor,
                        boneIndex);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        maxX, maxY, frontZ, backMinU, sideMinU,
                        minX, maxY, frontZ, backMaxU, sideMinU,
                        minX, maxY, backZ, backMaxU, sideMaxU,
                        maxX, maxY, backZ, backMinU, sideMaxU,
                        segmentColor,
                        boneIndex);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        maxX, minY, backZ, backMinU, sideMinU,
                        minX, minY, backZ, backMaxU, sideMinU,
                        minX, minY, frontZ, backMaxU, sideMaxU,
                        maxX, minY, frontZ, backMinU, sideMaxU,
                        segmentColor,
                        boneIndex);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        maxX, maxY, backZ, edgeMaxU, edgeMinV,
                        maxX, minY, backZ, edgeMaxU, edgeMaxV,
                        maxX, minY, frontZ, edgeMinU, edgeMaxV,
                        maxX, maxY, frontZ, edgeMinU, edgeMinV,
                        segmentColor,
                        boneIndex);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        minX, maxY, frontZ, edgeMaxU, edgeMinV,
                        minX, minY, frontZ, edgeMaxU, edgeMaxV,
                        minX, minY, backZ, edgeMinU, edgeMaxV,
                        minX, maxY, backZ, edgeMinU, edgeMinV,
                        segmentColor,
                        boneIndex);
            }
        }
    }

    private static int paintingSegmentColor(qv painting, float centerX, float centerY) {
        return ColorMath.packColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void ensureDynamicCaptureFrame() {
        if (dynamicCaptureFrameActive || !MinecraftRenderHooks.isInitialized()) {
            return;
        }

        long beginFrameStartNanos = System.nanoTime();
        MinecraftRenderHooks.beginDynamicEntityFrame();
        MinecraftRenderHooks.beginDestroyOverlayFrame();
        dynamicCaptureFrameActive = true;
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.dynamicEntity.ensureFrame.beginFrame",
                System.nanoTime() - beginFrameStartNanos);
    }

    private static String normalizeDynamicTexturePath(String primaryTexture, String fallbackTexture) {
        String normalizedPrimary = stripTexturePrefix(primaryTexture);
        if (!normalizedPrimary.isEmpty() && normalizedPrimary.charAt(0) == '/') {
            return normalizedPrimary;
        }

        String normalizedFallback = stripTexturePrefix(fallbackTexture);
        if (!normalizedFallback.isEmpty()) {
            return normalizedFallback;
        }

        return "";
    }

    private static String stripTexturePrefix(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            return "";
        }
        String normalized = texturePath;
        while (normalized.startsWith("%clamp%") || normalized.startsWith("%blur%")) {
            if (normalized.startsWith("%clamp%")) {
                normalized = normalized.substring(7);
            } else if (normalized.startsWith("%blur%")) {
                normalized = normalized.substring(6);
            }
        }
        return normalized;
    }

    private static String makeFirstPersonShadowTextureAlias(String texturePath) {
        String normalized = stripTexturePrefix(texturePath);
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.startsWith(FIRST_PERSON_PLAYER_SHADOW_TEXTURE_ALIAS_PREFIX)) {
            return normalized;
        }
        if (normalized.charAt(0) == '/') {
            normalized = normalized.substring(1);
        }
        return FIRST_PERSON_PLAYER_SHADOW_TEXTURE_ALIAS_PREFIX + normalized;
    }

    private static String activeCaptureTexture() {
        if (dynamicEntityActive && !activeDynamicEntityTexture.isEmpty()) {
            return activeDynamicEntityTexture;
        }
        if (firstPersonActive && !activeFirstPersonTexture.isEmpty()) {
            return activeFirstPersonTexture;
        }
        return "";
    }

    private static int stableTileEntityId(int x, int y, int z, int salt) {
        int hash = salt;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        return TILE_ENTITY_ID_NAMESPACE | (hash & 0x3FFFFFFF);
    }

    private static int allocateDynamicBoneIndex() {
        if (nextDynamicBoneIndex >= MAX_DYNAMIC_BONES) {
            if (!loggedDynamicEntityBoneOverflow) {
                loggedDynamicEntityBoneOverflow = true;
                System.err.println("[mcrtx] dynamic capture exceeded Remix bone limit; skipping excess dynamic geometry");
            }
            return -1;
        }

        int boneIndex = nextDynamicBoneIndex;
        nextDynamicBoneIndex += 1;
        return boneIndex;
    }

    private static void submitDynamicBoneTransform(int boneIndex, float[] columnMajorMatrix) {
        MinecraftRenderHooks.setDynamicEntityBoneTransform(
                boneIndex,
                columnMajorMatrix[0], columnMajorMatrix[4], columnMajorMatrix[8], columnMajorMatrix[12],
                columnMajorMatrix[1], columnMajorMatrix[5], columnMajorMatrix[9], columnMajorMatrix[13],
                columnMajorMatrix[2], columnMajorMatrix[6], columnMajorMatrix[10], columnMajorMatrix[14]);
    }

    private static float[] buildCameraTranslationMatrix() {
        return new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                RemixCameraState.cameraPositionX,
                RemixCameraState.cameraPositionY,
                RemixCameraState.cameraPositionZ,
                1.0f
        };
    }
}
