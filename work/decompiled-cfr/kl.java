/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class kl
extends bw {
    public void a(cf cf2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
        GL11.glEnable((int)32826);
        float f4 = 2.0f;
        GL11.glScalef((float)(f4 / 1.0f), (float)(f4 / 1.0f), (float)(f4 / 1.0f));
        int n2 = gm.aB.a(0);
        this.a("/gui/items.png");
        nw nw2 = nw.a;
        float f5 = (float)(n2 % 16 * 16 + 0) / 256.0f;
        float f6 = (float)(n2 % 16 * 16 + 16) / 256.0f;
        float f7 = (float)(n2 / 16 * 16 + 0) / 256.0f;
        float f8 = (float)(n2 / 16 * 16 + 16) / 256.0f;
        float f9 = 1.0f;
        float f10 = 0.5f;
        float f11 = 0.25f;
        GL11.glRotatef((float)(180.0f - this.b.i), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-this.b.j), (float)1.0f, (float)0.0f, (float)0.0f);
        nw2.b();
        nw2.b(0.0f, 1.0f, 0.0f);
        nw2.a(0.0f - f10, 0.0f - f11, 0.0, f5, f8);
        nw2.a(f9 - f10, 0.0f - f11, 0.0, f6, f8);
        nw2.a(f9 - f10, 1.0f - f11, 0.0, f6, f7);
        nw2.a(0.0f - f10, 1.0f - f11, 0.0, f5, f7);
        nw2.a();
        GL11.glDisable((int)32826);
        GL11.glPopMatrix();
    }
}

