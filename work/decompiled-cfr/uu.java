/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.Random;

public class uu {
    public static final ct d = new ct("stone", 1.0f, 1.0f);
    public static final ct e = new ct("wood", 1.0f, 1.0f);
    public static final ct f = new ct("gravel", 1.0f, 1.0f);
    public static final ct g = new ct("grass", 1.0f, 1.0f);
    public static final ct h = new ct("stone", 1.0f, 1.0f);
    public static final ct i = new ct("stone", 1.0f, 1.5f);
    public static final ct j = new al("stone", 1.0f, 1.0f);
    public static final ct k = new ct("cloth", 1.0f, 1.0f);
    public static final ct l = new aj("sand", 1.0f, 1.0f);
    public static final uu[] m = new uu[256];
    public static final boolean[] n = new boolean[256];
    public static final boolean[] o = new boolean[256];
    public static final boolean[] p = new boolean[256];
    public static final int[] q = new int[256];
    public static final boolean[] r = new boolean[256];
    public static final int[] s = new int[256];
    public static final boolean[] t = new boolean[256];
    public static final uu u = new eo(1, 1).c(1.5f).b(10.0f).a(h).a("stone");
    public static final wp v = (wp)new wp(2).c(0.6f).a(g).a("grass");
    public static final uu w = new ot(3, 2).c(0.5f).a(f).a("dirt");
    public static final uu x = new uu(4, 16, ln.e).c(2.0f).b(10.0f).a(h).a("stonebrick");
    public static final uu y = new uu(5, 4, ln.d).c(2.0f).b(5.0f).a(e).a("wood").j();
    public static final uu z = new he(6, 15).c(0.0f).a(g).a("sapling").j();
    public static final uu A = new uu(7, 17, ln.e).l().b(6000000.0f).a(h).a("bedrock").q();
    public static final uu B = new om(8, ln.g).c(100.0f).g(3).a("water").q().j();
    public static final uu C = new nx(9, ln.g).c(100.0f).g(3).a("water").q().j();
    public static final uu D = new om(10, ln.h).c(0.0f).a(1.0f).g(255).a("lava").q().j();
    public static final uu E = new nx(11, ln.h).c(100.0f).a(1.0f).g(255).a("lava").q().j();
    public static final uu F = new gk(12, 18).c(0.5f).a(l).a("sand");
    public static final uu G = new ne(13, 19).c(0.6f).a(f).a("gravel");
    public static final uu H = new mt(14, 32).c(3.0f).b(5.0f).a(h).a("oreGold");
    public static final uu I = new mt(15, 33).c(3.0f).b(5.0f).a(h).a("oreIron");
    public static final uu J = new mt(16, 34).c(3.0f).b(5.0f).a(h).a("oreCoal");
    public static final uu K = new vg(17).c(2.0f).a(e).a("log").j();
    public static final bk L = (bk)new bk(18, 52).c(0.2f).g(1).a(g).a("leaves").q().j();
    public static final uu M = new xf(19).c(0.6f).a(g).a("sponge");
    public static final uu N = new fk(20, 49, ln.p, false).c(0.3f).a(j).a("glass");
    public static final uu O = new mt(21, 160).c(3.0f).b(5.0f).a(h).a("oreLapis");
    public static final uu P = new uu(22, 144, ln.e).c(3.0f).b(5.0f).a(h).a("blockLapis");
    public static final uu Q = new xq(23).c(3.5f).a(h).a("dispenser").j();
    public static final uu R = new rd(24).a(h).c(0.8f).a("sandStone");
    public static final uu S = new pt(25).c(0.8f).a("musicBlock").j();
    public static final uu T = new ve(26).c(0.2f).a("bed").q().j();
    public static final uu U = new pc(27, 179, true).c(0.7f).a(i).a("goldenRail").j();
    public static final uu V = new ph(28, 195).c(0.7f).a(i).a("detectorRail").j();
    public static final uu W = new jq(29, 106, true).a("pistonStickyBase").j();
    public static final uu X = new rn(30, 11).g(1).c(4.0f).a("web");
    public static final ru Y = (ru)new ru(31, 39).c(0.0f).a(g).a("tallgrass");
    public static final jb Z = (jb)new jb(32, 55).c(0.0f).a(g).a("deadbush");
    public static final uu aa = new jq(33, 107, false).a("pistonBase").j();
    public static final h ab = (h)new h(34, 107).j();
    public static final uu ac = new ee().c(0.8f).a(k).a("cloth").j();
    public static final ut ad = new ut(36);
    public static final wb ae = (wb)new wb(37, 13).c(0.0f).a(g).a("flower");
    public static final wb af = (wb)new wb(38, 12).c(0.0f).a(g).a("rose");
    public static final wb ag = (wb)new tl(39, 29).c(0.0f).a(g).a(0.125f).a("mushroom");
    public static final wb ah = (wb)new tl(40, 28).c(0.0f).a(g).a("mushroom");
    public static final uu ai = new l(41, 23).c(3.0f).b(10.0f).a(i).a("blockGold");
    public static final uu aj = new l(42, 22).c(5.0f).b(10.0f).a(i).a("blockIron");
    public static final uu ak = new ys(43, true).c(2.0f).b(10.0f).a(h).a("stoneSlab");
    public static final uu al = new ys(44, false).c(2.0f).b(10.0f).a(h).a("stoneSlab");
    public static final uu am = new uu(45, 7, ln.e).c(2.0f).b(10.0f).a(h).a("brick");
    public static final uu an = new ah(46, 8).c(0.0f).a(g).a("tnt");
    public static final uu ao = new hb(47, 35).c(1.5f).a(e).a("bookshelf");
    public static final uu ap = new uu(48, 36, ln.e).c(2.0f).b(10.0f).a(h).a("stoneMoss");
    public static final uu aq = new fb(49, 37).c(10.0f).b(2000.0f).a(h).a("obsidian");
    public static final uu ar = new vm(50, 80).c(0.0f).a(0.9375f).a(e).a("torch").j();
    public static final yq as = (yq)new yq(51, 31).c(0.0f).a(1.0f).a(e).a("fire").q().j();
    public static final uu at = new dd(52, 65).c(5.0f).a(i).a("mobSpawner").q();
    public static final uu au = new ss(53, y).a("stairsWood").j();
    public static final uu av = new e(54).c(2.5f).a(e).a("chest").j();
    public static final uu aw = new sm(55, 164).c(0.0f).a(d).a("redstoneDust").q().j();
    public static final uu ax = new mt(56, 50).c(3.0f).b(5.0f).a(h).a("oreDiamond");
    public static final uu ay = new l(57, 24).c(5.0f).b(10.0f).a(i).a("blockDiamond");
    public static final uu az = new fi(58).c(2.5f).a(e).a("workbench");
    public static final uu aA = new ni(59, 88).c(0.0f).a(g).a("crops").q().j();
    public static final uu aB = new vl(60).c(0.6f).a(f).a("farmland");
    public static final uu aC = new tc(61, false).c(3.5f).a(h).a("furnace").j();
    public static final uu aD = new tc(62, true).c(3.5f).a(h).a(0.875f).a("furnace").j();
    public static final uu aE = new uj(63, yk.class, true).c(1.0f).a(e).a("sign").q().j();
    public static final uu aF = new le(64, ln.d).c(3.0f).a(e).a("doorWood").q().j();
    public static final uu aG = new dp(65, 83).c(0.4f).a(e).a("ladder").j();
    public static final uu aH = new pc(66, 128, false).c(0.7f).a(i).a("rail").j();
    public static final uu aI = new ss(67, x).a("stairsStone").j();
    public static final uu aJ = new uj(68, yk.class, false).c(1.0f).a(e).a("sign").q().j();
    public static final uu aK = new xr(69, 96).c(0.5f).a(e).a("lever").j();
    public static final uu aL = new bv(70, uu.u.bm, rt.b, ln.e).c(0.5f).a(h).a("pressurePlate").j();
    public static final uu aM = new le(71, ln.f).c(5.0f).a(i).a("doorIron").q().j();
    public static final uu aN = new bv(72, uu.y.bm, rt.a, ln.d).c(0.5f).a(e).a("pressurePlate").j();
    public static final uu aO = new bs(73, 51, false).c(3.0f).b(5.0f).a(h).a("oreRedstone").j();
    public static final uu aP = new bs(74, 51, true).a(0.625f).c(3.0f).b(5.0f).a(h).a("oreRedstone").j();
    public static final uu aQ = new db(75, 115, false).c(0.0f).a(e).a("notGate").j();
    public static final uu aR = new db(76, 99, true).c(0.0f).a(0.5f).a(e).a("notGate").j();
    public static final uu aS = new oi(77, uu.u.bm).c(0.5f).a(h).a("button").j();
    public static final uu aT = new jr(78, 66).c(0.1f).a(k).a("snow");
    public static final uu aU = new nk(79, 67).c(0.5f).g(3).a(j).a("ice");
    public static final uu aV = new ac(80, 66).c(0.2f).a(k).a("snow");
    public static final uu aW = new or(81, 70).c(0.4f).a(k).a("cactus");
    public static final uu aX = new rz(82, 72).c(0.6f).a(f).a("clay");
    public static final uu aY = new ri(83, 73).c(0.0f).a(g).a("reeds").q();
    public static final uu aZ = new fo(84, 74).c(2.0f).b(10.0f).a(h).a("jukebox").j();
    public static final uu ba = new jw(85, 4).c(2.0f).b(5.0f).a(e).a("fence").j();
    public static final uu bb = new fc(86, 102, false).c(1.0f).a(e).a("pumpkin").j();
    public static final uu bc = new yi(87, 103).c(0.4f).a(h).a("hellrock");
    public static final uu bd = new oa(88, 104).c(0.5f).a(l).a("hellsand");
    public static final uu be = new ly(89, 105, ln.e).c(0.3f).a(j).a(1.0f).a("lightgem");
    public static final ak bf = (ak)new ak(90, 14).c(-1.0f).a(j).a(0.75f).a("portal");
    public static final uu bg = new fc(91, 102, true).c(1.0f).a(e).a(1.0f).a("litpumpkin").j();
    public static final uu bh = new um(92, 121).c(0.5f).a(k).a("cake").q().j();
    public static final uu bi = new wo(93, false).c(0.0f).a(e).a("diode").q().j();
    public static final uu bj = new wo(94, true).c(0.0f).a(0.625f).a(e).a("diode").q().j();
    public static final uu bk = new ks(95).c(0.0f).a(1.0f).a(e).a("lockedchest").b(true).j();
    public static final uu bl = new oq(96, ln.d).c(3.0f).a(e).a("trapdoor").q().j();
    public int bm;
    public final int bn;
    protected float bo;
    protected float bp;
    protected boolean bq = true;
    protected boolean br = true;
    public double bs;
    public double bt;
    public double bu;
    public double bv;
    public double bw;
    public double bx;
    public ct by = d;
    public float bz = 1.0f;
    public final ln bA;
    public float bB = 0.6f;
    private String a;

