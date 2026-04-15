/*
 * Decompiled with CFR 0.152.
 */
public class pb
extends xw {
    public pb(fd fd2, double d2, double d3, double d4, gm gm2) {
        super(fd2, d2, d3, d4, 0.0, 0.0, 0.0);
        this.b = gm2.a(0);
        this.k = 1.0f;
        this.j = 1.0f;
        this.i = 1.0f;
        this.h = uu.aV.bz;
        this.g /= 2.0f;
    }

    public int c_() {
        return 2;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = ((float)(this.b % 16) + this.c / 4.0f) / 16.0f;
        float f9 = f8 + 0.015609375f;
        float f10 = ((float)(this.b / 16) + this.d / 4.0f) / 16.0f;
        float f11 = f10 + 0.015609375f;
        float f12 = 0.1f * this.g;
        float f13 = (float)(this.aJ + (this.aM - this.aJ) * (double)f2 - l);
        float f14 = (float)(this.aK + (this.aN - this.aK) * (double)f2 - m);
        float f15 = (float)(this.aL + (this.aO - this.aL) * (double)f2 - n);
        float f16 = this.a(f2);
        nw2.a(f16 * this.i, f16 * this.j, f16 * this.k);
        nw2.a(f13 - f3 * f12 - f6 * f12, f14 - f4 * f12, f15 - f5 * f12 - f7 * f12, f8, f11);
        nw2.a(f13 - f3 * f12 + f6 * f12, f14 + f4 * f12, f15 - f5 * f12 + f7 * f12, f8, f10);
        nw2.a(f13 + f3 * f12 + f6 * f12, f14 + f4 * f12, f15 + f5 * f12 + f7 * f12, f9, f10);
        nw2.a(f13 + f3 * f12 - f6 * f12, f14 - f4 * f12, f15 + f5 * f12 - f7 * f12, f9, f11);
    }
}

