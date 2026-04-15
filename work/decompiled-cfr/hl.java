/*
 * Decompiled with CFR 0.152.
 */
public class hl
extends sn {
    public iz a;
    private int e;
    public int b = 0;
    public int c;
    private int f = 5;
    public float d = (float)(Math.random() * Math.PI * 2.0);

    public hl(fd fd2, double d2, double d3, double d4, iz iz2) {
        super(fd2);
        this.b(0.25f, 0.25f);
        this.bf = this.bh / 2.0f;
        this.e(d2, d3, d4);
        this.a = iz2;
        this.aS = (float)(Math.random() * 360.0);
        this.aP = (float)(Math.random() * (double)0.2f - (double)0.1f);
        this.aQ = 0.2f;
        this.aR = (float)(Math.random() * (double)0.2f - (double)0.1f);
    }

    protected boolean n() {
        return false;
    }

    public hl(fd fd2) {
        super(fd2);
        this.b(0.25f, 0.25f);
        this.bf = this.bh / 2.0f;
    }

    protected void b() {
    }

    public void w_() {
        super.w_();
        if (this.c > 0) {
            --this.c;
        }
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        this.aQ -= (double)0.04f;
        if (this.aI.f(in.b(this.aM), in.b(this.aN), in.b(this.aO)) == ln.h) {
            this.aQ = 0.2f;
            this.aP = (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f;
            this.aR = (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f;
            this.aI.a(this, "random.fizz", 0.4f, 2.0f + this.bs.nextFloat() * 0.4f);
        }
        this.c(this.aM, (this.aW.b + this.aW.e) / 2.0, this.aO);
        this.b(this.aP, this.aQ, this.aR);
        float f2 = 0.98f;
        if (this.aX) {
            f2 = 0.58800006f;
            int n2 = this.aI.a(in.b(this.aM), in.b(this.aW.b) - 1, in.b(this.aO));
            if (n2 > 0) {
                f2 = uu.m[n2].bB * 0.98f;
            }
        }
        this.aP *= (double)f2;
        this.aQ *= (double)0.98f;
        this.aR *= (double)f2;
        if (this.aX) {
            this.aQ *= -0.5;
        }
        ++this.e;
        ++this.b;
        if (this.b >= 6000) {
            this.K();
        }
    }

    public boolean k_() {
        return this.aI.a(this.aW, ln.g, this);
    }

    protected void a(int n2) {
        this.a((sn)null, n2);
    }

    public boolean a(sn sn2, int n2) {
        this.ai();
        this.f -= n2;
        if (this.f <= 0) {
            this.K();
        }
        return false;
    }

    public void b(nu nu2) {
        nu2.a("Health", (short)((byte)this.f));
        nu2.a("Age", (short)this.b);
        nu2.a("Item", this.a.a(new nu()));
    }

    public void a(nu nu2) {
        this.f = nu2.d("Health") & 0xFF;
        this.b = nu2.d("Age");
        nu nu3 = nu2.k("Item");
        this.a = new iz(nu3);
    }

    public void b(gs gs2) {
        if (this.aI.B) {
            return;
        }
        int n2 = this.a.a;
        if (this.c == 0 && gs2.c.a(this.a)) {
            if (this.a.c == uu.K.bn) {
                gs2.a(ep.g);
            }
            if (this.a.c == gm.aD.bf) {
                gs2.a(ep.t);
            }
            this.aI.a(this, "random.pop", 0.2f, ((this.bs.nextFloat() - this.bs.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            gs2.b(this, n2);
            if (this.a.a <= 0) {
                this.K();
            }
        }
    }
}

