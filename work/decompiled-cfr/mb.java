/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class mb
extends bw {
    public void a(sn sn2, double d2, double d3, double d4, float f2, float f3) {
        GL11.glPushMatrix();
        mb.a(sn2.aW, d2 - sn2.bl, d3 - sn2.bm, d4 - sn2.bn);
        GL11.glPopMatrix();
    }
}

