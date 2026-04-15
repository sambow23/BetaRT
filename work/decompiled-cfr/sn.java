/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Random;

public abstract class sn {
    private static int a = 0;
    public int aD = a++;
    public double aE = 1.0;
    public boolean aF = false;
    public sn aG;
    public sn aH;
    public fd aI;
    public double aJ;
    public double aK;
    public double aL;
    public double aM;
    public double aN;
    public double aO;
    public double aP;
    public double aQ;
    public double aR;
    public float aS;
    public float aT;
    public float aU;
    public float aV;
    public final eq aW = eq.a(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public boolean aX = false;
    public boolean aY;
    public boolean aZ;
    public boolean ba = false;
    public boolean bb = false;
    public boolean bc;
    public boolean bd = true;
    public boolean be = false;
    public float bf = 0.0f;
    public float bg = 0.6f;
    public float bh = 1.8f;
    public float bi = 0.0f;
    public float bj = 0.0f;
    protected float bk = 0.0f;
    private int b = 1;
    public double bl;
    public double bm;
    public double bn;
    public float bo = 0.0f;
    public float bp = 0.0f;
    public boolean bq = false;
    public float br = 0.0f;
    protected Random bs = new Random();
    public int bt = 0;
    public int bu = 1;
    public int bv = 0;
    protected int bw = 300;
    protected boolean bx = false;
    public int by = 0;
    public int bz = 300;
    private boolean c = true;
    public String bA;
    public String bB;
    protected boolean bC = false;
    protected ud bD = new ud();
    public float bE = 0.0f;
    private double d;
    private double e;
    public boolean bF = false;
    public int bG;
    public int bH;
    public int bI;
    public int bJ;
    public int bK;
    public int bL;
    public boolean bM;

    public sn(fd fd2) {
        this.aI = fd2;
        this.e(0.0, 0.0, 0.0);
        this.bD.a(0, (byte)0);
        this.b();
    }

    protected abstract void b();

    public ud ad() {
        return this.bD;
    }

    public boolean equals(Object object) {
        if (object instanceof sn) {
            return ((sn)object).aD == this.aD;
        }
        return false;
    }

    public int hashCode() {
        return this.aD;
    }

    protected void t_() {
        if (this.aI == null) {
            return;
        }
        while (this.aN > 0.0) {
            this.e(this.aM, this.aN, this.aO);
            if (this.aI.a(this, this.aW).size() == 0) break;
            this.aN += 1.0;
        }
        this.aR = 0.0;
        this.aQ = 0.0;
        this.aP = 0.0;
        this.aT = 0.0f;
    }

    public void K() {
        this.be = true;
    }

    protected void b(float f2, float f3) {
        this.bg = f2;
        this.bh = f3;
    }

    protected void c(float f2, float f3) {
        this.aS = f2 % 360.0f;
        this.aT = f3 % 360.0f;
    }

    public void e(double d2, double d3, double d4) {
        this.aM = d2;
        this.aN = d3;
        this.aO = d4;
        float f2 = this.bg / 2.0f;
        float f3 = this.bh;
        this.aW.c(d2 - (double)f2, d3 - (double)this.bf + (double)this.bo, d4 - (double)f2, d2 + (double)f2, d3 - (double)this.bf + (double)this.bo + (double)f3, d4 + (double)f2);
    }

    public void d(float f2, float f3) {
        float f4 = this.aT;
        float f5 = this.aS;
        this.aS = (float)((double)this.aS + (double)f2 * 0.15);
        this.aT = (float)((double)this.aT - (double)f3 * 0.15);
        if (this.aT < -90.0f) {
            this.aT = -90.0f;
        }
        if (this.aT > 90.0f) {
            this.aT = 90.0f;
        }
        this.aV += this.aT - f4;
        this.aU += this.aS - f5;
    }

    public void w_() {
        this.U();
    }

    public void U() {
        if (this.aH != null && this.aH.be) {
            this.aH = null;
        }
        ++this.bt;
        this.bi = this.bj;
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        this.aV = this.aT;
        this.aU = this.aS;
        if (this.k_()) {
            if (!this.bx && !this.c) {
                float f2;
                float f3;
                float f4 = in.a(this.aP * this.aP * (double)0.2f + this.aQ * this.aQ + this.aR * this.aR * (double)0.2f) * 0.2f;
                if (f4 > 1.0f) {
                    f4 = 1.0f;
                }
                this.aI.a(this, "random.splash", f4, 1.0f + (this.bs.nextFloat() - this.bs.nextFloat()) * 0.4f);
                float f5 = in.b(this.aW.b);
                int n2 = 0;
                while ((float)n2 < 1.0f + this.bg * 20.0f) {
                    f3 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                    f2 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                    this.aI.a("bubble", this.aM + (double)f3, f5 + 1.0f, this.aO + (double)f2, this.aP, this.aQ - (double)(this.bs.nextFloat() * 0.2f), this.aR);
                    ++n2;
                }
                n2 = 0;
                while ((float)n2 < 1.0f + this.bg * 20.0f) {
                    f3 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                    f2 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg;
                    this.aI.a("splash", this.aM + (double)f3, f5 + 1.0f, this.aO + (double)f2, this.aP, this.aQ, this.aR);
                    ++n2;
                }
            }
            this.bk = 0.0f;
            this.bx = true;
            this.bv = 0;
        } else {
            this.bx = false;
        }
        if (this.aI.B) {
            this.bv = 0;
        } else if (this.bv > 0) {
            if (this.bC) {
                this.bv -= 4;
                if (this.bv < 0) {
                    this.bv = 0;
                }
            } else {
                if (this.bv % 20 == 0) {
                    this.a((sn)null, 1);
                }
                --this.bv;
            }
        }
        if (this.ah()) {
            this.ae();
        }
        if (this.aN < -64.0) {
            this.ab();
        }
        if (!this.aI.B) {
            this.b(0, this.bv > 0);
            this.b(2, this.aH != null);
        }
        this.c = false;
    }

    protected void ae() {
        if (!this.bC) {
            this.a((sn)null, 4);
            this.bv = 600;
        }
    }

    protected void ab() {
        this.K();
    }

    public boolean f(double d2, double d3, double d4) {
        eq eq2 = this.aW.c(d2, d3, d4);
        List list = this.aI.a(this, eq2);
        if (list.size() > 0) {
            return false;
        }
        return !this.aI.b(eq2);
    }

    public void b(double d2, double d3, double d4) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        double d5;
        int n9;
        int n10;
        boolean bl2;
        if (this.bq) {
            this.aW.d(d2, d3, d4);
            this.aM = (this.aW.a + this.aW.d) / 2.0;
            this.aN = this.aW.b + (double)this.bf - (double)this.bo;
            this.aO = (this.aW.c + this.aW.f) / 2.0;
            return;
        }
        this.bo *= 0.4f;
        double d6 = this.aM;
        double d7 = this.aO;
        if (this.bc) {
            this.bc = false;
            d2 *= 0.25;
            d3 *= (double)0.05f;
            d4 *= 0.25;
            this.aP = 0.0;
            this.aQ = 0.0;
            this.aR = 0.0;
        }
        double d8 = d2;
        double d9 = d3;
        double d10 = d4;
        eq eq2 = this.aW.d();
        boolean bl3 = bl2 = this.aX && this.t();
        if (bl2) {
            double d11 = 0.05;
            while (d2 != 0.0 && this.aI.a(this, this.aW.c(d2, -1.0, 0.0)).size() == 0) {
                d2 = d2 < d11 && d2 >= -d11 ? 0.0 : (d2 > 0.0 ? (d2 -= d11) : (d2 += d11));
                d8 = d2;
            }
            while (d4 != 0.0 && this.aI.a(this, this.aW.c(0.0, -1.0, d4)).size() == 0) {
                d4 = d4 < d11 && d4 >= -d11 ? 0.0 : (d4 > 0.0 ? (d4 -= d11) : (d4 += d11));
                d10 = d4;
            }
        }
        List list = this.aI.a(this, this.aW.a(d2, d3, d4));
        for (n10 = 0; n10 < list.size(); ++n10) {
            d3 = ((eq)list.get(n10)).b(this.aW, d3);
        }
        this.aW.d(0.0, d3, 0.0);
        if (!this.bd && d9 != d3) {
            d4 = 0.0;
            d3 = 0.0;
            d2 = 0.0;
        }
        n10 = this.aX || d9 != d3 && d9 < 0.0 ? 1 : 0;
        for (n9 = 0; n9 < list.size(); ++n9) {
            d2 = ((eq)list.get(n9)).a(this.aW, d2);
        }
        this.aW.d(d2, 0.0, 0.0);
        if (!this.bd && d8 != d2) {
            d4 = 0.0;
            d3 = 0.0;
            d2 = 0.0;
        }
        for (n9 = 0; n9 < list.size(); ++n9) {
            d4 = ((eq)list.get(n9)).c(this.aW, d4);
        }
        this.aW.d(0.0, 0.0, d4);
        if (!this.bd && d10 != d4) {
            d4 = 0.0;
            d3 = 0.0;
            d2 = 0.0;
        }
        if (this.bp > 0.0f && n10 != 0 && (bl2 || this.bo < 0.05f) && (d8 != d2 || d10 != d4)) {
            int n11;
            double d12 = d2;
            d5 = d3;
            double d13 = d4;
            d2 = d8;
            d3 = this.bp;
            d4 = d10;
            eq eq3 = this.aW.d();
            this.aW.b(eq2);
            list = this.aI.a(this, this.aW.a(d2, d3, d4));
            for (n11 = 0; n11 < list.size(); ++n11) {
                d3 = ((eq)list.get(n11)).b(this.aW, d3);
            }
            this.aW.d(0.0, d3, 0.0);
            if (!this.bd && d9 != d3) {
                d4 = 0.0;
                d3 = 0.0;
                d2 = 0.0;
            }
            for (n11 = 0; n11 < list.size(); ++n11) {
                d2 = ((eq)list.get(n11)).a(this.aW, d2);
            }
            this.aW.d(d2, 0.0, 0.0);
            if (!this.bd && d8 != d2) {
                d4 = 0.0;
                d3 = 0.0;
                d2 = 0.0;
            }
            for (n11 = 0; n11 < list.size(); ++n11) {
                d4 = ((eq)list.get(n11)).c(this.aW, d4);
            }
            this.aW.d(0.0, 0.0, d4);
            if (!this.bd && d10 != d4) {
                d4 = 0.0;
                d3 = 0.0;
                d2 = 0.0;
            }
            if (!this.bd && d9 != d3) {
                d4 = 0.0;
                d3 = 0.0;
                d2 = 0.0;
            } else {
                d3 = -this.bp;
                for (n11 = 0; n11 < list.size(); ++n11) {
                    d3 = ((eq)list.get(n11)).b(this.aW, d3);
                }
                this.aW.d(0.0, d3, 0.0);
            }
            if (d12 * d12 + d13 * d13 >= d2 * d2 + d4 * d4) {
                d2 = d12;
                d3 = d5;
                d4 = d13;
                this.aW.b(eq3);
            } else {
                double d14 = this.aW.b - (double)((int)this.aW.b);
                if (d14 > 0.0) {
                    this.bo = (float)((double)this.bo + (d14 + 0.01));
                }
            }
        }
        this.aM = (this.aW.a + this.aW.d) / 2.0;
        this.aN = this.aW.b + (double)this.bf - (double)this.bo;
        this.aO = (this.aW.c + this.aW.f) / 2.0;
        this.aY = d8 != d2 || d10 != d4;
        this.aZ = d9 != d3;
        this.aX = d9 != d3 && d9 < 0.0;
        this.ba = this.aY || this.aZ;
        this.a(d3, this.aX);
        if (d8 != d2) {
            this.aP = 0.0;
        }
        if (d9 != d3) {
            this.aQ = 0.0;
        }
        if (d10 != d4) {
            this.aR = 0.0;
        }
        double d15 = this.aM - d6;
        d5 = this.aO - d7;
        if (this.n() && !bl2 && this.aH == null) {
            this.bj = (float)((double)this.bj + (double)in.a(d15 * d15 + d5 * d5) * 0.6);
            int n12 = in.b(this.aM);
            n8 = in.b(this.aN - (double)0.2f - (double)this.bf);
            int n13 = in.b(this.aO);
            int n14 = this.aI.a(n12, n8, n13);
            if (this.aI.a(n12, n8 - 1, n13) == uu.ba.bn) {
                n14 = this.aI.a(n12, n8 - 1, n13);
            }
            if (this.bj > (float)this.b && n14 > 0) {
                ++this.b;
                ct ct2 = uu.m[n14].by;
                if (this.aI.a(n12, n8 + 1, n13) == uu.aT.bn) {
                    ct2 = uu.aT.by;
                    this.aI.a(this, ct2.d(), ct2.b() * 0.15f, ct2.c());
                } else if (!uu.m[n14].bA.d()) {
                    this.aI.a(this, ct2.d(), ct2.b() * 0.15f, ct2.c());
                }
                uu.m[n14].b(this.aI, n12, n8, n13, this);
            }
        }
        if (this.aI.a(n7 = in.b(this.aW.a + 0.001), n8 = in.b(this.aW.b + 0.001), n6 = in.b(this.aW.c + 0.001), n5 = in.b(this.aW.d - 0.001), n4 = in.b(this.aW.e - 0.001), n3 = in.b(this.aW.f - 0.001))) {
            for (n2 = n7; n2 <= n5; ++n2) {
                for (int i2 = n8; i2 <= n4; ++i2) {
                    for (int i3 = n6; i3 <= n3; ++i3) {
                        int n15 = this.aI.a(n2, i2, i3);
                        if (n15 <= 0) continue;
                        uu.m[n15].a(this.aI, n2, i2, i3, this);
                    }
                }
            }
        }
        n2 = this.af() ? 1 : 0;
        if (this.aI.c(this.aW.e(0.001, 0.001, 0.001))) {
            this.a(1);
            if (n2 == 0) {
                ++this.bv;
                if (this.bv == 0) {
                    this.bv = 300;
                }
            }
        } else if (this.bv <= 0) {
            this.bv = -this.bu;
        }
        if (n2 != 0 && this.bv > 0) {
            this.aI.a(this, "random.fizz", 0.7f, 1.6f + (this.bs.nextFloat() - this.bs.nextFloat()) * 0.4f);
            this.bv = -this.bu;
        }
    }

