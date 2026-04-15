/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

public class ep {
    public static int a;
    public static int b;
    public static int c;
    public static int d;
    public static List e;
    public static ny f;
    public static ny g;
    public static ny h;
    public static ny i;
    public static ny j;
    public static ny k;
    public static ny l;
    public static ny m;
    public static ny n;
    public static ny o;
    public static ny p;
    public static ny q;
    public static ny r;
    public static ny s;
    public static ny t;
    public static ny u;

    public static void a() {
    }

    static {
        e = new ArrayList();
        f = new ny(0, "openInventory", 0, 0, gm.aJ, null).a().c();
        g = new ny(1, "mineWood", 2, 1, uu.K, f).c();
        h = new ny(2, "buildWorkBench", 4, -1, uu.az, g).c();
        i = new ny(3, "buildPickaxe", 4, 2, gm.r, h).c();
        j = new ny(4, "buildFurnace", 3, 4, uu.aD, i).c();
        k = new ny(5, "acquireIron", 1, 4, gm.m, j).c();
        l = new ny(6, "buildHoe", 2, -3, gm.L, h).c();
        m = new ny(7, "makeBread", -1, -3, gm.S, l).c();
        n = new ny(8, "bakeCake", 0, -5, gm.aX, l).c();
        o = new ny(9, "buildBetterPickaxe", 6, 2, gm.v, i).c();
        p = new ny(10, "cookFish", 2, 6, gm.aT, j).c();
        q = new ny(11, "onARail", 2, 3, uu.aH, k).b().c();
        r = new ny(12, "buildSword", 6, -1, gm.p, h).c();
        s = new ny(13, "killEnemy", 8, -1, gm.aV, r).c();
        t = new ny(14, "killCow", 7, -3, gm.aD, r).c();
        u = new ny(15, "flyPig", 8, -4, gm.ay, t).b().c();
        System.out.println(e.size() + " achievements");
    }
}

