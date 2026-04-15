/*
 * Decompiled with CFR 0.152.
 */
public class yk
extends ow {
    public String[] a = new String[]{"", "", "", ""};
    public int b = -1;
    private boolean c = true;

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Text1", this.a[0]);
        nu2.a("Text2", this.a[1]);
        nu2.a("Text3", this.a[2]);
        nu2.a("Text4", this.a[3]);
    }

    public void a(nu nu2) {
        this.c = false;
        super.a(nu2);
        for (int i2 = 0; i2 < 4; ++i2) {
            this.a[i2] = nu2.i("Text" + (i2 + 1));
            if (this.a[i2].length() <= 15) continue;
            this.a[i2] = this.a[i2].substring(0, 15);
        }
    }
}

