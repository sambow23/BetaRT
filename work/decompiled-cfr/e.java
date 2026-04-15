/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class e
extends rw {
    private Random a = new Random();

    protected e(int n2) {
        super(n2, ln.d);
        this.bm = 26;
    }

    public int a(xp xp2, int n2, int n3, int n4, int n5) {
        if (n5 == 1) {
            return this.bm - 1;
        }
        if (n5 == 0) {
            return this.bm - 1;
        }
        int n6 = xp2.a(n2, n3, n4 - 1);
        int n7 = xp2.a(n2, n3, n4 + 1);
        int n8 = xp2.a(n2 - 1, n3, n4);
        int n9 = xp2.a(n2 + 1, n3, n4);
        if (n6 == this.bn || n7 == this.bn) {
            if (n5 == 2 || n5 == 3) {
                return this.bm;
            }
            int n10 = 0;
            if (n6 == this.bn) {
                n10 = -1;
            }
            int n11 = xp2.a(n2 - 1, n3, n6 == this.bn ? n4 - 1 : n4 + 1);
            int n12 = xp2.a(n2 + 1, n3, n6 == this.bn ? n4 - 1 : n4 + 1);
            if (n5 == 4) {
                n10 = -1 - n10;
            }
            int n13 = 5;
            if ((uu.o[n8] || uu.o[n11]) && !uu.o[n9] && !uu.o[n12]) {
                n13 = 5;
            }
            if ((uu.o[n9] || uu.o[n12]) && !uu.o[n8] && !uu.o[n11]) {
                n13 = 4;
            }
            return (n5 == n13 ? this.bm + 16 : this.bm + 32) + n10;
        }
        if (n8 == this.bn || n9 == this.bn) {
            if (n5 == 4 || n5 == 5) {
                return this.bm;
            }
            int n14 = 0;
            if (n8 == this.bn) {
                n14 = -1;
            }
            int n15 = xp2.a(n8 == this.bn ? n2 - 1 : n2 + 1, n3, n4 - 1);
            int n16 = xp2.a(n8 == this.bn ? n2 - 1 : n2 + 1, n3, n4 + 1);
            if (n5 == 3) {
                n14 = -1 - n14;
            }
            int n17 = 3;
            if ((uu.o[n6] || uu.o[n15]) && !uu.o[n7] && !uu.o[n16]) {
                n17 = 3;
            }
            if ((uu.o[n7] || uu.o[n16]) && !uu.o[n6] && !uu.o[n15]) {
                n17 = 2;
            }
            return (n5 == n17 ? this.bm + 16 : this.bm + 32) + n14;
        }
        int n18 = 3;
        if (uu.o[n6] && !uu.o[n7]) {
            n18 = 3;
        }
        if (uu.o[n7] && !uu.o[n6]) {
            n18 = 2;
        }
        if (uu.o[n8] && !uu.o[n9]) {
            n18 = 5;
        }
        if (uu.o[n9] && !uu.o[n8]) {
            n18 = 4;
        }
        return n5 == n18 ? this.bm + 1 : this.bm;
    }

    public int a(int n2) {
        if (n2 == 1) {
            return this.bm - 1;
        }
        if (n2 == 0) {
            return this.bm - 1;
        }
        if (n2 == 3) {
            return this.bm + 1;
        }
        return this.bm;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        int n5 = 0;
        if (fd2.a(n2 - 1, n3, n4) == this.bn) {
            ++n5;
        }
        if (fd2.a(n2 + 1, n3, n4) == this.bn) {
            ++n5;
        }
        if (fd2.a(n2, n3, n4 - 1) == this.bn) {
            ++n5;
        }
        if (fd2.a(n2, n3, n4 + 1) == this.bn) {
            ++n5;
        }
        if (n5 > 1) {
            return false;
        }
        if (this.h(fd2, n2 - 1, n3, n4)) {
            return false;
        }
        if (this.h(fd2, n2 + 1, n3, n4)) {
            return false;
        }
        if (this.h(fd2, n2, n3, n4 - 1)) {
            return false;
        }
        return !this.h(fd2, n2, n3, n4 + 1);
    }

    private boolean h(fd fd2, int n2, int n3, int n4) {
        if (fd2.a(n2, n3, n4) != this.bn) {
            return false;
        }
        if (fd2.a(n2 - 1, n3, n4) == this.bn) {
            return true;
        }
        if (fd2.a(n2 + 1, n3, n4) == this.bn) {
            return true;
        }
        if (fd2.a(n2, n3, n4 - 1) == this.bn) {
            return true;
        }
        return fd2.a(n2, n3, n4 + 1) == this.bn;
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        js js2 = (js)fd2.b(n2, n3, n4);
        for (int i2 = 0; i2 < js2.a(); ++i2) {
            iz iz2 = js2.f_(i2);
            if (iz2 == null) continue;
            float f2 = this.a.nextFloat() * 0.8f + 0.1f;
            float f3 = this.a.nextFloat() * 0.8f + 0.1f;
            float f4 = this.a.nextFloat() * 0.8f + 0.1f;
            while (iz2.a > 0) {
                int n5 = this.a.nextInt(21) + 10;
                if (n5 > iz2.a) {
                    n5 = iz2.a;
                }
                iz2.a -= n5;
                hl hl2 = new hl(fd2, (float)n2 + f2, (float)n3 + f3, (float)n4 + f4, new iz(iz2.c, n5, iz2.i()));
                float f5 = 0.05f;
                hl2.aP = (float)this.a.nextGaussian() * f5;
                hl2.aQ = (float)this.a.nextGaussian() * f5 + 0.2f;
                hl2.aR = (float)this.a.nextGaussian() * f5;
                fd2.b(hl2);
            }
        }
        super.b(fd2, n2, n3, n4);
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        lw lw2 = (js)fd2.b(n2, n3, n4);
        if (fd2.h(n2, n3 + 1, n4)) {
            return true;
        }
        if (fd2.a(n2 - 1, n3, n4) == this.bn && fd2.h(n2 - 1, n3 + 1, n4)) {
            return true;
        }
        if (fd2.a(n2 + 1, n3, n4) == this.bn && fd2.h(n2 + 1, n3 + 1, n4)) {
            return true;
        }
        if (fd2.a(n2, n3, n4 - 1) == this.bn && fd2.h(n2, n3 + 1, n4 - 1)) {
            return true;
        }
        if (fd2.a(n2, n3, n4 + 1) == this.bn && fd2.h(n2, n3 + 1, n4 + 1)) {
            return true;
        }
        if (fd2.a(n2 - 1, n3, n4) == this.bn) {
            lw2 = new og("Large chest", (js)fd2.b(n2 - 1, n3, n4), lw2);
        }
        if (fd2.a(n2 + 1, n3, n4) == this.bn) {
            lw2 = new og("Large chest", lw2, (js)fd2.b(n2 + 1, n3, n4));
        }
        if (fd2.a(n2, n3, n4 - 1) == this.bn) {
            lw2 = new og("Large chest", (js)fd2.b(n2, n3, n4 - 1), lw2);
        }
        if (fd2.a(n2, n3, n4 + 1) == this.bn) {
            lw2 = new og("Large chest", lw2, (js)fd2.b(n2, n3, n4 + 1));
        }
        if (fd2.B) {
            return true;
        }
        gs2.a(lw2);
        return true;
    }

    protected ow a_() {
        return new js();
    }
}

