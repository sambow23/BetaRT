/*
 * Decompiled with CFR 0.152.
 */
public class tr
extends gm {
    public final String a;

    protected tr(int n2, String string) {
        super(n2);
        this.a = string;
        this.bg = 1;
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (fd2.a(n2, n3, n4) == uu.aZ.bn && fd2.e(n2, n3, n4) == 0) {
            if (fd2.B) {
                return true;
            }
            ((fo)uu.aZ).f(fd2, n2, n3, n4, this.bf);
            fd2.a(null, 1005, n2, n3, n4, this.bf);
            --iz2.a;
            return true;
        }
        return false;
    }
}

