/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class oi
extends uu {
    protected oi(int n2, int n3) {
        super(n2, n3, ln.o);
        this.b(true);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public int e() {
        return 20;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, int n5) {
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
        return fd2.h(n2, n3, n4 + 1);
    }

    public void e(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        int n7 = n6 & 8;
        n6 &= 7;
        n6 = n5 == 2 && fd2.h(n2, n3, n4 + 1) ? 4 : (n5 == 3 && fd2.h(n2, n3, n4 - 1) ? 3 : (n5 == 4 && fd2.h(n2 + 1, n3, n4) ? 2 : (n5 == 5 && fd2.h(n2 - 1, n3, n4) ? 1 : this.h(fd2, n2, n3, n4))));
        fd2.d(n2, n3, n4, n6 + n7);
    }

    private int h(fd fd2, int n2, int n3, int n4) {
        if (fd2.h(n2 - 1, n3, n4)) {
            return 1;
        }
        if (fd2.h(n2 + 1, n3, n4)) {
            return 2;
        }
        if (fd2.h(n2, n3, n4 - 1)) {
            return 3;
        }
        if (fd2.h(n2, n3, n4 + 1)) {
            return 4;
        }
        return 1;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (this.i(fd2, n2, n3, n4)) {
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
            if (bl2) {
                this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
                fd2.f(n2, n3, n4, 0);
            }
        }
    }

    private boolean i(fd fd2, int n2, int n3, int n4) {
        if (!this.a(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
            return false;
        }
        return true;
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        int n6 = n5 & 7;
        boolean bl2 = (n5 & 8) > 0;
        float f2 = 0.375f;
        float f3 = 0.625f;
        float f4 = 0.1875f;
        float f5 = 0.125f;
        if (bl2) {
            f5 = 0.0625f;
        }
        if (n6 == 1) {
            this.a(0.0f, f2, 0.5f - f4, f5, f3, 0.5f + f4);
        } else if (n6 == 2) {
            this.a(1.0f - f5, f2, 0.5f - f4, 1.0f, f3, 0.5f + f4);
        } else if (n6 == 3) {
            this.a(0.5f - f4, f2, 0.0f, 0.5f + f4, f3, f5);
        } else if (n6 == 4) {
            this.a(0.5f - f4, f2, 1.0f - f5, 0.5f + f4, f3, 1.0f);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.a(fd2, n2, n3, n4, gs2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        int n5 = fd2.e(n2, n3, n4);
        int n6 = n5 & 7;
        int n7 = 8 - (n5 & 8);
        if (n7 == 0) {
            return true;
        }
        fd2.d(n2, n3, n4, n6 + n7);
        fd2.b(n2, n3, n4, n2, n3, n4);
        fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "random.click", 0.3f, 0.6f);
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
        fd2.c(n2, n3, n4, this.bn, this.e());
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

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.B) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        if ((n5 & 8) == 0) {
            return;
        }
        fd2.d(n2, n3, n4, n5 & 7);
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
        fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "random.click", 0.3f, 0.5f);
        fd2.b(n2, n3, n4, n2, n3, n4);
    }

    public void g() {
        float f2 = 0.1875f;
        float f3 = 0.125f;
        float f4 = 0.125f;
        this.a(0.5f - f2, 0.5f - f3, 0.5f - f4, 0.5f + f2, 0.5f + f3, 0.5f + f4);
    }
}

