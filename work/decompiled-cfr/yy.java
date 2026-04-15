/*
 * Decompiled with CFR 0.152.
 */
public class yy {
    public final int a;
    public final int b;

    public yy(int n2, int n3) {
        this.a = n2;
        this.b = n3;
    }

    public static int a(int n2, int n3) {
        return (n2 < 0 ? Integer.MIN_VALUE : 0) | (n2 & Short.MAX_VALUE) << 16 | (n3 < 0 ? 32768 : 0) | n3 & Short.MAX_VALUE;
    }

    public int hashCode() {
        return yy.a(this.a, this.b);
    }

    public boolean equals(Object object) {
        yy yy2 = (yy)object;
        return yy2.a == this.a && yy2.b == this.b;
    }
}

