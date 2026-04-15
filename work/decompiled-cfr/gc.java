/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import org.lwjgl.input.Keyboard;

public class gc
extends da {
    protected String a = "";
    private int i = 0;
    private static final String j = fp.a;

    public void b() {
        Keyboard.enableRepeatEvents((boolean)true);
    }

    public void h() {
        Keyboard.enableRepeatEvents((boolean)false);
    }

    public void a() {
        ++this.i;
    }

    protected void a(char c2, int n2) {
        if (n2 == 1) {
            this.b.a((da)null);
            return;
        }
        if (n2 == 28) {
            String string;
            String string2 = this.a.trim();
            if (string2.length() > 0 && !this.b.b(string = this.a.trim())) {
                this.b.h.a(string);
            }
            this.b.a((da)null);
            return;
        }
        if (n2 == 14 && this.a.length() > 0) {
            this.a = this.a.substring(0, this.a.length() - 1);
        }
        if (j.indexOf(c2) >= 0 && this.a.length() < 100) {
            this.a = this.a + c2;
        }
    }

    public void a(int n2, int n3, float f2) {
        this.a(2, this.d - 14, this.c - 2, this.d - 2, Integer.MIN_VALUE);
        this.b(this.g, "> " + this.a + (this.i / 6 % 2 == 0 ? "_" : ""), 4, this.d - 12, 0xE0E0E0);
        super.a(n2, n3, f2);
    }

    protected void a(int n2, int n3, int n4) {
        if (n4 == 0) {
            if (this.b.v.a != null) {
                if (this.a.length() > 0 && !this.a.endsWith(" ")) {
                    this.a = this.a + " ";
                }
                this.a = this.a + this.b.v.a;
                int n5 = 100;
                if (this.a.length() > n5) {
                    this.a = this.a.substring(0, n5);
                }
            } else {
                super.a(n2, n3, n4);
            }
        }
    }
}

