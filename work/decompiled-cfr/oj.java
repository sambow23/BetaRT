/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class oj
extends ki {
    public int a;
    public short b;
    public boolean c;

    public oj() {
    }

    public oj(int n2, short s2, boolean bl2) {
        this.a = n2;
        this.b = s2;
        this.c = bl2;
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readByte();
        this.b = dataInputStream.readShort();
        this.c = dataInputStream.readByte() != 0;
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeByte(this.a);
        dataOutputStream.writeShort(this.b);
        dataOutputStream.writeByte(this.c ? 1 : 0);
    }

    public int a() {
        return 4;
    }
}

