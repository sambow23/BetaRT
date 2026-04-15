/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class qs
extends ki {
    public int a;
    public int b;
    public int c;
    public short d;
    public iz e;
    public boolean f;

    public qs() {
    }

    public qs(int n2, int n3, int n4, boolean bl2, iz iz2, short s2) {
        this.a = n2;
        this.b = n3;
        this.c = n4;
        this.e = iz2;
        this.d = s2;
        this.f = bl2;
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readByte();
        this.b = dataInputStream.readShort();
        this.c = dataInputStream.readByte();
        this.d = dataInputStream.readShort();
        this.f = dataInputStream.readBoolean();
        short s2 = dataInputStream.readShort();
        if (s2 >= 0) {
            byte by2 = dataInputStream.readByte();
            short s3 = dataInputStream.readShort();
            this.e = new iz(s2, (int)by2, (int)s3);
        } else {
            this.e = null;
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeByte(this.a);
        dataOutputStream.writeShort(this.b);
        dataOutputStream.writeByte(this.c);
        dataOutputStream.writeShort(this.d);
        dataOutputStream.writeBoolean(this.f);
        if (this.e == null) {
            dataOutputStream.writeShort(-1);
        } else {
            dataOutputStream.writeShort(this.e.c);
            dataOutputStream.writeByte(this.e.a);
            dataOutputStream.writeShort(this.e.i());
        }
    }

    public int a() {
        return 11;
    }
}

