/*
 * Decompiled with CFR 0.152.
 */
public class vi {
    private static byte[] a = new byte[256];

    public static void a(byte[] byArray) {
        for (int i2 = 0; i2 < byArray.length; ++i2) {
            byArray[i2] = a[byArray[i2] & 0xFF];
        }
    }

    static {
        try {
            for (int i2 = 0; i2 < 256; ++i2) {
                byte by2 = (byte)i2;
                if (by2 != 0 && uu.m[by2 & 0xFF] == null) {
                    by2 = 0;
                }
                vi.a[i2] = by2;
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

