/*
 * Decompiled with CFR 0.152.
 */
class su
extends lg {
    final /* synthetic */ dv a;

    public su(dv dv2) {
        this.a = dv2;
        super(dv.a(dv2), dv2.c, dv2.d, 32, dv2.d - 64, 10);
        this.a(false);
    }

    protected int a() {
        return jl.c.size();
    }

    protected void a(int n2, boolean bl2) {
    }

    protected boolean c_(int n2) {
        return false;
    }

    protected int b() {
        return this.a() * 10;
    }

    protected void c() {
        this.a.i();
    }

    protected void a(int n2, int n3, int n4, int n5, nw nw2) {
        vr vr2 = (vr)jl.c.get(n2);
        this.a.b(dv.b(this.a), vr2.f, n3 + 2, n4 + 1, n2 % 2 == 0 ? 0xFFFFFF : 0x909090);
        String string = vr2.a(dv.c(this.a).a(vr2));
        this.a.b(dv.d(this.a), string, n3 + 2 + 213 - dv.e(this.a).a(string), n4 + 1, n2 % 2 == 0 ? 0xFFFFFF : 0x909090);
    }
}

