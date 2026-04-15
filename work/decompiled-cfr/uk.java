/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

public class uk
extends ow {
    private int a;
    private int b;
    private int c;
    private boolean i;
    private boolean j;
    private float k;
    private float l;
    private static List m = new ArrayList();

    public uk() {
    }

    public uk(int n2, int n3, int n4, boolean bl2, boolean bl3) {
        this.a = n2;
        this.b = n3;
        this.c = n4;
        this.i = bl2;
        this.j = bl3;
    }

    public int a() {
        return this.a;
    }

    public int e() {
        return this.b;
    }

    public boolean b() {
        return this.i;
    }

    public int d() {
        return this.c;
    }

    public boolean k() {
        return this.j;
    }

    public float a(float f2) {
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        return this.l + (this.k - this.l) * f2;
    }

    public float b(float f2) {
        if (this.i) {
            return (this.a(f2) - 1.0f) * (float)wj.b[this.c];
        }
        return (1.0f - this.a(f2)) * (float)wj.b[this.c];
    }

    public float c(float f2) {
        if (this.i) {
            return (this.a(f2) - 1.0f) * (float)wj.c[this.c];
        }
        return (1.0f - this.a(f2)) * (float)wj.c[this.c];
    }

    public float d(float f2) {
        if (this.i) {
            return (this.a(f2) - 1.0f) * (float)wj.d[this.c];
        }
        return (1.0f - this.a(f2)) * (float)wj.d[this.c];
    }

    private void a(float f2, float f3) {
        List list;
        f2 = !this.i ? (f2 -= 1.0f) : 1.0f - f2;
        eq eq2 = uu.ad.a(this.d, this.e, this.f, this.g, this.a, f2, this.c);
        if (eq2 != null && !(list = this.d.b(null, eq2)).isEmpty()) {
            m.addAll(list);
            for (sn sn2 : m) {
                sn2.b(f3 * (float)wj.b[this.c], f3 * (float)wj.c[this.c], f3 * (float)wj.d[this.c]);
            }
            m.clear();
        }
    }

    public void l() {
        if (this.l < 1.0f) {
            this.k = 1.0f;
            this.l = 1.0f;
            this.d.p(this.e, this.f, this.g);
            this.i();
            if (this.d.a(this.e, this.f, this.g) == uu.ad.bn) {
                this.d.b(this.e, this.f, this.g, this.a, this.b);
            }
        }
    }

    public void n_() {
        this.l = this.k;
        if (this.l >= 1.0f) {
            this.a(1.0f, 0.25f);
            this.d.p(this.e, this.f, this.g);
            this.i();
            if (this.d.a(this.e, this.f, this.g) == uu.ad.bn) {
                this.d.b(this.e, this.f, this.g, this.a, this.b);
            }
            return;
        }
        this.k += 0.5f;
        if (this.k >= 1.0f) {
            this.k = 1.0f;
        }
        if (this.i) {
            this.a(this.k, this.k - this.l + 0.0625f);
        }
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.a = nu2.e("blockId");
        this.b = nu2.e("blockData");
        this.c = nu2.e("facing");
        this.l = this.k = nu2.g("progress");
        this.i = nu2.m("extending");
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("blockId", this.a);
        nu2.a("blockData", this.b);
        nu2.a("facing", this.c);
        nu2.a("progress", this.l);
        nu2.a("extending", this.i);
    }
}

