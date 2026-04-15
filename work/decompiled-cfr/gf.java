/*
 * Decompiled with CFR 0.152.
 */
import java.util.Comparator;

class gf
implements Comparator {
    final /* synthetic */ dv a;
    final /* synthetic */ ci b;

    gf(ci ci2, dv dv2) {
        this.b = ci2;
        this.a = dv2;
    }

    public int a(tw tw2, tw tw3) {
        int n2 = tw2.a();
        int n3 = tw3.a();
        vr vr2 = null;
        vr vr3 = null;
        if (this.b.e == 0) {
            vr2 = jl.F[n2];
            vr3 = jl.F[n3];
        } else if (this.b.e == 1) {
            vr2 = jl.D[n2];
            vr3 = jl.D[n3];
        } else if (this.b.e == 2) {
            vr2 = jl.E[n2];
            vr3 = jl.E[n3];
        }
        if (vr2 != null || vr3 != null) {
            int n4;
            if (vr2 == null) {
                return 1;
            }
            if (vr3 == null) {
                return -1;
            }
            int n5 = dv.c(this.b.a).a(vr2);
            if (n5 != (n4 = dv.c(this.b.a).a(vr3))) {
                return (n5 - n4) * this.b.f;
            }
        }
        return n2 - n3;
    }
}

