/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;

public class tg {
    private int a;
    private int b;
    private int c;
    private float d;
    private float e;
    private float f;
    private IntBuffer g = ge.d(65536);
    private boolean h = false;
    private boolean i = false;

    public void a(int n2, int n3, int n4, double d2, double d3, double d4) {
        this.h = true;
        this.g.clear();
        this.a = n2;
        this.b = n3;
        this.c = n4;
        this.d = (float)d2;
        this.e = (float)d3;
        this.f = (float)d4;
    }

    public boolean a(int n2, int n3, int n4) {
        if (!this.h) {
            return false;
        }
        return n2 == this.a && n3 == this.b && n4 == this.c;
    }

    public void a(int n2) {
        this.g.put(n2);
        if (this.g.remaining() == 0) {
            this.a();
        }
    }

    public void a() {
        if (!this.h) {
            return;
        }
        if (!this.i) {
            this.g.flip();
            this.i = true;
        }
        if (this.g.remaining() > 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)((float)this.a - this.d), (float)((float)this.b - this.e), (float)((float)this.c - this.f));
            GL11.glCallLists((IntBuffer)this.g);
            GL11.glPopMatrix();
        }
    }

    public void b() {
        this.h = false;
        this.i = false;
    }
}

