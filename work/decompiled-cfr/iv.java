/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 */
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.lwjgl.input.Mouse;

abstract class iv
extends lg {
    protected int b;
    protected List c;
    protected Comparator d;
    protected int e;
    protected int f;
    final /* synthetic */ dv g;

    protected iv(dv dv2) {
        this.g = dv2;
        super(dv.f(dv2), dv2.c, dv2.d, 32, dv2.d - 64, 20);
        this.b = -1;
        this.e = -1;
        this.f = 0;
        this.a(false);
        this.a(true, 20);
    }

    protected void a(int n2, boolean bl2) {
    }

    protected boolean c_(int n2) {
        return false;
    }

    protected void c() {
        this.g.i();
    }

    protected void a(int n2, int n3, nw nw2) {
        if (!Mouse.isButtonDown((int)0)) {
            this.b = -1;
        }
        if (this.b == 0) {
            dv.a(this.g, n2 + 115 - 18, n3 + 1, 0, 0);
        } else {
            dv.a(this.g, n2 + 115 - 18, n3 + 1, 0, 18);
        }
        if (this.b == 1) {
            dv.a(this.g, n2 + 165 - 18, n3 + 1, 0, 0);
        } else {
            dv.a(this.g, n2 + 165 - 18, n3 + 1, 0, 18);
        }
        if (this.b == 2) {
            dv.a(this.g, n2 + 215 - 18, n3 + 1, 0, 0);
        } else {
            dv.a(this.g, n2 + 215 - 18, n3 + 1, 0, 18);
        }
        if (this.e != -1) {
            int n4 = 79;
            int n5 = 18;
            if (this.e == 1) {
                n4 = 129;
            } else if (this.e == 2) {
                n4 = 179;
            }
            if (this.f == 1) {
                n5 = 36;
            }
            dv.a(this.g, n2 + n4, n3 + 1, n5, 0);
        }
    }

    protected void a(int n2, int n3) {
        this.b = -1;
        if (n2 >= 79 && n2 < 115) {
            this.b = 0;
        } else if (n2 >= 129 && n2 < 165) {
            this.b = 1;
        } else if (n2 >= 179 && n2 < 215) {
            this.b = 2;
        }
        if (this.b >= 0) {
            this.c(this.b);
            dv.g((dv)this.g).B.a("random.click", 1.0f, 1.0f);
        }
    }

    protected final int a() {
        return this.c.size();
    }

    protected final tw b(int n2) {
        return (tw)this.c.get(n2);
    }

    protected abstract String a(int var1);

    protected void a(tw tw2, int n2, int n3, boolean bl2) {
        if (tw2 != null) {
            String string = tw2.a(dv.c(this.g).a(tw2));
            this.g.b(dv.h(this.g), string, n2 - dv.i(this.g).a(string), n3 + 5, bl2 ? 0xFFFFFF : 0x909090);
        } else {
            String string = "-";
            this.g.b(dv.j(this.g), string, n2 - dv.k(this.g).a(string), n3 + 5, bl2 ? 0xFFFFFF : 0x909090);
        }
    }

    protected void b(int n2, int n3) {
        if (n3 < this.h || n3 > this.i) {
            return;
        }
        int n4 = this.c(n2, n3);
        int n5 = this.g.c / 2 - 92 - 16;
        if (n4 >= 0) {
            if (n2 < n5 + 40 || n2 > n5 + 40 + 20) {
                return;
            }
            tw tw2 = this.b(n4);
            this.a(tw2, n2, n3);
        } else {
            String string = "";
            if (n2 >= n5 + 115 - 18 && n2 <= n5 + 115) {
                string = this.a(0);
            } else if (n2 >= n5 + 165 - 18 && n2 <= n5 + 165) {
                string = this.a(1);
            } else if (n2 >= n5 + 215 - 18 && n2 <= n5 + 215) {
                string = this.a(2);
            } else {
                return;
            }
            string = ("" + nh.a().a(string)).trim();
            if (string.length() > 0) {
                int n6 = n2 + 12;
                int n7 = n3 - 12;
                int n8 = dv.l(this.g).a(string);
                dv.a(this.g, n6 - 3, n7 - 3, n6 + n8 + 3, n7 + 8 + 3, -1073741824, -1073741824);
                dv.m(this.g).a(string, n6, n7, -1);
            }
        }
    }

    protected void a(tw tw2, int n2, int n3) {
        if (tw2 == null) {
            return;
        }
        gm gm2 = gm.c[tw2.a()];
        String string = ("" + nh.a().b(gm2.a())).trim();
        if (string.length() > 0) {
            int n4 = n2 + 12;
            int n5 = n3 - 12;
            int n6 = dv.n(this.g).a(string);
            dv.b(this.g, n4 - 3, n5 - 3, n4 + n6 + 3, n5 + 8 + 3, -1073741824, -1073741824);
            dv.o(this.g).a(string, n4, n5, -1);
        }
    }

    protected void c(int n2) {
        if (n2 != this.e) {
            this.e = n2;
            this.f = -1;
        } else if (this.f == -1) {
            this.f = 1;
        } else {
            this.e = -1;
            this.f = 0;
        }
        Collections.sort(this.c, this.d);
    }
}

