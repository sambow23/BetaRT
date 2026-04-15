/*
 * Decompiled with CFR 0.152.
 */
public class uy
extends ck {
    public uy(int n2) {
        super(n2);
        this.e(0);
        this.a(true);
    }

    public int b(int n2) {
        return n2 | 8;
    }

    public int a(int n2) {
        return uu.L.a(0, n2);
    }

    public int f(int n2) {
        if ((n2 & 1) == 1) {
            return jh.a();
        }
        if ((n2 & 2) == 2) {
            return jh.b();
        }
        return jh.c();
    }
}

