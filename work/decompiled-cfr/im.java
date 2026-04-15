/*
 * Decompiled with CFR 0.152.
 */
public class im
extends xw {
    float a;

    public im(fd fd2, double d2, double d3, double d4, float f2, float f3, float f4) {
        this(fd2, d2, d3, d4, 1.0f, f2, f3, f4);
    }

    public im(fd fd2, double d2, double d3, double d4, float f2, float f3, float f4, float f5) {
        super(fd2, d2, d3, d4, 0.0, 0.0, 0.0);
        this.aP *= (double)0.1f;
        this.aQ *= (double)0.1f;
        this.aR *= (double)0.1f;
        if (f3 == 0.0f) {
            f3 = 1.0f;
        }
        float f6 = (float)Math.random() * 0.4f + 0.6f;
        this.i = ((float)(Math.random() * (double)0.2f) + 0.8f) * f3 * f6;
        this.j = ((float)(Math.random() * (double)0.2f) + 0.8f) * f4 * f6;
        this.k = ((float)(Math.random() * (double)0.2f) + 0.8f) * f5 * f6;
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
        this.b(this.aP, this.aQ, this.aR);
        if (this.aN == this.aK) {
            this.aP *= 1.1;
            this.aR *= 1.1;
        }
        this.aP *= (double)0.96f;
        this.aQ *= (double)0.96f;
        this.aR *= (double)0.96f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }
}

