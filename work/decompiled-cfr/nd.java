/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class nd
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public byte e;
    public byte f;
    public byte g;
    public int h;
    public int i;
    public int l;

    public nd() {
    }

    public nd(hl hl2) {
        this.a = hl2.aD;
        this.h = hl2.a.c;
        this.i = hl2.a.a;
        this.l = hl2.a.i();
        this.b = in.b(hl2.aM * 32.0);
        this.c = in.b(hl2.aN * 32.0);
        this.d = in.b(hl2.aO * 32.0);
        this.e = (byte)(hl2.aP * 128.0);
        this.f = (byte)(hl2.aQ * 128.0);
        this.g = (byte)(hl2.aR * 128.0);
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.h = dataInputStream.readShort();
        this.i = dataInputStream.readByte();
        this.l = dataInputStream.readShort();
        this.b = dataInputStream.readInt();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
        this.e = dataInputStream.readByte();
        this.f = dataInputStream.readByte();
        this.g = dataInputStream.readByte();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeShort(this.h);
        dataOutputStream.writeByte(this.i);
        dataOutputStream.writeShort(this.l);
        dataOutputStream.writeInt(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
        dataOutputStream.writeByte(this.e);
        dataOutputStream.writeByte(this.f);
        dataOutputStream.writeByte(this.g);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 24;
    }
}

