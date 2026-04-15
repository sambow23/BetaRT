/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class em
extends xw {
    private sn a;
    private sn o;
    private int p = 0;
    private int q = 0;
    private float r;

    public em(fd fd2, sn sn2, sn sn3, float f2) {
        super(fd2, sn2.aM, sn2.aN, sn2.aO, sn2.aP, sn2.aQ, sn2.aR);
        this.a = sn2;
        this.o = sn3;
        this.q = 3;
        this.r = f2;
    }

    public void a(nw nw2, float f2, float f3, float f4, float f5, float f6, float f7) {
        float f8 = ((float)this.p + f2) / (float)this.q;
        f8 *= f8;
        double d2 = this.a.aM;
        double d3 = this.a.aN;
        double d4 = this.a.aO;
        double d5 = this.o.bl + (this.o.aM - this.o.bl) * (double)f2;
        double d6 = this.o.bm + (this.o.aN - this.o.bm) * (double)f2 + (double)this.r;
        double d7 = this.o.bn + (this.o.aO - this.o.bn) * (double)f2;
        double d8 = d2 + (d5 - d2) * (double)f8;
        double d9 = d3 + (d6 - d3) * (double)f8;
        double d10 = d4 + (d7 - d4) * (double)f8;
        int n2 = in.b(d8);
        int n3 = in.b(d9 + (double)(this.bf / 2.0f));
        int n4 = in.b(d10);
        float f9 = this.aI.c(n2, n3, n4);
        GL11.glColor4f((float)f9, (float)f9, (float)f9, (float)1.0f);
        th.a.a(this.a, (float)(d8 -= l), (float)(d9 -= m), (float)(d10 -= n), this.a.aS, f2);
    }

    public void w_() {
        ++this.p;
        if (this.p == this.q) {
            this.K();
        }
    }

    public int c_() {
        return 3;
    }
}

