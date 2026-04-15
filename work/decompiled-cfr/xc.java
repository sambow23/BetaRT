/*
 * Decompiled with CFR 0.152.
 */
public class xc {
    private String[][] a = new String[][]{{"X", "X", "#"}};
    private Object[][] b = new Object[][]{{uu.y, uu.x, gm.m, gm.l, gm.n}, {gm.p, gm.t, gm.o, gm.x, gm.E}};

    public void a(hk hk2) {
        for (int i2 = 0; i2 < this.b[0].length; ++i2) {
            Object object = this.b[0][i2];
            for (int i3 = 0; i3 < this.b.length - 1; ++i3) {
                gm gm2 = (gm)this.b[i3 + 1][i2];
                hk2.a(new iz(gm2), this.a[i3], Character.valueOf('#'), gm.B, Character.valueOf('X'), object);
            }
        }
        hk2.a(new iz(gm.i, 1), " #X", "# X", " #X", Character.valueOf('X'), gm.I, Character.valueOf('#'), gm.B);
        hk2.a(new iz(gm.j, 4), "X", "#", "Y", Character.valueOf('Y'), gm.J, Character.valueOf('X'), gm.an, Character.valueOf('#'), gm.B);
    }
}

