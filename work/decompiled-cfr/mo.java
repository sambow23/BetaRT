/*
 * Decompiled with CFR 0.152.
 */
public class mo
extends gm {
    public mo(int n2) {
        super(n2);
        this.bg = 16;
    }

    public iz a(iz iz2, fd fd2, gs gs2) {
        --iz2.a;
        fd2.a((sn)gs2, "random.bow", 0.5f, 0.4f / (b.nextFloat() * 0.4f + 0.8f));
        if (!fd2.B) {
            fd2.b(new vv(fd2, gs2));
        }
        return iz2;
    }
}

