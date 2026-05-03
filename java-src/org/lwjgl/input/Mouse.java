package org.lwjgl.input;

import java.util.ArrayDeque;
import mcrtx.bridge.McrtxRuntimeConfig;
import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.RemixBridgeNative;
import org.lwjgl.opengl.Display;

public final class Mouse {
    private static final int DEBUG_LOG_LIMIT = 200;
    private static final int GLFW_PRESS = 1;
    private static final int GLFW_CURSOR = 0x00033001;
    private static final int GLFW_CURSOR_NORMAL = 0x00034001;
    private static final int GLFW_CURSOR_DISABLED = 0x00034003;
    private static final int NATIVE_STATE_X = 0;
    private static final int NATIVE_STATE_Y = 1;
    private static final int NATIVE_STATE_DX = 2;
    private static final int NATIVE_STATE_DY = 3;
    private static final int NATIVE_STATE_DWHEEL = 4;
    private static final int NATIVE_STATE_BUTTON_MASK = 5;
    private static final int NATIVE_STATE_WINDOW_HEIGHT = 6;

    private static final ArrayDeque<MouseEvent> EVENTS = new ArrayDeque<MouseEvent>();
    private static final boolean[] BUTTONS = new boolean[8];
    private static final int[] NATIVE_STATE = new int[7];

    private static boolean created;
    private static boolean grabbed;
    private static MouseEvent currentEvent;
    private static int currentX;
    private static int currentY;
    private static int windowHeight = 480;
    private static int deltaX;
    private static int deltaY;
    private static int remainingDebugLogs;
    private static int nativePollSamples;
    private static int nativePollFailures;
    private static boolean restoreGrabOnFocus;
    private static boolean remixUiInputSuppressed;

    private static final boolean VERBOSE_INPUT_LOGGING = detectVerboseInputLoggingEnabled();

    private Mouse() {
    }

    public static void create() {
        created = true;
        EVENTS.clear();
        currentEvent = null;
        deltaX = 0;
        deltaY = 0;
        remainingDebugLogs = DEBUG_LOG_LIMIT;
        nativePollSamples = 0;
        nativePollFailures = 0;
        restoreGrabOnFocus = false;
        remixUiInputSuppressed = false;
        debugLog("create singleNative=" + Display.isSingleNativeWindowMode());
    }

    public static void destroy() {
        if (grabbed && Display.isSingleNativeWindowMode() && RemixBridgeNative.isAvailable()) {
            RemixBridgeNative.nSetNativeMouseGrabbed(false);
        }
        debugLog("destroy grabbed=" + grabbed + " currentX=" + currentX + " currentY=" + currentY);
        created = false;
        grabbed = false;
        restoreGrabOnFocus = false;
        remixUiInputSuppressed = false;
        EVENTS.clear();
        currentEvent = null;
        deltaX = 0;
        deltaY = 0;
        for (int index = 0; index < BUTTONS.length; index += 1) {
            BUTTONS[index] = false;
        }
    }

    public static boolean next() {
        if (syncSuppressedState()) {
            return false;
        }
        currentEvent = EVENTS.pollFirst();
        if (currentEvent != null) {
            debugLog("next event button=" + currentEvent.button
                    + " pressed=" + currentEvent.buttonState
                    + " dWheel=" + currentEvent.dWheel
                    + " x=" + currentEvent.x
                    + " y=" + currentEvent.y
                    + " remainingEvents=" + EVENTS.size());
        }
        return currentEvent != null;
    }

    public static int getEventButton() {
        return currentEvent == null ? -1 : currentEvent.button;
    }

    public static boolean getEventButtonState() {
        return currentEvent != null && currentEvent.buttonState;
    }

    public static int getEventDWheel() {
        return currentEvent == null ? 0 : currentEvent.dWheel;
    }

    public static int getEventX() {
        return currentEvent == null ? currentX : currentEvent.x;
    }

    public static int getEventY() {
        return currentEvent == null ? currentY : currentEvent.y;
    }

    public static int getX() {
        return currentX;
    }

    public static int getY() {
        return currentY;
    }

    public static boolean isButtonDown(int button) {
        if (syncSuppressedState()) {
            return false;
        }
        return button >= 0 && button < BUTTONS.length && BUTTONS[button];
    }

    public static int getDX() {
        if (syncSuppressedState()) {
            return 0;
        }
        int value = deltaX;
        deltaX = 0;
        if (value != 0) {
            debugLog("getDX value=" + value + " grabbed=" + grabbed + " currentX=" + currentX + " currentY=" + currentY);
        }
        return value;
    }

    public static int getDY() {
        if (syncSuppressedState()) {
            return 0;
        }
        int value = deltaY;
        deltaY = 0;
        if (value != 0) {
            debugLog("getDY value=" + value + " grabbed=" + grabbed + " currentX=" + currentX + " currentY=" + currentY);
        }
        return value;
    }

