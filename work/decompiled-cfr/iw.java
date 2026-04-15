/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class iw
extends ki {
    public int a;
    public int b;
    public String c;
    public int d;

    public void a(ti ti2) {
        ti2.a(this);
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readByte();
        this.b = dataInputStream.readByte();
        this.c = dataInputStream.readUTF();
        this.d = dataInputStream.readByte();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeByte(this.a);
        dataOutputStream.writeByte(this.b);
        dataOutputStream.writeUTF(this.c);
        dataOutputStream.writeByte(this.d);
    }

    public int a() {
        return 3 + this.c.length();
    }
}

