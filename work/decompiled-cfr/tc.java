/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class tc
extends rw {
    private Random a = new Random();
    private final boolean b;
    private static boolean c = false;

    protected tc(int n2, boolean bl2) {
        super(n2, ln.e);
        this.b = bl2;
        this.bm = 45;
    }

    public int a(int n2, Random random) {
        return uu.aC.bn;
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
        if (this.b) {
            return this.bm + 16;
        }
        return this.bm - 1;
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        if (!this.b) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        float f2 = (float)n2 + 0.5f;
        float f3 = (float)n3 + 0.0f + random.nextFloat() * 6.0f / 16.0f;
        float f4 = (float)n4 + 0.5f;
        float f5 = 0.52f;
        float f6 = random.nextFloat() * 0.6f - 0.3f;
        if (n5 == 4) {
            fd2.a("smoke", f2 - f5, f3, f4 + f6, 0.0, 0.0, 0.0);
            fd2.a("flame", f2 - f5, f3, f4 + f6, 0.0, 0.0, 0.0);
        } else if (n5 == 5) {
            fd2.a("smoke", f2 + f5, f3, f4 + f6, 0.0, 0.0, 0.0);
            fd2.a("flame", f2 + f5, f3, f4 + f6, 0.0, 0.0, 0.0);
        } else if (n5 == 2) {
            fd2.a("smoke", f2 + f6, f3, f4 - f5, 0.0, 0.0, 0.0);
            fd2.a("flame", f2 + f6, f3, f4 - f5, 0.0, 0.0, 0.0);
        } else if (n5 == 3) {
            fd2.a("smoke", f2 + f6, f3, f4 + f5, 0.0, 0.0, 0.0);
            fd2.a("flame", f2 + f6, f3, f4 + f5, 0.0, 0.0, 0.0);
        }
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm + 17;
        }
        if (n2 == 0) {
            return this.bm + 17;
        }
        if (n2 == 3) {
            return this.bm - 1;
        }
        return this.bm;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.B) {
            return true;
        }
        sk sk2 = (sk)fd2.b(n2, n3, n4);
        gs2.a(sk2);
        return true;
    }

    public static void a(boolean bl2, fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        ow ow2 = fd2.b(n2, n3, n4);
        c = true;
        if (bl2) {
            fd2.f(n2, n3, n4, uu.aD.bn);
        } else {
            fd2.f(n2, n3, n4, uu.aC.bn);
        }
        c = false;
        fd2.d(n2, n3, n4, n5);
        ow2.j();
        fd2.a(n2, n3, n4, ow2);
    }

    protected ow a_() {
        return new sk();
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
        if (!c) {
            sk sk2 = (sk)fd2.b(n2, n3, n4);
            for (int i2 = 0; i2 < sk2.a(); ++i2) {
                iz iz2 = sk2.f_(i2);
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
        }
        super.b(fd2, n2, n3, n4);
    }
}

