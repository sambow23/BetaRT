/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class vu
extends ub {
    private Minecraft a;
    private int b;
    private int c;
    private String d;
    private String e;
    private ny f;
    private long g;
    private bb h;
    private boolean i;

    public vu(Minecraft minecraft) {
        this.a = minecraft;
        this.h = new bb();
    }

    public void a(ny ny2) {
        this.d = do.a("achievement.get");
        this.e = ny2.f;
        this.g = System.currentTimeMillis();
        this.f = ny2;
        this.i = false;
    }

    public void b(ny ny2) {
        this.d = ny2.f;
        this.e = ny2.e();
        this.g = System.currentTimeMillis() - 2500L;
        this.f = ny2;
        this.i = true;
    }

    private void b() {
        GL11.glViewport((int)0, (int)0, (int)this.a.d, (int)this.a.e);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        this.b = this.a.d;
        this.c = this.a.e;
        qq qq2 = new qq(this.a.z, this.a.d, this.a.e);
        this.b = qq2.a();
        this.c = qq2.b();
        GL11.glClear((int)256);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GL11.glOrtho((double)0.0, (double)this.b, (double)this.c, (double)0.0, (double)1000.0, (double)3000.0);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-2000.0f);
    }

    public void a() {
        if (Minecraft.H > 0L) {
            GL11.glDisable((int)2929);
            GL11.glDepthMask((boolean)false);
            u.a();
            this.b();
            String string = "Minecraft Beta 1.7.3   Unlicensed Copy :(";
            String string2 = "(Or logged in from another location)";
            String string3 = "Purchase at minecraft.net";
            this.a.q.a(string, 2, 2, 0xFFFFFF);
            this.a.q.a(string2, 2, 11, 0xFFFFFF);
            this.a.q.a(string3, 2, 20, 0xFFFFFF);
            GL11.glDepthMask((boolean)true);
            GL11.glEnable((int)2929);
        }
        if (this.f == null || this.g == 0L) {
            return;
        }
        double d2 = (double)(System.currentTimeMillis() - this.g) / 3000.0;
        if (!this.i && !this.i && (d2 < 0.0 || d2 > 1.0)) {
            this.g = 0L;
            return;
        }
        this.b();
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        double d3 = d2 * 2.0;
        if (d3 > 1.0) {
            d3 = 2.0 - d3;
        }
        d3 *= 4.0;
        if ((d3 = 1.0 - d3) < 0.0) {
            d3 = 0.0;
        }
        d3 *= d3;
        d3 *= d3;
        int n2 = this.b - 160;
        int n3 = 0 - (int)(d3 * 36.0);
        int n4 = this.a.p.b("/achievement/bg.png");
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glEnable((int)3553);
        GL11.glBindTexture((int)3553, (int)n4);
        GL11.glDisable((int)2896);
        this.b(n2, n3, 96, 202, 160, 32);
        if (this.i) {
            this.a.q.a(this.e, n2 + 30, n3 + 7, 120, -1);
        } else {
            this.a.q.b(this.d, n2 + 30, n3 + 7, -256);
            this.a.q.b(this.e, n2 + 30, n3 + 18, -1);
        }
        GL11.glPushMatrix();
        GL11.glRotatef((float)180.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        u.b();
        GL11.glPopMatrix();
        GL11.glDisable((int)2896);
        GL11.glEnable((int)32826);
        GL11.glEnable((int)2903);
        GL11.glEnable((int)2896);
        this.h.a(this.a.q, this.a.p, this.f.d, n2 + 8, n3 + 8);
        GL11.glDisable((int)2896);
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
    }
}

