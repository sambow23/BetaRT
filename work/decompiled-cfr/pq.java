/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class pq
extends gv {
    public pq() {
        super(new lo(), 0.5f);
    }

    protected void a(bp bp2, float f2) {
        bp bp3 = bp2;
        float f3 = ((float)bp3.e + (float)(bp3.f - bp3.e) * f2) / 20.0f;
        if (f3 < 0.0f) {
            f3 = 0.0f;
        }
        f3 = 1.0f / (f3 * f3 * f3 * f3 * f3 * 2.0f + 1.0f);
        float f4 = (8.0f + f3) / 2.0f;
        float f5 = (8.0f + 1.0f / f3) / 2.0f;
        GL11.glScalef((float)f5, (float)f4, (float)f5);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }
}

