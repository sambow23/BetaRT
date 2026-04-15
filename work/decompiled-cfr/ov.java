/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class ov
extends id {
    private sk l;

    public ov(ix ix2, sk sk2) {
        super(new bm(ix2, sk2));
        this.l = sk2;
    }

    protected void k() {
        this.g.b("Furnace", 60, 6, 0x404040);
        this.g.b("Inventory", 8, this.i - 96 + 2, 0x404040);
    }

    protected void a(float f2) {
        int n2;
        int n3 = this.b.p.b("/gui/furnace.png");
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b.p.b(n3);
        int n4 = (this.c - this.a) / 2;
        int n5 = (this.d - this.i) / 2;
        this.b(n4, n5, 0, 0, this.a, this.i);
        if (this.l.b()) {
            n2 = this.l.c(12);
            this.b(n4 + 56, n5 + 36 + 12 - n2, 176, 12 - n2, 14, n2 + 2);
        }
        n2 = this.l.b(24);
        this.b(n4 + 79, n5 + 34, 176, 14, n2 + 1, 16);
    }
}

