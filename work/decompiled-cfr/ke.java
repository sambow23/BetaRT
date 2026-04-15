/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ke
extends ub {
    protected int a = 200;
    protected int b = 20;
    public int c;
    public int d;
    public String e;
    public int f;
    public boolean g = true;
    public boolean h = true;

    public ke(int n2, int n3, int n4, String string) {
        this(n2, n3, n4, 200, 20, string);
    }

    public ke(int n2, int n3, int n4, int n5, int n6, String string) {
        this.f = n2;
        this.c = n3;
        this.d = n4;
        this.a = n5;
        this.b = n6;
        this.e = string;
    }

    protected int a(boolean bl2) {
        int n2 = 1;
        if (!this.g) {
            n2 = 0;
        } else if (bl2) {
            n2 = 2;
        }
        return n2;
    }

    public void a(Minecraft minecraft, int n2, int n3) {
        if (!this.h) {
            return;
        }
        sj sj2 = minecraft.q;
        GL11.glBindTexture((int)3553, (int)minecraft.p.b("/gui/gui.png"));
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        boolean bl2 = n2 >= this.c && n3 >= this.d && n2 < this.c + this.a && n3 < this.d + this.b;
        int n4 = this.a(bl2);
        this.b(this.c, this.d, 0, 46 + n4 * 20, this.a / 2, this.b);
        this.b(this.c + this.a / 2, this.d, 200 - this.a / 2, 46 + n4 * 20, this.a / 2, this.b);
        this.b(minecraft, n2, n3);
        if (!this.g) {
            this.a(sj2, this.e, this.c + this.a / 2, this.d + (this.b - 8) / 2, -6250336);
        } else if (bl2) {
            this.a(sj2, this.e, this.c + this.a / 2, this.d + (this.b - 8) / 2, 0xFFFFA0);
        } else {
            this.a(sj2, this.e, this.c + this.a / 2, this.d + (this.b - 8) / 2, 0xE0E0E0);
        }
    }

    protected void b(Minecraft minecraft, int n2, int n3) {
    }

    public void a(int n2, int n3) {
    }

    public boolean c(Minecraft minecraft, int n2, int n3) {
        return this.g && n2 >= this.c && n3 >= this.d && n2 < this.c + this.a && n3 < this.d + this.b;
    }
}

