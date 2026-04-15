/*
 * Decompiled with CFR 0.152.
 */
public class fo
extends rw {
    protected fo(int n2, int n3) {
        super(n2, n3, ln.d);
    }

    public int a(int n2) {
        return this.bm + (n2 == 1 ? 1 : 0);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.e(n2, n3, n4) == 0) {
            return false;
        }
        this.b_(fd2, n2, n3, n4);
        return true;
    }

    public void f(fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.B) {
            return;
        }
        eg eg2 = (eg)fd2.b(n2, n3, n4);
        eg2.a = n5;
        eg2.y_();
        fd2.d(n2, n3, n4, 1);
    }

    public void b_(fd fd2, int n2, int n3, int n4) {
        if (fd2.B) {
            return;
        }
        eg eg2 = (eg)fd2.b(n2, n3, n4);
        int n5 = eg2.a;
        if (n5 == 0) {
            return;
        }
        fd2.e(1005, n2, n3, n4, 0);
        fd2.a((String)null, n2, n3, n4);
        eg2.a = 0;
        eg2.y_();
        fd2.d(n2, n3, n4, 0);
        int n6 = n5;
        float f2 = 0.7f;
        double d2 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        double d3 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.2 + 0.6;
        double d4 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        hl hl2 = new hl(fd2, (double)n2 + d2, (double)n3 + d3, (double)n4 + d4, new iz(n6, 1, 0));
        hl2.c = 10;
        fd2.b(hl2);
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        this.b_(fd2, n2, n3, n4);
        super.b(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, float f2) {
        if (fd2.B) {
            return;
        }
        super.a(fd2, n2, n3, n4, n5, f2);
    }

    protected ow a_() {
        return new eg();
    }
}

