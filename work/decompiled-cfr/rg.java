/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class rg
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public byte e;
    public byte f;

    public rg() {
    }

    public rg(sn sn2) {
        this.a = sn2.aD;
        this.b = in.b(sn2.aM * 32.0);
        this.c = in.b(sn2.aN * 32.0);
        this.d = in.b(sn2.aO * 32.0);
        this.e = (byte)(sn2.aS * 256.0f / 360.0f);
        this.f = (byte)(sn2.aT * 256.0f / 360.0f);
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.readInt();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
        this.e = (byte)dataInputStream.read();
        this.f = (byte)dataInputStream.read();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeInt(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
        dataOutputStream.write(this.e);
        dataOutputStream.write(this.f);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 34;
    }
}

