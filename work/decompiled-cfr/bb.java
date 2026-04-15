/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.Random;
import org.lwjgl.opengl.GL11;

public class bb
extends bw {
    private cv e = new cv();
    private Random f = new Random();
    public boolean a = true;

    public bb() {
        this.c = 0.15f;
        this.d = 0.75f;
    }

    public void a(hl hl2, double d2, double d3, double d4, float f2, float f3) {
        this.f.setSeed(187L);
        iz iz2 = hl2.a;
        GL11.glPushMatrix();
        float f4 = in.a(((float)hl2.b + f3) / 10.0f + hl2.d) * 0.1f + 0.1f;
        float f5 = (((float)hl2.b + f3) / 20.0f + hl2.d) * 57.295776f;
        int n2 = 1;
        if (hl2.a.a > 1) {
            n2 = 2;
        }
        if (hl2.a.a > 5) {
            n2 = 3;
        }
        if (hl2.a.a > 20) {
            n2 = 4;
        }
        GL11.glTranslatef((float)((float)d2), (float)((float)d3 + f4), (float)((float)d4));
        GL11.glEnable((int)32826);
        if (iz2.c < 256 && cv.a(uu.m[iz2.c].b())) {
            GL11.glRotatef((float)f5, (float)0.0f, (float)1.0f, (float)0.0f);
            this.a("/terrain.png");
            float f6 = 0.25f;
            if (!uu.m[iz2.c].d() && iz2.c != uu.al.bn && uu.m[iz2.c].b() != 16) {
                f6 = 0.5f;
            }
            GL11.glScalef((float)f6, (float)f6, (float)f6);
            for (int i2 = 0; i2 < n2; ++i2) {
                GL11.glPushMatrix();
                if (i2 > 0) {
                    float f7 = (this.f.nextFloat() * 2.0f - 1.0f) * 0.2f / f6;
                    float f8 = (this.f.nextFloat() * 2.0f - 1.0f) * 0.2f / f6;
                    float f9 = (this.f.nextFloat() * 2.0f - 1.0f) * 0.2f / f6;
                    GL11.glTranslatef((float)f7, (float)f8, (float)f9);
                }
                this.e.a(uu.m[iz2.c], iz2.i(), hl2.a(f3));
                GL11.glPopMatrix();
            }
        } else {
            float f10;
            float f11;
            float f12;
            int n3;
            GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
            int n4 = iz2.b();
            if (iz2.c < 256) {
                this.a("/terrain.png");
            } else {
                this.a("/gui/items.png");
            }
            nw nw2 = nw.a;
            float f13 = (float)(n4 % 16 * 16 + 0) / 256.0f;
            float f14 = (float)(n4 % 16 * 16 + 16) / 256.0f;
            float f15 = (float)(n4 / 16 * 16 + 0) / 256.0f;
            float f16 = (float)(n4 / 16 * 16 + 16) / 256.0f;
            float f17 = 1.0f;
            float f18 = 0.5f;
            float f19 = 0.25f;
            if (this.a) {
                n3 = gm.c[iz2.c].f(iz2.i());
                f12 = (float)(n3 >> 16 & 0xFF) / 255.0f;
                f11 = (float)(n3 >> 8 & 0xFF) / 255.0f;
                f10 = (float)(n3 & 0xFF) / 255.0f;
                float f20 = hl2.a(f3);
                GL11.glColor4f((float)(f12 * f20), (float)(f11 * f20), (float)(f10 * f20), (float)1.0f);
            }
            for (n3 = 0; n3 < n2; ++n3) {
                GL11.glPushMatrix();
                if (n3 > 0) {
                    f12 = (this.f.nextFloat() * 2.0f - 1.0f) * 0.3f;
                    f11 = (this.f.nextFloat() * 2.0f - 1.0f) * 0.3f;
                    f10 = (this.f.nextFloat() * 2.0f - 1.0f) * 0.3f;
                    GL11.glTranslatef((float)f12, (float)f11, (float)f10);
                }
                GL11.glRotatef((float)(180.0f - this.b.i), (float)0.0f, (float)1.0f, (float)0.0f);
                nw2.b();
                nw2.b(0.0f, 1.0f, 0.0f);
                nw2.a(0.0f - f18, 0.0f - f19, 0.0, f13, f16);
                nw2.a(f17 - f18, 0.0f - f19, 0.0, f14, f16);
                nw2.a(f17 - f18, 1.0f - f19, 0.0, f14, f15);
                nw2.a(0.0f - f18, 1.0f - f19, 0.0, f13, f15);
                nw2.a();
                GL11.glPopMatrix();
            }
        }
        GL11.glDisable((int)32826);
        GL11.glPopMatrix();
    }

    public void a(sj sj2, ji ji2, int n2, int n3, int n4, int n5, int n6) {
        if (n2 < 256 && cv.a(uu.m[n2].b())) {
            int n7 = n2;
            ji2.b(ji2.b("/terrain.png"));
            uu uu2 = uu.m[n7];
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(n5 - 2), (float)(n6 + 3), (float)-3.0f);
            GL11.glScalef((float)10.0f, (float)10.0f, (float)10.0f);
            GL11.glTranslatef((float)1.0f, (float)0.5f, (float)1.0f);
            GL11.glScalef((float)1.0f, (float)1.0f, (float)-1.0f);
            GL11.glRotatef((float)210.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            int n8 = gm.c[n2].f(n3);
            float f2 = (float)(n8 >> 16 & 0xFF) / 255.0f;
            float f3 = (float)(n8 >> 8 & 0xFF) / 255.0f;
            float f4 = (float)(n8 & 0xFF) / 255.0f;
            if (this.a) {
                GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)1.0f);
            }
            GL11.glRotatef((float)-90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            this.e.b = this.a;
            this.e.a(uu2, n3, 1.0f);
            this.e.b = true;
            GL11.glPopMatrix();
        } else if (n4 >= 0) {
            GL11.glDisable((int)2896);
            if (n2 < 256) {
                ji2.b(ji2.b("/terrain.png"));
            } else {
                ji2.b(ji2.b("/gui/items.png"));
            }
            int n9 = gm.c[n2].f(n3);
            float f5 = (float)(n9 >> 16 & 0xFF) / 255.0f;
            float f6 = (float)(n9 >> 8 & 0xFF) / 255.0f;
            float f7 = (float)(n9 & 0xFF) / 255.0f;
            if (this.a) {
                GL11.glColor4f((float)f5, (float)f6, (float)f7, (float)1.0f);
            }
            this.a(n5, n6, n4 % 16 * 16, n4 / 16 * 16, 16, 16);
            GL11.glEnable((int)2896);
        }
        GL11.glEnable((int)2884);
    }

    public void a(sj sj2, ji ji2, iz iz2, int n2, int n3) {
        if (iz2 == null) {
            return;
        }
        this.a(sj2, ji2, iz2.c, iz2.i(), iz2.b(), n2, n3);
    }

    public void b(sj sj2, ji ji2, iz iz2, int n2, int n3) {
        if (iz2 == null) {
            return;
        }
        if (iz2.a > 1) {
            String string = "" + iz2.a;
            GL11.glDisable((int)2896);
            GL11.glDisable((int)2929);
            sj2.a(string, n2 + 19 - 2 - sj2.a(string), n3 + 6 + 3, 0xFFFFFF);
            GL11.glEnable((int)2896);
            GL11.glEnable((int)2929);
        }
        if (iz2.g()) {
            int n4 = (int)Math.round(13.0 - (double)iz2.h() * 13.0 / (double)iz2.j());
            int n5 = (int)Math.round(255.0 - (double)iz2.h() * 255.0 / (double)iz2.j());
            GL11.glDisable((int)2896);
            GL11.glDisable((int)2929);
            GL11.glDisable((int)3553);
            nw nw2 = nw.a;
            int n6 = 255 - n5 << 16 | n5 << 8;
            int n7 = (255 - n5) / 4 << 16 | 0x3F00;
            this.a(nw2, n2 + 2, n3 + 13, 13, 2, 0);
            this.a(nw2, n2 + 2, n3 + 13, 12, 1, n7);
            this.a(nw2, n2 + 2, n3 + 13, n4, 1, n6);
            GL11.glEnable((int)3553);
            GL11.glEnable((int)2896);
            GL11.glEnable((int)2929);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    private void a(nw nw2, int n2, int n3, int n4, int n5, int n6) {
        nw2.b();
        nw2.b(n6);
        nw2.a((double)(n2 + 0), (double)(n3 + 0), 0.0);
        nw2.a((double)(n2 + 0), (double)(n3 + n5), 0.0);
        nw2.a((double)(n2 + n4), (double)(n3 + n5), 0.0);
        nw2.a((double)(n2 + n4), (double)(n3 + 0), 0.0);
        nw2.a();
    }

    public void a(int n2, int n3, int n4, int n5, int n6, int n7) {
        float f2 = 0.0f;
        float f3 = 0.00390625f;
        float f4 = 0.00390625f;
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(n2 + 0, n3 + n7, f2, (float)(n4 + 0) * f3, (float)(n5 + n7) * f4);
        nw2.a(n2 + n6, n3 + n7, f2, (float)(n4 + n6) * f3, (float)(n5 + n7) * f4);
        nw2.a(n2 + n6, n3 + 0, f2, (float)(n4 + n6) * f3, (float)(n5 + 0) * f4);
        nw2.a(n2 + 0, n3 + 0, f2, (float)(n4 + 0) * f3, (float)(n5 + 0) * f4);
        nw2.a();
    }
}

