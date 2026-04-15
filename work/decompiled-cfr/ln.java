/*
 * Decompiled with CFR 0.152.
 */
public class ln {
    public static final ln a = new lu(dx.b);
    public static final ln b = new ln(dx.c);
    public static final ln c = new ln(dx.l);
    public static final ln d = new ln(dx.o).o();
    public static final ln e = new ln(dx.m).n();
    public static final ln f = new ln(dx.h).n();
    public static final ln g = new sg(dx.n).k();
    public static final ln h = new sg(dx.f).k();
    public static final ln i = new ln(dx.i).o().m().k();
    public static final ln j = new aq(dx.i).k();
    public static final ln k = new ln(dx.e);
    public static final ln l = new ln(dx.e).o();
    public static final ln m = new lu(dx.b).k();
    public static final ln n = new ln(dx.d);
    public static final ln o = new aq(dx.b).k();
    public static final ln p = new ln(dx.b).m();
    public static final ln q = new ln(dx.f).o().m();
    public static final ln r = new ln(dx.i).k();
    public static final ln s = new ln(dx.g).m();
    public static final ln t = new aq(dx.j).f().m().n().k();
    public static final ln u = new ln(dx.j).n();
    public static final ln v = new ln(dx.i).m().k();
    public static final ln w = new ln(dx.k);
    public static final ln x = new ln(dx.i).k();
    public static final ln y = new ou(dx.b).l();
    public static final ln z = new ln(dx.b).k();
    public static final ln A = new ln(dx.e).n().k();
    public static final ln B = new ln(dx.m).l();
    private boolean D;
    private boolean E;
    private boolean F;
    public final dx C;
    private boolean G = true;
    private int H;

    public ln(dx dx2) {
        this.C = dx2;
    }

    public boolean d() {
        return false;
    }

    public boolean a() {
        return true;
    }

    public boolean b() {
        return true;
    }

    public boolean c() {
        return true;
    }

    private ln m() {
        this.F = true;
        return this;
    }

    private ln n() {
        this.G = false;
        return this;
    }

    private ln o() {
        this.D = true;
        return this;
    }

    public boolean e() {
        return this.D;
    }

    public ln f() {
        this.E = true;
        return this;
    }

    public boolean g() {
        return this.E;
    }

    public boolean h() {
        if (this.F) {
            return false;
        }
        return this.c();
    }

    public boolean i() {
        return this.G;
    }

    public int j() {
        return this.H;
    }

    protected ln k() {
        this.H = 1;
        return this;
    }

    protected ln l() {
        this.H = 2;
        return this;
    }
}

