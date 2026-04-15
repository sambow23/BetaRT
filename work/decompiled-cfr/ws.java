/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ws
extends pg {
    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        int n11;
        int n12 = random.nextInt(4) + 6;
        int n13 = 1 + random.nextInt(2);
        int n14 = n12 - n13;
        int n15 = 2 + random.nextInt(2);
        boolean bl2 = true;
        if (n3 < 1 || n3 + n12 + 1 > 128) {
            return false;
        }
        for (n11 = n3; n11 <= n3 + 1 + n12 && bl2; ++n11) {
            n10 = 1;
            n10 = n11 - n3 < n13 ? 0 : n15;
            for (n9 = n2 - n10; n9 <= n2 + n10 && bl2; ++n9) {
                for (n8 = n4 - n10; n8 <= n4 + n10 && bl2; ++n8) {
                    if (n11 >= 0 && n11 < 128) {
                        n7 = fd2.a(n9, n11, n8);
                        if (n7 == 0 || n7 == uu.L.bn) continue;
                        bl2 = false;
                        continue;
                    }
                    bl2 = false;
                }
            }
        }
        if (!bl2) {
            return false;
        }
        n11 = fd2.a(n2, n3 - 1, n4);
        if (n11 != uu.v.bn && n11 != uu.w.bn || n3 >= 128 - n12 - 1) {
            return false;
        }
        fd2.c(n2, n3 - 1, n4, uu.w.bn);
        n10 = random.nextInt(2);
        n9 = 1;
        n8 = 0;
        for (n7 = 0; n7 <= n14; ++n7) {
            n6 = n3 + n12 - n7;
            for (n5 = n2 - n10; n5 <= n2 + n10; ++n5) {
                int n16 = n5 - n2;
                for (int i2 = n4 - n10; i2 <= n4 + n10; ++i2) {
                    int n17 = i2 - n4;
                    if (Math.abs(n16) == n10 && Math.abs(n17) == n10 && n10 > 0 || uu.o[fd2.a(n5, n6, i2)]) continue;
                    fd2.a(n5, n6, i2, uu.L.bn, 1);
                }
            }
            if (n10 >= n9) {
                n10 = n8;
                n8 = 1;
                if (++n9 <= n15) continue;
                n9 = n15;
                continue;
            }
            ++n10;
        }
        n7 = random.nextInt(3);
        for (n6 = 0; n6 < n12 - n7; ++n6) {
            n5 = fd2.a(n2, n3 + n6, n4);
            if (n5 != 0 && n5 != uu.L.bn) continue;
            fd2.a(n2, n3 + n6, n4, uu.K.bn, 1);
        }
        return true;
    }
}

