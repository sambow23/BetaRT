/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ri
extends uu {
    protected ri(int n2, int n3) {
        super(n2, ln.j);
        this.bm = n3;
        float f2 = 0.375f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, 1.0f, 0.5f + f2);
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

    public boolean a(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3 - 1, n4);
        if (n5 == this.bn) {
            return true;
        }
        if (n5 != uu.v.bn && n5 != uu.w.bn) {
            return false;
        }
        if (fd2.f(n2 - 1, n3 - 1, n4) == ln.g) {
            return true;
        }
        if (fd2.f(n2 + 1, n3 - 1, n4) == ln.g) {
            return true;
        }
        if (fd2.f(n2, n3 - 1, n4 - 1) == ln.g) {
            return true;
        }
        return fd2.f(n2, n3 - 1, n4 + 1) == ln.g;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        this.h(fd2, n2, n3, n4);
    }

    protected final void h(fd fd2, int n2, int n3, int n4) {
        if (!this.g(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
        }
    }

    public boolean g(fd fd2, int n2, int n3, int n4) {
        return this.a(fd2, n2, n3, n4);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public int a(int n2, Random random) {
        return gm.aH.bf;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 1;
    }
}

