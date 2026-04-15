/*
 * Decompiled with CFR 0.152.
 */
public class ww
extends bg {
    public boolean a = false;
    public float b = 0.0f;
    public float c = 0.0f;
    public float f;
    public float g;
    public float h = 1.0f;
    public int i;

    public ww(fd fd2) {
        super(fd2);
        this.O = "/mob/chicken.png";
        this.b(0.3f, 0.4f);
        this.Y = 4;
        this.i = this.bs.nextInt(6000) + 6000;
    }

    public void o() {
        super.o();
        this.g = this.b;
        this.f = this.c;
        this.c = (float)((double)this.c + (double)(this.aX ? -1 : 4) * 0.3);
        if (this.c < 0.0f) {
            this.c = 0.0f;
        }
        if (this.c > 1.0f) {
            this.c = 1.0f;
        }
        if (!this.aX && this.h < 1.0f) {
            this.h = 1.0f;
        }
        this.h = (float)((double)this.h * 0.9);
        if (!this.aX && this.aQ < 0.0) {
            this.aQ *= 0.6;
        }
        this.b += this.h * 2.0f;
        if (!this.aI.B && --this.i <= 0) {
            this.aI.a(this, "mob.chickenplop", 1.0f, (this.bs.nextFloat() - this.bs.nextFloat()) * 0.2f + 1.0f);
            this.b(gm.aN.bf, 1);
            this.i = this.bs.nextInt(6000) + 6000;
        }
    }

    protected void b(float f2) {
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    protected String g() {
        return "mob.chicken";
    }

    protected String j_() {
        return "mob.chickenhurt";
    }

    protected String i() {
        return "mob.chickenhurt";
    }

    protected int j() {
        return gm.J.bf;
    }
}

