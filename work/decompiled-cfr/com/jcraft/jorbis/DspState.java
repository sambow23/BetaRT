/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.CodeBook;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.Mdct;
import com.jcraft.jorbis.Util;

public class DspState {
    static final float M_PI = (float)Math.PI;
    static final int VI_TRANSFORMB = 1;
    static final int VI_WINDOWB = 1;
    int analysisp;
    Info vi;
    int modebits;
    float[][] pcm;
    int pcm_storage;
    int pcm_current;
    int pcm_returned;
    float[] multipliers;
    int envelope_storage;
    int envelope_current;
    int eofflag;
    int lW;
    int W;
    int nW;
    int centerW;
    long granulepos;
    long sequence;
    long glue_bits;
    long time_bits;
    long floor_bits;
    long res_bits;
    float[][][][][] window;
    Object[][] transform = new Object[2][];
    CodeBook[] fullbooks;
    Object[] mode;
    byte[] header;
    byte[] header1;
    byte[] header2;

    public DspState() {
        this.window = new float[2][][][][];
        this.window[0] = new float[2][][][];
        this.window[0][0] = new float[2][][];
        this.window[0][1] = new float[2][][];
        this.window[0][0][0] = new float[2][];
        this.window[0][0][1] = new float[2][];
        this.window[0][1][0] = new float[2][];
        this.window[0][1][1] = new float[2][];
        this.window[1] = new float[2][][][];
        this.window[1][0] = new float[2][][];
        this.window[1][1] = new float[2][][];
        this.window[1][0][0] = new float[2][];
        this.window[1][0][1] = new float[2][];
        this.window[1][1][0] = new float[2][];
        this.window[1][1][1] = new float[2][];
    }

    static float[] window(int n2, int n3, int n4, int n5) {
        float[] fArray = new float[n3];
        switch (n2) {
            case 0: {
                float f2;
                int n6;
                int n7 = n3 / 4 - n4 / 2;
                int n8 = n3 - n3 / 4 - n5 / 2;
                for (n6 = 0; n6 < n4; ++n6) {
                    f2 = (float)(((double)n6 + 0.5) / (double)n4 * 3.1415927410125732 / 2.0);
                    f2 = (float)Math.sin(f2);
                    f2 *= f2;
                    f2 = (float)((double)f2 * 1.5707963705062866);
                    fArray[n6 + n7] = f2 = (float)Math.sin(f2);
                }
                for (n6 = n7 + n4; n6 < n8; ++n6) {
                    fArray[n6] = 1.0f;
                }
                for (n6 = 0; n6 < n5; ++n6) {
                    f2 = (float)(((double)(n5 - n6) - 0.5) / (double)n5 * 3.1415927410125732 / 2.0);
                    f2 = (float)Math.sin(f2);
                    f2 *= f2;
                    f2 = (float)((double)f2 * 1.5707963705062866);
                    fArray[n6 + n8] = f2 = (float)Math.sin(f2);
                }
                break;
            }
            default: {
                return null;
            }
        }
        return fArray;
    }

    int init(Info info, boolean bl2) {
        int n2;
        this.vi = info;
        this.modebits = Util.ilog2(info.modes);
        this.transform[0] = new Object[1];
        this.transform[1] = new Object[1];
        this.transform[0][0] = new Mdct();
        this.transform[1][0] = new Mdct();
        ((Mdct)this.transform[0][0]).init(info.blocksizes[0]);
        ((Mdct)this.transform[1][0]).init(info.blocksizes[1]);
        this.window[0][0][0] = new float[1][];
        this.window[0][0][1] = this.window[0][0][0];
        this.window[0][1][0] = this.window[0][0][0];
        this.window[0][1][1] = this.window[0][0][0];
        this.window[1][0][0] = new float[1][];
        this.window[1][0][1] = new float[1][];
        this.window[1][1][0] = new float[1][];
        this.window[1][1][1] = new float[1][];
        for (n2 = 0; n2 < 1; ++n2) {
            this.window[0][0][0][n2] = DspState.window(n2, info.blocksizes[0], info.blocksizes[0] / 2, info.blocksizes[0] / 2);
            this.window[1][0][0][n2] = DspState.window(n2, info.blocksizes[1], info.blocksizes[0] / 2, info.blocksizes[0] / 2);
            this.window[1][0][1][n2] = DspState.window(n2, info.blocksizes[1], info.blocksizes[0] / 2, info.blocksizes[1] / 2);
            this.window[1][1][0][n2] = DspState.window(n2, info.blocksizes[1], info.blocksizes[1] / 2, info.blocksizes[0] / 2);
            this.window[1][1][1][n2] = DspState.window(n2, info.blocksizes[1], info.blocksizes[1] / 2, info.blocksizes[1] / 2);
        }
        this.fullbooks = new CodeBook[info.books];
        for (n2 = 0; n2 < info.books; ++n2) {
            this.fullbooks[n2] = new CodeBook();
            this.fullbooks[n2].init_decode(info.book_param[n2]);
        }
        this.pcm_storage = 8192;
        this.pcm = new float[info.channels][];
        for (n2 = 0; n2 < info.channels; ++n2) {
            this.pcm[n2] = new float[this.pcm_storage];
        }
        this.lW = 0;
        this.W = 0;
        this.pcm_current = this.centerW = info.blocksizes[1] / 2;
        this.mode = new Object[info.modes];
        for (n2 = 0; n2 < info.modes; ++n2) {
            int n3 = info.mode_param[n2].mapping;
            int n4 = info.map_type[n3];
            this.mode[n2] = FuncMapping.mapping_P[n4].look(this, info.mode_param[n2], info.map_param[n3]);
        }
        return 0;
    }

