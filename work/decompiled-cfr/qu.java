/*
 * Decompiled with CFR 0.152.
 */
public class qu
extends xw {
    private float a;

    public qu(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        super(fd2, d2, d3, d4, d5, d6, d7);
        this.aP = this.aP * (double)0.01f + d5;
        this.aQ = this.aQ * (double)0.01f + d6;
        this.aR = this.aR * (double)0.01f + d7;
        d2 += (double)((this.bs.nextFloat() - this.bs.nextFloat()) * 0.05f);
        d3 += (double)((this.bs.nextFloat() - this.bs.nextFloat()) * 0.05f);
        d4 += (double)((this.bs.nextFloat() - this.bs.nextFloat()) * 0.05f);
        this.a = this.g;
        this.k = 1.0f;
        this.j = 1.0f;
        this.i = 1.0f;
        this.f = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
        this.bq = true;
        this.b = 48;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = ((float)this.e + f2) / (float)this.f;
        this.g = this.a * (1.0f - f8 * f8 * 0.5f);
        super.a(nw2, f2, f3, f4, f5, f6, f7);
    }

    public float a(float f2) {
        float f3 = ((float)this.e + f2) / (float)this.f;
        if (f3 < 0.0f) {
            f3 = 0.0f;
        }
        if (f3 > 1.0f) {
            f3 = 1.0f;
        }
        float f4 = super.a(f2);
        return f4 * f3 + (1.0f - f3);
    }

    public void w_() {
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        if (this.e++ >= this.f) {
            this.K();
        }
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.96f;
        this.aQ *= (double)0.96f;
        this.aR *= (double)0.96f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }
}

