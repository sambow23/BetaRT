/*
 * Decompiled with CFR 0.152.
 */
public class sk
extends ow
implements lw {
    private iz[] i = new iz[3];
    public int a = 0;
    public int b = 0;
    public int c = 0;

    public int a() {
        return this.i.length;
    }

    public iz f_(int n2) {
        return this.i[n2];
    }

    public iz a(int n2, int n3) {
        if (this.i[n2] != null) {
            if (this.i[n2].a <= n3) {
                iz iz2 = this.i[n2];
                this.i[n2] = null;
                return iz2;
            }
            iz iz3 = this.i[n2].a(n3);
            if (this.i[n2].a == 0) {
                this.i[n2] = null;
            }
            return iz3;
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        this.i[n2] = iz2;
        if (iz2 != null && iz2.a > this.d()) {
            iz2.a = this.d();
        }
    }

    public String c() {
        return "Furnace";
    }

    public void a(nu nu2) {
        super.a(nu2);
        sp sp2 = nu2.l("Items");
        this.i = new iz[this.a()];
        for (int i2 = 0; i2 < sp2.c(); ++i2) {
            nu nu3 = (nu)sp2.a(i2);
            byte by2 = nu3.c("Slot");
            if (by2 < 0 || by2 >= this.i.length) continue;
            this.i[by2] = new iz(nu3);
        }
        this.a = nu2.d("BurnTime");
        this.c = nu2.d("CookTime");
        this.b = this.a(this.i[1]);
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("BurnTime", (short)this.a);
        nu2.a("CookTime", (short)this.c);
        sp sp2 = new sp();
        for (int i2 = 0; i2 < this.i.length; ++i2) {
            if (this.i[i2] == null) continue;
            nu nu3 = new nu();
            nu3.a("Slot", (byte)i2);
            this.i[i2].a(nu3);
            sp2.a(nu3);
        }
        nu2.a("Items", sp2);
    }

    public int d() {
        return 64;
    }

    public int b(int n2) {
        return this.c * n2 / 200;
    }

    public int c(int n2) {
        if (this.b == 0) {
            this.b = 200;
        }
        return this.a * n2 / this.b;
    }

    public boolean b() {
        return this.a > 0;
    }

    public void n_() {
        boolean bl2 = this.a > 0;
        boolean bl3 = false;
        if (this.a > 0) {
            --this.a;
        }
        if (!this.d.B) {
            if (this.a == 0 && this.l()) {
                this.b = this.a = this.a(this.i[1]);
                if (this.a > 0) {
                    bl3 = true;
                    if (this.i[1] != null) {
                        --this.i[1].a;
                        if (this.i[1].a == 0) {
                            this.i[1] = null;
                        }
                    }
                }
            }
            if (this.b() && this.l()) {
                ++this.c;
                if (this.c == 200) {
                    this.c = 0;
                    this.k();
                    bl3 = true;
                }
            } else {
                this.c = 0;
            }
            if (bl2 != this.a > 0) {
                bl3 = true;
                tc.a(this.a > 0, this.d, this.e, this.f, this.g);
            }
        }
        if (bl3) {
            this.y_();
        }
    }

    private boolean l() {
        if (this.i[0] == null) {
            return false;
        }
        iz iz2 = ey.a().a(this.i[0].a().bf);
        if (iz2 == null) {
            return false;
        }
        if (this.i[2] == null) {
            return true;
        }
        if (!this.i[2].a(iz2)) {
            return false;
        }
        if (this.i[2].a < this.d() && this.i[2].a < this.i[2].c()) {
            return true;
        }
        return this.i[2].a < iz2.c();
    }

    public void k() {
        if (!this.l()) {
            return;
        }
        iz iz2 = ey.a().a(this.i[0].a().bf);
        if (this.i[2] == null) {
            this.i[2] = iz2.k();
        } else if (this.i[2].c == iz2.c) {
            ++this.i[2].a;
        }
        --this.i[0].a;
        if (this.i[0].a <= 0) {
            this.i[0] = null;
        }
    }

    private int a(iz iz2) {
        if (iz2 == null) {
            return 0;
        }
        int n2 = iz2.a().bf;
        if (n2 < 256 && uu.m[n2].bA == ln.d) {
            return 300;
        }
        if (n2 == gm.B.bf) {
            return 100;
        }
        if (n2 == gm.k.bf) {
            return 1600;
        }
        if (n2 == gm.aw.bf) {
            return 20000;
        }
        if (n2 == uu.z.bn) {
            return 100;
        }
        return 0;
    }

    public boolean a_(gs gs2) {
        if (this.d.b(this.e, this.f, this.g) != this) {
            return false;
        }
        return !(gs2.g((double)this.e + 0.5, (double)this.f + 0.5, (double)this.g + 0.5) > 64.0);
    }
}

