/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ys
extends uu {
    public static final String[] a = new String[]{"stone", "sand", "wood", "cobble"};
    private boolean b;

    public ys(int n2, boolean bl2) {
        super(n2, 6, ln.e);
        this.b = bl2;
        if (!bl2) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
        }
        this.g(255);
    }

    public int a(int n2, int n3) {
        if (n3 == 0) {
            if (n2 <= 1) {
                return 6;
            }
            return 5;
        }
        if (n3 == 1) {
            if (n2 == 0) {
                return 208;
            }
            if (n2 == 1) {
                return 176;
            }
            return 192;
        }
        if (n3 == 2) {
            return 4;
        }
        if (n3 == 3) {
            return 16;
        }
        return 6;
    }

    public int a(int n2) {
        return this.a(n2, 0);
    }

    public boolean c() {
        return this.b;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        int n5;
        if (this != uu.al) {
            super.c(fd2, n2, n3, n4);
        }
        int n6 = fd2.a(n2, n3 - 1, n4);
        int n7 = fd2.e(n2, n3, n4);
        if (n7 != (n5 = fd2.e(n2, n3 - 1, n4))) {
            return;
        }
        if (n6 == ys.al.bn) {
            fd2.f(n2, n3, n4, 0);
            fd2.b(n2, n3 - 1, n4, uu.ak.bn, n7);
        }
    }

    public int a(int n2, Random random) {
        return uu.al.bn;
    }

    public int a(Random random) {
        if (this.b) {
            return 2;
        }
        return 1;
    }

    protected int b_(int n2) {
        return n2;
    }

    public boolean d() {
        return this.b;
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        if (this != uu.al) {
            super.b(xp2, n2, n3, n4, n5);
        }
        if (n5 == 1) {
            return true;
        }
        if (!super.b(xp2, n2, n3, n4, n5)) {
            return false;
        }
        if (n5 == 0) {
            return true;
        }
        return xp2.a(n2, n3, n4) != this.bn;
    }
}

