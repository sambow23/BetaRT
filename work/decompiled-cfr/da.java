/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class da
extends ub {
    protected Minecraft b;
    public int c;
    public int d;
    protected List e = new ArrayList();
    public boolean f = false;
    protected sj g;
    public du h;
    private ke a = null;

    public void a(int n2, int n3, float f2) {
        for (int i2 = 0; i2 < this.e.size(); ++i2) {
            ke ke2 = (ke)this.e.get(i2);
            ke2.a(this.b, n2, n3);
        }
    }

    protected void a(char c2, int n2) {
        if (n2 == 1) {
            this.b.a((da)null);
            this.b.g();
        }
    }

    public static String d() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String string = (String)transferable.getTransferData(DataFlavor.stringFlavor);
                return string;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    protected void a(int n2, int n3, int n4) {
        if (n4 == 0) {
            for (int i2 = 0; i2 < this.e.size(); ++i2) {
                ke ke2 = (ke)this.e.get(i2);
                if (!ke2.c(this.b, n2, n3)) continue;
                this.a = ke2;
                this.b.B.a("random.click", 1.0f, 1.0f);
                this.a(ke2);
            }
        }
    }

    protected void b(int n2, int n3, int n4) {
        if (this.a != null && n4 == 0) {
            this.a.a(n2, n3);
            this.a = null;
        }
    }

    protected void a(ke ke2) {
    }

    public void a(Minecraft minecraft, int n2, int n3) {
        this.h = new du(minecraft);
        this.b = minecraft;
        this.g = minecraft.q;
        this.c = n2;
        this.d = n3;
        this.e.clear();
        this.b();
    }

    public void b() {
    }

    public void e() {
        while (Mouse.next()) {
            this.f();
        }
        while (Keyboard.next()) {
            this.g();
        }
    }

    public void f() {
        if (Mouse.getEventButtonState()) {
            int n2 = Mouse.getEventX() * this.c / this.b.d;
            int n3 = this.d - Mouse.getEventY() * this.d / this.b.e - 1;
            this.a(n2, n3, Mouse.getEventButton());
        } else {
            int n4 = Mouse.getEventX() * this.c / this.b.d;
            int n5 = this.d - Mouse.getEventY() * this.d / this.b.e - 1;
            this.b(n4, n5, Mouse.getEventButton());
        }
    }

    public void g() {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == 87) {
                this.b.j();
                return;
            }
            this.a(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }
    }

    public void a() {
    }

    public void h() {
    }

    public void i() {
        this.a(0);
    }

    public void a(int n2) {
        if (this.b.f != null) {
            this.a(0, 0, this.c, this.d, -1072689136, -804253680);
        } else {
            this.b(n2);
        }
    }

    public void b(int n2) {
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2912);
        nw nw2 = nw.a;
        GL11.glBindTexture((int)3553, (int)this.b.p.b("/gui/background.png"));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        float f2 = 32.0f;
        nw2.b();
        nw2.b(0x404040);
        nw2.a(0.0, this.d, 0.0, 0.0, (float)this.d / f2 + (float)n2);
        nw2.a(this.c, this.d, 0.0, (float)this.c / f2, (float)this.d / f2 + (float)n2);
        nw2.a(this.c, 0.0, 0.0, (float)this.c / f2, 0 + n2);
        nw2.a(0.0, 0.0, 0.0, 0.0, 0 + n2);
        nw2.a();
    }

    public boolean c() {
        return true;
    }

    public void a(boolean bl2, int n2) {
    }

    public void j() {
    }
}

