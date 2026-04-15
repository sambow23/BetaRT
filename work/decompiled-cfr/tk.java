/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class tk
extends dc {
    public nb bN;
    private int bO = 0;
    private boolean bP = false;
    private double bQ;
    private double bR;
    private double bS;
    private double bT;
    private float bU;
    private float bV;
    private boolean bW = false;
    private boolean bX = false;
    private int bY = 0;

    public tk(Minecraft minecraft, fd fd2, gr gr2, nb nb2) {
        super(minecraft, fd2, gr2, 0);
        this.bN = nb2;
    }

    public boolean a(sn sn2, int n2) {
        return false;
    }

    public void c(int n2) {
    }

    public void w_() {
        if (!this.aI.i(in.b(this.aM), 64, in.b(this.aO))) {
            return;
        }
        super.w_();
        this.am();
    }

    public void am() {
        boolean bl2;
        boolean bl3;
        if (this.bO++ == 20) {
            this.an();
            this.bO = 0;
        }
        if ((bl3 = this.t()) != this.bX) {
            if (bl3) {
                this.bN.b(new ts(this, 1));
            } else {
                this.bN.b(new ts(this, 2));
            }
            this.bX = bl3;
        }
        double d2 = this.aM - this.bQ;
        double d3 = this.aW.b - this.bR;
        double d4 = this.aN - this.bS;
        double d5 = this.aO - this.bT;
        double d6 = this.aS - this.bU;
        double d7 = this.aT - this.bV;
        boolean bl4 = d3 != 0.0 || d4 != 0.0 || d2 != 0.0 || d5 != 0.0;
        boolean bl5 = bl2 = d6 != 0.0 || d7 != 0.0;
        if (this.aH != null) {
            if (bl2) {
                this.bN.b(new af(this.aP, -999.0, -999.0, this.aR, this.aX));
            } else {
                this.bN.b(new ev(this.aP, -999.0, -999.0, this.aR, this.aS, this.aT, this.aX));
            }
            bl4 = false;
        } else if (bl4 && bl2) {
            this.bN.b(new ev(this.aM, this.aW.b, this.aN, this.aO, this.aS, this.aT, this.aX));
            this.bY = 0;
        } else if (bl4) {
            this.bN.b(new af(this.aM, this.aW.b, this.aN, this.aO, this.aX));
            this.bY = 0;
        } else if (bl2) {
            this.bN.b(new vh(this.aS, this.aT, this.aX));
            this.bY = 0;
        } else {
            this.bN.b(new ig(this.aX));
            this.bY = this.bW != this.aX || this.bY > 200 ? 0 : ++this.bY;
        }
        this.bW = this.aX;
        if (bl4) {
            this.bQ = this.aM;
            this.bR = this.aW.b;
            this.bS = this.aN;
            this.bT = this.aO;
        }
        if (bl2) {
            this.bU = this.aS;
            this.bV = this.aT;
        }
    }

    public void D() {
        this.bN.b(new jv(4, 0, 0, 0, 0));
    }

    private void an() {
    }

    protected void a(hl hl2) {
    }

    public void a(String string) {
        this.bN.b(new pe(string));
    }

    public void J() {
        super.J();
        this.bN.b(new nm(this, 1));
    }

    public void p_() {
        this.an();
        this.bN.b(new ox((byte)this.m));
    }

    protected void b(int n2) {
        this.Y -= n2;
    }

    public void r() {
        this.bN.b(new mn(this.e.f));
        this.c.b((iz)null);
        super.r();
    }

    public void d_(int n2) {
        if (this.bP) {
            super.d_(n2);
        } else {
            this.Y = n2;
            this.bP = true;
        }
    }

    public void a(vr vr2, int n2) {
        if (vr2 == null) {
            return;
        }
        if (vr2.g) {
            super.a(vr2, n2);
        }
    }

    public void b(vr vr2, int n2) {
        if (vr2 == null) {
            return;
        }
        if (!vr2.g) {
            super.a(vr2, n2);
        }
    }
}

