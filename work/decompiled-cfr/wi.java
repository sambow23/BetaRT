/*
 * Decompiled with CFR 0.152.
 */
public class wi {
    public final byte[] a;

    public wi(int n2) {
        this.a = new byte[n2 >> 1];
    }

    public wi(byte[] byArray) {
        this.a = byArray;
    }

    public int a(int n2, int n3, int n4) {
        int n5 = n2 << 11 | n4 << 7 | n3;
        int n6 = n5 >> 1;
        int n7 = n5 & 1;
        if (n7 == 0) {
            return this.a[n6] & 0xF;
        }
        return this.a[n6] >> 4 & 0xF;
    }

    public void a(int n2, int n3, int n4, int n5) {
        int n6 = n2 << 11 | n4 << 7 | n3;
        int n7 = n6 >> 1;
        int n8 = n6 & 1;
        this.a[n7] = n8 == 0 ? (byte)(this.a[n7] & 0xF0 | n5 & 0xF) : (byte)(this.a[n7] & 0xF | (n5 & 0xF) << 4);
    }

    public boolean a() {
        return this.a != null;
    }
}

