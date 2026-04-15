/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class jl {
    protected static Map a = new HashMap();
    public static List b = new ArrayList();
    public static List c = new ArrayList();
    public static List d = new ArrayList();
    public static List e = new ArrayList();
    public static vr f = new yd(1000, do.a("stat.startGame")).h().g();
    public static vr g = new yd(1001, do.a("stat.createWorld")).h().g();
    public static vr h = new yd(1002, do.a("stat.loadWorld")).h().g();
    public static vr i = new yd(1003, do.a("stat.joinMultiplayer")).h().g();
    public static vr j = new yd(1004, do.a("stat.leaveGame")).h().g();
    public static vr k = new yd(1100, do.a("stat.playOneMinute"), vr.j).h().g();
    public static vr l = new yd(2000, do.a("stat.walkOneCm"), vr.k).h().g();
    public static vr m = new yd(2001, do.a("stat.swimOneCm"), vr.k).h().g();
    public static vr n = new yd(2002, do.a("stat.fallOneCm"), vr.k).h().g();
    public static vr o = new yd(2003, do.a("stat.climbOneCm"), vr.k).h().g();
    public static vr p = new yd(2004, do.a("stat.flyOneCm"), vr.k).h().g();
    public static vr q = new yd(2005, do.a("stat.diveOneCm"), vr.k).h().g();
    public static vr r = new yd(2006, do.a("stat.minecartOneCm"), vr.k).h().g();
    public static vr s = new yd(2007, do.a("stat.boatOneCm"), vr.k).h().g();
    public static vr t = new yd(2008, do.a("stat.pigOneCm"), vr.k).h().g();
    public static vr u = new yd(2010, do.a("stat.jump")).h().g();
    public static vr v = new yd(2011, do.a("stat.drop")).h().g();
    public static vr w = new yd(2020, do.a("stat.damageDealt")).g();
    public static vr x = new yd(2021, do.a("stat.damageTaken")).g();
    public static vr y = new yd(2022, do.a("stat.deaths")).g();
    public static vr z = new yd(2023, do.a("stat.mobKills")).g();
    public static vr A = new yd(2024, do.a("stat.playerKills")).g();
    public static vr B = new yd(2025, do.a("stat.fishCaught")).g();
    public static vr[] C = jl.a("stat.mineBlock", 0x1000000);
    public static vr[] D;
    public static vr[] E;
    public static vr[] F;
    private static boolean G;
    private static boolean H;

    public static void a() {
    }

    public static void b() {
        E = jl.a(E, "stat.useItem", 0x1020000, 0, uu.m.length);
        F = jl.b(F, "stat.breakItem", 0x1030000, 0, uu.m.length);
        G = true;
        jl.d();
    }

    public static void c() {
        E = jl.a(E, "stat.useItem", 0x1020000, uu.m.length, 32000);
        F = jl.b(F, "stat.breakItem", 0x1030000, uu.m.length, 32000);
        H = true;
        jl.d();
    }

    public static void d() {
        if (!G || !H) {
            return;
        }
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (dt object : hk.a().b()) {
            hashSet.add(object.b().c);
        }
        for (iz iz2 : ey.a().b().values()) {
            hashSet.add(iz2.c);
        }
        D = new vr[32000];
        for (Integer n2 : hashSet) {
            if (gm.c[n2] == null) continue;
            String string = do.a("stat.craftItem", gm.c[n2].k());
            jl.D[n2.intValue()] = new tw(0x1010000 + n2, string, n2).g();
        }
        jl.a(D);
    }

    private static vr[] a(String string, int n2) {
        vr[] vrArray = new vr[256];
        for (int i2 = 0; i2 < 256; ++i2) {
            if (uu.m[i2] == null || !uu.m[i2].p()) continue;
            String string2 = do.a(string, uu.m[i2].n());
            vrArray[i2] = new tw(n2 + i2, string2, i2).g();
            e.add((tw)vrArray[i2]);
        }
        jl.a(vrArray);
        return vrArray;
    }

    private static vr[] a(vr[] vrArray, String string, int n2, int n3, int n4) {
        if (vrArray == null) {
            vrArray = new vr[32000];
        }
        for (int i2 = n3; i2 < n4; ++i2) {
            if (gm.c[i2] == null) continue;
            String string2 = do.a(string, gm.c[i2].k());
            vrArray[i2] = new tw(n2 + i2, string2, i2).g();
            if (i2 < uu.m.length) continue;
            d.add((tw)vrArray[i2]);
        }
        jl.a(vrArray);
        return vrArray;
    }

    private static vr[] b(vr[] vrArray, String string, int n2, int n3, int n4) {
        if (vrArray == null) {
            vrArray = new vr[32000];
        }
        for (int i2 = n3; i2 < n4; ++i2) {
            if (gm.c[i2] == null || !gm.c[i2].g()) continue;
            String string2 = do.a(string, gm.c[i2].k());
            vrArray[i2] = new tw(n2 + i2, string2, i2).g();
        }
        jl.a(vrArray);
        return vrArray;
    }

    private static void a(vr[] vrArray) {
        jl.a(vrArray, uu.C.bn, uu.B.bn);
        jl.a(vrArray, uu.E.bn, uu.E.bn);
        jl.a(vrArray, uu.bg.bn, uu.bb.bn);
        jl.a(vrArray, uu.aD.bn, uu.aC.bn);
        jl.a(vrArray, uu.aP.bn, uu.aO.bn);
        jl.a(vrArray, uu.bj.bn, uu.bi.bn);
        jl.a(vrArray, uu.aR.bn, uu.aQ.bn);
        jl.a(vrArray, uu.ah.bn, uu.ag.bn);
        jl.a(vrArray, uu.ak.bn, uu.al.bn);
        jl.a(vrArray, uu.v.bn, uu.w.bn);
        jl.a(vrArray, uu.aB.bn, uu.w.bn);
    }

    private static void a(vr[] vrArray, int n2, int n3) {
        if (vrArray[n2] != null && vrArray[n3] == null) {
            vrArray[n3] = vrArray[n2];
            return;
        }
        b.remove(vrArray[n2]);
        e.remove(vrArray[n2]);
        c.remove(vrArray[n2]);
        vrArray[n2] = vrArray[n3];
    }

    public static vr a(int n2) {
        return (vr)a.get(n2);
    }

    static {
        ep.a();
        G = false;
        H = false;
    }
}

