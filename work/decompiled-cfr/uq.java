/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class uq
extends ub {
    private static bb d = new bb();
    private List e = new ArrayList();
    private Random f = new Random();
    private Minecraft g;
    public String a = null;
    private int h = 0;
    private String i = "";
    private int j = 0;
    private boolean l = false;
    public float b;
    float c = 1.0f;

    public uq(Minecraft minecraft) {
        this.g = minecraft;
    }

    public void a(float f2, boolean bl2, int n2, int n3) {
        String string;
        int n4;
        int n5;
        int n6;
        boolean bl3;
        float f3;
        qq qq2 = new qq(this.g.z, this.g.d, this.g.e);
        int n7 = qq2.a();
        int n8 = qq2.b();
        sj sj2 = this.g.q;
        this.g.t.b();
        GL11.glEnable((int)3042);
        if (Minecraft.u()) {
            this.a(this.g.h.a(f2), n7, n8);
        }
        iz iz2 = this.g.h.c.d(3);
        if (!this.g.z.A && iz2 != null && iz2.c == uu.bb.bn) {
            this.a(n7, n8);
        }
        if ((f3 = this.g.h.C + (this.g.h.B - this.g.h.C) * f2) > 0.0f) {
            this.b(f3, n7, n8);
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glBindTexture((int)3553, (int)this.g.p.b("/gui/gui.png"));
        ix ix2 = this.g.h.c;
        this.k = -90.0f;
        this.b(n7 / 2 - 91, n8 - 22, 0, 0, 182, 22);
        this.b(n7 / 2 - 91 - 1 + ix2.c * 20, n8 - 22 - 1, 0, 22, 24, 22);
        GL11.glBindTexture((int)3553, (int)this.g.p.b("/gui/icons.png"));
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)775, (int)769);
        this.b(n7 / 2 - 7, n8 / 2 - 7, 0, 0, 16, 16);
        GL11.glDisable((int)3042);
        boolean bl4 = bl3 = this.g.h.by / 3 % 2 == 1;
        if (this.g.h.by < 10) {
            bl3 = false;
        }
        int n9 = this.g.h.Y;
        int n10 = this.g.h.Z;
        this.f.setSeed(this.h * 312871);
        if (this.g.c.d()) {
            int n11;
            n6 = this.g.h.s();
            for (n5 = 0; n5 < 10; ++n5) {
                n4 = n8 - 32;
                if (n6 > 0) {
                    n11 = n7 / 2 + 91 - n5 * 8 - 9;
                    if (n5 * 2 + 1 < n6) {
                        this.b(n11, n4, 34, 9, 9, 9);
                    }
                    if (n5 * 2 + 1 == n6) {
                        this.b(n11, n4, 25, 9, 9, 9);
                    }
                    if (n5 * 2 + 1 > n6) {
                        this.b(n11, n4, 16, 9, 9, 9);
                    }
                }
                n11 = 0;
                if (bl3) {
                    n11 = 1;
                }
                int n12 = n7 / 2 - 91 + n5 * 8;
                if (n9 <= 4) {
                    n4 += this.f.nextInt(2);
                }
                this.b(n12, n4, 16 + n11 * 9, 0, 9, 9);
                if (bl3) {
                    if (n5 * 2 + 1 < n10) {
                        this.b(n12, n4, 70, 0, 9, 9);
                    }
                    if (n5 * 2 + 1 == n10) {
                        this.b(n12, n4, 79, 0, 9, 9);
                    }
                }
                if (n5 * 2 + 1 < n9) {
                    this.b(n12, n4, 52, 0, 9, 9);
                }
                if (n5 * 2 + 1 != n9) continue;
                this.b(n12, n4, 61, 0, 9, 9);
            }
            if (this.g.h.a(ln.g)) {
                n5 = (int)Math.ceil((double)(this.g.h.bz - 2) * 10.0 / 300.0);
                n4 = (int)Math.ceil((double)this.g.h.bz * 10.0 / 300.0) - n5;
                for (n11 = 0; n11 < n5 + n4; ++n11) {
                    if (n11 < n5) {
                        this.b(n7 / 2 - 91 + n11 * 8, n8 - 32 - 9, 16, 18, 9, 9);
                        continue;
                    }
                    this.b(n7 / 2 - 91 + n11 * 8, n8 - 32 - 9, 25, 18, 9, 9);
                }
            }
        }
        GL11.glDisable((int)3042);
        GL11.glEnable((int)32826);
        GL11.glPushMatrix();
        GL11.glRotatef((float)120.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        u.b();
        GL11.glPopMatrix();
        for (n6 = 0; n6 < 9; ++n6) {
            n5 = n7 / 2 - 90 + n6 * 20 + 2;
            n4 = n8 - 16 - 3;
            this.a(n6, n5, n4, f2);
        }
        u.a();
        GL11.glDisable((int)32826);
        if (this.g.h.P() > 0) {
            GL11.glDisable((int)2929);
            GL11.glDisable((int)3008);
            n6 = this.g.h.P();
            float f4 = (float)n6 / 100.0f;
            if (f4 > 1.0f) {
                f4 = 1.0f - (float)(n6 - 100) / 10.0f;
            }
            n4 = (int)(220.0f * f4) << 24 | 0x101020;
            this.a(0, 0, n7, n8, n4);
            GL11.glEnable((int)3008);
            GL11.glEnable((int)2929);
        }
        if (this.g.z.B) {
            GL11.glPushMatrix();
            if (Minecraft.H > 0L) {
                GL11.glTranslatef((float)0.0f, (float)32.0f, (float)0.0f);
            }
            sj2.a("Minecraft Beta 1.7.3 (" + this.g.K + ")", 2, 2, 0xFFFFFF);
            sj2.a(this.g.o(), 2, 12, 0xFFFFFF);
            sj2.a(this.g.p(), 2, 22, 0xFFFFFF);
            sj2.a(this.g.r(), 2, 32, 0xFFFFFF);
            sj2.a(this.g.q(), 2, 42, 0xFFFFFF);
            long l2 = Runtime.getRuntime().maxMemory();
            long l3 = Runtime.getRuntime().totalMemory();
            long l4 = Runtime.getRuntime().freeMemory();
            long l5 = l3 - l4;
            string = "Used memory: " + l5 * 100L / l2 + "% (" + l5 / 1024L / 1024L + "MB) of " + l2 / 1024L / 1024L + "MB";
            this.b(sj2, string, n7 - sj2.a(string) - 2, 2, 0xE0E0E0);
            string = "Allocated memory: " + l3 * 100L / l2 + "% (" + l3 / 1024L / 1024L + "MB)";
            this.b(sj2, string, n7 - sj2.a(string) - 2, 12, 0xE0E0E0);
            this.b(sj2, "x: " + this.g.h.aM, 2, 64, 0xE0E0E0);
            this.b(sj2, "y: " + this.g.h.aN, 2, 72, 0xE0E0E0);
            this.b(sj2, "z: " + this.g.h.aO, 2, 80, 0xE0E0E0);
            this.b(sj2, "f: " + (in.b((double)(this.g.h.aS * 4.0f / 360.0f) + 0.5) & 3), 2, 88, 0xE0E0E0);
            GL11.glPopMatrix();
        }
        if (this.j > 0) {
            float f5 = (float)this.j - f2;
            int n13 = (int)(f5 * 256.0f / 20.0f);
            if (n13 > 255) {
                n13 = 255;
            }
            if (n13 > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(n7 / 2), (float)(n8 - 48), (float)0.0f);
                GL11.glEnable((int)3042);
                GL11.glBlendFunc((int)770, (int)771);
                int n14 = 0xFFFFFF;
                if (this.l) {
                    n14 = Color.HSBtoRGB(f5 / 50.0f, 0.7f, 0.6f) & 0xFFFFFF;
                }
                sj2.b(this.i, -sj2.a(this.i) / 2, -4, n14 + (n13 << 24));
                GL11.glDisable((int)3042);
                GL11.glPopMatrix();
            }
        }
        int n15 = 10;
        boolean bl5 = false;
        if (this.g.r instanceof gc) {
            n15 = 20;
            bl5 = true;
        }
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3008);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)0.0f, (float)(n8 - 48), (float)0.0f);
        for (int i2 = 0; i2 < this.e.size() && i2 < n15; ++i2) {
            if (((sw)this.e.get((int)i2)).b >= 200 && !bl5) continue;
            double d2 = (double)((sw)this.e.get((int)i2)).b / 200.0;
            d2 = 1.0 - d2;
            if ((d2 *= 10.0) < 0.0) {
                d2 = 0.0;
            }
            if (d2 > 1.0) {
                d2 = 1.0;
            }
            d2 *= d2;
            int n16 = (int)(255.0 * d2);
            if (bl5) {
                n16 = 255;
            }
            if (n16 <= 0) continue;
            int n17 = 2;
            int n18 = -i2 * 9;
            string = ((sw)this.e.get((int)i2)).a;
            this.a(n17, n18 - 1, n17 + 320, n18 + 8, n16 / 2 << 24);
            GL11.glEnable((int)3042);
            sj2.a(string, n17, n18, 0xFFFFFF + (n16 << 24));
        }
        GL11.glPopMatrix();
        GL11.glEnable((int)3008);
        GL11.glDisable((int)3042);
    }

    private void a(int n2, int n3) {
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3008);
        GL11.glBindTexture((int)3553, (int)this.g.p.b("%blur%/misc/pumpkinblur.png"));
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(0.0, n3, -90.0, 0.0, 1.0);
        nw2.a(n2, n3, -90.0, 1.0, 1.0);
        nw2.a(n2, 0.0, -90.0, 1.0, 0.0);
        nw2.a(0.0, 0.0, -90.0, 0.0, 0.0);
        nw2.a();
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
        GL11.glEnable((int)3008);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void a(float f2, int n2, int n3) {
        if ((f2 = 1.0f - f2) < 0.0f) {
            f2 = 0.0f;
        }
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        this.c = (float)((double)this.c + (double)(f2 - this.c) * 0.01);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glBlendFunc((int)0, (int)769);
        GL11.glColor4f((float)this.c, (float)this.c, (float)this.c, (float)1.0f);
        GL11.glBindTexture((int)3553, (int)this.g.p.b("%blur%/misc/vignette.png"));
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(0.0, n3, -90.0, 0.0, 1.0);
        nw2.a(n2, n3, -90.0, 1.0, 1.0);
        nw2.a(n2, 0.0, -90.0, 1.0, 0.0);
        nw2.a(0.0, 0.0, -90.0, 0.0, 0.0);
        nw2.a();
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glBlendFunc((int)770, (int)771);
    }

    private void b(float f2, int n2, int n3) {
        if (f2 < 1.0f) {
            f2 *= f2;
            f2 *= f2;
            f2 = f2 * 0.8f + 0.2f;
        }
        GL11.glDisable((int)3008);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)f2);
        GL11.glBindTexture((int)3553, (int)this.g.p.b("/terrain.png"));
        float f3 = (float)(uu.bf.bm % 16) / 16.0f;
        float f4 = (float)(uu.bf.bm / 16) / 16.0f;
        float f5 = (float)(uu.bf.bm % 16 + 1) / 16.0f;
        float f6 = (float)(uu.bf.bm / 16 + 1) / 16.0f;
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(0.0, n3, -90.0, f3, f6);
        nw2.a(n2, n3, -90.0, f5, f6);
        nw2.a(n2, 0.0, -90.0, f5, f4);
        nw2.a(0.0, 0.0, -90.0, f3, f4);
        nw2.a();
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
        GL11.glEnable((int)3008);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void a(int n2, int n3, int n4, float f2) {
        iz iz2 = this.g.h.c.a[n2];
        if (iz2 == null) {
            return;
        }
        float f3 = (float)iz2.b - f2;
        if (f3 > 0.0f) {
            GL11.glPushMatrix();
            float f4 = 1.0f + f3 / 5.0f;
            GL11.glTranslatef((float)(n3 + 8), (float)(n4 + 12), (float)0.0f);
            GL11.glScalef((float)(1.0f / f4), (float)((f4 + 1.0f) / 2.0f), (float)1.0f);
            GL11.glTranslatef((float)(-(n3 + 8)), (float)(-(n4 + 12)), (float)0.0f);
        }
        d.a(this.g.q, this.g.p, iz2, n3, n4);
        if (f3 > 0.0f) {
            GL11.glPopMatrix();
        }
        d.b(this.g.q, this.g.p, iz2, n3, n4);
    }

    public void a() {
        if (this.j > 0) {
            --this.j;
        }
        ++this.h;
        for (int i2 = 0; i2 < this.e.size(); ++i2) {
            ++((sw)this.e.get((int)i2)).b;
        }
    }

    public void b() {
        this.e.clear();
    }

    public void a(String string) {
        while (this.g.q.a(string) > 320) {
            int n2;
            for (n2 = 1; n2 < string.length() && this.g.q.a(string.substring(0, n2 + 1)) <= 320; ++n2) {
            }
            this.a(string.substring(0, n2));
            string = string.substring(n2);
        }
        this.e.add(0, new sw(string));
        while (this.e.size() > 50) {
            this.e.remove(this.e.size() - 1);
        }
    }

    public void b(String string) {
        this.i = "Now playing: " + string;
        this.j = 60;
        this.l = true;
    }

    public void c(String string) {
        nh nh2 = nh.a();
        String string2 = nh2.a(string);
        this.a(string2);
    }
}

