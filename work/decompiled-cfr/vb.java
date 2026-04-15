/*
 * Decompiled with CFR 0.152.
 */
public class vb
implements Comparable {
    private final String a;
    private final String b;
    private final long c;
    private final long d;
    private final boolean e;

    public vb(String string, String string2, long l2, long l3, boolean bl2) {
        this.a = string;
        this.b = string2;
        this.c = l2;
        this.d = l3;
        this.e = bl2;
    }

    public String a() {
        return this.a;
    }

    public String b() {
        return this.b;
    }

    public long c() {
        return this.d;
    }

    public boolean d() {
        return this.e;
    }

    public long e() {
        return this.c;
    }

    public int a(vb vb2) {
        if (this.c < vb2.c) {
            return 1;
        }
        if (this.c > vb2.c) {
            return -1;
        }
        return this.a.compareTo(vb2.a);
    }
}

