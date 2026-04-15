/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class vp
extends i {
    private ZipFile e;
    private int f = -1;
    private BufferedImage g;
    private File h;

    public vp(File file) {
        this.a = file.getName();
        this.h = file;
    }

    private String b(String string) {
        if (string != null && string.length() > 34) {
            string = string.substring(0, 34);
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void a(Minecraft minecraft) {
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            zipFile = new ZipFile(this.h);
            try {
                inputStream = zipFile.getInputStream(zipFile.getEntry("pack.txt"));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                this.b = this.b(bufferedReader.readLine());
                this.c = this.b(bufferedReader.readLine());
                bufferedReader.close();
                inputStream.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                inputStream = zipFile.getInputStream(zipFile.getEntry("pack.png"));
                this.g = ImageIO.read(inputStream);
                inputStream.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            zipFile.close();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            }
            catch (Exception exception) {}
            try {
                zipFile.close();
            }
            catch (Exception exception) {}
        }
    }

    public void b(Minecraft minecraft) {
        if (this.g != null) {
            minecraft.p.a(this.f);
        }
        this.b();
    }

    public void c(Minecraft minecraft) {
        if (this.g != null && this.f < 0) {
            this.f = minecraft.p.a(this.g);
        }
        if (this.g != null) {
            minecraft.p.b(this.f);
        } else {
            GL11.glBindTexture((int)3553, (int)minecraft.p.b("/gui/unknown_pack.png"));
        }
    }

    public void a() {
        try {
            this.e = new ZipFile(this.h);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void b() {
        try {
            this.e.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.e = null;
    }

    public InputStream a(String string) {
        try {
            ZipEntry zipEntry = this.e.getEntry(string.substring(1));
            if (zipEntry != null) {
                return this.e.getInputStream(zipEntry);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return i.class.getResourceAsStream(string);
    }
}

