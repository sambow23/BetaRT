/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class fz
extends sn {
    public int a = 0;
    public int b = 0;
    public int c = 1;
    private int d;
    private double e;
    private double f;
    private double g;
    private double h;
    private double i;
    private double j;
    private double k;
    private double l;

    public fz(fd fd2) {
        super(fd2);
        this.aF = true;
        this.b(1.5f, 0.6f);
        this.bf = this.bh / 2.0f;
    }

    protected boolean n() {
        return false;
    }

    protected void b() {
    }

    public eq a(sn sn2) {
        return sn2.aW;
    }

    public eq f() {
        return this.aW;
    }

    public boolean i_() {
        return true;
    }

    public fz(fd fd2, double d2, double d3, double d4) {
        this(fd2);
        this.e(d2, d3 + (double)this.bf, d4);
        this.aP = 0.0;
        this.aQ = 0.0;
        this.aR = 0.0;
        this.aJ = d2;
        this.aK = d3;
        this.aL = d4;
    }

    public double m() {
        return (double)this.bh * 0.0 - (double)0.3f;
    }

    public boolean a(sn sn2, int n2) {
        if (this.aI.B || this.be) {
            return true;
        }
        this.c = -this.c;
        this.b = 10;
        this.a += n2 * 10;
        this.ai();
        if (this.a > 40) {
            int n3;
            if (this.aG != null) {
                this.aG.i(this);
            }
            for (n3 = 0; n3 < 3; ++n3) {
                this.a(uu.y.bn, 1, 0.0f);
            }
            for (n3 = 0; n3 < 2; ++n3) {
                this.a(gm.B.bf, 1, 0.0f);
            }
            this.K();
        }
        return true;
    }

    public void h() {
        this.c = -this.c;
        this.b = 10;
        this.a += this.a * 10;
    }

    public boolean h_() {
        return !this.be;
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2) {
        this.e = d2;
        this.f = d3;
        this.g = d4;
        this.h = f2;
        this.i = f3;
        this.d = n2 + 4;
        this.aP = this.j;
        this.aQ = this.k;
        this.aR = this.l;
    }

    public void a(double d2, double d3, double d4) {
        this.j = this.aP = d2;
        this.k = this.aQ = d3;
        this.l = this.aR = d4;
    }

    public void w_() {
        double d2;
        double d3;
        double d4;
        super.w_();
        if (this.b > 0) {
            --this.b;
        }
        if (this.a > 0) {
            --this.a;
        }
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        int n2 = 5;
        double d5 = 0.0;
        for (int i2 = 0; i2 < n2; ++i2) {
            double d6 = this.aW.b + (this.aW.e - this.aW.b) * (double)(i2 + 0) / (double)n2 - 0.125;
            double d7 = this.aW.b + (this.aW.e - this.aW.b) * (double)(i2 + 1) / (double)n2 - 0.125;
            eq eq2 = eq.b(this.aW.a, d6, this.aW.c, this.aW.d, d7, this.aW.f);
            if (!this.aI.b(eq2, ln.g)) continue;
            d5 += 1.0 / (double)n2;
        }
        if (this.aI.B) {
            if (this.d > 0) {
                double d8;
                double d9 = this.aM + (this.e - this.aM) / (double)this.d;
                double d10 = this.aN + (this.f - this.aN) / (double)this.d;
                double d11 = this.aO + (this.g - this.aO) / (double)this.d;
                for (d8 = this.h - (double)this.aS; d8 < -180.0; d8 += 360.0) {
                }
                while (d8 >= 180.0) {
                    d8 -= 360.0;
                }
                this.aS = (float)((double)this.aS + d8 / (double)this.d);
                this.aT = (float)((double)this.aT + (this.i - (double)this.aT) / (double)this.d);
                --this.d;
                this.e(d9, d10, d11);
                this.c(this.aS, this.aT);
            } else {
                double d12 = this.aM + this.aP;
                double d13 = this.aN + this.aQ;
                double d14 = this.aO + this.aR;
                this.e(d12, d13, d14);
                if (this.aX) {
                    this.aP *= 0.5;
                    this.aQ *= 0.5;
                    this.aR *= 0.5;
                }
                this.aP *= (double)0.99f;
                this.aQ *= (double)0.95f;
                this.aR *= (double)0.99f;
            }
            return;
        }
        if (d5 < 1.0) {
            double d15 = d5 * 2.0 - 1.0;
            this.aQ += (double)0.04f * d15;
        } else {
            if (this.aQ < 0.0) {
                this.aQ /= 2.0;
            }
            this.aQ += (double)0.007f;
        }
        if (this.aG != null) {
            this.aP += this.aG.aP * 0.2;
            this.aR += this.aG.aR * 0.2;
        }
        if (this.aP < -(d4 = 0.4)) {
            this.aP = -d4;
        }
        if (this.aP > d4) {
            this.aP = d4;
        }
        if (this.aR < -d4) {
            this.aR = -d4;
        }
        if (this.aR > d4) {
            this.aR = d4;
        }
        if (this.aX) {
            this.aP *= 0.5;
            this.aQ *= 0.5;
            this.aR *= 0.5;
        }
        this.b(this.aP, this.aQ, this.aR);
        double d16 = Math.sqrt(this.aP * this.aP + this.aR * this.aR);
        if (d16 > 0.15) {
            double d17 = Math.cos((double)this.aS * Math.PI / 180.0);
            d3 = Math.sin((double)this.aS * Math.PI / 180.0);
            int n3 = 0;
            while ((double)n3 < 1.0 + d16 * 60.0) {
                double d18;
                double d19;
                double d20 = this.bs.nextFloat() * 2.0f - 1.0f;
                double d21 = (double)(this.bs.nextInt(2) * 2 - 1) * 0.7;
                if (this.bs.nextBoolean()) {
                    d19 = this.aM - d17 * d20 * 0.8 + d3 * d21;
                    d18 = this.aO - d3 * d20 * 0.8 - d17 * d21;
                    this.aI.a("splash", d19, this.aN - 0.125, d18, this.aP, this.aQ, this.aR);
                } else {
                    d19 = this.aM + d17 + d3 * d20 * 0.7;
                    d18 = this.aO + d3 - d17 * d20 * 0.7;
                    this.aI.a("splash", d19, this.aN - 0.125, d18, this.aP, this.aQ, this.aR);
                }
                ++n3;
            }
        }
        if (this.aY && d16 > 0.15) {
            if (!this.aI.B) {
                int n4;
                this.K();
                for (n4 = 0; n4 < 3; ++n4) {
                    this.a(uu.y.bn, 1, 0.0f);
                }
                for (n4 = 0; n4 < 2; ++n4) {
                    this.a(gm.B.bf, 1, 0.0f);
                }
            }
        } else {
            this.aP *= (double)0.99f;
            this.aQ *= (double)0.95f;
            this.aR *= (double)0.99f;
        }
        this.aT = 0.0f;
        double d22 = this.aS;
        d3 = this.aJ - this.aM;
        double d23 = this.aL - this.aO;
        if (d3 * d3 + d23 * d23 > 0.001) {
            d22 = (float)(Math.atan2(d23, d3) * 180.0 / Math.PI);
        }
        for (d2 = d22 - (double)this.aS; d2 >= 180.0; d2 -= 360.0) {
        }
        while (d2 < -180.0) {
            d2 += 360.0;
        }
        if (d2 > 20.0) {
            d2 = 20.0;
        }
        if (d2 < -20.0) {
            d2 = -20.0;
        }
        this.aS = (float)((double)this.aS + d2);
        this.c(this.aS, this.aT);
        List list = this.aI.b(this, this.aW.b(0.2f, 0.0, 0.2f));
        if (list != null && list.size() > 0) {
            for (int i3 = 0; i3 < list.size(); ++i3) {
                sn sn2 = (sn)list.get(i3);
                if (sn2 == this.aG || !sn2.i_() || !(sn2 instanceof fz)) continue;
                sn2.h(this);
            }
        }
        for (int i4 = 0; i4 < 4; ++i4) {
            int n5;
            int n6;
            int n7 = in.b(this.aM + ((double)(i4 % 2) - 0.5) * 0.8);
            if (this.aI.a(n7, n6 = in.b(this.aN), n5 = in.b(this.aO + ((double)(i4 / 2) - 0.5) * 0.8)) != uu.aT.bn) continue;
            this.aI.f(n7, n6, n5, 0);
        }
        if (this.aG != null && this.aG.be) {
            this.aG = null;
        }
    }

    public void l_() {
        if (this.aG == null) {
            return;
        }
        double d2 = Math.cos((double)this.aS * Math.PI / 180.0) * 0.4;
        double d3 = Math.sin((double)this.aS * Math.PI / 180.0) * 0.4;
        this.aG.e(this.aM + d2, this.aN + this.m() + this.aG.I(), this.aO + d3);
    }

    protected void b(nu nu2) {
    }

    protected void a(nu nu2) {
    }

    public float x_() {
        return 0.0f;
    }

    public boolean a(gs gs2) {
        if (this.aG != null && this.aG instanceof gs && this.aG != gs2) {
            return true;
        }
        if (!this.aI.B) {
            gs2.i(this);
        }
        return true;
    }
}

