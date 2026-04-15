/*
 * Decompiled with CFR 0.152.
 */
public class cs
extends gm {
    public cs(int n2) {
        super(n2);
        this.e(64);
        this.d(1);
    }

    public boolean b() {
        return true;
    }

    public boolean c() {
        return true;
    }

    public iz a(iz iz2, fd fd2, gs gs2) {
        if (gs2.D != null) {
            int n2 = gs2.D.k();
            iz2.a(n2, (sn)gs2);
            gs2.J();
        } else {
            fd2.a((sn)gs2, "random.bow", 0.5f, 0.4f / (b.nextFloat() * 0.4f + 0.8f));
            if (!fd2.B) {
                fd2.b(new lx(fd2, gs2));
            }
            gs2.J();
        }
        return iz2;
    }
}

