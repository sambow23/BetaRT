/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class nm
extends ki {
    public int a;
    public int b;

    public nm() {
    }

    public nm(sn sn2, int n2) {
        this.a = sn2.aD;
        this.b = n2;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.readByte();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeByte(this.b);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 5;
    }
}

