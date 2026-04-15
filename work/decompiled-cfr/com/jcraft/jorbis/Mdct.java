/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

class Mdct {
    int n;
    int log2n;
    float[] trig;
    int[] bitrev;
    float scale;
    float[] _x = new float[1024];
    float[] _w = new float[1024];

    Mdct() {
    }

    void init(int n2) {
        int n3;
        this.bitrev = new int[n2 / 4];
        this.trig = new float[n2 + n2 / 4];
        this.log2n = (int)Math.rint(Math.log(n2) / Math.log(2.0));
        this.n = n2;
        int n4 = 0;
        int n5 = 1;
        int n6 = n4 + n2 / 2;
        int n7 = n6 + 1;
        int n8 = n6 + n2 / 2;
        int n9 = n8 + 1;
        for (n3 = 0; n3 < n2 / 4; ++n3) {
            this.trig[n4 + n3 * 2] = (float)Math.cos(Math.PI / (double)n2 * (double)(4 * n3));
            this.trig[n5 + n3 * 2] = (float)(-Math.sin(Math.PI / (double)n2 * (double)(4 * n3)));
            this.trig[n6 + n3 * 2] = (float)Math.cos(Math.PI / (double)(2 * n2) * (double)(2 * n3 + 1));
            this.trig[n7 + n3 * 2] = (float)Math.sin(Math.PI / (double)(2 * n2) * (double)(2 * n3 + 1));
        }
        for (n3 = 0; n3 < n2 / 8; ++n3) {
            this.trig[n8 + n3 * 2] = (float)Math.cos(Math.PI / (double)n2 * (double)(4 * n3 + 2));
            this.trig[n9 + n3 * 2] = (float)(-Math.sin(Math.PI / (double)n2 * (double)(4 * n3 + 2)));
        }
        n3 = (1 << this.log2n - 1) - 1;
        int n10 = 1 << this.log2n - 2;
        for (int i2 = 0; i2 < n2 / 8; ++i2) {
            int n11 = 0;
            int n12 = 0;
            while (n10 >>> n12 != 0) {
                if ((n10 >>> n12 & i2) != 0) {
                    n11 |= 1 << n12;
                }
                ++n12;
            }
            this.bitrev[i2 * 2] = ~n11 & n3;
            this.bitrev[i2 * 2 + 1] = n11;
        }
        this.scale = 4.0f / (float)n2;
    }

    void clear() {
    }

    void forward(float[] fArray, float[] fArray2) {
    }

    synchronized void backward(float[] fArray, float[] fArray2) {
        int n2;
        if (this._x.length < this.n / 2) {
            this._x = new float[this.n / 2];
        }
        if (this._w.length < this.n / 2) {
            this._w = new float[this.n / 2];
        }
        float[] fArray3 = this._x;
        float[] fArray4 = this._w;
        int n3 = this.n >>> 1;
        int n4 = this.n >>> 2;
        int n5 = this.n >>> 3;
        int n6 = 1;
        int n7 = 0;
        int n8 = n3;
        for (n2 = 0; n2 < n5; ++n2) {
            fArray3[n7++] = -fArray[n6 + 2] * this.trig[(n8 -= 2) + 1] - fArray[n6] * this.trig[n8];
            fArray3[n7++] = fArray[n6] * this.trig[n8 + 1] - fArray[n6 + 2] * this.trig[n8];
            n6 += 4;
        }
        n6 = n3 - 4;
        for (n2 = 0; n2 < n5; ++n2) {
            fArray3[n7++] = fArray[n6] * this.trig[(n8 -= 2) + 1] + fArray[n6 + 2] * this.trig[n8];
            fArray3[n7++] = fArray[n6] * this.trig[n8] - fArray[n6 + 2] * this.trig[n8 + 1];
            n6 -= 4;
        }
        float[] fArray5 = this.mdct_kernel(fArray3, fArray4, this.n, n3, n4, n5);
        n7 = 0;
        n8 = n3;
        n2 = n4;
        int n9 = n2 - 1;
        int n10 = n4 + n3;
        int n11 = n10 - 1;
        for (int i2 = 0; i2 < n4; ++i2) {
            float f2 = fArray5[n7] * this.trig[n8 + 1] - fArray5[n7 + 1] * this.trig[n8];
            float f3 = -(fArray5[n7] * this.trig[n8] + fArray5[n7 + 1] * this.trig[n8 + 1]);
            fArray2[n2] = -f2;
            fArray2[n9] = f2;
            fArray2[n10] = f3;
            fArray2[n11] = f3;
            ++n2;
            --n9;
            ++n10;
            --n11;
            n7 += 2;
            n8 += 2;
        }
    }

