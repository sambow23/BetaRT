/*
 * Decompiled with CFR 0.152.
 */
public class wh
extends bg {
    public wh(fd fd2) {
        super(fd2);
        this.O = "/mob/pig.png";
        this.b(0.9f, 0.9f);
    }

    protected void b() {
        this.bD.a(16, (byte)0);
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Saddle", this.v());
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.a(nu2.m("Saddle"));
    }

    protected String g() {
        return "mob.pig";
    }

    protected String j_() {
        return "mob.pig";
    }

    protected String i() {
        return "mob.pigdeath";
    }

    public boolean a(gs gs2) {
        if (this.v() && !this.aI.B && (this.aG == null || this.aG == gs2)) {
            gs2.i(this);
            return true;
        }
        return false;
    }

    protected int j() {
        if (this.bv > 0) {
            return gm.ap.bf;
        }
        return gm.ao.bf;
    }

    public boolean v() {
        return (this.bD.a(16) & 1) != 0;
    }

    public void a(boolean bl2) {
        if (bl2) {
            this.bD.b(16, (byte)1);
        } else {
            this.bD.b(16, (byte)0);
        }
    }

    public void a(c c2) {
        if (this.aI.B) {
            return;
        }
        ya ya2 = new ya(this.aI);
        ya2.c(this.aM, this.aN, this.aO, this.aS, this.aT);
        this.aI.b(ya2);
        this.K();
    }

    protected void b(float f2) {
        super.b(f2);
        if (f2 > 5.0f && this.aG instanceof gs) {
            ((gs)this.aG).a(ep.u);
        }
    }
}

