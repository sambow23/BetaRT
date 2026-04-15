/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ir
extends pg {
    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        for (int i2 = 0; i2 < 20; ++i2) {
            int n5;
            int n6;
            int n7 = n2 + random.nextInt(4) - random.nextInt(4);
            if (!fd2.d(n7, n6 = n3, n5 = n4 + random.nextInt(4) - random.nextInt(4)) || fd2.f(n7 - 1, n6 - 1, n5) != ln.g && fd2.f(n7 + 1, n6 - 1, n5) != ln.g && fd2.f(n7, n6 - 1, n5 - 1) != ln.g && fd2.f(n7, n6 - 1, n5 + 1) != ln.g) continue;
            int n8 = 2 + random.nextInt(random.nextInt(3) + 1);
            for (int i3 = 0; i3 < n8; ++i3) {
                if (!uu.aY.g(fd2, n7, n6 + i3, n5)) continue;
                fd2.c(n7, n6 + i3, n5, uu.aY.bn);
            }
        }
        return true;
    }
}

