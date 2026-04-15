/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

class Drft {
    int n;
    float[] trigcache;
    int[] splitcache;
    static int[] ntryh = new int[]{4, 2, 3, 5};
    static float tpi = (float)Math.PI * 2;
    static float hsqt2 = 0.70710677f;
    static float taui = 0.8660254f;
    static float taur = -0.5f;
    static float sqrt2 = 1.4142135f;

    Drft() {
    }

    void backward(float[] fArray) {
        if (this.n == 1) {
            return;
        }
        Drft.drftb1(this.n, fArray, this.trigcache, this.trigcache, this.n, this.splitcache);
    }

    void init(int n2) {
        this.n = n2;
        this.trigcache = new float[3 * n2];
        this.splitcache = new int[32];
        Drft.fdrffti(n2, this.trigcache, this.splitcache);
    }

    void clear() {
        if (this.trigcache != null) {
            this.trigcache = null;
        }
        if (this.splitcache != null) {
            this.splitcache = null;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    static void drfti1(int n2, float[] fArray, int n3, int[] nArray) {
        int n4 = 0;
        int n5 = -1;
        int n6 = n2;
        int n7 = 0;
        int n8 = 101;
        while (true) {
            switch (n8) {
                case 101: {
                    n4 = ++n5 < 4 ? ntryh[n5] : (n4 += 2);
                }
                case 104: {
                    int n9;
                    int n10 = n6 / n4;
                    int n11 = n6 - n4 * n10;
                    if (n11 != 0) {
                        n8 = 101;
                        break;
                    }
                    nArray[++n7 + 1] = n4;
                    n6 = n10;
                    if (n4 != 2) {
                        n8 = 107;
                        break;
                    }
                    if (n7 == 1) {
                        n8 = 107;
                        break;
                    }
                    for (n9 = 1; n9 < n7; ++n9) {
                        int n12 = n7 - n9 + 1;
                        nArray[n12 + 1] = nArray[n12];
                    }
                    nArray[2] = 2;
                }
                case 107: {
                    int n9;
                    if (n6 != 1) {
                        n8 = 104;
                        break;
                    }
                    nArray[0] = n2;
                    nArray[1] = n7;
                    float f2 = tpi / (float)n2;
                    int n13 = 0;
                    int n14 = n7 - 1;
                    int n15 = 1;
                    if (n14 == 0) {
                        return;
                    }
                    for (int i2 = 0; i2 < n14; ++i2) {
                        int n16 = nArray[i2 + 2];
                        int n17 = 0;
                        int n18 = n15 * n16;
                        int n19 = n2 / n18;
                        int n20 = n16 - 1;
                        for (n5 = 0; n5 < n20; n13 += n19, ++n5) {
                            n9 = n13;
                            float f3 = (float)(n17 += n15) * f2;
                            float f4 = 0.0f;
                            for (int i3 = 2; i3 < n19; i3 += 2) {
                                float f5 = (f4 += 1.0f) * f3;
                                fArray[n3 + n9++] = (float)Math.cos(f5);
                                fArray[n3 + n9++] = (float)Math.sin(f5);
                            }
                        }
                        n15 = n18;
                    }
                    return;
                }
            }
        }
    }

    static void fdrffti(int n2, float[] fArray, int[] nArray) {
        if (n2 == 1) {
            return;
        }
        Drft.drfti1(n2, fArray, n2, nArray);
    }

    static void dradf2(int n2, int n3, float[] fArray, float[] fArray2, float[] fArray3, int n4) {
        int n5;
        int n6;
        int n7 = 0;
        int n8 = n6 = n3 * n2;
        int n9 = n2 << 1;
        for (n5 = 0; n5 < n3; ++n5) {
            fArray2[n7 << 1] = fArray[n7] + fArray[n6];
            fArray2[(n7 << 1) + n9 - 1] = fArray[n7] - fArray[n6];
            n7 += n2;
            n6 += n2;
        }
        if (n2 < 2) {
            return;
        }
        if (n2 != 2) {
            n7 = 0;
            n6 = n8;
            for (n5 = 0; n5 < n3; ++n5) {
                n9 = n6;
                int n10 = (n7 << 1) + (n2 << 1);
                int n11 = n7;
                int n12 = n7 + n7;
                for (int i2 = 2; i2 < n2; i2 += 2) {
                    float f2 = fArray3[n4 + i2 - 2] * fArray[(n9 += 2) - 1] + fArray3[n4 + i2 - 1] * fArray[n9];
                    float f3 = fArray3[n4 + i2 - 2] * fArray[n9] - fArray3[n4 + i2 - 1] * fArray[n9 - 1];
                    fArray2[n12 += 2] = fArray[n11 += 2] + f3;
                    fArray2[n10 -= 2] = f3 - fArray[n11];
                    fArray2[n12 - 1] = fArray[n11 - 1] + f2;
                    fArray2[n10 - 1] = fArray[n11 - 1] - f2;
                }
                n7 += n2;
                n6 += n2;
            }
            if (n2 % 2 == 1) {
                return;
            }
        }
        n7 = n2;
        n9 = n6 = n7 - 1;
        n6 += n8;
        for (n5 = 0; n5 < n3; ++n5) {
            fArray2[n7] = -fArray[n6];
            fArray2[n7 - 1] = fArray[n9];
            n7 += n2 << 1;
            n6 += n2;
            n9 += n2;
        }
    }

    static void dradf4(int n2, int n3, float[] fArray, float[] fArray2, float[] fArray3, int n4, float[] fArray4, int n5, float[] fArray5, int n6) {
        float f2;
        int n7;
        int n8;
        float f3;
        float f4;
        int n9;
        int n10;
        int n11 = n10 = n3 * n2;
        int n12 = n11 << 1;
        int n13 = n11 + (n11 << 1);
        int n14 = 0;
        for (n9 = 0; n9 < n3; ++n9) {
            f4 = fArray[n11] + fArray[n13];
            f3 = fArray[n14] + fArray[n12];
            n8 = n14 << 2;
            fArray2[n8] = f4 + f3;
            fArray2[(n2 << 2) + n8 - 1] = f3 - f4;
            fArray2[(n8 += n2 << 1) - 1] = fArray[n14] - fArray[n12];
            fArray2[n8] = fArray[n13] - fArray[n11];
            n11 += n2;
            n13 += n2;
            n14 += n2;
            n12 += n2;
        }
        if (n2 < 2) {
            return;
        }
        if (n2 != 2) {
            n11 = 0;
            for (n9 = 0; n9 < n3; ++n9) {
                n13 = n11;
                n12 = n11 << 2;
                n7 = n2 << 1;
                n8 = n7 + n12;
                for (int i2 = 2; i2 < n2; i2 += 2) {
                    n14 = n13 += 2;
                    n12 += 2;
                    n8 -= 2;
                    float f5 = fArray3[n4 + i2 - 2] * fArray[(n14 += n10) - 1] + fArray3[n4 + i2 - 1] * fArray[n14];
                    float f6 = fArray3[n4 + i2 - 2] * fArray[n14] - fArray3[n4 + i2 - 1] * fArray[n14 - 1];
                    float f7 = fArray4[n5 + i2 - 2] * fArray[(n14 += n10) - 1] + fArray4[n5 + i2 - 1] * fArray[n14];
                    float f8 = fArray4[n5 + i2 - 2] * fArray[n14] - fArray4[n5 + i2 - 1] * fArray[n14 - 1];
                    float f9 = fArray5[n6 + i2 - 2] * fArray[(n14 += n10) - 1] + fArray5[n6 + i2 - 1] * fArray[n14];
                    float f10 = fArray5[n6 + i2 - 2] * fArray[n14] - fArray5[n6 + i2 - 1] * fArray[n14 - 1];
                    f4 = f5 + f9;
                    float f11 = f9 - f5;
                    f2 = f6 + f10;
                    float f12 = f6 - f10;
                    float f13 = fArray[n13] + f8;
                    float f14 = fArray[n13] - f8;
                    f3 = fArray[n13 - 1] + f7;
                    float f15 = fArray[n13 - 1] - f7;
                    fArray2[n12 - 1] = f4 + f3;
                    fArray2[n12] = f2 + f13;
                    fArray2[n8 - 1] = f15 - f12;
                    fArray2[n8] = f11 - f14;
                    fArray2[n12 + n7 - 1] = f12 + f15;
                    fArray2[n12 + n7] = f11 + f14;
                    fArray2[n8 + n7 - 1] = f3 - f4;
                    fArray2[n8 + n7] = f2 - f13;
                }
                n11 += n2;
            }
            if ((n2 & 1) != 0) {
                return;
            }
        }
        n11 = n10 + n2 - 1;
        n13 = n11 + (n10 << 1);
        n14 = n2 << 2;
        n12 = n2;
        n8 = n2 << 1;
        n7 = n2;
        for (n9 = 0; n9 < n3; ++n9) {
            f2 = -hsqt2 * (fArray[n11] + fArray[n13]);
            f4 = hsqt2 * (fArray[n11] - fArray[n13]);
            fArray2[n12 - 1] = f4 + fArray[n7 - 1];
            fArray2[n12 + n8 - 1] = fArray[n7 - 1] - f4;
            fArray2[n12] = f2 - fArray[n11 + n10];
            fArray2[n12 + n8] = f2 + fArray[n11 + n10];
            n11 += n2;
            n13 += n2;
            n12 += n14;
            n7 += n2;
        }
    }

    /*
     * Unable to fully structure code
     */
    static void dradfg(int var0, int var1_1, int var2_2, int var3_3, float[] var4_4, float[] var5_5, float[] var6_6, float[] var7_7, float[] var8_8, float[] var9_9, int var10_10) {
        var22_11 = 0;
        var38_12 = 0.0f;
        var40_13 = 0.0f;
        var39_14 = Drft.tpi / (float)var1_1;
        var38_12 = (float)Math.cos(var39_14);
        var40_13 = (float)Math.sin(var39_14);
        var12_15 = var1_1 + 1 >> 1;
        var44_16 = var1_1;
        var43_17 = var0;
        var37_18 = var0 - 1 >> 1;
        var20_19 = var2_2 * var0;
        var30_20 = var1_1 * var0;
        var45_21 = 100;
        block7: while (true) {
            switch (var45_21) {
                case 101: {
                    if (var0 != 1) ** GOTO lbl20
                    var45_21 = 119;
                    ** GOTO lbl258
lbl20:
                    // 2 sources

                    for (var18_27 = 0; var18_27 < var3_3; ++var18_27) {
                        var8_8[var18_27] = var6_6[var18_27];
                    }
                    var21_29 = 0;
                    for (var14_24 = 1; var14_24 < var1_1; ++var14_24) {
                        var22_11 = var21_29 += var20_19;
                        for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                            var7_7[var22_11] = var5_5[var22_11];
                            var22_11 += var0;
                        }
                    }
                    var19_28 = -var0;
                    var21_29 = 0;
                    if (var37_18 > var2_2) {
                        for (var14_24 = 1; var14_24 < var1_1; ++var14_24) {
                            var19_28 += var0;
                            var22_11 = -var0 + (var21_29 += var20_19);
                            for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                                var11_22 = var19_28 - 1;
                                var23_30 = var22_11 += var0;
                                for (var13_23 = 2; var13_23 < var0; var13_23 += 2) {
                                    var7_7[(var23_30 += 2) - 1] = var9_9[var10_10 + (var11_22 += 2) - 1] * var5_5[var23_30 - 1] + var9_9[var10_10 + var11_22] * var5_5[var23_30];
                                    var7_7[var23_30] = var9_9[var10_10 + var11_22 - 1] * var5_5[var23_30] - var9_9[var10_10 + var11_22] * var5_5[var23_30 - 1];
                                }
                            }
                        }
                    } else {
                        for (var14_24 = 1; var14_24 < var1_1; ++var14_24) {
                            var11_22 = (var19_28 += var0) - 1;
                            var22_11 = var21_29 += var20_19;
                            for (var13_23 = 2; var13_23 < var0; var13_23 += 2) {
                                var11_22 += 2;
                                var23_30 = var22_11 += 2;
                                for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                                    var7_7[var23_30 - 1] = var9_9[var10_10 + var11_22 - 1] * var5_5[var23_30 - 1] + var9_9[var10_10 + var11_22] * var5_5[var23_30];
                                    var7_7[var23_30] = var9_9[var10_10 + var11_22 - 1] * var5_5[var23_30] - var9_9[var10_10 + var11_22] * var5_5[var23_30 - 1];
                                    var23_30 += var0;
                                }
                            }
                        }
                    }
                    var21_29 = 0;
                    var22_11 = var44_16 * var20_19;
                    if (var37_18 < var2_2) {
                        for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                            var23_30 = var21_29 += var20_19;
                            var24_31 = var22_11 -= var20_19;
                            for (var13_23 = 2; var13_23 < var0; var13_23 += 2) {
                                var25_32 = (var23_30 += 2) - var0;
                                var26_33 = (var24_31 += 2) - var0;
                                for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                                    var5_5[(var25_32 += var0) - 1] = var7_7[var25_32 - 1] + var7_7[(var26_33 += var0) - 1];
                                    var5_5[var26_33 - 1] = var7_7[var25_32] - var7_7[var26_33];
                                    var5_5[var25_32] = var7_7[var25_32] + var7_7[var26_33];
                                    var5_5[var26_33] = var7_7[var26_33 - 1] - var7_7[var25_32 - 1];
                                }
                            }
                        }
                    } else {
                        for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                            var23_30 = var21_29 += var20_19;
                            var24_31 = var22_11 -= var20_19;
                            for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                                var25_32 = var23_30;
                                var26_33 = var24_31;
                                for (var13_23 = 2; var13_23 < var0; var13_23 += 2) {
                                    var5_5[(var25_32 += 2) - 1] = var7_7[var25_32 - 1] + var7_7[(var26_33 += 2) - 1];
                                    var5_5[var26_33 - 1] = var7_7[var25_32] - var7_7[var26_33];
                                    var5_5[var25_32] = var7_7[var25_32] + var7_7[var26_33];
                                    var5_5[var26_33] = var7_7[var26_33 - 1] - var7_7[var25_32 - 1];
                                }
                                var23_30 += var0;
                                var24_31 += var0;
                            }
                        }
                    }
                }
                case 119: {
                    for (var18_27 = 0; var18_27 < var3_3; ++var18_27) {
                        var6_6[var18_27] = var8_8[var18_27];
                    }
                    var21_29 = 0;
                    var22_11 = var44_16 * var3_3;
                    for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                        var23_30 = (var21_29 += var20_19) - var0;
                        var24_31 = (var22_11 -= var20_19) - var0;
                        for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                            var5_5[var23_30 += var0] = var7_7[var23_30] + var7_7[var24_31 += var0];
                            var5_5[var24_31] = var7_7[var24_31] - var7_7[var23_30];
                        }
                    }
                    var34_40 = 1.0f;
                    var32_38 = 0.0f;
                    var21_29 = 0;
                    var22_11 = var44_16 * var3_3;
                    var23_30 = (var1_1 - 1) * var3_3;
                    for (var16_26 = 1; var16_26 < var12_15; ++var16_26) {
                        var41_43 = var38_12 * var34_40 - var40_13 * var32_38;
                        var32_38 = var38_12 * var32_38 + var40_13 * var34_40;
                        var34_40 = var41_43;
                        var24_31 = var21_29 += var3_3;
                        var25_32 = var22_11 -= var3_3;
                        var26_33 = var23_30;
                        var27_34 = var3_3;
                        for (var18_27 = 0; var18_27 < var3_3; ++var18_27) {
                            var8_8[var24_31++] = var6_6[var18_27] + var34_40 * var6_6[var27_34++];
                            var8_8[var25_32++] = var32_38 * var6_6[var26_33++];
                        }
                        var31_37 = var34_40;
                        var36_42 = var32_38;
                        var35_41 = var34_40;
                        var33_39 = var32_38;
                        var24_31 = var3_3;
                        var25_32 = (var44_16 - 1) * var3_3;
                        for (var14_24 = 2; var14_24 < var12_15; ++var14_24) {
                            var24_31 += var3_3;
                            var25_32 -= var3_3;
                            var42_44 = var31_37 * var35_41 - var36_42 * var33_39;
                            var33_39 = var31_37 * var33_39 + var36_42 * var35_41;
                            var35_41 = var42_44;
                            var26_33 = var21_29;
                            var27_34 = var22_11;
                            var28_35 = var24_31;
                            var29_36 = var25_32;
                            for (var18_27 = 0; var18_27 < var3_3; ++var18_27) {
                                v0 = var26_33++;
                                var8_8[v0] = var8_8[v0] + var35_41 * var6_6[var28_35++];
                                v1 = var27_34++;
                                var8_8[v1] = var8_8[v1] + var33_39 * var6_6[var29_36++];
                            }
                        }
                    }
                    var21_29 = 0;
                    for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                        var22_11 = var21_29 += var3_3;
                        var18_27 = 0;
                        while (var18_27 < var3_3) {
                            v2 = var18_27++;
                            var8_8[v2] = var8_8[v2] + var6_6[var22_11++];
                        }
                    }
                    if (var0 < var2_2) {
                        var45_21 = 132;
                    } else {
                        var21_29 = 0;
                        var22_11 = 0;
                        for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                            var23_30 = var21_29;
                            var24_31 = var22_11;
                            for (var13_23 = 0; var13_23 < var0; ++var13_23) {
                                var4_4[var24_31++] = var7_7[var23_30++];
                            }
                            var21_29 += var0;
                            var22_11 += var30_20;
                        }
                        var45_21 = 135;
                    }
                    ** GOTO lbl258
                }
                case 132: {
                    for (var13_23 = 0; var13_23 < var0; ++var13_23) {
                        var21_29 = var13_23;
                        var22_11 = var13_23;
                        for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                            var4_4[var22_11] = var7_7[var21_29];
                            var21_29 += var0;
                            var22_11 += var30_20;
                        }
                    }
                }
                case 135: {
                    var21_29 = 0;
                    var22_11 = var0 << 1;
                    var23_30 = 0;
                    var24_31 = var44_16 * var20_19;
                    for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                        var25_32 = var21_29 += var22_11;
                        var26_33 = var23_30 += var20_19;
                        var27_34 = var24_31 -= var20_19;
                        for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                            var4_4[var25_32 - 1] = var7_7[var26_33];
                            var4_4[var25_32] = var7_7[var27_34];
                            var25_32 += var30_20;
                            var26_33 += var0;
                            var27_34 += var0;
                        }
                    }
                    if (var0 == 1) {
                        return;
                    }
                    if (var37_18 >= var2_2) ** GOTO lbl206
                    var45_21 = 141;
                    ** GOTO lbl258
