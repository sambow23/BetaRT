/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class gm {
    protected static Random b = new Random();
    public static gm[] c = new gm[32000];
    public static gm d = new wc(0, bu.c).a(2, 5).a("shovelIron");
    public static gm e = new au(1, bu.c).a(2, 6).a("pickaxeIron");
    public static gm f = new ta(2, bu.c).a(2, 7).a("hatchetIron");
    public static gm g = new ye(3).a(5, 0).a("flintAndSteel");
    public static gm h = new yw(4, 4, false).a(10, 0).a("apple");
    public static gm i = new qz(5).a(5, 1).a("bow");
    public static gm j = new gm(6).a(5, 2).a("arrow");
    public static gm k = new rx(7).a(7, 0).a("coal");
    public static gm l = new gm(8).a(7, 3).a("emerald");
    public static gm m = new gm(9).a(7, 1).a("ingotIron");
    public static gm n = new gm(10).a(7, 2).a("ingotGold");
    public static gm o = new qd(11, bu.c).a(2, 4).a("swordIron");
    public static gm p = new qd(12, bu.a).a(0, 4).a("swordWood");
    public static gm q = new wc(13, bu.a).a(0, 5).a("shovelWood");
    public static gm r = new au(14, bu.a).a(0, 6).a("pickaxeWood");
    public static gm s = new ta(15, bu.a).a(0, 7).a("hatchetWood");
    public static gm t = new qd(16, bu.b).a(1, 4).a("swordStone");
    public static gm u = new wc(17, bu.b).a(1, 5).a("shovelStone");
    public static gm v = new au(18, bu.b).a(1, 6).a("pickaxeStone");
    public static gm w = new ta(19, bu.b).a(1, 7).a("hatchetStone");
    public static gm x = new qd(20, bu.d).a(3, 4).a("swordDiamond");
    public static gm y = new wc(21, bu.d).a(3, 5).a("shovelDiamond");
    public static gm z = new au(22, bu.d).a(3, 6).a("pickaxeDiamond");
    public static gm A = new ta(23, bu.d).a(3, 7).a("hatchetDiamond");
    public static gm B = new gm(24).a(5, 3).h().a("stick");
    public static gm C = new gm(25).a(7, 4).a("bowl");
    public static gm D = new bz(26, 10).a(8, 4).a("mushroomStew");
    public static gm E = new qd(27, bu.e).a(4, 4).a("swordGold");
    public static gm F = new wc(28, bu.e).a(4, 5).a("shovelGold");
    public static gm G = new au(29, bu.e).a(4, 6).a("pickaxeGold");
    public static gm H = new ta(30, bu.e).a(4, 7).a("hatchetGold");
    public static gm I = new gm(31).a(8, 0).a("string");
    public static gm J = new gm(32).a(8, 1).a("feather");
    public static gm K = new gm(33).a(8, 2).a("sulphur");
    public static gm L = new la(34, bu.a).a(0, 8).a("hoeWood");
    public static gm M = new la(35, bu.b).a(1, 8).a("hoeStone");
    public static gm N = new la(36, bu.c).a(2, 8).a("hoeIron");
    public static gm O = new la(37, bu.d).a(3, 8).a("hoeDiamond");
    public static gm P = new la(38, bu.e).a(4, 8).a("hoeGold");
    public static gm Q = new rk(39, uu.aA.bn).a(9, 0).a("seeds");
    public static gm R = new gm(40).a(9, 1).a("wheat");
    public static gm S = new yw(41, 5, false).a(9, 2).a("bread");
    public static gm T = new wa(42, 0, 0, 0).a(0, 0).a("helmetCloth");
    public static gm U = new wa(43, 0, 0, 1).a(0, 1).a("chestplateCloth");
    public static gm V = new wa(44, 0, 0, 2).a(0, 2).a("leggingsCloth");
    public static gm W = new wa(45, 0, 0, 3).a(0, 3).a("bootsCloth");
    public static gm X = new wa(46, 1, 1, 0).a(1, 0).a("helmetChain");
    public static gm Y = new wa(47, 1, 1, 1).a(1, 1).a("chestplateChain");
    public static gm Z = new wa(48, 1, 1, 2).a(1, 2).a("leggingsChain");
    public static gm aa = new wa(49, 1, 1, 3).a(1, 3).a("bootsChain");
    public static gm ab = new wa(50, 2, 2, 0).a(2, 0).a("helmetIron");
    public static gm ac = new wa(51, 2, 2, 1).a(2, 1).a("chestplateIron");
    public static gm ad = new wa(52, 2, 2, 2).a(2, 2).a("leggingsIron");
    public static gm ae = new wa(53, 2, 2, 3).a(2, 3).a("bootsIron");
    public static gm af = new wa(54, 3, 3, 0).a(3, 0).a("helmetDiamond");
    public static gm ag = new wa(55, 3, 3, 1).a(3, 1).a("chestplateDiamond");
    public static gm ah = new wa(56, 3, 3, 2).a(3, 2).a("leggingsDiamond");
    public static gm ai = new wa(57, 3, 3, 3).a(3, 3).a("bootsDiamond");
    public static gm aj = new wa(58, 1, 4, 0).a(4, 0).a("helmetGold");
    public static gm ak = new wa(59, 1, 4, 1).a(4, 1).a("chestplateGold");
    public static gm al = new wa(60, 1, 4, 2).a(4, 2).a("leggingsGold");
    public static gm am = new wa(61, 1, 4, 3).a(4, 3).a("bootsGold");
    public static gm an = new gm(62).a(6, 0).a("flint");
    public static gm ao = new yw(63, 3, true).a(7, 5).a("porkchopRaw");
    public static gm ap = new yw(64, 8, true).a(8, 5).a("porkchopCooked");
    public static gm aq = new ym(65).a(10, 1).a("painting");
    public static gm ar = new yw(66, 42, false).a(11, 0).a("appleGold");
    public static gm as = new vc(67).a(10, 2).a("sign");
    public static gm at = new hu(68, ln.d).a(11, 2).a("doorWood");
    public static gm au = new bc(69, 0).a(10, 4).a("bucket");
    public static gm av = new bc(70, uu.B.bn).a(11, 4).a("bucketWater").a(au);
    public static gm aw = new bc(71, uu.D.bn).a(12, 4).a("bucketLava").a(au);
    public static gm ax = new rl(72, 0).a(7, 8).a("minecart");
    public static gm ay = new sa(73).a(8, 6).a("saddle");
    public static gm az = new hu(74, ln.f).a(12, 2).a("doorIron");
    public static gm aA = new ie(75).a(8, 3).a("redstone");
    public static gm aB = new dm(76).a(14, 0).a("snowball");
    public static gm aC = new vd(77).a(8, 8).a("boat");
    public static gm aD = new gm(78).a(7, 6).a("leather");
    public static gm aE = new bc(79, -1).a(13, 4).a("milk").a(au);
    public static gm aF = new gm(80).a(6, 1).a("brick");
    public static gm aG = new gm(81).a(9, 3).a("clay");
    public static gm aH = new lv(82, uu.aY).a(11, 1).a("reeds");
    public static gm aI = new gm(83).a(10, 3).a("paper");
    public static gm aJ = new gm(84).a(11, 3).a("book");
    public static gm aK = new gm(85).a(14, 1).a("slimeball");
    public static gm aL = new rl(86, 1).a(7, 9).a("minecartChest");
    public static gm aM = new rl(87, 2).a(7, 10).a("minecartFurnace");
    public static gm aN = new mo(88).a(12, 0).a("egg");
    public static gm aO = new gm(89).a(6, 3).a("compass");
    public static gm aP = new cs(90).a(5, 4).a("fishingRod");
    public static gm aQ = new gm(91).a(6, 4).a("clock");
    public static gm aR = new gm(92).a(9, 4).a("yellowDust");
    public static gm aS = new yw(93, 2, false).a(9, 5).a("fishRaw");
    public static gm aT = new yw(94, 5, false).a(10, 5).a("fishCooked");
    public static gm aU = new km(95).a(14, 4).a("dyePowder");
    public static gm aV = new gm(96).a(12, 1).a("bone").h();
    public static gm aW = new gm(97).a(13, 0).a("sugar").h();
    public static gm aX = new lv(98, uu.bh).d(1).a(13, 1).a("cake");
    public static gm aY = new lp(99).d(1).a(13, 2).a("bed");
    public static gm aZ = new lv(100, uu.bi).a(6, 5).a("diode");
    public static gm ba = new qk(101, 1, false, 8).a(12, 5).a("cookie");
    public static wr bb = (wr)new wr(102).a(12, 3).a("map");
    public static bl bc = (bl)new bl(103).a(13, 5).a("shears");
    public static gm bd = new tr(2000, "13").a(0, 15).a("record");
    public static gm be = new tr(2001, "cat").a(1, 15).a("record");
    public final int bf;
    protected int bg = 64;
    private int a = 0;
    protected int bh;
    protected boolean bi = false;
    protected boolean bj = false;
    private gm bk = null;
    private String bl;

    protected gm(int n2) {
        this.bf = 256 + n2;
        if (c[256 + n2] != null) {
            System.out.println("CONFLICT @ " + n2);
        }
        gm.c[256 + n2] = this;
    }

    public gm c(int n2) {
        this.bh = n2;
        return this;
    }

    public gm d(int n2) {
        this.bg = n2;
        return this;
    }

    public gm a(int n2, int n3) {
        this.bh = n2 + n3 * 16;
        return this;
    }

    public int a(int n2) {
        return this.bh;
    }

    public final int b(iz iz2) {
        return this.a(iz2.i());
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        return false;
    }

    public float a(iz iz2, uu uu2) {
        return 1.0f;
    }

    public iz a(iz iz2, fd fd2, gs gs2) {
        return iz2;
    }

    public int d() {
        return this.bg;
    }

    public int b(int n2) {
        return 0;
    }

    public boolean e() {
        return this.bj;
    }

    protected gm a(boolean bl2) {
        this.bj = bl2;
        return this;
    }

    public int f() {
        return this.a;
    }

    protected gm e(int n2) {
        this.a = n2;
        return this;
    }

    public boolean g() {
        return this.a > 0 && !this.bj;
    }

    public boolean a(iz iz2, ls ls2, ls ls3) {
        return false;
    }

    public boolean a(iz iz2, int n2, int n3, int n4, int n5, ls ls2) {
        return false;
    }

    public int a(sn sn2) {
        return 1;
    }

    public boolean a(uu uu2) {
        return false;
    }

    public void a(iz iz2, ls ls2) {
    }

    public gm h() {
        this.bi = true;
        return this;
    }

    public boolean b() {
        return this.bi;
    }

    public boolean c() {
        return false;
    }

    public gm a(String string) {
        this.bl = "item." + string;
        return this;
    }

    public String a() {
        return this.bl;
    }

    public String a(iz iz2) {
        return this.bl;
    }

    public gm a(gm gm2) {
        if (this.bg > 1) {
            throw new IllegalArgumentException("Max stack size must be 1 for items with crafting results");
        }
        this.bk = gm2;
        return this;
    }

    public gm i() {
        return this.bk;
    }

    public boolean j() {
        return this.bk != null;
    }

    public String k() {
        return do.a(this.a() + ".name");
    }

    public int f(int n2) {
        return 0xFFFFFF;
    }

    public void a(iz iz2, fd fd2, sn sn2, int n2, boolean bl2) {
    }

    public void b(iz iz2, fd fd2, gs gs2) {
    }

    static {
        jl.c();
    }
}

