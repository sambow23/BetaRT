/*
 * Decompiled with CFR 0.152.
 */
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;

public class ku
extends aw {
    private Minecraft g;
    private int[] h = new int[256];
    private int[] i = new int[256];
    private double j;
    private double k;

    public ku(Minecraft minecraft) {
        super(gm.aQ.a(0));
        this.g = minecraft;
        this.f = 1;
        try {
            BufferedImage bufferedImage = ImageIO.read(Minecraft.class.getResource("/gui/items.png"));
            int n2 = this.b % 16 * 16;
            int n3 = this.b / 16 * 16;
            bufferedImage.getRGB(n2, n3, 16, 16, this.h, 0, 16);
            bufferedImage = ImageIO.read(Minecraft.class.getResource("/misc/dial.png"));
            bufferedImage.getRGB(0, 0, 16, 16, this.i, 0, 16);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void a() {
        double d2;
        double d3 = 0.0;
        if (this.g.f != null && this.g.h != null) {
            float f2 = this.g.f.b(1.0f);
            d3 = -f2 * (float)Math.PI * 2.0f;
            if (this.g.f.t.c) {
                d3 = Math.random() * 3.1415927410125732 * 2.0;
            }
        }
        for (d2 = d3 - this.j; d2 < -Math.PI; d2 += Math.PI * 2) {
        }
        while (d2 >= Math.PI) {
            d2 -= Math.PI * 2;
        }
        if (d2 < -1.0) {
            d2 = -1.0;
        }
        if (d2 > 1.0) {
            d2 = 1.0;
        }
        this.k += d2 * 0.1;
        this.k *= 0.8;
        this.j += this.k;
        double d4 = Math.sin(this.j);
        double d5 = Math.cos(this.j);
        for (int i2 = 0; i2 < 256; ++i2) {
            int n2 = this.h[i2] >> 24 & 0xFF;
            int n3 = this.h[i2] >> 16 & 0xFF;
            int n4 = this.h[i2] >> 8 & 0xFF;
            int n5 = this.h[i2] >> 0 & 0xFF;
            if (n3 == n5 && n4 == 0 && n5 > 0) {
                double d6 = -((double)(i2 % 16) / 15.0 - 0.5);
                double d7 = (double)(i2 / 16) / 15.0 - 0.5;
                int n6 = n3;
                int n7 = (int)((d6 * d5 + d7 * d4 + 0.5) * 16.0);
                int n8 = (int)((d7 * d5 - d6 * d4 + 0.5) * 16.0);
                int n9 = (n7 & 0xF) + (n8 & 0xF) * 16;
                n2 = this.i[n9] >> 24 & 0xFF;
                n3 = (this.i[n9] >> 16 & 0xFF) * n6 / 255;
                n4 = (this.i[n9] >> 8 & 0xFF) * n6 / 255;
                n5 = (this.i[n9] >> 0 & 0xFF) * n6 / 255;
            }
            if (this.c) {
                int n10 = (n3 * 30 + n4 * 59 + n5 * 11) / 100;
                int n11 = (n3 * 30 + n4 * 70) / 100;
                int n12 = (n3 * 30 + n5 * 70) / 100;
                n3 = n10;
                n4 = n11;
                n5 = n12;
            }
            this.a[i2 * 4 + 0] = (byte)n3;
            this.a[i2 * 4 + 1] = (byte)n4;
            this.a[i2 * 4 + 2] = (byte)n5;
            this.a[i2 * 4 + 3] = (byte)n2;
        }
    }
}