lbl206:
                    // 1 sources

                    var21_29 = -var0;
                    var23_30 = 0;
                    var24_31 = 0;
                    var25_32 = var44_16 * var20_19;
                    for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                        var26_33 = var21_29 += var22_11;
                        var27_34 = var23_30 += var22_11;
                        var28_35 = var24_31 += var20_19;
                        var29_36 = var25_32 -= var20_19;
                        for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                            for (var13_23 = 2; var13_23 < var0; var13_23 += 2) {
                                var17_45 = var43_17 - var13_23;
                                var4_4[var13_23 + var27_34 - 1] = var7_7[var13_23 + var28_35 - 1] + var7_7[var13_23 + var29_36 - 1];
                                var4_4[var17_45 + var26_33 - 1] = var7_7[var13_23 + var28_35 - 1] - var7_7[var13_23 + var29_36 - 1];
                                var4_4[var13_23 + var27_34] = var7_7[var13_23 + var28_35] + var7_7[var13_23 + var29_36];
                                var4_4[var17_45 + var26_33] = var7_7[var13_23 + var29_36] - var7_7[var13_23 + var28_35];
                            }
                            var26_33 += var30_20;
                            var27_34 += var30_20;
                            var28_35 += var0;
                            var29_36 += var0;
                        }
                    }
                    return;
                }
                case 141: {
                    var21_29 = -var0;
                    var23_30 = 0;
                    var24_31 = 0;
                    var25_32 = var44_16 * var20_19;
                    for (var14_24 = 1; var14_24 < var12_15; ++var14_24) {
                        var21_29 += var22_11;
                        var23_30 += var22_11;
                        var24_31 += var20_19;
                        var25_32 -= var20_19;
                        for (var13_23 = 2; var13_23 < var0; var13_23 += 2) {
                            var26_33 = var43_17 + var21_29 - var13_23;
                            var27_34 = var13_23 + var23_30;
                            var28_35 = var13_23 + var24_31;
                            var29_36 = var13_23 + var25_32;
                            for (var15_25 = 0; var15_25 < var2_2; ++var15_25) {
                                var4_4[var27_34 - 1] = var7_7[var28_35 - 1] + var7_7[var29_36 - 1];
                                var4_4[var26_33 - 1] = var7_7[var28_35 - 1] - var7_7[var29_36 - 1];
                                var4_4[var27_34] = var7_7[var28_35] + var7_7[var29_36];
                                var4_4[var26_33] = var7_7[var29_36] - var7_7[var28_35];
                                var26_33 += var30_20;
                                var27_34 += var30_20;
                                var28_35 += var0;
                                var29_36 += var0;
                            }
                        }
                    }
                    break block7;
                }
