/*
 * Decompiled with CFR 0.152.
 */
public class bl
extends gm {
    public bl(int n2) {
        super(n2);
        this.d(1);
        this.e(238);
    }

    public boolean a(iz iz2, int n2, int n3, int n4, int n5, ls ls2) {
        if (n2 == uu.L.bn || n2 == uu.X.bn) {
            iz2.a(1, ls2);
        }
        return super.a(iz2, n2, n3, n4, n5, ls2);
    }

    public boolean a(uu uu2) {
        return uu2.bn == uu.X.bn;
    }

    public float a(iz iz2, uu uu2) {
        if (uu2.bn == uu.X.bn || uu2.bn == uu.L.bn) {
            return 15.0f;
        }
        if (uu2.bn == uu.ac.bn) {
            return 5.0f;
        }
        return super.a(iz2, uu2);
    }
}

