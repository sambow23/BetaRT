/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;

public class th {
    private Map o = new HashMap();
    public static th a = new th();
    private sj p;
    public static double b;
    public static double c;
    public static double d;
    public ji e;
    public ra f;
    public fd g;
    public ls h;
    public float i;
    public float j;
    public kv k;
    public double l;
    public double m;
    public double n;

    private th() {
        this.o.put(cn.class, new yx());
        this.o.put(wh.class, new me(new eh(), new eh(0.5f), 0.7f));
        this.o.put(dl.class, new xy(new mw(), new ea(), 0.7f));
        this.o.put(bx.class, new va(new hh(), 0.7f));
        this.o.put(gi.class, new we(new o(), 0.5f));
        this.o.put(ww.class, new ip(new td(), 0.3f));
        this.o.put(gb.class, new m());
        this.o.put(fr.class, new v(new lc(), 0.5f));
        this.o.put(uz.class, new v(new ej(), 0.5f));
        this.o.put(uw.class, new mj(new no(16), new no(0), 0.25f));
        this.o.put(gs.class, new ds());
        this.o.put(nt.class, new yg(new ej(), 0.5f, 6.0f));
        this.o.put(bp.class, new pq());
        this.o.put(xt.class, new es(new wn(), 0.7f));
        this.o.put(ls.class, new gv(new fh(), 0.5f));
        this.o.put(sn.class, new mb());
        this.o.put(qv.class, new dy());
        this.o.put(sl.class, new mc());
        this.o.put(by.class, new dg(gm.aB.a(0)));
        this.o.put(vv.class, new dg(gm.aN.a(0)));
        this.o.put(cf.class, new kl());
        this.o.put(hl.class, new bb());
        this.o.put(qw.class, new on());
        this.o.put(ju.class, new gn());
        this.o.put(yl.class, new tb());
        this.o.put(fz.class, new fe());
        this.o.put(lx.class, new pl());
        this.o.put(c.class, new pi());
        for (bw bw2 : this.o.values()) {
            bw2.a(this);
        }
    }

    public bw a(Class clazz) {
        bw bw2 = (bw)this.o.get(clazz);
        if (bw2 == null && clazz != sn.class) {
            bw2 = this.a(clazz.getSuperclass());
            this.o.put(clazz, bw2);
        }
        return bw2;
    }

    public bw a(sn sn2) {
        return this.a(sn2.getClass());
    }

    public void a(fd fd2, ji ji2, sj sj2, ls ls2, kv kv2, float f2) {
        this.g = fd2;
        this.e = ji2;
        this.k = kv2;
        this.h = ls2;
        this.p = sj2;
        if (ls2.N()) {
            int n2 = fd2.a(in.b(ls2.aM), in.b(ls2.aN), in.b(ls2.aO));
            if (n2 == uu.T.bn) {
                int n3 = fd2.e(in.b(ls2.aM), in.b(ls2.aN), in.b(ls2.aO));
                int n4 = n3 & 3;
                this.i = n4 * 90 + 180;
                this.j = 0.0f;
            }
        } else {
            this.i = ls2.aU + (ls2.aS - ls2.aU) * f2;
            this.j = ls2.aV + (ls2.aT - ls2.aV) * f2;
        }
        this.l = ls2.bl + (ls2.aM - ls2.bl) * (double)f2;
        this.m = ls2.bm + (ls2.aN - ls2.bm) * (double)f2;
        this.n = ls2.bn + (ls2.aO - ls2.bn) * (double)f2;
    }

    public void a(sn sn2, float f2) {
        double d2 = sn2.bl + (sn2.aM - sn2.bl) * (double)f2;
        double d3 = sn2.bm + (sn2.aN - sn2.bm) * (double)f2;
        double d4 = sn2.bn + (sn2.aO - sn2.bn) * (double)f2;
        float f3 = sn2.aU + (sn2.aS - sn2.aU) * f2;
        float f4 = sn2.a(f2);
        GL11.glColor3f((float)f4, (float)f4, (float)f4);
        this.a(sn2, d2 - b, d3 - c, d4 - d, f3, f2);
    }

    public void a(sn sn2, double d2, double d3, double d4, float f2, float f3) {
        bw bw2 = this.a(sn2);
        if (bw2 != null) {
            bw2.a(sn2, d2, d3, d4, f2, f3);
            bw2.b(sn2, d2, d3, d4, f2, f3);
        }
    }

    public void a(fd fd2) {
        this.g = fd2;
    }

    public double a(double d2, double d3, double d4) {
        double d5 = d2 - this.l;
        double d6 = d3 - this.m;
        double d7 = d4 - this.n;
        return d5 * d5 + d6 * d6 + d7 * d7;
    }

    public sj a() {
        return this.p;
    }
}

