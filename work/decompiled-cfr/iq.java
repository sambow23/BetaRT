/*
 * Decompiled with CFR 0.152.
 */
public enum iq {
    a("Kebab", 16, 16, 0, 0),
    b("Aztec", 16, 16, 16, 0),
    c("Alban", 16, 16, 32, 0),
    d("Aztec2", 16, 16, 48, 0),
    e("Bomb", 16, 16, 64, 0),
    f("Plant", 16, 16, 80, 0),
    g("Wasteland", 16, 16, 96, 0),
    h("Pool", 32, 16, 0, 32),
    i("Courbet", 32, 16, 32, 32),
    j("Sea", 32, 16, 64, 32),
    k("Sunset", 32, 16, 96, 32),
    l("Creebet", 32, 16, 128, 32),
    m("Wanderer", 16, 32, 0, 64),
    n("Graham", 16, 32, 16, 64),
    o("Match", 32, 32, 0, 128),
    p("Bust", 32, 32, 32, 128),
    q("Stage", 32, 32, 64, 128),
    r("Void", 32, 32, 96, 128),
    s("SkullAndRoses", 32, 32, 128, 128),
    t("Fighters", 64, 32, 0, 96),
    u("Pointer", 64, 64, 0, 192),
    v("Pigscene", 64, 64, 64, 192),
    w("BurningSkull", 64, 64, 128, 192),
    x("Skeleton", 64, 48, 192, 64),
    y("DonkeyKong", 64, 48, 192, 112);

    public static final int z;
    public final String A;
    public final int B;
    public final int C;
    public final int D;
    public final int E;

    /*
     * WARNING - Possible parameter corruption
     * WARNING - void declaration
     */
    private iq(int n2, int n3, int n4) {
        void var7_5;
        void var6_4;
        this.A = (String)n2;
        this.B = n3;
        this.C = n4;
        this.D = var6_4;
        this.E = var7_5;
    }

    static {
        z = "SkullAndRoses".length();
    }
}

