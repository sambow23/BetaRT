import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.ColorMath;

public final class RemixParticleCapture {
    private RemixParticleCapture() {
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
}
