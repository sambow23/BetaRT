/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class aw {
    public byte[] a = new byte[1024];
    public int b;
    public boolean c = false;
    public int d = 0;
    public int e = 1;
    public int f = 0;

    public aw(int n2) {
        this.b = n2;
    }

    public void a() {
    }

    public void a(ji ji2) {
        if (this.f == 0) {
            GL11.glBindTexture((int)3553, (int)ji2.b("/terrain.png"));
        } else if (this.f == 1) {
            GL11.glBindTexture((int)3553, (int)ji2.b("/gui/items.png"));
        }
    }
}

