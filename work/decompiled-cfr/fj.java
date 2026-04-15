/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import java.util.Random;
import org.lwjgl.input.Keyboard;

public class fj
extends da {
    private da a;
    private ro i;
    private ro j;
    private String l;
    private boolean m;

    public fj(da da2) {
        this.a = da2;
    }

    public void a() {
        this.i.b();
        this.j.b();
    }

    public void b() {
        nh nh2 = nh.a();
        Keyboard.enableRepeatEvents((boolean)true);
        this.e.clear();
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 96 + 12, nh2.a("selectWorld.create")));
        this.e.add(new ke(1, this.c / 2 - 100, this.d / 4 + 120 + 12, nh2.a("gui.cancel")));
        this.i = new ro(this, this.g, this.c / 2 - 100, 60, 200, 20, nh2.a("selectWorld.newWorld"));
        this.i.a = true;
        this.i.a(32);
        this.j = new ro(this, this.g, this.c / 2 - 100, 116, 200, 20, "");
        this.k();
    }

    private void k() {
        this.l = this.i.a().trim();
        for (char c2 : fp.b) {
            this.l = this.l.replace(c2, '_');
        }
        if (in.a(this.l)) {
            this.l = "World";
        }
        this.l = fj.a(this.b.c(), this.l);
    }

    public static String a(nl nl2, String string) {
        while (nl2.b(string) != null) {
            string = string + "-";
        }
        return string;
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
            this.b.a((da)null);
            if (this.m) {
                return;
            }
            this.m = true;
            long l2 = new Random().nextLong();
            String string = this.j.a();
            if (!in.a(string)) {
                try {
                    long l3 = Long.parseLong(string);
                    if (l3 != 0L) {
                        l2 = l3;
                    }
                }
                catch (NumberFormatException numberFormatException) {
                    l2 = string.hashCode();
                }
            }
            this.b.c = new os(this.b);
            this.b.a(this.l, this.i.a(), l2);
            this.b.a((da)null);
        }
    }

    protected void a(char c2, int n2) {
        if (this.i.a) {
            this.i.a(c2, n2);
        } else {
            this.j.a(c2, n2);
        }
        if (c2 == '\r') {
            this.a((ke)this.e.get(0));
        }
        ((ke)this.e.get((int)0)).g = this.i.a().length() > 0;
        this.k();
    }

    protected void a(int n2, int n3, int n4) {
        super.a(n2, n3, n4);
        this.i.a(n2, n3, n4);
        this.j.a(n2, n3, n4);
    }

    public void a(int n2, int n3, float f2) {
        nh nh2 = nh.a();
        this.i();
        this.a(this.g, nh2.a("selectWorld.create"), this.c / 2, this.d / 4 - 60 + 20, 0xFFFFFF);
        this.b(this.g, nh2.a("selectWorld.enterName"), this.c / 2 - 100, 47, 0xA0A0A0);
        this.b(this.g, nh2.a("selectWorld.resultFolder") + " " + this.l, this.c / 2 - 100, 85, 0xA0A0A0);
        this.b(this.g, nh2.a("selectWorld.enterSeed"), this.c / 2 - 100, 104, 0xA0A0A0);
        this.b(this.g, nh2.a("selectWorld.seedInfo"), this.c / 2 - 100, 140, 0xA0A0A0);
        this.i.c();
        this.j.c();
        super.a(n2, n3, f2);
    }

    public void j() {
        if (this.i.a) {
            this.i.a(false);
            this.j.a(true);
        } else {
            this.i.a(true);
            this.j.a(false);
        }
    }
}

