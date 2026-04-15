/*
 * Decompiled with CFR 0.152.
 */
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import net.minecraft.client.Minecraft;

public final class kj
extends WindowAdapter {
    final /* synthetic */ Minecraft a;
    final /* synthetic */ Thread b;

    public kj(Minecraft minecraft, Thread thread) {
        this.a = minecraft;
        this.b = thread;
    }

    public void windowClosing(WindowEvent windowEvent) {
        this.a.f();
        try {
            this.b.join();
        }
        catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        System.exit(0);
    }
}