lbl258:
                // 5 sources

                default: {
                    continue block7;
                }
            }
            break;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    static void drftf1(int n2, float[] fArray, float[] fArray2, float[] fArray3, int[] nArray) {
        int n3 = nArray[1];
        int n4 = 1;
        int n5 = n2;
        int n6 = n2;
        int n7 = 0;
        while (true) {
            int n8;
            int n9;
            int n10;
            int n11;
            if (n7 < n3) {
                int n12 = n3 - n7;
                n11 = nArray[n12 + 1];
                n10 = n5 / n11;
                n9 = n2 / n5;
                n8 = n9 * n10;
                n6 -= (n11 - 1) * n9;
            } else {
                if (n4 == 1) {
                    return;
                }
                int n13 = 0;
                while (true) {
                    if (n13 >= n2) {
                        return;
                    }
                    fArray[n13] = fArray2[n13];
                    ++n13;
                }
            }
            n4 = 1 - n4;
            int n14 = 100;
            block10: while (true) {
                switch (n14) {
                    case 100: {
                        if (n11 != 4) {
                            n14 = 102;
                            break;
                        }
                        int n15 = n6 + n9;
                        int n16 = n15 + n9;
                        if (n4 != 0) {
                            Drft.dradf4(n9, n10, fArray2, fArray, fArray3, n6 - 1, fArray3, n15 - 1, fArray3, n16 - 1);
                        } else {
                            Drft.dradf4(n9, n10, fArray, fArray2, fArray3, n6 - 1, fArray3, n15 - 1, fArray3, n16 - 1);
                        }
                        n14 = 110;
                        break;
                    }
                    case 102: {
                        if (n11 != 2) {
                            n14 = 104;
                            break;
                        }
                        if (n4 != 0) {
                            n14 = 103;
                            break;
                        }
                        Drft.dradf2(n9, n10, fArray, fArray2, fArray3, n6 - 1);
                        n14 = 110;
                        break;
                    }
                    case 103: {
                        Drft.dradf2(n9, n10, fArray2, fArray, fArray3, n6 - 1);
                    }
                    case 104: {
                        if (n9 == 1) {
                            n4 = 1 - n4;
                        }
                        if (n4 != 0) {
                            n14 = 109;
                            break;
                        }
                        Drft.dradfg(n9, n11, n10, n8, fArray, fArray, fArray, fArray2, fArray2, fArray3, n6 - 1);
                        n4 = 1;
                        n14 = 110;
                        break;
                    }
                    case 109: {
                        Drft.dradfg(n9, n11, n10, n8, fArray2, fArray2, fArray2, fArray, fArray, fArray3, n6 - 1);
                        n4 = 0;
                    }
                    case 110: {
                        n5 = n10;
                        break block10;
                    }
                }
            }
            ++n7;
        }
    }

    static void dradb2(int n2, int n3, float[] fArray, float[] fArray2, float[] fArray3, int n4) {
        int n5;
        int n6 = n3 * n2;
        int n7 = 0;
        int n8 = 0;
        int n9 = (n2 << 1) - 1;
        for (n5 = 0; n5 < n3; ++n5) {
            fArray2[n7] = fArray[n8] + fArray[n9 + n8];
            fArray2[n7 + n6] = fArray[n8] - fArray[n9 + n8];
            n8 = (n7 += n2) << 1;
        }
        if (n2 < 2) {
            return;
        }
        if (n2 != 2) {
            n7 = 0;
            n8 = 0;
            for (n5 = 0; n5 < n3; ++n5) {
                n9 = n7;
                int n10 = n8;
                int n11 = n10 + (n2 << 1);
                int n12 = n6 + n7;
                for (int i2 = 2; i2 < n2; i2 += 2) {
                    fArray2[(n9 += 2) - 1] = fArray[(n10 += 2) - 1] + fArray[(n11 -= 2) - 1];
                    float f2 = fArray[n10 - 1] - fArray[n11 - 1];
                    fArray2[n9] = fArray[n10] - fArray[n11];
                    float f3 = fArray[n10] + fArray[n11];
                    fArray2[(n12 += 2) - 1] = fArray3[n4 + i2 - 2] * f2 - fArray3[n4 + i2 - 1] * f3;
                    fArray2[n12] = fArray3[n4 + i2 - 2] * f3 + fArray3[n4 + i2 - 1] * f2;
                }
                n8 = (n7 += n2) << 1;
            }
            if (n2 % 2 == 1) {
                return;
            }
        }
        n7 = n2 - 1;
        n8 = n2 - 1;
        for (n5 = 0; n5 < n3; ++n5) {
            fArray2[n7] = fArray[n8] + fArray[n8];
            fArray2[n7 + n6] = -(fArray[n8 + 1] + fArray[n8 + 1]);
            n7 += n2;
            n8 += n2 << 1;
        }
    }

    static void dradb3(int n2, int n3, float[] fArray, float[] fArray2, float[] fArray3, int n4, float[] fArray4, int n5) {
        float f2;
        float f3;
        float f4;
        int n6;
        int n7 = n3 * n2;
        int n8 = 0;
        int n9 = n7 << 1;
        int n10 = n2 << 1;
        int n11 = n2 + (n2 << 1);
        int n12 = 0;
        for (n6 = 0; n6 < n3; ++n6) {
            f4 = fArray[n10 - 1] + fArray[n10 - 1];
            f3 = fArray[n12] + taur * f4;
            fArray2[n8] = fArray[n12] + f4;
            f2 = taui * (fArray[n10] + fArray[n10]);
            fArray2[n8 + n7] = f3 - f2;
            fArray2[n8 + n9] = f3 + f2;
            n8 += n2;
            n10 += n11;
            n12 += n11;
        }
        if (n2 == 1) {
            return;
        }
        n8 = 0;
        n10 = n2 << 1;
        for (n6 = 0; n6 < n3; ++n6) {
            int n13 = n8 + (n8 << 1);
            int n14 = n12 = n13 + n10;
            int n15 = n8;
            int n16 = n8 + n7;
            int n17 = n16 + n7;
            for (int i2 = 2; i2 < n2; i2 += 2) {
                n16 += 2;
                n17 += 2;
                f4 = fArray[(n12 += 2) - 1] + fArray[(n14 -= 2) - 1];
                f3 = fArray[(n13 += 2) - 1] + taur * f4;
                fArray2[(n15 += 2) - 1] = fArray[n13 - 1] + f4;
                float f5 = fArray[n12] - fArray[n14];
                float f6 = fArray[n13] + taur * f5;
                fArray2[n15] = fArray[n13] + f5;
                float f7 = taui * (fArray[n12 - 1] - fArray[n14 - 1]);
                f2 = taui * (fArray[n12] + fArray[n14]);
                float f8 = f3 - f2;
                float f9 = f3 + f2;
                float f10 = f6 + f7;
                float f11 = f6 - f7;
                fArray2[n16 - 1] = fArray3[n4 + i2 - 2] * f8 - fArray3[n4 + i2 - 1] * f10;
                fArray2[n16] = fArray3[n4 + i2 - 2] * f10 + fArray3[n4 + i2 - 1] * f8;
                fArray2[n17 - 1] = fArray4[n5 + i2 - 2] * f9 - fArray4[n5 + i2 - 1] * f11;
                fArray2[n17] = fArray4[n5 + i2 - 2] * f11 + fArray4[n5 + i2 - 1] * f9;
            }
            n8 += n2;
        }
    }

    static void dradb4(int n2, int n3, float[] fArray, float[] fArray2, float[] fArray3, int n4, float[] fArray4, int n5, float[] fArray5, int n6) {
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        int n7;
        int n8;
        int n9;
        int n10 = n3 * n2;
        int n11 = 0;
        int n12 = n2 << 2;
        int n13 = 0;
        int n14 = n2 << 1;
        for (n9 = 0; n9 < n3; ++n9) {
            n8 = n13 + n14;
            n7 = n11;
            f7 = fArray[n8 - 1] + fArray[n8 - 1];
            f6 = fArray[n8] + fArray[n8];
            f5 = fArray[n13] - fArray[(n8 += n14) - 1];
            f4 = fArray[n13] + fArray[n8 - 1];
            fArray2[n7] = f4 + f7;
            fArray2[n7 += n10] = f5 - f6;
            fArray2[n7 += n10] = f4 - f7;
            fArray2[n7 += n10] = f5 + f6;
            n11 += n2;
            n13 += n12;
        }
        if (n2 < 2) {
            return;
        }
        if (n2 != 2) {
            n11 = 0;
            for (n9 = 0; n9 < n3; ++n9) {
                n12 = n11 << 2;
                n8 = n13 = n12 + n14;
                n7 = n13 + n14;
                int n15 = n11;
                for (int i2 = 2; i2 < n2; i2 += 2) {
                    n15 += 2;
                    f3 = fArray[n12 += 2] + fArray[n7 -= 2];
                    f2 = fArray[n12] - fArray[n7];
                    float f8 = fArray[n13 += 2] - fArray[n8 -= 2];
                    f6 = fArray[n13] + fArray[n8];
                    f5 = fArray[n12 - 1] - fArray[n7 - 1];
                    f4 = fArray[n12 - 1] + fArray[n7 - 1];
                    float f9 = fArray[n13 - 1] - fArray[n8 - 1];
                    f7 = fArray[n13 - 1] + fArray[n8 - 1];
                    fArray2[n15 - 1] = f4 + f7;
                    float f10 = f4 - f7;
                    fArray2[n15] = f2 + f8;
                    float f11 = f2 - f8;
                    float f12 = f5 - f6;
                    float f13 = f5 + f6;
                    float f14 = f3 + f9;
                    float f15 = f3 - f9;
                    int n16 = n15 + n10;
                    fArray2[n16 - 1] = fArray3[n4 + i2 - 2] * f12 - fArray3[n4 + i2 - 1] * f14;
                    fArray2[n16] = fArray3[n4 + i2 - 2] * f14 + fArray3[n4 + i2 - 1] * f12;
                    fArray2[(n16 += n10) - 1] = fArray4[n5 + i2 - 2] * f10 - fArray4[n5 + i2 - 1] * f11;
                    fArray2[n16] = fArray4[n5 + i2 - 2] * f11 + fArray4[n5 + i2 - 1] * f10;
                    fArray2[(n16 += n10) - 1] = fArray5[n6 + i2 - 2] * f13 - fArray5[n6 + i2 - 1] * f15;
                    fArray2[n16] = fArray5[n6 + i2 - 2] * f15 + fArray5[n6 + i2 - 1] * f13;
                }
                n11 += n2;
            }
            if (n2 % 2 == 1) {
                return;
            }
        }
        n11 = n2;
        n12 = n2 << 2;
        n13 = n2 - 1;
        n8 = n2 + (n2 << 1);
        for (n9 = 0; n9 < n3; ++n9) {
            n7 = n13;
            f3 = fArray[n11] + fArray[n8];
            f2 = fArray[n8] - fArray[n11];
            f5 = fArray[n11 - 1] - fArray[n8 - 1];
            f4 = fArray[n11 - 1] + fArray[n8 - 1];
            fArray2[n7] = f4 + f4;
            fArray2[n7 += n10] = sqrt2 * (f5 - f3);
            fArray2[n7 += n10] = f2 + f2;
            fArray2[n7 += n10] = -sqrt2 * (f5 + f3);
            n13 += n2;
            n11 += n12;
            n8 += n12;
        }
    }

    /*
     * Unable to fully structure code
     */
    static void dradbg(int var0, int var1_1, int var2_2, int var3_3, float[] var4_4, float[] var5_5, float[] var6_6, float[] var7_7, float[] var8_8, float[] var9_9, int var10_10) {
        var12_11 = 0;
        var19_12 = 0;
        var29_13 = 0;
        var38_14 = 0;
        var39_15 = 0.0f;
        var41_16 = 0.0f;
        var44_17 = 0;
        var45_18 = 100;
        block10: while (true) {
            switch (var45_18) {
                case 100: {
                    var29_13 = var1_1 * var0;
                    var19_12 = var2_2 * var0;
                    var40_41 = Drft.tpi / (float)var1_1;
                    var39_15 = (float)Math.cos(var40_41);
                    var41_16 = (float)Math.sin(var40_41);
                    var38_14 = var0 - 1 >>> 1;
                    var44_17 = var1_1;
                    var12_11 = var1_1 + 1 >>> 1;
                    if (var0 < var2_2) {
                        var45_18 = 103;
                    } else {
                        var20_24 = 0;
                        var21_25 = 0;
                        for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                            var22_26 = var20_24;
                            var23_27 = var21_25;
                            for (var13_19 = 0; var13_19 < var0; ++var13_19) {
                                var7_7[var22_26] = var4_4[var23_27];
                                ++var22_26;
                                ++var23_27;
                            }
                            var20_24 += var0;
                            var21_25 += var29_13;
                        }
                        var45_18 = 106;
                    }
                    ** GOTO lbl283
                }
                case 103: {
                    var20_24 = 0;
                    for (var13_19 = 0; var13_19 < var0; ++var13_19) {
                        var21_25 = var20_24;
                        var22_26 = var20_24;
                        for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                            var7_7[var21_25] = var4_4[var22_26];
                            var21_25 += var0;
                            var22_26 += var29_13;
                        }
                        ++var20_24;
                    }
                }
                case 106: {
                    var20_24 = 0;
                    var21_25 = var44_17 * var19_12;
                    var26_30 = var24_28 = var0 << 1;
                    for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                        var22_26 = var20_24 += var19_12;
                        var23_27 = var21_25 -= var19_12;
                        var25_29 = var24_28;
                        for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                            var7_7[var22_26] = var4_4[var25_29 - 1] + var4_4[var25_29 - 1];
                            var7_7[var23_27] = var4_4[var25_29] + var4_4[var25_29];
                            var22_26 += var0;
                            var23_27 += var0;
                            var25_29 += var29_13;
                        }
                        var24_28 += var26_30;
                    }
                    if (var0 == 1) {
                        var45_18 = 116;
                    } else if (var38_14 < var2_2) {
                        var45_18 = 112;
                    } else {
                        var20_24 = 0;
                        var21_25 = var44_17 * var19_12;
                        var26_30 = 0;
                        for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                            var22_26 = var20_24 += var19_12;
                            var23_27 = var21_25 -= var19_12;
                            var27_31 = var26_30 += var0 << 1;
                            for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                                var24_28 = var22_26;
                                var25_29 = var23_27;
                                var28_32 = var27_31;
                                var30_33 = var27_31;
                                for (var13_19 = 2; var13_19 < var0; var13_19 += 2) {
                                    var7_7[(var24_28 += 2) - 1] = var4_4[(var28_32 += 2) - 1] + var4_4[(var30_33 -= 2) - 1];
                                    var7_7[(var25_29 += 2) - 1] = var4_4[var28_32 - 1] - var4_4[var30_33 - 1];
                                    var7_7[var24_28] = var4_4[var28_32] - var4_4[var30_33];
                                    var7_7[var25_29] = var4_4[var28_32] + var4_4[var30_33];
                                }
                                var22_26 += var0;
                                var23_27 += var0;
                                var27_31 += var29_13;
                            }
                        }
                        var45_18 = 116;
                    }
                    ** GOTO lbl283
                }
                case 112: {
                    var20_24 = 0;
                    var21_25 = var44_17 * var19_12;
                    var26_30 = 0;
                    for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                        var22_26 = var20_24 += var19_12;
                        var23_27 = var21_25 -= var19_12;
                        var27_31 = var26_30 += var0 << 1;
                        var28_32 = var26_30;
                        for (var13_19 = 2; var13_19 < var0; var13_19 += 2) {
                            var24_28 = var22_26 += 2;
                            var25_29 = var23_27 += 2;
                            var30_33 = var27_31 += 2;
                            var31_34 = var28_32 -= 2;
                            for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                                var7_7[var24_28 - 1] = var4_4[var30_33 - 1] + var4_4[var31_34 - 1];
                                var7_7[var25_29 - 1] = var4_4[var30_33 - 1] - var4_4[var31_34 - 1];
                                var7_7[var24_28] = var4_4[var30_33] - var4_4[var31_34];
                                var7_7[var25_29] = var4_4[var30_33] + var4_4[var31_34];
                                var24_28 += var0;
                                var25_29 += var0;
                                var30_33 += var29_13;
                                var31_34 += var29_13;
                            }
                        }
                    }
                }
                case 116: {
                    var35_38 = 1.0f;
                    var33_36 = 0.0f;
                    var20_24 = 0;
                    var28_32 = var21_25 = var44_17 * var3_3;
                    var22_26 = (var1_1 - 1) * var3_3;
                    for (var16_22 = 1; var16_22 < var12_11; ++var16_22) {
                        var42_42 = var39_15 * var35_38 - var41_16 * var33_36;
                        var33_36 = var39_15 * var33_36 + var41_16 * var35_38;
                        var35_38 = var42_42;
                        var23_27 = var20_24 += var3_3;
                        var24_28 = var21_25 -= var3_3;
                        var25_29 = 0;
                        var26_30 = var3_3;
                        var27_31 = var22_26;
                        for (var17_23 = 0; var17_23 < var3_3; ++var17_23) {
                            var6_6[var23_27++] = var8_8[var25_29++] + var35_38 * var8_8[var26_30++];
                            var6_6[var24_28++] = var33_36 * var8_8[var27_31++];
                        }
                        var32_35 = var35_38;
                        var37_40 = var33_36;
                        var36_39 = var35_38;
                        var34_37 = var33_36;
                        var25_29 = var3_3;
                        var26_30 = var28_32 - var3_3;
                        for (var14_20 = 2; var14_20 < var12_11; ++var14_20) {
                            var25_29 += var3_3;
                            var26_30 -= var3_3;
                            var43_43 = var32_35 * var36_39 - var37_40 * var34_37;
                            var34_37 = var32_35 * var34_37 + var37_40 * var36_39;
                            var36_39 = var43_43;
                            var23_27 = var20_24;
                            var24_28 = var21_25;
                            var30_33 = var25_29;
                            var31_34 = var26_30;
                            for (var17_23 = 0; var17_23 < var3_3; ++var17_23) {
                                v0 = var23_27++;
                                var6_6[v0] = var6_6[v0] + var36_39 * var8_8[var30_33++];
                                v1 = var24_28++;
                                var6_6[v1] = var6_6[v1] + var34_37 * var8_8[var31_34++];
                            }
                        }
                    }
                    var20_24 = 0;
                    for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                        var21_25 = var20_24 += var3_3;
                        var17_23 = 0;
                        while (var17_23 < var3_3) {
                            v2 = var17_23++;
                            var8_8[v2] = var8_8[v2] + var8_8[var21_25++];
                        }
                    }
                    var20_24 = 0;
                    var21_25 = var44_17 * var19_12;
                    for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                        var22_26 = var20_24 += var19_12;
                        var23_27 = var21_25 -= var19_12;
                        for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                            var7_7[var22_26] = var5_5[var22_26] - var5_5[var23_27];
                            var7_7[var23_27] = var5_5[var22_26] + var5_5[var23_27];
                            var22_26 += var0;
                            var23_27 += var0;
                        }
                    }
                    if (var0 == 1) {
                        var45_18 = 132;
                    } else if (var38_14 < var2_2) {
                        var45_18 = 128;
                    } else {
                        var20_24 = 0;
                        var21_25 = var44_17 * var19_12;
                        for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                            var22_26 = var20_24 += var19_12;
                            var23_27 = var21_25 -= var19_12;
                            for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                                var24_28 = var22_26;
                                var25_29 = var23_27;
                                for (var13_19 = 2; var13_19 < var0; var13_19 += 2) {
                                    var7_7[(var24_28 += 2) - 1] = var5_5[var24_28 - 1] - var5_5[var25_29 += 2];
                                    var7_7[var25_29 - 1] = var5_5[var24_28 - 1] + var5_5[var25_29];
                                    var7_7[var24_28] = var5_5[var24_28] + var5_5[var25_29 - 1];
                                    var7_7[var25_29] = var5_5[var24_28] - var5_5[var25_29 - 1];
                                }
                                var22_26 += var0;
                                var23_27 += var0;
                            }
                        }
                        var45_18 = 132;
                    }
                    ** GOTO lbl283
                }
                case 128: {
                    var20_24 = 0;
                    var21_25 = var44_17 * var19_12;
                    for (var14_20 = 1; var14_20 < var12_11; ++var14_20) {
                        var22_26 = var20_24 += var19_12;
                        var23_27 = var21_25 -= var19_12;
                        for (var13_19 = 2; var13_19 < var0; var13_19 += 2) {
                            var24_28 = var22_26 += 2;
                            var25_29 = var23_27 += 2;
                            for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                                var7_7[var24_28 - 1] = var5_5[var24_28 - 1] - var5_5[var25_29];
                                var7_7[var25_29 - 1] = var5_5[var24_28 - 1] + var5_5[var25_29];
                                var7_7[var24_28] = var5_5[var24_28] + var5_5[var25_29 - 1];
                                var7_7[var25_29] = var5_5[var24_28] - var5_5[var25_29 - 1];
                                var24_28 += var0;
                                var25_29 += var0;
                            }
                        }
                    }
                }
                case 132: {
                    if (var0 == 1) {
                        return;
                    }
                    for (var17_23 = 0; var17_23 < var3_3; ++var17_23) {
                        var6_6[var17_23] = var8_8[var17_23];
                    }
                    var20_24 = 0;
                    for (var14_20 = 1; var14_20 < var1_1; ++var14_20) {
                        var21_25 = var20_24 += var19_12;
                        for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                            var5_5[var21_25] = var7_7[var21_25];
                            var21_25 += var0;
                        }
                    }
                    if (var38_14 <= var2_2) ** GOTO lbl250
                    var45_18 = 139;
                    ** GOTO lbl283
