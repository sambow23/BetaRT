/*
 * Decompiled with CFR 0.152.
 */
public class ye
extends gm {
    public ye(int n2) {
        super(n2);
        this.bg = 1;
        this.e(64);
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        int n6;
        if (n5 == 0) {
            --n3;
        }
        if (n5 == 1) {
            ++n3;
        }
        if (n5 == 2) {
            --n4;
        }
        if (n5 == 3) {
            ++n4;
        }
        if (n5 == 4) {
            --n2;
        }
        if (n5 == 5) {
            ++n2;
        }
        if ((n6 = fd2.a(n2, n3, n4)) == 0) {
            fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "fire.ignite", 1.0f, b.nextFloat() * 0.4f + 0.8f);
            fd2.f(n2, n3, n4, uu.as.bn);
        }
        iz2.a(1, (sn)gs2);
        return true;
    }
}

