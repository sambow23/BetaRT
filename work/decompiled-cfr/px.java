/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GLContext
 *  org.lwjgl.util.glu.GLU
 */
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import mcrtx.bridge.MinecraftRenderHooks;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

public class px {
    public static boolean a = false;
    public static int b;
    private Minecraft j;
    private float k = 0.0f;
    public ra c;
    private int l;
    private sn m = null;
    private cu n = new cu();
    private cu o = new cu();
    private cu p = new cu();
    private cu q = new cu();
    private cu r = new cu();
    private cu s = new cu();
    private float t = 4.0f;
    private float u = 4.0f;
    private float v = 0.0f;
    private float w = 0.0f;
    private float x = 0.0f;
    private float y = 0.0f;
    private float z = 0.0f;
    private float A = 0.0f;
    private float B = 0.0f;
    private float C = 0.0f;
    private boolean D = false;
    private double E = 1.0;
    private double F = 0.0;
    private double G = 0.0;
    private long H = System.currentTimeMillis();
    private long I = 0L;
    private Random J = new Random();
    private int K = 0;
    volatile int d = 0;
    volatile int e = 0;
    FloatBuffer f = ge.e(16);
    float g;
    float h;
    float i;
    private float L;
    private float M;

    public px(Minecraft minecraft) {
        this.j = minecraft;
        this.c = new ra(minecraft);
    }

    public void a() {
        this.L = this.M;
        this.u = this.t;
        this.w = this.v;
        this.y = this.x;
        this.A = this.z;
        this.C = this.B;
        if (this.j.i == null) {
            this.j.i = this.j.h;
        }
        float f2 = this.j.f.c(in.b(this.j.i.aM), in.b(this.j.i.aN), in.b(this.j.i.aO));
        float f3 = (float)(3 - this.j.z.e) / 3.0f;
        float f4 = f2 * (1.0f - f3) + f3;
        this.M += (f4 - this.M) * 0.1f;
        ++this.l;
        this.c.a();
        this.c();
    }

    public void a(float f2) {
        if (this.j.i == null) {
            return;
        }
        if (this.j.f == null) {
            return;
        }
        double d2 = this.j.c.b();
        this.j.y = this.j.i.a(d2, f2);
        double d3 = d2;
        bt bt2 = this.j.i.e(f2);
        if (this.j.y != null) {
            d3 = this.j.y.f.c(bt2);
        }
        if (this.j.c instanceof pj) {
            d2 = 32.0;
            d3 = 32.0;
        } else {
            if (d3 > 3.0) {
                d3 = 3.0;
            }
            d2 = d3;
        }
        bt bt3 = this.j.i.f(f2);
        bt bt4 = bt2.c(bt3.a * d2, bt3.b * d2, bt3.c * d2);
        this.m = null;
        float f3 = 1.0f;
        List list = this.j.f.b(this.j.i, this.j.i.aW.a(bt3.a * d2, bt3.b * d2, bt3.c * d2).b(f3, f3, f3));
        double d4 = 0.0;
        for (int i2 = 0; i2 < list.size(); ++i2) {
            double d5;
            sn sn2 = (sn)list.get(i2);
            if (!sn2.h_()) continue;
            float f4 = sn2.m_();
            eq eq2 = sn2.aW.b(f4, f4, f4);
            vf vf2 = eq2.a(bt2, bt4);
            if (eq2.a(bt2)) {
                if (!(0.0 < d4) && d4 != 0.0) continue;
                this.m = sn2;
                d4 = 0.0;
                continue;
            }
            if (vf2 == null || !((d5 = bt2.c(vf2.f)) < d4) && d4 != 0.0) continue;
            this.m = sn2;
            d4 = d5;
        }
        if (this.m != null && !(this.j.c instanceof pj)) {
            this.j.y = new vf(this.m);
        }
    }

    private float d(float f2) {
        ls ls2 = this.j.i;
        float f3 = 70.0f;
        if (ls2.a(ln.g)) {
            f3 = 60.0f;
        }
        if (ls2.Y <= 0) {
            float f4 = (float)ls2.ad + f2;
            f3 /= (1.0f - 500.0f / (f4 + 500.0f)) * 2.0f + 1.0f;
        }
        return f3 + this.A + (this.z - this.A) * f2;
    }

