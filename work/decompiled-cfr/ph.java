/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Random;

public class ph
extends pc {
    public ph(int n2, int n3) {
        super(n2, n3, true);
        this.b(true);
    }

    public int e() {
        return 20;
    }

    public boolean f() {
        return true;
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2) {
        if (fd2.B) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        if ((n5 & 8) != 0) {
            return;
        }
        this.f(fd2, n2, n3, n4, n5);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.B) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        if ((n5 & 8) == 0) {
            return;
        }
        this.f(fd2, n2, n3, n4, n5);
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        return (xp2.e(n2, n3, n4) & 8) != 0;
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        if ((fd2.e(n2, n3, n4) & 8) == 0) {
            return false;
        }
        return n5 == 1;
    }

    private void f(fd fd2, int n2, int n3, int n4, int n5) {
        boolean bl2 = (n5 & 8) != 0;
        boolean bl3 = false;
        float f2 = 0.125f;
        List list = fd2.a(yl.class, eq.b((float)n2 + f2, n3, (float)n4 + f2, (float)(n2 + 1) - f2, (double)n3 + 0.25, (float)(n4 + 1) - f2));
        if (list.size() > 0) {
            bl3 = true;
        }
        if (bl3 && !bl2) {
            fd2.d(n2, n3, n4, n5 | 8);
            fd2.i(n2, n3, n4, this.bn);
            fd2.i(n2, n3 - 1, n4, this.bn);
            fd2.b(n2, n3, n4, n2, n3, n4);
        }
        if (!bl3 && bl2) {
            fd2.d(n2, n3, n4, n5 & 7);
            fd2.i(n2, n3, n4, this.bn);
            fd2.i(n2, n3 - 1, n4, this.bn);
            fd2.b(n2, n3, n4, n2, n3, n4);
        }
        if (bl3) {
            fd2.c(n2, n3, n4, this.bn, this.e());
        }
    }
}

