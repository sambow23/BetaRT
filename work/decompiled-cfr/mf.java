/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class mf
extends ki {
    public int a;
    public String b;
    public int c;
    public int d;
    public int e;
    public byte f;
    public byte g;
    public int h;

    public mf() {
    }

    public mf(gs gs2) {
        this.a = gs2.aD;
        this.b = gs2.l;
        this.c = in.b(gs2.aM * 32.0);
        this.d = in.b(gs2.aN * 32.0);
        this.e = in.b(gs2.aO * 32.0);
        this.f = (byte)(gs2.aS * 256.0f / 360.0f);
        this.g = (byte)(gs2.aT * 256.0f / 360.0f);
        iz iz2 = gs2.c.b();
        this.h = iz2 == null ? 0 : iz2.c;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = mf.a(dataInputStream, 16);
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
        this.e = dataInputStream.readInt();
        this.f = dataInputStream.readByte();
        this.g = dataInputStream.readByte();
        this.h = dataInputStream.readShort();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        mf.a(this.b, dataOutputStream);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
        dataOutputStream.writeInt(this.e);
        dataOutputStream.writeByte(this.f);
        dataOutputStream.writeByte(this.g);
        dataOutputStream.writeShort(this.h);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 28;
    }
}

