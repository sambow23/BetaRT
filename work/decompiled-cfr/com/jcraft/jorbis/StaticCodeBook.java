/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.Util;

class StaticCodeBook {
    int dim;
    int entries;
    int[] lengthlist;
    int maptype;
    int q_min;
    int q_delta;
    int q_quant;
    int q_sequencep;
    int[] quantlist;
    static final int VQ_FEXP = 10;
    static final int VQ_FMAN = 21;
    static final int VQ_FEXP_BIAS = 768;

    StaticCodeBook() {
    }

    int pack(Buffer buffer) {
        int n2;
        int n3;
        boolean bl2 = false;
        buffer.write(5653314, 24);
        buffer.write(this.dim, 16);
        buffer.write(this.entries, 24);
        for (n3 = 1; n3 < this.entries && this.lengthlist[n3] >= this.lengthlist[n3 - 1]; ++n3) {
        }
        if (n3 == this.entries) {
            bl2 = true;
        }
        if (bl2) {
            n2 = 0;
            buffer.write(1, 1);
            buffer.write(this.lengthlist[0] - 1, 5);
            for (n3 = 1; n3 < this.entries; ++n3) {
                int n4 = this.lengthlist[n3];
                int n5 = this.lengthlist[n3 - 1];
                if (n4 <= n5) continue;
                for (int i2 = n5; i2 < n4; ++i2) {
                    buffer.write(n3 - n2, Util.ilog(this.entries - n2));
                    n2 = n3;
                }
            }
            buffer.write(n3 - n2, Util.ilog(this.entries - n2));
        } else {
            buffer.write(0, 1);
            for (n3 = 0; n3 < this.entries && this.lengthlist[n3] != 0; ++n3) {
            }
            if (n3 == this.entries) {
                buffer.write(0, 1);
                for (n3 = 0; n3 < this.entries; ++n3) {
                    buffer.write(this.lengthlist[n3] - 1, 5);
                }
            } else {
                buffer.write(1, 1);
                for (n3 = 0; n3 < this.entries; ++n3) {
                    if (this.lengthlist[n3] == 0) {
                        buffer.write(0, 1);
                        continue;
                    }
                    buffer.write(1, 1);
                    buffer.write(this.lengthlist[n3] - 1, 5);
                }
            }
        }
        buffer.write(this.maptype, 4);
        switch (this.maptype) {
            case 0: {
                break;
            }
            case 1: 
            case 2: {
                if (this.quantlist == null) {
                    return -1;
                }
                buffer.write(this.q_min, 32);
                buffer.write(this.q_delta, 32);
                buffer.write(this.q_quant - 1, 4);
                buffer.write(this.q_sequencep, 1);
                n2 = 0;
                switch (this.maptype) {
                    case 1: {
                        n2 = this.maptype1_quantvals();
                        break;
                    }
                    case 2: {
                        n2 = this.entries * this.dim;
                    }
                }
                for (n3 = 0; n3 < n2; ++n3) {
                    buffer.write(Math.abs(this.quantlist[n3]), this.q_quant);
                }
                break;
            }
            default: {
                return -1;
            }
        }
        return 0;
    }

