import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.MatrixMath;
import mcrtx.lwjglshim.OpenGlCompat;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class RemixDynamicEntityCapture {
    private static final int MAX_HURT_STAGE = 10;
    private static final int MAX_CREEPER_FUSE_STAGE = 10;
    private static final int MAX_DYNAMIC_BONES = 256;
    private static final int FIRST_PERSON_DYNAMIC_ENTITY_ID = Integer.MAX_VALUE - 1;
    private static final int FIRST_PERSON_PLAYER_SHADOW_ENTITY_ID = Integer.MAX_VALUE - 2;
    private static final int TILE_ENTITY_ID_NAMESPACE = 0x40000000;
    private static final int NO_HELD_ITEM = -1;
    private static final int TORCH_BLOCK_ID = 50;
    private static final int REDSTONE_TORCH_OFF_BLOCK_ID = 75;
    private static final int REDSTONE_TORCH_ON_BLOCK_ID = 76;
    private static final int FIRE_PRIMARY_TERRAIN_TILE_INDEX = 31;
    private static final int FIRE_ALTERNATE_TERRAIN_TILE_INDEX = 47;
    private static final int FIRE_ANIMATION_FRAME_COUNT = 16;
    private static final long FIRE_ANIMATION_FRAME_INTERVAL_MILLISECONDS = 50L;
    private static final int TERRAIN_ATLAS_TILE_COLUMNS = 16;
    private static final float TERRAIN_ATLAS_TILE_SPAN = 1.0f / TERRAIN_ATLAS_TILE_COLUMNS;
    private static final float ENTITY_FIRE_OVERLAY_ATLAS_ROW_COUNT = 2.0f;
    private static final float ENTITY_HELD_TORCH_RIGHT_NUDGE = 0.18f;
    private static final float FONT_GLYPH_SIZE = 7.99f;
    private static final float FONT_ATLAS_SIZE = 128.0f;
    private static final float FONT_GLYPH_TEXEL_SIZE = 8.0f;
    private static final float SIGN_TEXT_DEPTH_OFFSET = -0.30f;
    private static final int QUAD_VERTEX_COUNT = 4;
    private static final int QUAD_STRIDE_FLOATS = 20;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final String FIRST_PERSON_PLAYER_SHADOW_TEXTURE_ALIAS_PREFIX = "/mcrtx_alias/firstperson_shadow/";
    private static final String ENTITY_FIRE_OVERLAY_TEXTURE_ALIAS_PREFIX = "/mcrtx_alias/entity_fire_overlay/";
    private static final String ENTITY_FIRE_OVERLAY_TEXTURE_PATH = "/mcrtx_alias/entity_fire_overlay/terrain.png";
    private static final String FONT_TEXTURE_PATH = "/font/default.png";
    private static final String SIGN_TEXT_TEXTURE_PATH = "/mcrtx_alias/sign_text/font/default.png";
    private static final String PAINTING_TEXTURE_PATH = "/art/kz.png";
    private static final String SIGN_TEXTURE_PATH = "/item/sign.png";
    private static final String TERRAIN_TEXTURE_PATH = "/terrain.png";
    private static final String GUI_ITEMS_TEXTURE_PATH = "/gui/items.png";
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
    private static boolean movingPistonRenderActive;
    private static boolean dynamicEntityActive;
    private static boolean pickupParticleEntityRenderActive;
    private static boolean entityFireOverlayActive;
    private static int activeDynamicEntityId = -1;
    private static int activeDynamicEntityHurtStage;
    private static int activeDynamicEntityCreeperFuseStage;
    private static float activeDynamicEntityCreeperFuseProgress;
    private static String activeDynamicEntityTexture = "";
    private static Class<?> livingEntityBaseClass;
    private static Class<?> creeperEntityClass;
    private static Field livingEntityHurtTimeField;
    private static Field creeperFuseTimeField;
    private static Field creeperPreviousFuseTimeField;
    private static Field modelPartPolygonsField;
    private static Field fontCharacterWidthsField;
    private static int nextDynamicBoneIndex;
    private static boolean firstPersonActive;
    private static String activeFirstPersonTexture = "";
    private static boolean firstPersonShadowCaptureActive = false;
    private static boolean firstPersonShadowCaptureAvailable = true;
    private static volatile boolean playerShadowsEnabled = true;
    private static volatile boolean heldTorchLightsEnabled = true;
    private static volatile boolean dynamicEntityRenderingEnabled = true;
    private static volatile boolean livingEntityRenderingEnabled = true;
    private static volatile boolean itemEntityRenderingEnabled = true;
    private static volatile boolean signCaptureEnabled = true;
    private static volatile boolean signTextCaptureEnabled = true;
    private static boolean loggedDynamicEntityHookFailure;
    private static boolean loggedDynamicEntityBoneOverflow;
    private static boolean loggedFirstPersonShadowCaptureFailure;

    private static boolean voxelsGeneratedForCurrentItem = false;
    private static final java.util.Map<Integer, boolean[]> textureAlphaCache = new java.util.HashMap<Integer, boolean[]>();
    private static final java.util.Map<Integer, Integer> textureWidthCache = new java.util.HashMap<Integer, Integer>();
    private static final java.util.Map<Integer, Integer> textureHeightCache = new java.util.HashMap<Integer, Integer>();
    private static final java.util.Map<ps, CachedModelPartMesh> signModelMeshCache = new java.util.IdentityHashMap<ps, CachedModelPartMesh>();
    private static java.nio.ByteBuffer textureReadBuffer = null;
    private static int cachedFontTextureId = -1;

    private static final class CachedModelPartMesh {
        private final float[] quadData;
        private final int quadCount;

        private CachedModelPartMesh(float[] quadData, int quadCount) {
            this.quadData = quadData;
            this.quadCount = quadCount;
        }
    }

    private static float[] captureModelViewMatrix() {
        MODEL_VIEW_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER)) {
            return null;
        }
        float[] modelView = new float[16];
        MODEL_VIEW_BUFFER.get(modelView);
        return modelView;
    }

    private static float[] captureCurrentColor() {
        COLOR_BUFFER.clear();
        if (!OpenGlCompat.getFloat(GL_CURRENT_COLOR, COLOR_BUFFER)) {
            return null;
        }
        float[] currentColor = new float[4];
        COLOR_BUFFER.get(currentColor);
        return currentColor;
    }

    private static boolean[] getTextureAlphaMap(int textureId) {
        boolean[] cached = textureAlphaCache.get(textureId);
        if (cached != null) return cached;

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        if (width <= 0 || height <= 0) {
            boolean[] empty = new boolean[0];
            textureAlphaCache.put(textureId, empty);
            return empty;
        }

        int capacity = width * height * 4;
        if (textureReadBuffer == null || textureReadBuffer.capacity() < capacity) {
            textureReadBuffer = org.lwjgl.BufferUtils.createByteBuffer(capacity);
        } else {
            textureReadBuffer.clear();
        }

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureReadBuffer);

        boolean[] alphaMap = new boolean[width * height];
        for (int i = 0; i < width * height; i++) {
            byte a = textureReadBuffer.get(i * 4 + 3);
            alphaMap[i] = (a & 0xFF) > 128;
        }

        textureAlphaCache.put(textureId, alphaMap);
        textureWidthCache.put(textureId, width);
        textureHeightCache.put(textureId, height);
        return alphaMap;
    }

    private static boolean isPixelOpaque(boolean[] alphaMap, int width, int height, int logicalX, int logicalY) {
        if (logicalX < 0 || logicalX >= 256 || logicalY < 0 || logicalY >= 256) return false;
        if (alphaMap.length == 0) return true; // Fallback

        // Map the 256x256 logical coordinate to the actual texture resolution
        int px = (logicalX * width) / 256;
        int py = (logicalY * height) / 256;
        return alphaMap[py * width + px];
    }

    private static boolean isTexturePixelOpaque(boolean[] alphaMap, int width, int height, int pixelX, int pixelY) {
        if (pixelX < 0 || pixelX >= width || pixelY < 0 || pixelY >= height) {
            return false;
        }
        if (alphaMap.length == 0) {
            return true;
        }
        return alphaMap[pixelY * width + pixelX];
    }

    private static int resolveSignTextTextureId() {
        final int boundTextureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        if (boundTextureId > 0) {
            boolean[] alphaMap = getTextureAlphaMap(boundTextureId);
            Integer textureWidth = textureWidthCache.get(boundTextureId);
            Integer textureHeight = textureHeightCache.get(boundTextureId);
            if (textureWidth != null
                    && textureHeight != null
                    && textureWidth.intValue() == 128
                    && textureHeight.intValue() == 128
                    && alphaMap.length != 0) {
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
            boolean[] alphaMap,
            int textureWidth,
            int textureHeight) {
        final float pixelSpan = FONT_GLYPH_SIZE / FONT_GLYPH_TEXEL_SIZE;
        final int atlasPixelBaseX = Math.round(atlasX);
        final int atlasPixelBaseY = Math.round(atlasY);

        for (int pixelY = 0; pixelY < 8; pixelY++) {
            for (int pixelX = 0; pixelX < 8; pixelX++) {
                if (!isTexturePixelOpaque(alphaMap, textureWidth, textureHeight, atlasPixelBaseX + pixelX, atlasPixelBaseY + pixelY)) {
                    continue;
                }

                final float quadMinX = glyphMinX + pixelX * pixelSpan;
                final float quadMaxX = quadMinX + pixelSpan;
                final float quadMinY = glyphMinY + pixelY * pixelSpan;
                final float quadMaxY = quadMinY + pixelSpan;
                final float centerU = (atlasPixelBaseX + pixelX + 0.5f) / FONT_ATLAS_SIZE;
                final float centerV = (atlasPixelBaseY + pixelY + 0.5f) / FONT_ATLAS_SIZE;

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        quadMinX, quadMaxY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        quadMaxX, quadMaxY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        quadMaxX, quadMinY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        quadMinX, quadMinY, SIGN_TEXT_DEPTH_OFFSET, centerU, centerV,
                        colorRgba,
                        false,
                        boneIndex);
            }
        }
    }

    private static void generateVoxelMesh(int[] rawVertexData, boolean hasColor, int fallbackColorRgba, int boneIndex) {
        int textureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        boolean[] alphaMap = getTextureAlphaMap(textureId);
        Integer tWidthObj = textureWidthCache.get(textureId);
        Integer tHeightObj = textureHeightCache.get(textureId);
        if (tWidthObj == null || tHeightObj == null || alphaMap.length == 0) return;
        int textureWidth = tWidthObj;
        int textureHeight = tHeightObj;

        float u0 = Float.intBitsToFloat(rawVertexData[3]);
        float v2 = Float.intBitsToFloat(rawVertexData[2 * 8 + 4]);

        int logicalXBase = Math.round(u0 * 256.0f) - 15;
        int logicalYBase = Math.round(v2 * 256.0f);

        int quadColor = hasColor ? ColorMath.sanitizePackedColor(ColorMath.unpackTessellatorColor(rawVertexData, 5)) : fallbackColorRgba;

        for (int lx = 0; lx < 16; lx++) {
            for (int ly = 0; ly < 16; ly++) {
                if (!isPixelOpaque(alphaMap, textureWidth, textureHeight, logicalXBase + lx, logicalYBase + ly)) {
                    continue;
                }

                boolean leftOpaque = lx > 0 && isPixelOpaque(alphaMap, textureWidth, textureHeight, logicalXBase + lx - 1, logicalYBase + ly);
                boolean rightOpaque = lx < 15 && isPixelOpaque(alphaMap, textureWidth, textureHeight, logicalXBase + lx + 1, logicalYBase + ly);
                boolean topOpaque = ly > 0 && isPixelOpaque(alphaMap, textureWidth, textureHeight, logicalXBase + lx, logicalYBase + ly - 1);
                boolean bottomOpaque = ly < 15 && isPixelOpaque(alphaMap, textureWidth, textureHeight, logicalXBase + lx, logicalYBase + ly + 1);

                float pX0 = 1.0f - ((lx + 1) / 16.0f); // X- (Geometry Left)
                float pX1 = 1.0f - (lx / 16.0f);       // X+ (Geometry Right)
                float pY0 = 1.0f - ((ly + 1) / 16.0f); // Y- (Geometry Bottom)
                float pY1 = 1.0f - (ly / 16.0f);       // Y+ (Geometry Top)
                float zF = 0.0f;
                float zB = -0.0625f;

                float uC = (logicalXBase + lx + 0.5f) / 256.0f;
                float vC = (logicalYBase + ly + 0.5f) / 256.0f;
                
                float uLeft = (logicalXBase + lx) / 256.0f;
                float uRight = (logicalXBase + lx + 1) / 256.0f;
                float vTop = (logicalYBase + ly) / 256.0f;
                float vBottom = (logicalYBase + ly + 1) / 256.0f;

                // Geometry Right (X+), corresponds to Texture Left (!leftOpaque)
                // U is constant (uC). V goes from vTop to vBottom. pY1 is top, pY0 is bottom.
                if (!leftOpaque) {
                    MinecraftRenderHooks.captureDynamicEntityQuad(
                        pX1, pY1, zB, uC, vTop,
                        pX1, pY1, zF, uC, vTop,
                        pX1, pY0, zF, uC, vBottom,
                        pX1, pY0, zB, uC, vBottom,
                        quadColor, boneIndex
                    );
                }
                // Geometry Left (X-), corresponds to Texture Right (!rightOpaque)
                if (!rightOpaque) {
                    MinecraftRenderHooks.captureDynamicEntityQuad(
                        pX0, pY0, zB, uC, vBottom,
                        pX0, pY0, zF, uC, vBottom,
                        pX0, pY1, zF, uC, vTop,
                        pX0, pY1, zB, uC, vTop,
                        quadColor, boneIndex
                    );
                }
                // Geometry Top (Y+), corresponds to Texture Top (!topOpaque)
                // V is constant (vC). U goes from uLeft to uRight.
                // Texture Left is pX1 (uLeft). Texture Right is pX0 (uRight).
                if (!topOpaque) {
                    MinecraftRenderHooks.captureDynamicEntityQuad(
                        pX0, pY1, zF, uRight, vC,
                        pX1, pY1, zF, uLeft, vC,
                        pX1, pY1, zB, uLeft, vC,
                        pX0, pY1, zB, uRight, vC,
                        quadColor, boneIndex
                    );
                }
                // Geometry Bottom (Y-), corresponds to Texture Bottom (!bottomOpaque)
                if (!bottomOpaque) {
                    MinecraftRenderHooks.captureDynamicEntityQuad(
                        pX1, pY0, zF, uLeft, vC,
                        pX0, pY0, zF, uRight, vC,
                        pX0, pY0, zB, uRight, vC,
                        pX1, pY0, zB, uLeft, vC,
                        quadColor, boneIndex
                    );
                }
            }
        }
    }

    private RemixDynamicEntityCapture() {
    }

    private static int clampHurtStage(int hurtStage) {
        return Math.max(0, Math.min(MAX_HURT_STAGE, hurtStage));
    }

    private static int clampCreeperFuseStage(int fuseStage) {
        return Math.max(0, Math.min(MAX_CREEPER_FUSE_STAGE, fuseStage));
    }

    private static Class<?> resolveLivingEntityBaseClass(sn entity) {
        if (entity == null) {
            return null;
        }

        Class<?> cachedBaseClass = livingEntityBaseClass;
        if (cachedBaseClass != null && cachedBaseClass.isInstance(entity)) {
            return cachedBaseClass;
        }

        Class<?> type = entity.getClass();
        while (type != null) {
            if ("ls".equals(type.getName())) {
                livingEntityBaseClass = type;
                return type;
            }
            type = type.getSuperclass();
        }

        return null;
    }

    private static boolean isTrackedLivingEntity(sn entity) {
        Class<?> baseClass = resolveLivingEntityBaseClass(entity);
        return baseClass != null && baseClass.isInstance(entity);
    }

    private static int resolveLivingEntityHurtStage(sn entity) {
        if (entity == null) {
            return 0;
        }

        Class<?> baseClass = resolveLivingEntityBaseClass(entity);
        if (baseClass == null || !baseClass.isInstance(entity)) {
            return 0;
        }

        try {
            Field hurtTimeField = livingEntityHurtTimeField;
            if (hurtTimeField != null && hurtTimeField.getDeclaringClass().isInstance(entity)) {
                return clampHurtStage(hurtTimeField.getInt(entity));
            }

            hurtTimeField = null;
            Class<?> type = entity.getClass();
            while (type != null) {
                try {
                    Field candidateField = type.getDeclaredField("aa");
                    if (candidateField.getType() != Integer.TYPE) {
                        type = type.getSuperclass();
                        continue;
                    }

                    candidateField.setAccessible(true);
                    hurtTimeField = candidateField;
                    livingEntityHurtTimeField = candidateField;
                    break;
                } catch (NoSuchFieldException missingField) {
                    type = type.getSuperclass();
                }
            }

            if (hurtTimeField == null || !hurtTimeField.getDeclaringClass().isInstance(entity)) {
                return 0;
            }

            return clampHurtStage(hurtTimeField.getInt(entity));
        } catch (IllegalAccessException exception) {
            return 0;
        } catch (IllegalArgumentException exception) {
            return 0;
        }
    }

    private static Class<?> resolveCreeperEntityClass(sn entity) {
        if (entity == null) {
            return null;
        }

        Class<?> cachedCreeperClass = creeperEntityClass;
        if (cachedCreeperClass != null && cachedCreeperClass.isInstance(entity)) {
            return cachedCreeperClass;
        }

        Class<?> type = entity.getClass();
        while (type != null) {
            if ("gb".equals(type.getName())) {
                creeperEntityClass = type;
                return type;
            }
            type = type.getSuperclass();
        }

        return null;
    }

    private static boolean isTrackedCreeper(sn entity) {
        Class<?> trackedCreeperClass = resolveCreeperEntityClass(entity);
        return trackedCreeperClass != null && trackedCreeperClass.isInstance(entity);
    }

    private static Field resolveIntField(Class<?> startClass, String fieldName) {
        Class<?> type = startClass;
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                if (field.getType() == Integer.TYPE) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (NoSuchFieldException missingField) {
                // Continue up the hierarchy.
            }
            type = type.getSuperclass();
        }

        return null;
    }

    private static float resolveCreeperFuseProgress(sn entity, float partialTicks) {
        if (entity == null) {
            return 0.0f;
        }

        Class<?> trackedCreeperClass = resolveCreeperEntityClass(entity);
        if (trackedCreeperClass == null || !trackedCreeperClass.isInstance(entity)) {
            return 0.0f;
        }

        try {
            Field currentFuseField = creeperFuseTimeField;
            if (currentFuseField == null || !currentFuseField.getDeclaringClass().isInstance(entity)) {
                currentFuseField = resolveIntField(entity.getClass(), "a");
                creeperFuseTimeField = currentFuseField;
            }

            Field previousFuseField = creeperPreviousFuseTimeField;
            if (previousFuseField == null || !previousFuseField.getDeclaringClass().isInstance(entity)) {
                previousFuseField = resolveIntField(entity.getClass(), "b");
                creeperPreviousFuseTimeField = previousFuseField;
            }

            if (currentFuseField == null || previousFuseField == null) {
                return 0.0f;
            }

            float previousFuse = previousFuseField.getInt(entity);
            float currentFuse = currentFuseField.getInt(entity);
            float progress = (previousFuse + ((currentFuse - previousFuse) * partialTicks)) / 28.0f;
            if (progress <= 0.0f) {
                return 0.0f;
            }
            if (progress >= 1.0f) {
                return 1.0f;
            }
            return progress;
        } catch (IllegalAccessException exception) {
            return 0.0f;
        } catch (IllegalArgumentException exception) {
            return 0.0f;
        }
    }

    private static int fuseStageForProgress(float fuseProgress) {
        return clampCreeperFuseStage(Math.round(fuseProgress * MAX_CREEPER_FUSE_STAGE));
    }

    public static void onLivingEntityFrameBegin() {
        if (!canCaptureLivingEntities()) {
            return;
        }

        long beginNanos = System.nanoTime();
        ensureDynamicCaptureFrame();
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.onLivingEntityFrameBegin.ensureFrame",
                System.nanoTime() - beginNanos);
    }

    public static void onLivingEntityRenderStart(sn entity, float partialTicks) {
        if (!canCaptureLivingEntities() || entity == null) {
            return;
        }
        dynamicEntityActive = true;
        activeDynamicEntityId = entity.aD;
        activeDynamicEntityHurtStage = isTrackedLivingEntity(entity) ? resolveLivingEntityHurtStage(entity) : 0;
        activeDynamicEntityCreeperFuseProgress = isTrackedCreeper(entity) ? resolveCreeperFuseProgress(entity, partialTicks) : 0.0f;
        activeDynamicEntityCreeperFuseStage = fuseStageForProgress(activeDynamicEntityCreeperFuseProgress);
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(
                entity.aD,
                activeDynamicEntityHurtStage,
                activeDynamicEntityCreeperFuseStage);
    }

    public static void onLivingEntityRenderEnd() {
        if (!dynamicEntityActive) {
            return;
        }
        MinecraftRenderHooks.endDynamicEntity();
        dynamicEntityActive = false;
        entityFireOverlayActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void onPickupParticleEntityRenderStart(sn entity) {
        pickupParticleEntityRenderActive = false;
    }

    public static void onPickupParticleEntityRenderEnd() {
        pickupParticleEntityRenderActive = false;
    }

    public static void onItemEntityRenderStart(sn entity) {
        if (!canCaptureItemEntities() || entity == null) {
            return;
        }

        ensureDynamicCaptureFrame();
        pickupParticleEntityRenderActive = true;
        dynamicEntityActive = true;
        activeDynamicEntityId = entity.aD;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(entity.aD, 0, 0);
    }

    public static void onItemEntityRenderEnd() {
        if (!pickupParticleEntityRenderActive) {
            return;
        }

        pickupParticleEntityRenderActive = false;
        onLivingEntityRenderEnd();
    }

    public static void onEntityFireOverlayStart(sn entity) {
        if (!canCaptureLivingEntities() || entity == null || !isTrackedLivingEntity(entity)) {
            return;
        }

        ensureDynamicCaptureFrame();
        entityFireOverlayActive = true;
        dynamicEntityActive = true;
        activeDynamicEntityId = entity.aD;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = ENTITY_FIRE_OVERLAY_TEXTURE_PATH;
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(entity.aD, 0, 0);
        MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
    }

    public static void onEntityFireOverlayEnd() {
        if (!entityFireOverlayActive) {
            return;
        }

        entityFireOverlayActive = false;
        onLivingEntityRenderEnd();
    }

    public static void onSignRenderStart(yk sign) {
        if (!canCaptureSigns() || sign == null) {
            return;
        }

        ensureDynamicCaptureFrame();
        signRenderActive = true;
        dynamicEntityActive = true;
        activeDynamicEntityId = stableTileEntityId(sign.e, sign.f, sign.g, 0x5349474E);
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = SIGN_TEXTURE_PATH;
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(
            activeDynamicEntityId,
            activeDynamicEntityHurtStage,
            activeDynamicEntityCreeperFuseStage);
        MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
    }

    public static void onSignRenderEnd() {
        signRenderActive = false;
        onLivingEntityRenderEnd();
    }

    public static void onMovingPistonRenderStart(uk piston) {
        if (!canCaptureDynamicEntities() || piston == null) {
            return;
        }

        ensureDynamicCaptureFrame();
        movingPistonRenderActive = true;
        dynamicEntityActive = true;
        activeDynamicEntityId = stableTileEntityId(piston.e, piston.f, piston.g, 0x50495354);
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = TERRAIN_TEXTURE_PATH;
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(
            activeDynamicEntityId,
            activeDynamicEntityHurtStage,
            activeDynamicEntityCreeperFuseStage);
        MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
    }

    public static void onMovingPistonRenderEnd() {
        movingPistonRenderActive = false;
        onLivingEntityRenderEnd();
    }

    public static boolean shouldSuppressMovingPistonVanillaDraw() {
        return movingPistonRenderActive && dynamicEntityActive;
    }

    public static boolean captureSignModelRender(rf signModel) {
        if (!signRenderActive || !dynamicEntityActive || signModel == null) {
            return false;
        }

        CachedModelPartMesh signBoardMesh = resolveCachedModelPartMesh(signModel.a, 0.0625f);
        CachedModelPartMesh signPostMesh = resolveCachedModelPartMesh(signModel.b, 0.0625f);
        return captureCachedSignModel(signBoardMesh, signPostMesh);
    }

    public static boolean captureSignTextRender(sj fontRenderer, String text, int x, int y, int colorRgba) {
        if (!canCaptureSignText() || !signRenderActive || !dynamicEntityActive || fontRenderer == null) {
            return false;
        }

        int[] characterWidths = resolveFontCharacterWidths(fontRenderer);
        if (characterWidths == null || characterWidths.length == 0) {
            return false;
        }

        onSignTextRender(text, x, y, colorRgba, false, characterWidths);
        return true;
    }

    public static void onPaintingRender(qv painting) {
        capturePaintingRender(painting);
    }

    public static boolean capturePaintingRender(qv painting) {
        if (!canCaptureDynamicEntities() || painting == null || painting.e == null) {
            return false;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return false;
        }

        ensureDynamicCaptureFrame();

        try {
            long renderStartNanos = System.nanoTime();
            float[] modelView = captureModelViewMatrix();
            if (modelView == null) {
                return false;
            }
            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            long stateReadEndNanos = System.nanoTime();

            MinecraftRenderHooks.beginDynamicEntity(painting.aD, 0, 0);
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
            return true;
        } finally {
            MinecraftRenderHooks.endDynamicEntity();
        }
    }

    public static void onSignTextRender(String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        if (!signRenderActive || !dynamicEntityActive || text == null || text.isEmpty() || characterWidths == null || characterWidths.length == 0) {
            return;
        }
        if (shadow) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        try {
            if (!SIGN_TEXT_TEXTURE_PATH.equals(activeDynamicEntityTexture)) {
                activeDynamicEntityTexture = SIGN_TEXT_TEXTURE_PATH;
                MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
            }

                int textureId = resolveSignTextTextureId();
            boolean[] alphaMap = getTextureAlphaMap(textureId);
            Integer textureWidthBoxed = textureWidthCache.get(textureId);
            Integer textureHeightBoxed = textureHeightCache.get(textureId);
            boolean canCapturePerPixel = textureWidthBoxed != null
                    && textureHeightBoxed != null
                    && textureWidthBoxed.intValue() == 128
                    && textureHeightBoxed.intValue() == 128
                    && alphaMap.length != 0;
            int textureWidth = canCapturePerPixel ? textureWidthBoxed.intValue() : 0;
            int textureHeight = canCapturePerPixel ? textureHeightBoxed.intValue() : 0;

            float[] modelView = captureModelViewMatrix();
            if (modelView == null) {
                return;
            }

            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            int sanitizedColor = ColorMath.forceOpaqueAlpha(ColorMath.sanitizePackedColor(colorRgba));
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

                if (canCapturePerPixel) {
                    captureSignGlyph(
                        glyphMinX,
                        glyphMinY,
                        atlasX,
                        atlasY,
                        sanitizedColor,
                        boneIndex,
                        alphaMap,
                        textureWidth,
                        textureHeight);
                } else {
                    MinecraftRenderHooks.captureDynamicEntityQuad(
                        glyphMinX, glyphMaxY, SIGN_TEXT_DEPTH_OFFSET, u0, v1,
                        glyphMaxX, glyphMaxY, SIGN_TEXT_DEPTH_OFFSET, u1, v1,
                        glyphMaxX, glyphMinY, SIGN_TEXT_DEPTH_OFFSET, u1, v0,
                        glyphMinX, glyphMinY, SIGN_TEXT_DEPTH_OFFSET, u0, v0,
                        sanitizedColor,
                        false,
                        boneIndex);
                }

                cursorX += characterWidths[glyphId];
            }
        } catch (RuntimeException exception) {
            handleHookFailure(exception);
        }
    }

    private static boolean captureSignModelPart(ps modelPart, float scale) {
        tz[] polygons = resolveModelPartPolygons(modelPart);
        if (polygons == null || polygons.length == 0) {
            return false;
        }
        onModelPartRender(polygons, scale);
        return true;
    }

    private static boolean captureCachedSignModel(CachedModelPartMesh firstMesh, CachedModelPartMesh secondMesh) {
        if (firstMesh == null && secondMesh == null) {
            return false;
        }

        if (!SIGN_TEXTURE_PATH.equals(activeDynamicEntityTexture)) {
            activeDynamicEntityTexture = SIGN_TEXTURE_PATH;
            MinecraftRenderHooks.setDynamicEntityTexture(activeDynamicEntityTexture);
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

            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            float[] capturedColor = ColorMath.sanitizeDynamicEntityColor(color[0], color[1], color[2], color[3]);
            capturedColor = ColorMath.applyHurtIndicator(
                    capturedColor[0],
                    capturedColor[1],
                    capturedColor[2],
                    capturedColor[3],
                    activeDynamicEntityHurtStage);
            capturedColor = ColorMath.applyCreeperFuseIndicator(
                    capturedColor[0],
                    capturedColor[1],
                    capturedColor[2],
                    capturedColor[3],
                    activeDynamicEntityCreeperFuseProgress);
            int colorRgba = ColorMath.packColor(capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3]);
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return false;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);
            long setupEndNanos = System.nanoTime();

            boolean emitted = false;
            emitted |= emitCachedModelPartMesh(firstMesh, colorRgba, boneIndex);
            emitted |= emitCachedModelPartMesh(secondMesh, colorRgba, boneIndex);
            long quadEmitEndNanos = System.nanoTime();

            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.captureSignModelRender.readState",
                    stateReadEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.captureSignModelRender.setupTransform",
                    setupEndNanos - stateReadEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.captureSignModelRender.emitQuads",
                    quadEmitEndNanos - setupEndNanos);
            return emitted;
        } catch (RuntimeException exception) {
            handleHookFailure(exception);
            return false;
        }
    }

    private static boolean emitCachedModelPartMesh(CachedModelPartMesh mesh, int colorRgba, int boneIndex) {
        if (mesh == null || mesh.quadCount == 0) {
            return false;
        }

        float[] quadData = mesh.quadData;
        for (int quadIndex = 0; quadIndex < mesh.quadCount; quadIndex++) {
            int base = quadIndex * QUAD_STRIDE_FLOATS;
            MinecraftRenderHooks.captureDynamicEntityQuad(
                    quadData[base], quadData[base + 1], quadData[base + 2], quadData[base + 3], quadData[base + 4],
                    quadData[base + 5], quadData[base + 6], quadData[base + 7], quadData[base + 8], quadData[base + 9],
                    quadData[base + 10], quadData[base + 11], quadData[base + 12], quadData[base + 13], quadData[base + 14],
                    quadData[base + 15], quadData[base + 16], quadData[base + 17], quadData[base + 18], quadData[base + 19],
                    colorRgba,
                    boneIndex);
        }

        return true;
    }

    private static CachedModelPartMesh resolveCachedModelPartMesh(ps modelPart, float scale) {
        if (modelPart == null) {
            return null;
        }

        CachedModelPartMesh cachedMesh = signModelMeshCache.get(modelPart);
        if (cachedMesh != null) {
            return cachedMesh;
        }

        tz[] polygons = resolveModelPartPolygons(modelPart);
        CachedModelPartMesh builtMesh = buildCachedModelPartMesh(polygons, scale);
        if (builtMesh != null) {
            signModelMeshCache.put(modelPart, builtMesh);
        }
        return builtMesh;
    }

    private static CachedModelPartMesh buildCachedModelPartMesh(tz[] polygons, float scale) {
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

        return new CachedModelPartMesh(quadData, quadCount);
    }

    private static boolean isRenderableQuad(tz polygon) {
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
            handleHookFailure(new RuntimeException("Failed to access sign model polygons", exception));
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
            handleHookFailure(new RuntimeException("Failed to access sign font widths", exception));
            return null;
        }
    }

    public static void onFirstPersonRenderStart() {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        ensureDynamicCaptureFrame();
        MinecraftRenderHooks.setFirstPersonHeldItem(NO_HELD_ITEM);
        if (!dynamicEntityRenderingEnabled) {
            return;
        }

        firstPersonActive = true;
        activeFirstPersonTexture = "/mob/char.png";
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(
            FIRST_PERSON_DYNAMIC_ENTITY_ID,
            activeDynamicEntityHurtStage,
            activeDynamicEntityCreeperFuseStage);
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
    }

    public static void onFirstPersonShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        if (!dynamicEntityRenderingEnabled || !playerShadowsEnabled || !firstPersonShadowCaptureAvailable || !MinecraftRenderHooks.isInitialized() || minecraft == null || !(minecraft.h instanceof gs)) {
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
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = makeFirstPersonShadowTextureAlias("/mob/char.png");
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(
            FIRST_PERSON_PLAYER_SHADOW_ENTITY_ID,
            activeDynamicEntityHurtStage,
            activeDynamicEntityCreeperFuseStage);
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
            float[] overlayModelView = captureModelViewMatrix();
            if (overlayModelView == null) {
                return;
            }
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

    public static void setPlayerShadowsEnabled(boolean enabled) {
        playerShadowsEnabled = enabled;
        if (!enabled) {
            firstPersonShadowCaptureActive = false;
        }
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        heldTorchLightsEnabled = enabled;
    }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) {
        dynamicEntityRenderingEnabled = enabled;
        if (enabled) {
            return;
        }

        dynamicEntityActive = false;
        pickupParticleEntityRenderActive = false;
        entityFireOverlayActive = false;
        signRenderActive = false;
        firstPersonActive = false;
        firstPersonShadowCaptureActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = "";
        activeFirstPersonTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void setLivingEntityRenderingEnabled(boolean enabled) {
        livingEntityRenderingEnabled = enabled;
        if (enabled) {
            return;
        }

        if (!entityFireOverlayActive && !dynamicEntityActive) {
            return;
        }

        entityFireOverlayActive = false;
        dynamicEntityActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void setItemEntityRenderingEnabled(boolean enabled) {
        itemEntityRenderingEnabled = enabled;
        if (enabled || !pickupParticleEntityRenderActive) {
            return;
        }

        pickupParticleEntityRenderActive = false;
        dynamicEntityActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void setSignCaptureEnabled(boolean enabled) {
        signCaptureEnabled = enabled;
        if (enabled || !signRenderActive) {
            return;
        }

        signRenderActive = false;
        dynamicEntityActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityHurtStage = 0;
        activeDynamicEntityCreeperFuseStage = 0;
        activeDynamicEntityCreeperFuseProgress = 0.0f;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void setSignTextCaptureEnabled(boolean enabled) {
        signTextCaptureEnabled = enabled;
    }

    public static void onFramePresented() {
        dynamicCaptureFrameActive = false;
        signRenderActive = false;
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!firstPersonActive) {
            MinecraftRenderHooks.setFirstPersonHeldItem(
                    heldTorchLightsEnabled && isTorchLikeHeldItem(itemStack.c) ? itemStack.c : NO_HELD_ITEM);
            return;
        }

        activeFirstPersonTexture = texturePathForItem(itemStack);
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
        MinecraftRenderHooks.setFirstPersonHeldItem(
                heldTorchLightsEnabled && isTorchLikeHeldItem(itemStack.c) ? itemStack.c : NO_HELD_ITEM);
    }

    public static void onPlayerEquippedItemRenderStart(gs player, iz itemStack, float partialTicks) {
        if (!MinecraftRenderHooks.isInitialized() || player == null) {
            return;
        }

        syncEntityHeldTorch(player, itemStack, partialTicks);
        if (!dynamicEntityActive || itemStack == null) {
            return;
        }

        onEntityTextureBind(texturePathForItem(itemStack), null);
    }

    public static void onLivingEquippedItemRenderStart(ls entity, iz itemStack) {
        if (!MinecraftRenderHooks.isInitialized() || entity == null || itemStack == null) {
            return;
        }

        if (!dynamicEntityActive) {
            return;
        }

        onEntityTextureBind(texturePathForItem(itemStack), null);
    }

    private static boolean isTorchLikeHeldItem(int itemId) {
        return itemId == TORCH_BLOCK_ID
                || itemId == REDSTONE_TORCH_ON_BLOCK_ID
                || itemId == REDSTONE_TORCH_OFF_BLOCK_ID;
    }

    private static String texturePathForItem(iz itemStack) {
        return itemStack.c < 256 ? TERRAIN_TEXTURE_PATH : GUI_ITEMS_TEXTURE_PATH;
    }

    private static void syncEntityHeldTorch(gs player, iz heldItem, float partialTicks) {
        if (firstPersonShadowCaptureActive) {
            return;
        }

        if (!heldTorchLightsEnabled) {
            MinecraftRenderHooks.setEntityHeldTorch(player.aD, 0.0f, 0.0f, 0.0f, NO_HELD_ITEM);
            return;
        }

        int itemId = heldItem != null && isTorchLikeHeldItem(heldItem.c) ? heldItem.c : NO_HELD_ITEM;
        if (itemId == NO_HELD_ITEM) {
            MinecraftRenderHooks.setEntityHeldTorch(player.aD, 0.0f, 0.0f, 0.0f, NO_HELD_ITEM);
            return;
        }

        float[] modelView = captureModelViewMatrix();
        if (modelView == null) {
            return;
        }

        float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
        float[] handPosition = MatrixMath.transformPointColumnMajor(modelToWorld, 0.0f, 0.0f, 0.0f);
        float interpolatedYaw = player.aU + (player.aS - player.aU) * partialTicks;
        double yawRadians = Math.toRadians(interpolatedYaw);
        handPosition[0] += (float) (-Math.cos(yawRadians)) * ENTITY_HELD_TORCH_RIGHT_NUDGE;
        handPosition[2] += (float) (-Math.sin(yawRadians)) * ENTITY_HELD_TORCH_RIGHT_NUDGE;
        MinecraftRenderHooks.setEntityHeldTorch(player.aD, handPosition[0], handPosition[1], handPosition[2], itemId);
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        if (!dynamicEntityActive) {
            return;
        }
        String resolvedTexture = normalizeDynamicTexturePath(primaryTexture, fallbackTexture);
        if (entityFireOverlayActive) {
            resolvedTexture = makeEntityFireOverlayTextureAlias(resolvedTexture.isEmpty() ? TERRAIN_TEXTURE_PATH : resolvedTexture);
        } else if (firstPersonShadowCaptureActive) {
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
            float[] modelView = captureModelViewMatrix();
            if (modelView == null) {
                return;
            }

            float[] color = captureCurrentColor();
            if (color == null) {
                return;
            }
            long stateReadEndNanos = System.nanoTime();

            float[] modelToWorld;
            if (firstPersonShadowCaptureActive) {
                float[] overlayNeutralModelView = MatrixMath.multiplyColumnMajor(firstPersonShadowOverlayInverse, modelView);
                modelToWorld = MatrixMath.multiplyColumnMajor(buildCameraTranslationMatrix(), overlayNeutralModelView);
            } else {
                modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            }
                float[] capturedColor = ColorMath.sanitizeDynamicEntityColor(color[0], color[1], color[2], color[3]);
                capturedColor = ColorMath.applyHurtIndicator(
                    capturedColor[0],
                    capturedColor[1],
                    capturedColor[2],
                    capturedColor[3],
                    activeDynamicEntityHurtStage);
                capturedColor = ColorMath.applyCreeperFuseIndicator(
                    capturedColor[0],
                    capturedColor[1],
                    capturedColor[2],
                    capturedColor[3],
                    activeDynamicEntityCreeperFuseProgress);
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
        
        if (firstPersonActive && vertexCount == 6) {
            voxelsGeneratedForCurrentItem = false;
        }
        
        if (firstPersonActive && vertexCount == 96) {
            if (!voxelsGeneratedForCurrentItem) {
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

                    float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
                    int fallbackColorRgba = ColorMath.sanitizePackedColor(ColorMath.packColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]));
                    int boneIndex = allocateDynamicBoneIndex();
                    if (boneIndex >= 0) {
                        submitDynamicBoneTransform(boneIndex, modelToWorld);
                        generateVoxelMesh(rawVertexData, hasColor, fallbackColorRgba, boneIndex);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                voxelsGeneratedForCurrentItem = true;
            }
            return; // Drop vanilla side strips
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
                float p0u = Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 3]);
                float p0v = Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 4]);
                float p1x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8]);
                float p1y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 1]);
                float p1z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 2]);
                float p1u = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 3]);
                float p1v = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 4]);
                float p2x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8]);
                float p2y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 1]);
                float p2z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 2]);
                float p2u = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 3]);
                float p2v = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 4]);
                float p3x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8]);
                float p3y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 1]);
                float p3z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 2]);
                float p3u = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 3]);
                float p3v = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 4]);

                if (entityFireOverlayActive) {
                    float[] uv0 = remapEntityFireOverlayUv(p0u, p0v);
                    float[] uv1 = remapEntityFireOverlayUv(p1u, p1v);
                    float[] uv2 = remapEntityFireOverlayUv(p2u, p2v);
                    float[] uv3 = remapEntityFireOverlayUv(p3u, p3v);
                    p0u = uv0[0];
                    p0v = uv0[1];
                    p1u = uv1[0];
                    p1v = uv1[1];
                    p2u = uv2[0];
                    p2v = uv2[1];
                    p3u = uv3[0];
                    p3v = uv3[1];
                }

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        p0x, p0y, p0z, p0u, p0v,
                        p1x, p1y, p1z, p1u, p1v,
                        p2x, p2y, p2z, p2u, p2v,
                        p3x, p3y, p3z, p3u, p3v,
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
        entityFireOverlayActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityTexture = "";
        pickupParticleEntityRenderActive = false;
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
        MinecraftRenderHooks.beginBlockOutlineFrame();
        dynamicCaptureFrameActive = true;
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.dynamicEntity.ensureFrame.beginFrame",
                System.nanoTime() - beginFrameStartNanos);
    }

    private static boolean canCaptureDynamicEntities() {
        return dynamicEntityRenderingEnabled && MinecraftRenderHooks.isInitialized();
    }

    private static boolean canCaptureLivingEntities() {
        return livingEntityRenderingEnabled && canCaptureDynamicEntities();
    }

    private static boolean canCaptureItemEntities() {
        return itemEntityRenderingEnabled && canCaptureDynamicEntities();
    }

    private static boolean canCaptureSigns() {
        return signCaptureEnabled && canCaptureDynamicEntities();
    }

    private static boolean canCaptureSignText() {
        return signTextCaptureEnabled && canCaptureSigns();
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

    private static String makeEntityFireOverlayTextureAlias(String texturePath) {
        String normalized = stripTexturePrefix(texturePath);
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.startsWith(ENTITY_FIRE_OVERLAY_TEXTURE_ALIAS_PREFIX)) {
            return normalized;
        }
        if (normalized.charAt(0) == '/') {
            normalized = normalized.substring(1);
        }
        return ENTITY_FIRE_OVERLAY_TEXTURE_ALIAS_PREFIX + normalized;
    }

    private static float[] remapEntityFireOverlayUv(float u, float v) {
        int tileX = clampInt((int) Math.floor(u / TERRAIN_ATLAS_TILE_SPAN), 0, TERRAIN_ATLAS_TILE_COLUMNS - 1);
        int tileY = clampInt((int) Math.floor(v / TERRAIN_ATLAS_TILE_SPAN), 0, TERRAIN_ATLAS_TILE_COLUMNS - 1);
        int terrainTileIndex = tileX + tileY * TERRAIN_ATLAS_TILE_COLUMNS;
        int fireRow = terrainTileIndex == FIRE_ALTERNATE_TERRAIN_TILE_INDEX ? 1 : 0;
        int fireFrame = (int) ((System.currentTimeMillis() / FIRE_ANIMATION_FRAME_INTERVAL_MILLISECONDS) % FIRE_ANIMATION_FRAME_COUNT);

        float tileMinU = tileX * TERRAIN_ATLAS_TILE_SPAN;
        float tileMinV = tileY * TERRAIN_ATLAS_TILE_SPAN;
        float normalizedU = clamp01((u - tileMinU) / TERRAIN_ATLAS_TILE_SPAN);
        float normalizedV = clamp01((v - tileMinV) / TERRAIN_ATLAS_TILE_SPAN);
        return new float[] {
                (fireFrame + normalizedU) / FIRE_ANIMATION_FRAME_COUNT,
                (fireRow + normalizedV) / ENTITY_FIRE_OVERLAY_ATLAS_ROW_COUNT
        };
    }

    private static int clampInt(int value, int minValue, int maxValue) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
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
