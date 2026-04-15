/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;

public class jq
extends uu {
    private boolean a;
    private boolean b;

    public jq(int n2, int n3, boolean bl2) {
        super(n2, n3, ln.B);
        this.a = bl2;
        this.a(h);
        this.c(0.5f);
    }

    public int i() {
        if (this.a) {
            return 106;
        }
        return 107;
    }

    public int a(int n2, int n3) {
        int n4 = jq.d(n3);
        if (n4 > 5) {
            return this.bm;
        }
        if (n2 == n4) {
            if (jq.e(n3) || this.bs > 0.0 || this.bt > 0.0 || this.bu > 0.0 || this.bv < 1.0 || this.bw < 1.0 || this.bx < 1.0) {
                return 110;
            }
            return this.bm;
        }
        if (n2 == wj.a[n4]) {
            return 109;
        }
        return 108;
    }

    public int b() {
        return 16;
    }

    public boolean c() {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        return false;
    }

    public void a(fd fd2, int n2, int n3, int n4, ls ls2) {
        int n5 = jq.c(fd2, n2, n3, n4, (gs)ls2);
        fd2.d(n2, n3, n4, n5);
        if (!fd2.B) {
            this.h(fd2, n2, n3, n4);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (!fd2.B && !this.b) {
            this.h(fd2, n2, n3, n4);
        }
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        if (!fd2.B && fd2.b(n2, n3, n4) == null) {
            this.h(fd2, n2, n3, n4);
        }
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        int n6 = jq.d(n5);
        boolean bl2 = this.f(fd2, n2, n3, n4, n6);
        if (n5 == 7) {
            return;
        }
        if (bl2 && !jq.e(n5)) {
            if (jq.h(fd2, n2, n3, n4, n6)) {
                fd2.e(n2, n3, n4, n6 | 8);
                fd2.d(n2, n3, n4, 0, n6);
            }
        } else if (!bl2 && jq.e(n5)) {
            fd2.e(n2, n3, n4, n6);
            fd2.d(n2, n3, n4, 1, n6);
        }
    }

    private boolean f(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 != 0 && fd2.k(n2, n3 - 1, n4, 0)) {
            return true;
        }
        if (n5 != 1 && fd2.k(n2, n3 + 1, n4, 1)) {
            return true;
        }
        if (n5 != 2 && fd2.k(n2, n3, n4 - 1, 2)) {
            return true;
        }
        if (n5 != 3 && fd2.k(n2, n3, n4 + 1, 3)) {
            return true;
        }
        if (n5 != 5 && fd2.k(n2 + 1, n3, n4, 5)) {
            return true;
        }
        if (n5 != 4 && fd2.k(n2 - 1, n3, n4, 4)) {
            return true;
        }
        if (fd2.k(n2, n3, n4, 0)) {
            return true;
        }
        if (fd2.k(n2, n3 + 2, n4, 1)) {
            return true;
        }
        if (fd2.k(n2, n3 + 1, n4 - 1, 2)) {
            return true;
        }
        if (fd2.k(n2, n3 + 1, n4 + 1, 3)) {
            return true;
        }
        if (fd2.k(n2 - 1, n3 + 1, n4, 4)) {
            return true;
        }
        return fd2.k(n2 + 1, n3 + 1, n4, 5);
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, int n6) {
        this.b = true;
        int n7 = n6;
        if (n5 == 0) {
            if (this.i(fd2, n2, n3, n4, n7)) {
                fd2.d(n2, n3, n4, n7 | 8);
                fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "tile.piston.out", 0.5f, fd2.r.nextFloat() * 0.25f + 0.6f);
            }
        } else if (n5 == 1) {
            ow ow2 = fd2.b(n2 + wj.b[n7], n3 + wj.c[n7], n4 + wj.d[n7]);
            if (ow2 != null && ow2 instanceof uk) {
                ((uk)ow2).l();
            }
            fd2.a(n2, n3, n4, uu.ad.bn, n7);
            fd2.a(n2, n3, n4, ut.a(this.bn, n7, n7, false, true));
            if (this.a) {
                uk uk2;
                ow ow3;
                int n8 = n2 + wj.b[n7] * 2;
                int n9 = n3 + wj.c[n7] * 2;
                int n10 = n4 + wj.d[n7] * 2;
                int n11 = fd2.a(n8, n9, n10);
                int n12 = fd2.e(n8, n9, n10);
                boolean bl2 = false;
                if (n11 == uu.ad.bn && (ow3 = fd2.b(n8, n9, n10)) != null && ow3 instanceof uk && (uk2 = (uk)ow3).d() == n7 && uk2.b()) {
                    uk2.l();
                    n11 = uk2.a();
                    n12 = uk2.e();
                    bl2 = true;
                }
                if (!bl2 && n11 > 0 && jq.a(n11, fd2, n8, n9, n10, false) && (uu.m[n11].h() == 0 || n11 == uu.aa.bn || n11 == uu.W.bn)) {
                    this.b = false;
                    fd2.f(n8, n9, n10, 0);
                    this.b = true;
                    fd2.a(n2 += wj.b[n7], n3 += wj.c[n7], n4 += wj.d[n7], uu.ad.bn, n12);
                    fd2.a(n2, n3, n4, ut.a(n11, n12, n7, false, false));
                } else if (!bl2) {
                    this.b = false;
                    fd2.f(n2 + wj.b[n7], n3 + wj.c[n7], n4 + wj.d[n7], 0);
                    this.b = true;
                }
            } else {
                this.b = false;
                fd2.f(n2 + wj.b[n7], n3 + wj.c[n7], n4 + wj.d[n7], 0);
                this.b = true;
            }
            fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "tile.piston.in", 0.5f, fd2.r.nextFloat() * 0.15f + 0.6f);
        }
        this.b = false;
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        if (jq.e(n5)) {
            switch (jq.d(n5)) {
                case 0: {
                    this.a(0.0f, 0.25f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 1: {
                    this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.75f, 1.0f);
                    break;
                }
                case 2: {
                    this.a(0.0f, 0.0f, 0.25f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 3: {
                    this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.75f);
                    break;
                }
                case 4: {
                    this.a(0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 5: {
                    this.a(0.0f, 0.0f, 0.0f, 0.75f, 1.0f, 1.0f);
                }
            }
        } else {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public void g() {
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void a(fd fd2, int n2, int n3, int n4, eq eq2, ArrayList arrayList) {
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        super.a(fd2, n2, n3, n4, eq2, arrayList);
    }

    public boolean d() {
        return false;
    }

    public static int d(int n2) {
        return n2 & 7;
    }

    public static boolean e(int n2) {
        return (n2 & 8) != 0;
    }

    private static int c(fd fd2, int n2, int n3, int n4, gs gs2) {
        int n5;
        if (in.e((float)gs2.aM - (float)n2) < 2.0f && in.e((float)gs2.aO - (float)n4) < 2.0f) {
            double d2 = gs2.aN + 1.82 - (double)gs2.bf;
            if (d2 - (double)n3 > 2.0) {
                return 1;
            }
            if ((double)n3 - d2 > 0.0) {
                return 0;
            }
        }
        if ((n5 = in.b((double)(gs2.aS * 4.0f / 360.0f) + 0.5) & 3) == 0) {
            return 2;
        }
        if (n5 == 1) {
            return 5;
        }
        if (n5 == 2) {
            return 3;
        }
        if (n5 == 3) {
            return 4;
        }
        return 0;
    }

    private static boolean a(int n2, fd fd2, int n3, int n4, int n5, boolean bl2) {
        ow ow2;
        if (n2 == uu.aq.bn) {
            return false;
        }
        if (n2 == uu.aa.bn || n2 == uu.W.bn) {
            if (jq.e(fd2.e(n3, n4, n5))) {
                return false;
            }
        } else {
            if (uu.m[n2].m() == -1.0f) {
                return false;
            }
            if (uu.m[n2].h() == 2) {
                return false;
            }
            if (!bl2 && uu.m[n2].h() == 1) {
                return false;
            }
        }
        return (ow2 = fd2.b(n3, n4, n5)) == null;
    }

    private static boolean h(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = n2 + wj.b[n5];
        int n7 = n3 + wj.c[n5];
        int n8 = n4 + wj.d[n5];
        for (int i2 = 0; i2 < 13; ++i2) {
            if (n7 <= 0 || n7 >= 127) {
                return false;
            }
            int n9 = fd2.a(n6, n7, n8);
            if (n9 == 0) break;
            if (!jq.a(n9, fd2, n6, n7, n8, true)) {
                return false;
            }
            if (uu.m[n9].h() == 1) break;
            if (i2 == 12) {
                return false;
            }
            n6 += wj.b[n5];
            n7 += wj.c[n5];
            n8 += wj.d[n5];
        }
        return true;
    }

    private boolean i(fd fd2, int n2, int n3, int n4, int n5) {
        int n6;
        int n7;
        int n8 = n2 + wj.b[n5];
        int n9 = n3 + wj.c[n5];
        int n10 = n4 + wj.d[n5];
        for (n7 = 0; n7 < 13; ++n7) {
            if (n9 <= 0 || n9 >= 127) {
                return false;
            }
            n6 = fd2.a(n8, n9, n10);
            if (n6 == 0) break;
            if (!jq.a(n6, fd2, n8, n9, n10, true)) {
                return false;
            }
            if (uu.m[n6].h() == 1) {
                uu.m[n6].g(fd2, n8, n9, n10, fd2.e(n8, n9, n10));
                fd2.f(n8, n9, n10, 0);
                break;
            }
            if (n7 == 12) {
                return false;
            }
            n8 += wj.b[n5];
            n9 += wj.c[n5];
            n10 += wj.d[n5];
        }
        while (n8 != n2 || n9 != n3 || n10 != n4) {
            n7 = n8 - wj.b[n5];
            n6 = n9 - wj.c[n5];
            int n11 = n10 - wj.d[n5];
            int n12 = fd2.a(n7, n6, n11);
            int n13 = fd2.e(n7, n6, n11);
            if (n12 == this.bn && n7 == n2 && n6 == n3 && n11 == n4) {
                fd2.a(n8, n9, n10, uu.ad.bn, n5 | (this.a ? 8 : 0));
                fd2.a(n8, n9, n10, ut.a(uu.ab.bn, n5 | (this.a ? 8 : 0), n5, true, false));
            } else {
                fd2.a(n8, n9, n10, uu.ad.bn, n13);
                fd2.a(n8, n9, n10, ut.a(n12, n13, n5, true, false));
            }
            n8 = n7;
            n9 = n6;
            n10 = n11;
        }
        return true;
    }
}

