/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class bs
extends uu {
    private boolean a;

    public bs(int n2, int n3, boolean bl2) {
        super(n2, n3, ln.e);
        if (bl2) {
            this.b(true);
        }
        this.a = bl2;
    }

    public int e() {
        return 30;
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.h(fd2, n2, n3, n4);
        super.b(fd2, n2, n3, n4, gs2);
    }

    public void b(fd fd2, int n2, int n3, int n4, sn sn2) {
        this.h(fd2, n2, n3, n4);
        super.b(fd2, n2, n3, n4, sn2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.h(fd2, n2, n3, n4);
        return super.a(fd2, n2, n3, n4, gs2);
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        this.i(fd2, n2, n3, n4);
        if (this.bn == uu.aO.bn) {
            fd2.f(n2, n3, n4, uu.aP.bn);
        }
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (this.bn == uu.aP.bn) {
            fd2.f(n2, n3, n4, uu.aO.bn);
        }
    }

    public int a(int n2, Random random) {
        return gm.aA.bf;
    }

    public int a(Random random) {
        return 4 + random.nextInt(2);
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        if (this.a) {
            this.i(fd2, n2, n3, n4);
        }
    }

    private void i(fd fd2, int n2, int n3, int n4) {
        Random random = fd2.r;
        double d2 = 0.0625;
        for (int i2 = 0; i2 < 6; ++i2) {
            double d3 = (float)n2 + random.nextFloat();
            double d4 = (float)n3 + random.nextFloat();
            double d5 = (float)n4 + random.nextFloat();
            if (i2 == 0 && !fd2.g(n2, n3 + 1, n4)) {
                d4 = (double)(n3 + 1) + d2;
            }
            if (i2 == 1 && !fd2.g(n2, n3 - 1, n4)) {
                d4 = (double)(n3 + 0) - d2;
            }
            if (i2 == 2 && !fd2.g(n2, n3, n4 + 1)) {
                d5 = (double)(n4 + 1) + d2;
            }
            if (i2 == 3 && !fd2.g(n2, n3, n4 - 1)) {
                d5 = (double)(n4 + 0) - d2;
            }
            if (i2 == 4 && !fd2.g(n2 + 1, n3, n4)) {
                d3 = (double)(n2 + 1) + d2;
            }
            if (i2 == 5 && !fd2.g(n2 - 1, n3, n4)) {
                d3 = (double)(n2 + 0) - d2;
            }
            if (!(d3 < (double)n2 || d3 > (double)(n2 + 1) || d4 < 0.0 || d4 > (double)(n3 + 1) || d5 < (double)n4) && !(d5 > (double)(n4 + 1))) continue;
            fd2.a("reddust", d3, d4, d5, 0.0, 0.0, 0.0);
        }
    }
}

