/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class du
extends ub {
    private List a = new ArrayList();
    private Minecraft b;

    public du(Minecraft minecraft) {
        this.b = minecraft;
    }

    public void a() {
        for (int i2 = 0; i2 < this.a.size(); ++i2) {
            kw kw2 = (kw)this.a.get(i2);
            kw2.a();
            kw2.a(this);
            if (!kw2.h) continue;
            this.a.remove(i2--);
        }
    }

    public void a(float f2) {
        this.b.p.b(this.b.p.b("/gui/particles.png"));
        for (int i2 = 0; i2 < this.a.size(); ++i2) {
            kw kw2 = (kw)this.a.get(i2);
            int n2 = (int)(kw2.c + (kw2.a - kw2.c) * (double)f2 - 4.0);
            int n3 = (int)(kw2.d + (kw2.b - kw2.d) * (double)f2 - 4.0);
            float f3 = (float)(kw2.r + (kw2.n - kw2.r) * (double)f2);
            float f4 = (float)(kw2.o + (kw2.k - kw2.o) * (double)f2);
            float f5 = (float)(kw2.p + (kw2.l - kw2.p) * (double)f2);
            float f6 = (float)(kw2.q + (kw2.m - kw2.q) * (double)f2);
            GL11.glColor4f((float)f4, (float)f5, (float)f6, (float)f3);
            this.b(n2, n3, 40, 0, 8, 8);
        }
    }
}

