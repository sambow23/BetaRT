/*
 * Decompiled with CFR 0.152.
 */
public class z {
    private String[][] a = new String[][]{{"XXX", "X X"}, {"X X", "XXX", "XXX"}, {"XXX", "X X", "X X"}, {"X X", "X X"}};
    private Object[][] b = new Object[][]{{gm.aD, uu.as, gm.m, gm.l, gm.n}, {gm.T, gm.X, gm.ab, gm.af, gm.aj}, {gm.U, gm.Y, gm.ac, gm.ag, gm.ak}, {gm.V, gm.Z, gm.ad, gm.ah, gm.al}, {gm.W, gm.aa, gm.ae, gm.ai, gm.am}};

    public void a(hk hk2) {
        for (int i2 = 0; i2 < this.b[0].length; ++i2) {
            Object object = this.b[0][i2];
            for (int i3 = 0; i3 < this.b.length - 1; ++i3) {
                gm gm2 = (gm)this.b[i3 + 1][i2];
                hk2.a(new iz(gm2), this.a[i3], Character.valueOf('X'), object);
            }
        }
    }
}

