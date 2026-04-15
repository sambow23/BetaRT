/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class wp
extends uu {
    protected wp(int n2) {
        super(n2, ln.b);
        this.bm = 3;
        this.b(true);
    }

    public int a(xp xp2, int n2, int n3, int n4, int n5) {
        if (n5 == 1) {
            return 0;
        }
        if (n5 == 0) {
            return 2;
        }
        ln ln2 = xp2.f(n2, n3 + 1, n4);
        if (ln2 == ln.t || ln2 == ln.u) {
            return 68;
        }
        return 3;
    }

    public int b(xp xp2, int n2, int n3, int n4) {
        xp2.a().a(n2, n4, 1, 1);
        double d2 = xp2.a().a[0];
        double d3 = xp2.a().b[0];
        return ia.a(d2, d3);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.B) {
            return;
        }
        if (fd2.n(n2, n3 + 1, n4) < 4 && uu.q[fd2.a(n2, n3 + 1, n4)] > 2) {
            if (random.nextInt(4) != 0) {
                return;
            }
            fd2.f(n2, n3, n4, uu.w.bn);
        } else if (fd2.n(n2, n3 + 1, n4) >= 9) {
            int n5 = n2 + random.nextInt(3) - 1;
            int n6 = n3 + random.nextInt(5) - 3;
            int n7 = n4 + random.nextInt(3) - 1;
            int n8 = fd2.a(n5, n6 + 1, n7);
            if (fd2.a(n5, n6, n7) == uu.w.bn && fd2.n(n5, n6 + 1, n7) >= 4 && uu.q[n8] <= 2) {
                fd2.f(n5, n6, n7, uu.v.bn);
            }
        }
    }

    public int a(int n2, Random random) {
        return uu.w.a(0, random);
    }
}

