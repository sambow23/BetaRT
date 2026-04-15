/*
 * Decompiled with CFR 0.152.
 */
public class xz
extends gs {
    private int b;
    private double bN;
    private double bO;
    private double bP;
    private double bQ;
    private double bR;
    float a = 0.0f;

    public xz(fd fd2, String string) {
        super(fd2);
        this.l = string;
        this.bf = 0.0f;
        this.bp = 0.0f;
        if (string != null && string.length() > 0) {
            this.bA = "http://s3.amazonaws.com/MinecraftSkins/" + string + ".png";
        }
        this.bq = true;
        this.x = 0.25f;
        this.aE = 10.0;
    }

    protected void E() {
        this.bf = 0.0f;
    }

    public boolean a(sn sn2, int n2) {
        return true;
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2) {
        this.bN = d2;
        this.bO = d3;
        this.bP = d4;
        this.bQ = f2;
        this.bR = f3;
        this.b = n2;
    }

    public void w_() {
        this.x = 0.0f;
        super.w_();
        this.ak = this.al;
        double d2 = this.aM - this.aJ;
        double d3 = this.aO - this.aL;
        float f2 = in.a(d2 * d2 + d3 * d3) * 4.0f;
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        this.al += (f2 - this.al) * 0.4f;
        this.am += this.al;
    }

    public float x_() {
        return 0.0f;
    }

    public void o() {
        super.f_();
        if (this.b > 0) {
            double d2;
            double d3 = this.aM + (this.bN - this.aM) / (double)this.b;
            double d4 = this.aN + (this.bO - this.aN) / (double)this.b;
            double d5 = this.aO + (this.bP - this.aO) / (double)this.b;
            for (d2 = this.bQ - (double)this.aS; d2 < -180.0; d2 += 360.0) {
            }
            while (d2 >= 180.0) {
                d2 -= 360.0;
            }
            this.aS = (float)((double)this.aS + d2 / (double)this.b);
            this.aT = (float)((double)this.aT + (this.bR - (double)this.aT) / (double)this.b);
            --this.b;
            this.e(d3, d4, d5);
            this.c(this.aS, this.aT);
        }
        this.h = this.i;
        float f2 = in.a(this.aP * this.aP + this.aR * this.aR);
        float f3 = (float)Math.atan(-this.aQ * (double)0.2f) * 15.0f;
        if (f2 > 0.1f) {
            f2 = 0.1f;
        }
        if (!this.aX || this.Y <= 0) {
            f2 = 0.0f;
        }
        if (this.aX || this.Y <= 0) {
            f3 = 0.0f;
        }
        this.i += (f2 - this.i) * 0.4f;
        this.ag += (f3 - this.ag) * 0.8f;
    }

    public void c(int n2, int n3, int n4) {
        iz iz2 = null;
        if (n3 >= 0) {
            iz2 = new iz(n3, 1, n4);
        }
        if (n2 == 0) {
            this.c.a[this.c.c] = iz2;
        } else {
            this.c.b[n2 - 1] = iz2;
        }
    }

    public void v() {
    }
}

