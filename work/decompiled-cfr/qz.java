/*
 * Decompiled with CFR 0.152.
 */
public class qz
extends gm {
    public qz(int n2) {
        super(n2);
        this.bg = 1;
    }

    public iz a(iz iz2, fd fd2, gs gs2) {
        if (gs2.c.c(gm.j.bf)) {
            fd2.a((sn)gs2, "random.bow", 1.0f, 1.0f / (b.nextFloat() * 0.4f + 0.8f));
            if (!fd2.B) {
                fd2.b(new sl(fd2, gs2));
            }
        }
        return iz2;
    }
}

