/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;

public class Comment {
    private static byte[] _vorbis = "vorbis".getBytes();
    private static byte[] _vendor = "Xiphophorus libVorbis I 20000508".getBytes();
    private static final int OV_EIMPL = -130;
    public byte[][] user_comments;
    public int[] comment_lengths;
    public int comments;
    public byte[] vendor;

    public void init() {
        this.user_comments = null;
        this.comments = 0;
        this.vendor = null;
    }

    public void add(String string) {
        this.add(string.getBytes());
    }

    private void add(byte[] byArray) {
        byte[][] byArrayArray = new byte[this.comments + 2][];
        if (this.user_comments != null) {
            System.arraycopy(this.user_comments, 0, byArrayArray, 0, this.comments);
        }
        this.user_comments = byArrayArray;
        int[] nArray = new int[this.comments + 2];
        if (this.comment_lengths != null) {
            System.arraycopy(this.comment_lengths, 0, nArray, 0, this.comments);
        }
        this.comment_lengths = nArray;
        byte[] byArray2 = new byte[byArray.length + 1];
        System.arraycopy(byArray, 0, byArray2, 0, byArray.length);
        this.user_comments[this.comments] = byArray2;
        this.comment_lengths[this.comments] = byArray.length;
        ++this.comments;
        this.user_comments[this.comments] = null;
    }

    public void add_tag(String string, String string2) {
        if (string2 == null) {
            string2 = "";
        }
        this.add(string + "=" + string2);
    }

    static boolean tagcompare(byte[] byArray, byte[] byArray2, int n2) {
        for (int i2 = 0; i2 < n2; ++i2) {
            byte by2 = byArray[i2];
            byte by3 = byArray2[i2];
            if (90 >= by2 && by2 >= 65) {
                by2 = (byte)(by2 - 65 + 97);
            }
            if (90 >= by3 && by3 >= 65) {
                by3 = (byte)(by3 - 65 + 97);
            }
            if (by2 == by3) continue;
            return false;
        }
        return true;
    }

    public String query(String string) {
        return this.query(string, 0);
    }

    public String query(String string, int n2) {
        int n3 = this.query(string.getBytes(), n2);
        if (n3 == -1) {
            return null;
        }
        byte[] byArray = this.user_comments[n3];
        for (int i2 = 0; i2 < this.comment_lengths[n3]; ++i2) {
            if (byArray[i2] != 61) continue;
            return new String(byArray, i2 + 1, this.comment_lengths[n3] - (i2 + 1));
        }
        return null;
    }

    private int query(byte[] byArray, int n2) {
        int n3 = 0;
        int n4 = 0;
        int n5 = byArray.length + 1;
        byte[] byArray2 = new byte[n5];
        System.arraycopy(byArray, 0, byArray2, 0, byArray.length);
        byArray2[byArray.length] = 61;
        for (n3 = 0; n3 < this.comments; ++n3) {
            if (!Comment.tagcompare(this.user_comments[n3], byArray2, n5)) continue;
            if (n2 == n4) {
                return n3;
            }
            ++n4;
        }
        return -1;
    }

    int unpack(Buffer buffer) {
        int n2 = buffer.read(32);
        if (n2 < 0) {
            this.clear();
            return -1;
        }
        this.vendor = new byte[n2 + 1];
        buffer.read(this.vendor, n2);
        this.comments = buffer.read(32);
        if (this.comments < 0) {
            this.clear();
            return -1;
        }
        this.user_comments = new byte[this.comments + 1][];
        this.comment_lengths = new int[this.comments + 1];
        for (int i2 = 0; i2 < this.comments; ++i2) {
            int n3 = buffer.read(32);
            if (n3 < 0) {
                this.clear();
                return -1;
            }
            this.comment_lengths[i2] = n3;
            this.user_comments[i2] = new byte[n3 + 1];
            buffer.read(this.user_comments[i2], n3);
        }
        if (buffer.read(1) != 1) {
            this.clear();
            return -1;
        }
        return 0;
    }

    int pack(Buffer buffer) {
        buffer.write(3, 8);
        buffer.write(_vorbis);
        buffer.write(_vendor.length, 32);
        buffer.write(_vendor);
        buffer.write(this.comments, 32);
        if (this.comments != 0) {
            for (int i2 = 0; i2 < this.comments; ++i2) {
                if (this.user_comments[i2] != null) {
                    buffer.write(this.comment_lengths[i2], 32);
                    buffer.write(this.user_comments[i2]);
                    continue;
                }
                buffer.write(0, 32);
            }
        }
        buffer.write(1, 1);
        return 0;
    }

    public int header_out(Packet packet) {
        Buffer buffer = new Buffer();
        buffer.writeinit();
        if (this.pack(buffer) != 0) {
            return -130;
        }
        packet.packet_base = new byte[buffer.bytes()];
        packet.packet = 0;
        packet.bytes = buffer.bytes();
        System.arraycopy(buffer.buffer(), 0, packet.packet_base, 0, packet.bytes);
        packet.b_o_s = 0;
        packet.e_o_s = 0;
        packet.granulepos = 0L;
        return 0;
    }

    void clear() {
        for (int i2 = 0; i2 < this.comments; ++i2) {
            this.user_comments[i2] = null;
        }
        this.user_comments = null;
        this.vendor = null;
    }

    public String getVendor() {
        return new String(this.vendor, 0, this.vendor.length - 1);
    }

    public String getComment(int n2) {
        if (this.comments <= n2) {
            return null;
        }
        return new String(this.user_comments[n2], 0, this.user_comments[n2].length - 1);
    }

    public String toString() {
        String string = "Vendor: " + new String(this.vendor, 0, this.vendor.length - 1);
        for (int i2 = 0; i2 < this.comments; ++i2) {
            string = string + "\nComment: " + new String(this.user_comments[i2], 0, this.user_comments[i2].length - 1);
        }
        string = string + "\n";
        return string;
    }
}

