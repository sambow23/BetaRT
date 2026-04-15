/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ur {
    private Random a = new Random();

    public void a(fd fd2, sn sn2) {
        if (this.b(fd2, sn2)) {
            return;
        }
        this.c(fd2, sn2);
        this.b(fd2, sn2);
    }

    public boolean b(fd fd2, sn sn2) {
        double d2;
        int n2;
        int n3 = 128;
        double d3 = -1.0;
        int n4 = 0;
        int n5 = 0;
        int n6 = 0;
        int n7 = in.b(sn2.aM);
        int n8 = in.b(sn2.aO);
        for (n2 = n7 - n3; n2 <= n7 + n3; ++n2) {
            double d4 = (double)n2 + 0.5 - sn2.aM;
            for (int i2 = n8 - n3; i2 <= n8 + n3; ++i2) {
                double d5 = (double)i2 + 0.5 - sn2.aO;
                for (int i3 = 127; i3 >= 0; --i3) {
                    if (fd2.a(n2, i3, i2) != uu.bf.bn) continue;
                    while (fd2.a(n2, i3 - 1, i2) == uu.bf.bn) {
                        --i3;
                    }
                    d2 = (double)i3 + 0.5 - sn2.aN;
                    double d6 = d4 * d4 + d2 * d2 + d5 * d5;
                    if (!(d3 < 0.0) && !(d6 < d3)) continue;
                    d3 = d6;
                    n4 = n2;
                    n5 = i3;
                    n6 = i2;
                }
            }
        }
        if (d3 >= 0.0) {
            n2 = n4;
            int n9 = n5;
            int n10 = n6;
            double d7 = (double)n2 + 0.5;
            double d8 = (double)n9 + 0.5;
            d2 = (double)n10 + 0.5;
            if (fd2.a(n2 - 1, n9, n10) == uu.bf.bn) {
                d7 -= 0.5;
            }
            if (fd2.a(n2 + 1, n9, n10) == uu.bf.bn) {
                d7 += 0.5;
            }
            if (fd2.a(n2, n9, n10 - 1) == uu.bf.bn) {
                d2 -= 0.5;
            }
            if (fd2.a(n2, n9, n10 + 1) == uu.bf.bn) {
                d2 += 0.5;
            }
            sn2.c(d7, d8, d2, sn2.aS, 0.0f);
            sn2.aR = 0.0;
            sn2.aQ = 0.0;
            sn2.aP = 0.0;
            return true;
        }
        return false;
    }

    public boolean c(fd fd2, sn sn2) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        double d2;
        int n11;
        double d3;
        int n12;
        int n13 = 16;
        double d4 = -1.0;
        int n14 = in.b(sn2.aM);
        int n15 = in.b(sn2.aN);
        int n16 = in.b(sn2.aO);
        int n17 = n14;
        int n18 = n15;
        int n19 = n16;
        int n20 = 0;
        int n21 = this.a.nextInt(4);
        for (n12 = n14 - n13; n12 <= n14 + n13; ++n12) {
            d3 = (double)n12 + 0.5 - sn2.aM;
            for (n11 = n16 - n13; n11 <= n16 + n13; ++n11) {
                d2 = (double)n11 + 0.5 - sn2.aO;
                block2: for (n10 = 127; n10 >= 0; --n10) {
                    if (!fd2.d(n12, n10, n11)) continue;
                    while (n10 > 0 && fd2.d(n12, n10 - 1, n11)) {
                        --n10;
                    }
                    for (n9 = n21; n9 < n21 + 4; ++n9) {
                        n8 = n9 % 2;
                        n7 = 1 - n8;
                        if (n9 % 4 >= 2) {
                            n8 = -n8;
                            n7 = -n7;
                        }
                        for (n6 = 0; n6 < 3; ++n6) {
                            for (n5 = 0; n5 < 4; ++n5) {
                                for (n4 = -1; n4 < 4; ++n4) {
                                    n3 = n12 + (n5 - 1) * n8 + n6 * n7;
                                    n2 = n10 + n4;
                                    int n22 = n11 + (n5 - 1) * n7 - n6 * n8;
                                    if (n4 < 0 && !fd2.f(n3, n2, n22).a() || n4 >= 0 && !fd2.d(n3, n2, n22)) continue block2;
                                }
                            }
                        }
                        double d5 = (double)n10 + 0.5 - sn2.aN;
                        double d6 = d3 * d3 + d5 * d5 + d2 * d2;
                        if (!(d4 < 0.0) && !(d6 < d4)) continue;
                        d4 = d6;
                        n17 = n12;
                        n18 = n10;
                        n19 = n11;
                        n20 = n9 % 4;
                    }
                }
            }
        }
        if (d4 < 0.0) {
            for (n12 = n14 - n13; n12 <= n14 + n13; ++n12) {
                d3 = (double)n12 + 0.5 - sn2.aM;
                for (n11 = n16 - n13; n11 <= n16 + n13; ++n11) {
                    d2 = (double)n11 + 0.5 - sn2.aO;
                    block10: for (n10 = 127; n10 >= 0; --n10) {
                        if (!fd2.d(n12, n10, n11)) continue;
                        while (fd2.d(n12, n10 - 1, n11)) {
                            --n10;
                        }
                        for (n9 = n21; n9 < n21 + 2; ++n9) {
                            n8 = n9 % 2;
                            n7 = 1 - n8;
                            for (int i2 = 0; i2 < 4; ++i2) {
                                for (n5 = -1; n5 < 4; ++n5) {
                                    int n23 = n12 + (i2 - 1) * n8;
                                    n3 = n10 + n5;
                                    n2 = n11 + (i2 - 1) * n7;
                                    if (n5 < 0 && !fd2.f(n23, n3, n2).a() || n5 >= 0 && !fd2.d(n23, n3, n2)) continue block10;
                                }
                            }
                            double d7 = (double)n10 + 0.5 - sn2.aN;
                            double d8 = d3 * d3 + d7 * d7 + d2 * d2;
                            if (!(d4 < 0.0) && !(d8 < d4)) continue;
                            d4 = d8;
                            n17 = n12;
                            n18 = n10;
                            n19 = n11;
                            n20 = n9 % 2;
                        }
                    }
                }
            }
        }
        n12 = n20;
        int n24 = n17;
        int n25 = n18;
        n11 = n19;
        int n26 = n12 % 2;
        int n27 = 1 - n26;
        if (n12 % 4 >= 2) {
            n26 = -n26;
            n27 = -n27;
        }
        if (d4 < 0.0) {
            if (n18 < 70) {
                n18 = 70;
            }
            if (n18 > 118) {
                n18 = 118;
            }
            n25 = n18;
            for (n10 = -1; n10 <= 1; ++n10) {
                for (n9 = 1; n9 < 3; ++n9) {
                    for (n8 = -1; n8 < 3; ++n8) {
                        n7 = n24 + (n9 - 1) * n26 + n10 * n27;
                        n6 = n25 + n8;
                        n5 = n11 + (n9 - 1) * n27 - n10 * n26;
                        n4 = n8 < 0 ? 1 : 0;
                        fd2.f(n7, n6, n5, n4 != 0 ? uu.aq.bn : 0);
                    }
                }
            }
        }
        for (n10 = 0; n10 < 4; ++n10) {
            fd2.o = true;
            for (n9 = 0; n9 < 4; ++n9) {
                for (n8 = -1; n8 < 4; ++n8) {
                    n7 = n24 + (n9 - 1) * n26;
                    n6 = n25 + n8;
                    n5 = n11 + (n9 - 1) * n27;
                    n4 = n9 == 0 || n9 == 3 || n8 == -1 || n8 == 3 ? 1 : 0;
                    fd2.f(n7, n6, n5, n4 != 0 ? uu.aq.bn : uu.bf.bn);
                }
            }
            fd2.o = false;
            for (n9 = 0; n9 < 4; ++n9) {
                for (n8 = -1; n8 < 4; ++n8) {
                    n7 = n24 + (n9 - 1) * n26;
                    n6 = n25 + n8;
                    n5 = n11 + (n9 - 1) * n27;
                    fd2.i(n7, n6, n5, fd2.a(n7, n6, n5));
                }
            }
        }
        return true;
    }
}