lbl250:
                    // 1 sources

                    var18_44 = -var0 - 1;
                    var20_24 = 0;
                    for (var14_20 = 1; var14_20 < var1_1; ++var14_20) {
                        var11_46 = var18_44 += var0;
                        var21_25 = var20_24 += var19_12;
                        for (var13_19 = 2; var13_19 < var0; var13_19 += 2) {
                            var11_46 += 2;
                            var22_26 = var21_25 += 2;
                            for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                                var5_5[var22_26 - 1] = var9_9[var10_10 + var11_46 - 1] * var7_7[var22_26 - 1] - var9_9[var10_10 + var11_46] * var7_7[var22_26];
                                var5_5[var22_26] = var9_9[var10_10 + var11_46 - 1] * var7_7[var22_26] + var9_9[var10_10 + var11_46] * var7_7[var22_26 - 1];
                                var22_26 += var0;
                            }
                        }
                    }
                    return;
                }
                case 139: {
                    var18_45 = -var0 - 1;
                    var20_24 = 0;
                    for (var14_20 = 1; var14_20 < var1_1; ++var14_20) {
                        var18_45 += var0;
                        var21_25 = var20_24 += var19_12;
                        for (var15_21 = 0; var15_21 < var2_2; ++var15_21) {
                            var11_47 = var18_45;
                            var22_26 = var21_25;
                            for (var13_19 = 2; var13_19 < var0; var13_19 += 2) {
                                var5_5[(var22_26 += 2) - 1] = var9_9[var10_10 + (var11_47 += 2) - 1] * var7_7[var22_26 - 1] - var9_9[var10_10 + var11_47] * var7_7[var22_26];
                                var5_5[var22_26] = var9_9[var10_10 + var11_47 - 1] * var7_7[var22_26] + var9_9[var10_10 + var11_47] * var7_7[var22_26 - 1];
                            }
                            var21_25 += var0;
                        }
                    }
                    break block10;
                }
