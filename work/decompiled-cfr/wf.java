/*
 * Decompiled with CFR 0.152.
 */
public class wf {
    public final int a;
    public final int b;
    public final int c;

    public wf(int n2, int n3, int n4) {
        this.a = n2;
        this.b = n3;
        this.c = n4;
    }

    public boolean equals(Object object) {
        if (object instanceof wf) {
            wf wf2 = (wf)object;
            return wf2.a == this.a && wf2.b == this.b && wf2.c == this.c;
        }
        return false;
    }

    public int hashCode() {
        return this.a * 8976890 + this.b * 981131 + this.c;
    }
}

