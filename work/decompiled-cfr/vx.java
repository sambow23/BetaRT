/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class vx
extends da {
    private nb a;
    private boolean i = false;

    public vx(Minecraft minecraft, String string, int n2) {
        System.out.println("Connecting to " + string + ", " + n2);
        minecraft.a((fd)null);
        new wz(this, minecraft, string, n2).start();
    }

    public void a() {
        if (this.a != null) {
            this.a.a();
        }
    }

    protected void a(char c2, int n2) {
    }

    public void b() {
        nh nh2 = nh.a();
        this.e.clear();
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 120 + 12, nh2.a("gui.cancel")));
    }

    protected void a(ke ke2) {
        if (ke2.f == 0) {
            this.i = true;
            if (this.a != null) {
                this.a.b();
            }
            this.b.a(new fu());
        }
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        nh nh2 = nh.a();
        if (this.a == null) {
            this.a(this.g, nh2.a("connect.connecting"), this.c / 2, this.d / 2 - 50, 0xFFFFFF);
            this.a(this.g, "", this.c / 2, this.d / 2 - 10, 0xFFFFFF);
        } else {
            this.a(this.g, nh2.a("connect.authorizing"), this.c / 2, this.d / 2 - 50, 0xFFFFFF);
            this.a(this.g, this.a.a, this.c / 2, this.d / 2 - 10, 0xFFFFFF);
        }
        super.a(n2, n3, f2);
    }

    static /* synthetic */ nb a(vx vx2, nb nb2) {
        vx2.a = nb2;
        return vx2.a;
    }

    static /* synthetic */ boolean a(vx vx2) {
        return vx2.i;
    }

    static /* synthetic */ nb b(vx vx2) {
        return vx2.a;
    }
}

