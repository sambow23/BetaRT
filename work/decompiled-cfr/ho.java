/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ho
extends ki {
    public int a;

    public ho() {
    }

    public ho(int n2) {
        this.a = n2;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readShort();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeShort(this.a);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 2;
    }
}

