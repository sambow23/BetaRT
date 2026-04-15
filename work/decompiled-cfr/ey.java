/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashMap;
import java.util.Map;

public class ey {
    private static final ey a = new ey();
    private Map b = new HashMap();

    public static final ey a() {
        return a;
    }

    private ey() {
        this.a(uu.I.bn, new iz(gm.m));
        this.a(uu.H.bn, new iz(gm.n));
        this.a(uu.ax.bn, new iz(gm.l));
        this.a(uu.F.bn, new iz(uu.N));
        this.a(gm.ao.bf, new iz(gm.ap));
        this.a(gm.aS.bf, new iz(gm.aT));
        this.a(uu.x.bn, new iz(uu.u));
        this.a(gm.aG.bf, new iz(gm.aF));
        this.a(uu.aW.bn, new iz(gm.aU, 1, 2));
        this.a(uu.K.bn, new iz(gm.k, 1, 1));
    }

    public void a(int n2, iz iz2) {
        this.b.put(n2, iz2);
    }

    public iz a(int n2) {
        return (iz)this.b.get(n2);
    }

    public Map b() {
        return this.b;
    }
}

