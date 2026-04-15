/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class hp
extends id {
    private lw l;
    private lw m;
    private int n = 0;

    public hp(lw lw2, lw lw3) {
        super(new cd(lw2, lw3));
        this.l = lw2;
        this.m = lw3;
        this.f = false;
        int n2 = 222;
        int n3 = n2 - 108;
        this.n = lw3.a() / 9;
        this.i = n3 + this.n * 18;
    }

    protected void k() {
        this.g.b(this.m.c(), 8, 6, 0x404040);
        this.g.b(this.l.c(), 8, this.i - 96 + 2, 0x404040);
    }

    protected void a(float f2) {
        int n2 = this.b.p.b("/gui/container.png");
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b.p.b(n2);
        int n3 = (this.c - this.a) / 2;
        int n4 = (this.d - this.i) / 2;
        this.b(n3, n4, 0, 0, this.a, this.n * 18 + 17);
        this.b(n3, n4 + this.n * 18 + 17, 0, 126, this.a, 96);
    }
}

