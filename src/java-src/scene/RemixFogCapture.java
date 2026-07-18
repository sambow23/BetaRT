import mcrtx.bridge.RemixSceneBridge;
import mcrtx.bridge.RemixLifecycleBridge;

public final class RemixFogCapture {
    private static final int D3DFOG_NONE = 0;
    private static final int D3DFOG_EXP = 1;
    private static final int D3DFOG_LINEAR = 3;
    private static final float REMIX_LINEAR_FOG_FACTORS_SENTINEL = -1.0f;
    private static final float NIGHT_FOG_BRIGHTNESS = 0.1f;
    private static final float DAY_FOG_BRIGHTNESS = 2.0f;
    private static final float DAY_NIGHT_LUMINANCE_START = 0.02f;
    private static final float DAY_NIGHT_LUMINANCE_END = 0.30f;
    // Note: Remix's composite pass remaps external linear fog using
    // rtx.externalFogLinearStartFactor (0.4) and rtx.externalFogLinearEndFactor (1.5).
    // So the effective fog range is [end*0.4, end*1.5]. Values here are pre-remap.
    // Water: end=8 -> fog starts at ~3.2 blocks, fully opaque at ~12 blocks.
    private static final float WATER_FOG_END_BLOCKS = 8.0f;
    // Lava: end=1.5 -> fog starts at ~0.6 blocks, fully opaque at ~2.25 blocks.
    private static final float LAVA_FOG_END_BLOCKS = 1.5f;

    private static volatile boolean submergedInWater = false;

    private RemixFogCapture() {
    }

    public static boolean isSubmergedInWater() {
        return submergedInWater;
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
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }

        float fogColorR = toLinearColor(colorR);
        float fogColorG = toLinearColor(colorG);
        float fogColorB = toLinearColor(colorB);
        // Skyless dimensions (Nether) have no day/night cycle and use a static
        // dark fog color whose low luminance would be misread as "night" and
        // crushed to near-black by the day/night heuristic.
        if (!forceStartAtCamera) {
            float fogBrightness = computeDayNightFogBrightness(fogColorR, fogColorG, fogColorB);
            fogColorR *= fogBrightness;
            fogColorG *= fogBrightness;
            fogColorB *= fogBrightness;
        }

        int fogMode = D3DFOG_NONE;
        float fogScale = 0.0f;
        float fogEnd = 0.0f;
        float fogDensity = 0.0f;
        float clampedViewDistance = Math.max(0.0f, viewDistance);

        boolean submerged = false;
        if (thickFog) {
            fogMode = D3DFOG_EXP;
            fogDensity = 0.1f;
        } else if (entity != null && entity.a(ln.g)) {
            fogMode = D3DFOG_LINEAR;
            fogEnd = WATER_FOG_END_BLOCKS;
            fogScale = REMIX_LINEAR_FOG_FACTORS_SENTINEL;
            submerged = true;
        } else if (entity != null && entity.a(ln.h)) {
            fogMode = D3DFOG_LINEAR;
            fogEnd = LAVA_FOG_END_BLOCKS;
            fogScale = REMIX_LINEAR_FOG_FACTORS_SENTINEL;
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

        RemixSceneBridge.updateFogState(
                fogMode,
                fogColorR,
                fogColorG,
                fogColorB,
                fogScale,
                fogEnd,
                fogDensity);

        submergedInWater = submerged;

        // Vanilla-like underwater tint: deep blue at ~55% alpha, driven through
        // the native Remix screen-tint pass so it applies uniformly to the
        // raytraced scene (under the HUD). Cleared when not submerged.
        if (submerged) {
            RemixSceneBridge.setScreenTint(0.02f, 0.06f, 0.20f, 0.55f);
        } else {
            RemixSceneBridge.setScreenTint(0.0f, 0.0f, 0.0f, 0.0f);
        }
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
