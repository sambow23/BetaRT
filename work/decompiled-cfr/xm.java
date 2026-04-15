/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
import java.util.Random;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class xm
extends da {
    private static final int s = ep.a * 24 - 112;
    private static final int t = ep.b * 24 - 112;
    private static final int u = ep.c * 24 - 77;
    private static final int v = ep.d * 24 - 77;
    protected int a = 256;
    protected int i = 202;
    protected int j = 0;
    protected int l = 0;
    protected double m;
    protected double n;
    protected double o;
    protected double p;
    protected double q;
    protected double r;
    private int w = 0;
    private xi x;

    public xm(xi xi2) {
        this.x = xi2;
        int n2 = 141;
        int n3 = 141;
        this.o = this.q = (double)(ep.f.a * 24 - n2 / 2 - 12);
        this.m = this.q;
        this.p = this.r = (double)(ep.f.b * 24 - n3 / 2);
        this.n = this.r;
    }

    public void b() {
        this.e.clear();
        this.e.add(new ab(1, this.c / 2 + 24, this.d / 2 + 74, 80, 20, do.a("gui.done")));
    }

    protected void a(ke ke2) {
        if (ke2.f == 1) {
            this.b.a((da)null);
            this.b.g();
        }
        super.a(ke2);
    }

    protected void a(char c2, int n2) {
        if (n2 == this.b.z.r.b) {
            this.b.a((da)null);
            this.b.g();
        } else {
            super.a(c2, n2);
        }
    }

    public void a(int n2, int n3, float f2) {
        if (Mouse.isButtonDown((int)0)) {
            int n4 = (this.c - this.a) / 2;
            int n5 = (this.d - this.i) / 2;
            int n6 = n4 + 8;
            int n7 = n5 + 17;
            if ((this.w == 0 || this.w == 1) && n2 >= n6 && n2 < n6 + 224 && n3 >= n7 && n3 < n7 + 155) {
                if (this.w == 0) {
                    this.w = 1;
                } else {
                    this.o -= (double)(n2 - this.j);
                    this.p -= (double)(n3 - this.l);
                    this.q = this.m = this.o;
                    this.r = this.n = this.p;
                }
                this.j = n2;
                this.l = n3;
            }
            if (this.q < (double)s) {
                this.q = s;
            }
            if (this.r < (double)t) {
                this.r = t;
            }
            if (this.q >= (double)u) {
                this.q = u - 1;
            }
            if (this.r >= (double)v) {
                this.r = v - 1;
            }
        } else {
            this.w = 0;
        }
        this.i();
        this.b(n2, n3, f2);
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2929);
        this.k();
        GL11.glEnable((int)2896);
        GL11.glEnable((int)2929);
    }

    public void a() {
        this.m = this.o;
        this.n = this.p;
        double d2 = this.q - this.o;
        double d3 = this.r - this.p;
        if (d2 * d2 + d3 * d3 < 4.0) {
            this.o += d2;
            this.p += d3;
        } else {
            this.o += d2 * 0.85;
            this.p += d3 * 0.85;
        }
    }

    protected void k() {
        int n2 = (this.c - this.a) / 2;
        int n3 = (this.d - this.i) / 2;
        this.g.b("Achievements", n2 + 15, n3 + 5, 0x404040);
    }

    protected void b(int n2, int n3, float f2) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8 = in.b(this.m + (this.o - this.m) * (double)f2);
        int n9 = in.b(this.n + (this.p - this.n) * (double)f2);
        if (n8 < s) {
            n8 = s;
        }
        if (n9 < t) {
            n9 = t;
        }
        if (n8 >= u) {
            n8 = u - 1;
        }
        if (n9 >= v) {
            n9 = v - 1;
        }
        int n10 = this.b.p.b("/terrain.png");
        int n11 = this.b.p.b("/achievement/bg.png");
        int n12 = (this.c - this.a) / 2;
        int n13 = (this.d - this.i) / 2;
        int n14 = n12 + 16;
        int n15 = n13 + 17;
        this.k = 0.0f;
        GL11.glDepthFunc((int)518);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-200.0f);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2896);
        GL11.glEnable((int)32826);
        GL11.glEnable((int)2903);
        this.b.p.b(n10);
        int n16 = n8 + 288 >> 4;
        int n17 = n9 + 288 >> 4;
        int n18 = (n8 + 288) % 16;
        int n19 = (n9 + 288) % 16;
        Random random = new Random();
        int n20 = 0;
        while (n20 * 16 - n19 < 155) {
            float f3 = 0.6f - (float)(n17 + n20) / 25.0f * 0.3f;
            GL11.glColor4f((float)f3, (float)f3, (float)f3, (float)1.0f);
            int n21 = 0;
            while (n21 * 16 - n18 < 224) {
                random.setSeed(1234 + n16 + n21);
                random.nextInt();
                int n22 = random.nextInt(1 + n17 + n20) + (n17 + n20) / 2;
                int n23 = uu.F.bm;
                if (n22 > 37 || n17 + n20 == 35) {
                    n23 = uu.A.bm;
                } else if (n22 == 22) {
                    n23 = random.nextInt(2) == 0 ? uu.ax.bm : uu.aO.bm;
                } else if (n22 == 10) {
                    n23 = uu.I.bm;
                } else if (n22 == 8) {
                    n23 = uu.J.bm;
                } else if (n22 > 4) {
                    n23 = uu.u.bm;
                } else if (n22 > 0) {
                    n23 = uu.w.bm;
                }
                this.b(n14 + n21 * 16 - n18, n15 + n20 * 16 - n19, n23 % 16 << 4, n23 >> 4 << 4, 16, 16);
                ++n21;
            }
            ++n20;
        }
        GL11.glEnable((int)2929);
        GL11.glDepthFunc((int)515);
        GL11.glDisable((int)3553);
        for (n16 = 0; n16 < ep.e.size(); ++n16) {
            int n24;
            ny ny2 = (ny)ep.e.get(n16);
            if (ny2.c == null) continue;
            n18 = ny2.a * 24 - n8 + 11 + n14;
            n19 = ny2.b * 24 - n9 + 11 + n15;
            n7 = ny2.c.a * 24 - n8 + 11 + n14;
            n6 = ny2.c.b * 24 - n9 + 11 + n15;
            n5 = 0;
            n4 = this.x.a(ny2);
            boolean bl2 = this.x.b(ny2);
            int n25 = n24 = Math.sin((double)(System.currentTimeMillis() % 600L) / 600.0 * Math.PI * 2.0) > 0.6 ? 255 : 130;
            n5 = n4 != 0 ? -9408400 : (bl2 ? 65280 + (n24 << 24) : -16777216);
            this.a(n18, n7, n19, n5);
            this.b(n7, n19, n6, n5);
        }
        ny ny3 = null;
        bb bb2 = new bb();
        GL11.glPushMatrix();
        GL11.glRotatef((float)180.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        u.b();
        GL11.glPopMatrix();
        GL11.glDisable((int)2896);
        GL11.glEnable((int)32826);
        GL11.glEnable((int)2903);
        for (n18 = 0; n18 < ep.e.size(); ++n18) {
            float f4;
            ny ny4 = (ny)ep.e.get(n18);
            n7 = ny4.a * 24 - n8;
            n6 = ny4.b * 24 - n9;
            if (n7 < -24 || n6 < -24 || n7 > 224 || n6 > 155) continue;
            if (this.x.a(ny4)) {
                f4 = 1.0f;
                GL11.glColor4f((float)f4, (float)f4, (float)f4, (float)1.0f);
            } else if (this.x.b(ny4)) {
                f4 = Math.sin((double)(System.currentTimeMillis() % 600L) / 600.0 * Math.PI * 2.0) < 0.6 ? 0.6f : 0.8f;
                GL11.glColor4f((float)f4, (float)f4, (float)f4, (float)1.0f);
            } else {
                f4 = 0.3f;
                GL11.glColor4f((float)f4, (float)f4, (float)f4, (float)1.0f);
            }
            this.b.p.b(n11);
            n5 = n14 + n7;
            n4 = n15 + n6;
            if (ny4.f()) {
                this.b(n5 - 2, n4 - 2, 26, 202, 26, 26);
            } else {
                this.b(n5 - 2, n4 - 2, 0, 202, 26, 26);
            }
            if (!this.x.b(ny4)) {
                float f5 = 0.1f;
                GL11.glColor4f((float)f5, (float)f5, (float)f5, (float)1.0f);
                bb2.a = false;
            }
            GL11.glEnable((int)2896);
            GL11.glEnable((int)2884);
            bb2.a(this.b.q, this.b.p, ny4.d, n5 + 3, n4 + 3);
            GL11.glDisable((int)2896);
            if (!this.x.b(ny4)) {
                bb2.a = true;
            }
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            if (n2 < n14 || n3 < n15 || n2 >= n14 + 224 || n3 >= n15 + 155 || n2 < n5 || n2 > n5 + 22 || n3 < n4 || n3 > n4 + 22) continue;
            ny3 = ny4;
        }
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b.p.b(n11);
        this.b(n12, n13, 0, 0, this.a, this.i);
        GL11.glPopMatrix();
        this.k = 0.0f;
        GL11.glDepthFunc((int)515);
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3553);
        super.a(n2, n3, f2);
        if (ny3 != null) {
            ny ny5 = ny3;
            String string = ny5.f;
            String string2 = ny5.e();
            n6 = n2 + 12;
            n5 = n3 - 4;
            if (this.x.b(ny5)) {
                n4 = Math.max(this.g.a(string), 120);
                int n26 = this.g.a(string2, n4);
                if (this.x.a(ny5)) {
                    n26 += 12;
                }
                this.a(n6 - 3, n5 - 3, n6 + n4 + 3, n5 + n26 + 3 + 12, -1073741824, -1073741824);
                this.g.a(string2, n6, n5 + 12, n4, -6250336);
                if (this.x.a(ny5)) {
                    this.g.a(do.a("achievement.taken"), n6, n5 + n26 + 4, -7302913);
                }
            } else {
                n4 = Math.max(this.g.a(string), 120);
                String string3 = do.a("achievement.requires", ny5.c.f);
                int n27 = this.g.a(string3, n4);
                this.a(n6 - 3, n5 - 3, n6 + n4 + 3, n5 + n27 + 12 + 3, -1073741824, -1073741824);
                this.g.a(string3, n6, n5 + 12, n4, -9416624);
            }
            this.g.a(string, n6, n5, this.x.b(ny5) ? (ny5.f() ? -128 : -1) : (ny5.f() ? -8355776 : -8355712));
        }
        GL11.glEnable((int)2929);
        GL11.glEnable((int)2896);
        u.a();
    }

    public boolean c() {
        return true;
    }
}

