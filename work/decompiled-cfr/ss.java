/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.Random;

public class ss
extends uu {
    private uu a;

    protected ss(int n2, uu uu2) {
        super(n2, uu2.bm, uu2.bA);
        this.a = uu2;
        this.c(uu2.bo);
        this.b(uu2.bp / 3.0f);
        this.a(uu2.by);
        this.g(255);
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return super.e(fd2, n2, n3, n4);
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 10;
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        return super.b(xp2, n2, n3, n4, n5);
    }

    public void a(fd fd2, int n2, int n3, int n4, eq eq2, ArrayList arrayList) {
        int n5 = fd2.e(n2, n3, n4);
        if (n5 == 0) {
            this.a(0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 1.0f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
            this.a(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
        } else if (n5 == 1) {
            this.a(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
            this.a(0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
        } else if (n5 == 2) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
            this.a(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
        } else if (n5 == 3) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
            this.a(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
            super.a(fd2, n2, n3, n4, eq2, arrayList);
        }
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        this.a.b(fd2, n2, n3, n4, random);
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        this.a.b(fd2, n2, n3, n4, gs2);
    }

    public void c(fd fd2, int n2, int n3, int n4, int n5) {
        this.a.c(fd2, n2, n3, n4, n5);
    }

    public float d(xp xp2, int n2, int n3, int n4) {
        return this.a.d(xp2, n2, n3, n4);
    }

    public float a(sn sn2) {
        return this.a.a(sn2);
    }

    public int b_() {
        return this.a.b_();
    }

    public int a(int n2, Random random) {
        return this.a.a(n2, random);
    }

    public int a(Random random) {
        return this.a.a(random);
    }

    public int a(int n2, int n3) {
        return this.a.a(n2, n3);
    }

    public int a(int n2) {
        return this.a.a(n2);
    }

    public int a(xp xp2, int n2, int n3, int n4, int n5) {
        return this.a.a(xp2, n2, n3, n4, n5);
    }

    public int e() {
        return this.a.e();
    }

    public eq f(fd fd2, int n2, int n3, int n4) {
        return this.a.f(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2, bt bt2) {
        this.a.a(fd2, n2, n3, n4, sn2, bt2);
    }

    public boolean v_() {
        return this.a.v_();
    }

    public boolean a(int n2, boolean bl2) {
        return this.a.a(n2, bl2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return this.a.a(fd2, n2, n3, n4);
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        this.b(fd2, n2, n3, n4, 0);
        this.a.c(fd2, n2, n3, n4);
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        this.a.b(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, float f2) {
        this.a.a(fd2, n2, n3, n4, n5, f2);
    }

    public void b(fd fd2, int n2, int n3, int n4, sn sn2) {
        this.a.b(fd2, n2, n3, n4, sn2);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        this.a.a(fd2, n2, n3, n4, random);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        return this.a.a(fd2, n2, n3, n4, gs2);
    }

    public void d(fd fd2, int n2, int n3, int n4) {
        this.a.d(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, ls ls2) {
        int n5 = in.b((double)(ls2.aS * 4.0f / 360.0f) + 0.5) & 3;
        if (n5 == 0) {
            fd2.d(n2, n3, n4, 2);
        }
        if (n5 == 1) {
            fd2.d(n2, n3, n4, 1);
        }
        if (n5 == 2) {
            fd2.d(n2, n3, n4, 3);
        }
        if (n5 == 3) {
            fd2.d(n2, n3, n4, 0);
        }
    }
}

