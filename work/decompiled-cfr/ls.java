/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public abstract class ls
extends sn {
    public int E = 20;
    public float F;
    public float G;
    public float H = 0.0f;
    public float I = 0.0f;
    protected float J;
    protected float K;
    protected float L;
    protected float M;
    protected boolean N = true;
    protected String O = "/mob/char.png";
    protected boolean P = true;
    protected float Q = 0.0f;
    protected String R = null;
    protected float S = 1.0f;
    protected int T = 0;
    protected float U = 0.0f;
    public boolean V = false;
    public float W;
    public float X;
    public int Y = 10;
    public int Z;
    private int a;
    public int aa;
    public int ab;
    public float ac = 0.0f;
    public int ad = 0;
    public int ae = 0;
    public float af;
    public float ag;
    protected boolean ah = false;
    public int ai = -1;
    public float aj = (float)(Math.random() * (double)0.9f + (double)0.1f);
    public float ak;
    public float al;
    public float am;
    protected int an;
    protected double ao;
    protected double ap;
    protected double aq;
    protected double ar;
    protected double as;
    float at = 0.0f;
    protected int au = 0;
    protected int av = 0;
    protected float aw;
    protected float ax;
    protected float ay;
    protected boolean az = false;
    protected float aA = 0.0f;
    protected float aB = 0.7f;
    private sn b;
    protected int aC = 0;

    public ls(fd fd2) {
        super(fd2);
        this.aF = true;
        this.G = (float)(Math.random() + 1.0) * 0.01f;
        this.e(this.aM, this.aN, this.aO);
        this.F = (float)Math.random() * 12398.0f;
        this.aS = (float)(Math.random() * 3.1415927410125732 * 2.0);
        this.bp = 0.5f;
    }

    protected void b() {
    }

    public boolean e(sn sn2) {
        return this.aI.a(bt.b(this.aM, this.aN + (double)this.w(), this.aO), bt.b(sn2.aM, sn2.aN + (double)sn2.w(), sn2.aO)) == null;
    }

    public String q_() {
        return this.O;
    }

    public boolean h_() {
        return !this.be;
    }

    public boolean i_() {
        return !this.be;
    }

    public float w() {
        return this.bh * 0.85f;
    }

    public int e() {
        return 80;
    }

    public void T() {
        String string = this.g();
        if (string != null) {
            this.aI.a(this, string, this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
        }
    }

    public void U() {
        int n2;
        this.W = this.X;
        super.U();
        if (this.bs.nextInt(1000) < this.a++) {
            this.a = -this.e();
            this.T();
        }
        if (this.W() && this.L()) {
            this.a((sn)null, 1);
        }
        if (this.bC || this.aI.B) {
            this.bv = 0;
        }
        if (this.W() && this.a(ln.g) && !this.d_()) {
            --this.bz;
            if (this.bz == -20) {
                this.bz = 0;
                for (n2 = 0; n2 < 8; ++n2) {
                    float f2 = this.bs.nextFloat() - this.bs.nextFloat();
                    float f3 = this.bs.nextFloat() - this.bs.nextFloat();
                    float f4 = this.bs.nextFloat() - this.bs.nextFloat();
                    this.aI.a("bubble", this.aM + (double)f2, this.aN + (double)f3, this.aO + (double)f4, this.aP, this.aQ, this.aR);
                }
                this.a((sn)null, 2);
            }
            this.bv = 0;
        } else {
            this.bz = this.bw;
        }
        this.af = this.ag;
        if (this.ae > 0) {
            --this.ae;
        }
        if (this.aa > 0) {
            --this.aa;
        }
        if (this.by > 0) {
            --this.by;
        }
        if (this.Y <= 0) {
            ++this.ad;
            if (this.ad > 20) {
                this.aa();
                this.K();
                for (n2 = 0; n2 < 20; ++n2) {
                    double d2 = this.bs.nextGaussian() * 0.02;
                    double d3 = this.bs.nextGaussian() * 0.02;
                    double d4 = this.bs.nextGaussian() * 0.02;
                    this.aI.a("explode", this.aM + (double)(this.bs.nextFloat() * this.bg * 2.0f) - (double)this.bg, this.aN + (double)(this.bs.nextFloat() * this.bh), this.aO + (double)(this.bs.nextFloat() * this.bg * 2.0f) - (double)this.bg, d2, d3, d4);
                }
            }
        }
        this.M = this.L;
        this.I = this.H;
        this.aU = this.aS;
        this.aV = this.aT;
    }

    public void V() {
        for (int i2 = 0; i2 < 20; ++i2) {
            double d2 = this.bs.nextGaussian() * 0.02;
            double d3 = this.bs.nextGaussian() * 0.02;
            double d4 = this.bs.nextGaussian() * 0.02;
            double d5 = 10.0;
            this.aI.a("explode", this.aM + (double)(this.bs.nextFloat() * this.bg * 2.0f) - (double)this.bg - d2 * d5, this.aN + (double)(this.bs.nextFloat() * this.bh) - d3 * d5, this.aO + (double)(this.bs.nextFloat() * this.bg * 2.0f) - (double)this.bg - d4 * d5, d2, d3, d4);
        }
    }

    public void s_() {
        super.s_();
        this.J = this.K;
        this.K = 0.0f;
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2) {
        this.bf = 0.0f;
        this.ao = d2;
        this.ap = d3;
        this.aq = d4;
        this.ar = f2;
        this.as = f3;
        this.an = n2;
    }

    public void w_() {
        boolean bl2;
        float f2;
        float f3;
        super.w_();
        this.o();
        double d2 = this.aM - this.aJ;
        double d3 = this.aO - this.aL;
        float f4 = in.a(d2 * d2 + d3 * d3);
        float f5 = this.H;
        float f6 = 0.0f;
        this.J = this.K;
        float f7 = 0.0f;
        if (!(f4 <= 0.05f)) {
            f7 = 1.0f;
            f6 = f4 * 3.0f;
            f5 = (float)Math.atan2(d3, d2) * 180.0f / (float)Math.PI - 90.0f;
        }
        if (this.X > 0.0f) {
            f5 = this.aS;
        }
        if (!this.aX) {
            f7 = 0.0f;
        }
        this.K += (f7 - this.K) * 0.3f;
        for (f3 = f5 - this.H; f3 < -180.0f; f3 += 360.0f) {
        }
        while (f3 >= 180.0f) {
            f3 -= 360.0f;
        }
        this.H += f3 * 0.3f;
        for (f2 = this.aS - this.H; f2 < -180.0f; f2 += 360.0f) {
        }
        while (f2 >= 180.0f) {
            f2 -= 360.0f;
        }
        boolean bl3 = bl2 = f2 < -90.0f || f2 >= 90.0f;
        if (f2 < -75.0f) {
            f2 = -75.0f;
        }
        if (f2 >= 75.0f) {
            f2 = 75.0f;
        }
        this.H = this.aS - f2;
        if (f2 * f2 > 2500.0f) {
            this.H += f2 * 0.2f;
        }
        if (bl2) {
            f6 *= -1.0f;
        }
        while (this.aS - this.aU < -180.0f) {
            this.aU -= 360.0f;
        }
        while (this.aS - this.aU >= 180.0f) {
            this.aU += 360.0f;
        }
        while (this.H - this.I < -180.0f) {
            this.I -= 360.0f;
        }
        while (this.H - this.I >= 180.0f) {
            this.I += 360.0f;
        }
        while (this.aT - this.aV < -180.0f) {
            this.aV -= 360.0f;
        }
        while (this.aT - this.aV >= 180.0f) {
            this.aV += 360.0f;
        }
        this.L += f6;
    }

    protected void b(float f2, float f3) {
        super.b(f2, f3);
    }

    public void c(int n2) {
        if (this.Y <= 0) {
            return;
        }
        this.Y += n2;
        if (this.Y > 20) {
            this.Y = 20;
        }
        this.by = this.E / 2;
    }

    public boolean a(sn sn2, int n2) {
        if (this.aI.B) {
            return false;
        }
        this.av = 0;
        if (this.Y <= 0) {
            return false;
        }
        this.al = 1.5f;
        boolean bl2 = true;
        if ((float)this.by > (float)this.E / 2.0f) {
            if (n2 <= this.au) {
                return false;
            }
            this.b(n2 - this.au);
            this.au = n2;
            bl2 = false;
        } else {
            this.au = n2;
            this.Z = this.Y;
            this.by = this.E;
            this.b(n2);
            this.ab = 10;
            this.aa = 10;
        }
        this.ac = 0.0f;
        if (bl2) {
            this.aI.a((sn)this, (byte)2);
            this.ai();
            if (sn2 != null) {
                double d2 = sn2.aM - this.aM;
                double d3 = sn2.aO - this.aO;
                while (d2 * d2 + d3 * d3 < 1.0E-4) {
                    d2 = (Math.random() - Math.random()) * 0.01;
                    d3 = (Math.random() - Math.random()) * 0.01;
                }
                this.ac = (float)(Math.atan2(d3, d2) * 180.0 / 3.1415927410125732) - this.aS;
                this.a(sn2, n2, d2, d3);
            } else {
                this.ac = (int)(Math.random() * 2.0) * 180;
            }
        }
        if (this.Y <= 0) {
            if (bl2) {
                this.aI.a(this, this.i(), this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
            }
            this.b(sn2);
        } else if (bl2) {
            this.aI.a(this, this.j_(), this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
        }
        return true;
    }

    public void h() {
        this.ab = 10;
        this.aa = 10;
        this.ac = 0.0f;
    }

    protected void b(int n2) {
        this.Y -= n2;
    }

    protected float k() {
        return 1.0f;
    }

    protected String g() {
        return null;
    }

    protected String j_() {
        return "random.hurt";
    }

    protected String i() {
        return "random.hurt";
    }

    public void a(sn sn2, int n2, double d2, double d3) {
        float f2 = in.a(d2 * d2 + d3 * d3);
        float f3 = 0.4f;
        this.aP /= 2.0;
        this.aQ /= 2.0;
        this.aR /= 2.0;
        this.aP -= d2 / (double)f2 * (double)f3;
        this.aQ += (double)0.4f;
        this.aR -= d3 / (double)f2 * (double)f3;
        if (this.aQ > (double)0.4f) {
            this.aQ = 0.4f;
        }
    }

    public void b(sn sn2) {
        if (this.T >= 0 && sn2 != null) {
            sn2.c(this, this.T);
        }
        if (sn2 != null) {
            sn2.a(this);
        }
        this.ah = true;
        if (!this.aI.B) {
            this.q();
        }
        this.aI.a((sn)this, (byte)3);
    }

    protected void q() {
        int n2 = this.j();
        if (n2 > 0) {
            int n3 = this.bs.nextInt(3);
            for (int i2 = 0; i2 < n3; ++i2) {
                this.b(n2, 1);
            }
        }
    }

    protected int j() {
        return 0;
    }

    protected void b(float f2) {
        super.b(f2);
        int n2 = (int)Math.ceil(f2 - 3.0f);
        if (n2 > 0) {
            this.a((sn)null, n2);
            int n3 = this.aI.a(in.b(this.aM), in.b(this.aN - (double)0.2f - (double)this.bf), in.b(this.aO));
            if (n3 > 0) {
                ct ct2 = uu.m[n3].by;
                this.aI.a(this, ct2.d(), ct2.b() * 0.5f, ct2.c() * 0.75f);
            }
        }
    }

    public void a_(float f2, float f3) {
        if (this.ag()) {
            double d2 = this.aN;
            this.a(f2, f3, 0.02f);
            this.b(this.aP, this.aQ, this.aR);
            this.aP *= (double)0.8f;
            this.aQ *= (double)0.8f;
            this.aR *= (double)0.8f;
            this.aQ -= 0.02;
            if (this.aY && this.f(this.aP, this.aQ + (double)0.6f - this.aN + d2, this.aR)) {
                this.aQ = 0.3f;
            }
        } else if (this.ah()) {
            double d3 = this.aN;
            this.a(f2, f3, 0.02f);
            this.b(this.aP, this.aQ, this.aR);
            this.aP *= 0.5;
            this.aQ *= 0.5;
            this.aR *= 0.5;
            this.aQ -= 0.02;
            if (this.aY && this.f(this.aP, this.aQ + (double)0.6f - this.aN + d3, this.aR)) {
                this.aQ = 0.3f;
            }
        } else {
            float f4 = 0.91f;
            if (this.aX) {
                f4 = 0.54600006f;
                int n2 = this.aI.a(in.b(this.aM), in.b(this.aW.b) - 1, in.b(this.aO));
                if (n2 > 0) {
                    f4 = uu.m[n2].bB * 0.91f;
                }
            }
            float f5 = 0.16277136f / (f4 * f4 * f4);
            this.a(f2, f3, this.aX ? 0.1f * f5 : 0.02f);
            f4 = 0.91f;
            if (this.aX) {
                f4 = 0.54600006f;
                int n3 = this.aI.a(in.b(this.aM), in.b(this.aW.b) - 1, in.b(this.aO));
                if (n3 > 0) {
                    f4 = uu.m[n3].bB * 0.91f;
                }
            }
            if (this.p()) {
                float f6 = 0.15f;
                if (this.aP < (double)(-f6)) {
                    this.aP = -f6;
                }
                if (this.aP > (double)f6) {
                    this.aP = f6;
                }
                if (this.aR < (double)(-f6)) {
                    this.aR = -f6;
                }
                if (this.aR > (double)f6) {
                    this.aR = f6;
                }
                this.bk = 0.0f;
                if (this.aQ < -0.15) {
                    this.aQ = -0.15;
                }
                if (this.t() && this.aQ < 0.0) {
                    this.aQ = 0.0;
                }
            }
            this.b(this.aP, this.aQ, this.aR);
            if (this.aY && this.p()) {
                this.aQ = 0.2;
            }
            this.aQ -= 0.08;
            this.aQ *= (double)0.98f;
            this.aP *= (double)f4;
            this.aR *= (double)f4;
        }
        this.ak = this.al;
        double d4 = this.aM - this.aJ;
        double d5 = this.aO - this.aL;
        float f7 = in.a(d4 * d4 + d5 * d5) * 4.0f;
        if (f7 > 1.0f) {
            f7 = 1.0f;
        }
        this.al += (f7 - this.al) * 0.4f;
        this.am += this.al;
    }

    public boolean p() {
        int n2;
        int n3;
        int n4 = in.b(this.aM);
        return this.aI.a(n4, n3 = in.b(this.aW.b), n2 = in.b(this.aO)) == uu.aG.bn;
    }

    public void b(nu nu2) {
        nu2.a("Health", (short)this.Y);
        nu2.a("HurtTime", (short)this.aa);
        nu2.a("DeathTime", (short)this.ad);
        nu2.a("AttackTime", (short)this.ae);
    }

    public void a(nu nu2) {
        this.Y = nu2.d("Health");
        if (!nu2.b("Health")) {
            this.Y = 10;
        }
        this.aa = nu2.d("HurtTime");
        this.ad = nu2.d("DeathTime");
        this.ae = nu2.d("AttackTime");
    }

    public boolean W() {
        return !this.be && this.Y > 0;
    }

    public boolean d_() {
        return false;
    }

    public void o() {
        if (this.an > 0) {
            double d2;
            double d3 = this.aM + (this.ao - this.aM) / (double)this.an;
            double d4 = this.aN + (this.ap - this.aN) / (double)this.an;
            double d5 = this.aO + (this.aq - this.aO) / (double)this.an;
            for (d2 = this.ar - (double)this.aS; d2 < -180.0; d2 += 360.0) {
            }
            while (d2 >= 180.0) {
                d2 -= 360.0;
            }
            this.aS = (float)((double)this.aS + d2 / (double)this.an);
            this.aT = (float)((double)this.aT + (this.as - (double)this.aT) / (double)this.an);
            --this.an;
            this.e(d3, d4, d5);
            this.c(this.aS, this.aT);
            List list = this.aI.a((sn)this, this.aW.e(0.03125, 0.0, 0.03125));
            if (list.size() > 0) {
                double d6 = 0.0;
                for (int i2 = 0; i2 < list.size(); ++i2) {
                    eq eq2 = (eq)list.get(i2);
                    if (!(eq2.e > d6)) continue;
                    d6 = eq2.e;
                }
                this.e(d3, d4 += d6 - this.aW.b, d5);
            }
        }
        if (this.y()) {
            this.az = false;
            this.aw = 0.0f;
            this.ax = 0.0f;
            this.ay = 0.0f;
        } else if (!this.V) {
            this.f_();
        }
        boolean bl2 = this.ag();
        boolean bl3 = this.ah();
        if (this.az) {
            if (bl2) {
                this.aQ += (double)0.04f;
            } else if (bl3) {
                this.aQ += (double)0.04f;
            } else if (this.aX) {
                this.R();
            }
        }
        this.aw *= 0.98f;
        this.ax *= 0.98f;
        this.ay *= 0.9f;
        this.a_(this.aw, this.ax);
        List list = this.aI.b(this, this.aW.b(0.2f, 0.0, 0.2f));
        if (list != null && list.size() > 0) {
            for (int i3 = 0; i3 < list.size(); ++i3) {
                sn sn2 = (sn)list.get(i3);
                if (!sn2.i_()) continue;
                sn2.h(this);
            }
        }
    }

    protected boolean y() {
        return this.Y <= 0;
    }

    protected void R() {
        this.aQ = 0.42f;
    }

    protected boolean u() {
        return true;
    }

    protected void X() {
        gs gs2 = this.aI.a((sn)this, -1.0);
        if (this.u() && gs2 != null) {
            double d2 = gs2.aM - this.aM;
            double d3 = gs2.aN - this.aN;
            double d4 = gs2.aO - this.aO;
            double d5 = d2 * d2 + d3 * d3 + d4 * d4;
            if (d5 > 16384.0) {
                this.K();
            }
            if (this.av > 600 && this.bs.nextInt(800) == 0) {
                if (d5 < 1024.0) {
                    this.av = 0;
                } else {
                    this.K();
                }
            }
        }
    }

    protected void f_() {
        ++this.av;
        gs gs2 = this.aI.a((sn)this, -1.0);
        this.X();
        this.aw = 0.0f;
        this.ax = 0.0f;
        float f2 = 8.0f;
        if (this.bs.nextFloat() < 0.02f) {
            gs2 = this.aI.a((sn)this, (double)f2);
            if (gs2 != null) {
                this.b = gs2;
                this.aC = 10 + this.bs.nextInt(20);
            } else {
                this.ay = (this.bs.nextFloat() - 0.5f) * 20.0f;
            }
        }
        if (this.b != null) {
            this.a(this.b, 10.0f, (float)this.x());
            if (this.aC-- <= 0 || this.b.be || this.b.g(this) > (double)(f2 * f2)) {
                this.b = null;
            }
        } else {
            if (this.bs.nextFloat() < 0.05f) {
                this.ay = (this.bs.nextFloat() - 0.5f) * 20.0f;
            }
            this.aS += this.ay;
            this.aT = this.aA;
        }
        boolean bl2 = this.ag();
        boolean bl3 = this.ah();
        if (bl2 || bl3) {
            this.az = this.bs.nextFloat() < 0.8f;
        }
    }

    protected int x() {
        return 40;
    }

    public void a(sn sn2, float f2, float f3) {
        double d2;
        double d3 = sn2.aM - this.aM;
        double d4 = sn2.aO - this.aO;
        if (sn2 instanceof ls) {
            ls ls2 = (ls)sn2;
            d2 = this.aN + (double)this.w() - (ls2.aN + (double)ls2.w());
        } else {
            d2 = (sn2.aW.b + sn2.aW.e) / 2.0 - (this.aN + (double)this.w());
        }
        double d5 = in.a(d3 * d3 + d4 * d4);
        float f4 = (float)(Math.atan2(d4, d3) * 180.0 / 3.1415927410125732) - 90.0f;
        float f5 = (float)(-(Math.atan2(d2, d5) * 180.0 / 3.1415927410125732));
        this.aT = -this.b(this.aT, f5, f3);
        this.aS = this.b(this.aS, f4, f2);
    }

    public boolean Y() {
        return this.b != null;
    }

    public sn Z() {
        return this.b;
    }

    private float b(float f2, float f3, float f4) {
        float f5;
        for (f5 = f3 - f2; f5 < -180.0f; f5 += 360.0f) {
        }
        while (f5 >= 180.0f) {
            f5 -= 360.0f;
        }
        if (f5 > f4) {
            f5 = f4;
        }
        if (f5 < -f4) {
            f5 = -f4;
        }
        return f2 + f5;
    }

    public void aa() {
    }

    public boolean d() {
        return this.aI.a(this.aW) && this.aI.a((sn)this, this.aW).size() == 0 && !this.aI.b(this.aW);
    }

    protected void ab() {
        this.a((sn)null, 4);
    }

    public float d(float f2) {
        float f3 = this.X - this.W;
        if (f3 < 0.0f) {
            f3 += 1.0f;
        }
        return this.W + f3 * f2;
    }

    public bt e(float f2) {
        if (f2 == 1.0f) {
            return bt.b(this.aM, this.aN, this.aO);
        }
        double d2 = this.aJ + (this.aM - this.aJ) * (double)f2;
        double d3 = this.aK + (this.aN - this.aK) * (double)f2;
        double d4 = this.aL + (this.aO - this.aL) * (double)f2;
        return bt.b(d2, d3, d4);
    }

    public bt ac() {
        return this.f(1.0f);
    }

    public bt f(float f2) {
        if (f2 == 1.0f) {
            float f3 = in.b(-this.aS * ((float)Math.PI / 180) - (float)Math.PI);
            float f4 = in.a(-this.aS * ((float)Math.PI / 180) - (float)Math.PI);
            float f5 = -in.b(-this.aT * ((float)Math.PI / 180));
            float f6 = in.a(-this.aT * ((float)Math.PI / 180));
            return bt.b(f4 * f5, f6, f3 * f5);
        }
        float f7 = this.aV + (this.aT - this.aV) * f2;
        float f8 = this.aU + (this.aS - this.aU) * f2;
        float f9 = in.b(-f8 * ((float)Math.PI / 180) - (float)Math.PI);
        float f10 = in.a(-f8 * ((float)Math.PI / 180) - (float)Math.PI);
        float f11 = -in.b(-f7 * ((float)Math.PI / 180));
        float f12 = in.a(-f7 * ((float)Math.PI / 180));
        return bt.b(f10 * f11, f12, f9 * f11);
    }

    public vf a(double d2, float f2) {
        bt bt2 = this.e(f2);
        bt bt3 = this.f(f2);
        bt bt4 = bt2.c(bt3.a * d2, bt3.b * d2, bt3.c * d2);
        return this.aI.a(bt2, bt4);
    }

    public int l() {
        return 4;
    }

    public iz r_() {
        return null;
    }

    public void a(byte by2) {
        if (by2 == 2) {
            this.al = 1.5f;
            this.by = this.E;
            this.ab = 10;
            this.aa = 10;
            this.ac = 0.0f;
            this.aI.a(this, this.j_(), this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
            this.a((sn)null, 0);
        } else if (by2 == 3) {
            this.aI.a(this, this.i(), this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
            this.Y = 0;
            this.b((sn)null);
        } else {
            super.a(by2);
        }
    }

    public boolean N() {
        return false;
    }

    public int c(iz iz2) {
        return iz2.b();
    }
}

