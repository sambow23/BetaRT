/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ra {
    private Minecraft a;
    private iz b = null;
    private float c = 0.0f;
    private float d = 0.0f;
    private cv e = new cv();
    private iy f;
    private int g = -1;

    public ra(Minecraft minecraft) {
        this.a = minecraft;
        this.f = new iy(minecraft.q, minecraft.z, minecraft.p);
    }

    public void a(ls ls2, iz iz2) {
        GL11.glPushMatrix();
        if (iz2.c < 256 && cv.a(uu.m[iz2.c].b())) {
            GL11.glBindTexture((int)3553, (int)this.a.p.b("/terrain.png"));
            this.e.a(uu.m[iz2.c], iz2.i(), ls2.a(1.0f));
        } else {
            float f2;
            float f3;
            float f4;
            int n2;
            if (iz2.c < 256) {
                GL11.glBindTexture((int)3553, (int)this.a.p.b("/terrain.png"));
            } else {
                GL11.glBindTexture((int)3553, (int)this.a.p.b("/gui/items.png"));
            }
            nw nw2 = nw.a;
            int n3 = ls2.c(iz2);
            float f5 = ((float)(n3 % 16 * 16) + 0.0f) / 256.0f;
            float f6 = ((float)(n3 % 16 * 16) + 15.99f) / 256.0f;
            float f7 = ((float)(n3 / 16 * 16) + 0.0f) / 256.0f;
            float f8 = ((float)(n3 / 16 * 16) + 15.99f) / 256.0f;
            float f9 = 1.0f;
            float f10 = 0.0f;
            float f11 = 0.3f;
            GL11.glEnable((int)32826);
            GL11.glTranslatef((float)(-f10), (float)(-f11), (float)0.0f);
            float f12 = 1.5f;
            GL11.glScalef((float)f12, (float)f12, (float)f12);
            GL11.glRotatef((float)50.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)335.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glTranslatef((float)-0.9375f, (float)-0.0625f, (float)0.0f);
            float f13 = 0.0625f;
            nw2.b();
            nw2.b(0.0f, 0.0f, 1.0f);
            nw2.a(0.0, 0.0, 0.0, f6, f8);
            nw2.a(f9, 0.0, 0.0, f5, f8);
            nw2.a(f9, 1.0, 0.0, f5, f7);
            nw2.a(0.0, 1.0, 0.0, f6, f7);
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 0.0f, -1.0f);
            nw2.a(0.0, 1.0, 0.0f - f13, f6, f7);
            nw2.a(f9, 1.0, 0.0f - f13, f5, f7);
            nw2.a(f9, 0.0, 0.0f - f13, f5, f8);
            nw2.a(0.0, 0.0, 0.0f - f13, f6, f8);
            nw2.a();
            nw2.b();
            nw2.b(-1.0f, 0.0f, 0.0f);
            for (n2 = 0; n2 < 16; ++n2) {
                f4 = (float)n2 / 16.0f;
                f3 = f6 + (f5 - f6) * f4 - 0.001953125f;
                f2 = f9 * f4;
                nw2.a(f2, 0.0, 0.0f - f13, f3, f8);
                nw2.a(f2, 0.0, 0.0, f3, f8);
                nw2.a(f2, 1.0, 0.0, f3, f7);
                nw2.a(f2, 1.0, 0.0f - f13, f3, f7);
            }
            nw2.a();
            nw2.b();
            nw2.b(1.0f, 0.0f, 0.0f);
            for (n2 = 0; n2 < 16; ++n2) {
                f4 = (float)n2 / 16.0f;
                f3 = f6 + (f5 - f6) * f4 - 0.001953125f;
                f2 = f9 * f4 + 0.0625f;
                nw2.a(f2, 1.0, 0.0f - f13, f3, f7);
                nw2.a(f2, 1.0, 0.0, f3, f7);
                nw2.a(f2, 0.0, 0.0, f3, f8);
                nw2.a(f2, 0.0, 0.0f - f13, f3, f8);
            }
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 1.0f, 0.0f);
            for (n2 = 0; n2 < 16; ++n2) {
                f4 = (float)n2 / 16.0f;
                f3 = f8 + (f7 - f8) * f4 - 0.001953125f;
                f2 = f9 * f4 + 0.0625f;
                nw2.a(0.0, f2, 0.0, f6, f3);
                nw2.a(f9, f2, 0.0, f5, f3);
                nw2.a(f9, f2, 0.0f - f13, f5, f3);
                nw2.a(0.0, f2, 0.0f - f13, f6, f3);
            }
            nw2.a();
            nw2.b();
            nw2.b(0.0f, -1.0f, 0.0f);
            for (n2 = 0; n2 < 16; ++n2) {
                f4 = (float)n2 / 16.0f;
                f3 = f8 + (f7 - f8) * f4 - 0.001953125f;
                f2 = f9 * f4;
                nw2.a(f9, f2, 0.0, f5, f3);
                nw2.a(0.0, f2, 0.0, f6, f3);
                nw2.a(0.0, f2, 0.0f - f13, f6, f3);
                nw2.a(f9, f2, 0.0f - f13, f5, f3);
            }
            nw2.a();
            GL11.glDisable((int)32826);
        }
        GL11.glPopMatrix();
    }

    public void a(float f2) {
        float f3;
        float f4;
        float f5;
        float f6 = this.d + (this.c - this.d) * f2;
        dc dc2 = this.a.h;
        float f7 = dc2.aV + (dc2.aT - dc2.aV) * f2;
        GL11.glPushMatrix();
        GL11.glRotatef((float)f7, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)(dc2.aU + (dc2.aS - dc2.aU) * f2), (float)0.0f, (float)1.0f, (float)0.0f);
        u.b();
        GL11.glPopMatrix();
        iz iz2 = this.b;
        float f8 = this.a.f.c(in.b(dc2.aM), in.b(dc2.aN), in.b(dc2.aO));
        if (iz2 != null) {
            int n2 = gm.c[iz2.c].f(iz2.i());
            f5 = (float)(n2 >> 16 & 0xFF) / 255.0f;
            f4 = (float)(n2 >> 8 & 0xFF) / 255.0f;
            f3 = (float)(n2 & 0xFF) / 255.0f;
            GL11.glColor4f((float)(f8 * f5), (float)(f8 * f4), (float)(f8 * f3), (float)1.0f);
        } else {
            GL11.glColor4f((float)f8, (float)f8, (float)f8, (float)1.0f);
        }
        if (iz2 != null && iz2.c == gm.bb.bf) {
            GL11.glPushMatrix();
            float f9 = 0.8f;
            f5 = dc2.d(f2);
            f4 = in.a(f5 * (float)Math.PI);
            f3 = in.a(in.c(f5) * (float)Math.PI);
            GL11.glTranslatef((float)(-f3 * 0.4f), (float)(in.a(in.c(f5) * (float)Math.PI * 2.0f) * 0.2f), (float)(-f4 * 0.2f));
            f5 = 1.0f - f7 / 45.0f + 0.1f;
            if (f5 < 0.0f) {
                f5 = 0.0f;
            }
            if (f5 > 1.0f) {
                f5 = 1.0f;
            }
            f5 = -in.b(f5 * (float)Math.PI) * 0.5f + 0.5f;
            GL11.glTranslatef((float)0.0f, (float)(0.0f * f9 - (1.0f - f6) * 1.2f - f5 * 0.5f + 0.04f), (float)(-0.9f * f9));
            GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)(f5 * -85.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glEnable((int)32826);
            GL11.glBindTexture((int)3553, (int)this.a.p.a(this.a.h.bA, this.a.h.q_()));
            for (int i2 = 0; i2 < 2; ++i2) {
                int n3 = i2 * 2 - 1;
                GL11.glPushMatrix();
                GL11.glTranslatef((float)-0.0f, (float)-0.6f, (float)(1.1f * (float)n3));
                GL11.glRotatef((float)(-45 * n3), (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)-90.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glRotatef((float)59.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glRotatef((float)(-65 * n3), (float)0.0f, (float)1.0f, (float)0.0f);
                bw bw2 = th.a.a(this.a.h);
                ds ds2 = (ds)bw2;
                float f10 = 1.0f;
                GL11.glScalef((float)f10, (float)f10, (float)f10);
                ds2.b();
                GL11.glPopMatrix();
            }
            float f11 = dc2.d(f2);
            f3 = in.a(f11 * f11 * (float)Math.PI);
            float f12 = in.a(in.c(f11) * (float)Math.PI);
            GL11.glRotatef((float)(-f3 * 20.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)(-f12 * 20.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)(-f12 * 80.0f), (float)1.0f, (float)0.0f, (float)0.0f);
            f11 = 0.38f;
            GL11.glScalef((float)f11, (float)f11, (float)f11);
            GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glTranslatef((float)-1.0f, (float)-1.0f, (float)0.0f);
            f3 = 0.015625f;
            GL11.glScalef((float)f3, (float)f3, (float)f3);
            this.a.p.b(this.a.p.b("/misc/mapbg.png"));
            nw nw2 = nw.a;
            GL11.glNormal3f((float)0.0f, (float)0.0f, (float)-1.0f);
            nw2.b();
            int n4 = 7;
            nw2.a(0 - n4, 128 + n4, 0.0, 0.0, 1.0);
            nw2.a(128 + n4, 128 + n4, 0.0, 1.0, 1.0);
            nw2.a(128 + n4, 0 - n4, 0.0, 1.0, 0.0);
            nw2.a(0 - n4, 0 - n4, 0.0, 0.0, 0.0);
            nw2.a();
            iu iu2 = gm.bb.a(iz2, this.a.f);
            this.f.a(this.a.h, this.a.p, iu2);
            GL11.glPopMatrix();
        } else if (iz2 != null) {
            GL11.glPushMatrix();
            float f13 = 0.8f;
            f5 = dc2.d(f2);
            f4 = in.a(f5 * (float)Math.PI);
            f3 = in.a(in.c(f5) * (float)Math.PI);
            GL11.glTranslatef((float)(-f3 * 0.4f), (float)(in.a(in.c(f5) * (float)Math.PI * 2.0f) * 0.2f), (float)(-f4 * 0.2f));
            GL11.glTranslatef((float)(0.7f * f13), (float)(-0.65f * f13 - (1.0f - f6) * 0.6f), (float)(-0.9f * f13));
            GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glEnable((int)32826);
            f5 = dc2.d(f2);
            f4 = in.a(f5 * f5 * (float)Math.PI);
            f3 = in.a(in.c(f5) * (float)Math.PI);
            GL11.glRotatef((float)(-f4 * 20.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)(-f3 * 20.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)(-f3 * 80.0f), (float)1.0f, (float)0.0f, (float)0.0f);
            f5 = 0.4f;
            GL11.glScalef((float)f5, (float)f5, (float)f5);
            if (iz2.a().c()) {
                GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            }
            this.a(dc2, iz2);
            GL11.glPopMatrix();
        } else {
            GL11.glPushMatrix();
            float f14 = 0.8f;
            f5 = dc2.d(f2);
            f4 = in.a(f5 * (float)Math.PI);
            f3 = in.a(in.c(f5) * (float)Math.PI);
            GL11.glTranslatef((float)(-f3 * 0.3f), (float)(in.a(in.c(f5) * (float)Math.PI * 2.0f) * 0.4f), (float)(-f4 * 0.4f));
            GL11.glTranslatef((float)(0.8f * f14), (float)(-0.75f * f14 - (1.0f - f6) * 0.6f), (float)(-0.9f * f14));
            GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glEnable((int)32826);
            f5 = dc2.d(f2);
            f4 = in.a(f5 * f5 * (float)Math.PI);
            f3 = in.a(in.c(f5) * (float)Math.PI);
            GL11.glRotatef((float)(f3 * 70.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)(-f4 * 20.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glBindTexture((int)3553, (int)this.a.p.a(this.a.h.bA, this.a.h.q_()));
            GL11.glTranslatef((float)-1.0f, (float)3.6f, (float)3.5f);
            GL11.glRotatef((float)120.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)200.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glRotatef((float)-135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glScalef((float)1.0f, (float)1.0f, (float)1.0f);
            GL11.glTranslatef((float)5.6f, (float)0.0f, (float)0.0f);
            bw bw3 = th.a.a(this.a.h);
            ds ds3 = (ds)bw3;
            f3 = 1.0f;
            GL11.glScalef((float)f3, (float)f3, (float)f3);
            ds3.b();
            GL11.glPopMatrix();
        }
        GL11.glDisable((int)32826);
        u.a();
    }

    public void b(float f2) {
        int n2;
        GL11.glDisable((int)3008);
        if (this.a.h.ak()) {
            n2 = this.a.p.b("/terrain.png");
            GL11.glBindTexture((int)3553, (int)n2);
            this.d(f2);
        }
        if (this.a.h.L()) {
            n2 = in.b(this.a.h.aM);
            int n3 = in.b(this.a.h.aN);
            int n4 = in.b(this.a.h.aO);
            int n5 = this.a.p.b("/terrain.png");
            GL11.glBindTexture((int)3553, (int)n5);
            int n6 = this.a.f.a(n2, n3, n4);
            if (this.a.f.h(n2, n3, n4)) {
                this.a(f2, uu.m[n6].a(2));
            } else {
                for (int i2 = 0; i2 < 8; ++i2) {
                    int n7;
                    int n8;
                    float f3 = ((float)((i2 >> 0) % 2) - 0.5f) * this.a.h.bg * 0.9f;
                    float f4 = ((float)((i2 >> 1) % 2) - 0.5f) * this.a.h.bh * 0.2f;
                    float f5 = ((float)((i2 >> 2) % 2) - 0.5f) * this.a.h.bg * 0.9f;
                    int n9 = in.d((float)n2 + f3);
                    if (!this.a.f.h(n9, n8 = in.d((float)n3 + f4), n7 = in.d((float)n4 + f5))) continue;
                    n6 = this.a.f.a(n9, n8, n7);
                }
            }
            if (uu.m[n6] != null) {
                this.a(f2, uu.m[n6].a(2));
            }
        }
        if (this.a.h.a(ln.g)) {
            n2 = this.a.p.b("/misc/water.png");
            GL11.glBindTexture((int)3553, (int)n2);
            this.c(f2);
        }
        GL11.glEnable((int)3008);
    }

    private void a(float f2, int n2) {
        nw nw2 = nw.a;
        float f3 = this.a.h.a(f2);
        f3 = 0.1f;
        GL11.glColor4f((float)f3, (float)f3, (float)f3, (float)0.5f);
        GL11.glPushMatrix();
        float f4 = -1.0f;
        float f5 = 1.0f;
        float f6 = -1.0f;
        float f7 = 1.0f;
        float f8 = -0.5f;
        float f9 = 0.0078125f;
        float f10 = (float)(n2 % 16) / 256.0f - f9;
        float f11 = ((float)(n2 % 16) + 15.99f) / 256.0f + f9;
        float f12 = (float)(n2 / 16) / 256.0f - f9;
        float f13 = ((float)(n2 / 16) + 15.99f) / 256.0f + f9;
        nw2.b();
        nw2.a(f4, f6, f8, f11, f13);
        nw2.a(f5, f6, f8, f10, f13);
        nw2.a(f5, f7, f8, f10, f12);
        nw2.a(f4, f7, f8, f11, f12);
        nw2.a();
        GL11.glPopMatrix();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void c(float f2) {
        nw nw2 = nw.a;
        float f3 = this.a.h.a(f2);
        GL11.glColor4f((float)f3, (float)f3, (float)f3, (float)0.5f);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glPushMatrix();
        float f4 = 4.0f;
        float f5 = -1.0f;
        float f6 = 1.0f;
        float f7 = -1.0f;
        float f8 = 1.0f;
        float f9 = -0.5f;
        float f10 = -this.a.h.aS / 64.0f;
        float f11 = this.a.h.aT / 64.0f;
        nw2.b();
        nw2.a(f5, f7, f9, f4 + f10, f4 + f11);
        nw2.a(f6, f7, f9, 0.0f + f10, f4 + f11);
        nw2.a(f6, f8, f9, 0.0f + f10, 0.0f + f11);
        nw2.a(f5, f8, f9, f4 + f10, 0.0f + f11);
        nw2.a();
        GL11.glPopMatrix();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
    }

    private void d(float f2) {
        nw nw2 = nw.a;
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)0.9f);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        float f3 = 1.0f;
        for (int i2 = 0; i2 < 2; ++i2) {
            GL11.glPushMatrix();
            int n2 = uu.as.bm + i2 * 16;
            int n3 = (n2 & 0xF) << 4;
            int n4 = n2 & 0xF0;
            float f4 = (float)n3 / 256.0f;
            float f5 = ((float)n3 + 15.99f) / 256.0f;
            float f6 = (float)n4 / 256.0f;
            float f7 = ((float)n4 + 15.99f) / 256.0f;
            float f8 = (0.0f - f3) / 2.0f;
            float f9 = f8 + f3;
            float f10 = 0.0f - f3 / 2.0f;
            float f11 = f10 + f3;
            float f12 = -0.5f;
            GL11.glTranslatef((float)((float)(-(i2 * 2 - 1)) * 0.24f), (float)-0.3f, (float)0.0f);
            GL11.glRotatef((float)((float)(i2 * 2 - 1) * 10.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            nw2.b();
            nw2.a(f8, f10, f12, f5, f7);
            nw2.a(f9, f10, f12, f4, f7);
            nw2.a(f9, f11, f12, f4, f6);
            nw2.a(f8, f11, f12, f5, f6);
            nw2.a();
            GL11.glPopMatrix();
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
    }

    public void a() {
        float f2;
        float f3;
        float f4;
        boolean bl2;
        iz iz2;
        this.d = this.c;
        dc dc2 = this.a.h;
        iz iz3 = iz2 = dc2.c.b();
        boolean bl3 = bl2 = this.g == dc2.c.c && iz3 == this.b;
        if (this.b == null && iz3 == null) {
            bl2 = true;
        }
        if (iz3 != null && this.b != null && iz3 != this.b && iz3.c == this.b.c && iz3.i() == this.b.i()) {
            this.b = iz3;
            bl2 = true;
        }
        if ((f4 = (f3 = bl2 ? 1.0f : 0.0f) - this.c) < -(f2 = 0.4f)) {
            f4 = -f2;
        }
        if (f4 > f2) {
            f4 = f2;
        }
        this.c += f4;
        if (this.c < 0.1f) {
            this.b = iz3;
            this.g = dc2.c.c;
        }
    }

    public void b() {
        this.c = 0.0f;
    }

    public void c() {
        this.c = 0.0f;
    }
}

