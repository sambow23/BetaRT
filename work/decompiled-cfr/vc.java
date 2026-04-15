/*
 * Decompiled with CFR 0.152.
 */
public class vc
extends gm {
    public vc(int n2) {
        super(n2);
        this.bg = 1;
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 == 0) {
            return false;
        }
        if (!fd2.f(n2, n3, n4).a()) {
            return false;
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
        if (!uu.aE.a(fd2, n2, n3, n4)) {
            return false;
        }
        if (n5 == 1) {
            fd2.b(n2, n3, n4, uu.aE.bn, in.b((double)((gs2.aS + 180.0f) * 16.0f / 360.0f) + 0.5) & 0xF);
        } else {
            fd2.b(n2, n3, n4, uu.aJ.bn, n5);
        }
        --iz2.a;
        yk yk2 = (yk)fd2.b(n2, n3, n4);
        if (yk2 != null) {
            gs2.a(yk2);
        }
        return true;
    }
}

