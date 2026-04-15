/*
 * Decompiled with CFR 0.152.
 */
public class mu
extends xw {
    float a;

    public mu(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        this(fd2, d2, d3, d4, d5, d6, d7, 1.0f);
    }

    public mu(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7, float f2) {
        super(fd2, d2, d3, d4, d5, d6, d7);
        this.aP *= (double)0.1f;
        this.aQ *= (double)0.1f;
        this.aR *= (double)0.1f;
        this.aP += d5;
        this.aQ += d6;
        this.aR += d7;
        this.j = this.k = 1.0f - (float)(Math.random() * (double)0.3f);
        this.i = this.k;
        this.g *= 0.75f;
        this.g *= f2;
        this.a = this.g;
        this.f = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.f = (int)((float)this.f * f2);
        this.bq = false;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = ((float)this.e + f2) / (float)this.f * 32.0f;
        if (f8 < 0.0f) {
            f8 = 0.0f;
        }
        if (f8 > 1.0f) {
            f8 = 1.0f;
        }
        this.g = this.a * f8;
        super.a(nw2, f2, f3, f4, f5, f6, f7);
    }

    public void w_() {
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        if (this.e++ >= this.f) {
            this.K();
        }
        this.b = 7 - this.e * 8 / this.f;
        this.aQ -= 0.03;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.99f;
        this.aQ *= (double)0.99f;
        this.aR *= (double)0.99f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }
}