    protected uu(int n2, ln ln2) {
        if (m[n2] != null) {
            throw new IllegalArgumentException("Slot " + n2 + " is already occupied by " + m[n2] + " when adding " + this);
        }
        this.bA = ln2;
        uu.m[n2] = this;
        this.bn = n2;
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        uu.o[n2] = this.c();
        uu.q[n2] = this.c() ? 255 : 0;
        uu.r[n2] = !ln2.b();
        uu.p[n2] = false;
    }

    protected uu j() {
        uu.t[this.bn] = true;
        return this;
    }

    protected void k() {
    }

    protected uu(int n2, int n3, ln ln2) {
        this(n2, ln2);
        this.bm = n3;
    }

    protected uu a(ct ct2) {
        this.by = ct2;
        return this;
    }

    protected uu g(int n2) {
        uu.q[this.bn] = n2;
        return this;
    }

    protected uu a(float f2) {
        uu.s[this.bn] = (int)(15.0f * f2);
        return this;
    }

    protected uu b(float f2) {
        this.bp = f2 * 3.0f;
        return this;
    }

    public boolean d() {
        return true;
    }

    public int b() {
        return 0;
    }

    protected uu c(float f2) {
        this.bo = f2;
        if (this.bp < f2 * 5.0f) {
            this.bp = f2 * 5.0f;
        }
        return this;
    }

