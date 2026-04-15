/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class pe
extends ki {
    public String a;

    public pe() {
    }

    public pe(String string) {
        if (string.length() > 119) {
            string = string.substring(0, 119);
        }
        this.a = string;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = pe.a(dataInputStream, 119);
    }

    public void a(DataOutputStream dataOutputStream) {
        pe.a(this.a, dataOutputStream);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return this.a.length();
    }
}

