/*
 * Decompiled with CFR 0.152.
 */
public class xf
extends uu {
    protected xf(int n2) {
        super(n2, ln.k);
        this.bm = 48;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        int n5 = 2;
        for (int i2 = n2 - n5; i2 <= n2 + n5; ++i2) {
            for (int i3 = n3 - n5; i3 <= n3 + n5; ++i3) {
                for (int i4 = n4 - n5; i4 <= n4 + n5; ++i4) {
                    if (fd2.f(i2, i3, i4) != ln.g) continue;
                }
            }
        }
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        int n5 = 2;
        for (int i2 = n2 - n5; i2 <= n2 + n5; ++i2) {
            for (int i3 = n3 - n5; i3 <= n3 + n5; ++i3) {
                for (int i4 = n4 - n5; i4 <= n4 + n5; ++i4) {
                    fd2.i(i2, i3, i4, fd2.a(i2, i3, i4));
                }
            }
        }
    }
}

