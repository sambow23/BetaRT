/*
 * Decompiled with CFR 0.152.
 */
public class nq
extends ko {
    public ps[] a = new ps[7];

    public nq() {
        this.a[0] = new ps(0, 10);
        this.a[1] = new ps(0, 0);
        this.a[2] = new ps(0, 0);
        this.a[3] = new ps(0, 0);
        this.a[4] = new ps(0, 0);
        this.a[5] = new ps(44, 10);
        int n2 = 20;
        int n3 = 8;
        int n4 = 16;
        int n5 = 4;
        this.a[0].a(-n2 / 2, -n4 / 2, -1.0f, n2, n4, 2, 0.0f);
        this.a[0].a(0.0f, 0 + n5, 0.0f);
        this.a[5].a(-n2 / 2 + 1, -n4 / 2 + 1, -1.0f, n2 - 2, n4 - 2, 1, 0.0f);
        this.a[5].a(0.0f, 0 + n5, 0.0f);
        this.a[1].a(-n2 / 2 + 2, -n3 - 1, -1.0f, n2 - 4, n3, 2, 0.0f);
        this.a[1].a(-n2 / 2 + 1, 0 + n5, 0.0f);
        this.a[2].a(-n2 / 2 + 2, -n3 - 1, -1.0f, n2 - 4, n3, 2, 0.0f);
        this.a[2].a(n2 / 2 - 1, 0 + n5, 0.0f);
        this.a[3].a(-n2 / 2 + 2, -n3 - 1, -1.0f, n2 - 4, n3, 2, 0.0f);
        this.a[3].a(0.0f, 0 + n5, -n4 / 2 + 1);
        this.a[4].a(-n2 / 2 + 2, -n3 - 1, -1.0f, n2 - 4, n3, 2, 0.0f);
        this.a[4].a(0.0f, 0 + n5, n4 / 2 - 1);
        this.a[0].d = 1.5707964f;
        this.a[1].e = 4.712389f;
        this.a[2].e = 1.5707964f;
        this.a[3].e = (float)Math.PI;
        this.a[5].d = -1.5707964f;
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.a[5].b = 4.0f - f4;
        for (int i2 = 0; i2 < 6; ++i2) {
            this.a[i2].a(f7);
        }
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
    }
}

