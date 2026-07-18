import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.MatrixMath;
import mcrtx.bridge.RemixDynamicEntityBridge;
import mcrtx.bridge.RemixLifecycleBridge;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class RemixFirstPersonCapture {
    private static final int FIRST_PERSON_ENTITY_ID = Integer.MAX_VALUE - 1;
    private static final int FIRST_PERSON_SHADOW_ENTITY_ID = Integer.MAX_VALUE - 2;
    private static final String PLAYER_TEXTURE_PATH = "/mob/char.png";
    private static final String SHADOW_TEXTURE_ALIAS_PREFIX = "/mcrtx_alias/firstperson_shadow/";

    private static final float[] shadowOverlayInverse = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private static boolean active;
    private static String activeTexture = "";
    private static boolean shadowCaptureActive;
    private static boolean shadowCaptureAvailable = true;
    private static volatile boolean playerShadowsEnabled = true;
    private static boolean loggedShadowCaptureFailure;
    private static boolean voxelsGeneratedForCurrentItem;

    private RemixFirstPersonCapture() {
    }

    static void onRenderStart() {
        if (!RemixLifecycleBridge.isInitialized()) {
            return;
        }

        RemixDynamicEntitySession.ensureFrame();
        RemixDynamicEntityBridge.setFirstPersonHeldItem(RemixHeldItemCapture.NO_HELD_ITEM);
        if (!RemixDynamicEntitySession.isRenderingEnabled()) {
            return;
        }

        active = true;
        activeTexture = PLAYER_TEXTURE_PATH;
        RemixDynamicEntitySession.prepareAuxiliaryEntity(0, 0, 0.0f);
        RemixDynamicEntityBridge.beginDynamicEntity(FIRST_PERSON_ENTITY_ID, 0, 0);
        RemixDynamicEntityBridge.setDynamicEntityTexture(activeTexture);
    }

    static void onShadowPlayerRender(Minecraft minecraft, float partialTicks) {
        if (!RemixDynamicEntitySession.isRenderingEnabled()
                || !playerShadowsEnabled
                || !shadowCaptureAvailable
                || !RemixLifecycleBridge.isInitialized()
                || minecraft == null
                || !(minecraft.h instanceof gs)) {
            return;
        }

        long renderStartNanos = System.nanoTime();
        bw renderer = th.a.a(minecraft.h);
        if (!(renderer instanceof ds)) {
            return;
        }
        long lookupRendererEndNanos = System.nanoTime();

        RemixDynamicEntitySession.ensureFrame();
        RemixDynamicEntitySession.beginEntity(
                FIRST_PERSON_SHADOW_ENTITY_ID, 0, 0, 0.0f, shadowTextureAlias(PLAYER_TEXTURE_PATH));
        shadowCaptureActive = true;

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
            float[] overlayModelView = RemixDynamicModelCapture.captureModelViewMatrix();
            if (overlayModelView == null) {
                return;
            }
            float[] overlayInverse = MatrixMath.invertAffineColumnMajor(overlayModelView);
            System.arraycopy(overlayInverse, 0, shadowOverlayInverse, 0, shadowOverlayInverse.length);
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

            HookProfiler.record(HookProfiler.SIDE_HOOK,
                    "hook.onFirstPersonShadowPlayerRender.lookupRenderer",
                    lookupRendererEndNanos - renderStartNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK,
                    "hook.onFirstPersonShadowPlayerRender.setupCapture",
                    setupEndNanos - lookupRendererEndNanos);
            HookProfiler.record(HookProfiler.SIDE_HOOK,
                    "hook.onFirstPersonShadowPlayerRender.renderShadow",
                    shadowRenderEndNanos - setupEndNanos);
        } catch (RuntimeException exception) {
            disableShadowCapture(exception);
            return;
        } finally {
            shadowCaptureActive = false;
            if (RemixDynamicEntitySession.isEntityActive()
                    && RemixDynamicEntitySession.activeEntityId() == FIRST_PERSON_SHADOW_ENTITY_ID) {
                RemixDynamicEntitySession.endEntity();
            }
        }
    }

    static void onRenderEnd() {
        if (!active) {
            return;
        }
        RemixDynamicEntityBridge.endDynamicEntity();
        active = false;
        activeTexture = "";
        RemixDynamicEntitySession.prepareAuxiliaryEntity(0, 0, 0.0f);
    }

    static boolean isActive() {
        return active;
    }

    static String activeTexture() {
        return active && !activeTexture.isEmpty() ? activeTexture : "";
    }

    static void setActiveTexture(String texture) {
        activeTexture = texture == null ? "" : texture;
        RemixDynamicEntityBridge.setDynamicEntityTexture(activeTexture);
    }

    static boolean shouldSuppressVanillaDraw() {
        return !RemixDynamicEntitySession.activeCaptureTexture().isEmpty();
    }

    static void setPlayerShadowsEnabled(boolean enabled) {
        playerShadowsEnabled = enabled;
        if (!enabled) {
            shadowCaptureActive = false;
        }
    }

    static boolean isShadowCaptureActive() {
        return shadowCaptureActive;
    }

    static String shadowTextureAlias(String texturePath) {
        String normalized = RemixDynamicEntitySession.stripTexturePrefix(texturePath);
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.startsWith(SHADOW_TEXTURE_ALIAS_PREFIX)) {
            return normalized;
        }
        if (normalized.charAt(0) == '/') {
            normalized = normalized.substring(1);
        }
        return SHADOW_TEXTURE_ALIAS_PREFIX + normalized;
    }

    static RemixCameraState.PreciseTransform modelToWorldTransform(float[] modelView) {
        if (!shadowCaptureActive) {
            return RemixCameraState.buildModelToWorldTransform(modelView);
        }
        float[] overlayNeutralModelView = MatrixMath.multiplyColumnMajor(shadowOverlayInverse, modelView);
        return RemixCameraState.buildCameraTranslatedModelTransform(overlayNeutralModelView);
    }

    static void resetVoxelCapture() {
        voxelsGeneratedForCurrentItem = false;
    }

    static boolean hasGeneratedVoxels() {
        return voxelsGeneratedForCurrentItem;
    }

    static void markVoxelsGenerated() {
        voxelsGeneratedForCurrentItem = true;
    }

    static void resetActiveCapture() {
        active = false;
        activeTexture = "";
        shadowCaptureActive = false;
    }

    private static void disableShadowCapture(RuntimeException exception) {
        shadowCaptureAvailable = false;
        if (!loggedShadowCaptureFailure) {
            loggedShadowCaptureFailure = true;
            System.err.println("[mcrtx] disabling first-person shadow capture after hook failure");
            exception.printStackTrace();
        }
    }
}
