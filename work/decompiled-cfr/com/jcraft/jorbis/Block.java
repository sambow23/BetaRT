/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.Info;

public class Block {
    float[][] pcm = new float[0][];
    Buffer opb = new Buffer();
    int lW;
    int W;
    int nW;
    int pcmend;
    int mode;
    int eofflag;
    long granulepos;
    long sequence;
    DspState vd;
    int glue_bits;
    int time_bits;
    int floor_bits;
    int res_bits;

    public Block(DspState dspState) {
        this.vd = dspState;
        if (dspState.analysisp != 0) {
            this.opb.writeinit();
        }
    }

    public void init(DspState dspState) {
        this.vd = dspState;
    }

    public int clear() {
        if (this.vd != null && this.vd.analysisp != 0) {
            this.opb.writeclear();
        }
        return 0;
    }

    public int synthesis(Packet packet) {
        int n2;
        Info info = this.vd.vi;
        this.opb.readinit(packet.packet_base, packet.packet, packet.bytes);
        if (this.opb.read(1) != 0) {
            return -1;
        }
        int n3 = this.opb.read(this.vd.modebits);
        if (n3 == -1) {
            return -1;
        }
        this.mode = n3;
        this.W = info.mode_param[this.mode].blockflag;
        if (this.W != 0) {
            this.lW = this.opb.read(1);
            this.nW = this.opb.read(1);
            if (this.nW == -1) {
                return -1;
            }
        } else {
            this.lW = 0;
            this.nW = 0;
        }
        this.granulepos = packet.granulepos;
        this.sequence = packet.packetno - 3L;
        this.eofflag = packet.e_o_s;
        this.pcmend = info.blocksizes[this.W];
        if (this.pcm.length < info.channels) {
            this.pcm = new float[info.channels][];
        }
        for (n2 = 0; n2 < info.channels; ++n2) {
            if (this.pcm[n2] == null || this.pcm[n2].length < this.pcmend) {
                this.pcm[n2] = new float[this.pcmend];
                continue;
            }
            for (int i2 = 0; i2 < this.pcmend; ++i2) {
                this.pcm[n2][i2] = 0.0f;
            }
        }
        n2 = info.map_type[info.mode_param[this.mode].mapping];
        return FuncMapping.mapping_P[n2].inverse(this, this.vd.mode[this.mode]);
    }
}

