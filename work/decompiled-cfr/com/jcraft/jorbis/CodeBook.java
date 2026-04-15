/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.CodeBook$DecodeAux;
import com.jcraft.jorbis.StaticCodeBook;
import com.jcraft.jorbis.Util;

class CodeBook {
    int dim;
    int entries;
    StaticCodeBook c = new StaticCodeBook();
    float[] valuelist;
    int[] codelist;
    CodeBook$DecodeAux decode_tree;
    private int[] t = new int[15];

    CodeBook() {
    }

    int encode(int n2, Buffer buffer) {
        buffer.write(this.codelist[n2], this.c.lengthlist[n2]);
        return this.c.lengthlist[n2];
    }

    int errorv(float[] fArray) {
        int n2 = this.best(fArray, 1);
        for (int i2 = 0; i2 < this.dim; ++i2) {
            fArray[i2] = this.valuelist[n2 * this.dim + i2];
        }
        return n2;
    }

    int encodev(int n2, float[] fArray, Buffer buffer) {
        for (int i2 = 0; i2 < this.dim; ++i2) {
            fArray[i2] = this.valuelist[n2 * this.dim + i2];
        }
        return this.encode(n2, buffer);
    }

    int encodevs(float[] fArray, Buffer buffer, int n2, int n3) {
        int n4 = this.besterror(fArray, n2, n3);
        return this.encode(n4, buffer);
    }

    synchronized int decodevs_add(float[] fArray, int n2, Buffer buffer, int n3) {
        int n4;
        int n5 = n3 / this.dim;
        if (this.t.length < n5) {
            this.t = new int[n5];
        }
        for (n4 = 0; n4 < n5; ++n4) {
            int n6 = this.decode(buffer);
            if (n6 == -1) {
                return -1;
            }
            this.t[n4] = n6 * this.dim;
        }
        n4 = 0;
        int n7 = 0;
        while (n4 < this.dim) {
            for (int i2 = 0; i2 < n5; ++i2) {
                int n8 = n2 + n7 + i2;
                fArray[n8] = fArray[n8] + this.valuelist[this.t[i2] + n4];
            }
            ++n4;
            n7 += n5;
        }
        return 0;
    }

    int decodev_add(float[] fArray, int n2, Buffer buffer, int n3) {
        if (this.dim > 8) {
            int n4 = 0;
            while (n4 < n3) {
                int n5 = this.decode(buffer);
                if (n5 == -1) {
                    return -1;
                }
                int n6 = n5 * this.dim;
                int n7 = 0;
                while (n7 < this.dim) {
                    int n8 = n2 + n4++;
                    fArray[n8] = fArray[n8] + this.valuelist[n6 + n7++];
                }
            }
        } else {
            int n9 = 0;
            while (n9 < n3) {
                int n10 = this.decode(buffer);
                if (n10 == -1) {
                    return -1;
                }
                int n11 = n10 * this.dim;
                int n12 = 0;
                switch (this.dim) {
                    case 8: {
                        int n13 = n2 + n9++;
                        fArray[n13] = fArray[n13] + this.valuelist[n11 + n12++];
                    }
                    case 7: {
                        int n14 = n2 + n9++;
                        fArray[n14] = fArray[n14] + this.valuelist[n11 + n12++];
                    }
                    case 6: {
                        int n15 = n2 + n9++;
                        fArray[n15] = fArray[n15] + this.valuelist[n11 + n12++];
                    }
                    case 5: {
                        int n16 = n2 + n9++;
                        fArray[n16] = fArray[n16] + this.valuelist[n11 + n12++];
                    }
                    case 4: {
                        int n17 = n2 + n9++;
                        fArray[n17] = fArray[n17] + this.valuelist[n11 + n12++];
                    }
                    case 3: {
                        int n18 = n2 + n9++;
                        fArray[n18] = fArray[n18] + this.valuelist[n11 + n12++];
                    }
                    case 2: {
                        int n19 = n2 + n9++;
                        fArray[n19] = fArray[n19] + this.valuelist[n11 + n12++];
                    }
                    case 1: {
                        int n20 = n2 + n9++;
                        fArray[n20] = fArray[n20] + this.valuelist[n11 + n12++];
                    }
                }
            }
        }
        return 0;
    }