    public int synthesis_init(Info info) {
        this.init(info, false);
        this.pcm_returned = this.centerW;
        this.centerW -= info.blocksizes[this.W] / 4 + info.blocksizes[this.lW] / 4;
        this.granulepos = -1L;
        this.sequence = -1L;
        return 0;
    }

    DspState(Info info) {
        this();
        this.init(info, false);
        this.pcm_returned = this.centerW;
        this.centerW -= info.blocksizes[this.W] / 4 + info.blocksizes[this.lW] / 4;
        this.granulepos = -1L;
        this.sequence = -1L;
    }

    public int synthesis_blockin(Block block) {
        int n2;
        int n3;
        int n4;
        if (this.centerW > this.vi.blocksizes[1] / 2 && this.pcm_returned > 8192) {
            n4 = this.centerW - this.vi.blocksizes[1] / 2;
            n4 = this.pcm_returned < n4 ? this.pcm_returned : n4;
            this.pcm_current -= n4;
            this.centerW -= n4;
            this.pcm_returned -= n4;
            if (n4 != 0) {
                for (n3 = 0; n3 < this.vi.channels; ++n3) {
                    System.arraycopy(this.pcm[n3], n4, this.pcm[n3], 0, this.pcm_current);
                }
            }
        }
        this.lW = this.W;
        this.W = block.W;
        this.nW = -1;
        this.glue_bits += (long)block.glue_bits;
        this.time_bits += (long)block.time_bits;
        this.floor_bits += (long)block.floor_bits;
        this.res_bits += (long)block.res_bits;
        if (this.sequence + 1L != block.sequence) {
            this.granulepos = -1L;
        }
        this.sequence = block.sequence;
        n4 = this.vi.blocksizes[this.W];
        n3 = this.centerW + this.vi.blocksizes[this.lW] / 4 + n4 / 4;
        int n5 = n3 - n4 / 2;
        int n6 = n5 + n4;
        int n7 = 0;
        int n8 = 0;
        if (n6 > this.pcm_storage) {
            this.pcm_storage = n6 + this.vi.blocksizes[1];
            for (n2 = 0; n2 < this.vi.channels; ++n2) {
                float[] fArray = new float[this.pcm_storage];
                System.arraycopy(this.pcm[n2], 0, fArray, 0, this.pcm[n2].length);
                this.pcm[n2] = fArray;
            }
        }
        switch (this.W) {
            case 0: {
                n7 = 0;
                n8 = this.vi.blocksizes[0] / 2;
                break;
            }
            case 1: {
                n7 = this.vi.blocksizes[1] / 4 - this.vi.blocksizes[this.lW] / 4;
                n8 = n7 + this.vi.blocksizes[this.lW] / 2;
            }
        }
        for (n2 = 0; n2 < this.vi.channels; ++n2) {
            int n9 = n5;
            int n10 = 0;
            for (n10 = n7; n10 < n8; ++n10) {
                float[] fArray = this.pcm[n2];
                int n11 = n9 + n10;
                fArray[n11] = fArray[n11] + block.pcm[n2][n10];
            }
            while (n10 < n4) {
                this.pcm[n2][n9 + n10] = block.pcm[n2][n10];
                ++n10;
            }
        }
        if (this.granulepos == -1L) {
            this.granulepos = block.granulepos;
        } else {
            this.granulepos += (long)(n3 - this.centerW);
            if (block.granulepos != -1L && this.granulepos != block.granulepos) {
                if (this.granulepos > block.granulepos && block.eofflag != 0) {
                    n3 = (int)((long)n3 - (this.granulepos - block.granulepos));
                }
                this.granulepos = block.granulepos;
            }
        }
        this.centerW = n3;
        this.pcm_current = n6;
        if (block.eofflag != 0) {
            this.eofflag = 1;
        }
        return 0;
    }

    public int synthesis_pcmout(float[][][] fArray, int[] nArray) {
        if (this.pcm_returned < this.centerW) {
            if (fArray != null) {
                for (int i2 = 0; i2 < this.vi.channels; ++i2) {
                    nArray[i2] = this.pcm_returned;
                }
                fArray[0] = this.pcm;
            }
            return this.centerW - this.pcm_returned;
        }
        return 0;
    }

    public int synthesis_read(int n2) {
        if (n2 != 0 && this.pcm_returned + n2 > this.centerW) {
            return -1;
        }
        this.pcm_returned += n2;
        return 0;
    }

    public void clear() {
    }
}

