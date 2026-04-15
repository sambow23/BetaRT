/*
 * Decompiled with CFR 0.152.
 */
public class cd
extends dw {
    private lw a;
    private int b;

    public cd(lw lw2, lw lw3) {
        int n2;
        int n3;
        this.a = lw3;
        this.b = lw3.a() / 9;
        int n4 = (this.b - 4) * 18;
        for (n3 = 0; n3 < this.b; ++n3) {
            for (n2 = 0; n2 < 9; ++n2) {
                this.a(new gp(lw3, n2 + n3 * 9, 8 + n2 * 18, 18 + n3 * 18));
            }
        }
        for (n3 = 0; n3 < 3; ++n3) {
            for (n2 = 0; n2 < 9; ++n2) {
                this.a(new gp(lw2, n2 + n3 * 9 + 9, 8 + n2 * 18, 103 + n3 * 18 + n4));
            }
        }
        for (n3 = 0; n3 < 9; ++n3) {
            this.a(new gp(lw2, n3, 8 + n3 * 18, 161 + n4));
        }
    }

    public boolean b(gs gs2) {
        return this.a.a_(gs2);
    }

    public iz a(int n2) {
        iz iz2 = null;
        gp gp2 = (gp)this.e.get(n2);
        if (gp2 != null && gp2.b()) {
            iz iz3 = gp2.a();
            iz2 = iz3.k();
            if (n2 < this.b * 9) {
                this.a(iz3, this.b * 9, this.e.size(), true);
            } else {
                this.a(iz3, 0, this.b * 9, false);
            }
            if (iz3.a == 0) {
                gp2.c(null);
            } else {
                gp2.c();
            }
        }
        return iz2;
    }
}

