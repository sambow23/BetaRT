/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class xy
extends gv {
    public xy(ko ko2, ko ko3, float f2) {
        super(ko2, f2);
        this.a(ko3);
    }

    protected boolean a(dl dl2, int n2, float f2) {
        if (n2 == 0 && !dl2.s()) {
            this.a("/mob/sheep_fur.png");
            float f3 = dl2.a(f2);
            int n3 = dl2.r();
            GL11.glColor3f((float)(f3 * dl.a[n3][0]), (float)(f3 * dl.a[n3][1]), (float)(f3 * dl.a[n3][2]));
            return true;
        }
        return false;
    }
}

