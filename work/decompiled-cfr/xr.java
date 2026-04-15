/*
 * Decompiled with CFR 0.152.
 */
public class xr
extends uu {
    protected xr(int n2, int n3) {
        super(n2, n3, ln.o);
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
        return 12;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 == 1 && fd2.h(n2, n3 - 1, n4)) {
            return true;
        }
        if (n5 == 2 && fd2.h(n2, n3, n4 + 1)) {
            return true;
        }
        if (n5 == 3 && fd2.h(n2, n3, n4 - 1)) {
            return true;
        }
        if (n5 == 4 && fd2.h(n2 + 1, n3, n4)) {
            return true;
        }
        return n5 == 5 && fd2.h(n2 - 1, n3, n4);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        if (fd2.h(n2 - 1, n3, n4)) {
            return true;
        }
        if (fd2.h(n2 + 1, n3, n4)) {
            return true;
        }
        if (fd2.h(n2, n3, n4 - 1)) {
            return true;
        }
        if (fd2.h(n2, n3, n4 + 1)) {
            return true;
        }
        return fd2.h(n2, n3 - 1, n4);
    }

    public void e(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        int n7 = n6 & 8;
        n6 &= 7;
        n6 = -1;
        if (n5 == 1 && fd2.h(n2, n3 - 1, n4)) {
            n6 = 5 + fd2.r.nextInt(2);
        }
        if (n5 == 2 && fd2.h(n2, n3, n4 + 1)) {
            n6 = 4;
        }
        if (n5 == 3 && fd2.h(n2, n3, n4 - 1)) {
            n6 = 3;
        }
        if (n5 == 4 && fd2.h(n2 + 1, n3, n4)) {
            n6 = 2;
        }
        if (n5 == 5 && fd2.h(n2 - 1, n3, n4)) {
            n6 = 1;
        }
        if (n6 == -1) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
            return;
        }
        fd2.d(n2, n3, n4, n6 + n7);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (this.h(fd2, n2, n3, n4)) {
            int n6 = fd2.e(n2, n3, n4) & 7;
            boolean bl2 = false;
            if (!fd2.h(n2 - 1, n3, n4) && n6 == 1) {
                bl2 = true;
            }
            if (!fd2.h(n2 + 1, n3, n4) && n6 == 2) {
                bl2 = true;
            }
            if (!fd2.h(n2, n3, n4 - 1) && n6 == 3) {
                bl2 = true;
            }
            if (!fd2.h(n2, n3, n4 + 1) && n6 == 4) {
                bl2 = true;
            }
            if (!fd2.h(n2, n3 - 1, n4) && n6 == 5) {
                bl2 = true;
            }
            if (!fd2.h(n2, n3 - 1, n4) && n6 == 6) {
                bl2 = true;
            }
            if (bl2) {
                this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
                fd2.f(n2, n3, n4, 0);
            }
        }
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        if (!this.a(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
            return false;
        }
        return true;
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4) & 7;
        float f2 = 0.1875f;
        if (n5 == 1) {
            this.a(0.0f, 0.2f, 0.5f - f2, f2 * 2.0f, 0.8f, 0.5f + f2);
        } else if (n5 == 2) {
            this.a(1.0f - f2 * 2.0f, 0.2f, 0.5f - f2, 1.0f, 0.8f, 0.5f + f2);
        } else if (n5 == 3) {
            this.a(0.5f - f2, 0.2f, 0.0f, 0.5f + f2, 0.8f, f2 * 2.0f);
        } else if (n5 == 4) {
            this.a(0.5f - f2, 0.2f, 1.0f - f2 * 2.0f, 0.5f + f2, 0.8f, 1.0f);
        } else {
            f2 = 0.25f;
            this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, 0.6f, 0.5f + f2);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.a(fd2, n2, n3, n4, gs2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.B) {
            return true;
        }
        int n5 = fd2.e(n2, n3, n4);
        int n6 = n5 & 7;
        int n7 = 8 - (n5 & 8);
        fd2.d(n2, n3, n4, n6 + n7);
        fd2.b(n2, n3, n4, n2, n3, n4);
        fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "random.click", 0.3f, n7 > 0 ? 0.6f : 0.5f);
        fd2.i(n2, n3, n4, this.bn);
        if (n6 == 1) {
            fd2.i(n2 - 1, n3, n4, this.bn);
        } else if (n6 == 2) {
            fd2.i(n2 + 1, n3, n4, this.bn);
        } else if (n6 == 3) {
            fd2.i(n2, n3, n4 - 1, this.bn);
        } else if (n6 == 4) {
            fd2.i(n2, n3, n4 + 1, this.bn);
        } else {
            fd2.i(n2, n3 - 1, n4, this.bn);
        }
        return true;
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        if ((n5 & 8) > 0) {
            fd2.i(n2, n3, n4, this.bn);
            int n6 = n5 & 7;
            if (n6 == 1) {
                fd2.i(n2 - 1, n3, n4, this.bn);
            } else if (n6 == 2) {
                fd2.i(n2 + 1, n3, n4, this.bn);
            } else if (n6 == 3) {
                fd2.i(n2, n3, n4 - 1, this.bn);
            } else if (n6 == 4) {
                fd2.i(n2, n3, n4 + 1, this.bn);
            } else {
                fd2.i(n2, n3 - 1, n4, this.bn);
            }
        }
        super.b(fd2, n2, n3, n4);
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        return (xp2.e(n2, n3, n4) & 8) > 0;
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        if ((n6 & 8) == 0) {
            return false;
        }
        int n7 = n6 & 7;
        if (n7 == 6 && n5 == 1) {
            return true;
        }
        if (n7 == 5 && n5 == 1) {
            return true;
        }
        if (n7 == 4 && n5 == 2) {
            return true;
        }
        if (n7 == 3 && n5 == 3) {
            return true;
        }
        if (n7 == 2 && n5 == 4) {
            return true;
        }
        return n7 == 1 && n5 == 5;
    }

    public boolean f() {
        return true;
    }
}

