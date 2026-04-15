/*
 * Decompiled with CFR 0.152.
 */
public class wq
extends ls {
    public wq(fd fd2) {
        super(fd2);
    }

    protected void b(float f2) {
    }

    public void a_(float f2, float f3) {
        if (this.ag()) {
            this.a(f2, f3, 0.02f);
            this.b(this.aP, this.aQ, this.aR);
            this.aP *= (double)0.8f;
            this.aQ *= (double)0.8f;
            this.aR *= (double)0.8f;
        } else if (this.ah()) {
            this.a(f2, f3, 0.02f);
            this.b(this.aP, this.aQ, this.aR);
            this.aP *= 0.5;
            this.aQ *= 0.5;
            this.aR *= 0.5;
        } else {
            float f4 = 0.91f;
            if (this.aX) {
                f4 = 0.54600006f;
                int n2 = this.aI.a(in.b(this.aM), in.b(this.aW.b) - 1, in.b(this.aO));
                if (n2 > 0) {
                    f4 = uu.m[n2].bB * 0.91f;
                }
            }
            float f5 = 0.16277136f / (f4 * f4 * f4);
            this.a(f2, f3, this.aX ? 0.1f * f5 : 0.02f);
            f4 = 0.91f;
            if (this.aX) {
                f4 = 0.54600006f;
                int n3 = this.aI.a(in.b(this.aM), in.b(this.aW.b) - 1, in.b(this.aO));
                if (n3 > 0) {
                    f4 = uu.m[n3].bB * 0.91f;
                }
            }
            this.b(this.aP, this.aQ, this.aR);
            this.aP *= (double)f4;
            this.aQ *= (double)f4;
            this.aR *= (double)f4;
        }
        this.ak = this.al;
        double d2 = this.aM - this.aJ;
        double d3 = this.aO - this.aL;
        float f6 = in.a(d2 * d2 + d3 * d3) * 4.0f;
        if (f6 > 1.0f) {
            f6 = 1.0f;
        }
        this.al += (f6 - this.al) * 0.4f;
        this.am += this.al;
    }

    public boolean p() {
        return false;
    }
}

