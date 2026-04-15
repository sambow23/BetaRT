/*
 * Decompiled with CFR 0.152.
 */
public class dq
extends gm {
    private uu[] bk;
    private float bl = 4.0f;
    private int bm;
    protected bu a;

    protected dq(int n2, int n3, bu bu2, uu[] uuArray) {
        super(n2);
        this.a = bu2;
        this.bk = uuArray;
        this.bg = 1;
        this.e(bu2.a());
        this.bl = bu2.b();
        this.bm = n3 + bu2.c();
    }

    public float a(iz iz2, uu uu2) {
        for (int i2 = 0; i2 < this.bk.length; ++i2) {
            if (this.bk[i2] != uu2) continue;
            return this.bl;
        }
        return 1.0f;
    }

    public boolean a(iz iz2, ls ls2, ls ls3) {
        iz2.a(2, ls3);
        return true;
    }

    public boolean a(iz iz2, int n2, int n3, int n4, int n5, ls ls2) {
        iz2.a(1, ls2);
        return true;
    }

    public int a(sn sn2) {
        return this.bm;
    }

    public boolean b() {
        return true;
    }
}

