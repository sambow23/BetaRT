/*
 * Decompiled with CFR 0.152.
 */
public class lp
extends gm {
    public lp(int n2) {
        super(n2);
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 != 1) {
            return false;
        }
        ++n3;
        ve ve2 = (ve)uu.T;
        int n6 = in.b((double)(gs2.aS * 4.0f / 360.0f) + 0.5) & 3;
        int n7 = 0;
        int n8 = 0;
        if (n6 == 0) {
            n8 = 1;
        }
        if (n6 == 1) {
            n7 = -1;
        }
        if (n6 == 2) {
            n8 = -1;
        }
        if (n6 == 3) {
            n7 = 1;
        }
        if (fd2.d(n2, n3, n4) && fd2.d(n2 + n7, n3, n4 + n8) && fd2.h(n2, n3 - 1, n4) && fd2.h(n2 + n7, n3 - 1, n4 + n8)) {
            fd2.b(n2, n3, n4, ve2.bn, n6);
            fd2.b(n2 + n7, n3, n4 + n8, ve2.bn, n6 + 8);
            --iz2.a;
            return true;
        }
        return false;
    }
}

