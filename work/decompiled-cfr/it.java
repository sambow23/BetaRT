/*
 * Decompiled with CFR 0.152.
 */
public class it
extends dw {
    public mq a = new mq(this, 3, 3);
    public lw b = new wl();
    private fd c;
    private int h;
    private int i;
    private int j;

    public it(ix ix2, fd fd2, int n2, int n3, int n4) {
        int n5;
        int n6;
        this.c = fd2;
        this.h = n2;
        this.i = n3;
        this.j = n4;
        this.a(new yv(ix2.d, this.a, this.b, 0, 124, 35));
        for (n6 = 0; n6 < 3; ++n6) {
            for (n5 = 0; n5 < 3; ++n5) {
                this.a(new gp(this.a, n5 + n6 * 3, 30 + n5 * 18, 17 + n6 * 18));
            }
        }
        for (n6 = 0; n6 < 3; ++n6) {
            for (n5 = 0; n5 < 9; ++n5) {
                this.a(new gp(ix2, n5 + n6 * 9 + 9, 8 + n5 * 18, 84 + n6 * 18));
            }
        }
        for (n6 = 0; n6 < 9; ++n6) {
            this.a(new gp(ix2, n6, 8 + n6 * 18, 142));
        }
        this.a(this.a);
    }

    public void a(lw lw2) {
        this.b.a(0, hk.a().a(this.a));
    }

    public void a(gs gs2) {
        super.a(gs2);
        if (this.c.B) {
            return;
        }
        for (int i2 = 0; i2 < 9; ++i2) {
            iz iz2 = this.a.f_(i2);
            if (iz2 == null) continue;
            gs2.a(iz2);
        }
    }

    public boolean b(gs gs2) {
        if (this.c.a(this.h, this.i, this.j) != uu.az.bn) {
            return false;
        }
        return !(gs2.g((double)this.h + 0.5, (double)this.i + 0.5, (double)this.j + 0.5) > 64.0);
    }

    public iz a(int n2) {
        iz iz2 = null;
        gp gp2 = (gp)this.e.get(n2);
        if (gp2 != null && gp2.b()) {
            iz iz3 = gp2.a();
            iz2 = iz3.k();
            if (n2 == 0) {
                this.a(iz3, 10, 46, true);
            } else if (n2 >= 10 && n2 < 37) {
                this.a(iz3, 37, 46, false);
            } else if (n2 >= 37 && n2 < 46) {
                this.a(iz3, 10, 37, false);
            } else {
                this.a(iz3, 10, 46, false);
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

