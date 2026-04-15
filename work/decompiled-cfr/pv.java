/*
 * Decompiled with CFR 0.152.
 */
class pv
extends gp {
    final /* synthetic */ int d;
    final /* synthetic */ aa e;

    pv(aa aa2, lw lw2, int n2, int n3, int n4, int n5) {
        this.e = aa2;
        this.d = n5;
        super(lw2, n2, n3, n4);
    }

    public int d() {
        return 1;
    }

    public boolean b(iz iz2) {
        if (iz2.a() instanceof wa) {
            return ((wa)iz2.a()).bk == this.d;
        }
        if (iz2.a().bf == uu.bb.bn) {
            return this.d == 0;
        }
        return false;
    }
}

