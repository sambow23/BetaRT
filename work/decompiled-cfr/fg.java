/*
 * Decompiled with CFR 0.152.
 */
public class fg
extends xw {
    private float a;

    public fg(fd fd2, double d2, double d3, double d4) {
        super(fd2, d2, d3, d4, 0.0, 0.0, 0.0);
        this.aP *= (double)0.8f;
        this.aQ *= (double)0.8f;
        this.aR *= (double)0.8f;
        this.aQ = this.bs.nextFloat() * 0.4f + 0.05f;
        this.k = 1.0f;
        this.j = 1.0f;
        this.i = 1.0f;
        this.g *= this.bs.nextFloat() * 2.0f + 0.2f;
        this.a = this.g;
        this.f = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.bq = false;
        this.b = 49;
    }

    public float a(float f2) {
        return 1.0f;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = ((float)this.e + f2) / (float)this.f;
        this.g = this.a * (1.0f - f8 * f8);
        super.a(nw2, f2, f3, f4, f5, f6, f7);
    }

    public void w_() {
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        if (this.e++ >= this.f) {
            this.K();
        }
        float f2 = (float)this.e / (float)this.f;
        if (this.bs.nextFloat() > f2) {
            this.aI.a("smoke", this.aM, this.aN, this.aO, this.aP, this.aQ, this.aR);
        }
        this.aQ -= 0.03;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.999f;
        this.aQ *= (double)0.999f;
        this.aR *= (double)0.999f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }
}

