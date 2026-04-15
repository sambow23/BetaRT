/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class kb
extends ki {
    public int a;
    public iz[] b;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readByte();
        int n2 = dataInputStream.readShort();
        this.b = new iz[n2];
        for (int i2 = 0; i2 < n2; ++i2) {
            short s2 = dataInputStream.readShort();
            if (s2 < 0) continue;
            byte by2 = dataInputStream.readByte();
            short s3 = dataInputStream.readShort();
            this.b[i2] = new iz(s2, (int)by2, (int)s3);
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeByte(this.a);
        dataOutputStream.writeShort(this.b.length);
        for (int i2 = 0; i2 < this.b.length; ++i2) {
            if (this.b[i2] == null) {
                dataOutputStream.writeShort(-1);
                continue;
            }
            dataOutputStream.writeShort((short)this.b[i2].c);
            dataOutputStream.writeByte((byte)this.b[i2].a);
            dataOutputStream.writeShort((short)this.b[i2].i());
        }
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 3 + this.b.length * 5;
    }
}

