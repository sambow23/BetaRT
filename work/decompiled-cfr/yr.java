/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class yr
extends ki {
    public String a;

    public yr() {
    }

    public yr(String string) {
        this.a = string;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = yr.a(dataInputStream, 100);
    }

    public void a(DataOutputStream dataOutputStream) {
        yr.a(this.a, dataOutputStream);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return this.a.length();
    }
}

