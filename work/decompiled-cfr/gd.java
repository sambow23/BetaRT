/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class gd
extends pg {
    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        if (!fd2.d(n2, n3, n4)) {
            return false;
        }
        if (fd2.a(n2, n3 + 1, n4) != uu.bc.bn) {
            return false;
        }
        fd2.f(n2, n3, n4, uu.be.bn);
        for (int i2 = 0; i2 < 1500; ++i2) {
            int n5;
            int n6;
            int n7 = n2 + random.nextInt(8) - random.nextInt(8);
            if (fd2.a(n7, n6 = n3 - random.nextInt(12), n5 = n4 + random.nextInt(8) - random.nextInt(8)) != 0) continue;
            int n8 = 0;
            for (int i3 = 0; i3 < 6; ++i3) {
                int n9 = 0;
                if (i3 == 0) {
                    n9 = fd2.a(n7 - 1, n6, n5);
                }
                if (i3 == 1) {
                    n9 = fd2.a(n7 + 1, n6, n5);
                }
                if (i3 == 2) {
                    n9 = fd2.a(n7, n6 - 1, n5);
                }
                if (i3 == 3) {
                    n9 = fd2.a(n7, n6 + 1, n5);
                }
                if (i3 == 4) {
                    n9 = fd2.a(n7, n6, n5 - 1);
                }
                if (i3 == 5) {
                    n9 = fd2.a(n7, n6, n5 + 1);
                }
                if (n9 != uu.be.bn) continue;
                ++n8;
            }
            if (n8 != true) continue;
            fd2.f(n7, n6, n5, uu.be.bn);
        }
        return true;
    }
}

