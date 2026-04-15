/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class or
extends uu {
    protected or(int n2, int n3) {
        super(n2, n3, ln.v);
        this.b(true);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.d(n2, n3 + 1, n4)) {
            int n5 = 1;
            while (fd2.a(n2, n3 - n5, n4) == this.bn) {
                ++n5;
            }
            if (n5 < 3) {
                int n6 = fd2.e(n2, n3, n4);
                if (n6 == 15) {
                    fd2.f(n2, n3 + 1, n4, this.bn);
                    fd2.d(n2, n3, n4, 0);
                } else {
                    fd2.d(n2, n3, n4, n6 + 1);
                }
            }
        }
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        float f2 = 0.0625f;
        return eq.b((float)n2 + f2, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (float)(n3 + 1) - f2, (float)(n4 + 1) - f2);
    }

    public eq f(fd fd2, int n2, int n3, int n4) {
        float f2 = 0.0625f;
        return eq.b((float)n2 + f2, n3, (float)n4 + f2, (float)(n2 + 1) - f2, n3 + 1, (float)(n4 + 1) - f2);
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm - 1;
        }
        if (n2 == 0) {
            return this.bm + 1;
        }
        return this.bm;
    }

    public boolean d() {
        return false;
    }

    public boolean c() {
        return false;
    }

    public int b() {
        return 13;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        if (!super.a(fd2, n2, n3, n4)) {
            return false;
        }
        return this.g(fd2, n2, n3, n4);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (!this.g(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
        }
    }

    public boolean g(fd fd2, int n2, int n3, int n4) {
        if (fd2.f(n2 - 1, n3, n4).a()) {
            return false;
        }
        if (fd2.f(n2 + 1, n3, n4).a()) {
            return false;
        }
        if (fd2.f(n2, n3, n4 - 1).a()) {
            return false;
        }
        if (fd2.f(n2, n3, n4 + 1).a()) {
            return false;
        }
        int n5 = fd2.a(n2, n3 - 1, n4);
        return n5 == uu.aW.bn || n5 == uu.F.bn;
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2) {
        sn2.a((sn)null, 1);
    }
}

