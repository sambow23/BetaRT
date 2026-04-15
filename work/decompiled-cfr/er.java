/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class er
extends pg {
    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7;
        int n8 = 3;
        int n9 = random.nextInt(2) + 2;
        int n10 = random.nextInt(2) + 2;
        int n11 = 0;
        for (n7 = n2 - n9 - 1; n7 <= n2 + n9 + 1; ++n7) {
            for (n6 = n3 - 1; n6 <= n3 + n8 + 1; ++n6) {
                for (n5 = n4 - n10 - 1; n5 <= n4 + n10 + 1; ++n5) {
                    ln ln2 = fd2.f(n7, n6, n5);
                    if (n6 == n3 - 1 && !ln2.a()) {
                        return false;
                    }
                    if (n6 == n3 + n8 + 1 && !ln2.a()) {
                        return false;
                    }
                    if (n7 != n2 - n9 - 1 && n7 != n2 + n9 + 1 && n5 != n4 - n10 - 1 && n5 != n4 + n10 + 1 || n6 != n3 || !fd2.d(n7, n6, n5) || !fd2.d(n7, n6 + 1, n5)) continue;
                    ++n11;
                }
            }
        }
        if (n11 < 1 || n11 > 5) {
            return false;
        }
        for (n7 = n2 - n9 - 1; n7 <= n2 + n9 + 1; ++n7) {
            for (n6 = n3 + n8; n6 >= n3 - 1; --n6) {
                for (n5 = n4 - n10 - 1; n5 <= n4 + n10 + 1; ++n5) {
                    if (n7 == n2 - n9 - 1 || n6 == n3 - 1 || n5 == n4 - n10 - 1 || n7 == n2 + n9 + 1 || n6 == n3 + n8 + 1 || n5 == n4 + n10 + 1) {
                        if (n6 >= 0 && !fd2.f(n7, n6 - 1, n5).a()) {
                            fd2.f(n7, n6, n5, 0);
                            continue;
                        }
                        if (!fd2.f(n7, n6, n5).a()) continue;
                        if (n6 == n3 - 1 && random.nextInt(4) != 0) {
                            fd2.f(n7, n6, n5, uu.ap.bn);
                            continue;
                        }
                        fd2.f(n7, n6, n5, uu.x.bn);
                        continue;
                    }
                    fd2.f(n7, n6, n5, 0);
                }
            }
        }
        block6: for (n7 = 0; n7 < 2; ++n7) {
            for (n6 = 0; n6 < 3; ++n6) {
                int n12;
                int n13;
                n5 = n2 + random.nextInt(n9 * 2 + 1) - n9;
                if (!fd2.d(n5, n13 = n3, n12 = n4 + random.nextInt(n10 * 2 + 1) - n10)) continue;
                int n14 = 0;
                if (fd2.f(n5 - 1, n13, n12).a()) {
                    ++n14;
                }
                if (fd2.f(n5 + 1, n13, n12).a()) {
                    ++n14;
                }
                if (fd2.f(n5, n13, n12 - 1).a()) {
                    ++n14;
                }
                if (fd2.f(n5, n13, n12 + 1).a()) {
                    ++n14;
                }
                if (n14 != 1) continue;
                fd2.f(n5, n13, n12, uu.av.bn);
                js js2 = (js)fd2.b(n5, n13, n12);
                for (int i2 = 0; i2 < 8; ++i2) {
                    iz iz2 = this.a(random);
                    if (iz2 == null) continue;
                    js2.a(random.nextInt(js2.a()), iz2);
                }
                continue block6;
            }
        }
        fd2.f(n2, n3, n4, uu.at.bn);
        cy cy2 = (cy)fd2.b(n2, n3, n4);
        cy2.a(this.b(random));
        return true;
    }

    private iz a(Random random) {
        int n2 = random.nextInt(11);
        if (n2 == 0) {
            return new iz(gm.ay);
        }
        if (n2 == 1) {
            return new iz(gm.m, random.nextInt(4) + 1);
        }
        if (n2 == 2) {
            return new iz(gm.S);
        }
        if (n2 == 3) {
            return new iz(gm.R, random.nextInt(4) + 1);
        }
        if (n2 == 4) {
            return new iz(gm.K, random.nextInt(4) + 1);
        }
        if (n2 == 5) {
            return new iz(gm.I, random.nextInt(4) + 1);
        }
        if (n2 == 6) {
            return new iz(gm.au);
        }
        if (n2 == 7 && random.nextInt(100) == 0) {
            return new iz(gm.ar);
        }
        if (n2 == 8 && random.nextInt(2) == 0) {
            return new iz(gm.aA, random.nextInt(4) + 1);
        }
        if (n2 == 9 && random.nextInt(10) == 0) {
            return new iz(gm.c[gm.bd.bf + random.nextInt(2)]);
        }
        if (n2 == 10) {
            return new iz(gm.aU, 1, 3);
        }
        return null;
    }

    private String b(Random random) {
        int n2 = random.nextInt(4);
        if (n2 == 0) {
            return "Skeleton";
        }
        if (n2 == 1) {
            return "Zombie";
        }
        if (n2 == 2) {
            return "Zombie";
        }
        if (n2 == 3) {
            return "Spider";
        }
        return "";
    }
}

