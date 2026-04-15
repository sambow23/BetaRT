/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class xk
extends ob {
    private int c = -1;
    private int d = -1;
    private int e = -1;
    private float f = 0.0f;
    private float g = 0.0f;
    private float h = 0.0f;
    private int i = 0;
    private boolean j = false;
    private nb k;
    private int l = 0;

    public xk(Minecraft minecraft, nb nb2) {
        super(minecraft);
        this.k = nb2;
    }

    public void a(gs gs2) {
        gs2.aS = -180.0f;
    }

    public boolean b(int n2, int n3, int n4, int n5) {
        int n6 = this.a.f.a(n2, n3, n4);
        boolean bl2 = super.b(n2, n3, n4, n5);
        iz iz2 = this.a.h.G();
        if (iz2 != null) {
            iz2.a(n6, n2, n3, n4, this.a.h);
            if (iz2.a == 0) {
                iz2.a(this.a.h);
                this.a.h.H();
            }
        }
        return bl2;
    }

    public void a(int n2, int n3, int n4, int n5) {
        if (!this.j || n2 != this.c || n3 != this.d || n4 != this.e) {
            this.k.b(new jv(0, n2, n3, n4, n5));
            int n6 = this.a.f.a(n2, n3, n4);
            if (n6 > 0 && this.f == 0.0f) {
                uu.m[n6].b(this.a.f, n2, n3, n4, this.a.h);
            }
            if (n6 > 0 && uu.m[n6].a(this.a.h) >= 1.0f) {
                this.b(n2, n3, n4, n5);
            } else {
                this.j = true;
                this.c = n2;
                this.d = n3;
                this.e = n4;
                this.f = 0.0f;
                this.g = 0.0f;
                this.h = 0.0f;
            }
        }
    }

    public void a() {
        this.f = 0.0f;
        this.j = false;
    }

    public void c(int n2, int n3, int n4, int n5) {
        if (!this.j) {
            return;
        }
        this.e();
        if (this.i > 0) {
            --this.i;
            return;
        }
        if (n2 == this.c && n3 == this.d && n4 == this.e) {
            int n6 = this.a.f.a(n2, n3, n4);
            if (n6 == 0) {
                this.j = false;
                return;
            }
            uu uu2 = uu.m[n6];
            this.f += uu2.a(this.a.h);
            if (this.h % 4.0f == 0.0f && uu2 != null) {
                this.a.B.b(uu2.by.d(), (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f, (uu2.by.b() + 1.0f) / 8.0f, uu2.by.c() * 0.5f);
            }
            this.h += 1.0f;
            if (this.f >= 1.0f) {
                this.j = false;
                this.k.b(new jv(2, n2, n3, n4, n5));
                this.b(n2, n3, n4, n5);
                this.f = 0.0f;
                this.g = 0.0f;
                this.h = 0.0f;
                this.i = 5;
            }
        } else {
            this.a(n2, n3, n4, n5);
        }
    }

    public void a(float f2) {
        if (this.f <= 0.0f) {
            this.a.v.b = 0.0f;
            this.a.g.i = 0.0f;
        } else {
            float f3;
            this.a.v.b = f3 = this.g + (this.f - this.g) * f2;
            this.a.g.i = f3;
        }
    }

    public float b() {
        return 4.0f;
    }

    public void a(fd fd2) {
        super.a(fd2);
    }

    public void c() {
        this.e();
        this.g = this.f;
        this.a.B.c();
    }

    private void e() {
        int n2 = this.a.h.c.c;
        if (n2 != this.l) {
            this.l = n2;
            this.k.b(new ho(this.l));
        }
    }

    public boolean a(gs gs2, fd fd2, iz iz2, int n2, int n3, int n4, int n5) {
        this.e();
        this.k.b(new gx(n2, n3, n4, n5, gs2.c.b()));
        boolean bl2 = super.a(gs2, fd2, iz2, n2, n3, n4, n5);
        return bl2;
    }

    public boolean a(gs gs2, fd fd2, iz iz2) {
        this.e();
        this.k.b(new gx(-1, -1, -1, 255, gs2.c.b()));
        boolean bl2 = super.a(gs2, fd2, iz2);
        return bl2;
    }

    public gs b(fd fd2) {
        return new tk(this.a, fd2, this.a.k, this.k);
    }

    public void b(gs gs2, sn sn2) {
        this.e();
        this.k.b(new a(gs2.aD, sn2.aD, 1));
        gs2.d(sn2);
    }

    public void a(gs gs2, sn sn2) {
        this.e();
        this.k.b(new a(gs2.aD, sn2.aD, 0));
        gs2.c(sn2);
    }

    public iz a(int n2, int n3, int n4, boolean bl2, gs gs2) {
        short s2 = gs2.e.a(gs2.c);
        iz iz2 = super.a(n2, n3, n4, bl2, gs2);
        this.k.b(new qs(n2, n3, n4, bl2, iz2, s2));
        return iz2;
    }

    public void a(int n2, gs gs2) {
        if (n2 == -9999) {
            return;
        }
    }
}

