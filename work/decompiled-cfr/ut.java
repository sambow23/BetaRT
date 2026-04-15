/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ut
extends rw {
    public ut(int n2) {
        super(n2, ln.B);
        this.c(-1.0f);
    }

    protected ow a_() {
        return null;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        ow ow2 = fd2.b(n2, n3, n4);
        if (ow2 != null && ow2 instanceof uk) {
            ((uk)ow2).l();
        } else {
            super.b(fd2, n2, n3, n4);
        }
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, int n5) {
        return false;
    }

    public int b() {
        return -1;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (!fd2.B && fd2.b(n2, n3, n4) == null) {
            fd2.f(n2, n3, n4, 0);
            return true;
        }
        return false;
    }

    public int a(int n2, Random random) {
        return 0;
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, float f2) {
        if (fd2.B) {
            return;
        }
        uk uk2 = this.c((xp)fd2, n2, n3, n4);
        if (uk2 == null) {
            return;
        }
        uu.m[uk2.a()].g(fd2, n2, n3, n4, uk2.e());
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.B || fd2.b(n2, n3, n4) == null) {
            // empty if block
        }
    }

    public static ow a(int n2, int n3, int n4, boolean bl2, boolean bl3) {
        return new uk(n2, n3, n4, bl2, bl3);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        uk uk2 = this.c((xp)fd2, n2, n3, n4);
        if (uk2 == null) {
            return null;
        }
        float f2 = uk2.a(0.0f);
        if (uk2.b()) {
            f2 = 1.0f - f2;
        }
        return this.a(fd2, n2, n3, n4, uk2.a(), f2, uk2.d());
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        uk uk2 = this.c(xp2, n2, n3, n4);
        if (uk2 != null) {
            uu uu2 = uu.m[uk2.a()];
            if (uu2 == null || uu2 == this) {
                return;
            }
            uu2.a(xp2, n2, n3, n4);
            float f2 = uk2.a(0.0f);
            if (uk2.b()) {
                f2 = 1.0f - f2;
            }
            int n5 = uk2.d();
            this.bs = uu2.bs - (double)((float)wj.b[n5] * f2);
            this.bt = uu2.bt - (double)((float)wj.c[n5] * f2);
            this.bu = uu2.bu - (double)((float)wj.d[n5] * f2);
            this.bv = uu2.bv - (double)((float)wj.b[n5] * f2);
            this.bw = uu2.bw - (double)((float)wj.c[n5] * f2);
            this.bx = uu2.bx - (double)((float)wj.d[n5] * f2);
        }
    }

    public eq a(fd fd2, int n2, int n3, int n4, int n5, float f2, int n6) {
        if (n5 == 0 || n5 == this.bn) {
            return null;
        }
        eq eq2 = uu.m[n5].e(fd2, n2, n3, n4);
        if (eq2 == null) {
            return null;
        }
        eq2.a -= (double)((float)wj.b[n6] * f2);
        eq2.d -= (double)((float)wj.b[n6] * f2);
        eq2.b -= (double)((float)wj.c[n6] * f2);
        eq2.e -= (double)((float)wj.c[n6] * f2);
        eq2.c -= (double)((float)wj.d[n6] * f2);
        eq2.f -= (double)((float)wj.d[n6] * f2);
        return eq2;
    }

    private uk c(xp xp2, int n2, int n3, int n4) {
        ow ow2 = xp2.b(n2, n3, n4);
        if (ow2 != null && ow2 instanceof uk) {
            return (uk)ow2;
        }
        return null;
    }
}

