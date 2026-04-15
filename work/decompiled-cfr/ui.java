/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ui
extends ki {
    public int a;
    public int b;
    public int c;
    public String[] d;

    public ui() {
        this.k = true;
    }

    public ui(int n2, int n3, int n4, String[] stringArray) {
        this.k = true;
        this.a = n2;
        this.b = n3;
        this.c = n4;
        this.d = stringArray;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.readShort();
        this.c = dataInputStream.readInt();
        this.d = new String[4];
        for (int i2 = 0; i2 < 4; ++i2) {
            this.d[i2] = ui.a(dataInputStream, 15);
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeShort(this.b);
        dataOutputStream.writeInt(this.c);
        for (int i2 = 0; i2 < 4; ++i2) {
            ui.a(this.d[i2], dataOutputStream);
        }
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        int n2 = 0;
        for (int i2 = 0; i2 < 4; ++i2) {
            n2 += this.d[i2].length();
        }
        return n2;
    }
}

