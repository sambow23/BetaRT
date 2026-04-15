/*
 * Decompiled with CFR 0.152.
 */
public class qw
extends sn {
    public int a = 0;

    public qw(fd fd2) {
        super(fd2);
        this.aF = true;
        this.b(0.98f, 0.98f);
        this.bf = this.bh / 2.0f;
    }

    public qw(fd fd2, double d2, double d3, double d4) {
        this(fd2);
        this.e(d2, d3, d4);
        float f2 = (float)(Math.random() * 3.1415927410125732 * 2.0);
        this.aP = -in.a(f2 * (float)Math.PI / 180.0f) * 0.02f;
        this.aQ = 0.2f;
        this.aR = -in.b(f2 * (float)Math.PI / 180.0f) * 0.02f;
        this.a = 80;
        this.aJ = d2;
        this.aK = d3;
        this.aL = d4;
    }

    protected void b() {
    }

    protected boolean n() {
        return false;
    }

    public boolean h_() {
        return !this.be;
    }

    public void w_() {
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        this.aQ -= (double)0.04f;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.98f;
        this.aQ *= (double)0.98f;
        this.aR *= (double)0.98f;
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
            this.aQ *= -0.5;
        }
        if (this.a-- <= 0) {
            if (!this.aI.B) {
                this.K();
                this.k();
            } else {
                this.K();
            }
        } else {
            this.aI.a("smoke", this.aM, this.aN + 0.5, this.aO, 0.0, 0.0, 0.0);
        }
    }

    private void k() {
        float f2 = 4.0f;
        this.aI.a(null, this.aM, this.aN, this.aO, f2);
    }

    protected void b(nu nu2) {
        nu2.a("Fuse", (byte)this.a);
    }

    protected void a(nu nu2) {
        this.a = nu2.c("Fuse");
    }

    public float x_() {
        return 0.0f;
    }
}

