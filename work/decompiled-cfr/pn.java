/*
 * Decompiled with CFR 0.152.
 */
final class pn {
    private lb a;
    private lb b;

    private pn() {
    }

    static pn a() {
        return new pn();
    }

    pn a(lb lb2) {
        this.a = lb2;
        return this;
    }

    pn b(lb lb2) {
        this.b = lb2;
        return this;
    }

    qa b() {
        return (qa)this.a.b();
    }

    gu c() {
        return this.b.b();
    }
}

