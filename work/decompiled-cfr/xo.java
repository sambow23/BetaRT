/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class xo
extends pg {
    private int a;

    public xo(int n2) {
        this.a = n2;
    }

    public boolean a(fd fd2, Random random, int n2, int n3, int n4) {
        if (fd2.a(n2, n3 + 1, n4) != uu.u.bn) {
            return false;
        }
        if (fd2.a(n2, n3 - 1, n4) != uu.u.bn) {
            return false;
        }
        if (fd2.a(n2, n3, n4) != 0 && fd2.a(n2, n3, n4) != uu.u.bn) {
            return false;
        }
        int n5 = 0;
        if (fd2.a(n2 - 1, n3, n4) == uu.u.bn) {
            ++n5;
        }
        if (fd2.a(n2 + 1, n3, n4) == uu.u.bn) {
            ++n5;
        }
        if (fd2.a(n2, n3, n4 - 1) == uu.u.bn) {
            ++n5;
        }
        if (fd2.a(n2, n3, n4 + 1) == uu.u.bn) {
            ++n5;
        }
        int n6 = 0;
        if (fd2.d(n2 - 1, n3, n4)) {
            ++n6;
        }
        if (fd2.d(n2 + 1, n3, n4)) {
            ++n6;
        }
        if (fd2.d(n2, n3, n4 - 1)) {
            ++n6;
        }
        if (fd2.d(n2, n3, n4 + 1)) {
            ++n6;
        }
        if (n5 == 3 && n6 == 1) {
            fd2.f(n2, n3, n4, this.a);
            fd2.a = true;
            uu.m[this.a].a(fd2, n2, n3, n4, random);
            fd2.a = false;
        }
        return true;
    }
}

