/*
 * Decompiled with CFR 0.152.
 */
public class ny
extends vr {
    public final int a;
    public final int b;
    public final ny c;
    private final String l;
    private gt m;
    public final iz d;
    private boolean n;

    public ny(int n2, String string, int n3, int n4, gm gm2, ny ny2) {
        this(n2, string, n3, n4, new iz(gm2), ny2);
    }

    public ny(int n2, String string, int n3, int n4, uu uu2, ny ny2) {
        this(n2, string, n3, n4, new iz(uu2), ny2);
    }

    public ny(int n2, String string, int n3, int n4, iz iz2, ny ny2) {
        super(0x500000 + n2, do.a("achievement." + string));
        this.d = iz2;
        this.l = do.a("achievement." + string + ".desc");
        this.a = n3;
        this.b = n4;
        if (n3 < ep.a) {
            ep.a = n3;
        }
        if (n4 < ep.b) {
            ep.b = n4;
        }
        if (n3 > ep.c) {
            ep.c = n3;
        }
        if (n4 > ep.d) {
            ep.d = n4;
        }
        this.c = ny2;
    }

    public ny a() {
        this.g = true;
        return this;
    }

    public ny b() {
        this.n = true;
        return this;
    }

    public ny c() {
        super.g();
        ep.e.add(this);
        return this;
    }

    public boolean d() {
        return true;
    }

    public String e() {
        if (this.m != null) {
            return this.m.a(this.l);
        }
        return this.l;
    }

    public ny a(gt gt2) {
        this.m = gt2;
        return this;
    }

    public boolean f() {
        return this.n;
    }
}

