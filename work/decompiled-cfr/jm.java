/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

public class jm
extends ki {
    public int a;
    public byte b;
    public int c;
    public int d;
    public int e;
    public byte f;
    public byte g;
    private ud h;
    private List i;

    public jm() {
    }

    public jm(ls ls2) {
        this.a = ls2.aD;
        this.b = (byte)jc.a(ls2);
        this.c = in.b(ls2.aM * 32.0);
        this.d = in.b(ls2.aN * 32.0);
        this.e = in.b(ls2.aO * 32.0);
        this.f = (byte)(ls2.aS * 256.0f / 360.0f);
        this.g = (byte)(ls2.aT * 256.0f / 360.0f);
        this.h = ls2.ad();
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.readByte();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
        this.e = dataInputStream.readInt();
        this.f = dataInputStream.readByte();
        this.g = dataInputStream.readByte();
        this.i = ud.a(dataInputStream);
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeByte(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
        dataOutputStream.writeInt(this.e);
        dataOutputStream.writeByte(this.f);
        dataOutputStream.writeByte(this.g);
        this.h.a(dataOutputStream);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 20;
    }

    public List b() {
        return this.i;
    }
}

