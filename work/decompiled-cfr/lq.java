/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import org.lwjgl.input.Keyboard;

public class lq
extends da {
    private da a;
    private ro i;

    public lq(da da2) {
        this.a = da2;
    }

    public void a() {
        this.i.b();
    }

    public void b() {
        nh nh2 = nh.a();
        Keyboard.enableRepeatEvents((boolean)true);
        this.e.clear();
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 96 + 12, nh2.a("multiplayer.connect")));
        this.e.add(new ke(1, this.c / 2 - 100, this.d / 4 + 120 + 12, nh2.a("gui.cancel")));
        String string = this.b.z.C.replaceAll("_", ":");
        ((ke)this.e.get((int)0)).g = string.length() > 0;
        this.i = new ro(this, this.g, this.c / 2 - 100, this.d / 4 - 10 + 50 + 18, 200, 20, string);
        this.i.a = true;
        this.i.a(128);
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
            int n2;
            String string = this.i.a().trim();
            this.b.z.C = string.replaceAll(":", "_");
            this.b.z.b();
            String[] stringArray = string.split(":");
            if (string.startsWith("[") && (n2 = string.indexOf("]")) > 0) {
                String string2 = string.substring(1, n2);
                String string3 = string.substring(n2 + 1).trim();
                if (string3.startsWith(":") && string3.length() > 0) {
                    string3 = string3.substring(1);
                    stringArray = new String[]{string2, string3};
                } else {
                    stringArray = new String[]{string2};
                }
            }
            if (stringArray.length > 2) {
                stringArray = new String[]{string};
            }
            this.b.a(new vx(this.b, stringArray[0], stringArray.length > 1 ? this.a(stringArray[1], 25565) : 25565));
        }
    }

    private int a(String string, int n2) {
        try {
            return Integer.parseInt(string.trim());
        }
        catch (Exception exception) {
            return n2;
        }
    }

    protected void a(char c2, int n2) {
        this.i.a(c2, n2);
        if (c2 == '\r') {
            this.a((ke)this.e.get(0));
        }
        ((ke)this.e.get((int)0)).g = this.i.a().length() > 0;
    }

    protected void a(int n2, int n3, int n4) {
        super.a(n2, n3, n4);
        this.i.a(n2, n3, n4);
    }

    public void a(int n2, int n3, float f2) {
        nh nh2 = nh.a();
        this.i();
        this.a(this.g, nh2.a("multiplayer.title"), this.c / 2, this.d / 4 - 60 + 20, 0xFFFFFF);
        this.b(this.g, nh2.a("multiplayer.info1"), this.c / 2 - 140, this.d / 4 - 60 + 60 + 0, 0xA0A0A0);
        this.b(this.g, nh2.a("multiplayer.info2"), this.c / 2 - 140, this.d / 4 - 60 + 60 + 9, 0xA0A0A0);
        this.b(this.g, nh2.a("multiplayer.ipinfo"), this.c / 2 - 140, this.d / 4 - 60 + 60 + 36, 0xA0A0A0);
        this.i.c();
        super.a(n2, n3, f2);
    }
}

