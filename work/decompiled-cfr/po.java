/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class po
extends je {
    private rf b = new rf();

    public void a(yk yk2, double d2, double d3, double d4, float f2) {
        float f3;
        uu uu2 = yk2.f();
        GL11.glPushMatrix();
        float f4 = 0.6666667f;
        if (uu2 == uu.aE) {
            GL11.glTranslatef((float)((float)d2 + 0.5f), (float)((float)d3 + 0.75f * f4), (float)((float)d4 + 0.5f));
            float f5 = (float)(yk2.e() * 360) / 16.0f;
            GL11.glRotatef((float)(-f5), (float)0.0f, (float)1.0f, (float)0.0f);
            this.b.b.h = true;
        } else {
            int n2 = yk2.e();
            f3 = 0.0f;
            if (n2 == 2) {
                f3 = 180.0f;
            }
            if (n2 == 4) {
                f3 = 90.0f;
            }
            if (n2 == 5) {
                f3 = -90.0f;
            }
            GL11.glTranslatef((float)((float)d2 + 0.5f), (float)((float)d3 + 0.75f * f4), (float)((float)d4 + 0.5f));
            GL11.glRotatef((float)(-f3), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)-0.3125f, (float)-0.4375f);
            this.b.b.h = false;
        }
        this.a("/item/sign.png");
        GL11.glPushMatrix();
        GL11.glScalef((float)f4, (float)(-f4), (float)(-f4));
        this.b.a();
        GL11.glPopMatrix();
        sj sj2 = this.a();
        f3 = 0.016666668f * f4;
        GL11.glTranslatef((float)0.0f, (float)(0.5f * f4), (float)(0.07f * f4));
        GL11.glScalef((float)f3, (float)(-f3), (float)f3);
        GL11.glNormal3f((float)0.0f, (float)0.0f, (float)(-1.0f * f3));
        GL11.glDepthMask((boolean)false);
        int n3 = 0;
        for (int i2 = 0; i2 < yk2.a.length; ++i2) {
            String string = yk2.a[i2];
            if (i2 == yk2.b) {
                string = "> " + string + " <";
                sj2.b(string, -sj2.a(string) / 2, i2 * 10 - yk2.a.length * 5, n3);
                continue;
            }
            sj2.b(string, -sj2.a(string) / 2, i2 * 10 - yk2.a.length * 5, n3);
        }
        GL11.glDepthMask((boolean)true);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glPopMatrix();
    }
}

