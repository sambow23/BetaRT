/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.FuncFloor;
import com.jcraft.jorbis.FuncResidue;
import com.jcraft.jorbis.FuncTime;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.Mapping0;
import com.jcraft.jorbis.Mapping0$InfoMapping0;
import com.jcraft.jorbis.PsyLook;

class Mapping0$LookMapping0 {
    InfoMode mode;
    Mapping0$InfoMapping0 map;
    Object[] time_look;
    Object[] floor_look;
    Object[] floor_state;
    Object[] residue_look;
    PsyLook[] psy_look;
    FuncTime[] time_func;
    FuncFloor[] floor_func;
    FuncResidue[] residue_func;
    int ch;
    float[][] decay;
    int lastframe;
    final /* synthetic */ Mapping0 this$0;

    Mapping0$LookMapping0(Mapping0 mapping0) {
        this.this$0 = mapping0;
    }
}

