/*
 * Decompiled with CFR 0.152.
 */
public abstract class hm {
    public final String a;
    private boolean b;

    public hm(String string) {
        this.a = string;
    }

    public abstract void a(nu var1);

    public abstract void b(nu var1);

    public void a() {
        this.a(true);
    }

    public void a(boolean bl2) {
        this.b = bl2;
    }

    public boolean b() {
        return this.b;
    }
}

