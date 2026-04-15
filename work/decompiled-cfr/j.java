/*
 * Decompiled with CFR 0.152.
 */
final class j
implements yp {
    private final jt a;
    private final jt b;

    j(jt jt2, jt jt3) {
        this.a = jt2;
        this.b = jt3;
    }

    public boolean a(Object object) {
        return this.a.a(object) && this.b.a(this.a.b(object));
    }

    public Object b(Object object) {
        Object object2;
        Object object3;
        try {
            object3 = this.a.b(object);
        }
        catch (mz mz2) {
            throw mz.b(mz2, this.a);
        }
        try {
            object2 = this.b.b(object3);
        }
        catch (mz mz3) {
            throw mz.a(mz3, this.a);
        }
        return object2;
    }

    public String a() {
        return this.b.a();
    }

    public String toString() {
        return this.a.toString() + ", with " + this.b.toString();
    }
}

