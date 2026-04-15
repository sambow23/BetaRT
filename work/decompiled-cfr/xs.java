/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class xs
extends pg {
    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        for (int i2 = 0; i2 < 64; ++i2) {
            int n5;
            int n6;
            int n7 = n2 + random.nextInt(8) - random.nextInt(8);
            if (!fd2.d(n7, n6 = n3 + random.nextInt(4) - random.nextInt(4), n5 = n4 + random.nextInt(8) - random.nextInt(8)) || fd2.a(n7, n6 - 1, n5) != uu.bc.bn) continue;
            fd2.f(n7, n6, n5, uu.as.bn);
        }
        return true;
    }
}