    protected uu l() {
        this.c(-1.0f);
        return this;
    }

    public float m() {
        return this.bo;
    }

    protected uu b(boolean bl2) {
        uu.n[this.bn] = bl2;
        return this;
    }

    public void a(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.bs = f2;
        this.bt = f3;
        this.bu = f4;
        this.bv = f5;
        this.bw = f6;
        this.bx = f7;
    }

    public float d(xp xp2, int n2, int n3, int n4) {
        return xp2.a(n2, n3, n4, s[this.bn]);
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        if (n5 == 0 && this.bt > 0.0) {
            return true;
        }
        if (n5 == 1 && this.bw < 1.0) {
            return true;
        }
        if (n5 == 2 && this.bu > 0.0) {
            return true;
        }
        if (n5 == 3 && this.bx < 1.0) {
            return true;
        }
        if (n5 == 4 && this.bs > 0.0) {
            return true;
        }
        if (n5 == 5 && this.bv < 1.0) {
            return true;
        }
        return !xp2.g(n2, n3, n4);
    }

    public boolean d(xp xp2, int n2, int n3, int n4, int n5) {
        return xp2.f(n2, n3, n4).a();
    }

    public int a(xp xp2, int n2, int n3, int n4, int n5) {
        return this.a(n5, xp2.e(n2, n3, n4));
    }

