import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.MatrixMath;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class RemixDynamicEntityCapture {
    private static final int MAX_DYNAMIC_BONES = 256;
    private static final int FIRST_PERSON_DYNAMIC_ENTITY_ID = Integer.MAX_VALUE - 1;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);

    private static boolean dynamicEntityActive;
    private static int activeDynamicEntityId = -1;
    private static String activeDynamicEntityTexture = "";
    private static int nextDynamicBoneIndex;
    private static boolean firstPersonActive;
    private static String activeFirstPersonTexture = "";
    private static boolean loggedDynamicEntityHookFailure;
    private static boolean loggedDynamicEntityBoneOverflow;

    private RemixDynamicEntityCapture() {
    }

    public static void onLivingEntityFrameBegin() {
        MinecraftRenderHooks.beginDynamicEntityFrame();
        MinecraftRenderHooks.beginDestroyOverlayFrame();
        MinecraftRenderHooks.beginParticleFrame();
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

    public static void onFirstPersonRenderStart() {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        firstPersonActive = true;
        activeFirstPersonTexture = "/mob/char.png";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(FIRST_PERSON_DYNAMIC_ENTITY_ID);
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
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
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL_CURRENT_COLOR, COLOR_BUFFER);
            float[] color = new float[4];
            COLOR_BUFFER.get(color);

            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            float[] capturedColor = ColorMath.sanitizeDynamicEntityColor(color[0], color[1], color[2], color[3]);
            int colorRgba = ColorMath.packColor(capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3]);
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);

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
        if (!firstPersonActive || activeFirstPersonTexture.isEmpty()) {
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
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL_CURRENT_COLOR, COLOR_BUFFER);
            float[] currentColor = new float[4];
            COLOR_BUFFER.get(currentColor);

            float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
            int fallbackColorRgba = ColorMath.sanitizePackedColor(ColorMath.packColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]));
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);

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
        firstPersonActive = false;
        activeFirstPersonTexture = "";
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

    private static String activeCaptureTexture() {
        if (dynamicEntityActive && !activeDynamicEntityTexture.isEmpty()) {
            return activeDynamicEntityTexture;
        }
        if (firstPersonActive && !activeFirstPersonTexture.isEmpty()) {
            return activeFirstPersonTexture;
        }
        return "";
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
}
