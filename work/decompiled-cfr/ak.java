/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ak
extends jp {
    public ak(int n2, int n3) {
        super(n2, n3, ln.y, false);
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        if (xp2.a(n2 - 1, n3, n4) == this.bn || xp2.a(n2 + 1, n3, n4) == this.bn) {
            float f2 = 0.5f;
            float f3 = 0.125f;
            this.a(0.5f - f2, 0.0f, 0.5f - f3, 0.5f + f2, 1.0f, 0.5f + f3);
        } else {
            float f4 = 0.125f;
            float f5 = 0.5f;
            this.a(0.5f - f4, 0.0f, 0.5f - f5, 0.5f + f4, 1.0f, 0.5f + f5);
        }
    }

    public boolean c() {
        return false;
    }

    public boolean d() {
        return false;
    }

    public boolean a_(fd fd2, int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7 = 0;
        int n8 = 0;
        if (fd2.a(n2 - 1, n3, n4) == uu.aq.bn || fd2.a(n2 + 1, n3, n4) == uu.aq.bn) {
            n7 = 1;
        }
        if (fd2.a(n2, n3, n4 - 1) == uu.aq.bn || fd2.a(n2, n3, n4 + 1) == uu.aq.bn) {
            n8 = 1;
        }
        if (n7 == n8) {
            return false;
        }
        if (fd2.a(n2 - n7, n3, n4 - n8) == 0) {
            n2 -= n7;
            n4 -= n8;
        }
        for (n6 = -1; n6 <= 2; ++n6) {
            for (n5 = -1; n5 <= 3; ++n5) {
                boolean bl2;
                boolean bl3 = bl2 = n6 == -1 || n6 == 2 || n5 == -1 || n5 == 3;
                if (!(n6 != -1 && n6 != 2 || n5 != -1 && n5 != 3)) continue;
                int n9 = fd2.a(n2 + n7 * n6, n3 + n5, n4 + n8 * n6);
                if (!(bl2 ? n9 != uu.aq.bn : n9 != 0 && n9 != uu.as.bn)) continue;
                return false;
            }
        }
        fd2.o = true;
        for (n6 = 0; n6 < 2; ++n6) {
            for (n5 = 0; n5 < 3; ++n5) {
                fd2.f(n2 + n7 * n6, n3 + n5, n4 + n8 * n6, uu.bf.bn);
            }
        }
        fd2.o = false;
        return true;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        boolean bl2;
        int n6;
        int n7 = 0;
        int n8 = 1;
        if (fd2.a(n2 - 1, n3, n4) == this.bn || fd2.a(n2 + 1, n3, n4) == this.bn) {
            n7 = 1;
            n8 = 0;
        }
        int n9 = n3;
        while (fd2.a(n2, n9 - 1, n4) == this.bn) {
            --n9;
        }
        if (fd2.a(n2, n9 - 1, n4) != uu.aq.bn) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
        for (n6 = 1; n6 < 4 && fd2.a(n2, n9 + n6, n4) == this.bn; ++n6) {
        }
        if (n6 != 3 || fd2.a(n2, n9 + n6, n4) != uu.aq.bn) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
        boolean bl3 = fd2.a(n2 - 1, n3, n4) == this.bn || fd2.a(n2 + 1, n3, n4) == this.bn;
        boolean bl4 = bl2 = fd2.a(n2, n3, n4 - 1) == this.bn || fd2.a(n2, n3, n4 + 1) == this.bn;
        if (bl3 && bl2) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
        if (!(fd2.a(n2 + n7, n3, n4 + n8) == uu.aq.bn && fd2.a(n2 - n7, n3, n4 - n8) == this.bn || fd2.a(n2 - n7, n3, n4 - n8) == uu.aq.bn && fd2.a(n2 + n7, n3, n4 + n8) == this.bn)) {
            fd2.f(n2, n3, n4, 0);
            return;
        }
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        boolean bl2;
        if (xp2.a(n2, n3, n4) == this.bn) {
            return false;
        }
        boolean bl3 = xp2.a(n2 - 1, n3, n4) == this.bn && xp2.a(n2 - 2, n3, n4) != this.bn;
        boolean bl4 = xp2.a(n2 + 1, n3, n4) == this.bn && xp2.a(n2 + 2, n3, n4) != this.bn;
        boolean bl5 = xp2.a(n2, n3, n4 - 1) == this.bn && xp2.a(n2, n3, n4 - 2) != this.bn;
        boolean bl6 = xp2.a(n2, n3, n4 + 1) == this.bn && xp2.a(n2, n3, n4 + 2) != this.bn;
        boolean bl7 = bl3 || bl4;
        boolean bl8 = bl2 = bl5 || bl6;
        if (bl7 && n5 == 4) {
            return true;
        }
        if (bl7 && n5 == 5) {
            return true;
        }
        if (bl2 && n5 == 2) {
            return true;
        }
        return bl2 && n5 == 3;
    }

    public int a(Random random) {
        return 0;
    }

    public int b_() {
        return 1;
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2) {
        if (sn2.aH == null && sn2.aG == null) {
            sn2.S();
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
        if (random.nextInt(100) == 0) {
            fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "portal.portal", 1.0f, random.nextFloat() * 0.4f + 0.8f);
        }
        for (int i2 = 0; i2 < 4; ++i2) {
            double d2 = (float)n2 + random.nextFloat();
            double d3 = (float)n3 + random.nextFloat();
            double d4 = (float)n4 + random.nextFloat();
            double d5 = 0.0;
            double d6 = 0.0;
            double d7 = 0.0;
            int n5 = random.nextInt(2) * 2 - 1;
            d5 = ((double)random.nextFloat() - 0.5) * 0.5;
            d6 = ((double)random.nextFloat() - 0.5) * 0.5;
            d7 = ((double)random.nextFloat() - 0.5) * 0.5;
            if (fd2.a(n2 - 1, n3, n4) == this.bn || fd2.a(n2 + 1, n3, n4) == this.bn) {
                d4 = (double)n4 + 0.5 + 0.25 * (double)n5;
                d7 = random.nextFloat() * 2.0f * (float)n5;
            } else {
                d2 = (double)n2 + 0.5 + 0.25 * (double)n5;
                d5 = random.nextFloat() * 2.0f * (float)n5;
            }
            fd2.a("portal", d2, d3, d4, d5, d6, d7);
        }
    }
}

