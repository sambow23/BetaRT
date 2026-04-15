/*
 * Decompiled with CFR 0.152.
 */
public class cn
extends gz {
    public cn(fd fd2) {
        super(fd2);
        this.O = "/mob/spider.png";
        this.b(1.4f, 0.9f);
        this.aB = 0.8f;
    }

    public double m() {
        return (double)this.bh * 0.75 - 0.5;
    }

    protected boolean n() {
        return false;
    }

    protected sn g_() {
        float f2 = this.a(1.0f);
        if (f2 < 0.5f) {
            double d2 = 16.0;
            return this.aI.a((sn)this, d2);
        }
        return null;
    }

    protected String g() {
        return "mob.spider";
    }

    protected String j_() {
        return "mob.spider";
    }

    protected String i() {
        return "mob.spiderdeath";
    }

    protected void a(sn sn2, float f2) {
        float f3 = this.a(1.0f);
        if (f3 > 0.5f && this.bs.nextInt(100) == 0) {
            this.d = null;
            return;
        }
        if (f2 > 2.0f && f2 < 6.0f && this.bs.nextInt(10) == 0) {
            if (this.aX) {
                double d2 = sn2.aM - this.aM;
                double d3 = sn2.aO - this.aO;
                float f4 = in.a(d2 * d2 + d3 * d3);
                this.aP = d2 / (double)f4 * 0.5 * (double)0.8f + this.aP * (double)0.2f;
                this.aR = d3 / (double)f4 * 0.5 * (double)0.8f + this.aR * (double)0.2f;
                this.aQ = 0.4f;
            }
        } else {
            super.a(sn2, f2);
        }
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    protected int j() {
        return gm.I.bf;
    }

    public boolean p() {
        return this.aY;
    }
}

