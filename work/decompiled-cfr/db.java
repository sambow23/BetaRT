/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class db
extends vm {
    private boolean a = false;
    private static List b = new ArrayList();

    public int a(int n2, int n3) {
        if (n2 == 1) {
            return uu.aw.a(n2, n3);
        }
        return super.a(n2, n3);
    }

    private boolean a(fd fd2, int n2, int n3, int n4, boolean bl2) {
        if (bl2) {
            b.add(new xu(n2, n3, n4, fd2.t()));
        }
        int n5 = 0;
        for (int i2 = 0; i2 < b.size(); ++i2) {
            xu xu2 = (xu)b.get(i2);
            if (xu2.a != n2 || xu2.b != n3 || xu2.c != n4 || ++n5 < 8) continue;
            return true;
        }
        return false;
    }

    protected db(int n2, int n3, boolean bl2) {
        super(n2, n3);
        this.a = bl2;
        this.b(true);
    }

    public int e() {
        return 2;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        if (fd2.e(n2, n3, n4) == 0) {
            super.c(fd2, n2, n3, n4);
        }
        if (this.a) {
            fd2.i(n2, n3 - 1, n4, this.bn);
            fd2.i(n2, n3 + 1, n4, this.bn);
            fd2.i(n2 - 1, n3, n4, this.bn);
            fd2.i(n2 + 1, n3, n4, this.bn);
            fd2.i(n2, n3, n4 - 1, this.bn);
            fd2.i(n2, n3, n4 + 1, this.bn);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        if (this.a) {
            fd2.i(n2, n3 - 1, n4, this.bn);
            fd2.i(n2, n3 + 1, n4, this.bn);
            fd2.i(n2 - 1, n3, n4, this.bn);
            fd2.i(n2 + 1, n3, n4, this.bn);
            fd2.i(n2, n3, n4 - 1, this.bn);
            fd2.i(n2, n3, n4 + 1, this.bn);
        }
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        if (!this.a) {
            return false;
        }
        int n6 = xp2.e(n2, n3, n4);
        if (n6 == 5 && n5 == 1) {
            return false;
        }
        if (n6 == 3 && n5 == 3) {
            return false;
        }
        if (n6 == 4 && n5 == 2) {
            return false;
        }
        if (n6 == 1 && n5 == 5) {
            return false;
        }
        return n6 != 2 || n5 != 4;
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        if (n5 == 5 && fd2.k(n2, n3 - 1, n4, 0)) {
            return true;
        }
        if (n5 == 3 && fd2.k(n2, n3, n4 - 1, 2)) {
            return true;
        }
        if (n5 == 4 && fd2.k(n2, n3, n4 + 1, 3)) {
            return true;
        }
        if (n5 == 1 && fd2.k(n2 - 1, n3, n4, 4)) {
            return true;
        }
        return n5 == 2 && fd2.k(n2 + 1, n3, n4, 5);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        boolean bl2 = this.h(fd2, n2, n3, n4);
        while (b.size() > 0 && fd2.t() - ((xu)db.b.get((int)0)).d > 100L) {
            b.remove(0);
        }
        if (this.a) {
            if (bl2) {
                fd2.b(n2, n3, n4, uu.aQ.bn, fd2.e(n2, n3, n4));
                if (this.a(fd2, n2, n3, n4, true)) {
                    fd2.a((float)n2 + 0.5f, (double)((float)n3 + 0.5f), (double)((float)n4 + 0.5f), "random.fizz", 0.5f, 2.6f + (fd2.r.nextFloat() - fd2.r.nextFloat()) * 0.8f);
                    for (int i2 = 0; i2 < 5; ++i2) {
                        double d2 = (double)n2 + random.nextDouble() * 0.6 + 0.2;
                        double d3 = (double)n3 + random.nextDouble() * 0.6 + 0.2;
                        double d4 = (double)n4 + random.nextDouble() * 0.6 + 0.2;
                        fd2.a("smoke", d2, d3, d4, 0.0, 0.0, 0.0);
                    }
                }
            }
        } else if (!bl2 && !this.a(fd2, n2, n3, n4, false)) {
            fd2.b(n2, n3, n4, uu.aR.bn, fd2.e(n2, n3, n4));
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        super.b(fd2, n2, n3, n4, n5);
        fd2.c(n2, n3, n4, this.bn, this.e());
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 == 0) {
            return this.c((xp)fd2, n2, n3, n4, n5);
        }
        return false;
    }

    public int a(int n2, Random random) {
        return uu.aR.bn;
    }

    public boolean f() {
        return true;
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        if (!this.a) {
            return;
        }
        int n5 = fd2.e(n2, n3, n4);
        double d2 = (double)((float)n2 + 0.5f) + (double)(random.nextFloat() - 0.5f) * 0.2;
        double d3 = (double)((float)n3 + 0.7f) + (double)(random.nextFloat() - 0.5f) * 0.2;
        double d4 = (double)((float)n4 + 0.5f) + (double)(random.nextFloat() - 0.5f) * 0.2;
        double d5 = 0.22f;
        double d6 = 0.27f;
        if (n5 == 1) {
            fd2.a("reddust", d2 - d6, d3 + d5, d4, 0.0, 0.0, 0.0);
        } else if (n5 == 2) {
            fd2.a("reddust", d2 + d6, d3 + d5, d4, 0.0, 0.0, 0.0);
        } else if (n5 == 3) {
            fd2.a("reddust", d2, d3 + d5, d4 - d6, 0.0, 0.0, 0.0);
        } else if (n5 == 4) {
            fd2.a("reddust", d2, d3 + d5, d4 + d6, 0.0, 0.0, 0.0);
        } else {
            fd2.a("reddust", d2, d3, d4, 0.0, 0.0, 0.0);
        }
    }
}

