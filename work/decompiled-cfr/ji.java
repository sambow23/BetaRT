/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;

public class ji {
    public static boolean a = false;
    private HashMap b = new HashMap();
    private HashMap c = new HashMap();
    private HashMap d = new HashMap();
    private IntBuffer e = ge.d(1);
    private ByteBuffer f = ge.c(0x100000);
    private List g = new ArrayList();
    private Map h = new HashMap();
    private kv i;
    private boolean j = false;
    private boolean k = false;
    private ik l;
    private BufferedImage m = new BufferedImage(64, 64, 2);

    public ji(ik ik2, kv kv2) {
        this.l = ik2;
        this.i = kv2;
        Graphics graphics = this.m.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 64, 64);
        graphics.setColor(Color.BLACK);
        graphics.drawString("missingtex", 1, 10);
        graphics.dispose();
    }

    public int[] a(String string) {
        i i2 = this.l.a;
        int[] nArray = (int[])this.c.get(string);
        if (nArray != null) {
            return nArray;
        }
        try {
            nArray = null;
            if (string.startsWith("##")) {
                nArray = this.b(this.c(this.a(i2.a(string.substring(2)))));
            } else if (string.startsWith("%clamp%")) {
                this.j = true;
                nArray = this.b(this.a(i2.a(string.substring(7))));
                this.j = false;
            } else if (string.startsWith("%blur%")) {
                this.k = true;
                nArray = this.b(this.a(i2.a(string.substring(6))));
                this.k = false;
            } else {
                InputStream inputStream = i2.a(string);
                nArray = inputStream == null ? this.b(this.m) : this.b(this.a(inputStream));
            }
            this.c.put(string, nArray);
            return nArray;
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            int[] nArray2 = this.b(this.m);
            this.c.put(string, nArray2);
            return nArray2;
        }
    }

    private int[] b(BufferedImage bufferedImage) {
        int n2 = bufferedImage.getWidth();
        int n3 = bufferedImage.getHeight();
        int[] nArray = new int[n2 * n3];
        bufferedImage.getRGB(0, 0, n2, n3, nArray, 0, n2);
        return nArray;
    }

    private int[] a(BufferedImage bufferedImage, int[] nArray) {
        int n2 = bufferedImage.getWidth();
        int n3 = bufferedImage.getHeight();
        bufferedImage.getRGB(0, 0, n2, n3, nArray, 0, n2);
        return nArray;
    }

    public int b(String string) {
        i i2 = this.l.a;
        Integer n2 = (Integer)this.b.get(string);
        if (n2 != null) {
            return n2;
        }
        try {
            this.e.clear();
            ge.a(this.e);
            int n3 = this.e.get(0);
            if (string.startsWith("##")) {
                this.a(this.c(this.a(i2.a(string.substring(2)))), n3);
            } else if (string.startsWith("%clamp%")) {
                this.j = true;
                this.a(this.a(i2.a(string.substring(7))), n3);
                this.j = false;
            } else if (string.startsWith("%blur%")) {
                this.k = true;
                this.a(this.a(i2.a(string.substring(6))), n3);
                this.k = false;
            } else {
                InputStream inputStream = i2.a(string);
                if (inputStream == null) {
                    this.a(this.m, n3);
                } else {
                    this.a(this.a(inputStream), n3);
                }
            }
            this.b.put(string, n3);
            return n3;
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            ge.a(this.e);
            int n4 = this.e.get(0);
            this.a(this.m, n4);
            this.b.put(string, n4);
            return n4;
        }
    }

    private BufferedImage c(BufferedImage bufferedImage) {
        int n2 = bufferedImage.getWidth() / 16;
        BufferedImage bufferedImage2 = new BufferedImage(16, bufferedImage.getHeight() * n2, 2);
        Graphics graphics = bufferedImage2.getGraphics();
        for (int i2 = 0; i2 < n2; ++i2) {
            graphics.drawImage(bufferedImage, -i2 * 16, i2 * bufferedImage.getHeight(), null);
        }
        graphics.dispose();
        return bufferedImage2;
    }

    public int a(BufferedImage bufferedImage) {
        this.e.clear();
        ge.a(this.e);
        int n2 = this.e.get(0);
        this.a(bufferedImage, n2);
        this.d.put(n2, bufferedImage);
        return n2;
    }

    public void a(BufferedImage bufferedImage, int n2) {
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        GL11.glBindTexture((int)3553, (int)n2);
        if (a) {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9986);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        } else {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9728);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        }
        if (this.k) {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        }
        if (this.j) {
            GL11.glTexParameteri((int)3553, (int)10242, (int)10496);
            GL11.glTexParameteri((int)3553, (int)10243, (int)10496);
        } else {
            GL11.glTexParameteri((int)3553, (int)10242, (int)10497);
            GL11.glTexParameteri((int)3553, (int)10243, (int)10497);
        }
        int n11 = bufferedImage.getWidth();
        int n12 = bufferedImage.getHeight();
        int[] nArray = new int[n11 * n12];
        byte[] byArray = new byte[n11 * n12 * 4];
        bufferedImage.getRGB(0, 0, n11, n12, nArray, 0, n11);
        for (n10 = 0; n10 < nArray.length; ++n10) {
            n9 = nArray[n10] >> 24 & 0xFF;
            n8 = nArray[n10] >> 16 & 0xFF;
            n7 = nArray[n10] >> 8 & 0xFF;
            n6 = nArray[n10] & 0xFF;
            if (this.i != null && this.i.g) {
                n5 = (n8 * 30 + n7 * 59 + n6 * 11) / 100;
                n4 = (n8 * 30 + n7 * 70) / 100;
                n3 = (n8 * 30 + n6 * 70) / 100;
                n8 = n5;
                n7 = n4;
                n6 = n3;
            }
            byArray[n10 * 4 + 0] = (byte)n8;
            byArray[n10 * 4 + 1] = (byte)n7;
            byArray[n10 * 4 + 2] = (byte)n6;
            byArray[n10 * 4 + 3] = (byte)n9;
        }
        this.f.clear();
        this.f.put(byArray);
        this.f.position(0).limit(byArray.length);
        GL11.glTexImage2D((int)3553, (int)0, (int)6408, (int)n11, (int)n12, (int)0, (int)6408, (int)5121, (ByteBuffer)this.f);
        if (a) {
            for (n10 = 1; n10 <= 4; ++n10) {
                n9 = n11 >> n10 - 1;
                n8 = n11 >> n10;
                n7 = n12 >> n10;
                for (n6 = 0; n6 < n8; ++n6) {
                    for (n5 = 0; n5 < n7; ++n5) {
                        n4 = this.f.getInt((n6 * 2 + 0 + (n5 * 2 + 0) * n9) * 4);
                        n3 = this.f.getInt((n6 * 2 + 1 + (n5 * 2 + 0) * n9) * 4);
                        int n13 = this.f.getInt((n6 * 2 + 1 + (n5 * 2 + 1) * n9) * 4);
                        int n14 = this.f.getInt((n6 * 2 + 0 + (n5 * 2 + 1) * n9) * 4);
                        int n15 = this.b(this.b(n4, n3), this.b(n13, n14));
                        this.f.putInt((n6 + n5 * n8) * 4, n15);
                    }
                }
                GL11.glTexImage2D((int)3553, (int)n10, (int)6408, (int)n8, (int)n7, (int)0, (int)6408, (int)5121, (ByteBuffer)this.f);
            }
        }
    }

    public void a(int[] nArray, int n2, int n3, int n4) {
        GL11.glBindTexture((int)3553, (int)n4);
        if (a) {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9986);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        } else {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9728);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        }
        if (this.k) {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        }
        if (this.j) {
            GL11.glTexParameteri((int)3553, (int)10242, (int)10496);
            GL11.glTexParameteri((int)3553, (int)10243, (int)10496);
        } else {
            GL11.glTexParameteri((int)3553, (int)10242, (int)10497);
            GL11.glTexParameteri((int)3553, (int)10243, (int)10497);
        }
        byte[] byArray = new byte[n2 * n3 * 4];
        for (int i2 = 0; i2 < nArray.length; ++i2) {
            int n5 = nArray[i2] >> 24 & 0xFF;
            int n6 = nArray[i2] >> 16 & 0xFF;
            int n7 = nArray[i2] >> 8 & 0xFF;
            int n8 = nArray[i2] & 0xFF;
            if (this.i != null && this.i.g) {
                int n9 = (n6 * 30 + n7 * 59 + n8 * 11) / 100;
                int n10 = (n6 * 30 + n7 * 70) / 100;
                int n11 = (n6 * 30 + n8 * 70) / 100;
                n6 = n9;
                n7 = n10;
                n8 = n11;
            }
            byArray[i2 * 4 + 0] = (byte)n6;
            byArray[i2 * 4 + 1] = (byte)n7;
            byArray[i2 * 4 + 2] = (byte)n8;
            byArray[i2 * 4 + 3] = (byte)n5;
        }
        this.f.clear();
        this.f.put(byArray);
        this.f.position(0).limit(byArray.length);
        GL11.glTexSubImage2D((int)3553, (int)0, (int)0, (int)0, (int)n2, (int)n3, (int)6408, (int)5121, (ByteBuffer)this.f);
    }

    public void a(int n2) {
        this.d.remove(n2);
        this.e.clear();
        this.e.put(n2);
        this.e.flip();
        GL11.glDeleteTextures((IntBuffer)this.e);
    }

    public int a(String string, String string2) {
        ek ek2 = (ek)this.h.get(string);
        if (ek2 != null && ek2.a != null && !ek2.d) {
            if (ek2.c < 0) {
                ek2.c = this.a(ek2.a);
            } else {
                this.a(ek2.a, ek2.c);
            }
            ek2.d = true;
        }
        if (ek2 == null || ek2.c < 0) {
            if (string2 == null) {
                return -1;
            }
            return this.b(string2);
        }
        return ek2.c;
    }

    public ek a(String string, nf nf2) {
        ek ek2 = (ek)this.h.get(string);
        if (ek2 == null) {
            this.h.put(string, new ek(string, nf2));
        } else {
            ++ek2.b;
        }
        return ek2;
    }

    public void c(String string) {
        ek ek2 = (ek)this.h.get(string);
        if (ek2 != null) {
            --ek2.b;
            if (ek2.b == 0) {
                if (ek2.c >= 0) {
                    this.a(ek2.c);
                }
                this.h.remove(string);
            }
        }
    }

    public void a(aw aw2) {
        this.g.add(aw2);
        aw2.a();
    }

    public void a() {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        int n9;
        int n10;
        int n11;
        aw aw2;
        int n12;
        for (n12 = 0; n12 < this.g.size(); ++n12) {
            aw2 = (aw)this.g.get(n12);
            aw2.c = this.i.g;
            aw2.a();
            this.f.clear();
            this.f.put(aw2.a);
            this.f.position(0).limit(aw2.a.length);
            aw2.a(this);
            for (n11 = 0; n11 < aw2.e; ++n11) {
                for (n10 = 0; n10 < aw2.e; ++n10) {
                    GL11.glTexSubImage2D((int)3553, (int)0, (int)(aw2.b % 16 * 16 + n11 * 16), (int)(aw2.b / 16 * 16 + n10 * 16), (int)16, (int)16, (int)6408, (int)5121, (ByteBuffer)this.f);
                    if (!a) continue;
                    for (n9 = 1; n9 <= 4; ++n9) {
                        n8 = 16 >> n9 - 1;
                        n7 = 16 >> n9;
                        for (n6 = 0; n6 < n7; ++n6) {
                            for (n5 = 0; n5 < n7; ++n5) {
                                n4 = this.f.getInt((n6 * 2 + 0 + (n5 * 2 + 0) * n8) * 4);
                                n3 = this.f.getInt((n6 * 2 + 1 + (n5 * 2 + 0) * n8) * 4);
                                n2 = this.f.getInt((n6 * 2 + 1 + (n5 * 2 + 1) * n8) * 4);
                                int n13 = this.f.getInt((n6 * 2 + 0 + (n5 * 2 + 1) * n8) * 4);
                                int n14 = this.a(this.a(n4, n3), this.a(n2, n13));
                                this.f.putInt((n6 + n5 * n7) * 4, n14);
                            }
                        }
                        GL11.glTexSubImage2D((int)3553, (int)n9, (int)(aw2.b % 16 * n7), (int)(aw2.b / 16 * n7), (int)n7, (int)n7, (int)6408, (int)5121, (ByteBuffer)this.f);
                    }
                }
            }
        }
        for (n12 = 0; n12 < this.g.size(); ++n12) {
            aw2 = (aw)this.g.get(n12);
            if (aw2.d <= 0) continue;
            this.f.clear();
            this.f.put(aw2.a);
            this.f.position(0).limit(aw2.a.length);
            GL11.glBindTexture((int)3553, (int)aw2.d);
            GL11.glTexSubImage2D((int)3553, (int)0, (int)0, (int)0, (int)16, (int)16, (int)6408, (int)5121, (ByteBuffer)this.f);
            if (!a) continue;
            for (n11 = 1; n11 <= 4; ++n11) {
                n10 = 16 >> n11 - 1;
                n9 = 16 >> n11;
                for (n8 = 0; n8 < n9; ++n8) {
                    for (n7 = 0; n7 < n9; ++n7) {
                        n6 = this.f.getInt((n8 * 2 + 0 + (n7 * 2 + 0) * n10) * 4);
                        n5 = this.f.getInt((n8 * 2 + 1 + (n7 * 2 + 0) * n10) * 4);
                        n4 = this.f.getInt((n8 * 2 + 1 + (n7 * 2 + 1) * n10) * 4);
                        n3 = this.f.getInt((n8 * 2 + 0 + (n7 * 2 + 1) * n10) * 4);
                        n2 = this.a(this.a(n6, n5), this.a(n4, n3));
                        this.f.putInt((n8 + n7 * n9) * 4, n2);
                    }
                }
                GL11.glTexSubImage2D((int)3553, (int)n11, (int)0, (int)0, (int)n9, (int)n9, (int)6408, (int)5121, (ByteBuffer)this.f);
            }
        }
    }

    private int a(int n2, int n3) {
        int n4 = (n2 & 0xFF000000) >> 24 & 0xFF;
        int n5 = (n3 & 0xFF000000) >> 24 & 0xFF;
        return (n4 + n5 >> 1 << 24) + ((n2 & 0xFEFEFE) + (n3 & 0xFEFEFE) >> 1);
    }

    private int b(int n2, int n3) {
        int n4 = (n2 & 0xFF000000) >> 24 & 0xFF;
        int n5 = (n3 & 0xFF000000) >> 24 & 0xFF;
        int n6 = 255;
        if (n4 + n5 == 0) {
            n4 = 1;
            n5 = 1;
            n6 = 0;
        }
        int n7 = (n2 >> 16 & 0xFF) * n4;
        int n8 = (n2 >> 8 & 0xFF) * n4;
        int n9 = (n2 & 0xFF) * n4;
        int n10 = (n3 >> 16 & 0xFF) * n5;
        int n11 = (n3 >> 8 & 0xFF) * n5;
        int n12 = (n3 & 0xFF) * n5;
        int n13 = (n7 + n10) / (n4 + n5);
        int n14 = (n8 + n11) / (n4 + n5);
        int n15 = (n9 + n12) / (n4 + n5);
        return n6 << 24 | n13 << 16 | n14 << 8 | n15;
    }

    public void b() {
        BufferedImage bufferedImage;
        i i2 = this.l.a;
        Iterator<Object> iterator = this.d.keySet().iterator();
        while (iterator.hasNext()) {
            int n2 = (Integer)iterator.next();
            bufferedImage = (BufferedImage)this.d.get(n2);
            this.a(bufferedImage, n2);
        }
        for (ek ek2 : this.h.values()) {
            ek2.d = false;
        }
        for (String string : this.b.keySet()) {
            try {
                if (string.startsWith("##")) {
                    bufferedImage = this.c(this.a(i2.a(string.substring(2))));
                } else if (string.startsWith("%clamp%")) {
                    this.j = true;
                    bufferedImage = this.a(i2.a(string.substring(7)));
                } else if (string.startsWith("%blur%")) {
                    this.k = true;
                    bufferedImage = this.a(i2.a(string.substring(6)));
                } else {
                    bufferedImage = this.a(i2.a(string));
                }
                int n3 = (Integer)this.b.get(string);
                this.a(bufferedImage, n3);
                this.k = false;
                this.j = false;
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
        for (String string : this.c.keySet()) {
            try {
                if (string.startsWith("##")) {
                    bufferedImage = this.c(this.a(i2.a(string.substring(2))));
                } else if (string.startsWith("%clamp%")) {
                    this.j = true;
                    bufferedImage = this.a(i2.a(string.substring(7)));
                } else if (string.startsWith("%blur%")) {
                    this.k = true;
                    bufferedImage = this.a(i2.a(string.substring(6)));
                } else {
                    bufferedImage = this.a(i2.a(string));
                }
                this.a(bufferedImage, (int[])this.c.get(string));
                this.k = false;
                this.j = false;
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    }

    private BufferedImage a(InputStream inputStream) {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        inputStream.close();
        return bufferedImage;
    }

    public void b(int n2) {
        if (n2 < 0) {
            return;
        }
        GL11.glBindTexture((int)3553, (int)n2);
    }
}

