/*
 * Decompiled with CFR 0.152.
 */
import java.util.Date;

class mg
extends lg {
    final /* synthetic */ rq a;

    public mg(rq rq2) {
        this.a = rq2;
        super(rq2.b, rq2.c, rq2.d, 32, rq2.d - 64, 36);
    }

    protected int a() {
        return rq.a(this.a).size();
    }

    protected void a(int n2, boolean bl2) {
        boolean bl3;
        rq.a(this.a, n2);
        rq.c((rq)this.a).g = bl3 = rq.b(this.a) >= 0 && rq.b(this.a) < this.a();
        rq.d((rq)this.a).g = bl3;
        rq.e((rq)this.a).g = bl3;
        if (bl2 && bl3) {
            this.a.e(n2);
        }
    }

    protected boolean c_(int n2) {
        return n2 == rq.b(this.a);
    }

    protected int b() {
        return rq.a(this.a).size() * 36;
    }

    protected void c() {
        this.a.i();
    }

    protected void a(int n2, int n3, int n4, int n5, nw nw2) {
        vb vb2 = (vb)rq.a(this.a).get(n2);
        String string = vb2.b();
        if (string == null || in.a(string)) {
            string = rq.f(this.a) + " " + (n2 + 1);
        }
        String string2 = vb2.a();
        string2 = string2 + " (" + rq.g(this.a).format(new Date(vb2.e()));
        long l2 = vb2.c();
        string2 = string2 + ", " + (float)(l2 / 1024L * 100L / 1024L) / 100.0f + " MB)";
        String string3 = "";
        if (vb2.d()) {
            string3 = rq.h(this.a) + " " + string3;
        }
        this.a.b(this.a.g, string, n3 + 2, n4 + 1, 0xFFFFFF);
        this.a.b(this.a.g, string2, n3 + 2, n4 + 12, 0x808080);
        this.a.b(this.a.g, string3, n3 + 2, n4 + 12 + 10, 0x808080);
    }
}

