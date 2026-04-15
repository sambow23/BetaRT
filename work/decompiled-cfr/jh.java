/*
 * Decompiled with CFR 0.152.
 */
public class jh {
    private static int[] a = new int[65536];

    public static void a(int[] nArray) {
        a = nArray;
    }

    public static int a(double d2, double d3) {
        int n2 = (int)((1.0 - d2) * 255.0);
        int n3 = (int)((1.0 - (d3 *= d2)) * 255.0);
        return a[n3 << 8 | n2];
    }

    public static int a() {
        return 0x619961;
    }

    public static int b() {
        return 8431445;
    }

    public static int c() {
        return 4764952;
    }
}