    public int a(int n2, int n3) {
        return this.a(n2);
    }

    public int a(int n2) {
        return this.bm;
    }

    public eq f(fd fd2, int n2, int n3, int n4) {
        return eq.b((double)n2 + this.bs, (double)n3 + this.bt, (double)n4 + this.bu, (double)n2 + this.bv, (double)n3 + this.bw, (double)n4 + this.bx);
    }

    public void a(fd fd2, int n2, int n3, int n4, eq eq2, ArrayList arrayList) {
        eq eq3 = this.e(fd2, n2, n3, n4);
        if (eq3 != null && eq2.a(eq3)) {
            arrayList.add(eq3);
        }
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return eq.b((double)n2 + this.bs, (double)n3 + this.bt, (double)n4 + this.bu, (double)n2 + this.bv, (double)n3 + this.bw, (double)n4 + this.bx);
    }

    public boolean c() {
        return true;
    }

    public boolean a(int n2, boolean bl2) {
        return this.v_();
    }

    public boolean v_() {
        return true;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
    }

    public void b(fd fd2, int n2, int n3, int n4, Random random) {
    }

    public void c(fd fd2, int n2, int n3, int n4, int n5) {
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
    }

    public int e() {
        return 10;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
    }

    public void b(fd fd2, int n2, int n3, int n4) {
    }

    public int a(Random random) {
        return 1;
    }

    public int a(int n2, Random random) {
        return this.bn;
    }

    public float a(gs gs2) {
        if (this.bo < 0.0f) {
            return 0.0f;
        }
        if (!gs2.b(this)) {
            return 1.0f / this.bo / 100.0f;
        }
        return gs2.a(this) / this.bo / 30.0f;
    }

    public final void g(fd fd2, int n2, int n3, int n4, int n5) {
        this.a(fd2, n2, n3, n4, n5, 1.0f);
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, float f2) {
        if (fd2.B) {
            return;
        }
        int n6 = this.a(fd2.r);
        for (int i2 = 0; i2 < n6; ++i2) {
            int n7;
            if (fd2.r.nextFloat() > f2 || (n7 = this.a(n5, fd2.r)) <= 0) continue;
            this.a(fd2, n2, n3, n4, new iz(n7, 1, this.b_(n5)));
        }
    }

    protected void a(fd fd2, int n2, int n3, int n4, iz iz2) {
        if (fd2.B) {
            return;
        }
        float f2 = 0.7f;
        double d2 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        double d3 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        double d4 = (double)(fd2.r.nextFloat() * f2) + (double)(1.0f - f2) * 0.5;
        hl hl2 = new hl(fd2, (double)n2 + d2, (double)n3 + d3, (double)n4 + d4, iz2);
        hl2.c = 10;
        fd2.b(hl2);
    }

    protected int b_(int n2) {
        return 0;
    }

    public float a(sn sn2) {
        return this.bp / 5.0f;
    }

    public vf a(fd fd2, int n2, int n3, int n4, bt bt2, bt bt3) {
        this.a((xp)fd2, n2, n3, n4);
        bt2 = bt2.c(-n2, -n3, -n4);
        bt3 = bt3.c(-n2, -n3, -n4);
        bt bt4 = bt2.a(bt3, this.bs);
        bt bt5 = bt2.a(bt3, this.bv);
        bt bt6 = bt2.b(bt3, this.bt);
        bt bt7 = bt2.b(bt3, this.bw);
        bt bt8 = bt2.c(bt3, this.bu);
        bt bt9 = bt2.c(bt3, this.bx);
        if (!this.a(bt4)) {
            bt4 = null;
        }
        if (!this.a(bt5)) {
            bt5 = null;
        }
        if (!this.b(bt6)) {
            bt6 = null;
        }
        if (!this.b(bt7)) {
            bt7 = null;
        }
        if (!this.c(bt8)) {
            bt8 = null;
        }
        if (!this.c(bt9)) {
            bt9 = null;
        }
        bt bt10 = null;
        if (bt4 != null && (bt10 == null || bt2.c(bt4) < bt2.c(bt10))) {
            bt10 = bt4;
        }
        if (bt5 != null && (bt10 == null || bt2.c(bt5) < bt2.c(bt10))) {
            bt10 = bt5;
        }
        if (bt6 != null && (bt10 == null || bt2.c(bt6) < bt2.c(bt10))) {
            bt10 = bt6;
        }
        if (bt7 != null && (bt10 == null || bt2.c(bt7) < bt2.c(bt10))) {
            bt10 = bt7;
        }
        if (bt8 != null && (bt10 == null || bt2.c(bt8) < bt2.c(bt10))) {
            bt10 = bt8;
        }
        if (bt9 != null && (bt10 == null || bt2.c(bt9) < bt2.c(bt10))) {
            bt10 = bt9;
        }
        if (bt10 == null) {
            return null;
        }
        int n5 = -1;
        if (bt10 == bt4) {
            n5 = 4;
        }
        if (bt10 == bt5) {
            n5 = 5;
        }
        if (bt10 == bt6) {
            n5 = 0;
        }
        if (bt10 == bt7) {
            n5 = 1;
        }
        if (bt10 == bt8) {
            n5 = 2;
        }
        if (bt10 == bt9) {
            n5 = 3;
        }
        return new vf(n2, n3, n4, n5, bt10.c(n2, n3, n4));
    }

