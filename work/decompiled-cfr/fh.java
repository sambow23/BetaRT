/*
 * Decompiled with CFR 0.152.
 */
public class fh
extends ko {
    public ps a;
    public ps b;
    public ps c;
    public ps d;
    public ps e;
    public ps f;
    public ps g;
    public ps h;
    public ps i = new ps(0, 0);
    public boolean j = false;
    public boolean k = false;
    public boolean l = false;

    public fh() {
        this(0.0f);
    }

    public fh(float f2) {
        this(f2, 0.0f);
    }

    public fh(float f2, float f3) {
        this.i.a(-5.0f, 0.0f, -1.0f, 10, 16, 1, f2);
        this.h = new ps(24, 0);
        this.h.a(-3.0f, -6.0f, -1.0f, 6, 6, 1, f2);
        this.a = new ps(0, 0);
        this.a.a(-4.0f, -8.0f, -4.0f, 8, 8, 8, f2);
        this.a.a(0.0f, 0.0f + f3, 0.0f);
        this.b = new ps(32, 0);
        this.b.a(-4.0f, -8.0f, -4.0f, 8, 8, 8, f2 + 0.5f);
        this.b.a(0.0f, 0.0f + f3, 0.0f);
        this.c = new ps(16, 16);
        this.c.a(-4.0f, 0.0f, -2.0f, 8, 12, 4, f2);
        this.c.a(0.0f, 0.0f + f3, 0.0f);
        this.d = new ps(40, 16);
        this.d.a(-3.0f, -2.0f, -2.0f, 4, 12, 4, f2);
        this.d.a(-5.0f, 2.0f + f3, 0.0f);
        this.e = new ps(40, 16);
        this.e.g = true;
        this.e.a(-1.0f, -2.0f, -2.0f, 4, 12, 4, f2);
        this.e.a(5.0f, 2.0f + f3, 0.0f);
        this.f = new ps(0, 16);
        this.f.a(-2.0f, 0.0f, -2.0f, 4, 12, 4, f2);
        this.f.a(-2.0f, 12.0f + f3, 0.0f);
        this.g = new ps(0, 16);
        this.g.g = true;
        this.g.a(-2.0f, 0.0f, -2.0f, 4, 12, 4, f2);
        this.g.a(2.0f, 12.0f + f3, 0.0f);
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.b(f2, f3, f4, f5, f6, f7);
        this.a.a(f7);
        this.c.a(f7);
        this.d.a(f7);
        this.e.a(f7);
        this.f.a(f7);
        this.g.a(f7);
        this.b.a(f7);
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.a.e = f5 / 57.295776f;
        this.a.d = f6 / 57.295776f;
        this.b.e = this.a.e;
        this.b.d = this.a.d;
        this.d.d = in.b(f2 * 0.6662f + (float)Math.PI) * 2.0f * f3 * 0.5f;
        this.e.d = in.b(f2 * 0.6662f) * 2.0f * f3 * 0.5f;
        this.d.f = 0.0f;
        this.e.f = 0.0f;
        this.f.d = in.b(f2 * 0.6662f) * 1.4f * f3;
        this.g.d = in.b(f2 * 0.6662f + (float)Math.PI) * 1.4f * f3;
        this.f.e = 0.0f;
        this.g.e = 0.0f;
        if (this.n) {
            this.d.d += -0.62831855f;
            this.e.d += -0.62831855f;
            this.f.d = -1.2566371f;
            this.g.d = -1.2566371f;
            this.f.e = 0.31415927f;
            this.g.e = -0.31415927f;
        }
        if (this.j) {
            this.e.d = this.e.d * 0.5f - 0.31415927f;
        }
        if (this.k) {
            this.d.d = this.d.d * 0.5f - 0.31415927f;
        }
        this.d.e = 0.0f;
        this.e.e = 0.0f;
        if (this.m > -9990.0f) {
            float f8 = this.m;
            this.c.e = in.a(in.c(f8) * (float)Math.PI * 2.0f) * 0.2f;
            this.d.c = in.a(this.c.e) * 5.0f;
            this.d.a = -in.b(this.c.e) * 5.0f;
            this.e.c = -in.a(this.c.e) * 5.0f;
            this.e.a = in.b(this.c.e) * 5.0f;
            this.d.e += this.c.e;
            this.e.e += this.c.e;
            this.e.d += this.c.e;
            f8 = 1.0f - this.m;
            f8 *= f8;
            f8 *= f8;
            f8 = 1.0f - f8;
            float f9 = in.a(f8 * (float)Math.PI);
            float f10 = in.a(this.m * (float)Math.PI) * -(this.a.d - 0.7f) * 0.75f;
            this.d.d = (float)((double)this.d.d - ((double)f9 * 1.2 + (double)f10));
            this.d.e += this.c.e * 2.0f;
            this.d.f = in.a(this.m * (float)Math.PI) * -0.4f;
        }
        if (this.l) {
            this.c.d = 0.5f;
            this.f.d -= 0.0f;
            this.g.d -= 0.0f;
            this.d.d += 0.4f;
            this.e.d += 0.4f;
            this.f.c = 4.0f;
            this.g.c = 4.0f;
            this.f.b = 9.0f;
            this.g.b = 9.0f;
            this.a.b = 1.0f;
        } else {
            this.c.d = 0.0f;
            this.f.c = 0.0f;
            this.g.c = 0.0f;
            this.f.b = 12.0f;
            this.g.b = 12.0f;
            this.a.b = 0.0f;
        }
        this.d.f += in.b(f4 * 0.09f) * 0.05f + 0.05f;
        this.e.f -= in.b(f4 * 0.09f) * 0.05f + 0.05f;
        this.d.d += in.a(f4 * 0.067f) * 0.05f;
        this.e.d -= in.a(f4 * 0.067f) * 0.05f;
    }

    public void a(float f2) {
        this.h.e = this.a.e;
        this.h.d = this.a.d;
        this.h.a = 0.0f;
        this.h.b = 0.0f;
        this.h.a(f2);
    }

    public void b(float f2) {
        this.i.a(f2);
    }
}

