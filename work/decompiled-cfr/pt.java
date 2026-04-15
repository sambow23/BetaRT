/*
 * Decompiled with CFR 0.152.
 */
public class pt
extends rw {
    public pt(int n2) {
        super(n2, 74, ln.d);
    }

    public int a(int n2) {
        return this.bm;
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        if (n5 > 0 && uu.m[n5].f()) {
            boolean bl2 = fd2.r(n2, n3, n4);
            tn tn2 = (tn)fd2.b(n2, n3, n4);
            if (tn2.b != bl2) {
                if (bl2) {
                    tn2.a(fd2, n2, n3, n4);
                }
                tn2.b = bl2;
            }
        }
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.B) {
            return true;
        }
        tn tn2 = (tn)fd2.b(n2, n3, n4);
        tn2.a();
        tn2.a(fd2, n2, n3, n4);
        return true;
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
        if (fd2.B) {
            return;
        }
        tn tn2 = (tn)fd2.b(n2, n3, n4);
        tn2.a(fd2, n2, n3, n4);
    }

    protected ow a_() {
        return new tn();
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, int n6) {
        float f2 = (float)Math.pow(2.0, (double)(n6 - 12) / 12.0);
        String string = "harp";
        if (n5 == 1) {
            string = "bd";
        }
        if (n5 == 2) {
            string = "snare";
        }
        if (n5 == 3) {
            string = "hat";
        }
        if (n5 == 4) {
            string = "bassattack";
        }
        fd2.a((double)n2 + 0.5, (double)n3 + 0.5, (double)n4 + 0.5, "note." + string, 3.0f, f2);
        fd2.a("note", (double)n2 + 0.5, (double)n3 + 1.2, (double)n4 + 0.5, (double)n6 / 24.0, 0.0, 0.0);
    }
}

