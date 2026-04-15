/*
 * Decompiled with CFR 0.152.
 */
public class qm
extends xw {
    private uu a;
    private int o = 0;

    public qm(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7, uu uu2, int n2, int n3) {
        super(fd2, d2, d3, d4, d5, d6, d7);
        this.a = uu2;
        this.b = uu2.a(0, n3);
        this.h = uu2.bz;
        this.k = 0.6f;
        this.j = 0.6f;
        this.i = 0.6f;
        this.g /= 2.0f;
        this.o = n2;
    }

    public qm a(int n2, int n3, int n4) {
        if (this.a == uu.v) {
            return this;
        }
        int n5 = this.a.b((xp)this.aI, n2, n3, n4);
        this.i *= (float)(n5 >> 16 & 0xFF) / 255.0f;
        this.j *= (float)(n5 >> 8 & 0xFF) / 255.0f;
        this.k *= (float)(n5 & 0xFF) / 255.0f;
        return this;
    }

    public int c_() {
        return 1;
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

