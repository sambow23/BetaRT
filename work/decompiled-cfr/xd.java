/*
 * Decompiled with CFR 0.152.
 */
public class xd
extends xw {
    public xd(fd fd2, double d2, double d3, double d4) {
        super(fd2, d2, d3, d4, 0.0, 0.0, 0.0);
        this.aP *= (double)0.3f;
        this.aQ = (float)Math.random() * 0.2f + 0.1f;
        this.aR *= (double)0.3f;
        this.i = 1.0f;
        this.j = 1.0f;
        this.k = 1.0f;
        this.b = 19 + this.bs.nextInt(4);
        this.b(0.01f, 0.01f);
        this.h = 0.06f;
        this.f = (int)(8.0 / (Math.random() * 0.8 + 0.2));
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        super.a(nw2, f2, f3, f4, f5, f6, f7);
    }

    public void w_() {
        double d2;
        ln ln2;
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        this.aQ -= (double)this.h;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.98f;
        this.aQ *= (double)0.98f;
        this.aR *= (double)0.98f;
        if (this.f-- <= 0) {
            this.K();
        }
        if (this.aX) {
            if (Math.random() < 0.5) {
                this.K();
            }
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
        if (((ln2 = this.aI.f(in.b(this.aM), in.b(this.aN), in.b(this.aO))).d() || ln2.a()) && this.aN < (d2 = (double)((float)(in.b(this.aN) + 1) - rp.d(this.aI.e(in.b(this.aM), in.b(this.aN), in.b(this.aO)))))) {
            this.K();
        }
    }
}

