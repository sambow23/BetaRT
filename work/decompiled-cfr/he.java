/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class he
extends wb {
    protected he(int n2, int n3) {
        super(n2, n3);
        float f2 = 0.4f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, f2 * 2.0f, 0.5f + f2);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        if (fd2.B) {
            return;
        }
        super.a(fd2, n2, n3, n4, random);
        if (fd2.n(n2, n3 + 1, n4) >= 9 && random.nextInt(30) == 0) {
            int n5 = fd2.e(n2, n3, n4);
            if ((n5 & 8) == 0) {
                fd2.d(n2, n3, n4, n5 | 8);
            } else {
                this.c(fd2, n2, n3, n4, random);
            }
        }
    }

    public int a(int n2, int n3) {
        if ((n3 &= 3) == 1) {
            return 63;
        }
        if (n3 == 2) {
            return 79;
        }
        return super.a(n2, n3);
    }

    public void c(fd fd2, int n2, int n3, int n4, Random random) {
        int n5 = fd2.e(n2, n3, n4) & 3;
        fd2.c(n2, n3, n4, 0);
        pg pg2 = null;
        if (n5 == 1) {
            pg2 = new ws();
        } else if (n5 == 2) {
            pg2 = new k();
        } else {
            pg2 = new yh();
            if (random.nextInt(10) == 0) {
                pg2 = new ih();
            }
        }
        if (!((pg)pg2).a(fd2, random, n2, n3, n4)) {
            fd2.a(n2, n3, n4, this.bn, n5);
        }
    }

    protected int b_(int n2) {
        return n2 & 3;
    }
}

