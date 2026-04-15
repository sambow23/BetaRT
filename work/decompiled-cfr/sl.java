/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class sl
extends sn {
    private int d = -1;
    private int e = -1;
    private int f = -1;
    private int g = 0;
    private int h = 0;
    private boolean i = false;
    public boolean a = false;
    public int b = 0;
    public ls c;
    private int j;
    private int k = 0;

    public sl(fd fd2) {
        super(fd2);
        this.b(0.5f, 0.5f);
    }

    public sl(fd fd2, double d2, double d3, double d4) {
        super(fd2);
        this.b(0.5f, 0.5f);
        this.e(d2, d3, d4);
        this.bf = 0.0f;
    }

    public sl(fd fd2, ls ls2) {
        super(fd2);
        this.c = ls2;
        this.a = ls2 instanceof gs;
        this.b(0.5f, 0.5f);
        this.c(ls2.aM, ls2.aN + (double)ls2.w(), ls2.aO, ls2.aS, ls2.aT);
        this.aM -= (double)(in.b(this.aS / 180.0f * (float)Math.PI) * 0.16f);
        this.aN -= (double)0.1f;
        this.aO -= (double)(in.a(this.aS / 180.0f * (float)Math.PI) * 0.16f);
        this.e(this.aM, this.aN, this.aO);
        this.bf = 0.0f;
        this.aP = -in.a(this.aS / 180.0f * (float)Math.PI) * in.b(this.aT / 180.0f * (float)Math.PI);
        this.aR = in.b(this.aS / 180.0f * (float)Math.PI) * in.b(this.aT / 180.0f * (float)Math.PI);
        this.aQ = -in.a(this.aT / 180.0f * (float)Math.PI);
        this.a(this.aP, this.aQ, this.aR, 1.5f, 1.0f);
    }

    protected void b() {
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
        this.j = 0;
    }

    public void a(double d2, double d3, double d4) {
        this.aP = d2;
        this.aQ = d3;
        this.aR = d4;
        if (this.aV == 0.0f && this.aU == 0.0f) {
            float f2 = in.a(d2 * d2 + d4 * d4);
            this.aU = this.aS = (float)(Math.atan2(d2, d4) * 180.0 / 3.1415927410125732);
            this.aV = this.aT = (float)(Math.atan2(d3, f2) * 180.0 / 3.1415927410125732);
            this.aV = this.aT;
            this.aU = this.aS;
            this.c(this.aM, this.aN, this.aO, this.aS, this.aT);
            this.j = 0;
        }
    }

    public void w_() {
        float f2;
        Object object;
        int n2;
        super.w_();
        if (this.aV == 0.0f && this.aU == 0.0f) {
            float f3 = in.a(this.aP * this.aP + this.aR * this.aR);
            this.aU = this.aS = (float)(Math.atan2(this.aP, this.aR) * 180.0 / 3.1415927410125732);
            this.aV = this.aT = (float)(Math.atan2(this.aQ, f3) * 180.0 / 3.1415927410125732);
        }
        if ((n2 = this.aI.a(this.d, this.e, this.f)) > 0) {
            uu.m[n2].a((xp)this.aI, this.d, this.e, this.f);
            object = uu.m[n2].e(this.aI, this.d, this.e, this.f);
            if (object != null && ((eq)object).a(bt.b(this.aM, this.aN, this.aO))) {
                this.i = true;
            }
        }
        if (this.b > 0) {
            --this.b;
        }
        if (this.i) {
            n2 = this.aI.a(this.d, this.e, this.f);
            int n3 = this.aI.e(this.d, this.e, this.f);
            if (n2 != this.g || n3 != this.h) {
                this.i = false;
                this.aP *= (double)(this.bs.nextFloat() * 0.2f);
                this.aQ *= (double)(this.bs.nextFloat() * 0.2f);
                this.aR *= (double)(this.bs.nextFloat() * 0.2f);
                this.j = 0;
                this.k = 0;
                return;
            }
            ++this.j;
            if (this.j == 1200) {
                this.K();
            }
            return;
        }
        ++this.k;
        bt bt2 = bt.b(this.aM, this.aN, this.aO);
        object = bt.b(this.aM + this.aP, this.aN + this.aQ, this.aO + this.aR);
        vf vf2 = this.aI.a(bt2, (bt)object, false, true);
        bt2 = bt.b(this.aM, this.aN, this.aO);
        object = bt.b(this.aM + this.aP, this.aN + this.aQ, this.aO + this.aR);
        if (vf2 != null) {
            object = bt.b(vf2.f.a, vf2.f.b, vf2.f.c);
        }
        sn sn2 = null;
        List list = this.aI.b(this, this.aW.a(this.aP, this.aQ, this.aR).b(1.0, 1.0, 1.0));
        double d2 = 0.0;
        for (int i2 = 0; i2 < list.size(); ++i2) {
            double d3;
            eq eq2;
            vf vf3;
            sn sn3 = (sn)list.get(i2);
            if (!sn3.h_() || sn3 == this.c && this.k < 5 || (vf3 = (eq2 = sn3.aW.b(f2 = 0.3f, f2, f2)).a(bt2, (bt)object)) == null || !((d3 = bt2.c(vf3.f)) < d2) && d2 != 0.0) continue;
            sn2 = sn3;
            d2 = d3;
        }
        if (sn2 != null) {
            vf2 = new vf(sn2);
        }
        if (vf2 != null) {
            if (vf2.g != null) {
                if (vf2.g.a(this.c, 4)) {
                    this.aI.a(this, "random.drr", 1.0f, 1.2f / (this.bs.nextFloat() * 0.2f + 0.9f));
                    this.K();
                } else {
                    this.aP *= (double)-0.1f;
                    this.aQ *= (double)-0.1f;
                    this.aR *= (double)-0.1f;
                    this.aS += 180.0f;
                    this.aU += 180.0f;
                    this.k = 0;
                }
            } else {
                this.d = vf2.b;
                this.e = vf2.c;
                this.f = vf2.d;
                this.g = this.aI.a(this.d, this.e, this.f);
                this.h = this.aI.e(this.d, this.e, this.f);
                this.aP = (float)(vf2.f.a - this.aM);
                this.aQ = (float)(vf2.f.b - this.aN);
                this.aR = (float)(vf2.f.c - this.aO);
                float f4 = in.a(this.aP * this.aP + this.aQ * this.aQ + this.aR * this.aR);
                this.aM -= this.aP / (double)f4 * (double)0.05f;
                this.aN -= this.aQ / (double)f4 * (double)0.05f;
                this.aO -= this.aR / (double)f4 * (double)0.05f;
                this.aI.a(this, "random.drr", 1.0f, 1.2f / (this.bs.nextFloat() * 0.2f + 0.9f));
                this.i = true;
                this.b = 7;
            }
        }
        this.aM += this.aP;
        this.aN += this.aQ;
        this.aO += this.aR;
        float f5 = in.a(this.aP * this.aP + this.aR * this.aR);
        this.aS = (float)(Math.atan2(this.aP, this.aR) * 180.0 / 3.1415927410125732);
        this.aT = (float)(Math.atan2(this.aQ, f5) * 180.0 / 3.1415927410125732);
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
        float f6 = 0.99f;
        f2 = 0.03f;
        if (this.ag()) {
            for (int i3 = 0; i3 < 4; ++i3) {
                float f7 = 0.25f;
                this.aI.a("bubble", this.aM - this.aP * (double)f7, this.aN - this.aQ * (double)f7, this.aO - this.aR * (double)f7, this.aP, this.aQ, this.aR);
            }
            f6 = 0.8f;
        }
        this.aP *= (double)f6;
        this.aQ *= (double)f6;
        this.aR *= (double)f6;
        this.aQ -= (double)f2;
        this.e(this.aM, this.aN, this.aO);
    }

    public void b(nu nu2) {
        nu2.a("xTile", (short)this.d);
        nu2.a("yTile", (short)this.e);
        nu2.a("zTile", (short)this.f);
        nu2.a("inTile", (byte)this.g);
        nu2.a("inData", (byte)this.h);
        nu2.a("shake", (byte)this.b);
        nu2.a("inGround", (byte)(this.i ? 1 : 0));
        nu2.a("player", this.a);
    }

    public void a(nu nu2) {
        this.d = nu2.d("xTile");
        this.e = nu2.d("yTile");
        this.f = nu2.d("zTile");
        this.g = nu2.c("inTile") & 0xFF;
        this.h = nu2.c("inData") & 0xFF;
        this.b = nu2.c("shake") & 0xFF;
        this.i = nu2.c("inGround") == 1;
        this.a = nu2.m("player");
    }

    public void b(gs gs2) {
        if (this.aI.B) {
            return;
        }
        if (this.i && this.a && this.b <= 0 && gs2.c.a(new iz(gm.j, 1))) {
            this.aI.a(this, "random.pop", 0.2f, ((this.bs.nextFloat() - this.bs.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            gs2.b(this, 1);
            this.K();
        }
    }

    public float x_() {
        return 0.0f;
    }
}

