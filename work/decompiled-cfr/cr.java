/*
 * Decompiled with CFR 0.152.
 */
public class cr
extends xw {
    public cr(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        super(fd2, d2, d3, d4, d5, d6, d7);
        this.i = 1.0f;
        this.j = 1.0f;
        this.k = 1.0f;
        this.b = 32;
        this.b(0.02f, 0.02f);
        this.g *= this.bs.nextFloat() * 0.6f + 0.2f;
        this.aP = d5 * (double)0.2f + (double)((float)(Math.random() * 2.0 - 1.0) * 0.02f);
        this.aQ = d6 * (double)0.2f + (double)((float)(Math.random() * 2.0 - 1.0) * 0.02f);
        this.aR = d7 * (double)0.2f + (double)((float)(Math.random() * 2.0 - 1.0) * 0.02f);
        this.f = (int)(8.0 / (Math.random() * 0.8 + 0.2));
    }

    public void w_() {
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        this.aQ += 0.002;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.85f;
        this.aQ *= (double)0.85f;
        this.aR *= (double)0.85f;
        if (this.aI.f(in.b(this.aM), in.b(this.aN), in.b(this.aO)) != ln.g) {
            this.K();
        }
        if (this.f-- <= 0) {
            this.K();
        }
    }
}

