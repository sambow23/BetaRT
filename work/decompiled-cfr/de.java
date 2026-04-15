/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.List;
import org.lwjgl.opengl.GL11;

class de
extends lg {
    final /* synthetic */ ft a;

    public de(ft ft2) {
        this.a = ft2;
        super(ft.a(ft2), ft2.c, ft2.d, 32, ft2.d - 55 + 4, 36);
    }

    protected int a() {
        List list = ft.b((ft)this.a).D.b();
        return list.size();
    }

    protected void a(int n2, boolean bl2) {
        List list = ft.c((ft)this.a).D.b();
        ft.d((ft)this.a).D.a((i)list.get(n2));
        ft.e((ft)this.a).p.b();
    }

    protected boolean c_(int n2) {
        List list = ft.f((ft)this.a).D.b();
        return ft.g((ft)this.a).D.a == list.get(n2);
    }

    protected int b() {
        return this.a() * 36;
    }

    protected void c() {
        this.a.i();
    }

    protected void a(int n2, int n3, int n4, int n5, nw nw2) {
        i i2 = (i)ft.h((ft)this.a).D.b().get(n2);
        i2.c(ft.i(this.a));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        nw2.b();
        nw2.b(0xFFFFFF);
        nw2.a(n3, n4 + n5, 0.0, 0.0, 1.0);
        nw2.a(n3 + 32, n4 + n5, 0.0, 1.0, 1.0);
        nw2.a(n3 + 32, n4, 0.0, 1.0, 0.0);
        nw2.a(n3, n4, 0.0, 0.0, 0.0);
        nw2.a();
        this.a.b(ft.j(this.a), i2.a, n3 + 32 + 2, n4 + 1, 0xFFFFFF);
        this.a.b(ft.k(this.a), i2.b, n3 + 32 + 2, n4 + 12, 0x808080);
        this.a.b(ft.l(this.a), i2.c, n3 + 32 + 2, n4 + 12 + 10, 0x808080);
    }
}

