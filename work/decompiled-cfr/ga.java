/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;

class ga
extends iv {
    final /* synthetic */ dv a;

    public ga(dv dv2) {
        this.a = dv2;
        super(dv2);
        this.c = new ArrayList();
        for (tw tw2 : jl.e) {
            boolean bl2 = false;
            int n2 = tw2.a();
            if (dv.c(dv2).a(tw2) > 0) {
                bl2 = true;
            } else if (jl.E[n2] != null && dv.c(dv2).a(jl.E[n2]) > 0) {
                bl2 = true;
            } else if (jl.D[n2] != null && dv.c(dv2).a(jl.D[n2]) > 0) {
                bl2 = true;
            }
            if (!bl2) continue;
            this.c.add(tw2);
        }
        this.d = new pr(this, dv2);
    }

    protected void a(int n2, int n3, nw nw2) {
        super.a(n2, n3, nw2);
        if (this.b == 0) {
            dv.a(this.a, n2 + 115 - 18 + 1, n3 + 1 + 1, 18, 18);
        } else {
            dv.a(this.a, n2 + 115 - 18, n3 + 1, 18, 18);
        }
        if (this.b == 1) {
            dv.a(this.a, n2 + 165 - 18 + 1, n3 + 1 + 1, 36, 18);
        } else {
            dv.a(this.a, n2 + 165 - 18, n3 + 1, 36, 18);
        }
        if (this.b == 2) {
            dv.a(this.a, n2 + 215 - 18 + 1, n3 + 1 + 1, 54, 18);
        } else {
            dv.a(this.a, n2 + 215 - 18, n3 + 1, 54, 18);
        }
    }

    protected void a(int n2, int n3, int n4, int n5, nw nw2) {
        tw tw2 = this.b(n2);
        int n6 = tw2.a();
        dv.a(this.a, n3 + 40, n4, n6);
        this.a((tw)jl.D[n6], n3 + 115, n4, n2 % 2 == 0);
        this.a((tw)jl.E[n6], n3 + 165, n4, n2 % 2 == 0);
        this.a(tw2, n3 + 215, n4, n2 % 2 == 0);
    }

    protected String a(int n2) {
        if (n2 == 0) {
            return "stat.crafted";
        }
        if (n2 == 1) {
            return "stat.used";
        }
        return "stat.mined";
    }
}

