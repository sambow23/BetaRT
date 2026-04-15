/*
 * Decompiled with CFR 0.152.
 */
public class fi
extends uu {
    protected fi(int n2) {
        super(n2, ln.d);
        this.bm = 59;
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm - 16;
        }
        if (n2 == 0) {
            return uu.y.a(0);
        }
        if (n2 == 2 || n2 == 4) {
            return this.bm + 1;
        }
        return this.bm;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.B) {
            return true;
        }
        gs2.a(n2, n3, n4);
        return true;
    }
}

