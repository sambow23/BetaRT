/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.CodeBook;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Floor0$EchstateFloor0;
import com.jcraft.jorbis.Floor0$InfoFloor0;
import com.jcraft.jorbis.Floor0$LookFloor0;
import com.jcraft.jorbis.FuncFloor;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.Lsp;
import com.jcraft.jorbis.Util;

class Floor0
extends FuncFloor {
    float[] lsp = null;

    Floor0() {
    }

    void pack(Object object, Buffer buffer) {
        Floor0$InfoFloor0 floor0$InfoFloor0 = (Floor0$InfoFloor0)object;
        buffer.write(floor0$InfoFloor0.order, 8);
        buffer.write(floor0$InfoFloor0.rate, 16);
        buffer.write(floor0$InfoFloor0.barkmap, 16);
        buffer.write(floor0$InfoFloor0.ampbits, 6);
        buffer.write(floor0$InfoFloor0.ampdB, 8);
        buffer.write(floor0$InfoFloor0.numbooks - 1, 4);
        for (int i2 = 0; i2 < floor0$InfoFloor0.numbooks; ++i2) {
            buffer.write(floor0$InfoFloor0.books[i2], 8);
        }
    }

    Object unpack(Info info, Buffer buffer) {
        Floor0$InfoFloor0 floor0$InfoFloor0 = new Floor0$InfoFloor0(this);
        floor0$InfoFloor0.order = buffer.read(8);
        floor0$InfoFloor0.rate = buffer.read(16);
        floor0$InfoFloor0.barkmap = buffer.read(16);
        floor0$InfoFloor0.ampbits = buffer.read(6);
        floor0$InfoFloor0.ampdB = buffer.read(8);
        floor0$InfoFloor0.numbooks = buffer.read(4) + 1;
        if (floor0$InfoFloor0.order < 1 || floor0$InfoFloor0.rate < 1 || floor0$InfoFloor0.barkmap < 1 || floor0$InfoFloor0.numbooks < 1) {
            return null;
        }
        for (int i2 = 0; i2 < floor0$InfoFloor0.numbooks; ++i2) {
            floor0$InfoFloor0.books[i2] = buffer.read(8);
            if (floor0$InfoFloor0.books[i2] >= 0 && floor0$InfoFloor0.books[i2] < info.books) continue;
            return null;
        }
        return floor0$InfoFloor0;
    }

    Object look(DspState dspState, InfoMode infoMode, Object object) {
        Info info = dspState.vi;
        Floor0$InfoFloor0 floor0$InfoFloor0 = (Floor0$InfoFloor0)object;
        Floor0$LookFloor0 floor0$LookFloor0 = new Floor0$LookFloor0(this);
        floor0$LookFloor0.m = floor0$InfoFloor0.order;
        floor0$LookFloor0.n = info.blocksizes[infoMode.blockflag] / 2;
        floor0$LookFloor0.ln = floor0$InfoFloor0.barkmap;
        floor0$LookFloor0.vi = floor0$InfoFloor0;
        floor0$LookFloor0.lpclook.init(floor0$LookFloor0.ln, floor0$LookFloor0.m);
        float f2 = (float)floor0$LookFloor0.ln / Floor0.toBARK((float)((double)floor0$InfoFloor0.rate / 2.0));
        floor0$LookFloor0.linearmap = new int[floor0$LookFloor0.n];
        for (int i2 = 0; i2 < floor0$LookFloor0.n; ++i2) {
            int n2 = in.d(Floor0.toBARK((float)((double)floor0$InfoFloor0.rate / 2.0 / (double)floor0$LookFloor0.n * (double)i2)) * f2);
            if (n2 >= floor0$LookFloor0.ln) {
                n2 = floor0$LookFloor0.ln;
            }
            floor0$LookFloor0.linearmap[i2] = n2;
        }
        return floor0$LookFloor0;
    }

    static float toBARK(float f2) {
        return (float)(13.1 * Math.atan(7.4E-4 * (double)f2) + 2.24 * Math.atan((double)(f2 * f2) * 1.85E-8) + 1.0E-4 * (double)f2);
    }

    Object state(Object object) {
        Floor0$EchstateFloor0 floor0$EchstateFloor0 = new Floor0$EchstateFloor0(this);
        Floor0$InfoFloor0 floor0$InfoFloor0 = (Floor0$InfoFloor0)object;
        floor0$EchstateFloor0.codewords = new int[floor0$InfoFloor0.order];
        floor0$EchstateFloor0.curve = new float[floor0$InfoFloor0.barkmap];
        floor0$EchstateFloor0.frameno = -1L;
        return floor0$EchstateFloor0;
    }

    void free_info(Object object) {
    }

    void free_look(Object object) {
    }

    void free_state(Object object) {
    }

    int forward(Block block, Object object, float[] fArray, float[] fArray2, Object object2) {
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int inverse(Block block, Object object, float[] fArray) {
        Floor0$LookFloor0 floor0$LookFloor0 = (Floor0$LookFloor0)object;
        Floor0$InfoFloor0 floor0$InfoFloor0 = floor0$LookFloor0.vi;
        int n2 = block.opb.read(floor0$InfoFloor0.ampbits);
        if (n2 > 0) {
            int n3 = (1 << floor0$InfoFloor0.ampbits) - 1;
            float f2 = (float)n2 / (float)n3 * (float)floor0$InfoFloor0.ampdB;
            int n4 = block.opb.read(Util.ilog(floor0$InfoFloor0.numbooks));
            if (n4 != -1 && n4 < floor0$InfoFloor0.numbooks) {
                Floor0 floor0 = this;
                synchronized (floor0) {
                    int n5;
                    if (this.lsp == null || this.lsp.length < floor0$LookFloor0.m) {
                        this.lsp = new float[floor0$LookFloor0.m];
                    } else {
                        for (int i2 = 0; i2 < floor0$LookFloor0.m; ++i2) {
                            this.lsp[i2] = 0.0f;
                        }
                    }
                    CodeBook codeBook = block.vd.fullbooks[floor0$InfoFloor0.books[n4]];
                    float f3 = 0.0f;
                    for (n5 = 0; n5 < floor0$LookFloor0.m; ++n5) {
                        fArray[n5] = 0.0f;
                    }
                    for (n5 = 0; n5 < floor0$LookFloor0.m; n5 += codeBook.dim) {
                        if (codeBook.decodevs(this.lsp, n5, block.opb, 1, -1) != -1) continue;
                        for (int i3 = 0; i3 < floor0$LookFloor0.n; ++i3) {
                            fArray[i3] = 0.0f;
                        }
                        return 0;
                    }
                    n5 = 0;
                    while (n5 < floor0$LookFloor0.m) {
                        for (int i4 = 0; i4 < codeBook.dim; ++i4) {
                            int n6 = n5++;
                            this.lsp[n6] = this.lsp[n6] + f3;
                        }
                        f3 = this.lsp[n5 - 1];
                    }
                    Lsp.lsp_to_curve(fArray, floor0$LookFloor0.linearmap, floor0$LookFloor0.n, floor0$LookFloor0.ln, this.lsp, floor0$LookFloor0.m, f2, floor0$InfoFloor0.ampdB);
                    return 1;
                }
            }
        }
        return 0;
    }

    Object inverse1(Block block, Object object, Object object2) {
        int n2;
        Floor0$LookFloor0 floor0$LookFloor0 = (Floor0$LookFloor0)object;
        Floor0$InfoFloor0 floor0$InfoFloor0 = floor0$LookFloor0.vi;
        float[] fArray = null;
        if (object2 instanceof float[]) {
            fArray = (float[])object2;
        }
        if ((n2 = block.opb.read(floor0$InfoFloor0.ampbits)) > 0) {
            int n3 = (1 << floor0$InfoFloor0.ampbits) - 1;
            float f2 = (float)n2 / (float)n3 * (float)floor0$InfoFloor0.ampdB;
            int n4 = block.opb.read(Util.ilog(floor0$InfoFloor0.numbooks));
            if (n4 != -1 && n4 < floor0$InfoFloor0.numbooks) {
                int n5;
                CodeBook codeBook = block.vd.fullbooks[floor0$InfoFloor0.books[n4]];
                float f3 = 0.0f;
                if (fArray == null || fArray.length < floor0$LookFloor0.m + 1) {
                    fArray = new float[floor0$LookFloor0.m + 1];
                } else {
                    for (n5 = 0; n5 < fArray.length; ++n5) {
                        fArray[n5] = 0.0f;
                    }
                }
                for (n5 = 0; n5 < floor0$LookFloor0.m; n5 += codeBook.dim) {
                    if (codeBook.decodev_set(fArray, n5, block.opb, codeBook.dim) != -1) continue;
                    return null;
                }
                n5 = 0;
                while (n5 < floor0$LookFloor0.m) {
                    for (int i2 = 0; i2 < codeBook.dim; ++i2) {
                        int n6 = n5++;
                        fArray[n6] = fArray[n6] + f3;
                    }
                    f3 = fArray[n5 - 1];
                }
                fArray[floor0$LookFloor0.m] = f2;
                return fArray;
            }
        }
        return null;
    }

    int inverse2(Block block, Object object, Object object2, float[] fArray) {
        Floor0$LookFloor0 floor0$LookFloor0 = (Floor0$LookFloor0)object;
        Floor0$InfoFloor0 floor0$InfoFloor0 = floor0$LookFloor0.vi;
        if (object2 != null) {
            float[] fArray2 = (float[])object2;
            float f2 = fArray2[floor0$LookFloor0.m];
            Lsp.lsp_to_curve(fArray, floor0$LookFloor0.linearmap, floor0$LookFloor0.n, floor0$LookFloor0.ln, fArray2, floor0$LookFloor0.m, f2, floor0$InfoFloor0.ampdB);
            return 1;
        }
        for (int i2 = 0; i2 < floor0$LookFloor0.n; ++i2) {
            fArray[i2] = 0.0f;
        }
        return 0;
    }

    static float fromdB(float f2) {
        return (float)Math.exp((double)f2 * 0.11512925);
    }

    static void lsp_to_lpc(float[] fArray, float[] fArray2, int n2) {
        int n3;
        int n4;
        int n5 = n2 / 2;
        float[] fArray3 = new float[n5];
        float[] fArray4 = new float[n5];
        float[] fArray5 = new float[n5 + 1];
        float[] fArray6 = new float[n5 + 1];
        float[] fArray7 = new float[n5];
        float[] fArray8 = new float[n5];
        for (n4 = 0; n4 < n5; ++n4) {
            fArray3[n4] = (float)(-2.0 * Math.cos(fArray[n4 * 2]));
            fArray4[n4] = (float)(-2.0 * Math.cos(fArray[n4 * 2 + 1]));
        }
        for (n3 = 0; n3 < n5; ++n3) {
            fArray5[n3] = 0.0f;
            fArray6[n3] = 1.0f;
            fArray7[n3] = 0.0f;
            fArray8[n3] = 1.0f;
        }
        fArray6[n3] = 1.0f;
        fArray5[n3] = 1.0f;
        for (n4 = 1; n4 < n2 + 1; ++n4) {
            float f2 = 0.0f;
            float f3 = 0.0f;
            for (n3 = 0; n3 < n5; ++n3) {
                float f4 = fArray3[n3] * fArray6[n3] + fArray5[n3];
                fArray5[n3] = fArray6[n3];
                fArray6[n3] = f3;
                f3 += f4;
                f4 = fArray4[n3] * fArray8[n3] + fArray7[n3];
                fArray7[n3] = fArray8[n3];
                fArray8[n3] = f2;
                f2 += f4;
            }
            fArray2[n4 - 1] = (f3 + fArray6[n3] + f2 - fArray5[n3]) / 2.0f;
            fArray6[n3] = f3;
            fArray5[n3] = f2;
        }
    }

    static void lpc_to_curve(float[] fArray, float[] fArray2, float f2, Floor0$LookFloor0 floor0$LookFloor0, String string, int n2) {
        float[] fArray3 = new float[Math.max(floor0$LookFloor0.ln * 2, floor0$LookFloor0.m * 2 + 2)];
        if (f2 == 0.0f) {
            for (int i2 = 0; i2 < floor0$LookFloor0.n; ++i2) {
                fArray[i2] = 0.0f;
            }
            return;
        }
        floor0$LookFloor0.lpclook.lpc_to_curve(fArray3, fArray2, f2);
        for (int i3 = 0; i3 < floor0$LookFloor0.n; ++i3) {
            fArray[i3] = fArray3[floor0$LookFloor0.linearmap[i3]];
        }
    }
}

