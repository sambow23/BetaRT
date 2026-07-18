import mcrtx.bridge.RemixLifecycleBridge;
import mcrtx.bridge.RemixUiBridge;

final class RemixUiCaptureSession {
    private static final RemixUiDrawList DRAW_LIST = new RemixUiDrawList();
    private static final RemixUiDrawList.Checkpoint EMPTY_CHECKPOINT =
            new RemixUiDrawList.Checkpoint(0, 0);

    private static boolean active;
    private static int displayWidth;
    private static int displayHeight;
    private static boolean nameTagCaptureActive;
    private static RemixUiDrawList.Checkpoint pendingNameTagCheckpoint = EMPTY_CHECKPOINT;
    private static RemixUiDrawList.Checkpoint nameTagCaptureStartCheckpoint = EMPTY_CHECKPOINT;
    private static boolean nameTagCaptureDiscarded;

    private RemixUiCaptureSession() {
    }

    static boolean begin(int width, int height) {
        if (active || !RemixLifecycleBridge.isInitialized() || width <= 0 || height <= 0) {
            return false;
        }
        displayWidth = width;
        displayHeight = height;
        DRAW_LIST.rollback(pendingNameTagCheckpoint);
        pendingNameTagCheckpoint = EMPTY_CHECKPOINT;
        nameTagCaptureActive = false;
        nameTagCaptureStartCheckpoint = DRAW_LIST.checkpoint();
        nameTagCaptureDiscarded = false;
        active = true;
        return true;
    }

    static boolean clear(int width, int height) {
        if (!RemixLifecycleBridge.isInitialized() || width <= 0 || height <= 0) {
            return false;
        }
        displayWidth = width;
        displayHeight = height;
        active = false;
        nameTagCaptureActive = false;
        DRAW_LIST.clear();
        pendingNameTagCheckpoint = EMPTY_CHECKPOINT;
        nameTagCaptureStartCheckpoint = EMPTY_CHECKPOINT;
        nameTagCaptureDiscarded = false;
        submit();
        return true;
    }

    static void beginNameTagCapture(int width, int height) {
        if (active || !RemixLifecycleBridge.isInitialized() || width <= 0 || height <= 0) {
            return;
        }
        displayWidth = width;
        displayHeight = height;
        DRAW_LIST.rollback(pendingNameTagCheckpoint);
        nameTagCaptureActive = true;
        nameTagCaptureStartCheckpoint = DRAW_LIST.checkpoint();
        nameTagCaptureDiscarded = false;
    }

    static void endNameTagCapture() {
        if (!nameTagCaptureActive) {
            return;
        }
        if (nameTagCaptureDiscarded) {
            DRAW_LIST.rollback(nameTagCaptureStartCheckpoint);
        }
        nameTagCaptureActive = false;
        pendingNameTagCheckpoint = DRAW_LIST.checkpoint();
        nameTagCaptureStartCheckpoint = pendingNameTagCheckpoint;
        nameTagCaptureDiscarded = false;
    }

    static boolean isActive() {
        return active;
    }

    static boolean isNameTagCaptureActive() {
        return nameTagCaptureActive;
    }

    static boolean isCapturingUiOrNameTags() {
        return active || nameTagCaptureActive;
    }

    static boolean hasPendingNameTagDraws() {
        return pendingNameTagCheckpoint.vertexCount > 0
                && pendingNameTagCheckpoint.commandCount > 0;
    }

    static int displayWidth() {
        return displayWidth;
    }

    static int displayHeight() {
        return displayHeight;
    }

    static RemixUiDrawList drawList() {
        return DRAW_LIST;
    }

    static void discardActiveNameTagCapture() {
        if (!nameTagCaptureActive) {
            return;
        }
        DRAW_LIST.rollback(nameTagCaptureStartCheckpoint);
        nameTagCaptureDiscarded = true;
    }

    static boolean isNameTagCaptureDiscarded() {
        return nameTagCaptureDiscarded;
    }

    static void end() {
        if (!active) {
            return;
        }
        active = false;
        submit();
    }

    static void reset() {
        active = false;
        nameTagCaptureActive = false;
        DRAW_LIST.clear();
        pendingNameTagCheckpoint = EMPTY_CHECKPOINT;
        nameTagCaptureStartCheckpoint = EMPTY_CHECKPOINT;
        nameTagCaptureDiscarded = false;
    }

    private static void submit() {
        RemixUiBridge.submitUiDrawList(
                DRAW_LIST.vertexXyzuv(),
                DRAW_LIST.vertexColors(),
                DRAW_LIST.vertexCount(),
                DRAW_LIST.commandTextureIds(),
                DRAW_LIST.commandQuadCounts(),
                DRAW_LIST.commandFlags(),
                DRAW_LIST.commandCount(),
                displayWidth,
                displayHeight);
    }
}
