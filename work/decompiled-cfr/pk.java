/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class pk
extends fv {
    protected void a(int n2, int n3, byte[] byArray, double d2, double d3, double d4) {
        this.a(n2, n3, byArray, d2, d3, d4, 1.0f + this.b.nextFloat() * 6.0f, 0.0f, 0.0f, -1, -1, 0.5);
    }

    protected void a(int n2, int n3, byte[] byArray, double d2, double d3, double d4, float f2, float f3, float f4, int n4, int n5, double d5) {
        boolean bl2;
        int n6;
        double d6 = n2 * 16 + 8;
        double d7 = n3 * 16 + 8;
        float f5 = 0.0f;
        float f6 = 0.0f;
        Random random = new Random(this.b.nextLong());
        if (n5 <= 0) {
            n6 = this.a * 16 - 16;
            n5 = n6 - random.nextInt(n6 / 4);
        }
        n6 = 0;
        if (n4 == -1) {
            n4 = n5 / 2;
            n6 = 1;
        }
        int n7 = random.nextInt(n5 / 2) + n5 / 4;
        boolean bl3 = bl2 = random.nextInt(6) == 0;
        while (n4 < n5) {
            double d8 = 1.5 + (double)(in.a((float)n4 * (float)Math.PI / (float)n5) * f2 * 1.0f);
            double d9 = d8 * d5;
            float f7 = in.b(f4);
            float f8 = in.a(f4);
            d2 += (double)(in.b(f3) * f7);
            d3 += (double)f8;
            d4 += (double)(in.a(f3) * f7);
            f4 = bl2 ? (f4 *= 0.92f) : (f4 *= 0.7f);
            f4 += f6 * 0.1f;
            f3 += f5 * 0.1f;
            f6 *= 0.9f;
            f5 *= 0.75f;
            f6 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            f5 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;
            if (n6 == 0 && n4 == n7 && f2 > 1.0f) {
                this.a(n2, n3, byArray, d2, d3, d4, random.nextFloat() * 0.5f + 0.5f, f3 - 1.5707964f, f4 / 3.0f, n4, n5, 1.0);
                this.a(n2, n3, byArray, d2, d3, d4, random.nextFloat() * 0.5f + 0.5f, f3 + 1.5707964f, f4 / 3.0f, n4, n5, 1.0);
                return;
            }
            if (n6 != 0 || random.nextInt(4) != 0) {
                double d10 = d2 - d6;
                double d11 = d4 - d7;
                double d12 = n5 - n4;
                double d13 = f2 + 2.0f + 16.0f;
                if (d10 * d10 + d11 * d11 - d12 * d12 > d13 * d13) {
                    return;
                }
                if (!(d2 < d6 - 16.0 - d8 * 2.0 || d4 < d7 - 16.0 - d8 * 2.0 || d2 > d6 + 16.0 + d8 * 2.0 || d4 > d7 + 16.0 + d8 * 2.0)) {
                    int n8;
                    int n9;
                    int n10 = in.b(d2 - d8) - n2 * 16 - 1;
                    int n11 = in.b(d2 + d8) - n2 * 16 + 1;
                    int n12 = in.b(d3 - d9) - 1;
                    int n13 = in.b(d3 + d9) + 1;
                    int n14 = in.b(d4 - d8) - n3 * 16 - 1;
                    int n15 = in.b(d4 + d8) - n3 * 16 + 1;
                    if (n10 < 0) {
                        n10 = 0;
                    }
                    if (n11 > 16) {
                        n11 = 16;
                    }
                    if (n12 < 1) {
                        n12 = 1;
                    }
                    if (n13 > 120) {
                        n13 = 120;
                    }
                    if (n14 < 0) {
                        n14 = 0;
                    }
                    if (n15 > 16) {
                        n15 = 16;
                    }
                    boolean bl4 = false;
                    for (n9 = n10; !bl4 && n9 < n11; ++n9) {
                        for (int i2 = n14; !bl4 && i2 < n15; ++i2) {
                            for (int i3 = n13 + 1; !bl4 && i3 >= n12 - 1; --i3) {
                                n8 = (n9 * 16 + i2) * 128 + i3;
                                if (i3 < 0 || i3 >= 128) continue;
                                if (byArray[n8] == uu.D.bn || byArray[n8] == uu.E.bn) {
                                    bl4 = true;
                                }
                                if (i3 == n12 - 1 || n9 == n10 || n9 == n11 - 1 || i2 == n14 || i2 == n15 - 1) continue;
                                i3 = n12;
                            }
                        }
                    }
                    if (!bl4) {
                        for (n9 = n10; n9 < n11; ++n9) {
                            double d14 = ((double)(n9 + n2 * 16) + 0.5 - d2) / d8;
                            for (n8 = n14; n8 < n15; ++n8) {
                                double d15 = ((double)(n8 + n3 * 16) + 0.5 - d4) / d8;
                                int n16 = (n9 * 16 + n8) * 128 + n13;
                                for (int i4 = n13 - 1; i4 >= n12; --i4) {
                                    byte by2;
                                    double d16 = ((double)i4 + 0.5 - d3) / d9;
                                    if (d16 > -0.7 && d14 * d14 + d16 * d16 + d15 * d15 < 1.0 && ((by2 = byArray[n16]) == uu.bc.bn || by2 == uu.w.bn || by2 == uu.v.bn)) {
                                        byArray[n16] = 0;
                                    }
                                    --n16;
                                }
                            }
                        }
                        if (n6 != 0) break;
                    }
                }
            }
            ++n4;
        }
    }

    protected void a(fd fd2, int n2, int n3, int n4, int n5, byte[] byArray) {
        int n6 = this.b.nextInt(this.b.nextInt(this.b.nextInt(10) + 1) + 1);
        if (this.b.nextInt(5) != 0) {
            n6 = 0;
        }
        for (int i2 = 0; i2 < n6; ++i2) {
            double d2 = n2 * 16 + this.b.nextInt(16);
            double d3 = this.b.nextInt(128);
            double d4 = n3 * 16 + this.b.nextInt(16);
            int n7 = 1;
            if (this.b.nextInt(4) == 0) {
                this.a(n4, n5, byArray, d2, d3, d4);
                n7 += this.b.nextInt(4);
            }
            for (int i3 = 0; i3 < n7; ++i3) {
                float f2 = this.b.nextFloat() * (float)Math.PI * 2.0f;
                float f3 = (this.b.nextFloat() - 0.5f) * 2.0f / 8.0f;
                float f4 = this.b.nextFloat() * 2.0f + this.b.nextFloat();
                this.a(n4, n5, byArray, d2, d3, d4, f4 * 2.0f, f2, f3, 0, 0, 0.5);
            }
        }
    }
}

