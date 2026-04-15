/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class gx
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public iz e;

    public gx() {
    }

    public gx(int n2, int n3, int n4, int n5, iz iz2) {
        this.a = n2;
        this.b = n3;
        this.c = n4;
        this.d = n5;
        this.e = iz2;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.read();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.read();
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
        dataOutputStream.writeInt(this.a);
        dataOutputStream.write(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.write(this.d);
        if (this.e == null) {
            dataOutputStream.writeShort(-1);
        } else {
            dataOutputStream.writeShort(this.e.c);
            dataOutputStream.writeByte(this.e.a);
            dataOutputStream.writeShort(this.e.i());
        }
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 15;
    }
}

