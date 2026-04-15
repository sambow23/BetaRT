/*
 * Decompiled with CFR 0.152.
 */
import java.util.Iterator;
import java.util.List;

public abstract class gs
extends ls {
    public ix c = new ix(this);
    public dw d;
    public dw e;
    public byte f = 0;
    public int g = 0;
    public float h;
    public float i;
    public boolean j = false;
    public int k = 0;
    public String l;
    public int m;
    public String n;
    public double o;
    public double p;
    public double q;
    public double r;
    public double s;
    public double t;
    protected boolean u;
    public br v;
    private int a;
    public float w;
    public float x;
    public float y;
    private br b;
    private br bN;
    public int z = 20;
    protected boolean A = false;
    public float B;
    public float C;
    private int bO = 0;
    public lx D = null;

    public gs(fd fd2) {
        super(fd2);
        this.e = this.d = new aa(this.c, !fd2.B);
        this.bf = 1.62f;
        br br2 = fd2.u();
        this.c((double)br2.a + 0.5, br2.b + 1, (double)br2.c + 0.5, 0.0f, 0.0f);
        this.Y = 20;
        this.R = "humanoid";
        this.Q = 180.0f;
        this.bu = 20;
        this.O = "/mob/char.png";
    }

    protected void b() {
        super.b();
        this.bD.a(16, (byte)0);
    }

    public void w_() {
        if (this.N()) {
            ++this.a;
            if (this.a > 100) {
                this.a = 100;
            }
            if (!this.aI.B) {
                if (!this.am()) {
                    this.a(true, true, false);
                } else if (this.aI.f()) {
                    this.a(false, true, true);
                }
            }
        } else if (this.a > 0) {
            ++this.a;
            if (this.a >= 110) {
                this.a = 0;
            }
        }
        super.w_();
        if (!this.aI.B && this.e != null && !this.e.b(this)) {
            this.r();
            this.e = this.d;
        }
        this.o = this.r;
        this.p = this.s;
        this.q = this.t;
        double d2 = this.aM - this.r;
        double d3 = this.aN - this.s;
        double d4 = this.aO - this.t;
        double d5 = 10.0;
        if (d2 > d5) {
            this.o = this.r = this.aM;
        }
        if (d4 > d5) {
            this.q = this.t = this.aO;
        }
        if (d3 > d5) {
            this.p = this.s = this.aN;
        }
        if (d2 < -d5) {
            this.o = this.r = this.aM;
        }
        if (d4 < -d5) {
            this.q = this.t = this.aO;
        }
        if (d3 < -d5) {
            this.p = this.s = this.aN;
        }
        this.r += d2 * 0.25;
        this.t += d4 * 0.25;
        this.s += d3 * 0.25;
        this.a(jl.k, 1);
        if (this.aH == null) {
            this.bN = null;
        }
    }

    protected boolean y() {
        return this.Y <= 0 || this.N();
    }

    protected void r() {
        this.e = this.d;
    }

    public void u_() {
        this.bB = this.n = "http://s3.amazonaws.com/MinecraftCloaks/" + this.l + ".png";
    }

    public void s_() {
        double d2 = this.aM;
        double d3 = this.aN;
        double d4 = this.aO;
        super.s_();
        this.h = this.i;
        this.i = 0.0f;
        this.j(this.aM - d2, this.aN - d3, this.aO - d4);
    }

    public void t_() {
        this.bf = 1.62f;
        this.b(0.6f, 1.8f);
        super.t_();
        this.Y = 20;
        this.ad = 0;
    }

    protected void f_() {
        if (this.j) {
            ++this.k;
            if (this.k >= 8) {
                this.k = 0;
                this.j = false;
            }
        } else {
            this.k = 0;
        }
        this.X = (float)this.k / 8.0f;
    }

    public void o() {
        List list;
        if (this.aI.q == 0 && this.Y < 20 && this.bt % 20 * 12 == 0) {
            this.c(1);
        }
        this.c.e();
        this.h = this.i;
        super.o();
        float f2 = in.a(this.aP * this.aP + this.aR * this.aR);
        float f3 = (float)Math.atan(-this.aQ * (double)0.2f) * 15.0f;
        if (f2 > 0.1f) {
            f2 = 0.1f;
        }
        if (!this.aX || this.Y <= 0) {
            f2 = 0.0f;
        }
        if (this.aX || this.Y <= 0) {
            f3 = 0.0f;
        }
        this.i += (f2 - this.i) * 0.4f;
        this.ag += (f3 - this.ag) * 0.8f;
        if (this.Y > 0 && (list = this.aI.b(this, this.aW.b(1.0, 0.0, 1.0))) != null) {
            for (int i2 = 0; i2 < list.size(); ++i2) {
                sn sn2 = (sn)list.get(i2);
                if (sn2.be) continue;
                this.j(sn2);
            }
        }
    }

    private void j(sn sn2) {
        sn2.b(this);
    }

    public int C() {
        return this.g;
    }

    public void b(sn sn2) {
        super.b(sn2);
        this.b(0.2f, 0.2f);
        this.e(this.aM, this.aN, this.aO);
        this.aQ = 0.1f;
        if (this.l.equals("Notch")) {
            this.a(new iz(gm.h, 1), true);
        }
        this.c.g();
        if (sn2 != null) {
            this.aP = -in.b((this.ac + this.aS) * (float)Math.PI / 180.0f) * 0.1f;
            this.aR = -in.a((this.ac + this.aS) * (float)Math.PI / 180.0f) * 0.1f;
        } else {
            this.aR = 0.0;
            this.aP = 0.0;
        }
        this.bf = 0.1f;
        this.a(jl.y, 1);
    }

    public void c(sn sn2, int n2) {
        this.g += n2;
        if (sn2 instanceof gs) {
            this.a(jl.A, 1);
        } else {
            this.a(jl.z, 1);
        }
    }

    public void D() {
        this.a(this.c.a(this.c.c, 1), false);
    }

    public void a(iz iz2) {
        this.a(iz2, false);
    }

    public void a(iz iz2, boolean bl2) {
        if (iz2 == null) {
            return;
        }
        hl hl2 = new hl(this.aI, this.aM, this.aN - (double)0.3f + (double)this.w(), this.aO, iz2);
        hl2.c = 40;
        float f2 = 0.1f;
        if (bl2) {
            float f3 = this.bs.nextFloat() * 0.5f;
            float f4 = this.bs.nextFloat() * (float)Math.PI * 2.0f;
            hl2.aP = -in.a(f4) * f3;
            hl2.aR = in.b(f4) * f3;
            hl2.aQ = 0.2f;
        } else {
            f2 = 0.3f;
            hl2.aP = -in.a(this.aS / 180.0f * (float)Math.PI) * in.b(this.aT / 180.0f * (float)Math.PI) * f2;
            hl2.aR = in.b(this.aS / 180.0f * (float)Math.PI) * in.b(this.aT / 180.0f * (float)Math.PI) * f2;
            hl2.aQ = -in.a(this.aT / 180.0f * (float)Math.PI) * f2 + 0.1f;
            f2 = 0.02f;
            float f5 = this.bs.nextFloat() * (float)Math.PI * 2.0f;
            hl2.aP += Math.cos(f5) * (double)(f2 *= this.bs.nextFloat());
            hl2.aQ += (double)((this.bs.nextFloat() - this.bs.nextFloat()) * 0.1f);
            hl2.aR += Math.sin(f5) * (double)f2;
        }
        this.a(hl2);
        this.a(jl.v, 1);
    }

    protected void a(hl hl2) {
        this.aI.b(hl2);
    }

    public float a(uu uu2) {
        float f2 = this.c.a(uu2);
        if (this.a(ln.g)) {
            f2 /= 5.0f;
        }
        if (!this.aX) {
            f2 /= 5.0f;
        }
        return f2;
    }

    public boolean b(uu uu2) {
        return this.c.b(uu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
        sp sp2 = nu2.l("Inventory");
        this.c.b(sp2);
        this.m = nu2.e("Dimension");
        this.u = nu2.m("Sleeping");
        this.a = nu2.d("SleepTimer");
        if (this.u) {
            this.v = new br(in.b(this.aM), in.b(this.aN), in.b(this.aO));
            this.a(true, true, false);
        }
        if (nu2.b("SpawnX") && nu2.b("SpawnY") && nu2.b("SpawnZ")) {
            this.b = new br(nu2.e("SpawnX"), nu2.e("SpawnY"), nu2.e("SpawnZ"));
        }
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Inventory", this.c.a(new sp()));
        nu2.a("Dimension", this.m);
        nu2.a("Sleeping", this.u);
        nu2.a("SleepTimer", (short)this.a);
        if (this.b != null) {
            nu2.a("SpawnX", this.b.a);
            nu2.a("SpawnY", this.b.b);
            nu2.a("SpawnZ", this.b.c);
        }
    }

    public void a(lw lw2) {
    }

    public void a(int n2, int n3, int n4) {
    }

    public void b(sn sn2, int n2) {
    }

    public float w() {
        return 0.12f;
    }

    protected void E() {
        this.bf = 1.62f;
    }

    public boolean a(sn sn2, int n2) {
        this.av = 0;
        if (this.Y <= 0) {
            return false;
        }
        if (this.N() && !this.aI.B) {
            this.a(true, true, false);
        }
        if (sn2 instanceof gz || sn2 instanceof sl) {
            if (this.aI.q == 0) {
                n2 = 0;
            }
            if (this.aI.q == 1) {
                n2 = n2 / 3 + 1;
            }
            if (this.aI.q == 3) {
                n2 = n2 * 3 / 2;
            }
        }
        if (n2 == 0) {
            return false;
        }
        sn sn3 = sn2;
        if (sn3 instanceof sl && ((sl)sn3).c != null) {
            sn3 = ((sl)sn3).c;
        }
        if (sn3 instanceof ls) {
            this.a((ls)sn3, false);
        }
        this.a(jl.x, n2);
        return super.a(sn2, n2);
    }

    protected boolean F() {
        return false;
    }

    protected void a(ls ls2, boolean bl2) {
        Object object;
        if (ls2 instanceof gb || ls2 instanceof bp) {
            return;
        }
        if (ls2 instanceof gi && ((gi)(object = (gi)ls2)).D() && this.l.equals(((gi)object).A())) {
            return;
        }
        if (ls2 instanceof gs && !this.F()) {
            return;
        }
        object = this.aI.a(gi.class, eq.b(this.aM, this.aN, this.aO, this.aM + 1.0, this.aN + 1.0, this.aO + 1.0).b(16.0, 4.0, 16.0));
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            sn sn2 = (sn)iterator.next();
            gi gi2 = (gi)sn2;
            if (!gi2.D() || gi2.G() != null || !this.l.equals(gi2.A()) || bl2 && gi2.B()) continue;
            gi2.b(false);
            gi2.c(ls2);
        }
    }

    protected void b(int n2) {
        int n3 = 25 - this.c.f();
        int n4 = n2 * n3 + this.bO;
        this.c.e(n2);
        n2 = n4 / 25;
        this.bO = n4 % 25;
        super.b(n2);
    }

    public void a(sk sk2) {
    }

    public void a(az az2) {
    }

    public void a(yk yk2) {
    }

    public void c(sn sn2) {
        if (sn2.a(this)) {
            return;
        }
        iz iz2 = this.G();
        if (iz2 != null && sn2 instanceof ls) {
            iz2.a((ls)sn2);
            if (iz2.a <= 0) {
                iz2.a(this);
                this.H();
            }
        }
    }

    public iz G() {
        return this.c.b();
    }

    public void H() {
        this.c.a(this.c.c, null);
    }

    public double I() {
        return this.bf - 0.5f;
    }

    public void J() {
        this.k = -1;
        this.j = true;
    }

    public void d(sn sn2) {
        int n2 = this.c.a(sn2);
        if (n2 > 0) {
            if (this.aQ < 0.0) {
                ++n2;
            }
            sn2.a(this, n2);
            iz iz2 = this.G();
            if (iz2 != null && sn2 instanceof ls) {
                iz2.a((ls)sn2, this);
                if (iz2.a <= 0) {
                    iz2.a(this);
                    this.H();
                }
            }
            if (sn2 instanceof ls) {
                if (sn2.W()) {
                    this.a((ls)sn2, true);
                }
                this.a(jl.w, n2);
            }
        }
    }

    public void p_() {
    }

    public abstract void v();

    public void b(iz iz2) {
    }

    public void K() {
        super.K();
        this.d.a(this);
        if (this.e != null) {
            this.e.a(this);
        }
    }

    public boolean L() {
        return !this.u && super.L();
    }

    public cw b(int n2, int n3, int n4) {
        if (!this.aI.B) {
            if (this.N() || !this.W()) {
                return cw.e;
            }
            if (this.aI.t.c) {
                return cw.b;
            }
            if (this.aI.f()) {
                return cw.c;
            }
            if (Math.abs(this.aM - (double)n2) > 3.0 || Math.abs(this.aN - (double)n3) > 2.0 || Math.abs(this.aO - (double)n4) > 3.0) {
                return cw.d;
            }
        }
        this.b(0.2f, 0.2f);
        this.bf = 0.2f;
        if (this.aI.i(n2, n3, n4)) {
            int n5 = this.aI.e(n2, n3, n4);
            int n6 = ve.d(n5);
            float f2 = 0.5f;
            float f3 = 0.5f;
            switch (n6) {
                case 0: {
                    f3 = 0.9f;
                    break;
                }
                case 2: {
                    f3 = 0.1f;
                    break;
                }
                case 1: {
                    f2 = 0.1f;
                    break;
                }
                case 3: {
                    f2 = 0.9f;
                }
            }
            this.e(n6);
            this.e((float)n2 + f2, (float)n3 + 0.9375f, (float)n4 + f3);
        } else {
            this.e((float)n2 + 0.5f, (float)n3 + 0.9375f, (float)n4 + 0.5f);
        }
        this.u = true;
        this.a = 0;
        this.v = new br(n2, n3, n4);
        this.aQ = 0.0;
        this.aR = 0.0;
        this.aP = 0.0;
        if (!this.aI.B) {
            this.aI.y();
        }
        return cw.a;
    }

    private void e(int n2) {
        this.w = 0.0f;
        this.y = 0.0f;
        switch (n2) {
            case 0: {
                this.y = -1.8f;
                break;
            }
            case 2: {
                this.y = 1.8f;
                break;
            }
            case 1: {
                this.w = 1.8f;
                break;
            }
            case 3: {
                this.w = -1.8f;
            }
        }
    }

    public void a(boolean bl2, boolean bl3, boolean bl4) {
        this.b(0.6f, 1.8f);
        this.E();
        br br2 = this.v;
        br br3 = this.v;
        if (br2 != null && this.aI.a(br2.a, br2.b, br2.c) == uu.T.bn) {
            ve.a(this.aI, br2.a, br2.b, br2.c, false);
            br3 = ve.f(this.aI, br2.a, br2.b, br2.c, 0);
            if (br3 == null) {
                br3 = new br(br2.a, br2.b + 1, br2.c);
            }
            this.e((float)br3.a + 0.5f, (float)br3.b + this.bf + 0.1f, (float)br3.c + 0.5f);
        }
        this.u = false;
        if (!this.aI.B && bl3) {
            this.aI.y();
        }
        this.a = bl2 ? 0 : 100;
        if (bl4) {
            this.a(this.v);
        }
    }

    private boolean am() {
        return this.aI.a(this.v.a, this.v.b, this.v.c) == uu.T.bn;
    }

    public static br a(fd fd2, br br2) {
        cl cl2 = fd2.w();
        cl2.c(br2.a - 3 >> 4, br2.c - 3 >> 4);
        cl2.c(br2.a + 3 >> 4, br2.c - 3 >> 4);
        cl2.c(br2.a - 3 >> 4, br2.c + 3 >> 4);
        cl2.c(br2.a + 3 >> 4, br2.c + 3 >> 4);
        if (fd2.a(br2.a, br2.b, br2.c) != uu.T.bn) {
            return null;
        }
        br br3 = ve.f(fd2, br2.a, br2.b, br2.c, 0);
        return br3;
    }

    public float M() {
        if (this.v != null) {
            int n2 = this.aI.e(this.v.a, this.v.b, this.v.c);
            int n3 = ve.d(n2);
            switch (n3) {
                case 0: {
                    return 90.0f;
                }
                case 1: {
                    return 0.0f;
                }
                case 2: {
                    return 270.0f;
                }
                case 3: {
                    return 180.0f;
                }
            }
        }
        return 0.0f;
    }

    public boolean N() {
        return this.u;
    }

    public boolean O() {
        return this.u && this.a >= 100;
    }

    public int P() {
        return this.a;
    }

    public void b(String string) {
    }

    public br Q() {
        return this.b;
    }

    public void a(br br2) {
        this.b = br2 != null ? new br(br2) : null;
    }

    public void a(vr vr2) {
        this.a(vr2, 1);
    }

    public void a(vr vr2, int n2) {
    }

    protected void R() {
        super.R();
        this.a(jl.u, 1);
    }

    public void a_(float f2, float f3) {
        double d2 = this.aM;
        double d3 = this.aN;
        double d4 = this.aO;
        super.a_(f2, f3);
        this.i(this.aM - d2, this.aN - d3, this.aO - d4);
    }

    private void i(double d2, double d3, double d4) {
        if (this.aH != null) {
            return;
        }
        if (this.a(ln.g)) {
            int n2 = Math.round(in.a(d2 * d2 + d3 * d3 + d4 * d4) * 100.0f);
            if (n2 > 0) {
                this.a(jl.q, n2);
            }
        } else if (this.ag()) {
            int n3 = Math.round(in.a(d2 * d2 + d4 * d4) * 100.0f);
            if (n3 > 0) {
                this.a(jl.m, n3);
            }
        } else if (this.p()) {
            if (d3 > 0.0) {
                this.a(jl.o, (int)Math.round(d3 * 100.0));
            }
        } else if (this.aX) {
            int n4 = Math.round(in.a(d2 * d2 + d4 * d4) * 100.0f);
            if (n4 > 0) {
                this.a(jl.l, n4);
            }
        } else {
            int n5 = Math.round(in.a(d2 * d2 + d4 * d4) * 100.0f);
            if (n5 > 25) {
                this.a(jl.p, n5);
            }
        }
    }

    private void j(double d2, double d3, double d4) {
        int n2;
        if (this.aH != null && (n2 = Math.round(in.a(d2 * d2 + d3 * d3 + d4 * d4) * 100.0f)) > 0) {
            if (this.aH instanceof yl) {
                this.a(jl.r, n2);
                if (this.bN == null) {
                    this.bN = new br(in.b(this.aM), in.b(this.aN), in.b(this.aO));
                } else if (this.bN.a(in.b(this.aM), in.b(this.aN), in.b(this.aO)) >= 1000.0) {
                    this.a(ep.q, 1);
                }
            } else if (this.aH instanceof fz) {
                this.a(jl.s, n2);
            } else if (this.aH instanceof wh) {
                this.a(jl.t, n2);
            }
        }
    }

    protected void b(float f2) {
        if (f2 >= 2.0f) {
            this.a(jl.n, (int)Math.round((double)f2 * 100.0));
        }
        super.b(f2);
    }

    public void a(ls ls2) {
        if (ls2 instanceof gz) {
            this.a(ep.s);
        }
    }

    public int c(iz iz2) {
        int n2 = super.c(iz2);
        if (iz2.c == gm.aP.bf && this.D != null) {
            n2 = iz2.b() + 16;
        }
        return n2;
    }

    public void S() {
        if (this.z > 0) {
            this.z = 10;
            return;
        }
        this.A = true;
    }
}

