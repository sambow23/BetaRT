/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class nx
extends rp {
    protected nx(int n2, ln ln2) {
        super(n2, ln2);
        this.b(false);
        if (ln2 == ln.h) {
            this.b(true);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        super.b(fd2, n2, n3, n4, n5);
        if (fd2.a(n2, n3, n4) == this.bn) {
            this.j(fd2, n2, n3, n4);
        }
    }

    private void j(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.e(n2, n3, n4);
        fd2.o = true;
        fd2.a(n2, n3, n4, this.bn - 1, n5);
        fd2.b(n2, n3, n4, n2, n3, n4);
        fd2.c(n2, n3, n4, this.bn - 1, this.e());
        fd2.o = false;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (this.bA == ln.h) {
            int n5 = random.nextInt(3);
            for (int i2 = 0; i2 < n5; ++i2) {
                int n6 = fd2.a(n2 += random.nextInt(3) - 1, ++n3, n4 += random.nextInt(3) - 1);
                if (n6 == 0) {
                    if (!this.k(fd2, n2 - 1, n3, n4) && !this.k(fd2, n2 + 1, n3, n4) && !this.k(fd2, n2, n3, n4 - 1) && !this.k(fd2, n2, n3, n4 + 1) && !this.k(fd2, n2, n3 - 1, n4) && !this.k(fd2, n2, n3 + 1, n4)) continue;
                    fd2.f(n2, n3, n4, uu.as.bn);
                    return;
                }
                if (!uu.m[n6].bA.c()) continue;
                return;
            }
        }
    }

    private boolean k(fd fd2, int n2, int n3, int n4) {
        return fd2.f(n2, n3, n4).e();
    }
}

