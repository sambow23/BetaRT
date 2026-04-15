/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL11
 */
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class hj {
    private static DateFormat a = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static ByteBuffer b;
    private static byte[] c;
    private static int[] d;

    public static String a(File file, int n2, int n3) {
        try {
            File file2;
            File file3 = new File(file, "screenshots");
            file3.mkdir();
            if (b == null || b.capacity() < n2 * n3) {
                b = BufferUtils.createByteBuffer((int)(n2 * n3 * 3));
            }
            if (d == null || d.length < n2 * n3 * 3) {
                c = new byte[n2 * n3 * 3];
                d = new int[n2 * n3];
            }
            GL11.glPixelStorei((int)3333, (int)1);
            GL11.glPixelStorei((int)3317, (int)1);
            b.clear();
            GL11.glReadPixels((int)0, (int)0, (int)n2, (int)n3, (int)6407, (int)5121, (ByteBuffer)b);
            b.clear();
            String string = "" + a.format(new Date());
            int n4 = 1;
            while ((file2 = new File(file3, string + (n4 == 1 ? "" : "_" + n4) + ".png")).exists()) {
                ++n4;
            }
            b.get(c);
            for (int i2 = 0; i2 < n2; ++i2) {
                for (int i3 = 0; i3 < n3; ++i3) {
                    int n5;
                    int n6 = i2 + (n3 - i3 - 1) * n2;
                    int n7 = c[n6 * 3 + 0] & 0xFF;
                    int n8 = c[n6 * 3 + 1] & 0xFF;
                    int n9 = c[n6 * 3 + 2] & 0xFF;
                    hj.d[i2 + i3 * n2] = n5 = 0xFF000000 | n7 << 16 | n8 << 8 | n9;
                }
            }
            BufferedImage bufferedImage = new BufferedImage(n2, n3, 1);
            bufferedImage.setRGB(0, 0, n2, n3, d, 0, n2);
            ImageIO.write((RenderedImage)bufferedImage, "png", file2);
            return "Saved screenshot as " + file2.getName();
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return "Failed to save: " + exception;
        }
    }
}

