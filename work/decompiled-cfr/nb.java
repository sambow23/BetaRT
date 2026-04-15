/*
 * Decompiled with CFR 0.152.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;

public class nb
extends ti {
    private boolean d = false;
    private pf e;
    public String a;
    private Minecraft f;
    private mm g;
    private boolean h = false;
    public hc b = new hc(null);
    Random c = new Random();

    public nb(Minecraft minecraft, String string, int n2) {
        this.f = minecraft;
        Socket socket = new Socket(InetAddress.getByName(string), n2);
        this.e = new pf(socket, "Client", this);
    }

    public void a() {
        if (!this.d) {
            this.e.b();
        }
        this.e.a();
    }

    public void a(nz nz2) {
        this.f.c = new xk(this.f, this);
        this.f.I.a(jl.i, 1);
        this.g = new mm(this, nz2.c, nz2.d);
        this.g.B = true;
        this.f.a(this.g);
        this.f.h.m = nz2.d;
        this.f.a(new gg(this));
        this.f.h.aD = nz2.a;
    }

    public void a(nd nd2) {
        double d2 = (double)nd2.b / 32.0;
        double d3 = (double)nd2.c / 32.0;
        double d4 = (double)nd2.d / 32.0;
        hl hl2 = new hl(this.g, d2, d3, d4, new iz(nd2.h, nd2.i, nd2.l));
        hl2.aP = (double)nd2.e / 128.0;
        hl2.aQ = (double)nd2.f / 128.0;
        hl2.aR = (double)nd2.g / 128.0;
        hl2.bJ = nd2.b;
        hl2.bK = nd2.c;
        hl2.bL = nd2.d;
        this.g.a(nd2.a, hl2);
    }

    public void a(so so2) {
        double d2 = (double)so2.b / 32.0;
        double d3 = (double)so2.c / 32.0;
        double d4 = (double)so2.d / 32.0;
        sn sn2 = null;
        if (so2.h == 10) {
            sn2 = new yl(this.g, d2, d3, d4, 0);
        }
        if (so2.h == 11) {
            sn2 = new yl(this.g, d2, d3, d4, 1);
        }
        if (so2.h == 12) {
            sn2 = new yl(this.g, d2, d3, d4, 2);
        }
        if (so2.h == 90) {
            sn2 = new lx(this.g, d2, d3, d4);
        }
        if (so2.h == 60) {
            sn2 = new sl(this.g, d2, d3, d4);
        }
        if (so2.h == 61) {
            sn2 = new by(this.g, d2, d3, d4);
        }
        if (so2.h == 63) {
            sn2 = new cf(this.g, d2, d3, d4, (double)so2.e / 8000.0, (double)so2.f / 8000.0, (double)so2.g / 8000.0);
            so2.i = 0;
        }
        if (so2.h == 62) {
            sn2 = new vv(this.g, d2, d3, d4);
        }
        if (so2.h == 1) {
            sn2 = new fz(this.g, d2, d3, d4);
        }
        if (so2.h == 50) {
            sn2 = new qw(this.g, d2, d3, d4);
        }
        if (so2.h == 70) {
            sn2 = new ju(this.g, d2, d3, d4, uu.F.bn);
        }
        if (so2.h == 71) {
            sn2 = new ju(this.g, d2, d3, d4, uu.G.bn);
        }
        if (sn2 != null) {
            sn2.bJ = so2.b;
            sn2.bK = so2.c;
            sn2.bL = so2.d;
            sn2.aS = 0.0f;
            sn2.aT = 0.0f;
            sn2.aD = so2.a;
            this.g.a(so2.a, sn2);
            if (so2.i > 0) {
                sn sn3;
                if (so2.h == 60 && (sn3 = this.a(so2.i)) instanceof ls) {
                    ((sl)sn2).c = (ls)sn3;
                }
                sn2.a((double)so2.e / 8000.0, (double)so2.f / 8000.0, (double)so2.g / 8000.0);
            }
        }
    }

    public void a(fa fa2) {
        double d2 = (double)fa2.b / 32.0;
        double d3 = (double)fa2.c / 32.0;
        double d4 = (double)fa2.d / 32.0;
        c c2 = null;
        if (fa2.e == 1) {
            c2 = new c(this.g, d2, d3, d4);
        }
        if (c2 != null) {
            c2.bJ = fa2.b;
            c2.bK = fa2.c;
            c2.bL = fa2.d;
            c2.aS = 0.0f;
            c2.aT = 0.0f;
            c2.aD = fa2.a;
            this.g.a(c2);
        }
    }

    public void a(vt vt2) {
        qv qv2 = new qv(this.g, vt2.b, vt2.c, vt2.d, vt2.e, vt2.f);
        this.g.a(vt2.a, qv2);
    }

    public void a(gj gj2) {
        sn sn2 = this.a(gj2.a);
        if (sn2 == null) {
            return;
        }
        sn2.a((double)gj2.b / 8000.0, (double)gj2.c / 8000.0, (double)gj2.d / 8000.0);
    }

    public void a(ux ux2) {
        sn sn2 = this.a(ux2.a);
        if (sn2 != null && ux2.b() != null) {
            sn2.ad().a(ux2.b());
        }
    }

    public void a(mf mf2) {
        double d2 = (double)mf2.c / 32.0;
        double d3 = (double)mf2.d / 32.0;
        double d4 = (double)mf2.e / 32.0;
        float f2 = (float)(mf2.f * 360) / 256.0f;
        float f3 = (float)(mf2.g * 360) / 256.0f;
        xz xz2 = new xz(this.f.f, mf2.b);
        xz2.bJ = mf2.c;
        xz2.aJ = xz2.bl = (double)xz2.bJ;
        xz2.bK = mf2.d;
        xz2.aK = xz2.bm = (double)xz2.bK;
        xz2.bL = mf2.e;
        xz2.aL = xz2.bn = (double)xz2.bL;
        int n2 = mf2.h;
        xz2.c.a[xz2.c.c] = n2 == 0 ? null : new iz(n2, 1, 0);
        xz2.b(d2, d3, d4, f2, f3);
        this.g.a(mf2.a, xz2);
    }

    public void a(rg rg2) {
        sn sn2 = this.a(rg2.a);
        if (sn2 == null) {
            return;
        }
        sn2.bJ = rg2.b;
        sn2.bK = rg2.c;
        sn2.bL = rg2.d;
        double d2 = (double)sn2.bJ / 32.0;
        double d3 = (double)sn2.bK / 32.0 + 0.015625;
        double d4 = (double)sn2.bL / 32.0;
        float f2 = (float)(rg2.e * 360) / 256.0f;
        float f3 = (float)(rg2.f * 360) / 256.0f;
        sn2.a(d2, d3, d4, f2, f3, 3);
    }

    public void a(uh uh2) {
        sn sn2 = this.a(uh2.a);
        if (sn2 == null) {
            return;
        }
        sn2.bJ += uh2.b;
        sn2.bK += uh2.c;
        sn2.bL += uh2.d;
        double d2 = (double)sn2.bJ / 32.0;
        double d3 = (double)sn2.bK / 32.0;
        double d4 = (double)sn2.bL / 32.0;
        float f2 = uh2.g ? (float)(uh2.e * 360) / 256.0f : sn2.aS;
        float f3 = uh2.g ? (float)(uh2.f * 360) / 256.0f : sn2.aT;
        sn2.a(d2, d3, d4, f2, f3, 3);
    }

    public void a(rv rv2) {
        this.g.c(rv2.a);
    }

    public void a(ig ig2) {
        dc dc2 = this.f.h;
        double d2 = dc2.aM;
        double d3 = dc2.aN;
        double d4 = dc2.aO;
        float f2 = dc2.aS;
        float f3 = dc2.aT;
        if (ig2.h) {
            d2 = ig2.a;
            d3 = ig2.b;
            d4 = ig2.c;
        }
        if (ig2.i) {
            f2 = ig2.e;
            f3 = ig2.f;
        }
        dc2.bo = 0.0f;
        dc2.aR = 0.0;
        dc2.aQ = 0.0;
        dc2.aP = 0.0;
        dc2.b(d2, d3, d4, f2, f3);
        ig2.a = dc2.aM;
        ig2.b = dc2.aW.b;
        ig2.c = dc2.aO;
        ig2.d = dc2.aN;
        this.e.a(ig2);
        if (!this.h) {
            this.f.h.aJ = this.f.h.aM;
            this.f.h.aK = this.f.h.aN;
            this.f.h.aL = this.f.h.aO;
            this.h = true;
            this.f.a((da)null);
        }
    }

    public void a(se se2) {
        this.g.a(se2.a, se2.b, se2.c);
    }

    public void a(wu wu2) {
        lm lm2 = this.g.c(wu2.a, wu2.b);
        int n2 = wu2.a * 16;
        int n3 = wu2.b * 16;
        for (int i2 = 0; i2 < wu2.f; ++i2) {
            short s2 = wu2.c[i2];
            int n4 = wu2.d[i2] & 0xFF;
            byte by2 = wu2.e[i2];
            int n5 = s2 >> 12 & 0xF;
            int n6 = s2 >> 8 & 0xF;
            int n7 = s2 & 0xFF;
            lm2.a(n5, n7, n6, n4, (int)by2);
            this.g.c(n5 + n2, n7, n6 + n3, n5 + n2, n7, n6 + n3);
            this.g.b(n5 + n2, n7, n6 + n3, n5 + n2, n7, n6 + n3);
        }
    }

    public void a(ef ef2) {
        this.g.c(ef2.a, ef2.b, ef2.c, ef2.a + ef2.d - 1, ef2.b + ef2.e - 1, ef2.c + ef2.f - 1);
        this.g.a(ef2.a, ef2.b, ef2.c, ef2.d, ef2.e, ef2.f, ef2.g);
    }

    public void a(tv tv2) {
        this.g.f(tv2.a, tv2.b, tv2.c, tv2.d, tv2.e);
    }

    public void a(yr yr2) {
        this.e.a("disconnect.kicked", new Object[0]);
        this.d = true;
        this.f.a((fd)null);
        this.f.a(new ex("disconnect.disconnected", "disconnect.genericReason", yr2.a));
    }

    public void a(String string, Object[] objectArray) {
        if (this.d) {
            return;
        }
        this.d = true;
        this.f.a((fd)null);
        this.f.a(new ex("disconnect.lost", string, objectArray));
    }

    public void a(ki ki2) {
        if (this.d) {
            return;
        }
        this.e.a(ki2);
        this.e.c();
    }

    public void b(ki ki2) {
        if (this.d) {
            return;
        }
        this.e.a(ki2);
    }

    public void a(di di2) {
        sn sn2 = this.a(di2.a);
        ls ls2 = (ls)this.a(di2.b);
        if (ls2 == null) {
            ls2 = this.f.h;
        }
        if (sn2 != null) {
            this.g.a(sn2, "random.pop", 0.2f, ((this.c.nextFloat() - this.c.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            this.f.j.a(new em(this.f.f, sn2, ls2, -0.5f));
            this.g.c(di2.a);
        }
    }

    public void a(pe pe2) {
        this.f.v.a(pe2.a);
    }

    public void a(nm nm2) {
        sn sn2 = this.a(nm2.a);
        if (sn2 == null) {
            return;
        }
        if (nm2.b == 1) {
            gs gs2 = (gs)sn2;
            gs2.J();
        } else if (nm2.b == 2) {
            sn2.h();
        } else if (nm2.b == 3) {
            gs gs3 = (gs)sn2;
            gs3.a(false, false, false);
        } else if (nm2.b == 4) {
            gs gs4 = (gs)sn2;
            gs4.v();
        }
    }

    public void a(jz jz2) {
        sn sn2 = this.a(jz2.a);
        if (sn2 == null) {
            return;
        }
        if (jz2.e == 0) {
            gs gs2 = (gs)sn2;
            gs2.b(jz2.b, jz2.c, jz2.d);
        }
    }

    public void a(mp mp2) {
        if (mp2.a.equals("-")) {
            this.b(new nz(this.f.k.b, 14));
        } else {
            try {
                URL uRL = new URL("http://www.minecraft.net/game/joinserver.jsp?user=" + this.f.k.b + "&sessionId=" + this.f.k.c + "&serverId=" + mp2.a);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(uRL.openStream()));
                String string = bufferedReader.readLine();
                bufferedReader.close();
                if (string.equalsIgnoreCase("ok")) {
                    this.b(new nz(this.f.k.b, 14));
                } else {
                    this.e.a("disconnect.loginFailedInfo", string);
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
                this.e.a("disconnect.genericReason", "Internal client error: " + exception.toString());
            }
        }
    }

    public void b() {
        this.d = true;
        this.e.a();
        this.e.a("disconnect.closed", new Object[0]);
    }

    public void a(jm jm2) {
        double d2 = (double)jm2.c / 32.0;
        double d3 = (double)jm2.d / 32.0;
        double d4 = (double)jm2.e / 32.0;
        float f2 = (float)(jm2.f * 360) / 256.0f;
        float f3 = (float)(jm2.g * 360) / 256.0f;
        ls ls2 = (ls)jc.a(jm2.b, this.f.f);
        ls2.bJ = jm2.c;
        ls2.bK = jm2.d;
        ls2.bL = jm2.e;
        ls2.aD = jm2.a;
        ls2.b(d2, d3, d4, f2, f3);
        ls2.V = true;
        this.g.a(jm2.a, ls2);
        List list = jm2.b();
        if (list != null) {
            ls2.ad().a(list);
        }
    }

    public void a(hg hg2) {
        this.f.f.a(hg2.a);
    }

    public void a(rc rc2) {
        this.f.h.a(new br(rc2.a, rc2.b, rc2.c));
        this.f.f.x().a(rc2.a, rc2.b, rc2.c);
    }

    public void a(ns ns2) {
        sn sn2 = this.a(ns2.a);
        sn sn3 = this.a(ns2.b);
        if (ns2.a == this.f.h.aD) {
            sn2 = this.f.h;
        }
        if (sn2 == null) {
            return;
        }
        sn2.i(sn3);
    }

    public void a(jf jf2) {
        sn sn2 = this.a(jf2.a);
        if (sn2 != null) {
            sn2.a(jf2.b);
        }
    }

    private sn a(int n2) {
        if (n2 == this.f.h.aD) {
            return this.f.h;
        }
        return this.g.b(n2);
    }

    public void a(eu eu2) {
        this.f.h.d_(eu2.a);
    }

    public void a(ox ox2) {
        if (ox2.a != this.f.h.m) {
            this.h = false;
            this.g = new mm(this, this.g.x().b(), ox2.a);
            this.g.B = true;
            this.f.a(this.g);
            this.f.h.m = ox2.a;
            this.f.a(new gg(this));
        }
        this.f.a(true, (int)ox2.a);
    }

    public void a(rm rm2) {
        qx qx2 = new qx(this.f.f, null, rm2.a, rm2.b, rm2.c, rm2.d);
        qx2.g = rm2.e;
        qx2.a(true);
    }

    public void a(iw iw2) {
        if (iw2.b == 0) {
            qo qo2 = new qo(iw2.c, iw2.d);
            this.f.h.a(qo2);
            this.f.h.e.f = iw2.a;
        } else if (iw2.b == 2) {
            sk sk2 = new sk();
            this.f.h.a(sk2);
            this.f.h.e.f = iw2.a;
        } else if (iw2.b == 3) {
            az az2 = new az();
            this.f.h.a(az2);
            this.f.h.e.f = iw2.a;
        } else if (iw2.b == 1) {
            dc dc2 = this.f.h;
            this.f.h.a(in.b(dc2.aM), in.b(dc2.aN), in.b(dc2.aO));
            this.f.h.e.f = iw2.a;
        }
    }

    public void a(hq hq2) {
        if (hq2.a == -1) {
            this.f.h.c.b(hq2.c);
        } else if (hq2.a == 0 && hq2.b >= 36 && hq2.b < 45) {
            iz iz2 = this.f.h.d.b(hq2.b).a();
            if (hq2.c != null && (iz2 == null || iz2.a < hq2.c.a)) {
                hq2.c.b = 5;
            }
            this.f.h.d.a(hq2.b, hq2.c);
        } else if (hq2.a == this.f.h.e.f) {
            this.f.h.e.a(hq2.b, hq2.c);
        }
    }

    public void a(oj oj2) {
        dw dw2 = null;
        if (oj2.a == 0) {
            dw2 = this.f.h.d;
        } else if (oj2.a == this.f.h.e.f) {
            dw2 = this.f.h.e;
        }
        if (dw2 != null) {
            if (oj2.c) {
                dw2.a(oj2.b);
            } else {
                dw2.b(oj2.b);
                this.b(new oj(oj2.a, oj2.b, true));
            }
        }
    }

    public void a(kb kb2) {
        if (kb2.a == 0) {
            this.f.h.d.a(kb2.b);
        } else if (kb2.a == this.f.h.e.f) {
            this.f.h.e.a(kb2.b);
        }
    }

    public void a(ui ui2) {
        ow ow2;
        if (this.f.f.i(ui2.a, ui2.b, ui2.c) && (ow2 = this.f.f.b(ui2.a, ui2.b, ui2.c)) instanceof yk) {
            yk yk2 = (yk)ow2;
            for (int i2 = 0; i2 < 4; ++i2) {
                yk2.a[i2] = ui2.d[i2];
            }
            yk2.y_();
        }
    }

    public void a(mv mv2) {
        this.c(mv2);
        if (this.f.h.e != null && this.f.h.e.f == mv2.a) {
            this.f.h.e.a(mv2.b, mv2.c);
        }
    }

    public void a(s s2) {
        sn sn2 = this.a(s2.a);
        if (sn2 != null) {
            sn2.c(s2.b, s2.c, s2.d);
        }
    }

    public void a(mn mn2) {
        this.f.h.r();
    }

    public void a(vw vw2) {
        this.f.f.d(vw2.a, vw2.b, vw2.c, vw2.d, vw2.e);
    }

    public void a(ca ca2) {
        int n2 = ca2.b;
        if (n2 >= 0 && n2 < ca.a.length && ca.a[n2] != null) {
            this.f.h.b(ca.a[n2]);
        }
        if (n2 == 1) {
            this.g.x().b(true);
            this.g.h(1.0f);
        } else if (n2 == 2) {
            this.g.x().b(false);
            this.g.h(0.0f);
        }
    }

    public void a(ai ai2) {
        if (ai2.a == gm.bb.bf) {
            wr.a(ai2.b, this.f.f).a(ai2.c);
        } else {
            System.out.println("Unknown itemid: " + ai2.b);
        }
    }

    public void a(fn fn2) {
        this.f.f.e(fn2.a, fn2.c, fn2.d, fn2.e, fn2.b);
    }

    public void a(of of2) {
        ((tk)this.f.h).b(jl.a(of2.a), of2.b);
    }

    public boolean c() {
        return false;
    }
}

