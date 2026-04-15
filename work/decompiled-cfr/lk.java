/*
 * Decompiled with CFR 0.152.
 */
public enum lk {
    a(ff.class, 70, ln.a, false),
    b(bg.class, 15, ln.a, true),
    c(ar.class, 5, ln.g, true);

    private final Class d;
    private final int e;
    private final ln f;
    private final boolean g;

    /*
     * WARNING - Possible parameter corruption
     * WARNING - void declaration
     */
    private lk(ln ln2, boolean bl2) {
        void var6_4;
        void var5_3;
        this.d = ln2;
        this.e = bl2 ? 1 : 0;
        this.f = var5_3;
        this.g = var6_4;
    }

    public Class a() {
        return this.d;
    }

    public int b() {
        return this.e;
    }

    public ln c() {
        return this.f;
    }

    public boolean d() {
        return this.g;
    }
}

