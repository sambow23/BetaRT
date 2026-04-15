/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class wu
extends ki {
    public int a;
    public int b;
    public short[] c;
    public byte[] d;
    public byte[] e;
    public int f;

    public wu() {
        this.k = true;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.readInt();
        this.f = dataInputStream.readShort() & 0xFFFF;
        this.c = new short[this.f];
        this.d = new byte[this.f];
        this.e = new byte[this.f];
        for (int i2 = 0; i2 < this.f; ++i2) {
            this.c[i2] = dataInputStream.readShort();
        }
        dataInputStream.readFully(this.d);
        dataInputStream.readFully(this.e);
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeInt(this.b);
        dataOutputStream.writeShort((short)this.f);
        for (int i2 = 0; i2 < this.f; ++i2) {
            dataOutputStream.writeShort(this.c[i2]);
        }
        dataOutputStream.write(this.d);
        dataOutputStream.write(this.e);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 10 + this.f * 4;
    }
}

