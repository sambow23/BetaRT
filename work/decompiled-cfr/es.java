/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class es
extends gv {
    public es(ko ko2, float f2) {
        super(ko2, f2);
    }

    public void a(xt xt2, double d2, double d3, double d4, float f2, float f3) {
        super.a((ls)xt2, d2, d3, d4, f2, f3);
    }

    protected void a(xt xt2, float f2, float f3, float f4) {
        float f5 = xt2.b + (xt2.a - xt2.b) * f4;
        float f6 = xt2.f + (xt2.c - xt2.f) * f4;
        GL11.glTranslatef((float)0.0f, (float)0.5f, (float)0.0f);
        GL11.glRotatef((float)(180.0f - f3), (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)f5, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)f6, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)0.0f, (float)-1.2f, (float)0.0f);
    }

    protected void a(xt xt2, float f2) {
    }

    protected float b(xt xt2, float f2) {
        float f3 = xt2.j + (xt2.i - xt2.j) * f2;
        return f3;
    }
}

