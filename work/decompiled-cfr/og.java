/*
 * Decompiled with CFR 0.152.
 */
public class og
implements lw {
    private String a;
    private lw b;
    private lw c;

    public og(String string, lw lw2, lw lw3) {
        this.a = string;
        this.b = lw2;
        this.c = lw3;
    }

    public int a() {
        return this.b.a() + this.c.a();
    }

    public String c() {
        return this.a;
    }

    public iz f_(int n2) {
        if (n2 >= this.b.a()) {
            return this.c.f_(n2 - this.b.a());
        }
        return this.b.f_(n2);
    }

    public iz a(int n2, int n3) {
        if (n2 >= this.b.a()) {
            return this.c.a(n2 - this.b.a(), n3);
        }
        return this.b.a(n2, n3);
    }

    public void a(int n2, iz iz2) {
        if (n2 >= this.b.a()) {
            this.c.a(n2 - this.b.a(), iz2);
        } else {
            this.b.a(n2, iz2);
        }
    }

    public int d() {
        return this.b.d();
    }

    public void y_() {
        this.b.y_();
        this.c.y_();
    }

    public boolean a_(gs gs2) {
        return this.b.a_(gs2) && this.c.a_(gs2);
    }
}

