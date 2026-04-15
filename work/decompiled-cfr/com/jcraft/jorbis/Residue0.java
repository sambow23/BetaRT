/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.CodeBook;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.FuncResidue;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.Residue0$InfoResidue0;
import com.jcraft.jorbis.Residue0$LookResidue0;
import com.jcraft.jorbis.Util;

class Residue0
extends FuncResidue {
    private static int[][][] _01inverse_partword = new int[2][][];
    static int[][] _2inverse_partword = null;

    Residue0() {
    }

    void pack(Object object, Buffer buffer) {
        int n2;
        Residue0$InfoResidue0 residue0$InfoResidue0 = (Residue0$InfoResidue0)object;
        int n3 = 0;
        buffer.write(residue0$InfoResidue0.begin, 24);
        buffer.write(residue0$InfoResidue0.end, 24);
        buffer.write(residue0$InfoResidue0.grouping - 1, 24);
        buffer.write(residue0$InfoResidue0.partitions - 1, 6);
        buffer.write(residue0$InfoResidue0.groupbook, 8);
        for (n2 = 0; n2 < residue0$InfoResidue0.partitions; ++n2) {
            int n4 = residue0$InfoResidue0.secondstages[n2];
            if (Util.ilog(n4) > 3) {
                buffer.write(n4, 3);
                buffer.write(1, 1);
                buffer.write(n4 >>> 3, 5);
            } else {
                buffer.write(n4, 4);
            }
            n3 += Util.icount(n4);
        }
        for (n2 = 0; n2 < n3; ++n2) {
            buffer.write(residue0$InfoResidue0.booklist[n2], 8);
        }
    }

    Object unpack(Info info, Buffer buffer) {
        int n2;
        int n3 = 0;
        Residue0$InfoResidue0 residue0$InfoResidue0 = new Residue0$InfoResidue0(this);
        residue0$InfoResidue0.begin = buffer.read(24);
        residue0$InfoResidue0.end = buffer.read(24);
        residue0$InfoResidue0.grouping = buffer.read(24) + 1;
        residue0$InfoResidue0.partitions = buffer.read(6) + 1;
        residue0$InfoResidue0.groupbook = buffer.read(8);
        for (n2 = 0; n2 < residue0$InfoResidue0.partitions; ++n2) {
            int n4 = buffer.read(3);
            if (buffer.read(1) != 0) {
                n4 |= buffer.read(5) << 3;
            }
            residue0$InfoResidue0.secondstages[n2] = n4;
            n3 += Util.icount(n4);
        }
        for (n2 = 0; n2 < n3; ++n2) {
            residue0$InfoResidue0.booklist[n2] = buffer.read(8);
        }
        if (residue0$InfoResidue0.groupbook >= info.books) {
            this.free_info(residue0$InfoResidue0);
            return null;
        }
        for (n2 = 0; n2 < n3; ++n2) {
            if (residue0$InfoResidue0.booklist[n2] < info.books) continue;
            this.free_info(residue0$InfoResidue0);
            return null;
        }
        return residue0$InfoResidue0;
    }

    Object look(DspState dspState, InfoMode infoMode, Object object) {
        int n2;
        int n3;
        int n4;
        int n5;
        Residue0$InfoResidue0 residue0$InfoResidue0 = (Residue0$InfoResidue0)object;
        Residue0$LookResidue0 residue0$LookResidue0 = new Residue0$LookResidue0(this);
        int n6 = 0;
        int n7 = 0;
        residue0$LookResidue0.info = residue0$InfoResidue0;
        residue0$LookResidue0.map = infoMode.mapping;
        residue0$LookResidue0.parts = residue0$InfoResidue0.partitions;
        residue0$LookResidue0.fullbooks = dspState.fullbooks;
        residue0$LookResidue0.phrasebook = dspState.fullbooks[residue0$InfoResidue0.groupbook];
        int n8 = residue0$LookResidue0.phrasebook.dim;
        residue0$LookResidue0.partbooks = new int[residue0$LookResidue0.parts][];
        for (n5 = 0; n5 < residue0$LookResidue0.parts; ++n5) {
            n4 = residue0$InfoResidue0.secondstages[n5];
            n3 = Util.ilog(n4);
            if (n3 == 0) continue;
            if (n3 > n7) {
                n7 = n3;
            }
            residue0$LookResidue0.partbooks[n5] = new int[n3];
            for (n2 = 0; n2 < n3; ++n2) {
                if ((n4 & 1 << n2) == 0) continue;
                residue0$LookResidue0.partbooks[n5][n2] = residue0$InfoResidue0.booklist[n6++];
            }
        }
        residue0$LookResidue0.partvals = (int)Math.rint(Math.pow(residue0$LookResidue0.parts, n8));
        residue0$LookResidue0.stages = n7;
        residue0$LookResidue0.decodemap = new int[residue0$LookResidue0.partvals][];
        for (n5 = 0; n5 < residue0$LookResidue0.partvals; ++n5) {
            n4 = n5;
            n3 = residue0$LookResidue0.partvals / residue0$LookResidue0.parts;
            residue0$LookResidue0.decodemap[n5] = new int[n8];
            for (n2 = 0; n2 < n8; ++n2) {
                int n9 = n4 / n3;
                n4 -= n9 * n3;
                n3 /= residue0$LookResidue0.parts;
                residue0$LookResidue0.decodemap[n5][n2] = n9;
            }
        }
        return residue0$LookResidue0;
    }

    void free_info(Object object) {
    }

    void free_look(Object object) {
    }

    static synchronized int _01inverse(Block block, Object object, float[][] fArray, int n2, int n3) {
        int n4;
        Residue0$LookResidue0 residue0$LookResidue0 = (Residue0$LookResidue0)object;
        Residue0$InfoResidue0 residue0$InfoResidue0 = residue0$LookResidue0.info;
        int n5 = residue0$InfoResidue0.grouping;
        int n6 = residue0$LookResidue0.phrasebook.dim;
        int n7 = residue0$InfoResidue0.end - residue0$InfoResidue0.begin;
        int n8 = n7 / n5;
        int n9 = (n8 + n6 - 1) / n6;
        if (_01inverse_partword.length < n2) {
            _01inverse_partword = new int[n2][][];
        }
        for (n4 = 0; n4 < n2; ++n4) {
            if (_01inverse_partword[n4] != null && _01inverse_partword[n4].length >= n9) continue;
            Residue0._01inverse_partword[n4] = new int[n9][];
        }
        for (int i2 = 0; i2 < residue0$LookResidue0.stages; ++i2) {
            int n10 = 0;
            int n11 = 0;
            while (n10 < n8) {
                int n12;
                if (i2 == 0) {
                    for (n4 = 0; n4 < n2; ++n4) {
                        n12 = residue0$LookResidue0.phrasebook.decode(block.opb);
                        if (n12 == -1) {
                            return 0;
                        }
                        Residue0._01inverse_partword[n4][n11] = residue0$LookResidue0.decodemap[n12];
                        if (_01inverse_partword[n4][n11] != null) continue;
                        return 0;
                    }
                }
                for (int i3 = 0; i3 < n6 && n10 < n8; ++i3, ++n10) {
                    for (n4 = 0; n4 < n2; ++n4) {
                        CodeBook codeBook;
                        n12 = residue0$InfoResidue0.begin + n10 * n5;
                        int n13 = _01inverse_partword[n4][n11][i3];
                        if ((residue0$InfoResidue0.secondstages[n13] & 1 << i2) == 0 || (codeBook = residue0$LookResidue0.fullbooks[residue0$LookResidue0.partbooks[n13][i2]]) == null || !(n3 == 0 ? codeBook.decodevs_add(fArray[n4], n12, block.opb, n5) == -1 : n3 == 1 && codeBook.decodev_add(fArray[n4], n12, block.opb, n5) == -1)) continue;
                        return 0;
                    }
                }
                ++n11;
            }
        }
        return 0;
    }

    static synchronized int _2inverse(Block block, Object object, float[][] fArray, int n2) {
        Residue0$LookResidue0 residue0$LookResidue0 = (Residue0$LookResidue0)object;
        Residue0$InfoResidue0 residue0$InfoResidue0 = residue0$LookResidue0.info;
        int n3 = residue0$InfoResidue0.grouping;
        int n4 = residue0$LookResidue0.phrasebook.dim;
        int n5 = residue0$InfoResidue0.end - residue0$InfoResidue0.begin;
        int n6 = n5 / n3;
        int n7 = (n6 + n4 - 1) / n4;
        if (_2inverse_partword == null || _2inverse_partword.length < n7) {
            _2inverse_partword = new int[n7][];
        }
        for (int i2 = 0; i2 < residue0$LookResidue0.stages; ++i2) {
            int n8 = 0;
            int n9 = 0;
            while (n8 < n6) {
                int n10;
                if (i2 == 0) {
                    n10 = residue0$LookResidue0.phrasebook.decode(block.opb);
                    if (n10 == -1) {
                        return 0;
                    }
                    Residue0._2inverse_partword[n9] = residue0$LookResidue0.decodemap[n10];
                    if (_2inverse_partword[n9] == null) {
                        return 0;
                    }
                }
                for (int i3 = 0; i3 < n4 && n8 < n6; ++i3, ++n8) {
                    CodeBook codeBook;
                    n10 = residue0$InfoResidue0.begin + n8 * n3;
                    int n11 = _2inverse_partword[n9][i3];
                    if ((residue0$InfoResidue0.secondstages[n11] & 1 << i2) == 0 || (codeBook = residue0$LookResidue0.fullbooks[residue0$LookResidue0.partbooks[n11][i2]]) == null || codeBook.decodevv_add(fArray, n10, n2, block.opb, n3) != -1) continue;
                    return 0;
                }
                ++n9;
            }
        }
        return 0;
    }

    int inverse(Block block, Object object, float[][] fArray, int[] nArray, int n2) {
        int n3 = 0;
        for (int i2 = 0; i2 < n2; ++i2) {
            if (nArray[i2] == 0) continue;
            fArray[n3++] = fArray[i2];
        }
        if (n3 != 0) {
            return Residue0._01inverse(block, object, fArray, n3, 0);
        }
        return 0;
    }
}

