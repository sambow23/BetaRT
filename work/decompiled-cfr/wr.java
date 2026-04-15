/*
 * Decompiled with CFR 0.152.
 */
public class wr
extends cp {
    protected wr(int n2) {
        super(n2);
        this.d(1);
    }

    public static iu a(short s2, fd fd2) {
        String string = "map_" + s2;
        iu iu2 = (iu)fd2.a(iu.class, "map_" + s2);
        if (iu2 == null) {
            int n2 = fd2.b("map");
            string = "map_" + n2;
            iu2 = new iu(string);
            fd2.a(string, iu2);
        }
        return iu2;
    }

    public iu a(iz iz2, fd fd2) {
        String string = "map_" + iz2.i();
        iu iu2 = (iu)fd2.a(iu.class, "map_" + iz2.i());
        if (iu2 == null) {
            iz2.b(fd2.b("map"));
            string = "map_" + iz2.i();
            iu2 = new iu(string);
            iu2.b = fd2.x().c();
            iu2.c = fd2.x().e();
            iu2.e = (byte)3;
            iu2.d = (byte)fd2.t.g;
            iu2.a();
            fd2.a(string, iu2);
        }
        return iu2;
    }

    public void a(fd fd2, sn sn2, iu iu2) {
        if (fd2.t.g != iu2.d) {
            return;
        }
        int n2 = 128;
        int n3 = 128;
        int n4 = 1 << iu2.e;
        int n5 = iu2.b;
        int n6 = iu2.c;
        int n7 = in.b(sn2.aM - (double)n5) / n4 + n2 / 2;
        int n8 = in.b(sn2.aO - (double)n6) / n4 + n3 / 2;
        int n9 = 128 / n4;
        if (fd2.t.e) {
            n9 /= 2;
        }
        ++iu2.g;
        for (int i2 = n7 - n9 + 1; i2 < n7 + n9; ++i2) {
            if ((i2 & 0xF) != (iu2.g & 0xF)) continue;
            int n10 = 255;
            int n11 = 0;
            double d2 = 0.0;
            for (int i3 = n8 - n9 - 1; i3 < n8 + n9; ++i3) {
                byte by2;
                byte by3;
                int n12;
                int n13;
                int n14;
                int n15;
                int n16;
                if (i2 < 0 || i3 < -1 || i2 >= n2 || i3 >= n3) continue;
                int n17 = i2 - n7;
                int n18 = i3 - n8;
                boolean bl2 = n17 * n17 + n18 * n18 > (n9 - 2) * (n9 - 2);
                int n19 = (n5 / n4 + i2 - n2 / 2) * n4;
                int n20 = (n6 / n4 + i3 - n3 / 2) * n4;
                int n21 = 0;
                int n22 = 0;
                int n23 = 0;
                int[] nArray = new int[256];
                lm lm2 = fd2.b(n19, n20);
                int n24 = n19 & 0xF;
                int n25 = n20 & 0xF;
                int n26 = 0;
                double d3 = 0.0;
                if (fd2.t.e) {
                    n16 = n19 + n20 * 231871;
                    if (((n16 = n16 * n16 * 31287121 + n16 * 11) >> 20 & 1) == 0) {
                        int n27 = uu.w.bn;
                        nArray[n27] = nArray[n27] + 10;
                    } else {
                        int n28 = uu.u.bn;
                        nArray[n28] = nArray[n28] + 10;
                    }
                    d3 = 100.0;
                } else {
                    for (n16 = 0; n16 < n4; ++n16) {
                        for (n15 = 0; n15 < n4; ++n15) {
                            n14 = lm2.b(n16 + n24, n15 + n25) + 1;
                            int n29 = 0;
                            if (n14 > 1) {
                                n13 = 0;
                                do {
                                    n13 = 1;
                                    n29 = lm2.a(n16 + n24, n14 - 1, n15 + n25);
                                    if (n29 == 0) {
                                        n13 = 0;
                                    } else if (n14 > 0 && n29 > 0 && uu.m[n29].bA.C == dx.b) {
                                        n13 = 0;
                                    }
                                    if (n13 != 0) continue;
                                    n29 = lm2.a(n16 + n24, --n14 - 1, n15 + n25);
                                } while (n13 == 0);
                                if (n29 != 0 && uu.m[n29].bA.d()) {
                                    n12 = n14 - 1;
                                    int n30 = 0;
                                    do {
                                        n30 = lm2.a(n16 + n24, n12--, n15 + n25);
                                        ++n26;
                                    } while (n12 > 0 && n30 != 0 && uu.m[n30].bA.d());
                                }
                            }
                            d3 += (double)n14 / (double)(n4 * n4);
                            int n31 = n29;
                            nArray[n31] = nArray[n31] + 1;
                        }
                    }
                }
                n26 /= n4 * n4;
                n21 /= n4 * n4;
                n22 /= n4 * n4;
                n23 /= n4 * n4;
                n16 = 0;
                n15 = 0;
                for (n14 = 0; n14 < 256; ++n14) {
                    if (nArray[n14] <= n16) continue;
                    n15 = n14;
                    n16 = nArray[n14];
                }
                double d4 = (d3 - d2) * 4.0 / (double)(n4 + 4) + ((double)(i2 + i3 & 1) - 0.5) * 0.4;
                n13 = 1;
                if (d4 > 0.6) {
                    n13 = 2;
                }
                if (d4 < -0.6) {
                    n13 = 0;
                }
                n12 = 0;
                if (n15 > 0) {
                    dx dx2 = uu.m[n15].bA.C;
                    if (dx2 == dx.n) {
                        d4 = (double)n26 * 0.1 + (double)(i2 + i3 & 1) * 0.2;
                        n13 = 1;
                        if (d4 < 0.5) {
                            n13 = 2;
                        }
                        if (d4 > 0.9) {
                            n13 = 0;
                        }
                    }
                    n12 = dx2.q;
                }
                d2 = d3;
                if (i3 < 0 || n17 * n17 + n18 * n18 >= n9 * n9 || bl2 && (i2 + i3 & 1) == 0 || (by3 = iu2.f[i2 + i3 * n2]) == (by2 = (byte)(n12 * 4 + n13))) continue;
                if (n10 > i3) {
                    n10 = i3;
                }
                if (n11 < i3) {
                    n11 = i3;
                }
                iu2.f[i2 + i3 * n2] = by2;
            }
            if (n10 > n11) continue;
            iu2.a(i2, n10, n11);
        }
    }

    public void a(iz iz2, fd fd2, sn sn2, int n2, boolean bl2) {
        if (fd2.B) {
            return;
        }
        iu iu2 = this.a(iz2, fd2);
        if (sn2 instanceof gs) {
            gs gs2 = (gs)sn2;
            iu2.a(gs2, iz2);
        }
        if (bl2) {
            this.a(fd2, sn2, iu2);
        }
    }

    public void b(iz iz2, fd fd2, gs gs2) {
        iz2.b(fd2.b("map"));
        String string = "map_" + iz2.i();
        iu iu2 = new iu(string);
        fd2.a(string, iu2);
        iu2.b = in.b(gs2.aM);
        iu2.c = in.b(gs2.aO);
        iu2.e = (byte)3;
        iu2.d = (byte)fd2.t.g;
        iu2.a();
    }
}

