/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class gk
extends uu {
    public static boolean a = false;

    public gk(int n2, int n3) {
        super(n2, n3, ln.n);
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        fd2.c(n2, n3, n4, this.bn, this.e());
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        fd2.c(n2, n3, n4, this.bn, this.e());
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        this.h(fd2, n2, n3, n4);
    }

    private void h(fd fd2, int n2, int n3, int n4) {
        int n5 = n2;
        int n6 = n3;
        int n7 = n4;
        if (gk.c_(fd2, n5, n6 - 1, n7) && n6 >= 0) {
            int n8 = 32;
            if (a || !fd2.a(n2 - n8, n3 - n8, n4 - n8, n2 + n8, n3 + n8, n4 + n8)) {
                fd2.f(n2, n3, n4, 0);
                while (gk.c_(fd2, n2, n3 - 1, n4) && n3 > 0) {
                    --n3;
                }
                if (n3 > 0) {
                    fd2.f(n2, n3, n4, this.bn);
                }
            } else {
                ju ju2 = new ju(fd2, (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f, this.bn);
                fd2.b(ju2);
            }
        }
    }

    public int e() {
        return 3;
    }

    public static boolean c_(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3, n4);
        if (n5 == 0) {
            return true;
        }
        if (n5 == uu.as.bn) {
            return true;
        }
        ln ln2 = uu.m[n5].bA;
        if (ln2 == ln.g) {
            return true;
        }
        return ln2 == ln.h;
    }
}

