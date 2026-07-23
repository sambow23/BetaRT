package mcrtx.bridge;

public final class ColorMath {
    private static final int MAX_HURT_STAGE = 10;
    private static final int MAX_CREEPER_FUSE_STAGE = 10;
    private static final float MAX_CREEPER_FUSE_PROGRESS = 1.0f;

    private ColorMath() {
    }

    public static int packColor(float red, float green, float blue, float alpha) {
        int alphaByte = clampColor(alpha);
        int redByte = clampColor(red);
        int greenByte = clampColor(green);
        int blueByte = clampColor(blue);
        return (alphaByte << 24) | (redByte << 16) | (greenByte << 8) | blueByte;
    }

    public static int clampColor(float value) {
        if (value <= 0.0f) {
            return 0;
        }
        if (value >= 1.0f) {
            return 255;
        }
        return Math.round(value * 255.0f);
    }

    public static int unpackTessellatorColor(int[] rawVertexData, int colorIndex) {
        int packedColor = rawVertexData[colorIndex];
        int red = packedColor & 0xFF;
        int green = (packedColor >> 8) & 0xFF;
        int blue = (packedColor >> 16) & 0xFF;
        int alpha = (packedColor >> 24) & 0xFF;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int sanitizePackedColor(int packedColorRgba) {
        float alpha = ((packedColorRgba >> 24) & 0xFF) / 255.0f;
        float red = ((packedColorRgba >> 16) & 0xFF) / 255.0f;
        float green = ((packedColorRgba >> 8) & 0xFF) / 255.0f;
        float blue = (packedColorRgba & 0xFF) / 255.0f;
        float[] sanitizedColor = sanitizeDynamicEntityColor(red, green, blue, alpha);
        return packColor(sanitizedColor[0], sanitizedColor[1], sanitizedColor[2], sanitizedColor[3]);
    }

    public static int forceOpaqueAlpha(int packedColorRgba) {
        return packedColorRgba | 0xFF000000;
    }

    public static float[] sanitizeDynamicEntityColor(float red, float green, float blue, float alpha) {
        float maxChannelDelta = Math.max(Math.abs(red - green), Math.max(Math.abs(red - blue), Math.abs(green - blue)));
        if (alpha >= 0.999f && maxChannelDelta <= 0.01f) {
            return new float[]{1.0f, 1.0f, 1.0f, alpha};
        }
        return new float[]{red, green, blue, alpha};
    }

    public static float[] applyHurtIndicator(float red, float green, float blue, float alpha, int hurtStage) {
        int clampedStage = Math.max(0, Math.min(MAX_HURT_STAGE, hurtStage));
        if (clampedStage == 0) {
            return new float[]{red, green, blue, alpha};
        }

        float intensity = clampedStage / (float) MAX_HURT_STAGE;
        float tintedRed = red + ((1.0f - red) * intensity);
        float tintedGreen = green * (1.0f - intensity);
        float tintedBlue = blue * (1.0f - intensity);
        return new float[]{tintedRed, tintedGreen, tintedBlue, alpha};
    }

    public static float[] applyCreeperFuseIndicator(float red, float green, float blue, float alpha, float fuseProgress) {
        float clampedProgress = clampUnit(fuseProgress);
        if ((creeperFuseStage(clampedProgress) & 1) == 0) {
            return new float[]{red, green, blue, alpha};
        }

        float whiteBlend = clampUnit(clampedProgress * 0.2f);
        float flashedRed = red + ((1.0f - red) * whiteBlend);
        float flashedGreen = green + ((1.0f - green) * whiteBlend);
        float flashedBlue = blue + ((1.0f - blue) * whiteBlend);
        float flashedAlpha = alpha + ((1.0f - alpha) * whiteBlend);
        return new float[]{flashedRed, flashedGreen, flashedBlue, flashedAlpha};
    }

    public static int creeperFuseStage(float fuseProgress) {
        return (int) (clampUnit(fuseProgress) * MAX_CREEPER_FUSE_STAGE);
    }

    private static float clampUnit(float value) {
        if (value <= 0.0f) {
            return 0.0f;
        }
        if (value >= MAX_CREEPER_FUSE_PROGRESS) {
            return MAX_CREEPER_FUSE_PROGRESS;
        }
        return value;
    }
}
