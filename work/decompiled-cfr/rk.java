/*
 * Decompiled with CFR 0.152.
 */
public class rk
extends gm {
    private int a;

    public rk(int n2, int n3) {
        super(n2);
        this.a = n3;
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 != 1) {
            return false;
        }
        int n6 = fd2.a(n2, n3, n4);
        if (n6 == uu.aB.bn && fd2.d(n2, n3 + 1, n4)) {
            fd2.f(n2, n3 + 1, n4, this.a);
            --iz2.a;
            return true;
        }
        return false;
    }
}

