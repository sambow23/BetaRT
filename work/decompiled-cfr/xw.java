/*
 * Decompiled with CFR 0.152.
 */
public class xw
extends sn {
    protected int b;
    protected float c;
    protected float d;
    protected int e = 0;
    protected int f = 0;
    protected float g;
    protected float h;
    protected float i;
    protected float j;
    protected float k;
    public static double l;
    public static double m;
    public static double n;

    public xw(fd fd2, double d2, double d3, double d4, double d5, double d6, double d7) {
        super(fd2);
        this.b(0.2f, 0.2f);
        this.bf = this.bh / 2.0f;
        this.e(d2, d3, d4);
        this.k = 1.0f;
        this.j = 1.0f;
        this.i = 1.0f;
        this.aP = d5 + (double)((float)(Math.random() * 2.0 - 1.0) * 0.4f);
        this.aQ = d6 + (double)((float)(Math.random() * 2.0 - 1.0) * 0.4f);
        this.aR = d7 + (double)((float)(Math.random() * 2.0 - 1.0) * 0.4f);
        float f2 = (float)(Math.random() + Math.random() + 1.0) * 0.15f;
        float f3 = in.a(this.aP * this.aP + this.aQ * this.aQ + this.aR * this.aR);
        this.aP = this.aP / (double)f3 * (double)f2 * (double)0.4f;
        this.aQ = this.aQ / (double)f3 * (double)f2 * (double)0.4f + (double)0.1f;
        this.aR = this.aR / (double)f3 * (double)f2 * (double)0.4f;
        this.c = this.bs.nextFloat() * 3.0f;
        this.d = this.bs.nextFloat() * 3.0f;
        this.g = (this.bs.nextFloat() * 0.5f + 0.5f) * 2.0f;
        this.f = (int)(4.0f / (this.bs.nextFloat() * 0.9f + 0.1f));
        this.e = 0;
    }

    public xw c(float f2) {
        this.aP *= (double)f2;
        this.aQ = (this.aQ - (double)0.1f) * (double)f2 + (double)0.1f;
        this.aR *= (double)f2;
        return this;
    }

    public xw d(float f2) {
        this.b(0.2f * f2, 0.2f * f2);
        this.g *= f2;
        return this;
    }

    protected boolean n() {
        return false;
    }

    protected void b() {
    }

    public void w_() {
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        if (this.e++ >= this.f) {
            this.K();
        }
        this.aQ -= 0.04 * (double)this.h;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.98f;
        this.aQ *= (double)0.98f;
        this.aR *= (double)0.98f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
        }
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = (float)(this.b % 16) / 16.0f;
        float f9 = f8 + 0.0624375f;
        float f10 = (float)(this.b / 16) / 16.0f;
        float f11 = f10 + 0.0624375f;
        float f12 = 0.1f * this.g;
        float f13 = (float)(this.aJ + (this.aM - this.aJ) * (double)f2 - l);
        float f14 = (float)(this.aK + (this.aN - this.aK) * (double)f2 - m);
        float f15 = (float)(this.aL + (this.aO - this.aL) * (double)f2 - n);
        float f16 = this.a(f2);
        nw2.a(this.i * f16, this.j * f16, this.k * f16);
        nw2.a(f13 - f3 * f12 - f6 * f12, f14 - f4 * f12, f15 - f5 * f12 - f7 * f12, f9, f11);
        nw2.a(f13 - f3 * f12 + f6 * f12, f14 + f4 * f12, f15 - f5 * f12 + f7 * f12, f9, f10);
        nw2.a(f13 + f3 * f12 + f6 * f12, f14 + f4 * f12, f15 + f5 * f12 + f7 * f12, f8, f10);
        nw2.a(f13 + f3 * f12 - f6 * f12, f14 - f4 * f12, f15 + f5 * f12 - f7 * f12, f8, f11);
    }

    public int c_() {
        return 0;
    }

    public void b(nu nu2) {
    }

    public void a(nu nu2) {
    }
}