    protected boolean n() {
        return true;
    }

    protected void a(double d2, boolean bl2) {
        if (bl2) {
            if (this.bk > 0.0f) {
                this.b(this.bk);
                this.bk = 0.0f;
            }
        } else if (d2 < 0.0) {
            this.bk = (float)((double)this.bk - d2);
        }
    }

    public eq f() {
        return null;
    }

    protected void a(int n2) {
        if (!this.bC) {
            this.a((sn)null, n2);
        }
    }

    protected void b(float f2) {
        if (this.aG != null) {
            this.aG.b(f2);
        }
    }

    public boolean af() {
        return this.bx || this.aI.t(in.b(this.aM), in.b(this.aN), in.b(this.aO));
    }

    public boolean ag() {
        return this.bx;
    }

    public boolean k_() {
        return this.aI.a(this.aW.b(0.0, -0.4f, 0.0).e(0.001, 0.001, 0.001), ln.g, this);
    }

    public boolean a(ln ln2) {
        int n2;
        int n3;
        double d2 = this.aN + (double)this.w();
        int n4 = in.b(this.aM);
        int n5 = this.aI.a(n4, n3 = in.d(in.b(d2)), n2 = in.b(this.aO));
        if (n5 != 0 && uu.m[n5].bA == ln2) {
            float f2 = rp.d(this.aI.e(n4, n3, n2)) - 0.11111111f;
            float f3 = (float)(n3 + 1) - f2;
            return d2 < (double)f3;
        }
        return false;
    }

