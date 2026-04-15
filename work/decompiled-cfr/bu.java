/*
 * Decompiled with CFR 0.152.
 */
public enum bu {
    a(0, 59, 2.0f, 0),
    b(1, 131, 4.0f, 1),
    c(2, 250, 6.0f, 2),
    d(3, 1561, 8.0f, 3),
    e(0, 32, 12.0f, 0);

    private final int f;
    private final int g;
    private final float h;
    private final int i;

    /*
     * WARNING - Possible parameter corruption
     * WARNING - void declaration
     */
    private bu(float f2, int n3) {
        void var6_4;
        void var5_3;
        this.f = (int)f2;
        this.g = n3;
        this.h = var5_3;
        this.i = var6_4;
    }

    public int a() {
        return this.g;
    }

    public float b() {
        return this.h;
    }

    public int c() {
        return this.i;
    }

    public int d() {
        return this.f;
    }
}

