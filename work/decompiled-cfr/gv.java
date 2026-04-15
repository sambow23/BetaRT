/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class gv
extends bw {
    protected ko e;
    protected ko f;

    public gv(ko ko2, float f2) {
        this.e = ko2;
        this.c = f2;
    }

    public void a(ko ko2) {
        this.f = ko2;
    }

    public void a(ls ls2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glPushMatrix();
        GL11.glDisable((int)2884);
        this.e.m = this.d(ls2, f3);
        if (this.f != null) {
            this.f.m = this.e.m;
        }
        this.e.n = ls2.al();
        if (this.f != null) {
            this.f.n = this.e.n;
        }
        try {
            float f4 = ls2.I + (ls2.H - ls2.I) * f3;
            float f5 = ls2.aU + (ls2.aS - ls2.aU) * f3;
            float f6 = ls2.aV + (ls2.aT - ls2.aV) * f3;
            this.b(ls2, d2, d3, d4);
            float f7 = this.c(ls2, f3);
            this.a(ls2, f7, f4, f3);
            float f8 = 0.0625f;
            GL11.glEnable((int)32826);
            GL11.glScalef((float)-1.0f, (float)-1.0f, (float)1.0f);
            this.a(ls2, f3);
            GL11.glTranslatef((float)0.0f, (float)(-24.0f * f8 - 0.0078125f), (float)0.0f);
            float f9 = ls2.ak + (ls2.al - ls2.ak) * f3;
            float f10 = ls2.am - ls2.al * (1.0f - f3);
            if (f9 > 1.0f) {
                f9 = 1.0f;
            }
            this.a(ls2.bA, ls2.q_());
            GL11.glEnable((int)3008);
            this.e.a(ls2, f10, f9, f3);
            this.e.a(f10, f9, f7, f5 - f4, f6, f8);
            for (int i2 = 0; i2 < 4; ++i2) {
                if (!this.a(ls2, i2, f3)) continue;
                this.f.a(f10, f9, f7, f5 - f4, f6, f8);
                GL11.glDisable((int)3042);
                GL11.glEnable((int)3008);
            }
            this.b(ls2, f3);
            float f11 = ls2.a(f3);
            int n2 = this.a(ls2, f11, f3);
            if ((n2 >> 24 & 0xFF) > 0 || ls2.aa > 0 || ls2.ad > 0) {
                GL11.glDisable((int)3553);
                GL11.glDisable((int)3008);
                GL11.glEnable((int)3042);
                GL11.glBlendFunc((int)770, (int)771);
                GL11.glDepthFunc((int)514);
                if (ls2.aa > 0 || ls2.ad > 0) {
                    GL11.glColor4f((float)f11, (float)0.0f, (float)0.0f, (float)0.4f);
                    this.e.a(f10, f9, f7, f5 - f4, f6, f8);
                    for (int i3 = 0; i3 < 4; ++i3) {
                        if (!this.b(ls2, i3, f3)) continue;
                        GL11.glColor4f((float)f11, (float)0.0f, (float)0.0f, (float)0.4f);
                        this.f.a(f10, f9, f7, f5 - f4, f6, f8);
                    }
                }
                if ((n2 >> 24 & 0xFF) > 0) {
                    float f12 = (float)(n2 >> 16 & 0xFF) / 255.0f;
                    float f13 = (float)(n2 >> 8 & 0xFF) / 255.0f;
                    float f14 = (float)(n2 & 0xFF) / 255.0f;
                    float f15 = (float)(n2 >> 24 & 0xFF) / 255.0f;
                    GL11.glColor4f((float)f12, (float)f13, (float)f14, (float)f15);
                    this.e.a(f10, f9, f7, f5 - f4, f6, f8);
                    for (int i4 = 0; i4 < 4; ++i4) {
                        if (!this.b(ls2, i4, f3)) continue;
                        GL11.glColor4f((float)f12, (float)f13, (float)f14, (float)f15);
                        this.f.a(f10, f9, f7, f5 - f4, f6, f8);
                    }
                }
                GL11.glDepthFunc((int)515);
                GL11.glDisable((int)3042);
                GL11.glEnable((int)3008);
                GL11.glEnable((int)3553);
            }
            GL11.glDisable((int)32826);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        GL11.glEnable((int)2884);
        GL11.glPopMatrix();
        this.a(ls2, d2, d3, d4);
    }

    protected void b(ls ls2, double d2, double d3, double d4) {
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
    }

    protected void a(ls ls2, float f2, float f3, float f4) {
        GL11.glRotatef((float)(180.0f - f3), (float)0.0f, (float)1.0f, (float)0.0f);
        if (ls2.ad > 0) {
            float f5 = ((float)ls2.ad + f4 - 1.0f) / 20.0f * 1.6f;
            if ((f5 = in.c(f5)) > 1.0f) {
                f5 = 1.0f;
            }
            GL11.glRotatef((float)(f5 * this.a(ls2)), (float)0.0f, (float)0.0f, (float)1.0f);
        }
    }

    protected float d(ls ls2, float f2) {
        return ls2.d(f2);
    }

    protected float c(ls ls2, float f2) {
        return (float)ls2.bt + f2;
    }

    protected void b(ls ls2, float f2) {
    }

    protected boolean b(ls ls2, int n2, float f2) {
        return this.a(ls2, n2, f2);
    }

    protected boolean a(ls ls2, int n2, float f2) {
        return false;
    }

    protected float a(ls ls2) {
        return 90.0f;
    }

    protected int a(ls ls2, float f2, float f3) {
        return 0;
    }

    protected void a(ls ls2, float f2) {
    }

    protected void a(ls ls2, double d2, double d3, double d4) {
        if (Minecraft.w()) {
            this.a(ls2, Integer.toString(ls2.aD), d2, d3, d4, 64);
        }
    }

    protected void a(ls ls2, String string, double d2, double d3, double d4, int n2) {
        float f2 = ls2.f(this.b.h);
        if (f2 > (float)n2) {
            return;
        }
        sj sj2 = this.a();
        float f3 = 1.6f;
        float f4 = 0.016666668f * f3;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2 + 0.0f), (float)((float)d3 + 2.3f), (float)((float)d4));
        GL11.glNormal3f((float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-this.b.i), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)this.b.j, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glScalef((float)(-f4), (float)(-f4), (float)f4);
        GL11.glDisable((int)2896);
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        nw nw2 = nw.a;
        int n3 = 0;
        if (string.equals("deadmau5")) {
            n3 = -10;
        }
        GL11.glDisable((int)3553);
        nw2.b();
        int n4 = sj2.a(string) / 2;
        nw2.a(0.0f, 0.0f, 0.0f, 0.25f);
        nw2.a((double)(-n4 - 1), (double)(-1 + n3), 0.0);
        nw2.a((double)(-n4 - 1), (double)(8 + n3), 0.0);
        nw2.a((double)(n4 + 1), (double)(8 + n3), 0.0);
        nw2.a((double)(n4 + 1), (double)(-1 + n3), 0.0);
        nw2.a();
        GL11.glEnable((int)3553);
        sj2.b(string, -sj2.a(string) / 2, n3, 0x20FFFFFF);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        sj2.b(string, -sj2.a(string) / 2, n3, -1);
        GL11.glEnable((int)2896);
        GL11.glDisable((int)3042);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glPopMatrix();
    }
}

