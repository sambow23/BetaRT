/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ca
extends ki {
    public static final String[] a = new String[]{"tile.bed.notValid", null, null};
    public int b;

    public void a(DataInputStream dataInputStream) {
        this.b = dataInputStream.readByte();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeByte(this.b);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 1;
    }
}

