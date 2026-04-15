/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import org.lwjgl.opengl.GL11;

public class ps {
    private ib[] j;
    private tz[] k;
    private int l;
    private int m;
    public float a;
    public float b;
    public float c;
    public float d;
    public float e;
    public float f;
    private boolean n = false;
    private int o = 0;
    public boolean g = false;
    public boolean h = true;
    public boolean i = false;

    public ps(int n2, int n3) {
        this.l = n2;
        this.m = n3;
    }

    public void a(float f2, float f3, float f4, int n2, int n3, int n4) {
        this.a(f2, f3, f4, n2, n3, n4, 0.0f);
    }

    public void a(float f2, float f3, float f4, int n2, int n3, int n4, float f5) {
        this.j = new ib[8];
        this.k = new tz[6];
        float f6 = f2 + (float)n2;
        float f7 = f3 + (float)n3;
        float f8 = f4 + (float)n4;
        f2 -= f5;
        f3 -= f5;
        f4 -= f5;
        f6 += f5;
        f7 += f5;
        f8 += f5;
        if (this.g) {
            float f9 = f6;
            f6 = f2;
            f2 = f9;
        }
        ib ib2 = new ib(f2, f3, f4, 0.0f, 0.0f);
        ib ib3 = new ib(f6, f3, f4, 0.0f, 8.0f);
        ib ib4 = new ib(f6, f7, f4, 8.0f, 8.0f);
        ib ib5 = new ib(f2, f7, f4, 8.0f, 0.0f);
        ib ib6 = new ib(f2, f3, f8, 0.0f, 0.0f);
        ib ib7 = new ib(f6, f3, f8, 0.0f, 8.0f);
        ib ib8 = new ib(f6, f7, f8, 8.0f, 8.0f);
        ib ib9 = new ib(f2, f7, f8, 8.0f, 0.0f);
        this.j[0] = ib2;
        this.j[1] = ib3;
        this.j[2] = ib4;
        this.j[3] = ib5;
        this.j[4] = ib6;
        this.j[5] = ib7;
        this.j[6] = ib8;
        this.j[7] = ib9;
        this.k[0] = new tz(new ib[]{ib7, ib3, ib4, ib8}, this.l + n4 + n2, this.m + n4, this.l + n4 + n2 + n4, this.m + n4 + n3);
        this.k[1] = new tz(new ib[]{ib2, ib6, ib9, ib5}, this.l + 0, this.m + n4, this.l + n4, this.m + n4 + n3);
        this.k[2] = new tz(new ib[]{ib7, ib6, ib2, ib3}, this.l + n4, this.m + 0, this.l + n4 + n2, this.m + n4);
        this.k[3] = new tz(new ib[]{ib4, ib5, ib9, ib8}, this.l + n4 + n2, this.m + 0, this.l + n4 + n2 + n2, this.m + n4);
        this.k[4] = new tz(new ib[]{ib3, ib2, ib5, ib4}, this.l + n4, this.m + n4, this.l + n4 + n2, this.m + n4 + n3);
        this.k[5] = new tz(new ib[]{ib6, ib7, ib8, ib9}, this.l + n4 + n2 + n4, this.m + n4, this.l + n4 + n2 + n4 + n2, this.m + n4 + n3);
        if (this.g) {
            for (int i2 = 0; i2 < this.k.length; ++i2) {
                this.k[i2].a();
            }
        }
    }

    public void a(float f2, float f3, float f4) {
        this.a = f2;
        this.b = f3;
        this.c = f4;
    }

    public void a(float f2) {
        if (this.i) {
            return;
        }
        if (!this.h) {
            return;
        }
        if (!this.n) {
            this.d(f2);
        }
        if (this.d != 0.0f || this.e != 0.0f || this.f != 0.0f) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(this.a * f2), (float)(this.b * f2), (float)(this.c * f2));
            if (this.f != 0.0f) {
                GL11.glRotatef((float)(this.f * 57.295776f), (float)0.0f, (float)0.0f, (float)1.0f);
            }
            if (this.e != 0.0f) {
                GL11.glRotatef((float)(this.e * 57.295776f), (float)0.0f, (float)1.0f, (float)0.0f);
            }
            if (this.d != 0.0f) {
                GL11.glRotatef((float)(this.d * 57.295776f), (float)1.0f, (float)0.0f, (float)0.0f);
            }
            GL11.glCallList((int)this.o);
            GL11.glPopMatrix();
        } else if (this.a != 0.0f || this.b != 0.0f || this.c != 0.0f) {
            GL11.glTranslatef((float)(this.a * f2), (float)(this.b * f2), (float)(this.c * f2));
            GL11.glCallList((int)this.o);
            GL11.glTranslatef((float)(-this.a * f2), (float)(-this.b * f2), (float)(-this.c * f2));
        } else {
            GL11.glCallList((int)this.o);
        }
    }

    public void b(float f2) {
        if (this.i) {
            return;
        }
        if (!this.h) {
            return;
        }
        if (!this.n) {
            this.d(f2);
        }
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.a * f2), (float)(this.b * f2), (float)(this.c * f2));
        if (this.e != 0.0f) {
            GL11.glRotatef((float)(this.e * 57.295776f), (float)0.0f, (float)1.0f, (float)0.0f);
        }
        if (this.d != 0.0f) {
            GL11.glRotatef((float)(this.d * 57.295776f), (float)1.0f, (float)0.0f, (float)0.0f);
        }
        if (this.f != 0.0f) {
            GL11.glRotatef((float)(this.f * 57.295776f), (float)0.0f, (float)0.0f, (float)1.0f);
        }
        GL11.glCallList((int)this.o);
        GL11.glPopMatrix();
    }

    public void c(float f2) {
        if (this.i) {
            return;
        }
        if (!this.h) {
            return;
        }
        if (!this.n) {
            this.d(f2);
        }
        if (this.d != 0.0f || this.e != 0.0f || this.f != 0.0f) {
            GL11.glTranslatef((float)(this.a * f2), (float)(this.b * f2), (float)(this.c * f2));
            if (this.f != 0.0f) {
                GL11.glRotatef((float)(this.f * 57.295776f), (float)0.0f, (float)0.0f, (float)1.0f);
            }
            if (this.e != 0.0f) {
                GL11.glRotatef((float)(this.e * 57.295776f), (float)0.0f, (float)1.0f, (float)0.0f);
            }
            if (this.d != 0.0f) {
                GL11.glRotatef((float)(this.d * 57.295776f), (float)1.0f, (float)0.0f, (float)0.0f);
            }
        } else if (this.a != 0.0f || this.b != 0.0f || this.c != 0.0f) {
            GL11.glTranslatef((float)(this.a * f2), (float)(this.b * f2), (float)(this.c * f2));
        }
    }

    private void d(float f2) {
        this.o = ge.a(1);
        GL11.glNewList((int)this.o, (int)4864);
        nw nw2 = nw.a;
        for (int i2 = 0; i2 < this.k.length; ++i2) {
            this.k[i2].a(nw2, f2);
        }
        GL11.glEndList();
        this.n = true;
    }
}

