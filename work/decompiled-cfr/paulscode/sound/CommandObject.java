/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

public class CommandObject {
    public static final int INITIALIZE = 1;
    public static final int LOAD_SOUND = 2;
    public static final int UNLOAD_SOUND = 4;
    public static final int QUEUE_SOUND = 5;
    public static final int DEQUEUE_SOUND = 6;
    public static final int FADE_OUT = 7;
    public static final int FADE_OUT_IN = 8;
    public static final int CHECK_FADE_VOLUMES = 9;
    public static final int NEW_SOURCE = 10;
    public static final int RAW_DATA_STREAM = 11;
    public static final int QUICK_PLAY = 12;
    public static final int SET_POSITION = 13;
    public static final int SET_VOLUME = 14;
    public static final int SET_PITCH = 15;
    public static final int SET_PRIORITY = 16;
    public static final int SET_LOOPING = 17;
    public static final int SET_ATTENUATION = 18;
    public static final int SET_DIST_OR_ROLL = 19;
    public static final int PLAY = 21;
    public static final int FEED_RAW_AUDIO_DATA = 22;
    public static final int PAUSE = 23;
    public static final int STOP = 24;
    public static final int REWIND = 25;
    public static final int FLUSH = 26;
    public static final int CULL = 27;
    public static final int ACTIVATE = 28;
    public static final int SET_TEMPORARY = 29;
    public static final int REMOVE_SOURCE = 30;
    public static final int MOVE_LISTENER = 31;
    public static final int SET_LISTENER_POSITION = 32;
    public static final int TURN_LISTENER = 33;
    public static final int SET_LISTENER_ANGLE = 34;
    public static final int SET_LISTENER_ORIENTATION = 35;
    public static final int SET_MASTER_VOLUME = 36;
    public static final int NEW_LIBRARY = 37;
    public byte[] buffer;
    public int[] intArgs;
    public float[] floatArgs;
    public long[] longArgs;
    public boolean[] boolArgs;
    public String[] stringArgs;
    public Class[] classArgs;
    public Object[] objectArgs;
    public int Command;

    public CommandObject(int n2) {
        this.Command = n2;
    }

    public CommandObject(int n2, int n3) {
        this.Command = n2;
        this.intArgs = new int[1];
        this.intArgs[0] = n3;
    }

    public CommandObject(int n2, Class clazz) {
        this.Command = n2;
        this.classArgs = new Class[1];
        this.classArgs[0] = clazz;
    }

    public CommandObject(int n2, float f2) {
        this.Command = n2;
        this.floatArgs = new float[1];
        this.floatArgs[0] = f2;
    }

    public CommandObject(int n2, String string) {
        this.Command = n2;
        this.stringArgs = new String[1];
        this.stringArgs[0] = string;
    }

    public CommandObject(int n2, Object object) {
        this.Command = n2;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = object;
    }

    public CommandObject(int n2, String string, Object object) {
        this.Command = n2;
        this.stringArgs = new String[1];
        this.stringArgs[0] = string;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = object;
    }

    public CommandObject(int n2, String string, byte[] byArray) {
        this.Command = n2;
        this.stringArgs = new String[1];
        this.stringArgs[0] = string;
        this.buffer = byArray;
    }

    public CommandObject(int n2, String string, Object object, long l2) {
        this.Command = n2;
        this.stringArgs = new String[1];
        this.stringArgs[0] = string;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = object;
        this.longArgs = new long[1];
        this.longArgs[0] = l2;
    }

    public CommandObject(int n2, String string, Object object, long l2, long l3) {
        this.Command = n2;
        this.stringArgs = new String[1];
        this.stringArgs[0] = string;
        this.objectArgs = new Object[1];
        this.objectArgs[0] = object;
        this.longArgs = new long[2];
        this.longArgs[0] = l2;
        this.longArgs[1] = l3;
    }

    public CommandObject(int n2, String string, String string2) {
        this.Command = n2;
        this.stringArgs = new String[2];
        this.stringArgs[0] = string;
        this.stringArgs[1] = string2;
    }

