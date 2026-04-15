/*
 * Decompiled with CFR 0.152.
 */
public class bc
extends gm {
    private int a;

    public bc(int n2, int n3) {
        super(n2);
        this.bg = 1;
        this.a = n3;
    }

    public iz a(iz iz2, fd fd2, gs gs2) {
        float f2;
        float f3;
        float f4;
        double d2;
        float f5;
        float f6 = 1.0f;
        float f7 = gs2.aV + (gs2.aT - gs2.aV) * f6;
        float f8 = gs2.aU + (gs2.aS - gs2.aU) * f6;
        double d3 = gs2.aJ + (gs2.aM - gs2.aJ) * (double)f6;
        double d4 = gs2.aK + (gs2.aN - gs2.aK) * (double)f6 + 1.62 - (double)gs2.bf;
        double d5 = gs2.aL + (gs2.aO - gs2.aL) * (double)f6;
        bt bt2 = bt.b(d3, d4, d5);
        float f9 = in.b(-f8 * ((float)Math.PI / 180) - (float)Math.PI);
        float f10 = in.a(-f8 * ((float)Math.PI / 180) - (float)Math.PI);
        float f11 = f10 * (f5 = -in.b(-f7 * ((float)Math.PI / 180)));
        bt bt3 = bt2.c((double)f11 * (d2 = 5.0), (double)(f4 = (f3 = in.a(-f7 * ((float)Math.PI / 180)))) * d2, (double)(f2 = f9 * f5) * d2);
        vf vf2 = fd2.a(bt2, bt3, this.a == 0);
        if (vf2 == null) {
            return iz2;
        }
        if (vf2.a == jg.a) {
            int n2 = vf2.b;
            int n3 = vf2.c;
            int n4 = vf2.d;
            if (!fd2.a(gs2, n2, n3, n4)) {
                return iz2;
            }
            if (this.a == 0) {
                if (fd2.f(n2, n3, n4) == ln.g && fd2.e(n2, n3, n4) == 0) {
                    fd2.f(n2, n3, n4, 0);
                    return new iz(gm.av);
                }
                if (fd2.f(n2, n3, n4) == ln.h && fd2.e(n2, n3, n4) == 0) {
                    fd2.f(n2, n3, n4, 0);
                    return new iz(gm.aw);
                }
            } else {
                if (this.a < 0) {
                    return new iz(gm.au);
                }
                if (vf2.e == 0) {
                    --n3;
                }
                if (vf2.e == 1) {
                    ++n3;
                }
                if (vf2.e == 2) {
                    --n4;
                }
                if (vf2.e == 3) {
                    ++n4;
                }
                if (vf2.e == 4) {
                    --n2;
                }
                if (vf2.e == 5) {
                    ++n2;
                }
                if (fd2.d(n2, n3, n4) || !fd2.f(n2, n3, n4).a()) {
                    if (fd2.t.d && this.a == uu.B.bn) {
                        fd2.a(d3 + 0.5, d4 + 0.5, d5 + 0.5, "random.fizz", 0.5f, 2.6f + (fd2.r.nextFloat() - fd2.r.nextFloat()) * 0.8f);
                        for (int i2 = 0; i2 < 8; ++i2) {
                            fd2.a("largesmoke", (double)n2 + Math.random(), (double)n3 + Math.random(), (double)n4 + Math.random(), 0.0, 0.0, 0.0);
                        }
                    } else {
                        fd2.b(n2, n3, n4, this.a, 0);
                    }
                    return new iz(gm.au);
                }
            }
        } else if (this.a == 0 && vf2.g instanceof bx) {
            return new iz(gm.aE);
        }
        return iz2;
    }
}

