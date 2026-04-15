/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class um
extends uu {
    protected um(int n2, int n3) {
        super(n2, n3, ln.z);
        this.b(true);
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        float f2 = 0.0625f;
        float f3 = (float)(1 + n5 * 2) / 16.0f;
        float f4 = 0.5f;
        this.a(f3, 0.0f, f2, 1.0f - f2, f4, 1.0f - f2);
    }

    public void g() {
        float f2 = 0.0625f;
        float f3 = 0.5f;
        this.a(f2, 0.0f, f2, 1.0f - f2, f3, 1.0f - f2);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        float f2 = 0.0625f;
        float f3 = (float)(1 + n5 * 2) / 16.0f;
        float f4 = 0.5f;
        return eq.b((float)n2 + f3, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (float)n3 + f4 - f2, (float)(n4 + 1) - f2);
    }

    public eq f(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        float f2 = 0.0625f;
        float f3 = (float)(1 + n5 * 2) / 16.0f;
        float f4 = 0.5f;
        return eq.b((float)n2 + f3, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (float)n3 + f4, (float)(n4 + 1) - f2);
    }

    public int a(int n2, int n3) {
        if (n2 == 1) {
            return this.bm;
        }
        if (n2 == 0) {
            return this.bm + 3;
        }
        if (n3 > 0 && n2 == 4) {
            return this.bm + 2;
        }
        return this.bm + 1;
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm;
        }
        if (n2 == 0) {
            return this.bm + 3;
        }
        return this.bm + 1;
    }

    public boolean d() {
        return false;
    }

    public boolean c() {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.c(fd2, n2, n3, n4, gs2);
        return true;
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.c(fd2, n2, n3, n4, gs2);
    }

    private void c(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (gs2.Y < 20) {
            gs2.c(3);
            int n5 = fd2.e(n2, n3, n4) + 1;
            if (n5 >= 6) {
                fd2.f(n2, n3, n4, 0);
            } else {
                fd2.d(n2, n3, n4, n5);
                fd2.k(n2, n3, n4);
            }
        }
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
        return fd2.f(n2, n3 - 1, n4).a();
    }

    public int a(Random random) {
        return 0;
    }

    public int a(int n2, Random random) {
        return 0;
    }
}

