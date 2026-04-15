/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class gi
extends bg {
    private boolean a = false;
    private float b;
    private float c;
    private boolean f;
    private boolean g;
    private float h;
    private float i;

    public gi(fd fd2) {
        super(fd2);
        this.O = "/mob/wolf.png";
        this.b(0.8f, 0.8f);
        this.aB = 1.1f;
        this.Y = 8;
    }

    protected void b() {
        super.b();
        this.bD.a(16, (byte)0);
        this.bD.a(17, "");
        this.bD.a(18, new Integer(this.Y));
    }

    protected boolean n() {
        return false;
    }

    public String q_() {
        if (this.D()) {
            return "/mob/wolf_tame.png";
        }
        if (this.C()) {
            return "/mob/wolf_angry.png";
        }
        return super.q_();
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Angry", this.C());
        nu2.a("Sitting", this.B());
        if (this.A() == null) {
            nu2.a("Owner", "");
        } else {
            nu2.a("Owner", this.A());
        }
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.c(nu2.m("Angry"));
        this.b(nu2.m("Sitting"));
        String string = nu2.i("Owner");
        if (string.length() > 0) {
            this.a(string);
            this.d(true);
        }
    }

    protected boolean u() {
        return !this.D();
    }

    protected String g() {
        if (this.C()) {
            return "mob.wolf.growl";
        }
        if (this.bs.nextInt(3) == 0) {
            if (this.D() && this.bD.b(18) < 10) {
                return "mob.wolf.whine";
            }
            return "mob.wolf.panting";
        }
        return "mob.wolf.bark";
    }

    protected String j_() {
        return "mob.wolf.hurt";
    }

    protected String i() {
        return "mob.wolf.death";
    }

    protected float k() {
        return 0.4f;
    }

    protected int j() {
        return -1;
    }

    protected void f_() {
        List list;
        super.f_();
        if (!this.e && !this.F() && this.D() && this.aH == null) {
            gs gs2 = this.aI.a(this.A());
            if (gs2 != null) {
                float f2 = gs2.f(this);
                if (f2 > 5.0f) {
                    this.c((sn)gs2, f2);
                }
            } else if (!this.ag()) {
                this.b(true);
            }
        } else if (!(this.d != null || this.F() || this.D() || this.aI.r.nextInt(100) != 0 || (list = this.aI.a(dl.class, eq.b(this.aM, this.aN, this.aO, this.aM + 1.0, this.aN + 1.0, this.aO + 1.0).b(16.0, 4.0, 16.0))).isEmpty())) {
            this.c((sn)list.get(this.aI.r.nextInt(list.size())));
        }
        if (this.ag()) {
            this.b(false);
        }
        if (!this.aI.B) {
            this.bD.b(18, this.Y);
        }
    }

    public void o() {
        sn sn2;
        super.o();
        this.a = false;
        if (this.Y() && !this.F() && !this.C() && (sn2 = this.Z()) instanceof gs) {
            gs gs2 = (gs)sn2;
            iz iz2 = gs2.c.b();
            if (iz2 != null) {
                if (!this.D() && iz2.c == gm.aV.bf) {
                    this.a = true;
                } else if (this.D() && gm.c[iz2.c] instanceof yw) {
                    this.a = ((yw)gm.c[iz2.c]).m();
                }
            }
        }
        if (!this.V && this.f && !this.g && !this.F() && this.aX) {
            this.g = true;
            this.h = 0.0f;
            this.i = 0.0f;
            this.aI.a((sn)this, (byte)8);
        }
    }

    public void w_() {
        super.w_();
        this.c = this.b;
        this.b = this.a ? (this.b += (1.0f - this.b) * 0.4f) : (this.b += (0.0f - this.b) * 0.4f);
        if (this.a) {
            this.aC = 10;
        }
        if (this.af()) {
            this.f = true;
            this.g = false;
            this.h = 0.0f;
            this.i = 0.0f;
        } else if ((this.f || this.g) && this.g) {
            if (this.h == 0.0f) {
                this.aI.a(this, "mob.wolf.shake", this.k(), (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
            }
            this.i = this.h;
            this.h += 0.05f;
            if (this.i >= 2.0f) {
                this.f = false;
                this.g = false;
                this.i = 0.0f;
                this.h = 0.0f;
            }
            if (this.h > 0.4f) {
                float f2 = (float)this.aW.b;
                int n2 = (int)(in.a((this.h - 0.4f) * (float)Math.PI) * 7.0f);
                for (int i2 = 0; i2 < n2; ++i2) {
                    float f3 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg * 0.5f;
                    float f4 = (this.bs.nextFloat() * 2.0f - 1.0f) * this.bg * 0.5f;
                    this.aI.a("splash", this.aM + (double)f3, f2 + 0.8f, this.aO + (double)f4, this.aP, this.aQ, this.aR);
                }
            }
        }
    }

    public boolean v() {
        return this.f;
    }

    public float b_(float f2) {
        return 0.75f + (this.i + (this.h - this.i) * f2) / 2.0f * 0.25f;
    }

    public float a(float f2, float f3) {
        float f4 = (this.i + (this.h - this.i) * f2 + f3) / 1.8f;
        if (f4 < 0.0f) {
            f4 = 0.0f;
        } else if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        return in.a(f4 * (float)Math.PI) * in.a(f4 * (float)Math.PI * 11.0f) * 0.15f * (float)Math.PI;
    }

    public float c(float f2) {
        return (this.c + (this.b - this.c) * f2) * 0.15f * (float)Math.PI;
    }

    public float w() {
        return this.bh * 0.8f;
    }

    protected int x() {
        if (this.B()) {
            return 20;
        }
        return super.x();
    }

    private void c(sn sn2, float f2) {
        dh dh2 = this.aI.a(this, sn2, 16.0f);
        if (dh2 == null && f2 > 12.0f) {
            int n2 = in.b(sn2.aM) - 2;
            int n3 = in.b(sn2.aO) - 2;
            int n4 = in.b(sn2.aW.b);
            for (int i2 = 0; i2 <= 4; ++i2) {
                for (int i3 = 0; i3 <= 4; ++i3) {
                    if (i2 >= 1 && i3 >= 1 && i2 <= 3 && i3 <= 3 || !this.aI.h(n2 + i2, n4 - 1, n3 + i3) || this.aI.h(n2 + i2, n4, n3 + i3) || this.aI.h(n2 + i2, n4 + 1, n3 + i3)) continue;
                    this.c((float)(n2 + i2) + 0.5f, n4, (float)(n3 + i3) + 0.5f, this.aS, this.aT);
                    return;
                }
            }
        } else {
            this.a(dh2);
        }
    }

    protected boolean e_() {
        return this.B() || this.g;
    }

    public boolean a(sn sn2, int n2) {
        this.b(false);
        if (sn2 != null && !(sn2 instanceof gs) && !(sn2 instanceof sl)) {
            n2 = (n2 + 1) / 2;
        }
        if (super.a(sn2, n2)) {
            if (!this.D() && !this.C()) {
                if (sn2 instanceof gs) {
                    this.c(true);
                    this.d = sn2;
                }
                if (sn2 instanceof sl && ((sl)sn2).c != null) {
                    sn2 = ((sl)sn2).c;
                }
                if (sn2 instanceof ls) {
                    List list = this.aI.a(gi.class, eq.b(this.aM, this.aN, this.aO, this.aM + 1.0, this.aN + 1.0, this.aO + 1.0).b(16.0, 4.0, 16.0));
                    for (sn sn3 : list) {
                        gi gi2 = (gi)sn3;
                        if (gi2.D() || gi2.d != null) continue;
                        gi2.d = sn2;
                        if (!(sn2 instanceof gs)) continue;
                        gi2.c(true);
                    }
                }
            } else if (sn2 != this && sn2 != null) {
                if (this.D() && sn2 instanceof gs && ((gs)sn2).l.equalsIgnoreCase(this.A())) {
                    return true;
                }
                this.d = sn2;
            }
            return true;
        }
        return false;
    }

    protected sn g_() {
        if (this.C()) {
            return this.aI.a((sn)this, 16.0);
        }
        return null;
    }

    protected void a(sn sn2, float f2) {
        if (f2 > 2.0f && f2 < 6.0f && this.bs.nextInt(10) == 0) {
            if (this.aX) {
                double d2 = sn2.aM - this.aM;
                double d3 = sn2.aO - this.aO;
                float f3 = in.a(d2 * d2 + d3 * d3);
                this.aP = d2 / (double)f3 * 0.5 * (double)0.8f + this.aP * (double)0.2f;
                this.aR = d3 / (double)f3 * 0.5 * (double)0.8f + this.aR * (double)0.2f;
                this.aQ = 0.4f;
            }
        } else if ((double)f2 < 1.5 && sn2.aW.e > this.aW.b && sn2.aW.b < this.aW.e) {
            this.ae = 20;
            int n2 = 2;
            if (this.D()) {
                n2 = 4;
            }
            sn2.a(this, n2);
        }
    }

    public boolean a(gs gs2) {
        iz iz2 = gs2.c.b();
        if (!this.D()) {
            if (iz2 != null && iz2.c == gm.aV.bf && !this.C()) {
                --iz2.a;
                if (iz2.a <= 0) {
                    gs2.c.a(gs2.c.c, null);
                }
                if (!this.aI.B) {
                    if (this.bs.nextInt(3) == 0) {
                        this.d(true);
                        this.a((dh)null);
                        this.b(true);
                        this.Y = 20;
                        this.a(gs2.l);
                        this.a(true);
                        this.aI.a((sn)this, (byte)7);
                    } else {
                        this.a(false);
                        this.aI.a((sn)this, (byte)6);
                    }
                }
                return true;
            }
        } else {
            yw yw2;
            if (iz2 != null && gm.c[iz2.c] instanceof yw && (yw2 = (yw)gm.c[iz2.c]).m() && this.bD.b(18) < 20) {
                --iz2.a;
                if (iz2.a <= 0) {
                    gs2.c.a(gs2.c.c, null);
                }
                this.c(((yw)gm.ao).l());
                return true;
            }
            if (gs2.l.equalsIgnoreCase(this.A())) {
                if (!this.aI.B) {
                    this.b(!this.B());
                    this.az = false;
                    this.a((dh)null);
                }
                return true;
            }
        }
        return false;
    }

    void a(boolean bl2) {
        String string = "heart";
        if (!bl2) {
            string = "smoke";
        }
        for (int i2 = 0; i2 < 7; ++i2) {
            double d2 = this.bs.nextGaussian() * 0.02;
            double d3 = this.bs.nextGaussian() * 0.02;
            double d4 = this.bs.nextGaussian() * 0.02;
            this.aI.a(string, this.aM + (double)(this.bs.nextFloat() * this.bg * 2.0f) - (double)this.bg, this.aN + 0.5 + (double)(this.bs.nextFloat() * this.bh), this.aO + (double)(this.bs.nextFloat() * this.bg * 2.0f) - (double)this.bg, d2, d3, d4);
        }
    }

    public void a(byte by2) {
        if (by2 == 7) {
            this.a(true);
        } else if (by2 == 6) {
            this.a(false);
        } else if (by2 == 8) {
            this.g = true;
            this.h = 0.0f;
            this.i = 0.0f;
        } else {
            super.a(by2);
        }
    }

    public float z() {
        if (this.C()) {
            return 1.5393804f;
        }
        if (this.D()) {
            return (0.55f - (float)(20 - this.bD.b(18)) * 0.02f) * (float)Math.PI;
        }
        return 0.62831855f;
    }

    public int l() {
        return 8;
    }

    public String A() {
        return this.bD.c(17);
    }

    public void a(String string) {
        this.bD.b(17, string);
    }

    public boolean B() {
        return (this.bD.a(16) & 1) != 0;
    }

    public void b(boolean bl2) {
        byte by2 = this.bD.a(16);
        if (bl2) {
            this.bD.b(16, (byte)(by2 | 1));
        } else {
            this.bD.b(16, (byte)(by2 & 0xFFFFFFFE));
        }
    }

    public boolean C() {
        return (this.bD.a(16) & 2) != 0;
    }

    public void c(boolean bl2) {
        byte by2 = this.bD.a(16);
        if (bl2) {
            this.bD.b(16, (byte)(by2 | 2));
        } else {
            this.bD.b(16, (byte)(by2 & 0xFFFFFFFD));
        }
    }

    public boolean D() {
        return (this.bD.a(16) & 4) != 0;
    }

    public void d(boolean bl2) {
        byte by2 = this.bD.a(16);
        if (bl2) {
            this.bD.b(16, (byte)(by2 | 4));
        } else {
            this.bD.b(16, (byte)(by2 & 0xFFFFFFFB));
        }
    }
}

