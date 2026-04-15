/*
 * Decompiled with CFR 0.152.
 */
public class sd
extends aw {
    protected float[] g = new float[320];
    protected float[] h = new float[320];

    public sd(int n2) {
        super(uu.as.bm + n2 * 16);
    }

    public void a() {
        int n2;
        int n3;
        int n4;
        int n5;
        float f2;
        int n6;
        for (int i2 = 0; i2 < 16; ++i2) {
            for (n6 = 0; n6 < 20; ++n6) {
                int n7 = 18;
                f2 = this.g[i2 + (n6 + 1) % 20 * 16] * (float)n7;
                for (n5 = i2 - 1; n5 <= i2 + 1; ++n5) {
                    for (n4 = n6; n4 <= n6 + 1; ++n4) {
                        n3 = n5;
                        n2 = n4;
                        if (n3 >= 0 && n2 >= 0 && n3 < 16 && n2 < 20) {
                            f2 += this.g[n3 + n2 * 16];
                        }
                        ++n7;
                    }
                }
                this.h[i2 + n6 * 16] = f2 / ((float)n7 * 1.06f);
                if (n6 < 19) continue;
                this.h[i2 + n6 * 16] = (float)(Math.random() * Math.random() * Math.random() * 4.0 + Math.random() * (double)0.1f + (double)0.2f);
            }
        }
        float[] fArray = this.h;
        this.h = this.g;
        this.g = fArray;
        for (n6 = 0; n6 < 256; ++n6) {
            float f3 = this.g[n6] * 1.8f;
            if (f3 > 1.0f) {
                f3 = 1.0f;
            }
            if (f3 < 0.0f) {
                f3 = 0.0f;
            }
            f2 = f3;
            n5 = (int)(f2 * 155.0f + 100.0f);
            n4 = (int)(f2 * f2 * 255.0f);
            n3 = (int)(f2 * f2 * f2 * f2 * f2 * f2 * f2 * f2 * f2 * f2 * 255.0f);
            n2 = 255;
            if (f2 < 0.5f) {
                n2 = 0;
            }
            f2 = (f2 - 0.5f) * 2.0f;
            if (this.c) {
                int n8 = (n5 * 30 + n4 * 59 + n3 * 11) / 100;
                int n9 = (n5 * 30 + n4 * 70) / 100;
                int n10 = (n5 * 30 + n3 * 70) / 100;
                n5 = n8;
                n4 = n9;
                n3 = n10;
            }
            this.a[n6 * 4 + 0] = (byte)n5;
            this.a[n6 * 4 + 1] = (byte)n4;
            this.a[n6 * 4 + 2] = (byte)n3;
            this.a[n6 * 4 + 3] = (byte)n2;
        }
    }
}

