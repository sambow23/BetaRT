/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public abstract class bw {
    protected th b;
    private ko a = new fh();
    private cv e = new cv();
    protected float c = 0.0f;
    protected float d = 1.0f;

    public abstract void a(sn var1, double var2, double var4, double var6, float var8, float var9);

    protected void a(String string) {
        ji ji2 = this.b.e;
        ji2.b(ji2.b(string));
    }

    protected boolean a(String string, String string2) {
        ji ji2 = this.b.e;
        int n2 = ji2.a(string, string2);
        if (n2 >= 0) {
            ji2.b(n2);
            return true;
        }
        return false;
    }

    private void a(sn sn2, double d2, double d3, double d4, float f2) {
        GL11.glDisable((int)2896);
        int n2 = uu.as.bm;
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        float f3 = (float)n3 / 256.0f;
        float f4 = ((float)n3 + 15.99f) / 256.0f;
        float f5 = (float)n4 / 256.0f;
        float f6 = ((float)n4 + 15.99f) / 256.0f;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
        float f7 = sn2.bg * 1.4f;
        GL11.glScalef((float)f7, (float)f7, (float)f7);
        this.a("/terrain.png");
        nw nw2 = nw.a;
        float f8 = 0.5f;
        float f9 = 0.0f;
        float f10 = sn2.bh / f7;
        float f11 = (float)(sn2.aN - sn2.aW.b);
        GL11.glRotatef((float)(-this.b.i), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)(-0.3f + (float)((int)f10) * 0.02f));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        float f12 = 0.0f;
        int n5 = 0;
        nw2.b();
        while (f10 > 0.0f) {
            if (n5 % 2 == 0) {
                f3 = (float)n3 / 256.0f;
                f4 = ((float)n3 + 15.99f) / 256.0f;
                f5 = (float)n4 / 256.0f;
                f6 = ((float)n4 + 15.99f) / 256.0f;
            } else {
                f3 = (float)n3 / 256.0f;
                f4 = ((float)n3 + 15.99f) / 256.0f;
                f5 = (float)(n4 + 16) / 256.0f;
                f6 = ((float)(n4 + 16) + 15.99f) / 256.0f;
            }
            if (n5 / 2 % 2 == 0) {
                float f13 = f4;
                f4 = f3;
                f3 = f13;
            }
            nw2.a(f8 - f9, 0.0f - f11, f12, f4, f6);
            nw2.a(-f8 - f9, 0.0f - f11, f12, f3, f6);
            nw2.a(-f8 - f9, 1.4f - f11, f12, f3, f5);
            nw2.a(f8 - f9, 1.4f - f11, f12, f4, f5);
            f10 -= 0.45f;
            f11 -= 0.45f;
            f8 *= 0.9f;
            f12 += 0.03f;
            ++n5;
        }
        nw2.a();
        GL11.glPopMatrix();
        GL11.glEnable((int)2896);
    }

    private void c(sn sn2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        ji ji2 = this.b.e;
        ji2.b(ji2.b("%clamp%/misc/shadow.png"));
        fd fd2 = this.b();
        GL11.glDepthMask((boolean)false);
        float f4 = this.c;
        double d5 = sn2.bl + (sn2.aM - sn2.bl) * (double)f3;
        double d6 = sn2.bm + (sn2.aN - sn2.bm) * (double)f3 + (double)sn2.x_();
        double d7 = sn2.bn + (sn2.aO - sn2.bn) * (double)f3;
        int n2 = in.b(d5 - (double)f4);
        int n3 = in.b(d5 + (double)f4);
        int n4 = in.b(d6 - (double)f4);
        int n5 = in.b(d6);
        int n6 = in.b(d7 - (double)f4);
        int n7 = in.b(d7 + (double)f4);
        double d8 = d2 - d5;
        double d9 = d3 - d6;
        double d10 = d4 - d7;
        nw nw2 = nw.a;
        nw2.b();
        for (int i2 = n2; i2 <= n3; ++i2) {
            for (int i3 = n4; i3 <= n5; ++i3) {
                for (int i4 = n6; i4 <= n7; ++i4) {
                    int n8 = fd2.a(i2, i3 - 1, i4);
                    if (n8 <= 0 || fd2.n(i2, i3, i4) <= 3) continue;
                    this.a(uu.m[n8], d2, d3 + (double)sn2.x_(), d4, i2, i3, i4, f2, f4, d8, d9 + (double)sn2.x_(), d10);
                }
            }
        }
        nw2.a();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
        GL11.glDepthMask((boolean)true);
    }

    private fd b() {
        return this.b.g;
    }

    private void a(uu uu2, double d2, double d3, double d4, int n2, int n3, int n4, float f2, float f3, double d5, double d6, double d7) {
        nw nw2 = nw.a;
        if (!uu2.d()) {
            return;
        }
        double d8 = ((double)f2 - (d3 - ((double)n3 + d6)) / 2.0) * 0.5 * (double)this.b().c(n2, n3, n4);
        if (d8 < 0.0) {
            return;
        }
        if (d8 > 1.0) {
            d8 = 1.0;
        }
        nw2.a(1.0f, 1.0f, 1.0f, (float)d8);
        double d9 = (double)n2 + uu2.bs + d5;
        double d10 = (double)n2 + uu2.bv + d5;
        double d11 = (double)n3 + uu2.bt + d6 + 0.015625;
        double d12 = (double)n4 + uu2.bu + d7;
        double d13 = (double)n4 + uu2.bx + d7;
        float f4 = (float)((d2 - d9) / 2.0 / (double)f3 + 0.5);
        float f5 = (float)((d2 - d10) / 2.0 / (double)f3 + 0.5);
        float f6 = (float)((d4 - d12) / 2.0 / (double)f3 + 0.5);
        float f7 = (float)((d4 - d13) / 2.0 / (double)f3 + 0.5);
        nw2.a(d9, d11, d12, f4, f6);
        nw2.a(d9, d11, d13, f4, f7);
        nw2.a(d10, d11, d13, f5, f7);
        nw2.a(d10, d11, d12, f5, f6);
    }

    public static void a(eq eq2, double d2, double d3, double d4) {
        GL11.glDisable((int)3553);
        nw nw2 = nw.a;
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        nw2.b();
        nw2.b(d2, d3, d4);
        nw2.b(0.0f, 0.0f, -1.0f);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.b(0.0f, 0.0f, 1.0f);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.b(0.0f, -1.0f, 0.0f);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.b(0.0f, 1.0f, 0.0f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.b(-1.0f, 0.0f, 0.0f);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.b(1.0f, 0.0f, 0.0f);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.b(0.0, 0.0, 0.0);
        nw2.a();
        GL11.glEnable((int)3553);
    }

    public static void a(eq eq2) {
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a();
    }

    public void a(th th2) {
        this.b = th2;
    }

    public void b(sn sn2, double d2, double d3, double d4, float f2, float f3) {
        double d5;
        float f4;
        if (this.b.k.j && this.c > 0.0f && (f4 = (float)((1.0 - (d5 = this.b.a(sn2.aM, sn2.aN, sn2.aO)) / 256.0) * (double)this.d)) > 0.0f) {
            this.c(sn2, d2, d3, d4, f4, f3);
        }
        if (sn2.ak()) {
            this.a(sn2, d2, d3, d4, f3);
        }
    }

    public sj a() {
        return this.b.a();
    }
}

