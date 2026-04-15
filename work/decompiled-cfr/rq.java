/*
 * Decompiled with CFR 0.152.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public class rq
extends da {
    private final DateFormat j = new SimpleDateFormat();
    protected da a;
    protected String i = "Select world";
    private boolean l = false;
    private int m;
    private List n;
    private mg o;
    private String p;
    private String q;
    private boolean r;
    private ke s;
    private ke t;
    private ke u;

    public rq(da da2) {
        this.a = da2;
    }

    public void b() {
        nh nh2 = nh.a();
        this.i = nh2.a("selectWorld.title");
        this.p = nh2.a("selectWorld.world");
        this.q = nh2.a("selectWorld.conversion");
        this.l();
        this.o = new mg(this);
        this.o.a(this.e, 4, 5);
        this.k();
    }

    private void l() {
        nl nl2 = this.b.c();
        this.n = nl2.b();
        Collections.sort(this.n);
        this.m = -1;
    }

    protected String c(int n2) {
        return ((vb)this.n.get(n2)).a();
    }

    protected String d(int n2) {
        String string = ((vb)this.n.get(n2)).b();
        if (string == null || in.a(string)) {
            nh nh2 = nh.a();
            string = nh2.a("selectWorld.world") + " " + (n2 + 1);
        }
        return string;
    }

    public void k() {
        nh nh2 = nh.a();
        this.t = new ke(1, this.c / 2 - 154, this.d - 52, 150, 20, nh2.a("selectWorld.select"));
        this.e.add(this.t);
        this.s = new ke(6, this.c / 2 - 154, this.d - 28, 70, 20, nh2.a("selectWorld.rename"));
        this.e.add(this.s);
        this.u = new ke(2, this.c / 2 - 74, this.d - 28, 70, 20, nh2.a("selectWorld.delete"));
        this.e.add(this.u);
        this.e.add(new ke(3, this.c / 2 + 4, this.d - 52, 150, 20, nh2.a("selectWorld.create")));
        this.e.add(new ke(0, this.c / 2 + 4, this.d - 28, 150, 20, nh2.a("gui.cancel")));
        this.t.g = false;
        this.s.g = false;
        this.u.g = false;
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == 2) {
            String string = this.d(this.m);
            if (string != null) {
                this.r = true;
                nh nh2 = nh.a();
                String string2 = nh2.a("selectWorld.deleteQuestion");
                String string3 = "'" + string + "' " + nh2.a("selectWorld.deleteWarning");
                String string4 = nh2.a("selectWorld.deleteButton");
                String string5 = nh2.a("gui.cancel");
                qt qt2 = new qt(this, string2, string3, string4, string5, this.m);
                this.b.a(qt2);
            }
        } else if (ke2.f == 1) {
            this.e(this.m);
        } else if (ke2.f == 3) {
            this.b.a(new fj(this));
        } else if (ke2.f == 6) {
            this.b.a(new jk(this, this.c(this.m)));
        } else if (ke2.f == 0) {
            this.b.a(this.a);
        } else {
            this.o.a(ke2);
        }
    }

    public void e(int n2) {
        this.b.a((da)null);
        if (this.l) {
            return;
        }
        this.l = true;
        this.b.c = new os(this.b);
        String string = this.c(n2);
        if (string == null) {
            string = "World" + n2;
        }
        this.b.a(string, this.d(n2), 0L);
        this.b.a((da)null);
    }

    public void a(boolean bl2, int n2) {
        if (this.r) {
            this.r = false;
            if (bl2) {
                nl nl2 = this.b.c();
                nl2.c();
                nl2.c(this.c(n2));
                this.l();
            }
            this.b.a(this);
        }
    }

    public void a(int n2, int n3, float f2) {
        this.o.a(n2, n3, f2);
        this.a(this.g, this.i, this.c / 2, 20, 0xFFFFFF);
        super.a(n2, n3, f2);
    }

    static /* synthetic */ List a(rq rq2) {
        return rq2.n;
    }

    static /* synthetic */ int a(rq rq2, int n2) {
        rq2.m = n2;
        return rq2.m;
    }

    static /* synthetic */ int b(rq rq2) {
        return rq2.m;
    }

    static /* synthetic */ ke c(rq rq2) {
        return rq2.t;
    }

    static /* synthetic */ ke d(rq rq2) {
        return rq2.s;
    }

    static /* synthetic */ ke e(rq rq2) {
        return rq2.u;
    }

    static /* synthetic */ String f(rq rq2) {
        return rq2.p;
    }

    static /* synthetic */ DateFormat g(rq rq2) {
        return rq2.j;
    }

    static /* synthetic */ String h(rq rq2) {
        return rq2.q;
    }
}

