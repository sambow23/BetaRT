/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class mc
extends bw {
    public void a(sl sl2, double d2, double d3, double d4, float f2, float f3) {
        if (sl2.aU == 0.0f && sl2.aV == 0.0f) {
            return;
        }
        this.a("/item/arrows.png");
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
        GL11.glRotatef((float)(sl2.aU + (sl2.aS - sl2.aU) * f3 - 90.0f), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(sl2.aV + (sl2.aT - sl2.aV) * f3), (float)0.0f, (float)0.0f, (float)1.0f);
        nw nw2 = nw.a;
        int n2 = 0;
        float f4 = 0.0f;
        float f5 = 0.5f;
        float f6 = (float)(0 + n2 * 10) / 32.0f;
        float f7 = (float)(5 + n2 * 10) / 32.0f;
        float f8 = 0.0f;
        float f9 = 0.15625f;
        float f10 = (float)(5 + n2 * 10) / 32.0f;
        float f11 = (float)(10 + n2 * 10) / 32.0f;
        float f12 = 0.05625f;
        GL11.glEnable((int)32826);
        float f13 = (float)sl2.b - f3;
        if (f13 > 0.0f) {
            float f14 = -in.a(f13 * 3.0f) * f13;
            GL11.glRotatef((float)f14, (float)0.0f, (float)0.0f, (float)1.0f);
        }
        GL11.glRotatef((float)45.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glScalef((float)f12, (float)f12, (float)f12);
        GL11.glTranslatef((float)-4.0f, (float)0.0f, (float)0.0f);
        GL11.glNormal3f((float)f12, (float)0.0f, (float)0.0f);
        nw2.b();
        nw2.a(-7.0, -2.0, -2.0, f8, f10);
        nw2.a(-7.0, -2.0, 2.0, f9, f10);
        nw2.a(-7.0, 2.0, 2.0, f9, f11);
        nw2.a(-7.0, 2.0, -2.0, f8, f11);
        nw2.a();
        GL11.glNormal3f((float)(-f12), (float)0.0f, (float)0.0f);
        nw2.b();
        nw2.a(-7.0, 2.0, -2.0, f8, f10);
        nw2.a(-7.0, 2.0, 2.0, f9, f10);
        nw2.a(-7.0, -2.0, 2.0, f9, f11);
        nw2.a(-7.0, -2.0, -2.0, f8, f11);
        nw2.a();
        for (int i2 = 0; i2 < 4; ++i2) {
            GL11.glRotatef((float)90.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            GL11.glNormal3f((float)0.0f, (float)0.0f, (float)f12);
            nw2.b();
            nw2.a(-8.0, -2.0, 0.0, f4, f6);
            nw2.a(8.0, -2.0, 0.0, f5, f6);
            nw2.a(8.0, 2.0, 0.0, f5, f7);
            nw2.a(-8.0, 2.0, 0.0, f4, f7);
            nw2.a();
        }
        GL11.glDisable((int)32826);
        GL11.glPopMatrix();
    }
}

