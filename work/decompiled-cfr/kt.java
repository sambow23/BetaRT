/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class kt
extends pg {
    private int a;

    public kt(int n2) {
        this.a = n2;
    }

    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        int n5 = 0;
        while (((n5 = fd2.a(n2, n3, n4)) == 0 || n5 == uu.L.bn) && n3 > 0) {
            --n3;
        }
        for (int i2 = 0; i2 < 4; ++i2) {
            int n6;
            int n7;
            int n8 = n2 + random.nextInt(8) - random.nextInt(8);
            if (!fd2.d(n8, n7 = n3 + random.nextInt(4) - random.nextInt(4), n6 = n4 + random.nextInt(8) - random.nextInt(8)) || !((wb)uu.m[this.a]).g(fd2, n8, n7, n6)) continue;
            fd2.c(n8, n7, n6, this.a);
        }
        return true;
    }
}

