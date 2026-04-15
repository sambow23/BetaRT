/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.CodeBook;
import com.jcraft.jorbis.Residue0;
import com.jcraft.jorbis.Residue0$InfoResidue0;

class Residue0$LookResidue0 {
    Residue0$InfoResidue0 info;
    int map;
    int parts;
    int stages;
    CodeBook[] fullbooks;
    CodeBook phrasebook;
    int[][] partbooks;
    int partvals;
    int[][] decodemap;
    int postbits;
    int phrasebits;
    int frames;
    final /* synthetic */ Residue0 this$0;

    Residue0$LookResidue0(Residue0 residue0) {
        this.this$0 = residue0;
    }
}

