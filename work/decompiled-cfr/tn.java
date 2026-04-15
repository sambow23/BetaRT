/*
 * Decompiled with CFR 0.152.
 */
public class tn
extends ow {
    public byte a = 0;
    public boolean b = false;

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("note", this.a);
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.a = nu2.c("note");
        if (this.a < 0) {
            this.a = 0;
        }
        if (this.a > 24) {
            this.a = (byte)24;
        }
    }

    public void a() {
        this.a = (byte)((this.a + 1) % 25);
        this.y_();
    }

    public void a(fd fd2, int n2, int n3, int n4) {
        if (fd2.f(n2, n3 + 1, n4) != ln.a) {
            return;
        }
        ln ln2 = fd2.f(n2, n3 - 1, n4);
        int n5 = 0;
        if (ln2 == ln.e) {
            n5 = 1;
        }
        if (ln2 == ln.n) {
            n5 = 2;
        }
        if (ln2 == ln.p) {
            n5 = 3;
        }
        if (ln2 == ln.d) {
            n5 = 4;
        }
        fd2.d(n2, n3, n4, n5, this.a);
    }
}

