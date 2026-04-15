/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class sm
extends uu {
    private boolean a = true;
    private Set b = new HashSet();

    public sm(int n2, int n3) {
        super(n2, n3, ln.o);
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.0625f, 1.0f);
    }

    public int a(int n2, int n3) {
        return this.bm;
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
        return 5;
    }

    public int b(xp xp2, int n2, int n3, int n4) {
        return 0x800000;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return fd2.h(n2, n3 - 1, n4);
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        this.a(fd2, n2, n3, n4, n2, n3, n4);
        ArrayList arrayList = new ArrayList(this.b);
        this.b.clear();
        for (int i2 = 0; i2 < arrayList.size(); ++i2) {
            wf wf2 = (wf)arrayList.get(i2);
            fd2.i(wf2.a, wf2.b, wf2.c, this.bn);
        }
    }

    private void a(fd fd2, int n2, int n3, int n4, int n5, int n6, int n7) {
        int n8;
        int n9;
        int n10;
        int n11 = fd2.e(n2, n3, n4);
        int n12 = 0;
        this.a = false;
        boolean bl2 = fd2.s(n2, n3, n4);
        this.a = true;
        if (bl2) {
            n12 = 15;
        } else {
            for (n10 = 0; n10 < 4; ++n10) {
                n9 = n2;
                n8 = n4;
                if (n10 == 0) {
                    --n9;
                }
                if (n10 == 1) {
                    ++n9;
                }
                if (n10 == 2) {
                    --n8;
                }
                if (n10 == 3) {
                    ++n8;
                }
                if (n9 != n5 || n3 != n6 || n8 != n7) {
                    n12 = this.f(fd2, n9, n3, n8, n12);
                }
                if (fd2.h(n9, n3, n8) && !fd2.h(n2, n3 + 1, n4)) {
                    if (n9 == n5 && n3 + 1 == n6 && n8 == n7) continue;
                    n12 = this.f(fd2, n9, n3 + 1, n8, n12);
                    continue;
                }
                if (fd2.h(n9, n3, n8) || n9 == n5 && n3 - 1 == n6 && n8 == n7) continue;
                n12 = this.f(fd2, n9, n3 - 1, n8, n12);
            }
            n12 = n12 > 0 ? --n12 : 0;
        }
        if (n11 != n12) {
            fd2.o = true;
            fd2.d(n2, n3, n4, n12);
            fd2.b(n2, n3, n4, n2, n3, n4);
            fd2.o = false;
            for (n10 = 0; n10 < 4; ++n10) {
                n9 = n2;
                n8 = n4;
                int n13 = n3 - 1;
                if (n10 == 0) {
                    --n9;
                }
                if (n10 == 1) {
                    ++n9;
                }
                if (n10 == 2) {
                    --n8;
                }
                if (n10 == 3) {
                    ++n8;
                }
                if (fd2.h(n9, n3, n8)) {
                    n13 += 2;
                }
                int n14 = 0;
                n14 = this.f(fd2, n9, n3, n8, -1);
                n12 = fd2.e(n2, n3, n4);
                if (n12 > 0) {
                    --n12;
                }
                if (n14 >= 0 && n14 != n12) {
                    this.a(fd2, n9, n3, n8, n2, n3, n4);
                }
                n14 = this.f(fd2, n9, n13, n8, -1);
                n12 = fd2.e(n2, n3, n4);
                if (n12 > 0) {
                    --n12;
                }
                if (n14 < 0 || n14 == n12) continue;
                this.a(fd2, n9, n13, n8, n2, n3, n4);
            }
            if (n11 == 0 || n12 == 0) {
                this.b.add(new wf(n2, n3, n4));
                this.b.add(new wf(n2 - 1, n3, n4));
                this.b.add(new wf(n2 + 1, n3, n4));
                this.b.add(new wf(n2, n3 - 1, n4));
                this.b.add(new wf(n2, n3 + 1, n4));
                this.b.add(new wf(n2, n3, n4 - 1));
                this.b.add(new wf(n2, n3, n4 + 1));
            }
        }
    }

    private void i(fd fd2, int n2, int n3, int n4) {
        if (fd2.a(n2, n3, n4) != this.bn) {
            return;
        }
        fd2.i(n2, n3, n4, this.bn);
        fd2.i(n2 - 1, n3, n4, this.bn);
        fd2.i(n2 + 1, n3, n4, this.bn);
        fd2.i(n2, n3, n4 - 1, this.bn);
        fd2.i(n2, n3, n4 + 1, this.bn);
        fd2.i(n2, n3 - 1, n4, this.bn);
        fd2.i(n2, n3 + 1, n4, this.bn);
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        super.c(fd2, n2, n3, n4);
        if (fd2.B) {
            return;
        }
        this.h(fd2, n2, n3, n4);
        fd2.i(n2, n3 + 1, n4, this.bn);
        fd2.i(n2, n3 - 1, n4, this.bn);
        this.i(fd2, n2 - 1, n3, n4);
        this.i(fd2, n2 + 1, n3, n4);
        this.i(fd2, n2, n3, n4 - 1);
        this.i(fd2, n2, n3, n4 + 1);
        if (fd2.h(n2 - 1, n3, n4)) {
            this.i(fd2, n2 - 1, n3 + 1, n4);
        } else {
            this.i(fd2, n2 - 1, n3 - 1, n4);
        }
        if (fd2.h(n2 + 1, n3, n4)) {
            this.i(fd2, n2 + 1, n3 + 1, n4);
        } else {
            this.i(fd2, n2 + 1, n3 - 1, n4);
        }
        if (fd2.h(n2, n3, n4 - 1)) {
            this.i(fd2, n2, n3 + 1, n4 - 1);
        } else {
            this.i(fd2, n2, n3 - 1, n4 - 1);
        }
        if (fd2.h(n2, n3, n4 + 1)) {
            this.i(fd2, n2, n3 + 1, n4 + 1);
        } else {
            this.i(fd2, n2, n3 - 1, n4 + 1);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        super.b(fd2, n2, n3, n4);
        if (fd2.B) {
            return;
        }
        fd2.i(n2, n3 + 1, n4, this.bn);
        fd2.i(n2, n3 - 1, n4, this.bn);
        this.h(fd2, n2, n3, n4);
        this.i(fd2, n2 - 1, n3, n4);
        this.i(fd2, n2 + 1, n3, n4);
        this.i(fd2, n2, n3, n4 - 1);
        this.i(fd2, n2, n3, n4 + 1);
        if (fd2.h(n2 - 1, n3, n4)) {
            this.i(fd2, n2 - 1, n3 + 1, n4);
        } else {
            this.i(fd2, n2 - 1, n3 - 1, n4);
        }
        if (fd2.h(n2 + 1, n3, n4)) {
            this.i(fd2, n2 + 1, n3 + 1, n4);
        } else {
            this.i(fd2, n2 + 1, n3 - 1, n4);
        }
        if (fd2.h(n2, n3, n4 - 1)) {
            this.i(fd2, n2, n3 + 1, n4 - 1);
        } else {
            this.i(fd2, n2, n3 - 1, n4 - 1);
        }
        if (fd2.h(n2, n3, n4 + 1)) {
            this.i(fd2, n2, n3 + 1, n4 + 1);
        } else {
            this.i(fd2, n2, n3 - 1, n4 + 1);
        }
    }

    private int f(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.a(n2, n3, n4) != this.bn) {
            return n5;
        }
        int n6 = fd2.e(n2, n3, n4);
        if (n6 > n5) {
            return n6;
        }
        return n5;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.B) {
            return;
        }
        int n6 = fd2.e(n2, n3, n4);
        boolean bl2 = this.a(fd2, n2, n3, n4);
        if (!bl2) {
            this.g(fd2, n2, n3, n4, n6);
            fd2.f(n2, n3, n4, 0);
        } else {
            this.h(fd2, n2, n3, n4);
        }
        super.b(fd2, n2, n3, n4, n5);
    }

    public int a(int n2, Random random) {
        return gm.aA.bf;
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        if (!this.a) {
            return false;
        }
        return this.c((xp)fd2, n2, n3, n4, n5);
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        boolean bl2;
        if (!this.a) {
            return false;
        }
        if (xp2.e(n2, n3, n4) == 0) {
            return false;
        }
        if (n5 == 1) {
            return true;
        }
        boolean bl3 = sm.e(xp2, n2 - 1, n3, n4, 1) || !xp2.h(n2 - 1, n3, n4) && sm.e(xp2, n2 - 1, n3 - 1, n4, -1);
        boolean bl4 = sm.e(xp2, n2 + 1, n3, n4, 3) || !xp2.h(n2 + 1, n3, n4) && sm.e(xp2, n2 + 1, n3 - 1, n4, -1);
        boolean bl5 = sm.e(xp2, n2, n3, n4 - 1, 2) || !xp2.h(n2, n3, n4 - 1) && sm.e(xp2, n2, n3 - 1, n4 - 1, -1);
        boolean bl6 = bl2 = sm.e(xp2, n2, n3, n4 + 1, 0) || !xp2.h(n2, n3, n4 + 1) && sm.e(xp2, n2, n3 - 1, n4 + 1, -1);
        if (!xp2.h(n2, n3 + 1, n4)) {
            if (xp2.h(n2 - 1, n3, n4) && sm.e(xp2, n2 - 1, n3 + 1, n4, -1)) {
                bl3 = true;
            }
            if (xp2.h(n2 + 1, n3, n4) && sm.e(xp2, n2 + 1, n3 + 1, n4, -1)) {
                bl4 = true;
            }
            if (xp2.h(n2, n3, n4 - 1) && sm.e(xp2, n2, n3 + 1, n4 - 1, -1)) {
                bl5 = true;
            }
            if (xp2.h(n2, n3, n4 + 1) && sm.e(xp2, n2, n3 + 1, n4 + 1, -1)) {
                bl2 = true;
            }
        }
        if (!(bl5 || bl4 || bl3 || bl2 || n5 < 2 || n5 > 5)) {
            return true;
        }
        if (n5 == 2 && bl5 && !bl3 && !bl4) {
            return true;
        }
        if (n5 == 3 && bl2 && !bl3 && !bl4) {
            return true;
        }
        if (n5 == 4 && bl3 && !bl5 && !bl2) {
            return true;
        }
        return n5 == 5 && bl4 && !bl5 && !bl2;
    }

    public boolean f() {
        return this.a;
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        int n5 = fd2.e(n2, n3, n4);
        if (n5 > 0) {
            double d2 = (double)n2 + 0.5 + ((double)random.nextFloat() - 0.5) * 0.2;
            double d3 = (float)n3 + 0.0625f;
            double d4 = (double)n4 + 0.5 + ((double)random.nextFloat() - 0.5) * 0.2;
            float f2 = (float)n5 / 15.0f;
            float f3 = f2 * 0.6f + 0.4f;
            if (n5 == 0) {
                f3 = 0.0f;
            }
            float f4 = f2 * f2 * 0.7f - 0.5f;
            float f5 = f2 * f2 * 0.6f - 0.7f;
            if (f4 < 0.0f) {
                f4 = 0.0f;
            }
            if (f5 < 0.0f) {
                f5 = 0.0f;
            }
            fd2.a("reddust", d2, d3, d4, f3, f4, f5);
        }
    }

    public static boolean e(xp xp2, int n2, int n3, int n4, int n5) {
        int n6 = xp2.a(n2, n3, n4);
        if (n6 == uu.aw.bn) {
            return true;
        }
        if (n6 == 0) {
            return false;
        }
        if (uu.m[n6].f()) {
            return true;
        }
        if (n6 == uu.bi.bn || n6 == uu.bj.bn) {
            int n7 = xp2.e(n2, n3, n4);
            return n5 == jj.b[n7 & 3];
        }
        return false;
    }
}