    public static void setCursorPosition(int newX, int newY) {
        if (syncSuppressedState()) {
            return;
        }
        currentX = newX;
        currentY = newY;
        debugLog("setCursorPosition x=" + newX + " y=" + newY + " singleNative=" + Display.isSingleNativeWindowMode());
        if (Display.isSingleNativeWindowMode() && RemixBridgeNative.isAvailable()) {
            if (!RemixBridgeNative.nSetNativeCursorPosition(newX, newY)) {
                throw new IllegalStateException("Failed to move the native cursor");
            }
            return;
        }
        try {
            Display.bindings().setCursorPos(Display.windowHandle(), (double) newX, (double) toTopLeftY(newY));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to move the GLFW cursor", exception);
        }
    }

    public static void setGrabbed(boolean shouldGrab) {
        if (syncSuppressedState() && shouldGrab) {
            return;
        }
        boolean wasGrabbed = grabbed;
        grabbed = shouldGrab;
        debugLog("setGrabbed grabbed=" + shouldGrab + " singleNative=" + Display.isSingleNativeWindowMode());
        if (Display.isSingleNativeWindowMode() && RemixBridgeNative.isAvailable()) {
            if (!shouldGrab && wasGrabbed && !hasNativeInputFocus()) {
                restoreGrabOnFocus = true;
                debugLog("deferring native regrab until focus returns");
            } else if (shouldGrab) {
                restoreGrabOnFocus = false;
            } else if (!wasGrabbed) {
                restoreGrabOnFocus = false;
            }

            if (!RemixBridgeNative.nSetNativeMouseGrabbed(shouldGrab)) {
                throw new IllegalStateException("Failed to update native mouse grab state");
            }
            return;
        }

        if (shouldGrab || !wasGrabbed) {
            restoreGrabOnFocus = false;
        }

        try {
            if (shouldGrab) {
                Display.requestInputFocus();
            }
            Display.bindings().setInputMode(
                    Display.windowHandle(),
                    GLFW_CURSOR,
                    shouldGrab ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to update the GLFW cursor mode", exception);
        }
    }

    public static void updateWindowHeight(int newWindowHeight) {
        windowHeight = Math.max(1, newWindowHeight);
    }

    public static void handleCursorPosition(double x, double y) {
        if (!created || syncSuppressedState()) {
            return;
        }

        int previousX = currentX;
        int previousY = currentY;
        int translatedX = (int) Math.round(x);
        int translatedY = toBottomLeftY((int) Math.round(y));
        deltaX += translatedX - currentX;
        deltaY += translatedY - currentY;
        currentX = translatedX;
        currentY = translatedY;
        if (!grabbed && (translatedX != previousX || translatedY != previousY)) {
            EVENTS.addLast(new MouseEvent(-1, false, 0, currentX, currentY));
        }
    }

    public static void handleButton(int button, int action) {
        if (!created || syncSuppressedState() || button < 0 || button >= BUTTONS.length) {
            return;
        }

        boolean pressed = action == GLFW_PRESS;
        BUTTONS[button] = pressed;
        EVENTS.addLast(new MouseEvent(button, pressed, 0, currentX, currentY));
        debugLog("glfw button button=" + button + " pressed=" + pressed + " x=" + currentX + " y=" + currentY + " events=" + EVENTS.size());
    }

    public static void handleScroll(double offsetY) {
        if (!created || syncSuppressedState()) {
            return;
        }

        int dWheel = (int) Math.round(offsetY * 120.0);
        EVENTS.addLast(new MouseEvent(-1, false, dWheel, currentX, currentY));
        debugLog("glfw wheel dWheel=" + dWheel + " x=" + currentX + " y=" + currentY + " events=" + EVENTS.size());
    }

    public static void pollNativeState() {
        if (!created || !Display.isSingleNativeWindowMode() || !RemixBridgeNative.isAvailable()) {
            return;
        }

        if (syncSuppressedState()) {
            return;
        }

        if (restoreGrabOnFocus && !grabbed && hasNativeInputFocus()) {
            restoreGrabOnFocus = false;
            debugLog("restoring native grab after focus return");
            if (!MinecraftRenderHooks.restoreIngameFocusIfNeeded()) {
                setGrabbed(true);
            }
        }

        if (!RemixBridgeNative.nPollNativeMouseState(NATIVE_STATE)) {
            nativePollFailures += 1;
            if (nativePollFailures <= 10 || nativePollFailures % 60 == 0) {
                debugLog("native poll failed count=" + nativePollFailures + " grabbed=" + grabbed);
            }
            return;
        }
        nativePollFailures = 0;
        nativePollSamples += 1;

        int previousX = currentX;
        int previousY = currentY;
        updateWindowHeight(NATIVE_STATE[NATIVE_STATE_WINDOW_HEIGHT]);
        currentX = NATIVE_STATE[NATIVE_STATE_X];
        currentY = NATIVE_STATE[NATIVE_STATE_Y];
        deltaX += NATIVE_STATE[NATIVE_STATE_DX];
        deltaY += NATIVE_STATE[NATIVE_STATE_DY];

        int buttonsMask = NATIVE_STATE[NATIVE_STATE_BUTTON_MASK];
        if (nativePollSamples <= 10
                || NATIVE_STATE[NATIVE_STATE_DX] != 0
                || NATIVE_STATE[NATIVE_STATE_DY] != 0
                || NATIVE_STATE[NATIVE_STATE_DWHEEL] != 0
                || buttonsMask != currentButtonsMask()) {
            debugLog("native poll x=" + currentX
                    + " y=" + currentY
                    + " dx=" + NATIVE_STATE[NATIVE_STATE_DX]
                    + " dy=" + NATIVE_STATE[NATIVE_STATE_DY]
                    + " dWheel=" + NATIVE_STATE[NATIVE_STATE_DWHEEL]
                    + " buttonsMask=" + buttonsMask
                    + " grabbed=" + grabbed
                    + " events=" + EVENTS.size());
        }

        if (!grabbed && (currentX != previousX || currentY != previousY)) {
            EVENTS.addLast(new MouseEvent(-1, false, 0, currentX, currentY));
            debugLog("native move event x=" + currentX + " y=" + currentY + " events=" + EVENTS.size());
        }

        for (int button = 0; button < BUTTONS.length; button += 1) {
            boolean pressed = (buttonsMask & (1 << button)) != 0;
            if (BUTTONS[button] != pressed) {
                BUTTONS[button] = pressed;
                EVENTS.addLast(new MouseEvent(button, pressed, 0, currentX, currentY));
                debugLog("native button button=" + button + " pressed=" + pressed + " x=" + currentX + " y=" + currentY + " events=" + EVENTS.size());
            }
        }

        int dWheel = NATIVE_STATE[NATIVE_STATE_DWHEEL];
        if (dWheel != 0) {
            EVENTS.addLast(new MouseEvent(-1, false, dWheel, currentX, currentY));
            debugLog("native wheel dWheel=" + dWheel + " x=" + currentX + " y=" + currentY + " events=" + EVENTS.size());
        }
    }

    private static int currentButtonsMask() {
        int buttonsMask = 0;
        for (int button = 0; button < BUTTONS.length; button += 1) {
            if (BUTTONS[button]) {
                buttonsMask |= 1 << button;
            }
        }
        return buttonsMask;
    }

    private static boolean detectVerboseInputLoggingEnabled() {
        String value = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_VERBOSE_INPUT_LOG");
        if (value == null || value.isEmpty()) {
            value = McrtxRuntimeConfig.getEnvironmentValue("MCRTX_VERBOSE_LOG");
        }
        return McrtxRuntimeConfig.isTruthyValue(value);
    }

    private static boolean hasNativeInputFocus() {
        return Display.isSingleNativeWindowMode()
                && RemixBridgeNative.isAvailable()
                && RemixBridgeNative.nHasWindowFocus();
    }

    private static boolean syncSuppressedState() {
        boolean suppress = MinecraftRenderHooks.isRemixUiInputActive();
        if (!suppress) {
            remixUiInputSuppressed = false;
            return false;
        }

        if (!remixUiInputSuppressed) {
            if (grabbed && Display.isSingleNativeWindowMode() && RemixBridgeNative.isAvailable()) {
                RemixBridgeNative.nSetNativeMouseGrabbed(false);
            } else if (grabbed) {
                try {
                    Display.bindings().setInputMode(Display.windowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                } catch (Exception exception) {
                    throw new IllegalStateException("Failed to release the GLFW cursor while Remix UI input is active", exception);
                }
            }

            grabbed = false;
            restoreGrabOnFocus = false;
            deltaX = 0;
            deltaY = 0;
            currentEvent = null;
            EVENTS.clear();
            for (int index = 0; index < BUTTONS.length; index += 1) {
                BUTTONS[index] = false;
            }
            remixUiInputSuppressed = true;
        }

        return true;
    }

    private static void debugLog(String message) {
        if (!VERBOSE_INPUT_LOGGING || remainingDebugLogs <= 0) {
            return;
        }

        remainingDebugLogs -= 1;
        System.out.println("[mcrtx][mouse] " + message);
    }

    private static int toBottomLeftY(int topLeftY) {
        return Math.max(0, windowHeight - 1 - topLeftY);
    }

    private static int toTopLeftY(int bottomLeftY) {
        return Math.max(0, windowHeight - 1 - bottomLeftY);
    }

    private static final class MouseEvent {
        private final int button;
        private final boolean buttonState;
        private final int dWheel;
        private final int x;
        private final int y;

        private MouseEvent(int button, boolean buttonState, int dWheel, int x, int y) {
            this.button = button;
            this.buttonState = buttonState;
            this.dWheel = dWheel;
            this.x = x;
            this.y = y;
        }
    }
}