    int decodev_set(float[] fArray, int n2, Buffer buffer, int n3) {
        int n4 = 0;
        while (n4 < n3) {
            int n5 = this.decode(buffer);
            if (n5 == -1) {
                return -1;
            }
            int n6 = n5 * this.dim;
            int n7 = 0;
            while (n7 < this.dim) {
                fArray[n2 + n4++] = this.valuelist[n6 + n7++];
            }
        }
        return 0;
    }

    int decodevv_add(float[][] fArray, int n2, int n3, Buffer buffer, int n4) {
        int n5 = 0;
        int n6 = n2 / n3;
        while (n6 < (n2 + n4) / n3) {
            int n7 = this.decode(buffer);
            if (n7 == -1) {
                return -1;
            }
            int n8 = n7 * this.dim;
            for (int i2 = 0; i2 < this.dim; ++i2) {
                float[] fArray2 = fArray[n5++];
                int n9 = n6++;
                fArray2[n9] = fArray2[n9] + this.valuelist[n8 + i2];
                if (n5 != n3) continue;
                n5 = 0;
            }
        }
        return 0;
    }

    int decode(Buffer buffer) {
        int n2 = 0;
        CodeBook$DecodeAux codeBook$DecodeAux = this.decode_tree;
        int n3 = buffer.look(codeBook$DecodeAux.tabn);
        if (n3 >= 0) {
            n2 = codeBook$DecodeAux.tab[n3];
            buffer.adv(codeBook$DecodeAux.tabl[n3]);
            if (n2 <= 0) {
                return -n2;
            }
        }
        do {
            switch (buffer.read1()) {
                case 0: {
                    n2 = codeBook$DecodeAux.ptr0[n2];
                    break;
                }
                case 1: {
                    n2 = codeBook$DecodeAux.ptr1[n2];
                    break;
                }
                default: {
                    return -1;
                }
            }
        } while (n2 > 0);
        return -n2;
    }

    int decodevs(float[] fArray, int n2, Buffer buffer, int n3, int n4) {
        int n5 = this.decode(buffer);
        if (n5 == -1) {
            return -1;
        }
        switch (n4) {
            case -1: {
                int n6 = 0;
                int n7 = 0;
                while (n6 < this.dim) {
                    fArray[n2 + n7] = this.valuelist[n5 * this.dim + n6];
                    ++n6;
                    n7 += n3;
                }
                break;
            }
            case 0: {
                int n8 = 0;
                int n9 = 0;
                while (n8 < this.dim) {
                    int n10 = n2 + n9;
                    fArray[n10] = fArray[n10] + this.valuelist[n5 * this.dim + n8];
                    ++n8;
                    n9 += n3;
                }
                break;
            }
            case 1: {
                int n11 = 0;
                int n12 = 0;
                while (n11 < this.dim) {
                    int n13 = n2 + n12;
                    fArray[n13] = fArray[n13] * this.valuelist[n5 * this.dim + n11];
                    ++n11;
                    n12 += n3;
                }
                break;
            }
        }
        return n5;
    }

    int best(float[] fArray, int n2) {
        int n3 = -1;
        float f2 = 0.0f;
        int n4 = 0;
        for (int i2 = 0; i2 < this.entries; ++i2) {
            if (this.c.lengthlist[i2] > 0) {
                float f3 = CodeBook.dist(this.dim, this.valuelist, n4, fArray, n2);
                if (n3 == -1 || f3 < f2) {
                    f2 = f3;
                    n3 = i2;
                }
            }
            n4 += this.dim;
        }
        return n3;
    }

    int besterror(float[] fArray, int n2, int n3) {
        int n4 = this.best(fArray, n2);
        switch (n3) {
            case 0: {
                int n5 = 0;
                int n6 = 0;
                while (n5 < this.dim) {
                    int n7 = n6;
                    fArray[n7] = fArray[n7] - this.valuelist[n4 * this.dim + n5];
                    ++n5;
                    n6 += n2;
                }
                break;
            }
            case 1: {
                int n8 = 0;
                int n9 = 0;
                while (n8 < this.dim) {
                    float f2 = this.valuelist[n4 * this.dim + n8];
                    if (f2 == 0.0f) {
                        fArray[n9] = 0.0f;
                    } else {
                        int n10 = n9;
                        fArray[n10] = fArray[n10] / f2;
                    }
                    ++n8;
                    n9 += n2;
                }
                break;
            }
        }
        return n4;
    }

    void clear() {
    }

