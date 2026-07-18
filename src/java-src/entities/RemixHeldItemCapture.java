import mcrtx.bridge.RemixDynamicEntityBridge;
import mcrtx.bridge.RemixLifecycleBridge;

final class RemixHeldItemCapture {
    static final int NO_HELD_ITEM = -1;
    static final String TERRAIN_TEXTURE_PATH = "/terrain.png";
    static final String GUI_ITEMS_TEXTURE_PATH = "/gui/items.png";

    private static final int TORCH_BLOCK_ID = 50;
    private static final int REDSTONE_TORCH_OFF_BLOCK_ID = 75;
    private static final int REDSTONE_TORCH_ON_BLOCK_ID = 76;
    private static final float ENTITY_HELD_TORCH_RIGHT_NUDGE = 0.18f;

    private static volatile boolean heldTorchLightsEnabled = true;

    private RemixHeldItemCapture() {
    }

    static void setHeldTorchLightsEnabled(boolean enabled) {
        heldTorchLightsEnabled = enabled;
    }

    static void onFirstPersonItemRender(iz itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!RemixFirstPersonCapture.isActive()) {
            RemixDynamicEntityBridge.setFirstPersonHeldItem(
                    heldTorchLightsEnabled && isTorchLikeHeldItem(itemStack.c)
                            ? itemStack.c
                            : NO_HELD_ITEM);
            return;
        }

        RemixFirstPersonCapture.setActiveTexture(texturePathForItem(itemStack));
        RemixDynamicEntityBridge.setFirstPersonHeldItem(
                heldTorchLightsEnabled && isTorchLikeHeldItem(itemStack.c)
                        ? itemStack.c
                        : NO_HELD_ITEM);
    }

    static void onPlayerEquippedItemRenderStart(gs player, iz itemStack, float partialTicks) {
        if (!RemixLifecycleBridge.isInitialized() || player == null) {
            return;
        }

        syncEntityHeldTorch(player, itemStack, partialTicks);
        if (!RemixDynamicEntitySession.isEntityActive() || itemStack == null) {
            return;
        }
        RemixDynamicEntitySession.bindEntityTexture(texturePathForItem(itemStack), null);
    }

    static void onLivingEquippedItemRenderStart(ls entity, iz itemStack) {
        if (!RemixLifecycleBridge.isInitialized() || entity == null || itemStack == null) {
            return;
        }
        if (!RemixDynamicEntitySession.isEntityActive()) {
            return;
        }
        RemixDynamicEntitySession.bindEntityTexture(texturePathForItem(itemStack), null);
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
        if (RemixFirstPersonCapture.isShadowCaptureActive()) {
            return;
        }

        if (!heldTorchLightsEnabled) {
            RemixDynamicEntityBridge.setEntityHeldTorch(
                    player.aD, 0.0f, 0.0f, 0.0f, NO_HELD_ITEM);
            return;
        }

        int itemId = heldItem != null && isTorchLikeHeldItem(heldItem.c)
                ? heldItem.c
                : NO_HELD_ITEM;
        if (itemId == NO_HELD_ITEM) {
            RemixDynamicEntityBridge.setEntityHeldTorch(
                    player.aD, 0.0f, 0.0f, 0.0f, NO_HELD_ITEM);
            return;
        }

        float[] modelView = RemixDynamicModelCapture.captureModelViewMatrix();
        if (modelView == null) {
            return;
        }
        RemixCameraState.PreciseTransform modelToWorld =
                RemixCameraState.buildModelToWorldTransform(modelView);
        double handX = modelToWorld.x;
        double handY = modelToWorld.y;
        double handZ = modelToWorld.z;
        float interpolatedYaw = player.aU + (player.aS - player.aU) * partialTicks;
        double yawRadians = Math.toRadians(interpolatedYaw);
        handX += (-Math.cos(yawRadians)) * (double) ENTITY_HELD_TORCH_RIGHT_NUDGE;
        handZ += (-Math.sin(yawRadians)) * (double) ENTITY_HELD_TORCH_RIGHT_NUDGE;
        RemixDynamicEntityBridge.setEntityHeldTorch(player.aD, handX, handY, handZ, itemId);
    }
}
