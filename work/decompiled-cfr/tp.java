/*
 * Decompiled with CFR 0.152.
 */
public class tp {
    private Object[][] a = new Object[][]{{uu.ai, new iz(gm.n, 9)}, {uu.aj, new iz(gm.m, 9)}, {uu.ay, new iz(gm.l, 9)}, {uu.P, new iz(gm.aU, 9, 4)}};

    public void a(hk hk2) {
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            uu uu2 = (uu)this.a[i2][0];
            iz iz2 = (iz)this.a[i2][1];
            hk2.a(new iz(uu2), "###", "###", "###", Character.valueOf('#'), iz2);
            hk2.a(iz2, "#", Character.valueOf('#'), uu2);
        }
    }
}

