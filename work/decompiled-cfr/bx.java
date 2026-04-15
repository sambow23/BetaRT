/*
 * Decompiled with CFR 0.152.
 */
public class bx
extends bg {
    public bx(fd fd2) {
        super(fd2);
        this.O = "/mob/cow.png";
        this.b(0.9f, 1.3f);
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    protected String g() {
        return "mob.cow";
    }

    protected String j_() {
        return "mob.cowhurt";
    }

    protected String i() {
        return "mob.cowhurt";
    }

    protected float k() {
        return 0.4f;
    }

    protected int j() {
        return gm.aD.bf;
    }

    public boolean a(gs gs2) {
        iz iz2 = gs2.c.b();
        if (iz2 != null && iz2.c == gm.au.bf) {
            gs2.c.a(gs2.c.c, new iz(gm.aE));
            return true;
        }
        return false;
    }
}

