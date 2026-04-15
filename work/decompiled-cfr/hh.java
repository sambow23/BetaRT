/*
 * Decompiled with CFR 0.152.
 */
public class hh
extends nn {
    ps a;
    ps b;
    ps c;

    public hh() {
        super(12, 0.0f);
        this.d = new ps(0, 0);
        this.d.a(-4.0f, -4.0f, -6.0f, 8, 8, 6, 0.0f);
        this.d.a(0.0f, 4.0f, -8.0f);
        this.b = new ps(22, 0);
        this.b.a(-4.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f);
        this.b.a(0.0f, 3.0f, -7.0f);
        this.c = new ps(22, 0);
        this.c.a(3.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f);
        this.c.a(0.0f, 3.0f, -7.0f);
        this.a = new ps(52, 0);
        this.a.a(-2.0f, -3.0f, 0.0f, 4, 6, 2, 0.0f);
        this.a.a(0.0f, 14.0f, 6.0f);
        this.a.d = 1.5707964f;
        this.e = new ps(18, 4);
        this.e.a(-6.0f, -10.0f, -7.0f, 12, 18, 10, 0.0f);
        this.e.a(0.0f, 5.0f, 2.0f);
        this.f.a -= 1.0f;
        this.g.a += 1.0f;
        this.f.c += 0.0f;
        this.g.c += 0.0f;
        this.h.a -= 1.0f;
        this.i.a += 1.0f;
        this.h.c -= 1.0f;
        this.i.c -= 1.0f;
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        super.a(f2, f3, f4, f5, f6, f7);
        this.b.a(f7);
        this.c.a(f7);
        this.a.a(f7);
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
        super.b(f2, f3, f4, f5, f6, f7);
        this.b.e = this.d.e;
        this.b.d = this.d.d;
        this.c.e = this.d.e;
        this.c.d = this.d.d;
    }
}

