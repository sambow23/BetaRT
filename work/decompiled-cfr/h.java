/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.Random;

public class h
extends uu {
    private int a = -1;

    public h(int n2, int n3) {
        super(n2, n3, ln.B);
        this.a(h);
        this.c(0.5f);
    }

    public void a_(int n2) {
        this.a = n2;
    }

    public void a() {
        this.a = -1;
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        super.b(fd2, n2, n3, n4);
        int n5 = fd2.e(n2, n3, n4);
        int n6 = wj.a[h.c(n5)];
        int n7 = fd2.a(n2 += wj.b[n6], n3 += wj.c[n6], n4 += wj.d[n6]);
        if ((n7 == uu.aa.bn || n7 == uu.W.bn) && jq.e(n5 = fd2.e(n2, n3, n4))) {
            uu.m[n7].g(fd2, n2, n3, n4, n5);
            fd2.f(n2, n3, n4, 0);
        }
    }

    public int a(int n2, int n3) {
        int n4 = h.c(n3);
        if (n2 == n4) {
            if (this.a >= 0) {
                return this.a;
            }
            if ((n3 & 8) != 0) {
                return this.bm - 1;
            }
            return this.bm;
        }
        if (n2 == wj.a[n4]) {
            return 107;
        }
        return 108;
    }

    public int b() {
        return 17;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return false;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, int n5) {
        return false;
    }

    public int a(Random random) {
        return 0;
    }

    public void a(fd fd2, int n2, int n3, int n4, eq eq2, ArrayList arrayList) {
        int n5 = fd2.e(n2, n3, n4);
        switch (h.c(n5)) {
            case 0: {
                this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.25f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                this.a(0.375f, 0.25f, 0.375f, 0.625f, 1.0f, 0.625f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                break;
            }
            case 1: {
                this.a(0.0f, 0.75f, 0.0f, 1.0f, 1.0f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                this.a(0.375f, 0.0f, 0.375f, 0.625f, 0.75f, 0.625f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                break;
            }
            case 2: {
                this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.25f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                this.a(0.25f, 0.375f, 0.25f, 0.75f, 0.625f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                break;
            }
            case 3: {
                this.a(0.0f, 0.0f, 0.75f, 1.0f, 1.0f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                this.a(0.25f, 0.375f, 0.0f, 0.75f, 0.625f, 0.75f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                break;
            }
            case 4: {
                this.a(0.0f, 0.0f, 0.0f, 0.25f, 1.0f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                this.a(0.375f, 0.25f, 0.25f, 0.625f, 0.75f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                break;
            }
            case 5: {
                this.a(0.75f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
                this.a(0.0f, 0.375f, 0.25f, 0.75f, 0.625f, 0.75f);
                super.a(fd2, n2, n3, n4, eq2, arrayList);
            }
        }
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        switch (h.c(n5)) {
            case 0: {
                this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.25f, 1.0f);
                break;
            }
            case 1: {
                this.a(0.0f, 0.75f, 0.0f, 1.0f, 1.0f, 1.0f);
                break;
            }
            case 2: {
                this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.25f);
                break;
            }
            case 3: {
                this.a(0.0f, 0.0f, 0.75f, 1.0f, 1.0f, 1.0f);
                break;
            }
            case 4: {
                this.a(0.0f, 0.0f, 0.0f, 0.25f, 1.0f, 1.0f);
                break;
            }
            case 5: {
                this.a(0.75f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = h.c(fd2.e(n2, n3, n4));
        int n7 = fd2.a(n2 - wj.b[n6], n3 - wj.c[n6], n4 - wj.d[n6]);
        if (n7 != uu.aa.bn && n7 != uu.W.bn) {
            fd2.f(n2, n3, n4, 0);
        } else {
            uu.m[n7].b(fd2, n2 - wj.b[n6], n3 - wj.c[n6], n4 - wj.d[n6], n5);
        }
    }

    public static int c(int n2) {
        return n2 & 7;
    }
}

