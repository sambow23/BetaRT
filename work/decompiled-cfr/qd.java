/*
 * Decompiled with CFR 0.152.
 */
public class qd
extends gm {
    private int a;

    public qd(int n2, bu bu2) {
        super(n2);
        this.bg = 1;
        this.e(bu2.a());
        this.a = 4 + bu2.c() * 2;
    }

    public float a(iz iz2, uu uu2) {
        if (uu2.bn == uu.X.bn) {
            return 15.0f;
        }
        return 1.5f;
    }

    public boolean a(iz iz2, ls ls2, ls ls3) {
        iz2.a(1, ls3);
        return true;
    }

    public boolean a(iz iz2, int n2, int n3, int n4, int n5, ls ls2) {
        iz2.a(2, ls2);
        return true;
    }

    public int a(sn sn2) {
        return this.a;
    }

    public boolean b() {
        return true;
    }

    public boolean a(uu uu2) {
        return uu2.bn == uu.X.bn;
    }
}

