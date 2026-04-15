/*
 * Decompiled with CFR 0.152.
 */
public class bm
extends dw {
    private sk a;
    private int b = 0;
    private int c = 0;
    private int h = 0;

    public bm(ix ix2, sk sk2) {
        int n2;
        this.a = sk2;
        this.a(new gp(sk2, 0, 56, 17));
        this.a(new gp(sk2, 1, 56, 53));
        this.a(new vq(ix2.d, sk2, 2, 116, 35));
        for (n2 = 0; n2 < 3; ++n2) {
            for (int i2 = 0; i2 < 9; ++i2) {
                this.a(new gp(ix2, i2 + n2 * 9 + 9, 8 + i2 * 18, 84 + n2 * 18));
            }
        }
        for (n2 = 0; n2 < 9; ++n2) {
            this.a(new gp(ix2, n2, 8 + n2 * 18, 142));
        }
    }

    public void a() {
        super.a();
        for (int i2 = 0; i2 < this.g.size(); ++i2) {
            ec ec2 = (ec)this.g.get(i2);
            if (this.b != this.a.c) {
                ec2.a((dw)this, 0, this.a.c);
            }
            if (this.c != this.a.a) {
                ec2.a((dw)this, 1, this.a.a);
            }
            if (this.h == this.a.b) continue;
            ec2.a((dw)this, 2, this.a.b);
        }
        this.b = this.a.c;
        this.c = this.a.a;
        this.h = this.a.b;
    }

    public void a(int n2, int n3) {
        if (n2 == 0) {
            this.a.c = n3;
        }
        if (n2 == 1) {
            this.a.a = n3;
        }
        if (n2 == 2) {
            this.a.b = n3;
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
            if (n2 == 2) {
                this.a(iz3, 3, 39, true);
            } else if (n2 >= 3 && n2 < 30) {
                this.a(iz3, 30, 39, false);
            } else if (n2 >= 30 && n2 < 39) {
                this.a(iz3, 3, 30, false);
            } else {
                this.a(iz3, 3, 39, false);
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