    public float w() {
        return 0.0f;
    }

    public boolean ah() {
        return this.aI.a(this.aW.b(-0.1f, -0.4f, -0.1f), ln.h);
    }

    public void a(float f2, float f3, float f4) {
        float f5 = in.c(f2 * f2 + f3 * f3);
        if (f5 < 0.01f) {
            return;
        }
        if (f5 < 1.0f) {
            f5 = 1.0f;
        }
        f5 = f4 / f5;
        float f6 = in.a(this.aS * (float)Math.PI / 180.0f);
        float f7 = in.b(this.aS * (float)Math.PI / 180.0f);
        this.aP += (double)((f2 *= f5) * f7 - (f3 *= f5) * f6);
        this.aR += (double)(f3 * f7 + f2 * f6);
    }

    public float a(float f2) {
        int n2 = in.b(this.aM);
        double d2 = (this.aW.e - this.aW.b) * 0.66;
        int n3 = in.b(this.aN - (double)this.bf + d2);
        int n4 = in.b(this.aO);
        if (this.aI.a(in.b(this.aW.a), in.b(this.aW.b), in.b(this.aW.c), in.b(this.aW.d), in.b(this.aW.e), in.b(this.aW.f))) {
            float f3 = this.aI.c(n2, n3, n4);
            if (f3 < this.bE) {
                f3 = this.bE;
            }
            return f3;
        }
        return this.bE;
    }

