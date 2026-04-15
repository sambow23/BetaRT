/*
 * Decompiled with CFR 0.152.
 */
public class br
implements Comparable {
    public int a;
    public int b;
    public int c;

    public br() {
    }

    public br(int n2, int n3, int n4) {
        this.a = n2;
        this.b = n3;
        this.c = n4;
    }

    public br(br br2) {
        this.a = br2.a;
        this.b = br2.b;
        this.c = br2.c;
    }

    public boolean equals(Object object) {
        if (!(object instanceof br)) {
            return false;
        }
        br br2 = (br)object;
        return this.a == br2.a && this.b == br2.b && this.c == br2.c;
    }

    public int hashCode() {
        return this.a + this.c << 8 + this.b << 16;
    }

    public int a(br br2) {
        if (this.b == br2.b) {
            if (this.c == br2.c) {
                return this.a - br2.a;
            }
            return this.c - br2.c;
        }
        return this.b - br2.b;
    }

    public double a(int n2, int n3, int n4) {
        int n5 = this.a - n2;
        int n6 = this.b - n3;
        int n7 = this.c - n4;
        return Math.sqrt(n5 * n5 + n6 * n6 + n7 * n7);
    }
}

