/*
 * Decompiled with CFR 0.152.
 */
import java.io.PushbackReader;
import java.io.Reader;

final class lj
implements et {
    private final PushbackReader a;
    private int b = 0;
    private int c = 1;
    private boolean d = false;

    public lj(Reader reader) {
        this.a = new PushbackReader(reader);
    }

    public void a(char c2) {
        --this.b;
        if (this.b < 0) {
            this.b = 0;
        }
        this.a.unread(c2);
    }

    public void a(char[] cArray) {
        this.b -= cArray.length;
        if (this.b < 0) {
            this.b = 0;
        }
    }

    public int c() {
        int n2 = this.a.read();
        this.a(n2);
        return n2;
    }

    public int b(char[] cArray) {
        int n2 = this.a.read(cArray);
        for (char c2 : cArray) {
            this.a((int)c2);
        }
        return n2;
    }

    private void a(int n2) {
        if (13 == n2) {
            this.b = 0;
            ++this.c;
            this.d = true;
        } else {
            if (10 == n2 && !this.d) {
                this.b = 0;
                ++this.c;
            } else {
                ++this.b;
            }
            this.d = false;
        }
    }

    public int a() {
        return this.b;
    }

    public int b() {
        return this.c;
    }
}