    private static float dist(int n2, float[] fArray, int n3, float[] fArray2, int n4) {
        float f2 = 0.0f;
        for (int i2 = 0; i2 < n2; ++i2) {
            float f3 = fArray[n3 + i2] - fArray2[i2 * n4];
            f2 += f3 * f3;
        }
        return f2;
    }

    int init_decode(StaticCodeBook staticCodeBook) {
        this.c = staticCodeBook;
        this.entries = staticCodeBook.entries;
        this.dim = staticCodeBook.dim;
        this.valuelist = staticCodeBook.unquantize();
        this.decode_tree = this.make_decode_tree();
        if (this.decode_tree == null) {
            this.clear();
            return -1;
        }
        return 0;
    }

    static int[] make_words(int[] nArray, int n2) {
        int n3;
        int n4;
        int n5;
        int[] nArray2 = new int[33];
        int[] nArray3 = new int[n2];
        for (n5 = 0; n5 < n2; ++n5) {
            n4 = nArray[n5];
            if (n4 <= 0) continue;
            n3 = nArray2[n4];
            if (n4 < 32 && n3 >>> n4 != 0) {
                return null;
            }
            nArray3[n5] = n3;
            int n6 = n4;
            while (n6 > 0) {
                if ((nArray2[n6] & 1) != 0) {
                    if (n6 == 1) {
                        nArray2[1] = nArray2[1] + 1;
                        break;
                    }
                    nArray2[n6] = nArray2[n6 - 1] << 1;
                    break;
                }
                int n7 = n6--;
                nArray2[n7] = nArray2[n7] + 1;
            }
            for (n6 = n4 + 1; n6 < 33 && nArray2[n6] >>> 1 == n3; ++n6) {
                n3 = nArray2[n6];
                nArray2[n6] = nArray2[n6 - 1] << 1;
            }
        }
        for (n5 = 0; n5 < n2; ++n5) {
            n4 = 0;
            for (n3 = 0; n3 < nArray[n5]; ++n3) {
                n4 <<= 1;
                n4 |= nArray3[n5] >>> n3 & 1;
            }
            nArray3[n5] = n4;
        }
        return nArray3;
    }

    CodeBook$DecodeAux make_decode_tree() {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6 = 0;
        CodeBook$DecodeAux codeBook$DecodeAux = new CodeBook$DecodeAux(this);
        codeBook$DecodeAux.ptr0 = new int[this.entries * 2];
        int[] nArray = codeBook$DecodeAux.ptr0;
        codeBook$DecodeAux.ptr1 = new int[this.entries * 2];
        int[] nArray2 = codeBook$DecodeAux.ptr1;
        int[] nArray3 = CodeBook.make_words(this.c.lengthlist, this.c.entries);
        if (nArray3 == null) {
            return null;
        }
        codeBook$DecodeAux.aux = this.entries * 2;
        for (n5 = 0; n5 < this.entries; ++n5) {
            if (this.c.lengthlist[n5] <= 0) continue;
            n4 = 0;
            for (n3 = 0; n3 < this.c.lengthlist[n5] - 1; ++n3) {
                n2 = nArray3[n5] >>> n3 & 1;
                if (n2 == 0) {
                    if (nArray[n4] == 0) {
                        nArray[n4] = ++n6;
                    }
                    n4 = nArray[n4];
                    continue;
                }
                if (nArray2[n4] == 0) {
                    nArray2[n4] = ++n6;
                }
                n4 = nArray2[n4];
            }
            if ((nArray3[n5] >>> n3 & 1) == 0) {
                nArray[n4] = -n5;
                continue;
            }
            nArray2[n4] = -n5;
        }
        codeBook$DecodeAux.tabn = Util.ilog(this.entries) - 4;
        if (codeBook$DecodeAux.tabn < 5) {
            codeBook$DecodeAux.tabn = 5;
        }
        n5 = 1 << codeBook$DecodeAux.tabn;
        codeBook$DecodeAux.tab = new int[n5];
        codeBook$DecodeAux.tabl = new int[n5];
        for (n4 = 0; n4 < n5; ++n4) {
            n3 = 0;
            n2 = 0;
            for (n2 = 0; n2 < codeBook$DecodeAux.tabn && (n3 > 0 || n2 == 0); ++n2) {
                n3 = (n4 & 1 << n2) != 0 ? nArray2[n3] : nArray[n3];
            }
            codeBook$DecodeAux.tab[n4] = n3;
            codeBook$DecodeAux.tabl[n4] = n2;
        }
        return codeBook$DecodeAux;
    }
}

