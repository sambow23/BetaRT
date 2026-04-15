/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.LWJGLException
 *  org.lwjgl.input.Controllers
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.opengl.DisplayMode
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.util.glu.GLU
 */
package net.minecraft.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.File;
import mcrtx.bridge.MinecraftRenderHooks;
import net.minecraft.client.MinecraftApplet;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public abstract class Minecraft
implements Runnable {
    public static byte[] b = new byte[0xA00000];
    private static Minecraft a;
    public ob c;
    private boolean Q = false;
    private boolean R = false;
    public int d;
    public int e;
    private cx S;
    private py T = new py(20.0f);
    public fd f;
    public n g;
    public dc h;
    public ls i;
    public dn j;
    public gr k = null;
    public String l;
    public Canvas m;
    public boolean n = true;
    public volatile boolean o = false;
    public ji p;
    public sj q;
    public da r = null;
    public mk s = new mk(this);
    public px t;
    private cz U;
    private int V = 0;
    private int W = 0;
    private int X;
    private int Y;
    public vu u = new vu(this);
    public uq v;
    public boolean w = false;
    public fh x = new fh(0.0f);
    public vf y = null;
    public kv z;
    protected MinecraftApplet A;
    public yo B = new yo();
    public vy C;
    public ik D;
    private File Z;
    private nl aa;
    public static long[] E;
    public static long[] F;
    public static int G;
    public static long H;
    public xi I;
    private String ab;
    private int ac;
    private vs ad = new vs();
    private cg ae = new cg();
    private static File af;
    public volatile boolean J = true;
    public String K = "";
    boolean L = false;
    long M = -1L;
    public boolean N = false;
    private int ag = 0;
    public boolean O = false;
    long P = System.currentTimeMillis();
    private int ah = 0;

    public Minecraft(Component component, Canvas canvas, MinecraftApplet minecraftApplet, int n2, int n3, boolean bl2) {
        jl.a();
        this.Y = n3;
        this.Q = bl2;
        this.A = minecraftApplet;
        new kg(this, "Timer hack thread");
        this.m = canvas;
        this.d = n2;
        this.e = n3;
        this.Q = bl2;
        if (minecraftApplet == null || "true".equals(minecraftApplet.getParameter("stand-alone"))) {
            this.n = false;
        }
        a = this;
    }

    public void b(mh mh2) {
        this.R = true;
        this.a(mh2);
    }

    public abstract void a(mh var1);

    public void a(String string, int n2) {
        this.ab = string;
        this.ac = n2;
    }

    public void a() {
        if (this.m != null) {
            Graphics graphics = this.m.getGraphics();
            if (graphics != null) {
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, this.d, this.e);
                graphics.dispose();
            }
            Display.setParent((Canvas)this.m);
        } else if (this.Q) {
            Display.setFullscreen((boolean)true);
            this.d = Display.getDisplayMode().getWidth();
            this.e = Display.getDisplayMode().getHeight();
            if (this.d <= 0) {
                this.d = 1;
            }
            if (this.e <= 0) {
                this.e = 1;
            }
        } else {
            Display.setDisplayMode((DisplayMode)new DisplayMode(this.d, this.e));
        }
        Display.setTitle((String)"Minecraft Minecraft Beta 1.7.3");
        try {
            Display.create();
        }
        catch (LWJGLException lWJGLException) {
            lWJGLException.printStackTrace();
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            Display.create();
        }
        MinecraftRenderHooks.initializeForCurrentDisplay(this.d, this.e);
        this.Z = Minecraft.b();
        this.aa = new hi(new File(this.Z, "saves"));
        this.z = new kv(this, this.Z);
        this.D = new ik(this, this.Z);
        this.p = new ji(this.D, this.z);
        this.q = new sj(this.z, "/font/default.png", this.p);
        hw.a(this.p.a("/misc/watercolor.png"));
        ia.a(this.p.a("/misc/grasscolor.png"));
        jh.a(this.p.a("/misc/foliagecolor.png"));
        this.t = new px(this);
        th.a.f = new ra(this);
        this.I = new xi(this.k, this.Z);
        ep.f.a(new kh(this));
        this.x();
        Keyboard.create();
        Mouse.create();
        this.C = new vy(this.m);
        try {
            Controllers.create();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.c("Pre startup");
        GL11.glEnable((int)3553);
        GL11.glShadeModel((int)7425);
        GL11.glClearDepth((double)1.0);
        GL11.glEnable((int)2929);
        GL11.glDepthFunc((int)515);
        GL11.glEnable((int)3008);
        GL11.glAlphaFunc((int)516, (float)0.1f);
        GL11.glCullFace((int)1029);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glMatrixMode((int)5888);
        this.c("Startup");
        this.S = new cx();
        this.B.a(this.z);
        this.p.a(this.ae);
        this.p.a(this.ad);
        this.p.a(new hs());
        this.p.a(new av(this));
        this.p.a(new ku(this));
        this.p.a(new oh());
        this.p.a(new if());
        this.p.a(new sd(0));
        this.p.a(new sd(1));
        this.g = new n(this, this.p);
        GL11.glViewport((int)0, (int)0, (int)this.d, (int)this.e);
        this.j = new dn(this.f, this.p);
        try {
            this.U = new cz(this.Z, this);
            this.U.start();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.c("Post startup");
        this.v = new uq(this);
        if (this.ab != null) {
            this.a(new vx(this, this.ab, this.ac));
        } else {
            this.a(new fu());
        }
    }

    private void x() {
        qq qq2 = new qq(this.z, this.d, this.e);
        GL11.glClear((int)16640);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)0.0, (double)qq2.a, (double)qq2.b, (double)0.0, (double)1000.0, (double)3000.0);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-2000.0f);
        GL11.glViewport((int)0, (int)0, (int)this.d, (int)this.e);
        GL11.glClearColor((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f);
        nw nw2 = nw.a;
        GL11.glDisable((int)2896);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2912);
        GL11.glBindTexture((int)3553, (int)this.p.b("/title/mojang.png"));
        nw2.b();
        nw2.b(0xFFFFFF);
        nw2.a(0.0, this.e, 0.0, 0.0, 0.0);
        nw2.a(this.d, this.e, 0.0, 0.0, 0.0);
        nw2.a(this.d, 0.0, 0.0, 0.0, 0.0);
        nw2.a(0.0, 0.0, 0.0, 0.0, 0.0);
        nw2.a();
        int n2 = 256;
        int n3 = 256;
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        nw2.b(0xFFFFFF);
        this.a((qq2.a() - n2) / 2, (qq2.b() - n3) / 2, 0, 0, n2, n3);
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2912);
        GL11.glEnable((int)3008);
        GL11.glAlphaFunc((int)516, (float)0.1f);
        Display.swapBuffers();
    }

    public void a(int n2, int n3, int n4, int n5, int n6, int n7) {
        float f2 = 0.00390625f;
        float f3 = 0.00390625f;
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(n2 + 0, n3 + n7, 0.0, (float)(n4 + 0) * f2, (float)(n5 + n7) * f3);
        nw2.a(n2 + n6, n3 + n7, 0.0, (float)(n4 + n6) * f2, (float)(n5 + n7) * f3);
        nw2.a(n2 + n6, n3 + 0, 0.0, (float)(n4 + n6) * f2, (float)(n5 + 0) * f3);
        nw2.a(n2 + 0, n3 + 0, 0.0, (float)(n4 + 0) * f2, (float)(n5 + 0) * f3);
        nw2.a();
    }

    public static File b() {
        if (af == null) {
            af = Minecraft.a("minecraft");
        }
        return af;
    }

    public static File a(String string) {
        File file;
        String string2 = System.getProperty("user.home", ".");
        switch (Minecraft.y()) {
            case a: 
            case b: {
                file = new File(string2, '.' + string + '/');
                break;
            }
            case c: {
                String string3 = System.getenv("APPDATA");
                if (string3 != null) {
                    file = new File(string3, "." + string + '/');
                    break;
                }
                file = new File(string2, '.' + string + '/');
                break;
            }
            case d: {
                file = new File(string2, "Library/Application Support/" + string);
                break;
            }
            default: {
                file = new File(string2, string + '/');
            }
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + file);
        }
        return file;
    }

    private static pd y() {
        String string = System.getProperty("os.name").toLowerCase();
        if (string.contains("win")) {
            return pd.c;
        }
        if (string.contains("mac")) {
            return pd.d;
        }
        if (string.contains("solaris")) {
            return pd.b;
        }
        if (string.contains("sunos")) {
            return pd.b;
        }
        if (string.contains("linux")) {
            return pd.a;
        }
        if (string.contains("unix")) {
            return pd.a;
        }
        return pd.e;
    }

    public nl c() {
        return this.aa;
    }

    public void a(da da2) {
        if (this.r instanceof ce) {
            return;
        }
        if (this.r != null) {
            this.r.h();
        }
        if (da2 instanceof fu) {
            this.I.b();
        }
        this.I.c();
        if (da2 == null && this.f == null) {
            da2 = new fu();
        } else if (da2 == null && this.h.Y <= 0) {
            da2 = new ch();
        }
        if (da2 instanceof fu) {
            this.v.b();
        }
        this.r = da2;
        if (da2 != null) {
            this.h();
            qq qq2 = new qq(this.z, this.d, this.e);
            int n2 = qq2.a();
            int n3 = qq2.b();
            da2.a(this, n2, n3);
            this.w = false;
        } else {
            this.g();
        }
    }

    private void c(String string) {
        int n2 = GL11.glGetError();
        if (n2 != 0) {
            String string2 = GLU.gluErrorString((int)n2);
            System.out.println("########## GL ERROR ##########");
            System.out.println("@ " + string);
            System.out.println(n2 + ": " + string2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void d() {
        try {
            this.I.b();
            this.I.c();
            if (this.A != null) {
                this.A.c();
            }
            try {
                if (this.U != null) {
                    this.U.b();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            System.out.println("Stopping!");
            try {
                this.a((fd)null);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                ge.a();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.B.b();
            MinecraftRenderHooks.shutdown();
            Mouse.destroy();
            Keyboard.destroy();
        }
        finally {
            Display.destroy();
            if (!this.R) {
                System.exit(0);
            }
        }
        System.gc();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        this.J = true;
        try {
            this.a();
        }
        catch (Exception exception) {
            exception.printStackTrace();
            this.b(new mh("Failed to start game", exception));
            return;
        }
        try {
            long l2 = System.currentTimeMillis();
            int n2 = 0;
            while (this.J) {
                try {
                    if (this.A != null && !this.A.isActive()) {
                        break;
                    }
                    eq.b();
                    bt.b();
                    if (this.m == null && Display.isCloseRequested()) {
                        this.f();
                    }
                    if (this.o && this.f != null) {
                        float f2 = this.T.c;
                        this.T.a();
                        this.T.c = f2;
                    } else {
                        this.T.a();
                    }
                    long l3 = System.nanoTime();
                    for (int i2 = 0; i2 < this.T.b; ++i2) {
                        ++this.V;
                        try {
                            this.k();
                            continue;
                        }
                        catch (us us2) {
                            this.f = null;
                            this.a((fd)null);
                            this.a(new qh());
                        }
                    }
                    long l4 = System.nanoTime() - l3;
                    this.c("Pre render");
                    cv.a = this.z.j;
                    this.B.a(this.h, this.T.c);
                    GL11.glEnable((int)3553);
                    if (this.f != null) {
                        this.f.j();
                    }
                    if (!Keyboard.isKeyDown((int)65)) {
                        Display.update();
                    }
                    if (this.h != null && this.h.L()) {
                        this.z.A = false;
                    }
                    if (!this.w) {
                        if (this.c != null) {
                            this.c.a(this.T.c);
                        }
                        this.t.b(this.T.c);
                    }
                    if (!Display.isActive()) {
                        if (this.Q) {
                            this.j();
                        }
                        Thread.sleep(10L);
                    }
                    if (this.z.B) {
                        this.a(l4);
                    } else {
                        this.M = System.nanoTime();
                    }
                    this.u.a();
                    Thread.yield();
                    if (Keyboard.isKeyDown((int)65)) {
                        Display.update();
                    }
                    this.z();
                    if (!(this.m == null || this.Q || this.m.getWidth() == this.d && this.m.getHeight() == this.e)) {
                        this.d = this.m.getWidth();
                        this.e = this.m.getHeight();
                        if (this.d <= 0) {
                            this.d = 1;
                        }
                        if (this.e <= 0) {
                            this.e = 1;
                        }
                        this.a(this.d, this.e);
                    }
                    this.c("Post render");
                    ++n2;
                    boolean bl2 = this.o = !this.l() && this.r != null && this.r.c();
                    while (System.currentTimeMillis() >= l2 + 1000L) {
                        this.K = n2 + " fps, " + dk.b + " chunk updates";
                        dk.b = 0;
                        l2 += 1000L;
                        n2 = 0;
                    }
                }
                catch (us us3) {
                    this.f = null;
                    this.a((fd)null);
                    this.a(new qh());
                }
                catch (OutOfMemoryError outOfMemoryError) {
                    this.e();
                    this.a(new x());
                    System.gc();
                }
            }
        }
        catch (xx xx2) {
        }
        catch (Throwable throwable) {
            this.e();
            throwable.printStackTrace();
            this.b(new mh("Unexpected error", throwable));
        }
        finally {
            this.d();
        }
    }

    public void e() {
        try {
            b = new byte[0];
            this.g.f();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            System.gc();
            eq.a();
            bt.a();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            System.gc();
            this.a((fd)null);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        System.gc();
    }

    private void z() {
        if (Keyboard.isKeyDown((int)60)) {
            if (!this.L) {
                this.L = true;
                this.v.a(hj.a(af, this.d, this.e));
            }
        } else {
            this.L = false;
        }
    }

    private void a(long l2) {
        int n2;
        long l3 = 16666666L;
        if (this.M == -1L) {
            this.M = System.nanoTime();
        }
        long l4 = System.nanoTime();
        Minecraft.F[Minecraft.G & Minecraft.E.length - 1] = l2;
        Minecraft.E[Minecraft.G++ & Minecraft.E.length - 1] = l4 - this.M;
        this.M = l4;
        GL11.glClear((int)256);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)0.0, (double)this.d, (double)this.e, (double)0.0, (double)1000.0, (double)3000.0);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-2000.0f);
        GL11.glLineWidth((float)1.0f);
        GL11.glDisable((int)3553);
        nw nw2 = nw.a;
        nw2.a(7);
        int n3 = (int)(l3 / 200000L);
        nw2.b(0x20000000);
        nw2.a(0.0, (double)(this.e - n3), 0.0);
        nw2.a(0.0, (double)this.e, 0.0);
        nw2.a((double)E.length, (double)this.e, 0.0);
        nw2.a((double)E.length, (double)(this.e - n3), 0.0);
        nw2.b(0x20200000);
        nw2.a(0.0, (double)(this.e - n3 * 2), 0.0);
        nw2.a(0.0, (double)(this.e - n3), 0.0);
        nw2.a((double)E.length, (double)(this.e - n3), 0.0);
        nw2.a((double)E.length, (double)(this.e - n3 * 2), 0.0);
        nw2.a();
        long l5 = 0L;
        for (n2 = 0; n2 < E.length; ++n2) {
            l5 += E[n2];
        }
        n2 = (int)(l5 / 200000L / (long)E.length);
        nw2.a(7);
        nw2.b(0x20400000);
        nw2.a(0.0, (double)(this.e - n2), 0.0);
        nw2.a(0.0, (double)this.e, 0.0);
        nw2.a((double)E.length, (double)this.e, 0.0);
        nw2.a((double)E.length, (double)(this.e - n2), 0.0);
        nw2.a();
        nw2.a(1);
        for (int i2 = 0; i2 < E.length; ++i2) {
            int n4 = (i2 - G & E.length - 1) * 255 / E.length;
            int n5 = n4 * n4 / 255;
            n5 = n5 * n5 / 255;
            int n6 = n5 * n5 / 255;
            n6 = n6 * n6 / 255;
            if (E[i2] > l3) {
                nw2.b(-16777216 + n5 * 65536);
            } else {
                nw2.b(-16777216 + n5 * 256);
            }
            long l6 = E[i2] / 200000L;
            long l7 = F[i2] / 200000L;
            nw2.a((double)((float)i2 + 0.5f), (double)((float)((long)this.e - l6) + 0.5f), 0.0);
            nw2.a((double)((float)i2 + 0.5f), (double)((float)this.e + 0.5f), 0.0);
            nw2.b(-16777216 + n5 * 65536 + n5 * 256 + n5 * 1);
            nw2.a((double)((float)i2 + 0.5f), (double)((float)((long)this.e - l6) + 0.5f), 0.0);
            nw2.a((double)((float)i2 + 0.5f), (double)((float)((long)this.e - (l6 - l7)) + 0.5f), 0.0);
        }
        nw2.a();
        GL11.glEnable((int)3553);
    }

    public void f() {
        this.J = false;
    }

    public void g() {
        if (!Display.isActive()) {
            return;
        }
        if (this.N) {
            return;
        }
        this.N = true;
        this.C.a();
        this.a((da)null);
        this.W = 10000;
        this.ag = this.V + 10000;
    }

    public void h() {
        if (!this.N) {
            return;
        }
        if (this.h != null) {
            this.h.o_();
        }
        this.N = false;
        this.C.b();
    }

    public void i() {
        if (this.r != null) {
            return;
        }
        this.a(new oz());
    }

    private void a(int n2, boolean bl2) {
        if (this.c.b) {
            return;
        }
        if (!bl2) {
            this.W = 0;
        }
        if (n2 == 0 && this.W > 0) {
            return;
        }
        if (bl2 && this.y != null && this.y.a == jg.a && n2 == 0) {
            int n3 = this.y.b;
            int n4 = this.y.c;
            int n5 = this.y.d;
            this.c.c(n3, n4, n5, this.y.e);
            this.j.a(n3, n4, n5, this.y.e);
        } else {
            this.c.a();
        }
    }

    private void a(int n2) {
        iz iz2;
        if (n2 == 0 && this.W > 0) {
            return;
        }
        if (n2 == 0) {
            this.h.J();
        }
        boolean bl2 = true;
        if (this.y == null) {
            if (n2 == 0 && !(this.c instanceof pj)) {
                this.W = 10;
            }
        } else if (this.y.a == jg.b) {
            if (n2 == 0) {
                this.c.b(this.h, this.y.g);
            }
            if (n2 == 1) {
                this.c.a(this.h, this.y.g);
            }
        } else if (this.y.a == jg.a) {
            int n3 = this.y.b;
            int n4 = this.y.c;
            int n5 = this.y.d;
            int n6 = this.y.e;
            if (n2 == 0) {
                this.c.a(n3, n4, n5, this.y.e);
            } else {
                int n7;
                iz iz3 = this.h.c.b();
                int n8 = n7 = iz3 != null ? iz3.a : 0;
                if (this.c.a(this.h, this.f, iz3, n3, n4, n5, n6)) {
                    bl2 = false;
                    this.h.J();
                }
                if (iz3 == null) {
                    return;
                }
                if (iz3.a == 0) {
                    this.h.c.a[this.h.c.c] = null;
                } else if (iz3.a != n7) {
                    this.t.c.b();
                }
            }
        }
        if (bl2 && n2 == 1 && (iz2 = this.h.c.b()) != null && this.c.a(this.h, this.f, iz2)) {
            this.t.c.c();
        }
    }

    public void j() {
        try {
            boolean bl2 = this.Q = !this.Q;
            if (this.Q) {
                Display.setDisplayMode((DisplayMode)Display.getDesktopDisplayMode());
                this.d = Display.getDisplayMode().getWidth();
                this.e = Display.getDisplayMode().getHeight();
                if (this.d <= 0) {
                    this.d = 1;
                }
                if (this.e <= 0) {
                    this.e = 1;
                }
            } else {
                if (this.m != null) {
                    this.d = this.m.getWidth();
                    this.e = this.m.getHeight();
                } else {
                    this.d = this.X;
                    this.e = this.Y;
                }
                if (this.d <= 0) {
                    this.d = 1;
                }
                if (this.e <= 0) {
                    this.e = 1;
                }
            }
            if (this.r != null) {
                this.a(this.d, this.e);
            }
            Display.setFullscreen((boolean)this.Q);
            Display.update();
            MinecraftRenderHooks.reinitializeForCurrentDisplay(this.d, this.e);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void a(int n2, int n3) {
        if (n2 <= 0) {
            n2 = 1;
        }
        if (n3 <= 0) {
            n3 = 1;
        }
        this.d = n2;
        this.e = n3;
        MinecraftRenderHooks.resize(n2, n3);
        if (this.r != null) {
            qq qq2 = new qq(this.z, n2, n3);
            int n4 = qq2.a();
            int n5 = qq2.b();
            this.r.a(this, n4, n5);
        }
    }

    private void A() {
        if (this.y != null) {
            int n2 = this.f.a(this.y.b, this.y.c, this.y.d);
            if (n2 == uu.v.bn) {
                n2 = uu.w.bn;
            }
            if (n2 == uu.ak.bn) {
                n2 = uu.al.bn;
            }
            if (n2 == uu.A.bn) {
                n2 = uu.u.bn;
            }
            this.h.c.a(n2, this.c instanceof pj);
        }
    }

    private void B() {
        new kp(this).start();
    }

    public void k() {
        int n2;
        cl cl2;
        if (this.V == 6000) {
            this.B();
        }
        this.I.d();
        this.v.a();
        this.t.a(1.0f);
        if (this.h != null && (cl2 = this.f.w()) instanceof kx) {
            kx kx2 = (kx)cl2;
            n2 = in.d((int)this.h.aM) >> 4;
            int n3 = in.d((int)this.h.aO) >> 4;
            kx2.d(n2, n3);
        }
        if (!this.o && this.f != null) {
            this.c.c();
        }
        GL11.glBindTexture((int)3553, (int)this.p.b("/terrain.png"));
        if (!this.o) {
            this.p.a();
        }
        if (this.r == null && this.h != null) {
            if (this.h.Y <= 0) {
                this.a((da)null);
            } else if (this.h.N() && this.f != null && this.f.B) {
                this.a(new gh());
            }
        } else if (this.r != null && this.r instanceof gh && !this.h.N()) {
            this.a((da)null);
        }
        if (this.r != null) {
            this.W = 10000;
            this.ag = this.V + 10000;
        }
        if (this.r != null) {
            this.r.e();
            if (this.r != null) {
                this.r.h.a();
                this.r.a();
            }
        }
        if (this.r == null || this.r.f) {
            while (Mouse.next()) {
                long l2 = System.currentTimeMillis() - this.P;
                if (l2 > 200L) continue;
                n2 = Mouse.getEventDWheel();
                if (n2 != 0) {
                    this.h.c.b(n2);
                    if (this.z.D) {
                        if (n2 > 0) {
                            n2 = 1;
                        }
                        if (n2 < 0) {
                            n2 = -1;
                        }
                        this.z.G += (float)n2 * 0.25f;
                    }
                }
                if (this.r == null) {
                    if (!this.N && Mouse.getEventButtonState()) {
                        this.g();
                        continue;
                    }
                    if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                        this.a(0);
                        this.ag = this.V;
                    }
                    if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
                        this.a(1);
                        this.ag = this.V;
                    }
                    if (Mouse.getEventButton() != 2 || !Mouse.getEventButtonState()) continue;
                    this.A();
                    continue;
                }
                if (this.r == null) continue;
                this.r.f();
            }
            if (this.W > 0) {
                --this.W;
            }
            while (Keyboard.next()) {
                this.h.a(Keyboard.getEventKey(), Keyboard.getEventKeyState());
                if (!Keyboard.getEventKeyState()) continue;
                if (Keyboard.getEventKey() == 87) {
                    this.j();
                    continue;
                }
                if (this.r != null) {
                    this.r.g();
                } else {
                    if (Keyboard.getEventKey() == 1) {
                        this.i();
                    }
                    if (Keyboard.getEventKey() == 31 && Keyboard.isKeyDown((int)61)) {
                        this.C();
                    }
                    if (Keyboard.getEventKey() == 59) {
                        boolean bl2 = this.z.z = !this.z.z;
                    }
                    if (Keyboard.getEventKey() == 61) {
                        boolean bl3 = this.z.B = !this.z.B;
                    }
                    if (Keyboard.getEventKey() == 63) {
                        boolean bl4 = this.z.A = !this.z.A;
                    }
                    if (Keyboard.getEventKey() == 66) {
                        boolean bl5 = this.z.E = !this.z.E;
                    }
                    if (Keyboard.getEventKey() == this.z.r.b) {
                        this.a(new ue(this.h));
                    }
                    if (Keyboard.getEventKey() == this.z.s.b) {
                        this.h.D();
                    }
                    if (this.l() && Keyboard.getEventKey() == this.z.t.b) {
                        this.a(new gc());
                    }
                }
                for (int i2 = 0; i2 < 9; ++i2) {
                    if (Keyboard.getEventKey() != 2 + i2) continue;
                    this.h.c.c = i2;
                }
                if (Keyboard.getEventKey() != this.z.u.b) continue;
                this.z.a(ht.e, Keyboard.isKeyDown((int)42) || Keyboard.isKeyDown((int)54) ? -1 : 1);
            }
            if (this.r == null) {
                if (Mouse.isButtonDown((int)0) && (float)(this.V - this.ag) >= this.T.a / 4.0f && this.N) {
                    this.a(0);
                    this.ag = this.V;
                }
                if (Mouse.isButtonDown((int)1) && (float)(this.V - this.ag) >= this.T.a / 4.0f && this.N) {
                    this.a(1);
                    this.ag = this.V;
                }
            }
            this.a(0, this.r == null && Mouse.isButtonDown((int)0) && this.N);
        }
        if (this.f != null) {
            if (this.h != null) {
                ++this.ah;
                if (this.ah == 30) {
                    this.ah = 0;
                    this.f.g(this.h);
                }
            }
            this.f.q = this.z.y;
            if (this.f.B) {
                this.f.q = 3;
            }
            if (!this.o) {
                this.t.a();
            }
            if (!this.o) {
                this.g.d();
            }
            if (!this.o) {
                if (this.f.n > 0) {
                    --this.f.n;
                }
                this.f.g();
            }
            if (!this.o || this.l()) {
                this.f.a(this.z.y > 0, true);
                this.f.l();
            }
            if (!this.o && this.f != null) {
                this.f.q(in.b(this.h.aM), in.b(this.h.aN), in.b(this.h.aO));
            }
            if (!this.o) {
                this.j.a();
            }
        }
        this.P = System.currentTimeMillis();
    }

    private void C() {
        System.out.println("FORCING RELOAD!");
        this.B = new yo();
        this.B.a(this.z);
        this.U.a();
    }

    public boolean l() {
        return this.f != null && this.f.B;
    }

    public void a(String string, String string2, long l2) {
        this.a((fd)null);
        System.gc();
        if (this.aa.a(string)) {
            this.b(string, string2);
        } else {
            wt wt2 = this.aa.a(string, false);
            fd fd2 = null;
            fd2 = new fd(wt2, string2, l2);
            if (fd2.s) {
                this.I.a(jl.g, 1);
                this.I.a(jl.f, 1);
                this.a(fd2, "Generating level");
            } else {
                this.I.a(jl.h, 1);
                this.I.a(jl.f, 1);
                this.a(fd2, "Loading level");
            }
        }
    }

    public void m() {
        System.out.println("Toggling dimension!!");
        this.h.m = this.h.m == -1 ? 0 : -1;
        this.f.e(this.h);
        this.h.be = false;
        double d2 = this.h.aM;
        double d3 = this.h.aO;
        double d4 = 8.0;
        if (this.h.m == -1) {
            this.h.c(d2 /= d4, this.h.aN, d3 /= d4, this.h.aS, this.h.aT);
            if (this.h.W()) {
                this.f.a((sn)this.h, false);
            }
            fd fd2 = null;
            fd2 = new fd(this.f, xa.a(-1));
            this.a(fd2, "Entering the Nether", this.h);
        } else {
            this.h.c(d2 *= d4, this.h.aN, d3 *= d4, this.h.aS, this.h.aT);
            if (this.h.W()) {
                this.f.a((sn)this.h, false);
            }
            fd fd3 = null;
            fd3 = new fd(this.f, xa.a(0));
            this.a(fd3, "Leaving the Nether", this.h);
        }
        this.h.aI = this.f;
        if (this.h.W()) {
            this.h.c(d2, this.h.aN, d3, this.h.aS, this.h.aT);
            this.f.a((sn)this.h, false);
            new ur().a(this.f, this.h);
        }
    }

    public void a(fd fd2) {
        this.a(fd2, "");
    }

    public void a(fd fd2, String string) {
        this.a(fd2, string, null);
    }

    public void a(fd fd2, String string, gs gs2) {
        this.I.b();
        this.I.c();
        this.i = null;
        this.s.a(string);
        this.s.d("");
        this.B.a(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        if (this.f != null) {
            this.f.a(this.s);
        }
        this.f = fd2;
        if (fd2 != null) {
            cl cl2;
            this.c.a(fd2);
            if (!this.l()) {
                if (gs2 == null) {
                    this.h = (dc)fd2.a(dc.class);
                }
            } else if (this.h != null) {
                this.h.t_();
                if (fd2 != null) {
                    fd2.b(this.h);
                }
            }
            if (!fd2.B) {
                this.d(string);
            }
            if (this.h == null) {
                this.h = (dc)this.c.b(fd2);
                this.h.t_();
                this.c.a(this.h);
            }
            this.h.a = new lr(this.z);
            if (this.g != null) {
                this.g.a(fd2);
            }
            if (this.j != null) {
                this.j.a(fd2);
            }
            this.c.b(this.h);
            if (gs2 != null) {
                fd2.e();
            }
            if ((cl2 = fd2.w()) instanceof kx) {
                kx kx2 = (kx)cl2;
                int n2 = in.d((int)this.h.aM) >> 4;
                int n3 = in.d((int)this.h.aO) >> 4;
                kx2.d(n2, n3);
            }
            fd2.a(this.h);
            if (fd2.s) {
                fd2.a(this.s);
            }
            this.i = this.h;
        } else {
            this.h = null;
        }
        System.gc();
        this.P = 0L;
    }

    private void b(String string, String string2) {
        this.s.a("Converting World to " + this.aa.a());
        this.s.d("This may take a while :)");
        this.aa.a(string, this.s);
        this.a(string, string2, 0L);
    }

    private void d(String string) {
        this.s.a(string);
        this.s.d("Building terrain");
        int n2 = 128;
        int n3 = 0;
        int n4 = n2 * 2 / 16 + 1;
        n4 *= n4;
        cl cl2 = this.f.w();
        br br2 = this.f.u();
        if (this.h != null) {
            br2.a = (int)this.h.aM;
            br2.c = (int)this.h.aO;
        }
        if (cl2 instanceof kx) {
            kx kx2 = (kx)cl2;
            kx2.d(br2.a >> 4, br2.c >> 4);
        }
        for (int i2 = -n2; i2 <= n2; i2 += 16) {
            for (int i3 = -n2; i3 <= n2; i3 += 16) {
                this.s.a(n3++ * 100 / n4);
                this.f.a(br2.a + i2, 64, br2.c + i3);
                while (this.f.j()) {
                }
            }
        }
        this.s.d("Simulating world for a bit");
        n4 = 2000;
        this.f.p();
    }

    public void a(String string, File file) {
        int n2 = string.indexOf("/");
        String string2 = string.substring(0, n2);
        string = string.substring(n2 + 1);
        if (string2.equalsIgnoreCase("sound")) {
            this.B.a(string, file);
        } else if (string2.equalsIgnoreCase("newsound")) {
            this.B.a(string, file);
        } else if (string2.equalsIgnoreCase("streaming")) {
            this.B.b(string, file);
        } else if (string2.equalsIgnoreCase("music")) {
            this.B.c(string, file);
        } else if (string2.equalsIgnoreCase("newmusic")) {
            this.B.c(string, file);
        }
    }

    public cx n() {
        return this.S;
    }

    public String o() {
        return this.g.b();
    }

    public String p() {
        return this.g.c();
    }

    public String q() {
        return this.f.i();
    }

    public String r() {
        return "P: " + this.j.b() + ". T: " + this.f.h();
    }

    public void a(boolean bl2, int n2) {
        cl cl2;
        if (!this.f.B && !this.f.t.f()) {
            this.m();
        }
        br br2 = null;
        br br3 = null;
        boolean bl3 = true;
        if (this.h != null && !bl2 && (br2 = this.h.Q()) != null && (br3 = gs.a(this.f, br2)) == null) {
            this.h.b("tile.bed.notValid");
        }
        if (br3 == null) {
            br3 = this.f.u();
            bl3 = false;
        }
        if ((cl2 = this.f.w()) instanceof kx) {
            kx kx2 = (kx)cl2;
            kx2.d(br3.a >> 4, br3.c >> 4);
        }
        this.f.d();
        this.f.v();
        int n3 = 0;
        if (this.h != null) {
            n3 = this.h.aD;
            this.f.e(this.h);
        }
        this.i = null;
        this.h = (dc)this.c.b(this.f);
        this.h.m = n2;
        this.i = this.h;
        this.h.t_();
        if (bl3) {
            this.h.a(br2);
            this.h.c((float)br3.a + 0.5f, (float)br3.b + 0.1f, (float)br3.c + 0.5f, 0.0f, 0.0f);
        }
        this.c.a(this.h);
        this.f.a(this.h);
        this.h.a = new lr(this.z);
        this.h.aD = n3;
        this.h.v();
        this.c.b(this.h);
        this.d("Respawning");
        if (this.r instanceof ch) {
            this.a((da)null);
        }
    }

    public static void a(String string, String string2) {
        Minecraft.a(string, string2, null);
    }

    public static void a(String string, String string2, String string3) {
        boolean bl2 = false;
        String string4 = string;
        Frame frame = new Frame("Minecraft");
        Canvas canvas = new Canvas();
        frame.setLayout(new BorderLayout());
        frame.add((Component)canvas, "Center");
        canvas.setPreferredSize(new Dimension(854, 480));
        frame.pack();
        frame.setLocationRelativeTo(null);
        kq kq2 = new kq(frame, canvas, null, 854, 480, bl2, frame);
        Thread thread = new Thread((Runnable)kq2, "Minecraft main thread");
        thread.setPriority(10);
        kq2.l = "www.minecraft.net";
        kq2.k = string4 != null && string2 != null ? new gr(string4, string2) : new gr("Player" + System.currentTimeMillis() % 1000L, "");
        if (string3 != null) {
            String[] stringArray = string3.split(":");
            kq2.a(stringArray[0], Integer.parseInt(stringArray[1]));
        }
        frame.setVisible(true);
        frame.addWindowListener(new kj(kq2, thread));
        thread.start();
    }

    public nb s() {
        if (this.h instanceof tk) {
            return ((tk)this.h).bN;
        }
        return null;
    }

    public static void main(String[] stringArray) {
        String string = null;
        String string2 = null;
        string = "Player" + System.currentTimeMillis() % 1000L;
        if (stringArray.length > 0) {
            string = stringArray[0];
        }
        string2 = "-";
        if (stringArray.length > 1) {
            string2 = stringArray[1];
        }
        Minecraft.a(string, string2);
    }

    public static boolean t() {
        return a == null || !Minecraft.a.z.z;
    }

    public static boolean u() {
        return a != null && Minecraft.a.z.j;
    }

    public static boolean v() {
        return a != null && Minecraft.a.z.k;
    }

    public static boolean w() {
        return a != null && Minecraft.a.z.B;
    }

    public boolean b(String string) {
        if (string.startsWith("/")) {
            // empty if block
        }
        return false;
    }

    static {
        E = new long[512];
        F = new long[512];
        G = 0;
        H = 0L;
        af = null;
    }
}

