/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class gn
extends bw {
    private cv a = new cv();

    public gn() {
        this.c = 0.5f;
    }

    public void a(ju ju2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)d2), (float)((float)d3), (float)((float)d4));
        this.a("/terrain.png");
        uu uu2 = uu.m[ju2.a];
        fd fd2 = ju2.k();
        GL11.glDisable((int)2896);
        this.a.a(uu2, fd2, in.b(ju2.aM), in.b(ju2.aN), in.b(ju2.aO));
        GL11.glEnable((int)2896);
        GL11.glPopMatrix();
    }
}

