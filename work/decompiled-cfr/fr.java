/*
 * Decompiled with CFR 0.152.
 */
public class fr
extends gz {
    private static final iz a = new iz(gm.i, 1);

    public fr(fd fd2) {
        super(fd2);
        this.O = "/mob/skeleton.png";
    }

    protected String g() {
        return "mob.skeleton";
    }

    protected String j_() {
        return "mob.skeletonhurt";
    }

    protected String i() {
        return "mob.skeletonhurt";
    }

    public void o() {
        float f2;
        if (this.aI.f() && (f2 = this.a(1.0f)) > 0.5f && this.aI.l(in.b(this.aM), in.b(this.aN), in.b(this.aO)) && this.bs.nextFloat() * 30.0f < (f2 - 0.4f) * 2.0f) {
            this.bv = 300;
        }
        super.o();
    }

    protected void a(sn sn2, float f2) {
        if (f2 < 10.0f) {
            double d2 = sn2.aM - this.aM;
            double d3 = sn2.aO - this.aO;
            if (this.ae == 0) {
                sl sl2 = new sl(this.aI, this);
                sl2.aN += (double)1.4f;
                double d4 = sn2.aN + (double)sn2.w() - (double)0.2f - sl2.aN;
                float f3 = in.a(d2 * d2 + d3 * d3) * 0.2f;
                this.aI.a(this, "random.bow", 1.0f, 1.0f / (this.bs.nextFloat() * 0.4f + 0.8f));
                this.aI.b(sl2);
                sl2.a(d2, d4 + (double)f3, d3, 0.6f, 12.0f);
                this.ae = 30;
            }
            this.aS = (float)(Math.atan2(d3, d2) * 180.0 / 3.1415927410125732) - 90.0f;
            this.e = true;
        }
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    protected int j() {
        return gm.j.bf;
    }

    protected void q() {
        int n2;
        int n3 = this.bs.nextInt(3);
        for (n2 = 0; n2 < n3; ++n2) {
            this.b(gm.j.bf, 1);
        }
        n3 = this.bs.nextInt(3);
        for (n2 = 0; n2 < n3; ++n2) {
            this.b(gm.aV.bf, 1);
        }
    }

    public iz r_() {
        return a;
    }
}

