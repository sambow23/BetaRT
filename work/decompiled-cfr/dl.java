/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class dl
extends bg {
    public static final float[][] a = new float[][]{{1.0f, 1.0f, 1.0f}, {0.95f, 0.7f, 0.2f}, {0.9f, 0.5f, 0.85f}, {0.6f, 0.7f, 0.95f}, {0.9f, 0.9f, 0.2f}, {0.5f, 0.8f, 0.1f}, {0.95f, 0.7f, 0.8f}, {0.3f, 0.3f, 0.3f}, {0.6f, 0.6f, 0.6f}, {0.3f, 0.6f, 0.7f}, {0.7f, 0.4f, 0.9f}, {0.2f, 0.4f, 0.8f}, {0.5f, 0.4f, 0.3f}, {0.4f, 0.5f, 0.2f}, {0.8f, 0.3f, 0.3f}, {0.1f, 0.1f, 0.1f}};

    public dl(fd fd2) {
        super(fd2);
        this.O = "/mob/sheep.png";
        this.b(0.9f, 1.3f);
    }

    protected void b() {
        super.b();
        this.bD.a(16, new Byte(0));
    }

    public boolean a(sn sn2, int n2) {
        return super.a(sn2, n2);
    }

    protected void q() {
        if (!this.s()) {
            this.a(new iz(uu.ac.bn, 1, this.r()), 0.0f);
        }
    }

    protected int j() {
        return uu.ac.bn;
    }

    public boolean a(gs gs2) {
        iz iz2 = gs2.c.b();
        if (iz2 != null && iz2.c == gm.bc.bf && !this.s()) {
            if (!this.aI.B) {
                this.a(true);
                int n2 = 2 + this.bs.nextInt(3);
                for (int i2 = 0; i2 < n2; ++i2) {
                    hl hl2 = this.a(new iz(uu.ac.bn, 1, this.r()), 1.0f);
                    hl2.aQ += (double)(this.bs.nextFloat() * 0.05f);
                    hl2.aP += (double)((this.bs.nextFloat() - this.bs.nextFloat()) * 0.1f);
                    hl2.aR += (double)((this.bs.nextFloat() - this.bs.nextFloat()) * 0.1f);
                }
            }
            iz2.a(1, (sn)gs2);
        }
        return false;
    }

    public void b(nu nu2) {
        super.b(nu2);
        nu2.a("Sheared", this.s());
        nu2.a("Color", (byte)this.r());
    }

    public void a(nu nu2) {
        super.a(nu2);
        this.a(nu2.m("Sheared"));
        this.e_(nu2.c("Color"));
    }

    protected String g() {
        return "mob.sheep";
    }

    protected String j_() {
        return "mob.sheep";
    }

    protected String i() {
        return "mob.sheep";
    }

    public int r() {
        return this.bD.a(16) & 0xF;
    }

    public void e_(int n2) {
        byte by2 = this.bD.a(16);
        this.bD.b(16, (byte)(by2 & 0xF0 | n2 & 0xF));
    }

    public boolean s() {
        return (this.bD.a(16) & 0x10) != 0;
    }

    public void a(boolean bl2) {
        byte by2 = this.bD.a(16);
        if (bl2) {
            this.bD.b(16, (byte)(by2 | 0x10));
        } else {
            this.bD.b(16, (byte)(by2 & 0xFFFFFFEF));
        }
    }

    public static int a(Random random) {
        int n2 = random.nextInt(100);
        if (n2 < 5) {
            return 15;
        }
        if (n2 < 10) {
            return 7;
        }
        if (n2 < 15) {
            return 8;
        }
        if (n2 < 18) {
            return 12;
        }
        if (random.nextInt(500) == 0) {
            return 6;
        }
        return 0;
    }
}

