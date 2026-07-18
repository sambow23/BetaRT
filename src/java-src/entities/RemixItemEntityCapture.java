final class RemixItemEntityCapture {
    private static volatile boolean enabled = true;
    private static boolean pickupRenderActive;

    private RemixItemEntityCapture() {
    }

    static void onPickupRenderStart(sn entity) {
        pickupRenderActive = false;
    }

    static void onPickupRenderEnd() {
        pickupRenderActive = false;
    }

    static void onRenderStart(sn entity) {
        if (!canCapture() || entity == null) {
            return;
        }
        RemixDynamicEntitySession.ensureFrame();
        pickupRenderActive = true;
        RemixDynamicEntitySession.beginEntity(entity.aD, 0, 0, 0.0f, "");
    }

    static void onRenderEnd() {
        if (!pickupRenderActive) {
            return;
        }
        pickupRenderActive = false;
        RemixLivingEntityCapture.onRenderEnd();
    }

    static void setEnabled(boolean value) {
        enabled = value;
        if (value || !pickupRenderActive) {
            return;
        }
        pickupRenderActive = false;
        RemixDynamicEntitySession.clearEntityState();
    }

    private static boolean canCapture() {
        return enabled && RemixDynamicEntitySession.canCapture();
    }

    static void resetActiveCapture() {
        pickupRenderActive = false;
    }
}
