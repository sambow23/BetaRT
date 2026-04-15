/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class dv
extends da {
    private static bb j = new bb();
    protected da a;
    protected String i = "Select world";
    private su l;
    private ci m;
    private ga n;
    private xi o;
    private lg p = null;

    public dv(da da2, xi xi2) {
        this.a = da2;
        this.o = xi2;
    }

    public void b() {
        this.i = do.a("gui.stats");
        this.l = new su(this);
        this.l.a(this.e, 1, 1);
        this.m = new ci(this);
        this.m.a(this.e, 1, 1);
        this.n = new ga(this);
        this.n.a(this.e, 1, 1);
        this.p = this.l;
        this.k();
    }

    public void k() {
        nh nh2 = nh.a();
        this.e.add(new ke(0, this.c / 2 + 4, this.d - 28, 150, 20, nh2.a("gui.done")));
        this.e.add(new ke(1, this.c / 2 - 154, this.d - 52, 100, 20, nh2.a("stat.generalButton")));
        ke ke2 = new ke(2, this.c / 2 - 46, this.d - 52, 100, 20, nh2.a("stat.blocksButton"));
        this.e.add(ke2);
        ke ke3 = new ke(3, this.c / 2 + 62, this.d - 52, 100, 20, nh2.a("stat.itemsButton"));
        this.e.add(ke3);
        if (this.n.a() == 0) {
            ke2.g = false;
        }
        if (this.m.a() == 0) {
            ke3.g = false;
        }
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == 0) {
            this.b.a(this.a);
        } else if (ke2.f == 1) {
            this.p = this.l;
        } else if (ke2.f == 3) {
            this.p = this.m;
        } else if (ke2.f == 2) {
            this.p = this.n;
        } else {
            this.p.a(ke2);
        }
    }

    public void a(int n2, int n3, float f2) {
        this.p.a(n2, n3, f2);
        this.a(this.g, this.i, this.c / 2, 20, 0xFFFFFF);
        super.a(n2, n3, f2);
    }

    private void c(int n2, int n3, int n4) {
        this.a(n2 + 1, n3 + 1);
        GL11.glEnable((int)32826);
        GL11.glPushMatrix();
        GL11.glRotatef((float)180.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        u.b();
        GL11.glPopMatrix();
        j.a(this.g, this.b.p, n4, 0, gm.c[n4].a(0), n2 + 2, n3 + 2);
        u.a();
        GL11.glDisable((int)32826);
    }

    private void a(int n2, int n3) {
        this.c(n2, n3, 0, 0);
    }

    private void c(int n2, int n3, int n4, int n5) {
        int n6 = this.b.p.b("/gui/slot.png");
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.b.p.b(n6);
        nw nw2 = nw.a;
        nw2.b();
        nw2.a(n2 + 0, n3 + 18, this.k, (float)(n4 + 0) * 0.0078125f, (float)(n5 + 18) * 0.0078125f);
        nw2.a(n2 + 18, n3 + 18, this.k, (float)(n4 + 18) * 0.0078125f, (float)(n5 + 18) * 0.0078125f);
        nw2.a(n2 + 18, n3 + 0, this.k, (float)(n4 + 18) * 0.0078125f, (float)(n5 + 0) * 0.0078125f);
        nw2.a(n2 + 0, n3 + 0, this.k, (float)(n4 + 0) * 0.0078125f, (float)(n5 + 0) * 0.0078125f);
        nw2.a();
    }

    static /* synthetic */ Minecraft a(dv dv2) {
        return dv2.b;
    }

    static /* synthetic */ sj b(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ xi c(dv dv2) {
        return dv2.o;
    }

    static /* synthetic */ sj d(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ sj e(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ Minecraft f(dv dv2) {
        return dv2.b;
    }

    static /* synthetic */ void a(dv dv2, int n2, int n3, int n4, int n5) {
        dv2.c(n2, n3, n4, n5);
    }

    static /* synthetic */ Minecraft g(dv dv2) {
        return dv2.b;
    }

    static /* synthetic */ sj h(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ sj i(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ sj j(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ sj k(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ sj l(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ void a(dv dv2, int n2, int n3, int n4, int n5, int n6, int n7) {
        dv2.a(n2, n3, n4, n5, n6, n7);
    }

    static /* synthetic */ sj m(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ sj n(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ void b(dv dv2, int n2, int n3, int n4, int n5, int n6, int n7) {
        dv2.a(n2, n3, n4, n5, n6, n7);
    }

    static /* synthetic */ sj o(dv dv2) {
        return dv2.g;
    }

    static /* synthetic */ void a(dv dv2, int n2, int n3, int n4) {
        dv2.c(n2, n3, n4);
    }
}

