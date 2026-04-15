/*
 * Decompiled with CFR 0.152.
 */
public class ii
extends ls {
    private dh a;
    protected sn d;
    protected boolean e = false;

    public ii(fd fd2) {
        super(fd2);
    }

    protected boolean e_() {
        return false;
    }

    protected void f_() {
        this.e = this.e_();
        float f2 = 16.0f;
        if (this.d == null) {
            this.d = this.g_();
            if (this.d != null) {
                this.a = this.aI.a(this, this.d, f2);
            }
        } else if (!this.d.W()) {
            this.d = null;
        } else {
            float f3 = this.d.f(this);
            if (this.e(this.d)) {
                this.a(this.d, f3);
            } else {
                this.b(this.d, f3);
            }
        }
        if (!(this.e || this.d == null || this.a != null && this.bs.nextInt(20) != 0)) {
            this.a = this.aI.a(this, this.d, f2);
        } else if (!this.e && (this.a == null && this.bs.nextInt(80) == 0 || this.bs.nextInt(80) == 0)) {
            this.E();
        }
        int n2 = in.b(this.aW.b + 0.5);
        boolean bl2 = this.ag();
        boolean bl3 = this.ah();
        this.aT = 0.0f;
        if (this.a == null || this.bs.nextInt(100) == 0) {
            super.f_();
            this.a = null;
            return;
        }
        bt bt2 = this.a.a(this);
        double d2 = this.bg * 2.0f;
        while (bt2 != null && bt2.d(this.aM, bt2.b, this.aO) < d2 * d2) {
            this.a.a();
            if (this.a.b()) {
                bt2 = null;
                this.a = null;
                continue;
            }
            bt2 = this.a.a(this);
        }
        this.az = false;
        if (bt2 != null) {
            float f4;
            double d3 = bt2.a - this.aM;
            double d4 = bt2.c - this.aO;
            double d5 = bt2.b - (double)n2;
            float f5 = (float)(Math.atan2(d4, d3) * 180.0 / 3.1415927410125732) - 90.0f;
            this.ax = this.aB;
            for (f4 = f5 - this.aS; f4 < -180.0f; f4 += 360.0f) {
            }
            while (f4 >= 180.0f) {
                f4 -= 360.0f;
            }
            if (f4 > 30.0f) {
                f4 = 30.0f;
            }
            if (f4 < -30.0f) {
                f4 = -30.0f;
            }
            this.aS += f4;
            if (this.e && this.d != null) {
                double d6 = this.d.aM - this.aM;
                double d7 = this.d.aO - this.aO;
                float f6 = this.aS;
                this.aS = (float)(Math.atan2(d7, d6) * 180.0 / 3.1415927410125732) - 90.0f;
                f4 = (f6 - this.aS + 90.0f) * (float)Math.PI / 180.0f;
                this.aw = -in.a(f4) * this.ax * 1.0f;
                this.ax = in.b(f4) * this.ax * 1.0f;
            }
            if (d5 > 0.0) {
                this.az = true;
            }
        }
        if (this.d != null) {
            this.a(this.d, 30.0f, 30.0f);
        }
        if (this.aY && !this.F()) {
            this.az = true;
        }
        if (this.bs.nextFloat() < 0.8f && (bl2 || bl3)) {
            this.az = true;
        }
    }

    protected void E() {
        boolean bl2 = false;
        int n2 = -1;
        int n3 = -1;
        int n4 = -1;
        float f2 = -99999.0f;
        for (int i2 = 0; i2 < 10; ++i2) {
            int n5;
            int n6;
            int n7 = in.b(this.aM + (double)this.bs.nextInt(13) - 6.0);
            float f3 = this.a(n7, n6 = in.b(this.aN + (double)this.bs.nextInt(7) - 3.0), n5 = in.b(this.aO + (double)this.bs.nextInt(13) - 6.0));
            if (!(f3 > f2)) continue;
            f2 = f3;
            n2 = n7;
            n3 = n6;
            n4 = n5;
            bl2 = true;
        }
        if (bl2) {
            this.a = this.aI.a((sn)this, n2, n3, n4, 10.0f);
        }
    }

    protected void a(sn sn2, float f2) {
    }

    protected void b(sn sn2, float f2) {
    }

    protected float a(int n2, int n3, int n4) {
        return 0.0f;
    }

    protected sn g_() {
        return null;
    }

    public boolean d() {
        int n2 = in.b(this.aM);
        int n3 = in.b(this.aW.b);
        int n4 = in.b(this.aO);
        return super.d() && this.a(n2, n3, n4) >= 0.0f;
    }

    public boolean F() {
        return this.a != null;
    }

    public void a(dh dh2) {
        this.a = dh2;
    }

    public sn G() {
        return this.d;
    }

    public void c(sn sn2) {
        this.d = sn2;
    }
}

