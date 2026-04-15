/*
 * Decompiled with CFR 0.152.
 */
public class il
extends ko {
    public ps a;
    public ps b;
    public ps c;
    public ps d;
    public ps e;
    public ps f;
    public ps g;

    public il() {
        this(0.0f);
    }

    public il(float f2) {
        int n2 = 4;
        this.a = new ps(0, 0);
        this.a.a(-4.0f, -8.0f, -4.0f, 8, 8, 8, f2);
        this.a.a(0.0f, n2, 0.0f);
        this.b = new ps(32, 0);
        this.b.a(-4.0f, -8.0f, -4.0f, 8, 8, 8, f2 + 0.5f);
        this.b.a(0.0f, n2, 0.0f);
        this.c = new ps(16, 16);
        this.c.a(-4.0f, 0.0f, -2.0f, 8, 12, 4, f2);
        this.c.a(0.0f, n2, 0.0f);
        this.d = new ps(0, 16);
        this.d.a(-2.0f, 0.0f, -2.0f, 4, 6, 4, f2);
        this.d.a(-2.0f, 12 + n2, 4.0f);
        this.e = new ps(0, 16);
        this.e.a(-2.0f, 0.0f, -2.0f, 4, 6, 4, f2);
        this.e.a(2.0f, 12 + n2, 4.0f);
        this.f = new ps(0, 16);
        this.f.a(-2.0f, 0.0f, -2.0f, 4, 6, 4, f2);
        this.f.a(-2.0f, 12 + n2, -4.0f);
        this.g = new ps(0, 16);
        this.g.a(-2.0f, 0.0f, -2.0f, 4, 6, 4, f2);
        this.g.a(2.0f, 12 + n2, -4.0f);
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.b(f2, f3, f4, f5, f6, f7);
        this.a.a(f7);
        this.c.a(f7);
        this.d.a(f7);
        this.e.a(f7);
        this.f.a(f7);
        this.g.a(f7);
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.a.e = f5 / 57.295776f;
        this.a.d = f6 / 57.295776f;
        this.d.d = in.b(f2 * 0.6662f) * 1.4f * f3;
        this.e.d = in.b(f2 * 0.6662f + (float)Math.PI) * 1.4f * f3;
        this.f.d = in.b(f2 * 0.6662f + (float)Math.PI) * 1.4f * f3;
        this.g.d = in.b(f2 * 0.6662f) * 1.4f * f3;
    }
}

