/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.LWJGLException
 *  org.lwjgl.input.Cursor
 *  org.lwjgl.input.Mouse
 */
import java.awt.Component;
import java.nio.IntBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

public class vy {
    private Component c;
    private Cursor d;
    public int a;
    public int b;
    private int e = 10;

    public vy(Component component) {
        this.c = component;
        IntBuffer intBuffer = ge.d(1);
        intBuffer.put(0);
        intBuffer.flip();
        IntBuffer intBuffer2 = ge.d(1024);
        try {
            this.d = new Cursor(32, 32, 16, 16, 1, intBuffer2, intBuffer);
        }
        catch (LWJGLException lWJGLException) {
            lWJGLException.printStackTrace();
        }
    }

    public void a() {
        Mouse.setGrabbed((boolean)true);
        this.a = 0;
        this.b = 0;
    }

    public void b() {
        Mouse.setCursorPosition((int)(this.c.getWidth() / 2), (int)(this.c.getHeight() / 2));
        Mouse.setGrabbed((boolean)false);
    }

    public void c() {
        this.a = Mouse.getDX();
        this.b = Mouse.getDY();
    }
}

