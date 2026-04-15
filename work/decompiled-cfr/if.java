/*
 * Decompiled with CFR 0.152.
 */
public class if
extends aw {
    protected float[] g = new float[256];
    protected float[] h = new float[256];
    protected float[] i = new float[256];
    protected float[] j = new float[256];
    int k = 0;

    public if() {
        super(uu.D.bm + 1);
        this.e = 2;
    }

    public void a() {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        float f2;
        int n7;
        ++this.k;
        for (int i2 = 0; i2 < 16; ++i2) {
            for (n7 = 0; n7 < 16; ++n7) {
                f2 = 0.0f;
                int n8 = (int)(in.a((float)n7 * (float)Math.PI * 2.0f / 16.0f) * 1.2f);
                n6 = (int)(in.a((float)i2 * (float)Math.PI * 2.0f / 16.0f) * 1.2f);
                for (n5 = i2 - 1; n5 <= i2 + 1; ++n5) {
                    for (n4 = n7 - 1; n4 <= n7 + 1; ++n4) {
                        n3 = n5 + n8 & 0xF;
                        n2 = n4 + n6 & 0xF;
                        f2 += this.g[n3 + n2 * 16];
                    }
                }
                this.h[i2 + n7 * 16] = f2 / 10.0f + (this.i[(i2 + 0 & 0xF) + (n7 + 0 & 0xF) * 16] + this.i[(i2 + 1 & 0xF) + (n7 + 0 & 0xF) * 16] + this.i[(i2 + 1 & 0xF) + (n7 + 1 & 0xF) * 16] + this.i[(i2 + 0 & 0xF) + (n7 + 1 & 0xF) * 16]) / 4.0f * 0.8f;
                int n9 = i2 + n7 * 16;
                this.i[n9] = this.i[n9] + this.j[i2 + n7 * 16] * 0.01f;
                if (this.i[i2 + n7 * 16] < 0.0f) {
                    this.i[i2 + n7 * 16] = 0.0f;
                }
                int n10 = i2 + n7 * 16;
                this.j[n10] = this.j[n10] - 0.06f;
                if (!(Math.random() < 0.005)) continue;
                this.j[i2 + n7 * 16] = 1.5f;
            }
        }
        float[] fArray = this.h;
        this.h = this.g;
        this.g = fArray;
        for (n7 = 0; n7 < 256; ++n7) {
            f2 = this.g[n7 - this.k / 3 * 16 & 0xFF] * 2.0f;
            if (f2 > 1.0f) {
                f2 = 1.0f;
            }
            if (f2 < 0.0f) {
                f2 = 0.0f;
            }
            float f3 = f2;
            n6 = (int)(f3 * 100.0f + 155.0f);
            n5 = (int)(f3 * f3 * 255.0f);
            n4 = (int)(f3 * f3 * f3 * f3 * 128.0f);
            if (this.c) {
                n3 = (n6 * 30 + n5 * 59 + n4 * 11) / 100;
                n2 = (n6 * 30 + n5 * 70) / 100;
                int n11 = (n6 * 30 + n4 * 70) / 100;
                n6 = n3;
                n5 = n2;
                n4 = n11;
            }
            this.a[n7 * 4 + 0] = (byte)n6;
            this.a[n7 * 4 + 1] = (byte)n5;
            this.a[n7 * 4 + 2] = (byte)n4;
            this.a[n7 * 4 + 3] = -1;
        }
    }
}

