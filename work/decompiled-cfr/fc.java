/*
 * Decompiled with CFR 0.152.
 */
public class fc
extends uu {
    private boolean a;

    protected fc(int n2, int n3, boolean bl2) {
        super(n2, ln.x);
        this.bm = n3;
        this.b(true);
        this.a = bl2;
    }

    public int a(int n2, int n3) {
        if (n2 == 1) {
            return this.bm;
        }
        if (n2 == 0) {
            return this.bm;
        }
        int n4 = this.bm + 1 + 16;
        if (this.a) {
            ++n4;
        }
        if (n3 == 2 && n2 == 2) {
            return n4;
        }
        if (n3 == 3 && n2 == 5) {
            return n4;
        }
        if (n3 == 0 && n2 == 3) {
            return n4;
        }
        if (n3 == 1 && n2 == 4) {
            return n4;
        }
        return this.bm + 16;
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm;
        }
        if (n2 == 0) {
            return this.bm;
        }
        if (n2 == 3) {
            return this.bm + 1 + 16;
        }
        return this.bm + 16;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        super.c(fd2, n2, n3, n4);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3, n4);
        return (n5 == 0 || uu.m[n5].bA.g()) && fd2.h(n2, n3 - 1, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, ls ls2) {
        int n5 = in.b((double)(ls2.aS * 4.0f / 360.0f) + 2.5) & 3;
        fd2.d(n2, n3, n4, n5);
    }
}

