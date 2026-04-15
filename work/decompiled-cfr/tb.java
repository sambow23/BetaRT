/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class tb
extends bw {
    protected ko a;

    public tb() {
        this.c = 0.5f;
        this.a = new nq();
    }

    public void a(yl yl2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glPushMatrix();
        double d5 = yl2.bl + (yl2.aM - yl2.bl) * (double)f3;
        double d6 = yl2.bm + (yl2.aN - yl2.bm) * (double)f3;
        double d7 = yl2.bn + (yl2.aO - yl2.bn) * (double)f3;
        double d8 = 0.3f;
        bt bt2 = yl2.i(d5, d6, d7);
        float f4 = yl2.aV + (yl2.aT - yl2.aV) * f3;
        if (bt2 != null) {
            bt bt3 = yl2.a(d5, d6, d7, d8);
            bt bt4 = yl2.a(d5, d6, d7, -d8);
            if (bt3 == null) {
                bt3 = bt2;
            }
            if (bt4 == null) {
                bt4 = bt2;
            }
            d2 += bt2.a - d5;
            d3 += (bt3.b + bt4.b) / 2.0 - d6;
            d4 += bt2.c - d7;
            bt bt5 = bt4.c(-bt3.a, -bt3.b, -bt3.c);
            if (bt5.d() != 0.0) {
                bt5 = bt5.c();
                f2 = (float)(Math.atan2(bt5.c, bt5.a) * 180.0 / Math.PI);
                f4 = (float)(Math.atan(bt5.b) * 73.0);
            }
        }
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
        GL11.glRotatef((float)(180.0f - f2), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-f4), (float)0.0f, (float)0.0f, (float)1.0f);
        float f5 = (float)yl2.b - f3;
        float f6 = (float)yl2.a - f3;
        if (f6 < 0.0f) {
            f6 = 0.0f;
        }
        if (f5 > 0.0f) {
            GL11.glRotatef((float)(in.a(f5) * f5 * f6 / 10.0f * (float)yl2.c), (float)1.0f, (float)0.0f, (float)0.0f);
        }
        if (yl2.d != 0) {
            this.a("/terrain.png");
            float f7 = 0.75f;
            GL11.glScalef((float)f7, (float)f7, (float)f7);
            GL11.glTranslatef((float)0.0f, (float)0.3125f, (float)0.0f);
            GL11.glRotatef((float)90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            if (yl2.d == 1) {
                new cv().a(uu.av, 0, yl2.a(f3));
            } else if (yl2.d == 2) {
                new cv().a(uu.aC, 0, yl2.a(f3));
            }
            GL11.glRotatef((float)-90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)-0.3125f, (float)0.0f);
            GL11.glScalef((float)(1.0f / f7), (float)(1.0f / f7), (float)(1.0f / f7));
        }
        this.a("/item/cart.png");
        GL11.glScalef((float)-1.0f, (float)-1.0f, (float)1.0f);
        this.a.a(0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);
        GL11.glPopMatrix();
    }
}

