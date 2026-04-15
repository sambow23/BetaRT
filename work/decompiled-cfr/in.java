/*
 * Decompiled with CFR 0.152.
 */
public class in {
    private static float[] a = new float[65536];

    public static final float a(float f2) {
        return a[(int)(f2 * 10430.378f) & 0xFFFF];
    }

    public static final float b(float f2) {
        return a[(int)(f2 * 10430.378f + 16384.0f) & 0xFFFF];
    }

    public static final float c(float f2) {
        return (float)Math.sqrt(f2);
    }

    public static final float a(double d2) {
        return (float)Math.sqrt(d2);
    }

    public static int d(float f2) {
        int n2 = (int)f2;
        return f2 < (float)n2 ? n2 - 1 : n2;
    }

    public static int b(double d2) {
        int n2 = (int)d2;
        return d2 < (double)n2 ? n2 - 1 : n2;
    }

    public static float e(float f2) {
        return f2 >= 0.0f ? f2 : -f2;
    }

    public static double a(double d2, double d3) {
        if (d2 < 0.0) {
            d2 = -d2;
        }
        if (d3 < 0.0) {
            d3 = -d3;
        }
        return d2 > d3 ? d2 : d3;
    }

    public static int a(int n2, int n3) {
        if (n2 < 0) {
            return -((-n2 - 1) / n3) - 1;
        }
        return n2 / n3;
    }

    public static boolean a(String string) {
        return string == null || string.length() == 0;
    }

    static {
        for (int i2 = 0; i2 < 65536; ++i2) {
            in.a[i2] = (float)Math.sin((double)i2 * Math.PI * 2.0 / 65536.0);
        }
    }
}

