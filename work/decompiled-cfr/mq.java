/*
 * Decompiled with CFR 0.152.
 */
public class mq
implements lw {
    private iz[] a;
    private int b;
    private dw c;

    public mq(dw dw2, int n2, int n3) {
        int n4 = n2 * n3;
        this.a = new iz[n4];
        this.c = dw2;
        this.b = n2;
    }

    public int a() {
        return this.a.length;
    }

    public iz f_(int n2) {
        if (n2 >= this.a()) {
            return null;
        }
        return this.a[n2];
    }

    public iz b(int n2, int n3) {
        if (n2 < 0 || n2 >= this.b) {
            return null;
        }
        int n4 = n2 + n3 * this.b;
        return this.f_(n4);
    }

    public String c() {
        return "Crafting";
    }

    public iz a(int n2, int n3) {
        if (this.a[n2] != null) {
            if (this.a[n2].a <= n3) {
                iz iz2 = this.a[n2];
                this.a[n2] = null;
                this.c.a(this);
                return iz2;
            }
            iz iz3 = this.a[n2].a(n3);
            if (this.a[n2].a == 0) {
                this.a[n2] = null;
            }
            this.c.a(this);
            return iz3;
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        this.a[n2] = iz2;
        this.c.a(this);
    }

    public int d() {
        return 64;
    }

    public void y_() {
    }

    public boolean a_(gs gs2) {
        return true;
    }
}

