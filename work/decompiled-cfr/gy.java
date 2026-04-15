/*
 * Decompiled with CFR 0.152.
 */
public class gy
extends xw {
    public gy(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        super(fd2, d2, d3, d4, d5, d6, d7);
        this.aP = d5 + (double)((float)(Math.random() * 2.0 - 1.0) * 0.05f);
        this.aQ = d6 + (double)((float)(Math.random() * 2.0 - 1.0) * 0.05f);
        this.aR = d7 + (double)((float)(Math.random() * 2.0 - 1.0) * 0.05f);
        this.j = this.k = this.bs.nextFloat() * 0.3f + 0.7f;
        this.i = this.k;
        this.g = this.bs.nextFloat() * this.bs.nextFloat() * 6.0f + 1.0f;
        this.f = (int)(16.0 / ((double)this.bs.nextFloat() * 0.8 + 0.2)) + 2;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
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
        this.aQ += 0.004;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.9f;
        this.aQ *= (double)0.9f;
        this.aR *= (double)0.9f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }
}

