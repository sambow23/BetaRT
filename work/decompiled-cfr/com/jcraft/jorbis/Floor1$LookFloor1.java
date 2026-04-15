/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Floor1;
import com.jcraft.jorbis.Floor1$InfoFloor1;

class Floor1$LookFloor1 {
    static final int VIF_POSIT = 63;
    int[] sorted_index = new int[65];
    int[] forward_index = new int[65];
    int[] reverse_index = new int[65];
    int[] hineighbor = new int[63];
    int[] loneighbor = new int[63];
    int posts;
    int n;
    int quant_q;
    Floor1$InfoFloor1 vi;
    int phrasebits;
    int postbits;
    int frames;
    final /* synthetic */ Floor1 this$0;

    Floor1$LookFloor1(Floor1 floor1) {
        this.this$0 = floor1;
    }

    void free() {
        this.sorted_index = null;
        this.forward_index = null;
        this.reverse_index = null;
        this.hineighbor = null;
        this.loneighbor = null;
    }
}

