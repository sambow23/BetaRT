/*
 * Decompiled with CFR 0.152.
 */
public class wn
extends ko {
    ps a;
    ps[] b = new ps[8];

    public wn() {
        int n2 = -16;
        this.a = new ps(0, 0);
        this.a.a(-6.0f, -8.0f, -6.0f, 12, 16, 12);
        this.a.b += (float)(24 + n2);
        for (int i2 = 0; i2 < this.b.length; ++i2) {
            this.b[i2] = new ps(48, 0);
            double d2 = (double)i2 * Math.PI * 2.0 / (double)this.b.length;
            float f2 = (float)Math.cos(d2) * 5.0f;
            float f3 = (float)Math.sin(d2) * 5.0f;
            this.b[i2].a(-1.0f, 0.0f, -1.0f, 2, 18, 2);
            this.b[i2].a = f2;
            this.b[i2].c = f3;
            this.b[i2].b = 31 + n2;
            d2 = (double)i2 * Math.PI * -2.0 / (double)this.b.length + 1.5707963267948966;
            this.b[i2].e = (float)d2;
        }
    }

    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
        for (int i2 = 0; i2 < this.b.length; ++i2) {
            this.b[i2].d = f4;
        }
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.b(f2, f3, f4, f5, f6, f7);
        this.a.a(f7);
        for (int i2 = 0; i2 < this.b.length; ++i2) {
            this.b[i2].a(f7);
        }
    }
}

