/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class pa
extends i {
    private int e = -1;
    private BufferedImage f;

    public pa() {
        this.a = "Default";
        this.b = "The default look of Minecraft";
        try {
            this.f = ImageIO.read(pa.class.getResource("/pack.png"));
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void b(Minecraft minecraft) {
        if (this.f != null) {
            minecraft.p.a(this.e);
        }
    }

    public void c(Minecraft minecraft) {
        if (this.f != null && this.e < 0) {
            this.e = minecraft.p.a(this.f);
        }
        if (this.f != null) {
            minecraft.p.b(this.e);
        } else {
            GL11.glBindTexture((int)3553, (int)minecraft.p.b("/gui/unknown_pack.png"));
        }
    }
}

