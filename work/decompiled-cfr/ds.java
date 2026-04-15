/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ds
extends gv {
    private fh a;
    private fh g;
    private fh h;
    private static final String[] i = new String[]{"cloth", "chain", "iron", "diamond", "gold"};

    public ds() {
        super(new fh(0.0f), 0.5f);
        this.a = (fh)this.e;
        this.g = new fh(1.0f);
        this.h = new fh(0.5f);
    }

    protected boolean a(gs gs2, int n2, float f2) {
        gm gm2;
        iz iz2 = gs2.c.d(3 - n2);
        if (iz2 != null && (gm2 = iz2.a()) instanceof wa) {
            wa wa2 = (wa)gm2;
            this.a("/armor/" + i[wa2.bm] + "_" + (n2 == 2 ? 2 : 1) + ".png");
            fh fh2 = n2 == 2 ? this.h : this.g;
            fh2.a.h = n2 == 0;
            fh2.b.h = n2 == 0;
            fh2.c.h = n2 == 1 || n2 == 2;
            fh2.d.h = n2 == 1;
            fh2.e.h = n2 == 1;
            fh2.f.h = n2 == 2 || n2 == 3;
            fh2.g.h = n2 == 2 || n2 == 3;
            this.a(fh2);
            return true;
        }
        return false;
    }

    public void a(gs gs2, double d2, double d3, double d4, float f2, float f3) {
        iz iz2 = gs2.c.b();
        this.a.k = iz2 != null;
        this.h.k = this.a.k;
        this.g.k = this.a.k;
        this.h.l = this.a.l = gs2.t();
        this.g.l = this.a.l;
        double d5 = d3 - (double)gs2.bf;
        if (gs2.t() && !(gs2 instanceof dc)) {
            d5 -= 0.125;
        }
        super.a((ls)gs2, d2, d5, d4, f2, f3);
        this.a.l = false;
        this.h.l = false;
        this.g.l = false;
        this.a.k = false;
        this.h.k = false;
        this.g.k = false;
    }

    protected void a(gs gs2, double d2, double d3, double d4) {
        if (Minecraft.t() && gs2 != this.b.h) {
            float f2;
            float f3 = 1.6f;
            float f4 = 0.016666668f * f3;
            float f5 = gs2.f(this.b.h);
            float f6 = f2 = gs2.t() ? 32.0f : 64.0f;
            if (f5 < f2) {
                String string = gs2.l;
                if (!gs2.t()) {
                    if (gs2.N()) {
                        this.a((ls)gs2, string, d2, d3 - 1.5, d4, 64);
                    } else {
                        this.a((ls)gs2, string, d2, d3, d4, 64);
                    }
                } else {
                    sj sj2 = this.a();
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)((float)d2 + 0.0f), (float)((float)d3 + 2.3f), (float)((float)d4));
                    GL11.glNormal3f((float)0.0f, (float)1.0f, (float)0.0f);
                    GL11.glRotatef((float)(-this.b.i), (float)0.0f, (float)1.0f, (float)0.0f);
                    GL11.glRotatef((float)this.b.j, (float)1.0f, (float)0.0f, (float)0.0f);
                    GL11.glScalef((float)(-f4), (float)(-f4), (float)f4);
                    GL11.glDisable((int)2896);
                    GL11.glTranslatef((float)0.0f, (float)(0.25f / f4), (float)0.0f);
                    GL11.glDepthMask((boolean)false);
                    GL11.glEnable((int)3042);
                    GL11.glBlendFunc((int)770, (int)771);
                    nw nw2 = nw.a;
                    GL11.glDisable((int)3553);
                    nw2.b();
                    int n2 = sj2.a(string) / 2;
                    nw2.a(0.0f, 0.0f, 0.0f, 0.25f);
                    nw2.a((double)(-n2 - 1), -1.0, 0.0);
                    nw2.a((double)(-n2 - 1), 8.0, 0.0);
                    nw2.a((double)(n2 + 1), 8.0, 0.0);
                    nw2.a((double)(n2 + 1), -1.0, 0.0);
                    nw2.a();
                    GL11.glEnable((int)3553);
                    GL11.glDepthMask((boolean)true);
                    sj2.b(string, -sj2.a(string) / 2, 0, 0x20FFFFFF);
                    GL11.glEnable((int)2896);
                    GL11.glDisable((int)3042);
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    protected void a(gs gs2, float f2) {
        iz iz2;
        float f3;
        iz iz3 = gs2.c.d(3);
        if (iz3 != null && iz3.a().bf < 256) {
            GL11.glPushMatrix();
            this.a.a.c(0.0625f);
            if (cv.a(uu.m[iz3.c].b())) {
                float f4 = 0.625f;
                GL11.glTranslatef((float)0.0f, (float)-0.25f, (float)0.0f);
                GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glScalef((float)f4, (float)(-f4), (float)f4);
            }
            this.b.f.a(gs2, iz3);
            GL11.glPopMatrix();
        }
        if (gs2.l.equals("deadmau5") && this.a(gs2.bA, null)) {
            for (int i2 = 0; i2 < 2; ++i2) {
                f3 = gs2.aU + (gs2.aS - gs2.aU) * f2 - (gs2.I + (gs2.H - gs2.I) * f2);
                float f5 = gs2.aV + (gs2.aT - gs2.aV) * f2;
                GL11.glPushMatrix();
                GL11.glRotatef((float)f3, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glRotatef((float)f5, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glTranslatef((float)(0.375f * (float)(i2 * 2 - 1)), (float)0.0f, (float)0.0f);
                GL11.glTranslatef((float)0.0f, (float)-0.375f, (float)0.0f);
                GL11.glRotatef((float)(-f5), (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)(-f3), (float)0.0f, (float)1.0f, (float)0.0f);
                float f6 = 1.3333334f;
                GL11.glScalef((float)f6, (float)f6, (float)f6);
                this.a.a(0.0625f);
                GL11.glPopMatrix();
            }
        }
        if (this.a(gs2.n, null)) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.125f);
            double d2 = gs2.o + (gs2.r - gs2.o) * (double)f2 - (gs2.aJ + (gs2.aM - gs2.aJ) * (double)f2);
            double d3 = gs2.p + (gs2.s - gs2.p) * (double)f2 - (gs2.aK + (gs2.aN - gs2.aK) * (double)f2);
            double d4 = gs2.q + (gs2.t - gs2.q) * (double)f2 - (gs2.aL + (gs2.aO - gs2.aL) * (double)f2);
            float f7 = gs2.I + (gs2.H - gs2.I) * f2;
            double d5 = in.a(f7 * (float)Math.PI / 180.0f);
            double d6 = -in.b(f7 * (float)Math.PI / 180.0f);
            float f8 = (float)d3 * 10.0f;
            if (f8 < -6.0f) {
                f8 = -6.0f;
            }
            if (f8 > 32.0f) {
                f8 = 32.0f;
            }
            float f9 = (float)(d2 * d5 + d4 * d6) * 100.0f;
            float f10 = (float)(d2 * d6 - d4 * d5) * 100.0f;
            if (f9 < 0.0f) {
                f9 = 0.0f;
            }
            float f11 = gs2.h + (gs2.i - gs2.h) * f2;
            f8 += in.a((gs2.bi + (gs2.bj - gs2.bi) * f2) * 6.0f) * 32.0f * f11;
            if (gs2.t()) {
                f8 += 25.0f;
            }
            GL11.glRotatef((float)(6.0f + f9 / 2.0f + f8), (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glRotatef((float)(f10 / 2.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)(-f10 / 2.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            this.a.b(0.0625f);
            GL11.glPopMatrix();
        }
        if ((iz2 = gs2.c.b()) != null) {
            GL11.glPushMatrix();
            this.a.d.c(0.0625f);
            GL11.glTranslatef((float)-0.0625f, (float)0.4375f, (float)0.0625f);
            if (gs2.D != null) {
                iz2 = new iz(gm.B);
            }
            if (iz2.c < 256 && cv.a(uu.m[iz2.c].b())) {
                f3 = 0.5f;
                GL11.glTranslatef((float)0.0f, (float)0.1875f, (float)-0.3125f);
                GL11.glRotatef((float)20.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glScalef((float)(f3 *= 0.75f), (float)(-f3), (float)f3);
            } else if (gm.c[iz2.c].b()) {
                f3 = 0.625f;
                if (gm.c[iz2.c].c()) {
                    GL11.glRotatef((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                    GL11.glTranslatef((float)0.0f, (float)-0.125f, (float)0.0f);
                }
                GL11.glTranslatef((float)0.0f, (float)0.1875f, (float)0.0f);
                GL11.glScalef((float)f3, (float)(-f3), (float)f3);
                GL11.glRotatef((float)-100.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            } else {
                f3 = 0.375f;
                GL11.glTranslatef((float)0.25f, (float)0.1875f, (float)-0.1875f);
                GL11.glScalef((float)f3, (float)f3, (float)f3);
                GL11.glRotatef((float)60.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glRotatef((float)-90.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)20.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            }
            this.b.f.a(gs2, iz2);
            GL11.glPopMatrix();
        }
    }

    protected void b(gs gs2, float f2) {
        float f3 = 0.9375f;
        GL11.glScalef((float)f3, (float)f3, (float)f3);
    }

    public void b() {
        this.a.m = 0.0f;
        this.a.b(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
        this.a.d.a(0.0625f);
    }

    protected void b(gs gs2, double d2, double d3, double d4) {
        if (gs2.W() && gs2.N()) {
            super.b(gs2, d2 + (double)gs2.w, d3 + (double)gs2.x, d4 + (double)gs2.y);
        } else {
            super.b(gs2, d2, d3, d4);
        }
    }

    protected void a(gs gs2, float f2, float f3, float f4) {
        if (gs2.W() && gs2.N()) {
            GL11.glRotatef((float)gs2.M(), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)this.a(gs2), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)270.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        } else {
            super.a((ls)gs2, f2, f3, f4);
        }
    }
}

