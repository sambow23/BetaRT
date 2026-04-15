/*
 * Decompiled with CFR 0.152.
 */
public class vd
extends gm {
    public vd(int n2) {
        super(n2);
        this.bg = 1;
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
        vf vf2 = fd2.a(bt2, bt3, true);
        if (vf2 == null) {
            return iz2;
        }
        if (vf2.a == jg.a) {
            int n2 = vf2.b;
            int n3 = vf2.c;
            int n4 = vf2.d;
            if (!fd2.B) {
                if (fd2.a(n2, n3, n4) == uu.aT.bn) {
                    --n3;
                }
                fd2.b(new fz(fd2, (float)n2 + 0.5f, (float)n3 + 1.0f, (float)n4 + 0.5f));
            }
            --iz2.a;
        }
        return iz2;
    }
}

