/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class le
extends uu {
    protected le(int n2, ln ln2) {
        super(n2, ln2);
        this.bm = 97;
        if (ln2 == ln.f) {
            ++this.bm;
        }
        float f2 = 0.5f;
        float f3 = 1.0f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, f3, 0.5f + f2);
    }

    public int a(int n2, int n3) {
        if (n2 == 0 || n2 == 1) {
            return this.bm;
        }
        int n4 = this.e(n3);
        if ((n4 == 0 || n4 == 2) ^ n2 <= 3) {
            return this.bm;
        }
        int n5 = n4 / 2 + (n2 & 1 ^ n4);
        int n6 = this.bm - (n3 & 8) * 2;
        if (((n5 += (n3 & 4) / 4) & 1) != 0) {
            n6 = -n6;
        }
        return n6;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 7;
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
        this.d(this.e(xp2.e(n2, n3, n4)));
    }

    public void d(int n2) {
        float f2 = 0.1875f;
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f);
        if (n2 == 0) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, f2);
        }
        if (n2 == 1) {
            this.a(1.0f - f2, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        if (n2 == 2) {
            this.a(0.0f, 0.0f, 1.0f - f2, 1.0f, 1.0f, 1.0f);
        }
        if (n2 == 3) {
            this.a(0.0f, 0.0f, 0.0f, f2, 1.0f, 1.0f);
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
        if ((n5 & 8) != 0) {
            if (fd2.a(n2, n3 - 1, n4) == this.bn) {
                this.a(fd2, n2, n3 - 1, n4, gs2);
            }
            return true;
        }
        if (fd2.a(n2, n3 + 1, n4) == this.bn) {
            fd2.d(n2, n3 + 1, n4, (n5 ^ 4) + 8);
        }
        fd2.d(n2, n3, n4, n5 ^ 4);
        fd2.b(n2, n3 - 1, n4, n2, n3, n4);
        fd2.a(gs2, 1003, n2, n3, n4, 0);
        return true;
    }

    public void a(fd fd2, int n2, int n3, int n4, boolean bl2) {
        boolean bl3;
        int n5 = fd2.e(n2, n3, n4);
        if ((n5 & 8) != 0) {
            if (fd2.a(n2, n3 - 1, n4) == this.bn) {
                this.a(fd2, n2, n3 - 1, n4, bl2);
            }
            return;
        }
        boolean bl4 = bl3 = (fd2.e(n2, n3, n4) & 4) > 0;
        if (bl3 == bl2) {
            return;
        }
        if (fd2.a(n2, n3 + 1, n4) == this.bn) {
            fd2.d(n2, n3 + 1, n4, (n5 ^ 4) + 8);
        }
        fd2.d(n2, n3, n4, n5 ^ 4);
        fd2.b(n2, n3 - 1, n4, n2, n3, n4);
        fd2.a(null, 1003, n2, n3, n4, 0);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        if ((n6 & 8) != 0) {
            if (fd2.a(n2, n3 - 1, n4) != this.bn) {
                fd2.f(n2, n3, n4, 0);
            }
            if (n5 > 0 && uu.m[n5].f()) {
                this.b(fd2, n2, n3 - 1, n4, n5);
            }
        } else {
            boolean bl2 = false;
            if (fd2.a(n2, n3 + 1, n4) != this.bn) {
                fd2.f(n2, n3, n4, 0);
                bl2 = true;
            }
            if (!fd2.h(n2, n3 - 1, n4)) {
                fd2.f(n2, n3, n4, 0);
                bl2 = true;
                if (fd2.a(n2, n3 + 1, n4) == this.bn) {
                    fd2.f(n2, n3 + 1, n4, 0);
                }
            }
            if (bl2) {
                if (!fd2.B) {
                    this.g(fd2, n2, n3, n4, n6);
                }
            } else if (n5 > 0 && uu.m[n5].f()) {
                boolean bl3 = fd2.s(n2, n3, n4) || fd2.s(n2, n3 + 1, n4);
                this.a(fd2, n2, n3, n4, bl3);
            }
        }
    }

    public int a(int n2, Random random) {
        if ((n2 & 8) != 0) {
            return 0;
        }
        if (this.bA == ln.f) {
            return gm.az.bf;
        }
        return gm.at.bf;
    }

    public vf a(fd fd2, int n2, int n3, int n4, bt bt2, bt bt3) {
        this.a((xp)fd2, n2, n3, n4);
        return super.a(fd2, n2, n3, n4, bt2, bt3);
    }

    public int e(int n2) {
        if ((n2 & 4) == 0) {
            return n2 - 1 & 3;
        }
        return n2 & 3;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        if (n3 >= 127) {
            return false;
        }
        return fd2.h(n2, n3 - 1, n4) && super.a(fd2, n2, n3, n4) && super.a(fd2, n2, n3 + 1, n4);
    }

    public static boolean f(int n2) {
        return (n2 & 4) != 0;
    }

    public int h() {
        return 1;
    }
}

