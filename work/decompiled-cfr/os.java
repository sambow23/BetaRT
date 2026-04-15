/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class os
extends ob {
    private int c = -1;
    private int d = -1;
    private int e = -1;
    private float f = 0.0f;
    private float g = 0.0f;
    private float h = 0.0f;
    private int i = 0;

    public os(Minecraft minecraft) {
        super(minecraft);
    }

    public void a(gs gs2) {
        gs2.aS = -180.0f;
    }

    public boolean b(int n2, int n3, int n4, int n5) {
        int n6 = this.a.f.a(n2, n3, n4);
        int n7 = this.a.f.e(n2, n3, n4);
        boolean bl2 = super.b(n2, n3, n4, n5);
        iz iz2 = this.a.h.G();
        boolean bl3 = this.a.h.b(uu.m[n6]);
        if (iz2 != null) {
            iz2.a(n6, n2, n3, n4, this.a.h);
            if (iz2.a == 0) {
                iz2.a(this.a.h);
                this.a.h.H();
            }
        }
        if (bl2 && bl3) {
            uu.m[n6].a(this.a.f, this.a.h, n2, n3, n4, n7);
        }
        return bl2;
    }

    public void a(int n2, int n3, int n4, int n5) {
        this.a.f.a(this.a.h, n2, n3, n4, n5);
        int n6 = this.a.f.a(n2, n3, n4);
        if (n6 > 0 && this.f == 0.0f) {
            uu.m[n6].b(this.a.f, n2, n3, n4, this.a.h);
        }
        if (n6 > 0 && uu.m[n6].a(this.a.h) >= 1.0f) {
            this.b(n2, n3, n4, n5);
        }
    }

    public void a() {
        this.f = 0.0f;
        this.i = 0;
    }

    public void c(int n2, int n3, int n4, int n5) {
        if (this.i > 0) {
            --this.i;
            return;
        }
        if (n2 == this.c && n3 == this.d && n4 == this.e) {
            int n6 = this.a.f.a(n2, n3, n4);
            if (n6 == 0) {
                return;
            }
            uu uu2 = uu.m[n6];
            this.f += uu2.a(this.a.h);
            if (this.h % 4.0f == 0.0f && uu2 != null) {
                this.a.B.b(uu2.by.d(), (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f, (uu2.by.b() + 1.0f) / 8.0f, uu2.by.c() * 0.5f);
            }
            this.h += 1.0f;
            if (this.f >= 1.0f) {
                this.b(n2, n3, n4, n5);
                this.f = 0.0f;
                this.g = 0.0f;
                this.h = 0.0f;
                this.i = 5;
            }
        } else {
            this.f = 0.0f;
            this.g = 0.0f;
            this.h = 0.0f;
            this.c = n2;
            this.d = n3;
            this.e = n4;
        }
    }

    public void a(float f2) {
        if (this.f <= 0.0f) {
            this.a.v.b = 0.0f;
            this.a.g.i = 0.0f;
        } else {
            float f3;
            this.a.v.b = f3 = this.g + (this.f - this.g) * f2;
            this.a.g.i = f3;
        }
    }

    public float b() {
        return 4.0f;
    }

    public void a(fd fd2) {
        super.a(fd2);
    }

    public void c() {
        this.g = this.f;
        this.a.B.c();
    }
}