    public CommandObject(int n2, String string, int n3) {
        this.Command = n2;
        this.intArgs = new int[1];
        this.stringArgs = new String[1];
        this.intArgs[0] = n3;
        this.stringArgs[0] = string;
    }

    public CommandObject(int n2, String string, float f2) {
        this.Command = n2;
        this.floatArgs = new float[1];
        this.stringArgs = new String[1];
        this.floatArgs[0] = f2;
        this.stringArgs[0] = string;
    }

    public CommandObject(int n2, String string, boolean bl2) {
        this.Command = n2;
        this.boolArgs = new boolean[1];
        this.stringArgs = new String[1];
        this.boolArgs[0] = bl2;
        this.stringArgs[0] = string;
    }

    public CommandObject(int n2, float f2, float f3, float f4) {
        this.Command = n2;
        this.floatArgs = new float[3];
        this.floatArgs[0] = f2;
        this.floatArgs[1] = f3;
        this.floatArgs[2] = f4;
    }

    public CommandObject(int n2, String string, float f2, float f3, float f4) {
        this.Command = n2;
        this.floatArgs = new float[3];
        this.stringArgs = new String[1];
        this.floatArgs[0] = f2;
        this.floatArgs[1] = f3;
        this.floatArgs[2] = f4;
        this.stringArgs[0] = string;
    }

    public CommandObject(int n2, float f2, float f3, float f4, float f5, float f6, float f7) {
        this.Command = n2;
        this.floatArgs = new float[6];
        this.floatArgs[0] = f2;
        this.floatArgs[1] = f3;
        this.floatArgs[2] = f4;
        this.floatArgs[3] = f5;
        this.floatArgs[4] = f6;
        this.floatArgs[5] = f7;
    }

    public CommandObject(int n2, boolean bl2, boolean bl3, boolean bl4, String string, Object object, float f2, float f3, float f4, int n3, float f5) {
        this.Command = n2;
        this.intArgs = new int[1];
        this.floatArgs = new float[4];
        this.boolArgs = new boolean[3];
        this.stringArgs = new String[1];
        this.objectArgs = new Object[1];
        this.intArgs[0] = n3;
        this.floatArgs[0] = f2;
        this.floatArgs[1] = f3;
        this.floatArgs[2] = f4;
        this.floatArgs[3] = f5;
        this.boolArgs[0] = bl2;
        this.boolArgs[1] = bl3;
        this.boolArgs[2] = bl4;
        this.stringArgs[0] = string;
        this.objectArgs[0] = object;
    }

    public CommandObject(int n2, boolean bl2, boolean bl3, boolean bl4, String string, Object object, float f2, float f3, float f4, int n3, float f5, boolean bl5) {
        this.Command = n2;
        this.intArgs = new int[1];
        this.floatArgs = new float[4];
        this.boolArgs = new boolean[4];
        this.stringArgs = new String[1];
        this.objectArgs = new Object[1];
        this.intArgs[0] = n3;
        this.floatArgs[0] = f2;
        this.floatArgs[1] = f3;
        this.floatArgs[2] = f4;
        this.floatArgs[3] = f5;
        this.boolArgs[0] = bl2;
        this.boolArgs[1] = bl3;
        this.boolArgs[2] = bl4;
        this.boolArgs[3] = bl5;
        this.stringArgs[0] = string;
        this.objectArgs[0] = object;
    }

    public CommandObject(int n2, Object object, boolean bl2, String string, float f2, float f3, float f4, int n3, float f5) {
        this.Command = n2;
        this.intArgs = new int[1];
        this.floatArgs = new float[4];
        this.boolArgs = new boolean[1];
        this.stringArgs = new String[1];
        this.objectArgs = new Object[1];
        this.intArgs[0] = n3;
        this.floatArgs[0] = f2;
        this.floatArgs[1] = f3;
        this.floatArgs[2] = f4;
        this.floatArgs[3] = f5;
        this.boolArgs[0] = bl2;
        this.stringArgs[0] = string;
        this.objectArgs[0] = object;
    }
}

