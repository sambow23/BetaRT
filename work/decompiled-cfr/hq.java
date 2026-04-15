/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class hq
extends ki {
    public int a;
    public int b;
    public iz c;

    public void a(ti ti2) {
        ti2.a(this);
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readByte();
        this.b = dataInputStream.readShort();
        short s2 = dataInputStream.readShort();
        if (s2 >= 0) {
            byte by2 = dataInputStream.readByte();
            short s3 = dataInputStream.readShort();
            this.c = new iz(s2, (int)by2, (int)s3);
        } else {
            this.c = null;
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeByte(this.a);
        dataOutputStream.writeShort(this.b);
        if (this.c == null) {
            dataOutputStream.writeShort(-1);
        } else {
            dataOutputStream.writeShort(this.c.c);
            dataOutputStream.writeByte(this.c.a);
            dataOutputStream.writeShort(this.c.i());
        }
    }

    public int a() {
        return 8;
    }
}

