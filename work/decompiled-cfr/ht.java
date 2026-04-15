/*
 * Decompiled with CFR 0.152.
 */
public enum ht {
    a("options.music", true, false),
    b("options.sound", true, false),
    c("options.invertMouse", false, true),
    d("options.sensitivity", true, false),
    e("options.renderDistance", false, false),
    f("options.viewBobbing", false, true),
    g("options.anaglyph", false, true),
    h("options.advancedOpengl", false, true),
    i("options.framerateLimit", false, false),
    j("options.difficulty", false, false),
    k("options.graphics", false, false),
    l("options.ao", false, true),
    m("options.guiScale", false, false);

    private final boolean n;
    private final boolean o;
    private final String p;

    public static ht a(int n2) {
        for (ht ht2 : ht.values()) {
            if (ht2.c() != n2) continue;
            return ht2;
        }
        return null;
    }

    /*
     * WARNING - Possible parameter corruption
     * WARNING - void declaration
     */
    private ht(boolean bl2) {
        void var5_3;
        void var4_2;
        this.p = (String)bl2;
        this.n = var4_2;
        this.o = var5_3;
    }

    public boolean a() {
        return this.n;
    }

    public boolean b() {
        return this.o;
    }

    public int c() {
        return this.ordinal();
    }

    public String d() {
        return this.p;
    }
}

