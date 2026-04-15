/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class jr
extends uu {
    protected jr(int n2, int n3) {
        super(n2, n3, ln.t);
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.125f, 1.0f);
        this.b(true);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4) & 7;
        if (n5 >= 3) {
            return eq.b((double)n2 + this.bs, (double)n3 + this.bt, (double)n4 + this.bu, (double)n2 + this.bv, (float)n3 + 0.5f, (double)n4 + this.bx);
        }
        return null;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4) & 7;
        float f2 = (float)(2 * (1 + n5)) / 16.0f;
        this.a(0.0f, 0.0f, 0.0f, 1.0f, f2, 1.0f);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3 - 1, n4);
        if (n5 == 0 || !uu.m[n5].c()) {
            return false;
        }
        return fd2.f(n2, n3 - 1, n4).c();
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        this.h(fd2, n2, n3, n4);
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        if (!this.a(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
            return false;
        }
        return true;
    }

    public void a(fd fd2, gs gs2, int n2, int n3, int n4, int n5) {
        int n6 = gm.aB.bf;
        float f2 = 0.7f;
        double d2 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        double d3 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        double d4 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        hl hl2 = new hl(fd2, (double)n2 + d2, (double)n3 + d3, (double)n4 + d4, new iz(n6, 1, 0));
        hl2.c = 10;
        fd2.b(hl2);
        fd2.f(n2, n3, n4, 0);
        gs2.a(jl.C[this.bn], 1);
    }

    public int a(int n2, Random random) {
        return gm.aB.bf;
    }

    public int a(Random random) {
        return 0;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.a(eb.b, n2, n3, n4) > 11) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
        }
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        if (n5 == 1) {
            return true;
        }
        return super.b(xp2, n2, n3, n4, n5);
    }
}

