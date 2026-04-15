/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class c
extends yu {
    private int b;
    public long a = 0L;
    private int c;

    public c(fd fd2, double d2, double d3, double d4) {
        super(fd2);
        this.c(d2, d3, d4, 0.0f, 0.0f);
        this.b = 2;
        this.a = this.bs.nextLong();
        this.c = this.bs.nextInt(3) + 1;
        if (fd2.q >= 2 && fd2.b(in.b(d2), in.b(d3), in.b(d4), 10)) {
            int n2;
            int n3;
            int n4 = in.b(d2);
            if (fd2.a(n4, n3 = in.b(d3), n2 = in.b(d4)) == 0 && uu.as.a(fd2, n4, n3, n2)) {
                fd2.f(n4, n3, n2, uu.as.bn);
            }
            for (n4 = 0; n4 < 4; ++n4) {
                int n5;
                n3 = in.b(d2) + this.bs.nextInt(3) - 1;
                if (fd2.a(n3, n2 = in.b(d3) + this.bs.nextInt(3) - 1, n5 = in.b(d4) + this.bs.nextInt(3) - 1) != 0 || !uu.as.a(fd2, n3, n2, n5)) continue;
                fd2.f(n3, n2, n5, uu.as.bn);
            }
        }
    }

    public void w_() {
        super.w_();
        if (this.b == 2) {
            this.aI.a(this.aM, this.aN, this.aO, "ambient.weather.thunder", 10000.0f, 0.8f + this.bs.nextFloat() * 0.2f);
            this.aI.a(this.aM, this.aN, this.aO, "random.explode", 2.0f, 0.5f + this.bs.nextFloat() * 0.2f);
        }
        --this.b;
        if (this.b < 0) {
            if (this.c == 0) {
                this.K();
            } else if (this.b < -this.bs.nextInt(10)) {
                int n2;
                int n3;
                int n4;
                --this.c;
                this.b = 1;
                this.a = this.bs.nextLong();
                if (this.aI.b(in.b(this.aM), in.b(this.aN), in.b(this.aO), 10) && this.aI.a(n4 = in.b(this.aM), n3 = in.b(this.aN), n2 = in.b(this.aO)) == 0 && uu.as.a(this.aI, n4, n3, n2)) {
                    this.aI.f(n4, n3, n2, uu.as.bn);
                }
            }
        }
        if (this.b >= 0) {
            double d2 = 3.0;
            List list = this.aI.b(this, eq.b(this.aM - d2, this.aN - d2, this.aO - d2, this.aM + d2, this.aN + 6.0 + d2, this.aO + d2));
            for (int i2 = 0; i2 < list.size(); ++i2) {
                sn sn2 = (sn)list.get(i2);
                sn2.a(this);
            }
            this.aI.n = 2;
        }
    }

    protected void b() {
    }

    protected void a(nu nu2) {
    }

    protected void b(nu nu2) {
    }

    public boolean a(bt bt2) {
        return this.b >= 0;
    }
}

