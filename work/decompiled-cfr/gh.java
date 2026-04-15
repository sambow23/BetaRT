/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import org.lwjgl.input.Keyboard;

public class gh
extends gc {
    public void b() {
        Keyboard.enableRepeatEvents((boolean)true);
        nh nh2 = nh.a();
        this.e.add(new ke(1, this.c / 2 - 100, this.d - 40, nh2.a("multiplayer.stopSleeping")));
    }

    public void h() {
        Keyboard.enableRepeatEvents((boolean)false);
    }

    protected void a(char c2, int n2) {
        if (n2 == 1) {
            this.k();
        } else if (n2 == 28) {
            String string = this.a.trim();
            if (string.length() > 0) {
                this.b.h.a(this.a.trim());
            }
            this.a = "";
        } else {
            super.a(c2, n2);
        }
    }

    public void a(int n2, int n3, float f2) {
        super.a(n2, n3, f2);
    }

    protected void a(ke ke2) {
        if (ke2.f == 1) {
            this.k();
        } else {
            super.a(ke2);
        }
    }

    private void k() {
        if (this.b.h instanceof tk) {
            nb nb2 = ((tk)this.b.h).bN;
            nb2.b(new ts(this.b.h, 3));
        }
    }
}