    private float[] mdct_kernel(float[] fArray, float[] fArray2, int n2, int n3, int n4, int n5) {
        float f2;
        float f3;
        float f4;
        float f5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10 = n4;
        int n11 = 0;
        int n12 = n4;
        int n13 = n3;
        for (n9 = 0; n9 < n4; ++n9) {
            float f6 = fArray[n10] - fArray[n11];
            fArray2[n12 + n9] = fArray[n10++] + fArray[n11++];
            float f7 = fArray[n10] - fArray[n11];
            fArray2[n9++] = f6 * this.trig[n13 -= 4] + f7 * this.trig[n13 + 1];
            fArray2[n9] = f7 * this.trig[n13] - f6 * this.trig[n13 + 1];
            fArray2[n12 + n9] = fArray[n10++] + fArray[n11++];
        }
        for (n9 = 0; n9 < this.log2n - 3; ++n9) {
            int n14 = n2 >>> n9 + 2;
            int n15 = 1 << n9 + 3;
            n8 = n3 - 2;
            n13 = 0;
            for (n7 = 0; n7 < n14 >>> 2; ++n7) {
                n6 = n8;
                n12 = n6 - (n14 >> 1);
                f5 = this.trig[n13];
                f4 = this.trig[n13 + 1];
                n8 -= 2;
                ++n14;
                for (int i2 = 0; i2 < 2 << n9; ++i2) {
                    f3 = fArray2[n6] - fArray2[n12];
                    fArray[n6] = fArray2[n6] + fArray2[n12];
                    f2 = fArray2[++n6] - fArray2[++n12];
                    fArray[n6] = fArray2[n6] + fArray2[n12];
                    fArray[n12] = f2 * f5 - f3 * f4;
                    fArray[n12 - 1] = f3 * f5 + f2 * f4;
                    n6 -= n14;
                    n12 -= n14;
                }
                --n14;
                n13 += n15;
            }
            float[] fArray3 = fArray2;
            fArray2 = fArray;
            fArray = fArray3;
        }
        n9 = n2;
        int n16 = 0;
        int n17 = 0;
        n8 = n3 - 1;
        for (int i3 = 0; i3 < n5; ++i3) {
            n7 = this.bitrev[n16++];
            n6 = this.bitrev[n16++];
            f5 = fArray2[n7] - fArray2[n6 + 1];
            f2 = fArray2[n7 - 1] + fArray2[n6];
            f4 = fArray2[n7] + fArray2[n6 + 1];
            f3 = fArray2[n7 - 1] - fArray2[n6];
            float f8 = f5 * this.trig[n9];
            float f9 = f2 * this.trig[n9++];
            float f10 = f5 * this.trig[n9];
            float f11 = f2 * this.trig[n9++];
            fArray[n17++] = (f4 + f10 + f9) * 0.5f;
            fArray[n8--] = (-f3 + f11 - f8) * 0.5f;
            fArray[n17++] = (f3 + f11 - f8) * 0.5f;
            fArray[n8--] = (f4 - f10 - f9) * 0.5f;
        }
        return fArray;
    }
}

