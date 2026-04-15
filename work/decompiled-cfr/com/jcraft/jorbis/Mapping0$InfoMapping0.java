/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Mapping0;

class Mapping0$InfoMapping0 {
    int submaps;
    int[] chmuxlist = new int[256];
    int[] timesubmap = new int[16];
    int[] floorsubmap = new int[16];
    int[] residuesubmap = new int[16];
    int[] psysubmap = new int[16];
    int coupling_steps;
    int[] coupling_mag = new int[256];
    int[] coupling_ang = new int[256];
    final /* synthetic */ Mapping0 this$0;

    Mapping0$InfoMapping0(Mapping0 mapping0) {
        this.this$0 = mapping0;
    }

    void free() {
        this.chmuxlist = null;
        this.timesubmap = null;
        this.floorsubmap = null;
        this.residuesubmap = null;
        this.psysubmap = null;
        this.coupling_mag = null;
        this.coupling_ang = null;
    }
}

