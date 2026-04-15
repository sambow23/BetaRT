/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jogg;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;

public class StreamState {
    byte[] body_data;
    int body_storage;
    int body_fill;
    private int body_returned;
    int[] lacing_vals;
    long[] granule_vals;
    int lacing_storage;
    int lacing_fill;
    int lacing_packet;
    int lacing_returned;
    byte[] header = new byte[282];
    int header_fill;
    public int e_o_s;
    int b_o_s;
    int serialno;
    int pageno;
    long packetno;
    long granulepos;

    public StreamState() {
        this.init();
    }

    StreamState(int n2) {
        this();
        this.init(n2);
    }

    void init() {
        this.body_storage = 16384;
        this.body_data = new byte[this.body_storage];
        this.lacing_storage = 1024;
        this.lacing_vals = new int[this.lacing_storage];
        this.granule_vals = new long[this.lacing_storage];
    }

    public void init(int n2) {
        if (this.body_data == null) {
            this.init();
        } else {
            int n3;
            for (n3 = 0; n3 < this.body_data.length; ++n3) {
                this.body_data[n3] = 0;
            }
            for (n3 = 0; n3 < this.lacing_vals.length; ++n3) {
                this.lacing_vals[n3] = 0;
            }
            for (n3 = 0; n3 < this.granule_vals.length; ++n3) {
                this.granule_vals[n3] = 0L;
            }
        }
        this.serialno = n2;
    }

    public void clear() {
        this.body_data = null;
        this.lacing_vals = null;
        this.granule_vals = null;
    }

    void destroy() {
        this.clear();
    }

    void body_expand(int n2) {
        if (this.body_storage <= this.body_fill + n2) {
            this.body_storage += n2 + 1024;
            byte[] byArray = new byte[this.body_storage];
            System.arraycopy(this.body_data, 0, byArray, 0, this.body_data.length);
            this.body_data = byArray;
        }
    }

    void lacing_expand(int n2) {
        if (this.lacing_storage <= this.lacing_fill + n2) {
            this.lacing_storage += n2 + 32;
            int[] nArray = new int[this.lacing_storage];
            System.arraycopy(this.lacing_vals, 0, nArray, 0, this.lacing_vals.length);
            this.lacing_vals = nArray;
            long[] lArray = new long[this.lacing_storage];
            System.arraycopy(this.granule_vals, 0, lArray, 0, this.granule_vals.length);
            this.granule_vals = lArray;
        }
    }

    public int packetin(Packet packet) {
        int n2 = packet.bytes / 255 + 1;
        if (this.body_returned != 0) {
            this.body_fill -= this.body_returned;
            if (this.body_fill != 0) {
                System.arraycopy(this.body_data, this.body_returned, this.body_data, 0, this.body_fill);
            }
            this.body_returned = 0;
        }
        this.body_expand(packet.bytes);
        this.lacing_expand(n2);
        System.arraycopy(packet.packet_base, packet.packet, this.body_data, this.body_fill, packet.bytes);
        this.body_fill += packet.bytes;
        for (int i2 = 0; i2 < n2 - 1; ++i2) {
            this.lacing_vals[this.lacing_fill + i2] = 255;
            this.granule_vals[this.lacing_fill + i2] = this.granulepos;
        }
        this.lacing_vals[this.lacing_fill + i2] = packet.bytes % 255;
        long l2 = packet.granulepos;
        this.granule_vals[this.lacing_fill + i2] = l2;
        this.granulepos = l2;
        int n3 = this.lacing_fill;
        this.lacing_vals[n3] = this.lacing_vals[n3] | 0x100;
        this.lacing_fill += n2;
        ++this.packetno;
        if (packet.e_o_s != 0) {
            this.e_o_s = 1;
        }
        return 0;
    }

