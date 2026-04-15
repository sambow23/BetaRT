/*
 * Decompiled with CFR 0.152.
 */
public class gp {
    private final int d;
    private final lw e;
    public int a;
    public int b;
    public int c;

    public gp(lw lw2, int n2, int n3, int n4) {
        this.e = lw2;
        this.d = n2;
        this.b = n3;
        this.c = n4;
    }

    public void a(iz iz2) {
        this.c();
    }

    public boolean b(iz iz2) {
        return true;
    }

    public iz a() {
        return this.e.f_(this.d);
    }

    public boolean b() {
        return this.a() != null;
    }

    public void c(iz iz2) {
        this.e.a(this.d, iz2);
        this.c();
    }

    public void c() {
        this.e.y_();
    }

    public int d() {
        return this.e.d();
    }

    public int e() {
        return -1;
    }

    public iz a(int n2) {
        return this.e.a(this.d, n2);
    }
}

