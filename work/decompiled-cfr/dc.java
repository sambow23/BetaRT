/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class dc
extends gs {
    public uo a;
    protected Minecraft b;
    private cu bN = new cu();
    private cu bO = new cu();
    private cu bP = new cu();

    public dc(Minecraft minecraft, fd fd2, gr gr2, int n2) {
        super(fd2);
        this.b = minecraft;
        this.m = n2;
        if (gr2 != null && gr2.b != null && gr2.b.length() > 0) {
            this.bA = "http://s3.amazonaws.com/MinecraftSkins/" + gr2.b + ".png";
        }
        this.l = gr2.b;
    }

    public void b(double d2, double d3, double d4) {
        super.b(d2, d3, d4);
    }

    public void f_() {
        super.f_();
        this.aw = this.a.a;
        this.ax = this.a.b;
        this.az = this.a.d;
    }

    public void o() {
        if (!this.b.I.a(ep.f)) {
            this.b.u.b(ep.f);
        }
        this.C = this.B;
        if (this.A) {
            if (!this.aI.B && this.aH != null) {
                this.i(null);
            }
            if (this.b.r != null) {
                this.b.a((da)null);
            }
            if (this.B == 0.0f) {
                this.b.B.a("portal.trigger", 1.0f, this.bs.nextFloat() * 0.4f + 0.8f);
            }
            this.B += 0.0125f;
            if (this.B >= 1.0f) {
                this.B = 1.0f;
                if (!this.aI.B) {
                    this.z = 10;
                    this.b.B.a("portal.travel", 1.0f, this.bs.nextFloat() * 0.4f + 0.8f);
                    this.b.m();
                }
            }
            this.A = false;
        } else {
            if (this.B > 0.0f) {
                this.B -= 0.05f;
            }
            if (this.B < 0.0f) {
                this.B = 0.0f;
            }
        }
        if (this.z > 0) {
            --this.z;
        }
        this.a.a(this);
        if (this.a.e && this.bo < 0.2f) {
            this.bo = 0.2f;
        }
        this.c(this.aM - (double)this.bg * 0.35, this.aW.b + 0.5, this.aO + (double)this.bg * 0.35);
        this.c(this.aM - (double)this.bg * 0.35, this.aW.b + 0.5, this.aO - (double)this.bg * 0.35);
        this.c(this.aM + (double)this.bg * 0.35, this.aW.b + 0.5, this.aO - (double)this.bg * 0.35);
        this.c(this.aM + (double)this.bg * 0.35, this.aW.b + 0.5, this.aO + (double)this.bg * 0.35);
        super.o();
    }

    public void o_() {
        this.a.a();
    }

    public void a(int n2, boolean bl2) {
        this.a.a(n2, bl2);
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Score", this.g);
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.g = nu2.e("Score");
    }

    public void r() {
        super.r();
        this.b.a((da)null);
    }

    public void a(yk yk2) {
        this.b.a(new yc(yk2));
    }

    public void a(lw lw2) {
        this.b.a(new hp(this.c, lw2));
    }

    public void a(int n2, int n3, int n4) {
        this.b.a(new oo(this.c, this.aI, n2, n3, n4));
    }

    public void a(sk sk2) {
        this.b.a(new ov(this.c, sk2));
    }

    public void a(az az2) {
        this.b.a(new gq(this.c, az2));
    }

    public void b(sn sn2, int n2) {
        this.b.j.a(new em(this.b.f, sn2, this, -0.5f));
    }

    public int s() {
        return this.c.f();
    }

    public void a(String string) {
    }

    public boolean t() {
        return this.a.e && !this.u;
    }

    public void d_(int n2) {
        int n3 = this.Y - n2;
        if (n3 <= 0) {
            this.Y = n2;
            if (n3 < 0) {
                this.by = this.E / 2;
            }
        } else {
            this.au = n3;
            this.Z = this.Y;
            this.by = this.E;
            this.b(n3);
            this.ab = 10;
            this.aa = 10;
        }
    }

    public void p_() {
        this.b.a(false, 0);
    }

    public void v() {
    }

    public void b(String string) {
        this.b.v.c(string);
    }

    public void a(vr vr2, int n2) {
        if (vr2 == null) {
            return;
        }
        if (vr2.d()) {
            ny ny2 = (ny)vr2;
            if (ny2.c == null || this.b.I.a(ny2.c)) {
                if (!this.b.I.a(ny2)) {
                    this.b.u.a(ny2);
                }
                this.b.I.a(vr2, n2);
            }
        } else {
            this.b.I.a(vr2, n2);
        }
    }

    private boolean d(int n2, int n3, int n4) {
        return this.aI.h(n2, n3, n4);
    }

    protected boolean c(double d2, double d3, double d4) {
        int n2 = in.b(d2);
        int n3 = in.b(d3);
        int n4 = in.b(d4);
        double d5 = d2 - (double)n2;
        double d6 = d4 - (double)n4;
        if (this.d(n2, n3, n4) || this.d(n2, n3 + 1, n4)) {
            boolean bl2 = !this.d(n2 - 1, n3, n4) && !this.d(n2 - 1, n3 + 1, n4);
            boolean bl3 = !this.d(n2 + 1, n3, n4) && !this.d(n2 + 1, n3 + 1, n4);
            boolean bl4 = !this.d(n2, n3, n4 - 1) && !this.d(n2, n3 + 1, n4 - 1);
            boolean bl5 = !this.d(n2, n3, n4 + 1) && !this.d(n2, n3 + 1, n4 + 1);
            int n5 = -1;
            double d7 = 9999.0;
            if (bl2 && d5 < d7) {
                d7 = d5;
                n5 = 0;
            }
            if (bl3 && 1.0 - d5 < d7) {
                d7 = 1.0 - d5;
                n5 = 1;
            }
            if (bl4 && d6 < d7) {
                d7 = d6;
                n5 = 4;
            }
            if (bl5 && 1.0 - d6 < d7) {
                d7 = 1.0 - d6;
                n5 = 5;
            }
            float f2 = 0.1f;
            if (n5 == 0) {
                this.aP = -f2;
            }
            if (n5 == 1) {
                this.aP = f2;
            }
            if (n5 == 4) {
                this.aR = -f2;
            }
            if (n5 == 5) {
                this.aR = f2;
            }
        }
        return false;
    }
}

