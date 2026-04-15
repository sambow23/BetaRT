/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Residue0;

class Residue2
extends Residue0 {
    Residue2() {
    }

    int inverse(Block block, Object object, float[][] fArray, int[] nArray, int n2) {
        int n3 = 0;
        for (n3 = 0; n3 < n2 && nArray[n3] == 0; ++n3) {
        }
        if (n3 == n2) {
            return 0;
        }
        return Residue2._2inverse(block, object, fArray, n2);
    }
}

