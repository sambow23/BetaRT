/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.FuncFloor;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.FuncResidue;
import com.jcraft.jorbis.FuncTime;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.Mapping0$InfoMapping0;
import com.jcraft.jorbis.Mapping0$LookMapping0;
import com.jcraft.jorbis.Mdct;
import com.jcraft.jorbis.Util;

class Mapping0
extends FuncMapping {
    static int seq = 0;
    float[][] pcmbundle = null;
    int[] zerobundle = null;
    int[] nonzero = null;
    Object[] floormemo = null;

    Mapping0() {
    }

    void free_info(Object object) {
    }

    void free_look(Object object) {
    }

    Object look(DspState dspState, InfoMode infoMode, Object object) {
        Info info = dspState.vi;
        Mapping0$LookMapping0 mapping0$LookMapping0 = new Mapping0$LookMapping0(this);
        Mapping0$InfoMapping0 mapping0$InfoMapping0 = mapping0$LookMapping0.map = (Mapping0$InfoMapping0)object;
        mapping0$LookMapping0.mode = infoMode;
        mapping0$LookMapping0.time_look = new Object[mapping0$InfoMapping0.submaps];
        mapping0$LookMapping0.floor_look = new Object[mapping0$InfoMapping0.submaps];
        mapping0$LookMapping0.residue_look = new Object[mapping0$InfoMapping0.submaps];
        mapping0$LookMapping0.time_func = new FuncTime[mapping0$InfoMapping0.submaps];
        mapping0$LookMapping0.floor_func = new FuncFloor[mapping0$InfoMapping0.submaps];
        mapping0$LookMapping0.residue_func = new FuncResidue[mapping0$InfoMapping0.submaps];
        for (int i2 = 0; i2 < mapping0$InfoMapping0.submaps; ++i2) {
            int n2 = mapping0$InfoMapping0.timesubmap[i2];
            int n3 = mapping0$InfoMapping0.floorsubmap[i2];
            int n4 = mapping0$InfoMapping0.residuesubmap[i2];
            mapping0$LookMapping0.time_func[i2] = FuncTime.time_P[info.time_type[n2]];
            mapping0$LookMapping0.time_look[i2] = mapping0$LookMapping0.time_func[i2].look(dspState, infoMode, info.time_param[n2]);
            mapping0$LookMapping0.floor_func[i2] = FuncFloor.floor_P[info.floor_type[n3]];
            mapping0$LookMapping0.floor_look[i2] = mapping0$LookMapping0.floor_func[i2].look(dspState, infoMode, info.floor_param[n3]);
            mapping0$LookMapping0.residue_func[i2] = FuncResidue.residue_P[info.residue_type[n4]];
            mapping0$LookMapping0.residue_look[i2] = mapping0$LookMapping0.residue_func[i2].look(dspState, infoMode, info.residue_param[n4]);
        }
        if (info.psys == 0 || dspState.analysisp != 0) {
            // empty if block
        }
        mapping0$LookMapping0.ch = info.channels;
        return mapping0$LookMapping0;
    }

    void pack(Info info, Object object, Buffer buffer) {
        int n2;
        Mapping0$InfoMapping0 mapping0$InfoMapping0 = (Mapping0$InfoMapping0)object;
        if (mapping0$InfoMapping0.submaps > 1) {
            buffer.write(1, 1);
            buffer.write(mapping0$InfoMapping0.submaps - 1, 4);
        } else {
            buffer.write(0, 1);
        }
        if (mapping0$InfoMapping0.coupling_steps > 0) {
            buffer.write(1, 1);
            buffer.write(mapping0$InfoMapping0.coupling_steps - 1, 8);
            for (n2 = 0; n2 < mapping0$InfoMapping0.coupling_steps; ++n2) {
                buffer.write(mapping0$InfoMapping0.coupling_mag[n2], Util.ilog2(info.channels));
                buffer.write(mapping0$InfoMapping0.coupling_ang[n2], Util.ilog2(info.channels));
            }
        } else {
            buffer.write(0, 1);
        }
        buffer.write(0, 2);
        if (mapping0$InfoMapping0.submaps > 1) {
            for (n2 = 0; n2 < info.channels; ++n2) {
                buffer.write(mapping0$InfoMapping0.chmuxlist[n2], 4);
            }
        }
        for (n2 = 0; n2 < mapping0$InfoMapping0.submaps; ++n2) {
            buffer.write(mapping0$InfoMapping0.timesubmap[n2], 8);
            buffer.write(mapping0$InfoMapping0.floorsubmap[n2], 8);
            buffer.write(mapping0$InfoMapping0.residuesubmap[n2], 8);
        }
    }

    Object unpack(Info info, Buffer buffer) {
        int n2;
        Mapping0$InfoMapping0 mapping0$InfoMapping0 = new Mapping0$InfoMapping0(this);
        mapping0$InfoMapping0.submaps = buffer.read(1) != 0 ? buffer.read(4) + 1 : 1;
        if (buffer.read(1) != 0) {
            mapping0$InfoMapping0.coupling_steps = buffer.read(8) + 1;
            for (n2 = 0; n2 < mapping0$InfoMapping0.coupling_steps; ++n2) {
                int n3 = mapping0$InfoMapping0.coupling_mag[n2] = buffer.read(Util.ilog2(info.channels));
                int n4 = mapping0$InfoMapping0.coupling_ang[n2] = buffer.read(Util.ilog2(info.channels));
                if (n3 >= 0 && n4 >= 0 && n3 != n4 && n3 < info.channels && n4 < info.channels) continue;
                mapping0$InfoMapping0.free();
                return null;
            }
        }
        if (buffer.read(2) > 0) {
            mapping0$InfoMapping0.free();
            return null;
        }
        if (mapping0$InfoMapping0.submaps > 1) {
            for (n2 = 0; n2 < info.channels; ++n2) {
                mapping0$InfoMapping0.chmuxlist[n2] = buffer.read(4);
                if (mapping0$InfoMapping0.chmuxlist[n2] < mapping0$InfoMapping0.submaps) continue;
                mapping0$InfoMapping0.free();
                return null;
            }
        }
        for (n2 = 0; n2 < mapping0$InfoMapping0.submaps; ++n2) {
            mapping0$InfoMapping0.timesubmap[n2] = buffer.read(8);
            if (mapping0$InfoMapping0.timesubmap[n2] >= info.times) {
                mapping0$InfoMapping0.free();
                return null;
            }
            mapping0$InfoMapping0.floorsubmap[n2] = buffer.read(8);
            if (mapping0$InfoMapping0.floorsubmap[n2] >= info.floors) {
                mapping0$InfoMapping0.free();
                return null;
            }
            mapping0$InfoMapping0.residuesubmap[n2] = buffer.read(8);
            if (mapping0$InfoMapping0.residuesubmap[n2] < info.residues) continue;
            mapping0$InfoMapping0.free();
            return null;
        }
        return mapping0$InfoMapping0;
    }

    synchronized int inverse(Block block, Object object) {
        int n2;
        int n3;
        int n4;
        DspState dspState = block.vd;
        Info info = dspState.vi;
        Mapping0$LookMapping0 mapping0$LookMapping0 = (Mapping0$LookMapping0)object;
        Mapping0$InfoMapping0 mapping0$InfoMapping0 = mapping0$LookMapping0.map;
        InfoMode infoMode = mapping0$LookMapping0.mode;
        int n5 = block.pcmend = info.blocksizes[block.W];
        float[] fArray = dspState.window[block.W][block.lW][block.nW][infoMode.windowtype];
        if (this.pcmbundle == null || this.pcmbundle.length < info.channels) {
            this.pcmbundle = new float[info.channels][];
            this.nonzero = new int[info.channels];
            this.zerobundle = new int[info.channels];
            this.floormemo = new Object[info.channels];
        }
        for (n4 = 0; n4 < info.channels; ++n4) {
            float[] fArray2 = block.pcm[n4];
            n3 = mapping0$InfoMapping0.chmuxlist[n4];
            this.floormemo[n4] = mapping0$LookMapping0.floor_func[n3].inverse1(block, mapping0$LookMapping0.floor_look[n3], this.floormemo[n4]);
            this.nonzero[n4] = this.floormemo[n4] != null ? 1 : 0;
            for (n2 = 0; n2 < n5 / 2; ++n2) {
                fArray2[n2] = 0.0f;
            }
        }
        for (n4 = 0; n4 < mapping0$InfoMapping0.coupling_steps; ++n4) {
            if (this.nonzero[mapping0$InfoMapping0.coupling_mag[n4]] == 0 && this.nonzero[mapping0$InfoMapping0.coupling_ang[n4]] == 0) continue;
            this.nonzero[mapping0$InfoMapping0.coupling_mag[n4]] = 1;
            this.nonzero[mapping0$InfoMapping0.coupling_ang[n4]] = 1;
        }
        for (n4 = 0; n4 < mapping0$InfoMapping0.submaps; ++n4) {
            int n6 = 0;
            for (n3 = 0; n3 < info.channels; ++n3) {
                if (mapping0$InfoMapping0.chmuxlist[n3] != n4) continue;
                this.zerobundle[n6] = this.nonzero[n3] != 0 ? 1 : 0;
                this.pcmbundle[n6++] = block.pcm[n3];
            }
            mapping0$LookMapping0.residue_func[n4].inverse(block, mapping0$LookMapping0.residue_look[n4], this.pcmbundle, this.zerobundle, n6);
        }
        for (n4 = mapping0$InfoMapping0.coupling_steps - 1; n4 >= 0; --n4) {
            float[] fArray3 = block.pcm[mapping0$InfoMapping0.coupling_mag[n4]];
            float[] fArray4 = block.pcm[mapping0$InfoMapping0.coupling_ang[n4]];
            for (n2 = 0; n2 < n5 / 2; ++n2) {
                float f2 = fArray3[n2];
                float f3 = fArray4[n2];
                if (f2 > 0.0f) {
                    if (f3 > 0.0f) {
                        fArray3[n2] = f2;
                        fArray4[n2] = f2 - f3;
                        continue;
                    }
                    fArray4[n2] = f2;
                    fArray3[n2] = f2 + f3;
                    continue;
                }
                if (f3 > 0.0f) {
                    fArray3[n2] = f2;
                    fArray4[n2] = f2 + f3;
                    continue;
                }
                fArray4[n2] = f2;
                fArray3[n2] = f2 - f3;
            }
        }
        for (n4 = 0; n4 < info.channels; ++n4) {
            float[] fArray5 = block.pcm[n4];
            int n7 = mapping0$InfoMapping0.chmuxlist[n4];
            mapping0$LookMapping0.floor_func[n7].inverse2(block, mapping0$LookMapping0.floor_look[n7], this.floormemo[n4], fArray5);
        }
        for (n4 = 0; n4 < info.channels; ++n4) {
            float[] fArray6 = block.pcm[n4];
            ((Mdct)dspState.transform[block.W][0]).backward(fArray6, fArray6);
        }
        for (n4 = 0; n4 < info.channels; ++n4) {
            int n8;
            float[] fArray7 = block.pcm[n4];
            if (this.nonzero[n4] != 0) {
                for (n8 = 0; n8 < n5; ++n8) {
                    int n9 = n8;
                    fArray7[n9] = fArray7[n9] * fArray[n8];
                }
                continue;
            }
            for (n8 = 0; n8 < n5; ++n8) {
                fArray7[n8] = 0.0f;
            }
        }
        return 0;
    }
}

