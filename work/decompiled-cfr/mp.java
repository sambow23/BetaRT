/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class mp
extends ki {
    public String a;

    public mp() {
    }

    public mp(String string) {
        this.a = string;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = mp.a(dataInputStream, 32);
    }

    public void a(DataOutputStream dataOutputStream) {
        mp.a(this.a, dataOutputStream);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 4 + this.a.length() + 4;
    }
}

