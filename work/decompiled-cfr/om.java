/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class om
extends rp {
    int a = 0;
    boolean[] b = new boolean[4];
    int[] c = new int[4];

    protected om(int n2, ln ln2) {
        super(n2, ln2);
    }

    private void j(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        fd2.a(n2, n3, n4, this.bn + 1, n5);
        fd2.b(n2, n3, n4, n2, n3, n4);
        fd2.j(n2, n3, n4);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        int n5;
        int n6 = this.h(fd2, n2, n3, n4);
        int n7 = 1;
        if (this.bA == ln.h && !fd2.t.d) {
            n7 = 2;
        }
        boolean bl2 = true;
        if (n6 > 0) {
            int n8 = -100;
            this.a = 0;
            n8 = this.f(fd2, n2 - 1, n3, n4, n8);
            n8 = this.f(fd2, n2 + 1, n3, n4, n8);
            n8 = this.f(fd2, n2, n3, n4 - 1, n8);
            n5 = (n8 = this.f(fd2, n2, n3, n4 + 1, n8)) + n7;
            if (n5 >= 8 || n8 < 0) {
                n5 = -1;
            }
            if (this.h(fd2, n2, n3 + 1, n4) >= 0) {
                int n9 = this.h(fd2, n2, n3 + 1, n4);
                n5 = n9 >= 8 ? n9 : n9 + 8;
            }
            if (this.a >= 2 && this.bA == ln.g) {
                if (fd2.f(n2, n3 - 1, n4).a()) {
                    n5 = 0;
                } else if (fd2.f(n2, n3 - 1, n4) == this.bA && fd2.e(n2, n3, n4) == 0) {
                    n5 = 0;
                }
            }
            if (this.bA == ln.h && n6 < 8 && n5 < 8 && n5 > n6 && random.nextInt(4) != 0) {
                n5 = n6;
                bl2 = false;
            }
            if (n5 != n6) {
                n6 = n5;
                if (n6 < 0) {
                    fd2.f(n2, n3, n4, 0);
                } else {
                    fd2.d(n2, n3, n4, n6);
                    fd2.c(n2, n3, n4, this.bn, this.e());
                    fd2.i(n2, n3, n4, this.bn);
                }
            } else if (bl2) {
                this.j(fd2, n2, n3, n4);
            }
        } else {
            this.j(fd2, n2, n3, n4);
        }
        if (this.m(fd2, n2, n3 - 1, n4)) {
            if (n6 >= 8) {
                fd2.b(n2, n3 - 1, n4, this.bn, n6);
            } else {
                fd2.b(n2, n3 - 1, n4, this.bn, n6 + 8);
            }
        } else if (n6 >= 0 && (n6 == 0 || this.l(fd2, n2, n3 - 1, n4))) {
            boolean[] blArray = this.k(fd2, n2, n3, n4);
            n5 = n6 + n7;
            if (n6 >= 8) {
                n5 = 1;
            }
            if (n5 >= 8) {
                return;
            }
            if (blArray[0]) {
                this.h(fd2, n2 - 1, n3, n4, n5);
            }
            if (blArray[1]) {
                this.h(fd2, n2 + 1, n3, n4, n5);
            }
            if (blArray[2]) {
                this.h(fd2, n2, n3, n4 - 1, n5);
            }
            if (blArray[3]) {
                this.h(fd2, n2, n3, n4 + 1, n5);
            }
        }
    }

    private void h(fd fd2, int n2, int n3, int n4, int n5) {
        if (this.m(fd2, n2, n3, n4)) {
            int n6 = fd2.a(n2, n3, n4);
            if (n6 > 0) {
                if (this.bA == ln.h) {
                    this.i(fd2, n2, n3, n4);
                } else {
                    uu.m[n6].g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
                }
            }
            fd2.b(n2, n3, n4, this.bn, n5);
        }
    }

    private int b(fd fd2, int n2, int n3, int n4, int n5, int n6) {
        int n7 = 1000;
        for (int i2 = 0; i2 < 4; ++i2) {
            int n8;
            if (i2 == 0 && n6 == 1 || i2 == 1 && n6 == 0 || i2 == 2 && n6 == 3 || i2 == 3 && n6 == 2) continue;
            int n9 = n2;
            int n10 = n3;
            int n11 = n4;
            if (i2 == 0) {
                --n9;
            }
            if (i2 == 1) {
                ++n9;
            }
            if (i2 == 2) {
                --n11;
            }
            if (i2 == 3) {
                ++n11;
            }
            if (this.l(fd2, n9, n10, n11) || fd2.f(n9, n10, n11) == this.bA && fd2.e(n9, n10, n11) == 0) continue;
            if (!this.l(fd2, n9, n10 - 1, n11)) {
                return n5;
            }
            if (n5 >= 4 || (n8 = this.b(fd2, n9, n10, n11, n5 + 1, i2)) >= n7) continue;
            n7 = n8;
        }
        return n7;
    }

    private boolean[] k(fd fd2, int n2, int n3, int n4) {
        int n5;
        int n6;
        for (n6 = 0; n6 < 4; ++n6) {
            this.c[n6] = 1000;
            n5 = n2;
            int n7 = n3;
            int n8 = n4;
            if (n6 == 0) {
                --n5;
            }
            if (n6 == 1) {
                ++n5;
            }
            if (n6 == 2) {
                --n8;
            }
            if (n6 == 3) {
                ++n8;
            }
            if (this.l(fd2, n5, n7, n8) || fd2.f(n5, n7, n8) == this.bA && fd2.e(n5, n7, n8) == 0) continue;
            this.c[n6] = !this.l(fd2, n5, n7 - 1, n8) ? 0 : this.b(fd2, n5, n7, n8, 1, n6);
        }
        n6 = this.c[0];
        for (n5 = 1; n5 < 4; ++n5) {
            if (this.c[n5] >= n6) continue;
            n6 = this.c[n5];
        }
        for (n5 = 0; n5 < 4; ++n5) {
            this.b[n5] = this.c[n5] == n6;
        }
        return this.b;
    }

    private boolean l(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3, n4);
        if (n5 == uu.aF.bn || n5 == uu.aM.bn || n5 == uu.aE.bn || n5 == uu.aG.bn || n5 == uu.aY.bn) {
            return true;
        }
        if (n5 == 0) {
            return false;
        }
        ln ln2 = uu.m[n5].bA;
        return ln2.c();
    }

    protected int f(fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = this.h(fd2, n2, n3, n4);
        if (n6 < 0) {
            return n5;
        }
        if (n6 == 0) {
            ++this.a;
        }
        if (n6 >= 8) {
            n6 = 0;
        }
        return n5 < 0 || n6 < n5 ? n6 : n5;
    }

    private boolean m(fd fd2, int n2, int n3, int n4) {
        ln ln2 = fd2.f(n2, n3, n4);
        if (ln2 == this.bA) {
            return false;
        }
        if (ln2 == ln.h) {
            return false;
        }
        return !this.l(fd2, n2, n3, n4);
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        super.c(fd2, n2, n3, n4);
        if (fd2.a(n2, n3, n4) == this.bn) {
            fd2.c(n2, n3, n4, this.bn, this.e());
        }
    }
}

