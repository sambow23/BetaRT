/*
 * Decompiled with CFR 0.152.
 */
public class qy
implements Comparable {
    private static long f = 0L;
    public int a;
    public int b;
    public int c;
    public int d;
    public long e;
    private long g = f++;

    public qy(int n2, int n3, int n4, int n5) {
        this.a = n2;
        this.b = n3;
        this.c = n4;
        this.d = n5;
    }

    public boolean equals(Object object) {
        if (object instanceof qy) {
            qy qy2 = (qy)object;
            return this.a == qy2.a && this.b == qy2.b && this.c == qy2.c && this.d == qy2.d;
        }
        return false;
    }

    public int hashCode() {
        return (this.a * 128 * 1024 + this.c * 128 + this.b) * 256 + this.d;
    }

    public qy a(long l2) {
        this.e = l2;
        return this;
    }

    public int a(qy qy2) {
        if (this.e < qy2.e) {
            return -1;
        }
        if (this.e > qy2.e) {
            return 1;
        }
        if (this.g < qy2.g) {
            return -1;
        }
        if (this.g > qy2.g) {
            return 1;
        }
        return 0;
    }
}

