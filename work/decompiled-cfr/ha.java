/*
 * Decompiled with CFR 0.152.
 */
public class ha {
    private String[][] a = new String[][]{{"XXX", " # ", " # "}, {"X", "#", "#"}, {"XX", "X#", " #"}, {"XX", " #", " #"}};
    private Object[][] b = new Object[][]{{uu.y, uu.x, gm.m, gm.l, gm.n}, {gm.r, gm.v, gm.e, gm.z, gm.G}, {gm.q, gm.u, gm.d, gm.y, gm.F}, {gm.s, gm.w, gm.f, gm.A, gm.H}, {gm.L, gm.M, gm.N, gm.O, gm.P}};

    public void a(hk hk2) {
        for (int i2 = 0; i2 < this.b[0].length; ++i2) {
            Object object = this.b[0][i2];
            for (int i3 = 0; i3 < this.b.length - 1; ++i3) {
                gm gm2 = (gm)this.b[i3 + 1][i2];
                hk2.a(new iz(gm2), this.a[i3], Character.valueOf('#'), gm.B, Character.valueOf('X'), object);
            }
        }
        hk2.a(new iz(gm.bc), " #", "# ", Character.valueOf('#'), gm.m);
    }
}

