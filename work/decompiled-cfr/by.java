/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class by
extends sn {
    private int b = -1;
    private int c = -1;
    private int d = -1;
    private int e = 0;
    private boolean f = false;
    public int a = 0;
    private ls g;
    private int h;
    private int i = 0;

    public by(fd fd2) {
        super(fd2);
        this.b(0.25f, 0.25f);
    }

    protected void b() {
    }

    public boolean a(double d2) {
        double d3 = this.aW.c() * 4.0;
        return d2 < (d3 *= 64.0) * d3;
    }

    public by(fd fd2, ls ls2) {
        super(fd2);
        this.g = ls2;
        this.b(0.25f, 0.25f);
        this.c(ls2.aM, ls2.aN + (double)ls2.w(), ls2.aO, ls2.aS, ls2.aT);
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

    public by(fd fd2, double d2, double d3, double d4) {
        super(fd2);
        this.h = 0;
        this.b(0.25f, 0.25f);
        this.e(d2, d3, d4);
        this.bf = 0.0f;
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
        this.h = 0;
    }

    public void a(double d2, double d3, double d4) {
        this.aP = d2;
        this.aQ = d3;
        this.aR = d4;
        if (this.aV == 0.0f && this.aU == 0.0f) {
            float f2 = in.a(d2 * d2 + d4 * d4);
            this.aU = this.aS = (float)(Math.atan2(d2, d4) * 180.0 / 3.1415927410125732);
            this.aV = this.aT = (float)(Math.atan2(d3, f2) * 180.0 / 3.1415927410125732);
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public void w_() {
        block18: {
            this.bl = this.aM;
            this.bm = this.aN;
            this.bn = this.aO;
            super.w_();
            if (this.a > 0) {
                --this.a;
            }
            if (this.f) {
                int n2 = this.aI.a(this.b, this.c, this.d);
                if (n2 != this.e) {
                    this.f = false;
                    this.aP *= (double)(this.bs.nextFloat() * 0.2f);
                    this.aQ *= (double)(this.bs.nextFloat() * 0.2f);
                    this.aR *= (double)(this.bs.nextFloat() * 0.2f);
                    this.h = 0;
                    this.i = 0;
                    break block18;
                } else {
                    ++this.h;
                    if (this.h == 1200) {
                        this.K();
                    }
                    return;
                }
            }
            ++this.i;
        }
        bt bt2 = bt.b(this.aM, this.aN, this.aO);
        bt bt3 = bt.b(this.aM + this.aP, this.aN + this.aQ, this.aO + this.aR);
        vf vf2 = this.aI.a(bt2, bt3);
        bt2 = bt.b(this.aM, this.aN, this.aO);
        bt3 = bt.b(this.aM + this.aP, this.aN + this.aQ, this.aO + this.aR);
        if (vf2 != null) {
            bt3 = bt.b(vf2.f.a, vf2.f.b, vf2.f.c);
        }
        if (!this.aI.B) {
            sn sn2 = null;
            List list = this.aI.b(this, this.aW.a(this.aP, this.aQ, this.aR).b(1.0, 1.0, 1.0));
            double d2 = 0.0;
            for (int i2 = 0; i2 < list.size(); ++i2) {
                double d3;
                float f2;
                eq eq2;
                vf vf3;
                sn sn3 = (sn)list.get(i2);
                if (!sn3.h_() || sn3 == this.g && this.i < 5 || (vf3 = (eq2 = sn3.aW.b(f2 = 0.3f, f2, f2)).a(bt2, bt3)) == null || !((d3 = bt2.c(vf3.f)) < d2) && d2 != 0.0) continue;
                sn2 = sn3;
                d2 = d3;
            }
            if (sn2 != null) {
                vf2 = new vf(sn2);
            }
        }
        if (vf2 != null) {
            if (vf2.g == null || vf2.g.a(this.g, 0)) {
                // empty if block
            }
            for (int i3 = 0; i3 < 8; ++i3) {
                this.aI.a("snowballpoof", this.aM, this.aN, this.aO, 0.0, 0.0, 0.0);
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
        float f4 = 0.99f;
        float f5 = 0.03f;
        if (this.ag()) {
            for (int i4 = 0; i4 < 4; ++i4) {
                float f6 = 0.25f;
                this.aI.a("bubble", this.aM - this.aP * (double)f6, this.aN - this.aQ * (double)f6, this.aO - this.aR * (double)f6, this.aP, this.aQ, this.aR);
            }
            f4 = 0.8f;
        }
        this.aP *= (double)f4;
        this.aQ *= (double)f4;
        this.aR *= (double)f4;
        this.aQ -= (double)f5;
        this.e(this.aM, this.aN, this.aO);
    }

    public void b(nu nu2) {
        nu2.a("xTile", (short)this.b);
        nu2.a("yTile", (short)this.c);
        nu2.a("zTile", (short)this.d);
        nu2.a("inTile", (byte)this.e);
        nu2.a("shake", (byte)this.a);
        nu2.a("inGround", (byte)(this.f ? 1 : 0));
    }

    public void a(nu nu2) {
        this.b = nu2.d("xTile");
        this.c = nu2.d("yTile");
        this.d = nu2.d("zTile");
        this.e = nu2.c("inTile") & 0xFF;
        this.a = nu2.c("shake") & 0xFF;
        this.f = nu2.c("inGround") == 1;
    }

    public void b(gs gs2) {
        if (this.f && this.g == gs2 && this.a <= 0 && gs2.c.a(new iz(gm.j, 1))) {
            this.aI.a(this, "random.pop", 0.2f, ((this.bs.nextFloat() - this.bs.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            gs2.b(this, 1);
            this.K();
        }
    }

    public float x_() {
        return 0.0f;
    }
}