    private boolean a(bt bt2) {
        if (bt2 == null) {
            return false;
        }
        return bt2.b >= this.bt && bt2.b <= this.bw && bt2.c >= this.bu && bt2.c <= this.bx;
    }

    private boolean b(bt bt2) {
        if (bt2 == null) {
            return false;
        }
        return bt2.a >= this.bs && bt2.a <= this.bv && bt2.c >= this.bu && bt2.c <= this.bx;
    }

    private boolean c(bt bt2) {
        if (bt2 == null) {
            return false;
        }
        return bt2.a >= this.bs && bt2.a <= this.bv && bt2.b >= this.bt && bt2.b <= this.bw;
    }

    public void d(fd fd2, int n2, int n3, int n4) {
    }

    public int b_() {
        return 0;
    }

    public boolean a(fd fd2, int n2, int n3, int n4, int n5) {
        return this.a(fd2, n2, n3, n4);
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3, n4);
        return n5 == 0 || uu.m[n5].bA.g();
    }

    public boolean a(fd fd2, int n2, int n3, int n4, gs gs2) {
        return false;
    }

    public void b(fd fd2, int n2, int n3, int n4, sn sn2) {
    }

    public void e(fd fd2, int n2, int n3, int n4, int n5) {
    }

    public void b(fd fd2, int n2, int n3, int n4, gs gs2) {
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2, bt bt2) {
    }

    public void a(xp xp2, int n2, int n3, int n4) {
    }

    public int b(int n2) {
        return 0xFFFFFF;
    }

    public int b(xp xp2, int n2, int n3, int n4) {
        return 0xFFFFFF;
    }

    public boolean c(xp xp2, int n2, int n3, int n4, int n5) {
        return false;
    }

    public boolean f() {
        return false;
    }

    public void a(fd fd2, int n2, int n3, int n4, sn sn2) {
    }

    public boolean d(fd fd2, int n2, int n3, int n4, int n5) {
        return false;
    }

    public void g() {
    }

    public void a(fd fd2, gs gs2, int n2, int n3, int n4, int n5) {
        gs2.a(jl.C[this.bn], 1);
        this.g(fd2, n2, n3, n4, n5);
    }

    public boolean g(fd fd2, int n2, int n3, int n4) {
        return true;
    }

    public void a(fd fd2, int n2, int n3, int n4, ls ls2) {
    }

    public uu a(String string) {
        this.a = "tile." + string;
        return this;
    }

    public String n() {
        return do.a(this.o() + ".name");
    }

    public String o() {
        return this.a;
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, int n6) {
    }

    public boolean p() {
        return this.br;
    }

    protected uu q() {
        this.br = false;
        return this;
    }

    public int h() {
        return this.bA.j();
    }

    static {
        gm.c[uu.ac.bn] = new bi(uu.ac.bn - 256).a("cloth");
        gm.c[uu.K.bn] = new go(uu.K.bn - 256).a("log");
        gm.c[uu.al.bn] = new en(uu.al.bn - 256).a("stoneSlab");
        gm.c[uu.z.bn] = new mr(uu.z.bn - 256).a("sapling");
        gm.c[uu.L.bn] = new uy(uu.L.bn - 256).a("leaves");
        gm.c[uu.aa.bn] = new qr(uu.aa.bn - 256);
        gm.c[uu.W.bn] = new qr(uu.W.bn - 256);
        for (int i2 = 0; i2 < 256; ++i2) {
            if (m[i2] == null || gm.c[i2] != null) continue;
            gm.c[i2] = new ck(i2 - 256);
            m[i2].k();
        }
        uu.r[0] = true;
        jl.b();
    }
}

