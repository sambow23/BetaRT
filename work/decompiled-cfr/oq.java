/*
 * Decompiled with CFR 0.152.
 */
public class oq
extends uu {
    protected oq(int n2, ln ln2) {
        super(n2, ln2);
        this.bm = 84;
        if (ln2 == ln.f) {
            ++this.bm;
        }
        float f2 = 0.5f;
        float f3 = 1.0f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, f3, 0.5f + f2);
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 0;
    }

    public eq f(fd fd2, int n2, int n3, int n4) {
        this.a((xp)fd2, n2, n3, n4);
        return super.f(fd2, n2, n3, n4);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        this.a((xp)fd2, n2, n3, n4);
        return super.e(fd2, n2, n3, n4);
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        this.d(xp2.e(n2, n3, n4));
    }

    public void g() {
        float f2 = 0.1875f;
        this.a(0.0f, 0.5f - f2 / 2.0f, 0.0f, 1.0f, 0.5f + f2 / 2.0f, 1.0f);
    }

    public void d(int n2) {
        float f2 = 0.1875f;
        this.a(0.0f, 0.0f, 0.0f, 1.0f, f2, 1.0f);
        if (oq.e(n2)) {
            if ((n2 & 3) == 0) {
                this.a(0.0f, 0.0f, 1.0f - f2, 1.0f, 1.0f, 1.0f);
            }
            if ((n2 & 3) == 1) {
                this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, f2);
            }
            if ((n2 & 3) == 2) {
                this.a(1.0f - f2, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            }
            if ((n2 & 3) == 3) {
                this.a(0.0f, 0.0f, 0.0f, f2, 1.0f, 1.0f);
            }
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.a(fd2, n2, n3, n4, gs2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (this.bA == ln.f) {
            return true;
        }
        int n5 = fd2.e(n2, n3, n4);
        fd2.d(n2, n3, n4, n5 ^ 4);
        fd2.a(gs2, 1003, n2, n3, n4, 0);
        return true;
    }

    public void a(fd fd2, int n2, int n3, int n4, boolean bl2) {
        boolean bl3;
        int n5 = fd2.e(n2, n3, n4);
        boolean bl4 = bl3 = (n5 & 4) > 0;
        if (bl3 == bl2) {
            return;
        }
        fd2.d(n2, n3, n4, n5 ^ 4);
        fd2.a(null, 1003, n2, n3, n4, 0);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.B) {
            return;
        }
        int n6 = fd2.e(n2, n3, n4);
        int n7 = n2;
        int n8 = n4;
        if ((n6 & 3) == 0) {
            ++n8;
        }
        if ((n6 & 3) == 1) {
            --n8;
        }
        if ((n6 & 3) == 2) {
            ++n7;
        }
        if ((n6 & 3) == 3) {
            --n7;
        }
        if (!fd2.h(n7, n3, n8)) {
            fd2.f(n2, n3, n4, 0);
            this.g(fd2, n2, n3, n4, n6);
        }
        if (n5 > 0 && uu.m[n5].f()) {
            boolean bl2 = fd2.s(n2, n3, n4);
            this.a(fd2, n2, n3, n4, bl2);
        }
    }

    public vf a(fd fd2, int n2, int n3, int n4, bt bt2, bt bt3) {
        this.a((xp)fd2, n2, n3, n4);
        return super.a(fd2, n2, n3, n4, bt2, bt3);
    }

    public void e(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = 0;
        if (n5 == 2) {
            n6 = 0;
        }
        if (n5 == 3) {
            n6 = 1;
        }
        if (n5 == 4) {
            n6 = 2;
        }
        if (n5 == 5) {
            n6 = 3;
        }
        fd2.d(n2, n3, n4, n6);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 == 0) {
            return false;
        }
        if (n5 == 1) {
            return false;
        }
        if (n5 == 2) {
            ++n4;
        }
        if (n5 == 3) {
            --n4;
        }
        if (n5 == 4) {
            ++n2;
        }
        if (n5 == 5) {
            --n2;
        }
        return fd2.h(n2, n3, n4);
    }

    public static boolean e(int n2) {
        return (n2 & 4) != 0;
    }
}

