/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Lookup;

class Lsp {
    static final float M_PI = (float)Math.PI;

    Lsp() {
    }

    static void lsp_to_curve(float[] fArray, int[] nArray, int n2, int n3, float[] fArray2, int n4, float f2, float f3) {
        int n5;
        float f4 = (float)Math.PI / (float)n3;
        for (n5 = 0; n5 < n4; ++n5) {
            fArray2[n5] = Lookup.coslook(fArray2[n5]);
        }
        int n6 = n4 / 2 * 2;
        n5 = 0;
        while (n5 < n2) {
            int n7;
            int n8 = nArray[n5];
            float f5 = 0.70710677f;
            float f6 = 0.70710677f;
            float f7 = Lookup.coslook(f4 * (float)n8);
            for (n7 = 0; n7 < n6; n7 += 2) {
                f6 *= fArray2[n7] - f7;
                f5 *= fArray2[n7 + 1] - f7;
            }
            if ((n4 & 1) != 0) {
                f6 *= fArray2[n4 - 1] - f7;
                f6 *= f6;
                f5 *= f5 * (1.0f - f7 * f7);
            } else {
                f6 *= f6 * (1.0f + f7);
                f5 *= f5 * (1.0f - f7);
            }
            f6 = f5 + f6;
            n7 = Float.floatToIntBits(f6);
            int n9 = Integer.MAX_VALUE & n7;
            int n10 = 0;
            if (n9 < 2139095040 && n9 != 0) {
                if (n9 < 0x800000) {
                    f6 = (float)((double)f6 * 3.3554432E7);
                    n7 = Float.floatToIntBits(f6);
                    n9 = Integer.MAX_VALUE & n7;
                    n10 = -25;
                }
                n10 += (n9 >>> 23) - 126;
                n7 = n7 & 0x807FFFFF | 0x3F000000;
                f6 = Float.intBitsToFloat(n7);
            }
            f6 = Lookup.fromdBlook(f2 * Lookup.invsqlook(f6) * Lookup.invsq2explook(n10 + n4) - f3);
            do {
                int n11 = n5++;
                fArray[n11] = fArray[n11] * f6;
            } while (n5 < n2 && nArray[n5] == n8);
        }
    }
}

