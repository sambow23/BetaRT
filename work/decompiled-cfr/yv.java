/*
 * Decompiled with CFR 0.152.
 */
public class yv
extends gp {
    private final lw d;
    private gs e;

    public yv(gs gs2, lw lw2, lw lw3, int n2, int n3, int n4) {
        super(lw3, n2, n3, n4);
        this.e = gs2;
        this.d = lw2;
    }

    public boolean b(iz iz2) {
        return false;
    }

    public void a(iz iz2) {
        iz2.b(this.e.aI, this.e);
        if (iz2.c == uu.az.bn) {
            this.e.a(ep.h, 1);
        } else if (iz2.c == gm.r.bf) {
            this.e.a(ep.i, 1);
        } else if (iz2.c == uu.aC.bn) {
            this.e.a(ep.j, 1);
        } else if (iz2.c == gm.L.bf) {
            this.e.a(ep.l, 1);
        } else if (iz2.c == gm.S.bf) {
            this.e.a(ep.m, 1);
        } else if (iz2.c == gm.aX.bf) {
            this.e.a(ep.n, 1);
        } else if (iz2.c == gm.v.bf) {
            this.e.a(ep.o, 1);
        } else if (iz2.c == gm.p.bf) {
            this.e.a(ep.r, 1);
        }
        for (int i2 = 0; i2 < this.d.a(); ++i2) {
            iz iz3 = this.d.f_(i2);
            if (iz3 == null) continue;
            this.d.a(i2, 1);
            if (!iz3.a().j()) continue;
            this.d.a(i2, new iz(iz3.a().i()));
        }
    }
}

