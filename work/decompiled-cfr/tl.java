/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class tl
extends wb {
    protected tl(int n2, int n3) {
        super(n2, n3);
        float f2 = 0.2f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, f2 * 2.0f, 0.5f + f2);
        this.b(true);
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        int n5;
        int n6;
        int n7;
        if (random.nextInt(100) == 0 && fd2.d(n7 = n2 + random.nextInt(3) - 1, n6 = n3 + random.nextInt(2) - random.nextInt(2), n5 = n4 + random.nextInt(3) - 1) && this.g(fd2, n7, n6, n5)) {
            n2 += random.nextInt(3) - 1;
            n4 += random.nextInt(3) - 1;
            if (fd2.d(n7, n6, n5) && this.g(fd2, n7, n6, n5)) {
                fd2.f(n7, n6, n5, this.bn);
            }
        }
    }

    protected boolean d(int n2) {
        return uu.o[n2];
    }

    public boolean g(fd fd2, int n2, int n3, int n4) {
        if (n3 < 0 || n3 >= 128) {
            return false;
        }
        return fd2.m(n2, n3, n4) < 13 && this.d(fd2.a(n2, n3 - 1, n4));
    }
}

