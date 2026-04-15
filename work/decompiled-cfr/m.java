/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class m
extends gv {
    private ko a = new il(2.0f);

    public m() {
        super(new il(), 0.5f);
    }

    protected void a(gb gb2, float f2) {
        gb gb3 = gb2;
        float f3 = gb3.a_(f2);
        float f4 = 1.0f + in.a(f3 * 100.0f) * f3 * 0.01f;
        if (f3 < 0.0f) {
            f3 = 0.0f;
        }
        if (f3 > 1.0f) {
            f3 = 1.0f;
        }
        f3 *= f3;
        f3 *= f3;
        float f5 = (1.0f + f3 * 0.4f) * f4;
        float f6 = (1.0f + f3 * 0.1f) / f4;
        GL11.glScalef((float)f5, (float)f6, (float)f5);
    }

    protected int a(gb gb2, float f2, float f3) {
        gb gb3 = gb2;
        float f4 = gb3.a_(f3);
        if ((int)(f4 * 10.0f) % 2 == 0) {
            return 0;
        }
        int n2 = (int)(f4 * 0.2f * 255.0f);
        if (n2 < 0) {
            n2 = 0;
        }
        if (n2 > 255) {
            n2 = 255;
        }
        int n3 = 255;
        int n4 = 255;
        int n5 = 255;
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    protected boolean a(gb gb2, int n2, float f2) {
        if (gb2.s()) {
            if (n2 == 1) {
                float f3 = (float)gb2.bt + f2;
                this.a("/armor/power.png");
                GL11.glMatrixMode((int)5890);
                GL11.glLoadIdentity();
                float f4 = f3 * 0.01f;
                float f5 = f3 * 0.01f;
                GL11.glTranslatef((float)f4, (float)f5, (float)0.0f);
                this.a(this.a);
                GL11.glMatrixMode((int)5888);
                GL11.glEnable((int)3042);
                float f6 = 0.5f;
                GL11.glColor4f((float)f6, (float)f6, (float)f6, (float)1.0f);
                GL11.glDisable((int)2896);
                GL11.glBlendFunc((int)1, (int)1);
                return true;
            }
            if (n2 == 2) {
                GL11.glMatrixMode((int)5890);
                GL11.glLoadIdentity();
                GL11.glMatrixMode((int)5888);
                GL11.glEnable((int)2896);
                GL11.glDisable((int)3042);
            }
        }
        return false;
    }

    protected boolean b(gb gb2, int n2, float f2) {
        return false;
    }
}

