/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class gl
extends xw {
    private int a = 0;
    private int o = 0;
    private ji p;

    public gl(ji ji2, fd fd2, double d2, double d3, double d4) {
        super(fd2, d2, d3, d4, 0.0, 0.0, 0.0);
        this.p = ji2;
        this.aR = 0.0;
        this.aQ = 0.0;
        this.aP = 0.0;
        this.o = 200;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8;
        float f9 = ((float)this.a + f2) / (float)this.o;
        if ((f8 = 2.0f - (f9 *= f9) * 2.0f) > 1.0f) {
            f8 = 1.0f;
        }
        f8 *= 0.2f;
        GL11.glDisable((int)2896);
        float f10 = 0.125f;
        float f11 = (float)(this.aM - l);
        float f12 = (float)(this.aN - m);
        float f13 = (float)(this.aO - n);
        float f14 = this.aI.c(in.b(this.aM), in.b(this.aN), in.b(this.aO));
        this.p.b(this.p.b("/misc/footprint.png"));
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        nw2.b();
        nw2.a(f14, f14, f14, f8);
        nw2.a(f11 - f10, f12, f13 + f10, 0.0, 1.0);
        nw2.a(f11 + f10, f12, f13 + f10, 1.0, 1.0);
        nw2.a(f11 + f10, f12, f13 - f10, 1.0, 0.0);
        nw2.a(f11 - f10, f12, f13 - f10, 0.0, 0.0);
        nw2.a();
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2896);
    }

    public void w_() {
        ++this.a;
        if (this.a == this.o) {
            this.K();
        }
    }

    public int c_() {
        return 3;
    }
}

