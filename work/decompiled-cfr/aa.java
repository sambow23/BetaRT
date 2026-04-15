/*
 * Decompiled with CFR 0.152.
 */
public class aa
extends dw {
    public mq a = new mq(this, 2, 2);
    public lw b = new wl();
    public boolean c = false;

    public aa(ix ix2) {
        this(ix2, true);
    }

    public aa(ix ix2, boolean bl2) {
        int n2;
        int n3;
        this.c = bl2;
        this.a(new yv(ix2.d, this.a, this.b, 0, 144, 36));
        for (n3 = 0; n3 < 2; ++n3) {
            for (n2 = 0; n2 < 2; ++n2) {
                this.a(new gp(this.a, n2 + n3 * 2, 88 + n2 * 18, 26 + n3 * 18));
            }
        }
        for (n3 = 0; n3 < 4; ++n3) {
            n2 = n3;
            this.a(new pv(this, ix2, ix2.a() - 1 - n3, 8, 8 + n3 * 18, n2));
        }
        for (n3 = 0; n3 < 3; ++n3) {
            for (n2 = 0; n2 < 9; ++n2) {
                this.a(new gp(ix2, n2 + (n3 + 1) * 9, 8 + n2 * 18, 84 + n3 * 18));
            }
        }
        for (n3 = 0; n3 < 9; ++n3) {
            this.a(new gp(ix2, n3, 8 + n3 * 18, 142));
        }
        this.a(this.a);
    }

    public void a(lw lw2) {
        this.b.a(0, hk.a().a(this.a));
    }

    public void a(gs gs2) {
        super.a(gs2);
        for (int i2 = 0; i2 < 4; ++i2) {
            iz iz2 = this.a.f_(i2);
            if (iz2 == null) continue;
            gs2.a(iz2);
            this.a.a(i2, null);
        }
    }

    public boolean b(gs gs2) {
        return true;
    }

    public iz a(int n2) {
        iz iz2 = null;
        gp gp2 = (gp)this.e.get(n2);
        if (gp2 != null && gp2.b()) {
            iz iz3 = gp2.a();
            iz2 = iz3.k();
            if (n2 == 0) {
                this.a(iz3, 9, 45, true);
            } else if (n2 >= 9 && n2 < 36) {
                this.a(iz3, 36, 45, false);
            } else if (n2 >= 36 && n2 < 45) {
                this.a(iz3, 9, 36, false);
            } else {
                this.a(iz3, 9, 45, false);
            }
            if (iz3.a == 0) {
                gp2.c(null);
            } else {
                gp2.c();
            }
            if (iz3.a != iz2.a) {
                gp2.a(iz3);
            } else {
                return null;
            }
        }
        return iz2;
    }
}

