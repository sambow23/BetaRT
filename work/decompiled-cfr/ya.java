/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class ya
extends uz {
    private int a = 0;
    private int b = 0;
    private static final iz f = new iz(gm.E, 1);

    public ya(fd fd2) {
        super(fd2);
        this.O = "/mob/pigzombie.png";
        this.aB = 0.5f;
        this.c = 5;
        this.bC = true;
    }

    public void w_() {
        float f2 = this.aB = this.d != null ? 0.95f : 0.5f;
        if (this.b > 0 && --this.b == 0) {
            this.aI.a(this, "mob.zombiepig.zpigangry", this.k() * 2.0f, ((this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f) * 1.8f);
        }
        super.w_();
    }

    public boolean d() {
        return this.aI.q > 0 && this.aI.a(this.aW) && this.aI.a((sn)this, this.aW).size() == 0 && !this.aI.b(this.aW);
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Anger", (short)this.a);
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.a = nu2.d("Anger");
    }

    protected sn g_() {
        if (this.a == 0) {
            return null;
        }
        return super.g_();
    }

    public void o() {
        super.o();
    }

    public boolean a(sn sn2, int n2) {
        if (sn2 instanceof gs) {
            List list = this.aI.b(this, this.aW.b(32.0, 32.0, 32.0));
            for (int i2 = 0; i2 < list.size(); ++i2) {
                sn sn3 = (sn)list.get(i2);
                if (!(sn3 instanceof ya)) continue;
                ya ya2 = (ya)sn3;
                ya2.d(sn2);
            }
            this.d(sn2);
        }
        return super.a(sn2, n2);
    }

    private void d(sn sn2) {
        this.d = sn2;
        this.a = 400 + this.bs.nextInt(400);
        this.b = this.bs.nextInt(40);
    }

    protected String g() {
        return "mob.zombiepig.zpig";
    }

    protected String j_() {
        return "mob.zombiepig.zpighurt";
    }

    protected String i() {
        return "mob.zombiepig.zpigdeath";
    }

    protected int j() {
        return gm.ap.bf;
    }

    public iz r_() {
        return f;
    }
}

