/*
 * Decompiled with CFR 0.152.
 */
public class kc
extends xw {
    float a;

    public kc(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        this(fd2, d2, d3, d4, d5, d6, d7, 2.0f);
    }

    public kc(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7, float f2) {
        super(fd2, d2, d3, d4, 0.0, 0.0, 0.0);
        this.aP *= (double)0.01f;
        this.aQ *= (double)0.01f;
        this.aR *= (double)0.01f;
        this.aQ += 0.1;
        this.g *= 0.75f;
        this.g *= f2;
        this.a = this.g;
        this.f = 16;
        this.bq = false;
        this.b = 80;
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
        this.b(this.aP, this.aQ, this.aR);
        if (this.aN == this.aK) {
            this.aP *= 1.1;
            this.aR *= 1.1;
        }
        this.aP *= (double)0.86f;
        this.aQ *= (double)0.86f;
        this.aR *= (double)0.86f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }
}

