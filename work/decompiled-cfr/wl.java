/*
 * Decompiled with CFR 0.152.
 */
public class wl
implements lw {
    private iz[] a = new iz[1];

    public int a() {
        return 1;
    }

    public iz f_(int n2) {
        return this.a[n2];
    }

    public String c() {
        return "Result";
    }

    public iz a(int n2, int n3) {
        if (this.a[n2] != null) {
            iz iz2 = this.a[n2];
            this.a[n2] = null;
            return iz2;
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        this.a[n2] = iz2;
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

