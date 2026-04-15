/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class bk
extends nr {
    private int c;
    int[] a;

    protected bk(int n2, int n3) {
        super(n2, n3, ln.i, false);
        this.c = n3;
        this.b(true);
    }

    public int b(int n2) {
        if ((n2 & 1) == 1) {
            return jh.a();
        }
        if ((n2 & 2) == 2) {
            return jh.b();
        }
        return jh.c();
    }

    public int b(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        if ((n5 & 1) == 1) {
            return jh.a();
        }
        if ((n5 & 2) == 2) {
            return jh.b();
        }
        xp2.a().a(n2, n4, 1, 1);
        double d2 = xp2.a().a[0];
        double d3 = xp2.a().b[0];
        return jh.a(d2, d3);
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        int n5 = 1;
        int n6 = n5 + 1;
        if (fd2.a(n2 - n6, n3 - n6, n4 - n6, n2 + n6, n3 + n6, n4 + n6)) {
            for (int i2 = -n5; i2 <= n5; ++i2) {
                for (int i3 = -n5; i3 <= n5; ++i3) {
                    for (int i4 = -n5; i4 <= n5; ++i4) {
                        int n7 = fd2.a(n2 + i2, n3 + i3, n4 + i4);
                        if (n7 != uu.L.bn) continue;
                        int n8 = fd2.e(n2 + i2, n3 + i3, n4 + i4);
                        fd2.e(n2 + i2, n3 + i3, n4 + i4, n8 | 8);
                    }
                }
            }
        }
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.B) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        if ((n5 & 8) != 0) {
            int n6;
            int n7 = 4;
            int n8 = n7 + 1;
            int n9 = 32;
            int n10 = n9 * n9;
            int n11 = n9 / 2;
            if (this.a == null) {
                this.a = new int[n9 * n9 * n9];
            }
            if (fd2.a(n2 - n8, n3 - n8, n4 - n8, n2 + n8, n3 + n8, n4 + n8)) {
                int n12;
                int n13;
                int n14;
                for (n6 = -n7; n6 <= n7; ++n6) {
                    for (n14 = -n7; n14 <= n7; ++n14) {
                        for (n13 = -n7; n13 <= n7; ++n13) {
                            n12 = fd2.a(n2 + n6, n3 + n14, n4 + n13);
                            this.a[(n6 + n11) * n10 + (n14 + n11) * n9 + (n13 + n11)] = n12 == uu.K.bn ? 0 : (n12 == uu.L.bn ? -2 : -1);
                        }
                    }
                }
                for (n6 = 1; n6 <= 4; ++n6) {
                    for (n14 = -n7; n14 <= n7; ++n14) {
                        for (n13 = -n7; n13 <= n7; ++n13) {
                            for (n12 = -n7; n12 <= n7; ++n12) {
                                if (this.a[(n14 + n11) * n10 + (n13 + n11) * n9 + (n12 + n11)] != n6 - 1) continue;
                                if (this.a[(n14 + n11 - 1) * n10 + (n13 + n11) * n9 + (n12 + n11)] == -2) {
                                    this.a[(n14 + n11 - 1) * n10 + (n13 + n11) * n9 + (n12 + n11)] = n6;
                                }
                                if (this.a[(n14 + n11 + 1) * n10 + (n13 + n11) * n9 + (n12 + n11)] == -2) {
                                    this.a[(n14 + n11 + 1) * n10 + (n13 + n11) * n9 + (n12 + n11)] = n6;
                                }
                                if (this.a[(n14 + n11) * n10 + (n13 + n11 - 1) * n9 + (n12 + n11)] == -2) {
                                    this.a[(n14 + n11) * n10 + (n13 + n11 - 1) * n9 + (n12 + n11)] = n6;
                                }
                                if (this.a[(n14 + n11) * n10 + (n13 + n11 + 1) * n9 + (n12 + n11)] == -2) {
                                    this.a[(n14 + n11) * n10 + (n13 + n11 + 1) * n9 + (n12 + n11)] = n6;
                                }
                                if (this.a[(n14 + n11) * n10 + (n13 + n11) * n9 + (n12 + n11 - 1)] == -2) {
                                    this.a[(n14 + n11) * n10 + (n13 + n11) * n9 + (n12 + n11 - 1)] = n6;
                                }
                                if (this.a[(n14 + n11) * n10 + (n13 + n11) * n9 + (n12 + n11 + 1)] != -2) continue;
                                this.a[(n14 + n11) * n10 + (n13 + n11) * n9 + (n12 + n11 + 1)] = n6;
                            }
                        }
                    }
                }
            }
            if ((n6 = this.a[n11 * n10 + n11 * n9 + n11]) >= 0) {
                fd2.e(n2, n3, n4, n5 & 0xFFFFFFF7);
            } else {
                this.h(fd2, n2, n3, n4);
            }
        }
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
        fd2.f(n2, n3, n4, 0);
    }

    public int a(Random random) {
        return random.nextInt(20) == 0 ? 1 : 0;
    }

    public int a(int n2, Random random) {
        return uu.z.bn;
    }

    public void a(fd fd2, gs gs2, int n2, int n3, int n4, int n5) {
        if (!fd2.B && gs2.G() != null && gs2.G().c == gm.bc.bf) {
            gs2.a(jl.C[this.bn], 1);
            this.a(fd2, n2, n3, n4, new iz(uu.L.bn, 1, n5 & 3));
        } else {
            super.a(fd2, gs2, n2, n3, n4, n5);
        }
    }

    protected int b_(int n2) {
        return n2 & 3;
    }

    public boolean c() {
        return !this.b;
    }

    public int a(int n2, int n3) {
        if ((n3 & 3) == 1) {
            return this.bm + 80;
        }
        return this.bm;
    }

    public void a(boolean bl2) {
        this.b = bl2;
        this.bm = this.c + (bl2 ? 0 : 1);
    }

    public void b(fd fd2, int n2, int n3, int n4, sn sn2) {
        super.b(fd2, n2, n3, n4, sn2);
    }
}

