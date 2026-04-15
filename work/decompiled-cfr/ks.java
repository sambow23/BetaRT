/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ks
extends uu {
    protected ks(int n2) {
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
        int n10 = 3;
        if (uu.o[n6] && !uu.o[n7]) {
            n10 = 3;
        }
        if (uu.o[n7] && !uu.o[n6]) {
            n10 = 2;
        }
        if (uu.o[n8] && !uu.o[n9]) {
            n10 = 5;
        }
        if (uu.o[n9] && !uu.o[n8]) {
            n10 = 4;
        }
        return n5 == n10 ? this.bm + 1 : this.bm;
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
        return true;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        fd2.f(n2, n3, n4, 0);
    }
}