    public int packetout(Packet packet) {
        int n2;
        if (this.lacing_packet <= (n2 = this.lacing_returned++)) {
            return 0;
        }
        if ((this.lacing_vals[n2] & 0x400) != 0) {
            ++this.packetno;
            return -1;
        }
        int n3 = this.lacing_vals[n2] & 0xFF;
        int n4 = 0;
        packet.packet_base = this.body_data;
        packet.packet = this.body_returned;
        packet.e_o_s = this.lacing_vals[n2] & 0x200;
        packet.b_o_s = this.lacing_vals[n2] & 0x100;
        n4 += n3;
        while (n3 == 255) {
            int n5 = this.lacing_vals[++n2];
            n3 = n5 & 0xFF;
            if ((n5 & 0x200) != 0) {
                packet.e_o_s = 512;
            }
            n4 += n3;
        }
        packet.packetno = this.packetno++;
        packet.granulepos = this.granule_vals[n2];
        packet.bytes = n4;
        this.body_returned += n4;
        this.lacing_returned = n2 + 1;
        return 1;
    }

    public int pagein(Page page) {
        byte[] byArray = page.header_base;
        int n2 = page.header;
        byte[] byArray2 = page.body_base;
        int n3 = page.body;
        int n4 = page.body_len;
        int n5 = 0;
        int n6 = page.version();
        int n7 = page.continued();
        int n8 = page.bos();
        int n9 = page.eos();
        long l2 = page.granulepos();
        int n10 = page.serialno();
        int n11 = page.pageno();
        int n12 = byArray[n2 + 26] & 0xFF;
        int n13 = this.lacing_returned;
        int n14 = this.body_returned;
        if (n14 != 0) {
            this.body_fill -= n14;
            if (this.body_fill != 0) {
                System.arraycopy(this.body_data, n14, this.body_data, 0, this.body_fill);
            }
            this.body_returned = 0;
        }
        if (n13 != 0) {
            if (this.lacing_fill - n13 != 0) {
                System.arraycopy(this.lacing_vals, n13, this.lacing_vals, 0, this.lacing_fill - n13);
                System.arraycopy(this.granule_vals, n13, this.granule_vals, 0, this.lacing_fill - n13);
            }
            this.lacing_fill -= n13;
            this.lacing_packet -= n13;
            this.lacing_returned = 0;
        }
        if (n10 != this.serialno) {
            return -1;
        }
        if (n6 > 0) {
            return -1;
        }
        this.lacing_expand(n12 + 1);
        if (n11 != this.pageno) {
            for (n13 = this.lacing_packet; n13 < this.lacing_fill; ++n13) {
                this.body_fill -= this.lacing_vals[n13] & 0xFF;
            }
            this.lacing_fill = this.lacing_packet++;
            if (this.pageno != -1) {
                this.lacing_vals[this.lacing_fill++] = 1024;
            }
            if (n7 != 0) {
                n8 = 0;
                while (n5 < n12) {
                    n14 = byArray[n2 + 27 + n5] & 0xFF;
                    n3 += n14;
                    n4 -= n14;
                    if (n14 < 255) {
                        ++n5;
                        break;
                    }
                    ++n5;
                }
            }
        }
        if (n4 != 0) {
            this.body_expand(n4);
            System.arraycopy(byArray2, n3, this.body_data, this.body_fill, n4);
            this.body_fill += n4;
        }
        n13 = -1;
        while (n5 < n12) {
            this.lacing_vals[this.lacing_fill] = n14 = byArray[n2 + 27 + n5] & 0xFF;
            this.granule_vals[this.lacing_fill] = -1L;
            if (n8 != 0) {
                int n15 = this.lacing_fill;
                this.lacing_vals[n15] = this.lacing_vals[n15] | 0x100;
                n8 = 0;
            }
            if (n14 < 255) {
                n13 = this.lacing_fill;
            }
            ++this.lacing_fill;
            ++n5;
            if (n14 >= 255) continue;
            this.lacing_packet = this.lacing_fill;
        }
        if (n13 != -1) {
            this.granule_vals[n13] = l2;
        }
        if (n9 != 0) {
            this.e_o_s = 1;
            if (this.lacing_fill > 0) {
                int n16 = this.lacing_fill - 1;
                this.lacing_vals[n16] = this.lacing_vals[n16] | 0x200;
            }
        }
        this.pageno = n11 + 1;
        return 0;
    }

