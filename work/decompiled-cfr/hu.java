/*
 * Decompiled with CFR 0.152.
 */
public class hu
extends gm {
    private ln a;

    public hu(int n2, ln ln2) {
        super(n2);
        this.a = ln2;
        this.bg = 1;
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 != 1) {
            return false;
        }
        uu uu2 = this.a == ln.d ? uu.aF : uu.aM;
        if (!uu2.a(fd2, n2, ++n3, n4)) {
            return false;
        }
        int n6 = in.b((double)((gs2.aS + 180.0f) * 4.0f / 360.0f) - 0.5) & 3;
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
        int n9 = (fd2.h(n2 - n7, n3, n4 - n8) ? 1 : 0) + (fd2.h(n2 - n7, n3 + 1, n4 - n8) ? 1 : 0);
        int n10 = (fd2.h(n2 + n7, n3, n4 + n8) ? 1 : 0) + (fd2.h(n2 + n7, n3 + 1, n4 + n8) ? 1 : 0);
        boolean bl2 = fd2.a(n2 - n7, n3, n4 - n8) == uu2.bn || fd2.a(n2 - n7, n3 + 1, n4 - n8) == uu2.bn;
        boolean bl3 = fd2.a(n2 + n7, n3, n4 + n8) == uu2.bn || fd2.a(n2 + n7, n3 + 1, n4 + n8) == uu2.bn;
        boolean bl4 = false;
        if (bl2 && !bl3) {
            bl4 = true;
        } else if (n10 > n9) {
            bl4 = true;
        }
        if (bl4) {
            n6 = n6 - 1 & 3;
            n6 += 4;
        }
        fd2.o = true;
        fd2.b(n2, n3, n4, uu2.bn, n6);
        fd2.b(n2, n3 + 1, n4, uu2.bn, n6 + 8);
        fd2.o = false;
        fd2.i(n2, n3, n4, uu2.bn);
        fd2.i(n2, n3 + 1, n4, uu2.bn);
        --iz2.a;
        return true;
    }
}

