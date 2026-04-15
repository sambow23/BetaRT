/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Random;

public class bv
extends uu {
    private rt a;

    protected bv(int n2, int n3, rt rt2, ln ln2) {
        super(n2, n3, ln2);
        this.a = rt2;
        this.b(true);
        float f2 = 0.0625f;
        this.a(f2, 0.0f, f2, 1.0f - f2, 0.03125f, 1.0f - f2);
    }

    public int e() {
        return 20;
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

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return fd2.h(n2, n3 - 1, n4);
    }

    public void c(fd fd2, int n2, int n3, int n4) {
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        boolean bl2 = false;
        if (!fd2.h(n2, n3 - 1, n4)) {
            bl2 = true;
        }
        if (bl2) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
        }
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.B) {
            return;
        }
        if (fd2.e(n2, n3, n4) == 0) {
            return;
        }
        this.h(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2) {
        if (fd2.B) {
            return;
        }
        if (fd2.e(n2, n3, n4) == 1) {
            return;
        }
        this.h(fd2, n2, n3, n4);
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        boolean bl2 = fd2.e(n2, n3, n4) == 1;
        boolean bl3 = false;
        float f2 = 0.125f;
        List list = null;
        if (this.a == rt.a) {
            list = fd2.b(null, eq.b((float)n2 + f2, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (double)n3 + 0.25, (float)(n4 + 1) - f2));
        }
        if (this.a == rt.b) {
            list = fd2.a(ls.class, eq.b((float)n2 + f2, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (double)n3 + 0.25, (float)(n4 + 1) - f2));
        }
        if (this.a == rt.c) {
            list = fd2.a(gs.class, eq.b((float)n2 + f2, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (double)n3 + 0.25, (float)(n4 + 1) - f2));
        }
        if (list.size() > 0) {
            bl3 = true;
        }
        if (bl3 && !bl2) {
            fd2.d(n2, n3, n4, 1);
            fd2.i(n2, n3, n4, this.bn);
            fd2.i(n2, n3 - 1, n4, this.bn);
            fd2.b(n2, n3, n4, n2, n3, n4);
            fd2.a((double)n2 + 0.5, (double)n3 + 0.1, (double)n4 + 0.5, "random.click", 0.3f, 0.6f);
        }
        if (!bl3 && bl2) {
            fd2.d(n2, n3, n4, 0);
            fd2.i(n2, n3, n4, this.bn);
            fd2.i(n2, n3 - 1, n4, this.bn);
            fd2.b(n2, n3, n4, n2, n3, n4);
            fd2.a((double)n2 + 0.5, (double)n3 + 0.1, (double)n4 + 0.5, "random.click", 0.3f, 0.5f);
        }
        if (bl3) {
            fd2.c(n2, n3, n4, this.bn, this.e());
        }
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        if (n5 > 0) {
            fd2.i(n2, n3, n4, this.bn);
            fd2.i(n2, n3 - 1, n4, this.bn);
        }
        super.b(fd2, n2, n3, n4);
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        boolean bl2 = xp2.e(n2, n3, n4) == 1;
        float f2 = 0.0625f;
        if (bl2) {
            this.a(f2, 0.0f, f2, 1.0f - f2, 0.03125f, 1.0f - f2);
        } else {
            this.a(f2, 0.0f, f2, 1.0f - f2, 0.0625f, 1.0f - f2);
        }
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        return xp2.e(n2, n3, n4) > 0;
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.e(n2, n3, n4) == 0) {
            return false;
        }
        return n5 == 1;
    }

    public boolean f() {
        return true;
    }

    public void g() {
        float f2 = 0.5f;
        float f3 = 0.125f;
        float f4 = 0.5f;
        this.a(0.5f - f2, 0.5f - f3, 0.5f - f4, 0.5f + f2, 0.5f + f3, 0.5f + f4);
    }

    public int h() {
        return 1;
    }
}

