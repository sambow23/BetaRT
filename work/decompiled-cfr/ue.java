/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class ue
extends id {
    private float l;
    private float m;

    public ue(gs gs2) {
        super(gs2.d);
        this.f = true;
        gs2.a(ep.f, 1);
    }

    public void b() {
        this.e.clear();
    }

    protected void k() {
        this.g.b("Crafting", 86, 16, 0x404040);
    }

    public void a(int n2, int n3, float f2) {
        super.a(n2, n3, f2);
        this.l = n2;
        this.m = n3;
    }

    protected void a(float f2) {
        int n2 = this.b.p.b("/gui/inventory.png");
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b.p.b(n2);
        int n3 = (this.c - this.a) / 2;
        int n4 = (this.d - this.i) / 2;
        this.b(n3, n4, 0, 0, this.a, this.i);
        GL11.glEnable((int)32826);
        GL11.glEnable((int)2903);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(n3 + 51), (float)(n4 + 75), (float)50.0f);
        float f3 = 30.0f;
        GL11.glScalef((float)(-f3), (float)f3, (float)f3);
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        float f4 = this.b.h.H;
        float f5 = this.b.h.aS;
        float f6 = this.b.h.aT;
        float f7 = (float)(n3 + 51) - this.l;
        float f8 = (float)(n4 + 75 - 50) - this.m;
        GL11.glRotatef((float)135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        u.b();
        GL11.glRotatef((float)-135.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)(-((float)Math.atan(f8 / 40.0f)) * 20.0f), (float)1.0f, (float)0.0f, (float)0.0f);
        this.b.h.H = (float)Math.atan(f7 / 40.0f) * 20.0f;
        this.b.h.aS = (float)Math.atan(f7 / 40.0f) * 40.0f;
        this.b.h.aT = -((float)Math.atan(f8 / 40.0f)) * 20.0f;
        this.b.h.bE = 1.0f;
        GL11.glTranslatef((float)0.0f, (float)this.b.h.bf, (float)0.0f);
        th.a.i = 180.0f;
        th.a.a(this.b.h, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        this.b.h.bE = 0.0f;
        this.b.h.H = f4;
        this.b.h.aS = f5;
        this.b.h.aT = f6;
        GL11.glPopMatrix();
        u.a();
        GL11.glDisable((int)32826);
    }

    protected void a(ke ke2) {
        if (ke2.f == 0) {
            this.b.a(new xm(this.b.I));
        }
        if (ke2.f == 1) {
            this.b.a(new dv(this, this.b.I));
        }
    }
}

