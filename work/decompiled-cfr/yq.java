/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class yq
extends uu {
    private int[] a = new int[256];
    private int[] b = new int[256];

    protected yq(int n2, int n3) {
        super(n2, n3, ln.m);
        this.b(true);
    }

    public void k() {
        this.a(uu.y.bn, 5, 20);
        this.a(uu.ba.bn, 5, 20);
        this.a(uu.au.bn, 5, 20);
        this.a(uu.K.bn, 5, 5);
        this.a(uu.L.bn, 30, 60);
        this.a(uu.ao.bn, 30, 20);
        this.a(uu.an.bn, 15, 100);
        this.a(uu.Y.bn, 60, 100);
        this.a(uu.ac.bn, 30, 60);
    }

    private void a(int n2, int n3, int n4) {
        this.a[n2] = n3;
        this.b[n2] = n4;
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 3;
    }

    public int a(Random random) {
        return 0;
    }

    public int e() {
        return 40;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        boolean bl2;
        boolean bl3 = bl2 = fd2.a(n2, n3 - 1, n4) == uu.bc.bn;
        if (!this.a(fd2, n2, n3, n4)) {
            fd2.f(n2, n3, n4, 0);
        }
        if (!bl2 && fd2.C() && (fd2.t(n2, n3, n4) || fd2.t(n2 - 1, n3, n4) || fd2.t(n2 + 1, n3, n4) || fd2.t(n2, n3, n4 - 1) || fd2.t(n2, n3, n4 + 1))) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        if (n5 < 15) {
            fd2.e(n2, n3, n4, n5 + random.nextInt(3) / 2);
        }
        fd2.c(n2, n3, n4, this.bn, this.e());
        if (!bl2 && !this.h(fd2, n2, n3, n4)) {
            if (!fd2.h(n2, n3 - 1, n4) || n5 > 3) {
                fd2.f(n2, n3, n4, 0);
            }
            return;
        }
        if (!bl2 && !this.c((xp)fd2, n2, n3 - 1, n4) && n5 == 15 && random.nextInt(4) == 0) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
        this.a(fd2, n2 + 1, n3, n4, 300, random, n5);
        this.a(fd2, n2 - 1, n3, n4, 300, random, n5);
        this.a(fd2, n2, n3 - 1, n4, 250, random, n5);
        this.a(fd2, n2, n3 + 1, n4, 250, random, n5);
        this.a(fd2, n2, n3, n4 - 1, 300, random, n5);
        this.a(fd2, n2, n3, n4 + 1, 300, random, n5);
        for (int i2 = n2 - 1; i2 <= n2 + 1; ++i2) {
            for (int i3 = n4 - 1; i3 <= n4 + 1; ++i3) {
                for (int i4 = n3 - 1; i4 <= n3 + 4; ++i4) {
                    int n6;
                    int n7;
                    if (i2 == n2 && i4 == n3 && i3 == n4) continue;
                    int n8 = 100;
                    if (i4 > n3 + 1) {
                        n8 += (i4 - (n3 + 1)) * 100;
                    }
                    if ((n7 = this.i(fd2, i2, i4, i3)) <= 0 || (n6 = (n7 + 40) / (n5 + 30)) <= 0 || random.nextInt(n8) > n6 || fd2.C() && fd2.t(i2, i4, i3) || fd2.t(i2 - 1, i4, n4) || fd2.t(i2 + 1, i4, i3) || fd2.t(i2, i4, i3 - 1) || fd2.t(i2, i4, i3 + 1)) continue;
                    int n9 = n5 + random.nextInt(5) / 4;
                    if (n9 > 15) {
                        n9 = 15;
                    }
                    fd2.b(i2, i4, i3, this.bn, n9);
                }
            }
        }
    }

    private void a(fd fd2, int n2, int n3, int n4, int n5, Random random, int n6) {
        int n7 = this.b[fd2.a(n2, n3, n4)];
        if (random.nextInt(n5) < n7) {
            boolean bl2;
            boolean bl3 = bl2 = fd2.a(n2, n3, n4) == uu.an.bn;
            if (random.nextInt(n6 + 10) < 5 && !fd2.t(n2, n3, n4)) {
                int n8 = n6 + random.nextInt(5) / 4;
                if (n8 > 15) {
                    n8 = 15;
                }
                fd2.b(n2, n3, n4, this.bn, n8);
            } else {
                fd2.f(n2, n3, n4, 0);
            }
            if (bl2) {
                uu.an.c(fd2, n2, n3, n4, 1);
            }
        }
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        if (this.c((xp)fd2, n2 + 1, n3, n4)) {
            return true;
        }
        if (this.c((xp)fd2, n2 - 1, n3, n4)) {
            return true;
        }
        if (this.c((xp)fd2, n2, n3 - 1, n4)) {
            return true;
        }
        if (this.c((xp)fd2, n2, n3 + 1, n4)) {
            return true;
        }
        if (this.c((xp)fd2, n2, n3, n4 - 1)) {
            return true;
        }
        return this.c((xp)fd2, n2, n3, n4 + 1);
    }

    private int i(fd fd2, int n2, int n3, int n4) {
        int n5 = 0;
        if (!fd2.d(n2, n3, n4)) {
            return 0;
        }
        n5 = this.f(fd2, n2 + 1, n3, n4, n5);
        n5 = this.f(fd2, n2 - 1, n3, n4, n5);
        n5 = this.f(fd2, n2, n3 - 1, n4, n5);
        n5 = this.f(fd2, n2, n3 + 1, n4, n5);
        n5 = this.f(fd2, n2, n3, n4 - 1, n5);
        n5 = this.f(fd2, n2, n3, n4 + 1, n5);
        return n5;
    }

    public boolean v_() {
        return false;
    }

    public boolean c(xp xp2, int n2, int n3, int n4) {
        return this.a[xp2.a(n2, n3, n4)] > 0;
    }

    public int f(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = this.a[fd2.a(n2, n3, n4)];
        if (n6 > n5) {
            return n6;
        }
        return n5;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return fd2.h(n2, n3 - 1, n4) || this.h(fd2, n2, n3, n4);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (!fd2.h(n2, n3 - 1, n4) && !this.h(fd2, n2, n3, n4)) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        if (fd2.a(n2, n3 - 1, n4) == uu.aq.bn && uu.bf.a_(fd2, n2, n3, n4)) {
            return;
        }
        if (!fd2.h(n2, n3 - 1, n4) && !this.h(fd2, n2, n3, n4)) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
        fd2.c(n2, n3, n4, this.bn, this.e());
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        block12: {
            float f2;
            float f3;
            float f4;
            int n5;
            block11: {
                if (random.nextInt(24) == 0) {
                    fd2.a((float)n2 + 0.5f, (double)((float)n3 + 0.5f), (double)((float)n4 + 0.5f), "fire.fire", 1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f);
                }
                if (!fd2.h(n2, n3 - 1, n4) && !uu.as.c((xp)fd2, n2, n3 - 1, n4)) break block11;
                for (int i2 = 0; i2 < 3; ++i2) {
                    float f5 = (float)n2 + random.nextFloat();
                    float f6 = (float)n3 + random.nextFloat() * 0.5f + 0.5f;
                    float f7 = (float)n4 + random.nextFloat();
                    fd2.a("largesmoke", f5, f6, f7, 0.0, 0.0, 0.0);
                }
                break block12;
            }
            if (uu.as.c((xp)fd2, n2 - 1, n3, n4)) {
                for (n5 = 0; n5 < 2; ++n5) {
                    f4 = (float)n2 + random.nextFloat() * 0.1f;
                    f3 = (float)n3 + random.nextFloat();
                    f2 = (float)n4 + random.nextFloat();
                    fd2.a("largesmoke", f4, f3, f2, 0.0, 0.0, 0.0);
                }
            }
            if (uu.as.c((xp)fd2, n2 + 1, n3, n4)) {
                for (n5 = 0; n5 < 2; ++n5) {
                    f4 = (float)(n2 + 1) - random.nextFloat() * 0.1f;
                    f3 = (float)n3 + random.nextFloat();
                    f2 = (float)n4 + random.nextFloat();
                    fd2.a("largesmoke", f4, f3, f2, 0.0, 0.0, 0.0);
                }
            }
            if (uu.as.c((xp)fd2, n2, n3, n4 - 1)) {
                for (n5 = 0; n5 < 2; ++n5) {
                    f4 = (float)n2 + random.nextFloat();
                    f3 = (float)n3 + random.nextFloat();
                    f2 = (float)n4 + random.nextFloat() * 0.1f;
                    fd2.a("largesmoke", f4, f3, f2, 0.0, 0.0, 0.0);
                }
            }
            if (uu.as.c((xp)fd2, n2, n3, n4 + 1)) {
                for (n5 = 0; n5 < 2; ++n5) {
                    f4 = (float)n2 + random.nextFloat();
                    f3 = (float)n3 + random.nextFloat();
                    f2 = (float)(n4 + 1) - random.nextFloat() * 0.1f;
                    fd2.a("largesmoke", f4, f3, f2, 0.0, 0.0, 0.0);
                }
            }
            if (!uu.as.c((xp)fd2, n2, n3 + 1, n4)) break block12;
            for (n5 = 0; n5 < 2; ++n5) {
                f4 = (float)n2 + random.nextFloat();
                f3 = (float)(n3 + 1) - random.nextFloat() * 0.1f;
                f2 = (float)n4 + random.nextFloat();
                fd2.a("largesmoke", f4, f3, f2, 0.0, 0.0, 0.0);
            }
        }
    }
}

