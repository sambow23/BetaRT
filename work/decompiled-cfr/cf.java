/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class cf
extends sn {
    private int f = -1;
    private int g = -1;
    private int h = -1;
    private int i = 0;
    private boolean j = false;
    public int a = 0;
    public ls b;
    private int k;
    private int l = 0;
    public double c;
    public double d;
    public double e;

    public cf(fd fd2) {
        super(fd2);
        this.b(1.0f, 1.0f);
    }

    protected void b() {
    }

    public boolean a(double d2) {
        double d3 = this.aW.c() * 4.0;
        return d2 < (d3 *= 64.0) * d3;
    }

    public cf(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        super(fd2);
        this.b(1.0f, 1.0f);
        this.c(d2, d3, d4, this.aS, this.aT);
        this.e(d2, d3, d4);
        double d8 = in.a(d5 * d5 + d6 * d6 + d7 * d7);
        this.c = d5 / d8 * 0.1;
        this.d = d6 / d8 * 0.1;
        this.e = d7 / d8 * 0.1;
    }

    public cf(fd fd2, ls ls2, double d2, double d3, double d4) {
        super(fd2);
        this.b = ls2;
        this.b(1.0f, 1.0f);
        this.c(ls2.aM, ls2.aN, ls2.aO, ls2.aS, ls2.aT);
        this.e(this.aM, this.aN, this.aO);
        this.bf = 0.0f;
        this.aR = 0.0;
        this.aQ = 0.0;
        this.aP = 0.0;
        double d5 = in.a((d2 += this.bs.nextGaussian() * 0.4) * d2 + (d3 += this.bs.nextGaussian() * 0.4) * d3 + (d4 += this.bs.nextGaussian() * 0.4) * d4);
        this.c = d2 / d5 * 0.1;
        this.d = d3 / d5 * 0.1;
        this.e = d4 / d5 * 0.1;
    }

    /*
     * Enabled aggressive block sorting
     */
    public void w_() {
        block17: {
            super.w_();
            this.bv = 10;
            if (this.a > 0) {
                --this.a;
            }
            if (this.j) {
                int n2 = this.aI.a(this.f, this.g, this.h);
                if (n2 != this.i) {
                    this.j = false;
                    this.aP *= (double)(this.bs.nextFloat() * 0.2f);
                    this.aQ *= (double)(this.bs.nextFloat() * 0.2f);
                    this.aR *= (double)(this.bs.nextFloat() * 0.2f);
                    this.k = 0;
                    this.l = 0;
                    break block17;
                } else {
                    ++this.k;
                    if (this.k == 1200) {
                        this.K();
                    }
                    return;
                }
            }
            ++this.l;
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
        double d2 = 0.0;
        for (int i2 = 0; i2 < list.size(); ++i2) {
            double d3;
            float f2;
            eq eq2;
            vf vf3;
            sn sn3 = (sn)list.get(i2);
            if (!sn3.h_() || sn3 == this.b && this.l < 25 || (vf3 = (eq2 = sn3.aW.b(f2 = 0.3f, f2, f2)).a(bt2, bt3)) == null || !((d3 = bt2.c(vf3.f)) < d2) && d2 != 0.0) continue;
            sn2 = sn3;
            d2 = d3;
        }
        if (sn2 != null) {
            vf2 = new vf(sn2);
        }
        if (vf2 != null) {
            if (!this.aI.B) {
                if (vf2.g == null || vf2.g.a(this.b, 0)) {
                    // empty if block
                }
                this.aI.a(null, this.aM, this.aN, this.aO, 1.0f, true);
            }
            this.K();
        }
        this.aM += this.aP;
        this.aN += this.aQ;
        this.aO += this.aR;
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
        float f4 = 0.95f;
        if (this.ag()) {
            for (int i3 = 0; i3 < 4; ++i3) {
                float f5 = 0.25f;
                this.aI.a("bubble", this.aM - this.aP * (double)f5, this.aN - this.aQ * (double)f5, this.aO - this.aR * (double)f5, this.aP, this.aQ, this.aR);
            }
            f4 = 0.8f;
        }
        this.aP += this.c;
        this.aQ += this.d;
        this.aR += this.e;
        this.aP *= (double)f4;
        this.aQ *= (double)f4;
        this.aR *= (double)f4;
        this.aI.a("smoke", this.aM, this.aN + 0.5, this.aO, 0.0, 0.0, 0.0);
        this.e(this.aM, this.aN, this.aO);
    }

    public void b(nu nu2) {
        nu2.a("xTile", (short)this.f);
        nu2.a("yTile", (short)this.g);
        nu2.a("zTile", (short)this.h);
        nu2.a("inTile", (byte)this.i);
        nu2.a("shake", (byte)this.a);
        nu2.a("inGround", (byte)(this.j ? 1 : 0));
    }

    public void a(nu nu2) {
        this.f = nu2.d("xTile");
        this.g = nu2.d("yTile");
        this.h = nu2.d("zTile");
        this.i = nu2.c("inTile") & 0xFF;
        this.a = nu2.c("shake") & 0xFF;
        this.j = nu2.c("inGround") == 1;
    }

    public boolean h_() {
        return true;
    }

    public float m_() {
        return 1.0f;
    }

    public boolean a(sn sn2, int n2) {
        this.ai();
        if (sn2 != null) {
            bt bt2 = sn2.ac();
            if (bt2 != null) {
                this.aP = bt2.a;
                this.aQ = bt2.b;
                this.aR = bt2.c;
                this.c = this.aP * 0.1;
                this.d = this.aQ * 0.1;
                this.e = this.aR * 0.1;
            }
            return true;
        }
        return false;
    }

    public float x_() {
        return 0.0f;
    }
}

