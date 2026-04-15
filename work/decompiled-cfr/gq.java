/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class gq
extends id {
    public gq(ix ix2, az az2) {
        super(new sh(ix2, az2));
    }

    protected void k() {
        this.g.b("Dispenser", 60, 6, 0x404040);
        this.g.b("Inventory", 8, this.i - 96 + 2, 0x404040);
    }

    protected void a(float f2) {
        int n2 = this.b.p.b("/gui/trap.png");
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b.p.b(n2);
        int n3 = (this.c - this.a) / 2;
        int n4 = (this.d - this.i) / 2;
        this.b(n3, n4, 0, 0, this.a, this.i);
    }
}

