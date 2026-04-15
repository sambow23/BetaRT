/*
 * Decompiled with CFR 0.152.
 */
public class ie
extends gm {
    public ie(int n2) {
        super(n2);
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.a(n2, n3, n4) != uu.aT.bn) {
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
            if (!fd2.d(n2, n3, n4)) {
                return false;
            }
        }
        if (uu.aw.a(fd2, n2, n3, n4)) {
            --iz2.a;
            fd2.f(n2, n3, n4, uu.aw.bn);
        }
        return true;
    }
}

