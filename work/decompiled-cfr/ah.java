/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ah
extends uu {
    public ah(int n2, int n3) {
        super(n2, n3, ln.q);
    }

    public int a(int n2) {
        if (n2 == 0) {
            return this.bm + 2;
        }
        if (n2 == 1) {
            return this.bm + 1;
        }
        return this.bm;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        super.c(fd2, n2, n3, n4);
        if (fd2.s(n2, n3, n4)) {
            this.c(fd2, n2, n3, n4, 1);
            fd2.f(n2, n3, n4, 0);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 > 0 && uu.m[n5].f() && fd2.s(n2, n3, n4)) {
            this.c(fd2, n2, n3, n4, 1);
            fd2.f(n2, n3, n4, 0);
        }
    }

    public int a(Random random) {
        return 0;
    }

    public void d(fd fd2, int n2, int n3, int n4) {
        qw qw2 = new qw(fd2, (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f);
        qw2.a = fd2.r.nextInt(qw2.a / 4) + qw2.a / 8;
        fd2.b(qw2);
    }

    public void c(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.B) {
            return;
        }
        if ((n5 & 1) == 0) {
            this.a(fd2, n2, n3, n4, new iz(uu.an.bn, 1, 0));
        } else {
            qw qw2 = new qw(fd2, (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f);
            fd2.b(qw2);
            fd2.a(qw2, "random.fuse", 1.0f, 1.0f);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (gs2.G() != null && gs2.G().c == gm.g.bf) {
            fd2.e(n2, n3, n4, 1);
        }
        super.b(fd2, n2, n3, n4, gs2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        return super.a(fd2, n2, n3, n4, gs2);
    }
}