    public int flush(Page page) {
        int n2;
        int n3 = 0;
        int n4 = this.lacing_fill > 255 ? 255 : this.lacing_fill;
        int n5 = 0;
        int n6 = 0;
        long l2 = this.granule_vals[0];
        if (n4 == 0) {
            return 0;
        }
        if (this.b_o_s == 0) {
            l2 = 0L;
            for (n3 = 0; n3 < n4; ++n3) {
                if ((this.lacing_vals[n3] & 0xFF) >= 255) continue;
                ++n3;
                break;
            }
        } else {
            for (n3 = 0; n3 < n4 && n6 <= 4096; n6 += this.lacing_vals[n3] & 0xFF, ++n3) {
                l2 = this.granule_vals[n3];
            }
        }
        System.arraycopy("OggS".getBytes(), 0, this.header, 0, 4);
        this.header[4] = 0;
        this.header[5] = 0;
        if ((this.lacing_vals[0] & 0x100) == 0) {
            this.header[5] = (byte)(this.header[5] | 1);
        }
        if (this.b_o_s == 0) {
            this.header[5] = (byte)(this.header[5] | 2);
        }
        if (this.e_o_s != 0 && this.lacing_fill == n3) {
            this.header[5] = (byte)(this.header[5] | 4);
        }
        this.b_o_s = 1;
        for (n2 = 6; n2 < 14; ++n2) {
            this.header[n2] = (byte)l2;
            l2 >>>= 8;
        }
        int n7 = this.serialno;
        for (n2 = 14; n2 < 18; ++n2) {
            this.header[n2] = (byte)n7;
            n7 >>>= 8;
        }
        if (this.pageno == -1) {
            this.pageno = 0;
        }
        n7 = this.pageno++;
        for (n2 = 18; n2 < 22; ++n2) {
            this.header[n2] = (byte)n7;
            n7 >>>= 8;
        }
        this.header[22] = 0;
        this.header[23] = 0;
        this.header[24] = 0;
        this.header[25] = 0;
        this.header[26] = (byte)n3;
        for (n2 = 0; n2 < n3; ++n2) {
            this.header[n2 + 27] = (byte)this.lacing_vals[n2];
            n5 += this.header[n2 + 27] & 0xFF;
        }
        page.header_base = this.header;
        page.header = 0;
        page.header_len = this.header_fill = n3 + 27;
        page.body_base = this.body_data;
        page.body = this.body_returned;
        page.body_len = n5;
        this.lacing_fill -= n3;
        System.arraycopy(this.lacing_vals, n3, this.lacing_vals, 0, this.lacing_fill * 4);
        System.arraycopy(this.granule_vals, n3, this.granule_vals, 0, this.lacing_fill * 8);
        this.body_returned += n5;
        page.checksum();
        return 1;
    }

    public int pageout(Page page) {
        if (this.e_o_s != 0 && this.lacing_fill != 0 || this.body_fill - this.body_returned > 4096 || this.lacing_fill >= 255 || this.lacing_fill != 0 && this.b_o_s == 0) {
            return this.flush(page);
        }
        return 0;
    }

    public int eof() {
        return this.e_o_s;
    }

    public int reset() {
        this.body_fill = 0;
        this.body_returned = 0;
        this.lacing_fill = 0;
        this.lacing_packet = 0;
        this.lacing_returned = 0;
        this.header_fill = 0;
        this.e_o_s = 0;
        this.b_o_s = 0;
        this.pageno = -1;
        this.packetno = 0L;
        this.granulepos = 0L;
        return 0;
    }
}

