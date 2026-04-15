/*
 * Decompiled with CFR 0.152.
 */
public class ex
extends da {
    private String a;
    private String i;

    public ex(String string, String string2, Object ... objectArray) {
        nh nh2 = nh.a();
        this.a = nh2.a(string);
        this.i = objectArray != null ? nh2.a(string2, objectArray) : nh2.a(string2);
    }

    public void a() {
    }

    protected void a(char c2, int n2) {
    }

    public void b() {
        nh nh2 = nh.a();
        this.e.clear();
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 120 + 12, nh2.a("gui.toMenu")));
    }

    protected void a(ke ke2) {
        if (ke2.f == 0) {
            this.b.a(new fu());
        }
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        this.a(this.g, this.a, this.c / 2, this.d / 2 - 50, 0xFFFFFF);
        this.a(this.g, this.i, this.c / 2, this.d / 2 - 10, 0xFFFFFF);
        super.a(n2, n3, f2);
    }
}

