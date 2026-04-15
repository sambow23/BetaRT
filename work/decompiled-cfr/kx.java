/*
 * Decompiled with CFR 0.152.
 */
import java.io.IOException;

public class kx
implements cl {
    private lm c;
    private cl d;
    private bf e;
    private lm[] f;
    private fd g;
    int a;
    int b;
    private lm h;
    private int i;
    private int j;

    public void d(int n2, int n3) {
        this.i = n2;
        this.j = n3;
    }

    public boolean e(int n2, int n3) {
        int n4 = 15;
        return n2 >= this.i - n4 && n3 >= this.j - n4 && n2 <= this.i + n4 && n3 <= this.j + n4;
    }

    public boolean a(int n2, int n3) {
        if (!this.e(n2, n3)) {
            return false;
        }
        if (n2 == this.a && n3 == this.b && this.h != null) {
            return true;
        }
        int n4 = n2 & 0x1F;
        int n5 = n3 & 0x1F;
        int n6 = n4 + n5 * 32;
        return this.f[n6] != null && (this.f[n6] == this.c || this.f[n6].a(n2, n3));
    }

    public lm c(int n2, int n3) {
        return this.b(n2, n3);
    }

    public lm b(int n2, int n3) {
        if (n2 == this.a && n3 == this.b && this.h != null) {
            return this.h;
        }
        if (!this.g.y && !this.e(n2, n3)) {
            return this.c;
        }
        int n4 = n2 & 0x1F;
        int n5 = n3 & 0x1F;
        int n6 = n4 + n5 * 32;
        if (!this.a(n2, n3)) {
            lm lm2;
            if (this.f[n6] != null) {
                this.f[n6].f();
                this.b(this.f[n6]);
                this.a(this.f[n6]);
            }
            if ((lm2 = this.f(n2, n3)) == null) {
                if (this.d == null) {
                    lm2 = this.c;
                } else {
                    lm2 = this.d.b(n2, n3);
                    lm2.i();
                }
            }
            this.f[n6] = lm2;
            lm2.d();
            if (this.f[n6] != null) {
                this.f[n6].e();
            }
            if (!this.f[n6].n && this.a(n2 + 1, n3 + 1) && this.a(n2, n3 + 1) && this.a(n2 + 1, n3)) {
                this.a(this, n2, n3);
            }
            if (this.a(n2 - 1, n3) && !this.b((int)(n2 - 1), (int)n3).n && this.a(n2 - 1, n3 + 1) && this.a(n2, n3 + 1) && this.a(n2 - 1, n3)) {
                this.a(this, n2 - 1, n3);
            }
            if (this.a(n2, n3 - 1) && !this.b((int)n2, (int)(n3 - 1)).n && this.a(n2 + 1, n3 - 1) && this.a(n2, n3 - 1) && this.a(n2 + 1, n3)) {
                this.a(this, n2, n3 - 1);
            }
            if (this.a(n2 - 1, n3 - 1) && !this.b((int)(n2 - 1), (int)(n3 - 1)).n && this.a(n2 - 1, n3 - 1) && this.a(n2, n3 - 1) && this.a(n2 - 1, n3)) {
                this.a(this, n2 - 1, n3 - 1);
            }
        }
        this.a = n2;
        this.b = n3;
        this.h = this.f[n6];
        return this.f[n6];
    }

    private lm f(int n2, int n3) {
        if (this.e == null) {
            return this.c;
        }
        try {
            lm lm2 = this.e.a(this.g, n2, n3);
            if (lm2 != null) {
                lm2.r = this.g.t();
            }
            return lm2;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return this.c;
        }
    }

    private void a(lm lm2) {
        if (this.e == null) {
            return;
        }
        try {
            this.e.b(this.g, lm2);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void b(lm lm2) {
        if (this.e == null) {
            return;
        }
        try {
            lm2.r = this.g.t();
            this.e.a(this.g, lm2);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void a(cl cl2, int n2, int n3) {
        lm lm2 = this.b(n2, n3);
        if (!lm2.n) {
            lm2.n = true;
            if (this.d != null) {
                this.d.a(cl2, n2, n3);
                lm2.g();
            }
        }
    }

    public boolean a(boolean bl2, yb yb2) {
        int n2;
        int n3 = 0;
        int n4 = 0;
        if (yb2 != null) {
            for (n2 = 0; n2 < this.f.length; ++n2) {
                if (this.f[n2] == null || !this.f[n2].a(bl2)) continue;
                ++n4;
            }
        }
        n2 = 0;
        for (int i2 = 0; i2 < this.f.length; ++i2) {
            if (this.f[i2] == null) continue;
            if (bl2 && !this.f[i2].p) {
                this.a(this.f[i2]);
            }
            if (!this.f[i2].a(bl2)) continue;
            this.b(this.f[i2]);
            this.f[i2].o = false;
            if (++n3 == 2 && !bl2) {
                return false;
            }
            if (yb2 == null || ++n2 % 10 != 0) continue;
            yb2.a(n2 * 100 / n4);
        }
        if (bl2) {
            if (this.e == null) {
                return true;
            }
            this.e.b();
        }
        return true;
    }

    public boolean a() {
        if (this.e != null) {
            this.e.a();
        }
        return this.d.a();
    }

    public boolean b() {
        return true;
    }

    public String c() {
        return "ChunkCache: " + this.f.length;
    }
}

