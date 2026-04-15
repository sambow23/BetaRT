/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class fd
implements xp {
    public boolean a = false;
    private List C = new ArrayList();
    public List b = new ArrayList();
    private List D = new ArrayList();
    private TreeSet E = new TreeSet();
    private Set F = new HashSet();
    public List c = new ArrayList();
    private List G = new ArrayList();
    public List d = new ArrayList();
    public List e = new ArrayList();
    private long H = 0xFFFFFFL;
    public int f = 0;
    protected int g = new Random().nextInt();
    protected final int h = 1013904223;
    protected float i;
    protected float j;
    protected float k;
    protected float l;
    protected int m = 0;
    public int n = 0;
    public boolean o = false;
    private long I = System.currentTimeMillis();
    protected int p = 40;
    public int q;
    public Random r = new Random();
    public boolean s = false;
    public final xa t;
    protected List u = new ArrayList();
    protected cl v;
    protected final wt w;
    protected ei x;
    public boolean y;
    private boolean J;
    public hc z;
    private ArrayList K = new ArrayList();
    private boolean L;
    private int M = 0;
    private boolean N = true;
    private boolean O = true;
    static int A = 0;
    private Set P = new HashSet();
    private int Q = this.r.nextInt(12000);
    private List R = new ArrayList();
    public boolean B = false;

    public xv a() {
        return this.t.b;
    }

    public fd(wt wt2, String string, xa xa2, long l2) {
        this.w = wt2;
        this.x = new ei(l2, string);
        this.t = xa2;
        this.z = new hc(wt2);
        xa2.a(this);
        this.v = this.b();
        this.k();
        this.E();
    }

    public fd(fd fd2, xa xa2) {
        this.I = fd2.I;
        this.w = fd2.w;
        this.x = new ei(fd2.x);
        this.z = new hc(this.w);
        this.t = xa2;
        xa2.a(this);
        this.v = this.b();
        this.k();
        this.E();
    }

    public fd(wt wt2, String string, long l2) {
        this(wt2, string, l2, null);
    }

    public fd(wt wt2, String string, long l2, xa xa2) {
        this.w = wt2;
        this.z = new hc(wt2);
        this.x = wt2.c();
        boolean bl2 = this.s = this.x == null;
        this.t = xa2 != null ? xa2 : (this.x != null && this.x.i() == -1 ? xa.a(-1) : xa.a(0));
        boolean bl3 = false;
        if (this.x == null) {
            this.x = new ei(l2, string);
            bl3 = true;
        } else {
            this.x.a(string);
        }
        this.t.a(this);
        this.v = this.b();
        if (bl3) {
            this.c();
        }
        this.k();
        this.E();
    }

    protected cl b() {
        bf bf2 = this.w.a(this.t);
        return new ok(this, bf2, this.t.b());
    }

    protected void c() {
        this.y = true;
        int n2 = 0;
        int n3 = 64;
        int n4 = 0;
        while (!this.t.a(n2, n4)) {
            n2 += this.r.nextInt(64) - this.r.nextInt(64);
            n4 += this.r.nextInt(64) - this.r.nextInt(64);
        }
        this.x.a(n2, n3, n4);
        this.y = false;
    }

    public void d() {
        if (this.x.d() <= 0) {
            this.x.b(64);
        }
        int n2 = this.x.c();
        int n3 = this.x.e();
        while (this.a(n2, n3) == 0) {
            n2 += this.r.nextInt(8) - this.r.nextInt(8);
            n3 += this.r.nextInt(8) - this.r.nextInt(8);
        }
        this.x.a(n2);
        this.x.c(n3);
    }

    public int a(int n2, int n3) {
        int n4 = 63;
        while (!this.d(n2, n4 + 1, n3)) {
            ++n4;
        }
        return this.a(n2, n4, n3);
    }

    public void e() {
    }

    public void a(gs gs2) {
        try {
            nu nu2 = this.x.h();
            if (nu2 != null) {
                gs2.e(nu2);
                this.x.a((nu)null);
            }
            if (this.v instanceof kx) {
                kx kx2 = (kx)this.v;
                int n2 = in.d((int)gs2.aM) >> 4;
                int n3 = in.d((int)gs2.aO) >> 4;
                kx2.d(n2, n3);
            }
            this.b(gs2);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void a(boolean bl2, yb yb2) {
        if (!this.v.b()) {
            return;
        }
        if (yb2 != null) {
            yb2.b("Saving level");
        }
        this.D();
        if (yb2 != null) {
            yb2.d("Saving chunks");
        }
        this.v.a(bl2, yb2);
    }

    private void D() {
        this.r();
        this.w.a(this.x, this.d);
        this.z.a();
    }

    public boolean a(int n2) {
        if (!this.v.b()) {
            return true;
        }
        if (n2 == 0) {
            this.D();
        }
        return this.v.a(false, null);
    }

    public int a(int n2, int n3, int n4) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return 0;
        }
        if (n3 < 0) {
            return 0;
        }
        if (n3 >= 128) {
            return 0;
        }
        return this.c(n2 >> 4, n4 >> 4).a(n2 & 0xF, n3, n4 & 0xF);
    }

    public boolean d(int n2, int n3, int n4) {
        return this.a(n2, n3, n4) == 0;
    }

    public boolean i(int n2, int n3, int n4) {
        if (n3 < 0 || n3 >= 128) {
            return false;
        }
        return this.f(n2 >> 4, n4 >> 4);
    }

    public boolean b(int n2, int n3, int n4, int n5) {
        return this.a(n2 - n5, n3 - n5, n4 - n5, n2 + n5, n3 + n5, n4 + n5);
    }

    public boolean a(int n2, int n3, int n4, int n5, int n6, int n7) {
        if (n6 < 0 || n3 >= 128) {
            return false;
        }
        n2 >>= 4;
        n3 >>= 4;
        n4 >>= 4;
        n5 >>= 4;
        n6 >>= 4;
        n7 >>= 4;
        for (int i2 = n2; i2 <= n5; ++i2) {
            for (int i3 = n4; i3 <= n7; ++i3) {
                if (this.f(i2, i3)) continue;
                return false;
            }
        }
        return true;
    }

    private boolean f(int n2, int n3) {
        return this.v.a(n2, n3);
    }

    public lm b(int n2, int n3) {
        return this.c(n2 >> 4, n3 >> 4);
    }

    public lm c(int n2, int n3) {
        return this.v.b(n2, n3);
    }

    public boolean a(int n2, int n3, int n4, int n5, int n6) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return false;
        }
        if (n3 < 0) {
            return false;
        }
        if (n3 >= 128) {
            return false;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        return lm2.a(n2 & 0xF, n3, n4 & 0xF, n5, n6);
    }

    public boolean c(int n2, int n3, int n4, int n5) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return false;
        }
        if (n3 < 0) {
            return false;
        }
        if (n3 >= 128) {
            return false;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        return lm2.a(n2 & 0xF, n3, n4 & 0xF, n5);
    }

    public ln f(int n2, int n3, int n4) {
        int n5 = this.a(n2, n3, n4);
        if (n5 == 0) {
            return ln.a;
        }
        return uu.m[n5].bA;
    }

    public int e(int n2, int n3, int n4) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return 0;
        }
        if (n3 < 0) {
            return 0;
        }
        if (n3 >= 128) {
            return 0;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        return lm2.b(n2 &= 0xF, n3, n4 &= 0xF);
    }

    public void d(int n2, int n3, int n4, int n5) {
        if (this.e(n2, n3, n4, n5)) {
            int n6 = this.a(n2, n3, n4);
            if (uu.t[n6 & 0xFF]) {
                this.g(n2, n3, n4, n6);
            } else {
                this.i(n2, n3, n4, n6);
            }
        }
    }

    public boolean e(int n2, int n3, int n4, int n5) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return false;
        }
        if (n3 < 0) {
            return false;
        }
        if (n3 >= 128) {
            return false;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        lm2.b(n2 &= 0xF, n3, n4 &= 0xF, n5);
        return true;
    }

    public boolean f(int n2, int n3, int n4, int n5) {
        if (this.c(n2, n3, n4, n5)) {
            this.g(n2, n3, n4, n5);
            return true;
        }
        return false;
    }

    public boolean b(int n2, int n3, int n4, int n5, int n6) {
        if (this.a(n2, n3, n4, n5, n6)) {
            this.g(n2, n3, n4, n5);
            return true;
        }
        return false;
    }

    public void j(int n2, int n3, int n4) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(n2, n3, n4);
        }
    }

    protected void g(int n2, int n3, int n4, int n5) {
        this.j(n2, n3, n4);
        this.i(n2, n3, n4, n5);
    }

    public void h(int n2, int n3, int n4, int n5) {
        if (n4 > n5) {
            int n6 = n5;
            n5 = n4;
            n4 = n6;
        }
        this.b(n2, n4, n3, n2, n5, n3);
    }

    public void k(int n2, int n3, int n4) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).b(n2, n3, n4, n2, n3, n4);
        }
    }

    public void b(int n2, int n3, int n4, int n5, int n6, int n7) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).b(n2, n3, n4, n5, n6, n7);
        }
    }

    public void i(int n2, int n3, int n4, int n5) {
        this.l(n2 - 1, n3, n4, n5);
        this.l(n2 + 1, n3, n4, n5);
        this.l(n2, n3 - 1, n4, n5);
        this.l(n2, n3 + 1, n4, n5);
        this.l(n2, n3, n4 - 1, n5);
        this.l(n2, n3, n4 + 1, n5);
    }

    private void l(int n2, int n3, int n4, int n5) {
        if (this.o || this.B) {
            return;
        }
        uu uu2 = uu.m[this.a(n2, n3, n4)];
        if (uu2 != null) {
            uu2.b(this, n2, n3, n4, n5);
        }
    }

    public boolean l(int n2, int n3, int n4) {
        return this.c(n2 >> 4, n4 >> 4).c(n2 & 0xF, n3, n4 & 0xF);
    }

    public int m(int n2, int n3, int n4) {
        if (n3 < 0) {
            return 0;
        }
        if (n3 >= 128) {
            n3 = 127;
        }
        return this.c(n2 >> 4, n4 >> 4).c(n2 & 0xF, n3, n4 & 0xF, 0);
    }

    public int n(int n2, int n3, int n4) {
        return this.a(n2, n3, n4, true);
    }

    public int a(int n2, int n3, int n4, boolean bl2) {
        int n5;
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return 15;
        }
        if (bl2 && ((n5 = this.a(n2, n3, n4)) == uu.al.bn || n5 == uu.aB.bn || n5 == uu.aI.bn || n5 == uu.au.bn)) {
            int n6 = this.a(n2, n3 + 1, n4, false);
            int n7 = this.a(n2 + 1, n3, n4, false);
            int n8 = this.a(n2 - 1, n3, n4, false);
            int n9 = this.a(n2, n3, n4 + 1, false);
            int n10 = this.a(n2, n3, n4 - 1, false);
            if (n7 > n6) {
                n6 = n7;
            }
            if (n8 > n6) {
                n6 = n8;
            }
            if (n9 > n6) {
                n6 = n9;
            }
            if (n10 > n6) {
                n6 = n10;
            }
            return n6;
        }
        if (n3 < 0) {
            return 0;
        }
        if (n3 >= 128) {
            n3 = 127;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        return lm2.c(n2 &= 0xF, n3, n4 &= 0xF, this.f);
    }

    public boolean o(int n2, int n3, int n4) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return false;
        }
        if (n3 < 0) {
            return false;
        }
        if (n3 >= 128) {
            return true;
        }
        if (!this.f(n2 >> 4, n4 >> 4)) {
            return false;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        return lm2.c(n2 &= 0xF, n3, n4 &= 0xF);
    }

    public int d(int n2, int n3) {
        if (n2 < -32000000 || n3 < -32000000 || n2 >= 32000000 || n3 > 32000000) {
            return 0;
        }
        if (!this.f(n2 >> 4, n3 >> 4)) {
            return 0;
        }
        lm lm2 = this.c(n2 >> 4, n3 >> 4);
        return lm2.b(n2 & 0xF, n3 & 0xF);
    }

    public void a(eb eb2, int n2, int n3, int n4, int n5) {
        int n6;
        if (this.t.e && eb2 == eb.a) {
            return;
        }
        if (!this.i(n2, n3, n4)) {
            return;
        }
        if (eb2 == eb.a) {
            if (this.o(n2, n3, n4)) {
                n5 = 15;
            }
        } else if (eb2 == eb.b && uu.s[n6 = this.a(n2, n3, n4)] > n5) {
            n5 = uu.s[n6];
        }
        if (this.a(eb2, n2, n3, n4) != n5) {
            this.a(eb2, n2, n3, n4, n2, n3, n4);
        }
    }

    public int a(eb eb2, int n2, int n3, int n4) {
        if (n3 < 0) {
            n3 = 0;
        }
        if (n3 >= 128) {
            n3 = 127;
        }
        if (n3 < 0 || n3 >= 128 || n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return eb2.c;
        }
        int n5 = n2 >> 4;
        int n6 = n4 >> 4;
        if (!this.f(n5, n6)) {
            return 0;
        }
        lm lm2 = this.c(n5, n6);
        return lm2.a(eb2, n2 & 0xF, n3, n4 & 0xF);
    }

    public void b(eb eb2, int n2, int n3, int n4, int n5) {
        if (n2 < -32000000 || n4 < -32000000 || n2 >= 32000000 || n4 > 32000000) {
            return;
        }
        if (n3 < 0) {
            return;
        }
        if (n3 >= 128) {
            return;
        }
        if (!this.f(n2 >> 4, n4 >> 4)) {
            return;
        }
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        lm2.a(eb2, n2 & 0xF, n3, n4 & 0xF, n5);
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(n2, n3, n4);
        }
    }

    public float a(int n2, int n3, int n4, int n5) {
        int n6 = this.n(n2, n3, n4);
        if (n6 < n5) {
            n6 = n5;
        }
        return this.t.f[n6];
    }

    public float c(int n2, int n3, int n4) {
        return this.t.f[this.n(n2, n3, n4)];
    }

    public boolean f() {
        return this.f < 4;
    }

    public vf a(bt bt2, bt bt3) {
        return this.a(bt2, bt3, false, false);
    }

    public vf a(bt bt2, bt bt3, boolean bl2) {
        return this.a(bt2, bt3, bl2, false);
    }

    public vf a(bt bt2, bt bt3, boolean bl2, boolean bl3) {
        vf vf2;
        if (Double.isNaN(bt2.a) || Double.isNaN(bt2.b) || Double.isNaN(bt2.c)) {
            return null;
        }
        if (Double.isNaN(bt3.a) || Double.isNaN(bt3.b) || Double.isNaN(bt3.c)) {
            return null;
        }
        int n2 = in.b(bt3.a);
        int n3 = in.b(bt3.b);
        int n4 = in.b(bt3.c);
        int n5 = in.b(bt2.a);
        int n6 = in.b(bt2.b);
        int n7 = in.b(bt2.c);
        int n8 = this.a(n5, n6, n7);
        int n9 = this.e(n5, n6, n7);
        uu uu2 = uu.m[n8];
        if ((!bl3 || uu2 == null || uu2.e(this, n5, n6, n7) != null) && n8 > 0 && uu2.a(n9, bl2) && (vf2 = uu2.a(this, n5, n6, n7, bt2, bt3)) != null) {
            return vf2;
        }
        n8 = 200;
        while (n8-- >= 0) {
            vf vf3;
            if (Double.isNaN(bt2.a) || Double.isNaN(bt2.b) || Double.isNaN(bt2.c)) {
                return null;
            }
            if (n5 == n2 && n6 == n3 && n7 == n4) {
                return null;
            }
            n9 = 1;
            boolean bl4 = true;
            boolean bl5 = true;
            double d2 = 999.0;
            double d3 = 999.0;
            double d4 = 999.0;
            if (n2 > n5) {
                d2 = (double)n5 + 1.0;
            } else if (n2 < n5) {
                d2 = (double)n5 + 0.0;
            } else {
                n9 = 0;
            }
            if (n3 > n6) {
                d3 = (double)n6 + 1.0;
            } else if (n3 < n6) {
                d3 = (double)n6 + 0.0;
            } else {
                bl4 = false;
            }
            if (n4 > n7) {
                d4 = (double)n7 + 1.0;
            } else if (n4 < n7) {
                d4 = (double)n7 + 0.0;
            } else {
                bl5 = false;
            }
            double d5 = 999.0;
            double d6 = 999.0;
            double d7 = 999.0;
            double d8 = bt3.a - bt2.a;
            double d9 = bt3.b - bt2.b;
            double d10 = bt3.c - bt2.c;
            if (n9 != 0) {
                d5 = (d2 - bt2.a) / d8;
            }
            if (bl4) {
                d6 = (d3 - bt2.b) / d9;
            }
            if (bl5) {
                d7 = (d4 - bt2.c) / d10;
            }
            int n10 = 0;
            if (d5 < d6 && d5 < d7) {
                n10 = n2 > n5 ? 4 : 5;
                bt2.a = d2;
                bt2.b += d9 * d5;
                bt2.c += d10 * d5;
            } else if (d6 < d7) {
                n10 = n3 > n6 ? 0 : 1;
                bt2.a += d8 * d6;
                bt2.b = d3;
                bt2.c += d10 * d6;
            } else {
                n10 = n4 > n7 ? 2 : 3;
                bt2.a += d8 * d7;
                bt2.b += d9 * d7;
                bt2.c = d4;
            }
            bt bt4 = bt.b(bt2.a, bt2.b, bt2.c);
            bt4.a = in.b(bt2.a);
            n5 = (int)bt4.a;
            if (n10 == 5) {
                --n5;
                bt4.a += 1.0;
            }
            bt4.b = in.b(bt2.b);
            n6 = (int)bt4.b;
            if (n10 == 1) {
                --n6;
                bt4.b += 1.0;
            }
            bt4.c = in.b(bt2.c);
            n7 = (int)bt4.c;
            if (n10 == 3) {
                --n7;
                bt4.c += 1.0;
            }
            int n11 = this.a(n5, n6, n7);
            int n12 = this.e(n5, n6, n7);
            uu uu3 = uu.m[n11];
            if (bl3 && uu3 != null && uu3.e(this, n5, n6, n7) == null || n11 <= 0 || !uu3.a(n12, bl2) || (vf3 = uu3.a(this, n5, n6, n7, bt2, bt3)) == null) continue;
            return vf3;
        }
        return null;
    }

    public void a(sn sn2, String string, float f2, float f3) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(string, sn2.aM, sn2.aN - (double)sn2.bf, sn2.aO, f2, f3);
        }
    }

    public void a(double d2, double d3, double d4, String string, float f2, float f3) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(string, d2, d3, d4, f2, f3);
        }
    }

    public void a(String string, int n2, int n3, int n4) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(string, n2, n3, n4);
        }
    }

    public void a(String string, double d2, double d3, double d4, double d5, double d6, double d7) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(string, d2, d3, d4, d5, d6, d7);
        }
    }

    public boolean a(sn sn2) {
        this.e.add(sn2);
        return true;
    }

    public boolean b(sn sn2) {
        int n2 = in.b(sn2.aM / 16.0);
        int n3 = in.b(sn2.aO / 16.0);
        boolean bl2 = false;
        if (sn2 instanceof gs) {
            bl2 = true;
        }
        if (bl2 || this.f(n2, n3)) {
            if (sn2 instanceof gs) {
                gs gs2 = (gs)sn2;
                this.d.add(gs2);
                this.y();
            }
            this.c(n2, n3).a(sn2);
            this.b.add(sn2);
            this.c(sn2);
            return true;
        }
        return false;
    }

    protected void c(sn sn2) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(sn2);
        }
    }

    protected void d(sn sn2) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).b(sn2);
        }
    }

    public void e(sn sn2) {
        if (sn2.aG != null) {
            sn2.aG.i(null);
        }
        if (sn2.aH != null) {
            sn2.i(null);
        }
        sn2.K();
        if (sn2 instanceof gs) {
            this.d.remove((gs)sn2);
            this.y();
        }
    }

    public void a(pm pm2) {
        this.u.add(pm2);
    }

    public void b(pm pm2) {
        this.u.remove(pm2);
    }

    public List a(sn sn2, eq eq2) {
        this.K.clear();
        int n2 = in.b(eq2.a);
        int n3 = in.b(eq2.d + 1.0);
        int n4 = in.b(eq2.b);
        int n5 = in.b(eq2.e + 1.0);
        int n6 = in.b(eq2.c);
        int n7 = in.b(eq2.f + 1.0);
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n6; i3 < n7; ++i3) {
                if (!this.i(i2, 64, i3)) continue;
                for (int i4 = n4 - 1; i4 < n5; ++i4) {
                    uu uu2 = uu.m[this.a(i2, i4, i3)];
                    if (uu2 == null) continue;
                    uu2.a(this, i2, i4, i3, eq2, this.K);
                }
            }
        }
        double d2 = 0.25;
        List list = this.b(sn2, eq2.b(d2, d2, d2));
        for (int i5 = 0; i5 < list.size(); ++i5) {
            eq eq3 = ((sn)list.get(i5)).f();
            if (eq3 != null && eq3.a(eq2)) {
                this.K.add(eq3);
            }
            if ((eq3 = sn2.a((sn)list.get(i5))) == null || !eq3.a(eq2)) continue;
            this.K.add(eq3);
        }
        return this.K;
    }

    public int a(float f2) {
        float f3 = this.b(f2);
        float f4 = 1.0f - (in.b(f3 * (float)Math.PI * 2.0f) * 2.0f + 0.5f);
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        f4 = 1.0f - f4;
        f4 = (float)((double)f4 * (1.0 - (double)(this.g(f2) * 5.0f) / 16.0));
        f4 = (float)((double)f4 * (1.0 - (double)(this.f(f2) * 5.0f) / 16.0));
        f4 = 1.0f - f4;
        return (int)(f4 * 11.0f);
    }

    public bt a(sn sn2, float f2) {
        float f3;
        float f4;
        float f5 = this.b(f2);
        float f6 = in.b(f5 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        if (f6 < 0.0f) {
            f6 = 0.0f;
        }
        if (f6 > 1.0f) {
            f6 = 1.0f;
        }
        int n2 = in.b(sn2.aM);
        int n3 = in.b(sn2.aO);
        float f7 = (float)this.a().b(n2, n3);
        int n4 = this.a().a(n2, n3).a(f7);
        float f8 = (float)(n4 >> 16 & 0xFF) / 255.0f;
        float f9 = (float)(n4 >> 8 & 0xFF) / 255.0f;
        float f10 = (float)(n4 & 0xFF) / 255.0f;
        f8 *= f6;
        f9 *= f6;
        f10 *= f6;
        float f11 = this.g(f2);
        if (f11 > 0.0f) {
            f4 = (f8 * 0.3f + f9 * 0.59f + f10 * 0.11f) * 0.6f;
            f3 = 1.0f - f11 * 0.75f;
            f8 = f8 * f3 + f4 * (1.0f - f3);
            f9 = f9 * f3 + f4 * (1.0f - f3);
            f10 = f10 * f3 + f4 * (1.0f - f3);
        }
        if ((f4 = this.f(f2)) > 0.0f) {
            f3 = (f8 * 0.3f + f9 * 0.59f + f10 * 0.11f) * 0.2f;
            float f12 = 1.0f - f4 * 0.75f;
            f8 = f8 * f12 + f3 * (1.0f - f12);
            f9 = f9 * f12 + f3 * (1.0f - f12);
            f10 = f10 * f12 + f3 * (1.0f - f12);
        }
        if (this.n > 0) {
            f3 = (float)this.n - f2;
            if (f3 > 1.0f) {
                f3 = 1.0f;
            }
            f8 = f8 * (1.0f - (f3 *= 0.45f)) + 0.8f * f3;
            f9 = f9 * (1.0f - f3) + 0.8f * f3;
            f10 = f10 * (1.0f - f3) + 1.0f * f3;
        }
        return bt.b(f8, f9, f10);
    }

    public float b(float f2) {
        return this.t.a(this.x.f(), f2);
    }

    public bt c(float f2) {
        float f3;
        float f4;
        float f5 = this.b(f2);
        float f6 = in.b(f5 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        if (f6 < 0.0f) {
            f6 = 0.0f;
        }
        if (f6 > 1.0f) {
            f6 = 1.0f;
        }
        float f7 = (float)(this.H >> 16 & 0xFFL) / 255.0f;
        float f8 = (float)(this.H >> 8 & 0xFFL) / 255.0f;
        float f9 = (float)(this.H & 0xFFL) / 255.0f;
        float f10 = this.g(f2);
        if (f10 > 0.0f) {
            f4 = (f7 * 0.3f + f8 * 0.59f + f9 * 0.11f) * 0.6f;
            f3 = 1.0f - f10 * 0.95f;
            f7 = f7 * f3 + f4 * (1.0f - f3);
            f8 = f8 * f3 + f4 * (1.0f - f3);
            f9 = f9 * f3 + f4 * (1.0f - f3);
        }
        f7 *= f6 * 0.9f + 0.1f;
        f8 *= f6 * 0.9f + 0.1f;
        f9 *= f6 * 0.85f + 0.15f;
        f4 = this.f(f2);
        if (f4 > 0.0f) {
            f3 = (f7 * 0.3f + f8 * 0.59f + f9 * 0.11f) * 0.2f;
            float f11 = 1.0f - f4 * 0.95f;
            f7 = f7 * f11 + f3 * (1.0f - f11);
            f8 = f8 * f11 + f3 * (1.0f - f11);
            f9 = f9 * f11 + f3 * (1.0f - f11);
        }
        return bt.b(f7, f8, f9);
    }

    public bt d(float f2) {
        float f3 = this.b(f2);
        return this.t.b(f3, f2);
    }

    public int e(int n2, int n3) {
        lm lm2 = this.b(n2, n3);
        n2 &= 0xF;
        n3 &= 0xF;
        for (int i2 = 127; i2 > 0; --i2) {
            ln ln2;
            int n4 = lm2.a(n2, i2, n3);
            ln ln3 = ln2 = n4 == 0 ? ln.a : uu.m[n4].bA;
            if (!ln2.c() && !ln2.d()) {
                continue;
            }
            return i2 + 1;
        }
        return -1;
    }

    public float e(float f2) {
        float f3 = this.b(f2);
        float f4 = 1.0f - (in.b(f3 * (float)Math.PI * 2.0f) * 2.0f + 0.75f);
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        return f4 * f4 * 0.5f;
    }

    public void c(int n2, int n3, int n4, int n5, int n6) {
        qy qy2 = new qy(n2, n3, n4, n5);
        int n7 = 8;
        if (this.a) {
            int n8;
            if (this.a(qy2.a - n7, qy2.b - n7, qy2.c - n7, qy2.a + n7, qy2.b + n7, qy2.c + n7) && (n8 = this.a(qy2.a, qy2.b, qy2.c)) == qy2.d && n8 > 0) {
                uu.m[n8].a(this, qy2.a, qy2.b, qy2.c, this.r);
            }
            return;
        }
        if (this.a(n2 - n7, n3 - n7, n4 - n7, n2 + n7, n3 + n7, n4 + n7)) {
            if (n5 > 0) {
                qy2.a((long)n6 + this.x.f());
            }
            if (!this.F.contains(qy2)) {
                this.F.add(qy2);
                this.E.add(qy2);
            }
        }
    }

    public void g() {
        int n2;
        int n3;
        Object object;
        int n4;
        for (n4 = 0; n4 < this.e.size(); ++n4) {
            object = (sn)this.e.get(n4);
            ((sn)object).w_();
            if (!((sn)object).be) continue;
            this.e.remove(n4--);
        }
        this.b.removeAll(this.D);
        for (n4 = 0; n4 < this.D.size(); ++n4) {
            object = (sn)this.D.get(n4);
            n3 = ((sn)object).bG;
            n2 = ((sn)object).bI;
            if (!((sn)object).bF || !this.f(n3, n2)) continue;
            this.c(n3, n2).b((sn)object);
        }
        for (n4 = 0; n4 < this.D.size(); ++n4) {
            this.d((sn)this.D.get(n4));
        }
        this.D.clear();
        for (n4 = 0; n4 < this.b.size(); ++n4) {
            object = (sn)this.b.get(n4);
            if (((sn)object).aH != null) {
                if (!((sn)object).aH.be && ((sn)object).aH.aG == object) continue;
                ((sn)object).aH.aG = null;
                ((sn)object).aH = null;
            }
            if (!((sn)object).be) {
                this.f((sn)object);
            }
            if (!((sn)object).be) continue;
            n3 = ((sn)object).bG;
            n2 = ((sn)object).bI;
            if (((sn)object).bF && this.f(n3, n2)) {
                this.c(n3, n2).b((sn)object);
            }
            this.b.remove(n4--);
            this.d((sn)object);
        }
        this.L = true;
        Iterator iterator = this.c.iterator();
        while (iterator.hasNext()) {
            object = (ow)iterator.next();
            if (!((ow)object).g()) {
                ((ow)object).n_();
            }
            if (!((ow)object).g()) continue;
            iterator.remove();
            lm lm2 = this.c(((ow)object).e >> 4, ((ow)object).g >> 4);
            if (lm2 == null) continue;
            lm2.e(((ow)object).e & 0xF, ((ow)object).f, ((ow)object).g & 0xF);
        }
        this.L = false;
        if (!this.G.isEmpty()) {
            for (ow ow2 : this.G) {
                lm lm3;
                if (ow2.g()) continue;
                if (!this.c.contains(ow2)) {
                    this.c.add(ow2);
                }
                if ((lm3 = this.c(ow2.e >> 4, ow2.g >> 4)) != null) {
                    lm3.a(ow2.e & 0xF, ow2.f, ow2.g & 0xF, ow2);
                }
                this.j(ow2.e, ow2.f, ow2.g);
            }
            this.G.clear();
        }
    }

    public void a(Collection collection) {
        if (this.L) {
            this.G.addAll(collection);
        } else {
            this.c.addAll(collection);
        }
    }

    public void f(sn sn2) {
        this.a(sn2, true);
    }

    public void a(sn sn2, boolean bl2) {
        int n2 = in.b(sn2.aM);
        int n3 = in.b(sn2.aO);
        int n4 = 32;
        if (bl2 && !this.a(n2 - n4, 0, n3 - n4, n2 + n4, 128, n3 + n4)) {
            return;
        }
        sn2.bl = sn2.aM;
        sn2.bm = sn2.aN;
        sn2.bn = sn2.aO;
        sn2.aU = sn2.aS;
        sn2.aV = sn2.aT;
        if (bl2 && sn2.bF) {
            if (sn2.aH != null) {
                sn2.s_();
            } else {
                sn2.w_();
            }
        }
        if (Double.isNaN(sn2.aM) || Double.isInfinite(sn2.aM)) {
            sn2.aM = sn2.bl;
        }
        if (Double.isNaN(sn2.aN) || Double.isInfinite(sn2.aN)) {
            sn2.aN = sn2.bm;
        }
        if (Double.isNaN(sn2.aO) || Double.isInfinite(sn2.aO)) {
            sn2.aO = sn2.bn;
        }
        if (Double.isNaN(sn2.aT) || Double.isInfinite(sn2.aT)) {
            sn2.aT = sn2.aV;
        }
        if (Double.isNaN(sn2.aS) || Double.isInfinite(sn2.aS)) {
            sn2.aS = sn2.aU;
        }
        int n5 = in.b(sn2.aM / 16.0);
        int n6 = in.b(sn2.aN / 16.0);
        int n7 = in.b(sn2.aO / 16.0);
        if (!sn2.bF || sn2.bG != n5 || sn2.bH != n6 || sn2.bI != n7) {
            if (sn2.bF && this.f(sn2.bG, sn2.bI)) {
                this.c(sn2.bG, sn2.bI).a(sn2, sn2.bH);
            }
            if (this.f(n5, n7)) {
                sn2.bF = true;
                this.c(n5, n7).a(sn2);
            } else {
                sn2.bF = false;
            }
        }
        if (bl2 && sn2.bF && sn2.aG != null) {
            if (sn2.aG.be || sn2.aG.aH != sn2) {
                sn2.aG.aH = null;
                sn2.aG = null;
            } else {
                this.f(sn2.aG);
            }
        }
    }

    public boolean a(eq eq2) {
        List list = this.b(null, eq2);
        for (int i2 = 0; i2 < list.size(); ++i2) {
            sn sn2 = (sn)list.get(i2);
            if (sn2.be || !sn2.aF) continue;
            return false;
        }
        return true;
    }

    public boolean b(eq eq2) {
        int n2 = in.b(eq2.a);
        int n3 = in.b(eq2.d + 1.0);
        int n4 = in.b(eq2.b);
        int n5 = in.b(eq2.e + 1.0);
        int n6 = in.b(eq2.c);
        int n7 = in.b(eq2.f + 1.0);
        if (eq2.a < 0.0) {
            --n2;
        }
        if (eq2.b < 0.0) {
            --n4;
        }
        if (eq2.c < 0.0) {
            --n6;
        }
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n4; i3 < n5; ++i3) {
                for (int i4 = n6; i4 < n7; ++i4) {
                    uu uu2 = uu.m[this.a(i2, i3, i4)];
                    if (uu2 == null || !uu2.bA.d()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean c(eq eq2) {
        int n2;
        int n3 = in.b(eq2.a);
        int n4 = in.b(eq2.d + 1.0);
        int n5 = in.b(eq2.b);
        int n6 = in.b(eq2.e + 1.0);
        int n7 = in.b(eq2.c);
        if (this.a(n3, n5, n7, n4, n6, n2 = in.b(eq2.f + 1.0))) {
            for (int i2 = n3; i2 < n4; ++i2) {
                for (int i3 = n5; i3 < n6; ++i3) {
                    for (int i4 = n7; i4 < n2; ++i4) {
                        int n8 = this.a(i2, i3, i4);
                        if (n8 != uu.as.bn && n8 != uu.D.bn && n8 != uu.E.bn) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean a(eq eq2, ln ln2, sn sn2) {
        int n2;
        int n3 = in.b(eq2.a);
        int n4 = in.b(eq2.d + 1.0);
        int n5 = in.b(eq2.b);
        int n6 = in.b(eq2.e + 1.0);
        int n7 = in.b(eq2.c);
        if (!this.a(n3, n5, n7, n4, n6, n2 = in.b(eq2.f + 1.0))) {
            return false;
        }
        boolean bl2 = false;
        bt bt2 = bt.b(0.0, 0.0, 0.0);
        for (int i2 = n3; i2 < n4; ++i2) {
            for (int i3 = n5; i3 < n6; ++i3) {
                for (int i4 = n7; i4 < n2; ++i4) {
                    double d2;
                    uu uu2 = uu.m[this.a(i2, i3, i4)];
                    if (uu2 == null || uu2.bA != ln2 || !((double)n6 >= (d2 = (double)((float)(i3 + 1) - rp.d(this.e(i2, i3, i4)))))) continue;
                    bl2 = true;
                    uu2.a(this, i2, i3, i4, sn2, bt2);
                }
            }
        }
        if (bt2.d() > 0.0) {
            bt2 = bt2.c();
            double d3 = 0.014;
            sn2.aP += bt2.a * d3;
            sn2.aQ += bt2.b * d3;
            sn2.aR += bt2.c * d3;
        }
        return bl2;
    }

    public boolean a(eq eq2, ln ln2) {
        int n2 = in.b(eq2.a);
        int n3 = in.b(eq2.d + 1.0);
        int n4 = in.b(eq2.b);
        int n5 = in.b(eq2.e + 1.0);
        int n6 = in.b(eq2.c);
        int n7 = in.b(eq2.f + 1.0);
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n4; i3 < n5; ++i3) {
                for (int i4 = n6; i4 < n7; ++i4) {
                    uu uu2 = uu.m[this.a(i2, i3, i4)];
                    if (uu2 == null || uu2.bA != ln2) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean b(eq eq2, ln ln2) {
        int n2 = in.b(eq2.a);
        int n3 = in.b(eq2.d + 1.0);
        int n4 = in.b(eq2.b);
        int n5 = in.b(eq2.e + 1.0);
        int n6 = in.b(eq2.c);
        int n7 = in.b(eq2.f + 1.0);
        for (int i2 = n2; i2 < n3; ++i2) {
            for (int i3 = n4; i3 < n5; ++i3) {
                for (int i4 = n6; i4 < n7; ++i4) {
                    uu uu2 = uu.m[this.a(i2, i3, i4)];
                    if (uu2 == null || uu2.bA != ln2) continue;
                    int n8 = this.e(i2, i3, i4);
                    double d2 = i3 + 1;
                    if (n8 < 8) {
                        d2 = (double)(i3 + 1) - (double)n8 / 8.0;
                    }
                    if (!(d2 >= eq2.b)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public qx a(sn sn2, double d2, double d3, double d4, float f2) {
        return this.a(sn2, d2, d3, d4, f2, false);
    }

    public qx a(sn sn2, double d2, double d3, double d4, float f2, boolean bl2) {
        qx qx2 = new qx(this, sn2, d2, d3, d4, f2);
        qx2.a = bl2;
        qx2.a();
        qx2.a(true);
        return qx2;
    }

    public float a(bt bt2, eq eq2) {
        double d2 = 1.0 / ((eq2.d - eq2.a) * 2.0 + 1.0);
        double d3 = 1.0 / ((eq2.e - eq2.b) * 2.0 + 1.0);
        double d4 = 1.0 / ((eq2.f - eq2.c) * 2.0 + 1.0);
        int n2 = 0;
        int n3 = 0;
        float f2 = 0.0f;
        while (f2 <= 1.0f) {
            float f3 = 0.0f;
            while (f3 <= 1.0f) {
                float f4 = 0.0f;
                while (f4 <= 1.0f) {
                    double d5 = eq2.a + (eq2.d - eq2.a) * (double)f2;
                    double d6 = eq2.b + (eq2.e - eq2.b) * (double)f3;
                    double d7 = eq2.c + (eq2.f - eq2.c) * (double)f4;
                    if (this.a(bt.b(d5, d6, d7), bt2) == null) {
                        ++n2;
                    }
                    ++n3;
                    f4 = (float)((double)f4 + d4);
                }
                f3 = (float)((double)f3 + d3);
            }
            f2 = (float)((double)f2 + d2);
        }
        return (float)n2 / (float)n3;
    }

    public void a(gs gs2, int n2, int n3, int n4, int n5) {
        if (n5 == 0) {
            --n3;
        }
        if (n5 == 1) {
            ++n3;
        }
        if (n5 == 2) {
            --n4;
        }
        if (n5 == 3) {
            ++n4;
        }
        if (n5 == 4) {
            --n2;
        }
        if (n5 == 5) {
            ++n2;
        }
        if (this.a(n2, n3, n4) == uu.as.bn) {
            this.a(gs2, 1004, n2, n3, n4, 0);
            this.f(n2, n3, n4, 0);
        }
    }

    public sn a(Class clazz) {
        return null;
    }

    public String h() {
        return "All: " + this.b.size();
    }

    public String i() {
        return this.v.c();
    }

    public ow b(int n2, int n3, int n4) {
        lm lm2 = this.c(n2 >> 4, n4 >> 4);
        if (lm2 != null) {
            return lm2.d(n2 & 0xF, n3, n4 & 0xF);
        }
        return null;
    }

    public void a(int n2, int n3, int n4, ow ow2) {
        if (!ow2.g()) {
            if (this.L) {
                ow2.e = n2;
                ow2.f = n3;
                ow2.g = n4;
                this.G.add(ow2);
            } else {
                this.c.add(ow2);
                lm lm2 = this.c(n2 >> 4, n4 >> 4);
                if (lm2 != null) {
                    lm2.a(n2 & 0xF, n3, n4 & 0xF, ow2);
                }
            }
        }
    }

    public void p(int n2, int n3, int n4) {
        ow ow2 = this.b(n2, n3, n4);
        if (ow2 != null && this.L) {
            ow2.i();
        } else {
            lm lm2;
            if (ow2 != null) {
                this.c.remove(ow2);
            }
            if ((lm2 = this.c(n2 >> 4, n4 >> 4)) != null) {
                lm2.e(n2 & 0xF, n3, n4 & 0xF);
            }
        }
    }

    public boolean g(int n2, int n3, int n4) {
        uu uu2 = uu.m[this.a(n2, n3, n4)];
        if (uu2 == null) {
            return false;
        }
        return uu2.c();
    }

    public boolean h(int n2, int n3, int n4) {
        uu uu2 = uu.m[this.a(n2, n3, n4)];
        if (uu2 == null) {
            return false;
        }
        return uu2.bA.h() && uu2.d();
    }

    public void a(yb yb2) {
        this.a(true, yb2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean j() {
        if (this.M >= 50) {
            return false;
        }
        ++this.M;
        try {
            int n2 = 500;
            while (this.C.size() > 0) {
                if (--n2 <= 0) {
                    boolean bl2 = true;
                    return bl2;
                }
                ((st)this.C.remove(this.C.size() - 1)).a(this);
            }
            boolean bl3 = false;
            return bl3;
        }
        finally {
            --this.M;
        }
    }

    public void a(eb eb2, int n2, int n3, int n4, int n5, int n6, int n7) {
        this.a(eb2, n2, n3, n4, n5, n6, n7, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void a(eb eb2, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl2) {
        if (this.t.e && eb2 == eb.a) {
            return;
        }
        ++A;
        try {
            int n8;
            if (A == 50) {
                return;
            }
            int n9 = (n5 + n2) / 2;
            int n10 = (n7 + n4) / 2;
            if (!this.i(n9, 64, n10)) {
                return;
            }
            if (this.b(n9, n10).h()) {
                return;
            }
            int n11 = this.C.size();
            if (bl2) {
                n8 = 5;
                if (n8 > n11) {
                    n8 = n11;
                }
                for (int i2 = 0; i2 < n8; ++i2) {
                    st st2 = (st)this.C.get(this.C.size() - i2 - 1);
                    if (st2.a != eb2 || !st2.a(n2, n3, n4, n5, n6, n7)) continue;
                    return;
                }
            }
            this.C.add(new st(eb2, n2, n3, n4, n5, n6, n7));
            n8 = 1000000;
            if (this.C.size() > 1000000) {
                System.out.println("More than " + n8 + " updates, aborting lighting updates");
                this.C.clear();
            }
        }
        finally {
            --A;
        }
    }

    public void k() {
        int n2 = this.a(1.0f);
        if (n2 != this.f) {
            this.f = n2;
        }
    }

    public void a(boolean bl2, boolean bl3) {
        this.N = bl2;
        this.O = bl3;
    }

    public void l() {
        long l2;
        int n2;
        this.m();
        if (this.A()) {
            n2 = 0;
            if (this.N && this.q >= 1) {
                n2 = cq.a(this, this.d);
            }
            if (n2 == 0) {
                l2 = this.x.f() + 24000L;
                this.x.a(l2 - l2 % 24000L);
                this.z();
            }
        }
        cq.a(this, this.N, this.O);
        this.v.a();
        n2 = this.a(1.0f);
        if (n2 != this.f) {
            this.f = n2;
            for (int i2 = 0; i2 < this.u.size(); ++i2) {
                ((pm)this.u.get(i2)).e();
            }
        }
        if ((l2 = this.x.f() + 1L) % (long)this.p == 0L) {
            this.a(false, null);
        }
        this.x.a(l2);
        this.a(false);
        this.n();
    }

    private void E() {
        if (this.x.o()) {
            this.j = 1.0f;
            if (this.x.m()) {
                this.l = 1.0f;
            }
        }
    }

    protected void m() {
        int n2;
        if (this.t.e) {
            return;
        }
        if (this.m > 0) {
            --this.m;
        }
        if ((n2 = this.x.n()) <= 0) {
            if (this.x.m()) {
                this.x.e(this.r.nextInt(12000) + 3600);
            } else {
                this.x.e(this.r.nextInt(168000) + 12000);
            }
        } else {
            this.x.e(--n2);
            if (n2 <= 0) {
                this.x.a(!this.x.m());
            }
        }
        int n3 = this.x.p();
        if (n3 <= 0) {
            if (this.x.o()) {
                this.x.f(this.r.nextInt(12000) + 12000);
            } else {
                this.x.f(this.r.nextInt(168000) + 12000);
            }
        } else {
            this.x.f(--n3);
            if (n3 <= 0) {
                this.x.b(!this.x.o());
            }
        }
        this.i = this.j;
        this.j = this.x.o() ? (float)((double)this.j + 0.01) : (float)((double)this.j - 0.01);
        if (this.j < 0.0f) {
            this.j = 0.0f;
        }
        if (this.j > 1.0f) {
            this.j = 1.0f;
        }
        this.k = this.l;
        this.l = this.x.m() ? (float)((double)this.l + 0.01) : (float)((double)this.l - 0.01);
        if (this.l < 0.0f) {
            this.l = 0.0f;
        }
        if (this.l > 1.0f) {
            this.l = 1.0f;
        }
    }

    private void F() {
        this.x.f(0);
        this.x.b(false);
        this.x.e(0);
        this.x.a(false);
    }

    protected void n() {
        int n2;
        int n3;
        int n4;
        int n5;
        this.P.clear();
        for (int i2 = 0; i2 < this.d.size(); ++i2) {
            Object object = (gs)this.d.get(i2);
            n5 = in.b(((gs)object).aM / 16.0);
            n4 = in.b(((gs)object).aO / 16.0);
            int n6 = 9;
            for (n3 = -n6; n3 <= n6; ++n3) {
                for (n2 = -n6; n2 <= n6; ++n2) {
                    this.P.add(new yy(n3 + n5, n2 + n4));
                }
            }
        }
        if (this.Q > 0) {
            --this.Q;
        }
        for (Object object : this.P) {
            int n7;
            int n8;
            int n9;
            n5 = ((yy)object).a * 16;
            n4 = ((yy)object).b * 16;
            lm lm2 = this.c(((yy)object).a, ((yy)object).b);
            if (this.Q == 0) {
                gs gs2;
                this.g = this.g * 3 + 1013904223;
                n3 = this.g >> 2;
                n2 = n3 & 0xF;
                n9 = n3 >> 8 & 0xF;
                n8 = n3 >> 16 & 0x7F;
                n7 = lm2.a(n2, n8, n9);
                if (n7 == 0 && this.m(n2 += n5, n8, n9 += n4) <= this.r.nextInt(8) && this.a(eb.a, n2, n8, n9) <= 0 && (gs2 = this.a((double)n2 + 0.5, (double)n8 + 0.5, (double)n9 + 0.5, 8.0)) != null && gs2.g((double)n2 + 0.5, (double)n8 + 0.5, (double)n9 + 0.5) > 4.0) {
                    this.a((double)n2 + 0.5, (double)n8 + 0.5, (double)n9 + 0.5, "ambient.cave.cave", 0.7f, 0.8f + this.r.nextFloat() * 0.2f);
                    this.Q = this.r.nextInt(12000) + 6000;
                }
            }
            if (this.r.nextInt(100000) == 0 && this.C() && this.B()) {
                this.g = this.g * 3 + 1013904223;
                n3 = this.g >> 2;
                n2 = n5 + (n3 & 0xF);
                n9 = n4 + (n3 >> 8 & 0xF);
                n8 = this.e(n2, n9);
                if (this.t(n2, n8, n9)) {
                    this.a(new c(this, n2, n8, n9));
                    this.m = 2;
                }
            }
            if (this.r.nextInt(16) == 0) {
                this.g = this.g * 3 + 1013904223;
                n3 = this.g >> 2;
                n2 = n3 & 0xF;
                n9 = n3 >> 8 & 0xF;
                n8 = this.e(n2 + n5, n9 + n4);
                if (this.a().a(n2 + n5, n9 + n4).c() && n8 >= 0 && n8 < 128 && lm2.a(eb.b, n2, n8, n9) < 10) {
                    n7 = lm2.a(n2, n8 - 1, n9);
                    int n10 = lm2.a(n2, n8, n9);
                    if (this.C() && n10 == 0 && uu.aT.a(this, n2 + n5, n8, n9 + n4) && n7 != 0 && n7 != uu.aU.bn && uu.m[n7].bA.c()) {
                        this.f(n2 + n5, n8, n9 + n4, uu.aT.bn);
                    }
                    if (n7 == uu.C.bn && lm2.b(n2, n8 - 1, n9) == 0) {
                        this.f(n2 + n5, n8 - 1, n9 + n4, uu.aU.bn);
                    }
                }
            }
            for (n3 = 0; n3 < 80; ++n3) {
                this.g = this.g * 3 + 1013904223;
                n2 = this.g >> 2;
                n9 = n2 & 0xF;
                n8 = n2 >> 8 & 0xF;
                n7 = n2 >> 16 & 0x7F;
                int n11 = lm2.b[n9 << 11 | n8 << 7 | n7] & 0xFF;
                if (!uu.n[n11]) continue;
                uu.m[n11].a(this, n9 + n5, n7, n8 + n4, this.r);
            }
        }
    }

    public boolean a(boolean bl2) {
        int n2 = this.E.size();
        if (n2 != this.F.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        if (n2 > 1000) {
            n2 = 1000;
        }
        for (int i2 = 0; i2 < n2; ++i2) {
            int n3;
            qy qy2 = (qy)this.E.first();
            if (!bl2 && qy2.e > this.x.f()) break;
            this.E.remove(qy2);
            this.F.remove(qy2);
            int n4 = 8;
            if (!this.a(qy2.a - n4, qy2.b - n4, qy2.c - n4, qy2.a + n4, qy2.b + n4, qy2.c + n4) || (n3 = this.a(qy2.a, qy2.b, qy2.c)) != qy2.d || n3 <= 0) continue;
            uu.m[n3].a(this, qy2.a, qy2.b, qy2.c, this.r);
        }
        return this.E.size() != 0;
    }

    public void q(int n2, int n3, int n4) {
        int n5 = 16;
        Random random = new Random();
        for (int i2 = 0; i2 < 1000; ++i2) {
            int n6;
            int n7;
            int n8 = n2 + this.r.nextInt(n5) - this.r.nextInt(n5);
            int n9 = this.a(n8, n7 = n3 + this.r.nextInt(n5) - this.r.nextInt(n5), n6 = n4 + this.r.nextInt(n5) - this.r.nextInt(n5));
            if (n9 <= 0) continue;
            uu.m[n9].b(this, n8, n7, n6, random);
        }
    }

    public List b(sn sn2, eq eq2) {
        this.R.clear();
        int n2 = in.b((eq2.a - 2.0) / 16.0);
        int n3 = in.b((eq2.d + 2.0) / 16.0);
        int n4 = in.b((eq2.c - 2.0) / 16.0);
        int n5 = in.b((eq2.f + 2.0) / 16.0);
        for (int i2 = n2; i2 <= n3; ++i2) {
            for (int i3 = n4; i3 <= n5; ++i3) {
                if (!this.f(i2, i3)) continue;
                this.c(i2, i3).a(sn2, eq2, this.R);
            }
        }
        return this.R;
    }

    public List a(Class clazz, eq eq2) {
        int n2 = in.b((eq2.a - 2.0) / 16.0);
        int n3 = in.b((eq2.d + 2.0) / 16.0);
        int n4 = in.b((eq2.c - 2.0) / 16.0);
        int n5 = in.b((eq2.f + 2.0) / 16.0);
        ArrayList arrayList = new ArrayList();
        for (int i2 = n2; i2 <= n3; ++i2) {
            for (int i3 = n4; i3 <= n5; ++i3) {
                if (!this.f(i2, i3)) continue;
                this.c(i2, i3).a(clazz, eq2, arrayList);
            }
        }
        return arrayList;
    }

    public List o() {
        return this.b;
    }

    public void b(int n2, int n3, int n4, ow ow2) {
        if (this.i(n2, n3, n4)) {
            this.b(n2, n4).g();
        }
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(n2, n3, n4, ow2);
        }
    }

    public int b(Class clazz) {
        int n2 = 0;
        for (int i2 = 0; i2 < this.b.size(); ++i2) {
            sn sn2 = (sn)this.b.get(i2);
            if (!clazz.isAssignableFrom(sn2.getClass())) continue;
            ++n2;
        }
        return n2;
    }

    public void a(List list) {
        this.b.addAll(list);
        for (int i2 = 0; i2 < list.size(); ++i2) {
            this.c((sn)list.get(i2));
        }
    }

    public void b(List list) {
        this.D.addAll(list);
    }

    public void p() {
        while (this.v.a()) {
        }
    }

    public boolean a(int n2, int n3, int n4, int n5, boolean bl2, int n6) {
        int n7 = this.a(n3, n4, n5);
        uu uu2 = uu.m[n7];
        uu uu3 = uu.m[n2];
        eq eq2 = uu3.e(this, n3, n4, n5);
        if (bl2) {
            eq2 = null;
        }
        if (eq2 != null && !this.a(eq2)) {
            return false;
        }
        if (uu2 == uu.B || uu2 == uu.C || uu2 == uu.D || uu2 == uu.E || uu2 == uu.as || uu2 == uu.aT) {
            uu2 = null;
        }
        return n2 > 0 && uu2 == null && uu3.a(this, n3, n4, n5, n6);
    }

    public dh a(sn sn2, sn sn3, float f2) {
        int n2 = in.b(sn2.aM);
        int n3 = in.b(sn2.aN);
        int n4 = in.b(sn2.aO);
        int n5 = (int)(f2 + 16.0f);
        int n6 = n2 - n5;
        int n7 = n3 - n5;
        int n8 = n4 - n5;
        int n9 = n2 + n5;
        int n10 = n3 + n5;
        int n11 = n4 + n5;
        ew ew2 = new ew(this, n6, n7, n8, n9, n10, n11);
        return new fw(ew2).a(sn2, sn3, f2);
    }

    public dh a(sn sn2, int n2, int n3, int n4, float f2) {
        int n5 = in.b(sn2.aM);
        int n6 = in.b(sn2.aN);
        int n7 = in.b(sn2.aO);
        int n8 = (int)(f2 + 8.0f);
        int n9 = n5 - n8;
        int n10 = n6 - n8;
        int n11 = n7 - n8;
        int n12 = n5 + n8;
        int n13 = n6 + n8;
        int n14 = n7 + n8;
        ew ew2 = new ew(this, n9, n10, n11, n12, n13, n14);
        return new fw(ew2).a(sn2, n2, n3, n4, f2);
    }

    public boolean j(int n2, int n3, int n4, int n5) {
        int n6 = this.a(n2, n3, n4);
        if (n6 == 0) {
            return false;
        }
        return uu.m[n6].d(this, n2, n3, n4, n5);
    }

    public boolean r(int n2, int n3, int n4) {
        if (this.j(n2, n3 - 1, n4, 0)) {
            return true;
        }
        if (this.j(n2, n3 + 1, n4, 1)) {
            return true;
        }
        if (this.j(n2, n3, n4 - 1, 2)) {
            return true;
        }
        if (this.j(n2, n3, n4 + 1, 3)) {
            return true;
        }
        if (this.j(n2 - 1, n3, n4, 4)) {
            return true;
        }
        return this.j(n2 + 1, n3, n4, 5);
    }

    public boolean k(int n2, int n3, int n4, int n5) {
        if (this.h(n2, n3, n4)) {
            return this.r(n2, n3, n4);
        }
        int n6 = this.a(n2, n3, n4);
        if (n6 == 0) {
            return false;
        }
        return uu.m[n6].c((xp)this, n2, n3, n4, n5);
    }

    public boolean s(int n2, int n3, int n4) {
        if (this.k(n2, n3 - 1, n4, 0)) {
            return true;
        }
        if (this.k(n2, n3 + 1, n4, 1)) {
            return true;
        }
        if (this.k(n2, n3, n4 - 1, 2)) {
            return true;
        }
        if (this.k(n2, n3, n4 + 1, 3)) {
            return true;
        }
        if (this.k(n2 - 1, n3, n4, 4)) {
            return true;
        }
        return this.k(n2 + 1, n3, n4, 5);
    }

    public gs a(sn sn2, double d2) {
        return this.a(sn2.aM, sn2.aN, sn2.aO, d2);
    }

    public gs a(double d2, double d3, double d4, double d5) {
        double d6 = -1.0;
        gs gs2 = null;
        for (int i2 = 0; i2 < this.d.size(); ++i2) {
            gs gs3 = (gs)this.d.get(i2);
            double d7 = gs3.g(d2, d3, d4);
            if (!(d5 < 0.0) && !(d7 < d5 * d5) || d6 != -1.0 && !(d7 < d6)) continue;
            d6 = d7;
            gs2 = gs3;
        }
        return gs2;
    }

    public gs a(String string) {
        for (int i2 = 0; i2 < this.d.size(); ++i2) {
            if (!string.equals(((gs)this.d.get((int)i2)).l)) continue;
            return (gs)this.d.get(i2);
        }
        return null;
    }

    public void a(int n2, int n3, int n4, int n5, int n6, int n7, byte[] byArray) {
        int n8 = n2 >> 4;
        int n9 = n4 >> 4;
        int n10 = n2 + n5 - 1 >> 4;
        int n11 = n4 + n7 - 1 >> 4;
        int n12 = 0;
        int n13 = n3;
        int n14 = n3 + n6;
        if (n13 < 0) {
            n13 = 0;
        }
        if (n14 > 128) {
            n14 = 128;
        }
        for (int i2 = n8; i2 <= n10; ++i2) {
            int n15 = n2 - i2 * 16;
            int n16 = n2 + n5 - i2 * 16;
            if (n15 < 0) {
                n15 = 0;
            }
            if (n16 > 16) {
                n16 = 16;
            }
            for (int i3 = n9; i3 <= n11; ++i3) {
                int n17 = n4 - i3 * 16;
                int n18 = n4 + n7 - i3 * 16;
                if (n17 < 0) {
                    n17 = 0;
                }
                if (n18 > 16) {
                    n18 = 16;
                }
                n12 = this.c(i2, i3).a(byArray, n15, n13, n17, n16, n14, n18, n12);
                this.b(i2 * 16 + n15, n13, i3 * 16 + n17, i2 * 16 + n16, n14, i3 * 16 + n18);
            }
        }
    }

    public void q() {
    }

    public void r() {
        this.w.b();
    }

    public void a(long l2) {
        this.x.a(l2);
    }

    public long s() {
        return this.x.b();
    }

    public long t() {
        return this.x.f();
    }

    public br u() {
        return new br(this.x.c(), this.x.d(), this.x.e());
    }

    public void a(br br2) {
        this.x.a(br2.a, br2.b, br2.c);
    }

    public void g(sn sn2) {
        int n2 = in.b(sn2.aM / 16.0);
        int n3 = in.b(sn2.aO / 16.0);
        int n4 = 2;
        for (int i2 = n2 - n4; i2 <= n2 + n4; ++i2) {
            for (int i3 = n3 - n4; i3 <= n3 + n4; ++i3) {
                this.c(i2, i3);
            }
        }
        if (!this.b.contains(sn2)) {
            this.b.add(sn2);
        }
    }

    public boolean a(gs gs2, int n2, int n3, int n4) {
        return true;
    }

    public void a(sn sn2, byte by2) {
    }

    public void v() {
        int n2;
        int n3;
        sn sn2;
        int n4;
        this.b.removeAll(this.D);
        for (n4 = 0; n4 < this.D.size(); ++n4) {
            sn2 = (sn)this.D.get(n4);
            n3 = sn2.bG;
            n2 = sn2.bI;
            if (!sn2.bF || !this.f(n3, n2)) continue;
            this.c(n3, n2).b(sn2);
        }
        for (n4 = 0; n4 < this.D.size(); ++n4) {
            this.d((sn)this.D.get(n4));
        }
        this.D.clear();
        for (n4 = 0; n4 < this.b.size(); ++n4) {
            sn2 = (sn)this.b.get(n4);
            if (sn2.aH != null) {
                if (!sn2.aH.be && sn2.aH.aG == sn2) continue;
                sn2.aH.aG = null;
                sn2.aH = null;
            }
            if (!sn2.be) continue;
            n3 = sn2.bG;
            n2 = sn2.bI;
            if (sn2.bF && this.f(n3, n2)) {
                this.c(n3, n2).b(sn2);
            }
            this.b.remove(n4--);
            this.d(sn2);
        }
    }

    public cl w() {
        return this.v;
    }

    public void d(int n2, int n3, int n4, int n5, int n6) {
        int n7 = this.a(n2, n3, n4);
        if (n7 > 0) {
            uu.m[n7].a(this, n2, n3, n4, n5, n6);
        }
    }

    public ei x() {
        return this.x;
    }

    public void y() {
        this.J = !this.d.isEmpty();
        for (gs gs2 : this.d) {
            if (gs2.N()) continue;
            this.J = false;
            break;
        }
    }

    protected void z() {
        this.J = false;
        for (gs gs2 : this.d) {
            if (!gs2.N()) continue;
            gs2.a(false, false, true);
        }
        this.F();
    }

    public boolean A() {
        if (this.J && !this.B) {
            for (gs gs2 : this.d) {
                if (gs2.O()) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public float f(float f2) {
        return (this.k + (this.l - this.k) * f2) * this.g(f2);
    }

    public float g(float f2) {
        return this.i + (this.j - this.i) * f2;
    }

    public void h(float f2) {
        this.i = f2;
        this.j = f2;
    }

    public boolean B() {
        return (double)this.f(1.0f) > 0.9;
    }

    public boolean C() {
        return (double)this.g(1.0f) > 0.2;
    }

    public boolean t(int n2, int n3, int n4) {
        if (!this.C()) {
            return false;
        }
        if (!this.l(n2, n3, n4)) {
            return false;
        }
        if (this.e(n2, n4) > n3) {
            return false;
        }
        kd kd2 = this.a().a(n2, n4);
        if (kd2.c()) {
            return false;
        }
        return kd2.d();
    }

    public void a(String string, hm hm2) {
        this.z.a(string, hm2);
    }

    public hm a(Class clazz, String string) {
        return this.z.a(clazz, string);
    }

    public int b(String string) {
        return this.z.a(string);
    }

    public void e(int n2, int n3, int n4, int n5, int n6) {
        this.a(null, n2, n3, n4, n5, n6);
    }

    public void a(gs gs2, int n2, int n3, int n4, int n5, int n6) {
        for (int i2 = 0; i2 < this.u.size(); ++i2) {
            ((pm)this.u.get(i2)).a(gs2, n2, n3, n4, n5, n6);
        }
    }
}

