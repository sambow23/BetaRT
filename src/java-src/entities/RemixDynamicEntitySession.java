import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixDynamicEntityBridge;
import mcrtx.bridge.RemixLifecycleBridge;
import mcrtx.bridge.RemixParticleOverlayBridge;

final class RemixDynamicEntitySession {
    private static final int MAX_DYNAMIC_BONES = 256;
    private static final int TILE_ENTITY_ID_NAMESPACE = 0x40000000;

    private static boolean frameActive;
    private static boolean entityActive;
    private static int activeEntityId = -1;
    private static int activeHurtStage;
    private static int activeCreeperFuseStage;
    private static float activeCreeperFuseProgress;
    private static String activeEntityTexture = "";
    private static int nextBoneIndex;
    private static volatile boolean renderingEnabled = true;
    private static boolean loggedHookFailure;
    private static boolean loggedBoneOverflow;

    private RemixDynamicEntitySession() {
    }

    static void ensureFrame() {
        if (frameActive || !RemixLifecycleBridge.isInitialized()) {
            return;
        }

        long beginFrameStartNanos = System.nanoTime();
        RemixDynamicEntityBridge.beginDynamicEntityFrame();
        RemixParticleOverlayBridge.beginDestroyOverlayFrame();
        RemixParticleOverlayBridge.beginBlockOutlineFrame();
        frameActive = true;
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.dynamicEntity.ensureFrame.beginFrame",
                System.nanoTime() - beginFrameStartNanos);
    }

    static void onFramePresented() {
        frameActive = false;
        RemixSignCapture.onFramePresented();
    }

    static boolean canCapture() {
        return renderingEnabled && RemixLifecycleBridge.isInitialized();
    }

    static boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    static void setRenderingEnabled(boolean enabled) {
        renderingEnabled = enabled;
        if (enabled) {
            return;
        }

        clearEntityState();
        RemixItemEntityCapture.resetActiveCapture();
        RemixEntityFireCapture.resetActiveCapture();
        RemixSignCapture.resetActiveCapture();
        RemixFirstPersonCapture.resetActiveCapture();
    }

    static void beginEntity(int entityId, int hurtStage, int creeperFuseStage,
            float creeperFuseProgress, String texture) {
        entityActive = true;
        activeEntityId = entityId;
        preparePresentation(hurtStage, creeperFuseStage, creeperFuseProgress);
        activeEntityTexture = texture == null ? "" : texture;
        RemixDynamicEntityBridge.beginDynamicEntity(entityId, hurtStage, creeperFuseStage);
        if (!activeEntityTexture.isEmpty()) {
            RemixDynamicEntityBridge.setDynamicEntityTexture(activeEntityTexture);
        }
    }

    static void prepareAuxiliaryEntity(int hurtStage, int creeperFuseStage, float creeperFuseProgress) {
        preparePresentation(hurtStage, creeperFuseStage, creeperFuseProgress);
    }

    private static void preparePresentation(int hurtStage, int creeperFuseStage, float creeperFuseProgress) {
        activeHurtStage = hurtStage;
        activeCreeperFuseStage = creeperFuseStage;
        activeCreeperFuseProgress = creeperFuseProgress;
        nextBoneIndex = 0;
    }

    static void endEntity() {
        if (!entityActive) {
            return;
        }
        RemixDynamicEntityBridge.endDynamicEntity();
        clearEntityState();
    }

    static void endAuxiliaryEntity() {
        RemixDynamicEntityBridge.endDynamicEntity();
        clearPresentation();
    }

    static void clearEntityState() {
        entityActive = false;
        activeEntityId = -1;
        activeEntityTexture = "";
        clearPresentation();
    }

    private static void clearPresentation() {
        activeHurtStage = 0;
        activeCreeperFuseStage = 0;
        activeCreeperFuseProgress = 0.0f;
        nextBoneIndex = 0;
    }

    static boolean isEntityActive() {
        return entityActive;
    }

    static int activeEntityId() {
        return activeEntityId;
    }

    static int activeHurtStage() {
        return activeHurtStage;
    }

    static int activeCreeperFuseStage() {
        return activeCreeperFuseStage;
    }

    static float activeCreeperFuseProgress() {
        return activeCreeperFuseProgress;
    }

    static String activeEntityTexture() {
        return activeEntityTexture;
    }

    static String activeCaptureTexture() {
        if (entityActive && !activeEntityTexture.isEmpty()) {
            return activeEntityTexture;
        }
        return RemixFirstPersonCapture.activeTexture();
    }

    static void setEntityTexture(String texture) {
        String normalized = texture == null ? "" : texture;
        if (normalized.isEmpty() || normalized.equals(activeEntityTexture)) {
            return;
        }
        activeEntityTexture = normalized;
        RemixDynamicEntityBridge.setDynamicEntityTexture(normalized);
    }

    static void bindEntityTexture(String primaryTexture, String fallbackTexture) {
        if (!entityActive) {
            return;
        }
        String resolvedTexture = normalizeTexturePath(primaryTexture, fallbackTexture);
        if (RemixEntityFireCapture.isActive()) {
            resolvedTexture = RemixEntityFireCapture.textureAlias(
                    resolvedTexture.isEmpty() ? RemixHeldItemCapture.TERRAIN_TEXTURE_PATH : resolvedTexture);
        } else if (RemixFirstPersonCapture.isShadowCaptureActive()) {
            resolvedTexture = RemixFirstPersonCapture.shadowTextureAlias(resolvedTexture);
        }
        setEntityTexture(resolvedTexture);
    }

    static String normalizeTexturePath(String primaryTexture, String fallbackTexture) {
        String normalizedPrimary = stripTexturePrefix(primaryTexture);
        if (!normalizedPrimary.isEmpty() && normalizedPrimary.charAt(0) == '/') {
            return normalizedPrimary;
        }

        String normalizedFallback = stripTexturePrefix(fallbackTexture);
        return normalizedFallback.isEmpty() ? "" : normalizedFallback;
    }

    static String stripTexturePrefix(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            return "";
        }
        String normalized = texturePath;
        while (normalized.startsWith("%clamp%") || normalized.startsWith("%blur%")) {
            if (normalized.startsWith("%clamp%")) {
                normalized = normalized.substring(7);
            } else {
                normalized = normalized.substring(6);
            }
        }
        return normalized;
    }

    static int stableTileEntityId(int x, int y, int z, int salt) {
        int hash = salt;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        return TILE_ENTITY_ID_NAMESPACE | (hash & 0x3FFFFFFF);
    }

    static int allocateBoneIndex() {
        if (nextBoneIndex >= MAX_DYNAMIC_BONES) {
            if (!loggedBoneOverflow) {
                loggedBoneOverflow = true;
                System.err.println("[mcrtx] dynamic capture exceeded Remix bone limit; skipping excess dynamic geometry");
            }
            return -1;
        }

        int boneIndex = nextBoneIndex;
        nextBoneIndex += 1;
        return boneIndex;
    }

    static void submitBoneTransform(int boneIndex, RemixCameraState.PreciseTransform transform) {
        float[] matrix = transform.matrix;
        RemixDynamicEntityBridge.setDynamicEntityBoneTransform(
                boneIndex,
                matrix[0], matrix[4], matrix[8], transform.x,
                matrix[1], matrix[5], matrix[9], transform.y,
                matrix[2], matrix[6], matrix[10], transform.z);
    }

    static void handleFailure(RuntimeException exception) {
        RemixDynamicEntityBridge.endDynamicEntity();
        if (!loggedHookFailure) {
            loggedHookFailure = true;
            System.err.println("[mcrtx] dynamic entity capture disabled after hook failure");
            exception.printStackTrace();
        }
        clearEntityState();
        RemixItemEntityCapture.resetActiveCapture();
        RemixEntityFireCapture.resetActiveCapture();
        RemixSignCapture.resetActiveCapture();
        RemixFirstPersonCapture.resetActiveCapture();
    }
}
