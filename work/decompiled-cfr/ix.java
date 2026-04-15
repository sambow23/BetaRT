/*
 * Decompiled with CFR 0.152.
 */
public class ix
implements lw {
    public iz[] a = new iz[36];
    public iz[] b = new iz[4];
    public int c = 0;
    public gs d;
    private iz f;
    public boolean e = false;

    public ix(gs gs2) {
        this.d = gs2;
    }

    public iz b() {
        if (this.c < 9 && this.c >= 0) {
            return this.a[this.c];
        }
        return null;
    }

    private int f(int n2) {
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            if (this.a[i2] == null || this.a[i2].c != n2) continue;
            return i2;
        }
        return -1;
    }

    private int d(iz iz2) {
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            if (this.a[i2] == null || this.a[i2].c != iz2.c || !this.a[i2].d() || this.a[i2].a >= this.a[i2].c() || this.a[i2].a >= this.d() || this.a[i2].f() && this.a[i2].i() != iz2.i()) continue;
            return i2;
        }
        return -1;
    }

    private int j() {
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            if (this.a[i2] != null) continue;
            return i2;
        }
        return -1;
    }

    public void a(int n2, boolean bl2) {
        int n3 = this.f(n2);
        if (n3 >= 0 && n3 < 9) {
            this.c = n3;
            return;
        }
    }

    public void b(int n2) {
        if (n2 > 0) {
            n2 = 1;
        }
        if (n2 < 0) {
            n2 = -1;
        }
        this.c -= n2;
        while (this.c < 0) {
            this.c += 9;
        }
        while (this.c >= 9) {
            this.c -= 9;
        }
    }

    private int e(iz iz2) {
        int n2;
        int n3 = iz2.c;
        int n4 = iz2.a;
        int n5 = this.d(iz2);
        if (n5 < 0) {
            n5 = this.j();
        }
        if (n5 < 0) {
            return n4;
        }
        if (this.a[n5] == null) {
            this.a[n5] = new iz(n3, 0, iz2.i());
        }
        if ((n2 = n4) > this.a[n5].c() - this.a[n5].a) {
            n2 = this.a[n5].c() - this.a[n5].a;
        }
        if (n2 > this.d() - this.a[n5].a) {
            n2 = this.d() - this.a[n5].a;
        }
        if (n2 == 0) {
            return n4;
        }
        this.a[n5].a += n2;
        this.a[n5].b = 5;
        return n4 -= n2;
    }

    public void e() {
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            if (this.a[i2] == null) continue;
            this.a[i2].a(this.d.aI, this.d, i2, this.c == i2);
        }
    }

    public boolean c(int n2) {
        int n3 = this.f(n2);
        if (n3 < 0) {
            return false;
        }
        if (--this.a[n3].a <= 0) {
            this.a[n3] = null;
        }
        return true;
    }

    public boolean a(iz iz2) {
        if (!iz2.g()) {
            int n2;
            do {
                n2 = iz2.a;
                iz2.a = this.e(iz2);
            } while (iz2.a > 0 && iz2.a < n2);
            return iz2.a < n2;
        }
        int n3 = this.j();
        if (n3 >= 0) {
            this.a[n3] = iz.b(iz2);
            this.a[n3].b = 5;
            iz2.a = 0;
            return true;
        }
        return false;
    }

    public iz a(int n2, int n3) {
        iz[] izArray = this.a;
        if (n2 >= this.a.length) {
            izArray = this.b;
            n2 -= this.a.length;
        }
        if (izArray[n2] != null) {
            if (izArray[n2].a <= n3) {
                iz iz2 = izArray[n2];
                izArray[n2] = null;
                return iz2;
            }
            iz iz3 = izArray[n2].a(n3);
            if (izArray[n2].a == 0) {
                izArray[n2] = null;
            }
            return iz3;
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        iz[] izArray = this.a;
        if (n2 >= izArray.length) {
            n2 -= izArray.length;
            izArray = this.b;
        }
        izArray[n2] = iz2;
    }

    public float a(uu uu2) {
        float f2 = 1.0f;
        if (this.a[this.c] != null) {
            f2 *= this.a[this.c].a(uu2);
        }
        return f2;
    }

    public sp a(sp sp2) {
        nu nu2;
        int n2;
        for (n2 = 0; n2 < this.a.length; ++n2) {
            if (this.a[n2] == null) continue;
            nu2 = new nu();
            nu2.a("Slot", (byte)n2);
            this.a[n2].a(nu2);
            sp2.a(nu2);
        }
        for (n2 = 0; n2 < this.b.length; ++n2) {
            if (this.b[n2] == null) continue;
            nu2 = new nu();
            nu2.a("Slot", (byte)(n2 + 100));
            this.b[n2].a(nu2);
            sp2.a(nu2);
        }
        return sp2;
    }

    public void b(sp sp2) {
        this.a = new iz[36];
        this.b = new iz[4];
        for (int i2 = 0; i2 < sp2.c(); ++i2) {
            nu nu2 = (nu)sp2.a(i2);
            int n2 = nu2.c("Slot") & 0xFF;
            iz iz2 = new iz(nu2);
            if (iz2.a() == null) continue;
            if (n2 >= 0 && n2 < this.a.length) {
                this.a[n2] = iz2;
            }
            if (n2 < 100 || n2 >= this.b.length + 100) continue;
            this.b[n2 - 100] = iz2;
        }
    }

    public int a() {
        return this.a.length + 4;
    }

    public iz f_(int n2) {
        iz[] izArray = this.a;
        if (n2 >= izArray.length) {
            n2 -= izArray.length;
            izArray = this.b;
        }
        return izArray[n2];
    }

    public String c() {
        return "Inventory";
    }

    public int d() {
        return 64;
    }

    public int a(sn sn2) {
        iz iz2 = this.f_(this.c);
        if (iz2 != null) {
            return iz2.a(sn2);
        }
        return 1;
    }

    public boolean b(uu uu2) {
        if (uu2.bA.i()) {
            return true;
        }
        iz iz2 = this.f_(this.c);
        if (iz2 != null) {
            return iz2.b(uu2);
        }
        return false;
    }

    public iz d(int n2) {
        return this.b[n2];
    }

    public int f() {
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (int i2 = 0; i2 < this.b.length; ++i2) {
            if (this.b[i2] == null || !(this.b[i2].a() instanceof wa)) continue;
            int n5 = this.b[i2].j();
            int n6 = this.b[i2].h();
            int n7 = n5 - n6;
            n3 += n7;
            n4 += n5;
            int n8 = ((wa)this.b[i2].a()).bl;
            n2 += n8;
        }
        if (n4 == 0) {
            return 0;
        }
        return (n2 - 1) * n3 / n4 + 1;
    }

    public void e(int n2) {
        for (int i2 = 0; i2 < this.b.length; ++i2) {
            if (this.b[i2] == null || !(this.b[i2].a() instanceof wa)) continue;
            this.b[i2].a(n2, (sn)this.d);
            if (this.b[i2].a != 0) continue;
            this.b[i2].a(this.d);
            this.b[i2] = null;
        }
    }

    public void g() {
        int n2;
        for (n2 = 0; n2 < this.a.length; ++n2) {
            if (this.a[n2] == null) continue;
            this.d.a(this.a[n2], true);
            this.a[n2] = null;
        }
        for (n2 = 0; n2 < this.b.length; ++n2) {
            if (this.b[n2] == null) continue;
            this.d.a(this.b[n2], true);
            this.b[n2] = null;
        }
    }

    public void y_() {
        this.e = true;
    }

    public void b(iz iz2) {
        this.f = iz2;
        this.d.b(iz2);
    }

    public iz i() {
        return this.f;
    }

    public boolean a_(gs gs2) {
        if (this.d.be) {
            return false;
        }
        return !(gs2.g(this.d) > 64.0);
    }

    public boolean c(iz iz2) {
        int n2;
        for (n2 = 0; n2 < this.b.length; ++n2) {
            if (this.b[n2] == null || !this.b[n2].c(iz2)) continue;
            return true;
        }
        for (n2 = 0; n2 < this.a.length; ++n2) {
            if (this.a[n2] == null || !this.a[n2].c(iz2)) continue;
            return true;
        }
        return false;
    }
}