    private void e(float f2) {
        float f3;
        ls ls2 = this.j.i;
        float f4 = (float)ls2.aa - f2;
        if (ls2.Y <= 0) {
            f3 = (float)ls2.ad + f2;
            GL11.glRotatef((float)(40.0f - 8000.0f / (f3 + 200.0f)), (float)0.0f, (float)0.0f, (float)1.0f);
        }
        if (f4 < 0.0f) {
            return;
        }
        f4 /= (float)ls2.ab;
        f4 = in.a(f4 * f4 * f4 * f4 * (float)Math.PI);
        f3 = ls2.ac;
        GL11.glRotatef((float)(-f3), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-f4 * 14.0f), (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glRotatef((float)f3, (float)0.0f, (float)1.0f, (float)0.0f);
    }

    private void f(float f2) {
        if (!(this.j.i instanceof gs)) {
            return;
        }
        gs gs2 = (gs)this.j.i;
        float f3 = gs2.bj - gs2.bi;
        float f4 = -(gs2.bj + f3 * f2);
        float f5 = gs2.h + (gs2.i - gs2.h) * f2;
        float f6 = gs2.af + (gs2.ag - gs2.af) * f2;
        GL11.glTranslatef((float)(in.a(f4 * (float)Math.PI) * f5 * 0.5f), (float)(-Math.abs(in.b(f4 * (float)Math.PI) * f5)), (float)0.0f);
        GL11.glRotatef((float)(in.a(f4 * (float)Math.PI) * f5 * 3.0f), (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glRotatef((float)(Math.abs(in.b(f4 * (float)Math.PI - 0.2f) * f5) * 5.0f), (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)f6, (float)1.0f, (float)0.0f, (float)0.0f);
    }

    private void g(float f2) {
        ls ls2 = this.j.i;
        float f3 = ls2.bf - 1.62f;
        double d2 = ls2.aJ + (ls2.aM - ls2.aJ) * (double)f2;
        double d3 = ls2.aK + (ls2.aN - ls2.aK) * (double)f2 - (double)f3;
        double d4 = ls2.aL + (ls2.aO - ls2.aL) * (double)f2;
        GL11.glRotatef((float)(this.C + (this.B - this.C) * f2), (float)0.0f, (float)0.0f, (float)1.0f);
        if (ls2.N()) {
            f3 = (float)((double)f3 + 1.0);
            GL11.glTranslatef((float)0.0f, (float)0.3f, (float)0.0f);
            if (!this.j.z.F) {
                int n2 = this.j.f.a(in.b(ls2.aM), in.b(ls2.aN), in.b(ls2.aO));
                if (n2 == uu.T.bn) {
                    int n3 = this.j.f.e(in.b(ls2.aM), in.b(ls2.aN), in.b(ls2.aO));
                    int n4 = n3 & 3;
                    GL11.glRotatef((float)(n4 * 90), (float)0.0f, (float)1.0f, (float)0.0f);
                }
                GL11.glRotatef((float)(ls2.aU + (ls2.aS - ls2.aU) * f2 + 180.0f), (float)0.0f, (float)-1.0f, (float)0.0f);
                GL11.glRotatef((float)(ls2.aV + (ls2.aT - ls2.aV) * f2), (float)-1.0f, (float)0.0f, (float)0.0f);
            }
        } else if (this.j.z.A) {
            double d5 = this.u + (this.t - this.u) * f2;
            if (this.j.z.F) {
                float f4 = this.w + (this.v - this.w) * f2;
                float f5 = this.y + (this.x - this.y) * f2;
                GL11.glTranslatef((float)0.0f, (float)0.0f, (float)((float)(-d5)));
                GL11.glRotatef((float)f5, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)f4, (float)0.0f, (float)1.0f, (float)0.0f);
            } else {
                float f6 = ls2.aS;
                float f7 = ls2.aT;
                double d6 = (double)(-in.a(f6 / 180.0f * (float)Math.PI) * in.b(f7 / 180.0f * (float)Math.PI)) * d5;
                double d7 = (double)(in.b(f6 / 180.0f * (float)Math.PI) * in.b(f7 / 180.0f * (float)Math.PI)) * d5;
                double d8 = (double)(-in.a(f7 / 180.0f * (float)Math.PI)) * d5;
                for (int i2 = 0; i2 < 8; ++i2) {
                    double d9;
                    vf vf2;
                    float f8 = (i2 & 1) * 2 - 1;
                    float f9 = (i2 >> 1 & 1) * 2 - 1;
                    float f10 = (i2 >> 2 & 1) * 2 - 1;
                    if ((vf2 = this.j.f.a(bt.b(d2 + (double)(f8 *= 0.1f), d3 + (double)(f9 *= 0.1f), d4 + (double)(f10 *= 0.1f)), bt.b(d2 - d6 + (double)f8 + (double)f10, d3 - d8 + (double)f9, d4 - d7 + (double)f10))) == null || !((d9 = vf2.f.c(bt.b(d2, d3, d4))) < d5)) continue;
                    d5 = d9;
                }
                GL11.glRotatef((float)(ls2.aT - f7), (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)(ls2.aS - f6), (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glTranslatef((float)0.0f, (float)0.0f, (float)((float)(-d5)));
                GL11.glRotatef((float)(f6 - ls2.aS), (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glRotatef((float)(f7 - ls2.aT), (float)1.0f, (float)0.0f, (float)0.0f);
            }
        } else {
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-0.1f);
        }
        if (!this.j.z.F) {
            GL11.glRotatef((float)(ls2.aV + (ls2.aT - ls2.aV) * f2), (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glRotatef((float)(ls2.aU + (ls2.aS - ls2.aU) * f2 + 180.0f), (float)0.0f, (float)1.0f, (float)0.0f);
        }
        GL11.glTranslatef((float)0.0f, (float)f3, (float)0.0f);
        d2 = ls2.aJ + (ls2.aM - ls2.aJ) * (double)f2;
        d3 = ls2.aK + (ls2.aN - ls2.aK) * (double)f2 - (double)f3;
        d4 = ls2.aL + (ls2.aO - ls2.aL) * (double)f2;
        this.D = this.j.g.a(d2, d3, d4, f2);
    }

    private void a(float f2, int n2) {
        float f3;
        this.k = 256 >> this.j.z.e;
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        float f4 = 0.07f;
        if (this.j.z.g) {
            GL11.glTranslatef((float)((float)(-(n2 * 2 - 1)) * f4), (float)0.0f, (float)0.0f);
        }
        if (this.E != 1.0) {
            GL11.glTranslatef((float)((float)this.F), (float)((float)(-this.G)), (float)0.0f);
            GL11.glScaled((double)this.E, (double)this.E, (double)1.0);
            GLU.gluPerspective((float)this.d(f2), (float)((float)this.j.d / (float)this.j.e), (float)0.05f, (float)(this.k * 2.0f));
        } else {
            GLU.gluPerspective((float)this.d(f2), (float)((float)this.j.d / (float)this.j.e), (float)0.05f, (float)(this.k * 2.0f));
        }
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        if (this.j.z.g) {
            GL11.glTranslatef((float)((float)(n2 * 2 - 1) * 0.1f), (float)0.0f, (float)0.0f);
        }
        this.e(f2);
        if (this.j.z.f) {
            this.f(f2);
        }
        if ((f3 = this.j.h.C + (this.j.h.B - this.j.h.C) * f2) > 0.0f) {
            float f5 = 5.0f / (f3 * f3 + 5.0f) - f3 * 0.04f;
            f5 *= f5;
            GL11.glRotatef((float)(((float)this.l + f2) * 20.0f), (float)0.0f, (float)1.0f, (float)1.0f);
            GL11.glScalef((float)(1.0f / f5), (float)1.0f, (float)1.0f);
            GL11.glRotatef((float)(-((float)this.l + f2) * 20.0f), (float)0.0f, (float)1.0f, (float)1.0f);
        }
        this.g(f2);
    }

    private void b(float f2, int n2) {
        GL11.glLoadIdentity();
        if (this.j.z.g) {
            GL11.glTranslatef((float)((float)(n2 * 2 - 1) * 0.1f), (float)0.0f, (float)0.0f);
        }
        GL11.glPushMatrix();
        this.e(f2);
        if (this.j.z.f) {
            this.f(f2);
        }
        if (!(this.j.z.A || this.j.i.N() || this.j.z.z)) {
            this.c.a(f2);
        }
        GL11.glPopMatrix();
        if (!this.j.z.A && !this.j.i.N()) {
            this.c.b(f2);
            this.e(f2);
        }
        if (this.j.z.f) {
            this.f(f2);
        }
    }

    public void b(float f2) {
        int n2;
        if (!Display.isActive()) {
            if (System.currentTimeMillis() - this.H > 500L) {
                this.j.i();
            }
        } else {
            this.H = System.currentTimeMillis();
        }
        if (this.j.N) {
            this.j.C.c();
            float f3 = this.j.z.c * 0.6f + 0.2f;
            float f4 = f3 * f3 * f3 * 8.0f;
            float f5 = (float)this.j.C.a * f4;
            float f6 = (float)this.j.C.b * f4;
            n2 = 1;
            if (this.j.z.d) {
                n2 = -1;
            }
            if (this.j.z.E) {
                f5 = this.n.a(f5, 0.05f * f4);
                f6 = this.o.a(f6, 0.05f * f4);
            }
            this.j.h.d(f5, f6 * (float)n2);
        }
        if (this.j.w) {
            return;
        }
        a = this.j.z.g;
        qq qq2 = new qq(this.j.z, this.j.d, this.j.e);
        int n3 = qq2.a();
        int n4 = qq2.b();
        int n5 = Mouse.getX() * n3 / this.j.d;
        n2 = n4 - Mouse.getY() * n4 / this.j.e - 1;
        int n6 = 200;
        if (this.j.z.i == 1) {
            n6 = 120;
        }
        if (this.j.z.i == 2) {
            n6 = 40;
        }
        if (this.j.f != null) {
            long l2;
            if (this.j.z.i == 0) {
                this.a(f2, 0L);
            } else {
                this.a(f2, this.I + (long)(1000000000 / n6));
            }
            if (this.j.z.i == 2 && (l2 = (this.I + (long)(1000000000 / n6) - System.nanoTime()) / 1000000L) > 0L && l2 < 500L) {
                try {
                    Thread.sleep(l2);
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            this.I = System.nanoTime();
            if (!this.j.z.z || this.j.r != null) {
                this.j.v.a(f2, this.j.r != null, n5, n2);
            }
        } else {
            GL11.glViewport((int)0, (int)0, (int)this.j.d, (int)this.j.e);
            GL11.glMatrixMode((int)5889);
            GL11.glLoadIdentity();
            GL11.glMatrixMode((int)5888);
            GL11.glLoadIdentity();
            this.b();
            if (this.j.z.i == 2) {
                long l3 = (this.I + (long)(1000000000 / n6) - System.nanoTime()) / 1000000L;
                if (l3 < 0L) {
                    l3 += 10L;
                }
                if (l3 > 0L && l3 < 500L) {
                    try {
                        Thread.sleep(l3);
                    }
                    catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
            this.I = System.nanoTime();
        }
        if (this.j.r != null) {
            GL11.glClear((int)256);
            this.j.r.a(n5, n2, f2);
            if (this.j.r != null && this.j.r.h != null) {
                this.j.r.h.a(f2);
            }
        }
        MinecraftRenderHooks.present();
    }

    private void updateRemixCamera(float f2) {
        if (!MinecraftRenderHooks.isInitialized() || this.j.i == null) {
            return;
        }
        ls ls2 = this.j.i;
        bt position = ls2.e(f2);
        bt forward = ls2.f(f2);
        float aspect = this.j.e <= 0 ? 1.0f : (float)this.j.d / (float)this.j.e;
        MinecraftRenderHooks.updateCamera(
                (float)position.a,
                (float)(position.b + (double)ls2.w()),
                (float)position.c,
                (float)forward.a,
                (float)forward.b,
                (float)forward.c,
                this.d(f2),
                aspect,
                0.05f,
                this.k * 2.0f);
    }

    public void a(float f2, long l2) {
        int n2;
        GL11.glEnable((int)2884);
        GL11.glEnable((int)2929);
        if (this.j.i == null) {
            this.j.i = this.j.h;
        }
        this.a(f2);
        ls ls2 = this.j.i;
        n n3 = this.j.g;
        dn dn2 = this.j.j;
        double d2 = ls2.bl + (ls2.aM - ls2.bl) * (double)f2;
        double d3 = ls2.bm + (ls2.aN - ls2.bm) * (double)f2;
        double d4 = ls2.bn + (ls2.aO - ls2.bn) * (double)f2;
        cl cl2 = this.j.f.w();
        if (cl2 instanceof kx) {
            kx kx2 = (kx)cl2;
            int n4 = in.d((int)d2) >> 4;
            n2 = in.d((int)d4) >> 4;
            kx2.d(n4, n2);
        }
        for (int i2 = 0; i2 < 2; ++i2) {
            if (this.j.z.g) {
                b = i2;
                if (b == 0) {
                    GL11.glColorMask((boolean)false, (boolean)true, (boolean)true, (boolean)false);
                } else {
                    GL11.glColorMask((boolean)true, (boolean)false, (boolean)false, (boolean)false);
                }
            }
            GL11.glViewport((int)0, (int)0, (int)this.j.d, (int)this.j.e);
            this.h(f2);
            GL11.glClear((int)16640);
            GL11.glEnable((int)2884);
            this.a(f2, i2);
            if (i2 == 0) {
                this.updateRemixCamera(f2);
            }
            w.a();
            if (this.j.z.e < 2) {
                this.a(-1, f2);
                n3.a(f2);
            }
            GL11.glEnable((int)2912);
            this.a(1, f2);
            if (this.j.z.k) {
                GL11.glShadeModel((int)7425);
            }
            sr sr2 = new sr();
            sr2.a(d2, d3, d4);
            this.j.g.a(sr2, f2);
            if (i2 == 0) {
                long l3;
                while (!this.j.g.a(ls2, false) && l2 != 0L && (l3 = l2 - System.nanoTime()) >= 0L && l3 <= 1000000000L) {
                }
            }
            this.a(0, f2);
            GL11.glEnable((int)2912);
            GL11.glBindTexture((int)3553, (int)this.j.p.b("/terrain.png"));
            u.a();
            n3.a(ls2, 0, (double)f2);
            GL11.glShadeModel((int)7424);
            u.b();
            n3.a(ls2.e(f2), sr2, f2);
            dn2.b(ls2, f2);
            u.a();
            this.a(0, f2);
            dn2.a(ls2, f2);
            if (this.j.y != null && ls2.a(ln.g) && ls2 instanceof gs) {
                gs gs2 = (gs)ls2;
                GL11.glDisable((int)3008);
                n3.a(gs2, this.j.y, 0, gs2.c.b(), f2);
                n3.b(gs2, this.j.y, 0, gs2.c.b(), f2);
                GL11.glEnable((int)3008);
            }
            GL11.glBlendFunc((int)770, (int)771);
            this.a(0, f2);
            GL11.glEnable((int)3042);
            GL11.glDisable((int)2884);
            GL11.glBindTexture((int)3553, (int)this.j.p.b("/terrain.png"));
            if (this.j.z.j) {
                if (this.j.z.k) {
                    GL11.glShadeModel((int)7425);
                }
                GL11.glColorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
                n2 = n3.a(ls2, 1, (double)f2);
                if (this.j.z.g) {
                    if (b == 0) {
                        GL11.glColorMask((boolean)false, (boolean)true, (boolean)true, (boolean)true);
                    } else {
                        GL11.glColorMask((boolean)true, (boolean)false, (boolean)false, (boolean)true);
                    }
                } else {
                    GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
                }
                if (n2 > 0) {
                    n3.a(1, (double)f2);
                }
                GL11.glShadeModel((int)7424);
            } else {
                n3.a(ls2, 1, (double)f2);
            }
            GL11.glDepthMask((boolean)true);
            GL11.glEnable((int)2884);
            GL11.glDisable((int)3042);
            if (this.E == 1.0 && ls2 instanceof gs && this.j.y != null && !ls2.a(ln.g)) {
                gs gs3 = (gs)ls2;
                GL11.glDisable((int)3008);
                n3.a(gs3, this.j.y, 0, gs3.c.b(), f2);
                n3.b(gs3, this.j.y, 0, gs3.c.b(), f2);
                GL11.glEnable((int)3008);
            }
            this.c(f2);
            GL11.glDisable((int)2912);
            if (this.m != null) {
                // empty if block
            }
            this.a(0, f2);
            GL11.glEnable((int)2912);
            n3.b(f2);
            GL11.glDisable((int)2912);
            this.a(1, f2);
            if (this.E == 1.0) {
                GL11.glClear((int)256);
                this.b(f2, i2);
            }
            if (this.j.z.g) continue;
            return;
        }
        GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)false);
    }

    private void c() {
        float f2 = this.j.f.g(1.0f);
        if (!this.j.z.j) {
            f2 /= 2.0f;
        }
        if (f2 == 0.0f) {
            return;
        }
        this.J.setSeed((long)this.l * 312987231L);
        ls ls2 = this.j.i;
        fd fd2 = this.j.f;
        int n2 = in.b(ls2.aM);
        int n3 = in.b(ls2.aN);
        int n4 = in.b(ls2.aO);
        int n5 = 10;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        int n6 = 0;
        for (int i2 = 0; i2 < (int)(100.0f * f2 * f2); ++i2) {
            int n7 = n2 + this.J.nextInt(n5) - this.J.nextInt(n5);
            int n8 = n4 + this.J.nextInt(n5) - this.J.nextInt(n5);
            int n9 = fd2.e(n7, n8);
            int n10 = fd2.a(n7, n9 - 1, n8);
            if (n9 > n3 + n5 || n9 < n3 - n5 || !fd2.a().a(n7, n8).d()) continue;
            float f3 = this.J.nextFloat();
            float f4 = this.J.nextFloat();
            if (n10 <= 0) continue;
            if (uu.m[n10].bA == ln.h) {
                this.j.j.a(new xn(fd2, (float)n7 + f3, (double)((float)n9 + 0.1f) - uu.m[n10].bt, (float)n8 + f4, 0.0, 0.0, 0.0));
                continue;
            }
            if (this.J.nextInt(++n6) == 0) {
                d2 = (float)n7 + f3;
                d3 = (double)((float)n9 + 0.1f) - uu.m[n10].bt;
                d4 = (float)n8 + f4;
            }
            this.j.j.a(new xd(fd2, (float)n7 + f3, (double)((float)n9 + 0.1f) - uu.m[n10].bt, (float)n8 + f4));
        }
        if (n6 > 0 && this.J.nextInt(3) < this.K++) {
            this.K = 0;
            if (d3 > ls2.aN + 1.0 && fd2.e(in.b(ls2.aM), in.b(ls2.aO)) > in.b(ls2.aN)) {
                this.j.f.a(d2, d3, d4, "ambient.weather.rain", 0.1f, 0.5f);
            } else {
                this.j.f.a(d2, d3, d4, "ambient.weather.rain", 0.2f, 1.0f);
            }
        }
    }

    protected void c(float f2) {
        float f3;
        int n2;
        int n3;
        int n4;
        kd kd2;
        int n5;
        int n6;
        float f4 = this.j.f.g(f2);
        if (f4 <= 0.0f) {
            return;
        }
        ls ls2 = this.j.i;
        fd fd2 = this.j.f;
        int n7 = in.b(ls2.aM);
        int n8 = in.b(ls2.aN);
        int n9 = in.b(ls2.aO);
        nw nw2 = nw.a;
        GL11.glDisable((int)2884);
        GL11.glNormal3f((float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glAlphaFunc((int)516, (float)0.01f);
        GL11.glBindTexture((int)3553, (int)this.j.p.b("/environment/snow.png"));
        double d2 = ls2.bl + (ls2.aM - ls2.bl) * (double)f2;
        double d3 = ls2.bm + (ls2.aN - ls2.bm) * (double)f2;
        double d4 = ls2.bn + (ls2.aO - ls2.bn) * (double)f2;
        int n10 = in.b(d3);
        int n11 = 5;
        if (this.j.z.j) {
            n11 = 10;
        }
        kd[] kdArray = fd2.a().a(n7 - n11, n9 - n11, n11 * 2 + 1, n11 * 2 + 1);
        int n12 = 0;
        for (n6 = n7 - n11; n6 <= n7 + n11; ++n6) {
            for (n5 = n9 - n11; n5 <= n9 + n11; ++n5) {
                if (!(kd2 = kdArray[n12++]).c()) continue;
                n4 = fd2.e(n6, n5);
                if (n4 < 0) {
                    n4 = 0;
                }
                if ((n3 = n4) < n10) {
                    n3 = n10;
                }
                n2 = n8 - n11;
                int n13 = n8 + n11;
                if (n2 < n4) {
                    n2 = n4;
                }
                if (n13 < n4) {
                    n13 = n4;
                }
                f3 = 1.0f;
                if (n2 == n13) continue;
                this.J.setSeed(n6 * n6 * 3121 + n6 * 45238971 + n5 * n5 * 418711 + n5 * 13761);
                float f5 = (float)this.l + f2;
                float f6 = ((float)(this.l & 0x1FF) + f2) / 512.0f;
                float f7 = this.J.nextFloat() + f5 * 0.01f * (float)this.J.nextGaussian();
                float f8 = this.J.nextFloat() + f5 * (float)this.J.nextGaussian() * 0.001f;
                double d5 = (double)((float)n6 + 0.5f) - ls2.aM;
                double d6 = (double)((float)n5 + 0.5f) - ls2.aO;
                float f9 = in.a(d5 * d5 + d6 * d6) / (float)n11;
                nw2.b();
                float f10 = fd2.c(n6, n3, n5);
                GL11.glColor4f((float)f10, (float)f10, (float)f10, (float)(((1.0f - f9 * f9) * 0.3f + 0.5f) * f4));
                nw2.b(-d2 * 1.0, -d3 * 1.0, -d4 * 1.0);
                nw2.a(n6 + 0, n2, (double)n5 + 0.5, 0.0f * f3 + f7, (float)n2 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a(n6 + 1, n2, (double)n5 + 0.5, 1.0f * f3 + f7, (float)n2 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a(n6 + 1, n13, (double)n5 + 0.5, 1.0f * f3 + f7, (float)n13 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a(n6 + 0, n13, (double)n5 + 0.5, 0.0f * f3 + f7, (float)n13 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a((double)n6 + 0.5, n2, n5 + 0, 0.0f * f3 + f7, (float)n2 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a((double)n6 + 0.5, n2, n5 + 1, 1.0f * f3 + f7, (float)n2 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a((double)n6 + 0.5, n13, n5 + 1, 1.0f * f3 + f7, (float)n13 * f3 / 4.0f + f6 * f3 + f8);
                nw2.a((double)n6 + 0.5, n13, n5 + 0, 0.0f * f3 + f7, (float)n13 * f3 / 4.0f + f6 * f3 + f8);
                nw2.b(0.0, 0.0, 0.0);
                nw2.a();
            }
        }
        GL11.glBindTexture((int)3553, (int)this.j.p.b("/environment/rain.png"));
        if (this.j.z.j) {
            n11 = 10;
        }
        n12 = 0;
        for (n6 = n7 - n11; n6 <= n7 + n11; ++n6) {
            for (n5 = n9 - n11; n5 <= n9 + n11; ++n5) {
                if (!(kd2 = kdArray[n12++]).d()) continue;
                n4 = fd2.e(n6, n5);
                n3 = n8 - n11;
                n2 = n8 + n11;
                if (n3 < n4) {
                    n3 = n4;
                }
                if (n2 < n4) {
                    n2 = n4;
                }
                float f11 = 1.0f;
                if (n3 == n2) continue;
                this.J.setSeed(n6 * n6 * 3121 + n6 * 45238971 + n5 * n5 * 418711 + n5 * 13761);
                f3 = ((float)(this.l + n6 * n6 * 3121 + n6 * 45238971 + n5 * n5 * 418711 + n5 * 13761 & 0x1F) + f2) / 32.0f * (3.0f + this.J.nextFloat());
                double d7 = (double)((float)n6 + 0.5f) - ls2.aM;
                double d8 = (double)((float)n5 + 0.5f) - ls2.aO;
                float f12 = in.a(d7 * d7 + d8 * d8) / (float)n11;
                nw2.b();
                float f13 = fd2.c(n6, 128, n5) * 0.85f + 0.15f;
                GL11.glColor4f((float)f13, (float)f13, (float)f13, (float)(((1.0f - f12 * f12) * 0.5f + 0.5f) * f4));
                nw2.b(-d2 * 1.0, -d3 * 1.0, -d4 * 1.0);
                nw2.a(n6 + 0, n3, (double)n5 + 0.5, 0.0f * f11, (float)n3 * f11 / 4.0f + f3 * f11);
                nw2.a(n6 + 1, n3, (double)n5 + 0.5, 1.0f * f11, (float)n3 * f11 / 4.0f + f3 * f11);
                nw2.a(n6 + 1, n2, (double)n5 + 0.5, 1.0f * f11, (float)n2 * f11 / 4.0f + f3 * f11);
                nw2.a(n6 + 0, n2, (double)n5 + 0.5, 0.0f * f11, (float)n2 * f11 / 4.0f + f3 * f11);
                nw2.a((double)n6 + 0.5, n3, n5 + 0, 0.0f * f11, (float)n3 * f11 / 4.0f + f3 * f11);
                nw2.a((double)n6 + 0.5, n3, n5 + 1, 1.0f * f11, (float)n3 * f11 / 4.0f + f3 * f11);
                nw2.a((double)n6 + 0.5, n2, n5 + 1, 1.0f * f11, (float)n2 * f11 / 4.0f + f3 * f11);
                nw2.a((double)n6 + 0.5, n2, n5 + 0, 0.0f * f11, (float)n2 * f11 / 4.0f + f3 * f11);
                nw2.b(0.0, 0.0, 0.0);
                nw2.a();
            }
        }
        GL11.glEnable((int)2884);
        GL11.glDisable((int)3042);
        GL11.glAlphaFunc((int)516, (float)0.1f);
    }

    public void b() {
        qq qq2 = new qq(this.j.z, this.j.d, this.j.e);
        GL11.glClear((int)256);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)0.0, (double)qq2.a, (double)qq2.b, (double)0.0, (double)1000.0, (double)3000.0);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-2000.0f);
    }

    private void h(float f2) {
        float f3;
        float f4;
        fd fd2 = this.j.f;
        ls ls2 = this.j.i;
        float f5 = 1.0f / (float)(4 - this.j.z.e);
        f5 = 1.0f - (float)Math.pow(f5, 0.25);
        bt bt2 = fd2.a((sn)this.j.i, f2);
        float f6 = (float)bt2.a;
        float f7 = (float)bt2.b;
        float f8 = (float)bt2.c;
        bt bt3 = fd2.d(f2);
        this.g = (float)bt3.a;
        this.h = (float)bt3.b;
        this.i = (float)bt3.c;
        this.g += (f6 - this.g) * f5;
        this.h += (f7 - this.h) * f5;
        this.i += (f8 - this.i) * f5;
        float f9 = fd2.g(f2);
        if (f9 > 0.0f) {
            f4 = 1.0f - f9 * 0.5f;
            f3 = 1.0f - f9 * 0.4f;
            this.g *= f4;
            this.h *= f4;
            this.i *= f3;
        }
        if ((f4 = fd2.f(f2)) > 0.0f) {
            f3 = 1.0f - f4 * 0.5f;
            this.g *= f3;
            this.h *= f3;
            this.i *= f3;
        }
        if (this.D) {
            bt bt4 = fd2.c(f2);
            this.g = (float)bt4.a;
            this.h = (float)bt4.b;
            this.i = (float)bt4.c;
        } else if (ls2.a(ln.g)) {
            this.g = 0.02f;
            this.h = 0.02f;
            this.i = 0.2f;
        } else if (ls2.a(ln.h)) {
            this.g = 0.6f;
            this.h = 0.1f;
            this.i = 0.0f;
        }
        float f10 = this.L + (this.M - this.L) * f2;
        this.g *= f10;
        this.h *= f10;
        this.i *= f10;
        if (this.j.z.g) {
            float f11 = (this.g * 30.0f + this.h * 59.0f + this.i * 11.0f) / 100.0f;
            float f12 = (this.g * 30.0f + this.h * 70.0f) / 100.0f;
            float f13 = (this.g * 30.0f + this.i * 70.0f) / 100.0f;
            this.g = f11;
            this.h = f12;
            this.i = f13;
        }
        GL11.glClearColor((float)this.g, (float)this.h, (float)this.i, (float)0.0f);
    }

    private void a(int n2, float f2) {
        ls ls2 = this.j.i;
        GL11.glFog((int)2918, (FloatBuffer)this.a(this.g, this.h, this.i, 1.0f));
        GL11.glNormal3f((float)0.0f, (float)-1.0f, (float)0.0f);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (this.D) {
            GL11.glFogi((int)2917, (int)2048);
            GL11.glFogf((int)2914, (float)0.1f);
            float f3 = 1.0f;
            float f4 = 1.0f;
            float f5 = 1.0f;
            if (this.j.z.g) {
                float f6 = (f3 * 30.0f + f4 * 59.0f + f5 * 11.0f) / 100.0f;
                float f7 = (f3 * 30.0f + f4 * 70.0f) / 100.0f;
                float f8 = (f3 * 30.0f + f5 * 70.0f) / 100.0f;
                f3 = f6;
                f4 = f7;
                f5 = f8;
            }
        } else if (ls2.a(ln.g)) {
            GL11.glFogi((int)2917, (int)2048);
            GL11.glFogf((int)2914, (float)0.1f);
            float f9 = 0.4f;
            float f10 = 0.4f;
            float f11 = 0.9f;
            if (this.j.z.g) {
                float f12 = (f9 * 30.0f + f10 * 59.0f + f11 * 11.0f) / 100.0f;
                float f13 = (f9 * 30.0f + f10 * 70.0f) / 100.0f;
                float f14 = (f9 * 30.0f + f11 * 70.0f) / 100.0f;
                f9 = f12;
                f10 = f13;
                f11 = f14;
            }
        } else if (ls2.a(ln.h)) {
            GL11.glFogi((int)2917, (int)2048);
            GL11.glFogf((int)2914, (float)2.0f);
            float f15 = 0.4f;
            float f16 = 0.3f;
            float f17 = 0.3f;
            if (this.j.z.g) {
                float f18 = (f15 * 30.0f + f16 * 59.0f + f17 * 11.0f) / 100.0f;
                float f19 = (f15 * 30.0f + f16 * 70.0f) / 100.0f;
                float f20 = (f15 * 30.0f + f17 * 70.0f) / 100.0f;
                f15 = f18;
                f16 = f19;
                f17 = f20;
            }
        } else {
            GL11.glFogi((int)2917, (int)9729);
            GL11.glFogf((int)2915, (float)(this.k * 0.25f));
            GL11.glFogf((int)2916, (float)this.k);
            if (n2 < 0) {
                GL11.glFogf((int)2915, (float)0.0f);
                GL11.glFogf((int)2916, (float)(this.k * 0.8f));
            }
            if (GLContext.getCapabilities().GL_NV_fog_distance) {
                GL11.glFogi((int)34138, (int)34139);
            }
            if (this.j.f.t.c) {
                GL11.glFogf((int)2915, (float)0.0f);
            }
        }
        GL11.glEnable((int)2903);
        GL11.glColorMaterial((int)1028, (int)4608);
    }

    private FloatBuffer a(float f2, float f3, float f4, float f5) {
        this.f.clear();
        this.f.put(f2).put(f3).put(f4).put(f5);
        this.f.flip();
        return this.f;
    }
}

