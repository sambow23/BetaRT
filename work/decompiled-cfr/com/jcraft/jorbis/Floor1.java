/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.CodeBook;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Floor1$InfoFloor1;
import com.jcraft.jorbis.Floor1$LookFloor1;
import com.jcraft.jorbis.FuncFloor;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.Util;

class Floor1
extends FuncFloor {
    static final int floor1_rangedb = 140;
    static final int VIF_POSIT = 63;
    private static float[] FLOOR_fromdB_LOOKUP = new float[]{1.0649863E-7f, 1.1341951E-7f, 1.2079015E-7f, 1.2863978E-7f, 1.369995E-7f, 1.459025E-7f, 1.5538409E-7f, 1.6548181E-7f, 1.7623574E-7f, 1.8768856E-7f, 1.998856E-7f, 2.128753E-7f, 2.2670913E-7f, 2.4144197E-7f, 2.5713223E-7f, 2.7384212E-7f, 2.9163792E-7f, 3.1059022E-7f, 3.307741E-7f, 3.5226967E-7f, 3.7516213E-7f, 3.995423E-7f, 4.255068E-7f, 4.5315863E-7f, 4.8260745E-7f, 5.1397E-7f, 5.4737063E-7f, 5.829419E-7f, 6.208247E-7f, 6.611694E-7f, 7.041359E-7f, 7.4989464E-7f, 7.98627E-7f, 8.505263E-7f, 9.057983E-7f, 9.646621E-7f, 1.0273513E-6f, 1.0941144E-6f, 1.1652161E-6f, 1.2409384E-6f, 1.3215816E-6f, 1.4074654E-6f, 1.4989305E-6f, 1.5963394E-6f, 1.7000785E-6f, 1.8105592E-6f, 1.9282195E-6f, 2.053526E-6f, 2.1869757E-6f, 2.3290977E-6f, 2.4804558E-6f, 2.6416496E-6f, 2.813319E-6f, 2.9961443E-6f, 3.1908505E-6f, 3.39821E-6f, 3.619045E-6f, 3.8542307E-6f, 4.1047006E-6f, 4.371447E-6f, 4.6555283E-6f, 4.958071E-6f, 5.280274E-6f, 5.623416E-6f, 5.988857E-6f, 6.3780467E-6f, 6.7925284E-6f, 7.2339453E-6f, 7.704048E-6f, 8.2047E-6f, 8.737888E-6f, 9.305725E-6f, 9.910464E-6f, 1.0554501E-5f, 1.1240392E-5f, 1.1970856E-5f, 1.2748789E-5f, 1.3577278E-5f, 1.4459606E-5f, 1.5399271E-5f, 1.6400005E-5f, 1.7465769E-5f, 1.8600793E-5f, 1.9809577E-5f, 2.1096914E-5f, 2.2467912E-5f, 2.3928002E-5f, 2.5482977E-5f, 2.7139005E-5f, 2.890265E-5f, 3.078091E-5f, 3.2781227E-5f, 3.4911533E-5f, 3.718028E-5f, 3.9596467E-5f, 4.2169668E-5f, 4.491009E-5f, 4.7828602E-5f, 5.0936775E-5f, 5.424693E-5f, 5.7772202E-5f, 6.152657E-5f, 6.552491E-5f, 6.9783084E-5f, 7.4317984E-5f, 7.914758E-5f, 8.429104E-5f, 8.976875E-5f, 9.560242E-5f, 1.0181521E-4f, 1.0843174E-4f, 1.1547824E-4f, 1.2298267E-4f, 1.3097477E-4f, 1.3948625E-4f, 1.4855085E-4f, 1.5820454E-4f, 1.6848555E-4f, 1.7943469E-4f, 1.9109536E-4f, 2.0351382E-4f, 2.167393E-4f, 2.3082423E-4f, 2.4582449E-4f, 2.6179955E-4f, 2.7881275E-4f, 2.9693157E-4f, 3.1622787E-4f, 3.3677815E-4f, 3.5866388E-4f, 3.8197188E-4f, 4.0679457E-4f, 4.3323037E-4f, 4.613841E-4f, 4.913675E-4f, 5.2329927E-4f, 5.573062E-4f, 5.935231E-4f, 6.320936E-4f, 6.731706E-4f, 7.16917E-4f, 7.635063E-4f, 8.1312325E-4f, 8.6596457E-4f, 9.2223985E-4f, 9.821722E-4f, 0.0010459992f, 0.0011139743f, 0.0011863665f, 0.0012634633f, 0.0013455702f, 0.0014330129f, 0.0015261382f, 0.0016253153f, 0.0017309374f, 0.0018434235f, 0.0019632196f, 0.0020908006f, 0.0022266726f, 0.0023713743f, 0.0025254795f, 0.0026895993f, 0.0028643848f, 0.0030505287f, 0.003248769f, 0.0034598925f, 0.0036847359f, 0.0039241905f, 0.0041792067f, 0.004450795f, 0.004740033f, 0.005048067f, 0.0053761187f, 0.005725489f, 0.0060975635f, 0.0064938175f, 0.0069158226f, 0.0073652514f, 0.007843887f, 0.008353627f, 0.008896492f, 0.009474637f, 0.010090352f, 0.01074608f, 0.011444421f, 0.012188144f, 0.012980198f, 0.013823725f, 0.014722068f, 0.015678791f, 0.016697686f, 0.017782796f, 0.018938422f, 0.020169148f, 0.021479854f, 0.022875736f, 0.02436233f, 0.025945531f, 0.027631618f, 0.029427277f, 0.031339627f, 0.03337625f, 0.035545226f, 0.037855156f, 0.0403152f, 0.042935107f, 0.045725275f, 0.048696756f, 0.05186135f, 0.05523159f, 0.05882085f, 0.062643364f, 0.06671428f, 0.07104975f, 0.075666964f, 0.08058423f, 0.08582105f, 0.09139818f, 0.097337745f, 0.1036633f, 0.11039993f, 0.11757434f, 0.12521498f, 0.13335215f, 0.14201812f, 0.15124726f, 0.16107617f, 0.1715438f, 0.18269168f, 0.19456401f, 0.20720787f, 0.22067343f, 0.23501402f, 0.25028655f, 0.26655158f, 0.28387362f, 0.3023213f, 0.32196787f, 0.34289113f, 0.36517414f, 0.3889052f, 0.41417846f, 0.44109413f, 0.4697589f, 0.50028646f, 0.53279793f, 0.5674221f, 0.6042964f, 0.64356697f, 0.6853896f, 0.72993004f, 0.777365f, 0.8278826f, 0.88168305f, 0.9389798f, 1.0f};

    Floor1() {
    }

    void pack(Object object, Buffer buffer) {
        int n2;
        int n3;
        Floor1$InfoFloor1 floor1$InfoFloor1 = (Floor1$InfoFloor1)object;
        int n4 = 0;
        int n5 = floor1$InfoFloor1.postlist[1];
        int n6 = -1;
        buffer.write(floor1$InfoFloor1.partitions, 5);
        for (n3 = 0; n3 < floor1$InfoFloor1.partitions; ++n3) {
            buffer.write(floor1$InfoFloor1.partitionclass[n3], 4);
            if (n6 >= floor1$InfoFloor1.partitionclass[n3]) continue;
            n6 = floor1$InfoFloor1.partitionclass[n3];
        }
        for (n3 = 0; n3 < n6 + 1; ++n3) {
            buffer.write(floor1$InfoFloor1.class_dim[n3] - 1, 3);
            buffer.write(floor1$InfoFloor1.class_subs[n3], 2);
            if (floor1$InfoFloor1.class_subs[n3] != 0) {
                buffer.write(floor1$InfoFloor1.class_book[n3], 8);
            }
            for (n2 = 0; n2 < 1 << floor1$InfoFloor1.class_subs[n3]; ++n2) {
                buffer.write(floor1$InfoFloor1.class_subbook[n3][n2] + 1, 8);
            }
        }
        buffer.write(floor1$InfoFloor1.mult - 1, 2);
        buffer.write(Util.ilog2(n5), 4);
        int n7 = Util.ilog2(n5);
        n2 = 0;
        for (n3 = 0; n3 < floor1$InfoFloor1.partitions; ++n3) {
            n4 += floor1$InfoFloor1.class_dim[floor1$InfoFloor1.partitionclass[n3]];
            while (n2 < n4) {
                buffer.write(floor1$InfoFloor1.postlist[n2 + 2], n7);
                ++n2;
            }
        }
    }

    Object unpack(Info info, Buffer buffer) {
        int n2;
        int n3;
        int n4 = 0;
        int n5 = -1;
        Floor1$InfoFloor1 floor1$InfoFloor1 = new Floor1$InfoFloor1(this);
        floor1$InfoFloor1.partitions = buffer.read(5);
        for (n3 = 0; n3 < floor1$InfoFloor1.partitions; ++n3) {
            floor1$InfoFloor1.partitionclass[n3] = buffer.read(4);
            if (n5 >= floor1$InfoFloor1.partitionclass[n3]) continue;
            n5 = floor1$InfoFloor1.partitionclass[n3];
        }
        for (n3 = 0; n3 < n5 + 1; ++n3) {
            floor1$InfoFloor1.class_dim[n3] = buffer.read(3) + 1;
            floor1$InfoFloor1.class_subs[n3] = buffer.read(2);
            if (floor1$InfoFloor1.class_subs[n3] < 0) {
                floor1$InfoFloor1.free();
                return null;
            }
            if (floor1$InfoFloor1.class_subs[n3] != 0) {
                floor1$InfoFloor1.class_book[n3] = buffer.read(8);
            }
            if (floor1$InfoFloor1.class_book[n3] < 0 || floor1$InfoFloor1.class_book[n3] >= info.books) {
                floor1$InfoFloor1.free();
                return null;
            }
            for (n2 = 0; n2 < 1 << floor1$InfoFloor1.class_subs[n3]; ++n2) {
                floor1$InfoFloor1.class_subbook[n3][n2] = buffer.read(8) - 1;
                if (floor1$InfoFloor1.class_subbook[n3][n2] >= -1 && floor1$InfoFloor1.class_subbook[n3][n2] < info.books) continue;
                floor1$InfoFloor1.free();
                return null;
            }
        }
        floor1$InfoFloor1.mult = buffer.read(2) + 1;
        int n6 = buffer.read(4);
        n2 = 0;
        for (n3 = 0; n3 < floor1$InfoFloor1.partitions; ++n3) {
            n4 += floor1$InfoFloor1.class_dim[floor1$InfoFloor1.partitionclass[n3]];
            while (n2 < n4) {
                floor1$InfoFloor1.postlist[n2 + 2] = buffer.read(n6);
                int n7 = floor1$InfoFloor1.postlist[n2 + 2];
                if (n7 < 0 || n7 >= 1 << n6) {
                    floor1$InfoFloor1.free();
                    return null;
                }
                ++n2;
            }
        }
        floor1$InfoFloor1.postlist[0] = 0;
        floor1$InfoFloor1.postlist[1] = 1 << n6;
        return floor1$InfoFloor1;
    }

    Object look(DspState dspState, InfoMode infoMode, Object object) {
        int n2;
        int n3;
        int n4;
        int n5 = 0;
        int[] nArray = new int[65];
        Floor1$InfoFloor1 floor1$InfoFloor1 = (Floor1$InfoFloor1)object;
        Floor1$LookFloor1 floor1$LookFloor1 = new Floor1$LookFloor1(this);
        floor1$LookFloor1.vi = floor1$InfoFloor1;
        floor1$LookFloor1.n = floor1$InfoFloor1.postlist[1];
        for (n4 = 0; n4 < floor1$InfoFloor1.partitions; ++n4) {
            n5 += floor1$InfoFloor1.class_dim[floor1$InfoFloor1.partitionclass[n4]];
        }
        floor1$LookFloor1.posts = n5 += 2;
        for (n4 = 0; n4 < n5; ++n4) {
            nArray[n4] = n4;
        }
        for (n3 = 0; n3 < n5 - 1; ++n3) {
            for (n2 = n3; n2 < n5; ++n2) {
                if (floor1$InfoFloor1.postlist[nArray[n3]] <= floor1$InfoFloor1.postlist[nArray[n2]]) continue;
                n4 = nArray[n2];
                nArray[n2] = nArray[n3];
                nArray[n3] = n4;
            }
        }
        for (n3 = 0; n3 < n5; ++n3) {
            floor1$LookFloor1.forward_index[n3] = nArray[n3];
        }
        for (n3 = 0; n3 < n5; ++n3) {
            floor1$LookFloor1.reverse_index[floor1$LookFloor1.forward_index[n3]] = n3;
        }
        for (n3 = 0; n3 < n5; ++n3) {
            floor1$LookFloor1.sorted_index[n3] = floor1$InfoFloor1.postlist[floor1$LookFloor1.forward_index[n3]];
        }
        switch (floor1$InfoFloor1.mult) {
            case 1: {
                floor1$LookFloor1.quant_q = 256;
                break;
            }
            case 2: {
                floor1$LookFloor1.quant_q = 128;
                break;
            }
            case 3: {
                floor1$LookFloor1.quant_q = 86;
                break;
            }
            case 4: {
                floor1$LookFloor1.quant_q = 64;
                break;
            }
            default: {
                floor1$LookFloor1.quant_q = -1;
            }
        }
        for (n3 = 0; n3 < n5 - 2; ++n3) {
            n2 = 0;
            int n6 = 1;
            int n7 = 0;
            int n8 = floor1$LookFloor1.n;
            int n9 = floor1$InfoFloor1.postlist[n3 + 2];
            for (int i2 = 0; i2 < n3 + 2; ++i2) {
                int n10 = floor1$InfoFloor1.postlist[i2];
                if (n10 > n7 && n10 < n9) {
                    n2 = i2;
                    n7 = n10;
                }
                if (n10 >= n8 || n10 <= n9) continue;
                n6 = i2;
                n8 = n10;
            }
            floor1$LookFloor1.loneighbor[n3] = n2;
            floor1$LookFloor1.hineighbor[n3] = n6;
        }
        return floor1$LookFloor1;
    }

    void free_info(Object object) {
    }

    void free_look(Object object) {
    }

    void free_state(Object object) {
    }

    int forward(Block block, Object object, float[] fArray, float[] fArray2, Object object2) {
        return 0;
    }

    Object inverse1(Block block, Object object, Object object2) {
        Floor1$LookFloor1 floor1$LookFloor1 = (Floor1$LookFloor1)object;
        Floor1$InfoFloor1 floor1$InfoFloor1 = floor1$LookFloor1.vi;
        CodeBook[] codeBookArray = block.vd.fullbooks;
        if (block.opb.read(1) == 1) {
            int n2;
            int n3;
            int n4;
            int n5;
            int n6;
            int[] nArray = null;
            if (object2 instanceof int[]) {
                nArray = (int[])object2;
            }
            if (nArray == null || nArray.length < floor1$LookFloor1.posts) {
                nArray = new int[floor1$LookFloor1.posts];
            } else {
                for (n6 = 0; n6 < nArray.length; ++n6) {
                    nArray[n6] = 0;
                }
            }
            nArray[0] = block.opb.read(Util.ilog(floor1$LookFloor1.quant_q - 1));
            nArray[1] = block.opb.read(Util.ilog(floor1$LookFloor1.quant_q - 1));
            int n7 = 2;
            for (n6 = 0; n6 < floor1$InfoFloor1.partitions; ++n6) {
                n5 = floor1$InfoFloor1.partitionclass[n6];
                n4 = floor1$InfoFloor1.class_dim[n5];
                n3 = floor1$InfoFloor1.class_subs[n5];
                n2 = 1 << n3;
                int n8 = 0;
                if (n3 != 0 && (n8 = codeBookArray[floor1$InfoFloor1.class_book[n5]].decode(block.opb)) == -1) {
                    return null;
                }
                for (int i2 = 0; i2 < n4; ++i2) {
                    int n9 = floor1$InfoFloor1.class_subbook[n5][n8 & n2 - 1];
                    n8 >>>= n3;
                    if (n9 >= 0) {
                        nArray[n7 + i2] = codeBookArray[n9].decode(block.opb);
                        if (nArray[n7 + i2] != -1) continue;
                        return null;
                    }
                    nArray[n7 + i2] = 0;
                }
                n7 += n4;
            }
            for (n6 = 2; n6 < floor1$LookFloor1.posts; ++n6) {
                n7 = Floor1.render_point(floor1$InfoFloor1.postlist[floor1$LookFloor1.loneighbor[n6 - 2]], floor1$InfoFloor1.postlist[floor1$LookFloor1.hineighbor[n6 - 2]], nArray[floor1$LookFloor1.loneighbor[n6 - 2]], nArray[floor1$LookFloor1.hineighbor[n6 - 2]], floor1$InfoFloor1.postlist[n6]);
                n5 = floor1$LookFloor1.quant_q - n7;
                n3 = (n5 < (n4 = n7) ? n5 : n4) << 1;
                n2 = nArray[n6];
                if (n2 != 0) {
                    n2 = n2 >= n3 ? (n5 > n4 ? (n2 -= n4) : -1 - (n2 - n5)) : ((n2 & 1) != 0 ? -(n2 + 1 >>> 1) : (n2 >>= 1));
                    nArray[n6] = n2 + n7;
                    int n10 = floor1$LookFloor1.loneighbor[n6 - 2];
                    nArray[n10] = nArray[n10] & Short.MAX_VALUE;
                    int n11 = floor1$LookFloor1.hineighbor[n6 - 2];
                    nArray[n11] = nArray[n11] & Short.MAX_VALUE;
                    continue;
                }
                nArray[n6] = n7 | 0x8000;
            }
            return nArray;
        }
        return null;
    }

    private static int render_point(int n2, int n3, int n4, int n5, int n6) {
        int n7 = (n5 &= Short.MAX_VALUE) - (n4 &= Short.MAX_VALUE);
        int n8 = n3 - n2;
        int n9 = Math.abs(n7);
        int n10 = n9 * (n6 - n2);
        int n11 = n10 / n8;
        if (n7 < 0) {
            return n4 - n11;
        }
        return n4 + n11;
    }

    int inverse2(Block block, Object object, Object object2, float[] fArray) {
        Floor1$LookFloor1 floor1$LookFloor1 = (Floor1$LookFloor1)object;
        Floor1$InfoFloor1 floor1$InfoFloor1 = floor1$LookFloor1.vi;
        int n2 = block.vd.vi.blocksizes[block.mode] / 2;
        if (object2 != null) {
            int n3;
            int[] nArray = (int[])object2;
            int n4 = 0;
            int n5 = 0;
            int n6 = nArray[0] * floor1$InfoFloor1.mult;
            for (n3 = 1; n3 < floor1$LookFloor1.posts; ++n3) {
                int n7 = floor1$LookFloor1.forward_index[n3];
                int n8 = nArray[n7] & Short.MAX_VALUE;
                if (n8 != nArray[n7]) continue;
                n4 = floor1$InfoFloor1.postlist[n7];
                Floor1.render_line(n5, n4, n6, n8 *= floor1$InfoFloor1.mult, fArray);
                n5 = n4;
                n6 = n8;
            }
            for (n3 = n4; n3 < n2; ++n3) {
                int n9 = n3;
                fArray[n9] = fArray[n9] * fArray[n3 - 1];
            }
            return 1;
        }
        for (int i2 = 0; i2 < n2; ++i2) {
            fArray[i2] = 0.0f;
        }
        return 0;
    }

    private static void render_line(int n2, int n3, int n4, int n5, float[] fArray) {
        int n6 = n5 - n4;
        int n7 = n3 - n2;
        int n8 = Math.abs(n6);
        int n9 = n6 / n7;
        int n10 = n6 < 0 ? n9 - 1 : n9 + 1;
        int n11 = n2;
        int n12 = n4;
        int n13 = 0;
        n8 -= Math.abs(n9 * n7);
        int n14 = n11;
        fArray[n14] = fArray[n14] * FLOOR_fromdB_LOOKUP[n12];
        while (++n11 < n3) {
            if ((n13 += n8) >= n7) {
                n13 -= n7;
                n12 += n10;
            } else {
                n12 += n9;
            }
            int n15 = n11;
            fArray[n15] = fArray[n15] * FLOOR_fromdB_LOOKUP[n12];
        }
    }
}

