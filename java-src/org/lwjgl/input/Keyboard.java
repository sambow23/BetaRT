package org.lwjgl.input;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public final class Keyboard {
    public static final int KEY_ESCAPE = 1;
    public static final int KEY_1 = 2;
    public static final int KEY_2 = 3;
    public static final int KEY_3 = 4;
    public static final int KEY_4 = 5;
    public static final int KEY_5 = 6;
    public static final int KEY_6 = 7;
    public static final int KEY_7 = 8;
    public static final int KEY_8 = 9;
    public static final int KEY_9 = 10;
    public static final int KEY_0 = 11;
    public static final int KEY_MINUS = 12;
    public static final int KEY_EQUALS = 13;
    public static final int KEY_BACK = 14;
    public static final int KEY_TAB = 15;
    public static final int KEY_Q = 16;
    public static final int KEY_W = 17;
    public static final int KEY_E = 18;
    public static final int KEY_R = 19;
    public static final int KEY_T = 20;
    public static final int KEY_Y = 21;
    public static final int KEY_U = 22;
    public static final int KEY_I = 23;
    public static final int KEY_O = 24;
    public static final int KEY_P = 25;
    public static final int KEY_LBRACKET = 26;
    public static final int KEY_RBRACKET = 27;
    public static final int KEY_RETURN = 28;
    public static final int KEY_LCONTROL = 29;
    public static final int KEY_A = 30;
    public static final int KEY_S = 31;
    public static final int KEY_D = 32;
    public static final int KEY_F = 33;
    public static final int KEY_G = 34;
    public static final int KEY_H = 35;
    public static final int KEY_J = 36;
    public static final int KEY_K = 37;
    public static final int KEY_L = 38;
    public static final int KEY_SEMICOLON = 39;
    public static final int KEY_APOSTROPHE = 40;
    public static final int KEY_GRAVE = 41;
    public static final int KEY_LSHIFT = 42;
    public static final int KEY_BACKSLASH = 43;
    public static final int KEY_Z = 44;
    public static final int KEY_X = 45;
    public static final int KEY_C = 46;
    public static final int KEY_V = 47;
    public static final int KEY_B = 48;
    public static final int KEY_N = 49;
    public static final int KEY_M = 50;
    public static final int KEY_COMMA = 51;
    public static final int KEY_PERIOD = 52;
    public static final int KEY_SLASH = 53;
    public static final int KEY_RSHIFT = 54;
    public static final int KEY_MULTIPLY = 55;
    public static final int KEY_LMENU = 56;
    public static final int KEY_SPACE = 57;
    public static final int KEY_CAPITAL = 58;
    public static final int KEY_F1 = 59;
    public static final int KEY_F2 = 60;
    public static final int KEY_F3 = 61;
    public static final int KEY_F4 = 62;
    public static final int KEY_F5 = 63;
    public static final int KEY_F6 = 64;
    public static final int KEY_F7 = 65;
    public static final int KEY_F8 = 66;
    public static final int KEY_F9 = 67;
    public static final int KEY_F10 = 68;
    public static final int KEY_F11 = 87;
    public static final int KEY_F12 = 88;
    public static final int KEY_RCONTROL = 157;
    public static final int KEY_RMENU = 184;
    public static final int KEY_HOME = 199;
    public static final int KEY_UP = 200;
    public static final int KEY_PRIOR = 201;
    public static final int KEY_LEFT = 203;
    public static final int KEY_RIGHT = 205;
    public static final int KEY_END = 207;
    public static final int KEY_DOWN = 208;
    public static final int KEY_NEXT = 209;
    public static final int KEY_INSERT = 210;
    public static final int KEY_DELETE = 211;

    private static final int GLFW_PRESS = 1;
    private static final int GLFW_REPEAT = 2;
    private static final int GLFW_MOD_SHIFT = 0x0001;
    private static final boolean[] DOWN = new boolean[256];
    private static final ArrayDeque<KeyboardEvent> EVENTS = new ArrayDeque<KeyboardEvent>();
    private static final Map<Integer, KeyMapping> GLFW_TO_KEY = new HashMap<Integer, KeyMapping>();
    private static final Map<Integer, KeyMapping> LWJGL_TO_KEY = new HashMap<Integer, KeyMapping>();

    private static boolean created;
    private static boolean repeatEvents;
    private static KeyboardEvent currentEvent;

    static {
        register(256, KEY_ESCAPE, "ESCAPE", (char) 27, (char) 27);
        register(49, KEY_1, "1", '1', '!');
        register(50, KEY_2, "2", '2', '@');
        register(51, KEY_3, "3", '3', '#');
        register(52, KEY_4, "4", '4', '$');
        register(53, KEY_5, "5", '5', '%');
        register(54, KEY_6, "6", '6', '^');
        register(55, KEY_7, "7", '7', '&');
        register(56, KEY_8, "8", '8', '*');
        register(57, KEY_9, "9", '9', '(');
        register(48, KEY_0, "0", '0', ')');
        register(45, KEY_MINUS, "MINUS", '-', '_');
        register(61, KEY_EQUALS, "EQUALS", '=', '+');
        register(258, KEY_TAB, "TAB", '\t', '\t');
        register(257, KEY_RETURN, "RETURN", '\n', '\n');
        register(259, KEY_BACK, "BACK", '\b', '\b');
        register(32, KEY_SPACE, "SPACE", ' ', ' ');
        register(65, KEY_A, "A", 'a', 'A');
        register(66, KEY_B, "B", 'b', 'B');
        register(67, KEY_C, "C", 'c', 'C');
        register(68, KEY_D, "D", 'd', 'D');
        register(69, KEY_E, "E", 'e', 'E');
        register(70, KEY_F, "F", 'f', 'F');
        register(71, KEY_G, "G", 'g', 'G');
        register(72, KEY_H, "H", 'h', 'H');
        register(73, KEY_I, "I", 'i', 'I');
        register(74, KEY_J, "J", 'j', 'J');
        register(75, KEY_K, "K", 'k', 'K');
        register(76, KEY_L, "L", 'l', 'L');
        register(77, KEY_M, "M", 'm', 'M');
        register(78, KEY_N, "N", 'n', 'N');
        register(79, KEY_O, "O", 'o', 'O');
        register(80, KEY_P, "P", 'p', 'P');
        register(81, KEY_Q, "Q", 'q', 'Q');
        register(82, KEY_R, "R", 'r', 'R');
        register(83, KEY_S, "S", 's', 'S');
        register(84, KEY_T, "T", 't', 'T');
        register(85, KEY_U, "U", 'u', 'U');
        register(86, KEY_V, "V", 'v', 'V');
        register(87, KEY_W, "W", 'w', 'W');
        register(88, KEY_X, "X", 'x', 'X');
        register(89, KEY_Y, "Y", 'y', 'Y');
        register(90, KEY_Z, "Z", 'z', 'Z');
        register(91, KEY_LBRACKET, "LBRACKET", '[', '{');
        register(93, KEY_RBRACKET, "RBRACKET", ']', '}');
        register(59, KEY_SEMICOLON, "SEMICOLON", ';', ':');
        register(39, KEY_APOSTROPHE, "APOSTROPHE", '\'', '"');
        register(96, KEY_GRAVE, "GRAVE", '`', '~');
        register(92, KEY_BACKSLASH, "BACKSLASH", '\\', '|');
        register(44, KEY_COMMA, "COMMA", ',', '<');
        register(46, KEY_PERIOD, "PERIOD", '.', '>');
        register(47, KEY_SLASH, "SLASH", '/', '?');
        register(340, KEY_LSHIFT, "LSHIFT", (char) 0, (char) 0);
        register(344, KEY_RSHIFT, "RSHIFT", (char) 0, (char) 0);
        register(341, KEY_LCONTROL, "LCONTROL", (char) 0, (char) 0);
        register(345, KEY_RCONTROL, "RCONTROL", (char) 0, (char) 0);
        register(342, KEY_LMENU, "LMENU", (char) 0, (char) 0);
        register(346, KEY_RMENU, "RMENU", (char) 0, (char) 0);
        register(290, KEY_F1, "F1", (char) 0, (char) 0);
        register(291, KEY_F2, "F2", (char) 0, (char) 0);
        register(292, KEY_F3, "F3", (char) 0, (char) 0);
        register(293, KEY_F4, "F4", (char) 0, (char) 0);
        register(294, KEY_F5, "F5", (char) 0, (char) 0);
        register(295, KEY_F6, "F6", (char) 0, (char) 0);
        register(296, KEY_F7, "F7", (char) 0, (char) 0);
        register(297, KEY_F8, "F8", (char) 0, (char) 0);
        register(298, KEY_F9, "F9", (char) 0, (char) 0);
        register(299, KEY_F10, "F10", (char) 0, (char) 0);
        register(300, KEY_F11, "F11", (char) 0, (char) 0);
        register(301, KEY_F12, "F12", (char) 0, (char) 0);
        register(262, KEY_RIGHT, "RIGHT", (char) 0, (char) 0);
        register(263, KEY_LEFT, "LEFT", (char) 0, (char) 0);
        register(264, KEY_DOWN, "DOWN", (char) 0, (char) 0);
        register(265, KEY_UP, "UP", (char) 0, (char) 0);
        register(268, KEY_HOME, "HOME", (char) 0, (char) 0);
        register(269, KEY_END, "END", (char) 0, (char) 0);
        register(266, KEY_PRIOR, "PRIOR", (char) 0, (char) 0);
        register(267, KEY_NEXT, "NEXT", (char) 0, (char) 0);
        register(260, KEY_INSERT, "INSERT", (char) 0, (char) 0);
        register(261, KEY_DELETE, "DELETE", (char) 127, (char) 127);
    }

    private Keyboard() {
    }

    public static void create() {
        created = true;
        EVENTS.clear();
        currentEvent = null;
    }

    public static void destroy() {
        created = false;
        EVENTS.clear();
        currentEvent = null;
        for (int index = 0; index < DOWN.length; index += 1) {
            DOWN[index] = false;
        }
    }

    public static void enableRepeatEvents(boolean enabled) {
        repeatEvents = enabled;
    }

    public static boolean next() {
        currentEvent = EVENTS.pollFirst();
        return currentEvent != null;
    }

    public static char getEventCharacter() {
        return currentEvent == null ? 0 : currentEvent.character;
    }

    public static int getEventKey() {
        return currentEvent == null ? 0 : currentEvent.key;
    }

    public static boolean getEventKeyState() {
        return currentEvent != null && currentEvent.pressed;
    }

    public static String getKeyName(int key) {
        KeyMapping mapping = LWJGL_TO_KEY.get(Integer.valueOf(key));
        return mapping == null ? "UNKNOWN" : mapping.name;
    }

    public static boolean isKeyDown(int key) {
        return key >= 0 && key < DOWN.length && DOWN[key];
    }

    public static void handleGlfwKey(int glfwKey, int action, int mods) {
        if (!created) {
            return;
        }

        KeyMapping mapping = GLFW_TO_KEY.get(Integer.valueOf(glfwKey));
        if (mapping == null) {
            return;
        }

        boolean pressed = action == GLFW_PRESS || action == GLFW_REPEAT;
        if (mapping.lwjglKey >= 0 && mapping.lwjglKey < DOWN.length) {
            DOWN[mapping.lwjglKey] = pressed;
        }

        if (action == GLFW_REPEAT && !repeatEvents) {
            return;
        }

        char character = 0;
        if (pressed) {
            character = ((mods & GLFW_MOD_SHIFT) != 0) ? mapping.shiftedCharacter : mapping.character;
        }

        EVENTS.addLast(new KeyboardEvent(mapping.lwjglKey, character, pressed));
    }

    private static void register(int glfwKey, int lwjglKey, String name, char character, char shiftedCharacter) {
        KeyMapping mapping = new KeyMapping(glfwKey, lwjglKey, name, character, shiftedCharacter);
        GLFW_TO_KEY.put(Integer.valueOf(glfwKey), mapping);
        LWJGL_TO_KEY.put(Integer.valueOf(lwjglKey), mapping);
    }

    private static final class KeyboardEvent {
        private final int key;
        private final char character;
        private final boolean pressed;

        private KeyboardEvent(int key, char character, boolean pressed) {
            this.key = key;
            this.character = character;
            this.pressed = pressed;
        }
    }

    private static final class KeyMapping {
        private final int glfwKey;
        private final int lwjglKey;
        private final String name;
        private final char character;
        private final char shiftedCharacter;

        private KeyMapping(int glfwKey, int lwjglKey, String name, char character, char shiftedCharacter) {
            this.glfwKey = glfwKey;
            this.lwjglKey = lwjglKey;
            this.name = name;
            this.character = character;
            this.shiftedCharacter = shiftedCharacter;
        }
    }
}