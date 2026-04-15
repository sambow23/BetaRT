/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

public class ux
extends ki {
    public int a;
    private List b;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = ud.a(dataInputStream);
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        ud.a(this.b, dataOutputStream);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 5;
    }

    public List b() {
        return this.b;
    }
}

