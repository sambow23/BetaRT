/*
 * Decompiled with CFR 0.152.
 */
public class bp
extends wq
implements ff {
    public int a = 0;
    public double b;
    public double c;
    public double d;
    private sn g = null;
    private int h = 0;
    public int e = 0;
    public int f = 0;

    public bp(fd fd2) {
        super(fd2);
        this.O = "/mob/ghast.png";
        this.b(4.0f, 4.0f);
        this.bC = true;
    }

    protected void b() {
        super.b();
        this.bD.a(16, (byte)0);
    }

    public void w_() {
        super.w_();
        byte by2 = this.bD.a(16);
        this.O = by2 == 1 ? "/mob/ghast_fire.png" : "/mob/ghast.png";
    }

    protected void f_() {
        byte by2;
        byte by3;
        if (!this.aI.B && this.aI.q == 0) {
            this.K();
        }
        this.X();
        this.e = this.f;
        double d2 = this.b - this.aM;
        double d3 = this.c - this.aN;
        double d4 = this.d - this.aO;
        double d5 = in.a(d2 * d2 + d3 * d3 + d4 * d4);
        if (d5 < 1.0 || d5 > 60.0) {
            this.b = this.aM + (double)((this.bs.nextFloat() * 2.0f - 1.0f) * 16.0f);
            this.c = this.aN + (double)((this.bs.nextFloat() * 2.0f - 1.0f) * 16.0f);
            this.d = this.aO + (double)((this.bs.nextFloat() * 2.0f - 1.0f) * 16.0f);
        }
        if (this.a-- <= 0) {
            this.a += this.bs.nextInt(5) + 2;
            if (this.a(this.b, this.c, this.d, d5)) {
                this.aP += d2 / d5 * 0.1;
                this.aQ += d3 / d5 * 0.1;
                this.aR += d4 / d5 * 0.1;
            } else {
                this.b = this.aM;
                this.c = this.aN;
                this.d = this.aO;
            }
        }
        if (this.g != null && this.g.be) {
            this.g = null;
        }
        if (this.g == null || this.h-- <= 0) {
            this.g = this.aI.a((sn)this, 100.0);
            if (this.g != null) {
                this.h = 20;
            }
        }
        double d6 = 64.0;
        if (this.g != null && this.g.g(this) < d6 * d6) {
            double d7 = this.g.aM - this.aM;
            double d8 = this.g.aW.b + (double)(this.g.bh / 2.0f) - (this.aN + (double)(this.bh / 2.0f));
            double d9 = this.g.aO - this.aO;
            this.H = this.aS = -((float)Math.atan2(d7, d9)) * 180.0f / (float)Math.PI;
            if (this.e(this.g)) {
                if (this.f == 10) {
                    this.aI.a(this, "mob.ghast.charge", this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
                }
                ++this.f;
                if (this.f == 20) {
                    this.aI.a(this, "mob.ghast.fireball", this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
                    cf cf2 = new cf(this.aI, this, d7, d8, d9);
                    double d10 = 4.0;
                    bt bt2 = this.f(1.0f);
                    cf2.aM = this.aM + bt2.a * d10;
                    cf2.aN = this.aN + (double)(this.bh / 2.0f) + 0.5;
                    cf2.aO = this.aO + bt2.c * d10;
                    this.aI.b(cf2);
                    this.f = -40;
                }
            } else if (this.f > 0) {
                --this.f;
            }
        } else {
            this.H = this.aS = -((float)Math.atan2(this.aP, this.aR)) * 180.0f / (float)Math.PI;
            if (this.f > 0) {
                --this.f;
            }
        }
        if (!this.aI.B && (by3 = this.bD.a(16)) != (by2 = (byte)(this.f > 10 ? 1 : 0))) {
            this.bD.b(16, by2);
        }
    }

    private boolean a(double d2, double d3, double d4, double d5) {
        double d6 = (this.b - this.aM) / d5;
        double d7 = (this.c - this.aN) / d5;
        double d8 = (this.d - this.aO) / d5;
        eq eq2 = this.aW.d();
        int n2 = 1;
        while ((double)n2 < d5) {
            eq2.d(d6, d7, d8);
            if (this.aI.a((sn)this, eq2).size() > 0) {
                return false;
            }
            ++n2;
        }
        return true;
    }

    protected String g() {
        return "mob.ghast.moan";
    }

    protected String j_() {
        return "mob.ghast.scream";
    }

    protected String i() {
        return "mob.ghast.death";
    }

    protected int j() {
        return gm.K.bf;
    }

    protected float k() {
        return 10.0f;
    }

    public boolean d() {
        return this.bs.nextInt(20) == 0 && super.d() && this.aI.q > 0;
    }

    public int l() {
        return 1;
    }
}