    public void a(fd fd2) {
        this.aI = fd2;
    }

    public void b(double d2, double d3, double d4, float f2, float f3) {
        this.aJ = this.aM = d2;
        this.aK = this.aN = d3;
        this.aL = this.aO = d4;
        this.aU = this.aS = f2;
        this.aV = this.aT = f3;
        this.bo = 0.0f;
        double d5 = this.aU - f2;
        if (d5 < -180.0) {
            this.aU += 360.0f;
        }
        if (d5 >= 180.0) {
            this.aU -= 360.0f;
        }
        this.e(this.aM, this.aN, this.aO);
        this.c(f2, f3);
    }

    public void c(double d2, double d3, double d4, float f2, float f3) {
        this.aJ = this.aM = d2;
        this.bl = this.aM;
        this.aK = this.aN = d3 + (double)this.bf;
        this.bm = this.aN;
        this.aL = this.aO = d4;
        this.bn = this.aO;
        this.aS = f2;
        this.aT = f3;
        this.e(this.aM, this.aN, this.aO);
    }

    public float f(sn sn2) {
        float f2 = (float)(this.aM - sn2.aM);
        float f3 = (float)(this.aN - sn2.aN);
        float f4 = (float)(this.aO - sn2.aO);
        return in.c(f2 * f2 + f3 * f3 + f4 * f4);
    }

