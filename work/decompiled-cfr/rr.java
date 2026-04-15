/*
 * Decompiled with CFR 0.152.
 */
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class rr
implements nf {
    private int[] a;
    private int b;
    private int c;

    public BufferedImage a(BufferedImage bufferedImage) {
        int n2;
        int n3;
        int n4;
        if (bufferedImage == null) {
            return null;
        }
        this.b = 64;
        this.c = 32;
        BufferedImage bufferedImage2 = new BufferedImage(this.b, this.c, 2);
        Graphics graphics = bufferedImage2.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        graphics.dispose();
        this.a = ((DataBufferInt)bufferedImage2.getRaster().getDataBuffer()).getData();
        this.b(0, 0, 32, 16);
        this.a(32, 0, 64, 32);
        this.b(0, 16, 64, 32);
        boolean bl2 = false;
        for (n4 = 32; n4 < 64; ++n4) {
            for (n3 = 0; n3 < 16; ++n3) {
                n2 = this.a[n4 + n3 * 64];
                if ((n2 >> 24 & 0xFF) >= 128) continue;
                bl2 = true;
            }
        }
        if (!bl2) {
            for (n4 = 32; n4 < 64; ++n4) {
                for (n3 = 0; n3 < 16; ++n3) {
                    n2 = this.a[n4 + n3 * 64];
                    if ((n2 >> 24 & 0xFF) >= 128) continue;
                    bl2 = true;
                }
            }
        }
        return bufferedImage2;
    }

    private void a(int n2, int n3, int n4, int n5) {
        if (this.c(n2, n3, n4, n5)) {
            return;
        }
        for (int i2 = n2; i2 < n4; ++i2) {
            for (int i3 = n3; i3 < n5; ++i3) {
                int n6 = i2 + i3 * this.b;
                this.a[n6] = this.a[n6] & 0xFFFFFF;
            }
        }
    }

    private void b(int n2, int n3, int n4, int n5) {
        for (int i2 = n2; i2 < n4; ++i2) {
            for (int i3 = n3; i3 < n5; ++i3) {
                int n6 = i2 + i3 * this.b;
                this.a[n6] = this.a[n6] | 0xFF000000;
            }
        }
    }

    private boolean c(int n2, int n3, int n4, int n5) {
        for (int i2 = n2; i2 < n4; ++i2) {
            for (int i3 = n3; i3 < n5; ++i3) {
                int n6 = this.a[i2 + i3 * this.b];
                if ((n6 >> 24 & 0xFF) >= 128) continue;
                return true;
            }
        }
        return false;
    }
}

