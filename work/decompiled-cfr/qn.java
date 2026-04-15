/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class qn
implements cl {
    private Random h;
    private uf i;
    private uf j;
    private uf k;
    private uf l;
    private uf m;
    public uf a;
    public uf b;
    private fd n;
    private double[] o;
    private double[] p = new double[256];
    private double[] q = new double[256];
    private double[] r = new double[256];
    private fv s = new pk();
    double[] c;
    double[] d;
    double[] e;
    double[] f;
    double[] g;

    public qn(fd fd2, long l2) {
        this.n = fd2;
        this.h = new Random(l2);
        this.i = new uf(this.h, 16);
        this.j = new uf(this.h, 16);
        this.k = new uf(this.h, 8);
        this.l = new uf(this.h, 4);
        this.m = new uf(this.h, 4);
        this.a = new uf(this.h, 10);
        this.b = new uf(this.h, 16);
    }

    public void a(int n2, int n3, byte[] byArray) {
        int n4 = 4;
        int n5 = 32;
        int n6 = n4 + 1;
        int n7 = 17;
        int n8 = n4 + 1;
        this.o = this.a(this.o, n2 * n4, 0, n3 * n4, n6, n7, n8);
        for (int i2 = 0; i2 < n4; ++i2) {
            for (int i3 = 0; i3 < n4; ++i3) {
                for (int i4 = 0; i4 < 16; ++i4) {
                    double d2 = 0.125;
                    double d3 = this.o[((i2 + 0) * n8 + (i3 + 0)) * n7 + (i4 + 0)];
                    double d4 = this.o[((i2 + 0) * n8 + (i3 + 1)) * n7 + (i4 + 0)];
                    double d5 = this.o[((i2 + 1) * n8 + (i3 + 0)) * n7 + (i4 + 0)];
                    double d6 = this.o[((i2 + 1) * n8 + (i3 + 1)) * n7 + (i4 + 0)];
                    double d7 = (this.o[((i2 + 0) * n8 + (i3 + 0)) * n7 + (i4 + 1)] - d3) * d2;
                    double d8 = (this.o[((i2 + 0) * n8 + (i3 + 1)) * n7 + (i4 + 1)] - d4) * d2;
                    double d9 = (this.o[((i2 + 1) * n8 + (i3 + 0)) * n7 + (i4 + 1)] - d5) * d2;
                    double d10 = (this.o[((i2 + 1) * n8 + (i3 + 1)) * n7 + (i4 + 1)] - d6) * d2;
                    for (int i5 = 0; i5 < 8; ++i5) {
                        double d11 = 0.25;
                        double d12 = d3;
                        double d13 = d4;
                        double d14 = (d5 - d3) * d11;
                        double d15 = (d6 - d4) * d11;
                        for (int i6 = 0; i6 < 4; ++i6) {
                            int n9 = i6 + i2 * 4 << 11 | 0 + i3 * 4 << 7 | i4 * 8 + i5;
                            int n10 = 128;
                            double d16 = 0.25;
                            double d17 = d12;
                            double d18 = (d13 - d12) * d16;
                            for (int i7 = 0; i7 < 4; ++i7) {
                                int n11 = 0;
                                if (i4 * 8 + i5 < n5) {
                                    n11 = uu.E.bn;
                                }
                                if (d17 > 0.0) {
                                    n11 = uu.bc.bn;
                                }
                                byArray[n9] = (byte)n11;
                                n9 += n10;
                                d17 += d18;
                            }
                            d12 += d14;
                            d13 += d15;
                        }
                        d3 += d7;
                        d4 += d8;
                        d5 += d9;
                        d6 += d10;
                    }
                }
            }
        }
    }

    public void b(int n2, int n3, byte[] byArray) {
        int n4 = 64;
        double d2 = 0.03125;
        this.p = this.l.a(this.p, n2 * 16, n3 * 16, 0.0, 16, 16, 1, d2, d2, 1.0);
        this.q = this.l.a(this.q, n2 * 16, 109.0134, n3 * 16, 16, 1, 16, d2, 1.0, d2);
        this.r = this.m.a(this.r, n2 * 16, n3 * 16, 0.0, 16, 16, 1, d2 * 2.0, d2 * 2.0, d2 * 2.0);
        for (int i2 = 0; i2 < 16; ++i2) {
            for (int i3 = 0; i3 < 16; ++i3) {
                boolean bl2 = this.p[i2 + i3 * 16] + this.h.nextDouble() * 0.2 > 0.0;
                boolean bl3 = this.q[i2 + i3 * 16] + this.h.nextDouble() * 0.2 > 0.0;
                int n5 = (int)(this.r[i2 + i3 * 16] / 3.0 + 3.0 + this.h.nextDouble() * 0.25);
                int n6 = -1;
                byte by2 = (byte)uu.bc.bn;
                byte by3 = (byte)uu.bc.bn;
                for (int i4 = 127; i4 >= 0; --i4) {
                    int n7 = (i3 * 16 + i2) * 128 + i4;
                    if (i4 >= 127 - this.h.nextInt(5)) {
                        byArray[n7] = (byte)uu.A.bn;
                        continue;
                    }
                    if (i4 <= 0 + this.h.nextInt(5)) {
                        byArray[n7] = (byte)uu.A.bn;
                        continue;
                    }
                    byte by4 = byArray[n7];
                    if (by4 == 0) {
                        n6 = -1;
                        continue;
                    }
                    if (by4 != uu.bc.bn) continue;
                    if (n6 == -1) {
                        if (n5 <= 0) {
                            by2 = 0;
                            by3 = (byte)uu.bc.bn;
                        } else if (i4 >= n4 - 4 && i4 <= n4 + 1) {
                            by2 = (byte)uu.bc.bn;
                            by3 = (byte)uu.bc.bn;
                            if (bl3) {
                                by2 = (byte)uu.G.bn;
                            }
                            if (bl3) {
                                by3 = (byte)uu.bc.bn;
                            }
                            if (bl2) {
                                by2 = (byte)uu.bd.bn;
                            }
                            if (bl2) {
                                by3 = (byte)uu.bd.bn;
                            }
                        }
                        if (i4 < n4 && by2 == 0) {
                            by2 = (byte)uu.E.bn;
                        }
                        n6 = n5;
                        if (i4 >= n4 - 1) {
                            byArray[n7] = by2;
                            continue;
                        }
                        byArray[n7] = by3;
                        continue;
                    }
                    if (n6 <= 0) continue;
                    --n6;
                    byArray[n7] = by3;
                }
            }
        }
    }

    public lm c(int n2, int n3) {
        return this.b(n2, n3);
    }

    public lm b(int n2, int n3) {
        this.h.setSeed((long)n2 * 341873128712L + (long)n3 * 132897987541L);
        byte[] byArray = new byte[32768];
        this.a(n2, n3, byArray);
        this.b(n2, n3, byArray);
        this.s.a(this, this.n, n2, n3, byArray);
        lm lm2 = new lm(this.n, byArray, n2, n3);
        return lm2;
    }

    private double[] a(double[] dArray, int n2, int n3, int n4, int n5, int n6, int n7) {
        int n8;
        if (dArray == null) {
            dArray = new double[n5 * n6 * n7];
        }
        double d2 = 684.412;
        double d3 = 2053.236;
        this.f = this.a.a(this.f, n2, n3, n4, n5, 1, n7, 1.0, 0.0, 1.0);
        this.g = this.b.a(this.g, n2, n3, n4, n5, 1, n7, 100.0, 0.0, 100.0);
        this.c = this.k.a(this.c, n2, n3, n4, n5, n6, n7, d2 / 80.0, d3 / 60.0, d2 / 80.0);
        this.d = this.i.a(this.d, n2, n3, n4, n5, n6, n7, d2, d3, d2);
        this.e = this.j.a(this.e, n2, n3, n4, n5, n6, n7, d2, d3, d2);
        int n9 = 0;
        int n10 = 0;
        double[] dArray2 = new double[n6];
        for (n8 = 0; n8 < n6; ++n8) {
            dArray2[n8] = Math.cos((double)n8 * Math.PI * 6.0 / (double)n6) * 2.0;
            double d4 = n8;
            if (n8 > n6 / 2) {
                d4 = n6 - 1 - n8;
            }
            if (!(d4 < 4.0)) continue;
            d4 = 4.0 - d4;
            int n11 = n8;
            dArray2[n11] = dArray2[n11] - d4 * d4 * d4 * 10.0;
        }
        for (n8 = 0; n8 < n5; ++n8) {
            for (int i2 = 0; i2 < n7; ++i2) {
                double d5 = (this.f[n10] + 256.0) / 512.0;
                if (d5 > 1.0) {
                    d5 = 1.0;
                }
                double d6 = 0.0;
                double d7 = this.g[n10] / 8000.0;
                if (d7 < 0.0) {
                    d7 = -d7;
                }
                if ((d7 = d7 * 3.0 - 3.0) < 0.0) {
                    if ((d7 /= 2.0) < -1.0) {
                        d7 = -1.0;
                    }
                    d7 /= 1.4;
                    d7 /= 2.0;
                    d5 = 0.0;
                } else {
                    if (d7 > 1.0) {
                        d7 = 1.0;
                    }
                    d7 /= 6.0;
                }
                d5 += 0.5;
                d7 = d7 * (double)n6 / 16.0;
                ++n10;
                for (int i3 = 0; i3 < n6; ++i3) {
                    double d8;
                    double d9 = 0.0;
                    double d10 = dArray2[i3];
                    double d11 = this.d[n9] / 512.0;
                    double d12 = this.e[n9] / 512.0;
                    double d13 = (this.c[n9] / 10.0 + 1.0) / 2.0;
                    d9 = d13 < 0.0 ? d11 : (d13 > 1.0 ? d12 : d11 + (d12 - d11) * d13);
                    d9 -= d10;
                    if (i3 > n6 - 4) {
                        d8 = (float)(i3 - (n6 - 4)) / 3.0f;
                        d9 = d9 * (1.0 - d8) + -10.0 * d8;
                    }
                    if ((double)i3 < d6) {
                        d8 = (d6 - (double)i3) / 4.0;
                        if (d8 < 0.0) {
                            d8 = 0.0;
                        }
                        if (d8 > 1.0) {
                            d8 = 1.0;
                        }
                        d9 = d9 * (1.0 - d8) + -10.0 * d8;
                    }
                    dArray[n9] = d9;
                    ++n9;
                }
            }
        }
        return dArray;
    }

    public boolean a(int n2, int n3) {
        return true;
    }

    public void a(cl cl2, int n2, int n3) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        gk.a = true;
        int n9 = n2 * 16;
        int n10 = n3 * 16;
        for (n8 = 0; n8 < 8; ++n8) {
            n7 = n9 + this.h.nextInt(16) + 8;
            n6 = this.h.nextInt(120) + 4;
            n5 = n10 + this.h.nextInt(16) + 8;
            new qc(uu.D.bn).a(this.n, this.h, n7, n6, n5);
        }
        n8 = this.h.nextInt(this.h.nextInt(10) + 1) + 1;
        for (n7 = 0; n7 < n8; ++n7) {
            n6 = n9 + this.h.nextInt(16) + 8;
            n5 = this.h.nextInt(120) + 4;
            n4 = n10 + this.h.nextInt(16) + 8;
            new xs().a(this.n, this.h, n6, n5, n4);
        }
        n8 = this.h.nextInt(this.h.nextInt(10) + 1);
        for (n7 = 0; n7 < n8; ++n7) {
            n6 = n9 + this.h.nextInt(16) + 8;
            n5 = this.h.nextInt(120) + 4;
            n4 = n10 + this.h.nextInt(16) + 8;
            new gd().a(this.n, this.h, n6, n5, n4);
        }
        for (n7 = 0; n7 < 10; ++n7) {
            n6 = n9 + this.h.nextInt(16) + 8;
            n5 = this.h.nextInt(128);
            n4 = n10 + this.h.nextInt(16) + 8;
            new sf().a(this.n, this.h, n6, n5, n4);
        }
        if (this.h.nextInt(1) == 0) {
            n7 = n9 + this.h.nextInt(16) + 8;
            n6 = this.h.nextInt(128);
            n5 = n10 + this.h.nextInt(16) + 8;
            new be(uu.ag.bn).a(this.n, this.h, n7, n6, n5);
        }
        if (this.h.nextInt(1) == 0) {
            n7 = n9 + this.h.nextInt(16) + 8;
            n6 = this.h.nextInt(128);
            n5 = n10 + this.h.nextInt(16) + 8;
            new be(uu.ah.bn).a(this.n, this.h, n7, n6, n5);
        }
        gk.a = false;
    }

    public boolean a(boolean bl2, yb yb2) {
        return true;
    }

    public boolean a() {
        return false;
    }

    public boolean b() {
        return true;
    }

    public String c() {
        return "HellRandomLevelSource";
    }
}

