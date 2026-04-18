import java.nio.FloatBuffer;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.ColorMath;
import mcrtx.bridge.MatrixMath;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class RemixParticleCapture {
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final int WEATHER_TEXTURE_KIND_NONE = -1;
    private static final int WEATHER_TEXTURE_KIND_RAIN = 4;
    private static final String RAIN_TEXTURE_PATH = "/environment/rain.png";
    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);

    private static int activeWeatherTextureKind = WEATHER_TEXTURE_KIND_NONE;

    private RemixParticleCapture() {
    }

    public static void onFrameRenderStart() {
        activeWeatherTextureKind = WEATHER_TEXTURE_KIND_NONE;
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }
        MinecraftRenderHooks.beginParticleFrame();
    }

    public static void onParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        if (!MinecraftRenderHooks.isInitialized() || particle == null) {
            return;
        }

        int textureKind = particle.c_();
        if (textureKind < 0 || textureKind > 3) {
            return;
        }

        float minU = (float) (particle.b % 16) / 16.0f;
        float maxU = minU + 0.0624375f;
        float minV = (float) (particle.b / 16) / 16.0f;
        float maxV = minV + 0.0624375f;
        float particleScale = 0.1f * particle.g;
        float originX = (float) (particle.aJ + (particle.aM - particle.aJ) * (double) partialTicks);
        float originY = (float) (particle.aK + (particle.aN - particle.aK) * (double) partialTicks);
        float originZ = (float) (particle.aL + (particle.aO - particle.aL) * (double) partialTicks);
        float brightness = particle.a(partialTicks);
        int colorRgba = ColorMath.packColor(particle.i * brightness, particle.j * brightness, particle.k * brightness, 1.0f);

        MinecraftRenderHooks.captureParticleQuad(
                originX - f3 * particleScale - f6 * particleScale,
                originY - f4 * particleScale,
                originZ - f5 * particleScale - f7 * particleScale,
                maxU,
                maxV,
                originX - f3 * particleScale + f6 * particleScale,
                originY + f4 * particleScale,
                originZ - f5 * particleScale + f7 * particleScale,
                maxU,
                minV,
                originX + f3 * particleScale + f6 * particleScale,
                originY + f4 * particleScale,
                originZ + f5 * particleScale + f7 * particleScale,
                minU,
                minV,
                originX + f3 * particleScale - f6 * particleScale,
                originY - f4 * particleScale,
                originZ + f5 * particleScale - f7 * particleScale,
                minU,
                maxV,
                colorRgba,
                textureKind);
    }

    public static void onWeatherTextureBind(String texturePath) {
        if (!MinecraftRenderHooks.isInitialized()) {
            activeWeatherTextureKind = WEATHER_TEXTURE_KIND_NONE;
            return;
        }
        activeWeatherTextureKind = RAIN_TEXTURE_PATH.equals(texturePath)
                ? WEATHER_TEXTURE_KIND_RAIN
                : WEATHER_TEXTURE_KIND_NONE;
    }

    public static void onWeatherRenderEnd() {
        activeWeatherTextureKind = WEATHER_TEXTURE_KIND_NONE;
    }

    public static void onTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        if (activeWeatherTextureKind == WEATHER_TEXTURE_KIND_NONE) {
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

        MODEL_VIEW_BUFFER.clear();
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
        float[] modelView = new float[16];
        MODEL_VIEW_BUFFER.get(modelView);

        COLOR_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, COLOR_BUFFER);
        float[] currentColor = new float[4];
        COLOR_BUFFER.get(currentColor);

        float[] modelToWorld = MatrixMath.multiplyColumnMajor(RemixCameraState.buildInverseViewMatrix(), modelView);
        int fallbackColorRgba = ColorMath.sanitizePackedColor(
                ColorMath.packColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]));

        for (int vertexIndex = 0; vertexIndex + 5 < vertexCount; vertexIndex += 6) {
            int quadColor = hasColor
                    ? ColorMath.sanitizePackedColor(ColorMath.unpackTessellatorColor(rawVertexData, vertexIndex * 8 + 5))
                    : fallbackColorRgba;

            float[] p0 = MatrixMath.transformPointColumnMajor(
                    modelToWorld,
                    Float.intBitsToFloat(rawVertexData[vertexIndex * 8]),
                    Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 1]),
                    Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 2]));
            float[] p1 = MatrixMath.transformPointColumnMajor(
                    modelToWorld,
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8]),
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 1]),
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 2]));
            float[] p2 = MatrixMath.transformPointColumnMajor(
                    modelToWorld,
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8]),
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 1]),
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 2]));
            float[] p3 = MatrixMath.transformPointColumnMajor(
                    modelToWorld,
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8]),
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 1]),
                    Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 2]));

                if (!shouldCaptureRainQuad(p0, p1, p2, p3)) {
                continue;
                }

            MinecraftRenderHooks.captureParticleQuad(
                    p0[0], p0[1], p0[2], Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 3]), Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 4]),
                    p1[0], p1[1], p1[2], Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 4]),
                    p2[0], p2[1], p2[2], Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 4]),
                    p3[0], p3[1], p3[2], Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 4]),
                    quadColor,
                    activeWeatherTextureKind);
        }
    }

    private static boolean shouldCaptureRainQuad(float[] p0, float[] p1, float[] p2, float[] p3) {
        if (activeWeatherTextureKind != WEATHER_TEXTURE_KIND_RAIN) {
            return true;
        }

        float centerX = (p0[0] + p1[0] + p2[0] + p3[0]) * 0.25f;
        float centerZ = (p0[2] + p1[2] + p2[2] + p3[2]) * 0.25f;
        int rainColumnX = (int) Math.floor(centerX);
        int rainColumnZ = (int) Math.floor(centerZ);
        int selector = rainColumnX * 73428767 ^ rainColumnZ * 912931;
        boolean keepEastWestSheet = Math.floorMod(selector, 2) == 0;

        float spanX = Math.max(
                Math.max(Math.abs(p1[0] - p0[0]), Math.abs(p2[0] - p3[0])),
                Math.max(Math.abs(p2[0] - p1[0]), Math.abs(p3[0] - p0[0])));
        float spanZ = Math.max(
                Math.max(Math.abs(p1[2] - p0[2]), Math.abs(p2[2] - p3[2])),
                Math.max(Math.abs(p2[2] - p1[2]), Math.abs(p3[2] - p0[2])));
        boolean isEastWestSheet = spanX >= spanZ;
        return isEastWestSheet == keepEastWestSheet;
    }
}
