/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class pw
extends pg {
    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10 = random.nextInt(5) + 7;
        int n11 = n10 - random.nextInt(2) - 3;
        int n12 = n10 - n11;
        int n13 = 1 + random.nextInt(n12 + 1);
        boolean bl2 = true;
        if (n3 < 1 || n3 + n10 + 1 > 128) {
            return false;
        }
        for (n9 = n3; n9 <= n3 + 1 + n10 && bl2; ++n9) {
            n8 = 1;
            n8 = n9 - n3 < n11 ? 0 : n13;
            for (n7 = n2 - n8; n7 <= n2 + n8 && bl2; ++n7) {
                for (n6 = n4 - n8; n6 <= n4 + n8 && bl2; ++n6) {
                    if (n9 >= 0 && n9 < 128) {
                        n5 = fd2.a(n7, n9, n6);
                        if (n5 == 0 || n5 == uu.L.bn) continue;
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
        n9 = fd2.a(n2, n3 - 1, n4);
        if (n9 != uu.v.bn && n9 != uu.w.bn || n3 >= 128 - n10 - 1) {
            return false;
        }
        fd2.c(n2, n3 - 1, n4, uu.w.bn);
        n8 = 0;
        for (n7 = n3 + n10; n7 >= n3 + n11; --n7) {
            for (n6 = n2 - n8; n6 <= n2 + n8; ++n6) {
                n5 = n6 - n2;
                for (int i2 = n4 - n8; i2 <= n4 + n8; ++i2) {
                    int n14 = i2 - n4;
                    if (Math.abs(n5) == n8 && Math.abs(n14) == n8 && n8 > 0 || uu.o[fd2.a(n6, n7, i2)]) continue;
                    fd2.a(n6, n7, i2, uu.L.bn, 1);
                }
            }
            if (n8 >= 1 && n7 == n3 + n11 + 1) {
                --n8;
                continue;
            }
            if (n8 >= n13) continue;
            ++n8;
        }
        for (n7 = 0; n7 < n10 - 1; ++n7) {
            n6 = fd2.a(n2, n3 + n7, n4);
            if (n6 != 0 && n6 != uu.L.bn) continue;
            fd2.a(n2, n3 + n7, n4, uu.K.bn, 1);
        }
        return true;
    }
}

