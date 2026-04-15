/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class vm
extends uu {
    protected vm(int n2, int n3) {
        super(n2, n3, ln.o);
        this.b(true);
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
        return 2;
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        return fd2.h(n2, n3, n4) || fd2.a(n2, n3, n4) == uu.ba.bn;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        if (fd2.h(n2 - 1, n3, n4)) {
            return true;
        }
        if (fd2.h(n2 + 1, n3, n4)) {
            return true;
        }
        if (fd2.h(n2, n3, n4 - 1)) {
            return true;
        }
        if (fd2.h(n2, n3, n4 + 1)) {
            return true;
        }
        return this.h(fd2, n2, n3 - 1, n4);
    }

    public void e(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.e(n2, n3, n4);
        if (n5 == 1 && this.h(fd2, n2, n3 - 1, n4)) {
            n6 = 5;
        }
        if (n5 == 2 && fd2.h(n2, n3, n4 + 1)) {
            n6 = 4;
        }
        if (n5 == 3 && fd2.h(n2, n3, n4 - 1)) {
            n6 = 3;
        }
        if (n5 == 4 && fd2.h(n2 + 1, n3, n4)) {
            n6 = 2;
        }
        if (n5 == 5 && fd2.h(n2 - 1, n3, n4)) {
            n6 = 1;
        }
        fd2.d(n2, n3, n4, n6);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        super.a(fd2, n2, n3, n4, random);
        if (fd2.e(n2, n3, n4) == 0) {
            this.c(fd2, n2, n3, n4);
        }
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        if (fd2.h(n2 - 1, n3, n4)) {
            fd2.d(n2, n3, n4, 1);
        } else if (fd2.h(n2 + 1, n3, n4)) {
            fd2.d(n2, n3, n4, 2);
        } else if (fd2.h(n2, n3, n4 - 1)) {
            fd2.d(n2, n3, n4, 3);
        } else if (fd2.h(n2, n3, n4 + 1)) {
            fd2.d(n2, n3, n4, 4);
        } else if (this.h(fd2, n2, n3 - 1, n4)) {
            fd2.d(n2, n3, n4, 5);
        }
        this.i(fd2, n2, n3, n4);
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (this.i(fd2, n2, n3, n4)) {
            int n6 = fd2.e(n2, n3, n4);
            boolean bl2 = false;
            if (!fd2.h(n2 - 1, n3, n4) && n6 == 1) {
                bl2 = true;
            }
            if (!fd2.h(n2 + 1, n3, n4) && n6 == 2) {
                bl2 = true;
            }
            if (!fd2.h(n2, n3, n4 - 1) && n6 == 3) {
                bl2 = true;
            }
            if (!fd2.h(n2, n3, n4 + 1) && n6 == 4) {
                bl2 = true;
            }
            if (!this.h(fd2, n2, n3 - 1, n4) && n6 == 5) {
                bl2 = true;
            }
            if (bl2) {
                this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
                fd2.f(n2, n3, n4, 0);
            }
        }
    }

    private boolean i(fd fd2, int n2, int n3, int n4) {
        if (!this.a(fd2, n2, n3, n4)) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
            return false;
        }
        return true;
    }

    public vf a(fd fd2, int n2, int n3, int n4, bt bt2, bt bt3) {
        int n5 = fd2.e(n2, n3, n4) & 7;
        float f2 = 0.15f;
        if (n5 == 1) {
            this.a(0.0f, 0.2f, 0.5f - f2, f2 * 2.0f, 0.8f, 0.5f + f2);
        } else if (n5 == 2) {
            this.a(1.0f - f2 * 2.0f, 0.2f, 0.5f - f2, 1.0f, 0.8f, 0.5f + f2);
        } else if (n5 == 3) {
            this.a(0.5f - f2, 0.2f, 0.0f, 0.5f + f2, 0.8f, f2 * 2.0f);
        } else if (n5 == 4) {
            this.a(0.5f - f2, 0.2f, 1.0f - f2 * 2.0f, 0.5f + f2, 0.8f, 1.0f);
        } else {
            f2 = 0.1f;
            this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, 0.6f, 0.5f + f2);
        }
        return super.a(fd2, n2, n3, n4, bt2, bt3);
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        int n5 = fd2.e(n2, n3, n4);
        double d2 = (float)n2 + 0.5f;
        double d3 = (float)n3 + 0.7f;
        double d4 = (float)n4 + 0.5f;
        double d5 = 0.22f;
        double d6 = 0.27f;
        if (n5 == 1) {
            fd2.a("smoke", d2 - d6, d3 + d5, d4, 0.0, 0.0, 0.0);
            fd2.a("flame", d2 - d6, d3 + d5, d4, 0.0, 0.0, 0.0);
        } else if (n5 == 2) {
            fd2.a("smoke", d2 + d6, d3 + d5, d4, 0.0, 0.0, 0.0);
            fd2.a("flame", d2 + d6, d3 + d5, d4, 0.0, 0.0, 0.0);
        } else if (n5 == 3) {
            fd2.a("smoke", d2, d3 + d5, d4 - d6, 0.0, 0.0, 0.0);
            fd2.a("flame", d2, d3 + d5, d4 - d6, 0.0, 0.0, 0.0);
        } else if (n5 == 4) {
            fd2.a("smoke", d2, d3 + d5, d4 + d6, 0.0, 0.0, 0.0);
            fd2.a("flame", d2, d3 + d5, d4 + d6, 0.0, 0.0, 0.0);
        } else {
            fd2.a("smoke", d2, d3, d4, 0.0, 0.0, 0.0);
            fd2.a("flame", d2, d3, d4, 0.0, 0.0, 0.0);
        }
    }
}

