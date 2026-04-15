/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

class Util {
    Util() {
    }

    static int ilog(int n2) {
        int n3 = 0;
        while (n2 != 0) {
            ++n3;
            n2 >>>= 1;
        }
        return n3;
    }

    static int ilog2(int n2) {
        int n3 = 0;
        while (n2 > 1) {
            ++n3;
            n2 >>>= 1;
        }
        return n3;
    }

    static int icount(int n2) {
        int n3 = 0;
        while (n2 != 0) {
            n3 += n2 & 1;
            n2 >>>= 1;
        }
        return n3;
    }
}

