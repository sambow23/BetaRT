/*
 * Decompiled with CFR 0.152.
 */
public final class iz {
    public int a = 0;
    public int b;
    public int c;
    private int d;

    public iz(uu uu2) {
        this(uu2, 1);
    }

    public iz(uu uu2, int n2) {
        this(uu2.bn, n2, 0);
    }

    public iz(uu uu2, int n2, int n3) {
        this(uu2.bn, n2, n3);
    }

    public iz(gm gm2) {
        this(gm2.bf, 1, 0);
    }

    public iz(gm gm2, int n2) {
        this(gm2.bf, n2, 0);
    }

    public iz(gm gm2, int n2, int n3) {
        this(gm2.bf, n2, n3);
    }

    public iz(int n2, int n3, int n4) {
        this.c = n2;
        this.a = n3;
        this.d = n4;
    }

    public iz(nu nu2) {
        this.b(nu2);
    }

    public iz a(int n2) {
        this.a -= n2;
        return new iz(this.c, n2, this.d);
    }

    public gm a() {
        return gm.c[this.c];
    }

    public int b() {
        return this.a().b(this);
    }

    public boolean a(gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        boolean bl2 = this.a().a(this, gs2, fd2, n2, n3, n4, n5);
        if (bl2) {
            gs2.a(jl.E[this.c], 1);
        }
        return bl2;
    }

    public float a(uu uu2) {
        return this.a().a(this, uu2);
    }

    public iz a(fd fd2, gs gs2) {
        return this.a().a(this, fd2, gs2);
    }

    public nu a(nu nu2) {
        nu2.a("id", (short)this.c);
        nu2.a("Count", (byte)this.a);
        nu2.a("Damage", (short)this.d);
        return nu2;
    }

    public void b(nu nu2) {
        this.c = nu2.d("id");
        this.a = nu2.c("Count");
        this.d = nu2.d("Damage");
    }

    public int c() {
        return this.a().d();
    }

    public boolean d() {
        return this.c() > 1 && (!this.e() || !this.g());
    }

    public boolean e() {
        return gm.c[this.c].f() > 0;
    }

    public boolean f() {
        return gm.c[this.c].e();
    }

    public boolean g() {
        return this.e() && this.d > 0;
    }

    public int h() {
        return this.d;
    }

    public int i() {
        return this.d;
    }

    public void b(int n2) {
        this.d = n2;
    }

    public int j() {
        return gm.c[this.c].f();
    }

    public void a(int n2, sn sn2) {
        if (!this.e()) {
            return;
        }
        this.d += n2;
        if (this.d > this.j()) {
            if (sn2 instanceof gs) {
                ((gs)sn2).a(jl.F[this.c], 1);
            }
            --this.a;
            if (this.a < 0) {
                this.a = 0;
            }
            this.d = 0;
        }
    }

    public void a(ls ls2, gs gs2) {
        boolean bl2 = gm.c[this.c].a(this, ls2, (ls)gs2);
        if (bl2) {
            gs2.a(jl.E[this.c], 1);
        }
    }

    public void a(int n2, int n3, int n4, int n5, gs gs2) {
        boolean bl2 = gm.c[this.c].a(this, n2, n3, n4, n5, gs2);
        if (bl2) {
            gs2.a(jl.E[this.c], 1);
        }
    }

    public int a(sn sn2) {
        return gm.c[this.c].a(sn2);
    }

    public boolean b(uu uu2) {
        return gm.c[this.c].a(uu2);
    }

    public void a(gs gs2) {
    }

    public void a(ls ls2) {
        gm.c[this.c].a(this, ls2);
    }

    public iz k() {
        return new iz(this.c, this.a, this.d);
    }

    public static boolean a(iz iz2, iz iz3) {
        if (iz2 == null && iz3 == null) {
            return true;
        }
        if (iz2 == null || iz3 == null) {
            return false;
        }
        return iz2.d(iz3);
    }

    private boolean d(iz iz2) {
        if (this.a != iz2.a) {
            return false;
        }
        if (this.c != iz2.c) {
            return false;
        }
        return this.d == iz2.d;
    }

    public boolean a(iz iz2) {
        return this.c == iz2.c && this.d == iz2.d;
    }

    public String l() {
        return gm.c[this.c].a(this);
    }

    public static iz b(iz iz2) {
        return iz2 == null ? null : iz2.k();
    }

    public String toString() {
        return this.a + "x" + gm.c[this.c].a() + "@" + this.d;
    }

    public void a(fd fd2, sn sn2, int n2, boolean bl2) {
        if (this.b > 0) {
            --this.b;
        }
        gm.c[this.c].a(this, fd2, sn2, n2, bl2);
    }

    public void b(fd fd2, gs gs2) {
        gs2.a(jl.D[this.c], this.a);
        gm.c[this.c].b(this, fd2, gs2);
    }

    public boolean c(iz iz2) {
        return this.c == iz2.c && this.a == iz2.a && this.d == iz2.d;
    }
}

