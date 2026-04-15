/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class nz
extends ki {
    public int a;
    public String b;
    public long c;
    public byte d;

    public nz() {
    }

    public nz(String string, int n2) {
        this.b = string;
        this.a = n2;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = nz.a(dataInputStream, 16);
        this.c = dataInputStream.readLong();
        this.d = dataInputStream.readByte();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        nz.a(this.b, dataOutputStream);
        dataOutputStream.writeLong(this.c);
        dataOutputStream.writeByte(this.d);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 4 + this.b.length() + 4 + 5;
    }
}

