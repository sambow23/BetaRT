/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class pj
extends ob {
    public pj(Minecraft minecraft) {
        super(minecraft);
        this.b = true;
    }

    public void b(gs gs2) {
        for (int i2 = 0; i2 < 9; ++i2) {
            if (gs2.c.a[i2] == null) {
                this.a.h.c.a[i2] = new iz((uu)gr.a.get(i2));
                continue;
            }
            this.a.h.c.a[i2].a = 1;
        }
    }

    public boolean d() {
        return false;
    }

    public void a(fd fd2) {
        super.a(fd2);
    }

    public void c() {
    }
}

