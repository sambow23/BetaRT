/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class nk
extends jp {
    public nk(int n2, int n3) {
        super(n2, n3, ln.s, false);
        this.bB = 0.98f;
        this.b(true);
    }

    public int b_() {
        return 1;
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        return super.b(xp2, n2, n3, n4, 1 - n5);
    }

    public void a(fd fd2, gs gs2, int n2, int n3, int n4, int n5) {
        super.a(fd2, gs2, n2, n3, n4, n5);
        ln ln2 = fd2.f(n2, n3 - 1, n4);
        if (ln2.c() || ln2.d()) {
            fd2.f(n2, n3, n4, uu.B.bn);
        }
    }

    public int a(Random random) {
        return 0;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.a(eb.b, n2, n3, n4) > 11 - uu.q[this.bn]) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, uu.C.bn);
        }
    }

    public int h() {
        return 0;
    }
}

