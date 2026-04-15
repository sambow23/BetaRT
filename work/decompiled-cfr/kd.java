/*
 * Decompiled with CFR 0.152.
 */
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class kd {
    public static final kd a = new yj().b(588342).a("Rainforest").a(2094168);
    public static final kd b = new tf().b(522674).a("Swampland").a(9154376);
    public static final kd c = new kd().b(10215459).a("Seasonal Forest");
    public static final kd d = new rb().b(353825).a("Forest").a(5159473);
    public static final kd e = new fs().b(14278691).a("Savanna");
    public static final kd f = new kd().b(10595616).a("Shrubland");
    public static final kd g = new g().b(3060051).a("Taiga").b().a(8107825);
    public static final kd h = new fs().b(16421912).a("Desert").e();
    public static final kd i = new fs().b(16767248).a("Plains");
    public static final kd j = new fs().b(16772499).a("Ice Desert").b().e().a(12899129);
    public static final kd k = new kd().b(5762041).a("Tundra").b().a(12899129);
    public static final kd l = new t().b(0xFF0000).a("Hell").e();
    public static final kd m = new ry().b(0x8080FF).a("Sky").e();
    public String n;
    public int o;
    public byte p;
    public byte q;
    public int r;
    protected List s;
    protected List t;
    protected List u;
    private boolean v;
    private boolean w;
    private static kd[] x = new kd[4096];

    protected kd() {
        this.p = (byte)uu.v.bn;
        this.q = (byte)uu.w.bn;
        this.r = 5169201;
        this.s = new ArrayList();
        this.t = new ArrayList();
        this.u = new ArrayList();
        this.w = true;
        this.s.add(new bj(cn.class, 10));
        this.s.add(new bj(uz.class, 10));
        this.s.add(new bj(fr.class, 10));
        this.s.add(new bj(gb.class, 10));
        this.s.add(new bj(uw.class, 10));
        this.t.add(new bj(dl.class, 12));
        this.t.add(new bj(wh.class, 10));
        this.t.add(new bj(ww.class, 10));
        this.t.add(new bj(bx.class, 8));
        this.u.add(new bj(xt.class, 10));
    }

    private kd e() {
        this.w = false;
        return this;
    }

    public static void a() {
        for (int i2 = 0; i2 < 64; ++i2) {
            for (int i3 = 0; i3 < 64; ++i3) {
                kd.x[i2 + i3 * 64] = kd.a((float)i2 / 63.0f, (float)i3 / 63.0f);
            }
        }
        kd.h.p = kd.h.q = (byte)uu.F.bn;
        kd.j.p = kd.j.q = (byte)uu.F.bn;
    }

    public pg a(Random random) {
        if (random.nextInt(10) == 0) {
            return new ih();
        }
        return new yh();
    }

    protected kd b() {
        this.v = true;
        return this;
    }

    protected kd a(String string) {
        this.n = string;
        return this;
    }

    protected kd a(int n2) {
        this.r = n2;
        return this;
    }

    protected kd b(int n2) {
        this.o = n2;
        return this;
    }

    public static kd a(double d2, double d3) {
        int n2 = (int)(d2 * 63.0);
        int n3 = (int)(d3 * 63.0);
        return x[n2 + n3 * 64];
    }

    public static kd a(float f2, float f3) {
        f3 *= f2;
        if (f2 < 0.1f) {
            return k;
        }
        if (f3 < 0.2f) {
            if (f2 < 0.5f) {
                return k;
            }
            if (f2 < 0.95f) {
                return e;
            }
            return h;
        }
        if (f3 > 0.5f && f2 < 0.7f) {
            return b;
        }
        if (f2 < 0.5f) {
            return g;
        }
        if (f2 < 0.97f) {
            if (f3 < 0.35f) {
                return f;
            }
            return d;
        }
        if (f3 < 0.45f) {
            return i;
        }
        if (f3 < 0.9f) {
            return c;
        }
        return a;
    }

    public int a(float f2) {
        if ((f2 /= 3.0f) < -1.0f) {
            f2 = -1.0f;
        }
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        return Color.getHSBColor(0.62222224f - f2 * 0.05f, 0.5f + f2 * 0.1f, 1.0f).getRGB();
    }

    public List a(lk lk2) {
        if (lk2 == lk.a) {
            return this.s;
        }
        if (lk2 == lk.b) {
            return this.t;
        }
        if (lk2 == lk.c) {
            return this.u;
        }
        return null;
    }

    public boolean c() {
        return this.v;
    }

    public boolean d() {
        if (this.v) {
            return false;
        }
        return this.w;
    }

    static {
        kd.a();
    }
}

