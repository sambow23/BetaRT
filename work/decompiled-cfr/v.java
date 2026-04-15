/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class v
extends gv {
    protected fh a;

    public v(fh fh2, float f2) {
        super(fh2, f2);
        this.a = fh2;
    }

    protected void b(ls ls2, float f2) {
        iz iz2 = ls2.r_();
        if (iz2 != null) {
            GL11.glPushMatrix();
            this.a.d.c(0.0625f);
            GL11.glTranslatef((float)-0.0625f, (float)0.4375f, (float)0.0625f);
            if (iz2.c < 256 && cv.a(uu.m[iz2.c].b())) {
                float f3 = 0.5f;
                GL11.glTranslatef((float)0.0f, (float)0.1875f, (float)-0.3125f);
                GL11.glRotatef((float)20.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
                GL11.glScalef((float)(f3 *= 0.75f), (float)(-f3), (float)f3);
            } else if (gm.c[iz2.c].b()) {
                float f4 = 0.625f;
                GL11.glTranslatef((float)0.0f, (float)0.1875f, (float)0.0f);
                GL11.glScalef((float)f4, (float)(-f4), (float)f4);
                GL11.glRotatef((float)-100.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)45.0f, (float)0.0f, (float)1.0f, (float)0.0f);
            } else {
                float f5 = 0.375f;
                GL11.glTranslatef((float)0.25f, (float)0.1875f, (float)-0.1875f);
                GL11.glScalef((float)f5, (float)f5, (float)f5);
                GL11.glRotatef((float)60.0f, (float)0.0f, (float)0.0f, (float)1.0f);
                GL11.glRotatef((float)-90.0f, (float)1.0f, (float)0.0f, (float)0.0f);
                GL11.glRotatef((float)20.0f, (float)0.0f, (float)0.0f, (float)1.0f);
            }
            this.b.f.a(ls2, iz2);
            GL11.glPopMatrix();
        }
    }
}

