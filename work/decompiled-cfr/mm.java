/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class mm
extends fd {
    private LinkedList C = new LinkedList();
    private nb D;
    private uv E;
    private jx F = new jx();
    private Set G = new HashSet();
    private Set H = new HashSet();

    public mm(nb nb2, long l2, int n2) {
        super((wt)new wy(), "MpServer", xa.a(n2), l2);
        this.D = nb2;
        this.a(new br(8, 64, 8));
        this.z = nb2.b;
    }

    public void l() {
        Object object;
        int n2;
        this.a(this.t() + 1L);
        int n3 = this.a(1.0f);
        if (n3 != this.f) {
            this.f = n3;
            for (n2 = 0; n2 < this.u.size(); ++n2) {
                ((pm)this.u.get(n2)).e();
            }
        }
        for (n2 = 0; n2 < 10 && !this.H.isEmpty(); ++n2) {
            object = (sn)this.H.iterator().next();
            if (this.b.contains(object)) continue;
            this.b((sn)object);
        }
        this.D.a();
        for (n2 = 0; n2 < this.C.size(); ++n2) {
            object = (tm)this.C.get(n2);
            if (--((tm)object).d != 0) continue;
            super.a(((tm)object).a, ((tm)object).b, ((tm)object).c, ((tm)object).e, ((tm)object).f);
            super.j(((tm)object).a, ((tm)object).b, ((tm)object).c);
            this.C.remove(n2--);
        }
    }

    public void c(int n2, int n3, int n4, int n5, int n6, int n7) {
        for (int i2 = 0; i2 < this.C.size(); ++i2) {
            tm tm2 = (tm)this.C.get(i2);
            if (tm2.a < n2 || tm2.b < n3 || tm2.c < n4 || tm2.a > n5 || tm2.b > n6 || tm2.c > n7) continue;
            this.C.remove(i2--);
        }
    }

    protected cl b() {
        this.E = new uv(this);
        return this.E;
    }

    public void d() {
        this.a(new br(8, 64, 8));
    }

    protected void n() {
    }

    public void c(int n2, int n3, int n4, int n5, int n6) {
    }

    public boolean a(boolean bl2) {
        return false;
    }

    public void a(int n2, int n3, boolean bl2) {
        if (bl2) {
            this.E.c(n2, n3);
        } else {
            this.E.d(n2, n3);
        }
        if (!bl2) {
            this.b(n2 * 16, 0, n3 * 16, n2 * 16 + 15, 128, n3 * 16 + 15);
        }
    }

    public boolean b(sn sn2) {
        boolean bl2 = super.b(sn2);
        this.G.add(sn2);
        if (!bl2) {
            this.H.add(sn2);
        }
        return bl2;
    }

    public void e(sn sn2) {
        super.e(sn2);
        this.G.remove(sn2);
    }

    protected void c(sn sn2) {
        super.c(sn2);
        if (this.H.contains(sn2)) {
            this.H.remove(sn2);
        }
    }

    protected void d(sn sn2) {
        super.d(sn2);
        if (this.G.contains(sn2)) {
            this.H.add(sn2);
        }
    }

    public void a(int n2, sn sn2) {
        sn sn3 = this.b(n2);
        if (sn3 != null) {
            this.e(sn3);
        }
        this.G.add(sn2);
        sn2.aD = n2;
        if (!this.b(sn2)) {
            this.H.add(sn2);
        }
        this.F.a(n2, sn2);
    }

    public sn b(int n2) {
        return (sn)this.F.a(n2);
    }

    public sn c(int n2) {
        sn sn2 = (sn)this.F.b(n2);
        if (sn2 != null) {
            this.G.remove(sn2);
            this.e(sn2);
        }
        return sn2;
    }

    public boolean e(int n2, int n3, int n4, int n5) {
        int n6 = this.a(n2, n3, n4);
        int n7 = this.e(n2, n3, n4);
        if (super.e(n2, n3, n4, n5)) {
            this.C.add(new tm(this, n2, n3, n4, n6, n7));
            return true;
        }
        return false;
    }

    public boolean a(int n2, int n3, int n4, int n5, int n6) {
        int n7 = this.a(n2, n3, n4);
        int n8 = this.e(n2, n3, n4);
        if (super.a(n2, n3, n4, n5, n6)) {
            this.C.add(new tm(this, n2, n3, n4, n7, n8));
            return true;
        }
        return false;
    }

    public boolean c(int n2, int n3, int n4, int n5) {
        int n6 = this.a(n2, n3, n4);
        int n7 = this.e(n2, n3, n4);
        if (super.c(n2, n3, n4, n5)) {
            this.C.add(new tm(this, n2, n3, n4, n6, n7));
            return true;
        }
        return false;
    }

    public boolean f(int n2, int n3, int n4, int n5, int n6) {
        this.c(n2, n3, n4, n2, n3, n4);
        if (super.a(n2, n3, n4, n5, n6)) {
            this.g(n2, n3, n4, n5);
            return true;
        }
        return false;
    }

    public void q() {
        this.D.a((ki)new yr("Quitting"));
    }

    protected void m() {
        if (this.t.e) {
            return;
        }
        if (this.m > 0) {
            --this.m;
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
}

