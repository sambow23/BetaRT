/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class yc
extends da {
    protected String a = "Edit sign message:";
    private yk i;
    private int j;
    private int l = 0;
    private static final String m = fp.a;

    public yc(yk yk2) {
        this.i = yk2;
    }

    public void b() {
        this.e.clear();
        Keyboard.enableRepeatEvents((boolean)true);
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 120, "Done"));
    }

    public void h() {
        Keyboard.enableRepeatEvents((boolean)false);
        if (this.b.f.B) {
            this.b.s().b(new ui(this.i.e, this.i.f, this.i.g, this.i.a));
        }
    }

    public void a() {
        ++this.j;
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == 0) {
            this.i.y_();
            this.b.a((da)null);
        }
    }

    protected void a(char c2, int n2) {
        if (n2 == 200) {
            this.l = this.l - 1 & 3;
        }
        if (n2 == 208 || n2 == 28) {
            this.l = this.l + 1 & 3;
        }
        if (n2 == 14 && this.i.a[this.l].length() > 0) {
            this.i.a[this.l] = this.i.a[this.l].substring(0, this.i.a[this.l].length() - 1);
        }
        if (m.indexOf(c2) >= 0 && this.i.a[this.l].length() < 15) {
            int n3 = this.l;
            this.i.a[n3] = this.i.a[n3] + c2;
        }
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        this.a(this.g, this.a, this.c / 2, 40, 0xFFFFFF);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.c / 2), (float)0.0f, (float)50.0f);
        float f3 = 93.75f;
        GL11.glScalef((float)(-f3), (float)(-f3), (float)(-f3));
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        uu uu2 = this.i.f();
        if (uu2 == uu.aE) {
            float f4 = (float)(this.i.e() * 360) / 16.0f;
            GL11.glRotatef((float)f4, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)-1.0625f, (float)0.0f);
        } else {
            int n4 = this.i.e();
            float f5 = 0.0f;
            if (n4 == 2) {
                f5 = 180.0f;
            }
            if (n4 == 4) {
                f5 = 90.0f;
            }
            if (n4 == 5) {
                f5 = -90.0f;
            }
            GL11.glRotatef((float)f5, (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glTranslatef((float)0.0f, (float)-1.0625f, (float)0.0f);
        }
        if (this.j / 6 % 2 == 0) {
            this.i.b = this.l;
        }
        ll.a.a(this.i, -0.5, -0.75, -0.5, 0.0f);
        this.i.b = -1;
        GL11.glPopMatrix();
        super.a(n2, n3, f2);
    }
}

