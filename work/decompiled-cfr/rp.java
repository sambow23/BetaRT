/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public abstract class rp
extends uu {
    protected rp(int n2, ln ln2) {
        super(n2, (ln2 == ln.h ? 14 : 12) * 16 + 13, ln2);
        float f2 = 0.0f;
        float f3 = 0.0f;
        this.a(0.0f + f3, 0.0f + f2, 0.0f + f3, 1.0f + f3, 1.0f + f2, 1.0f + f3);
        this.b(true);
    }

    public int b(xp xp2, int n2, int n3, int n4) {
        return 0xFFFFFF;
    }

    public static float d(int n2) {
        if (n2 >= 8) {
            n2 = 0;
        }
        float f2 = (float)(n2 + 1) / 9.0f;
        return f2;
    }

    public int a(int n2) {
        if (n2 == 0 || n2 == 1) {
            return this.bm;
        }
        return this.bm + 1;
    }

    protected int h(fd fd2, int n2, int n3, int n4) {
        if (fd2.f(n2, n3, n4) != this.bA) {
            return -1;
        }
        return fd2.e(n2, n3, n4);
    }

    protected int c(xp xp2, int n2, int n3, int n4) {
        if (xp2.f(n2, n3, n4) != this.bA) {
            return -1;
        }
        int n5 = xp2.e(n2, n3, n4);
        if (n5 >= 8) {
            n5 = 0;
        }
        return n5;
    }

    public boolean d() {
        return false;
    }

    public boolean c() {
        return false;
    }

    public boolean a(int n2, boolean bl2) {
        return bl2 && n2 == 0;
    }

    public boolean d(xp xp2, int n2, int n3, int n4, int n5) {
        ln ln2 = xp2.f(n2, n3, n4);
        if (ln2 == this.bA) {
            return false;
        }
        if (ln2 == ln.s) {
            return false;
        }
        if (n5 == 1) {
            return true;
        }
        return super.d(xp2, n2, n3, n4, n5);
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        ln ln2 = xp2.f(n2, n3, n4);
        if (ln2 == this.bA) {
            return false;
        }
        if (ln2 == ln.s) {
            return false;
        }
        if (n5 == 1) {
            return true;
        }
        return super.b(xp2, n2, n3, n4, n5);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public int b() {
        return 4;
    }

    public int a(int n2, Random random) {
        return 0;
    }

    public int a(Random random) {
        return 0;
    }

    private bt e(xp xp2, int n2, int n3, int n4) {
        int n5;
        bt bt2 = bt.b(0.0, 0.0, 0.0);
        int n6 = this.c(xp2, n2, n3, n4);
        for (n5 = 0; n5 < 4; ++n5) {
            int n7;
            int n8;
            int n9 = n2;
            int n10 = n3;
            int n11 = n4;
            if (n5 == 0) {
                --n9;
            }
            if (n5 == 1) {
                --n11;
            }
            if (n5 == 2) {
                ++n9;
            }
            if (n5 == 3) {
                ++n11;
            }
            if ((n8 = this.c(xp2, n9, n10, n11)) < 0) {
                if (xp2.f(n9, n10, n11).c() || (n8 = this.c(xp2, n9, n10 - 1, n11)) < 0) continue;
                n7 = n8 - (n6 - 8);
                bt2 = bt2.c((n9 - n2) * n7, (n10 - n3) * n7, (n11 - n4) * n7);
                continue;
            }
            if (n8 < 0) continue;
            n7 = n8 - n6;
            bt2 = bt2.c((n9 - n2) * n7, (n10 - n3) * n7, (n11 - n4) * n7);
        }
        if (xp2.e(n2, n3, n4) >= 8) {
            n5 = 0;
            if (n5 != 0 || this.d(xp2, n2, n3, n4 - 1, 2)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2, n3, n4 + 1, 3)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2 - 1, n3, n4, 4)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2 + 1, n3, n4, 5)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2, n3 + 1, n4 - 1, 2)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2, n3 + 1, n4 + 1, 3)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2 - 1, n3 + 1, n4, 4)) {
                n5 = 1;
            }
            if (n5 != 0 || this.d(xp2, n2 + 1, n3 + 1, n4, 5)) {
                n5 = 1;
            }
            if (n5 != 0) {
                bt2 = bt2.c().c(0.0, -6.0, 0.0);
            }
        }
        bt2 = bt2.c();
        return bt2;
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2, bt bt2) {
        bt bt3 = this.e((xp)fd2, n2, n3, n4);
        bt2.a += bt3.a;
        bt2.b += bt3.b;
        bt2.c += bt3.c;
    }

    public int e() {
        if (this.bA == ln.g) {
            return 5;
        }
        if (this.bA == ln.h) {
            return 30;
        }
        return 0;
    }

    public float d(xp xp2, int n2, int n3, int n4) {
        float f2;
        float f3 = xp2.c(n2, n3, n4);
        return f3 > (f2 = xp2.c(n2, n3 + 1, n4)) ? f3 : f2;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        super.a(fd2, n2, n3, n4, random);
    }

    public int b_() {
        return this.bA == ln.g ? 1 : 0;
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        int n5;
        if (this.bA == ln.g && random.nextInt(64) == 0 && (n5 = fd2.e(n2, n3, n4)) > 0 && n5 < 8) {
            fd2.a((float)n2 + 0.5f, (double)((float)n3 + 0.5f), (double)((float)n4 + 0.5f), "liquid.water", random.nextFloat() * 0.25f + 0.75f, random.nextFloat() * 1.0f + 0.5f);
        }
        if (this.bA == ln.h && fd2.f(n2, n3 + 1, n4) == ln.a && !fd2.g(n2, n3 + 1, n4) && random.nextInt(100) == 0) {
            double d2 = (float)n2 + random.nextFloat();
            double d3 = (double)n3 + this.bw;
            double d4 = (float)n4 + random.nextFloat();
            fd2.a("lava", d2, d3, d4, 0.0, 0.0, 0.0);
        }
    }

    public static double a(xp xp2, int n2, int n3, int n4, ln ln2) {
        bt bt2 = null;
        if (ln2 == ln.g) {
            bt2 = ((rp)uu.B).e(xp2, n2, n3, n4);
        }
        if (ln2 == ln.h) {
            bt2 = ((rp)uu.D).e(xp2, n2, n3, n4);
        }
        if (bt2.a == 0.0 && bt2.c == 0.0) {
            return -1000.0;
        }
        return Math.atan2(bt2.c, bt2.a) - 1.5707963267948966;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        this.j(fd2, n2, n3, n4);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        this.j(fd2, n2, n3, n4);
    }

    private void j(fd fd2, int n2, int n3, int n4) {
        if (fd2.a(n2, n3, n4) != this.bn) {
            return;
        }
        if (this.bA == ln.h) {
            boolean bl2 = false;
            if (bl2 || fd2.f(n2, n3, n4 - 1) == ln.g) {
                bl2 = true;
            }
            if (bl2 || fd2.f(n2, n3, n4 + 1) == ln.g) {
                bl2 = true;
            }
            if (bl2 || fd2.f(n2 - 1, n3, n4) == ln.g) {
                bl2 = true;
            }
            if (bl2 || fd2.f(n2 + 1, n3, n4) == ln.g) {
                bl2 = true;
            }
            if (bl2 || fd2.f(n2, n3 + 1, n4) == ln.g) {
                bl2 = true;
            }
            if (bl2) {
                int n5 = fd2.e(n2, n3, n4);
                if (n5 == 0) {
                    fd2.f(n2, n3, n4, uu.aq.bn);
                } else if (n5 <= 4) {
                    fd2.f(n2, n3, n4, uu.x.bn);
                }
                this.i(fd2, n2, n3, n4);
            }
        }
    }

    protected void i(fd fd2, int n2, int n3, int n4) {
        fd2.a((float)n2 + 0.5f, (double)((float)n3 + 0.5f), (double)((float)n4 + 0.5f), "random.fizz", 0.5f, 2.6f + (fd2.r.nextFloat() - fd2.r.nextFloat()) * 0.8f);
        for (int i2 = 0; i2 < 8; ++i2) {
            fd2.a("largesmoke", (double)n2 + Math.random(), (double)n3 + 1.2, (double)n4 + Math.random(), 0.0, 0.0, 0.0);
        }
    }
}

