/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class dp
extends uu {
    protected dp(int n2, int n3) {
        super(n2, n3, ln.o);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        float f2 = 0.125f;
        if (n5 == 2) {
            this.a(0.0f, 0.0f, 1.0f - f2, 1.0f, 1.0f, 1.0f);
        }
        if (n5 == 3) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, f2);
        }
        if (n5 == 4) {
            this.a(1.0f - f2, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        if (n5 == 5) {
            this.a(0.0f, 0.0f, 0.0f, f2, 1.0f, 1.0f);
        }
        return super.e(fd2, n2, n3, n4);
    }

    public eq f(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        float f2 = 0.125f;
        if (n5 == 2) {
            this.a(0.0f, 0.0f, 1.0f - f2, 1.0f, 1.0f, 1.0f);
        }
        if (n5 == 3) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, f2);
        }
        if (n5 == 4) {
            this.a(1.0f - f2, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        if (n5 == 5) {
            this.a(0.0f, 0.0f, 0.0f, f2, 1.0f, 1.0f);
        }
        return super.f(fd2, n2, n3, n4);
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 8;
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
        if ((n6 == 0 || n5 == 2) && fd2.h(n2, n3, n4 + 1)) {
            n6 = 2;
        }
        if ((n6 == 0 || n5 == 3) && fd2.h(n2, n3, n4 - 1)) {
            n6 = 3;
        }
        if ((n6 == 0 || n5 == 4) && fd2.h(n2 + 1, n3, n4)) {
            n6 = 4;
        }
        if ((n6 == 0 || n5 == 5) && fd2.h(n2 - 1, n3, n4)) {
            n6 = 5;
        }
        fd2.d(n2, n3, n4, n6);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        boolean bl2 = false;
        if (n6 == 2 && fd2.h(n2, n3, n4 + 1)) {
            bl2 = true;
        }
        if (n6 == 3 && fd2.h(n2, n3, n4 - 1)) {
            bl2 = true;
        }
        if (n6 == 4 && fd2.h(n2 + 1, n3, n4)) {
            bl2 = true;
        }
        if (n6 == 5 && fd2.h(n2 - 1, n3, n4)) {
            bl2 = true;
        }
        if (!bl2) {
            this.g(fd2, n2, n3, n4, n6);
            fd2.f(n2, n3, n4, 0);
        }
        super.b(fd2, n2, n3, n4, n5);
    }

    public int a(Random random) {
        return 1;
    }
}

