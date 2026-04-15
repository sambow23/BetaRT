/*
 * Decompiled with CFR 0.152.
 */
public class gb
extends gz {
    int a;
    int b;

    public gb(fd fd2) {
        super(fd2);
        this.O = "/mob/creeper.png";
    }

    protected void b() {
        super.b();
        this.bD.a(16, (byte)-1);
        this.bD.a(17, (byte)0);
    }

    public void b(nu nu2) {
        super.b(nu2);
        if (this.bD.a(17) == 1) {
            nu2.a("powered", true);
        }
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.bD.b(17, (byte)(nu2.m("powered") ? 1 : 0));
    }

    protected void b(sn sn2, float f2) {
        if (this.aI.B) {
            return;
        }
        if (this.a > 0) {
            this.e(-1);
            --this.a;
            if (this.a < 0) {
                this.a = 0;
            }
        }
    }

    public void w_() {
        this.b = this.a;
        if (this.aI.B) {
            int n2 = this.v();
            if (n2 > 0 && this.a == 0) {
                this.aI.a(this, "random.fuse", 1.0f, 0.5f);
            }
            this.a += n2;
            if (this.a < 0) {
                this.a = 0;
            }
            if (this.a >= 30) {
                this.a = 30;
            }
        }
        super.w_();
        if (this.d == null && this.a > 0) {
            this.e(-1);
            --this.a;
            if (this.a < 0) {
                this.a = 0;
            }
        }
    }

    protected String j_() {
        return "mob.creeper";
    }

    protected String i() {
        return "mob.creeperdeath";
    }

    public void b(sn sn2) {
        super.b(sn2);
        if (sn2 instanceof fr) {
            this.b(gm.bd.bf + this.bs.nextInt(2), 1);
        }
    }

    protected void a(sn sn2, float f2) {
        if (this.aI.B) {
            return;
        }
        int n2 = this.v();
        if (n2 <= 0 && f2 < 3.0f || n2 > 0 && f2 < 7.0f) {
            if (this.a == 0) {
                this.aI.a(this, "random.fuse", 1.0f, 0.5f);
            }
            this.e(1);
            ++this.a;
            if (this.a >= 30) {
                if (this.s()) {
                    this.aI.a((sn)this, this.aM, this.aN, this.aO, 6.0f);
                } else {
                    this.aI.a((sn)this, this.aM, this.aN, this.aO, 3.0f);
                }
                this.K();
            }
            this.e = true;
        } else {
            this.e(-1);
            --this.a;
            if (this.a < 0) {
                this.a = 0;
            }
        }
    }

    public boolean s() {
        return this.bD.a(17) == 1;
    }

    public float a_(float f2) {
        return ((float)this.b + (float)(this.a - this.b) * f2) / 28.0f;
    }

    protected int j() {
        return gm.K.bf;
    }

    private int v() {
        return this.bD.a(16);
    }

    private void e(int n2) {
        this.bD.b(16, (byte)n2);
    }

    public void a(c c2) {
        super.a(c2);
        this.bD.b(17, (byte)1);
    }
}

