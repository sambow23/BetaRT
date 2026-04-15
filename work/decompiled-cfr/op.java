/*
 * Decompiled with CFR 0.152.
 */
public class op
extends xw {
    private float a;
    private double o;
    private double p;
    private double q;

    public op(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        super(fd2, d2, d3, d4, d5, d6, d7);
        this.aP = d5;
        this.aQ = d6;
        this.aR = d7;
        this.o = this.aM = d2;
        this.p = this.aN = d3;
        this.q = this.aO = d4;
        float f2 = this.bs.nextFloat() * 0.6f + 0.4f;
        this.a = this.g = this.bs.nextFloat() * 0.2f + 0.5f;
        this.j = this.k = 1.0f * f2;
        this.i = this.k;
        this.j *= 0.3f;
        this.i *= 0.9f;
        this.f = (int)(Math.random() * 10.0) + 40;
        this.bq = true;
        this.b = (int)(Math.random() * 8.0);
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = ((float)this.e + f2) / (float)this.f;
        f8 = 1.0f - f8;
        f8 *= f8;
        f8 = 1.0f - f8;
        this.g = this.a * f8;
        super.a(nw2, f2, f3, f4, f5, f6, f7);
    }

    public float a(float f2) {
        float f3 = super.a(f2);
        float f4 = (float)this.e / (float)this.f;
        f4 *= f4;
        f4 *= f4;
        return f3 * (1.0f - f4) + f4;
    }

    public void w_() {
        float f2;
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        float f3 = f2 = (float)this.e / (float)this.f;
        f2 = -f2 + f2 * f2 * 2.0f;
        f2 = 1.0f - f2;
        this.aM = this.o + this.aP * (double)f2;
        this.aN = this.p + this.aQ * (double)f2 + (double)(1.0f - f3);
        this.aO = this.q + this.aR * (double)f2;
        if (this.e++ >= this.f) {
            this.K();
        }
    }
}

