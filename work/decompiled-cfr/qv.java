/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

public class qv
extends sn {
    private int f = 0;
    public int a = 0;
    public int b;
    public int c;
    public int d;
    public iq e;

    public qv(fd fd2) {
        super(fd2);
        this.bf = 0.0f;
        this.b(0.5f, 0.5f);
    }

    public qv(fd fd2, int n2, int n3, int n4, int n5) {
        this(fd2);
        this.b = n2;
        this.c = n3;
        this.d = n4;
        ArrayList<iq> arrayList = new ArrayList<iq>();
        iq[] iqArray = iq.values();
        int n6 = iqArray.length;
        for (int i2 = 0; i2 < n6; ++i2) {
            iq iq2;
            this.e = iq2 = iqArray[i2];
            this.b(n5);
            if (!this.k()) continue;
            arrayList.add(iq2);
        }
        if (arrayList.size() > 0) {
            this.e = (iq)((Object)arrayList.get(this.bs.nextInt(arrayList.size())));
        }
        this.b(n5);
    }

    public qv(fd fd2, int n2, int n3, int n4, int n5, String string) {
        this(fd2);
        this.b = n2;
        this.c = n3;
        this.d = n4;
        for (iq iq2 : iq.values()) {
            if (!iq2.A.equals(string)) continue;
            this.e = iq2;
            break;
        }
        this.b(n5);
    }

    protected void b() {
    }

    public void b(int n2) {
        this.a = n2;
        this.aU = this.aS = (float)(n2 * 90);
        float f2 = this.e.B;
        float f3 = this.e.C;
        float f4 = this.e.B;
        if (n2 == 0 || n2 == 2) {
            f4 = 0.5f;
        } else {
            f2 = 0.5f;
        }
        f2 /= 32.0f;
        f3 /= 32.0f;
        f4 /= 32.0f;
        float f5 = (float)this.b + 0.5f;
        float f6 = (float)this.c + 0.5f;
        float f7 = (float)this.d + 0.5f;
        float f8 = 0.5625f;
        if (n2 == 0) {
            f7 -= f8;
        }
        if (n2 == 1) {
            f5 -= f8;
        }
        if (n2 == 2) {
            f7 += f8;
        }
        if (n2 == 3) {
            f5 += f8;
        }
        if (n2 == 0) {
            f5 -= this.c(this.e.B);
        }
        if (n2 == 1) {
            f7 += this.c(this.e.B);
        }
        if (n2 == 2) {
            f5 += this.c(this.e.B);
        }
        if (n2 == 3) {
            f7 -= this.c(this.e.B);
        }
        this.e(f5, f6 += this.c(this.e.C), f7);
        float f9 = -0.00625f;
        this.aW.c(f5 - f2 - f9, f6 - f3 - f9, f7 - f4 - f9, f5 + f2 + f9, f6 + f3 + f9, f7 + f4 + f9);
    }

    private float c(int n2) {
        if (n2 == 32) {
            return 0.5f;
        }
        if (n2 == 64) {
            return 0.5f;
        }
        return 0.0f;
    }

    public void w_() {
        if (this.f++ == 100 && !this.aI.B) {
            this.f = 0;
            if (!this.k()) {
                this.K();
                this.aI.b(new hl(this.aI, this.aM, this.aN, this.aO, new iz(gm.aq)));
            }
        }
    }

    public boolean k() {
        int n2;
        if (this.aI.a((sn)this, this.aW).size() > 0) {
            return false;
        }
        int n3 = this.e.B / 16;
        int n4 = this.e.C / 16;
        int n5 = this.b;
        int n6 = this.c;
        int n7 = this.d;
        if (this.a == 0) {
            n5 = in.b(this.aM - (double)((float)this.e.B / 32.0f));
        }
        if (this.a == 1) {
            n7 = in.b(this.aO - (double)((float)this.e.B / 32.0f));
        }
        if (this.a == 2) {
            n5 = in.b(this.aM - (double)((float)this.e.B / 32.0f));
        }
        if (this.a == 3) {
            n7 = in.b(this.aO - (double)((float)this.e.B / 32.0f));
        }
        n6 = in.b(this.aN - (double)((float)this.e.C / 32.0f));
        for (int i2 = 0; i2 < n3; ++i2) {
            for (n2 = 0; n2 < n4; ++n2) {
                ln ln2 = this.a == 0 || this.a == 2 ? this.aI.f(n5 + i2, n6 + n2, this.d) : this.aI.f(this.b, n6 + n2, n7 + i2);
                if (ln2.a()) continue;
                return false;
            }
        }
        List list = this.aI.b(this, this.aW);
        for (n2 = 0; n2 < list.size(); ++n2) {
            if (!(list.get(n2) instanceof qv)) continue;
            return false;
        }
        return true;
    }

    public boolean h_() {
        return true;
    }

    public boolean a(sn sn2, int n2) {
        if (!this.be && !this.aI.B) {
            this.K();
            this.ai();
            this.aI.b(new hl(this.aI, this.aM, this.aN, this.aO, new iz(gm.aq)));
        }
        return true;
    }

    public void b(nu nu2) {
        nu2.a("Dir", (byte)this.a);
        nu2.a("Motive", this.e.A);
        nu2.a("TileX", this.b);
        nu2.a("TileY", this.c);
        nu2.a("TileZ", this.d);
    }

    public void a(nu nu2) {
        this.a = nu2.c("Dir");
        this.b = nu2.e("TileX");
        this.c = nu2.e("TileY");
        this.d = nu2.e("TileZ");
        String string = nu2.i("Motive");
        for (iq iq2 : iq.values()) {
            if (!iq2.A.equals(string)) continue;
            this.e = iq2;
        }
        if (this.e == null) {
            this.e = iq.a;
        }
        this.b(this.a);
    }

    public void b(double d2, double d3, double d4) {
        if (!this.aI.B && d2 * d2 + d3 * d3 + d4 * d4 > 0.0) {
            this.K();
            this.aI.b(new hl(this.aI, this.aM, this.aN, this.aO, new iz(gm.aq)));
        }
    }

    public void d(double d2, double d3, double d4) {
        if (!this.aI.B && d2 * d2 + d3 * d3 + d4 * d4 > 0.0) {
            this.K();
            this.aI.b(new hl(this.aI, this.aM, this.aN, this.aO, new iz(gm.aq)));
        }
    }
}

