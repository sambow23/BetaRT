/*
 * Decompiled with CFR 0.152.
 */
public class qq {
    private int d;
    private int e;
    public double a;
    public double b;
    public int c;

    public qq(kv kv2, int n2, int n3) {
        this.d = n2;
        this.e = n3;
        this.c = 1;
        int n4 = kv2.I;
        if (n4 == 0) {
            n4 = 1000;
        }
        while (this.c < n4 && this.d / (this.c + 1) >= 320 && this.e / (this.c + 1) >= 240) {
            ++this.c;
        }
        this.a = (double)this.d / (double)this.c;
        this.b = (double)this.e / (double)this.c;
        this.d = (int)Math.ceil(this.a);
        this.e = (int)Math.ceil(this.b);
    }

    public int a() {
        return this.d;
    }

    public int b() {
        return this.e;
    }
}

