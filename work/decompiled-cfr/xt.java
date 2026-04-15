/*
 * Decompiled with CFR 0.152.
 */
public class xt
extends ar {
    public float a = 0.0f;
    public float b = 0.0f;
    public float c = 0.0f;
    public float f = 0.0f;
    public float g = 0.0f;
    public float h = 0.0f;
    public float i = 0.0f;
    public float j = 0.0f;
    private float k = 0.0f;
    private float l = 0.0f;
    private float m = 0.0f;
    private float n = 0.0f;
    private float o = 0.0f;
    private float p = 0.0f;

    public xt(fd fd2) {
        super(fd2);
        this.O = "/mob/squid.png";
        this.b(0.95f, 0.95f);
        this.l = 1.0f / (this.bs.nextFloat() + 1.0f) * 0.2f;
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    protected String g() {
        return null;
    }

    protected String j_() {
        return null;
    }

    protected String i() {
        return null;
    }

    protected float k() {
        return 0.4f;
    }

    protected int j() {
        return 0;
    }

    protected void q() {
        int n2 = this.bs.nextInt(3) + 1;
        for (int i2 = 0; i2 < n2; ++i2) {
            this.a(new iz(gm.aU, 1, 0), 0.0f);
        }
    }

    public boolean a(gs gs2) {
        return false;
    }

    public boolean ag() {
        return this.aI.a(this.aW.b(0.0, -0.6f, 0.0), ln.g, this);
    }

    public void o() {
        super.o();
        this.b = this.a;
        this.f = this.c;
        this.h = this.g;
        this.j = this.i;
        this.g += this.l;
        if (this.g > (float)Math.PI * 2) {
            this.g -= (float)Math.PI * 2;
            if (this.bs.nextInt(10) == 0) {
                this.l = 1.0f / (this.bs.nextFloat() + 1.0f) * 0.2f;
            }
        }
        if (this.ag()) {
            float f2;
            if (this.g < (float)Math.PI) {
                f2 = this.g / (float)Math.PI;
                this.i = in.a(f2 * f2 * (float)Math.PI) * (float)Math.PI * 0.25f;
                if ((double)f2 > 0.75) {
                    this.k = 1.0f;
                    this.m = 1.0f;
                } else {
                    this.m *= 0.8f;
                }
            } else {
                this.i = 0.0f;
                this.k *= 0.9f;
                this.m *= 0.99f;
            }
            if (!this.V) {
                this.aP = this.n * this.k;
                this.aQ = this.o * this.k;
                this.aR = this.p * this.k;
            }
            f2 = in.a(this.aP * this.aP + this.aR * this.aR);
            this.H += (-((float)Math.atan2(this.aP, this.aR)) * 180.0f / (float)Math.PI - this.H) * 0.1f;
            this.aS = this.H;
            this.c += (float)Math.PI * this.m * 1.5f;
            this.a += (-((float)Math.atan2(f2, this.aQ)) * 180.0f / (float)Math.PI - this.a) * 0.1f;
        } else {
            this.i = in.e(in.a(this.g)) * (float)Math.PI * 0.25f;
            if (!this.V) {
                this.aP = 0.0;
                this.aQ -= 0.08;
                this.aQ *= (double)0.98f;
                this.aR = 0.0;
            }
            this.a = (float)((double)this.a + (double)(-90.0f - this.a) * 0.02);
        }
    }

    public void a_(float f2, float f3) {
        this.b(this.aP, this.aQ, this.aR);
    }

    protected void f_() {
        if (this.bs.nextInt(50) == 0 || !this.bx || this.n == 0.0f && this.o == 0.0f && this.p == 0.0f) {
            float f2 = this.bs.nextFloat() * (float)Math.PI * 2.0f;
            this.n = in.b(f2) * 0.2f;
            this.o = -0.1f + this.bs.nextFloat() * 0.2f;
            this.p = in.a(f2) * 0.2f;
        }
        this.X();
    }
}

