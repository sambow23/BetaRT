/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class so
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public int e;
    public int f;
    public int g;
    public int h;
    public int i;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.h = dataInputStream.readByte();
        this.b = dataInputStream.readInt();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
        this.i = dataInputStream.readInt();
        if (this.i > 0) {
            this.e = dataInputStream.readShort();
            this.f = dataInputStream.readShort();
            this.g = dataInputStream.readShort();
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeByte(this.h);
        dataOutputStream.writeInt(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
        dataOutputStream.writeInt(this.i);
        if (this.i > 0) {
            dataOutputStream.writeShort(this.e);
            dataOutputStream.writeShort(this.f);
            dataOutputStream.writeShort(this.g);
        }
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 21 + this.i > 0 ? 6 : 0;
    }
}

