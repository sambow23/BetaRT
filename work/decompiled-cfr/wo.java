/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class wo
extends uu {
    public static final double[] a = new double[]{-0.0625, 0.0625, 0.1875, 0.3125};
    private static final int[] b = new int[]{1, 2, 3, 4};
    private final boolean c;

    protected wo(int n2, boolean bl2) {
        super(n2, 6, ln.o);
        this.c = bl2;
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.125f, 1.0f);
    }

    public boolean d() {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        if (!fd2.h(n2, n3 - 1, n4)) {
            return false;
        }
        return super.a(fd2, n2, n3, n4);
    }

    public boolean g(fd fd2, int n2, int n3, int n4) {
        if (!fd2.h(n2, n3 - 1, n4)) {
            return false;
        }
        return super.g(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        int n5 = fd2.e(n2, n3, n4);
        boolean bl2 = this.f(fd2, n2, n3, n4, n5);
        if (this.c && !bl2) {
            fd2.b(n2, n3, n4, uu.bi.bn, n5);
        } else if (!this.c) {
            fd2.b(n2, n3, n4, uu.bj.bn, n5);
            if (!bl2) {
                int n6 = (n5 & 0xC) >> 2;
                fd2.c(n2, n3, n4, uu.bj.bn, b[n6] * 2);
            }
        }
    }

    public int a(int n2, int n3) {
        if (n2 == 0) {
            if (this.c) {
                return 99;
            }
            return 115;
        }
        if (n2 == 1) {
            if (this.c) {
                return 147;
            }
            return 131;
        }
        return 5;
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        return n5 != 0 && n5 != 1;
    }

    public int b() {
        return 15;
    }

    public int a(int n2) {
        return this.a(n2, 0);
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        return this.c((xp)fd2, n2, n3, n4, n5);
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        if (!this.c) {
            return false;
        }
        int n6 = xp2.e(n2, n3, n4) & 3;
        if (n6 == 0 && n5 == 3) {
            return true;
        }
        if (n6 == 1 && n5 == 4) {
            return true;
        }
        if (n6 == 2 && n5 == 2) {
            return true;
        }
        return n6 == 3 && n5 == 5;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (!this.g(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
            return;
        }
        int n6 = fd2.e(n2, n3, n4);
        boolean bl2 = this.f(fd2, n2, n3, n4, n6);
        int n7 = (n6 & 0xC) >> 2;
        if (this.c && !bl2) {
            fd2.c(n2, n3, n4, this.bn, b[n7] * 2);
        } else if (!this.c && bl2) {
            fd2.c(n2, n3, n4, this.bn, b[n7] * 2);
        }
    }

    private boolean f(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = n5 & 3;
        switch (n6) {
            case 0: {
                return fd2.k(n2, n3, n4 + 1, 3) || fd2.a(n2, n3, n4 + 1) == uu.aw.bn && fd2.e(n2, n3, n4 + 1) > 0;
            }
            case 2: {
                return fd2.k(n2, n3, n4 - 1, 2) || fd2.a(n2, n3, n4 - 1) == uu.aw.bn && fd2.e(n2, n3, n4 - 1) > 0;
            }
            case 3: {
                return fd2.k(n2 + 1, n3, n4, 5) || fd2.a(n2 + 1, n3, n4) == uu.aw.bn && fd2.e(n2 + 1, n3, n4) > 0;
            }
            case 1: {
                return fd2.k(n2 - 1, n3, n4, 4) || fd2.a(n2 - 1, n3, n4) == uu.aw.bn && fd2.e(n2 - 1, n3, n4) > 0;
            }
        }
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        int n5 = fd2.e(n2, n3, n4);
        int n6 = (n5 & 0xC) >> 2;
        n6 = n6 + 1 << 2 & 0xC;
        fd2.d(n2, n3, n4, n6 | n5 & 3);
        return true;
    }

    public boolean f() {
        return false;
    }

    public void a(fd fd2, int n2, int n3, int n4, ls ls2) {
        int n5 = ((in.b((double)(ls2.aS * 4.0f / 360.0f) + 0.5) & 3) + 2) % 4;
        fd2.d(n2, n3, n4, n5);
        boolean bl2 = this.f(fd2, n2, n3, n4, n5);
        if (bl2) {
            fd2.c(n2, n3, n4, this.bn, 1);
        }
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        fd2.i(n2 + 1, n3, n4, this.bn);
        fd2.i(n2 - 1, n3, n4, this.bn);
        fd2.i(n2, n3, n4 + 1, this.bn);
        fd2.i(n2, n3, n4 - 1, this.bn);
        fd2.i(n2, n3 - 1, n4, this.bn);
        fd2.i(n2, n3 + 1, n4, this.bn);
    }

    public boolean c() {
        return false;
    }

    public int a(int n2, Random random) {
        return gm.aZ.bf;
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        if (!this.c) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        double d2 = (double)((float)n2 + 0.5f) + (double)(random.nextFloat() - 0.5f) * 0.2;
        double d3 = (double)((float)n3 + 0.4f) + (double)(random.nextFloat() - 0.5f) * 0.2;
        double d4 = (double)((float)n4 + 0.5f) + (double)(random.nextFloat() - 0.5f) * 0.2;
        double d5 = 0.0;
        double d6 = 0.0;
        if (random.nextInt(2) == 0) {
            switch (n5 & 3) {
                case 0: {
                    d6 = -0.3125;
                    break;
                }
                case 2: {
                    d6 = 0.3125;
                    break;
                }
                case 3: {
                    d5 = -0.3125;
                    break;
                }
                case 1: {
                    d5 = 0.3125;
                }
            }
        } else {
            int n6 = (n5 & 0xC) >> 2;
            switch (n5 & 3) {
                case 0: {
                    d6 = a[n6];
                    break;
                }
                case 2: {
                    d6 = -a[n6];
                    break;
                }
                case 3: {
                    d5 = a[n6];
                    break;
                }
                case 1: {
                    d5 = -a[n6];
                }
            }
        }
        fd2.a("reddust", d2 + d5, d3, d4 + d6, 0.0, 0.0, 0.0);
    }
}

