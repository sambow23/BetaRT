/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class kh
implements gt {
    final /* synthetic */ Minecraft a;

    public kh(Minecraft minecraft) {
        this.a = minecraft;
    }

    public String a(String string) {
        return String.format(string, Keyboard.getKeyName((int)this.a.z.r.b));
    }
}

