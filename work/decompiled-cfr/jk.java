/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import org.lwjgl.input.Keyboard;

public class jk
extends da {
    private da a;
    private ro i;
    private final String j;

    public jk(da da2, String string) {
        this.a = da2;
        this.j = string;
    }

    public void a() {
        this.i.b();
    }

    public void b() {
        nh nh2 = nh.a();
        Keyboard.enableRepeatEvents((boolean)true);
        this.e.clear();
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 96 + 12, nh2.a("selectWorld.renameButton")));
        this.e.add(new ke(1, this.c / 2 - 100, this.d / 4 + 120 + 12, nh2.a("gui.cancel")));
        nl nl2 = this.b.c();
        ei ei2 = nl2.b(this.j);
        String string = ei2.j();
        this.i = new ro(this, this.g, this.c / 2 - 100, 60, 200, 20, string);
        this.i.a = true;
        this.i.a(32);
    }

    public void h() {
        Keyboard.enableRepeatEvents((boolean)false);
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == 1) {
            this.b.a(this.a);
        } else if (ke2.f == 0) {
            nl nl2 = this.b.c();
            nl2.a(this.j, this.i.a().trim());
            this.b.a(this.a);
        }
    }

    protected void a(char c2, int n2) {
        this.i.a(c2, n2);
        boolean bl2 = ((ke)this.e.get((int)0)).g = this.i.a().trim().length() > 0;
        if (c2 == '\r') {
            this.a((ke)this.e.get(0));
        }
    }

    protected void a(int n2, int n3, int n4) {
        super.a(n2, n3, n4);
        this.i.a(n2, n3, n4);
    }

    public void a(int n2, int n3, float f2) {
        nh nh2 = nh.a();
        this.i();
        this.a(this.g, nh2.a("selectWorld.renameTitle"), this.c / 2, this.d / 4 - 60 + 20, 0xFFFFFF);
        this.b(this.g, nh2.a("selectWorld.enterName"), this.c / 2 - 100, 47, 0xA0A0A0);
        this.i.c();
        super.a(n2, n3, f2);
    }
}

