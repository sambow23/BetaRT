/*
 * Decompiled with CFR 0.152.
 */
import java.util.Stack;

final class lt
implements wg {
    private final Stack a = new Stack();
    private lb b;

    lt() {
    }

    qe a() {
        return (qe)this.b.b();
    }

    public void b() {
    }

    public void c() {
    }

    public void d() {
        ed ed2 = te.e();
        this.a(ed2);
        this.a.push(new na(this, ed2));
    }

    public void e() {
        this.a.pop();
    }

    public void f() {
        sx sx2 = te.d();
        this.a(sx2);
        this.a.push(new my(this, sx2));
    }

    public void g() {
        this.a.pop();
    }

    public void a(String string) {
        pn pn2 = pn.a().a(te.b(string));
        ((pu)this.a.peek()).a(pn2);
        this.a.push(new nc(this, pn2));
    }

    public void h() {
        this.a.pop();
    }

    public void b(String string) {
        this.b(te.a(string));
    }

    public void i() {
        this.b(te.b());
    }

    public void c(String string) {
        this.b(te.b(string));
    }

    public void j() {
        this.b(te.c());
    }

    public void k() {
        this.b(te.a());
    }

    private void a(lb lb2) {
        if (this.b == null) {
            this.b = lb2;
        } else {
            this.b(lb2);
        }
    }

    private void b(lb lb2) {
        ((pu)this.a.peek()).a(lb2);
    }
}

