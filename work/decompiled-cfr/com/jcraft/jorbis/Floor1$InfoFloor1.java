/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Floor1;

class Floor1$InfoFloor1 {
    static final int VIF_POSIT = 63;
    static final int VIF_CLASS = 16;
    static final int VIF_PARTS = 31;
    int partitions;
    int[] partitionclass = new int[31];
    int[] class_dim = new int[16];
    int[] class_subs = new int[16];
    int[] class_book = new int[16];
    int[][] class_subbook = new int[16][];
    int mult;
    int[] postlist = new int[65];
    float maxover;
    float maxunder;
    float maxerr;
    int twofitminsize;
    int twofitminused;
    int twofitweight;
    float twofitatten;
    int unusedminsize;
    int unusedmin_n;
    int n;
    final /* synthetic */ Floor1 this$0;

    Floor1$InfoFloor1(Floor1 floor1) {
        this.this$0 = floor1;
        for (int i2 = 0; i2 < this.class_subbook.length; ++i2) {
            this.class_subbook[i2] = new int[8];
        }
    }

    void free() {
        this.partitionclass = null;
        this.class_dim = null;
        this.class_subs = null;
        this.class_book = null;
        this.class_subbook = null;
        this.postlist = null;
    }

    Object copy_info() {
        Floor1$InfoFloor1 floor1$InfoFloor1 = this;
        Floor1$InfoFloor1 floor1$InfoFloor12 = new Floor1$InfoFloor1(this.this$0);
        floor1$InfoFloor12.partitions = floor1$InfoFloor1.partitions;
        System.arraycopy(floor1$InfoFloor1.partitionclass, 0, floor1$InfoFloor12.partitionclass, 0, 31);
        System.arraycopy(floor1$InfoFloor1.class_dim, 0, floor1$InfoFloor12.class_dim, 0, 16);
        System.arraycopy(floor1$InfoFloor1.class_subs, 0, floor1$InfoFloor12.class_subs, 0, 16);
        System.arraycopy(floor1$InfoFloor1.class_book, 0, floor1$InfoFloor12.class_book, 0, 16);
        for (int i2 = 0; i2 < 16; ++i2) {
            System.arraycopy(floor1$InfoFloor1.class_subbook[i2], 0, floor1$InfoFloor12.class_subbook[i2], 0, 8);
        }
        floor1$InfoFloor12.mult = floor1$InfoFloor1.mult;
        System.arraycopy(floor1$InfoFloor1.postlist, 0, floor1$InfoFloor12.postlist, 0, 65);
        floor1$InfoFloor12.maxover = floor1$InfoFloor1.maxover;
        floor1$InfoFloor12.maxunder = floor1$InfoFloor1.maxunder;
        floor1$InfoFloor12.maxerr = floor1$InfoFloor1.maxerr;
        floor1$InfoFloor12.twofitminsize = floor1$InfoFloor1.twofitminsize;
        floor1$InfoFloor12.twofitminused = floor1$InfoFloor1.twofitminused;
        floor1$InfoFloor12.twofitweight = floor1$InfoFloor1.twofitweight;
        floor1$InfoFloor12.twofitatten = floor1$InfoFloor1.twofitatten;
        floor1$InfoFloor12.unusedminsize = floor1$InfoFloor1.unusedminsize;
        floor1$InfoFloor12.unusedmin_n = floor1$InfoFloor1.unusedmin_n;
        floor1$InfoFloor12.n = floor1$InfoFloor1.n;
        return floor1$InfoFloor12;
    }
}