    public double g(double d2, double d3, double d4) {
        double d5 = this.aM - d2;
        double d6 = this.aN - d3;
        double d7 = this.aO - d4;
        return d5 * d5 + d6 * d6 + d7 * d7;
    }

    public double h(double d2, double d3, double d4) {
        double d5 = this.aM - d2;
        double d6 = this.aN - d3;
        double d7 = this.aO - d4;
        return in.a(d5 * d5 + d6 * d6 + d7 * d7);
    }

    public double g(sn sn2) {
        double d2 = this.aM - sn2.aM;
        double d3 = this.aN - sn2.aN;
        double d4 = this.aO - sn2.aO;
        return d2 * d2 + d3 * d3 + d4 * d4;
    }

    public void b(gs gs2) {
    }

    public void h(sn sn2) {
        if (sn2.aG == this || sn2.aH == this) {
            return;
        }
        double d2 = sn2.aM - this.aM;
        double d3 = sn2.aO - this.aO;
        double d4 = in.a(d2, d3);
        if (d4 >= (double)0.01f) {
            d4 = in.a(d4);
            d2 /= d4;
            d3 /= d4;
            double d5 = 1.0 / d4;
            if (d5 > 1.0) {
                d5 = 1.0;
            }
            d2 *= d5;
            d3 *= d5;
            d2 *= (double)0.05f;
            d3 *= (double)0.05f;
            this.d(-(d2 *= (double)(1.0f - this.br)), 0.0, -(d3 *= (double)(1.0f - this.br)));
            sn2.d(d2, 0.0, d3);
        }
    }

