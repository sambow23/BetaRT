/*
 * Decompiled with CFR 0.152.
 */
public class au
extends dq {
    private static uu[] bk = new uu[]{uu.x, uu.ak, uu.al, uu.u, uu.R, uu.ap, uu.I, uu.aj, uu.J, uu.ai, uu.H, uu.ax, uu.ay, uu.aU, uu.bc, uu.O, uu.P};

    protected au(int n2, bu bu2) {
        super(n2, 2, bu2, bk);
    }

    public boolean a(uu uu2) {
        if (uu2 == uu.aq) {
            return this.a.d() == 3;
        }
        if (uu2 == uu.ay || uu2 == uu.ax) {
            return this.a.d() >= 2;
        }
        if (uu2 == uu.ai || uu2 == uu.H) {
            return this.a.d() >= 2;
        }
        if (uu2 == uu.aj || uu2 == uu.I) {
            return this.a.d() >= 1;
        }
        if (uu2 == uu.P || uu2 == uu.O) {
            return this.a.d() >= 1;
        }
        if (uu2 == uu.aO || uu2 == uu.aP) {
            return this.a.d() >= 2;
        }
        if (uu2.bA == ln.e) {
            return true;
        }
        return uu2.bA == ln.f;
    }
}

