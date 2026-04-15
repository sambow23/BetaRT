/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class mj
extends gv {
    private ko a;

    public mj(ko ko2, ko ko3, float f2) {
        super(ko2, f2);
        this.a = ko3;
    }

    protected boolean a(uw uw2, int n2, float f2) {
        if (n2 == 0) {
            this.a(this.a);
            GL11.glEnable((int)2977);
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)771);
            return true;
        }
        if (n2 == 1) {
            GL11.glDisable((int)3042);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
        return false;
    }

    protected void a(uw uw2, float f2) {
        int n2 = uw2.v();
        float f3 = (uw2.b + (uw2.a - uw2.b) * f2) / ((float)n2 * 0.5f + 1.0f);
        float f4 = 1.0f / (f3 + 1.0f);
        float f5 = n2;
        GL11.glScalef((float)(f4 * f5), (float)(1.0f / f4 * f5), (float)(f4 * f5));
    }
}

