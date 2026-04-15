/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class pl
extends bw {
    public void a(lx lx2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
        GL11.glEnable((int)32826);
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        int n2 = 1;
        int n3 = 2;
        this.a("/particles.png");
        nw nw2 = nw.a;
        float f4 = (float)(n2 * 8 + 0) / 128.0f;
        float f5 = (float)(n2 * 8 + 8) / 128.0f;
        float f6 = (float)(n3 * 8 + 0) / 128.0f;
        float f7 = (float)(n3 * 8 + 8) / 128.0f;
        float f8 = 1.0f;
        float f9 = 0.5f;
        float f10 = 0.5f;
        GL11.glRotatef((float)(180.0f - this.b.i), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-this.b.j), (float)1.0f, (float)0.0f, (float)0.0f);
        nw2.b();
        nw2.b(0.0f, 1.0f, 0.0f);
        nw2.a(0.0f - f9, 0.0f - f10, 0.0, f4, f7);
        nw2.a(f8 - f9, 0.0f - f10, 0.0, f5, f7);
        nw2.a(f8 - f9, 1.0f - f10, 0.0, f5, f6);
        nw2.a(0.0f - f9, 1.0f - f10, 0.0, f4, f6);
        nw2.a();
        GL11.glDisable((int)32826);
        GL11.glPopMatrix();
        if (lx2.b != null) {
            float f11 = (lx2.b.aU + (lx2.b.aS - lx2.b.aU) * f3) * (float)Math.PI / 180.0f;
            double d5 = in.a(f11);
            double d6 = in.b(f11);
            float f12 = lx2.b.d(f3);
            float f13 = in.a(in.c(f12) * (float)Math.PI);
            bt bt2 = bt.b(-0.5, 0.03, 0.8);
            bt2.a(-(lx2.b.aV + (lx2.b.aT - lx2.b.aV) * f3) * (float)Math.PI / 180.0f);
            bt2.b(-(lx2.b.aU + (lx2.b.aS - lx2.b.aU) * f3) * (float)Math.PI / 180.0f);
            bt2.b(f13 * 0.5f);
            bt2.a(-f13 * 0.7f);
            double d7 = lx2.b.aJ + (lx2.b.aM - lx2.b.aJ) * (double)f3 + bt2.a;
            double d8 = lx2.b.aK + (lx2.b.aN - lx2.b.aK) * (double)f3 + bt2.b;
            double d9 = lx2.b.aL + (lx2.b.aO - lx2.b.aL) * (double)f3 + bt2.c;
            if (this.b.k.A) {
                f11 = (lx2.b.I + (lx2.b.H - lx2.b.I) * f3) * (float)Math.PI / 180.0f;
                d5 = in.a(f11);
                d6 = in.b(f11);
                d7 = lx2.b.aJ + (lx2.b.aM - lx2.b.aJ) * (double)f3 - d6 * 0.35 - d5 * 0.85;
                d8 = lx2.b.aK + (lx2.b.aN - lx2.b.aK) * (double)f3 - 0.45;
                d9 = lx2.b.aL + (lx2.b.aO - lx2.b.aL) * (double)f3 - d5 * 0.35 + d6 * 0.85;
            }
            double d10 = lx2.aJ + (lx2.aM - lx2.aJ) * (double)f3;
            double d11 = lx2.aK + (lx2.aN - lx2.aK) * (double)f3 + 0.25;
            double d12 = lx2.aL + (lx2.aO - lx2.aL) * (double)f3;
            double d13 = (float)(d7 - d10);
            double d14 = (float)(d8 - d11);
            double d15 = (float)(d9 - d12);
            GL11.glDisable((int)3553);
            GL11.glDisable((int)2896);
            nw2.a(3);
            nw2.b(0);
            int n4 = 16;
            for (int i2 = 0; i2 <= n4; ++i2) {
                float f14 = (float)i2 / (float)n4;
                nw2.a(d2 + d13 * (double)f14, d3 + d14 * (double)(f14 * f14 + f14) * 0.5 + 0.25, d4 + d15 * (double)f14);
            }
            nw2.a();
            GL11.glEnable((int)2896);
            GL11.glEnable((int)3553);
        }
    }
}

