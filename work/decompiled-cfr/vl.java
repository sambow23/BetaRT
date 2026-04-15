/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class vl
extends uu {
    protected vl(int n2) {
        super(n2, ln.c);
        this.bm = 87;
        this.b(true);
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.9375f, 1.0f);
        this.g(255);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return eq.b(n2 + 0, n3 + 0, n4 + 0, n2 + 1, n3 + 1, n4 + 1);
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int a(int n2, int n3) {
        if (n2 == 1 && n3 > 0) {
            return this.bm - 1;
        }
        if (n2 == 1) {
            return this.bm;
        }
        return 2;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (random.nextInt(5) == 0) {
            if (this.i(fd2, n2, n3, n4) || fd2.t(n2, n3 + 1, n4)) {
                fd2.d(n2, n3, n4, 7);
            } else {
                int n5 = fd2.e(n2, n3, n4);
                if (n5 > 0) {
                    fd2.d(n2, n3, n4, n5 - 1);
                } else if (!this.h(fd2, n2, n3, n4)) {
                    fd2.f(n2, n3, n4, uu.w.bn);
                }
            }
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, sn sn2) {
        if (fd2.r.nextInt(4) == 0) {
            fd2.f(n2, n3, n4, uu.w.bn);
        }
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        int n5 = 0;
        for (int i2 = n2 - n5; i2 <= n2 + n5; ++i2) {
            for (int i3 = n4 - n5; i3 <= n4 + n5; ++i3) {
                if (fd2.a(i2, n3 + 1, i3) != uu.aA.bn) continue;
                return true;
            }
        }
        return false;
    }

    private boolean i(fd fd2, int n2, int n3, int n4) {
        for (int i2 = n2 - 4; i2 <= n2 + 4; ++i2) {
            for (int i3 = n3; i3 <= n3 + 1; ++i3) {
                for (int i4 = n4 - 4; i4 <= n4 + 4; ++i4) {
                    if (fd2.f(i2, i3, i4) != ln.g) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        super.b(fd2, n2, n3, n4, n5);
        ln ln2 = fd2.f(n2, n3 + 1, n4);
        if (ln2.a()) {
            fd2.f(n2, n3, n4, uu.w.bn);
        }
    }

    public int a(int n2, Random random) {
        return uu.w.a(0, random);
    }
}

