/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class lx
extends sn {
    private int d = -1;
    private int e = -1;
    private int f = -1;
    private int g = 0;
    private boolean h = false;
    public int a = 0;
    public gs b;
    private int i;
    private int j = 0;
    private int k = 0;
    public sn c = null;
    private int l;
    private double m;
    private double n;
    private double o;
    private double p;
    private double q;
    private double r;
    private double s;
    private double t;

    public lx(fd fd2) {
        super(fd2);
        this.b(0.25f, 0.25f);
        this.bM = true;
    }

    public lx(fd fd2, double d2, double d3, double d4) {
        this(fd2);
        this.e(d2, d3, d4);
        this.bM = true;
    }

    public lx(fd fd2, gs gs2) {
        super(fd2);
        this.bM = true;
        this.b = gs2;
        this.b.D = this;
        this.b(0.25f, 0.25f);
        this.c(gs2.aM, gs2.aN + 1.62 - (double)gs2.bf, gs2.aO, gs2.aS, gs2.aT);
        this.aM -= (double)(in.b(this.aS / 180.0f * (float)Math.PI) * 0.16f);
        this.aN -= (double)0.1f;
        this.aO -= (double)(in.a(this.aS / 180.0f * (float)Math.PI) * 0.16f);
        this.e(this.aM, this.aN, this.aO);
        this.bf = 0.0f;
        float f2 = 0.4f;
        this.aP = -in.a(this.aS / 180.0f * (float)Math.PI) * in.b(this.aT / 180.0f * (float)Math.PI) * f2;
        this.aR = in.b(this.aS / 180.0f * (float)Math.PI) * in.b(this.aT / 180.0f * (float)Math.PI) * f2;
        this.aQ = -in.a(this.aT / 180.0f * (float)Math.PI) * f2;
        this.a(this.aP, this.aQ, this.aR, 1.5f, 1.0f);
    }

    protected void b() {
    }

    public boolean a(double d2) {
        double d3 = this.aW.c() * 4.0;
        return d2 < (d3 *= 64.0) * d3;
    }

    public void a(double d2, double d3, double d4, float f2, float f3) {
        float f4 = in.a(d2 * d2 + d3 * d3 + d4 * d4);
        d2 /= (double)f4;
        d3 /= (double)f4;
        d4 /= (double)f4;
        d2 += this.bs.nextGaussian() * (double)0.0075f * (double)f3;
        d3 += this.bs.nextGaussian() * (double)0.0075f * (double)f3;
        d4 += this.bs.nextGaussian() * (double)0.0075f * (double)f3;
        this.aP = d2 *= (double)f2;
        this.aQ = d3 *= (double)f2;
        this.aR = d4 *= (double)f2;
        float f5 = in.a(d2 * d2 + d4 * d4);
        this.aU = this.aS = (float)(Math.atan2(d2, d4) * 180.0 / 3.1415927410125732);
        this.aV = this.aT = (float)(Math.atan2(d3, f5) * 180.0 / 3.1415927410125732);
        this.i = 0;
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2) {
        this.m = d2;
        this.n = d3;
        this.o = d4;
        this.p = f2;
        this.q = f3;
        this.l = n2;
        this.aP = this.r;
        this.aQ = this.s;
        this.aR = this.t;
    }

    public void a(double d2, double d3, double d4) {
        this.r = this.aP = d2;
        this.s = this.aQ = d3;
        this.t = this.aR = d4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public void w_() {
        int n2;
        block36: {
            super.w_();
            if (this.l > 0) {
                double d2;
                double d3 = this.aM + (this.m - this.aM) / (double)this.l;
                double d4 = this.aN + (this.n - this.aN) / (double)this.l;
                double d5 = this.aO + (this.o - this.aO) / (double)this.l;
                for (d2 = this.p - (double)this.aS; d2 < -180.0; d2 += 360.0) {
                }
                while (true) {
                    if (!(d2 >= 180.0)) {
                        this.aS = (float)((double)this.aS + d2 / (double)this.l);
                        this.aT = (float)((double)this.aT + (this.q - (double)this.aT) / (double)this.l);
                        --this.l;
                        this.e(d3, d4, d5);
                        this.c(this.aS, this.aT);
                        return;
                    }
                    d2 -= 360.0;
                }
            }
            if (!this.aI.B) {
                iz iz2 = this.b.G();
                if (this.b.be || !this.b.W() || iz2 == null || iz2.a() != gm.aP || this.g(this.b) > 1024.0) {
                    this.K();
                    this.b.D = null;
                    return;
                }
                if (this.c != null) {
                    if (!this.c.be) {
                        this.aM = this.c.aM;
                        this.aN = this.c.aW.b + (double)this.c.bh * 0.8;
                        this.aO = this.c.aO;
                        return;
                    }
                    this.c = null;
                }
            }
            if (this.a > 0) {
                --this.a;
            }
            if (this.h) {
                int n3 = this.aI.a(this.d, this.e, this.f);
                if (n3 != this.g) {
                    this.h = false;
                    this.aP *= (double)(this.bs.nextFloat() * 0.2f);
                    this.aQ *= (double)(this.bs.nextFloat() * 0.2f);
                    this.aR *= (double)(this.bs.nextFloat() * 0.2f);
                    this.i = 0;
                    this.j = 0;
                    break block36;
                } else {
                    ++this.i;
                    if (this.i == 1200) {
                        this.K();
                    }
                    return;
                }
            }
            ++this.j;
        }
        bt bt2 = bt.b(this.aM, this.aN, this.aO);
        bt bt3 = bt.b(this.aM + this.aP, this.aN + this.aQ, this.aO + this.aR);
        vf vf2 = this.aI.a(bt2, bt3);
        bt2 = bt.b(this.aM, this.aN, this.aO);
        bt3 = bt.b(this.aM + this.aP, this.aN + this.aQ, this.aO + this.aR);
        if (vf2 != null) {
            bt3 = bt.b(vf2.f.a, vf2.f.b, vf2.f.c);
        }
        sn sn2 = null;
        List list = this.aI.b(this, this.aW.a(this.aP, this.aQ, this.aR).b(1.0, 1.0, 1.0));
        double d6 = 0.0;
        for (int i2 = 0; i2 < list.size(); ++i2) {
            double d7;
            float f2;
            eq eq2;
            vf vf3;
            sn sn3 = (sn)list.get(i2);
            if (!sn3.h_() || sn3 == this.b && this.j < 5 || (vf3 = (eq2 = sn3.aW.b(f2 = 0.3f, f2, f2)).a(bt2, bt3)) == null || !((d7 = bt2.c(vf3.f)) < d6) && d6 != 0.0) continue;
            sn2 = sn3;
            d6 = d7;
        }
        if (sn2 != null) {
            vf2 = new vf(sn2);
        }
        if (vf2 != null) {
            if (vf2.g != null) {
                if (vf2.g.a(this.b, 0)) {
                    this.c = vf2.g;
                }
            } else {
                this.h = true;
            }
        }
        if (this.h) {
            return;
        }
        this.b(this.aP, this.aQ, this.aR);
        float f3 = in.a(this.aP * this.aP + this.aR * this.aR);
        this.aS = (float)(Math.atan2(this.aP, this.aR) * 180.0 / 3.1415927410125732);
        this.aT = (float)(Math.atan2(this.aQ, f3) * 180.0 / 3.1415927410125732);
        while (this.aT - this.aV < -180.0f) {
            this.aV -= 360.0f;
        }
        while (this.aT - this.aV >= 180.0f) {
            this.aV += 360.0f;
        }
        while (this.aS - this.aU < -180.0f) {
            this.aU -= 360.0f;
        }
        while (this.aS - this.aU >= 180.0f) {
            this.aU += 360.0f;
        }
        this.aT = this.aV + (this.aT - this.aV) * 0.2f;
        this.aS = this.aU + (this.aS - this.aU) * 0.2f;
        float f4 = 0.92f;
        if (this.aX || this.aY) {
            f4 = 0.5f;
        }
        int n4 = 5;
        double d8 = 0.0;
        for (n2 = 0; n2 < n4; ++n2) {
            double d9 = this.aW.b + (this.aW.e - this.aW.b) * (double)(n2 + 0) / (double)n4 - 0.125 + 0.125;
            double d10 = this.aW.b + (this.aW.e - this.aW.b) * (double)(n2 + 1) / (double)n4 - 0.125 + 0.125;
            eq eq3 = eq.b(this.aW.a, d9, this.aW.c, this.aW.d, d10, this.aW.f);
            if (!this.aI.b(eq3, ln.g)) continue;
            d8 += 1.0 / (double)n4;
        }
        if (d8 > 0.0) {
            if (this.k > 0) {
                --this.k;
            } else {
                n2 = 500;
                if (this.aI.t(in.b(this.aM), in.b(this.aN) + 1, in.b(this.aO))) {
                    n2 = 300;
                }
                if (this.bs.nextInt(n2) == 0) {
                    float f5;
                    this.k = this.bs.nextInt(30) + 10;
                    this.aQ -= (double)0.2f;
                    this.aI.a(this, "random.splash", 0.25f, 1.0f + (this.bs.nextFloat() - this.bs.nextFloat()) * 0.4f);
                    float f6 = in.b(this.aW.b);
                    int n5 = 0;
                    while ((float)n5 < 1.0f + this.bg * 20.0f) {
                        float f7 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                        f5 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                        this.aI.a("bubble", this.aM + (double)f7, f6 + 1.0f, this.aO + (double)f5, this.aP, this.aQ - (double)(this.bs.nextFloat() * 0.2f), this.aR);
                        ++n5;
                    }
                    n5 = 0;
                    while ((float)n5 < 1.0f + this.bg * 20.0f) {
                        float f8 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                        f5 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                        this.aI.a("splash", this.aM + (double)f8, f6 + 1.0f, this.aO + (double)f5, this.aP, this.aQ, this.aR);
                        ++n5;
                    }
                }
            }
        }
        if (this.k > 0) {
            this.aQ -= (double)(this.bs.nextFloat() * this.bs.nextFloat() * this.bs.nextFloat()) * 0.2;
        }
        double d11 = d8 * 2.0 - 1.0;
        this.aQ += (double)0.04f * d11;
        if (d8 > 0.0) {
            f4 = (float)((double)f4 * 0.9);
            this.aQ *= 0.8;
        }
        this.aP *= (double)f4;
        this.aQ *= (double)f4;
        this.aR *= (double)f4;
        this.e(this.aM, this.aN, this.aO);
    }

    public void b(nu nu2) {
        nu2.a("xTile", (short)this.d);
        nu2.a("yTile", (short)this.e);
        nu2.a("zTile", (short)this.f);
        nu2.a("inTile", (byte)this.g);
        nu2.a("shake", (byte)this.a);
        nu2.a("inGround", (byte)(this.h ? 1 : 0));
    }

    public void a(nu nu2) {
        this.d = nu2.d("xTile");
        this.e = nu2.d("yTile");
        this.f = nu2.d("zTile");
        this.g = nu2.c("inTile") & 0xFF;
        this.a = nu2.c("shake") & 0xFF;
        this.h = nu2.c("inGround") == 1;
    }

    public float x_() {
        return 0.0f;
    }

    public int k() {
        int n2 = 0;
        if (this.c != null) {
            double d2 = this.b.aM - this.aM;
            double d3 = this.b.aN - this.aN;
            double d4 = this.b.aO - this.aO;
            double d5 = in.a(d2 * d2 + d3 * d3 + d4 * d4);
            double d6 = 0.1;
            this.c.aP += d2 * d6;
            this.c.aQ += d3 * d6 + (double)in.a(d5) * 0.08;
            this.c.aR += d4 * d6;
            n2 = 3;
        } else if (this.k > 0) {
            hl hl2 = new hl(this.aI, this.aM, this.aN, this.aO, new iz(gm.aS));
            double d7 = this.b.aM - this.aM;
            double d8 = this.b.aN - this.aN;
            double d9 = this.b.aO - this.aO;
            double d10 = in.a(d7 * d7 + d8 * d8 + d9 * d9);
            double d11 = 0.1;
            hl2.aP = d7 * d11;
            hl2.aQ = d8 * d11 + (double)in.a(d10) * 0.08;
            hl2.aR = d9 * d11;
            this.aI.b(hl2);
            this.b.a(jl.B, 1);
            n2 = 1;
        }
        if (this.h) {
            n2 = 2;
        }
        this.K();
        this.b.D = null;
        return n2;
    }
}

