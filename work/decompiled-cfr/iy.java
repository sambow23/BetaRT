/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.awt.image.BufferedImage;
import org.lwjgl.opengl.GL11;

public class iy {
    private int[] a = new int[16384];
    private int b;
    private kv c;
    private sj d;

    public iy(sj sj2, kv kv2, ji ji2) {
        this.c = kv2;
        this.d = sj2;
        this.b = ji2.a(new BufferedImage(128, 128, 2));
        for (int i2 = 0; i2 < 16384; ++i2) {
            this.a[i2] = 0;
        }
    }

    public void a(gs gs2, ji ji2, iu iu2) {
        byte by2;
        int n2;
        for (n2 = 0; n2 < 16384; ++n2) {
            by2 = iu2.f[n2];
            if (by2 / 4 == 0) {
                this.a[n2] = (n2 + n2 / 128 & 1) * 8 + 16 << 24;
                continue;
            }
            int n3 = dx.a[by2 / 4].p;
            int n4 = by2 & 3;
            int n5 = 220;
            if (n4 == 2) {
                n5 = 255;
            }
            if (n4 == 0) {
                n5 = 180;
            }
            int n6 = (n3 >> 16 & 0xFF) * n5 / 255;
            int n7 = (n3 >> 8 & 0xFF) * n5 / 255;
            int n8 = (n3 & 0xFF) * n5 / 255;
            if (this.c.g) {
                int n9 = (n6 * 30 + n7 * 59 + n8 * 11) / 100;
                int n10 = (n6 * 30 + n7 * 70) / 100;
                int n11 = (n6 * 30 + n8 * 70) / 100;
                n6 = n9;
                n7 = n10;
                n8 = n11;
            }
            this.a[n2] = 0xFF000000 | n6 << 16 | n7 << 8 | n8;
        }
        ji2.a(this.a, 128, 128, this.b);
        n2 = 0;
        by2 = 0;
        nw nw2 = nw.a;
        float f2 = 0.0f;
        GL11.glBindTexture((int)3553, (int)this.b);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3008);
        nw2.b();
        nw2.a((float)(n2 + 0) + f2, (float)(by2 + 128) - f2, -0.01f, 0.0, 1.0);
        nw2.a((float)(n2 + 128) - f2, (float)(by2 + 128) - f2, -0.01f, 1.0, 1.0);
        nw2.a((float)(n2 + 128) - f2, (float)(by2 + 0) + f2, -0.01f, 1.0, 0.0);
        nw2.a((float)(n2 + 0) + f2, (float)(by2 + 0) + f2, -0.01f, 0.0, 0.0);
        nw2.a();
        GL11.glEnable((int)3008);
        GL11.glDisable((int)3042);
        ji2.b(ji2.b("/misc/mapicons.png"));
        for (ax ax2 : iu2.i) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)((float)n2 + (float)ax2.b / 2.0f + 64.0f), (float)((float)by2 + (float)ax2.c / 2.0f + 64.0f), (float)-0.02f);
            GL11.glRotatef((float)((float)(ax2.d * 360) / 16.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glScalef((float)4.0f, (float)4.0f, (float)3.0f);
            GL11.glTranslatef((float)-0.125f, (float)0.125f, (float)0.0f);
            float f3 = (float)(ax2.a % 4 + 0) / 4.0f;
            float f4 = (float)(ax2.a / 4 + 0) / 4.0f;
            float f5 = (float)(ax2.a % 4 + 1) / 4.0f;
            float f6 = (float)(ax2.a / 4 + 1) / 4.0f;
            nw2.b();
            nw2.a(-1.0, 1.0, 0.0, f3, f4);
            nw2.a(1.0, 1.0, 0.0, f5, f4);
            nw2.a(1.0, -1.0, 0.0, f5, f6);
            nw2.a(-1.0, -1.0, 0.0, f3, f6);
            nw2.a();
            GL11.glPopMatrix();
        }
        GL11.glPushMatrix();
        GL11.glTranslatef((float)0.0f, (float)0.0f, (float)-0.04f);
        GL11.glScalef((float)1.0f, (float)1.0f, (float)1.0f);
        this.d.b(iu2.a, n2, by2, -16777216);
        GL11.glPopMatrix();
    }
}

