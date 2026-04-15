/*
 * Decompiled with CFR 0.152.
 */
public class vs
extends aw {
    protected float[] g = new float[256];
    protected float[] h = new float[256];
    protected float[] i = new float[256];
    protected float[] j = new float[256];
    private int k = 0;

    public vs() {
        super(uu.B.bm);
    }

    public void a() {
        int n2;
        int n3;
        float f2;
        int n4;
        int n5;
        ++this.k;
        for (n5 = 0; n5 < 16; ++n5) {
            for (n4 = 0; n4 < 16; ++n4) {
                f2 = 0.0f;
                for (int i2 = n5 - 1; i2 <= n5 + 1; ++i2) {
                    n3 = i2 & 0xF;
                    n2 = n4 & 0xF;
                    f2 += this.g[n3 + n2 * 16];
                }
                this.h[n5 + n4 * 16] = f2 / 3.3f + this.i[n5 + n4 * 16] * 0.8f;
            }
        }
        for (n5 = 0; n5 < 16; ++n5) {
            for (n4 = 0; n4 < 16; ++n4) {
                int n6 = n5 + n4 * 16;
                this.i[n6] = this.i[n6] + this.j[n5 + n4 * 16] * 0.05f;
                if (this.i[n5 + n4 * 16] < 0.0f) {
                    this.i[n5 + n4 * 16] = 0.0f;
                }
                int n7 = n5 + n4 * 16;
                this.j[n7] = this.j[n7] - 0.1f;
                if (!(Math.random() < 0.05)) continue;
                this.j[n5 + n4 * 16] = 0.5f;
            }
        }
        float[] fArray = this.h;
        this.h = this.g;
        this.g = fArray;
        for (n4 = 0; n4 < 256; ++n4) {
            f2 = this.g[n4];
            if (f2 > 1.0f) {
                f2 = 1.0f;
            }
            if (f2 < 0.0f) {
                f2 = 0.0f;
            }
            float f3 = f2 * f2;
            n3 = (int)(32.0f + f3 * 32.0f);
            n2 = (int)(50.0f + f3 * 64.0f);
            int n8 = 255;
            int n9 = (int)(146.0f + f3 * 50.0f);
            if (this.c) {
                int n10 = (n3 * 30 + n2 * 59 + n8 * 11) / 100;
                int n11 = (n3 * 30 + n2 * 70) / 100;
                int n12 = (n3 * 30 + n8 * 70) / 100;
                n3 = n10;
                n2 = n11;
                n8 = n12;
            }
            this.a[n4 * 4 + 0] = (byte)n3;
            this.a[n4 * 4 + 1] = (byte)n2;
            this.a[n4 * 4 + 2] = (byte)n8;
            this.a[n4 * 4 + 3] = (byte)n9;
        }
    }
}