    public void d(double d2, double d3, double d4) {
        this.aP += d2;
        this.aQ += d3;
        this.aR += d4;
    }

    protected void ai() {
        this.bb = true;
    }

    public boolean a(sn sn2, int n2) {
        this.ai();
        return false;
    }

    public boolean h_() {
        return false;
    }

    public boolean i_() {
        return false;
    }

    public void c(sn sn2, int n2) {
    }

    public boolean a(bt bt2) {
        double d2 = this.aM - bt2.a;
        double d3 = this.aN - bt2.b;
        double d4 = this.aO - bt2.c;
        double d5 = d2 * d2 + d3 * d3 + d4 * d4;
        return this.a(d5);
    }

    public boolean a(double d2) {
        double d3 = this.aW.c();
        return d2 < (d3 *= 64.0 * this.aE) * d3;
    }

    public String q_() {
        return null;
    }

    public boolean c(nu nu2) {
        String string = this.aj();
        if (this.be || string == null) {
            return false;
        }
        nu2.a("id", string);
        this.d(nu2);
        return true;
    }

    public void d(nu nu2) {
        nu2.a("Pos", this.a(new double[]{this.aM, this.aN + (double)this.bo, this.aO}));
        nu2.a("Motion", this.a(new double[]{this.aP, this.aQ, this.aR}));
        nu2.a("Rotation", this.a(this.aS, this.aT));
        nu2.a("FallDistance", this.bk);
        nu2.a("Fire", (short)this.bv);
        nu2.a("Air", (short)this.bz);
        nu2.a("OnGround", this.aX);
        this.b(nu2);
    }

    public void e(nu nu2) {
        sp sp2 = nu2.l("Pos");
        sp sp3 = nu2.l("Motion");
        sp sp4 = nu2.l("Rotation");
        this.aP = ((sz)sp3.a((int)0)).a;
        this.aQ = ((sz)sp3.a((int)1)).a;
        this.aR = ((sz)sp3.a((int)2)).a;
        if (Math.abs(this.aP) > 10.0) {
            this.aP = 0.0;
        }
        if (Math.abs(this.aQ) > 10.0) {
            this.aQ = 0.0;
        }
        if (Math.abs(this.aR) > 10.0) {
            this.aR = 0.0;
        }
        this.bl = this.aM = ((sz)sp2.a((int)0)).a;
        this.aJ = this.aM;
        this.bm = this.aN = ((sz)sp2.a((int)1)).a;
        this.aK = this.aN;
        this.bn = this.aO = ((sz)sp2.a((int)2)).a;
        this.aL = this.aO;
        this.aU = this.aS = ((p)sp4.a((int)0)).a;
        this.aV = this.aT = ((p)sp4.a((int)1)).a;
        this.bk = nu2.g("FallDistance");
        this.bv = nu2.d("Fire");
        this.bz = nu2.d("Air");
        this.aX = nu2.m("OnGround");
        this.e(this.aM, this.aN, this.aO);
        this.c(this.aS, this.aT);
        this.a(nu2);
    }

