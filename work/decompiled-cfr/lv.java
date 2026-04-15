/*
 * Decompiled with CFR 0.152.
 */
public class lv
extends gm {
    private int a;

    public lv(int n2, uu uu2) {
        super(n2);
        this.a = uu2.bn;
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.a(n2, n3, n4) == uu.aT.bn) {
            n5 = 0;
        } else {
            if (n5 == 0) {
                --n3;
            }
            if (n5 == 1) {
                ++n3;
            }
            if (n5 == 2) {
                --n4;
            }
            if (n5 == 3) {
                ++n4;
            }
            if (n5 == 4) {
                --n2;
            }
            if (n5 == 5) {
                ++n2;
            }
        }
        if (iz2.a == 0) {
            return false;
        }
        if (fd2.a(this.a, n2, n3, n4, false, n5)) {
            uu uu2 = uu.m[this.a];
            if (fd2.f(n2, n3, n4, this.a)) {
                uu.m[this.a].e(fd2, n2, n3, n4, n5);
                uu.m[this.a].a(fd2, n2, n3, n4, (ls)gs2);
                fd2.a((float)n2 + 0.5f, (double)((float)n3 + 0.5f), (double)((float)n4 + 0.5f), uu2.by.d(), (uu2.by.b() + 1.0f) / 2.0f, uu2.by.c() * 0.8f);
                --iz2.a;
            }
        }
        return true;
    }
}

