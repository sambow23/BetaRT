/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class be
extends pg {
    private int a;

    public be(int n2) {
        this.a = n2;
    }

    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        for (int i2 = 0; i2 < 64; ++i2) {
            int n5;
            int n6;
            int n7 = n2 + random.nextInt(8) - random.nextInt(8);
            if (!fd2.d(n7, n6 = n3 + random.nextInt(4) - random.nextInt(4), n5 = n4 + random.nextInt(8) - random.nextInt(8)) || !((wb)uu.m[this.a]).g(fd2, n7, n6, n5)) continue;
            fd2.c(n7, n6, n5, this.a);
        }
        return true;
    }
}

