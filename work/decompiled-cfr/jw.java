/*
 * Decompiled with CFR 0.152.
 */
public class jw
extends uu {
    public jw(int n2, int n3) {
        super(n2, n3, ln.d);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        if (fd2.a(n2, n3 - 1, n4) == this.bn) {
            return true;
        }
        if (!fd2.f(n2, n3 - 1, n4).a()) {
            return false;
        }
        return super.a(fd2, n2, n3, n4);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return eq.b(n2, n3, n4, n2 + 1, (float)n3 + 1.5f, n4 + 1);
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 11;
    }
}