lbl283:
                // 10 sources

                default: {
                    continue block10;
                }
            }
            break;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    static void drftb1(int n2, float[] fArray, float[] fArray2, float[] fArray3, int n3, int[] nArray) {
        int n4 = 0;
        int n5 = 0;
        int n6 = 0;
        int n7 = 0;
        int n8 = nArray[1];
        int n9 = 0;
        int n10 = 1;
        int n11 = 1;
        int n12 = 0;
        while (true) {
            if (n12 >= n8) {
                if (n9 == 0) {
                    return;
                }
                int n13 = 0;
                while (true) {
                    if (n13 >= n2) {
                        return;
                    }
                    fArray[n13] = fArray2[n13];
                    ++n13;
                }
            }
            int n14 = 100;
            block9: while (true) {
                switch (n14) {
                    case 100: {
                        n5 = nArray[n12 + 2];
                        n4 = n5 * n10;
                        n6 = n2 / n4;
                        n7 = n6 * n10;
                        if (n5 != 4) {
                            n14 = 103;
                            break;
                        }
                        int n15 = n11 + n6;
                        int n16 = n15 + n6;
                        if (n9 != 0) {
                            Drft.dradb4(n6, n10, fArray2, fArray, fArray3, n3 + n11 - 1, fArray3, n3 + n15 - 1, fArray3, n3 + n16 - 1);
                        } else {
                            Drft.dradb4(n6, n10, fArray, fArray2, fArray3, n3 + n11 - 1, fArray3, n3 + n15 - 1, fArray3, n3 + n16 - 1);
                        }
                        n9 = 1 - n9;
                        n14 = 115;
                        break;
                    }
                    case 103: {
                        if (n5 != 2) {
                            n14 = 106;
                            break;
                        }
                        if (n9 != 0) {
                            Drft.dradb2(n6, n10, fArray2, fArray, fArray3, n3 + n11 - 1);
                        } else {
                            Drft.dradb2(n6, n10, fArray, fArray2, fArray3, n3 + n11 - 1);
                        }
                        n9 = 1 - n9;
                        n14 = 115;
                        break;
                    }
                    case 106: {
                        if (n5 != 3) {
                            n14 = 109;
                            break;
                        }
                        int n15 = n11 + n6;
                        if (n9 != 0) {
                            Drft.dradb3(n6, n10, fArray2, fArray, fArray3, n3 + n11 - 1, fArray3, n3 + n15 - 1);
                        } else {
                            Drft.dradb3(n6, n10, fArray, fArray2, fArray3, n3 + n11 - 1, fArray3, n3 + n15 - 1);
                        }
                        n9 = 1 - n9;
                        n14 = 115;
                        break;
                    }
                    case 109: {
                        if (n9 != 0) {
                            Drft.dradbg(n6, n5, n10, n7, fArray2, fArray2, fArray2, fArray, fArray, fArray3, n3 + n11 - 1);
                        } else {
                            Drft.dradbg(n6, n5, n10, n7, fArray, fArray, fArray, fArray2, fArray2, fArray3, n3 + n11 - 1);
                        }
                        if (n6 == 1) {
                            n9 = 1 - n9;
                        }
                    }
                    case 115: {
                        n10 = n4;
                        n11 += (n5 - 1) * n6;
                        break block9;
                    }
                }
            }
            ++n12;
        }
    }
}

