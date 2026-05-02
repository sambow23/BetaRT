package org.lwjgl.input;

import java.util.ArrayDeque;
import org.lwjgl.opengl.Display;

public final class Mouse {
    private static final int GLFW_PRESS = 1;
    private static final int GLFW_CURSOR = 0x00033001;
    private static final int GLFW_CURSOR_NORMAL = 0x00034001;
    private static final int GLFW_CURSOR_DISABLED = 0x00034003;

    private static final ArrayDeque<MouseEvent> EVENTS = new ArrayDeque<MouseEvent>();
    private static final boolean[] BUTTONS = new boolean[8];

    private static boolean created;
    private static boolean grabbed;
    private static MouseEvent currentEvent;
    private static int currentX;
    private static int currentY;
    private static int windowHeight = 480;
    private static int deltaX;
    private static int deltaY;

    private Mouse() {
    }

    public static void create() {
        created = true;
        EVENTS.clear();
        currentEvent = null;
        deltaX = 0;
        deltaY = 0;
    }

    public static void destroy() {
        created = false;
        EVENTS.clear();
        currentEvent = null;
        deltaX = 0;
        deltaY = 0;
        for (int index = 0; index < BUTTONS.length; index += 1) {
            BUTTONS[index] = false;
        }
    }

    public static boolean next() {
        currentEvent = EVENTS.pollFirst();
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
        return button >= 0 && button < BUTTONS.length && BUTTONS[button];
    }

    public static int getDX() {
        int value = deltaX;
        deltaX = 0;
        return value;
    }

    public static int getDY() {
        int value = deltaY;
        deltaY = 0;
        return value;
    }

    public static void setCursorPosition(int newX, int newY) {
        currentX = newX;
        currentY = newY;
        try {
            Display.bindings().setCursorPos(Display.windowHandle(), (double) newX, (double) toTopLeftY(newY));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to move the GLFW cursor", exception);
        }
    }

    public static void setGrabbed(boolean shouldGrab) {
        grabbed = shouldGrab;
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
        if (!created) {
            return;
        }

        int translatedX = (int) Math.round(x);
        int translatedY = toBottomLeftY((int) Math.round(y));
        deltaX += translatedX - currentX;
        deltaY += translatedY - currentY;
        currentX = translatedX;
        currentY = translatedY;
    }

    public static void handleButton(int button, int action) {
        if (!created || button < 0 || button >= BUTTONS.length) {
            return;
        }

        boolean pressed = action == GLFW_PRESS;
        BUTTONS[button] = pressed;
        EVENTS.addLast(new MouseEvent(button, pressed, 0, currentX, currentY));
    }

    public static void handleScroll(double offsetY) {
        if (!created) {
            return;
        }

        int dWheel = (int) Math.round(offsetY * 120.0);
        EVENTS.addLast(new MouseEvent(-1, false, dWheel, currentX, currentY));
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