    protected final String aj() {
        return jc.b(this);
    }

    protected abstract void a(nu var1);

    protected abstract void b(nu var1);

    protected sp a(double ... dArray) {
        sp sp2 = new sp();
        for (double d2 : dArray) {
            sp2.a(new sz(d2));
        }
        return sp2;
    }

    protected sp a(float ... fArray) {
        sp sp2 = new sp();
        for (float f2 : fArray) {
            sp2.a(new p(f2));
        }
        return sp2;
    }

    public float x_() {
        return this.bh / 2.0f;
    }

    public hl b(int n2, int n3) {
        return this.a(n2, n3, 0.0f);
    }

    public hl a(int n2, int n3, float f2) {
        return this.a(new iz(n2, n3, 0), f2);
    }

    public hl a(iz iz2, float f2) {
        hl hl2 = new hl(this.aI, this.aM, this.aN + (double)f2, this.aO, iz2);
        hl2.c = 10;
        this.aI.b(hl2);
        return hl2;
    }

    public boolean W() {
        return !this.be;
    }

    public boolean L() {
        for (int i2 = 0; i2 < 8; ++i2) {
            int n2;
            int n3;
            float f2 = ((float)((i2 >> 0) % 2) - 0.5f) * this.bg * 0.9f;
            float f3 = ((float)((i2 >> 1) % 2) - 0.5f) * 0.1f;
            float f4 = ((float)((i2 >> 2) % 2) - 0.5f) * this.bg * 0.9f;
            int n4 = in.b(this.aM + (double)f2);
            if (!this.aI.h(n4, n3 = in.b(this.aN + (double)this.w() + (double)f3), n2 = in.b(this.aO + (double)f4))) continue;
            return true;
        }
        return false;
    }

    public boolean a(gs gs2) {
        return false;
    }

    public eq a(sn sn2) {
        return null;
    }

    public void s_() {
        if (this.aH.be) {
            this.aH = null;
            return;
        }
        this.aP = 0.0;
        this.aQ = 0.0;
        this.aR = 0.0;
        this.w_();
        if (this.aH == null) {
            return;
        }
        this.aH.l_();
        this.e += (double)(this.aH.aS - this.aH.aU);
        this.d += (double)(this.aH.aT - this.aH.aV);
        while (this.e >= 180.0) {
            this.e -= 360.0;
        }
        while (this.e < -180.0) {
            this.e += 360.0;
        }
        while (this.d >= 180.0) {
            this.d -= 360.0;
        }
        while (this.d < -180.0) {
            this.d += 360.0;
        }
        double d2 = this.e * 0.5;
        double d3 = this.d * 0.5;
        float f2 = 10.0f;
        if (d2 > (double)f2) {
            d2 = f2;
        }
        if (d2 < (double)(-f2)) {
            d2 = -f2;
        }
        if (d3 > (double)f2) {
            d3 = f2;
        }
        if (d3 < (double)(-f2)) {
            d3 = -f2;
        }
        this.e -= d2;
        this.d -= d3;
        this.aS = (float)((double)this.aS + d2);
        this.aT = (float)((double)this.aT + d3);
    }

    public void l_() {
        this.aG.e(this.aM, this.aN + this.m() + this.aG.I(), this.aO);
    }

    public double I() {
        return this.bf;
    }

    public double m() {
        return (double)this.bh * 0.75;
    }

