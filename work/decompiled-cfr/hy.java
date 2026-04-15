/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class hy
extends je {
    private cv b;

    public void a(uk uk2, double d2, double d3, double d4, float f2) {
        uu uu2 = uu.m[uk2.a()];
        if (uu2 != null && uk2.a(f2) < 1.0f) {
            nw nw2 = nw.a;
            this.a("/terrain.png");
            u.a();
            GL11.glBlendFunc((int)770, (int)771);
            GL11.glEnable((int)3042);
            GL11.glDisable((int)2884);
            if (Minecraft.v()) {
                GL11.glShadeModel((int)7425);
            } else {
                GL11.glShadeModel((int)7424);
            }
            nw2.b();
            nw2.b((double)((float)d2 - (float)uk2.e + uk2.b(f2)), (double)((float)d3 - (float)uk2.f + uk2.c(f2)), (double)((float)d4 - (float)uk2.g + uk2.d(f2)));
            nw2.a(1, 1, 1);
            if (uu2 == uu.ab && uk2.a(f2) < 0.5f) {
                this.b.a(uu2, uk2.e, uk2.f, uk2.g, false);
            } else if (uk2.k() && !uk2.b()) {
                uu.ab.a_(((jq)uu2).i());
                this.b.a((uu)uu.ab, uk2.e, uk2.f, uk2.g, uk2.a(f2) < 0.5f);
                uu.ab.a();
                nw2.b((double)((float)d2 - (float)uk2.e), (double)((float)d3 - (float)uk2.f), (double)((float)d4 - (float)uk2.g));
                this.b.d(uu2, uk2.e, uk2.f, uk2.g);
            } else {
                this.b.a(uu2, uk2.e, uk2.f, uk2.g);
            }
            nw2.b(0.0, 0.0, 0.0);
            nw2.a();
            u.b();
        }
    }

    public void a(fd fd2) {
        this.b = new cv(fd2);
    }
}