    int unpack(Buffer buffer) {
        int n2;
        int n3;
        if (buffer.read(24) != 5653314) {
            this.clear();
            return -1;
        }
        this.dim = buffer.read(16);
        this.entries = buffer.read(24);
        if (this.entries == -1) {
            this.clear();
            return -1;
        }
        switch (buffer.read(1)) {
            case 0: {
                this.lengthlist = new int[this.entries];
                if (buffer.read(1) != 0) {
                    for (n3 = 0; n3 < this.entries; ++n3) {
                        if (buffer.read(1) != 0) {
                            n2 = buffer.read(5);
                            if (n2 == -1) {
                                this.clear();
                                return -1;
                            }
                            this.lengthlist[n3] = n2 + 1;
                            continue;
                        }
                        this.lengthlist[n3] = 0;
                    }
                } else {
                    for (n3 = 0; n3 < this.entries; ++n3) {
                        n2 = buffer.read(5);
                        if (n2 == -1) {
                            this.clear();
                            return -1;
                        }
                        this.lengthlist[n3] = n2 + 1;
                    }
                }
                break;
            }
            case 1: {
                n2 = buffer.read(5) + 1;
                this.lengthlist = new int[this.entries];
                n3 = 0;
                while (n3 < this.entries) {
                    int n4 = buffer.read(Util.ilog(this.entries - n3));
                    if (n4 == -1) {
                        this.clear();
                        return -1;
                    }
                    int n5 = 0;
                    while (n5 < n4) {
                        this.lengthlist[n3] = n2;
                        ++n5;
                        ++n3;
                    }
                    ++n2;
                }
                break;
            }
            default: {
                return -1;
            }
        }
        this.maptype = buffer.read(4);
        switch (this.maptype) {
            case 0: {
                break;
            }
            case 1: 
            case 2: {
                this.q_min = buffer.read(32);
                this.q_delta = buffer.read(32);
                this.q_quant = buffer.read(4) + 1;
                this.q_sequencep = buffer.read(1);
                n2 = 0;
                switch (this.maptype) {
                    case 1: {
                        n2 = this.maptype1_quantvals();
                        break;
                    }
                    case 2: {
                        n2 = this.entries * this.dim;
                    }
                }
                this.quantlist = new int[n2];
                for (n3 = 0; n3 < n2; ++n3) {
                    this.quantlist[n3] = buffer.read(this.q_quant);
                }
                if (this.quantlist[n2 - 1] != -1) break;
                this.clear();
                return -1;
            }
            default: {
                this.clear();
                return -1;
            }
        }
        return 0;
    }

    private int maptype1_quantvals() {
        int n2 = in.b(Math.pow(this.entries, 1.0 / (double)this.dim));
        while (true) {
            int n3 = 1;
            int n4 = 1;
            for (int i2 = 0; i2 < this.dim; ++i2) {
                n3 *= n2;
                n4 *= n2 + 1;
            }
            if (n3 <= this.entries && n4 > this.entries) {
                return n2;
            }
            if (n3 > this.entries) {
                --n2;
                continue;
            }
            ++n2;
        }
    }

    void clear() {
    }

    float[] unquantize() {
        if (this.maptype == 1 || this.maptype == 2) {
            float f2 = StaticCodeBook.float32_unpack(this.q_min);
            float f3 = StaticCodeBook.float32_unpack(this.q_delta);
            float[] fArray = new float[this.entries * this.dim];
            switch (this.maptype) {
                case 1: {
                    int n2 = this.maptype1_quantvals();
                    for (int i2 = 0; i2 < this.entries; ++i2) {
                        float f4 = 0.0f;
                        int n3 = 1;
                        for (int i3 = 0; i3 < this.dim; ++i3) {
                            int n4 = i2 / n3 % n2;
                            float f5 = this.quantlist[n4];
                            f5 = Math.abs(f5) * f3 + f2 + f4;
                            if (this.q_sequencep != 0) {
                                f4 = f5;
                            }
                            fArray[i2 * this.dim + i3] = f5;
                            n3 *= n2;
                        }
                    }
                    break;
                }
                case 2: {
                    for (int i4 = 0; i4 < this.entries; ++i4) {
                        float f6 = 0.0f;
                        for (int i5 = 0; i5 < this.dim; ++i5) {
                            float f7 = this.quantlist[i4 * this.dim + i5];
                            f7 = Math.abs(f7) * f3 + f2 + f6;
                            if (this.q_sequencep != 0) {
                                f6 = f7;
                            }
                            fArray[i4 * this.dim + i5] = f7;
                        }
                    }
                    break;
                }
            }
            return fArray;
        }
        return null;
    }

    static long float32_pack(float f2) {
        int n2 = 0;
        if (f2 < 0.0f) {
            n2 = Integer.MIN_VALUE;
            f2 = -f2;
        }
        int n3 = in.b(Math.log(f2) / Math.log(2.0));
        int n4 = (int)Math.rint(Math.pow(f2, 20 - n3));
        n3 = n3 + 768 << 21;
        return n2 | n3 | n4;
    }

    static float float32_unpack(int n2) {
        float f2 = n2 & 0x1FFFFF;
        float f3 = (n2 & 0x7FE00000) >>> 21;
        if ((n2 & Integer.MIN_VALUE) != 0) {
            f2 = -f2;
        }
        return StaticCodeBook.ldexp(f2, (int)f3 - 20 - 768);
    }

    static float ldexp(float f2, int n2) {
        return (float)((double)f2 * Math.pow(2.0, n2));
    }
}

