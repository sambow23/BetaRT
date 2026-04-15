/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class o
extends ko {
    public ps a;
    public ps b;
    public ps c;
    public ps d;
    public ps e;
    public ps f;
    ps g;
    ps h;
    ps i;
    ps j;
    ps k;

    public o() {
        float f2 = 0.0f;
        float f3 = 13.5f;
        this.a = new ps(0, 0);
        this.a.a(-3.0f, -3.0f, -2.0f, 6, 6, 4, f2);
        this.a.a(-1.0f, f3, -7.0f);
        this.b = new ps(18, 14);
        this.b.a(-4.0f, -2.0f, -3.0f, 6, 9, 6, f2);
        this.b.a(0.0f, 14.0f, 2.0f);
        this.k = new ps(21, 0);
        this.k.a(-4.0f, -3.0f, -3.0f, 8, 6, 7, f2);
        this.k.a(-1.0f, 14.0f, 2.0f);
        this.c = new ps(0, 18);
        this.c.a(-1.0f, 0.0f, -1.0f, 2, 8, 2, f2);
        this.c.a(-2.5f, 16.0f, 7.0f);
        this.d = new ps(0, 18);
        this.d.a(-1.0f, 0.0f, -1.0f, 2, 8, 2, f2);
        this.d.a(0.5f, 16.0f, 7.0f);
        this.e = new ps(0, 18);
        this.e.a(-1.0f, 0.0f, -1.0f, 2, 8, 2, f2);
        this.e.a(-2.5f, 16.0f, -4.0f);
        this.f = new ps(0, 18);
        this.f.a(-1.0f, 0.0f, -1.0f, 2, 8, 2, f2);
        this.f.a(0.5f, 16.0f, -4.0f);
        this.j = new ps(9, 18);
        this.j.a(-1.0f, 0.0f, -1.0f, 2, 8, 2, f2);
        this.j.a(-1.0f, 12.0f, 8.0f);
        this.g = new ps(16, 14);
        this.g.a(-3.0f, -5.0f, 0.0f, 2, 2, 1, f2);
        this.g.a(-1.0f, f3, -7.0f);
        this.h = new ps(16, 14);
        this.h.a(1.0f, -5.0f, 0.0f, 2, 2, 1, f2);
        this.h.a(-1.0f, f3, -7.0f);
        this.i = new ps(0, 10);
        this.i.a(-2.0f, 0.0f, -5.0f, 3, 3, 4, f2);
        this.i.a(-0.5f, f3, -7.0f);
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        super.a(f2, f3, f4, f5, f6, f7);
        this.b(f2, f3, f4, f5, f6, f7);
        this.a.b(f7);
        this.b.a(f7);
        this.c.a(f7);
        this.d.a(f7);
        this.e.a(f7);
        this.f.a(f7);
        this.g.b(f7);
        this.h.b(f7);
        this.i.b(f7);
        this.j.b(f7);
        this.k.a(f7);
    }

    public void a(ls ls2, float f2, float f3, float f4) {
        float f5;
        gi gi2 = (gi)ls2;
        this.j.e = gi2.C() ? 0.0f : in.b(f2 * 0.6662f) * 1.4f * f3;
        if (gi2.B()) {
            this.k.a(-1.0f, 16.0f, -3.0f);
            this.k.d = 1.2566371f;
            this.k.e = 0.0f;
            this.b.a(0.0f, 18.0f, 0.0f);
            this.b.d = 0.7853982f;
            this.j.a(-1.0f, 21.0f, 6.0f);
            this.c.a(-2.5f, 22.0f, 2.0f);
            this.c.d = 4.712389f;
            this.d.a(0.5f, 22.0f, 2.0f);
            this.d.d = 4.712389f;
            this.e.d = 5.811947f;
            this.e.a(-2.49f, 17.0f, -4.0f);
            this.f.d = 5.811947f;
            this.f.a(0.51f, 17.0f, -4.0f);
        } else {
            this.b.a(0.0f, 14.0f, 2.0f);
            this.b.d = 1.5707964f;
            this.k.a(-1.0f, 14.0f, -3.0f);
            this.k.d = this.b.d;
            this.j.a(-1.0f, 12.0f, 8.0f);
            this.c.a(-2.5f, 16.0f, 7.0f);
            this.d.a(0.5f, 16.0f, 7.0f);
            this.e.a(-2.5f, 16.0f, -4.0f);
            this.f.a(0.5f, 16.0f, -4.0f);
            this.c.d = in.b(f2 * 0.6662f) * 1.4f * f3;
            this.d.d = in.b(f2 * 0.6662f + (float)Math.PI) * 1.4f * f3;
            this.e.d = in.b(f2 * 0.6662f + (float)Math.PI) * 1.4f * f3;
            this.f.d = in.b(f2 * 0.6662f) * 1.4f * f3;
        }
        this.a.f = f5 = gi2.c(f4) + gi2.a(f4, 0.0f);
        this.g.f = f5;
        this.h.f = f5;
        this.i.f = f5;
        this.k.f = gi2.a(f4, -0.08f);
        this.b.f = gi2.a(f4, -0.16f);
        this.j.f = gi2.a(f4, -0.2f);
        if (gi2.v()) {
            float f6 = gi2.a(f4) * gi2.b_(f4);
            GL11.glColor3f((float)f6, (float)f6, (float)f6);
        }
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
        super.b(f2, f3, f4, f5, f6, f7);
        this.a.d = f6 / 57.295776f;
        this.g.e = this.a.e = f5 / 57.295776f;
        this.g.d = this.a.d;
        this.h.e = this.a.e;
        this.h.d = this.a.d;
        this.i.e = this.a.e;
        this.i.d = this.a.d;
        this.j.d = f4;
    }
}

