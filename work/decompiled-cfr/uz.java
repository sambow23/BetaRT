/*
 * Decompiled with CFR 0.152.
 */
public class uz
extends gz {
    public uz(fd fd2) {
        super(fd2);
        this.O = "/mob/zombie.png";
        this.aB = 0.5f;
        this.c = 5;
    }

    public void o() {
        float f2;
        if (this.aI.f() && (f2 = this.a(1.0f)) > 0.5f && this.aI.l(in.b(this.aM), in.b(this.aN), in.b(this.aO)) && this.bs.nextFloat() * 30.0f < (f2 - 0.4f) * 2.0f) {
            this.bv = 300;
        }
        super.o();
    }

    protected String g() {
        return "mob.zombie";
    }

    protected String j_() {
        return "mob.zombiehurt";
    }

    protected String i() {
        return "mob.zombiedeath";
    }

    protected int j() {
        return gm.J.bf;
    }
}

