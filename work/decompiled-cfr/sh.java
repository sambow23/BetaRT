/*
 * Decompiled with CFR 0.152.
 */
public class sh
extends dw {
    private az a;

    public sh(lw lw2, az az2) {
        int n2;
        int n3;
        this.a = az2;
        for (n3 = 0; n3 < 3; ++n3) {
            for (n2 = 0; n2 < 3; ++n2) {
                this.a(new gp(az2, n2 + n3 * 3, 62 + n2 * 18, 17 + n3 * 18));
            }
        }
        for (n3 = 0; n3 < 3; ++n3) {
            for (n2 = 0; n2 < 9; ++n2) {
                this.a(new gp(lw2, n2 + n3 * 9 + 9, 8 + n2 * 18, 84 + n3 * 18));
            }
        }
        for (n3 = 0; n3 < 9; ++n3) {
            this.a(new gp(lw2, n3, 8 + n3 * 18, 142));
        }
    }

    public boolean b(gs gs2) {
        return this.a.a_(gs2);
    }
}