    public void i(sn sn2) {
        this.d = 0.0;
        this.e = 0.0;
        if (sn2 == null) {
            if (this.aH != null) {
                this.c(this.aH.aM, this.aH.aW.b + (double)this.aH.bh, this.aH.aO, this.aS, this.aT);
                this.aH.aG = null;
            }
            this.aH = null;
            return;
        }
        if (this.aH == sn2) {
            this.aH.aG = null;
            this.aH = null;
            this.c(sn2.aM, sn2.aW.b + (double)sn2.bh, sn2.aO, this.aS, this.aT);
            return;
        }
        if (this.aH != null) {
            this.aH.aG = null;
        }
        if (sn2.aG != null) {
            sn2.aG.aH = null;
        }
        this.aH = sn2;
        sn2.aG = this;
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2) {
        this.e(d2, d3, d4);
        this.c(f2, f3);
        List list = this.aI.a(this, this.aW.e(0.03125, 0.0, 0.03125));
        if (list.size() > 0) {
            double d5 = 0.0;
            for (int i2 = 0; i2 < list.size(); ++i2) {
                eq eq2 = (eq)list.get(i2);
                if (!(eq2.e > d5)) continue;
                d5 = eq2.e;
            }
            this.e(d2, d3 += d5 - this.aW.b, d4);
        }
    }

    public float m_() {
        return 0.1f;
    }

    public bt ac() {
        return null;
    }

    public void S() {
    }

    public void a(double d2, double d3, double d4) {
        this.aP = d2;
        this.aQ = d3;
        this.aR = d4;
    }

    public void a(byte by2) {
    }

    public void h() {
    }

    public void u_() {
    }

    public void c(int n2, int n3, int n4) {
    }

    public boolean ak() {
        return this.bv > 0 || this.d(0);
    }

    public boolean al() {
        return this.aH != null || this.d(2);
    }

    public boolean t() {
        return this.d(1);
    }

    protected boolean d(int n2) {
        return (this.bD.a(0) & 1 << n2) != 0;
    }

    protected void b(int n2, boolean bl2) {
        byte by2 = this.bD.a(0);
        if (bl2) {
            this.bD.b(0, (byte)(by2 | 1 << n2));
        } else {
            this.bD.b(0, (byte)(by2 & ~(1 << n2)));
        }
    }

    public void a(c c2) {
        this.a(5);
        ++this.bv;
        if (this.bv == 0) {
            this.bv = 300;
        }
    }

    public void a(ls ls2) {
    }

    protected boolean c(double d2, double d3, double d4) {
        int n2 = in.b(d2);
        int n3 = in.b(d3);
        int n4 = in.b(d4);
        double d5 = d2 - (double)n2;
        double d6 = d3 - (double)n3;
        double d7 = d4 - (double)n4;
        if (this.aI.h(n2, n3, n4)) {
            boolean bl2 = !this.aI.h(n2 - 1, n3, n4);
            boolean bl3 = !this.aI.h(n2 + 1, n3, n4);
            boolean bl4 = !this.aI.h(n2, n3 - 1, n4);
            boolean bl5 = !this.aI.h(n2, n3 + 1, n4);
            boolean bl6 = !this.aI.h(n2, n3, n4 - 1);
            boolean bl7 = !this.aI.h(n2, n3, n4 + 1);
            int n5 = -1;
            double d8 = 9999.0;
            if (bl2 && d5 < d8) {
                d8 = d5;
                n5 = 0;
            }
            if (bl3 && 1.0 - d5 < d8) {
                d8 = 1.0 - d5;
                n5 = 1;
            }
            if (bl4 && d6 < d8) {
                d8 = d6;
                n5 = 2;
            }
            if (bl5 && 1.0 - d6 < d8) {
                d8 = 1.0 - d6;
                n5 = 3;
            }
            if (bl6 && d7 < d8) {
                d8 = d7;
                n5 = 4;
            }
            if (bl7 && 1.0 - d7 < d8) {
                d8 = 1.0 - d7;
                n5 = 5;
            }
            float f2 = this.bs.nextFloat() * 0.2f + 0.1f;
            if (n5 == 0) {
                this.aP = -f2;
            }
            if (n5 == 1) {
                this.aP = f2;
            }
            if (n5 == 2) {
                this.aQ = -f2;
            }
            if (n5 == 3) {
                this.aQ = f2;
            }
            if (n5 == 4) {
                this.aR = -f2;
            }
            if (n5 == 5) {
                this.aR = f2;
            }
        }
        return false;
    }
}

