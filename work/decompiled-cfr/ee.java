/*
 * Decompiled with CFR 0.152.
 */
public class ee
extends uu {
    public ee() {
        super(35, 64, ln.l);
    }

    public int a(int n2, int n3) {
        if (n3 == 0) {
            return this.bm;
        }
        n3 = ~(n3 & 0xF);
        return 113 + ((n3 & 8) >> 3) + (n3 & 7) * 16;
    }

    protected int b_(int n2) {
        return n2;
    }

    public static int d(int n2) {
        return ~n2 & 0xF;
    }

    public static int e(int n2) {
        return ~n2 & 0xF;
    }
}

