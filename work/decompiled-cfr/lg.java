/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Mouse
 *  org.lwjgl.opengl.GL11
 */
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class lg {
    private final Minecraft a;
    private final int b;
    private final int c;
    protected final int h;
    protected final int i;
    private final int d;
    private final int e;
    protected final int j;
    private int f;
    private int g;
    private float k = -2.0f;
    private float l;
    private float m;
    private int n = -1;
    private long o = 0L;
    private boolean p = true;
    private boolean q;
    private int r;

    public lg(Minecraft minecraft, int n2, int n3, int n4, int n5, int n6) {
        this.a = minecraft;
        this.b = n2;
        this.c = n3;
        this.h = n4;
        this.i = n5;
        this.j = n6;
        this.e = 0;
        this.d = n2;
    }

    public void a(boolean bl2) {
        this.p = bl2;
    }

    protected void a(boolean bl2, int n2) {
        this.q = bl2;
        this.r = n2;
        if (!bl2) {
            this.r = 0;
        }
    }

    protected abstract int a();

    protected abstract void a(int var1, boolean var2);

    protected abstract boolean c_(int var1);

    protected int b() {
        return this.a() * this.j + this.r;
    }

    protected abstract void c();

    protected abstract void a(int var1, int var2, int var3, int var4, nw var5);

    protected void a(int n2, int n3, nw nw2) {
    }

    protected void a(int n2, int n3) {
    }

    protected void b(int n2, int n3) {
    }

    public int c(int n2, int n3) {
        int n4 = this.b / 2 - 110;
        int n5 = this.b / 2 + 110;
        int n6 = n3 - this.h - this.r + (int)this.m - 4;
        int n7 = n6 / this.j;
        if (n2 >= n4 && n2 <= n5 && n7 >= 0 && n6 >= 0 && n7 < this.a()) {
            return n7;
        }
        return -1;
    }

    public void a(List list, int n2, int n3) {
        this.f = n2;
        this.g = n3;
    }

    private void d() {
        int n2 = this.b() - (this.i - this.h - 4);
        if (n2 < 0) {
            n2 /= 2;
        }
        if (this.m < 0.0f) {
            this.m = 0.0f;
        }
        if (this.m > (float)n2) {
            this.m = n2;
        }
    }

    public void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == this.f) {
            this.m -= (float)(this.j * 2 / 3);
            this.k = -2.0f;
            this.d();
        } else if (ke2.f == this.g) {
            this.m += (float)(this.j * 2 / 3);
            this.k = -2.0f;
            this.d();
        }
    }

    public void a(int n2, int n3, float f2) {
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        this.c();
        int n10 = this.a();
        int n11 = this.b / 2 + 124;
        int n12 = n11 + 6;
        if (Mouse.isButtonDown((int)0)) {
            if (this.k == -1.0f) {
                boolean bl2 = true;
                if (n3 >= this.h && n3 <= this.i) {
                    int n13 = this.b / 2 - 110;
                    n9 = this.b / 2 + 110;
                    n8 = n3 - this.h - this.r + (int)this.m - 4;
                    n7 = n8 / this.j;
                    if (n2 >= n13 && n2 <= n9 && n7 >= 0 && n8 >= 0 && n7 < n10) {
                        n6 = n7 == this.n && System.currentTimeMillis() - this.o < 250L ? 1 : 0;
                        this.a(n7, n6 != 0);
                        this.n = n7;
                        this.o = System.currentTimeMillis();
                    } else if (n2 >= n13 && n2 <= n9 && n8 < 0) {
                        this.a(n2 - n13, n3 - this.h + (int)this.m - 4);
                        bl2 = false;
                    }
                    if (n2 >= n11 && n2 <= n12) {
                        this.l = -1.0f;
                        n6 = this.b() - (this.i - this.h - 4);
                        if (n6 < 1) {
                            n6 = 1;
                        }
                        if ((n5 = (int)((float)((this.i - this.h) * (this.i - this.h)) / (float)this.b())) < 32) {
                            n5 = 32;
                        }
                        if (n5 > this.i - this.h - 8) {
                            n5 = this.i - this.h - 8;
                        }
                        this.l /= (float)(this.i - this.h - n5) / (float)n6;
                    } else {
                        this.l = 1.0f;
                    }
                    this.k = bl2 ? (float)n3 : -2.0f;
                } else {
                    this.k = -2.0f;
                }
            } else if (this.k >= 0.0f) {
                this.m -= ((float)n3 - this.k) * this.l;
                this.k = n3;
            }
        } else {
            this.k = -1.0f;
        }
        this.d();
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2912);
        nw nw2 = nw.a;
        GL11.glBindTexture((int)3553, (int)this.a.p.b("/gui/background.png"));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        float f3 = 32.0f;
        nw2.b();
        nw2.b(0x202020);
        nw2.a(this.e, this.i, 0.0, (float)this.e / f3, (float)(this.i + (int)this.m) / f3);
        nw2.a(this.d, this.i, 0.0, (float)this.d / f3, (float)(this.i + (int)this.m) / f3);
        nw2.a(this.d, this.h, 0.0, (float)this.d / f3, (float)(this.h + (int)this.m) / f3);
        nw2.a(this.e, this.h, 0.0, (float)this.e / f3, (float)(this.h + (int)this.m) / f3);
        nw2.a();
        n9 = this.b / 2 - 92 - 16;
        n8 = this.h + 4 - (int)this.m;
        if (this.q) {
            this.a(n9, n8, nw2);
        }
        for (n7 = 0; n7 < n10; ++n7) {
            n6 = n8 + n7 * this.j + this.r;
            n5 = this.j - 4;
            if (n6 > this.i || n6 + n5 < this.h) continue;
            if (this.p && this.c_(n7)) {
                n4 = this.b / 2 - 110;
                int n14 = this.b / 2 + 110;
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                GL11.glDisable((int)3553);
                nw2.b();
                nw2.b(0x808080);
                nw2.a(n4, n6 + n5 + 2, 0.0, 0.0, 1.0);
                nw2.a(n14, n6 + n5 + 2, 0.0, 1.0, 1.0);
                nw2.a(n14, n6 - 2, 0.0, 1.0, 0.0);
                nw2.a(n4, n6 - 2, 0.0, 0.0, 0.0);
                nw2.b(0);
                nw2.a(n4 + 1, n6 + n5 + 1, 0.0, 0.0, 1.0);
                nw2.a(n14 - 1, n6 + n5 + 1, 0.0, 1.0, 1.0);
                nw2.a(n14 - 1, n6 - 1, 0.0, 1.0, 0.0);
                nw2.a(n4 + 1, n6 - 1, 0.0, 0.0, 0.0);
                nw2.a();
                GL11.glEnable((int)3553);
            }
            this.a(n7, n9, n6, n5, nw2);
        }
        GL11.glDisable((int)2929);
        n7 = 4;
        this.a(0, this.h, 255, 255);
        this.a(this.i, this.c, 255, 255);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3008);
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3553);
        nw2.b();
        nw2.a(0, 0);
        nw2.a(this.e, this.h + n7, 0.0, 0.0, 1.0);
        nw2.a(this.d, this.h + n7, 0.0, 1.0, 1.0);
        nw2.a(0, 255);
        nw2.a(this.d, this.h, 0.0, 1.0, 0.0);
        nw2.a(this.e, this.h, 0.0, 0.0, 0.0);
        nw2.a();
        nw2.b();
        nw2.a(0, 255);
        nw2.a(this.e, this.i, 0.0, 0.0, 1.0);
        nw2.a(this.d, this.i, 0.0, 1.0, 1.0);
        nw2.a(0, 0);
        nw2.a(this.d, this.i - n7, 0.0, 1.0, 0.0);
        nw2.a(this.e, this.i - n7, 0.0, 0.0, 0.0);
        nw2.a();
        n6 = this.b() - (this.i - this.h - 4);
        if (n6 > 0) {
            n5 = (this.i - this.h) * (this.i - this.h) / this.b();
            if (n5 < 32) {
                n5 = 32;
            }
            if (n5 > this.i - this.h - 8) {
                n5 = this.i - this.h - 8;
            }
            if ((n4 = (int)this.m * (this.i - this.h - n5) / n6 + this.h) < this.h) {
                n4 = this.h;
            }
            nw2.b();
            nw2.a(0, 255);
            nw2.a(n11, this.i, 0.0, 0.0, 1.0);
            nw2.a(n12, this.i, 0.0, 1.0, 1.0);
            nw2.a(n12, this.h, 0.0, 1.0, 0.0);
            nw2.a(n11, this.h, 0.0, 0.0, 0.0);
            nw2.a();
            nw2.b();
            nw2.a(0x808080, 255);
            nw2.a(n11, n4 + n5, 0.0, 0.0, 1.0);
            nw2.a(n12, n4 + n5, 0.0, 1.0, 1.0);
            nw2.a(n12, n4, 0.0, 1.0, 0.0);
            nw2.a(n11, n4, 0.0, 0.0, 0.0);
            nw2.a();
            nw2.b();
            nw2.a(0xC0C0C0, 255);
            nw2.a(n11, n4 + n5 - 1, 0.0, 0.0, 1.0);
            nw2.a(n12 - 1, n4 + n5 - 1, 0.0, 1.0, 1.0);
            nw2.a(n12 - 1, n4, 0.0, 1.0, 0.0);
            nw2.a(n11, n4, 0.0, 0.0, 0.0);
            nw2.a();
        }
        this.b(n2, n3);
        GL11.glEnable((int)3553);
        GL11.glShadeModel((int)7424);
        GL11.glEnable((int)3008);
        GL11.glDisable((int)3042);
    }

    private void a(int n2, int n3, int n4, int n5) {
        nw nw2 = nw.a;
        GL11.glBindTexture((int)3553, (int)this.a.p.b("/gui/background.png"));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        float f2 = 32.0f;
        nw2.b();
        nw2.a(0x404040, n5);
        nw2.a(0.0, n3, 0.0, 0.0, (float)n3 / f2);
        nw2.a(this.b, n3, 0.0, (float)this.b / f2, (float)n3 / f2);
        nw2.a(0x404040, n4);
        nw2.a(this.b, n2, 0.0, (float)this.b / f2, (float)n2 / f2);
        nw2.a(0.0, n2, 0.0, 0.0, (float)n2 / f2);
        nw2.a();
    }
}

