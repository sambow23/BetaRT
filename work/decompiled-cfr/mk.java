/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class mk
implements yb {
    private String a = "";
    private Minecraft b;
    private String c = "";
    private long d = System.currentTimeMillis();
    private boolean e = false;

    public mk(Minecraft minecraft) {
        this.b = minecraft;
    }

    public void a(String string) {
        this.e = false;
        this.c(string);
    }

    public void b(String string) {
        this.e = true;
        this.c(this.c);
    }

    public void c(String string) {
        if (!this.b.J) {
            if (this.e) {
                return;
            }
            throw new xx();
        }
        this.c = string;
        qq qq2 = new qq(this.b.z, this.b.d, this.b.e);
        GL11.glClear((int)256);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)0.0, (double)qq2.a, (double)qq2.b, (double)0.0, (double)100.0, (double)300.0);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-200.0f);
    }

    public void d(String string) {
        if (!this.b.J) {
            if (this.e) {
                return;
            }
            throw new xx();
        }
        this.d = 0L;
        this.a = string;
        this.a(-1);
        this.d = 0L;
    }

    public void a(int n2) {
        if (!this.b.J) {
            if (this.e) {
                return;
            }
            throw new xx();
        }
        long l2 = System.currentTimeMillis();
        if (l2 - this.d < 20L) {
            return;
        }
        this.d = l2;
        qq qq2 = new qq(this.b.z, this.b.d, this.b.e);
        int n3 = qq2.a();
        int n4 = qq2.b();
        GL11.glClear((int)256);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)0.0, (double)qq2.a, (double)qq2.b, (double)0.0, (double)100.0, (double)300.0);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-200.0f);
        GL11.glClear((int)16640);
        nw nw2 = nw.a;
        int n5 = this.b.p.b("/gui/background.png");
        GL11.glBindTexture((int)3553, (int)n5);
        float f2 = 32.0f;
        nw2.b();
        nw2.b(0x404040);
        nw2.a(0.0, n4, 0.0, 0.0, (float)n4 / f2);
        nw2.a(n3, n4, 0.0, (float)n3 / f2, (float)n4 / f2);
        nw2.a(n3, 0.0, 0.0, (float)n3 / f2, 0.0);
        nw2.a(0.0, 0.0, 0.0, 0.0, 0.0);
        nw2.a();
        if (n2 >= 0) {
            int n6 = 100;
            int n7 = 2;
            int n8 = n3 / 2 - n6 / 2;
            int n9 = n4 / 2 + 16;
            GL11.glDisable((int)3553);
            nw2.b();
            nw2.b(0x808080);
            nw2.a((double)n8, (double)n9, 0.0);
            nw2.a((double)n8, (double)(n9 + n7), 0.0);
            nw2.a((double)(n8 + n6), (double)(n9 + n7), 0.0);
            nw2.a((double)(n8 + n6), (double)n9, 0.0);
            nw2.b(0x80FF80);
            nw2.a((double)n8, (double)n9, 0.0);
            nw2.a((double)n8, (double)(n9 + n7), 0.0);
            nw2.a((double)(n8 + n2), (double)(n9 + n7), 0.0);
            nw2.a((double)(n8 + n2), (double)n9, 0.0);
            nw2.a();
            GL11.glEnable((int)3553);
        }
        this.b.q.a(this.c, (n3 - this.b.q.a(this.c)) / 2, n4 / 2 - 4 - 16, 0xFFFFFF);
        this.b.q.a(this.a, (n3 - this.b.q.a(this.a)) / 2, n4 / 2 - 4 + 8, 0xFFFFFF);
        Display.update();
        try {
            Thread.yield();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

