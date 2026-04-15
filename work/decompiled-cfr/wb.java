/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class wb
extends uu {
    protected wb(int n2, int n3) {
        super(n2, ln.j);
        this.bm = n3;
        this.b(true);
        float f2 = 0.2f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, f2 * 3.0f, 0.5f + f2);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return super.a(fd2, n2, n3, n4) && this.d(fd2.a(n2, n3 - 1, n4));
    }

    protected boolean d(int n2) {
        return n2 == uu.v.bn || n2 == uu.w.bn || n2 == uu.aB.bn;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        super.b(fd2, n2, n3, n4, n5);
        this.h(fd2, n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        this.h(fd2, n2, n3, n4);
    }

    protected final void h(fd fd2, int n2, int n3, int n4) {
        if (!this.g(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
        }
    }

    public boolean g(fd fd2, int n2, int n3, int n4) {
        return (fd2.m(n2, n3, n4) >= 8 || fd2.l(n2, n3, n4)) && this.d(fd2.a(n2, n3 - 1, n4));
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 1;
    }
}

