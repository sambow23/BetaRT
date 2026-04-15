/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class dj
extends pg {
    private int a;

    public dj(int n2) {
        this.a = n2;
    }

    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        int n5;
        int n6;
        n2 -= 8;
        n4 -= 8;
        while (n3 > 0 && fd2.d(n2, n3, n4)) {
            --n3;
        }
        n3 -= 4;
        boolean[] blArray = new boolean[2048];
        int n7 = random.nextInt(4) + 4;
        for (n6 = 0; n6 < n7; ++n6) {
            double d2 = random.nextDouble() * 6.0 + 3.0;
            double d3 = random.nextDouble() * 4.0 + 2.0;
            double d4 = random.nextDouble() * 6.0 + 3.0;
            double d5 = random.nextDouble() * (16.0 - d2 - 2.0) + 1.0 + d2 / 2.0;
            double d6 = random.nextDouble() * (8.0 - d3 - 4.0) + 2.0 + d3 / 2.0;
            double d7 = random.nextDouble() * (16.0 - d4 - 2.0) + 1.0 + d4 / 2.0;
            for (int i2 = 1; i2 < 15; ++i2) {
                for (int i3 = 1; i3 < 15; ++i3) {
                    for (int i4 = 1; i4 < 7; ++i4) {
                        double d8 = ((double)i2 - d5) / (d2 / 2.0);
                        double d9 = ((double)i4 - d6) / (d3 / 2.0);
                        double d10 = ((double)i3 - d7) / (d4 / 2.0);
                        double d11 = d8 * d8 + d9 * d9 + d10 * d10;
                        if (!(d11 < 1.0)) continue;
                        blArray[(i2 * 16 + i3) * 8 + i4] = true;
                    }
                }
            }
        }
        for (n6 = 0; n6 < 16; ++n6) {
            for (int i5 = 0; i5 < 16; ++i5) {
                for (n5 = 0; n5 < 8; ++n5) {
                    boolean bl2;
                    boolean bl3 = bl2 = !blArray[(n6 * 16 + i5) * 8 + n5] && (n6 < 15 && blArray[((n6 + 1) * 16 + i5) * 8 + n5] || n6 > 0 && blArray[((n6 - 1) * 16 + i5) * 8 + n5] || i5 < 15 && blArray[(n6 * 16 + (i5 + 1)) * 8 + n5] || i5 > 0 && blArray[(n6 * 16 + (i5 - 1)) * 8 + n5] || n5 < 7 && blArray[(n6 * 16 + i5) * 8 + (n5 + 1)] || n5 > 0 && blArray[(n6 * 16 + i5) * 8 + (n5 - 1)]);
                    if (!bl2) continue;
                    ln ln2 = fd2.f(n2 + n6, n3 + n5, n4 + i5);
                    if (n5 >= 4 && ln2.d()) {
                        return false;
                    }
                    if (n5 >= 4 || ln2.a() || fd2.a(n2 + n6, n3 + n5, n4 + i5) == this.a) continue;
                    return false;
                }
            }
        }
        for (n6 = 0; n6 < 16; ++n6) {
            for (int i6 = 0; i6 < 16; ++i6) {
                for (n5 = 0; n5 < 8; ++n5) {
                    if (!blArray[(n6 * 16 + i6) * 8 + n5]) continue;
                    fd2.c(n2 + n6, n3 + n5, n4 + i6, n5 >= 4 ? 0 : this.a);
                }
            }
        }
        for (n6 = 0; n6 < 16; ++n6) {
            for (int i7 = 0; i7 < 16; ++i7) {
                for (n5 = 4; n5 < 8; ++n5) {
                    if (!blArray[(n6 * 16 + i7) * 8 + n5] || fd2.a(n2 + n6, n3 + n5 - 1, n4 + i7) != uu.w.bn || fd2.a(eb.a, n2 + n6, n3 + n5, n4 + i7) <= 0) continue;
                    fd2.c(n2 + n6, n3 + n5 - 1, n4 + i7, uu.v.bn);
                }
            }
        }
        if (uu.m[this.a].bA == ln.h) {
            for (n6 = 0; n6 < 16; ++n6) {
                for (int i8 = 0; i8 < 16; ++i8) {
                    for (n5 = 0; n5 < 8; ++n5) {
                        boolean bl4;
                        boolean bl5 = bl4 = !blArray[(n6 * 16 + i8) * 8 + n5] && (n6 < 15 && blArray[((n6 + 1) * 16 + i8) * 8 + n5] || n6 > 0 && blArray[((n6 - 1) * 16 + i8) * 8 + n5] || i8 < 15 && blArray[(n6 * 16 + (i8 + 1)) * 8 + n5] || i8 > 0 && blArray[(n6 * 16 + (i8 - 1)) * 8 + n5] || n5 < 7 && blArray[(n6 * 16 + i8) * 8 + (n5 + 1)] || n5 > 0 && blArray[(n6 * 16 + i8) * 8 + (n5 - 1)]);
                        if (!bl4 || n5 >= 4 && random.nextInt(2) == 0 || !fd2.f(n2 + n6, n3 + n5, n4 + i8).a()) continue;
                        fd2.c(n2 + n6, n3 + n5, n4 + i8, uu.u.bn);
                    }
                }
            }
        }
        return true;
    }
}

