/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class yf
implements cl {
    private Random j;
    private uf k;
    private uf l;
    private uf m;
    private uf n;
    private uf o;
    public uf a;
    public uf b;
    public uf c;
    private fd p;
    private double[] q;
    private double[] r = new double[256];
    private double[] s = new double[256];
    private double[] t = new double[256];
    private fv u = new sq();
    private kd[] v;
    double[] d;
    double[] e;
    double[] f;
    double[] g;
    double[] h;
    int[][] i = new int[32][32];
    private double[] w;

    public yf(fd fd2, long l2) {
        this.p = fd2;
        this.j = new Random(l2);
        this.k = new uf(this.j, 16);
        this.l = new uf(this.j, 16);
        this.m = new uf(this.j, 8);
        this.n = new uf(this.j, 4);
        this.o = new uf(this.j, 4);
        this.a = new uf(this.j, 10);
        this.b = new uf(this.j, 16);
        this.c = new uf(this.j, 8);
    }

    public void a(int n2, int n3, byte[] byArray, kd[] kdArray, double[] dArray) {
        int n4 = 4;
        int n5 = 64;
        int n6 = n4 + 1;
        int n7 = 17;
        int n8 = n4 + 1;
        this.q = this.a(this.q, n2 * n4, 0, n3 * n4, n6, n7, n8);
        for (int i2 = 0; i2 < n4; ++i2) {
            for (int i3 = 0; i3 < n4; ++i3) {
                for (int i4 = 0; i4 < 16; ++i4) {
                    double d2 = 0.125;
                    double d3 = this.q[((i2 + 0) * n8 + (i3 + 0)) * n7 + (i4 + 0)];
                    double d4 = this.q[((i2 + 0) * n8 + (i3 + 1)) * n7 + (i4 + 0)];
                    double d5 = this.q[((i2 + 1) * n8 + (i3 + 0)) * n7 + (i4 + 0)];
                    double d6 = this.q[((i2 + 1) * n8 + (i3 + 1)) * n7 + (i4 + 0)];
                    double d7 = (this.q[((i2 + 0) * n8 + (i3 + 0)) * n7 + (i4 + 1)] - d3) * d2;
                    double d8 = (this.q[((i2 + 0) * n8 + (i3 + 1)) * n7 + (i4 + 1)] - d4) * d2;
                    double d9 = (this.q[((i2 + 1) * n8 + (i3 + 0)) * n7 + (i4 + 1)] - d5) * d2;
                    double d10 = (this.q[((i2 + 1) * n8 + (i3 + 1)) * n7 + (i4 + 1)] - d6) * d2;
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
                                double d19 = dArray[(i2 * 4 + i6) * 16 + (i3 * 4 + i7)];
                                int n11 = 0;
                                if (i4 * 8 + i5 < n5) {
                                    n11 = d19 < 0.5 && i4 * 8 + i5 >= n5 - 1 ? uu.aU.bn : uu.C.bn;
                                }
                                if (d17 > 0.0) {
                                    n11 = uu.u.bn;
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

    public void a(int n2, int n3, byte[] byArray, kd[] kdArray) {
        int n4 = 64;
        double d2 = 0.03125;
        this.r = this.n.a(this.r, n2 * 16, n3 * 16, 0.0, 16, 16, 1, d2, d2, 1.0);
        this.s = this.n.a(this.s, n2 * 16, 109.0134, n3 * 16, 16, 1, 16, d2, 1.0, d2);
        this.t = this.o.a(this.t, n2 * 16, n3 * 16, 0.0, 16, 16, 1, d2 * 2.0, d2 * 2.0, d2 * 2.0);
        for (int i2 = 0; i2 < 16; ++i2) {
            for (int i3 = 0; i3 < 16; ++i3) {
                kd kd2 = kdArray[i2 + i3 * 16];
                boolean bl2 = this.r[i2 + i3 * 16] + this.j.nextDouble() * 0.2 > 0.0;
                boolean bl3 = this.s[i2 + i3 * 16] + this.j.nextDouble() * 0.2 > 3.0;
                int n5 = (int)(this.t[i2 + i3 * 16] / 3.0 + 3.0 + this.j.nextDouble() * 0.25);
                int n6 = -1;
                byte by2 = kd2.p;
                byte by3 = kd2.q;
                for (int i4 = 127; i4 >= 0; --i4) {
                    int n7 = (i3 * 16 + i2) * 128 + i4;
                    if (i4 <= 0 + this.j.nextInt(5)) {
                        byArray[n7] = (byte)uu.A.bn;
                        continue;
                    }
                    byte by4 = byArray[n7];
                    if (by4 == 0) {
                        n6 = -1;
                        continue;
                    }
                    if (by4 != uu.u.bn) continue;
                    if (n6 == -1) {
                        if (n5 <= 0) {
                            by2 = 0;
                            by3 = (byte)uu.u.bn;
                        } else if (i4 >= n4 - 4 && i4 <= n4 + 1) {
                            by2 = kd2.p;
                            by3 = kd2.q;
                            if (bl3) {
                                by2 = 0;
                            }
                            if (bl3) {
                                by3 = (byte)uu.G.bn;
                            }
                            if (bl2) {
                                by2 = (byte)uu.F.bn;
                            }
                            if (bl2) {
                                by3 = (byte)uu.F.bn;
                            }
                        }
                        if (i4 < n4 && by2 == 0) {
                            by2 = (byte)uu.C.bn;
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
                    byArray[n7] = by3;
                    if (--n6 != 0 || by3 != uu.F.bn) continue;
                    n6 = this.j.nextInt(4);
                    by3 = (byte)uu.R.bn;
                }
            }
        }
    }

    public lm c(int n2, int n3) {
        return this.b(n2, n3);
    }

    public lm b(int n2, int n3) {
        this.j.setSeed((long)n2 * 341873128712L + (long)n3 * 132897987541L);
        byte[] byArray = new byte[32768];
        lm lm2 = new lm(this.p, byArray, n2, n3);
        this.v = this.p.a().a(this.v, n2 * 16, n3 * 16, 16, 16);
        double[] dArray = this.p.a().a;
        this.a(n2, n3, byArray, this.v, dArray);
        this.a(n2, n3, byArray, this.v);
        this.u.a(this, this.p, n2, n3, byArray);
        lm2.c();
        return lm2;
    }

    private double[] a(double[] dArray, int n2, int n3, int n4, int n5, int n6, int n7) {
        if (dArray == null) {
            dArray = new double[n5 * n6 * n7];
        }
        double d2 = 684.412;
        double d3 = 684.412;
        double[] dArray2 = this.p.a().a;
        double[] dArray3 = this.p.a().b;
        this.g = this.a.a(this.g, n2, n4, n5, n7, 1.121, 1.121, 0.5);
        this.h = this.b.a(this.h, n2, n4, n5, n7, 200.0, 200.0, 0.5);
        this.d = this.m.a(this.d, n2, n3, n4, n5, n6, n7, d2 / 80.0, d3 / 160.0, d2 / 80.0);
        this.e = this.k.a(this.e, n2, n3, n4, n5, n6, n7, d2, d3, d2);
        this.f = this.l.a(this.f, n2, n3, n4, n5, n6, n7, d2, d3, d2);
        int n8 = 0;
        int n9 = 0;
        int n10 = 16 / n5;
        for (int i2 = 0; i2 < n5; ++i2) {
            int n11 = i2 * n10 + n10 / 2;
            for (int i3 = 0; i3 < n7; ++i3) {
                double d4;
                int n12 = i3 * n10 + n10 / 2;
                double d5 = dArray2[n11 * 16 + n12];
                double d6 = dArray3[n11 * 16 + n12] * d5;
                double d7 = 1.0 - d6;
                d7 *= d7;
                d7 *= d7;
                d7 = 1.0 - d7;
                double d8 = (this.g[n9] + 256.0) / 512.0;
                if ((d8 *= d7) > 1.0) {
                    d8 = 1.0;
                }
                if ((d4 = this.h[n9] / 8000.0) < 0.0) {
                    d4 = -d4 * 0.3;
                }
                if ((d4 = d4 * 3.0 - 2.0) < 0.0) {
                    if ((d4 /= 2.0) < -1.0) {
                        d4 = -1.0;
                    }
                    d4 /= 1.4;
                    d4 /= 2.0;
                    d8 = 0.0;
                } else {
                    if (d4 > 1.0) {
                        d4 = 1.0;
                    }
                    d4 /= 8.0;
                }
                if (d8 < 0.0) {
                    d8 = 0.0;
                }
                d8 += 0.5;
                d4 = d4 * (double)n6 / 16.0;
                double d9 = (double)n6 / 2.0 + d4 * 4.0;
                ++n9;
                for (int i4 = 0; i4 < n6; ++i4) {
                    double d10 = 0.0;
                    double d11 = ((double)i4 - d9) * 12.0 / d8;
                    if (d11 < 0.0) {
                        d11 *= 4.0;
                    }
                    double d12 = this.e[n8] / 512.0;
                    double d13 = this.f[n8] / 512.0;
                    double d14 = (this.d[n8] / 10.0 + 1.0) / 2.0;
                    d10 = d14 < 0.0 ? d12 : (d14 > 1.0 ? d13 : d12 + (d13 - d12) * d14);
                    d10 -= d11;
                    if (i4 > n6 - 4) {
                        double d15 = (float)(i4 - (n6 - 4)) / 3.0f;
                        d10 = d10 * (1.0 - d15) + -10.0 * d15;
                    }
                    dArray[n8] = d10;
                    ++n8;
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
        int n9;
        int n10;
        int n11;
        int n12;
        gk.a = true;
        int n13 = n2 * 16;
        int n14 = n3 * 16;
        kd kd2 = this.p.a().a(n13 + 16, n14 + 16);
        this.j.setSeed(this.p.s());
        long l2 = this.j.nextLong() / 2L * 2L + 1L;
        long l3 = this.j.nextLong() / 2L * 2L + 1L;
        this.j.setSeed((long)n2 * l2 + (long)n3 * l3 ^ this.p.s());
        double d2 = 0.25;
        if (this.j.nextInt(4) == 0) {
            n12 = n13 + this.j.nextInt(16) + 8;
            n11 = this.j.nextInt(128);
            n10 = n14 + this.j.nextInt(16) + 8;
            new dj(uu.C.bn).a(this.p, this.j, n12, n11, n10);
        }
        if (this.j.nextInt(8) == 0) {
            n12 = n13 + this.j.nextInt(16) + 8;
            n11 = this.j.nextInt(this.j.nextInt(120) + 8);
            n10 = n14 + this.j.nextInt(16) + 8;
            if (n11 < 64 || this.j.nextInt(10) == 0) {
                new dj(uu.E.bn).a(this.p, this.j, n12, n11, n10);
            }
        }
        for (n12 = 0; n12 < 8; ++n12) {
            n11 = n13 + this.j.nextInt(16) + 8;
            n10 = this.j.nextInt(128);
            n9 = n14 + this.j.nextInt(16) + 8;
            new er().a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 10; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(128);
            n9 = n14 + this.j.nextInt(16);
            new ms(32).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 20; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(128);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.w.bn, 32).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 10; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(128);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.G.bn, 32).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 20; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(128);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.J.bn, 16).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 20; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(64);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.I.bn, 8).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 2; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(32);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.H.bn, 8).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 8; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(16);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.aO.bn, 7).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 1; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(16);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.ax.bn, 7).a(this.p, this.j, n11, n10, n9);
        }
        for (n12 = 0; n12 < 1; ++n12) {
            n11 = n13 + this.j.nextInt(16);
            n10 = this.j.nextInt(16) + this.j.nextInt(16);
            n9 = n14 + this.j.nextInt(16);
            new fl(uu.O.bn, 6).a(this.p, this.j, n11, n10, n9);
        }
        d2 = 0.5;
        n12 = (int)((this.c.a((double)n13 * d2, (double)n14 * d2) / 8.0 + this.j.nextDouble() * 4.0 + 4.0) / 3.0);
        n11 = 0;
        if (this.j.nextInt(10) == 0) {
            ++n11;
        }
        if (kd2 == kd.d) {
            n11 += n12 + 5;
        }
        if (kd2 == kd.a) {
            n11 += n12 + 5;
        }
        if (kd2 == kd.c) {
            n11 += n12 + 2;
        }
        if (kd2 == kd.g) {
            n11 += n12 + 5;
        }
        if (kd2 == kd.h) {
            n11 -= 20;
        }
        if (kd2 == kd.k) {
            n11 -= 20;
        }
        if (kd2 == kd.i) {
            n11 -= 20;
        }
        for (n10 = 0; n10 < n11; ++n10) {
            n9 = n13 + this.j.nextInt(16) + 8;
            n8 = n14 + this.j.nextInt(16) + 8;
            pg pg2 = kd2.a(this.j);
            pg2.a(1.0, 1.0, 1.0);
            pg2.a(this.p, this.j, n9, this.p.d(n9, n8), n8);
        }
        n10 = 0;
        if (kd2 == kd.d) {
            n10 = 2;
        }
        if (kd2 == kd.c) {
            n10 = 4;
        }
        if (kd2 == kd.g) {
            n10 = 2;
        }
        if (kd2 == kd.i) {
            n10 = 3;
        }
        for (n9 = 0; n9 < n10; ++n9) {
            n8 = n13 + this.j.nextInt(16) + 8;
            int n15 = this.j.nextInt(128);
            n7 = n14 + this.j.nextInt(16) + 8;
            new be(uu.ae.bn).a(this.p, this.j, n8, n15, n7);
        }
        n9 = 0;
        if (kd2 == kd.d) {
            n9 = 2;
        }
        if (kd2 == kd.a) {
            n9 = 10;
        }
        if (kd2 == kd.c) {
            n9 = 2;
        }
        if (kd2 == kd.g) {
            n9 = 1;
        }
        if (kd2 == kd.i) {
            n9 = 10;
        }
        for (n8 = 0; n8 < n9; ++n8) {
            int n16 = 1;
            if (kd2 == kd.a && this.j.nextInt(3) != 0) {
                n16 = 2;
            }
            n7 = n13 + this.j.nextInt(16) + 8;
            n6 = this.j.nextInt(128);
            n5 = n14 + this.j.nextInt(16) + 8;
            new ap(uu.Y.bn, n16).a(this.p, this.j, n7, n6, n5);
        }
        n9 = 0;
        if (kd2 == kd.h) {
            n9 = 2;
        }
        for (n8 = 0; n8 < n9; ++n8) {
            int n17 = n13 + this.j.nextInt(16) + 8;
            n7 = this.j.nextInt(128);
            n6 = n14 + this.j.nextInt(16) + 8;
            new kt(uu.Z.bn).a(this.p, this.j, n17, n7, n6);
        }
        if (this.j.nextInt(2) == 0) {
            n8 = n13 + this.j.nextInt(16) + 8;
            int n18 = this.j.nextInt(128);
            n7 = n14 + this.j.nextInt(16) + 8;
            new be(uu.af.bn).a(this.p, this.j, n8, n18, n7);
        }
        if (this.j.nextInt(4) == 0) {
            n8 = n13 + this.j.nextInt(16) + 8;
            int n19 = this.j.nextInt(128);
            n7 = n14 + this.j.nextInt(16) + 8;
            new be(uu.ag.bn).a(this.p, this.j, n8, n19, n7);
        }
        if (this.j.nextInt(8) == 0) {
            n8 = n13 + this.j.nextInt(16) + 8;
            int n20 = this.j.nextInt(128);
            n7 = n14 + this.j.nextInt(16) + 8;
            new be(uu.ah.bn).a(this.p, this.j, n8, n20, n7);
        }
        for (n8 = 0; n8 < 10; ++n8) {
            int n21 = n13 + this.j.nextInt(16) + 8;
            n7 = this.j.nextInt(128);
            n6 = n14 + this.j.nextInt(16) + 8;
            new ir().a(this.p, this.j, n21, n7, n6);
        }
        if (this.j.nextInt(32) == 0) {
            n8 = n13 + this.j.nextInt(16) + 8;
            int n22 = this.j.nextInt(128);
            n7 = n14 + this.j.nextInt(16) + 8;
            new wx().a(this.p, this.j, n8, n22, n7);
        }
        n8 = 0;
        if (kd2 == kd.h) {
            n8 += 10;
        }
        for (n4 = 0; n4 < n8; ++n4) {
            n7 = n13 + this.j.nextInt(16) + 8;
            n6 = this.j.nextInt(128);
            n5 = n14 + this.j.nextInt(16) + 8;
            new fx().a(this.p, this.j, n7, n6, n5);
        }
        for (n4 = 0; n4 < 50; ++n4) {
            n7 = n13 + this.j.nextInt(16) + 8;
            n6 = this.j.nextInt(this.j.nextInt(120) + 8);
            n5 = n14 + this.j.nextInt(16) + 8;
            new xo(uu.B.bn).a(this.p, this.j, n7, n6, n5);
        }
        for (n4 = 0; n4 < 20; ++n4) {
            n7 = n13 + this.j.nextInt(16) + 8;
            n6 = this.j.nextInt(this.j.nextInt(this.j.nextInt(112) + 8) + 8);
            n5 = n14 + this.j.nextInt(16) + 8;
            new xo(uu.D.bn).a(this.p, this.j, n7, n6, n5);
        }
        this.w = this.p.a().a(this.w, n13 + 8, n14 + 8, 16, 16);
        for (n4 = n13 + 8; n4 < n13 + 8 + 16; ++n4) {
            for (n7 = n14 + 8; n7 < n14 + 8 + 16; ++n7) {
                n6 = n4 - (n13 + 8);
                n5 = n7 - (n14 + 8);
                int n23 = this.p.e(n4, n7);
                double d3 = this.w[n6 * 16 + n5] - (double)(n23 - 64) / 64.0 * 0.3;
                if (!(d3 < 0.5) || n23 <= 0 || n23 >= 128 || !this.p.d(n4, n23, n7) || !this.p.f(n4, n23 - 1, n7).c() || this.p.f(n4, n23 - 1, n7) == ln.s) continue;
                this.p.f(n4, n23, n7, uu.aT.bn);
            }
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
        return "RandomLevelSource";
    }
}

