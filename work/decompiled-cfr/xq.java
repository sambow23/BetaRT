/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class xq
extends rw {
    private Random a = new Random();

    protected xq(int n2) {
        super(n2, ln.e);
        this.bm = 45;
    }

    public int e() {
        return 4;
    }

    public int a(int n2, Random random) {
        return uu.Q.bn;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        super.c(fd2, n2, n3, n4);
        this.h(fd2, n2, n3, n4);
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        if (fd2.B) {
            return;
        }
        int n5 = fd2.a(n2, n3, n4 - 1);
        int n6 = fd2.a(n2, n3, n4 + 1);
        int n7 = fd2.a(n2 - 1, n3, n4);
        int n8 = fd2.a(n2 + 1, n3, n4);
        int n9 = 3;
        if (uu.o[n5] && !uu.o[n6]) {
            n9 = 3;
        }
        if (uu.o[n6] && !uu.o[n5]) {
            n9 = 2;
        }
        if (uu.o[n7] && !uu.o[n8]) {
            n9 = 5;
        }
        if (uu.o[n8] && !uu.o[n7]) {
            n9 = 4;
        }
        fd2.d(n2, n3, n4, n9);
    }

    public int a(xp xp2, int n2, int n3, int n4, int n5) {
        if (n5 == 1) {
            return this.bm + 17;
        }
        if (n5 == 0) {
            return this.bm + 17;
        }
        int n6 = xp2.e(n2, n3, n4);
        if (n5 != n6) {
            return this.bm;
        }
        return this.bm + 1;
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm + 17;
        }
        if (n2 == 0) {
            return this.bm + 17;
        }
        if (n2 == 3) {
            return this.bm + 1;
        }
        return this.bm;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.B) {
            return true;
        }
        az az2 = (az)fd2.b(n2, n3, n4);
        gs2.a(az2);
        return true;
    }

    private void c(fd fd2, int n2, int n3, int n4, Random random) {
        int n5 = fd2.e(n2, n3, n4);
        int n6 = 0;
        int n7 = 0;
        if (n5 == 3) {
            n7 = 1;
        } else if (n5 == 2) {
            n7 = -1;
        } else {
            n6 = n5 == 5 ? 1 : -1;
        }
        az az2 = (az)fd2.b(n2, n3, n4);
        iz iz2 = az2.b();
        double d2 = (double)n2 + (double)n6 * 0.6 + 0.5;
        double d3 = (double)n3 + 0.5;
        double d4 = (double)n4 + (double)n7 * 0.6 + 0.5;
        if (iz2 == null) {
            fd2.e(1001, n2, n3, n4, 0);
        } else {
            if (iz2.c == gm.j.bf) {
                sl sl2 = new sl(fd2, d2, d3, d4);
                sl2.a((double)n6, (double)0.1f, (double)n7, 1.1f, 6.0f);
                sl2.a = true;
                fd2.b(sl2);
                fd2.e(1002, n2, n3, n4, 0);
            } else if (iz2.c == gm.aN.bf) {
                vv vv2 = new vv(fd2, d2, d3, d4);
                vv2.a((double)n6, (double)0.1f, (double)n7, 1.1f, 6.0f);
                fd2.b(vv2);
                fd2.e(1002, n2, n3, n4, 0);
            } else if (iz2.c == gm.aB.bf) {
                by by2 = new by(fd2, d2, d3, d4);
                by2.a((double)n6, (double)0.1f, (double)n7, 1.1f, 6.0f);
                fd2.b(by2);
                fd2.e(1002, n2, n3, n4, 0);
            } else {
                hl hl2 = new hl(fd2, d2, d3 - 0.3, d4, iz2);
                double d5 = random.nextDouble() * 0.1 + 0.2;
                hl2.aP = (double)n6 * d5;
                hl2.aQ = 0.2f;
                hl2.aR = (double)n7 * d5;
                hl2.aP += random.nextGaussian() * (double)0.0075f * 6.0;
                hl2.aQ += random.nextGaussian() * (double)0.0075f * 6.0;
                hl2.aR += random.nextGaussian() * (double)0.0075f * 6.0;
                fd2.b(hl2);
                fd2.e(1000, n2, n3, n4, 0);
            }
            fd2.e(2000, n2, n3, n4, n6 + 1 + (n7 + 1) * 3);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 > 0 && uu.m[n5].f()) {
            boolean bl2;
            boolean bl3 = bl2 = fd2.s(n2, n3, n4) || fd2.s(n2, n3 + 1, n4);
            if (bl2) {
                fd2.c(n2, n3, n4, this.bn, this.e());
            }
        }
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.s(n2, n3, n4) || fd2.s(n2, n3 + 1, n4)) {
            this.c(fd2, n2, n3, n4, random);
        }
    }

    protected ow a_() {
        return new az();
    }

    public void a(fd fd2, int n2, int n3, int n4, ls ls2) {
        int n5 = in.b((double)(ls2.aS * 4.0f / 360.0f) + 0.5) & 3;
        if (n5 == 0) {
            fd2.d(n2, n3, n4, 2);
        }
        if (n5 == 1) {
            fd2.d(n2, n3, n4, 5);
        }
        if (n5 == 2) {
            fd2.d(n2, n3, n4, 3);
        }
        if (n5 == 3) {
            fd2.d(n2, n3, n4, 4);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        az az2 = (az)fd2.b(n2, n3, n4);
        for (int i2 = 0; i2 < az2.a(); ++i2) {
            iz iz2 = az2.f_(i2);
            if (iz2 == null) continue;
            float f2 = this.a.nextFloat() * 0.8f + 0.1f;
            float f3 = this.a.nextFloat() * 0.8f + 0.1f;
            float f4 = this.a.nextFloat() * 0.8f + 0.1f;
            while (iz2.a > 0) {
                int n5 = this.a.nextInt(21) + 10;
                if (n5 > iz2.a) {
                    n5 = iz2.a;
                }
                iz2.a -= n5;
                hl hl2 = new hl(fd2, (float)n2 + f2, (float)n3 + f3, (float)n4 + f4, new iz(iz2.c, n5, iz2.i()));
                float f5 = 0.05f;
                hl2.aP = (float)this.a.nextGaussian() * f5;
                hl2.aQ = (float)this.a.nextGaussian() * f5 + 0.2f;
                hl2.aR = (float)this.a.nextGaussian() * f5;
                fd2.b(hl2);
            }
        }
        super.b(fd2, n2, n3, n4);
    }
}

