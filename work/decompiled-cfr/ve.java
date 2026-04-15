/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ve
extends uu {
    public static final int[][] a = new int[][]{{0, 1}, {-1, 0}, {0, -1}, {1, 0}};

    public ve(int n2) {
        super(n2, 134, ln.l);
        this.r();
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        cw cw2;
        if (fd2.B) {
            return true;
        }
        int n5 = fd2.e(n2, n3, n4);
        if (!ve.e(n5)) {
            int n6 = ve.d(n5);
            if (fd2.a(n2 += a[n6][0], n3, n4 += a[n6][1]) != this.bn) {
                return true;
            }
            n5 = fd2.e(n2, n3, n4);
        }
        if (!fd2.t.f()) {
            double d2 = (double)n2 + 0.5;
            double d3 = (double)n3 + 0.5;
            double d4 = (double)n4 + 0.5;
            fd2.f(n2, n3, n4, 0);
            int n7 = ve.d(n5);
            if (fd2.a(n2 += a[n7][0], n3, n4 += a[n7][1]) == this.bn) {
                fd2.f(n2, n3, n4, 0);
                d2 = (d2 + (double)n2 + 0.5) / 2.0;
                d3 = (d3 + (double)n3 + 0.5) / 2.0;
                d4 = (d4 + (double)n4 + 0.5) / 2.0;
            }
            fd2.a(null, (double)((float)n2 + 0.5f), (double)((float)n3 + 0.5f), (float)n4 + 0.5f, 5.0f, true);
            return true;
        }
        if (ve.f(n5)) {
            gs gs3 = null;
            for (gs gs4 : fd2.d) {
                if (!gs4.N()) continue;
                br br2 = gs4.v;
                if (br2.a != n2 || br2.b != n3 || br2.c != n4) continue;
                gs3 = gs4;
            }
            if (gs3 == null) {
                ve.a(fd2, n2, n3, n4, false);
            } else {
                gs2.b("tile.bed.occupied");
                return true;
            }
        }
        if ((cw2 = gs2.b(n2, n3, n4)) == cw.a) {
            ve.a(fd2, n2, n3, n4, true);
            return true;
        }
        if (cw2 == cw.c) {
            gs2.b("tile.bed.noSleep");
        }
        return true;
    }

    public int a(int n2, int n3) {
        if (n2 == 0) {
            return uu.y.bm;
        }
        int n4 = ve.d(n3);
        int n5 = jj.c[n4][n2];
        if (ve.e(n3)) {
            if (n5 == 2) {
                return this.bm + 2 + 16;
            }
            if (n5 == 5 || n5 == 4) {
                return this.bm + 1 + 16;
            }
            return this.bm + 1;
        }
        if (n5 == 3) {
            return this.bm - 1 + 16;
        }
        if (n5 == 5 || n5 == 4) {
            return this.bm + 16;
        }
        return this.bm;
    }

    public int b() {
        return 14;
    }

    public boolean d() {
        return false;
    }

    public boolean c() {
        return false;
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        this.r();
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        int n7 = ve.d(n6);
        if (ve.e(n6)) {
            if (fd2.a(n2 - a[n7][0], n3, n4 - a[n7][1]) != this.bn) {
                fd2.f(n2, n3, n4, 0);
            }
        } else if (fd2.a(n2 + a[n7][0], n3, n4 + a[n7][1]) != this.bn) {
            fd2.f(n2, n3, n4, 0);
            if (!fd2.B) {
                this.g(fd2, n2, n3, n4, n6);
            }
        }
    }

    public int a(int n2, Random random) {
        if (ve.e(n2)) {
            return 0;
        }
        return gm.aY.bf;
    }

    private void r() {
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.5625f, 1.0f);
    }

    public static int d(int n2) {
        return n2 & 3;
    }

    public static boolean e(int n2) {
        return (n2 & 8) != 0;
    }

    public static boolean f(int n2) {
        return (n2 & 4) != 0;
    }

    public static void a(fd fd2, int n2, int n3, int n4, boolean bl2) {
        int n5 = fd2.e(n2, n3, n4);
        n5 = bl2 ? (n5 |= 4) : (n5 &= 0xFFFFFFFB);
        fd2.d(n2, n3, n4, n5);
    }

    public static br f(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        int n7 = ve.d(n6);
        for (int i2 = 0; i2 <= 1; ++i2) {
            int n8 = n2 - a[n7][0] * i2 - 1;
            int n9 = n4 - a[n7][1] * i2 - 1;
            int n10 = n8 + 2;
            int n11 = n9 + 2;
            for (int i3 = n8; i3 <= n10; ++i3) {
                for (int i4 = n9; i4 <= n11; ++i4) {
                    if (!fd2.h(i3, n3 - 1, i4) || !fd2.d(i3, n3, i4) || !fd2.d(i3, n3 + 1, i4)) continue;
                    if (n5 > 0) {
                        --n5;
                        continue;
                    }
                    return new br(i3, n3, i4);
                }
            }
        }
        return null;
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, float f2) {
        if (!ve.e(n5)) {
            super.a(fd2, n2, n3, n4, n5, f2);
        }
    }

    public int h() {
        return 1;
    }
}

