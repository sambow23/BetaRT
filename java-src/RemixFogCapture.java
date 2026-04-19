import mcrtx.bridge.MinecraftRenderHooks;

public final class RemixFogCapture {
    private static final int D3DFOG_NONE = 0;
    private static final int D3DFOG_EXP = 1;
    private static final int D3DFOG_LINEAR = 3;
    private static final float REMIX_LINEAR_FOG_FACTORS_SENTINEL = -1.0f;
    private static final float NIGHT_FOG_BRIGHTNESS = 0.1f;
    private static final float DAY_FOG_BRIGHTNESS = 2.0f;
    private static final float DAY_NIGHT_LUMINANCE_START = 0.02f;
    private static final float DAY_NIGHT_LUMINANCE_END = 0.30f;

    private RemixFogCapture() {
    }

    public static void onFogState(
            ls entity,
            boolean thickFog,
            int renderLayer,
            boolean forceStartAtCamera,
            float viewDistance,
            float colorR,
            float colorG,
            float colorB) {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        float fogColorR = toLinearColor(colorR);
        float fogColorG = toLinearColor(colorG);
        float fogColorB = toLinearColor(colorB);
        float fogBrightness = computeDayNightFogBrightness(fogColorR, fogColorG, fogColorB);
        fogColorR *= fogBrightness;
        fogColorG *= fogBrightness;
        fogColorB *= fogBrightness;

        int fogMode = D3DFOG_NONE;
        float fogScale = 0.0f;
        float fogEnd = 0.0f;
        float fogDensity = 0.0f;
        float clampedViewDistance = Math.max(0.0f, viewDistance);

        if (thickFog) {
            fogMode = D3DFOG_EXP;
            fogDensity = 0.1f;
        } else if (entity != null && entity.a(ln.g)) {
            fogMode = D3DFOG_EXP;
            fogDensity = 0.1f;
        } else if (entity != null && entity.a(ln.h)) {
            fogMode = D3DFOG_EXP;
            fogDensity = 2.0f;
        } else if (clampedViewDistance > 0.0f) {
            fogMode = D3DFOG_LINEAR;
            float fogStart = clampedViewDistance * 0.4f;
            fogEnd = clampedViewDistance;
            if (renderLayer < 0) {
                fogStart = 0.0f;
                fogEnd = clampedViewDistance * 0.8f;
            } else if (forceStartAtCamera) {
                fogStart = 0.0f;
            } else {
                fogScale = REMIX_LINEAR_FOG_FACTORS_SENTINEL;
                fogEnd = clampedViewDistance;
            }

            if (fogScale != REMIX_LINEAR_FOG_FACTORS_SENTINEL) {
                float fogRange = fogEnd - fogStart;
                fogScale = fogRange > 1.0e-4f ? 1.0f / fogRange : 0.0f;
            }
        }

        MinecraftRenderHooks.updateFogState(
                fogMode,
                fogColorR,
                fogColorG,
                fogColorB,
                fogScale,
                fogEnd,
                fogDensity);
    }

    private static float toLinearColor(float channel) {
        float clamped = Math.max(0.0f, Math.min(1.0f, channel));
        if (clamped <= 0.04045f) {
            return clamped / 12.92f;
        }
        return (float) Math.pow((clamped + 0.055f) / 1.055f, 2.4f);
    }

    private static float computeDayNightFogBrightness(float colorR, float colorG, float colorB) {
        float luminance = colorR * 0.2126f + colorG * 0.7152f + colorB * 0.0722f;
        float dayFactor = saturate((luminance - DAY_NIGHT_LUMINANCE_START)
                / (DAY_NIGHT_LUMINANCE_END - DAY_NIGHT_LUMINANCE_START));
        dayFactor = dayFactor * dayFactor * (3.0f - 2.0f * dayFactor);
        return NIGHT_FOG_BRIGHTNESS + (DAY_FOG_BRIGHTNESS - NIGHT_FOG_BRIGHTNESS) * dayFactor;
    }

    private static float saturate(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}