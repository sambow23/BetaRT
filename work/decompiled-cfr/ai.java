/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ai
extends ki {
    public short a;
    public short b;
    public byte[] c;

    public ai() {
        this.k = true;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readShort();
        this.b = dataInputStream.readShort();
        this.c = new byte[dataInputStream.readByte() & 0xFF];
        dataInputStream.readFully(this.c);
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeShort(this.a);
        dataOutputStream.writeShort(this.b);
        dataOutputStream.writeByte(this.c.length);
        dataOutputStream.write(this.c);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 4 + this.c.length;
    }
}

