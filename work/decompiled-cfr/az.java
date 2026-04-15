/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class az
extends ow
implements lw {
    private iz[] a = new iz[9];
    private Random b = new Random();

    public int a() {
        return 9;
    }

    public iz f_(int n2) {
        return this.a[n2];
    }

    public iz a(int n2, int n3) {
        if (this.a[n2] != null) {
            if (this.a[n2].a <= n3) {
                iz iz2 = this.a[n2];
                this.a[n2] = null;
                this.y_();
                return iz2;
            }
            iz iz3 = this.a[n2].a(n3);
            if (this.a[n2].a == 0) {
                this.a[n2] = null;
            }
            this.y_();
            return iz3;
        }
        return null;
    }

    public iz b() {
        int n2 = -1;
        int n3 = 1;
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            if (this.a[i2] == null || this.b.nextInt(n3++) != 0) continue;
            n2 = i2;
        }
        if (n2 >= 0) {
            return this.a(n2, 1);
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        this.a[n2] = iz2;
        if (iz2 != null && iz2.a > this.d()) {
            iz2.a = this.d();
        }
        this.y_();
    }

    public String c() {
        return "Trap";
    }

    public void a(nu nu2) {
        super.a(nu2);
        sp sp2 = nu2.l("Items");
        this.a = new iz[this.a()];
        for (int i2 = 0; i2 < sp2.c(); ++i2) {
            nu nu3 = (nu)sp2.a(i2);
            int n2 = nu3.c("Slot") & 0xFF;
            if (n2 < 0 || n2 >= this.a.length) continue;
            this.a[n2] = new iz(nu3);
        }
    }

    public void b(nu nu2) {
        super.b(nu2);
        sp sp2 = new sp();
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            if (this.a[i2] == null) continue;
            nu nu3 = new nu();
            nu3.a("Slot", (byte)i2);
            this.a[i2].a(nu3);
            sp2.a(nu3);
        }
        nu2.a("Items", sp2);
    }

    public int d() {
        return 64;
    }

    public boolean a_(gs gs2) {
        if (this.d.b(this.e, this.f, this.g) != this) {
            return false;
        }
        return !(gs2.g((double)this.e + 0.5, (double)this.f + 0.5, (double)this.g + 0.5) > 64.0);
    }
}

