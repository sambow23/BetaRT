/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jogg;

import com.jcraft.jogg.Page;

public class SyncState {
    public byte[] data;
    int storage;
    int fill;
    int returned;
    int unsynced;
    int headerbytes;
    int bodybytes;
    private Page pageseek = new Page();
    private byte[] chksum = new byte[4];

    public int clear() {
        this.data = null;
        return 0;
    }

    public int buffer(int n2) {
        if (this.returned != 0) {
            this.fill -= this.returned;
            if (this.fill > 0) {
                System.arraycopy(this.data, this.returned, this.data, 0, this.fill);
            }
            this.returned = 0;
        }
        if (n2 > this.storage - this.fill) {
            int n3 = n2 + this.fill + 4096;
            if (this.data != null) {
                byte[] byArray = new byte[n3];
                System.arraycopy(this.data, 0, byArray, 0, this.data.length);
                this.data = byArray;
            } else {
                this.data = new byte[n3];
            }
            this.storage = n3;
        }
        return this.fill;
    }

    public int wrote(int n2) {
        if (this.fill + n2 > this.storage) {
            return -1;
        }
        this.fill += n2;
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int pageseek(Page page) {
        int n2 = this.returned;
        int n3 = this.fill - this.returned;
        if (this.headerbytes == 0) {
            if (n3 < 27) {
                return 0;
            }
            if (this.data[n2] != 79 || this.data[n2 + 1] != 103 || this.data[n2 + 2] != 103 || this.data[n2 + 3] != 83) {
                this.headerbytes = 0;
                this.bodybytes = 0;
                int n4 = 0;
                for (int i2 = 0; i2 < n3 - 1; ++i2) {
                    if (this.data[n2 + 1 + i2] != 79) continue;
                    n4 = n2 + 1 + i2;
                    break;
                }
                if (n4 == 0) {
                    n4 = this.fill;
                }
                this.returned = n4;
                return -(n4 - n2);
            }
            int n5 = (this.data[n2 + 26] & 0xFF) + 27;
            if (n3 < n5) {
                return 0;
            }
            for (int i3 = 0; i3 < (this.data[n2 + 26] & 0xFF); ++i3) {
                this.bodybytes += this.data[n2 + 27 + i3] & 0xFF;
            }
            this.headerbytes = n5;
        }
        if (this.bodybytes + this.headerbytes > n3) {
            return 0;
        }
        byte[] byArray = this.chksum;
        synchronized (this.chksum) {
            System.arraycopy(this.data, n2 + 22, this.chksum, 0, 4);
            this.data[n2 + 22] = 0;
            this.data[n2 + 23] = 0;
            this.data[n2 + 24] = 0;
            this.data[n2 + 25] = 0;
            Page page2 = this.pageseek;
            page2.header_base = this.data;
            page2.header = n2;
            page2.header_len = this.headerbytes;
            page2.body_base = this.data;
            page2.body = n2 + this.headerbytes;
            page2.body_len = this.bodybytes;
            page2.checksum();
            if (this.chksum[0] != this.data[n2 + 22] || this.chksum[1] != this.data[n2 + 23] || this.chksum[2] != this.data[n2 + 24] || this.chksum[3] != this.data[n2 + 25]) {
                System.arraycopy(this.chksum, 0, this.data, n2 + 22, 4);
                this.headerbytes = 0;
                this.bodybytes = 0;
                int n6 = 0;
                for (int i4 = 0; i4 < n3 - 1; ++i4) {
                    if (this.data[n2 + 1 + i4] != 79) continue;
                    n6 = n2 + 1 + i4;
                    break;
                }
                if (n6 == 0) {
                    n6 = this.fill;
                }
                this.returned = n6;
                // ** MonitorExit[var5_9] (shouldn't be in output)
                return -(n6 - n2);
            }
            // ** MonitorExit[var5_9] (shouldn't be in output)
            n2 = this.returned;
            if (page != null) {
                page.header_base = this.data;
                page.header = n2;
                page.header_len = this.headerbytes;
                page.body_base = this.data;
                page.body = n2 + this.headerbytes;
                page.body_len = this.bodybytes;
            }
            this.unsynced = 0;
            n3 = this.headerbytes + this.bodybytes;
            this.returned += n3;
            this.headerbytes = 0;
            this.bodybytes = 0;
            return n3;
        }
    }

    public int pageout(Page page) {
        do {
            int n2;
            if ((n2 = this.pageseek(page)) > 0) {
                return 1;
            }
            if (n2 != 0) continue;
            return 0;
        } while (this.unsynced != 0);
        this.unsynced = 1;
        return -1;
    }

    public int reset() {
        this.fill = 0;
        this.returned = 0;
        this.unsynced = 0;
        this.headerbytes = 0;
        this.bodybytes = 0;
        return 0;
    }

    public void init() {
    }

    public int getDataOffset() {
        return this.returned;
    }

    public int getBufferOffset() {
        return this.fill;
    }
}

