/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public abstract class id
extends da {
    private static bb l = new bb();
    protected int a = 176;
    protected int i = 166;
    public dw j;

    public id(dw dw2) {
        this.j = dw2;
    }

    public void b() {
        super.b();
        this.b.h.e = this.j;
    }

    public void a(int n2, int n3, float f2) {
        int n4;
        int n5;
        Object object;
        this.i();
        int n6 = (this.c - this.a) / 2;
        int n7 = (this.d - this.i) / 2;
        this.a(f2);
        GL11.glPushMatrix();
        GL11.glRotatef((float)120.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        u.b();
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)n6, (float)n7, (float)0.0f);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)32826);
        gp gp2 = null;
        for (int i2 = 0; i2 < this.j.e.size(); ++i2) {
            object = (gp)this.j.e.get(i2);
            this.a((gp)object);
            if (!this.a((gp)object, n2, n3)) continue;
            gp2 = object;
            GL11.glDisable((int)2896);
            GL11.glDisable((int)2929);
            n5 = ((gp)object).b;
            n4 = ((gp)object).c;
            this.a(n5, n4, n5 + 16, n4 + 16, -2130706433, -2130706433);
            GL11.glEnable((int)2896);
            GL11.glEnable((int)2929);
        }
        ix ix2 = this.b.h.c;
        if (ix2.i() != null) {
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)32.0f);
            l.a(this.g, this.b.p, ix2.i(), n2 - n6 - 8, n3 - n7 - 8);
            l.b(this.g, this.b.p, ix2.i(), n2 - n6 - 8, n3 - n7 - 8);
        }
        GL11.glDisable((int)32826);
        u.a();
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2929);
        this.k();
        if (ix2.i() == null && gp2 != null && gp2.b() && ((String)(object = ("" + nh.a().b(gp2.a().l())).trim())).length() > 0) {
            n5 = n2 - n6 + 12;
            n4 = n3 - n7 - 12;
            int n8 = this.g.a((String)object);
            this.a(n5 - 3, n4 - 3, n5 + n8 + 3, n4 + 8 + 3, -1073741824, -1073741824);
            this.g.a((String)object, n5, n4, -1);
        }
        GL11.glPopMatrix();
        super.a(n2, n3, f2);
        GL11.glEnable((int)2896);
        GL11.glEnable((int)2929);
    }

    protected void k() {
    }

    protected abstract void a(float var1);

    private void a(gp gp2) {
        int n2;
        int n3 = gp2.b;
        int n4 = gp2.c;
        iz iz2 = gp2.a();
        if (iz2 == null && (n2 = gp2.e()) >= 0) {
            GL11.glDisable((int)2896);
            this.b.p.b(this.b.p.b("/gui/items.png"));
            this.b(n3, n4, n2 % 16 * 16, n2 / 16 * 16, 16, 16);
            GL11.glEnable((int)2896);
            return;
        }
        l.a(this.g, this.b.p, iz2, n3, n4);
        l.b(this.g, this.b.p, iz2, n3, n4);
    }

    private gp a(int n2, int n3) {
        for (int i2 = 0; i2 < this.j.e.size(); ++i2) {
            gp gp2 = (gp)this.j.e.get(i2);
            if (!this.a(gp2, n2, n3)) continue;
            return gp2;
        }
        return null;
    }

    private boolean a(gp gp2, int n2, int n3) {
        int n4 = (this.c - this.a) / 2;
        int n5 = (this.d - this.i) / 2;
        return (n2 -= n4) >= gp2.b - 1 && n2 < gp2.b + 16 + 1 && (n3 -= n5) >= gp2.c - 1 && n3 < gp2.c + 16 + 1;
    }

    protected void a(int n2, int n3, int n4) {
        super.a(n2, n3, n4);
        if (n4 == 0 || n4 == 1) {
            gp gp2 = this.a(n2, n3);
            int n5 = (this.c - this.a) / 2;
            int n6 = (this.d - this.i) / 2;
            boolean bl2 = n2 < n5 || n3 < n6 || n2 >= n5 + this.a || n3 >= n6 + this.i;
            int n7 = -1;
            if (gp2 != null) {
                n7 = gp2.a;
            }
            if (bl2) {
                n7 = -999;
            }
            if (n7 != -1) {
                boolean bl3 = n7 != -999 && (Keyboard.isKeyDown((int)42) || Keyboard.isKeyDown((int)54));
                this.b.c.a(this.j.f, n7, n4, bl3, this.b.h);
            }
        }
    }

    protected void b(int n2, int n3, int n4) {
        if (n4 == 0) {
            // empty if block
        }
    }

    protected void a(char c2, int n2) {
        if (n2 == 1 || n2 == this.b.z.r.b) {
            this.b.h.r();
        }
    }

    public void h() {
        if (this.b.h == null) {
            return;
        }
        this.b.c.a(this.j.f, this.b.h);
    }

    public boolean c() {
        return false;
    }

    public void a() {
        super.a();
        if (!this.b.h.W() || this.b.h.be) {
            this.b.h.r();
        }
    }
}

