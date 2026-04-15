/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;

public class sj {
    private int[] b = new int[256];
    public int a = 0;
    private int c;
    private IntBuffer d = ge.d(1024);

    public sj(kv kv2, String string, ji ji2) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(ji.class.getResourceAsStream(string));
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        int n7 = bufferedImage.getWidth();
        int n8 = bufferedImage.getHeight();
        int[] nArray = new int[n7 * n8];
        bufferedImage.getRGB(0, 0, n7, n8, nArray, 0, n7);
        for (int i2 = 0; i2 < 256; ++i2) {
            n6 = i2 % 16;
            n5 = i2 / 16;
            for (n4 = 7; n4 >= 0; --n4) {
                int n9 = n6 * 8 + n4;
                boolean bl2 = true;
                for (int i3 = 0; i3 < 8 && bl2; ++i3) {
                    n3 = (n5 * 8 + i3) * n7;
                    n2 = nArray[n9 + n3] & 0xFF;
                    if (n2 <= 0) continue;
                    bl2 = false;
                }
                if (!bl2) break;
            }
            if (i2 == 32) {
                n4 = 2;
            }
            this.b[i2] = n4 + 2;
        }
        this.a = ji2.a(bufferedImage);
        this.c = ge.a(288);
        nw nw2 = nw.a;
        for (n6 = 0; n6 < 256; ++n6) {
            GL11.glNewList((int)(this.c + n6), (int)4864);
            nw2.b();
            n5 = n6 % 16 * 8;
            n4 = n6 / 16 * 8;
            float f2 = 7.99f;
            float f3 = 0.0f;
            float f4 = 0.0f;
            nw2.a(0.0, 0.0f + f2, 0.0, (float)n5 / 128.0f + f3, ((float)n4 + f2) / 128.0f + f4);
            nw2.a(0.0f + f2, 0.0f + f2, 0.0, ((float)n5 + f2) / 128.0f + f3, ((float)n4 + f2) / 128.0f + f4);
            nw2.a(0.0f + f2, 0.0, 0.0, ((float)n5 + f2) / 128.0f + f3, (float)n4 / 128.0f + f4);
            nw2.a(0.0, 0.0, 0.0, (float)n5 / 128.0f + f3, (float)n4 / 128.0f + f4);
            nw2.a();
            GL11.glTranslatef((float)this.b[n6], (float)0.0f, (float)0.0f);
            GL11.glEndList();
        }
        for (n6 = 0; n6 < 32; ++n6) {
            boolean bl3;
            n5 = (n6 >> 3 & 1) * 85;
            n4 = (n6 >> 2 & 1) * 170 + n5;
            int n10 = (n6 >> 1 & 1) * 170 + n5;
            int n11 = (n6 >> 0 & 1) * 170 + n5;
            if (n6 == 6) {
                n4 += 85;
            }
            boolean bl4 = bl3 = n6 >= 16;
            if (kv2.g) {
                n3 = (n4 * 30 + n10 * 59 + n11 * 11) / 100;
                n2 = (n4 * 30 + n10 * 70) / 100;
                int n12 = (n4 * 30 + n11 * 70) / 100;
                n4 = n3;
                n10 = n2;
                n11 = n12;
            }
            if (bl3) {
                n4 /= 4;
                n10 /= 4;
                n11 /= 4;
            }
            GL11.glNewList((int)(this.c + 256 + n6), (int)4864);
            GL11.glColor3f((float)((float)n4 / 255.0f), (float)((float)n10 / 255.0f), (float)((float)n11 / 255.0f));
            GL11.glEndList();
        }
    }

    public void a(String string, int n2, int n3, int n4) {
        this.a(string, n2 + 1, n3 + 1, n4, true);
        this.b(string, n2, n3, n4);
    }

    public void b(String string, int n2, int n3, int n4) {
        this.a(string, n2, n3, n4, false);
    }

    public void a(String string, int n2, int n3, int n4, boolean bl2) {
        if (string == null) {
            return;
        }
        if (bl2) {
            int n5 = n4 & 0xFF000000;
            n4 = (n4 & 0xFCFCFC) >> 2;
            n4 += n5;
        }
        GL11.glBindTexture((int)3553, (int)this.a);
        float f2 = (float)(n4 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n4 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n4 & 0xFF) / 255.0f;
        float f5 = (float)(n4 >> 24 & 0xFF) / 255.0f;
        if (f5 == 0.0f) {
            f5 = 1.0f;
        }
        GL11.glColor4f((float)f2, (float)f3, (float)f4, (float)f5);
        this.d.clear();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)n2, (float)n3, (float)0.0f);
        for (int i2 = 0; i2 < string.length(); ++i2) {
            int n6;
            while (string.length() > i2 + 1 && string.charAt(i2) == '\u00a7') {
                int n7 = "0123456789abcdef".indexOf(string.toLowerCase().charAt(i2 + 1));
                if (n7 < 0 || n7 > 15) {
                    n7 = 15;
                }
                this.d.put(this.c + 256 + n7 + (bl2 ? 16 : 0));
                if (this.d.remaining() == 0) {
                    this.d.flip();
                    GL11.glCallLists((IntBuffer)this.d);
                    this.d.clear();
                }
                i2 += 2;
            }
            if (i2 < string.length() && (n6 = fp.a.indexOf(string.charAt(i2))) >= 0) {
                this.d.put(this.c + n6 + 32);
            }
            if (this.d.remaining() != 0) continue;
            this.d.flip();
            GL11.glCallLists((IntBuffer)this.d);
            this.d.clear();
        }
        this.d.flip();
        GL11.glCallLists((IntBuffer)this.d);
        GL11.glPopMatrix();
    }

    public int a(String string) {
        if (string == null) {
            return 0;
        }
        int n2 = 0;
        for (int i2 = 0; i2 < string.length(); ++i2) {
            if (string.charAt(i2) == '\u00a7') {
                ++i2;
                continue;
            }
            int n3 = fp.a.indexOf(string.charAt(i2));
            if (n3 < 0) continue;
            n2 += this.b[n3 + 32];
        }
        return n2;
    }

    public void a(String string, int n2, int n3, int n4, int n5) {
        String[] stringArray = string.split("\n");
        if (stringArray.length > 1) {
            for (int i2 = 0; i2 < stringArray.length; ++i2) {
                this.a(stringArray[i2], n2, n3, n4, n5);
                n3 += this.a(stringArray[i2], n4);
            }
            return;
        }
        String[] stringArray2 = string.split(" ");
        int n6 = 0;
        while (n6 < stringArray2.length) {
            String string2 = stringArray2[n6++] + " ";
            while (n6 < stringArray2.length && this.a(string2 + stringArray2[n6]) < n4) {
                string2 = string2 + stringArray2[n6++] + " ";
            }
            while (this.a(string2) > n4) {
                int n7 = 0;
                while (this.a(string2.substring(0, n7 + 1)) <= n4) {
                    ++n7;
                }
                if (string2.substring(0, n7).trim().length() > 0) {
                    this.b(string2.substring(0, n7), n2, n3, n5);
                    n3 += 8;
                }
                string2 = string2.substring(n7);
            }
            if (string2.trim().length() <= 0) continue;
            this.b(string2, n2, n3, n5);
            n3 += 8;
        }
    }

    public int a(String string, int n2) {
        String[] stringArray = string.split("\n");
        if (stringArray.length > 1) {
            int n3 = 0;
            for (int i2 = 0; i2 < stringArray.length; ++i2) {
                n3 += this.a(stringArray[i2], n2);
            }
            return n3;
        }
        String[] stringArray2 = string.split(" ");
        int n4 = 0;
        int n5 = 0;
        while (n4 < stringArray2.length) {
            String string2 = stringArray2[n4++] + " ";
            while (n4 < stringArray2.length && this.a(string2 + stringArray2[n4]) < n2) {
                string2 = string2 + stringArray2[n4++] + " ";
            }
            while (this.a(string2) > n2) {
                int n6 = 0;
                while (this.a(string2.substring(0, n6 + 1)) <= n2) {
                    ++n6;
                }
                if (string2.substring(0, n6).trim().length() > 0) {
                    n5 += 8;
                }
                string2 = string2.substring(n6);
            }
            if (string2.trim().length() <= 0) continue;
            n5 += 8;
        }
        if (n5 < 8) {
            n5 += 8;
        }
        return n5;
    }
}

