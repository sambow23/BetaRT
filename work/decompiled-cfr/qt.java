/*
 * Decompiled with CFR 0.152.
 */
public class qt
extends da {
    private da a;
    private String i;
    private String j;
    private String l;
    private String m;
    private int n;

    public qt(da da2, String string, String string2, String string3, String string4, int n2) {
        this.a = da2;
        this.i = string;
        this.j = string2;
        this.l = string3;
        this.m = string4;
        this.n = n2;
    }

    public void b() {
        this.e.add(new ab(0, this.c / 2 - 155 + 0, this.d / 6 + 96, this.l));
        this.e.add(new ab(1, this.c / 2 - 155 + 160, this.d / 6 + 96, this.m));
    }

    protected void a(ke ke2) {
        this.a.a(ke2.f == 0, this.n);
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        this.a(this.g, this.i, this.c / 2, 70, 0xFFFFFF);
        this.a(this.g, this.j, this.c / 2, 90, 0xFFFFFF);
        super.a(n2, n3, f2);
    }
}

