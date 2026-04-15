/*
 * Decompiled with CFR 0.152.
 */
public class ym
extends gm {
    public ym(int n2) {
        super(n2);
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        qv qv2;
        if (n5 == 0) {
            return false;
        }
        if (n5 == 1) {
            return false;
        }
        int n6 = 0;
        if (n5 == 4) {
            n6 = 1;
        }
        if (n5 == 3) {
            n6 = 2;
        }
        if (n5 == 5) {
            n6 = 3;
        }
        if ((qv2 = new qv(fd2, n2, n3, n4, n6)).k()) {
            if (!fd2.B) {
                fd2.b(qv2);
            }
            --iz2.a;
        }
        return true;
    }
}

