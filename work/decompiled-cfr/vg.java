/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class vg
extends uu {
    protected vg(int n2) {
        super(n2, ln.d);
        this.bm = 20;
    }

    public int a(Random random) {
        return 1;
    }

    public int a(int n2, Random random) {
        return uu.K.bn;
    }

    public void a(fd fd2, gs gs2, int n2, int n3, int n4, int n5) {
        super.a(fd2, gs2, n2, n3, n4, n5);
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        int n5 = 4;
        int n6 = n5 + 1;
        if (fd2.a(n2 - n6, n3 - n6, n4 - n6, n2 + n6, n3 + n6, n4 + n6)) {
            for (int i2 = -n5; i2 <= n5; ++i2) {
                for (int i3 = -n5; i3 <= n5; ++i3) {
                    for (int i4 = -n5; i4 <= n5; ++i4) {
                        int n7;
                        int n8 = fd2.a(n2 + i2, n3 + i3, n4 + i4);
                        if (n8 != uu.L.bn || ((n7 = fd2.e(n2 + i2, n3 + i3, n4 + i4)) & 8) != 0) continue;
                        fd2.e(n2 + i2, n3 + i3, n4 + i4, n7 | 8);
                    }
                }
            }
        }
    }

    public int a(int n2, int n3) {
        if (n2 == 1) {
            return 21;
        }
        if (n2 == 0) {
            return 21;
        }
        if (n3 == 1) {
            return 116;
        }
        if (n3 == 2) {
            return 117;
        }
        return 20;
    }

    protected int b_(int n2) {
        return n2;
    }
}

