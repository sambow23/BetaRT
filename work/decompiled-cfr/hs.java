/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class hs
extends aw {
    private int g = 0;
    private byte[][] h = new byte[32][1024];

    public hs() {
        super(uu.bf.bm);
        Random random = new Random(100L);
        for (int i2 = 0; i2 < 32; ++i2) {
            for (int i3 = 0; i3 < 16; ++i3) {
                for (int i4 = 0; i4 < 16; ++i4) {
                    int n2;
                    float f2 = 0.0f;
                    for (n2 = 0; n2 < 2; ++n2) {
                        float f3 = n2 * 8;
                        float f4 = n2 * 8;
                        float f5 = ((float)i3 - f3) / 16.0f * 2.0f;
                        float f6 = ((float)i4 - f4) / 16.0f * 2.0f;
                        if (f5 < -1.0f) {
                            f5 += 2.0f;
                        }
                        if (f5 >= 1.0f) {
                            f5 -= 2.0f;
                        }
                        if (f6 < -1.0f) {
                            f6 += 2.0f;
                        }
                        if (f6 >= 1.0f) {
                            f6 -= 2.0f;
                        }
                        float f7 = f5 * f5 + f6 * f6;
                        float f8 = (float)Math.atan2(f6, f5) + ((float)i2 / 32.0f * (float)Math.PI * 2.0f - f7 * 10.0f + (float)(n2 * 2)) * (float)(n2 * 2 - 1);
                        f8 = (in.a(f8) + 1.0f) / 2.0f;
                        f2 += (f8 /= f7 + 1.0f) * 0.5f;
                    }
                    n2 = (int)((f2 += random.nextFloat() * 0.1f) * 100.0f + 155.0f);
                    int n3 = (int)(f2 * f2 * 200.0f + 55.0f);
                    int n4 = (int)(f2 * f2 * f2 * f2 * 255.0f);
                    int n5 = (int)(f2 * 100.0f + 155.0f);
                    int n6 = i4 * 16 + i3;
                    this.h[i2][n6 * 4 + 0] = (byte)n3;
                    this.h[i2][n6 * 4 + 1] = (byte)n4;
                    this.h[i2][n6 * 4 + 2] = (byte)n2;
                    this.h[i2][n6 * 4 + 3] = (byte)n5;
                }
            }
        }
    }

    public void a() {
        ++this.g;
        byte[] byArray = this.h[this.g & 0x1F];
        for (int i2 = 0; i2 < 256; ++i2) {
            int n2 = byArray[i2 * 4 + 0] & 0xFF;
            int n3 = byArray[i2 * 4 + 1] & 0xFF;
            int n4 = byArray[i2 * 4 + 2] & 0xFF;
            int n5 = byArray[i2 * 4 + 3] & 0xFF;
            if (this.c) {
                int n6 = (n2 * 30 + n3 * 59 + n4 * 11) / 100;
                int n7 = (n2 * 30 + n3 * 70) / 100;
                int n8 = (n2 * 30 + n4 * 70) / 100;
                n2 = n6;
                n3 = n7;
                n4 = n8;
            }
            this.a[i2 * 4 + 0] = (byte)n2;
            this.a[i2 * 4 + 1] = (byte)n3;
            this.a[i2 * 4 + 2] = (byte)n4;
            this.a[i2 * 4 + 3] = (byte)n5;
        }
    }
}

