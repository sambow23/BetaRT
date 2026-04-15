/*
 * Decompiled with CFR 0.152.
 */
public class la
extends gm {
    public la(int n2, bu bu2) {
        super(n2);
        this.bg = 1;
        this.e(bu2.a());
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.a(n2, n3, n4);
        int n7 = fd2.a(n2, n3 + 1, n4);
        if (n5 != 0 && n7 == 0 && n6 == uu.v.bn || n6 == uu.w.bn) {
            uu uu2 = uu.aB;
            fd2.a((float)n2 + 0.5f, (double)((float)n3 + 0.5f), (double)((float)n4 + 0.5f), uu2.by.d(), (uu2.by.b() + 1.0f) / 2.0f, uu2.by.c() * 0.8f);
            if (fd2.B) {
                return true;
            }
            fd2.f(n2, n3, n4, uu2.bn);
            iz2.a(1, (sn)gs2);
            return true;
        }
        return false;
    }

    public boolean b() {
        return true;
    }
}

