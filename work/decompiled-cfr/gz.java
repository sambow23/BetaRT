/*
 * Decompiled with CFR 0.152.
 */
public class gz
extends ii
implements ff {
    protected int c = 2;

    public gz(fd fd2) {
        super(fd2);
        this.Y = 20;
    }

    public void o() {
        float f2 = this.a(1.0f);
        if (f2 > 0.5f) {
            this.av += 2;
        }
        super.o();
    }

    public void w_() {
        super.w_();
        if (!this.aI.B && this.aI.q == 0) {
            this.K();
        }
    }

    protected sn g_() {
        gs gs2 = this.aI.a((sn)this, 16.0);
        if (gs2 != null && this.e(gs2)) {
            return gs2;
        }
        return null;
    }

    public boolean a(sn sn2, int n2) {
        if (super.a(sn2, n2)) {
            if (this.aG == sn2 || this.aH == sn2) {
                return true;
            }
            if (sn2 != this) {
                this.d = sn2;
            }
            return true;
        }
        return false;
    }

    protected void a(sn sn2, float f2) {
        if (this.ae <= 0 && f2 < 2.0f && sn2.aW.e > this.aW.b && sn2.aW.b < this.aW.e) {
            this.ae = 20;
            sn2.a(this, this.c);
        }
    }

    protected float a(int n2, int n3, int n4) {
        return 0.5f - this.aI.c(n2, n3, n4);
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    public boolean d() {
        int n2;
        int n3;
        int n4 = in.b(this.aM);
        if (this.aI.a(eb.a, n4, n3 = in.b(this.aW.b), n2 = in.b(this.aO)) > this.bs.nextInt(32)) {
            return false;
        }
        int n5 = this.aI.n(n4, n3, n2);
        if (this.aI.B()) {
            int n6 = this.aI.f;
            this.aI.f = 10;
            n5 = this.aI.n(n4, n3, n2);
            this.aI.f = n6;
        }
        return n5 <= this.bs.nextInt(8) && super.d();
    }
}

