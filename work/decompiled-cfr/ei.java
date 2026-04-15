/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class ei {
    private long a;
    private int b;
    private int c;
    private int d;
    private long e;
    private long f;
    private long g;
    private nu h;
    private int i;
    private String j;
    private int k;
    private boolean l;
    private int m;
    private boolean n;
    private int o;

    public ei(nu nu2) {
        this.a = nu2.f("RandomSeed");
        this.b = nu2.e("SpawnX");
        this.c = nu2.e("SpawnY");
        this.d = nu2.e("SpawnZ");
        this.e = nu2.f("Time");
        this.f = nu2.f("LastPlayed");
        this.g = nu2.f("SizeOnDisk");
        this.j = nu2.i("LevelName");
        this.k = nu2.e("version");
        this.m = nu2.e("rainTime");
        this.l = nu2.m("raining");
        this.o = nu2.e("thunderTime");
        this.n = nu2.m("thundering");
        if (nu2.b("Player")) {
            this.h = nu2.k("Player");
            this.i = this.h.e("Dimension");
        }
    }

    public ei(long l2, String string) {
        this.a = l2;
        this.j = string;
    }

    public ei(ei ei2) {
        this.a = ei2.a;
        this.b = ei2.b;
        this.c = ei2.c;
        this.d = ei2.d;
        this.e = ei2.e;
        this.f = ei2.f;
        this.g = ei2.g;
        this.h = ei2.h;
        this.i = ei2.i;
        this.j = ei2.j;
        this.k = ei2.k;
        this.m = ei2.m;
        this.l = ei2.l;
        this.o = ei2.o;
        this.n = ei2.n;
    }

    public nu a() {
        nu nu2 = new nu();
        this.a(nu2, this.h);
        return nu2;
    }

    public nu a(List list) {
        nu nu2 = new nu();
        gs gs2 = null;
        nu nu3 = null;
        if (list.size() > 0) {
            gs2 = (gs)list.get(0);
        }
        if (gs2 != null) {
            nu3 = new nu();
            gs2.d(nu3);
        }
        this.a(nu2, nu3);
        return nu2;
    }

    private void a(nu nu2, nu nu3) {
        nu2.a("RandomSeed", this.a);
        nu2.a("SpawnX", this.b);
        nu2.a("SpawnY", this.c);
        nu2.a("SpawnZ", this.d);
        nu2.a("Time", this.e);
        nu2.a("SizeOnDisk", this.g);
        nu2.a("LastPlayed", System.currentTimeMillis());
        nu2.a("LevelName", this.j);
        nu2.a("version", this.k);
        nu2.a("rainTime", this.m);
        nu2.a("raining", this.l);
        nu2.a("thunderTime", this.o);
        nu2.a("thundering", this.n);
        if (nu3 != null) {
            nu2.a("Player", nu3);
        }
    }

    public long b() {
        return this.a;
    }

    public int c() {
        return this.b;
    }

    public int d() {
        return this.c;
    }

    public int e() {
        return this.d;
    }

    public long f() {
        return this.e;
    }

    public long g() {
        return this.g;
    }

    public nu h() {
        return this.h;
    }

    public int i() {
        return this.i;
    }

    public void a(int n2) {
        this.b = n2;
    }

    public void b(int n2) {
        this.c = n2;
    }

    public void c(int n2) {
        this.d = n2;
    }

    public void a(long l2) {
        this.e = l2;
    }

    public void b(long l2) {
        this.g = l2;
    }

    public void a(nu nu2) {
        this.h = nu2;
    }

    public void a(int n2, int n3, int n4) {
        this.b = n2;
        this.c = n3;
        this.d = n4;
    }

    public String j() {
        return this.j;
    }

    public void a(String string) {
        this.j = string;
    }

    public int k() {
        return this.k;
    }

    public void d(int n2) {
        this.k = n2;
    }

    public long l() {
        return this.f;
    }

    public boolean m() {
        return this.n;
    }

    public void a(boolean bl2) {
        this.n = bl2;
    }

    public int n() {
        return this.o;
    }

    public void e(int n2) {
        this.o = n2;
    }

    public boolean o() {
        return this.l;
    }

    public void b(boolean bl2) {
        this.l = bl2;
    }

    public int p() {
        return this.m;
    }

    public void f(int n2) {
        this.m = n2;
    }
}

