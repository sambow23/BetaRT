/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class yg
extends gv {
    private float a;

    public yg(ko ko2, float f2, float f3) {
        super(ko2, f2 * f3);
        this.a = f3;
    }

    protected void a(nt nt2, float f2) {
        GL11.glScalef((float)this.a, (float)this.a, (float)this.a);
    }
}

