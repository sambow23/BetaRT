/*
 * Decompiled with CFR 0.152.
 */
public class ju
extends sn {
    public int a;
    public int b = 0;

    public ju(fd fd2) {
        super(fd2);
    }

    public ju(fd fd2, double d2, double d3, double d4, int n2) {
        super(fd2);
        this.a = n2;
        this.aF = true;
        this.b(0.98f, 0.98f);
        this.bf = this.bh / 2.0f;
        this.e(d2, d3, d4);
        this.aP = 0.0;
        this.aQ = 0.0;
        this.aR = 0.0;
        this.aJ = d2;
        this.aK = d3;
        this.aL = d4;
    }

    protected boolean n() {
        return false;
    }

    protected void b() {
    }

    public boolean h_() {
        return !this.be;
    }

    public void w_() {
        if (this.a == 0) {
            this.K();
            return;
        }
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        ++this.b;
        this.aQ -= (double)0.04f;
        this.b(this.aP, this.aQ, this.aR);
        this.aP *= (double)0.98f;
        this.aQ *= (double)0.98f;
        this.aR *= (double)0.98f;
        int n2 = in.b(this.aM);
        int n3 = in.b(this.aN);
        int n4 = in.b(this.aO);
        if (this.aI.a(n2, n3, n4) == this.a) {
            this.aI.f(n2, n3, n4, 0);
        }
        if (this.aX) {
            this.aP *= (double)0.7f;
            this.aR *= (double)0.7f;
            this.aQ *= -0.5;
            this.K();
            if (!(this.aI.a(this.a, n2, n3, n4, true, 1) && !gk.c_(this.aI, n2, n3 - 1, n4) && this.aI.f(n2, n3, n4, this.a) || this.aI.B)) {
                this.b(this.a, 1);
            }
        } else if (this.b > 100 && !this.aI.B) {
            this.b(this.a, 1);
            this.K();
        }
    }

    protected void b(nu nu2) {
        nu2.a("Tile", (byte)this.a);
    }

    protected void a(nu nu2) {
        this.a = nu2.c("Tile") & 0xFF;
    }

    public float x_() {
        return 0.0f;
    }

    public fd k() {
        return this.aI;
    }
}

