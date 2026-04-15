/*
 * Decompiled with CFR 0.152.
 */
public class no
extends ko {
    ps a;
    ps b;
    ps c;
    ps d;

    public no(int n2) {
        this.a = new ps(0, n2);
        this.a.a(-4.0f, 16.0f, -4.0f, 8, 8, 8);
        if (n2 > 0) {
            this.a = new ps(0, n2);
            this.a.a(-3.0f, 17.0f, -3.0f, 6, 6, 6);
            this.b = new ps(32, 0);
            this.b.a(-3.25f, 18.0f, -3.5f, 2, 2, 2);
            this.c = new ps(32, 4);
            this.c.a(1.25f, 18.0f, -3.5f, 2, 2, 2);
            this.d = new ps(32, 8);
            this.d.a(0.0f, 21.0f, -3.5f, 1, 1, 1);
        }
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.b(f2, f3, f4, f5, f6, f7);
        this.a.a(f7);
        if (this.b != null) {
            this.b.a(f7);
            this.c.a(f7);
            this.d.a(f7);
        }
    }
}

