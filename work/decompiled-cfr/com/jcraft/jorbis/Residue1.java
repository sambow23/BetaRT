/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Residue0;

class Residue1
extends Residue0 {
    Residue1() {
    }

    int inverse(Block block, Object object, float[][] fArray, int[] nArray, int n2) {
        int n3 = 0;
        for (int i2 = 0; i2 < n2; ++i2) {
            if (nArray[i2] == 0) continue;
            fArray[n3++] = fArray[i2];
        }
        if (n3 != 0) {
            return Residue1._01inverse(block, object, fArray, n3, 1);
        }
        return 0;
    }
}

