/*
 * Decompiled with CFR 0.152.
 */
public class uw
extends ls
implements ff {
    public float a;
    public float b;
    private int c = 0;

    public uw(fd fd2) {
        super(fd2);
        this.O = "/mob/slime.png";
        int n2 = 1 << this.bs.nextInt(3);
        this.bf = 0.0f;
        this.c = this.bs.nextInt(20) + 10;
        this.e(n2);
    }

    protected void b() {
        super.b();
        this.bD.a(16, new Byte(1));
    }

    public void e(int n2) {
        this.bD.b(16, new Byte((byte)n2));
        this.b(0.6f * (float)n2, 0.6f * (float)n2);
        this.Y = n2 * n2;
        this.e(this.aM, this.aN, this.aO);
    }

    public int v() {
        return this.bD.a(16);
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Size", this.v() - 1);
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.e(nu2.e("Size") + 1);
    }

    public void w_() {
        this.b = this.a;
        boolean bl2 = this.aX;
        super.w_();
        if (this.aX && !bl2) {
            int n2 = this.v();
            for (int i2 = 0; i2 < n2 * 8; ++i2) {
                float f2 = this.bs.nextFloat() * (float)Math.PI * 2.0f;
                float f3 = this.bs.nextFloat() * 0.5f + 0.5f;
                float f4 = in.a(f2) * (float)n2 * 0.5f * f3;
                float f5 = in.b(f2) * (float)n2 * 0.5f * f3;
                this.aI.a("slime", this.aM + (double)f4, this.aW.b, this.aO + (double)f5, 0.0, 0.0, 0.0);
            }
            if (n2 > 2) {
                this.aI.a(this, "mob.slime", this.k(), ((this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            }
            this.a = -0.5f;
        }
        this.a *= 0.6f;
    }

    protected void f_() {
        this.X();
        gs gs2 = this.aI.a((sn)this, 16.0);
        if (gs2 != null) {
            this.a(gs2, 10.0f, 20.0f);
        }
        if (this.aX && this.c-- <= 0) {
            this.c = this.bs.nextInt(20) + 10;
            if (gs2 != null) {
                this.c /= 3;
            }
            this.az = true;
            if (this.v() > 1) {
                this.aI.a(this, "mob.slime", this.k(), ((this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f) * 0.8f);
            }
            this.a = 1.0f;
            this.aw = 1.0f - this.bs.nextFloat() * 2.0f;
            this.ax = 1 * this.v();
        } else {
            this.az = false;
            if (this.aX) {
                this.ax = 0.0f;
                this.aw = 0.0f;
            }
        }
    }

    public void K() {
        int n2 = this.v();
        if (!this.aI.B && n2 > 1 && this.Y == 0) {
            for (int i2 = 0; i2 < 4; ++i2) {
                float f2 = ((float)(i2 % 2) - 0.5f) * (float)n2 / 4.0f;
                float f3 = ((float)(i2 / 2) - 0.5f) * (float)n2 / 4.0f;
                uw uw2 = new uw(this.aI);
                uw2.e(n2 / 2);
                uw2.c(this.aM + (double)f2, this.aN + 0.5, this.aO + (double)f3, this.bs.nextFloat() * 360.0f, 0.0f);
                this.aI.b(uw2);
            }
        }
        super.K();
    }

    public void b(gs gs2) {
        int n2 = this.v();
        if (n2 > 1 && this.e(gs2) && (double)this.f(gs2) < 0.6 * (double)n2 && gs2.a((sn)this, n2)) {
            this.aI.a(this, "mob.slimeattack", 1.0f, (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
        }
    }

    protected String j_() {
        return "mob.slime";
    }

    protected String i() {
        return "mob.slime";
    }

    protected int j() {
        if (this.v() == 1) {
            return gm.aK.bf;
        }
        return 0;
    }

    public boolean d() {
        lm lm2 = this.aI.b(in.b(this.aM), in.b(this.aO));
        return (this.v() == 1 || this.aI.q > 0) && this.bs.nextInt(10) == 0 && lm2.a(987234911L).nextInt(10) == 0 && this.aN < 16.0;
    }

    protected float k() {
        return 0.6f;
    }
}

